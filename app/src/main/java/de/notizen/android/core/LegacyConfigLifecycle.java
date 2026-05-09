package de.notizen.android.core;

import java.util.LinkedHashMap;
import java.util.Map;

/** Small xml_kram on_load/on_exit model for window/toolstrip/recent persistence. */
public final class LegacyConfigLifecycle {
    public static final class WindowData {
        public final int x, y, width, height;
        public final String windowState;
        public final boolean showInTaskbar;
        public final int norm0OrMax1;
        public WindowData(int x, int y, int width, int height, String windowState, boolean showInTaskbar, int norm0OrMax1) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.windowState = windowState; this.showInTaskbar = showInTaskbar; this.norm0OrMax1 = norm0OrMax1;
        }
    }
    public static final class ExitSnapshot {
        public final WindowData window;
        public final String directory;
        public final String file;
        public final int scrollbarsChoice;
        public final boolean showDesknoteBorders;
        public final Map<String, int[]> toolstrips;
        public ExitSnapshot(WindowData window, String directory, String file, int scrollbarsChoice, boolean showDesknoteBorders, Map<String, int[]> toolstrips) {
            this.window = window; this.directory = directory; this.file = file; this.scrollbarsChoice = scrollbarsChoice; this.showDesknoteBorders = showDesknoteBorders;
            this.toolstrips = new LinkedHashMap<>();
            if (toolstrips != null) for (Map.Entry<String, int[]> e : toolstrips.entrySet()) this.toolstrips.put(e.getKey(), new int[]{e.getValue()[0], e.getValue()[1]});
        }
    }
    private LegacyConfigLifecycle() {}
    public static WindowData onLoad(LegacySettings s, int workWidth, int workHeight) {
        if (s == null) s = new LegacySettings();
        int x = s.windowX, y = s.windowY;
        if (x >= workWidth - 50 || y >= workHeight - 50 || x < -10000 || y < -10000) { x = 60; y = 60; }
        String ws = LegacySettings.normalizeWindowState(s.windowState);
        boolean taskbar = "Minimized".equals(ws);
        int norm = "Maximized".equals(ws) ? 1 : 0;
        return new WindowData(x, y, Math.max(320, s.windowWidth), Math.max(240, s.windowHeight), ws, taskbar, norm);
    }
    public static ExitSnapshot onExit(LegacySettings s, String currentDirectory, String currentFile) {
        if (s == null) s = new LegacySettings();
        return new ExitSnapshot(onLoad(s, Integer.MAX_VALUE / 4, Integer.MAX_VALUE / 4), currentDirectory == null ? s.lastDirectory : currentDirectory,
                currentFile == null ? s.lastFile : currentFile, s.scrollbarsChoice, s.showDesknoteBorders, s.toolstripPositions);
    }
    public static void applyExitSnapshot(LegacySettings s, ExitSnapshot snap) {
        if (s == null || snap == null) return;
        s.windowX = snap.window.x; s.windowY = snap.window.y; s.windowWidth = snap.window.width; s.windowHeight = snap.window.height; s.windowState = snap.window.windowState;
        s.lastDirectory = snap.directory; s.lastFile = snap.file; s.scrollbarsChoice = snap.scrollbarsChoice; s.showDesknoteBorders = snap.showDesknoteBorders;
        s.toolstripPositions.clear();
        for (Map.Entry<String, int[]> e : snap.toolstrips.entrySet()) s.toolstripPositions.put(e.getKey(), new int[]{e.getValue()[0], e.getValue()[1]});
    }
}
