package de.notizen.android.core;

import java.util.List;
import java.util.Locale;

/**
 * Portable subset of the old WinForms RichTextBox/QTextEdit presentation rules.
 * The Android app uses it only as a WebView bridge; no Android APIs are needed.
 */
public final class LegacyRichTextBoxSemantics {
    public static final String DEFAULT_FONT = "Microsoft Sans Serif";
    public static final int DEFAULT_FONT_SIZE_HALF_POINTS = 16;
    public static final String CLIPBOARD_NODE_XML = "notizen-node-xml";
    public static final String CLIPBOARD_RTF = "text/rtf";
    public static final String CLIPBOARD_HTML = "text/html";
    public static final String CLIPBOARD_TEXT = "text/plain";

    public enum ClipboardFormat { NODE_XML, RTF, HTML, TEXT, NONE }

    public static final class Metrics {
        public final int hardParagraphs;
        public final int softLineBreaks;
        public final int tabs;
        public final int fields;
        public final int hyperlinks;
        public final int images;
        public final int objects;
        public final int textCharacters;

        public Metrics(int hardParagraphs, int softLineBreaks, int tabs, int fields, int hyperlinks, int images, int objects, int textCharacters) {
            this.hardParagraphs = Math.max(0, hardParagraphs);
            this.softLineBreaks = Math.max(0, softLineBreaks);
            this.tabs = Math.max(0, tabs);
            this.fields = Math.max(0, fields);
            this.hyperlinks = Math.max(0, hyperlinks);
            this.images = Math.max(0, images);
            this.objects = Math.max(0, objects);
            this.textCharacters = Math.max(0, textCharacters);
        }

        public String summaryGerman() {
            return "Absätze: " + hardParagraphs
                    + "\nweiche Zeilenumbrüche: " + softLineBreaks
                    + "\nTabs: " + tabs
                    + "\nFelder: " + fields
                    + "\nHyperlinks: " + hyperlinks
                    + "\nBilder: " + images
                    + "\nObjekte: " + objects
                    + "\nTextzeichen: " + textCharacters;
        }
    }

    private LegacyRichTextBoxSemantics() {}

    public static String baseCss(boolean desktopNote) {
        String lineHeight = desktopNote ? "line-height:100%;" : "line-height:1.35;";
        String padding = desktopNote ? "padding:0;" : "padding:12px;";
        String background = desktopNote ? "background:transparent;" : "background:#fff;";
        return "body{white-space:pre-wrap;margin:0;" + padding + background
                + "font-family:'" + DEFAULT_FONT + "',sans-serif;font-size:10pt;" + lineHeight + "color:#111;}"
                + "p{margin-top:0;margin-bottom:" + (desktopNote ? "0" : "0.65em") + ";}"
                + "img{max-width:100%;height:auto;}"
                + "table.notizen-rtf-table{border-collapse:collapse;margin:.25em 0;}"
                + "table.notizen-rtf-table td{border:1px solid #ccc;padding:2px 6px;vertical-align:top;}"
                + ".notizen-object{display:inline-block;border:1px solid #999;padding:2px 4px;border-radius:3px;background:#eee;color:#333;}";
    }

    public static String decorateHtmlDocument(String fragment, boolean desktopNote) {
        String body = fragment == null ? "" : fragment;
        return "<!doctype html><html><head><meta charset=\"utf-8\"/>"
                + "<style>" + baseCss(desktopNote) + "</style></head><body>" + body + "</body></html>";
    }

    public static String desktopNoteHtmlDocument(String rtf) {
        String fragment = RtfUtils.rtfToHtml(rtf == null ? "" : rtf)
                .replaceAll("(?is)<p\\b[^>]*>", "")
                .replaceAll("(?is)</p>\\s*", "<br/>")
                .replaceAll("(?is)(<br/>\\s*)+$", "")
                .replace("<br/>\n", "<br/>")
                .replace("\n", "");
        return decorateHtmlDocument(fragment, true);
    }

    public static boolean looksLikeLegacyRichTextBoxRtf(String rtf) {
        if (!RtfUtils.looksLikeRtf(rtf)) return false;
        String s = rtf.toLowerCase(Locale.ROOT);
        return s.contains("\\ansi") || s.contains("\\ansicpg") || s.contains("\\viewkind4") || s.contains("\\pard");
    }

    public static Metrics inspectRtf(String rtf) {
        String src = rtf == null ? "" : rtf;
        int hard = countControl(src, "par");
        int soft = countControl(src, "line");
        int tabs = countControl(src, "tab");
        int fields = 0, links = 0, images = 0, objects = 0;
        List<RtfContentPart> parts = RtfUtils.rtfToContentParts(src);
        for (RtfContentPart p : parts) {
            if (p instanceof RtfHyperlink) { fields++; links++; }
            else if (p instanceof RtfField) fields++;
            else if (p instanceof RtfImagePart) images++;
            else if (p instanceof RtfObject) objects++;
        }
        return new Metrics(hard, soft, tabs, fields, links, images, objects, RtfUtils.rtfToPlainText(src).length());
    }

    public static ClipboardFormat preferredClipboardFormat(boolean hasNodeXml, boolean hasRtf, boolean hasHtml, boolean hasText) {
        if (hasNodeXml) return ClipboardFormat.NODE_XML;
        if (hasRtf) return ClipboardFormat.RTF;
        if (hasHtml) return ClipboardFormat.HTML;
        if (hasText) return ClipboardFormat.TEXT;
        return ClipboardFormat.NONE;
    }

    private static int countControl(String src, String word) {
        if (src == null || src.isEmpty()) return 0;
        int count = 0;
        for (int i = 0; i < src.length(); i++) {
            if (src.charAt(i) != '\\') continue;
            int start = i + 1;
            if (start + word.length() > src.length()) continue;
            if (!src.regionMatches(start, word, 0, word.length())) continue;
            int end = start + word.length();
            if (end < src.length() && Character.isLetter(src.charAt(end))) continue;
            count++;
        }
        return count;
    }
}
