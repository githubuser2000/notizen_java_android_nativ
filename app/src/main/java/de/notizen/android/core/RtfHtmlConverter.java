package de.notizen.android.core;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rich HTML bridge for the mobile Java port.
 *
 * This is intentionally dependency-free: Android's WebView can render the HTML
 * fragment, and the pure Java tests can exercise the same converter without the
 * Android SDK. It mirrors the PyQt port's pragmatic approach: preserve Notizen
 * data and the most common RichTextBox formatting, while keeping unknown RTF
 * destinations harmless.
 */
final class RtfHtmlConverter {
    private static final Charset CP1252 = Charset.forName("windows-1252");
    private static final String SOFT_LINE_BREAK = "\u2028";
    private static final int MAX_HTML_IMAGE_BYTES = 1024 * 1024;
    private static final Pattern HYPERLINK_FIELD = Pattern.compile("HYPERLINK\\s+(?:\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"|([^\\\\{}\\s]+))", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAG_RE = Pattern.compile("(?is)<(/?)([a-zA-Z][a-zA-Z0-9:_-]*)([^>]*)>|([^<]+)");
    private static final Pattern ATTR_RE = Pattern.compile("([a-zA-Z_:][-a-zA-Z0-9_:.]*)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))");


    private static final Map<String, String> CSS_NAMED_COLORS = cssNamedColors();

    private static Map<String, String> cssNamedColors() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String[][] pairs = {
                {"black","#000000"}, {"silver","#c0c0c0"}, {"gray","#808080"}, {"grey","#808080"},
                {"white","#ffffff"}, {"maroon","#800000"}, {"red","#ff0000"}, {"purple","#800080"},
                {"fuchsia","#ff00ff"}, {"magenta","#ff00ff"}, {"green","#008000"}, {"lime","#00ff00"},
                {"olive","#808000"}, {"yellow","#ffff00"}, {"navy","#000080"}, {"blue","#0000ff"},
                {"teal","#008080"}, {"aqua","#00ffff"}, {"cyan","#00ffff"}, {"orange","#ffa500"},
                {"aliceblue","#f0f8ff"}, {"antiquewhite","#faebd7"}, {"aquamarine","#7fffd4"},
                {"azure","#f0ffff"}, {"beige","#f5f5dc"}, {"bisque","#ffe4c4"}, {"brown","#a52a2a"},
                {"burlywood","#deb887"}, {"cadetblue","#5f9ea0"}, {"chartreuse","#7fff00"},
                {"chocolate","#d2691e"}, {"coral","#ff7f50"}, {"cornflowerblue","#6495ed"},
                {"cornsilk","#fff8dc"}, {"crimson","#dc143c"}, {"darkblue","#00008b"},
                {"darkcyan","#008b8b"}, {"darkgoldenrod","#b8860b"}, {"darkgray","#a9a9a9"},
                {"darkgrey","#a9a9a9"}, {"darkgreen","#006400"}, {"darkorange","#ff8c00"},
                {"darkorchid","#9932cc"}, {"darkred","#8b0000"}, {"darksalmon","#e9967a"},
                {"darkseagreen","#8fbc8f"}, {"darkslateblue","#483d8b"}, {"darkslategray","#2f4f4f"},
                {"darkslategrey","#2f4f4f"}, {"darkturquoise","#00ced1"}, {"darkviolet","#9400d3"},
                {"deeppink","#ff1493"}, {"deepskyblue","#00bfff"}, {"dimgray","#696969"}, {"dimgrey","#696969"},
                {"dodgerblue","#1e90ff"}, {"firebrick","#b22222"}, {"floralwhite","#fffaf0"},
                {"forestgreen","#228b22"}, {"gainsboro","#dcdcdc"}, {"ghostwhite","#f8f8ff"},
                {"gold","#ffd700"}, {"goldenrod","#daa520"}, {"greenyellow","#adff2f"},
                {"honeydew","#f0fff0"}, {"hotpink","#ff69b4"}, {"indianred","#cd5c5c"},
                {"indigo","#4b0082"}, {"ivory","#fffff0"}, {"khaki","#f0e68c"}, {"lavender","#e6e6fa"},
                {"lavenderblush","#fff0f5"}, {"lawngreen","#7cfc00"}, {"lemonchiffon","#fffacd"},
                {"lightblue","#add8e6"}, {"lightcoral","#f08080"}, {"lightcyan","#e0ffff"},
                {"lightgoldenrodyellow","#fafad2"}, {"lightgray","#d3d3d3"}, {"lightgrey","#d3d3d3"},
                {"lightgreen","#90ee90"}, {"lightpink","#ffb6c1"}, {"lightsalmon","#ffa07a"},
                {"lightseagreen","#20b2aa"}, {"lightskyblue","#87cefa"}, {"lightslategray","#778899"},
                {"lightslategrey","#778899"}, {"lightsteelblue","#b0c4de"}, {"lightyellow","#ffffe0"},
                {"linen","#faf0e6"}, {"mediumaquamarine","#66cdaa"}, {"mediumblue","#0000cd"},
                {"mediumorchid","#ba55d3"}, {"mediumpurple","#9370db"}, {"mediumseagreen","#3cb371"},
                {"mediumslateblue","#7b68ee"}, {"mediumspringgreen","#00fa9a"}, {"mediumturquoise","#48d1cc"},
                {"mediumvioletred","#c71585"}, {"midnightblue","#191970"}, {"mintcream","#f5fffa"},
                {"mistyrose","#ffe4e1"}, {"moccasin","#ffe4b5"}, {"oldlace","#fdf5e6"},
                {"olivedrab","#6b8e23"}, {"orangered","#ff4500"}, {"orchid","#da70d6"},
                {"palegoldenrod","#eee8aa"}, {"palegreen","#98fb98"}, {"paleturquoise","#afeeee"},
                {"palevioletred","#db7093"}, {"papayawhip","#ffefd5"}, {"peachpuff","#ffdab9"},
                {"peru","#cd853f"}, {"pink","#ffc0cb"}, {"plum","#dda0dd"}, {"powderblue","#b0e0e6"},
                {"rebeccapurple","#663399"}, {"rosybrown","#bc8f8f"}, {"royalblue","#4169e1"},
                {"saddlebrown","#8b4513"}, {"salmon","#fa8072"}, {"sandybrown","#f4a460"},
                {"seagreen","#2e8b57"}, {"seashell","#fff5ee"}, {"sienna","#a0522d"},
                {"skyblue","#87ceeb"}, {"slateblue","#6a5acd"}, {"slategray","#708090"}, {"slategrey","#708090"},
                {"snow","#fffafa"}, {"springgreen","#00ff7f"}, {"steelblue","#4682b4"},
                {"tan","#d2b48c"}, {"thistle","#d8bfd8"}, {"tomato","#ff6347"},
                {"turquoise","#40e0d0"}, {"violet","#ee82ee"}, {"wheat","#f5deb3"},
                {"whitesmoke","#f5f5f5"}, {"yellowgreen","#9acd32"}
        };
        for (String[] pair : pairs) m.put(pair[0], pair[1]);
        return m;
    }

    private RtfHtmlConverter() {}

    static String rtfToHtml(String rtf) {
        if (rtf == null || rtf.isEmpty()) return "";
        if (!RtfUtils.looksLikeRtf(rtf)) return RtfUtils.plainTextToHtml(rtf);
        return new RtfRenderer(rtf).render();
    }

    static String rtfToHtmlDocument(String rtf) {
        return LegacyRichTextBoxSemantics.decorateHtmlDocument(rtfToHtml(rtf), false);
    }

    static String rtfToDesktopHtml(String rtf) {
        return LegacyRichTextBoxSemantics.desktopNoteHtmlDocument(rtf);
    }

    static String htmlToRtf(String html) {
        HtmlRtfWriter writer = new HtmlRtfWriter();
        writer.convert(html == null ? "" : html);
        return writer.finish();
    }

    // ---------------------------------------------------------------------
    // RTF -> styled HTML
    // ---------------------------------------------------------------------

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

    private static final class RichStyle {
        boolean bold;
        boolean italic;
        boolean underline;
        boolean strike;
        boolean hidden;
        boolean allCaps;
        boolean smallCaps;
        Integer fgIndex;
        Integer bgIndex;
        Integer fontIndex;
        Integer fontSizeHalfPoints;
        String align;
        int leftIndentTwips;
        int rightIndentTwips;
        int firstIndentTwips;
        int spaceBeforeTwips;
        int spaceAfterTwips;
        int lineSpacingTwips;
        boolean lineSpacingMultiple;
        String vertical;
        String direction;
        int letterSpacingTwips;

        RichStyle copy() {
            RichStyle s = new RichStyle();
            s.bold = bold;
            s.italic = italic;
            s.underline = underline;
            s.strike = strike;
            s.hidden = hidden;
            s.allCaps = allCaps;
            s.smallCaps = smallCaps;
            s.fgIndex = fgIndex;
            s.bgIndex = bgIndex;
            s.fontIndex = fontIndex;
            s.fontSizeHalfPoints = fontSizeHalfPoints;
            s.align = align;
            s.leftIndentTwips = leftIndentTwips;
            s.rightIndentTwips = rightIndentTwips;
            s.firstIndentTwips = firstIndentTwips;
            s.spaceBeforeTwips = spaceBeforeTwips;
            s.spaceAfterTwips = spaceAfterTwips;
            s.lineSpacingTwips = lineSpacingTwips;
            s.lineSpacingMultiple = lineSpacingMultiple;
            s.vertical = vertical;
            s.direction = direction;
            s.letterSpacingTwips = letterSpacingTwips;
            return s;
        }

        void resetCharacterStyle() {
            bold = false;
            italic = false;
            underline = false;
            strike = false;
            hidden = false;
            allCaps = false;
            smallCaps = false;
            fgIndex = null;
            bgIndex = null;
            fontIndex = null;
            fontSizeHalfPoints = null;
            vertical = null;
            direction = null;
            letterSpacingTwips = 0;
        }

        void resetParagraphStyle() {
            align = null;
            leftIndentTwips = 0;
            rightIndentTwips = 0;
            firstIndentTwips = 0;
            spaceBeforeTwips = 0;
            spaceAfterTwips = 0;
            lineSpacingTwips = 0;
            lineSpacingMultiple = false;
        }

        boolean isDefault() {
            return !bold && !italic && !underline && !strike && !hidden && !allCaps && !smallCaps
                    && fgIndex == null && bgIndex == null && fontIndex == null && fontSizeHalfPoints == null
                    && align == null && leftIndentTwips == 0 && rightIndentTwips == 0 && firstIndentTwips == 0
                    && spaceBeforeTwips == 0 && spaceAfterTwips == 0 && lineSpacingTwips == 0
                    && !lineSpacingMultiple && vertical == null && direction == null && letterSpacingTwips == 0;
        }
    }

    private static final class Segment {
        final String text;
        final RichStyle style;
        final boolean rawHtml;

        Segment(String text, RichStyle style, boolean rawHtml) {
            this.text = text == null ? "" : text;
            this.style = style == null ? new RichStyle() : style;
            this.rawHtml = rawHtml;
        }
    }

    private static final class RtfState {
        RichStyle style = new RichStyle();
        boolean skip;
        boolean starDestination;
        int uc = 1;
        int skipChars = 0;

        RtfState copy() {
            RtfState s = new RtfState();
            s.style = style.copy();
            s.skip = skip;
            s.starDestination = starDestination;
            s.uc = uc;
            s.skipChars = skipChars;
            return s;
        }
    }

    private static final class RtfRenderer {
        final String src;
        final Charset ansiCharset;
        final Map<Integer, String> fonts;
        final Map<Integer, String> colors;
        final ArrayDeque<RtfState> stack = new ArrayDeque<>();
        final ArrayList<Segment> segments = new ArrayList<>();
        RtfState state = new RtfState();
        int i;
        boolean tableOpen;
        boolean tableRowOpen;
        final ArrayList<String> tableCells = new ArrayList<>();
        final StringBuilder tableCell = new StringBuilder();

        RtfRenderer(String src) {
            this.src = src == null ? "" : src;
            this.ansiCharset = detectAnsiCharset(this.src);
            this.fonts = extractFontTable(this.src);
            this.colors = extractColorTable(this.src);
        }

        String render() {
            parse();
            flushTable();
            return renderSegments();
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
                if (c == '{') stack.push(state.copy());
                else if (c == '}') state = stack.isEmpty() ? new RtfState() : stack.pop();
                else if (c == '\\') parseControl();
                else if (c != '\r' && c != '\n') appendChar(c);
            }
        }

        private void appendSpecial(SpecialGroup special) {
            if (special.kind == SpecialKind.PICT) {
                String raw = special.pictGroup == null ? special.raw : special.pictGroup;
                int estimatedBytes = estimatePictPayloadBytes(raw, MAX_HTML_IMAGE_BYTES + 1);
                if (estimatedBytes > MAX_HTML_IMAGE_BYTES) {
                    appendSpecialHtml(largeImagePlaceholderHtml(estimatedBytes));
                    return;
                }
                try {
                    RtfUtils.RtfImage image = parsePictGroup(raw);
                    appendSpecialHtml(image == null || image.data == null || image.data.length == 0 ? RtfUtils.escapeXml(RtfUtils.LEGACY_IMAGE_PLACEHOLDER) : imageToHtml(image));
                } catch (OutOfMemoryError | RuntimeException ex) {
                    appendSpecialHtml(largeImagePlaceholderHtml(estimatedBytes));
                }
            } else if (special.kind == SpecialKind.FIELD) {
                appendSpecialHtml(fieldToHtml(special.raw));
            } else if (special.kind == SpecialKind.OBJECT) {
                appendSpecialHtml(objectToHtml(special.raw));
            }
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
                } catch (NumberFormatException ignored) {}
            }
            if (i < src.length() && src.charAt(i) == ' ') i++;
            handleControlWord(word, param);
        }

        private void parseHexByte() {
            int end = Math.min(i + 2, src.length());
            String hex = src.substring(i, end);
            i = end;
            if (state.skip) return;
            if (state.skipChars > 0) {
                state.skipChars--;
                return;
            }
            try {
                int value = Integer.parseInt(hex, 16);
                appendText(new String(new byte[]{(byte) value}, ansiCharset));
            } catch (Exception ignored) {}
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
                case "ul":
                case "ulw":
                case "ulwave":
                case "uld":
                case "uldash":
                case "uldashd":
                case "uldashdd":
                case "uldb":
                case "ulth": state.style.underline = on(param); break;
                case "ulnone": state.style.underline = false; break;
                case "strike":
                case "striked": state.style.strike = on(param); break;
                case "v": state.style.hidden = on(param); break;
                case "caps": state.style.allCaps = on(param); break;
                case "scaps": state.style.smallCaps = on(param); break;
                case "cf": state.style.fgIndex = param == null || param <= 0 ? null : param; break;
                case "highlight":
                case "cbpat":
                case "chcbpat": state.style.bgIndex = param == null || param <= 0 ? null : param; break;
                case "f": state.style.fontIndex = param == null || param < 0 ? null : param; break;
                case "fs": state.style.fontSizeHalfPoints = param == null || param <= 0 ? null : param; break;
                case "plain": state.style.resetCharacterStyle(); break;
                case "pard": state.style.resetParagraphStyle(); break;
                case "trowd": beginTableRow(); break;
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
                case "rtlpar":
                case "rtlch": state.style.direction = "rtl"; break;
                case "ltrpar":
                case "ltrch": state.style.direction = "ltr"; break;
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
                case "sect":
                case "column": appendText("\n"); break;
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
                case "cell":
                case "nestcell":
                    if (tableRowOpen) closeTableCell(); else appendText("\t");
                    break;
                case "row":
                    if (tableRowOpen) closeTableRow(); else appendText("\n");
                    break;
                default: break;
            }
        }

        private boolean on(Integer param) { return param == null || param != 0; }

        private void appendChar(char c) {
            if (state.skip) return;
            if (state.skipChars > 0) {
                state.skipChars--;
                return;
            }
            appendText(String.valueOf(c));
        }

        private void appendText(String text) {
            if (state.skip || text == null || text.isEmpty()) return;
            if (state.skipChars > 0) {
                int drop = Math.min(state.skipChars, text.length());
                state.skipChars -= drop;
                text = text.substring(drop);
            }
            if (text.isEmpty()) return;
            if (tableRowOpen) {
                appendTableCellText(text);
                return;
            }
            if (tableOpen) flushTable();
            if (!segments.isEmpty()) {
                Segment last = segments.get(segments.size() - 1);
                if (!last.rawHtml && sameStyle(last.style, state.style)) {
                    segments.set(segments.size() - 1, new Segment(last.text + text, last.style, false));
                    return;
                }
            }
            segments.add(new Segment(text, state.style.copy(), false));
        }

        private void appendSpecialHtml(String html) {
            if (html == null || html.isEmpty()) return;
            if (tableRowOpen) tableCell.append(html);
            else appendRawHtml(html);
        }

        private void appendRawHtml(String html) {
            if (html == null || html.isEmpty()) return;
            if (tableOpen) flushTable();
            emitRawHtml(html);
        }

        private void emitRawHtml(String html) {
            if (html == null || html.isEmpty()) return;
            segments.add(new Segment(html, state.style.copy(), true));
        }

        private void beginTableRow() {
            if (state.skip) return;
            if (tableRowOpen) closeTableRow();
            if (!tableOpen) {
                emitRawHtml("<table class=\"notizen-rtf-table\">");
                tableOpen = true;
            }
            tableRowOpen = true;
            tableCells.clear();
            tableCell.setLength(0);
        }

        private void closeTableCell() {
            if (!tableRowOpen) return;
            tableCells.add(tableCell.toString());
            tableCell.setLength(0);
        }

        private void closeTableRow() {
            if (!tableRowOpen) return;
            closeTableCell();
            StringBuilder row = new StringBuilder("<tr>");
            for (String cell : tableCells) row.append("<td>").append(cell == null ? "" : cell).append("</td>");
            row.append("</tr>");
            emitRawHtml(row.toString());
            tableCells.clear();
            tableCell.setLength(0);
            tableRowOpen = false;
        }

        private void flushTable() {
            if (tableRowOpen) closeTableRow();
            if (tableOpen) {
                emitRawHtml("</table>");
                tableOpen = false;
            }
        }

        private void appendTableCellText(String text) {
            String normalized = applyCase(text, state.style).replace("\r\n", "\n").replace('\r', '\n').replace(SOFT_LINE_BREAK, "\n");
            if (normalized.isEmpty()) return;
            String[] lines = normalized.split("\n", -1);
            for (int li = 0; li < lines.length; li++) {
                if (li > 0) tableCell.append("<br/>");
                if (!lines[li].isEmpty()) appendStyledText(tableCell, lines[li], state.style);
            }
        }

        private String renderSegments() {
            StringBuilder out = new StringBuilder(src.length() + 128);
            boolean trailingBreak = false;
            for (Segment segment : segments) {
                if (segment.rawHtml) {
                    out.append(segment.text);
                    trailingBreak = false;
                    continue;
                }
                String text = applyCase(segment.text, segment.style).replace("\r\n", "\n").replace('\r', '\n').replace(SOFT_LINE_BREAK, "\n");
                if (text.isEmpty()) continue;
                String[] lines = text.split("\n", -1);
                for (int li = 0; li < lines.length; li++) {
                    if (li > 0) {
                        out.append("<br/>\n");
                        trailingBreak = true;
                    }
                    if (!lines[li].isEmpty()) {
                        appendStyledText(out, lines[li], segment.style);
                        trailingBreak = false;
                    }
                }
            }
            String html = out.toString();
            while (html.endsWith("<br/>\n")) html = html.substring(0, html.length() - 6);
            return html;
        }

        private String applyCase(String text, RichStyle style) {
            if (style.allCaps && text != null) return text.toUpperCase(Locale.ROOT);
            return text;
        }

        private void appendStyledText(StringBuilder out, String text, RichStyle style) {
            String css = cssFor(style, fonts, colors);
            String escaped = RtfUtils.escapeXml(text).replace("\t", "&emsp;").replace("\f", "<hr/>");
            if (css.isEmpty()) out.append(escaped);
            else out.append("<span style=\"").append(css).append("\">").append(escaped).append("</span>");
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

    private static boolean sameStyle(RichStyle a, RichStyle b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return a.bold == b.bold
                && a.italic == b.italic
                && a.underline == b.underline
                && a.strike == b.strike
                && a.hidden == b.hidden
                && a.allCaps == b.allCaps
                && a.smallCaps == b.smallCaps
                && equalsObj(a.fgIndex, b.fgIndex)
                && equalsObj(a.bgIndex, b.bgIndex)
                && equalsObj(a.fontIndex, b.fontIndex)
                && equalsObj(a.fontSizeHalfPoints, b.fontSizeHalfPoints)
                && equalsObj(a.align, b.align)
                && a.leftIndentTwips == b.leftIndentTwips
                && a.rightIndentTwips == b.rightIndentTwips
                && a.firstIndentTwips == b.firstIndentTwips
                && a.spaceBeforeTwips == b.spaceBeforeTwips
                && a.spaceAfterTwips == b.spaceAfterTwips
                && a.lineSpacingTwips == b.lineSpacingTwips
                && a.lineSpacingMultiple == b.lineSpacingMultiple
                && equalsObj(a.vertical, b.vertical)
                && equalsObj(a.direction, b.direction)
                && a.letterSpacingTwips == b.letterSpacingTwips;
    }

    private static boolean equalsObj(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    private static String cssFor(RichStyle style, Map<Integer, String> fonts, Map<Integer, String> colors) {
        if (style == null || style.isDefault()) return "";
        ArrayList<String> css = new ArrayList<>();
        if (style.bold) css.add("font-weight:700");
        if (style.italic) css.add("font-style:italic");
        if (style.underline || style.strike) {
            String value = (style.underline ? "underline" : "") + (style.underline && style.strike ? " " : "") + (style.strike ? "line-through" : "");
            css.add("text-decoration:" + value);
        }
        if (style.fgIndex != null && colors.containsKey(style.fgIndex) && !colors.get(style.fgIndex).isEmpty()) css.add("color:" + colors.get(style.fgIndex));
        if (style.bgIndex != null && colors.containsKey(style.bgIndex) && !colors.get(style.bgIndex).isEmpty()) css.add("background-color:" + colors.get(style.bgIndex));
        if (style.fontIndex != null && fonts.containsKey(style.fontIndex)) css.add("font-family:&quot;" + htmlAttr(fonts.get(style.fontIndex)) + "&quot;");
        if (style.fontSizeHalfPoints != null && style.fontSizeHalfPoints > 0) css.add("font-size:" + trimFloat(style.fontSizeHalfPoints / 2.0d) + "pt");
        if (style.align != null) css.add("text-align:" + style.align);
        if (style.leftIndentTwips > 0) css.add("margin-left:" + trimFloat(style.leftIndentTwips / 20.0d) + "pt");
        if (style.rightIndentTwips > 0) css.add("margin-right:" + trimFloat(style.rightIndentTwips / 20.0d) + "pt");
        if (style.firstIndentTwips != 0) css.add("text-indent:" + trimFloat(style.firstIndentTwips / 20.0d) + "pt");
        if (style.spaceBeforeTwips > 0) css.add("margin-top:" + trimFloat(style.spaceBeforeTwips / 20.0d) + "pt");
        if (style.spaceAfterTwips > 0) css.add("margin-bottom:" + trimFloat(style.spaceAfterTwips / 20.0d) + "pt");
        if (style.lineSpacingTwips > 0) {
            if (style.lineSpacingMultiple) css.add("line-height:" + trimFloat(style.lineSpacingTwips / 240.0d));
            else css.add("line-height:" + trimFloat(style.lineSpacingTwips / 20.0d) + "pt");
        }
        if (style.vertical != null) css.add("vertical-align:" + style.vertical);
        if (style.direction != null) css.add("direction:" + style.direction);
        if (style.allCaps) css.add("text-transform:uppercase");
        if (style.smallCaps) css.add("font-variant:small-caps");
        if (style.hidden) css.add("display:none");
        if (style.letterSpacingTwips != 0) css.add("letter-spacing:" + trimFloat(style.letterSpacingTwips / 20.0d) + "pt");
        return String.join(";", css);
    }

    // ---------------------------------------------------------------------
    // HTML -> RTF
    // ---------------------------------------------------------------------

    private static final class HtmlRtfWriter {
        final StringBuilder body = new StringBuilder();
        final LinkedHashMap<String, Integer> colorIndexes = new LinkedHashMap<>();
        final LinkedHashMap<String, Integer> fontIndexes = new LinkedHashMap<>();
        final ArrayDeque<String> closingGroups = new ArrayDeque<>();
        final ArrayDeque<ListState> lists = new ArrayDeque<>();
        int skipDepth;
        boolean atStart = true;
        boolean firstTableCell = true;

        private static final class ListState {
            final boolean ordered;
            int index = 1;
            ListState(boolean ordered) { this.ordered = ordered; }
        }

        HtmlRtfWriter() {
            fontIndex("Microsoft Sans Serif");
        }

        void convert(String html) {
            Matcher m = TAG_RE.matcher(html == null ? "" : html);
            int pos = 0;
            while (m.find()) {
                if (m.start() > pos) appendText(html.substring(pos, m.start()));
                if (m.group(4) != null) {
                    appendText(m.group(4));
                } else {
                    boolean close = !m.group(1).isEmpty();
                    String name = m.group(2).toLowerCase(Locale.ROOT);
                    String attrText = m.group(3) == null ? "" : m.group(3);
                    boolean selfClosing = attrText.trim().endsWith("/");
                    Map<String, String> attrs = parseAttributes(attrText);
                    if (close) handleEnd(name);
                    else handleStart(name, attrs, selfClosing);
                }
                pos = m.end();
            }
            if (pos < (html == null ? 0 : html.length())) appendText(html.substring(pos));
        }

        String finish() {
            while (!closingGroups.isEmpty()) body.append(closingGroups.pop());
            StringBuilder rtf = new StringBuilder(body.length() + 256);
            rtf.append("{\\rtf1\\ansi\\ansicpg1252\\deff0");
            rtf.append("{\\fonttbl");
            for (Map.Entry<String, Integer> e : fontIndexes.entrySet()) {
                rtf.append("{\\f").append(e.getValue()).append("\\fnil ").append(RtfUtils.rtfEscapeText(e.getKey())).append(";}");
            }
            rtf.append("}");
            if (!colorIndexes.isEmpty()) {
                rtf.append("{\\colortbl ;");
                for (String color : colorIndexes.keySet()) {
                    int[] rgb = hexToRgb(color);
                    rtf.append("\\red").append(rgb[0]).append("\\green").append(rgb[1]).append("\\blue").append(rgb[2]).append(';');
                }
                rtf.append("}");
            }
            rtf.append("\\viewkind4\\uc1\\pard\\f0\\fs20 ");
            rtf.append(body);
            if (!body.toString().endsWith("\\par\n")) rtf.append("\\par");
            rtf.append('}');
            return rtf.toString();
        }

        private void handleStart(String name, Map<String, String> attrs, boolean selfClosing) {
            if (skipDepth > 0) {
                if (!selfClosing) skipDepth++;
                return;
            }
            String encodedField = firstAttr(attrs, "data-notizen-rtf-field");
            String encodedObject = firstAttr(attrs, "data-notizen-rtf-object");
            if (encodedField != null) {
                body.append(new String(base64Decode(encodedField), StandardCharsets.UTF_8));
                if (!selfClosing) skipDepth = 1;
                return;
            }
            if (encodedObject != null) {
                body.append(new String(base64Decode(encodedObject), StandardCharsets.UTF_8));
                if (!selfClosing) skipDepth = 1;
                return;
            }
            if ("img".equals(name)) {
                appendImage(attrs);
                return;
            }
            if ("br".equals(name)) {
                body.append("\\line ");
                return;
            }
            if ("hr".equals(name)) {
                body.append("\\par----------------\\par\n");
                return;
            }
            if ("ol".equals(name) || "ul".equals(name)) {
                lists.push(new ListState("ol".equals(name)));
                return;
            }
            if ("li".equals(name)) {
                if (!atStart && !body.toString().endsWith("\\par\n")) body.append("\\par\n");
                ListState list = lists.peek();
                if (list != null && list.ordered) body.append(list.index++).append(". ");
                else body.append("\\bullet ");
                atStart = false;
                return;
            }
            if ("table".equals(name) || "tbody".equals(name) || "thead".equals(name)) {
                return;
            }
            if ("tr".equals(name)) {
                firstTableCell = true;
                return;
            }
            if ("td".equals(name) || "th".equals(name)) {
                if (!firstTableCell) body.append("\\tab ");
                firstTableCell = false;
                return;
            }
            if ("p".equals(name) || "div".equals(name) || "section".equals(name) || "body".equals(name)) {
                if (!atStart) body.append("\\par\n");
                String controls = blockControls(name, attrs);
                startGroup(controls);
                return;
            }
            if ("center".equals(name)) {
                if (!atStart) body.append("\\par\n");
                startGroup("\\pard\\qc ");
                return;
            }
            if ("blockquote".equals(name)) {
                if (!atStart) body.append("\\par\n");
                startGroup("\\pard\\li720\\ri720 ");
                return;
            }
            if (name.matches("h[1-6]")) {
                if (!atStart) body.append("\\par\n");
                int level = Integer.parseInt(name.substring(1));
                int size = Math.max(20, 44 - (level * 4));
                String controls = "\\pard" + alignmentControl(attrs) + "\\b\\fs" + size + " ";
                startGroup(controls);
                return;
            }
            if ("a".equals(name)) {
                String href = firstAttr(attrs, "href");
                if (href != null && !href.isEmpty()) {
                    body.append("{\\field{\\*\\fldinst HYPERLINK \"").append(RtfUtils.rtfEscapeText(href)).append("\"}{\\fldrslt ");
                    closingGroups.push("}}");
                    return;
                }
            }
            if ("b".equals(name) || "strong".equals(name)) { startGroup("\\b "); return; }
            if ("i".equals(name) || "em".equals(name)) { startGroup("\\i "); return; }
            if ("u".equals(name) || "ins".equals(name)) { startGroup("\\ul "); return; }
            if ("s".equals(name) || "strike".equals(name) || "del".equals(name)) { startGroup("\\strike "); return; }
            if ("sub".equals(name)) { startGroup("\\dn6 "); return; }
            if ("sup".equals(name)) { startGroup("\\up6 "); return; }
            if ("big".equals(name)) { startGroup("\\fs28 "); return; }
            if ("small".equals(name)) { startGroup("\\fs16 "); return; }
            if ("code".equals(name) || "pre".equals(name) || "tt".equals(name) || "kbd".equals(name) || "samp".equals(name)) { startGroup("\\f" + fontIndex("Courier New") + " "); return; }
            if ("font".equals(name) || "span".equals(name)) {
                String controls = inlineControls(name, attrs);
                if (!controls.isEmpty()) startGroup(controls + " ");
                else if (!selfClosing) closingGroups.push("");
            }
        }

        private void handleEnd(String name) {
            if (skipDepth > 0) {
                skipDepth--;
                return;
            }
            if ("ol".equals(name) || "ul".equals(name)) {
                if (!lists.isEmpty()) lists.pop();
                return;
            }
            if ("li".equals(name)) {
                body.append("\\par\n");
                return;
            }
            if ("tr".equals(name)) {
                body.append("\\par\n");
                return;
            }
            if ("td".equals(name) || "th".equals(name) || "table".equals(name) || "tbody".equals(name) || "thead".equals(name)) {
                return;
            }
            if ("p".equals(name) || "div".equals(name) || "section".equals(name) || "center".equals(name) || "blockquote".equals(name) || name.matches("h[1-6]")) {
                closeGroup();
                body.append("\\par\n");
                return;
            }
            if ("body".equals(name)) {
                closeGroup();
                return;
            }
            if ("a".equals(name) || "b".equals(name) || "strong".equals(name) || "i".equals(name) || "em".equals(name)
                    || "u".equals(name) || "ins".equals(name) || "s".equals(name) || "strike".equals(name) || "del".equals(name)
                    || "sub".equals(name) || "sup".equals(name) || "big".equals(name) || "small".equals(name) || "code".equals(name)
                    || "pre".equals(name) || "tt".equals(name) || "kbd".equals(name) || "samp".equals(name) || "font".equals(name) || "span".equals(name)) {
                closeGroup();
            }
        }

        private void startGroup(String controls) {
            body.append('{').append(controls == null ? "" : controls);
            closingGroups.push("}");
            atStart = false;
        }

        private void closeGroup() {
            if (!closingGroups.isEmpty()) body.append(closingGroups.pop());
        }

        private void appendText(String text) {
            if (skipDepth > 0 || text == null || text.isEmpty()) return;
            String decoded = decodeEntities(text);
            if (decoded.isEmpty()) return;
            body.append(RtfUtils.rtfEscapeMultiline(decoded));
            atStart = false;
        }

        private void appendImage(Map<String, String> attrs) {
            String src = firstAttr(attrs, "src");
            if (src == null) return;
            Matcher m = Pattern.compile("(?is)^data:([^;,]+);base64,(.*)$").matcher(src.trim());
            if (!m.find()) return;
            byte[] data = base64Decode(m.group(2).replaceAll("\\s+", ""));
            String pict = RtfUtils.rtfPictureFromImage(data, m.group(1));
            if (pict != null) {
                body.append(pict).append("\\line ");
                atStart = false;
            }
        }

        private String blockControls(String name, Map<String, String> attrs) {
            Css css = Css.parse(firstAttr(attrs, "style"));
            StringBuilder c = new StringBuilder("\\pard");
            c.append(alignmentControl(attrs));
            String textAlign = css.get("text-align");
            if (textAlign != null) c.append(alignmentFromCss(textAlign));
            int[] margin = css.marginTwips();
            if (margin[0] > 0) c.append("\\sb").append(margin[0]);
            if (margin[1] > 0) c.append("\\ri").append(margin[1]);
            if (margin[2] > 0) c.append("\\sa").append(margin[2]);
            if (margin[3] > 0) c.append("\\li").append(margin[3]);
            String indent = css.get("-qt-block-indent");
            if (indent != null) c.append("\\li").append(Math.max(0, toInt(indent, 0)) * 720);
            String paddingLeft = css.get("padding-left");
            if (paddingLeft != null) c.append("\\li").append(Math.max(0, toTwips(paddingLeft)));
            String textIndent = css.get("text-indent");
            if (textIndent != null) c.append("\\fi").append(toTwips(textIndent));
            String lineHeight = css.get("line-height");
            if (lineHeight != null) appendLineHeight(c, lineHeight);
            String dir = firstNonEmpty(firstAttr(attrs, "dir"), css.get("direction"));
            if ("rtl".equalsIgnoreCase(trim(dir))) c.append("\\rtlpar\\rtlch");
            else if ("ltr".equalsIgnoreCase(trim(dir))) c.append("\\ltrpar\\ltrch");
            String textColor = parseCssColor(firstAttr(attrs, "text"));
            if (textColor != null) c.append("\\cf").append(colorIndex(textColor));
            String bg = firstNonEmpty(parseCssColor(firstAttr(attrs, "bgcolor")), css.color("background-color", "background"));
            if (bg != null) c.append("\\highlight").append(colorIndex(bg));
            c.append(' ');
            String inline = inlineControls(name, attrs);
            if (!inline.isEmpty()) c.append(inline).append(' ');
            return c.toString();
        }

        private String inlineControls(String name, Map<String, String> attrs) {
            Css css = Css.parse(firstAttr(attrs, "style"));
            StringBuilder c = new StringBuilder();
            String tag = name == null ? "" : name.toLowerCase(Locale.ROOT);
            if ("font".equals(tag)) {
                String face = firstAttr(attrs, "face");
                String color = parseCssColor(firstAttr(attrs, "color"));
                String size = firstAttr(attrs, "size");
                if (face != null && !face.isEmpty()) c.append("\\f").append(fontIndex(face));
                if (color != null) c.append("\\cf").append(colorIndex(color));
                if (size != null) c.append("\\fs").append(fontSizeFromHtmlFont(size));
            }
            String fg = css.color("color");
            String bg = css.color("background-color", "background");
            String family = css.get("font-family");
            String fontSize = css.get("font-size");
            String decoration = css.get("text-decoration");
            String weight = css.get("font-weight");
            String style = css.get("font-style");
            String variant = css.get("font-variant");
            String transform = css.get("text-transform");
            String display = css.get("display");
            String visibility = css.get("visibility");
            String dir = firstNonEmpty(firstAttr(attrs, "dir"), css.get("direction"));
            String letterSpacing = css.get("letter-spacing");
            if (fg != null) c.append("\\cf").append(colorIndex(fg));
            if (bg != null) c.append("\\highlight").append(colorIndex(bg));
            if (family != null && !family.isEmpty()) c.append("\\f").append(fontIndex(stripQuotes(family.split(",")[0].trim())));
            if (fontSize != null) c.append("\\fs").append(Math.max(2, Math.round(toPoints(fontSize, 10) * 2)));
            if (decoration != null) {
                String d = decoration.toLowerCase(Locale.ROOT);
                if (d.contains("underline")) c.append("\\ul");
                if (d.contains("line-through")) c.append("\\strike");
            }
            if (weight != null && (weight.toLowerCase(Locale.ROOT).contains("bold") || toInt(weight, 0) >= 600)) c.append("\\b");
            if (style != null && style.toLowerCase(Locale.ROOT).contains("italic")) c.append("\\i");
            if (variant != null && variant.toLowerCase(Locale.ROOT).contains("small-caps")) c.append("\\scaps");
            if (transform != null && transform.toLowerCase(Locale.ROOT).contains("uppercase")) c.append("\\caps");
            if (display != null && display.toLowerCase(Locale.ROOT).contains("none")) c.append("\\v");
            if (visibility != null && visibility.toLowerCase(Locale.ROOT).contains("hidden")) c.append("\\v");
            if ("rtl".equalsIgnoreCase(trim(dir))) c.append("\\rtlch");
            else if ("ltr".equalsIgnoreCase(trim(dir))) c.append("\\ltrch");
            if (letterSpacing != null) c.append("\\expndtw").append(Math.round(toPoints(letterSpacing, 0) * 20));
            return c.toString();
        }

        private String alignmentControl(Map<String, String> attrs) {
            String align = firstAttr(attrs, "align");
            return alignmentFromCss(align);
        }

        private String alignmentFromCss(String align) {
            if (align == null) return "";
            String a = align.trim().toLowerCase(Locale.ROOT);
            if (a.contains("center")) return "\\qc";
            if (a.contains("right")) return "\\qr";
            if (a.contains("justify")) return "\\qj";
            if (a.contains("left")) return "\\ql";
            return "";
        }

        private void appendLineHeight(StringBuilder c, String value) {
            String v = trim(value).toLowerCase(Locale.ROOT);
            if (v.endsWith("%")) {
                double pct = toDouble(v.substring(0, v.length() - 1), 100d);
                c.append("\\sl").append(Math.round(240d * pct / 100d)).append("\\slmult1");
            } else {
                c.append("\\sl").append(Math.round(toPoints(v, 12) * 20));
            }
        }

        private int fontIndex(String name) {
            String n = name == null || name.trim().isEmpty() ? "Microsoft Sans Serif" : stripQuotes(name.trim());
            Integer existing = fontIndexes.get(n);
            if (existing != null) return existing;
            int idx = fontIndexes.size();
            fontIndexes.put(n, idx);
            return idx;
        }

        private int colorIndex(String color) {
            String hex = parseCssColor(color);
            if (hex == null) hex = "#000000";
            Integer existing = colorIndexes.get(hex);
            if (existing != null) return existing;
            int idx = colorIndexes.size() + 1;
            colorIndexes.put(hex, idx);
            return idx;
        }
    }

    private static final class Css {
        final Map<String, String> values = new LinkedHashMap<>();

        static Css parse(String style) {
            Css css = new Css();
            if (style == null) return css;
            for (String part : style.split(";")) {
                int idx = part.indexOf(':');
                if (idx <= 0) continue;
                String key = part.substring(0, idx).trim().toLowerCase(Locale.ROOT);
                String value = part.substring(idx + 1).trim();
                if (!key.isEmpty()) css.values.put(key, value);
            }
            return css;
        }

        String get(String key) { return key == null ? null : values.get(key.toLowerCase(Locale.ROOT)); }

        String color(String... keys) {
            for (String key : keys) {
                String value = parseCssColor(get(key));
                if (value != null) return value;
            }
            return null;
        }

        int[] marginTwips() {
            int[] out = new int[]{0,0,0,0};
            String margin = get("margin");
            if (margin != null) {
                String[] pieces = margin.trim().split("\\s+");
                if (pieces.length == 1) {
                    int v = toTwips(pieces[0]); out[0] = out[1] = out[2] = out[3] = v;
                } else if (pieces.length == 2) {
                    int v = toTwips(pieces[0]); int h = toTwips(pieces[1]); out[0] = out[2] = v; out[1] = out[3] = h;
                } else if (pieces.length == 3) {
                    out[0] = toTwips(pieces[0]); out[1] = out[3] = toTwips(pieces[1]); out[2] = toTwips(pieces[2]);
                } else if (pieces.length >= 4) {
                    out[0] = toTwips(pieces[0]); out[1] = toTwips(pieces[1]); out[2] = toTwips(pieces[2]); out[3] = toTwips(pieces[3]);
                }
            }
            override(out, 0, "margin-top");
            override(out, 1, "margin-right");
            override(out, 2, "margin-bottom");
            override(out, 3, "margin-left");
            return out;
        }

        private void override(int[] out, int index, String key) {
            String value = get(key);
            if (value != null) out[index] = toTwips(value);
        }
    }

    // ---------------------------------------------------------------------
    // Shared parsing helpers
    // ---------------------------------------------------------------------

    private static Map<String, String> parseAttributes(String raw) {
        LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
        Matcher m = ATTR_RE.matcher(raw == null ? "" : raw);
        while (m.find()) {
            String value = m.group(2) != null ? m.group(2) : (m.group(3) != null ? m.group(3) : m.group(4));
            attrs.put(m.group(1).toLowerCase(Locale.ROOT), decodeEntities(value == null ? "" : value));
        }
        return attrs;
    }

    private static String firstAttr(Map<String, String> attrs, String key) {
        if (attrs == null || key == null) return null;
        return attrs.get(key.toLowerCase(Locale.ROOT));
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

    private static Map<Integer, String> extractFontTable(String rtf) {
        return RtfContentParser.extractFontTable(rtf);
    }

    private static Map<Integer, String> extractColorTable(String rtf) {
        return RtfContentParser.extractColorTable(rtf);
    }

    private static int clamp255(int v) { return Math.max(0, Math.min(255, v)); }


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
                        // delimiter consumed by for-loop increment
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

    private static String largeImagePlaceholderHtml(int estimatedBytes) {
        String suffix = estimatedBytes > 0 ? " (" + Math.max(1, estimatedBytes / 1024) + " KB)" : "";
        return "<span class=\"notizen-image-placeholder\">" + RtfUtils.escapeXml(RtfUtils.LEGACY_IMAGE_PLACEHOLDER + " zu groß" + suffix) + "</span>";
    }

    private static RtfUtils.RtfImage parsePictGroup(String group) {
        if (group == null || group.isEmpty()) return null;
        String mimeType;
        String rtfControl;
        boolean wantsBmpWrap = false;
        if (group.contains("\\pngblip")) { mimeType = "image/png"; rtfControl = "pngblip"; }
        else if (group.contains("\\jpegblip") || group.contains("\\jpgblip")) { mimeType = "image/jpeg"; rtfControl = "jpegblip"; }
        else if (group.contains("\\dibitmap") || group.contains("\\wbitmap")) { mimeType = "image/bmp"; rtfControl = "dibitmap0"; wantsBmpWrap = true; }
        else if (group.contains("\\emfblip")) { mimeType = "image/x-emf"; rtfControl = "emfblip"; }
        else {
            Matcher wmf = Pattern.compile("\\\\wmetafile(-?\\d+)?").matcher(group);
            if (wmf.find()) { mimeType = "image/wmf"; rtfControl = "wmetafile" + (wmf.group(1) == null ? "8" : wmf.group(1)); }
            else return null;
        }
        String scrubbed = group.replaceAll("\\\\'[0-9a-fA-F]{2}", " ").replaceAll("\\\\[a-zA-Z]+-?\\d* ?", " ");
        String hex = scrubbed.replaceAll("[^0-9a-fA-F]", "");
        if ((hex.length() & 1) == 1) hex = hex.substring(0, hex.length() - 1);
        if (hex.isEmpty()) return null;
        byte[] data = RtfUtils.hexToBytes(hex);
        if (wantsBmpWrap) {
            byte[] wrapped = RtfUtils.dibToBmpBytes(data);
            if (wrapped != null) data = wrapped;
        }
        return new RtfUtils.RtfImage(mimeType, data, controlNumber(group, "picwgoal", 0), controlNumber(group, "pichgoal", 0), rtfControl, group);
    }

    private static int controlNumber(String group, String name, int fallback) {
        Matcher m = Pattern.compile("\\\\" + Pattern.quote(name) + "(-?\\d+)").matcher(group == null ? "" : group);
        if (!m.find()) return fallback;
        return toInt(m.group(1), fallback);
    }

    private static String imageToHtml(RtfUtils.RtfImage image) {
        StringBuilder out = new StringBuilder(image.data.length * 2 + 128);
        out.append("<img src=\"data:").append(RtfUtils.escapeXml(image.mimeType)).append(";base64,").append(base64Encode(image.data)).append("\"");
        if (image.widthTwips > 0) out.append(" width=\"").append(Math.max(1, Math.round(image.widthTwips / 15.0f))).append("\"");
        if (image.heightTwips > 0) out.append(" height=\"").append(Math.max(1, Math.round(image.heightTwips / 15.0f))).append("\"");
        out.append("/>");
        return out.toString();
    }

    private static String fieldToHtml(String group) {
        String display = extractFieldResultText(group);
        Matcher m = HYPERLINK_FIELD.matcher(group == null ? "" : group);
        if (m.find()) {
            String url = m.group(1) != null ? m.group(1) : (m.group(2) == null ? "" : m.group(2));
            url = url.replace("\\\"", "\"").replace("\\\\", "\\");
            String label = display.isEmpty() ? url : display;
            return "<a href=\"" + RtfUtils.escapeXml(url) + "\">" + RtfUtils.plainTextToHtml(label) + "</a>";
        }
        String encoded = base64Encode((group == null ? "" : group).getBytes(StandardCharsets.UTF_8));
        return "<span data-notizen-rtf-field=\"" + encoded + "\">" + RtfUtils.plainTextToHtml(display) + "</span>";
    }

    private static String objectToHtml(String group) {
        String className = extractObjectClassName(group);
        String encoded = base64Encode((group == null ? "" : group).getBytes(StandardCharsets.UTF_8));
        String title = className.isEmpty() ? "" : " title=\"" + RtfUtils.escapeXml(className) + "\"";
        return "<span data-notizen-rtf-object=\"" + encoded + "\"" + title + ">" + RtfUtils.escapeXml(RtfUtils.LEGACY_OBJECT_PLACEHOLDER) + "</span>";
    }

    private static String extractFieldResultText(String group) {
        if (group == null) return "";
        int start = group.indexOf("{\\fldrslt");
        if (start < 0) return "";
        int end = RtfUtils.findGroupEnd(group, start);
        if (end < 0) return "";
        return RtfUtils.rtfToPlainText(group.substring(start, end + 1)).trim();
    }

    private static String extractObjectClassName(String group) {
        if (group == null) return "";
        Matcher m = Pattern.compile("\\\\objclass\\s+([^{}\\\\]+)").matcher(group);
        if (!m.find()) return "";
        return m.group(1).trim().replaceAll("[;}]+$", "").replaceAll("\\\\'[0-9a-fA-F]{2}", " ").trim();
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) return a;
        if (b != null && !b.trim().isEmpty()) return b;
        return null;
    }

    private static String parseCssColor(String value) {
        if (value == null) return null;
        String raw = value.trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty() || "transparent".equals(raw) || "none".equals(raw)) return null;
        if (raw.startsWith("#")) {
            String h = raw.substring(1).replaceAll("[^0-9a-f]", "");
            if (h.length() == 3) h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2);
            else if (h.length() == 4) h = "" + h.charAt(1) + h.charAt(1) + h.charAt(2) + h.charAt(2) + h.charAt(3) + h.charAt(3);
            else if (h.length() == 8) h = h.substring(2, 8); // legacy Qt-style #AARRGGBB
            if (h.length() >= 6) return "#" + h.substring(0, 6);
        }
        Matcher rgb = Pattern.compile("rgba?\\(([^)]*)\\)").matcher(raw);
        if (rgb.find()) {
            String[] parts = rgb.group(1).split(",");
            if (parts.length >= 3) {
                int r = cssColorComponent(parts[0]);
                int g = cssColorComponent(parts[1]);
                int b = cssColorComponent(parts[2]);
                return String.format(Locale.ROOT, "#%02x%02x%02x", r, g, b);
            }
        }
        String named = CSS_NAMED_COLORS.get(raw.replace(" ", ""));
        if (named != null) return named;
        return null;
    }

    private static int cssColorComponent(String raw) {
        String s = trim(raw);
        if (s.endsWith("%")) return clamp255((int)Math.round(toDouble(s.substring(0, s.length() - 1), 0) * 2.55d));
        return clamp255(toInt(s, 0));
    }

    private static int[] hexToRgb(String hex) {
        String h = parseCssColor(hex);
        if (h == null) h = "#000000";
        return new int[]{Integer.parseInt(h.substring(1,3), 16), Integer.parseInt(h.substring(3,5), 16), Integer.parseInt(h.substring(5,7), 16)};
    }

    private static int fontSizeFromHtmlFont(String size) {
        String s = trim(size);
        if (s.startsWith("+")) return Math.max(2, 20 + (toInt(s.substring(1), 0) * 4));
        if (s.startsWith("-")) return Math.max(2, 20 - (toInt(s.substring(1), 0) * 4));
        int n = toInt(s, 3);
        int[] pt = {16, 20, 24, 28, 36, 48, 64};
        if (n >= 1 && n <= pt.length) return pt[n - 1];
        return Math.max(2, n * 2);
    }

    private static int toTwips(String cssValue) { return (int)Math.round(toPoints(cssValue, 0) * 20); }

    private static double toPoints(String cssValue, double fallback) {
        String s = trim(cssValue).toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return fallback;
        double value = toDouble(s.replaceAll("[^0-9+.,-].*$", ""), fallback);
        if (s.contains("px")) return value * 0.75d;
        if (s.contains("em")) return value * 12d;
        if (s.contains("cm")) return value * 28.3464567d;
        if (s.contains("mm")) return value * 2.83464567d;
        if (s.contains("in")) return value * 72d;
        if (s.contains("pc")) return value * 12d;
        return value;
    }

    private static int toInt(String value, int fallback) {
        try { return (int)Math.round(Double.parseDouble(trim(value).replace(',', '.'))); }
        catch (Exception e) { return fallback; }
    }

    private static double toDouble(String value, double fallback) {
        try { return Double.parseDouble(trim(value).replace(',', '.')); }
        catch (Exception e) { return fallback; }
    }

    private static String trim(String s) { return s == null ? "" : s.trim(); }

    private static String stripQuotes(String s) {
        String out = trim(s);
        while ((out.startsWith("\"") && out.endsWith("\"")) || (out.startsWith("'") && out.endsWith("'"))) out = out.substring(1, out.length() - 1).trim();
        return out;
    }

    private static String htmlAttr(String s) {
        return (s == null ? "" : s).replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String trimFloat(double value) {
        if (Math.abs(value - Math.round(value)) < 0.0001d) return Long.toString(Math.round(value));
        String s = String.format(Locale.ROOT, "%.3f", value);
        while (s.contains(".") && s.endsWith("0")) s = s.substring(0, s.length() - 1);
        if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static String decodeEntities(String text) {
        if (text == null || text.indexOf('&') < 0) return text == null ? "" : text;
        String s = text.replace("&nbsp;", "\u00a0").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'");
        Matcher hex = Pattern.compile("&#x([0-9a-fA-F]+);").matcher(s);
        StringBuffer sb = new StringBuffer();
        while (hex.find()) {
            int cp = Integer.parseInt(hex.group(1), 16);
            hex.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(cp))));
        }
        hex.appendTail(sb);
        Matcher dec = Pattern.compile("&#(\\d+);").matcher(sb.toString());
        sb = new StringBuffer();
        while (dec.find()) {
            int cp = Integer.parseInt(dec.group(1));
            dec.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(cp))));
        }
        dec.appendTail(sb);
        return sb.toString();
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

    private static byte[] base64Decode(String text) {
        String s = text == null ? "" : text.replaceAll("\\s+", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(0, s.length() * 3 / 4));
        int[] quad = new int[4];
        int q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int v;
            if (c >= 'A' && c <= 'Z') v = c - 'A';
            else if (c >= 'a' && c <= 'z') v = c - 'a' + 26;
            else if (c >= '0' && c <= '9') v = c - '0' + 52;
            else if (c == '+') v = 62;
            else if (c == '/') v = 63;
            else if (c == '=') v = -2;
            else continue;
            quad[q++] = v;
            if (q == 4) {
                int b0 = (quad[0] << 2) | ((quad[1] & 0x30) >>> 4);
                out.write(b0 & 0xff);
                if (quad[2] != -2) {
                    int b1 = ((quad[1] & 0x0f) << 4) | ((quad[2] & 0x3c) >>> 2);
                    out.write(b1 & 0xff);
                }
                if (quad[3] != -2) {
                    int b2 = ((quad[2] & 0x03) << 6) | quad[3];
                    out.write(b2 & 0xff);
                }
                q = 0;
            }
        }
        return out.toByteArray();
    }
}
