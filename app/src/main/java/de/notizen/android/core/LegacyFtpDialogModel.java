package de.notizen.android.core;

/** Portable model for ftpkram.vb. */
public final class LegacyFtpDialogModel {
    public enum Action { STORE, OPEN, UPLOAD }

    public static final class FtpDialogFields {
        public final String host;
        public final String path;
        public final String username;
        public final String password;
        public final String safeLegacyUrl;

        public FtpDialogFields(String host, String path, String username, String password) {
            this.host = cleanHost(host);
            this.path = normalizePath(path);
            this.username = username == null ? "" : FtpTarget.percentDecode(username.trim());
            this.password = password == null ? "" : FtpTarget.percentDecode(password);
            this.safeLegacyUrl = buildSafeLegacyUrl(this.host, this.path, this.username, this.password);
        }
    }

    public static final class FtpDialogDecision {
        public final boolean accepted;
        public final Action action;
        public final FtpTarget target;
        public final String host;
        public final String normalizedPath;
        public final String username;
        public final String password;
        public final String legacyUrl;
        public final String safeLegacyUrl;
        public final String message;

        public FtpDialogDecision(boolean accepted, Action action, FtpTarget target, String host,
                                 String normalizedPath, String username, String password,
                                 String message) {
            this.accepted = accepted;
            this.action = action == null ? Action.STORE : action;
            this.target = target;
            this.host = host == null ? "" : host;
            this.normalizedPath = normalizePath(normalizedPath);
            this.username = username == null ? "" : username;
            this.password = password == null ? "" : password;
            this.legacyUrl = buildLegacyUrl(this.host, this.normalizedPath, this.username, this.password);
            this.safeLegacyUrl = buildSafeLegacyUrl(this.host, this.normalizedPath, this.username, this.password);
            this.message = message == null ? "" : message;
        }
    }

    private LegacyFtpDialogModel() {}

    public static FtpDialogFields normalizeFields(String hostOrUrl, String path, String username, String password) {
        String host = hostOrUrl == null ? "" : hostOrUrl.trim();
        String p = path == null ? "" : path.trim();
        String u = username == null ? "" : username.trim();
        String pass = password == null ? "" : password;
        try {
            FtpTarget parsed = FtpTarget.fromFields(host, p, u, pass);
            String h = parsed.host + (parsed.port == 21 ? "" : ":" + parsed.port);
            return new FtpDialogFields(h, parsed.remotePath, parsed.username, parsed.password);
        } catch (Exception ignored) {
            // For "save settings only" we still want to clean values even if no .alx path exists yet.
        }
        String[] parsed = parseLooseUrl(host, p, u, pass);
        return new FtpDialogFields(parsed[0], parsed[1], parsed[2], parsed[3]);
    }

    public static FtpDialogDecision decide(Action action, String hostOrUrl, String path, String username, String password) {
        Action a = action == null ? Action.STORE : action;
        FtpDialogFields fields = normalizeFields(hostOrUrl, path, username, password);
        if (fields.host.isEmpty()) {
            return new FtpDialogDecision(false, a, null, fields.host, fields.path, fields.username, fields.password, "FTP-Host fehlt.");
        }
        if (a == Action.STORE) {
            return new FtpDialogDecision(true, a, null, fields.host, fields.path, fields.username, fields.password, "FTP-Daten gespeichert.");
        }
        try {
            FtpTarget target = FtpTarget.fromFields(fields.host, fields.path, fields.username, fields.password);
            String h = target.host + (target.port == 21 ? "" : ":" + target.port);
            return new FtpDialogDecision(true, a, target, h, target.remotePath, target.username, target.password,
                    a == Action.OPEN ? "FTP öffnen." : "FTP speichern.");
        } catch (Exception e) {
            return new FtpDialogDecision(false, a, null, fields.host, fields.path, fields.username, fields.password, e.getMessage());
        }
    }

    public static void applyToSettings(LegacySettings settings, FtpDialogDecision decision) {
        if (settings == null || decision == null || !decision.accepted) return;
        settings.ftpHost = decision.host;
        settings.ftpPath = decision.normalizedPath;
        settings.ftpUsername = decision.username;
        settings.ftpPassword = decision.password;
    }

    public static String buildLegacyUrl(String host, String path, String username, String password) {
        String h = cleanHost(host);
        String p = normalizePath(path);
        String u = username == null ? "" : username.trim();
        String pass = password == null ? "" : password;
        String auth = u.isEmpty() ? "" : u + (pass.isEmpty() ? "" : ":" + pass) + "@";
        return "ftp://" + auth + h + p;
    }

    public static String buildSafeLegacyUrl(String host, String path, String username, String password) {
        String h = cleanHost(host);
        String p = normalizePath(path);
        String u = username == null ? "" : username.trim();
        String pass = password == null ? "" : password;
        String auth = u.isEmpty() ? "" : u + (pass.isEmpty() ? "" : ":***") + "@";
        return "ftp://" + auth + h + p;
    }

    private static String[] parseLooseUrl(String hostOrUrl, String path, String username, String password) {
        String host = hostOrUrl == null ? "" : hostOrUrl.trim();
        String p = path == null ? "" : path.trim();
        String u = username == null ? "" : username.trim();
        String pass = password == null ? "" : password;
        try {
            java.net.URI uri = new java.net.URI(host.contains("://") ? host : "ftp://" + host);
            if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                host = uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
                if (p.isEmpty() && uri.getRawPath() != null) p = FtpTarget.percentDecode(uri.getRawPath());
                if (uri.getRawUserInfo() != null) {
                    String[] parts = uri.getRawUserInfo().split(":", 2);
                    if (u.isEmpty()) u = FtpTarget.percentDecode(parts[0]);
                    if (pass.isEmpty() && parts.length > 1) pass = FtpTarget.percentDecode(parts[1]);
                }
            }
        } catch (Exception ignored) {}
        return new String[]{cleanHost(host), normalizePath(p), FtpTarget.percentDecode(u), FtpTarget.percentDecode(pass)};
    }

    private static String cleanHost(String host) {
        String h = host == null ? "" : host.trim();
        h = h.replaceFirst("(?i)^ftp://", "");
        int slash = h.indexOf('/');
        if (slash >= 0) h = h.substring(0, slash);
        while (h.endsWith("/")) h = h.substring(0, h.length() - 1);
        return h;
    }

    private static String normalizePath(String path) {
        String p = path == null ? "" : FtpTarget.percentDecode(path.trim());
        while (p.startsWith("//")) p = p.substring(1);
        if (!p.isEmpty() && !p.startsWith("/")) p = "/" + p;
        return p;
    }
}
