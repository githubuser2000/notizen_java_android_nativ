package de.notizen.android.core;

/** Portable decisions from change_win_state / VisibleChanged / Deactivate. */
public final class LegacyWindowToggle {
    public enum WindowState { NORMAL, MINIMIZED, MAXIMIZED, HIDDEN }

    public static final class Result {
        public final boolean visible;
        public final WindowState state;
        public final String action;
        public final int minimizedStartedCount;
        public final boolean rememberedMaximized;

        public Result(boolean visible, WindowState state, String action, int minimizedStartedCount, boolean rememberedMaximized) {
            this.visible = visible;
            this.state = state == null ? WindowState.NORMAL : state;
            this.action = action == null ? "" : action;
            this.minimizedStartedCount = Math.max(0, minimizedStartedCount);
            this.rememberedMaximized = rememberedMaximized;
        }
    }

    private LegacyWindowToggle() {}

    public static Result changeWinState(boolean currentlyVisible, WindowState currentState, boolean restoreMaximized, boolean minimizeToTaskbar) {
        WindowState cur = currentState == null ? WindowState.NORMAL : currentState;
        if (!currentlyVisible || cur == WindowState.HIDDEN) {
            return new Result(true, restoreMaximized ? WindowState.MAXIMIZED : WindowState.NORMAL, "restore", 0, restoreMaximized);
        }
        if (minimizeToTaskbar) return new Result(true, WindowState.MINIMIZED, "minimize", 0, cur == WindowState.MAXIMIZED);
        return new Result(false, WindowState.HIDDEN, "hide", 0, cur == WindowState.MAXIMIZED);
    }

    public static boolean rememberMaximizedFromLocationChange(WindowState state, boolean formBeingClosed) {
        return !formBeingClosed && state == WindowState.MAXIMIZED;
    }

    public static Result visibleChangedForMinimizedStart(boolean minimizedStart, boolean firstVisibleEvent, int minimizedStartedCount, boolean canMinimize) {
        int count = Math.max(0, minimizedStartedCount);
        if (minimizedStart && firstVisibleEvent && count == 0 && canMinimize) {
            return new Result(true, WindowState.MINIMIZED, "minimized-start", 1, false);
        }
        return new Result(true, WindowState.NORMAL, "visible", count, false);
    }

    public static WindowState fromLegacyString(String value) {
        String s = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        if (s.startsWith("max")) return WindowState.MAXIMIZED;
        if (s.startsWith("min")) return WindowState.MINIMIZED;
        if (s.equals("hidden") || s.equals("versteckt")) return WindowState.HIDDEN;
        return WindowState.NORMAL;
    }
}
