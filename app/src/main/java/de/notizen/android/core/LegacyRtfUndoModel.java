package de.notizen.android.core;

/** Small dependency-free model for RichTextBox/QTextEdit undo/redo stack limits. */
public final class LegacyRtfUndoModel {
    public static final int DEFAULT_LIMIT = 32;

    public static final class State {
        public final int undoCount;
        public final int redoCount;
        public final int limit;

        public State(int undoCount, int redoCount, int limit) {
            this.undoCount = Math.max(0, undoCount);
            this.redoCount = Math.max(0, redoCount);
            this.limit = Math.max(1, limit);
        }

        public boolean canUndo() { return undoCount > 0; }
        public boolean canRedo() { return redoCount > 0; }
        public String statusGerman() { return "Rückgängig: " + undoCount + ", Wiederholen: " + redoCount; }
    }

    private LegacyRtfUndoModel() {}

    public static int trimStartIndex(int size, int limit) {
        int safeLimit = Math.max(1, limit);
        return Math.max(0, size - safeLimit);
    }
}
