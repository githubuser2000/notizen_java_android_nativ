package de.notizen.android.core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Current-node export helpers matching the old tree context-menu "Speichern" path. */
public final class LegacyNodeExport {
    public static final String LEGACY_TREE_CONTEXT_SAVE_DEFAULT = "unbenannt.rtf";

    private LegacyNodeExport() {}

    public static String currentNodeRtf(NoteNode node) {
        if (node == null) return RtfUtils.plainTextToRtf("");
        String rtf = node.rtf == null ? "" : node.rtf;
        if (RtfUtils.looksLikeRtf(rtf)) return rtf;
        return RtfUtils.plainTextToRtf(rtf);
    }

    public static String currentNodePlainText(NoteNode node) {
        return normalizeCrlf(RtfUtils.rtfToPlainText(currentNodeRtf(node)));
    }

    public static byte[] currentNodeRtfBytes(NoteNode node) {
        return currentNodeRtf(node).getBytes(Charset.forName("windows-1252"));
    }

    public static byte[] currentNodeTextBytes(NoteNode node) {
        return currentNodePlainText(node).getBytes(StandardCharsets.UTF_8);
    }

    public static String defaultFileName(NoteNode node, String extension) {
        String ext = extension == null || extension.trim().isEmpty() ? ".rtf" : extension.trim();
        if (!ext.startsWith(".")) ext = "." + ext;
        String title = node == null ? "" : node.title;
        String safe = sanitizeBaseName(title);
        if (safe.isEmpty()) safe = "unbenannt";
        return safe + ext.toLowerCase(Locale.ROOT);
    }

    public static String sanitizeBaseName(String title) {
        if (title == null) return "";
        StringBuilder out = new StringBuilder(title.length());
        boolean lastUnderscore = false;
        for (int i = 0; i < title.length(); i++) {
            char c = title.charAt(i);
            boolean ok = Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '.';
            if (ok) {
                out.append(c);
                lastUnderscore = false;
            } else if (!lastUnderscore) {
                out.append('_');
                lastUnderscore = true;
            }
        }
        while (out.length() > 0 && (out.charAt(0) == '_' || out.charAt(0) == '.')) out.deleteCharAt(0);
        while (out.length() > 0 && out.charAt(out.length() - 1) == '_') out.setLength(out.length() - 1);
        return out.toString();
    }

    public static String normalizeCrlf(String text) {
        String s = text == null ? "" : text.replace("\r\n", "\n").replace('\r', '\n');
        return s.replace("\n", "\r\n");
    }
}
