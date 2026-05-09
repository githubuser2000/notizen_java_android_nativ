package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Legacy model of Notizen.NET's ToolStrip_fontstyle commands. */
public final class LegacyRichTextToolbar {
    public static final String TOOLBAR_OBJECT_NAME = "ToolStrip_fontstyle";
    public static final int LEGACY_TOOLBAR_HEIGHT_DP = 48;
    public static final int LEGACY_FIELD_HEIGHT_DP = 38;
    public static final int LEGACY_FONT_SIZE_MIN = 6;
    public static final int LEGACY_FONT_SIZE_MAX = 99;

    public static final class ActionSpec {
        public final String objectName;
        public final String action;
        public final String label;
        public final String tooltip;
        public final String shortcut;
        public final String iconGlyph;
        public final boolean checkable;

        public ActionSpec(String objectName, String action, String label, String tooltip, String shortcut, String iconGlyph, boolean checkable) {
            this.objectName = objectName == null ? "" : objectName;
            this.action = action == null ? "" : action;
            this.label = label == null ? "" : label;
            this.tooltip = tooltip == null ? "" : tooltip;
            this.shortcut = shortcut == null ? "" : shortcut;
            this.iconGlyph = iconGlyph == null ? "" : iconGlyph;
            this.checkable = checkable;
        }
    }

    private static final List<ActionSpec> FONT_ACTIONS;
    private static final List<String> LEGACY_FONT_FAMILIES;
    private static final int[] LEGACY_FONT_SIZE_ITEMS = new int[]{6,8,9,10,11,12,14,16,18,20,22,24,26,28,36,48,72};

    static {
        ArrayList<ActionSpec> actions = new ArrayList<>();
        actions.add(new ActionSpec("ToolStrip_regular", "format_regular", "Normal", "Zeichenformat zurücksetzen", "", "A", false));
        actions.add(new ActionSpec("ToolStrip_bold", "format_bold", "Fett", "Fett", "Ctrl+B", "𝐁", true));
        actions.add(new ActionSpec("ToolStrip_italic", "format_italic", "Kursiv", "Kursiv", "Ctrl+I", "𝐼", true));
        actions.add(new ActionSpec("ToolStrip_underline", "format_underline", "Unterstrichen", "Unterstrichen", "", "U̲", true));
        actions.add(new ActionSpec("ToolStrip_strikeout", "format_strike", "Durchgestrichen", "Durchgestrichen", "", "S̶", true));
        actions.add(new ActionSpec("ToolStrip_bigger", "font_bigger", "Schrift größer", "Schrift größer", "Ctrl++", "+", false));
        actions.add(new ActionSpec("ToolStrip_smaller", "font_smaller", "Schrift kleiner", "Schrift kleiner", "Ctrl+-", "−", false));
        actions.add(new ActionSpec("ToolStrip_fontsizenumber", "font_size", "Schriftgröße", "Schriftgröße", "", "12", false));
        actions.add(new ActionSpec("ToolStrip_fonts", "font_family", "Schriftart", "Schriftart", "", "F", false));
        actions.add(new ActionSpec("ToolStrip_dot", "legacy_bullet", "Punkt", "Aufzählungspunkt", "", "•", false));
        actions.add(new ActionSpec("ToolStrip_whatscroll", "cycle_scrollbars", "Scroll", "Scrollleisten wechseln", "", "↕", false));
        actions.add(new ActionSpec("fgcolorToolStripMenuItem", "text_color", "Textfarbe", "Textfarbe", "", "■", false));
        actions.add(new ActionSpec("bgcolorToolStripMenuItem", "highlight_color", "Hintergrundfarbe", "Hintergrundfarbe", "", "▣", false));
        actions.add(new ActionSpec("ToolStrip_alignleft", "align_left", "Linksbündig", "Linksbündig", "", "☰", true));
        actions.add(new ActionSpec("ToolStrip_aligncenter", "align_center", "Zentriert", "Zentriert", "", "≡", true));
        actions.add(new ActionSpec("ToolStrip_alignright", "align_right", "Rechtsbündig", "Rechtsbündig", "", "☷", true));
        actions.add(new ActionSpec("ToolStrip_alignjustify", "align_justify", "Blocksatz", "Blocksatz", "", "▤", true));
        FONT_ACTIONS = Collections.unmodifiableList(actions);

        ArrayList<String> families = new ArrayList<>();
        Collections.addAll(families,
                "Microsoft Sans Serif", "Arial", "Calibri", "Times New Roman", "Courier New",
                "Verdana", "Tahoma", "Segoe UI", "Georgia", "Consolas");
        LEGACY_FONT_FAMILIES = Collections.unmodifiableList(families);
    }

    private LegacyRichTextToolbar() {}

    public static List<ActionSpec> fontActions() { return FONT_ACTIONS; }
    public static List<String> legacyFontFamilies() { return LEGACY_FONT_FAMILIES; }

    public static int[] legacyFontSizeItems() {
        int[] copy = new int[LEGACY_FONT_SIZE_ITEMS.length];
        System.arraycopy(LEGACY_FONT_SIZE_ITEMS, 0, copy, 0, LEGACY_FONT_SIZE_ITEMS.length);
        return copy;
    }

    public static ActionSpec findByObjectName(String objectName) {
        if (objectName == null) return null;
        for (ActionSpec spec : FONT_ACTIONS) if (objectName.equals(spec.objectName)) return spec;
        return null;
    }

    public static ActionSpec findByAction(String action) {
        if (action == null) return null;
        for (ActionSpec spec : FONT_ACTIONS) if (action.equals(spec.action)) return spec;
        return null;
    }

    public static int normalizeFontSize(int value) {
        if (value < LEGACY_FONT_SIZE_MIN) return LEGACY_FONT_SIZE_MIN;
        if (value > LEGACY_FONT_SIZE_MAX) return LEGACY_FONT_SIZE_MAX;
        return value;
    }

    public static String normalizeFontFamily(String value) {
        String clean = value == null ? "" : value.replace('\\', ' ').replace('{', ' ').replace('}', ' ').replace(';', ' ').trim();
        clean = clean.replaceAll("\\s+", " ");
        if (clean.isEmpty()) return "Microsoft Sans Serif";
        for (String family : LEGACY_FONT_FAMILIES) if (family.equalsIgnoreCase(clean)) return family;
        return clean;
    }

    public static int nearestLegacyFontSize(int value) {
        int normalized = normalizeFontSize(value);
        int best = LEGACY_FONT_SIZE_ITEMS[0];
        int bestDiff = Math.abs(best - normalized);
        for (int size : LEGACY_FONT_SIZE_ITEMS) {
            int diff = Math.abs(size - normalized);
            if (diff < bestDiff) { best = size; bestDiff = diff; }
        }
        return best;
    }

    public static int nextFontSize(int current, int deltaSteps) {
        int nearest = nearestLegacyFontSize(current);
        int idx = 0;
        for (int i = 0; i < LEGACY_FONT_SIZE_ITEMS.length; i++) {
            if (LEGACY_FONT_SIZE_ITEMS[i] == nearest) { idx = i; break; }
        }
        int target = Math.max(0, Math.min(LEGACY_FONT_SIZE_ITEMS.length - 1, idx + deltaSteps));
        return LEGACY_FONT_SIZE_ITEMS[target];
    }

    public static String iconOnlyDescription(ActionSpec spec) {
        if (spec == null) return "";
        String glyph = spec.iconGlyph.isEmpty() ? "□" : spec.iconGlyph;
        return glyph + " — " + spec.tooltip;
    }
}
