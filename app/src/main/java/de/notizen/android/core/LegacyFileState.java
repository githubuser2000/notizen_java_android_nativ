package de.notizen.android.core;

import java.util.Locale;

/** Stateful Java analogue of Datei.vb without platform-specific directory creation. */
public final class LegacyFileState {
    private final String startDirectory;
    private String filename = LegacyPaths.LEGACY_DEFAULT_FILENAME;
    private String directory;
    private boolean fileExists;
    private boolean saved;

    public LegacyFileState(String startDirectory) {
        this.startDirectory = clean(startDirectory).isEmpty() ? LegacyPaths.documentsNotizenDir(null) : clean(startDirectory);
        this.directory = this.startDirectory;
    }

    public String saveName() { return filename; }
    public String directory() { return directory; }
    public boolean fileExists() { return fileExists; }
    public boolean saved() { return saved; }

    public void setSaved(boolean saved) { this.saved = saved; }

    public void setSaveName(String value) {
        String v = clean(value);
        if (v.isEmpty()) v = LegacyPaths.LEGACY_DEFAULT_FILENAME;
        String[] split = LegacyPaths.splitLegacyFileLocation(v, directory);
        this.directory = split[0];
        this.filename = split[1].isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : split[1];
        this.fileExists = true;
    }

    public void setDirectory(String value) {
        String v = clean(value);
        this.directory = v.length() < 2 ? startDirectory : v;
    }

    public String fullPath() { return LegacyPaths.join(directory, filename); }

    public String displayNameEnsuringAlx() {
        String n = filename == null || filename.isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : filename;
        return n.toLowerCase(Locale.ROOT).endsWith(".alx") ? n : n + ".alx";
    }

    public static String normalizeLegacySaveName(String value, String defaultDirectory) {
        String[] split = LegacyPaths.splitLegacyFileLocation(value, defaultDirectory);
        return LegacyPaths.join(split[0], split[1]);
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
