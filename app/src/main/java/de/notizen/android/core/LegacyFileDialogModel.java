package de.notizen.android.core;

/** Old OpenFileDialog/SaveFileDialog metadata represented without Android dependencies. */
public final class LegacyFileDialogModel {
    private LegacyFileDialogModel() {}

    public enum Kind {
        OPEN_ALX,
        SAVE_ALX,
        EXPORT_TXT_ANSI,
        EXPORT_TXT_UTF8,
        EXPORT_TXT_UNICODE,
        EXPORT_RTF,
        EXPORT_HTML,
        IMPORT_CONFIG,
        EXPORT_CONFIG,
        INSERT_IMAGE,
        IMPORT_HTML,
        IMPORT_TEXT,
        IMPORT_RTF
    }

    public static final class Spec {
        public final Kind kind;
        public final String title;
        public final String legacyFilter;
        public final String defaultExtension;
        public final String defaultFileName;
        public final String directory;
        public final String androidMimeType;
        public final String[] androidMimeTypes;
        public final boolean open;
        public final boolean create;

        public Spec(Kind kind, String title, String legacyFilter, String defaultExtension, String defaultFileName, String directory,
                    String androidMimeType, String[] androidMimeTypes, boolean open, boolean create) {
            this.kind = kind == null ? Kind.OPEN_ALX : kind;
            this.title = title == null ? "" : title;
            this.legacyFilter = legacyFilter == null ? "" : legacyFilter;
            this.defaultExtension = normalizeExt(defaultExtension);
            this.defaultFileName = ensureExtension(defaultFileName == null ? "" : defaultFileName, this.defaultExtension);
            this.directory = directory == null ? "" : directory;
            this.androidMimeType = (androidMimeType == null || androidMimeType.trim().isEmpty()) ? "*/*" : androidMimeType.trim();
            this.androidMimeTypes = androidMimeTypes == null ? new String[0] : androidMimeTypes.clone();
            this.open = open;
            this.create = create;
        }
    }

    public static Spec openAlx(String directory, String currentName) {
        return new Spec(Kind.OPEN_ALX, "Notizen öffnen", "alx Files|*.alx|xml Files|*.xml|All Files|*.*", ".alx", "", directory,
                "*/*", new String[]{"application/octet-stream", "text/xml", "application/xml", "text/*"}, true, false);
    }

    public static Spec saveAlx(String directory, String currentName) {
        return new Spec(Kind.SAVE_ALX, "Notizen speichern", "alx Files|*.alx", ".alx", currentName == null || currentName.isEmpty() ? "unbenannt.alx" : currentName, directory,
                "application/octet-stream", new String[]{"application/octet-stream", "text/xml", "application/xml"}, false, true);
    }

    public static Spec exportTxtAnsi(String directory) {
        return new Spec(Kind.EXPORT_TXT_ANSI, "ANSI-TXT exportieren", "ANSI txt Files|*.txt", ".txt", "export.txt", directory,
                "text/plain", new String[]{"text/plain"}, false, true);
    }

    public static Spec exportTxtUtf8(String directory) {
        return new Spec(Kind.EXPORT_TXT_UTF8, "UTF-8-TXT exportieren", "UTF-8 txt Files|*.txt", ".txt", "export.txt", directory,
                "text/plain", new String[]{"text/plain"}, false, true);
    }

    public static Spec exportTxtUnicode(String directory) {
        return new Spec(Kind.EXPORT_TXT_UNICODE, "Unicode-TXT exportieren", "Unicode txt Files|*.txt", ".txt", "export.txt", directory,
                "text/plain", new String[]{"text/plain"}, false, true);
    }

    public static Spec exportRtf(String directory) {
        return new Spec(Kind.EXPORT_RTF, "RTF exportieren", "rtf Files|*.rtf", ".rtf", "export.rtf", directory,
                "application/rtf", new String[]{"text/rtf", "application/rtf"}, false, true);
    }

    public static Spec exportHtml(String directory, String sourceName) {
        return new Spec(Kind.EXPORT_HTML, "HTML exportieren", "html Files|*.html;*.htm", ".html", baseName(sourceName) + ".html", directory,
                "text/html", new String[]{"text/html"}, false, true);
    }

    public static Spec importText(String directory) {
        return new Spec(Kind.IMPORT_TEXT, "TXT importieren", "txt Files|*.txt|All Files|*.*", ".txt", "", directory,
                "text/*", new String[]{"text/plain", "text/*", "application/octet-stream"}, true, false);
    }

    public static Spec importRtf(String directory) {
        return new Spec(Kind.IMPORT_RTF, "RTF importieren", "rtf Files|*.rtf|txt Files|*.txt|All Files|*.*", ".rtf", "", directory,
                "*/*", new String[]{"text/rtf", "application/rtf", "text/plain", "application/octet-stream"}, true, false);
    }

    public static Spec importHtml(String directory) {
        return new Spec(Kind.IMPORT_HTML, "HTML importieren", "html Files|*.html;*.htm|All Files|*.*", ".html", "", directory,
                "text/*", new String[]{"text/html", "application/xhtml+xml", "text/plain"}, true, false);
    }

    public static Spec importConfig(String directory) {
        return new Spec(Kind.IMPORT_CONFIG, "Config importieren", "config Files|notizen.config.xml|xml Files|*.xml|All Files|*.*", ".xml", "", directory,
                "*/*", new String[]{"text/xml", "application/xml", "text/*", "application/octet-stream"}, true, false);
    }

    public static Spec exportConfig(String directory) {
        return new Spec(Kind.EXPORT_CONFIG, "Config exportieren", "xml Files|*.xml", ".xml", "notizen.config.xml", directory,
                "text/xml", new String[]{"text/xml", "application/xml"}, false, true);
    }

    public static Spec insertImage(String directory) {
        return new Spec(Kind.INSERT_IMAGE, "Bild einfügen", "Image Files|*.png;*.jpg;*.jpeg;*.bmp", "", "", directory,
                "image/*", new String[]{"image/png", "image/jpeg", "image/bmp", "image/*"}, true, false);
    }

    public static String ensureExtension(String name, String ext) {
        String file = name == null ? "" : name.trim();
        String e = normalizeExt(ext);
        if (file.isEmpty() || e.isEmpty()) return file;
        if (file.toLowerCase(java.util.Locale.ROOT).endsWith(e.toLowerCase(java.util.Locale.ROOT))) return file;
        return file + e;
    }

    public static String normalizeExt(String ext) {
        String e = ext == null ? "" : ext.trim();
        if (e.isEmpty()) return "";
        return e.startsWith(".") ? e : "." + e;
    }

    private static String baseName(String sourceName) {
        String s = sourceName == null || sourceName.trim().isEmpty() ? "export" : sourceName.trim();
        int slash = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'));
        if (slash >= 0) s = s.substring(slash + 1);
        int dot = s.lastIndexOf('.');
        if (dot > 0) s = s.substring(0, dot);
        return s.isEmpty() ? "export" : s;
    }
}
