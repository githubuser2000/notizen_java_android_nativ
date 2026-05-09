package de.notizen.android.core;

/** Builds legacy-compatible document/window title strings without Android APIs. */
public final class LegacyDocumentTitle {
    public static final String DEFAULT_DISPLAY_NAME = LegacyPaths.LEGACY_DEFAULT_FILENAME;

    public static final class State {
        public final String appTitle;
        public final String sourceLine;
        public final String displayName;
        public final boolean changed;
        public final boolean hasSaveTarget;

        public State(String appTitle, String sourceLine, String displayName, boolean changed, boolean hasSaveTarget) {
            this.appTitle = safe(appTitle);
            this.sourceLine = safe(sourceLine);
            this.displayName = safe(displayName);
            this.changed = changed;
            this.hasSaveTarget = hasSaveTarget;
        }
    }

    private LegacyDocumentTitle() {}

    public static State build(String appName, String displayName, boolean changed, String ftpDisplay, String localFilePath, String androidUri) {
        String name = sanitizeDisplayName(displayName);
        String source = "";
        boolean hasTarget = false;
        if (!safe(ftpDisplay).isEmpty()) {
            source = safe(ftpDisplay);
            hasTarget = true;
            if (isDefaultOrEmpty(displayName)) name = filenameFromPath(source, name);
        } else if (!safe(localFilePath).isEmpty()) {
            source = safe(localFilePath);
            hasTarget = true;
            if (isDefaultOrEmpty(displayName)) name = filenameFromPath(source, name);
        } else if (!safe(androidUri).isEmpty()) {
            source = safe(androidUri);
            hasTarget = true;
        } else {
            source = "Noch nicht gespeichert";
        }
        String star = changed ? " *" : "";
        String app = safe(appName).isEmpty() ? "Notizen" : safe(appName);
        return new State(app + " - " + name + star, source + star, name, changed, hasTarget);
    }

    public static String legacyDesktopTitle(String filename, String directory) {
        String file = sanitizeDisplayName(filename);
        String dir = safe(directory);
        return dir.isEmpty() ? file : file + " in " + dir;
    }

    public static String sanitizeDisplayName(String displayName) {
        String n = safe(displayName);
        if (n.isEmpty()) return DEFAULT_DISPLAY_NAME;
        int slash = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < n.length()) n = n.substring(slash + 1);
        return n.trim().isEmpty() ? DEFAULT_DISPLAY_NAME : n.trim();
    }

    private static boolean isDefaultOrEmpty(String value) {
        String s = safe(value);
        return s.isEmpty() || DEFAULT_DISPLAY_NAME.equalsIgnoreCase(s);
    }

    private static String filenameFromPath(String value, String fallback) {
        String s = safe(value);
        int query = s.indexOf('?');
        if (query >= 0) s = s.substring(0, query);
        int slash = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < s.length()) s = s.substring(slash + 1);
        s = LegacyOpenTarget.percentDecode(s);
        return s.trim().isEmpty() ? fallback : s.trim();
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
