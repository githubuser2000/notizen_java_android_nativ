package de.notizen.android.core;

/** Portable state for the PyQt-like quick search row in the Android port. */
public final class LegacyQuickSearchBar {
    public enum Scope { CURRENT_SUBTREE, WHOLE_TREE }

    public static final class Options {
        public final String term;
        public final Scope scope;
        public final boolean wholeWords;
        public final boolean caseSensitive;
        public final boolean includeTitles;
        public final boolean accepted;
        public final String message;

        private Options(String term, Scope scope, boolean wholeWords, boolean caseSensitive, boolean includeTitles, boolean accepted, String message) {
            this.term = term == null ? "" : term;
            this.scope = scope == null ? Scope.CURRENT_SUBTREE : scope;
            this.wholeWords = wholeWords;
            this.caseSensitive = caseSensitive;
            this.includeTitles = includeTitles;
            this.accepted = accepted;
            this.message = message == null ? "" : message;
        }
    }

    private LegacyQuickSearchBar() {}

    public static Options normalize(String term, boolean wholeTree, boolean wholeWords, boolean caseSensitive, boolean includeTitles) {
        String t = term == null ? "" : term.trim();
        if (t.isEmpty()) return new Options("", wholeTree ? Scope.WHOLE_TREE : Scope.CURRENT_SUBTREE, wholeWords, caseSensitive, includeTitles, false, "Bitte Suchbegriff eingeben.");
        return new Options(t, wholeTree ? Scope.WHOLE_TREE : Scope.CURRENT_SUBTREE, wholeWords, caseSensitive, includeTitles, true, "");
    }

    public static String scopeLabel(Scope scope) {
        return scope == Scope.WHOLE_TREE ? "ganzer Baum" : "aktueller Teilbaum";
    }

    public static String status(int indexOneBased, int total, Scope scope) {
        if (total <= 0) return "Keine Treffer";
        int safe = Math.max(1, Math.min(indexOneBased, total));
        return "Treffer " + safe + " von " + total + " im " + scopeLabel(scope);
    }
}
