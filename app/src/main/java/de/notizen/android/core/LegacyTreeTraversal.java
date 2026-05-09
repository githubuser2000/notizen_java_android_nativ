package de.notizen.android.core;

import java.util.ArrayList;
import java.util.List;

/** Testable approximation of Baum.allnodes: recursive order, depth and legacy path numbers. */
public final class LegacyTreeTraversal {
    public static final class Entry {
        public final NoteNode node;
        public final int depth;
        public final String path;
        public final boolean enter;

        public Entry(NoteNode node, int depth, String path, boolean enter) {
            this.node = node;
            this.depth = depth;
            this.path = path == null ? "" : path;
            this.enter = enter;
        }

        public String title() { return node == null || node.title == null ? "" : node.title; }
    }

    private LegacyTreeTraversal() {}

    public static List<Entry> preorder(NoteNode root) {
        ArrayList<Entry> out = new ArrayList<>();
        preorderInto(root, 0, "1", out);
        return out;
    }

    private static void preorderInto(NoteNode node, int depth, String path, List<Entry> out) {
        if (node == null) return;
        out.add(new Entry(node, depth, path, true));
        for (int i = 0; i < node.children.size(); i++) {
            preorderInto(node.children.get(i), depth + 1, path + "." + (i + 1), out);
        }
    }

    public static List<Entry> enterExit(NoteNode root) {
        ArrayList<Entry> out = new ArrayList<>();
        enterExitInto(root, 0, "1", out);
        return out;
    }

    private static void enterExitInto(NoteNode node, int depth, String path, List<Entry> out) {
        if (node == null) return;
        out.add(new Entry(node, depth, path, true));
        for (int i = 0; i < node.children.size(); i++) enterExitInto(node.children.get(i), depth + 1, path + "." + (i + 1), out);
        out.add(new Entry(node, depth, path, false));
    }

    public static String pathOf(NoteNode node) {
        if (node == null) return "";
        ArrayList<Integer> parts = new ArrayList<>();
        for (NoteNode n = node; n != null; n = n.parent) {
            parts.add(0, n.parent == null ? 1 : n.parent.children.indexOf(n) + 1);
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) out.append('.');
            out.append(parts.get(i));
        }
        return out.toString();
    }
}
