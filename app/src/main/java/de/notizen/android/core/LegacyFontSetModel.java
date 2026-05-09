package de.notizen.android.core;

import java.util.Locale;

/**
 * Decision model for the old Notizen.vb font_set(...) routine. It separates
 * focus routing (tree vs editor), empty-selection fallback and RTF action naming
 * so Android can show/verify the legacy behavior without a WinForms RichTextBox.
 */
public final class LegacyFontSetModel {
    public enum Function { STYLE, FAMILY, SIZE_SET, SIZE_BIGGER, SIZE_SMALLER, COLOR, HIGHLIGHT }
    public enum Target { EDITOR_SELECTION, TREE_NODES, BLOCKED_NO_DOCUMENT }

    public static final class SelectionPlan {
        public final int originalStart;
        public final int originalLength;
        public final int applyStart;
        public final int applyLength;
        public final int restoreStart;
        public final int restoreLength;
        public final boolean selectedAllBecauseEmpty;
        public SelectionPlan(int originalStart, int originalLength, int applyStart, int applyLength, int restoreStart, int restoreLength, boolean selectedAllBecauseEmpty) {
            this.originalStart = Math.max(0, originalStart);
            this.originalLength = Math.max(0, originalLength);
            this.applyStart = Math.max(0, applyStart);
            this.applyLength = Math.max(0, applyLength);
            this.restoreStart = Math.max(0, restoreStart);
            this.restoreLength = Math.max(0, restoreLength);
            this.selectedAllBecauseEmpty = selectedAllBecauseEmpty;
        }
        public String summary() { return "apply=" + applyStart + "+" + applyLength + ", restore=" + restoreStart + "+" + restoreLength + ", all=" + selectedAllBecauseEmpty; }
    }

    public static final class FontPlan {
        public final Target target;
        public final Function function;
        public final String style;
        public final String family;
        public final int resultingSize;
        public final int colorArgb;
        public final SelectionPlan selection;
        public final String rtfAction;
        public final String treeStyle;
        public final boolean nodeRtfChanged;
        public final boolean desktopNoteReload;
        public FontPlan(Target target, Function function, String style, String family, int resultingSize, int colorArgb, SelectionPlan selection, String rtfAction, String treeStyle, boolean nodeRtfChanged, boolean desktopNoteReload) {
            this.target = target == null ? Target.BLOCKED_NO_DOCUMENT : target;
            this.function = function == null ? Function.STYLE : function;
            this.style = clean(style);
            this.family = clean(family);
            this.resultingSize = clampSize(resultingSize);
            this.colorArgb = colorArgb;
            this.selection = selection == null ? selection(0, 0, 0) : selection;
            this.rtfAction = clean(rtfAction);
            this.treeStyle = clean(treeStyle);
            this.nodeRtfChanged = nodeRtfChanged;
            this.desktopNoteReload = desktopNoteReload;
        }
        public String summary() {
            return target + " " + function + " action=" + rtfAction + " tree=" + treeStyle + " size=" + resultingSize + " selection=" + selection.summary() + " reload=" + desktopNoteReload;
        }
    }

    private LegacyFontSetModel() {}

    public static SelectionPlan selection(int textLength, int selectionStart, int selectionLength) {
        int len = Math.max(0, textLength);
        int start = Math.max(0, Math.min(selectionStart, len));
        int slen = Math.max(0, Math.min(selectionLength, len - start));
        if (len > 0 && slen == 0) return new SelectionPlan(start, 0, 0, len, start, 0, true);
        return new SelectionPlan(start, slen, start, slen, start, slen, false);
    }

    public static FontPlan plan(boolean hasDocument, boolean treeHasFocus, int textLength, int selectionStart, int selectionLength, Function function, String style, String family, int currentSize, int colorArgb, boolean desktopNotePresent) {
        if (!hasDocument) return new FontPlan(Target.BLOCKED_NO_DOCUMENT, function, style, family, currentSize, colorArgb, selection(textLength, selectionStart, selectionLength), "blocked-no-document", "", false, false);
        Function f = function == null ? Function.STYLE : function;
        boolean treeTarget = treeHasFocus && f == Function.STYLE && isTreeStyle(style);
        SelectionPlan sel = selection(textLength, selectionStart, selectionLength);
        int size = sizeAfter(f, currentSize);
        String rtfAction = rtfAction(f, style, family, size, colorArgb);
        String treeStyle = treeTarget ? normalizedStyle(style) : "";
        Target target = treeTarget ? Target.TREE_NODES : Target.EDITOR_SELECTION;
        return new FontPlan(target, f, style, family, size, colorArgb, sel, rtfAction, treeStyle, true, desktopNotePresent);
    }

    private static int sizeAfter(Function f, int currentSize) {
        int s = clampSize(currentSize <= 0 ? 10 : currentSize);
        if (f == Function.SIZE_BIGGER) return clampSize(s + 1);
        if (f == Function.SIZE_SMALLER) return clampSize(s - 1);
        return s;
    }

    public static int clampSize(int size) {
        if (size < 8) return 8;
        if (size > 100) return 100;
        return size;
    }

    private static boolean isTreeStyle(String style) {
        String s = normalizedStyle(style);
        return "bold".equals(s) || "italic".equals(s) || "underline".equals(s) || "strike".equals(s);
    }

    private static String normalizedStyle(String style) {
        String s = clean(style).toLowerCase(Locale.ROOT);
        if (s.equals("fett")) return "bold";
        if (s.equals("kursiv")) return "italic";
        if (s.equals("unterstrichen")) return "underline";
        if (s.equals("durchgestrichen")) return "strike";
        return s;
    }

    private static String rtfAction(Function f, String style, String family, int size, int colorArgb) {
        switch (f == null ? Function.STYLE : f) {
            case FAMILY: return "font_family:" + (clean(family).isEmpty() ? "Microsoft Sans Serif" : clean(family));
            case SIZE_SET: return "font_size:" + clampSize(size);
            case SIZE_BIGGER: return "font_size_bigger:" + clampSize(size);
            case SIZE_SMALLER: return "font_size_smaller:" + clampSize(size);
            case COLOR: return "text_color:#" + String.format(Locale.ROOT, "%08X", colorArgb);
            case HIGHLIGHT: return "highlight_color:#" + String.format(Locale.ROOT, "%08X", colorArgb);
            default:
                String s = normalizedStyle(style);
                if (s.isEmpty()) s = "toggle";
                return "format_" + s;
        }
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
