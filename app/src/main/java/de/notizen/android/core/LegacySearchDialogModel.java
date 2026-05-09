package de.notizen.android.core;

/** Search dialog state bridge from suche.vb. */
public final class LegacySearchDialogModel {
    public static final class SearchOptions {
        public final boolean accepted;
        public final String term;
        public final boolean wholeWords;
        public final boolean caseSensitive;
        public final boolean includeTitles;
        public final String title;
        public final String message;

        public SearchOptions(boolean accepted, String term, boolean wholeWords, boolean caseSensitive,
                             boolean includeTitles, String title, String message) {
            this.accepted = accepted;
            this.term = term == null ? "" : term;
            this.wholeWords = wholeWords;
            this.caseSensitive = caseSensitive;
            this.includeTitles = includeTitles;
            this.title = title == null ? "" : title;
            this.message = message == null ? "" : message;
        }
    }

    private LegacySearchDialogModel() {}

    public static SearchOptions normalize(String term, boolean wholeWords, boolean caseSensitive,
                                          boolean includeTitles, String language) {
        String t = term == null ? "" : term.trim();
        String title = dialogTitle(language);
        if (t.isEmpty()) {
            String msg = LegacyI18n.resolveLanguage(language).equals("Deutsch") ? "Suchbegriff fehlt." : "Search term is missing.";
            return new SearchOptions(false, "", wholeWords, caseSensitive, includeTitles, title, msg);
        }
        return new SearchOptions(true, t, wholeWords, caseSensitive, includeTitles, title, "");
    }

    public static String dialogTitle(String language) {
        return LegacyI18n.tr(language, "suche1", LegacyI18n.resolveLanguage(language).equals("Deutsch") ? "Suche" : "Search");
    }

    public static String searchButton(String language) {
        return LegacyI18n.tr(language, "suche2", LegacyI18n.resolveLanguage(language).equals("Deutsch") ? "Suchen" : "Search");
    }

    public static String resultsTitle(int count, String language) {
        return LegacyI18n.resolveLanguage(language).equals("Deutsch") ? "Ergebnisse: " + count : "Results: " + count;
    }

    public static String statusForResult(int indexOneBased, int count, String language) {
        return LegacyI18n.resolveLanguage(language).equals("Deutsch")
                ? "Treffer " + indexOneBased + " von " + count
                : "Result " + indexOneBased + " of " + count;
    }
}
