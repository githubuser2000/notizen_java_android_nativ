package de.notizen.android.core;

/** Small portable shadow of old CText/inhalt state. */
public final class LegacyCTextState {
    public static final class State {
        public final String text;
        public final String title;
        public final boolean mark;
        public final boolean hasDesktopNote;
        public final boolean desktopNoteNeedsReload;
        private State(String text, String title, boolean mark, boolean hasDesktopNote, boolean desktopNoteNeedsReload) {
            this.text = text == null ? "" : text;
            this.title = title == null || title.trim().isEmpty() ? "..." : title;
            this.mark = mark;
            this.hasDesktopNote = hasDesktopNote;
            this.desktopNoteNeedsReload = desktopNoteNeedsReload;
        }
        public String summary() { return "CText title=" + title + ", mark=" + mark + ", desktop=" + hasDesktopNote + ", reload=" + desktopNoteNeedsReload; }
    }

    private LegacyCTextState() {}

    public static State create() { return new State("", "...", false, false, false); }

    public static State setText(State previous, String text, boolean markChanged, boolean desktopNoteNeedsReload) {
        State p = previous == null ? create() : previous;
        return new State(text, p.title, markChanged || p.mark, p.hasDesktopNote, desktopNoteNeedsReload || p.desktopNoteNeedsReload);
    }

    public static State fromNode(NoteNode node) {
        if (node == null) return create();
        return new State(RtfUtils.rtfToPlainText(node.rtf), node.title, false, node.desktopNote != null, false);
    }

    public static State afterSaveToNode(NoteNode node, String editorText, String title) {
        boolean desk = node != null && node.desktopNote != null;
        return new State(editorText, title, true, desk, desk);
    }
}
