package de.notizen.android.core;

/** Java model for the old fontsize.vb ToolStripTextBox. */
public final class LegacyFontSizeEntry {
    public static final String DEFAULT_TEXT = "8";
    public static final int MAX_SIZE = 99;

    private LegacyFontSizeEntry() {}

    public static final class State {
        public final String text;
        public final String lastValidText;
        public final boolean validNumber;
        public final boolean emptyIntermediate;
        public final int numericValue;
        public final boolean apply;

        public State(String text, String lastValidText, boolean validNumber, boolean emptyIntermediate, int numericValue, boolean apply) {
            this.text = clean(text);
            this.lastValidText = clean(lastValidText).isEmpty() ? DEFAULT_TEXT : clean(lastValidText);
            this.validNumber = validNumber;
            this.emptyIntermediate = emptyIntermediate;
            this.numericValue = numericValue;
            this.apply = apply;
        }
    }

    public static State onTextChanged(String input, String lastValidText) {
        String previous = normalizeLast(lastValidText);
        String raw = clean(input);
        if (raw.isEmpty()) return new State("", previous, false, true, parseInt(previous, 8), false);
        Integer parsed = parseNumericLikeVb(raw);
        if (parsed == null) return new State(previous, previous, true, false, parseInt(previous, 8), false);
        int value = Math.min(MAX_SIZE, Math.max(0, parsed));
        String text = Integer.toString(value);
        return new State(text, text, true, false, value, false);
    }

    public static State onEnter(String input, String lastValidText) {
        State changed = onTextChanged(input, lastValidText);
        if (!changed.validNumber || changed.emptyIntermediate) return new State(changed.text, changed.lastValidText, changed.validNumber, changed.emptyIntermediate, changed.numericValue, false);
        return new State(changed.text, changed.lastValidText, true, false, changed.numericValue, true);
    }

    public static int valueOrDefault(String input) {
        return onTextChanged(input, DEFAULT_TEXT).numericValue;
    }

    private static Integer parseNumericLikeVb(String raw) {
        try {
            double d = Double.parseDouble(raw.replace(',', '.'));
            return (int) Math.round(d);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseInt(String s, int fallback) {
        try { return Integer.parseInt(clean(s)); }
        catch (Exception e) { return fallback; }
    }

    private static String normalizeLast(String last) {
        String l = clean(last);
        Integer n = parseNumericLikeVb(l);
        if (n == null) return DEFAULT_TEXT;
        return Integer.toString(Math.min(MAX_SIZE, Math.max(0, n)));
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
