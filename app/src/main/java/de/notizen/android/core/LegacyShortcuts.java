package de.notizen.android.core;

import java.util.Locale;

/** Pure Java mapping of the legacy Notizen.NET global/tree keyboard shortcuts. */
public final class LegacyShortcuts {
    public static final class Shortcut {
        public final String action;
        public final boolean requiresTreeFocus;
        public final boolean requiresEditorFocus;

        private Shortcut(String action, boolean requiresTreeFocus, boolean requiresEditorFocus) {
            this.action = action == null ? "" : action;
            this.requiresTreeFocus = requiresTreeFocus;
            this.requiresEditorFocus = requiresEditorFocus;
        }
    }

    private LegacyShortcuts() {}

    public static Shortcut resolve(String keyName, boolean control, boolean shift, boolean alt, boolean treeFocus, boolean editorFocus) {
        if (alt) return null;
        String key = keyName == null ? "" : keyName.trim().toLowerCase(Locale.ROOT);

        if (control && !shift) {
            String action;
            switch (key) {
                case "space": action = "alarm"; break;
                case "s": action = "save"; break;
                case "o": action = "open"; break;
                case "n": action = "new_document"; break;
                case "q": action = "quit"; break;
                case "c": action = "copy"; break;
                case "v": action = "paste"; break;
                case "x": action = "cut"; break;
                case "u": action = "rename"; break;
                case "f": action = "search"; break;
                case "+":
                case "plus":
                case "add":
                case "=": action = "font_bigger"; break;
                case "minus":
                case "-":
                case "subtract": action = "font_smaller"; break;
                default: return null;
            }
            if (("font_bigger".equals(action) || "font_smaller".equals(action)) && !editorFocus) return null;
            return new Shortcut(action, false, "font_bigger".equals(action) || "font_smaller".equals(action));
        }

        if (shift && !control) {
            if (!treeFocus) return null;
            if ("insert".equals(key)) return new Shortcut("paste_node", true, false);
            if ("delete".equals(key)) return new Shortcut("cut_node", true, false);
            return null;
        }

        if (!control && !shift && treeFocus) {
            if ("insert".equals(key)) return new Shortcut("add_child", true, false);
            if ("delete".equals(key)) return new Shortcut("delete_node", true, false);
            if ("return".equals(key) || "enter".equals(key)) return new Shortcut("add_sibling", true, false);
        }
        return null;
    }
}
