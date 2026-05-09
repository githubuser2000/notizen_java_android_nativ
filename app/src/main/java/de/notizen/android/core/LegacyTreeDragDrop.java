package de.notizen.android.core;

/** WinForms TreeView mouse-up drag/drop decision model. */
public final class LegacyTreeDragDrop {
    public static final class Decision {
        public final boolean allowed;
        public final String reason;

        public Decision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason == null ? "" : reason;
        }
    }

    private LegacyTreeDragDrop() {}

    public static Decision decision(NoteNode source, NoteNode target, NoteNode topNode) {
        if (source == null) return new Decision(false, "source-missing");
        if (target == null) return new Decision(false, "target-missing");
        if (source == target) return new Decision(false, "same-node");
        if (source.parent == null || source == topNode) return new Decision(false, "root-source");
        if (target.parent == null || target == topNode) return new Decision(false, "root-target");
        if (source.isAncestorOf(target)) return new Decision(false, "descendant-target");
        return new Decision(true, "move-before-target");
    }

    public static NoteNode moveBefore(NoteNode source, NoteNode target, NoteNode topNode) {
        if (!decision(source, target, topNode).allowed) return null;
        return NoteTreeOps.legacyMoveBeforeTarget(source, target);
    }
}
