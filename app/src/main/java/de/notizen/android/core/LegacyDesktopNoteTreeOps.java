package de.notizen.android.core;

import java.util.ArrayList;
import java.util.List;

/** Baum.vb mach_haft_weg / loesche_haftnotiz_aus_baum as portable tree operations. */
public final class LegacyDesktopNoteTreeOps {
    public static final class ClearResult {
        public final int visitedNodes;
        public final int clearedDesktopNotes;
        public final List<String> titles;

        public ClearResult(int visitedNodes, int clearedDesktopNotes, List<String> titles) {
            this.visitedNodes = visitedNodes;
            this.clearedDesktopNotes = clearedDesktopNotes;
            this.titles = titles == null ? new ArrayList<String>() : titles;
        }
    }

    private LegacyDesktopNoteTreeOps() {}

    public static int countDesktopNotes(NoteNode root) {
        if (root == null) return 0;
        int count = root.desktopNote == null ? 0 : 1;
        for (NoteNode child : root.children) count += countDesktopNotes(child);
        return count;
    }

    public static int countVisibleDesktopNotes(NoteNode root) {
        if (root == null) return 0;
        int count = root.desktopNote != null && root.desktopNote.visible ? 1 : 0;
        for (NoteNode child : root.children) count += countVisibleDesktopNotes(child);
        return count;
    }

    public static ClearResult clearDesktopNotes(NoteNode root) {
        ArrayList<String> titles = new ArrayList<>();
        int[] counters = new int[2];
        clearInto(root, counters, titles);
        return new ClearResult(counters[0], counters[1], titles);
    }

    private static void clearInto(NoteNode node, int[] counters, List<String> titles) {
        if (node == null) return;
        counters[0]++;
        if (node.desktopNote != null) {
            counters[1]++;
            titles.add(node.title == null || node.title.isEmpty() ? "..." : node.title);
            node.desktopNote = null;
        }
        for (NoteNode child : node.children) clearInto(child, counters, titles);
    }

    public static boolean wouldReloadDesktopNote(NoteNode node) {
        return node != null && node.desktopNote != null;
    }

    public static String clearStatusText(ClearResult result) {
        int n = result == null ? 0 : result.clearedDesktopNotes;
        if (n <= 0) return "Keine Haftnotizen im Teilbaum";
        if (n == 1) return "1 Haftnotiz im Teilbaum entfernt";
        return n + " Haftnotizen im Teilbaum entfernt";
    }
}
