package de.notizen.android.core;

import java.util.Locale;

/** Legacy RichTextBoxScrollBars cycle from inhalt.vb / ToolStrip_whatscroll. */
public final class LegacyScrollbars {
    public static final int NONE = 0;
    public static final int HORIZONTAL = 1;
    public static final int VERTICAL = 2;
    public static final int BOTH = 3;

    public static final class Mode {
        public final int choice;
        public final boolean horizontal;
        public final boolean vertical;
        public final String legacyName;
        public final String labelDe;

        private Mode(int choice, boolean horizontal, boolean vertical, String legacyName, String labelDe) {
            this.choice = choice;
            this.horizontal = horizontal;
            this.vertical = vertical;
            this.legacyName = legacyName;
            this.labelDe = labelDe;
        }
    }

    private LegacyScrollbars() {}

    public static int normalizeChoice(int choice) {
        if (choice < NONE || choice > BOTH) return BOTH;
        return choice;
    }

    public static int normalizeChoice(String value) {
        if (value == null || value.trim().isEmpty()) return BOTH;
        String s = value.trim().toLowerCase(Locale.ROOT);
        try { return normalizeChoice(Integer.parseInt(s)); } catch (Exception ignored) {}
        if (s.equals("none") || s.equals("keine") || s.equals("no") || s.equals("0")) return NONE;
        if (s.equals("horizontal") || s.equals("horizontale") || s.equals("waagerecht") || s.equals("1")) return HORIZONTAL;
        if (s.equals("vertical") || s.equals("vertikal") || s.equals("senkrecht") || s.equals("2")) return VERTICAL;
        if (s.equals("both") || s.equals("beide") || s.equals("alle") || s.equals("3")) return BOTH;
        return BOTH;
    }

    public static int nextChoice(int choice) {
        return (normalizeChoice(choice) + 1) % 4;
    }

    public static Mode modeForChoice(int choice) {
        switch (normalizeChoice(choice)) {
            case NONE: return new Mode(NONE, false, false, "None", "Keine");
            case HORIZONTAL: return new Mode(HORIZONTAL, true, false, "Horizontal", "Horizontal");
            case VERTICAL: return new Mode(VERTICAL, false, true, "Vertical", "Vertikal");
            default: return new Mode(BOTH, true, true, "Both", "Beide");
        }
    }

    public static String label(int choice) { return modeForChoice(choice).labelDe; }

    public static String legacyName(int choice) { return modeForChoice(choice).legacyName; }
}
