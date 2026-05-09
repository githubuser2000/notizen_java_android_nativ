package de.notizen.android.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Portable startup/autostart helpers ported from ApplicationEvents.vb, xml_kram.vb and PyQt startup.py. */
public final class LegacyStartup {
    public static final String AUTOSTART_SCRIPT_NAME = "Notizen PyQt.cmd";

    private LegacyStartup() {}

    public interface ExistsPredicate { boolean exists(String path); }

    public static final class StartupOptions {
        public final String file;
        public final boolean minimized;
        public final boolean helpRequested;
        public final List<String> cleanedArgs;

        public StartupOptions(String file, boolean minimized, boolean helpRequested, List<String> cleanedArgs) {
            this.file = file;
            this.minimized = minimized;
            this.helpRequested = helpRequested;
            this.cleanedArgs = Collections.unmodifiableList(new ArrayList<>(cleanedArgs == null ? Collections.<String>emptyList() : cleanedArgs));
        }

        public StartupOptions withFile(String nextFile) {
            return new StartupOptions(nextFile, minimized, helpRequested, cleanedArgs);
        }
    }

    public static final class StartupTargetValidation {
        public final StartupOptions options;
        public final String missingFile;

        public StartupTargetValidation(StartupOptions options, String missingFile) {
            this.options = options;
            this.missingFile = missingFile;
        }
    }

    public static final class AutostartResult {
        public final boolean changed;
        public final File path;
        public final String command;
        public final String message;

        public AutostartResult(boolean changed, File path, String command, String message) {
            this.changed = changed;
            this.path = path;
            this.command = command == null ? "" : command;
            this.message = message == null ? "" : message;
        }
    }

    public static boolean looksLikeNotizenFile(String token) {
        String lower = token == null ? "" : token.toLowerCase(Locale.ROOT);
        return lower.endsWith(".alx") || lower.startsWith("ftp://");
    }

    public static StartupOptions parseLegacyStartupArgs(String... argv) {
        String file = null;
        boolean minimized = false;
        boolean help = false;
        ArrayList<String> cleaned = new ArrayList<>();
        boolean skip = false;
        if (argv != null) {
            for (String raw : argv) {
                String token = raw == null ? "" : raw;
                if (skip) {
                    cleaned.add(token);
                    skip = false;
                    continue;
                }
                String lower = token.toLowerCase(Locale.ROOT);
                if ("/min".equals(lower) || "-min".equals(lower) || "min".equals(lower)) {
                    minimized = true;
                    continue;
                }
                if ("/h".equals(lower) || "-h".equals(lower) || "h".equals(lower)
                        || "/?".equals(lower) || "-?".equals(lower) || "?".equals(lower)) {
                    help = true;
                    continue;
                }
                if (looksLikeNotizenFile(token)) {
                    file = token;
                    continue;
                }
                cleaned.add(token);
                if ("--password".equals(token)) skip = true;
            }
        }
        return new StartupOptions(file, minimized, help, cleaned);
    }

    public static StartupTargetValidation validateLegacyStartupTarget(StartupOptions options, ExistsPredicate exists) {
        StartupOptions opt = options == null ? new StartupOptions(null, false, false, Collections.<String>emptyList()) : options;
        if (opt.file == null || opt.file.isEmpty()) return new StartupTargetValidation(opt, null);
        if (opt.file.toLowerCase(Locale.ROOT).startsWith("ftp://")) return new StartupTargetValidation(opt, null);
        ExistsPredicate pred = exists == null ? path -> new File(path).exists() : exists;
        if (pred.exists(opt.file)) return new StartupTargetValidation(opt, null);
        return new StartupTargetValidation(opt.withFile(null), opt.file);
    }

    public static String legacyAutostartTargetFile(List<String> recentFiles) {
        if (recentFiles == null) return "";
        for (int i = recentFiles.size() - 1; i >= 0; i--) {
            String c = recentFiles.get(i);
            if (c == null) continue;
            String t = c.trim();
            if (t.isEmpty()) continue;
            String lower = t.toLowerCase(Locale.ROOT);
            if (t.indexOf('\\') >= 0 || t.indexOf('/') >= 0 || lower.startsWith("ftp://")) return t;
        }
        return "";
    }

    public static List<String> legacyAutostartArguments(boolean enabled, boolean minimized, List<String> recentFiles) {
        if (!enabled) return Collections.emptyList();
        ArrayList<String> args = new ArrayList<>();
        String target = legacyAutostartTargetFile(recentFiles);
        if (minimized) args.add("-min");
        if (!target.isEmpty()) args.add(target);
        return Collections.unmodifiableList(args);
    }

    public static String buildAutostartCommand(String executable, String module, List<String> arguments) {
        ArrayList<String> parts = new ArrayList<>();
        parts.add(executable == null || executable.isEmpty() ? "python" : executable);
        parts.add("-m");
        parts.add(module == null || module.isEmpty() ? "notizen_py_qt" : module);
        if (arguments != null) parts.addAll(arguments);
        return list2cmdline(parts);
    }

    public static File windowsStartupFolder(String appData) {
        String base = appData == null || appData.trim().isEmpty() ? System.getenv("APPDATA") : appData;
        if (base == null || base.trim().isEmpty()) {
            String home = System.getProperty("user.home", "");
            return new File(new File(home, "AppData/Roaming"), "Microsoft/Windows/Start Menu/Programs/Startup");
        }
        return new File(base, "Microsoft/Windows/Start Menu/Programs/Startup");
    }

    public static File autostartScriptPath(File startupDir) {
        return new File(startupDir == null ? windowsStartupFolder(null) : startupDir, AUTOSTART_SCRIPT_NAME);
    }

    public static AutostartResult applyWindowsAutostartScript(boolean enabled, boolean minimized, List<String> recentFiles,
                                                              String pythonExecutable, File startupDir) throws IOException {
        File path = autostartScriptPath(startupDir);
        boolean isWindows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        if (!isWindows && startupDir == null) {
            return new AutostartResult(false, null, "", "Autostart wird auf diesem System nicht automatisch eingerichtet.");
        }
        if (!enabled) {
            if (path.exists()) {
                if (!path.delete()) throw new IOException("Autostart konnte nicht entfernt werden: " + path);
                return new AutostartResult(true, path, "", "Autostart entfernt.");
            }
            return new AutostartResult(false, path, "", "Autostart war nicht eingerichtet.");
        }
        List<String> args = legacyAutostartArguments(true, minimized, recentFiles);
        String command = buildAutostartCommand(pythonExecutable, "notizen_py_qt", args);
        String script = "@echo off\r\nstart \"\" " + command + "\r\n";
        if (path.getParentFile() != null && !path.getParentFile().isDirectory() && !path.getParentFile().mkdirs()) {
            throw new IOException("Autostart-Ordner konnte nicht erzeugt werden: " + path.getParentFile());
        }
        boolean changed = !path.exists() || !script.equals(readUtf8(path));
        if (changed) writeUtf8(path, script);
        return new AutostartResult(changed, path, command, changed ? "Autostart eingerichtet." : "Autostart war bereits aktuell.");
    }

    private static String readUtf8(File file) throws IOException {
        final int maxBytes = 1024 * 1024;
        if (file != null && file.length() > maxBytes) throw new IOException("Autostart-Datei ist zu groß.");
        FileInputStream in = new FileInputStream(file);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream((int) Math.max(128, Math.min(maxBytes, file.length())));
            byte[] buf = new byte[4096];
            int n;
            int total = 0;
            while ((n = in.read(buf)) != -1) {
                if (total > maxBytes - n) throw new IOException("Autostart-Datei ist zu groß.");
                out.write(buf, 0, n);
                total += n;
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }

    private static void writeUtf8(File file, String text) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
        } finally {
            out.close();
        }
    }

    public static String list2cmdline(List<String> parts) {
        StringBuilder out = new StringBuilder();
        if (parts != null) {
            for (String p : parts) {
                if (out.length() > 0) out.append(' ');
                out.append(quoteWindowsArgument(p == null ? "" : p));
            }
        }
        return out.toString();
    }

    public static String quoteWindowsArgument(String argument) {
        String arg = argument == null ? "" : argument;
        if (!arg.isEmpty() && arg.indexOf(' ') < 0 && arg.indexOf('\t') < 0 && arg.indexOf('"') < 0) return arg;
        StringBuilder out = new StringBuilder("\"");
        int bs = 0;
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);
            if (c == '\\') bs++;
            else if (c == '"') {
                appendBackslashes(out, bs * 2 + 1);
                out.append('"');
                bs = 0;
            } else {
                appendBackslashes(out, bs);
                bs = 0;
                out.append(c);
            }
        }
        appendBackslashes(out, bs * 2);
        out.append('"');
        return out.toString();
    }

    private static void appendBackslashes(StringBuilder out, int count) {
        for (int i = 0; i < count; i++) out.append('\\');
    }
}
