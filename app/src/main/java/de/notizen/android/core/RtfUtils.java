package de.notizen.android.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RtfUtils {
    private static final Charset CP1252 = Charset.forName("windows-1252");
    public static final String LEGACY_OBJECT_PLACEHOLDER = "[Objekt]";
    public static final String LEGACY_IMAGE_PLACEHOLDER = "[Bild]";
    private static final String RTF_SOFT_LINE_BREAK = "\u2028";
    private static final Pattern CONTROL_NUMBER = Pattern.compile("\\\\%s(-?\\d+)");
    private static final Pattern HYPERLINK_FIELD = Pattern.compile("HYPERLINK\\s+(?:\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"|([^\\\\{}\\s]+))", Pattern.CASE_INSENSITIVE);

    private RtfUtils() {}

    public static final class RtfImage {
        public final String mimeType;
        public final byte[] data;
        public final int widthTwips;
        public final int heightTwips;
        public final String rtfControl;
        public final String rawRtf;

        public RtfImage(String mimeType, byte[] data, int widthTwips, int heightTwips, String rtfControl, String rawRtf) {
            this.mimeType = mimeType == null ? "" : mimeType;
            this.data = data == null ? new byte[0] : data;
            this.widthTwips = widthTwips;
            this.heightTwips = heightTwips;
            this.rtfControl = rtfControl == null ? "" : rtfControl;
            this.rawRtf = rawRtf == null ? "" : rawRtf;
        }
    }

    public static boolean looksLikeRtf(String text) {
        if (text == null) return false;
        String s = text.trim();
        return s.startsWith("{\\rtf") || s.startsWith("\\rtf");
    }

    public static String plainTextToRtf(String text) {
        String src = text == null ? "" : text;
        StringBuilder out = new StringBuilder(src.length() + 80);
        out.append("{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fnil Microsoft Sans Serif;}}\\viewkind4\\uc1\\pard ");
        out.append(rtfEscapeMultiline(src));
        out.append("\\par}");
        return out.toString();
    }

    public static String rtfToPlainText(String rtf) {
        if (rtf == null || rtf.isEmpty()) return "";
        if (!looksLikeRtf(rtf)) return rtf;
        Parser parser = new Parser(rtf);
        return parser.parse();
    }

    public static String rtfToHtml(String rtf) {
        return RtfHtmlConverter.rtfToHtml(rtf);
    }

    public static String htmlToRtf(String html) {
        return RtfHtmlConverter.htmlToRtf(html);
    }

    public static String rtfToHtmlDocument(String rtf) {
        return RtfHtmlConverter.rtfToHtmlDocument(rtf);
    }

    public static String rtfToDesktopHtml(String rtf) {
        return RtfHtmlConverter.rtfToDesktopHtml(rtf);
    }

    public static List<RtfTextSegment> rtfToTextSegments(String rtf) {
        return RtfContentParser.textSegments(rtf);
    }

    public static List<RtfContentPart> rtfToContentParts(String rtf) {
        return RtfContentParser.contentParts(rtf);
    }

    public static Map<Integer, String> extractFontTable(String rtf) {
        return RtfContentParser.extractFontTable(rtf);
    }

    public static Map<Integer, String> extractColorTable(String rtf) {
        return RtfContentParser.extractColorTable(rtf);
    }

    public static List<RtfImage> extractImages(String rtf) {
        ArrayList<RtfImage> images = new ArrayList<>();
        if (rtf == null || rtf.isEmpty()) return images;
        int i = 0;
        while (i < rtf.length()) {
            SpecialGroup group = specialGroupAt(rtf, i);
            if (group != null && group.kind == SpecialKind.PICT) {
                RtfImage image = parsePictGroup(group.pictGroup == null ? group.raw : group.pictGroup);
                if (image != null) images.add(image);
                i = group.end + 1;
            } else {
                i++;
            }
        }
        return images;
    }

    /**
     * Extract only a small preview-safe subset of embedded RTF images.
     * This is used by Android widgets, where loading several multi-megabyte
     * picture groups on the UI path could otherwise make the app unstable.
     */
    public static List<RtfImage> extractImagesLimited(String rtf, int maxImages, int maxBytesEach, int maxTotalBytes) {
        ArrayList<RtfImage> images = new ArrayList<>();
        if (rtf == null || rtf.isEmpty() || maxImages <= 0 || maxBytesEach <= 0 || maxTotalBytes <= 0) return images;
        int total = 0;
        int i = 0;
        while (i < rtf.length() && images.size() < maxImages && total < maxTotalBytes) {
            SpecialGroup group = specialGroupAt(rtf, i);
            if (group != null && group.kind == SpecialKind.PICT) {
                String pict = group.pictGroup == null ? group.raw : group.pictGroup;
                int estimate = estimatePictPayloadBytes(pict, Math.min(maxBytesEach, maxTotalBytes - total) + 1);
                if (estimate > 0 && estimate <= maxBytesEach && total + estimate <= maxTotalBytes) {
                    RtfImage image = parsePictGroup(pict);
                    if (image != null && image.data.length <= maxBytesEach && total + image.data.length <= maxTotalBytes) {
                        images.add(image);
                        total += image.data.length;
                    }
                }
                i = group.end + 1;
            } else {
                i++;
            }
        }
        return images;
    }

    public static int countImages(String rtf) {
        return extractImages(rtf).size();
    }

    /** Count embedded RTF picture groups without decoding their hex payload. */
    public static int countImageGroups(String rtf) {
        if (rtf == null || rtf.isEmpty()) return 0;
        int count = 0;
        int i = 0;
        while (i < rtf.length()) {
            SpecialGroup group = specialGroupAt(rtf, i);
            if (group != null && group.kind == SpecialKind.PICT) {
                count++;
                i = group.end + 1;
            } else {
                i++;
            }
        }
        return count;
    }

    /**
     * Return a safe body fragment for combined RTF export.
     *
     * RichTextBox-based Notizen.NET pasted each note body into a temporary editor.
     * On Android we cannot do that, so this keeps readable text plus the legacy
     * RTF objects that otherwise vanish: pictures, hyperlink/generic fields and
     * OLE/object groups. Ordinary character styling is intentionally flattened
     * rather than emitting incompatible font/color table indexes.
     */
    public static String rtfToEmbeddableContent(String rtf) {
        if (rtf == null || rtf.isEmpty()) return "";
        if (!looksLikeRtf(rtf)) return rtfEscapeMultiline(rtf);
        String src = rtf;
        StringBuilder out = new StringBuilder(src.length());
        int lastPlain = 0;
        int i = 0;
        while (i < src.length()) {
            SpecialGroup group = specialGroupAt(src, i);
            if (group != null) {
                appendPlainChunkAsRtf(out, src.substring(lastPlain, i));
                if (group.kind == SpecialKind.PICT) {
                    String raw = group.pictGroup == null ? group.raw : group.pictGroup;
                    out.append(raw);
                } else if (group.kind == SpecialKind.FIELD || group.kind == SpecialKind.OBJECT) {
                    out.append(group.raw);
                }
                i = group.end + 1;
                lastPlain = i;
            } else {
                i++;
            }
        }
        appendPlainChunkAsRtf(out, src.substring(lastPlain));
        return out.toString();
    }

    public static String rtfPictureFromImage(byte[] bytes, String mimeType) {
        if (bytes == null || bytes.length == 0) return null;
        String mime = normalizeImageMime(bytes, mimeType);
        String blip;
        byte[] payload = bytes;
        if ("image/png".equals(mime)) {
            blip = "pngblip";
        } else if ("image/jpeg".equals(mime) || "image/jpg".equals(mime)) {
            blip = "jpegblip";
        } else if ("image/bmp".equals(mime) || "image/x-ms-bmp".equals(mime)) {
            blip = "dibitmap0";
            payload = bmpToDibBytes(bytes);
        } else {
            return null;
        }
        StringBuilder out = new StringBuilder(payload.length * 2 + 64);
        out.append("{\\pict\\").append(blip).append('\n');
        String hex = bytesToHex(payload);
        for (int i = 0; i < hex.length(); i += 64) {
            int end = Math.min(hex.length(), i + 64);
            out.append(hex, i, end).append('\n');
        }
        out.append('}');
        return out.toString();
    }

    public static String appendImageToRtf(String rtf, byte[] bytes, String mimeType) {
        String picture = rtfPictureFromImage(bytes, mimeType);
        if (picture == null) return rtf == null ? "" : rtf;
        String base = looksLikeRtf(rtf) ? rtf.trim() : plainTextToRtf(rtf == null ? "" : rtf);
        int end = base.lastIndexOf('}');
        if (end < 0) return base + "\\par\n" + picture;
        return base.substring(0, end) + "\\par\n" + picture + "\\par\n" + base.substring(end);
    }

    private static void appendPlainChunkAsHtml(StringBuilder html, String chunk) {
        String plain = rtfToPlainText(chunk);
        if (!plain.isEmpty()) html.append(plainTextToHtml(plain));
    }

    private static void appendPlainChunkAsRtf(StringBuilder out, String chunk) {
        String plain = rtfToPlainText(chunk);
        if (!plain.isEmpty()) out.append(rtfEscapeMultiline(plain));
    }

    private static String fieldToHtml(String group) {
        String display = extractFieldResultText(group);
        Matcher m = HYPERLINK_FIELD.matcher(group == null ? "" : group);
        if (m.find()) {
            String url = m.group(1) != null ? m.group(1) : (m.group(2) == null ? "" : m.group(2));
            url = url.replace("\\\"", "\"").replace("\\\\", "\\");
            String label = display.isEmpty() ? url : display;
            return "<a href=\"" + escapeXml(url) + "\">" + plainTextToHtml(label) + "</a>";
        }
        String encoded = base64Encode((group == null ? "" : group).getBytes(StandardCharsets.UTF_8));
        return "<span data-notizen-rtf-field=\"" + encoded + "\">" + plainTextToHtml(display) + "</span>";
    }

    private static String objectToHtml(String group) {
        String className = extractObjectClassName(group);
        String encoded = base64Encode((group == null ? "" : group).getBytes(StandardCharsets.UTF_8));
        String title = className.isEmpty() ? "" : " title=\"" + escapeXml(className) + "\"";
        return "<span data-notizen-rtf-object=\"" + encoded + "\"" + title + ">" + escapeXml(LEGACY_OBJECT_PLACEHOLDER) + "</span>";
    }

    private static String imageToHtml(RtfImage image) {
        StringBuilder out = new StringBuilder(image.data.length * 2 + 128);
        out.append("<img src=\"data:").append(escapeXml(image.mimeType)).append(";base64,").append(base64Encode(image.data)).append("\"");
        if (image.widthTwips > 0) out.append(" width=\"").append(Math.max(1, Math.round(image.widthTwips / 15.0f))).append("\"");
        if (image.heightTwips > 0) out.append(" height=\"").append(Math.max(1, Math.round(image.heightTwips / 15.0f))).append("\"");
        out.append("/>");
        return out.toString();
    }

    private enum SpecialKind { PICT, FIELD, OBJECT }

    private static final class SpecialGroup {
        final SpecialKind kind;
        final int start;
        final int end;
        final String raw;
        final String pictGroup;

        SpecialGroup(SpecialKind kind, int start, int end, String raw, String pictGroup) {
            this.kind = kind;
            this.start = start;
            this.end = end;
            this.raw = raw;
            this.pictGroup = pictGroup;
        }
    }

    private static SpecialGroup specialGroupAt(String src, int index) {
        if (src == null || index < 0 || index >= src.length()) return null;
        if (src.startsWith("{\\field", index)) {
            int end = findGroupEnd(src, index);
            if (end >= 0) return new SpecialGroup(SpecialKind.FIELD, index, end, src.substring(index, end + 1), null);
        }
        if (src.startsWith("{\\object", index)) {
            int end = findGroupEnd(src, index);
            if (end >= 0) return new SpecialGroup(SpecialKind.OBJECT, index, end, src.substring(index, end + 1), null);
        }
        if (src.startsWith("{\\pict", index) || src.startsWith("{\\*\\shppict", index) || src.startsWith("{\\nonshppict", index)) {
            int end = findGroupEnd(src, index);
            if (end < 0) return null;
            String raw = src.substring(index, end + 1);
            String pict = raw;
            if (!raw.startsWith("{\\pict")) {
                int inner = raw.indexOf("{\\pict");
                if (inner >= 0) {
                    int innerEnd = findGroupEnd(raw, inner);
                    if (innerEnd >= 0) pict = raw.substring(inner, innerEnd + 1);
                }
            }
            return new SpecialGroup(SpecialKind.PICT, index, end, raw, pict);
        }
        return null;
    }

    public static int findGroupEnd(String text, int start) {
        if (text == null || start < 0 || start >= text.length()) return -1;
        int depth = 0;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static RtfImage parsePictGroup(String group) {
        if (group == null || group.isEmpty()) return null;
        String mimeType;
        String rtfControl;
        boolean wantsBmpWrap = false;
        if (group.contains("\\pngblip")) {
            mimeType = "image/png";
            rtfControl = "pngblip";
        } else if (group.contains("\\jpegblip") || group.contains("\\jpgblip")) {
            mimeType = "image/jpeg";
            rtfControl = "jpegblip";
        } else if (group.contains("\\dibitmap") || group.contains("\\wbitmap")) {
            mimeType = "image/bmp";
            rtfControl = "dibitmap0";
            wantsBmpWrap = true;
        } else if (group.contains("\\emfblip")) {
            mimeType = "image/x-emf";
            rtfControl = "emfblip";
        } else {
            Matcher wmf = Pattern.compile("\\\\wmetafile(-?\\d+)?").matcher(group);
            if (wmf.find()) {
                mimeType = "image/wmf";
                rtfControl = "wmetafile" + (wmf.group(1) == null ? "8" : wmf.group(1));
            } else {
                return null;
            }
        }
        String scrubbed = group.replaceAll("\\\\'[0-9a-fA-F]{2}", " ");
        scrubbed = scrubbed.replaceAll("\\\\[a-zA-Z]+-?\\d* ?", " ");
        String hex = scrubbed.replaceAll("[^0-9a-fA-F]", "");
        if ((hex.length() & 1) == 1) hex = hex.substring(0, hex.length() - 1);
        if (hex.isEmpty()) return null;
        byte[] data = hexToBytes(hex);
        if (wantsBmpWrap) {
            byte[] wrapped = dibToBmpBytes(data);
            if (wrapped != null) data = wrapped;
        }
        return new RtfImage(mimeType, data, controlNumber(group, "picwgoal", 0), controlNumber(group, "pichgoal", 0), rtfControl, group);
    }

    private static int controlNumber(String group, String name, int fallback) {
        Pattern p = Pattern.compile(String.format(Locale.ROOT, CONTROL_NUMBER.pattern(), Pattern.quote(name)));
        Matcher m = p.matcher(group == null ? "" : group);
        if (!m.find()) return fallback;
        try { return Integer.parseInt(m.group(1)); }
        catch (Exception e) { return fallback; }
    }

    private static String extractFieldResultText(String group) {
        if (group == null) return "";
        int start = group.indexOf("{\\fldrslt");
        if (start < 0) return "";
        int end = findGroupEnd(group, start);
        if (end < 0) return "";
        return rtfToPlainText(group.substring(start, end + 1)).trim();
    }

    private static String extractObjectClassName(String group) {
        if (group == null) return "";
        Matcher m = Pattern.compile("\\\\objclass\\s+([^{}\\\\]+)").matcher(group);
        if (!m.find()) return "";
        String raw = m.group(1).trim().replaceAll("[;}]+$", "");
        return raw.replaceAll("\\\\'[0-9a-fA-F]{2}", " ").trim();
    }

    public static String escapeXml(String text) {
        if (text == null) return "";
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    public static String plainTextToHtml(String text) {
        String escaped = escapeXml(text == null ? "" : text);
        return escaped.replace("\r\n", "\n").replace("\r", "\n").replace(RTF_SOFT_LINE_BREAK, "<br/>").replace("\n", "<br/>\n");
    }

    public static String rtfEscapeText(String text) {
        StringBuilder out = new StringBuilder(text == null ? 0 : text.length() + 16);
        if (text != null) {
            for (int offset = 0; offset < text.length(); ) {
                int cp = text.codePointAt(offset);
                offset += Character.charCount(cp);
                appendRtfEscapedCodePoint(out, cp);
            }
        }
        return out.toString();
    }

    public static String rtfEscapeMultiline(String text) {
        String normalized = (text == null ? "" : text).replace("\r\n", "\n").replace('\r', '\n');
        StringBuilder out = new StringBuilder(normalized.length() + 32);
        for (int offset = 0; offset < normalized.length(); ) {
            int cp = normalized.codePointAt(offset);
            offset += Character.charCount(cp);
            if (cp == RTF_SOFT_LINE_BREAK.codePointAt(0)) out.append("\\line ");
            else if (cp == '\n') out.append("\\par\n");
            else if (cp == '\t') out.append("\\tab ");
            else appendRtfEscapedCodePoint(out, cp);
        }
        return out.toString();
    }

    private static void appendRtfEscapedCodePoint(StringBuilder out, int cp) {
        if (cp == '\\' || cp == '{' || cp == '}') out.append('\\').append((char) cp);
        else if (cp >= 0x20 && cp <= 0x7e) out.append((char) cp);
        else if (cp <= 0xffff) {
            int signed = cp;
            if (signed > 32767) signed -= 65536;
            out.append("\\u").append(signed).append('?');
        } else {
            char[] chars = Character.toChars(cp);
            for (char ch : chars) {
                int signed = ch;
                if (signed > 32767) signed -= 65536;
                out.append("\\u").append(signed).append('?');
            }
        }
    }

    private static final class State {
        boolean skip;
        boolean starDestination;
        boolean hidden;
        int uc = 1;
        int skipChars = 0;
        State copy() {
            State s = new State();
            s.skip = skip;
            s.starDestination = starDestination;
            s.hidden = hidden;
            s.uc = uc;
            s.skipChars = skipChars;
            return s;
        }
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
        try { return Charset.forName("windows-" + codepage); }
        catch (Exception ignored) {}
        try { return Charset.forName("cp" + codepage); }
        catch (Exception ignored) {}
        return CP1252;
    }

    private static final class Parser {
        private final String src;
        private final Charset ansiCharset;
        private final StringBuilder out = new StringBuilder();
        private final java.util.ArrayDeque<State> stack = new java.util.ArrayDeque<>();
        private State state = new State();
        private int i = 0;

        Parser(String src) {
            this.src = src;
            this.ansiCharset = detectAnsiCharset(src);
        }

        String parse() {
            while (i < src.length()) {
                char c = src.charAt(i++);
                if (c == '{') {
                    stack.push(state.copy());
                } else if (c == '}') {
                    state = stack.isEmpty() ? new State() : stack.pop();
                } else if (c == '\\') {
                    parseControl();
                } else {
                    if (c != '\r' && c != '\n') appendChar(c);
                }
            }
            return normalizeResult(combineSurrogates(out.toString()));
        }

        private static String normalizeResult(String s) {
            s = s == null ? "" : s.replace(RTF_SOFT_LINE_BREAK, "\n");
            while (s.endsWith("\n")) s = s.substring(0, s.length() - 1);
            return s;
        }
        private static String combineSurrogates(String s) {
            // Java strings naturally keep surrogate pairs; this method exists for symmetry with the PyQt port.
            return s;
        }

        private void appendChar(char c) {
            if (state.skip || state.hidden) return;
            if (state.skipChars > 0) {
                state.skipChars--;
                return;
            }
            out.append(c);
        }

        private void appendText(String s) {
            if (state.skip || state.hidden) return;
            if (state.skipChars > 0) {
                int drop = Math.min(state.skipChars, s.length());
                state.skipChars -= drop;
                s = s.substring(drop);
            }
            out.append(s);
        }

        private void parseControl() {
            if (i >= src.length()) return;
            char c = src.charAt(i++);
            if (c == '\\' || c == '{' || c == '}') {
                appendChar(c);
                return;
            }
            if (c == '\'') {
                parseHexByte();
                return;
            }
            if (!Character.isLetter(c)) {
                handleControlSymbol(c);
                return;
            }

            int start = i - 1;
            while (i < src.length() && Character.isLetter(src.charAt(i))) i++;
            String word = src.substring(start, i).toLowerCase(Locale.ROOT);

            boolean negative = false;
            if (i < src.length() && (src.charAt(i) == '-' || src.charAt(i) == '+')) {
                negative = src.charAt(i) == '-';
                i++;
            }
            int numberStart = i;
            while (i < src.length() && Character.isDigit(src.charAt(i))) i++;
            Integer param = null;
            if (numberStart < i) {
                try {
                    int value = Integer.parseInt(src.substring(numberStart, i));
                    param = negative ? -value : value;
                } catch (NumberFormatException ignored) {
                    param = null;
                }
            }
            if (i < src.length() && src.charAt(i) == ' ') i++;
            handleControlWord(word, param);
        }

        private void parseHexByte() {
            if (i + 1 > src.length()) return;
            int end = Math.min(i + 2, src.length());
            String hex = src.substring(i, end);
            i = end;
            if (state.skip || state.hidden) return;
            if (state.skipChars > 0) {
                state.skipChars--;
                return;
            }
            try {
                int value = Integer.parseInt(hex, 16);
                out.append(new String(new byte[]{(byte) value}, ansiCharset));
            } catch (Exception ignored) {
                // ignore malformed hex escape
            }
        }

        private void handleControlSymbol(char c) {
            if (c == '*') {
                state.starDestination = true;
                return;
            }
            if (state.skip) return;
            switch (c) {
                case '~': appendText("\u00a0"); break;
                case '-': appendText("\u00ad"); break;
                case '_': appendText("\u2011"); break;
                default:
                    // unknown single-char control symbol: no visible output
            }
        }

        private void handleControlWord(String word, Integer param) {
            if (!state.skip && "object".equals(word)) {
                appendText(LEGACY_OBJECT_PLACEHOLDER);
                state.skip = true;
                state.starDestination = false;
                return;
            }
            if (!state.skip && "pict".equals(word)) {
                appendText(LEGACY_IMAGE_PLACEHOLDER);
                state.skip = true;
                state.starDestination = false;
                return;
            }
            if (isDestinationToSkip(word) || (state.starDestination && isLikelyUnknownDestination(word))) {
                state.skip = true;
                return;
            }
            state.starDestination = false;
            if (state.skip && !"uc".equals(word)) return;

            switch (word) {
                case "par":
                    appendText("\n");
                    break;
                case "line":
                    appendText(RTF_SOFT_LINE_BREAK);
                    break;
                case "tab":
                    appendText("\t");
                    break;
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
                case "sect":
                case "column": appendText("\n"); break;
                case "v":
                    state.hidden = param == null || param != 0;
                    break;
                case "u":
                    if (param != null) {
                        int code = param;
                        if (code < 0) code += 65536;
                        appendChar((char) code);
                        state.skipChars = Math.max(0, state.uc);
                    }
                    break;
                case "uc":
                    if (param != null && param >= 0 && param <= 16) state.uc = param;
                    break;
                case "bin":
                    if (param != null && param > 0) i = Math.min(src.length(), i + param);
                    break;
                case "cell":
                case "nestcell":
                    appendText("\t");
                    break;
                case "row":
                    appendText("\n");
                    break;
                default:
                    // formatting controls are intentionally ignored for plain text
            }
        }

        private static boolean isLikelyUnknownDestination(String word) {
            return !("fldrslt".equals(word) || "pntext".equals(word) || "listtext".equals(word));
        }

        private static boolean isDestinationToSkip(String word) {
            switch (word) {
                case "fonttbl":
                case "colortbl":
                case "stylesheet":
                case "info":
                case "pict":
                case "object":
                case "objclass":
                case "objdata":
                case "datafield":
                case "generator":
                case "datastore":
                case "themedata":
                case "colorschememapping":
                case "listtable":
                case "listoverridetable":
                case "rsidtbl":
                case "fldinst":
                    return true;
                default:
                    return false;
            }
        }
    }

    public static byte[] normalizeExportNewlinesUtf8(String text) {
        String normalized = (text == null ? "" : text).replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\r\n");
        return normalized.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] normalizeExportNewlinesCp1252(String text) {
        String normalized = (text == null ? "" : text).replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\r\n");
        return normalized.getBytes(CP1252);
    }

    public static byte[] dibToBmpBytes(byte[] dib) {
        if (dib == null || dib.length == 0) return null;
        if (dib.length >= 2 && dib[0] == 'B' && dib[1] == 'M') return dib;
        if (dib.length < 4) return null;
        int headerSize = littleInt(dib, 0, 4);
        if (headerSize < 12 || headerSize > dib.length) return null;
        int paletteSize = 0;
        if (headerSize == 12) {
            if (dib.length < 12) return null;
            int bitCount = littleInt(dib, 10, 2);
            if (bitCount > 0 && bitCount <= 8) paletteSize = (1 << bitCount) * 3;
        } else {
            int bitCount = dib.length >= 16 ? littleInt(dib, 14, 2) : 0;
            int compression = dib.length >= 20 ? littleInt(dib, 16, 4) : 0;
            int colorsUsed = dib.length >= 36 ? littleInt(dib, 32, 4) : 0;
            if (colorsUsed != 0) paletteSize = colorsUsed * 4;
            else if (bitCount > 0 && bitCount <= 8) paletteSize = (1 << bitCount) * 4;
            if (compression == 3 && headerSize == 40 && (bitCount == 16 || bitCount == 32)) paletteSize += 12;
        }
        int pixelOffset = 14 + Math.min(dib.length, headerSize + paletteSize);
        int fileSize = 14 + dib.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream(fileSize);
        out.write('B'); out.write('M');
        writeLittle(out, fileSize, 4);
        writeLittle(out, 0, 4);
        writeLittle(out, pixelOffset, 4);
        out.write(dib, 0, dib.length);
        return out.toByteArray();
    }

    public static byte[] bmpToDibBytes(byte[] data) {
        if (data != null && data.length >= 14 && data[0] == 'B' && data[1] == 'M') {
            byte[] out = new byte[data.length - 14];
            System.arraycopy(data, 14, out, 0, out.length);
            return out;
        }
        return data == null ? new byte[0] : data;
    }

    private static int littleInt(byte[] data, int offset, int length) {
        int value = 0;
        for (int i = 0; i < length && offset + i < data.length; i++) value |= (data[offset + i] & 0xff) << (8 * i);
        return value;
    }

    private static void writeLittle(ByteArrayOutputStream out, int value, int length) {
        for (int i = 0; i < length; i++) out.write((value >> (8 * i)) & 0xff);
    }

    private static String normalizeImageMime(byte[] bytes, String supplied) {
        String s = supplied == null ? "" : supplied.toLowerCase(Locale.ROOT);
        if (s.contains("png")) return "image/png";
        if (s.contains("jpeg") || s.contains("jpg")) return "image/jpeg";
        if (s.contains("bmp")) return "image/bmp";
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') return "image/png";
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff) return "image/jpeg";
        if (bytes.length >= 2 && bytes[0] == 'B' && bytes[1] == 'M') return "image/bmp";
        return s;
    }

    private static int estimatePictPayloadBytes(String group, int stopAboveBytes) {
        if (group == null || group.isEmpty()) return 0;
        int hexDigits = 0;
        int stopDigits = Math.max(2, stopAboveBytes * 2);
        boolean inControl = false;
        for (int i = 0; i < group.length(); i++) {
            char c = group.charAt(i);
            if (c == '\\') {
                inControl = true;
                continue;
            }
            if (inControl) {
                if (Character.isWhitespace(c) || c == '\r' || c == '\n' || c == ';' || c == '}' || c == '{') inControl = false;
                continue;
            }
            if (Character.digit(c, 16) >= 0) {
                hexDigits++;
                if (hexDigits > stopDigits) return stopAboveBytes + 1;
            }
        }
        return hexDigits / 2;
    }

    public static String bytesToHex(byte[] data) {
        if (data == null) return "";
        char[] hex = "0123456789abcdef".toCharArray();
        StringBuilder out = new StringBuilder(data.length * 2);
        for (byte b : data) {
            out.append(hex[(b >> 4) & 0x0f]).append(hex[b & 0x0f]);
        }
        return out.toString();
    }

    public static byte[] hexToBytes(String hex) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (hex == null) return out.toByteArray();
        int high = -1;
        for (int i = 0; i < hex.length(); i++) {
            int v = Character.digit(hex.charAt(i), 16);
            if (v < 0) continue;
            if (high < 0) high = v;
            else {
                out.write((high << 4) | v);
                high = -1;
            }
        }
        return out.toByteArray();
    }

    private static String base64Encode(byte[] data) {
        if (data == null || data.length == 0) return "";
        char[] table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        StringBuilder out = new StringBuilder(((data.length + 2) / 3) * 4);
        int i = 0;
        while (i < data.length) {
            int b0 = data[i++] & 0xff;
            int b1 = i < data.length ? data[i++] & 0xff : -1;
            int b2 = i < data.length ? data[i++] & 0xff : -1;
            out.append(table[b0 >>> 2]);
            out.append(table[((b0 & 0x03) << 4) | (b1 >= 0 ? (b1 >>> 4) : 0)]);
            out.append(b1 >= 0 ? table[((b1 & 0x0f) << 2) | (b2 >= 0 ? (b2 >>> 6) : 0)] : '=');
            out.append(b2 >= 0 ? table[b2 & 0x3f] : '=');
        }
        return out.toString();
    }
}
