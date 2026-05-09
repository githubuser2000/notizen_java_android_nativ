package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Notizen.Designer.vb Startmenue/Export/Einheit/recent files as a pure Java menu tree. */
public final class LegacyStartMenuModel {
    public static final class Item {
        public final String objectName;
        public final String action;
        public final String label;
        public final boolean separator;
        public final List<Item> children;

        public Item(String objectName, String action, String label, boolean separator, List<Item> children) {
            this.objectName = objectName == null ? "" : objectName;
            this.action = action == null ? "" : action;
            this.label = label == null ? "" : label;
            this.separator = separator;
            this.children = Collections.unmodifiableList(children == null ? new ArrayList<Item>() : new ArrayList<Item>(children));
        }

        public boolean hasChildren() { return !children.isEmpty(); }
    }

    private LegacyStartMenuModel() {}

    public static List<Item> startMenu(LegacySettings settings) {
        ArrayList<Item> out = new ArrayList<>();
        out.add(item("oeffnenToolStripMenuItem", "file_open", "Öffnen"));
        out.add(item("SpeichernToolStripMenuItem", "file_save", "Speichern"));
        out.add(item("SpeichernUnterToolStripMenuItem", "file_save_as", "Speichern unter"));
        out.add(item("SchliessenToolStripMenuItem", "file_close", "Schließen"));
        out.add(item("ftpToolStripMenuItem", "ftp_open", "FTP"));
        out.add(item("passwortToolStripMenuItem", "password", "Passwort"));
        out.add(parent("EinheitToolStripMenuItem", "unify", "Einheit", new Item[]{
                item("einheit_start_MenuItem", "unify_root", "Gesamter Baum"),
                item("einheit_node_MenuItem", "unify_current", "Aktueller Teilbaum")
        }));
        out.add(parent("ExportToolStripMenuItem", "export", "Export", new Item[]{
                item("export_rtf_MenuItem", "export_rtf", "RTF"),
                item("export_txt_MenuItem", "export_txt", "TXT UTF-8"),
                item("export_txt2_MenuItem", "export_txt_ansi", "TXT ANSI")
        }));
        out.add(separator("ToolStripSeparator4"));
        out.add(item("ToolStripMenuItem1", "settings", "Einstellungen"));
        out.add(separator("ToolStripSeparator1"));
        out.add(item("infoToolStripMenuItem", "about", "Info"));
        out.add(separator("ToolStripSeparator3"));
        out.add(item("BeendenToolStripMenuItem", "exit", "Beenden"));
        List<String> recent = settings == null ? Collections.<String>emptyList() : settings.recentFiles;
        if (!recent.isEmpty()) {
            out.add(separator("ToolStripSeparator5"));
            int start = Math.max(0, recent.size() - 4);
            for (int i = start; i < recent.size(); i++) {
                String path = recent.get(i);
                out.add(item("File" + (i - start + 1) + "ToolStripMenuItem", "recent_" + i, displayName(path)));
            }
        }
        return Collections.unmodifiableList(out);
    }

    public static List<Item> trayMenu(LegacySettings settings) {
        ArrayList<Item> out = new ArrayList<>();
        out.add(item("showToolStripMenuItem", "show_main", "Notizen anzeigen"));
        out.add(item("newDesktopNoteToolStripMenuItem", "desktop_note_new", "Neue Haftnotiz"));
        out.add(separator("traySeparator1"));
        out.add(item("settingsToolStripMenuItem", "settings", "Einstellungen"));
        out.add(item("exitToolStripMenuItem", "exit", "Beenden"));
        return Collections.unmodifiableList(out);
    }

    public static String displayName(String path) {
        if (path == null || path.isEmpty()) return "";
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    public static Item findByAction(List<Item> items, String action) {
        if (items == null || action == null) return null;
        for (Item item : items) {
            if (action.equals(item.action)) return item;
            Item child = findByAction(item.children, action);
            if (child != null) return child;
        }
        return null;
    }

    private static Item item(String objectName, String action, String label) { return new Item(objectName, action, label, false, null); }
    private static Item separator(String objectName) { return new Item(objectName, "separator", "", true, null); }
    private static Item parent(String objectName, String action, String label, Item[] children) {
        ArrayList<Item> list = new ArrayList<>();
        if (children != null) Collections.addAll(list, children);
        return new Item(objectName, action, label, false, list);
    }
}
