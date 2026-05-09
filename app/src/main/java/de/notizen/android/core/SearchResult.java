package de.notizen.android.core;

public final class SearchResult {
    public final NoteNode node;
    public final int start;
    public final int length;
    public final boolean titleMatch;
    public final String preview;

    public SearchResult(NoteNode node, int start, int length, boolean titleMatch, String preview) {
        this.node = node;
        this.start = start;
        this.length = length;
        this.titleMatch = titleMatch;
        this.preview = preview == null ? "" : preview;
    }

    public String label() {
        String marker = titleMatch ? " [Titel]" : "";
        return nodePath(node) + marker + " · " + (start + 1) + ": " + preview;
    }

    public static String nodePath(NoteNode node) {
        if (node == null) return "";
        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        for (NoteNode n = node; n != null; n = n.parent) parts.add(0, n.title == null ? "..." : n.title);
        return String.join(" / ", parts);
    }
}
