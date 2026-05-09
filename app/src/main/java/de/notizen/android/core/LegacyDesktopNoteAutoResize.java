package de.notizen.android.core;

/** Decision model for desknote.vb set_clientsizes() without depending on WinForms widgets. */
public final class LegacyDesktopNoteAutoResize {
    public enum Action { SKIP, SHRINK, GROW_BOTH, GROW_WIDTH, NONE }

    public static final class Decision {
        public final Action action;
        public final LegacyDesktopNote.Rect rect;
        public final boolean shouldStoreWindow;
        public final boolean scrollManualAfterPass;
        public final String reason;

        public Decision(Action action, LegacyDesktopNote.Rect rect, boolean shouldStoreWindow, boolean scrollManualAfterPass, String reason) {
            this.action = action == null ? Action.NONE : action;
            this.rect = rect;
            this.shouldStoreWindow = shouldStoreWindow;
            this.scrollManualAfterPass = scrollManualAfterPass;
            this.reason = reason == null ? "" : reason;
        }
    }

    private LegacyDesktopNoteAutoResize() {}

    public static boolean mayRun(boolean mouseResizeActive, boolean scrollManual, long nowMillis, long lastResizeMillis) {
        return !mouseResizeActive && !scrollManual && nowMillis > lastResizeMillis + LegacyDesktopNote.AUTORESIZE_IDLE_MS;
    }

    public static Decision decideStep(LegacyDesktopNote.Rect rect,
                                      int richTextWidth, int richTextClientWidth,
                                      int richTextHeight, int richTextClientHeight,
                                      int workAreaWidth, int workAreaHeight,
                                      boolean mouseResizeActive, boolean scrollManual,
                                      long nowMillis, long lastResizeMillis) {
        LegacyDesktopNote.Rect safe = rect == null ? new LegacyDesktopNote.Rect(0, 0, 1, LegacyDesktopNote.MIN_AUTOSIZE_HEIGHT) : rect;
        if (!mayRun(mouseResizeActive, scrollManual, nowMillis, lastResizeMillis)) {
            return new Decision(Action.SKIP, safe, false, scrollManual, "idle guard or manual resize active");
        }
        int pad = LegacyDesktopNote.AUTORESIZE_SCROLL_PAD;
        if (richTextWidth < richTextClientWidth + pad) {
            return new Decision(Action.SHRINK, LegacyDesktopNote.autoResizeShrinkStep(safe), true, true, "set_clientsizes_a shrink");
        }
        if (richTextWidth > richTextClientWidth + pad && LegacyDesktopNote.autoResizeCanGrow(safe, workAreaWidth, workAreaHeight)) {
            return new Decision(Action.GROW_BOTH, LegacyDesktopNote.autoResizeGrowBothStep(safe, workAreaWidth, workAreaHeight), true, true, "set_clientsizes_b grow width and height");
        }
        if (richTextHeight > richTextClientHeight + pad && LegacyDesktopNote.autoResizeCanGrow(safe, workAreaWidth, workAreaHeight)) {
            return new Decision(Action.GROW_WIDTH, LegacyDesktopNote.autoResizeGrowWidthStep(safe, workAreaWidth, workAreaHeight), true, true, "set_clientsizes_c grow width");
        }
        return new Decision(Action.NONE, safe, true, true, "no scrollbar size change required");
    }
}
