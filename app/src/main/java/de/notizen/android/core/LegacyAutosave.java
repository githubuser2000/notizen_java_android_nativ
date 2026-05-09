package de.notizen.android.core;

import java.io.File;
import java.util.Locale;

/** Portable decision model for the old WinForms/PyQt autosave timer. */
public final class LegacyAutosave {
    public enum TargetKind { NONE, FILE, ANDROID_URI, FTP }

    public static final class Decision {
        public final boolean shouldSave;
        public final int intervalSeconds;
        public final TargetKind targetKind;
        public final String reason;

        private Decision(boolean shouldSave, int intervalSeconds, TargetKind targetKind, String reason) {
            this.shouldSave = shouldSave;
            this.intervalSeconds = intervalSeconds;
            this.targetKind = targetKind == null ? TargetKind.NONE : targetKind;
            this.reason = reason == null ? "" : reason;
        }

        public String summary() {
            return (shouldSave ? "save" : "skip") + ":" + targetKind + ":" + intervalSeconds + ":" + reason;
        }
    }

    private LegacyAutosave() {}

    public static int normalizedIntervalSeconds(Object value) {
        return LegacySettings.normalizeAutosaveSeconds(value);
    }

    public static long normalizedDelayMillis(Object value) {
        int seconds = normalizedIntervalSeconds(value);
        return seconds <= 0 ? 0L : seconds * 1000L;
    }

    public static TargetKind androidTargetKind(boolean hasAndroidUri, boolean hasRawFile, boolean hasFtpTarget) {
        if (hasFtpTarget) return TargetKind.FTP;
        if (hasAndroidUri) return TargetKind.ANDROID_URI;
        if (hasRawFile) return TargetKind.FILE;
        return TargetKind.NONE;
    }

    public static TargetKind targetKindFromPath(String path) {
        String p = path == null ? "" : path.trim();
        if (p.isEmpty()) return TargetKind.NONE;
        String lower = p.toLowerCase(Locale.ROOT);
        if (lower.startsWith("ftp://")) return TargetKind.FTP;
        if (lower.startsWith("content://")) return TargetKind.ANDROID_URI;
        if (lower.startsWith("file://")) return TargetKind.FILE;
        return TargetKind.FILE;
    }

    public static Decision decide(boolean rootExists, boolean changed, Object autosaveSeconds, TargetKind targetKind) {
        int interval = normalizedIntervalSeconds(autosaveSeconds);
        TargetKind kind = targetKind == null ? TargetKind.NONE : targetKind;
        if (!rootExists) return new Decision(false, interval, kind, "no-root");
        if (interval <= 0) return new Decision(false, interval, kind, "disabled");
        if (!changed) return new Decision(false, interval, kind, "unchanged");
        if (kind == TargetKind.NONE) return new Decision(false, interval, kind, "no-target");
        return new Decision(true, interval, kind, "changed-with-target");
    }

    public static Decision decideForLegacyFile(boolean rootExists, boolean fileAssociated, boolean fileStillExists, boolean changed, Object autosaveSeconds) {
        int interval = normalizedIntervalSeconds(autosaveSeconds);
        TargetKind kind = fileAssociated ? TargetKind.FILE : TargetKind.NONE;
        if (!rootExists) return new Decision(false, interval, kind, "no-root");
        if (interval <= 0) return new Decision(false, interval, kind, "disabled");
        if (!changed) return new Decision(false, interval, kind, "unchanged");
        if (!fileAssociated) return new Decision(false, interval, kind, "no-target");
        if (!fileStillExists) return new Decision(false, interval, kind, "missing-file");
        return new Decision(true, interval, TargetKind.FILE, "changed-existing-file");
    }

    public static boolean fileStillExists(File file) {
        return file != null && file.isFile();
    }
}
