package de.notizen.android.core;

/** Global tree expansion helpers mirroring WinForms TreeView ExpandAll/CollapseAll actions. */
public final class LegacyTreeExpansion {
    private LegacyTreeExpansion() {}

    public static int expandAll(NoteNode root) {
        return setExpanded(root, true);
    }

    public static int collapseAll(NoteNode root) {
        return setExpanded(root, false);
    }

    public static int setExpanded(NoteNode node, boolean expanded) {
        if (node == null) return 0;
        int count = 0;
        if (node.expanded != expanded) count++;
        node.expanded = expanded;
        for (NoteNode child : node.children) count += setExpanded(child, expanded);
        return count;
    }

    public static int countExpanded(NoteNode root) {
        if (root == null) return 0;
        int n = root.expanded ? 1 : 0;
        for (NoteNode child : root.children) n += countExpanded(child);
        return n;
    }
}
