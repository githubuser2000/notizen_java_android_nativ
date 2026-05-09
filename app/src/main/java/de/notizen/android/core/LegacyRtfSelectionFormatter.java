package de.notizen.android.core;

/** Minimal RichTextBox-style formatting bridge for Android's plain EditText selection. */
public final class LegacyRtfSelectionFormatter {
    private LegacyRtfSelectionFormatter() {}

    public static String applyToSelection(String plainText, int selectionStart, int selectionEnd, String action) {
        String text = plainText == null ? "" : plainText;
        int start = Math.max(0, Math.min(selectionStart, text.length()));
        int end = Math.max(0, Math.min(selectionEnd, text.length()));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        if (start == end) { start = 0; end = text.length(); }
        String normalizedAction = action == null ? "" : action;
        if (normalizedAction.startsWith("align_")) return paragraphFormattedRtf(text, normalizedAction);
        String prefix = inlinePrefix(normalizedAction);
        if (prefix.isEmpty()) return RtfUtils.plainTextToRtf(text);
        return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fnil Microsoft Sans Serif;}}\\viewkind4\\uc1\\pard "
                + RtfUtils.rtfEscapeMultiline(text.substring(0, start))
                + "{" + prefix + " " + RtfUtils.rtfEscapeMultiline(text.substring(start, end)) + "}"
                + RtfUtils.rtfEscapeMultiline(text.substring(end))
                + "\\par}";
    }

    public static String applyFontSizeToSelection(String plainText, int selectionStart, int selectionEnd, int points) {
        int clamped = LegacyRichTextToolbar.normalizeFontSize(points);
        return applyCustomPrefix(plainText, selectionStart, selectionEnd, "\\fs" + (clamped * 2));
    }

    public static String applyFontFamilyToSelection(String plainText, int selectionStart, int selectionEnd, String family) {
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family);
        String text = plainText == null ? "" : plainText;
        int start = Math.max(0, Math.min(selectionStart, text.length()));
        int end = Math.max(0, Math.min(selectionEnd, text.length()));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        if (start == end) { start = 0; end = text.length(); }
        return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fnil Microsoft Sans Serif;}{\\f1\\fnil " + clean + ";}}\\viewkind4\\uc1\\pard "
                + RtfUtils.rtfEscapeMultiline(text.substring(0, start))
                + "{\\f1 " + RtfUtils.rtfEscapeMultiline(text.substring(start, end)) + "}"
                + RtfUtils.rtfEscapeMultiline(text.substring(end))
                + "\\par}";
    }

    public static String applyColorToSelection(String plainText, int selectionStart, int selectionEnd, LegacyColorDialogModel.Decision color) {
        LegacyColorDialogModel.Decision c = color == null ? LegacyColorDialogModel.invalid(LegacyColorDialogModel.Role.RTF_TEXT) : color;
        if (!c.valid) return RtfUtils.plainTextToRtf(plainText == null ? "" : plainText);
        String text = plainText == null ? "" : plainText;
        int start = Math.max(0, Math.min(selectionStart, text.length()));
        int end = Math.max(0, Math.min(selectionEnd, text.length()));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        if (start == end) { start = 0; end = text.length(); }
        String reset = c.role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT ? "\\highlight0" : "\\cf0";
        return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fnil Microsoft Sans Serif;}}"
                + c.rtfColorTable
                + "\\viewkind4\\uc1\\pard "
                + RtfUtils.rtfEscapeMultiline(text.substring(0, start))
                + "{" + c.rtfPrefix + " " + RtfUtils.rtfEscapeMultiline(text.substring(start, end)) + reset + "}"
                + RtfUtils.rtfEscapeMultiline(text.substring(end))
                + "\\par}";
    }

    private static String applyCustomPrefix(String plainText, int selectionStart, int selectionEnd, String prefix) {
        String text = plainText == null ? "" : plainText;
        int start = Math.max(0, Math.min(selectionStart, text.length()));
        int end = Math.max(0, Math.min(selectionEnd, text.length()));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        if (start == end) { start = 0; end = text.length(); }
        return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fnil Microsoft Sans Serif;}}\\viewkind4\\uc1\\pard "
                + RtfUtils.rtfEscapeMultiline(text.substring(0, start))
                + "{" + prefix + " " + RtfUtils.rtfEscapeMultiline(text.substring(start, end)) + "}"
                + RtfUtils.rtfEscapeMultiline(text.substring(end))
                + "\\par}";
    }

    private static String paragraphFormattedRtf(String text, String action) {
        String align;
        if ("align_center".equals(action)) align = "\\qc";
        else if ("align_right".equals(action)) align = "\\qr";
        else if ("align_justify".equals(action)) align = "\\qj";
        else align = "\\ql";
        return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl{\\f0\\fnil Microsoft Sans Serif;}}\\viewkind4\\uc1\\pard" + align + " "
                + RtfUtils.rtfEscapeMultiline(text)
                + "\\par}";
    }

    private static String inlinePrefix(String action) {
        if ("format_bold".equals(action)) return "\\b";
        if ("format_italic".equals(action)) return "\\i";
        if ("format_underline".equals(action)) return "\\ul";
        if ("format_strike".equals(action)) return "\\strike";
        if ("format_regular".equals(action)) return "\\plain";
        if ("font_bigger".equals(action)) return "\\fs24";
        if ("font_smaller".equals(action)) return "\\fs16";
        return "";
    }
}
