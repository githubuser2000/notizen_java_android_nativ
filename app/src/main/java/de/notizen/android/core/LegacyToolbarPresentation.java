package de.notizen.android.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Portable model of the late PyQt toolbar pass: icon-only buttons with stable
 * legacy action names and meaningful tooltips. Android uses these glyphs as
 * compact text icons and keeps the old German label as accessibility text.
 */
public final class LegacyToolbarPresentation {
    public static final int MAIN_TOOLBAR_HEIGHT_DP = 48;
    public static final int MAIN_TOOLBAR_BUTTON_HEIGHT_DP = 46;
    public static final int MAIN_TOOLBAR_PADDING_DP = 6;

    public static final class ButtonSpec {
        public final String legacyLabel;
        public final String action;
        public final String tooltip;
        public final String iconGlyph;
        public final String shortcut;
        public final String toolstripGroup;

        public ButtonSpec(String legacyLabel, String action, String tooltip, String iconGlyph, String shortcut, String toolstripGroup) {
            this.legacyLabel = clean(legacyLabel);
            this.action = clean(action);
            this.tooltip = clean(tooltip);
            this.iconGlyph = clean(iconGlyph);
            this.shortcut = clean(shortcut);
            this.toolstripGroup = clean(toolstripGroup);
        }

        public String displayText(boolean iconOnly) {
            if (iconOnly && !iconGlyph.isEmpty()) return iconGlyph;
            return legacyLabel;
        }

        public String contentDescription() {
            if (shortcut.isEmpty()) return tooltip.isEmpty() ? legacyLabel : tooltip;
            return (tooltip.isEmpty() ? legacyLabel : tooltip) + " (" + shortcut + ")";
        }
    }

    private static final Map<String, ButtonSpec> BY_LABEL;
    static {
        LinkedHashMap<String, ButtonSpec> m = new LinkedHashMap<>();
        put(m, "Neu", "file_new", "Neue Notizdatei", "＋", "Ctrl+N", "haupt");
        put(m, "Öffnen", "file_open", "ALX-Datei öffnen", "📂", "Ctrl+O", "haupt");
        put(m, "Letzte", "file_recent", "Letzte Datei öffnen", "🕘", "", "haupt");
        put(m, "Speichern", "file_save", "Speichern", "💾", "Ctrl+S", "haupt");
        put(m, "Speichern unter", "file_save_as", "Speichern unter", "⇩", "", "haupt");
        put(m, "Sicherungen", "file_backups", "Sicherungen anzeigen", "☷", "", "haupt");
        put(m, "Einstellungen", "settings", "Einstellungen", "⚙", "", "haupt");
        put(m, "Config", "config_snapshot", "Legacy-Config anzeigen", "☰", "", "haupt");
        put(m, "FTP öffnen", "ftp_open", "Per FTP öffnen", "⇣", "", "haupt");
        put(m, "FTP speichern", "ftp_save", "Per FTP speichern", "⇡", "", "haupt");
        put(m, "Schließen", "file_close", "Datei schließen", "×", "Ctrl+W", "haupt");
        put(m, "Status", "legacy_status", "Legacy-Dokumentstatus", "ⓢ", "", "haupt");
        put(m, "Kind", "tree_new_child", "Neuen Unterknoten anlegen", "▸＋", "Insert", "elements");
        put(m, "Daneben", "tree_new_next", "Neuen Knoten daneben anlegen", "＋▸", "Shift+Insert", "elements");
        put(m, "Auf/Zu", "tree_toggle", "Unterknoten ein- oder ausklappen", "▾", "Enter", "elements");
        put(m, "Alle auf", "tree_expand_all", "Alle Knoten öffnen", "⇊", "", "elements");
        put(m, "Alle zu", "tree_collapse_all", "Alle Knoten schließen", "⇈", "", "elements");
        put(m, "Löschen", "tree_delete", "Knoten löschen", "🗑", "Delete", "elements");
        put(m, "Kopieren", "tree_copy", "Knoten kopieren", "⧉", "Ctrl+C", "cutpastecopy");
        put(m, "Ausschneiden", "tree_cut", "Knoten ausschneiden", "✂", "Ctrl+X", "cutpastecopy");
        put(m, "Einfügen", "tree_paste", "Knoten einfügen", "📋", "Ctrl+V", "cutpastecopy");
        put(m, "Rauf", "tree_up", "Knoten nach oben", "↑", "Ctrl+↑", "elements");
        put(m, "Runter", "tree_down", "Knoten nach unten", "↓", "Ctrl+↓", "elements");
        put(m, "Einrücken", "tree_indent", "Knoten einrücken", "↳", "Ctrl++", "elements");
        put(m, "Ausrücken", "tree_outdent", "Knoten ausrücken", "↰", "Ctrl+-", "elements");
        put(m, "Vor Ziel", "tree_move_before", "Knoten vor Ziel verschieben", "⇥", "", "elements");
        put(m, "Datum", "insert_date", "Datum einfügen", "📅", "Ctrl+D", "font");
        put(m, "Punkt", "insert_bullet", "Aufzählungspunkt einfügen", "•", "", "font");
        put(m, "RTF Format", "rtf_format", "RTF-Formatierung anwenden", "𝐁", "", "font");
        put(m, "Scroll", "cycle_scrollbars", "Scrollleisten wechseln", "↕", "", "font");
        put(m, "Bild", "insert_image", "Bild einfügen", "▧", "", "font");
        put(m, "Vorschau", "preview", "HTML-/RTF-Vorschau", "👁", "", "haupt");
        put(m, "RTF Info", "rtf_info", "RTF-Metriken anzeigen", "ℹR", "", "haupt");
        put(m, "Drucken", "print", "Drucken", "⎙", "Ctrl+P", "haupt");
        put(m, "HTML Import", "import_html", "HTML importieren", "H", "", "haupt");
        put(m, "TXT Import", "import_txt", "Text importieren", "T", "", "haupt");
        put(m, "RTF Import", "import_rtf", "RTF importieren", "R", "", "haupt");
        put(m, "Farben", "colors", "Farben", "🎨", "", "font");
        put(m, "Haftnotiz", "desktop_note", "Desktop-Haftnotizdaten", "▣", "", "elements");
        put(m, "Haft Layout", "desktop_note_layout", "Haftnotiz-Layout anzeigen", "▧", "", "elements");
        put(m, "Haftliste", "desktop_note_tray", "Haftnotiz-Trayliste", "☰▣", "", "elements");
        put(m, "Haft weg", "desktop_notes_clear_subtree", "Haftnotizen im Teilbaum entfernen", "▢", "", "elements");
        put(m, "Wecker", "alarm", "Wecker", "⏰", "", "elements");
        put(m, "Suche", "search", "Suchen", "⌕", "Ctrl+F", "haupt");
        put(m, "Export TXT", "export_txt", "Als Text exportieren", "TXT", "", "haupt");
        put(m, "Export ANSI", "export_txt_ansi", "Als ANSI-Text exportieren", "ANSI", "", "haupt");
        put(m, "Export Unicode", "export_txt_unicode", "Als Unicode-Text exportieren", "UNI", "", "haupt");
        put(m, "Export HTML", "export_html", "Als HTML exportieren", "HTML", "", "haupt");
        put(m, "Export RTF", "export_rtf", "Als RTF exportieren", "RTF", "", "haupt");
        put(m, "Knoten TXT", "export_node_txt", "Aktuellen Knoten als Text speichern", "N-TXT", "", "haupt");
        put(m, "Knoten RTF", "export_node_rtf", "Aktuellen Knoten als RTF speichern", "N-RTF", "", "haupt");
        put(m, "Teilbaum", "unify_current", "Aktuellen Teilbaum zusammenfassen", "Σ◦", "", "haupt");
        put(m, "Gesamt", "unify_root", "Gesamten Baum zusammenfassen", "Σ", "", "haupt");
        put(m, "Passwort", "password", "Passwort setzen oder entfernen", "🔒", "", "haupt");
        put(m, "Statistik", "stats", "Statistik", "#", "", "haupt");
        put(m, "Validieren", "validate", "ALX validieren", "✓", "", "haupt");
        put(m, "Diagnose", "diagnostics", "Lokale Diagnose anzeigen", "⚕", "", "haupt");
        put(m, "Layout", "layout_diagnostics", "Legacy-Layoutdiagnose", "▤", "", "haupt");
        put(m, "Launcher", "runtime_identity", "Launcher-/Runtime-Identität", "☸", "", "haupt");
        put(m, "Dialoge", "dialog_focus", "Legacy-Dialogfokus", "◫", "", "haupt");
        put(m, "Toolbars", "toolbar_toggles", "Legacy-ToolStrip-Toggles", "▥", "", "haupt");
        put(m, "Fenster", "main_window_chrome", "Legacy-Hauptfenster-Chrome", "▣", "", "haupt");
        put(m, "Autosave", "autosave_tick", "Legacy-Autosave-Tick", "⟳", "", "haupt");
        put(m, "Fontplan", "font_set_plan", "Legacy-font_set-Modell", "F+", "", "haupt");
        put(m, "Maus", "window_move_resize", "Legacy-Move-/Resize-Modell", "↔", "", "haupt");
        put(m, "ALX Pipe", "alx_stream_pipeline", "Legacy-ALX-Stream-Pipeline", "XML", "", "haupt");
        put(m, "Buildplan", "apk_build_plan", "Termux-APK-Buildplan", "APK", "", "haupt");
        put(m, "Feedback", "feedback", "Feedback lokal archivieren", "✉", "", "haupt");
        put(m, "Info", "about", "Informationen", "i", "", "haupt");
        BY_LABEL = Collections.unmodifiableMap(m);
    }

    private LegacyToolbarPresentation() {}

    public static Map<String, ButtonSpec> specsByLabel() { return BY_LABEL; }

    public static ButtonSpec forLabel(String label) {
        ButtonSpec spec = BY_LABEL.get(clean(label));
        if (spec != null) return spec;
        return new ButtonSpec(label, legacyActionFromLabel(label), clean(label), "", "", "haupt");
    }

    public static String iconText(String label) { return forLabel(label).displayText(true); }

    public static String tooltip(String label) { return forLabel(label).contentDescription(); }

    public static String legacyActionFromLabel(String label) {
        String s = clean(label).toLowerCase(java.util.Locale.ROOT);
        if (s.isEmpty()) return "";
        s = s.replace('ä', 'a').replace('ö', 'o').replace('ü', 'u').replace('ß', 's');
        StringBuilder out = new StringBuilder(s.length());
        boolean underscore = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                out.append(c);
                underscore = false;
            } else if (!underscore) {
                out.append('_');
                underscore = true;
            }
        }
        while (out.length() > 0 && out.charAt(out.length() - 1) == '_') out.setLength(out.length() - 1);
        return out.toString();
    }

    private static void put(Map<String, ButtonSpec> m, String label, String action, String tooltip, String glyph, String shortcut, String group) {
        m.put(clean(label), new ButtonSpec(label, action, tooltip, glyph, shortcut, group));
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
