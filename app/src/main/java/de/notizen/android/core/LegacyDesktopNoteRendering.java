package de.notizen.android.core;

import java.util.Locale;

/** Compact mobile rendering bridge for legacy desktop sticky-note RTF. */
public final class LegacyDesktopNoteRendering {
    public static final String BODY_STYLE = "white-space: pre-wrap; margin:0; padding:0; line-height:100%;";
    public static final String EDITOR_CSS = "border:0; padding:0px; margin:0px; line-height:100%;";

    private LegacyDesktopNoteRendering() {}

    public static String compactHtml(NoteNode node) {
        return RtfUtils.rtfToDesktopHtml(node == null ? "" : node.rtf);
    }

    public static String argbToCssRgba(int argb, double opacityMultiplier) {
        int a = (argb >>> 24) & 0xff;
        int r = (argb >>> 16) & 0xff;
        int g = (argb >>> 8) & 0xff;
        int b = argb & 0xff;
        double alpha = Math.max(0.0, Math.min(1.0, (a / 255.0) * opacityMultiplier));
        return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.3f)", r, g, b, alpha);
    }

    public static String androidCardCss(DesktopNoteState state) {
        int argb = state == null ? 0xffffff99 : state.argb;
        double opacity = state == null ? 0.85 : LegacyDesktopNote.opacityForInactive(state.opacity);
        return "background-color: " + argbToCssRgba(argb, opacity) + "; " + EDITOR_CSS;
    }

    public static boolean shouldUseBottomHint(DesktopNoteState state) {
        return state != null && state.visible && state.opacity < 1.0;
    }

    public static String minimizedSemantics() {
        return "visible=true; showMinimized; no active-opacity reset";
    }
}
