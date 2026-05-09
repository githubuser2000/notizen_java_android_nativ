package de.notizen.android.core;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Dependency-free parser for visible RTF content plus selected RichTextBox metadata. */
final class RtfContentParser {
    private static final Charset CP1252 = Charset.forName("windows-1252");
    private static final String SOFT_LINE_BREAK = "\u2028";
    private static final int MAX_EDITOR_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Pattern HYPERLINK_FIELD = Pattern.compile("HYPERLINK\\s+(?:\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"|([^\\\\{}\\s]+))", Pattern.CASE_INSENSITIVE);

    private enum SpecialKind { PICT, FIELD, OBJECT }

    private static final class SpecialGroup {
        final SpecialKind kind;
        final int start;
        final int end;
        final String raw;
        final String pictGroup;
        SpecialGroup(SpecialKind kind, int start, int end, String raw, String pictGroup) {
            this.kind = kind; this.start = start; this.end = end; this.raw = raw; this.pictGroup = pictGroup;
        }
    }

    private static final class State {
        RtfTextStyle style = new RtfTextStyle();
        boolean skip;
        boolean starDestination;
        int uc = 1;
        int skipChars;
        State copy() {
            State s = new State();
            s.style = style.copy();
            s.skip = skip;
            s.starDestination = starDestination;
            s.uc = uc;
            s.skipChars = skipChars;
            return s;
        }
    }

    private final String src;
    private final Charset ansiCharset;
    private final Map<Integer, String> fonts;
    private final Map<Integer, String> colors;
    private final ArrayDeque<State> stack = new ArrayDeque<>();
    private final ArrayList<RtfTextSegment> segments = new ArrayList<>();
    private final ArrayList<RtfContentPart> parts = new ArrayList<>();
    private State state = new State();
    private int i;

    private RtfContentParser(String src) {
        this.src = src == null ? "" : src;
        this.ansiCharset = detectAnsiCharset(this.src);
        this.fonts = extractFontTable(this.src);
        this.colors = extractColorTable(this.src);
    }

    static List<RtfTextSegment> textSegments(String rtf) {
        if (rtf == null || rtf.isEmpty()) return new ArrayList<>();
        if (!RtfUtils.looksLikeRtf(rtf)) {
            ArrayList<RtfTextSegment> out = new ArrayList<>();
            out.add(new RtfTextSegment(rtf, new RtfTextStyle()));
            return out;
        }
        RtfContentParser parser = new RtfContentParser(rtf);
        parser.parse();
        return parser.segments;
    }

    static List<RtfContentPart> contentParts(String rtf) {
        if (rtf == null || rtf.isEmpty()) return new ArrayList<>();
        if (!RtfUtils.looksLikeRtf(rtf)) {
            ArrayList<RtfContentPart> out = new ArrayList<>();
            out.add(new RtfContentPart(rtf, "", new RtfTextStyle()));
            return out;
        }
        RtfContentParser parser = new RtfContentParser(rtf);
        parser.parse();
        return parser.parts;
    }

    private void parse() {
        while (i < src.length()) {
            SpecialGroup special = specialGroupAt(src, i);
            if (special != null && !state.skip) {
                appendSpecial(special);
                i = special.end + 1;
                continue;
            }
            char c = src.charAt(i++);
            if (c == '{') {
                stack.push(state.copy());
            } else if (c == '}') {
                state = stack.isEmpty() ? new State() : stack.pop();
            } else if (c == '\\') {
                parseControl();
            } else if (c != '\r' && c != '\n') {
                appendChar(c);
            }
        }
        normalizeSegmentsAndParts();
    }

    private void appendSpecial(SpecialGroup special) {
        if (special.kind == SpecialKind.FIELD) {
            String display = extractFieldResultText(special.raw);
            Matcher m = HYPERLINK_FIELD.matcher(special.raw == null ? "" : special.raw);
            if (m.find()) {
                String url = m.group(1) != null ? m.group(1) : (m.group(2) == null ? "" : m.group(2));
                url = url.replace("\\\"", "\"").replace("\\\\", "\\");
                appendVisible(display, special.raw, new RtfHyperlink(display, special.raw, styleNow(), url));
            } else {
                appendVisible(display, special.raw, new RtfField(display, special.raw, styleNow()));
            }
            return;
        }
        if (special.kind == SpecialKind.OBJECT) {
            String className = extractObjectClassName(special.raw);
            appendVisible(RtfUtils.LEGACY_OBJECT_PLACEHOLDER, special.raw, new RtfObject(RtfUtils.LEGACY_OBJECT_PLACEHOLDER, special.raw, styleNow(), className));
            return;
        }
        if (special.kind == SpecialKind.PICT) {
            String raw = special.pictGroup == null ? special.raw : special.pictGroup;
            int estimatedBytes = estimatePictPayloadBytes(raw, MAX_EDITOR_IMAGE_BYTES + 1);
            if (estimatedBytes > MAX_EDITOR_IMAGE_BYTES) {
                appendVisible(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, raw, new RtfImagePart(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, raw, styleNow(), pictMimeHint(raw), new byte[0], 0, 0));
                return;
            }
            try {
                RtfUtils.RtfImage image = parsePictGroup(raw);
                String mime = image == null ? pictMimeHint(raw) : image.mimeType;
                byte[] data = image == null ? new byte[0] : image.data;
                int widthTwips = image == null ? 0 : image.widthTwips;
                int heightTwips = image == null ? 0 : image.heightTwips;
                appendVisible(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, raw, new RtfImagePart(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, raw, styleNow(), mime, data, widthTwips, heightTwips));
            } catch (OutOfMemoryError | RuntimeException ex) {
                appendVisible(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, raw, new RtfImagePart(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, raw, styleNow(), pictMimeHint(raw), new byte[0], 0, 0));
            }
        }
    }


    private static int estimatePictPayloadBytes(String group, int stopAboveBytes) {
        if (group == null || group.isEmpty()) return 0;
        int hexDigits = 0;
        int cap = Math.max(2, stopAboveBytes) * 2 + 2;
        for (int i = 0; i < group.length(); i++) {
            char c = group.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= group.length()) break;
                char control = group.charAt(i);
                if (control == '\'') {
                    i += 2;
                    continue;
                }
                if (Character.isLetter(control)) {
                    while (i < group.length() && Character.isLetter(group.charAt(i))) i++;
                    if (i < group.length() && (group.charAt(i) == '-' || group.charAt(i) == '+')) i++;
                    while (i < group.length() && Character.isDigit(group.charAt(i))) i++;
                    if (i < group.length() && group.charAt(i) == ' ') {
                        // Control-word delimiter; consumed by increment at loop end.
                    } else {
                        i--;
                    }
                    continue;
                }
                continue;
            }
            if (isHexDigit(c)) {
                hexDigits++;
                if (hexDigits > cap) return stopAboveBytes + 1;
            }
        }
        return hexDigits / 2;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static String pictMimeHint(String group) {
        String g = group == null ? "" : group;
        if (g.contains("\\pngblip")) return "image/png";
        if (g.contains("\\jpegblip") || g.contains("\\jpgblip")) return "image/jpeg";
        if (g.contains("\\dibitmap") || g.contains("\\wbitmap")) return "image/bmp";
        if (g.contains("\\emfblip")) return "image/x-emf";
        if (g.contains("\\wmetafile")) return "image/wmf";
        return "";
    }

    private void appendVisible(String text, String rawRtf, RtfContentPart part) {
        String visible = text == null ? "" : text;
        if (!visible.isEmpty()) appendText(visible, false);
        parts.add(part);
    }

    private RtfTextStyle styleNow() { return state.style.copy(); }

    private void parseControl() {
        if (i >= src.length()) return;
        char c = src.charAt(i++);
        if (c == '\\' || c == '{' || c == '}') { appendChar(c); return; }
        if (c == '\'') { parseHexByte(); return; }
        if (!Character.isLetter(c)) { handleControlSymbol(c); return; }
        int start = i - 1;
        while (i < src.length() && Character.isLetter(src.charAt(i))) i++;
        String word = src.substring(start, i).toLowerCase(Locale.ROOT);
        boolean negative = false;
        if (i < src.length() && (src.charAt(i) == '-' || src.charAt(i) == '+')) { negative = src.charAt(i) == '-'; i++; }
        int numberStart = i;
        while (i < src.length() && Character.isDigit(src.charAt(i))) i++;
        Integer param = null;
        if (numberStart < i) {
            try {
                int value = Integer.parseInt(src.substring(numberStart, i));
                param = negative ? -value : value;
            } catch (NumberFormatException ignored) {}
        }
        if (i < src.length() && src.charAt(i) == ' ') i++;
        handleControlWord(word, param);
    }

    private void parseHexByte() {
        int end = Math.min(i + 2, src.length());
        String hex = src.substring(i, end);
        i = end;
        if (state.skip || state.skipChars > 0) { if (state.skipChars > 0) state.skipChars--; return; }
        try {
            int value = Integer.parseInt(hex, 16);
            appendText(new String(new byte[]{(byte) value}, ansiCharset));
        } catch (Exception ignored) {}
    }

    private void handleControlSymbol(char c) {
        if (c == '*') { state.starDestination = true; return; }
        if (state.skip) return;
        switch (c) {
            case '~': appendText("\u00a0"); break;
            case '-': appendText("\u00ad"); break;
            case '_': appendText("\u2011"); break;
            default: break;
        }
    }

    private void handleControlWord(String word, Integer param) {
        if (isDestinationToSkip(word) || (state.starDestination && isLikelyUnknownDestination(word))) {
            state.skip = true;
            return;
        }
        state.starDestination = false;
        if (state.skip && !"uc".equals(word)) return;

        switch (word) {
            case "b": state.style.bold = on(param); break;
            case "i": state.style.italic = on(param); break;
            case "ul": case "ulw": case "ulwave": case "uld": case "uldash": case "uldashd": case "uldashdd": case "uldb": case "ulth":
                state.style.underline = on(param); break;
            case "ulnone": state.style.underline = false; break;
            case "strike": case "striked": state.style.strike = on(param); break;
            case "v": state.style.hidden = on(param); break;
            case "caps": state.style.allCaps = on(param); break;
            case "scaps": state.style.smallCaps = on(param); break;
            case "cf": state.style.fgColor = color(param); break;
            case "highlight": case "cbpat": case "chcbpat": state.style.bgColor = color(param); break;
            case "f": state.style.fontFamily = param == null ? null : fonts.get(param); break;
            case "fs": state.style.fontSizeHalfPoints = param == null || param <= 0 ? null : param; break;
            case "plain": state.style.resetCharacterStyle(); break;
            case "pard": state.style.resetParagraphStyle(); break;
            case "ql": state.style.align = null; break;
            case "qc": state.style.align = "center"; break;
            case "qr": state.style.align = "right"; break;
            case "qj": state.style.align = "justify"; break;
            case "li": state.style.leftIndentTwips = param == null ? 0 : Math.max(0, param); break;
            case "ri": state.style.rightIndentTwips = param == null ? 0 : Math.max(0, param); break;
            case "fi": state.style.firstIndentTwips = param == null ? 0 : param; break;
            case "sb": state.style.spaceBeforeTwips = param == null ? 0 : Math.max(0, param); break;
            case "sa": state.style.spaceAfterTwips = param == null ? 0 : Math.max(0, param); break;
            case "sl": state.style.lineSpacingTwips = param == null ? 0 : Math.abs(param); break;
            case "slmult": state.style.lineSpacingMultiple = param != null && param != 0; break;
            case "rtlpar": case "rtlch": state.style.direction = "rtl"; break;
            case "ltrpar": case "ltrch": state.style.direction = "ltr"; break;
            case "up": state.style.vertical = (param == null || param == 0) ? null : "super"; break;
            case "super": state.style.vertical = (param != null && param == 0) ? null : "super"; break;
            case "dn": state.style.vertical = (param == null || param == 0) ? null : "sub"; break;
            case "sub": state.style.vertical = (param != null && param == 0) ? null : "sub"; break;
            case "nosupersub": state.style.vertical = null; break;
            case "expnd": state.style.letterSpacingTwips = param == null ? 0 : param * 5; break;
            case "expndtw": state.style.letterSpacingTwips = param == null ? 0 : param; break;
            case "par": appendText("\n"); break;
            case "line": appendText(SOFT_LINE_BREAK); break;
            case "tab": appendText("\t"); break;
            case "emdash": appendText("—"); break;
            case "endash": appendText("–"); break;
            case "emspace": appendText(" "); break;
            case "enspace": appendText(" "); break;
            case "qmspace": appendText(" "); break;
            case "bullet": appendText("•"); break;
            case "lquote": appendText("‘"); break;
            case "rquote": appendText("’"); break;
            case "ldblquote": appendText("“"); break;
            case "rdblquote": appendText("”"); break;
            case "page": appendText("\f"); break;
            case "sect": case "column": appendText("\n"); break;
            case "u":
                if (param != null) {
                    int code = param;
                    if (code < 0) code += 65536;
                    appendChar((char) code);
                    state.skipChars = Math.max(0, state.uc);
                }
                break;
            case "uc": if (param != null && param >= 0 && param <= 16) state.uc = param; break;
            case "bin": if (param != null && param > 0) i = Math.min(src.length(), i + param); break;
            case "cell": case "nestcell": appendText("\t"); break;
            case "row": appendText("\n"); break;
            default: break;
        }
    }

    private boolean on(Integer param) { return param == null || param != 0; }
    private String color(Integer param) { return param == null || param <= 0 ? null : colors.get(param); }

    private void appendChar(char c) {
        if (state.skip) return;
        if (state.skipChars > 0) { state.skipChars--; return; }
        appendText(String.valueOf(c));
    }

    private void appendText(String text) {
        appendText(text, true);
    }

    private void appendText(String text, boolean addToContentParts) {
        if (state.skip || text == null || text.isEmpty()) return;
        if (state.skipChars > 0) {
            int drop = Math.min(state.skipChars, text.length());
            state.skipChars -= drop;
            text = text.substring(drop);
        }
        if (text.isEmpty()) return;
        String visible = state.style.allCaps ? text.toUpperCase(Locale.ROOT) : text;
        RtfTextStyle snapshot = styleNow();
        if (!segments.isEmpty()) {
            RtfTextSegment last = segments.get(segments.size() - 1);
            if (last.style.equals(state.style)) {
                segments.set(segments.size() - 1, new RtfTextSegment(last.text + visible, last.style));
            } else {
                segments.add(new RtfTextSegment(visible, snapshot));
            }
        } else {
            segments.add(new RtfTextSegment(visible, snapshot));
        }
        if (addToContentParts) appendTextPart(visible, snapshot);
    }

    private void appendTextPart(String visible, RtfTextStyle snapshot) {
        if (visible == null || visible.isEmpty()) return;
        String text = visible.replace(SOFT_LINE_BREAK, "\n");
        if (text.isEmpty()) return;
        if (!parts.isEmpty()) {
            RtfContentPart last = parts.get(parts.size() - 1);
            if (!(last instanceof RtfImagePart) && !(last instanceof RtfHyperlink) && !(last instanceof RtfField) && !(last instanceof RtfObject)
                    && last.rtf.isEmpty() && last.style.equals(snapshot)) {
                parts.set(parts.size() - 1, new RtfContentPart(last.text + text, "", last.style));
                return;
            }
        }
        parts.add(new RtfContentPart(text, "", snapshot));
    }

    private void normalizeSegmentsAndParts() {
        for (int idx = 0; idx < segments.size(); idx++) {
            RtfTextSegment s = segments.get(idx);
            String text = s.text.replace(SOFT_LINE_BREAK, "\n");
            if (!text.equals(s.text)) segments.set(idx, new RtfTextSegment(text, s.style));
        }
    }

    private static SpecialGroup specialGroupAt(String src, int index) {
        if (src == null || index < 0 || index >= src.length()) return null;
        if (src.startsWith("{\\field", index)) {
            int end = RtfUtils.findGroupEnd(src, index);
            if (end >= 0) return new SpecialGroup(SpecialKind.FIELD, index, end, src.substring(index, end + 1), null);
        }
        if (src.startsWith("{\\object", index)) {
            int end = RtfUtils.findGroupEnd(src, index);
            if (end >= 0) return new SpecialGroup(SpecialKind.OBJECT, index, end, src.substring(index, end + 1), null);
        }
        if (src.startsWith("{\\pict", index) || src.startsWith("{\\*\\shppict", index) || src.startsWith("{\\nonshppict", index)) {
            int end = RtfUtils.findGroupEnd(src, index);
            if (end < 0) return null;
            String raw = src.substring(index, end + 1);
            String pict = raw;
            if (!raw.startsWith("{\\pict")) {
                int inner = raw.indexOf("{\\pict");
                if (inner >= 0) {
                    int innerEnd = RtfUtils.findGroupEnd(raw, inner);
                    if (innerEnd >= 0) pict = raw.substring(inner, innerEnd + 1);
                }
            }
            return new SpecialGroup(SpecialKind.PICT, index, end, raw, pict);
        }
        return null;
    }

    static Map<Integer, String> extractFontTable(String rtf) {
        LinkedHashMap<Integer, String> fonts = new LinkedHashMap<>();
        int start = rtf == null ? -1 : rtf.indexOf("{\\fonttbl");
        if (start < 0) return fonts;
        int end = RtfUtils.findGroupEnd(rtf, start);
        if (end < 0) return fonts;
        String tbl = rtf.substring(start, end + 1);
        int i = "{\\fonttbl".length();
        while (i < tbl.length()) {
            int groupStart = tbl.indexOf("{\\f", i);
            if (groupStart < 0) break;
            int digit = groupStart + 3;
            if (digit >= tbl.length() || !Character.isDigit(tbl.charAt(digit))) {
                i = groupStart + 2;
                continue;
            }
            int groupEnd = RtfUtils.findGroupEnd(tbl, groupStart);
            if (groupEnd < 0) break;
            parseFontGroup(tbl.substring(groupStart, groupEnd + 1), fonts);
            i = groupEnd + 1;
        }
        if (fonts.isEmpty()) {
            Matcher m = Pattern.compile("\\\\f(\\d+)(?:[^;{}]|\\{[^{}]*\\})*?\\s([^;{}]+);").matcher(tbl);
            while (m.find()) {
                try {
                    int idx = Integer.parseInt(m.group(1));
                    String name = cleanFontName(m.group(2));
                    if (!name.isEmpty()) fonts.put(idx, name);
                } catch (Exception ignored) {}
            }
        }
        return fonts;
    }

    private static void parseFontGroup(String group, Map<Integer, String> fonts) {
        Matcher id = Pattern.compile("\\\\f(\\d+)").matcher(group);
        if (!id.find()) return;
        try {
            int idx = Integer.parseInt(id.group(1));
            String body = group.substring(1, group.length() - 1);
            body = body.replaceAll("(?is)\\{\\\\\\*\\\\falt[^}]*\\}", "");
            body = body.replaceAll("\\\\'[0-9a-fA-F]{2}", " ");
            body = body.replaceAll("\\\\[a-zA-Z]+-?\\d* ?", "");
            body = body.replaceAll("\\\\[^a-zA-Z]", "");
            body = body.replace(';', ' ').replace('{', ' ').replace('}', ' ');
            String name = cleanFontName(body);
            if (!name.isEmpty()) fonts.put(idx, name);
        } catch (Exception ignored) {}
    }

    private static String cleanFontName(String raw) {
        return (raw == null ? "" : raw).replaceAll("\\\\'[0-9a-fA-F]{2}", " ").replaceAll("\\s+", " ").trim();
    }

    static Map<Integer, String> extractColorTable(String rtf) {
        LinkedHashMap<Integer, String> colors = new LinkedHashMap<>();
        int start = rtf == null ? -1 : rtf.indexOf("{\\colortbl");
        if (start < 0) return colors;
        int end = RtfUtils.findGroupEnd(rtf, start);
        if (end < 0) return colors;
        String body = rtf.substring(start + "{\\colortbl".length(), end);
        int index = 0;
        String[] entries = body.split(";", -1);
        for (int entryIndex = 0; entryIndex < entries.length; entryIndex++) {
            String e = entries[entryIndex].trim();
            if (e.isEmpty()) {
                if (entryIndex == entries.length - 1 && body.endsWith(";")) continue;
                colors.put(index, "");
                index++;
                continue;
            }
            Matcher r = Pattern.compile("\\\\red(-?\\d+)").matcher(e);
            Matcher g = Pattern.compile("\\\\green(-?\\d+)").matcher(e);
            Matcher b = Pattern.compile("\\\\blue(-?\\d+)").matcher(e);
            if (r.find() && g.find() && b.find()) {
                int target = index == 0 && !colors.containsKey(0) ? 1 : index;
                colors.put(target, String.format(Locale.ROOT, "#%02x%02x%02x", clamp255(toInt(r.group(1), 0)), clamp255(toInt(g.group(1), 0)), clamp255(toInt(b.group(1), 0))));
                index = target + 1;
            } else {
                index++;
            }
        }
        if (!colors.containsKey(0)) colors.put(0, "");
        return colors;
    }

    private static Charset detectAnsiCharset(String rtf) {
        String marker = "\\ansicpg";
        int pos = rtf == null ? -1 : rtf.indexOf(marker);
        if (pos < 0) return CP1252;
        int start = pos + marker.length();
        int end = start;
        while (end < rtf.length() && Character.isDigit(rtf.charAt(end))) end++;
        if (end == start) return CP1252;
        String codepage = rtf.substring(start, end);
        try { return Charset.forName("windows-" + codepage); } catch (Exception ignored) {}
        try { return Charset.forName("cp" + codepage); } catch (Exception ignored) {}
        return CP1252;
    }

    private static boolean isLikelyUnknownDestination(String word) {
        return !("fldrslt".equals(word) || "pntext".equals(word) || "listtext".equals(word));
    }

    private static boolean isDestinationToSkip(String word) {
        switch (word) {
            case "fonttbl": case "colortbl": case "stylesheet": case "info": case "pict": case "object":
            case "objclass": case "objdata": case "datafield": case "generator": case "datastore": case "themedata":
            case "colorschememapping": case "listtable": case "listoverridetable": case "rsidtbl": case "fldinst":
                return true;
            default:
                return false;
        }
    }

    private static String extractFieldResultText(String group) {
        int start = group == null ? -1 : group.indexOf("{\\fldrslt");
        if (start < 0) return "";
        int end = RtfUtils.findGroupEnd(group, start);
        if (end < 0) return "";
        return RtfUtils.rtfToPlainText("{\\rtf1\\ansi " + group.substring(start, end + 1) + "}").trim();
    }

    private static String extractObjectClassName(String group) {
        if (group == null) return "";
        Matcher star = Pattern.compile("\\\\\\*\\\\objclass\\s+([^{}\\\\]+)").matcher(group);
        Matcher plain = Pattern.compile("\\\\objclass\\s+([^{}\\\\]+)").matcher(group);
        Matcher m = star.find() ? star : (plain.find() ? plain : null);
        if (m == null) return "";
        String raw = m.group(1).trim().replaceAll("[;}]+$", "");
        return raw.replaceAll("\\\\'[0-9a-fA-F]{2}", " ").trim();
    }

    private static RtfUtils.RtfImage parsePictGroup(String group) {
        if (group == null || group.isEmpty()) return null;
        String mimeType;
        String control;
        boolean wrapDib = false;
        if (group.contains("\\pngblip")) { mimeType = "image/png"; control = "pngblip"; }
        else if (group.contains("\\jpegblip") || group.contains("\\jpgblip")) { mimeType = "image/jpeg"; control = "jpegblip"; }
        else if (group.contains("\\dibitmap") || group.contains("\\wbitmap")) { mimeType = "image/bmp"; control = "dibitmap0"; wrapDib = true; }
        else return null;
        String scrubbed = group.replaceAll("\\\\'[0-9a-fA-F]{2}", " ").replaceAll("\\\\[a-zA-Z]+-?\\d* ?", " ");
        String hex = scrubbed.replaceAll("[^0-9a-fA-F]", "");
        if ((hex.length() & 1) == 1) hex = hex.substring(0, hex.length() - 1);
        if (hex.isEmpty()) return null;
        byte[] data = RtfUtils.hexToBytes(hex);
        if (wrapDib) {
            byte[] bmp = RtfUtils.dibToBmpBytes(data);
            if (bmp != null) data = bmp;
        }
        return new RtfUtils.RtfImage(mimeType, data, controlNumber(group, "picwgoal", 0), controlNumber(group, "pichgoal", 0), control, group);
    }

    private static int controlNumber(String group, String name, int fallback) {
        Matcher m = Pattern.compile("\\\\" + Pattern.quote(name) + "(-?\\d+)").matcher(group == null ? "" : group);
        if (!m.find()) return fallback;
        return toInt(m.group(1), fallback);
    }

    private static int toInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception e) { return fallback; } }
    private static int clamp255(int v) { return Math.max(0, Math.min(255, v)); }
}
