package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Portable model for the old NotifyIcon context menu desktop-note entries. */
public final class LegacyDesktopNoteTrayRegistry {
    public static final class MenuItem {
        public final int index;
        public final String label;
        public final String action;
        public final NoteNode node;
        public final boolean separator;
        public MenuItem(int index, String label, String action, NoteNode node, boolean separator) {
            this.index = index;
            this.label = label == null ? "" : label;
            this.action = action == null ? "" : action;
            this.node = node;
            this.separator = separator;
        }
    }

    public static final class Registry {
        public final List<NoteNode> desktopNodes;
        public final List<MenuItem> menuItems;
        public final int desktopCount;
        public Registry(List<NoteNode> desktopNodes, List<MenuItem> menuItems) {
            this.desktopNodes = Collections.unmodifiableList(new ArrayList<>(desktopNodes));
            this.menuItems = Collections.unmodifiableList(new ArrayList<>(menuItems));
            this.desktopCount = desktopNodes.size();
        }
        public String summary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Haftnotizen: ").append(desktopCount);
            for (MenuItem item : menuItems) if (item.node != null) sb.append('\n').append(item.index).append(". ").append(item.label);
            return sb.toString();
        }
    }

    private LegacyDesktopNoteTrayRegistry() {}

    public static Registry fromTree(NoteNode root) {
        ArrayList<NoteNode> nodes = new ArrayList<>();
        if (root != null) for (NoteNode n : root.walk()) if (n.desktopNote != null) nodes.add(n);
        ArrayList<MenuItem> items = new ArrayList<>();
        items.add(new MenuItem(0, "Beenden", "exit", null, false));
        items.add(new MenuItem(1, "Hauptfenster anzeigen/verstecken", "toggle_main", null, false));
        if (!nodes.isEmpty()) items.add(new MenuItem(2, "-", "separator", null, true));
        int index = 3;
        int number = 1;
        for (NoteNode n : nodes) {
            items.add(new MenuItem(index++, number + " " + safeTitle(n), "show_desktop_note", n, false));
            number++;
        }
        return new Registry(nodes, items);
    }

    public static int closeAllDesktopNotes(NoteNode root) {
        int count = 0;
        if (root != null) for (NoteNode n : root.walk()) if (n.desktopNote != null) { n.desktopNote = null; count++; }
        return count;
    }

    private static String safeTitle(NoteNode n) {
        String s = n == null ? "" : n.title;
        return s == null || s.trim().isEmpty() ? "..." : s.trim();
    }
}
