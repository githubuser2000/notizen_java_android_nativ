package de.notizen.android.core;

/** WinForms-like focus dispatch for copy/cut/paste/delete between tree and editor. */
public final class LegacyClipboardFocus {
    public enum Action { COPY, CUT, PASTE, DELETE }
    public enum Area { TREE, EDITOR, NONE }

    public static final class Decision {
        public final Action action;
        public final Area area;
        public final boolean allowed;
        public final String reason;

        public Decision(Action action, Area area, boolean allowed, String reason) {
            this.action = action == null ? Action.COPY : action;
            this.area = area == null ? Area.NONE : area;
            this.allowed = allowed;
            this.reason = reason == null ? "" : reason;
        }
    }

    private LegacyClipboardFocus() {}

    public static Decision decide(Action action, boolean treeFocused, boolean editorFocused, boolean editorHasSelection,
                                  boolean hasCurrentNode, boolean currentIsRoot, boolean hasTreeClipboard,
                                  boolean hasTextClipboard) {
        Action a = action == null ? Action.COPY : action;
        if (editorFocused || (editorHasSelection && !treeFocused)) {
            if (a == Action.PASTE) return new Decision(a, Area.EDITOR, hasTextClipboard, hasTextClipboard ? "editor paste" : "kein Text in der Zwischenablage");
            if (a == Action.COPY) return new Decision(a, Area.EDITOR, editorHasSelection, editorHasSelection ? "editor copy" : "keine Textauswahl");
            if (a == Action.CUT || a == Action.DELETE) return new Decision(a, Area.EDITOR, editorHasSelection, editorHasSelection ? "editor selection" : "keine Textauswahl");
        }
        if (treeFocused || hasCurrentNode) {
            if (!hasCurrentNode) return new Decision(a, Area.TREE, false, "kein Knoten");
            if (a == Action.COPY) return new Decision(a, Area.TREE, true, "tree copy");
            if (a == Action.PASTE) return new Decision(a, Area.TREE, hasTreeClipboard, hasTreeClipboard ? "tree paste" : "kein Knoten in der Zwischenablage");
            if (currentIsRoot) return new Decision(a, Area.TREE, false, "Root-Knoten geschützt");
            return new Decision(a, Area.TREE, true, "tree node");
        }
        return new Decision(a, Area.NONE, false, "kein Fokus");
    }

    public static boolean shouldUseTree(Decision d) { return d != null && d.area == Area.TREE && d.allowed; }
    public static boolean shouldUseEditor(Decision d) { return d != null && d.area == Area.EDITOR && d.allowed; }
}
