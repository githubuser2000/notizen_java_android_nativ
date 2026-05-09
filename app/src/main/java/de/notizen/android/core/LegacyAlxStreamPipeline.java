package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Documents the old ALX stream layering for load/save paths: target stream,
 * optional three-pass DES password layer, GZip and UTF-16 XML reader/writer.
 */
public final class LegacyAlxStreamPipeline {
    public enum Operation { OPEN, SAVE }
    public enum Target { LOCAL_FILE, FTP, ANDROID_URI, MEMORY, UNKNOWN }

    public static final class Plan {
        public final Operation operation;
        public final Target target;
        public final String normalizedTarget;
        public final boolean encrypted;
        public final boolean backupBeforeWrite;
        public final List<String> layers;
        public final List<String> closeOrder;
        public final String reason;
        public Plan(Operation operation, Target target, String normalizedTarget, boolean encrypted, boolean backupBeforeWrite, List<String> layers, List<String> closeOrder, String reason) {
            this.operation = operation == null ? Operation.OPEN : operation;
            this.target = target == null ? Target.UNKNOWN : target;
            this.normalizedTarget = normalizedTarget == null ? "" : normalizedTarget;
            this.encrypted = encrypted;
            this.backupBeforeWrite = backupBeforeWrite;
            this.layers = Collections.unmodifiableList(new ArrayList<>(layers == null ? Collections.<String>emptyList() : layers));
            this.closeOrder = Collections.unmodifiableList(new ArrayList<>(closeOrder == null ? Collections.<String>emptyList() : closeOrder));
            this.reason = reason == null ? "" : reason;
        }
        public String summary() {
            StringBuilder b = new StringBuilder();
            b.append(operation).append(" ").append(target).append(" ").append(normalizedTarget).append('\n');
            b.append("encrypted=").append(encrypted).append(", backupBeforeWrite=").append(backupBeforeWrite).append('\n');
            b.append("layers=").append(layers).append('\n');
            b.append("closeOrder=").append(closeOrder);
            if (!reason.isEmpty()) b.append('\n').append(reason);
            return b.toString();
        }
    }

    private LegacyAlxStreamPipeline() {}

    public static Plan openPlan(String target, String password) {
        return plan(Operation.OPEN, target, false, 0, password);
    }

    public static Plan savePlan(String target, boolean changed, int backupKeep, String password) {
        return plan(Operation.SAVE, target, changed, backupKeep, password);
    }

    private static Plan plan(Operation op, String target, boolean changed, int backupKeep, String password) {
        Target t = detectTarget(target);
        boolean encrypted = hasLegacyPassword(password);
        boolean backup = op == Operation.SAVE && changed && backupKeep > 0 && t == Target.LOCAL_FILE;
        ArrayList<String> layers = new ArrayList<>();
        layers.add(targetLayer(t, op));
        if (encrypted) {
            layers.add("DES3 legacy CryptoStream");
            layers.add("DES2 legacy CryptoStream");
            layers.add("DES1 legacy CryptoStream");
        }
        layers.add("GZipStream");
        layers.add(op == Operation.SAVE ? "XmlTextWriter UTF-16" : "XmlTextReader UTF-16");
        ArrayList<String> close = new ArrayList<>();
        close.add(op == Operation.SAVE ? "XmlTextWriter" : "XmlTextReader");
        close.add("GZipStream");
        if (encrypted) close.add("CryptoStreams DES1/DES2/DES3");
        close.add(targetClose(t));
        String reason = backup ? "Legacy-Sicherung vor lokalem Überschreiben" : "Keine lokale Vorsicherung nötig";
        return new Plan(op, t, target == null ? "" : target.trim(), encrypted, backup, layers, close, reason);
    }

    public static boolean hasLegacyPassword(String password) {
        if (password == null) return false;
        String p = password;
        if (p.length() > 24) p = p.substring(0, 24);
        while (p.length() < 24) p += " ";
        return p.trim().length() > 0;
    }

    public static Target detectTarget(String target) {
        if (target == null || target.trim().isEmpty()) return Target.MEMORY;
        String s = target.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("ftp://")) return Target.FTP;
        if (s.startsWith("content://") || s.startsWith("android://")) return Target.ANDROID_URI;
        if (s.startsWith("file://") || s.contains("/") || s.contains("\\") || s.endsWith(".alx")) return Target.LOCAL_FILE;
        return Target.UNKNOWN;
    }

    private static String targetLayer(Target target, Operation op) {
        switch (target) {
            case FTP: return op == Operation.SAVE ? "FTP upload stream" : "FTP download stream";
            case ANDROID_URI: return "ContentResolver " + (op == Operation.SAVE ? "OutputStream" : "InputStream");
            case LOCAL_FILE: return "FileStream";
            case MEMORY: return "MemoryStream";
            default: return "UnknownStream";
        }
    }

    private static String targetClose(Target target) {
        switch (target) {
            case FTP: return "FTP stream";
            case ANDROID_URI: return "ContentResolver stream";
            case LOCAL_FILE: return "FileStream";
            case MEMORY: return "MemoryStream";
            default: return "UnknownStream";
        }
    }
}
