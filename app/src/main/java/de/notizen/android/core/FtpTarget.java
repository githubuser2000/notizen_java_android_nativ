package de.notizen.android.core;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FtpTarget {
    public final String host;
    public final int port;
    public final String remotePath;
    public final String username;
    public final String password;
    public final int timeoutMillis;
    public final boolean passive;

    public FtpTarget(String host, int port, String remotePath, String username, String password, int timeoutMillis, boolean passive) {
        this.host = host == null ? "" : host;
        this.port = port <= 0 ? 21 : port;
        this.remotePath = remotePath == null ? "" : remotePath;
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.timeoutMillis = timeoutMillis <= 0 ? 30000 : timeoutMillis;
        this.passive = passive;
    }

    public static FtpTarget fromFields(String host, String remotePath, String username, String password) throws FtpSyncError {
        return fromFields(host, remotePath, username, password, 30000, true);
    }

    public static FtpTarget fromFields(String host, String remotePath, String username, String password, int timeoutMillis, boolean passive) throws FtpSyncError {
        String rawHost = trim(host);
        String rawPath = trim(remotePath);
        String user = trim(username);
        String pass = password == null ? "" : percentDecode(password);
        int port = 21;
        String normalizedHost = rawHost;

        if (rawHost.isEmpty()) throw new FtpSyncError("FTP-Host fehlt.");
        try {
            URI uri = new URI(rawHost.contains("://") ? rawHost : "ftp://" + rawHost);
            if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                normalizedHost = uri.getHost();
                if (uri.getPort() > 0) port = uri.getPort();
                if ((user == null || user.isEmpty()) && uri.getRawUserInfo() != null) {
                    String[] parts = uri.getRawUserInfo().split(":", 2);
                    user = percentDecode(parts[0]);
                    if (parts.length > 1 && pass.isEmpty()) pass = percentDecode(parts[1]);
                }
                if (rawPath.isEmpty() && uri.getRawPath() != null) rawPath = percentDecode(uri.getRawPath());
            }
        } catch (Exception ignored) {
            // Accept plain legacy host strings that java.net.URI cannot parse.
        }

        normalizedHost = trim(normalizedHost).replaceFirst("^ftp://", "").replaceAll("/+$", "");
        if (normalizedHost.isEmpty()) throw new FtpSyncError("FTP-Host fehlt.");
        rawPath = percentDecode(rawPath);
        if (rawPath.isEmpty()) throw new FtpSyncError("FTP-Pfad fehlt.");
        if (!rawPath.startsWith("/")) rawPath = "/" + rawPath;
        if (!rawPath.toLowerCase(Locale.ROOT).endsWith(".alx")) throw new FtpSyncError("FTP-Pfad muss auf eine .alx-Datei zeigen.");
        return new FtpTarget(normalizedHost, port, rawPath, user == null ? "" : percentDecode(user), pass, timeoutMillis, passive);
    }

    public String displayUrl() {
        String portText = port == 21 ? "" : ":" + port;
        String user = username.isEmpty() ? "" : percentEncode(username) + "@";
        return "ftp://" + user + host + portText + percentEncodePath(remotePath);
    }

    public String safeDisplayUrl() { return displayUrl(); }

    public String filename() {
        int pos = remotePath.lastIndexOf('/');
        return pos >= 0 ? remotePath.substring(pos + 1) : remotePath;
    }

    public String directory() {
        int pos = remotePath.lastIndexOf('/');
        if (pos <= 0) return "/";
        return remotePath.substring(0, pos);
    }

    public static String percentDecode(String value) {
        if (value == null) return "";
        String s = value;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(s.length());
        StringBuilder result = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%' && i + 2 < s.length()) {
                int hi = Character.digit(s.charAt(i + 1), 16);
                int lo = Character.digit(s.charAt(i + 2), 16);
                if (hi >= 0 && lo >= 0) {
                    out.write((hi << 4) | lo);
                    i += 2;
                    continue;
                }
            }
            flushUtf8(out, result);
            result.append(c);
        }
        flushUtf8(out, result);
        return result.toString();
    }

    private static void flushUtf8(java.io.ByteArrayOutputStream out, StringBuilder result) {
        if (out.size() == 0) return;
        result.append(new String(out.toByteArray(), StandardCharsets.UTF_8));
        out.reset();
    }

    private static String percentEncodePath(String path) {
        String p = path == null ? "" : path;
        StringBuilder out = new StringBuilder(p.length() + 16);
        for (int offset = 0; offset < p.length(); ) {
            int cp = p.codePointAt(offset);
            offset += Character.charCount(cp);
            if (cp == '/') out.append('/');
            else out.append(percentEncode(new String(Character.toChars(cp))));
        }
        return out.toString();
    }

    private static String percentEncode(String value) {
        try {
            return URLEncoder.encode(value == null ? "" : value, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return value == null ? "" : value;
        }
    }

    private static String trim(String value) { return value == null ? "" : value.trim(); }
}
