package de.notizen.android.core;

import java.util.ArrayList;
import java.util.List;

public final class NoteTreeOps {
    private NoteTreeOps() {}

    public static NoteNode legacyPasteClone(NoteNode source, NoteNode selected) {
        NoteNode pasted = source.cloneDeep(false);
        if (selected.parent == null) selected.insertChild(0, pasted);
        else selected.parent.insertChild(selected.indexInParent(), pasted);
        return pasted;
    }

    public static List<NoteNode> legacyVisibleWalk(NoteNode root) {
        ArrayList<NoteNode> out = new ArrayList<>();
        visibleInto(root, out);
        return out;
    }

    private static void visibleInto(NoteNode node, List<NoteNode> out) {
        out.add(node);
        if (node.expanded) {
            for (NoteNode child : node.children) visibleInto(child, out);
        }
    }

    public static NoteNode legacyPreviousVisibleNode(NoteNode selected) {
        if (selected == null) return null;
        List<NoteNode> visible = legacyVisibleWalk(selected.root());
        int index = visible.indexOf(selected);
        if (index <= 0) return null;
        return visible.get(index - 1);
    }

    public static NoteNode legacyDeleteFallbackNode(NoteNode selected) {
        if (selected == null || selected.parent == null) return null;
        NoteNode prev = legacyPreviousVisibleNode(selected);
        return prev == null ? selected.parent : prev;
    }

    /** Legacy "Neu daneben" appends at the end of the parent level. */
    public static NoteNode legacyNewNextNode(NoteNode selected, String title) {
        NoteNode parent = selected.parent == null ? selected : selected.parent;
        return parent.addChild(new NoteNode(title == null ? "..." : title, ""));
    }

    public static boolean legacyCanMoveBeforeTarget(NoteNode source, NoteNode target) {
        if (source == null || target == null) return false;
        if (source == target) return false;
        if (source.parent == null) return false;
        if (target.parent == null) return false;
        return !source.isAncestorOf(target);
    }

    public static NoteNode legacyMoveBeforeTarget(NoteNode source, NoteNode target) {
        if (!legacyCanMoveBeforeTarget(source, target)) return null;
        NoteNode oldParent = source.parent;
        NoteNode newParent = target.parent;
        int oldIndex = oldParent.children.indexOf(source);
        int newIndex = newParent.children.indexOf(target);
        oldParent.children.remove(oldIndex);
        if (oldParent == newParent && oldIndex < newIndex) newIndex--;
        source.parent = null;
        newParent.insertChild(newIndex, source);
        return source;
    }



    public static boolean canMoveAfterTarget(NoteNode source, NoteNode target) {
        if (source == null || target == null) return false;
        if (source == target) return false;
        if (source.parent == null) return false;
        if (target.parent == null) return false;
        return !source.isAncestorOf(target);
    }

    public static NoteNode legacyMoveAfterTarget(NoteNode source, NoteNode target) {
        if (!canMoveAfterTarget(source, target)) return null;
        NoteNode oldParent = source.parent;
        NoteNode newParent = target.parent;
        int oldIndex = oldParent.children.indexOf(source);
        int targetIndex = newParent.children.indexOf(target);
        oldParent.children.remove(oldIndex);
        if (oldParent == newParent && oldIndex < targetIndex) targetIndex--;
        source.parent = null;
        newParent.insertChild(targetIndex + 1, source);
        return source;
    }

    public static boolean canMoveAsLastChild(NoteNode source, NoteNode target) {
        if (source == null || target == null) return false;
        if (source == target) return false;
        if (source.parent == null) return false;
        return !source.isAncestorOf(target);
    }

    public static NoteNode legacyMoveAsLastChild(NoteNode source, NoteNode target) {
        if (!canMoveAsLastChild(source, target)) return null;
        source.removeFromParent();
        target.addChild(source);
        target.expanded = true;
        return source;
    }

    public static NoteNode legacyPasteCloneAsLastChild(NoteNode source, NoteNode selected) {
        if (source == null || selected == null) return null;
        NoteNode pasted = source.cloneDeep(false);
        selected.addChild(pasted);
        selected.expanded = true;
        return pasted;
    }

    public static boolean canIndentUnderPreviousSibling(NoteNode node) {
        return node != null && node.parent != null && node.indexInParent() > 0;
    }

    public static NoteNode indentUnderPreviousSibling(NoteNode node) {
        if (!canIndentUnderPreviousSibling(node)) return null;
        NoteNode parent = node.parent;
        int index = parent.children.indexOf(node);
        NoteNode previous = parent.children.get(index - 1);
        parent.children.remove(index);
        node.parent = null;
        previous.addChild(node);
        previous.expanded = true;
        return node;
    }

    public static boolean canOutdentAfterParent(NoteNode node) {
        return node != null && node.parent != null && node.parent.parent != null;
    }

    public static NoteNode outdentAfterParent(NoteNode node) {
        if (!canOutdentAfterParent(node)) return null;
        NoteNode parent = node.parent;
        NoteNode grandParent = parent.parent;
        int parentIndex = grandParent.children.indexOf(parent);
        parent.children.remove(node);
        node.parent = null;
        grandParent.insertChild(parentIndex + 1, node);
        return node;
    }

    public static int depth(NoteNode node) {
        int depth = 0;
        for (NoteNode n = node == null ? null : node.parent; n != null; n = n.parent) depth++;
        return depth;
    }
}
