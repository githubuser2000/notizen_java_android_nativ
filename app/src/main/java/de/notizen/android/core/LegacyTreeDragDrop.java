package de.notizen.android.core;

/** WinForms TreeView mouse-up drag/drop decision model, extended for Android finger zones. */
public final class LegacyTreeDragDrop {
    public enum DropMode { BEFORE, AS_CHILD, AFTER }

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
        return decision(source, target, topNode, DropMode.BEFORE);
    }

    public static Decision decision(NoteNode source, NoteNode target, NoteNode topNode, DropMode mode) {
        DropMode safeMode = mode == null ? DropMode.BEFORE : mode;
        if (source == null) return new Decision(false, "source-missing");
        if (target == null) return new Decision(false, "target-missing");
        if (source == target) return new Decision(false, "same-node");
        if (source.parent == null || source == topNode) return new Decision(false, "root-source");
        if (source.isAncestorOf(target)) return new Decision(false, "descendant-target");
        if (safeMode != DropMode.AS_CHILD && (target.parent == null || target == topNode)) return new Decision(false, "root-target");
        return new Decision(true, safeMode == DropMode.AS_CHILD ? "move-as-child" : (safeMode == DropMode.AFTER ? "move-after-target" : "move-before-target"));
    }

    public static NoteNode moveBefore(NoteNode source, NoteNode target, NoteNode topNode) {
        if (!decision(source, target, topNode, DropMode.BEFORE).allowed) return null;
        return NoteTreeOps.legacyMoveBeforeTarget(source, target);
    }

    public static NoteNode moveAfter(NoteNode source, NoteNode target, NoteNode topNode) {
        if (!decision(source, target, topNode, DropMode.AFTER).allowed) return null;
        return NoteTreeOps.legacyMoveAfterTarget(source, target);
    }

    public static NoteNode moveAsChild(NoteNode source, NoteNode target, NoteNode topNode) {
        if (!decision(source, target, topNode, DropMode.AS_CHILD).allowed) return null;
        return NoteTreeOps.legacyMoveAsLastChild(source, target);
    }

    public static NoteNode move(NoteNode source, NoteNode target, NoteNode topNode, DropMode mode) {
        DropMode safeMode = mode == null ? DropMode.BEFORE : mode;
        if (safeMode == DropMode.AS_CHILD) return moveAsChild(source, target, topNode);
        if (safeMode == DropMode.AFTER) return moveAfter(source, target, topNode);
        return moveBefore(source, target, topNode);
    }
}
