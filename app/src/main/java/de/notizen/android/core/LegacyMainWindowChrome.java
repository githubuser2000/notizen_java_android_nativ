package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Custom borderless main-window chrome from the old WinForms version. */
public final class LegacyMainWindowChrome {
    public static final int HEADER_HEIGHT = 31;
    public static final int MIN_BUTTON_WIDTH = 30;
    public static final int CLOSE_BUTTON_WIDTH = 32;
    public static final int RESIZE_GRIP = 16;

    public enum HitAction { NONE, MOVE, MINIMIZE, CLOSE, RESIZE_BOTTOM_RIGHT }

    public static final class HitDecision {
        public final HitAction action;
        public final boolean blocked;
        public final String cursor;
        public final String reason;
        private HitDecision(HitAction action, boolean blocked, String cursor, String reason) {
            this.action = action == null ? HitAction.NONE : action;
            this.blocked = blocked;
            this.cursor = cursor == null ? "" : cursor;
            this.reason = reason == null ? "" : reason;
        }
        public String summary() { return (blocked ? "blockiert: " : "") + action + " / " + cursor + " / " + reason; }
    }

    public static final class PaintCommand {
        public final String type;
        public final int x;
        public final int y;
        public final int width;
        public final int height;
        public final String text;
        public PaintCommand(String type, int x, int y, int width, int height, String text) {
            this.type = type == null ? "" : type;
            this.x = x; this.y = y; this.width = width; this.height = height; this.text = text == null ? "" : text;
        }
        public String toString() { return summary(); }
        public String summary() { return type + " " + x + "," + y + " " + width + "x" + height + (text.isEmpty() ? "" : " " + text); }
    }

    private LegacyMainWindowChrome() {}

    public static HitDecision hitTest(int x, int y, int width, int height, boolean leftButtonDown, boolean blocked) {
        if (blocked) return new HitDecision(HitAction.NONE, true, "Default", "Legacy-Dialog sichtbar");
        int w = Math.max(0, width), h = Math.max(0, height);
        if (leftButtonDown && x >= w - RESIZE_GRIP && y >= h - RESIZE_GRIP) return new HitDecision(HitAction.RESIZE_BOTTOM_RIGHT, false, "SizeNWSE", "untere rechte Resize-Zone");
        if (y < HEADER_HEIGHT) {
            if (x < MIN_BUTTON_WIDTH) return new HitDecision(HitAction.MINIMIZE, false, "Hand", "linker _-Button");
            if (x >= w - CLOSE_BUTTON_WIDTH) return new HitDecision(HitAction.CLOSE, false, "Hand", "rechter x-Button");
            if (leftButtonDown) return new HitDecision(HitAction.MOVE, false, "SizeAll", "Titelleiste ziehen");
        }
        return new HitDecision(HitAction.NONE, false, "Default", "Clientbereich");
    }

    public static List<PaintCommand> paintCommands(String title, int width, int height) {
        int w = Math.max(1, width), h = Math.max(1, height);
        ArrayList<PaintCommand> out = new ArrayList<>();
        out.add(new PaintCommand("image-left-bb", 0, 0, 16, HEADER_HEIGHT, "bb"));
        out.add(new PaintCommand("header-bg", 16, 0, Math.max(0, w - 32), HEADER_HEIGHT, ""));
        out.add(new PaintCommand("image-right-bb", Math.max(0, w - 16), 0, 16, HEADER_HEIGHT, "bb"));
        out.add(new PaintCommand("button-minimize", 0, 0, MIN_BUTTON_WIDTH, HEADER_HEIGHT, "_"));
        out.add(new PaintCommand("button-close", Math.max(0, w - CLOSE_BUTTON_WIDTH), 0, CLOSE_BUTTON_WIDTH, HEADER_HEIGHT, "x"));
        out.add(new PaintCommand("title", MIN_BUTTON_WIDTH + 4, 0, Math.max(0, w - MIN_BUTTON_WIDTH - CLOSE_BUTTON_WIDTH - 8), HEADER_HEIGHT, title == null ? "" : title));
        out.add(new PaintCommand("border-left", 0, HEADER_HEIGHT, 1, Math.max(0, h - HEADER_HEIGHT), ""));
        out.add(new PaintCommand("border-right", Math.max(0, w - 1), HEADER_HEIGHT, 1, Math.max(0, h - HEADER_HEIGHT), ""));
        out.add(new PaintCommand("resize-grip", Math.max(0, w - RESIZE_GRIP), Math.max(0, h - RESIZE_GRIP), RESIZE_GRIP, RESIZE_GRIP, ""));
        return Collections.unmodifiableList(out);
    }

    public static String summary(String title, int width, int height) {
        StringBuilder b = new StringBuilder("Legacy-Hauptfenster-Chrome ").append(width).append("x").append(height);
        for (PaintCommand c : paintCommands(title, width, height)) b.append('\n').append(c.summary());
        return b.toString();
    }
}
