package de.notizen.android.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small Java model of xml_kram.vb's direct config accessors and on_load/on_exit
 * window/config behavior. It lets Android inspect and round-trip the old
 * notizen.config.xml semantics without depending on WinForms.
 */
public final class LegacyConfigSnapshot {
    public static final int WINDOW_EDGE_GUARD = 50;

    public static final class Point {
        public final int x;
        public final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Point)) return false;
            Point p = (Point) other;
            return x == p.x && y == p.y;
        }

        @Override public int hashCode() { return 31 * x + y; }
        @Override public String toString() { return x + "," + y; }
    }

    public static final class WindowData {
        public final Point location;
        public final Point size;
        public final String windowState;
        public final boolean showInTaskbar;
        public final boolean minimizedTaskbarPulse;
        public final int norm0OrMax1;
        public final boolean locationAccepted;

        public WindowData(Point location, Point size, String windowState, boolean showInTaskbar,
                          boolean minimizedTaskbarPulse, int norm0OrMax1, boolean locationAccepted) {
            this.location = location == null ? new Point(0, 0) : location;
            this.size = size == null ? new Point(1000, 700) : size;
            this.windowState = LegacySettings.normalizeWindowState(windowState);
            this.showInTaskbar = showInTaskbar;
            this.minimizedTaskbarPulse = minimizedTaskbarPulse;
            this.norm0OrMax1 = norm0OrMax1;
            this.locationAccepted = locationAccepted;
        }

        public String summary() {
            return "Fenster: " + windowState + " " + size.x + "x" + size.y + " @ " + location.x + "," + location.y
                    + (locationAccepted ? "" : " (Position verworfen)")
                    + (minimizedTaskbarPulse ? " · Minimized-Taskbar-Pulse" : "");
        }
    }

    public static final class Snapshot {
        public final String directory;
        public final String file;
        public final String language;
        public final boolean showInDeskbar;
        public final boolean showDesknoteBorders;
        public final int scrollbarsChoice;
        public final WindowData window;
        public final Map<String, Point> toolstrips;

        public Snapshot(String directory, String file, String language, boolean showInDeskbar,
                        boolean showDesknoteBorders, int scrollbarsChoice, WindowData window,
                        Map<String, Point> toolstrips) {
            this.directory = directory == null ? "" : directory;
            this.file = file == null || file.isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : file;
            this.language = language == null || language.isEmpty() ? "Auto" : language;
            this.showInDeskbar = showInDeskbar;
            this.showDesknoteBorders = showDesknoteBorders;
            this.scrollbarsChoice = LegacyScrollbars.normalizeChoice(scrollbarsChoice);
            this.window = window;
            this.toolstrips = new LinkedHashMap<>(toolstrips == null ? new LinkedHashMap<String, Point>() : toolstrips);
        }

        public String summary() {
            StringBuilder b = new StringBuilder();
            b.append("Datei: ").append(directory.isEmpty() ? "" : directory + "/").append(file).append('\n');
            b.append("Sprache: ").append(language).append('\n');
            b.append("Scrollleisten: ").append(LegacyScrollbars.label(scrollbarsChoice)).append('\n');
            b.append("Taskbar minimiert: ").append(showInDeskbar ? "ja" : "nein").append('\n');
            b.append("Haftnotiz-Ränder: ").append(showDesknoteBorders ? "ja" : "nein").append('\n');
            b.append(window == null ? "Fenster: -" : window.summary()).append('\n');
            b.append("Toolstrips: ");
            boolean first = true;
            for (Map.Entry<String, Point> e : toolstrips.entrySet()) {
                if (!first) b.append(" · ");
                first = false;
                b.append(e.getKey()).append('=').append(e.getValue());
            }
            return b.toString();
        }
    }

    private LegacyConfigSnapshot() {}

    public static Snapshot fromSettings(LegacySettings settings, int workAreaWidth, int workAreaHeight) {
        LegacySettings s = settings == null ? new LegacySettings() : settings;
        LinkedHashMap<String, Point> points = new LinkedHashMap<>();
        points.put("haupt", toolstripPoint(s, 1));
        points.put("font", toolstripPoint(s, 2));
        points.put("cutpastecopy", toolstripPoint(s, 3));
        points.put("elements", toolstripPoint(s, 4));
        return new Snapshot(
                getDir(s), getFile(s), s.language, s.showInTaskbarWhenMinimized, s.showDesknoteBorders,
                s.scrollbarsChoice, onLoadWindowData(s, workAreaWidth, workAreaHeight), points);
    }

    public static String getDir(LegacySettings settings) {
        return settings == null ? "" : (settings.lastDirectory == null ? "" : settings.lastDirectory);
    }

    public static String getFile(LegacySettings settings) {
        String file = settings == null ? "" : settings.lastFile;
        return file == null || file.isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : file;
    }

    public static Point toolstripPoint(LegacySettings settings, int legacyIndex) {
        String key;
        switch (legacyIndex) {
            case 1: key = "haupt"; break;
            case 2: key = "font"; break;
            case 3: key = "cutpastecopy"; break;
            case 4: key = "elements"; break;
            default: key = "haupt"; break;
        }
        int[] raw = settings == null ? null : settings.toolstripPositions.get(key);
        int x = raw == null || raw.length < 1 ? 0 : raw[0];
        int y = raw == null || raw.length < 2 ? 0 : raw[1];
        if (legacyIndex == 3 && x < 0) x = 0;
        return new Point(Math.max(0, x), Math.max(0, y));
    }

    public static WindowData onLoadWindowData(LegacySettings settings, int workAreaWidth, int workAreaHeight) {
        LegacySettings s = settings == null ? new LegacySettings() : settings;
        int ww = Math.max(WINDOW_EDGE_GUARD + 1, workAreaWidth);
        int wh = Math.max(WINDOW_EDGE_GUARD + 1, workAreaHeight);
        boolean locationAccepted = s.windowX < ww - WINDOW_EDGE_GUARD && s.windowY < wh - WINDOW_EDGE_GUARD;
        Point location = locationAccepted ? new Point(Math.max(0, s.windowX), Math.max(0, s.windowY)) : new Point(60, 60);
        Point size = new Point(Math.max(80, s.windowWidth), Math.max(60, s.windowHeight));
        String state = LegacySettings.normalizeWindowState(s.windowState);
        if ("Maximized".equals(state)) return new WindowData(location, size, state, s.showInTaskbarWhenMinimized, false, 1, locationAccepted);
        if ("Minimized".equals(state)) return new WindowData(location, size, state, false, true, 0, locationAccepted);
        return new WindowData(location, size, "Normal", s.showInTaskbarWhenMinimized, false, 0, locationAccepted);
    }

    public static LegacySettings applyOnExit(LegacySettings original, WindowData window, String directory, String file,
                                             Map<String, Point> toolstrips, int scrollChoice,
                                             boolean showDesknoteBorders) {
        LegacySettings s = original == null ? new LegacySettings() : original.copy();
        WindowData w = window == null ? onLoadWindowData(s, 1920, 1080) : window;
        if (!"Maximized".equals(w.windowState)) {
            s.windowX = w.location.x;
            s.windowY = w.location.y;
            s.windowWidth = w.size.x;
            s.windowHeight = w.size.y;
        }
        s.windowState = LegacySettings.normalizeWindowState(w.windowState);
        s.lastDirectory = directory == null ? "" : directory;
        s.lastFile = file == null || file.isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : file;
        s.scrollbarsChoice = LegacyScrollbars.normalizeChoice(scrollChoice);
        s.showDesknoteBorders = showDesknoteBorders;
        if (toolstrips != null) for (Map.Entry<String, Point> e : toolstrips.entrySet()) {
            Point p = e.getValue();
            if (p != null && s.toolstripPositions.containsKey(e.getKey())) s.toolstripPositions.put(e.getKey(), new int[]{Math.max(0, p.x), Math.max(0, p.y)});
        }
        return s;
    }

    public static LegacySettings consumeOpenOnce(LegacySettings original) {
        LegacySettings s = original == null ? new LegacySettings() : original.copy();
        s.openOnceFile = "";
        s.openOnceTimestamp = "";
        return s;
    }
}
