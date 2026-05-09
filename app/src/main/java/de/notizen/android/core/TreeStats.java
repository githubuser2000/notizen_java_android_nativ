package de.notizen.android.core;

import java.util.ArrayDeque;
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
        ArrayDeque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(root, 1));
        while (!stack.isEmpty()) {
            Frame frame = stack.pop();
            NoteNode node = frame.node;
            if (node == null) continue;
            LegacyCrashHardening.checkTreeDepth(frame.depth);
            LegacyCrashHardening.checkTreeNodeCount(stats.nodes + 1);
            stats.nodes++;
            if (node.children.isEmpty()) stats.leaves++;
            if (frame.depth > stats.maxDepth) stats.maxDepth = frame.depth;
            if (node.desktopNote != null) stats.desktopNotes++;
            String rtf = node.rtf == null ? "" : node.rtf;
            stats.rtfBytes = LegacyCrashHardening.saturatedAdd(stats.rtfBytes, LegacyCrashHardening.utf8LengthEstimate(rtf));
            stats.images = LegacyCrashHardening.saturatedAdd(stats.images, RtfUtils.countImages(rtf));
            String text = RtfUtils.rtfToPlainText(rtf);
            stats.characters = LegacyCrashHardening.saturatedAdd(stats.characters, text.length());
            int noSpace = 0;
            for (int i = 0; i < text.length(); i++) if (!Character.isWhitespace(text.charAt(i))) noSpace++;
            stats.charactersNoSpace = LegacyCrashHardening.saturatedAdd(stats.charactersNoSpace, noSpace);
            stats.lines = LegacyCrashHardening.saturatedAdd(stats.lines, countLines(text));
            java.util.regex.Matcher m = WORD_RE.matcher(text);
            while (m.find()) stats.words = LegacyCrashHardening.saturatedAdd(stats.words, 1);
            for (int i = node.children.size() - 1; i >= 0; i--) stack.push(new Frame(node.children.get(i), frame.depth + 1));
        }
        return stats;
    }

    private static final class Frame {
        final NoteNode node;
        final int depth;
        Frame(NoteNode node, int depth) { this.node = node; this.depth = depth; }
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
