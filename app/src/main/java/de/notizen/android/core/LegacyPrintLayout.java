package de.notizen.android.core;

/** Helpers for the legacy print actions: current note, subtree and whole tree. */
public final class LegacyPrintLayout {
    public enum Scope { CURRENT_NOTE, CURRENT_SUBTREE, ROOT }

    public static final class PrintJob {
        public final Scope scope;
        public final String title;
        public final String html;

        public PrintJob(Scope scope, String title, String html) {
            this.scope = scope == null ? Scope.CURRENT_NOTE : scope;
            this.title = safeDocumentTitle(title);
            this.html = html == null ? "" : html;
        }
    }

    private LegacyPrintLayout() {}

    public static PrintJob currentNote(NoteNode node) {
        if (node == null) return new PrintJob(Scope.CURRENT_NOTE, "Notizen", RtfUtils.rtfToHtmlDocument(""));
        return new PrintJob(Scope.CURRENT_NOTE, node.title, RtfUtils.rtfToHtmlDocument(node.rtf));
    }

    public static PrintJob subtree(NoteNode node) {
        if (node == null) return currentNote(null);
        return new PrintJob(Scope.CURRENT_SUBTREE, node.title, RtfUtils.rtfToHtmlDocument(Exporters.treeToRtf(node)));
    }

    public static PrintJob root(NoteDocument document) {
        NoteNode root = document == null ? null : document.ensureRoot();
        if (root == null) return currentNote(null);
        return new PrintJob(Scope.ROOT, root.title, RtfUtils.rtfToHtmlDocument(Exporters.treeToRtf(root)));
    }

    public static String labelForScope(Scope scope) {
        if (scope == Scope.CURRENT_SUBTREE) return "Aktueller Teilbaum";
        if (scope == Scope.ROOT) return "Gesamter Baum";
        return "Aktuelle Notiz";
    }

    public static String safeDocumentTitle(String title) {
        String raw = title == null || title.trim().isEmpty() ? "Notizen" : title.trim();
        StringBuilder cleanedBuilder = new StringBuilder(raw.length());
        String bad = "<>:\"/\\|?*";
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            cleanedBuilder.append(ch < 32 || bad.indexOf(ch) >= 0 ? '_' : ch);
        }
        String cleaned = cleanedBuilder.toString().replaceAll("\\s+", " ").trim();
        return cleaned.isEmpty() ? "Notizen" : cleaned;
    }

    public static String safeFileName(String title, String suffix) {
        String ext = suffix == null || suffix.isEmpty() ? ".html" : suffix;
        if (!ext.startsWith(".")) ext = "." + ext;
        String base = safeDocumentTitle(title).replace(' ', '_').replaceAll("_+", "_");
        while (base.startsWith("_")) base = base.substring(1);
        while (base.endsWith("_")) base = base.substring(0, base.length() - 1);
        if (base.isEmpty()) base = "Notizen";
        return base + ext;
    }
}
