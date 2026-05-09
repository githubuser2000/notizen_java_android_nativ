package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cached next-result search session modelled after suche.vb. */
public final class LegacySearchSession {
    private String cachedTerm = "";
    private boolean cachedAllNodes;
    private boolean cachedWholeWords;
    private boolean cachedCaseSensitive;
    private boolean cachedIncludeTitles;
    private List<SearchResult> results = Collections.emptyList();
    private int nextIndex;

    public static final class SearchStep {
        public final boolean rebuilt;
        public final List<SearchResult> results;
        public final SearchResult selected;
        public final int selectedIndexOneBased;
        public final String resultCountLabel;
        public final String status;

        public SearchStep(boolean rebuilt, List<SearchResult> results, SearchResult selected, int selectedIndexOneBased, String resultCountLabel, String status) {
            this.rebuilt = rebuilt;
            this.results = Collections.unmodifiableList(new ArrayList<>(results == null ? Collections.<SearchResult>emptyList() : results));
            this.selected = selected;
            this.selectedIndexOneBased = selectedIndexOneBased;
            this.resultCountLabel = resultCountLabel == null ? "" : resultCountLabel;
            this.status = status == null ? "" : status;
        }
    }

    public SearchStep begin(NoteNode root, NoteNode currentNode, String term, boolean allNodes, boolean wholeWords,
                            boolean caseSensitive, boolean includeTitles, String language) {
        String normalized = term == null ? "" : term.trim();
        boolean rebuild = shouldRebuild(normalized, allNodes, wholeWords, caseSensitive, includeTitles);
        if (rebuild) {
            cachedTerm = normalized;
            cachedAllNodes = allNodes;
            cachedWholeWords = wholeWords;
            cachedCaseSensitive = caseSensitive;
            cachedIncludeTitles = includeTitles;
            results = run(root, currentNode, normalized, allNodes, wholeWords, caseSensitive, includeTitles);
            nextIndex = 0;
        }
        SearchResult selected = null;
        int indexOneBased = 0;
        if (!results.isEmpty()) {
            if (nextIndex >= results.size()) nextIndex = 0;
            selected = results.get(nextIndex);
            indexOneBased = nextIndex + 1;
            nextIndex++;
        }
        String count = LegacySearchDialogModel.resultsTitle(results.size(), language);
        String status = selected == null ? "Keine Treffer" : LegacySearchDialogModel.statusForResult(indexOneBased, results.size(), language);
        return new SearchStep(rebuild, results, selected, indexOneBased, count, status);
    }

    public boolean shouldRebuild(String term, boolean allNodes, boolean wholeWords, boolean caseSensitive, boolean includeTitles) {
        String t = term == null ? "" : term.trim();
        return !t.equals(cachedTerm) || cachedAllNodes != allNodes || cachedWholeWords != wholeWords
                || cachedCaseSensitive != caseSensitive || cachedIncludeTitles != includeTitles;
    }

    public String cachedTerm() { return cachedTerm; }
    public boolean cachedAllNodes() { return cachedAllNodes; }
    public boolean cachedWholeWords() { return cachedWholeWords; }
    public boolean cachedCaseSensitive() { return cachedCaseSensitive; }
    public boolean cachedIncludeTitles() { return cachedIncludeTitles; }
    public int resultCount() { return results.size(); }

    private static List<SearchResult> run(NoteNode root, NoteNode currentNode, String term, boolean allNodes,
                                          boolean wholeWords, boolean caseSensitive, boolean includeTitles) {
        if (term == null || term.isEmpty()) return Collections.emptyList();
        if (allNodes) return Search.searchNodes(root, term, wholeWords, caseSensitive, includeTitles);
        ArrayList<SearchResult> out = new ArrayList<>();
        NoteNode node = currentNode == null ? root : currentNode;
        if (node == null) return out;
        if (includeTitles) {
            String title = node.title == null ? "" : node.title;
            for (int[] span : Search.findInText(title, term, wholeWords, caseSensitive)) {
                out.add(new SearchResult(node, span[0], span[1] - span[0], true, Search.snippet(title, span[0], span[1] - span[0])));
            }
        }
        String plain = RtfUtils.rtfToPlainText(node.rtf == null ? "" : node.rtf);
        for (int[] span : Search.findInText(plain, term, wholeWords, caseSensitive)) {
            out.add(new SearchResult(node, span[0], span[1] - span[0], false, Search.snippet(plain, span[0], span[1] - span[0])));
        }
        return out;
    }
}
