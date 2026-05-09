package de.notizen.android.core;

import java.util.regex.Pattern;

public final class TreeStats {
    private static final Pattern WORD_RE = Pattern.compile("\\w+", Pattern.UNICODE_CHARACTER_CLASS);

    public int nodes;
    public int leaves;
    public int maxDepth;
    public int desktopNotes;
    public int characters;
    public int charactersNoSpace;
    public int words;
    public int lines;
    public int images;
    public int rtfBytes;

    public static TreeStats collect(NoteNode root) {
        TreeStats stats = new TreeStats();
        if (root == null) return stats;
        collectInto(root, 1, stats);
        return stats;
    }

    private static void collectInto(NoteNode node, int depth, TreeStats stats) {
        stats.nodes++;
        if (node.children.isEmpty()) stats.leaves++;
        if (depth > stats.maxDepth) stats.maxDepth = depth;
        if (node.desktopNote != null) stats.desktopNotes++;
        String rtf = node.rtf == null ? "" : node.rtf;
        stats.rtfBytes += rtf.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        stats.images += RtfUtils.countImages(rtf);
        String text = RtfUtils.rtfToPlainText(rtf);
        stats.characters += text.length();
        for (int i = 0; i < text.length(); i++) if (!Character.isWhitespace(text.charAt(i))) stats.charactersNoSpace++;
        stats.lines += countLines(text);
        java.util.regex.Matcher m = WORD_RE.matcher(text);
        while (m.find()) stats.words++;
        for (NoteNode child : node.children) collectInto(child, depth + 1, stats);
    }

    private static int countLines(String text) {
        if (text == null || text.isEmpty()) return 0;
        int lines = 1;
        for (int i = 0; i < text.length(); i++) if (text.charAt(i) == '\n') lines++;
        return lines;
    }

    public String asLegacyText() {
        return "Knoten: " + nodes + "\n" +
                "Blätter: " + leaves + "\n" +
                "Maximale Tiefe: " + maxDepth + "\n" +
                "Desktop-Notizen: " + desktopNotes + "\n" +
                "Zeilen: " + lines + "\n" +
                "Wörter: " + words + "\n" +
                "Zeichen: " + characters + "\n" +
                "Zeichen ohne Leerraum: " + charactersNoSpace + "\n" +
                "Bilder: " + images + "\n" +
                "RTF-Bytes: " + rtfBytes;
    }
}
