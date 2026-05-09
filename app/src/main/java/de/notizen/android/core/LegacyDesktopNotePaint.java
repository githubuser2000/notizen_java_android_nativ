package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Geometry-only port of desknote.vb Form1_Paint and related hover-border layout. */
public final class LegacyDesktopNotePaint {
    public static final String RESOURCE_CORNER = "aa";

    public static final class DrawCommand {
        public final String kind;
        public final int x1;
        public final int y1;
        public final int x2;
        public final int y2;
        public final String text;

        public DrawCommand(String kind, int x1, int y1, int x2, int y2, String text) {
            this.kind = kind == null ? "" : kind;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.text = text == null ? "" : text;
        }

        @Override public String toString() {
            return kind + '(' + x1 + ',' + y1 + ',' + x2 + ',' + y2 + (text.isEmpty() ? "" : "," + text) + ')';
        }
    }

    public static final class PaintModel {
        public final LegacyDesktopNote.Rect window;
        public final LegacyDesktopNote.Rect editorRect;
        public final LegacyDesktopNote.Rect labelRect;
        public final int labelMaxWidth;
        public final boolean labelVisible;
        public final List<DrawCommand> commands;

        public PaintModel(LegacyDesktopNote.Rect window, LegacyDesktopNote.Rect editorRect,
                          LegacyDesktopNote.Rect labelRect, int labelMaxWidth, boolean labelVisible,
                          List<DrawCommand> commands) {
            this.window = window;
            this.editorRect = editorRect;
            this.labelRect = labelRect;
            this.labelMaxWidth = labelMaxWidth;
            this.labelVisible = labelVisible;
            this.commands = Collections.unmodifiableList(new ArrayList<>(commands == null ? Collections.<DrawCommand>emptyList() : commands));
        }

        public String summary() {
            return "Fenster " + window + " · Editor " + editorRect + " · Titel " + labelRect
                    + " · Befehle " + commands.size();
        }
    }

    private LegacyDesktopNotePaint() {}

    public static PaintModel expandedModel(int windowWidth, int windowHeight, int labelWidth, int labelHeight) {
        int w = Math.max(LegacyDesktopNote.HEADER_BUTTON_WIDTH * 2 + 1, windowWidth);
        int h = Math.max(LegacyDesktopNote.HEADER_HEIGHT + 1, windowHeight);
        LegacyDesktopNote.Rect win = new LegacyDesktopNote.Rect(0, 0, w, h);
        LegacyDesktopNote.Rect editor = LegacyDesktopNote.editorRect(w, h, true);
        LegacyDesktopNote.Rect label = LegacyDesktopNote.labelGeometry(w, Math.max(1, labelWidth), Math.max(1, labelHeight));
        int labelMax = labelWidth > w - 70 ? Math.max(1, w - 70) : 0;
        return new PaintModel(win, editor, label, labelMax, true, paintCommands(w, h));
    }

    public static PaintModel hiddenModel(int windowWidth, int windowHeight) {
        int w = Math.max(1, windowWidth);
        int h = Math.max(1, windowHeight);
        LegacyDesktopNote.Rect win = new LegacyDesktopNote.Rect(0, 0, w, h);
        return new PaintModel(win, LegacyDesktopNote.editorRect(w, h, false), new LegacyDesktopNote.Rect(0, 0, 0, 0), 0, false, Collections.<DrawCommand>emptyList());
    }

    public static PaintModel forState(DesktopNoteState state, String title) {
        DesktopNoteState s = state == null ? new DesktopNoteState() : state;
        int labelWidth = Math.max(20, (title == null ? 0 : title.length()) * 7);
        return expandedModel(Math.max(80, s.width), Math.max(60, s.height), labelWidth, 22);
    }

    public static List<DrawCommand> paintCommands(int windowWidth, int windowHeight) {
        int w = Math.max(LegacyDesktopNote.HEADER_BUTTON_WIDTH * 2 + 1, windowWidth);
        ArrayList<DrawCommand> out = new ArrayList<>();
        out.add(new DrawCommand("image:" + RESOURCE_CORNER, 0, 0, 36, 40, "left"));
        out.add(new DrawCommand("image:" + RESOURCE_CORNER, w - 36, 0, w, 40, "right"));
        out.add(new DrawCommand("line", 36, 26, w - 36, 26, "top-title"));
        out.add(new DrawCommand("line", 36, 0, 36, 40, "left-separator"));
        out.add(new DrawCommand("line", w - 36, 0, w - 36, 40, "right-separator"));
        out.add(new DrawCommand("line", 0, 40, 36, 40, "left-bottom"));
        out.add(new DrawCommand("line", w - 36, 40, w, 40, "right-bottom"));
        out.add(new DrawCommand("text", w - 30, 5, 0, 0, "x"));
        out.add(new DrawCommand("text", 10, 5, 0, 0, "_"));
        return Collections.unmodifiableList(out);
    }

    public static String mouseLeaveDecision(boolean showBorders, LegacyDesktopNote.Rect window, int mouseX, int mouseY, boolean editorExpanded) {
        if (!showBorders) return "keep";
        if (!editorExpanded) return "keep";
        return LegacyDesktopNote.pointOutside(window, mouseX, mouseY) ? "collapse-border" : "keep";
    }
}
