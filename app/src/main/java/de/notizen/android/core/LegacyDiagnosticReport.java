package de.notizen.android.core;

/** Local privacy-light diagnostics: metadata and metrics, no raw note body. */
public final class LegacyDiagnosticReport {
    private LegacyDiagnosticReport() {}

    public static String build(NoteDocument document, NoteNode currentNode, LegacyDocumentTitle.State title, LegacySettings settings, String versionName) {
        NoteDocument doc = document == null ? NoteDocument.newDocument() : document;
        NoteNode root = doc.ensureRoot();
        TreeStats stats = TreeStats.collect(root);
        NoteNode current = currentNode == null ? root : currentNode;
        LegacyRichTextBoxSemantics.Metrics metrics = LegacyRichTextBoxSemantics.inspectRtf(current.rtf == null ? "" : current.rtf);
        LegacySettings cfg = settings == null ? new LegacySettings() : settings;
        LegacyDocumentTitle.State state = title == null ? LegacyDocumentTitle.build("Notizen Android", doc.displayName, doc.changed, "", "", "") : title;
        StringBuilder out = new StringBuilder();
        out.append("Notizen Android Diagnose\n");
        out.append("Version: ").append(clean(versionName)).append('\n');
        out.append("Paket: de.notizen.android\n");
        out.append("Dokument: ").append(state.displayName).append('\n');
        out.append("Quelle: ").append(state.sourceLine).append('\n');
        out.append("Geändert: ").append(doc.changed ? "ja" : "nein").append('\n');
        out.append("Speicherziel vorhanden: ").append(state.hasSaveTarget ? "ja" : "nein").append('\n');
        out.append("Aktueller Knoten: ").append(clean(current.title).isEmpty() ? "..." : clean(current.title)).append('\n');
        out.append('\n').append(stats.asLegacyText()).append('\n');
        out.append("\nRTF aktuelle Notiz:\n").append(metrics.summaryGerman()).append('\n');
        out.append("\nEinstellungen:\n");
        out.append("Sprache: ").append(clean(cfg.language)).append('\n');
        out.append("Backups behalten: ").append(cfg.backupKeep).append('\n');
        out.append("Autosave Sekunden: ").append(cfg.autosaveSeconds).append('\n');
        out.append("Scrollleisten: ").append(cfg.scrollbarsChoice).append('\n');
        out.append("Recent-Anzahl: ").append(cfg.recentFiles == null ? 0 : cfg.recentFiles.size()).append('\n');
        out.append("FTP konfiguriert: ").append(clean(cfg.ftpHost).isEmpty() ? "nein" : "ja").append('\n');
        out.append("\nHinweis: Diese Diagnose enthält absichtlich keine rohen Notiztexte.");
        return out.toString();
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
