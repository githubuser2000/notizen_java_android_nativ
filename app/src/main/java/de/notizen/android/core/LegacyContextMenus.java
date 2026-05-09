package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pure Java model of the WinForms context menu order from kontext_inhalt.vb
 * and Baum_Kontext_.vb. Android does not recreate those exact controls, but
 * the old item order and action identity are important for parity tests and
 * keyboard/context-menu bridges.
 */
public final class LegacyContextMenus {
    public enum MenuKind { EDITOR, TREE }

    public static final class MenuItemSpec {
        public final MenuKind kind;
        public final int legacyIndex;
        public final String legacyLabel;
        public final String normalizedLabel;
        public final String action;
        public final boolean requiresNode;
        public final boolean opensDialog;

        public MenuItemSpec(MenuKind kind, int legacyIndex, String legacyLabel, String normalizedLabel,
                            String action, boolean requiresNode, boolean opensDialog) {
            this.kind = kind;
            this.legacyIndex = legacyIndex;
            this.legacyLabel = clean(legacyLabel);
            this.normalizedLabel = clean(normalizedLabel);
            this.action = clean(action);
            this.requiresNode = requiresNode;
            this.opensDialog = opensDialog;
        }

        public String displayLabel(boolean normalizeUmlauts) {
            return normalizeUmlauts && !normalizedLabel.isEmpty() ? normalizedLabel : legacyLabel;
        }

        public String accessibilityLabel() {
            String base = displayLabel(true);
            return opensDialog ? base + "…" : base;
        }
    }

    private static final List<MenuItemSpec> EDITOR_ITEMS;
    private static final List<MenuItemSpec> TREE_ITEMS;
    private static final Map<String, MenuItemSpec> BY_ACTION;

    static {
        ArrayList<MenuItemSpec> editor = new ArrayList<>();
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 0, "Kopieren", "Kopieren", "editor_copy", false, false));
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 1, "Ausschneiden", "Ausschneiden", "editor_cut", false, false));
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 2, "Einfügen", "Einfügen", "editor_paste", false, false));
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 3, "Bild einfügen", "Bild einfügen", "editor_insert_image", false, true));
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 4, "Datum einfügen", "Datum einfügen", "editor_insert_date", false, false));
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 5, "Löschen", "Löschen", "editor_delete_selection", false, false));
        editor.add(new MenuItemSpec(MenuKind.EDITOR, 6, "Suchen", "Suchen", "editor_search", false, true));
        EDITOR_ITEMS = Collections.unmodifiableList(editor);

        ArrayList<MenuItemSpec> tree = new ArrayList<>();
        tree.add(new MenuItemSpec(MenuKind.TREE, 0, "Neu", "Neu darunter", "tree_new_child", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 1, "Neu", "Neu daneben", "tree_new_next", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 2, "Umbenennen", "Umbenennen", "tree_rename", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 3, "Kopieren", "Kopieren", "tree_copy", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 4, "Ausschneiden", "Ausschneiden", "tree_cut", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 5, "Einfuegen", "Einfügen", "tree_paste", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 6, "Loechen", "Löschen", "tree_delete", true, false));
        tree.add(new MenuItemSpec(MenuKind.TREE, 7, "Speichern", "RTF speichern", "tree_save_current_rtf", true, true));
        tree.add(new MenuItemSpec(MenuKind.TREE, 8, "Haft-Notiz", "Haft-Notiz", "tree_desktop_note", true, true));
        tree.add(new MenuItemSpec(MenuKind.TREE, 9, "Hintergrundfarbe", "Hintergrundfarbe", "tree_background_color", true, true));
        tree.add(new MenuItemSpec(MenuKind.TREE, 10, "Schriftfarbe", "Schriftfarbe", "tree_foreground_color", true, true));
        TREE_ITEMS = Collections.unmodifiableList(tree);

        LinkedHashMap<String, MenuItemSpec> byAction = new LinkedHashMap<>();
        for (MenuItemSpec item : EDITOR_ITEMS) byAction.put(item.action, item);
        for (MenuItemSpec item : TREE_ITEMS) byAction.put(item.action, item);
        BY_ACTION = Collections.unmodifiableMap(byAction);
    }

    private LegacyContextMenus() {}

    public static List<MenuItemSpec> editorItems() { return EDITOR_ITEMS; }
    public static List<MenuItemSpec> treeItems() { return TREE_ITEMS; }

    public static MenuItemSpec byAction(String action) {
        return BY_ACTION.get(clean(action));
    }

    public static MenuItemSpec byLegacyIndex(MenuKind kind, int index) {
        List<MenuItemSpec> items = kind == MenuKind.TREE ? TREE_ITEMS : EDITOR_ITEMS;
        for (MenuItemSpec item : items) if (item.legacyIndex == index) return item;
        return null;
    }

    public static List<String> displayLabels(MenuKind kind, boolean normalizeUmlauts) {
        List<MenuItemSpec> items = kind == MenuKind.TREE ? TREE_ITEMS : EDITOR_ITEMS;
        ArrayList<String> out = new ArrayList<>(items.size());
        for (MenuItemSpec item : items) out.add(item.displayLabel(normalizeUmlauts));
        return out;
    }

    public static String normalizedLegacyTreeLabel(String label) {
        String s = clean(label);
        if (s.equalsIgnoreCase("Einfuegen")) return "Einfügen";
        if (s.equalsIgnoreCase("Loechen")) return "Löschen";
        if (s.equalsIgnoreCase("Speichern")) return "RTF speichern";
        return s;
    }

    public static String actionFromLegacyLabel(MenuKind kind, String label, int duplicateIndexHint) {
        String wanted = clean(label).toLowerCase(Locale.ROOT);
        List<MenuItemSpec> items = kind == MenuKind.TREE ? TREE_ITEMS : EDITOR_ITEMS;
        int duplicate = 0;
        for (MenuItemSpec item : items) {
            if (item.legacyLabel.toLowerCase(Locale.ROOT).equals(wanted)
                    || item.normalizedLabel.toLowerCase(Locale.ROOT).equals(wanted)) {
                if (duplicate == duplicateIndexHint) return item.action;
                duplicate++;
            }
        }
        return "";
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
