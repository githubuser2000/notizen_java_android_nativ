package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Model of the old suchergebnisse.vb search result window entries and navigation. */
public final class LegacySearchResultNavigator {
    public static final class Entry {
        public final NoteNode node;
        public final int selectionStart;
        public final int selectionLength;
        public final String label;

        public Entry(NoteNode node, int selectionStart, int selectionLength, String label) {
            this.node = node;
            this.selectionStart = Math.max(0, selectionStart);
            this.selectionLength = Math.max(0, selectionLength);
            this.label = label == null ? "" : label;
        }
    }

    public static final class State {
        public final List<Entry> entries;
        public final int index;

        public State(List<Entry> entries, int index) {
            this.entries = Collections.unmodifiableList(new ArrayList<>(entries == null ? Collections.<Entry>emptyList() : entries));
            this.index = normalizeIndex(index, this.entries.size());
        }

        public Entry current() { return entries.isEmpty() ? null : entries.get(index); }
        public String statusGerman() { return entries.isEmpty() ? "Keine Treffer" : "Treffer " + (index + 1) + " von " + entries.size(); }
    }

    private LegacySearchResultNavigator() {}

    public static State fromResults(List<SearchResult> results) {
        ArrayList<Entry> entries = new ArrayList<>();
        if (results != null) for (SearchResult r : results) {
            if (r == null) continue;
            entries.add(new Entry(r.node, r.start, r.length, r.label()));
        }
        return new State(entries, 0);
    }

    public static State next(State state) {
        if (state == null || state.entries.isEmpty()) return new State(Collections.<Entry>emptyList(), 0);
        return new State(state.entries, state.index + 1);
    }

    public static State previous(State state) {
        if (state == null || state.entries.isEmpty()) return new State(Collections.<Entry>emptyList(), 0);
        return new State(state.entries, state.index - 1);
    }

    private static int normalizeIndex(int index, int size) {
        if (size <= 0) return 0;
        int v = index % size;
        return v < 0 ? v + size : v;
    }
}
