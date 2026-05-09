package de.notizen.android.core;

import java.util.Locale;

/** Packaging permission policy from the PyQt ZIP/launcher maintenance tests. */
public final class LegacyPackagePermissionModel {
    public static final int MODE_EXECUTABLE = 0755;
    public static final int MODE_FILE = 0644;

    private LegacyPackagePermissionModel() {}

    public static int unixZipModeForPath(String relativePath, boolean directory) {
        String p = relativePath == null ? "" : relativePath.replace('\\', '/');
        String lower = p.toLowerCase(Locale.ROOT);
        if (directory) return MODE_EXECUTABLE;
        if (lower.endsWith(".desktop")) return MODE_EXECUTABLE;
        if (lower.startsWith("scripts/") && (lower.endsWith(".sh") || lower.endsWith(".py"))) return MODE_EXECUTABLE;
        if (lower.equals("notizen-starten.sh") || lower.equals("notizen-starten-venv.sh")) return MODE_EXECUTABLE;
        if (lower.startsWith("legacy_build_metadata/")) return MODE_FILE;
        return MODE_FILE;
    }

    public static String modeString(int mode) {
        String oct = Integer.toOctalString(mode & 0777);
        while (oct.length() < 3) oct = "0" + oct;
        return oct;
    }

    public static String describe(String relativePath, boolean directory) {
        int mode = unixZipModeForPath(relativePath, directory);
        return (relativePath == null ? "" : relativePath) + " -> " + modeString(mode);
    }
}
