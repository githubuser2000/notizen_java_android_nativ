package de.notizen.android.core;

/** Old TreeView label editing rules with the historical fallback title. */
public final class LegacyTreeLabelEditing {
    public static final String EMPTY_TITLE_FALLBACK = "...";

    public static final class EditResult {
        public final String oldTitle;
        public final String newTitle;
        public final boolean changed;
        public final boolean usedFallback;

        public EditResult(String oldTitle, String newTitle, boolean changed, boolean usedFallback) {
            this.oldTitle = oldTitle == null ? "" : oldTitle;
            this.newTitle = newTitle == null ? EMPTY_TITLE_FALLBACK : newTitle;
            this.changed = changed;
            this.usedFallback = usedFallback;
        }
    }

    private LegacyTreeLabelEditing() {}

    public static String normalize(String candidate) {
        String t = candidate == null ? "" : candidate.trim();
        return t.isEmpty() ? EMPTY_TITLE_FALLBACK : t;
    }

    public static EditResult commit(String oldTitle, String candidate) {
        String before = oldTitle == null ? "" : oldTitle;
        String after = normalize(candidate);
        return new EditResult(before, after, !before.equals(after), EMPTY_TITLE_FALLBACK.equals(after));
    }

    public static boolean canEdit(NoteNode node) {
        return node != null;
    }
}
