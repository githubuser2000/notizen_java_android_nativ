package de.notizen.android.core;

/** Low-level mouse drag model for the legacy borderless main/sticky windows. */
public final class LegacyWindowMoveResizeModel {
    public enum Mode { NONE, MOVE, RESIZE, MINIMIZE, CLOSE, BLOCKED }

    public static final class Rect {
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public Rect(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = Math.max(1, width); this.height = Math.max(1, height);
        }
        public String toString() { return x + "," + y + " " + width + "x" + height; }
    }

    public static final class DragState {
        public final Mode mode;
        public final Rect startRect;
        public final int mouseScreenX;
        public final int mouseScreenY;
        public final int localX;
        public final int localY;
        public final boolean hideToolstripsDuringResize;
        public DragState(Mode mode, Rect startRect, int mouseScreenX, int mouseScreenY, int localX, int localY, boolean hideToolstripsDuringResize) {
            this.mode = mode == null ? Mode.NONE : mode;
            this.startRect = startRect == null ? new Rect(0, 0, 1, 1) : startRect;
            this.mouseScreenX = mouseScreenX; this.mouseScreenY = mouseScreenY; this.localX = localX; this.localY = localY;
            this.hideToolstripsDuringResize = hideToolstripsDuringResize;
        }
        public String summary() { return mode + " start=" + startRect + " local=" + localX + "," + localY; }
    }

    public static final class Update {
        public final Rect rect;
        public final boolean showToolstripsAgain;
        public final String status;
        public Update(Rect rect, boolean showToolstripsAgain, String status) {
            this.rect = rect == null ? new Rect(0, 0, 1, 1) : rect;
            this.showToolstripsAgain = showToolstripsAgain;
            this.status = status == null ? "" : status;
        }
        public String summary() { return status + " -> " + rect + (showToolstripsAgain ? " / ToolStrips zurück" : ""); }
    }

    private LegacyWindowMoveResizeModel() {}

    public static DragState mouseDown(Rect current, int localX, int localY, int screenX, int screenY, boolean leftButton, boolean blocked) {
        Rect r = current == null ? new Rect(0, 0, 1, 1) : current;
        if (blocked) return new DragState(Mode.BLOCKED, r, screenX, screenY, localX, localY, false);
        if (!leftButton) return new DragState(Mode.NONE, r, screenX, screenY, localX, localY, false);
        LegacyMainWindowChrome.HitDecision hit = LegacyMainWindowChrome.hitTest(localX, localY, r.width, r.height, true, false);
        Mode mode;
        switch (hit.action) {
            case MOVE: mode = Mode.MOVE; break;
            case MINIMIZE: mode = Mode.MINIMIZE; break;
            case CLOSE: mode = Mode.CLOSE; break;
            case RESIZE_BOTTOM_RIGHT: mode = Mode.RESIZE; break;
            default: mode = Mode.NONE; break;
        }
        return new DragState(mode, r, screenX, screenY, localX, localY, mode == Mode.RESIZE);
    }

    public static Update mouseMove(DragState state, int screenX, int screenY, int minWidth, int minHeight) {
        DragState s = state == null ? new DragState(Mode.NONE, new Rect(0, 0, 1, 1), screenX, screenY, 0, 0, false) : state;
        int dx = screenX - s.mouseScreenX;
        int dy = screenY - s.mouseScreenY;
        if (s.mode == Mode.MOVE) return new Update(new Rect(s.startRect.x + dx, s.startRect.y + dy, s.startRect.width, s.startRect.height), false, "move");
        if (s.mode == Mode.RESIZE) return new Update(new Rect(s.startRect.x, s.startRect.y, Math.max(minWidth, s.startRect.width + dx - 1), Math.max(minHeight, s.startRect.height + dy - 1)), false, "resize");
        return new Update(s.startRect, false, s.mode == Mode.BLOCKED ? "blocked" : "unchanged");
    }

    public static Update mouseUp(DragState state) {
        DragState s = state == null ? new DragState(Mode.NONE, new Rect(0, 0, 1, 1), 0, 0, 0, 0, false) : state;
        return new Update(s.startRect, s.hideToolstripsDuringResize, "mouse-up");
    }
}
