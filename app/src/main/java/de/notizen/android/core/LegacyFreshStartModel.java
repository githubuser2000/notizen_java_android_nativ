package de.notizen.android.core;

/**
 * Portable model for Notizen .NET new_topnode/schliessen_ohne_fragen.
 * The legacy app clears the whole TreeView/RTF box first and then creates one
 * empty root node named "start" with no child nodes and no rich text body.
 */
public final class LegacyFreshStartModel {
    public static final String LEGACY_EMPTY_ROOT_TITLE = "start";

    private LegacyFreshStartModel() {}

    public static NoteDocument newFreshStartDocument() {
        NoteDocument d = new NoteDocument();
        d.root = new NoteNode(LEGACY_EMPTY_ROOT_TITLE, "");
        d.displayName = LegacyPaths.LEGACY_DEFAULT_FILENAME;
        d.changed = true;
        return d;
    }

    public static boolean isFreshStartShape(NoteDocument document) {
        if (document == null || document.root == null) return true;
        return document.root.children.isEmpty()
                && (document.root.rtf == null || document.root.rtf.isEmpty())
                && LEGACY_EMPTY_ROOT_TITLE.equals(document.root.title);
    }
}
