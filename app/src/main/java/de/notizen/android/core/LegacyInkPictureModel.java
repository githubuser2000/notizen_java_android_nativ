package de.notizen.android.core;

/**
 * Platform-neutral description of the Android ink-picture bridge.
 *
 * Notizen .NET/PyQt stored drawings in rich text as normal embedded pictures.
 * Android cannot embed a live desktop canvas into RTF, so the native port opens
 * a pen canvas and then commits the result as a regular RTF picture group.
 */
public final class LegacyInkPictureModel {
    public static final int DEFAULT_WIDTH_DP = 900;
    public static final int DEFAULT_HEIGHT_DP = 360;
    public static final int MIN_WIDTH_DP = 240;
    public static final int MIN_HEIGHT_DP = 160;
    public static final int MAX_WIDTH_DP = 1400;
    public static final int MAX_HEIGHT_DP = 900;

    private LegacyInkPictureModel() {}

    public static final class CanvasSpec {
        public final int widthDp;
        public final int heightDp;
        public final boolean stylusFriendly;
        public final String commitFormat;

        public CanvasSpec(int widthDp, int heightDp, boolean stylusFriendly, String commitFormat) {
            this.widthDp = clamp(widthDp, MIN_WIDTH_DP, MAX_WIDTH_DP);
            this.heightDp = clamp(heightDp, MIN_HEIGHT_DP, MAX_HEIGHT_DP);
            this.stylusFriendly = stylusFriendly;
            this.commitFormat = commitFormat == null || commitFormat.trim().isEmpty() ? "image/jpeg" : commitFormat.trim();
        }

        public String summary() {
            return widthDp + "x" + heightDp + "dp, " + commitFormat + (stylusFriendly ? ", Stift/Finger" : ", Finger");
        }
    }

    public static CanvasSpec defaultCanvas() {
        return new CanvasSpec(DEFAULT_WIDTH_DP, DEFAULT_HEIGHT_DP, true, "image/jpeg");
    }

    public static boolean canCommitToRtf(byte[] encodedImage, String mimeType) {
        return RtfUtils.rtfPictureFromImage(encodedImage, mimeType) != null;
    }

    public static String statusAfterCommit(int byteCount) {
        int safe = Math.max(0, byteCount);
        return "Stiftbild als RTF-Bild gespeichert (" + safe + " Bytes)";
    }

    public static float pressureWidthFactor(float pressure) {
        float p = pressure <= 0f ? 1f : pressure;
        return Math.max(0.45f, Math.min(2.35f, 0.35f + p * 1.65f));
    }

    public static boolean pressureChangesStrokeWidth(float lightPressure, float hardPressure) {
        return pressureWidthFactor(hardPressure) > pressureWidthFactor(lightPressure);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
