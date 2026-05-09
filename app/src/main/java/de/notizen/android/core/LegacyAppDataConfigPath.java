package de.notizen.android.core;

/** Path rules for xml_kram's %APPDATA%/Notizen/notizen.config.xml convention. */
public final class LegacyAppDataConfigPath {
    public static final String CONFIG_FILE = "notizen.config.xml";
    public static final String APP_DIR = "Notizen";
    public static final class Paths {
        public final String appDataRoot;
        public final String configDirectory;
        public final String configFile;
        public final String documentsDirectory;
        public final String defaultAlxFile;
        public Paths(String appDataRoot, String configDirectory, String configFile, String documentsDirectory, String defaultAlxFile) {
            this.appDataRoot = appDataRoot; this.configDirectory = configDirectory; this.configFile = configFile;
            this.documentsDirectory = documentsDirectory; this.defaultAlxFile = defaultAlxFile;
        }
    }
    private LegacyAppDataConfigPath() {}
    public static Paths build(String appDataRoot, String documentsRoot) {
        String appRoot = trimSlash(appDataRoot == null || appDataRoot.trim().isEmpty() ? "%APPDATA%" : appDataRoot.trim());
        String docs = trimSlash(documentsRoot == null || documentsRoot.trim().isEmpty() ? "Documents" : documentsRoot.trim());
        String configDir = ensureSingleNotizen(appRoot);
        String docDir = ensureSingleNotizen(docs);
        return new Paths(appRoot, configDir, join(configDir, CONFIG_FILE), docDir, join(docDir, LegacyPaths.LEGACY_DEFAULT_FILENAME));
    }
    public static String ensureSingleNotizen(String base) {
        String b = trimSlash(base == null ? "" : base.trim());
        String lower = b.toLowerCase(java.util.Locale.ROOT).replace('\\', '/');
        if (lower.endsWith("/notizen")) return b;
        return join(b, APP_DIR);
    }
    private static String join(String a, String b) {
        if (a == null || a.isEmpty()) return b;
        char sep = a.indexOf('\\') >= 0 ? '\\' : '/';
        return trimSlash(a) + sep + b;
    }
    private static String trimSlash(String s) {
        if (s == null) return "";
        while (s.endsWith("/") || s.endsWith("\\")) s = s.substring(0, s.length() - 1);
        return s;
    }
}
