package de.notizen.android.core;

import java.util.Locale;

/**
 * Dependency-free bridge for old WinForms ColorDialog choices.
 * Android uses it for node colors, desktop-note colors and RTF selection colors.
 */
public final class LegacyColorDialogModel {
    private LegacyColorDialogModel() {}

    public enum Role {
        NODE_BACKGROUND,
        NODE_FOREGROUND,
        RTF_TEXT,
        RTF_HIGHLIGHT,
        DESKTOP_NOTE_BACKGROUND
    }

    public static final class Decision {
        public final Role role;
        public final int argb;
        public final int red;
        public final int green;
        public final int blue;
        public final String css;
        public final String rtfColorTable;
        public final String rtfPrefix;
        public final boolean valid;

        private Decision(Role role, int argb, boolean valid) {
            this.role = role == null ? Role.NODE_BACKGROUND : role;
            this.argb = argb;
            this.red = (argb >> 16) & 0xff;
            this.green = (argb >> 8) & 0xff;
            this.blue = argb & 0xff;
            this.css = String.format(Locale.ROOT, "#%02x%02x%02x", red, green, blue);
            this.rtfColorTable = "{\\colortbl ;\\red" + red + "\\green" + green + "\\blue" + blue + ";}";
            this.rtfPrefix = this.role == Role.RTF_HIGHLIGHT ? "\\highlight1" : "\\cf1";
            this.valid = valid;
        }
    }

    public static Decision choosePalette(Role role, int index) {
        int[] p = legacyPaletteArgb();
        int safe = index < 0 ? 0 : Math.min(index, p.length - 1);
        return new Decision(role, p[safe], true);
    }

    public static Decision chooseCss(Role role, String css) {
        Integer color = parseCssColor(css);
        if (color == null) return invalid(role);
        return new Decision(role, color.intValue(), true);
    }

    public static Decision invalid(Role role) {
        return new Decision(role, 0xff000000, false);
    }

    public static String[] paletteLabels() {
        int[] p = legacyPaletteArgb();
        String[] out = new String[p.length];
        String[] names = {"Schwarz", "Dunkelgrau", "Grau", "Hellgrau", "Weiß", "Rot", "Orange", "Gelb", "Grün", "Türkis", "Blau", "Violett", "Rosa", "Braun", "Zufall-Legacy"};
        for (int i = 0; i < p.length; i++) out[i] = names[i] + " " + toCss(p[i]);
        return out;
    }

    public static int[] legacyPaletteArgb() {
        return new int[]{
                0xff000000, 0xff404040, 0xff808080, 0xffc0c0c0, 0xffffffff,
                0xffff0000, 0xffffa500, 0xffffff00, 0xff00a000, 0xff00c0c0,
                0xff0000ff, 0xff800080, 0xffff80c0, 0xff8b4513, 0xfff5f5dc
        };
    }

    /** Historical Random.Next(0,14) never reached index 14. */
    public static int legacyRandomReachableCount() {
        return 14;
    }

    public static String toCss(int argb) {
        return String.format(Locale.ROOT, "#%02x%02x%02x", (argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff);
    }

    public static Integer parseCssColor(String value) {
        if (value == null) return null;
        String s = value.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty() || "transparent".equals(s) || "none".equals(s)) return null;
        if (s.startsWith("#")) return parseHex(s.substring(1));
        if (s.startsWith("rgba(") && s.endsWith(")")) return parseRgbArgs(s.substring(5, s.length() - 1));
        if (s.startsWith("rgb(") && s.endsWith(")")) return parseRgbArgs(s.substring(4, s.length() - 1));
        String named = namedColor(s);
        return named == null ? null : parseHex(named.substring(1));
    }

    private static Integer parseHex(String h) {
        try {
            if (h.length() == 3) {
                int r = Integer.parseInt(h.substring(0, 1) + h.substring(0, 1), 16);
                int g = Integer.parseInt(h.substring(1, 2) + h.substring(1, 2), 16);
                int b = Integer.parseInt(h.substring(2, 3) + h.substring(2, 3), 16);
                return 0xff000000 | (r << 16) | (g << 8) | b;
            }
            if (h.length() == 6) return 0xff000000 | Integer.parseInt(h, 16);
            if (h.length() == 8) {
                // CSS/Qt-style #AARRGGBB: keep visible RGB and force opaque storage.
                return 0xff000000 | Integer.parseInt(h.substring(2), 16);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static Integer parseRgbArgs(String body) {
        String[] parts = body.split(",");
        if (parts.length < 3) return null;
        try {
            int r = parseRgbComponent(parts[0]);
            int g = parseRgbComponent(parts[1]);
            int b = parseRgbComponent(parts[2]);
            return 0xff000000 | (r << 16) | (g << 8) | b;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int parseRgbComponent(String raw) {
        String s = raw.trim();
        if (s.endsWith("%")) {
            double p = Double.parseDouble(s.substring(0, s.length() - 1).trim());
            return clamp((int)Math.round(255.0 * p / 100.0));
        }
        return clamp((int)Math.round(Double.parseDouble(s)));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static String namedColor(String s) {
        if ("black".equals(s)) return "#000000";
        if ("white".equals(s)) return "#ffffff";
        if ("red".equals(s)) return "#ff0000";
        if ("green".equals(s)) return "#008000";
        if ("lime".equals(s)) return "#00ff00";
        if ("blue".equals(s)) return "#0000ff";
        if ("yellow".equals(s)) return "#ffff00";
        if ("orange".equals(s)) return "#ffa500";
        if ("purple".equals(s)) return "#800080";
        if ("magenta".equals(s) || "fuchsia".equals(s)) return "#ff00ff";
        if ("cyan".equals(s) || "aqua".equals(s)) return "#00ffff";
        if ("gray".equals(s) || "grey".equals(s)) return "#808080";
        if ("silver".equals(s)) return "#c0c0c0";
        if ("brown".equals(s)) return "#a52a2a";
        if ("chocolate".equals(s)) return "#d2691e";
        if ("teal".equals(s)) return "#008080";
        if ("lightgray".equals(s) || "lightgrey".equals(s)) return "#d3d3d3";
        if ("rebeccapurple".equals(s)) return "#663399";
        return null;
    }
}
