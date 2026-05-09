package de.notizen.android.core;

/**
 * Portable node/editor binding distilled from inhalt.vb and CText.vb.
 * It keeps the old rules explicit: selecting a node loads its RTF and title;
 * editing text writes RTF back to the node; editing the node with an attached
 * desktop note asks the caller to refresh the sticky-note view.
 */
public final class LegacyEditorNodeSync {
    private LegacyEditorNodeSync() {}

    public static final class LoadState {
        public final String titleText;
        public final String editorPlainText;
        public final String rawRtf;
        public final boolean hasDesktopNote;
        public final boolean rootTitle;

        public LoadState(String titleText, String editorPlainText, String rawRtf, boolean hasDesktopNote, boolean rootTitle) {
            this.titleText = normalizeTitle(titleText);
            this.editorPlainText = editorPlainText == null ? "" : editorPlainText;
            this.rawRtf = rawRtf == null ? "" : rawRtf;
            this.hasDesktopNote = hasDesktopNote;
            this.rootTitle = rootTitle;
        }
    }

    public static final class SaveState {
        public final boolean changed;
        public final String nextTitle;
        public final String nextRtf;
        public final boolean reloadDesktopNote;
        public final String status;

        public SaveState(boolean changed, String nextTitle, String nextRtf, boolean reloadDesktopNote, String status) {
            this.changed = changed;
            this.nextTitle = normalizeTitle(nextTitle);
            this.nextRtf = nextRtf == null ? "" : nextRtf;
            this.reloadDesktopNote = reloadDesktopNote;
            this.status = status == null ? "" : status;
        }
    }

    public static LoadState load(NoteNode node) {
        String rtf = node == null ? "" : node.rtf;
        return new LoadState(node == null ? "..." : node.title, RtfUtils.rtfToPlainText(rtf == null ? "" : rtf), rtf,
                node != null && node.desktopNote != null, node != null && node.parent == null);
    }

    public static SaveState save(NoteNode node, String titleText, String plainText, boolean editorDirty, boolean titleDirty) {
        if (node == null) return new SaveState(false, "...", "", false, "Kein Knoten gewählt");
        String oldTitle = normalizeTitle(node.title);
        String oldRtf = node.rtf == null ? "" : node.rtf;
        String nextTitle = titleDirty ? normalizeTitle(titleText) : oldTitle;
        String nextRtf = editorDirty ? RtfUtils.plainTextToRtf(plainText == null ? "" : plainText) : oldRtf;
        boolean changed = !oldTitle.equals(nextTitle) || !oldRtf.equals(nextRtf);
        boolean reload = changed && node.desktopNote != null;
        return new SaveState(changed, nextTitle, nextRtf, reload, changed ? "Knoteninhalt aktualisiert" : "Knoten unverändert");
    }

    public static SaveState applyToNode(NoteNode node, String titleText, String plainText, boolean editorDirty, boolean titleDirty) {
        SaveState s = save(node, titleText, plainText, editorDirty, titleDirty);
        if (node != null && s.changed) {
            node.title = s.nextTitle;
            node.rtf = s.nextRtf;
        }
        return s;
    }

    public static String normalizeTitle(String title) {
        String t = title == null ? "" : title.trim();
        return t.isEmpty() ? "..." : t;
    }
}
