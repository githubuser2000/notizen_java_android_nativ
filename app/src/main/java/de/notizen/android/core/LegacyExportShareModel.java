package de.notizen.android.core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Android-friendly export/share model translated from the .NET/PyQt export paths. */
public final class LegacyExportShareModel {
    private LegacyExportShareModel() {}

    public enum Scope { CURRENT_NOTE, CURRENT_SUBTREE, ROOT }
    public enum Format { TXT_UTF8, TXT_ANSI, TXT_UNICODE, HTML, RTF }

    public static final class Payload {
        public final Scope scope;
        public final Format format;
        public final String title;
        public final String fileName;
        public final String mimeType;
        public final byte[] bytes;
        public final String textForShare;
        public final String status;

        public Payload(Scope scope, Format format, String title, String fileName, String mimeType, byte[] bytes, String textForShare, String status) {
            this.scope = scope == null ? Scope.CURRENT_NOTE : scope;
            this.format = format == null ? Format.TXT_UTF8 : format;
            this.title = title == null ? "Notizen" : title;
            this.fileName = fileName == null || fileName.trim().isEmpty() ? "notizen.txt" : fileName;
            this.mimeType = mimeType == null || mimeType.trim().isEmpty() ? "text/plain" : mimeType;
            this.bytes = bytes == null ? new byte[0] : bytes;
            this.textForShare = textForShare == null ? "" : textForShare;
            this.status = status == null ? "Export vorbereitet" : status;
        }
    }

    public static Payload build(NoteDocument document, NoteNode current, Scope scope, Format format) {
        Scope s = scope == null ? Scope.CURRENT_NOTE : scope;
        Format f = format == null ? Format.TXT_UTF8 : format;
        NoteNode root = document == null ? null : document.ensureRoot();
        NoteNode node = s == Scope.ROOT ? root : (current == null ? root : current);
        if (node == null) node = new NoteNode("Notiz", "");
        String title = titleForScope(s, node, root);
        String base = LegacyNodeExport.sanitizeBaseName(title);
        if (base.isEmpty()) base = "notizen";

        if (f == Format.HTML) {
            String html = s == Scope.CURRENT_NOTE ? singleNodeHtml(node) : Exporters.treeToHtml(node);
            return new Payload(s, f, title, base + ".html", "text/html", html.getBytes(StandardCharsets.UTF_8), stripHtmlForShare(html), status(s, f));
        }
        if (f == Format.RTF) {
            String rtf = s == Scope.CURRENT_NOTE ? LegacyNodeExport.currentNodeRtf(node) : Exporters.treeToRtf(node);
            return new Payload(s, f, title, base + ".rtf", "application/rtf", rtf.getBytes(Charset.forName("windows-1252")), RtfUtils.rtfToPlainText(rtf), status(s, f));
        }

        String text = s == Scope.CURRENT_NOTE ? LegacyNodeExport.currentNodePlainText(node) : LegacyTextExportModel.normalizeCrLf(Exporters.treeToPlainText(node, new ExportOptions()));
        LegacyTextExportModel.Mode mode = f == Format.TXT_ANSI ? LegacyTextExportModel.Mode.ANSI : (f == Format.TXT_UNICODE ? LegacyTextExportModel.Mode.UNICODE : LegacyTextExportModel.Mode.UTF8);
        String suffix = f == Format.TXT_ANSI ? "-ansi.txt" : (f == Format.TXT_UNICODE ? "-unicode.txt" : ".txt");
        return new Payload(s, f, title, base + suffix, "text/plain", LegacyTextExportModel.encode(text, mode), text, status(s, f));
    }

    public static String[] hubLabels() {
        return new String[]{
                "Aktuellen Knoten speichern",
                "Aktuellen Teilbaum speichern",
                "Ganzen Baum speichern",
                "Aktuellen Knoten teilen",
                "Aktuellen Teilbaum teilen",
                "Ganzen Baum teilen",
                "Drucken / PDF-Vorschau"
        };
    }

    public static String[] formatLabels() {
        return new String[]{"TXT UTF-8", "TXT ANSI", "TXT Unicode", "HTML", "RTF"};
    }

    public static Scope scopeForHubIndex(int index) {
        int i = Math.max(0, index % 3);
        if (i == 1) return Scope.CURRENT_SUBTREE;
        if (i == 2) return Scope.ROOT;
        return Scope.CURRENT_NOTE;
    }

    public static boolean hubIndexShares(int index) {
        return index >= 3 && index <= 5;
    }

    public static Format formatForIndex(int index) {
        switch (index) {
            case 1: return Format.TXT_ANSI;
            case 2: return Format.TXT_UNICODE;
            case 3: return Format.HTML;
            case 4: return Format.RTF;
            default: return Format.TXT_UTF8;
        }
    }

    public static String status(Scope scope, Format format) {
        return scopeLabel(scope) + " als " + formatLabel(format) + " vorbereitet";
    }

    public static String scopeLabel(Scope scope) {
        if (scope == Scope.ROOT) return "Ganzer Baum";
        if (scope == Scope.CURRENT_SUBTREE) return "Aktueller Teilbaum";
        return "Aktueller Knoten";
    }

    public static String formatLabel(Format format) {
        if (format == Format.HTML) return "HTML";
        if (format == Format.RTF) return "RTF";
        if (format == Format.TXT_ANSI) return "ANSI-TXT";
        if (format == Format.TXT_UNICODE) return "Unicode-TXT";
        return "TXT";
    }

    private static String titleForScope(Scope scope, NoteNode node, NoteNode root) {
        if (scope == Scope.ROOT) return root == null ? "Gesamter_Baum" : cleanTitle(root.title, "Gesamter_Baum");
        if (scope == Scope.CURRENT_SUBTREE) return cleanTitle(node == null ? "Teilbaum" : node.title, "Teilbaum");
        return cleanTitle(node == null ? "Notiz" : node.title, "Notiz");
    }

    private static String cleanTitle(String title, String fallback) {
        String t = title == null ? "" : title.trim();
        return t.isEmpty() ? fallback : t;
    }

    private static String singleNodeHtml(NoteNode node) {
        String title = RtfUtils.escapeXml(cleanTitle(node == null ? "Notiz" : node.title, "Notiz"));
        String html = RtfUtils.rtfToHtml(node == null ? "" : node.rtf);
        return "<!doctype html>\n<html lang=\"de\"><head><meta charset=\"utf-8\"/><title>" + title + "</title>" +
                "<style>body{font-family:Arial,Helvetica,sans-serif;margin:2rem}.notizen-body{white-space:pre-wrap}.notizen-body img{max-width:100%;height:auto}</style>" +
                "</head><body><h1>" + title + "</h1><div class=\"notizen-body\">" + html + "</div></body></html>\n";
    }

    private static String stripHtmlForShare(String html) {
        if (html == null) return "";
        return html.replaceAll("(?is)<style.*?</style>", " ")
                .replaceAll("(?is)<script.*?</script>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll(" *\\n *", "\n")
                .trim();
    }
}
