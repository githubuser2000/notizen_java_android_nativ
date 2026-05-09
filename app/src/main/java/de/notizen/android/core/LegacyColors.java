package de.notizen.android.core;

import java.util.Random;

/** System.Drawing KnownColor values used by Notizen.NET get_lightcolor(). */
public final class LegacyColors {
    private LegacyColors() {}

    public static final String[] LIGHT_COLOR_RGB = new String[]{
            "#f08080", // 0 LightCoral
            "#e0ffff", // 1 LightCyan
            "#90ee90", // 2 LightGreen
            "#87cefa", // 3 LightSkyBlue
            "#ffffe0", // 4 LightYellow
            "#b0c4de", // 5 LightSteelBlue
            "#ffa07a", // 6 LightSalmon
            "#fafad2", // 7 LightGoldenrodYellow
            "#add8e6", // 8 LightBlue
            "#87ceeb", // 9 SkyBlue
            "#ffff00", // 10 Yellow
            "#ffffff", // 11 White
            "#adff2f", // 12 GreenYellow
            "#00ffff", // 13 Cyan
            "#ff00ff", // 14 Magenta, present in source but unreachable by Random.Next(0, 14)
            "#d3d3d3"  // 15 LightGray fallback, also unreachable by the old automatic choice
    };

    public static final String[] LIGHT_COLOR_NAMES = new String[]{
            "LightCoral", "LightCyan", "LightGreen", "LightSkyBlue", "LightYellow", "LightSteelBlue",
            "LightSalmon", "LightGoldenrodYellow", "LightBlue", "SkyBlue", "Yellow", "White",
            "GreenYellow", "Cyan", "Magenta", "LightGray"
    };

    /** VB Random.Next(0, 14) returns only 0..13. */
    public static final int LEGACY_RANDOM_LIGHT_COLOR_COUNT = 14;

    public static final int[] LIGHT_COLOR_ARGB = new int[LIGHT_COLOR_RGB.length];
    public static final int[] LEGACY_RANDOM_LIGHT_COLOR_ARGB = new int[LEGACY_RANDOM_LIGHT_COLOR_COUNT];

    static {
        for (int i = 0; i < LIGHT_COLOR_RGB.length; i++) {
            LIGHT_COLOR_ARGB[i] = rgbToSignedArgb(LIGHT_COLOR_RGB[i], 255);
            if (i < LEGACY_RANDOM_LIGHT_COLOR_COUNT) {
                LEGACY_RANDOM_LIGHT_COLOR_ARGB[i] = LIGHT_COLOR_ARGB[i];
            }
        }
    }

    public static int rgbToSignedArgb(String rgb, int alpha) {
        String text = rgb == null ? "" : rgb.trim();
        if (text.startsWith("#")) text = text.substring(1);
        if (text.length() != 6) throw new IllegalArgumentException("Expected #RRGGBB color: " + rgb);
        long value = ((long) (alpha & 0xff) << 24) | Long.parseLong(text, 16);
        return (int) value;
    }

    public static int legacyLightColorArgb(Random random) {
        Random rng = random == null ? new Random() : random;
        return LEGACY_RANDOM_LIGHT_COLOR_ARGB[rng.nextInt(LEGACY_RANDOM_LIGHT_COLOR_COUNT)];
    }

    public static String toCssRgb(int argb) {
        return String.format("#%06x", argb & 0x00ffffff);
    }
}
