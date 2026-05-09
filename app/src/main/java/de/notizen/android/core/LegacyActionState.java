package de.notizen.android.core;

/** Menu/toolbar enablement rules, kept outside Android widgets for regression tests. */
public final class LegacyActionState {
    public static final class State {
        public final boolean hasDocument;
        public final boolean hasCurrentNode;
        public final boolean currentIsRoot;
        public final boolean changed;
        public final boolean clipboardHasNode;
        public final boolean editorHasSelection;
        public final boolean fileAssociated;
        public final boolean hasDesktopNotesInSubtree;

        public State(boolean hasDocument, boolean hasCurrentNode, boolean currentIsRoot, boolean changed, boolean clipboardHasNode, boolean editorHasSelection, boolean fileAssociated, boolean hasDesktopNotesInSubtree) {
            this.hasDocument = hasDocument;
            this.hasCurrentNode = hasCurrentNode;
            this.currentIsRoot = currentIsRoot;
            this.changed = changed;
            this.clipboardHasNode = clipboardHasNode;
            this.editorHasSelection = editorHasSelection;
            this.fileAssociated = fileAssociated;
            this.hasDesktopNotesInSubtree = hasDesktopNotesInSubtree;
        }
    }

    private LegacyActionState() {}

    public static State from(NoteDocument document, NoteNode current, boolean clipboardHasNode, boolean editorHasSelection, boolean fileAssociated) {
        return new State(document != null && document.root != null,
                current != null,
                current != null && current.parent == null,
                document != null && document.changed,
                clipboardHasNode,
                editorHasSelection,
                fileAssociated,
                LegacyDesktopNoteTreeOps.countDesktopNotes(current) > 0);
    }

    public static boolean isEnabled(String action, State s) {
        if (s == null || action == null) return false;
        if (action.startsWith("file_new") || action.equals("file_close") || action.equals("settings") || action.equals("about") || action.equals("layout_diagnostics") || action.equals("runtime_identity") || action.equals("dialog_focus") || action.equals("toolbar_toggles") || action.equals("main_window_chrome") || action.equals("autosave_tick") || action.equals("font_set_plan") || action.equals("window_move_resize") || action.equals("alx_stream_pipeline") || action.equals("apk_build_plan")) return true;
        if (action.equals("file_save")) return s.hasDocument && (s.changed || s.fileAssociated);
        if (action.equals("file_save_as") || action.startsWith("export_") || action.equals("print") || action.equals("stats") || action.equals("validate") || action.equals("legacy_status")) return s.hasDocument;
        if (action.startsWith("tree_new") || action.equals("tree_toggle") || action.equals("tree_copy") || action.equals("tree_expand_all") || action.equals("tree_collapse_all")) return s.hasCurrentNode;
        if (action.equals("tree_delete") || action.equals("tree_cut") || action.equals("tree_move_before") || action.equals("tree_up") || action.equals("tree_down") || action.equals("tree_indent") || action.equals("tree_outdent")) return s.hasCurrentNode && !s.currentIsRoot;
        if (action.equals("tree_paste")) return s.hasCurrentNode && s.clipboardHasNode;
        if (action.equals("desktop_notes_clear_subtree")) return s.hasCurrentNode && s.hasDesktopNotesInSubtree;
        if (action.equals("desktop_note_tray")) return s.hasDocument;
        if (action.startsWith("insert_") || action.startsWith("format_") || action.startsWith("font_") || action.startsWith("align_") || action.equals("cycle_scrollbars")) return s.hasCurrentNode;
        if (action.equals("unify_root")) return s.hasDocument;
        if (action.equals("unify_current")) return s.hasCurrentNode;
        return s.hasDocument || s.hasCurrentNode;
    }
}
