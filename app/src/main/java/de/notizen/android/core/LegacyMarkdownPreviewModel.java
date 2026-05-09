package de.notizen.android.core;

import java.util.regex.Pattern;

/**
 * Portable model for the Android Markdown preview bridge.
 * The real RTF stays in the note node.  Preview mode first converts that RTF
 * to raw text, drops non-text picture/object placeholders, detects Markdown,
 * and lets the Android UI render a read-only representation from that raw text.
 */
public final class LegacyMarkdownPreviewModel {
    private static final Pattern HEADING = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s+\\S.*$");
    private static final Pattern FENCED_CODE = Pattern.compile("(?m)^\\s{0,3}(```|~~~).*$");
    private static final Pattern UNORDERED_LIST = Pattern.compile("(?m)^\\s{0,6}[-+*]\\s+\\S.*$");
    private static final Pattern ORDERED_LIST = Pattern.compile("(?m)^\\s{0,6}\\d{1,4}[.)]\\s+\\S.*$");
    private static final Pattern BLOCKQUOTE = Pattern.compile("(?m)^\\s{0,3}>\\s+\\S.*$");
    private static final Pattern TABLE_ROW = Pattern.compile("(?m)^\\s*\\|.+\\|\\s*$");
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("(?m)^\\s*\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?\\s*$");
    private static final Pattern INLINE_STRONG = Pattern.compile("(?s)(\\*\\*|__)[^\\r\\n].*?\\1");
    private static final Pattern INLINE_EMPHASIS = Pattern.compile("(?s)(?<!\\*)\\*[^\\s*][^\\r\\n]*?\\*(?!\\*)|(?<!_)_[^\\s_][^\\r\\n]*?_(?!_)");
    private static final Pattern INLINE_CODE = Pattern.compile("`[^`\\r\\n]+`");
    private static final Pattern INLINE_STRIKE = Pattern.compile("~~[^~\\r\\n]+~~");
    private static final Pattern INLINE_LINK = Pattern.compile("!?\\[[^\\]\\r\\n]+\\]\\([^\\s)]+(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern HR = Pattern.compile("(?m)^\\s{0,3}([-*_])(?:\\s*\\1){2,}\\s*$");

    private LegacyMarkdownPreviewModel() {}

    public static String rawMarkdownTextFromRtf(String rtf) {
        String plain = RtfUtils.rtfToPlainText(rtf == null ? "" : rtf);
        return cleanRawText(plain);
    }

    public static String cleanRawText(String text) {
        if (text == null || text.isEmpty()) return "";
        String out = text.replace("\r\n", "\n").replace('\r', '\n');
        out = out.replace(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, "");
        out = out.replace(RtfUtils.LEGACY_OBJECT_PLACEHOLDER, "");
        out = out.replace("[Bild]", "");
        out = out.replace("[Objekt]", "");
        // Remove common accidental spaces left by stripped picture/object placeholders.
        out = out.replaceAll("(?m)[ \\t]+$", "");
        return out;
    }

    public static boolean looksLikeMarkdown(String rawText) {
        String text = cleanRawText(rawText);
        if (text.trim().isEmpty()) return false;
        int score = markdownScore(text);
        return score >= 2 || hasStrongBlockMarkdown(text);
    }

    public static int markdownScore(String rawText) {
        String text = cleanRawText(rawText);
        if (text.trim().isEmpty()) return 0;
        int score = 0;
        if (HEADING.matcher(text).find()) score += 3;
        if (FENCED_CODE.matcher(text).find()) score += 3;
        if (UNORDERED_LIST.matcher(text).find()) score += 2;
        if (ORDERED_LIST.matcher(text).find()) score += 2;
        if (BLOCKQUOTE.matcher(text).find()) score += 2;
        if (TABLE_ROW.matcher(text).find() && TABLE_SEPARATOR.matcher(text).find()) score += 3;
        if (HR.matcher(text).find()) score += 2;
        if (INLINE_STRONG.matcher(text).find()) score += 2;
        if (INLINE_LINK.matcher(text).find()) score += 2;
        if (INLINE_CODE.matcher(text).find()) score += 1;
        if (INLINE_STRIKE.matcher(text).find()) score += 1;
        if (INLINE_EMPHASIS.matcher(text).find()) score += 1;
        return score;
    }

    public static boolean hasStrongBlockMarkdown(String rawText) {
        String text = cleanRawText(rawText);
        return HEADING.matcher(text).find()
                || FENCED_CODE.matcher(text).find()
                || (TABLE_ROW.matcher(text).find() && TABLE_SEPARATOR.matcher(text).find());
    }

    public static String statusForPreview(String rawText) {
        String text = cleanRawText(rawText);
        int lines = text.isEmpty() ? 0 : text.split("\n", -1).length;
        int chars = text.length();
        return "Markdown-Vorschau · " + lines + " Zeilen · " + chars + " Zeichen";
    }
}
