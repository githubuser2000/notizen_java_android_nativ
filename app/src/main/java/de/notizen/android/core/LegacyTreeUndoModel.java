package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Portable tree undo/redo snapshot model.
 *
 * The WinForms/PyQt desktop applications mutate the TreeView directly.  On
 * Android we keep immutable deep snapshots of the note tree before a tree
 * operation so Ctrl+Z / Rückgängig can restore create/delete/move/rename,
 * expansion and node formatting without touching the RTF editor undo stack.
 */
public final class LegacyTreeUndoModel {
    public static final int DEFAULT_LIMIT = 80;

    public static final class Snapshot {
        public final NoteNode root;
        public final int[] selectedPath;
        public final String reason;
        public final String signature;

        public Snapshot(NoteNode root, int[] selectedPath, String reason) {
            this.root = root == null ? null : root.cloneDeep(true);
            this.selectedPath = copyPath(selectedPath);
            this.reason = reason == null ? "" : reason;
            this.signature = signature(this.root) + "@@selected=" + pathKey(this.selectedPath);
        }

        public NoteNode copyRoot() {
            return root == null ? null : root.cloneDeep(true);
        }
    }

    private LegacyTreeUndoModel() {}

    public static Snapshot capture(NoteDocument document, NoteNode selected, String reason) {
        if (document == null || document.ensureRoot() == null) return null;
        NoteNode root = document.ensureRoot();
        return new Snapshot(root, pathFromRoot(root, selected == null ? root : selected), reason);
    }

    public static boolean sameSnapshot(Snapshot a, Snapshot b) {
        if (a == null || b == null) return false;
        return a.signature.equals(b.signature);
    }

    public static int trimStartIndex(int size, int limit) {
        int safeLimit = Math.max(1, limit);
        return size <= safeLimit ? 0 : size - safeLimit;
    }

    public static int[] pathFromRoot(NoteNode root, NoteNode node) {
        if (root == null || node == null) return new int[0];
        if (root == node) return new int[0];
        ArrayList<Integer> reversed = new ArrayList<>();
        NoteNode cursor = node;
        while (cursor != null && cursor != root) {
            NoteNode parent = cursor.parent;
            if (parent == null) return new int[0];
            int index = parent.children.indexOf(cursor);
            if (index < 0) return new int[0];
            reversed.add(index);
            cursor = parent;
        }
        if (cursor != root) return new int[0];
        Collections.reverse(reversed);
        int[] path = new int[reversed.size()];
        for (int i = 0; i < reversed.size(); i++) path[i] = reversed.get(i);
        return path;
    }

    public static NoteNode nodeAtPath(NoteNode root, int[] path) {
        NoteNode node = root;
        if (node == null) return null;
        int[] safe = path == null ? new int[0] : path;
        for (int index : safe) {
            if (index < 0 || index >= node.children.size()) return root;
            node = node.children.get(index);
        }
        return node == null ? root : node;
    }

    public static String pathKey(int[] path) {
        if (path == null || path.length == 0) return "root";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            if (i > 0) b.append('/');
            b.append(path[i]);
        }
        return b.toString();
    }

    public static String signature(NoteNode root) {
        StringBuilder b = new StringBuilder();
        appendNodeSignature(b, root);
        return b.toString();
    }

    private static void appendNodeSignature(StringBuilder b, NoteNode node) {
        if (node == null) {
            b.append("<null>");
            return;
        }
        b.append('{');
        appendEscaped(b, node.title);
        b.append('|').append(node.expanded ? '1' : '0');
        b.append('|').append(node.bgArgb).append('|').append(node.fgArgb);
        b.append('|').append(sampledHash(node.rtf)).append(':').append(node.rtf == null ? 0 : node.rtf.length());
        b.append("|attrs=");
        for (Map.Entry<String, String> entry : node.extraAttrs.entrySet()) {
            appendEscaped(b, entry.getKey());
            b.append('=');
            appendEscaped(b, entry.getValue());
            b.append(';');
        }
        b.append("|extra=");
        for (String extra : node.extraChildXml) {
            b.append(sampledHash(extra)).append(':').append(extra == null ? 0 : extra.length()).append(';');
        }
        b.append("|desk=");
        appendDesktopSignature(b, node.desktopNote);
        b.append("|children=").append(node.children.size());
        for (NoteNode child : node.children) appendNodeSignature(b, child);
        b.append('}');
    }


    private static int sampledHash(String text) {
        if (text == null || text.isEmpty()) return 0;
        int len = text.length();
        if (len <= 8192) return text.hashCode();
        int h = 1125899907;
        h = 31 * h + len;
        for (int i = 0; i < 4096; i++) h = 31 * h + text.charAt(i);
        for (int i = Math.max(4096, len - 4096); i < len; i++) h = 31 * h + text.charAt(i);
        return h;
    }

    private static void appendDesktopSignature(StringBuilder b, DesktopNoteState state) {
        if (state == null) {
            b.append("none");
            return;
        }
        b.append(state.visible ? '1' : '0').append(',')
                .append(state.x).append(',').append(state.y).append(',')
                .append(state.width).append(',').append(state.height).append(',')
                .append(state.opacity).append(',').append(state.argb).append(',')
                .append(state.legacySparse ? '1' : '0');
    }

    private static void appendEscaped(StringBuilder b, String text) {
        if (text == null) return;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' || c == '|' || c == ';' || c == '{' || c == '}') b.append('\\');
            b.append(c);
        }
    }

    private static int[] copyPath(int[] path) {
        if (path == null || path.length == 0) return new int[0];
        int[] copy = new int[path.length];
        System.arraycopy(path, 0, copy, 0, path.length);
        return copy;
    }
}
