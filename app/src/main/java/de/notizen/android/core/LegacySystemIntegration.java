package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** File-association and launcher command helpers ported from the PyQt system integration module. */
public final class LegacySystemIntegration {
    public static final String LEGACY_ALX_EXTENSION = ".alx";
    public static final String LEGACY_ALX_PROG_ID = "notizenfile";
    public static final String LEGACY_ALX_EXTENSION_DEFAULT = "Notizenfile";
    public static final String LEGACY_OPEN_WITH_EXE = "Notizen.exe";
    public static final String WINDOWS_CLASSES_ROOT_USER = "Software\\Classes";

    private LegacySystemIntegration() {}

    public static final class WindowsRegistryEntry {
        public final String key;
        public final String name;
        public final String value;
        public final boolean writeIfMissing;

        public WindowsRegistryEntry(String key, String name, String value, boolean writeIfMissing) {
            this.key = key == null ? "" : key;
            this.name = name == null ? "" : name;
            this.value = value == null ? "" : value;
            this.writeIfMissing = writeIfMissing;
        }

        public String displayName() { return name.isEmpty() ? "(Default)" : name; }
    }

    public static String quoteWindowsArgument(String value) {
        String v = value == null ? "" : value;
        if (v.isEmpty()) return "\"\"";
        boolean needsQuote = false;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (Character.isWhitespace(c) || c == '"') { needsQuote = true; break; }
        }
        if (!needsQuote) return v;
        StringBuilder out = new StringBuilder(v.length() + 8).append('"');
        int backslashes = 0;
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            if (c == '\\') { backslashes++; continue; }
            if (c == '"') {
                for (int j = 0; j < backslashes * 2 + 1; j++) out.append('\\');
                out.append('"');
                backslashes = 0;
                continue;
            }
            for (int j = 0; j < backslashes; j++) out.append('\\');
            backslashes = 0;
            out.append(c);
        }
        for (int j = 0; j < backslashes * 2; j++) out.append('\\');
        out.append('"');
        return out.toString();
    }

    public static String buildWindowsModuleOpenCommand(String pythonExecutable, String module, boolean visible, boolean noTray, boolean resetWindow) {
        ArrayList<String> args = new ArrayList<>();
        args.add(pythonExecutable == null || pythonExecutable.isEmpty() ? "python" : pythonExecutable);
        args.add("-m");
        args.add(module == null || module.isEmpty() ? "notizen_py_qt" : module);
        if (visible) args.add("--show");
        if (noTray) args.add("--no-tray");
        if (resetWindow) args.add("--reset-window");
        return joinWindowsCommand(args) + " \"%1\"";
    }

    public static String buildWindowsScriptOpenCommand(String launcher, boolean visible, boolean noTray, boolean resetWindow) {
        ArrayList<String> args = new ArrayList<>();
        args.add(launcher == null || launcher.isEmpty() ? "Notizen.exe" : launcher);
        if (visible) args.add("--show");
        if (noTray) args.add("--no-tray");
        if (resetWindow) args.add("--reset-window");
        return joinWindowsCommand(args) + " \"%1\"";
    }

    public static List<WindowsRegistryEntry> legacyWindowsAlxRegistryEntries(String openCommand, String iconPath, String classesRoot, String progId) {
        String root = trimTrailingBackslash(classesRoot == null || classesRoot.isEmpty() ? WINDOWS_CLASSES_ROOT_USER : classesRoot);
        String id = progId == null || progId.isEmpty() ? LEGACY_ALX_PROG_ID : progId;
        String extensionKey = root + "\\" + LEGACY_ALX_EXTENSION;
        String progKey = root + "\\" + id;
        ArrayList<WindowsRegistryEntry> out = new ArrayList<>();
        out.add(new WindowsRegistryEntry(extensionKey, "", LEGACY_ALX_EXTENSION_DEFAULT, true));
        out.add(new WindowsRegistryEntry(extensionKey + "\\OpenWithList", "", "", true));
        out.add(new WindowsRegistryEntry(extensionKey + "\\OpenWithList\\" + LEGACY_OPEN_WITH_EXE, "", "", true));
        out.add(new WindowsRegistryEntry(extensionKey + "\\OpenWithProgIds", "", "", true));
        out.add(new WindowsRegistryEntry(extensionKey + "\\OpenWithProgIds", id, "", true));
        out.add(new WindowsRegistryEntry(progKey, "", "", true));
        out.add(new WindowsRegistryEntry(progKey + "\\Shell", "", "Open", true));
        out.add(new WindowsRegistryEntry(progKey + "\\Shell\\Open", "", "", true));
        out.add(new WindowsRegistryEntry(progKey + "\\Shell\\Open\\Command", "", openCommand == null ? "" : openCommand, false));
        out.add(new WindowsRegistryEntry(progKey + "\\DefaultIcon", "", iconPath == null ? "" : iconPath, false));
        return Collections.unmodifiableList(out);
    }

    public static List<String> windowsAssociationPreviewLines(List<WindowsRegistryEntry> entries) {
        ArrayList<String> out = new ArrayList<>();
        if (entries == null) return out;
        for (WindowsRegistryEntry entry : entries) {
            out.add((entry.writeIfMissing ? "if-missing " : "set ") + entry.key + " [" + entry.displayName() + "] = " + entry.value);
        }
        return out;
    }

    public static String buildLinuxDesktopExec(String module, String pythonExecutable, boolean resetWindowEnv, String resourceNameEnv, boolean visible, boolean noTray, boolean resetWindow, String filePlaceholder) {
        ArrayList<String> args = new ArrayList<>();
        args.add("env");
        if (resetWindowEnv) args.add("NOTIZEN_RESET_WINDOW=1");
        if (resourceNameEnv != null && !resourceNameEnv.isEmpty()) args.add("RESOURCE_NAME=" + resourceNameEnv);
        args.add(pythonExecutable == null || pythonExecutable.isEmpty() ? "python3" : pythonExecutable);
        args.add("-m");
        args.add(module == null || module.isEmpty() ? "notizen_py_qt" : module);
        if (visible) args.add("--show");
        if (noTray) args.add("--no-tray");
        if (resetWindow) args.add("--reset-window");
        args.add(filePlaceholder == null || filePlaceholder.isEmpty() ? "%f" : filePlaceholder);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) out.append(' ');
            String part = args.get(i);
            out.append(part.indexOf(' ') >= 0 ? quoteWindowsArgument(part) : part);
        }
        return out.toString();
    }

    public static List<String> androidAlxMimeTypes() {
        ArrayList<String> out = new ArrayList<>();
        out.add("application/x-notizen-alx");
        out.add("application/octet-stream");
        out.add("text/xml");
        out.add("application/gzip");
        return out;
    }

    private static String joinWindowsCommand(List<String> args) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) out.append(' ');
            out.append(quoteWindowsArgument(args.get(i)));
        }
        return out.toString();
    }

    private static String trimTrailingBackslash(String s) {
        String value = s == null ? "" : s;
        while (value.endsWith("\\")) value = value.substring(0, value.length() - 1);
        return value;
    }
}
