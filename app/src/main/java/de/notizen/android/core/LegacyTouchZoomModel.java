package de.notizen.android.core;

/**
 * Small platform-neutral model for the Android two-finger zoom controls.
 *
 * Notizen .NET changed font sizes through toolbar buttons.  Android adds
 * pinch gestures, but the values still need to stay in the same conservative
 * ranges so ALX files remain readable on desktop and mobile.
 */
public final class LegacyTouchZoomModel {
    public static final int DEFAULT_TOOLBAR_BUTTON_DP = 24;
    public static final int MIN_TOOLBAR_BUTTON_DP = 24;
    public static final int MAX_TOOLBAR_BUTTON_DP = 48;
    public static final int TOOLBAR_BUTTON_STEP_DP = 4;

    public static final float DEFAULT_HEADER_TEXT_SP = 14f;
    public static final float MIN_HEADER_TEXT_SP = 10f;
    public static final float MAX_HEADER_TEXT_SP = 26f;
    public static final float HEADER_TEXT_STEP_SP = 1f;

    public static final int MAX_EMBED_IMAGE_LONG_EDGE_PX = 1600;
    public static final int MAX_EMBEDDED_IMAGE_BYTES = 1536 * 1024;

    private LegacyTouchZoomModel() {}

    public static int normalizeToolbarButtonDp(int value) {
        if (value <= 0) return DEFAULT_TOOLBAR_BUTTON_DP;
        return Math.max(MIN_TOOLBAR_BUTTON_DP, Math.min(MAX_TOOLBAR_BUTTON_DP, value));
    }

    public static int toolbarButtonDpByStep(int current, int direction) {
        int base = normalizeToolbarButtonDp(current);
        int delta = direction < 0 ? -TOOLBAR_BUTTON_STEP_DP : (direction > 0 ? TOOLBAR_BUTTON_STEP_DP : 0);
        return normalizeToolbarButtonDp(base + delta);
    }

    public static float normalizeHeaderTextSp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value) || value <= 0f) return DEFAULT_HEADER_TEXT_SP;
        return Math.max(MIN_HEADER_TEXT_SP, Math.min(MAX_HEADER_TEXT_SP, value));
    }

    public static float headerTextSpByStep(float current, int direction) {
        float base = normalizeHeaderTextSp(current);
        float delta = direction < 0 ? -HEADER_TEXT_STEP_SP : (direction > 0 ? HEADER_TEXT_STEP_SP : 0f);
        return normalizeHeaderTextSp(base + delta);
    }

    public static int fontSizeByStep(int currentPointSize, int direction) {
        return LegacyRichTextToolbar.nextFontSize(currentPointSize, direction < 0 ? -1 : (direction > 0 ? 1 : 0));
    }

    public static int safeImageLongEdge(int width, int height) {
        return Math.max(1, Math.max(width, height));
    }

    public static boolean shouldDownsampleForRtf(int width, int height, long byteCount, boolean rtfSupported) {
        if (!rtfSupported) return true;
        if (byteCount > MAX_EMBEDDED_IMAGE_BYTES) return true;
        return safeImageLongEdge(width, height) > MAX_EMBED_IMAGE_LONG_EDGE_PX;
    }
}
