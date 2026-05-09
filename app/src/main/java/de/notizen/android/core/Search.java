package de.notizen.android.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Search {
    private Search() {}

    public static List<SearchResult> searchNodes(NoteNode root, String term, boolean wholeWords, boolean caseSensitive, boolean includeTitles) {
        ArrayList<SearchResult> results = new ArrayList<>();
        if (root == null || term == null || term.isEmpty()) return results;
        for (NoteNode node : root.walk()) {
            if (includeTitles) {
                for (int[] span : findInText(node.title == null ? "" : node.title, term, wholeWords, caseSensitive)) {
                    results.add(new SearchResult(node, span[0], span[1] - span[0], true, snippet(node.title, span[0], span[1] - span[0])));
                }
            }
            String plain = RtfUtils.rtfToPlainText(node.rtf == null ? "" : node.rtf);
            for (int[] span : findInText(plain, term, wholeWords, caseSensitive)) {
                results.add(new SearchResult(node, span[0], span[1] - span[0], false, snippet(plain, span[0], span[1] - span[0])));
            }
        }
        return results;
    }

    public static List<int[]> findInText(String text, String term, boolean wholeWords, boolean caseSensitive) {
        if (wholeWords) return legacyWholeWordMatches(text, term, caseSensitive);
        ArrayList<int[]> out = new ArrayList<>();
        if (text == null || term == null || term.isEmpty()) return out;
        String hay = caseSensitive ? text : text.toLowerCase(Locale.ROOT);
        String needle = caseSensitive ? term : term.toLowerCase(Locale.ROOT);
        int pos = 0;
        while (true) {
            int found = hay.indexOf(needle, pos);
            if (found < 0) break;
            out.add(new int[]{found, found + term.length()});
            pos = found + Math.max(1, term.length());
        }
        return out;
    }

    /** Notizen.NET whole-word mode split only at spaces and CR/LF. Tabs and punctuation stay in-token. */
    private static List<int[]> legacyWholeWordMatches(String text, String term, boolean caseSensitive) {
        ArrayList<int[]> out = new ArrayList<>();
        if (text == null || term == null || term.isEmpty()) return out;
        int tokenStart = -1;
        for (int i = 0; i <= text.length(); i++) {
            boolean boundary = i == text.length() || text.charAt(i) == ' ' || text.charAt(i) == '\r' || text.charAt(i) == '\n';
            if (!boundary && tokenStart < 0) tokenStart = i;
            if (boundary && tokenStart >= 0) {
                String token = text.substring(tokenStart, i);
                if (same(token, term, caseSensitive)) out.add(new int[]{tokenStart, i});
                tokenStart = -1;
            }
        }
        return out;
    }

    private static boolean same(String a, String b, boolean caseSensitive) {
        return caseSensitive ? a.equals(b) : a.equalsIgnoreCase(b);
    }

    public static String snippet(String text, int start, int length) {
        if (text == null) return "";
        int radius = 36;
        int safeStart = Math.max(0, Math.min(start, text.length()));
        int safeEnd = Math.max(safeStart, Math.min(safeStart + Math.max(0, length), text.length()));
        int s = Math.max(0, safeStart - radius);
        int e = Math.min(text.length(), safeEnd + radius);
        String raw = text.substring(s, e).replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        String snippet = raw.trim().replaceAll("\\s+", " ");
        if (snippet.isEmpty()) snippet = text.substring(safeStart, safeEnd);
        if (snippet.isEmpty()) snippet = "...";
        if (s > 0) snippet = "…" + snippet;
        if (e < text.length()) snippet = snippet + "…";
        return snippet;
    }
}
