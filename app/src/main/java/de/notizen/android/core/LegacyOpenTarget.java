package de.notizen.android.core;

import java.io.File;
import java.util.Locale;

/** Normalizes old file/FTP targets and Android SAF URIs for Recent/openOnce/autosave bridges. */
public final class LegacyOpenTarget {
    public enum Kind { NONE, FILE, ANDROID_URI, FTP }

    public final Kind kind;
    public final String raw;
    public final String normalized;

    private LegacyOpenTarget(Kind kind, String raw, String normalized) {
        this.kind = kind == null ? Kind.NONE : kind;
        this.raw = raw == null ? "" : raw;
        this.normalized = normalized == null ? "" : normalized;
    }

    public static LegacyOpenTarget none() {
        return new LegacyOpenTarget(Kind.NONE, "", "");
    }

    public static LegacyOpenTarget parse(String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isEmpty()) return none();
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.startsWith("ftp://")) return new LegacyOpenTarget(Kind.FTP, raw, raw);
        if (lower.startsWith("content://")) return new LegacyOpenTarget(Kind.ANDROID_URI, raw, raw);
        if (lower.startsWith("file://")) return new LegacyOpenTarget(Kind.FILE, raw, decodeFileUriPath(raw));
        return new LegacyOpenTarget(Kind.FILE, raw, raw);
    }

    public boolean isUsable() {
        return kind != Kind.NONE && !normalized.isEmpty();
    }

    public boolean isLocalFile() { return kind == Kind.FILE; }
    public boolean isAndroidUri() { return kind == Kind.ANDROID_URI; }
    public boolean isFtp() { return kind == Kind.FTP; }

    public LegacyAutosave.TargetKind autosaveKind() {
        switch (kind) {
            case FILE: return LegacyAutosave.TargetKind.FILE;
            case ANDROID_URI: return LegacyAutosave.TargetKind.ANDROID_URI;
            case FTP: return LegacyAutosave.TargetKind.FTP;
            default: return LegacyAutosave.TargetKind.NONE;
        }
    }

    public boolean localFileExists() {
        return kind == Kind.FILE && new File(normalized).exists();
    }

    public String displayName() {
        if (normalized.isEmpty()) return "";
        if (kind == Kind.ANDROID_URI || kind == Kind.FTP) return normalized;
        int slash = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private static String decodeFileUriPath(String uri) {
        String s = uri == null ? "" : uri.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("file://")) return s;
        String rest = s.substring(7);
        // file:///C:/x -> C:/x, file://localhost/x -> /x, file://server/share -> //server/share.
        if (rest.toLowerCase(Locale.ROOT).startsWith("localhost/")) rest = rest.substring("localhost".length());
        else if (rest.startsWith("/") && rest.length() >= 4 && Character.isLetter(rest.charAt(1)) && rest.charAt(2) == ':') rest = rest.substring(1);
        else if (!rest.startsWith("/")) rest = "//" + rest;
        return percentDecode(rest);
    }

    public static String percentDecode(String value) {
        if (value == null || value.indexOf('%') < 0) return value == null ? "" : value;
        StringBuilder out = new StringBuilder(value.length());
        byte[] buf = new byte[value.length()];
        for (int i = 0; i < value.length();) {
            char ch = value.charAt(i);
            if (ch == '%' && i + 2 < value.length()) {
                int n = 0;
                while (i + 2 < value.length() && value.charAt(i) == '%') {
                    int hi = hex(value.charAt(i + 1));
                    int lo = hex(value.charAt(i + 2));
                    if (hi < 0 || lo < 0) break;
                    buf[n++] = (byte) ((hi << 4) | lo);
                    i += 3;
                }
                if (n > 0) {
                    out.append(new String(buf, 0, n, java.nio.charset.StandardCharsets.UTF_8));
                    continue;
                }
            }
            out.append(ch);
            i++;
        }
        return out.toString();
    }

    private static int hex(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return 10 + c - 'a';
        if (c >= 'A' && c <= 'F') return 10 + c - 'A';
        return -1;
    }

    @Override public String toString() {
        return kind + ":" + normalized;
    }
}
