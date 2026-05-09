package de.notizen.android.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Exporters {
    private Exporters() {}

    public static String treeToPlainText(NoteNode root, ExportOptions options) {
        if (root == null) return "";
        ExportOptions opt = options == null ? new ExportOptions() : options;
        ArrayList<String> parts = new ArrayList<>();
        walkNumbered(root, 0, new ArrayList<Integer>(), opt, parts);
        String text = String.join("\n", parts).replaceAll("[\n\\s]+$", "") + "\n";
        return text;
    }

    private static void walkNumbered(NoteNode node, int depth, List<Integer> counters, ExportOptions opt, List<String> parts) {
        while (counters.size() <= depth) counters.add(0);
        while (counters.size() > depth + 1) counters.remove(counters.size() - 1);
        counters.set(depth, counters.get(depth) + 1);

        String heading = node.title == null ? "" : node.title;
        if (opt.numberedHeadings && (depth > 0 || opt.includeRootNumber)) {
            int start = opt.includeRootNumber ? 0 : 1;
            StringBuilder number = new StringBuilder();
            for (int i = start; i <= depth; i++) number.append(counters.get(i)).append('.');
            heading = number + " " + heading;
        }
        String body = RtfUtils.rtfToPlainText(node.rtf == null ? "" : node.rtf).replaceAll("\\s+$", "");
        if (!heading.isEmpty() || opt.includeEmptyNotes) {
            parts.add(heading);
            for (int i = 1; i < opt.titleSeparatorBlankLines; i++) parts.add("");
        }
        if (!body.isEmpty() || opt.includeEmptyNotes) {
            if (!body.isEmpty()) parts.add(body);
            for (int i = 0; i < opt.bodySeparatorBlankLines; i++) parts.add("");
        }
        for (NoteNode child : node.children) walkNumbered(child, depth + 1, counters, opt, parts);
    }

    public static byte[] treeToTextBytes(NoteNode root, String encoding) {
        String text = treeToPlainText(root, new ExportOptions());
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').replace("\n", "\r\n");
        if (encoding == null) encoding = "utf-8";
        String e = encoding.toLowerCase().replace('_', '-');
        if (e.equals("ansi") || e.equals("cp1252") || e.equals("windows-1252")) {
            return normalized.getBytes(java.nio.charset.Charset.forName("windows-1252"));
        }
        if (e.equals("unicode") || e.equals("utf-16") || e.equals("utf16")) {
            return normalized.getBytes(java.nio.charset.StandardCharsets.UTF_16);
        }
        return normalized.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public static NoteNode unifiedNote(NoteNode root, String title) {
        String cleanTitle = title == null || title.isEmpty() ? "Gesamt" : title;
        return new NoteNode(cleanTitle, treeToRtf(root));
    }

    public static String treeToHtml(NoteNode root) {
        if (root == null) return "";
        StringBuilder out = new StringBuilder();
        out.append("<!doctype html>\n<html lang=\"de\">\n<head>\n");
        out.append("<meta charset=\"utf-8\"/>\n");
        out.append("<title>").append(RtfUtils.escapeXml(root.title == null || root.title.isEmpty() ? "Notizen" : root.title)).append("</title>\n");
        out.append("<style>\n");
        out.append("body{font-family:Arial,Helvetica,sans-serif;white-space:normal;margin:2rem;}\n");
        out.append(".notizen-node{margin:0 0 1.2rem 0;}\n");
        out.append(".notizen-body{white-space:pre-wrap;}\n");
        out.append(".notizen-rtf-table{border-collapse:collapse;margin:.25em 0;}\n");
        out.append(".notizen-rtf-table td{border:1px solid #ccc;padding:2px 6px;vertical-align:top;}\n");
        out.append(".notizen-body img{max-width:100%;height:auto;}\n");
        out.append(".notizen-body span[data-notizen-rtf-object]{border:1px solid #bbb;border-radius:4px;padding:2px 5px;background:#eee;}\n");
        out.append("h1,h2,h3,h4,h5,h6{margin:1rem 0 .35rem 0;}\n");
        out.append("</style>\n</head>\n<body>\n");
        htmlWalk(root, 0, new ArrayList<Integer>(), new ExportOptions(), out);
        out.append("</body>\n</html>\n");
        return out.toString();
    }

    private static void htmlWalk(NoteNode node, int depth, List<Integer> counters, ExportOptions opt, StringBuilder out) {
        while (counters.size() <= depth) counters.add(0);
        while (counters.size() > depth + 1) counters.remove(counters.size() - 1);
        counters.set(depth, counters.get(depth) + 1);
        String heading = node.title == null ? "" : node.title;
        if (opt.numberedHeadings && (depth > 0 || opt.includeRootNumber)) {
            int start = opt.includeRootNumber ? 0 : 1;
            StringBuilder number = new StringBuilder();
            for (int i = start; i <= depth; i++) number.append(counters.get(i)).append('.');
            heading = number + " " + heading;
        }
        int level = Math.min(6, Math.max(1, depth + 1));
        out.append("<section class=\"notizen-node\"><h").append(level).append('>').append(RtfUtils.escapeXml(heading)).append("</h").append(level).append(">\n");
        out.append("<div class=\"notizen-body\">").append(RtfUtils.rtfToHtml(node.rtf)).append("</div></section>\n");
        for (NoteNode child : node.children) htmlWalk(child, depth + 1, counters, opt, out);
    }


    public static String treeToRtf(NoteNode root) {
        if (root == null) return "{\\rtf1\\ansi }";
        ExportOptions opt = new ExportOptions();
        ArrayList<RtfExportEntry> entries = new ArrayList<>();
        collectRtfEntries(root, 0, new ArrayList<Integer>(), opt, entries);
        Map<String, Integer> fonts = collectRtfFonts(entries);
        Map<String, Integer> colors = collectRtfColors(entries);
        StringBuilder body = new StringBuilder();
        for (RtfExportEntry entry : entries) {
            body.append("{\\b\\fs28 ").append(rtfEscape(entry.heading)).append("}\\par\n");
            body.append("\\par\n");
            if (!entry.parts.isEmpty()) {
                for (RtfContentPart part : entry.parts) body.append(emitRtfPart(part, colors, fonts));
                body.append("\\par\n");
            }
            body.append("\\par\n");
        }
        return "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1031"
                + rtfFontTable(fonts)
                + rtfColorTable(colors)
                + "\\viewkind4\\uc1\\pard\\f0\\fs17 "
                + body.toString().trim()
                + "\n}\n";
    }

    private static final class RtfExportEntry {
        final String heading;
        final ArrayList<RtfContentPart> parts;
        RtfExportEntry(String heading, ArrayList<RtfContentPart> parts) {
            this.heading = heading == null ? "" : heading;
            this.parts = parts == null ? new ArrayList<RtfContentPart>() : parts;
        }
    }

    private static void collectRtfEntries(NoteNode node, int depth, List<Integer> counters, ExportOptions opt, List<RtfExportEntry> entries) {
        while (counters.size() <= depth) counters.add(0);
        while (counters.size() > depth + 1) counters.remove(counters.size() - 1);
        counters.set(depth, counters.get(depth) + 1);
        String heading = node.title == null ? "" : node.title;
        if (opt.numberedHeadings && (depth > 0 || opt.includeRootNumber)) {
            int start = opt.includeRootNumber ? 0 : 1;
            StringBuilder number = new StringBuilder();
            for (int i = start; i <= depth; i++) number.append(counters.get(i)).append('.');
            heading = number + " " + heading;
        }
        entries.add(new RtfExportEntry(heading, stripTrailingTextParts(RtfUtils.rtfToContentParts(node.rtf == null ? "" : node.rtf))));
        for (NoteNode child : node.children) collectRtfEntries(child, depth + 1, counters, opt, entries);
    }

    private static ArrayList<RtfContentPart> stripTrailingTextParts(List<RtfContentPart> parts) {
        ArrayList<RtfContentPart> stripped = new ArrayList<>(parts == null ? new ArrayList<RtfContentPart>() : parts);
        while (!stripped.isEmpty() && isPlainTextPart(stripped.get(stripped.size() - 1)) && stripped.get(stripped.size() - 1).text.trim().isEmpty()) {
            stripped.remove(stripped.size() - 1);
        }
        if (!stripped.isEmpty() && isPlainTextPart(stripped.get(stripped.size() - 1))) {
            RtfContentPart last = stripped.get(stripped.size() - 1);
            String trimmed = rstrip(last.text);
            if (!trimmed.equals(last.text)) stripped.set(stripped.size() - 1, new RtfContentPart(trimmed, "", last.style));
        }
        return stripped;
    }

    private static String rstrip(String value) {
        if (value == null || value.isEmpty()) return "";
        int end = value.length();
        while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) end--;
        return value.substring(0, end);
    }

    private static boolean isPlainTextPart(RtfContentPart part) {
        return part != null && !(part instanceof RtfImagePart) && !(part instanceof RtfHyperlink) && !(part instanceof RtfField) && !(part instanceof RtfObject)
                && (part.rtf == null || part.rtf.isEmpty());
    }

    private static Map<String, Integer> collectRtfFonts(List<RtfExportEntry> entries) {
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        for (RtfExportEntry entry : entries) {
            for (RtfContentPart part : entry.parts) {
                if (part instanceof RtfImagePart || part instanceof RtfField || part instanceof RtfObject) continue;
                String family = part.style == null ? null : part.style.fontFamily;
                if (family != null && !family.trim().isEmpty() && !out.containsKey(family)) out.put(family, out.size() + 1);
            }
        }
        return out;
    }

    private static Map<String, Integer> collectRtfColors(List<RtfExportEntry> entries) {
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        for (RtfExportEntry entry : entries) {
            for (RtfContentPart part : entry.parts) {
                if (part instanceof RtfImagePart || part instanceof RtfField || part instanceof RtfObject) continue;
                addColor(out, part.style == null ? null : part.style.fgColor);
                addColor(out, part.style == null ? null : part.style.bgColor);
            }
        }
        return out;
    }

    private static void addColor(Map<String, Integer> out, String color) {
        if (color == null) return;
        String c = color.trim().toLowerCase(Locale.ROOT);
        if (!c.matches("#[0-9a-f]{6}")) return;
        if (!out.containsKey(c)) out.put(c, out.size() + 1);
    }

    private static String rtfFontTable(Map<String, Integer> fonts) {
        StringBuilder out = new StringBuilder();
        out.append("{\\fonttbl{\\f0\\fnil\\fcharset0 Microsoft Sans Serif;}");
        for (Map.Entry<String, Integer> entry : fonts.entrySet()) {
            out.append("{\\f").append(entry.getValue()).append("\\fnil\\fcharset0 ")
                    .append(cleanRtfFontName(entry.getKey())).append(";}");
        }
        out.append("}\n");
        return out.toString();
    }

    private static String cleanRtfFontName(String name) {
        String cleaned = name == null ? "" : name.replace('\\', ' ').replace('{', ' ').replace('}', ' ').replace(';', ' ').trim();
        return cleaned.isEmpty() ? "Microsoft Sans Serif" : cleaned;
    }

    private static String rtfColorTable(Map<String, Integer> colors) {
        if (colors == null || colors.isEmpty()) return "";
        StringBuilder out = new StringBuilder("{\\colortbl ;");
        for (String color : colors.keySet()) {
            int r = Integer.parseInt(color.substring(1, 3), 16);
            int g = Integer.parseInt(color.substring(3, 5), 16);
            int b = Integer.parseInt(color.substring(5, 7), 16);
            out.append("\\red").append(r).append("\\green").append(g).append("\\blue").append(b).append(';');
        }
        out.append("}\n");
        return out.toString();
    }

    private static String emitRtfPart(RtfContentPart part, Map<String, Integer> colors, Map<String, Integer> fonts) {
        if (part == null) return "";
        if (part instanceof RtfImagePart) return part.rtf == null || part.rtf.isEmpty() ? rtfEscape(RtfUtils.LEGACY_IMAGE_PLACEHOLDER) : part.rtf;
        if (part instanceof RtfField) return part.rtf == null || part.rtf.isEmpty() ? rtfEscape(part.text) : part.rtf;
        if (part instanceof RtfObject) return part.rtf == null || part.rtf.isEmpty() ? rtfEscape(RtfUtils.LEGACY_OBJECT_PLACEHOLDER) : part.rtf;
        if (part instanceof RtfHyperlink) {
            RtfHyperlink link = (RtfHyperlink) part;
            if (link.url != null && !link.url.isEmpty()) return rtfHyperlinkField(link.url, link.text, rtfStylePrefix(link.style, colors, fonts));
            return part.rtf == null || part.rtf.isEmpty() ? emitRtfText(part.text, part.style, colors, fonts) : part.rtf;
        }
        return emitRtfText(part.text, part.style, colors, fonts);
    }

    private static String emitRtfText(String text, RtfTextStyle style, Map<String, Integer> colors, Map<String, Integer> fonts) {
        if (text == null || text.isEmpty()) return "";
        String escaped = rtfEscapeMultiline(text);
        String prefix = rtfStylePrefix(style, colors, fonts);
        if (prefix.isEmpty()) return escaped;
        return "{" + prefix + " " + escaped + "}";
    }

    private static String rtfStylePrefix(RtfTextStyle style, Map<String, Integer> colors, Map<String, Integer> fonts) {
        if (style == null) return "";
        StringBuilder out = new StringBuilder();
        if ("left".equals(style.align)) out.append("\\ql");
        else if ("center".equals(style.align)) out.append("\\qc");
        else if ("right".equals(style.align)) out.append("\\qr");
        else if ("justify".equals(style.align)) out.append("\\qj");
        if (style.leftIndentTwips != 0) out.append("\\li").append(style.leftIndentTwips);
        if (style.rightIndentTwips != 0) out.append("\\ri").append(style.rightIndentTwips);
        if (style.firstIndentTwips != 0) out.append("\\fi").append(style.firstIndentTwips);
        if (style.spaceBeforeTwips != 0) out.append("\\sb").append(Math.max(0, style.spaceBeforeTwips));
        if (style.spaceAfterTwips != 0) out.append("\\sa").append(Math.max(0, style.spaceAfterTwips));
        if (style.lineSpacingTwips > 0) out.append("\\sl").append(style.lineSpacingTwips);
        if (style.lineSpacingTwips > 0 || style.lineSpacingMultiple) out.append("\\slmult").append(style.lineSpacingMultiple ? 1 : 0);
        if ("rtl".equals(style.direction)) out.append("\\rtlpar\\rtlch");
        else if ("ltr".equals(style.direction)) out.append("\\ltrpar\\ltrch");
        if (style.fontFamily != null && fonts.containsKey(style.fontFamily)) out.append("\\f").append(fonts.get(style.fontFamily));
        if (style.bold) out.append("\\b");
        if (style.italic) out.append("\\i");
        if (style.underline) out.append("\\ul");
        if (style.strike) out.append("\\strike");
        if (style.allCaps) out.append("\\caps");
        if (style.smallCaps) out.append("\\scaps");
        if (style.hidden) out.append("\\v");
        if (style.letterSpacingTwips != 0) out.append("\\expndtw").append(style.letterSpacingTwips);
        if ("super".equals(style.vertical)) out.append("\\super");
        else if ("sub".equals(style.vertical)) out.append("\\sub");
        if (style.fontSizeHalfPoints != null && style.fontSizeHalfPoints > 0) out.append("\\fs").append(style.fontSizeHalfPoints);
        String fg = style.fgColor == null ? null : style.fgColor.toLowerCase(Locale.ROOT);
        String bg = style.bgColor == null ? null : style.bgColor.toLowerCase(Locale.ROOT);
        if (fg != null && colors.containsKey(fg)) out.append("\\cf").append(colors.get(fg));
        if (bg != null && colors.containsKey(bg)) out.append("\\highlight").append(colors.get(bg));
        return out.toString();
    }

    private static String rtfHyperlinkField(String url, String text, String prefix) {
        String safeUrl = (url == null ? "" : url).replace("\\", "\\\\").replace("\"", "\\\"");
        String label = text == null || text.isEmpty() ? safeUrl : text;
        String result = prefix == null || prefix.isEmpty() ? rtfEscape(label) : "{" + prefix + " " + rtfEscape(label) + "}";
        return "{\\field{\\*\\fldinst HYPERLINK \"" + safeUrl + "\"}{\\fldrslt " + result + "}}";
    }

    private static String rtfEscapeMultiline(String text) {
        return RtfUtils.rtfEscapeMultiline(text);
    }

    private static String rtfEscape(String text) {
        return RtfUtils.rtfEscapeText(text);
    }

}
