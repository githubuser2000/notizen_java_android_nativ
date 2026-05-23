package de.notizen.android.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Portable model for the Android Markdown preview bridge.
 * The real RTF stays in the note node.  Preview mode first converts that RTF
 * to raw text, drops non-text picture/object placeholders, detects Markdown,
 * and lets the Android UI render a read-only representation from that raw text.
 */
public final class LegacyMarkdownPreviewModel {
    private static final Pattern HEADING = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s+\\S.*$");
    private static final Pattern SETEXT_HEADING = Pattern.compile("(?m)^\\S.*\\R\\s*(=+|-+)\\s*$");
    private static final Pattern FENCED_CODE = Pattern.compile("(?m)^\\s{0,3}(`{3,}|~{3,}).*$");
    private static final Pattern UNORDERED_LIST = Pattern.compile("(?m)^\\s{0,24}[-+*]\\s+\\S.*$");
    private static final Pattern TASK_LIST = Pattern.compile("(?m)^\\s{0,24}(?:[-+*]|\\d{1,9}[.)])\\s+\\[[ xX]\\](?:\\s+.*)?$");
    private static final Pattern ORDERED_LIST = Pattern.compile("(?m)^\\s{0,24}\\d{1,9}[.)]\\s+\\S.*$");
    private static final Pattern BLOCKQUOTE = Pattern.compile("(?m)^\\s{0,3}>\\s*\\S.*$");
    private static final Pattern INLINE_STRONG = Pattern.compile("(?s)(\\*\\*|__)[^\\r\\n].*?\\1");
    private static final Pattern INLINE_EMPHASIS = Pattern.compile("(?s)(?<!\\*)\\*[^\\s*][^\\r\\n]*?[^\\s*]\\*(?!\\*)|(?<![\\p{Alnum}_])_[^\\s_][^\\r\\n]*?[^\\s_]_(?![\\p{Alnum}_])");
    private static final Pattern INLINE_CODE = Pattern.compile("`+[^`\\r\\n]+`+");
    private static final Pattern INLINE_STRIKE = Pattern.compile("~~[^~\\r\\n]+~~");
    private static final Pattern INLINE_MARK = Pattern.compile("==[^=\\r\\n]+==");
    private static final Pattern INLINE_INS = Pattern.compile("\\+\\+[^+\\r\\n]+\\+\\+");
    private static final Pattern INLINE_MATH = Pattern.compile("(?s)(?<!\\$)\\$[^\\s$][^\\r\\n]*?[^\\s$]\\$(?!\\$)|\\$\\$.*?\\$\\$");
    private static final Pattern INLINE_LINK = Pattern.compile("!?\\[[^\\]\\r\\n]+\\]\\([^\\r\\n)]*\\)|!?\\[[^\\]\\r\\n]+\\]\\[[^\\]\\r\\n]*\\]");
    private static final Pattern BARE_URL = Pattern.compile("(?i)\\b(?:https?://|www\\.)[^\\s<>()]+(?:\\([^\\s<>()]*\\)[^\\s<>()]*)*");
    private static final Pattern BARE_EMAIL = Pattern.compile("(?i)(?<![A-Z0-9._%+-])[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}(?![A-Z0-9._%+-])");
    private static final Pattern REFERENCE_LINK = Pattern.compile("(?m)^\\s{0,3}\\[[^\\]\\r\\n]+\\]:\\s*\\S+.*$");
    private static final Pattern FOOTNOTE = Pattern.compile("(?m)^\\s{0,3}\\[\\^[^\\]\\r\\n]+\\]:\\s+.*$");
    private static final Pattern HR = Pattern.compile("(?m)^\\s{0,3}([-*_])(?:\\s*\\1){2,}\\s*$");
    private static final Pattern FRONT_MATTER = Pattern.compile("(?s)^\\s*---\\R.+?\\R---\\R");
    private static final Pattern HTML_BLOCK_HINT = Pattern.compile("(?im)^\\s{0,3}</?(?:address|article|aside|blockquote|details|div|dl|dt|dd|figcaption|figure|footer|h[1-6]|header|hr|li|main|nav|ol|p|pre|section|summary|table|thead|tbody|tfoot|tr|td|th|ul)\\b[^>]*>");
    private static final Pattern GFM_ALERT = Pattern.compile("(?im)^\\s{0,3}>\\s*\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\\]\\s*$");
    private static final Pattern HTML_INLINE_HINT = Pattern.compile("(?i)</?(?:a|abbr|br|code|kbd|mark|span|sub|sup|u|ins|del|s|small|strong|em|b|i|img)\\b[^>]*>");
    private static final String HARD_BREAK = "\u0000NOTIZEN_MD_BR\u0000";
    private static final Set<String> ALLOWED_HTML_ELEMENTS = allowedHtmlElements();

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
        return out;
    }

    public static boolean looksLikeMarkdown(String rawText) {
        String text = cleanRawText(rawText);
        if (text.trim().isEmpty()) return false;
        int score = markdownScore(text);
        return score >= 1 || hasStrongBlockMarkdown(text);
    }

    public static int markdownScore(String rawText) {
        String text = cleanRawText(rawText);
        if (text.trim().isEmpty()) return 0;
        int score = 0;
        if (HEADING.matcher(text).find()) score += 3;
        if (SETEXT_HEADING.matcher(text).find()) score += 2;
        if (FENCED_CODE.matcher(text).find()) score += 3;
        if (TASK_LIST.matcher(text).find()) score += 3;
        if (UNORDERED_LIST.matcher(text).find()) score += 2;
        if (ORDERED_LIST.matcher(text).find()) score += 2;
        if (BLOCKQUOTE.matcher(text).find()) score += 2;
        if (hasMarkdownTable(text)) score += 4;
        if (HR.matcher(text).find()) score += 2;
        if (REFERENCE_LINK.matcher(text).find()) score += 2;
        if (FOOTNOTE.matcher(text).find()) score += 2;
        if (FRONT_MATTER.matcher(text).find()) score += 1;
        if (INLINE_STRONG.matcher(text).find()) score += 2;
        if (INLINE_LINK.matcher(text).find()) score += 2;
        if (INLINE_CODE.matcher(text).find()) score += 1;
        if (INLINE_STRIKE.matcher(text).find()) score += 1;
        if (INLINE_MARK.matcher(text).find()) score += 1;
        if (INLINE_INS.matcher(text).find()) score += 1;
        if (INLINE_MATH.matcher(text).find()) score += 1;
        if (INLINE_EMPHASIS.matcher(text).find()) score += 1;
        if (BARE_URL.matcher(text).find()) score += 1;
        if (BARE_EMAIL.matcher(text).find()) score += 1;
        if (HTML_INLINE_HINT.matcher(text).find()) score += 1;
        if (HTML_BLOCK_HINT.matcher(text).find()) score += 1;
        if (GFM_ALERT.matcher(text).find()) score += 2;
        return score;
    }

    public static boolean hasStrongBlockMarkdown(String rawText) {
        String text = cleanRawText(rawText);
        return HEADING.matcher(text).find()
                || SETEXT_HEADING.matcher(text).find()
                || FENCED_CODE.matcher(text).find()
                || hasMarkdownTable(text);
    }

    public static boolean hasMarkdownTable(String rawText) {
        String[] lines = cleanRawText(rawText).split("\n", -1);
        for (int i = 0; i + 1 < lines.length; i++) {
            List<String> header = splitTableRow(lines[i]);
            if (header.size() > 1 && parseTableAlignments(lines[i + 1], header.size()) != null) return true;
        }
        return false;
    }

    public static String statusForPreview(String rawText) {
        String text = cleanRawText(rawText);
        int lines = text.isEmpty() ? 0 : text.split("\n", -1).length;
        int chars = text.length();
        return "Markdown-Vorschau · " + lines + " Zeilen · " + chars + " Zeichen";
    }

    public static String toHtmlDocument(String rawText) {
        String clean = cleanRawText(rawText);
        String fragment = commonmarkToHtmlFragmentIfAvailable(clean);
        if (fragment == null) fragment = markdownToHtmlFragment(clean);
        return "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>"
                + "html,body{margin:0;padding:0;background:#fff;color:#1f2937;}"
                + "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;font-size:16px;line-height:1.48;padding:10px;word-wrap:break-word;}"
                + "h1,h2,h3,h4,h5,h6{line-height:1.22;margin:0.75em 0 0.35em;font-weight:700;color:#111827;}"
                + "h1{font-size:1.65em;border-bottom:1px solid #e5e7eb;padding-bottom:.18em;}h2{font-size:1.42em;border-bottom:1px solid #eef0f3;padding-bottom:.14em;}h3{font-size:1.24em;}h4{font-size:1.12em;}h5,h6{font-size:1em;}"
                + "p{margin:.55em 0;}a{color:#1554b7;text-decoration:underline;}"
                + "table{border-collapse:collapse;max-width:100%;width:auto;margin:.75em 0;display:block;overflow-x:auto;}th,td{border:1px solid #cfd6df;padding:.35em .55em;vertical-align:top;}th{background:#f3f6fa;font-weight:700;}tr:nth-child(even) td{background:#fafbfc;}"
                + "blockquote{border-left:4px solid #c8d2df;margin:.65em 0;padding:.25em .75em;color:#374151;background:#f8fafc;}"
                + "pre{background:#f1f5f9;border:1px solid #d8e0ea;border-radius:6px;padding:.65em;overflow:auto;}code{background:#eef2f7;border-radius:4px;padding:.08em .25em;font-family:'Droid Sans Mono','Roboto Mono',monospace;}pre code{background:transparent;padding:0;}"
                + "ul,ol{margin:.55em 0 .55em 1.4em;padding-left:1.1em;}li{margin:.22em 0;}li>p:first-child{margin-top:.15em;}li>p:last-child{margin-bottom:.15em;}"
                + "hr{border:0;border-top:1px solid #cfd6df;margin:.9em 0;}dl{margin:.55em 0;}dt{font-weight:700;}dd{margin:0 0 .45em 1.25em;}"
                + "mark{background:#fff4a3;padding:0 .08em;}kbd{border:1px solid #c8d2df;border-bottom-width:2px;border-radius:4px;background:#f8fafc;padding:.05em .3em;font-family:monospace;}"
                + ".task{font-family:monospace;margin-right:.35em;}.task.done{color:#166534;}.task-checkbox{margin-right:.35em;vertical-align:-.08em;}.md-image{max-width:100%;height:auto;border:1px solid #e5e7eb;border-radius:4px;}.md-image-alt{color:#6b7280;font-style:italic;}"
                + ".md-alert,.markdown-alert{border-left-width:5px;}.md-alert-title,.markdown-alert-title{font-weight:700;margin-bottom:.25em;}.md-alert-note,.markdown-alert-note{border-left-color:#3b82f6;}.md-alert-tip,.markdown-alert-tip{border-left-color:#16a34a;}.md-alert-important,.markdown-alert-important{border-left-color:#7c3aed;}.md-alert-warning,.markdown-alert-warning{border-left-color:#d97706;}.md-alert-caution,.markdown-alert-caution{border-left-color:#dc2626;}.math{font-family:serif;background:#f8fafc;border:1px solid #e5e7eb;border-radius:4px;padding:.04em .22em;}div.math{display:block;margin:.55em 0;padding:.45em .6em;overflow:auto;}"
                + ".footnotes{border-top:1px solid #d8e0ea;margin-top:1em;padding-top:.4em;font-size:.92em;color:#374151;}.footnote-backref{margin-left:.35em;text-decoration:none;}"
                + "</style></head><body>" + fragment + "</body></html>";
    }

    public static String markdownToHtmlFragment(String rawText) {
        MarkdownRenderer renderer = new MarkdownRenderer(cleanRawText(rawText));
        return renderer.render();
    }


    /**
     * Prefer a real CommonMark parser when the Android APK includes it.  The
     * core tests intentionally compile without external jars, so this path is
     * reflective and falls back to the local renderer when the dependency is not
     * available.  This gives APK builds CommonMark/GFM coverage without making
     * the portable Java core unbuildable in minimal environments.
     */
    private static String commonmarkToHtmlFragmentIfAvailable(String rawText) {
        if (Boolean.getBoolean("notizen.markdown.forceLegacyRenderer")) return null;
        try {
            ArrayList<Object> extensions = new ArrayList<>();
            addCommonmarkExtension(extensions, "org.commonmark.ext.autolink.AutolinkExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.gfm.strikethrough.StrikethroughExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.gfm.tables.TablesExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.gfm.alerts.AlertsExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.footnotes.FootnotesExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.heading.anchor.HeadingAnchorExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.ins.InsExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.task.list.items.TaskListItemsExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.image.attributes.ImageAttributesExtension");
            addCommonmarkExtension(extensions, "org.commonmark.ext.front.matter.YamlFrontMatterExtension");

            Class<?> parserClass = Class.forName("org.commonmark.parser.Parser");
            Object parserBuilder = parserClass.getMethod("builder").invoke(null);
            invokeFluent(parserBuilder, "extensions", extensions);
            Object parser = parserBuilder.getClass().getMethod("build").invoke(parserBuilder);
            Object document = parserClass.getMethod("parse", String.class).invoke(parser, rawText == null ? "" : rawText);

            Class<?> rendererClass = Class.forName("org.commonmark.renderer.html.HtmlRenderer");
            Class<?> nodeClass = Class.forName("org.commonmark.node.Node");
            Object rendererBuilder = rendererClass.getMethod("builder").invoke(null);
            invokeFluent(rendererBuilder, "extensions", extensions);
            invokeFluent(rendererBuilder, "sanitizeUrls", Boolean.TRUE);
            invokeFluent(rendererBuilder, "escapeHtml", Boolean.FALSE);
            Object renderer = rendererBuilder.getClass().getMethod("build").invoke(rendererBuilder);
            String html = (String) rendererClass.getMethod("render", nodeClass).invoke(renderer, document);
            return sanitizeRenderedHtml(html == null ? "" : html);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void addCommonmarkExtension(List<Object> extensions, String className) {
        try {
            Class<?> type = Class.forName(className);
            Object extension = null;
            if (className.endsWith("FootnotesExtension")) {
                try {
                    Object builder = type.getMethod("builder").invoke(null);
                    invokeFluent(builder, "inlineFootnotes", Boolean.TRUE);
                    extension = builder.getClass().getMethod("build").invoke(builder);
                } catch (Throwable ignored) {
                    extension = null;
                }
            }
            if (extension == null) extension = type.getMethod("create").invoke(null);
            if (extension != null) extensions.add(extension);
        } catch (Throwable ignored) {
            // Extension is optional.  Skip it rather than disabling the preview.
        }
    }

    private static boolean invokeFluent(Object target, String name, Object argument) {
        if (target == null || name == null) return false;
        java.lang.reflect.Method[] methods = target.getClass().getMethods();
        for (java.lang.reflect.Method method : methods) {
            if (!name.equals(method.getName()) || method.getParameterTypes().length != 1) continue;
            Class<?> p = method.getParameterTypes()[0];
            if (!reflectParameterAccepts(p, argument)) continue;
            try {
                method.invoke(target, argument);
                return true;
            } catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    private static boolean reflectParameterAccepts(Class<?> parameter, Object argument) {
        if (parameter == null) return false;
        if (parameter.isPrimitive()) {
            return parameter == Boolean.TYPE && argument instanceof Boolean
                    || parameter == Integer.TYPE && argument instanceof Integer
                    || parameter == Long.TYPE && argument instanceof Long;
        }
        if (argument == null) return !parameter.isPrimitive();
        return parameter.isInstance(argument)
                || (parameter == Iterable.class && argument instanceof Iterable)
                || (parameter == List.class && argument instanceof List);
    }

    private static String sanitizeRenderedHtml(String html) {
        if (html == null || html.isEmpty()) return "";
        StringBuilder out = new StringBuilder(html.length() + 16);
        int i = 0;
        while (i < html.length()) {
            int lt = html.indexOf('<', i);
            if (lt < 0) {
                out.append(html, i, html.length());
                break;
            }
            out.append(html, i, lt);
            int gt = findHtmlTagEnd(html, lt + 1);
            if (gt < 0) {
                out.append("&lt;");
                i = lt + 1;
                continue;
            }
            String safe = sanitizeHtmlTag(html.substring(lt + 1, gt));
            if (safe == null) out.append(escapeHtml(html.substring(lt, gt + 1)));
            else out.append(safe);
            i = gt + 1;
        }
        return out.toString();
    }

    private static int findHtmlTagEnd(String html, int start) {
        boolean single = false;
        boolean dbl = false;
        for (int i = start; i < html.length(); i++) {
            char c = html.charAt(i);
            if (c == '\'' && !dbl) single = !single;
            else if (c == '"' && !single) dbl = !dbl;
            else if (c == '>' && !single && !dbl) return i;
        }
        return -1;
    }

    private static String sanitizeHtmlTag(String rawInside) {
        if (rawInside == null) return null;
        String s = rawInside.trim();
        if (s.isEmpty() || s.startsWith("!") || s.startsWith("?")) return null;
        boolean closing = s.startsWith("/");
        if (closing) s = s.substring(1).trim();
        int nameEnd = 0;
        while (nameEnd < s.length() && (Character.isLetterOrDigit(s.charAt(nameEnd)) || s.charAt(nameEnd) == '-')) nameEnd++;
        if (nameEnd <= 0) return null;
        String name = s.substring(0, nameEnd).toLowerCase(Locale.ROOT);
        if (!isAllowedHtmlElement(name)) return null;
        if (closing) return "</" + name + ">";
        String rest = s.substring(nameEnd).trim();
        boolean selfClosing = rest.endsWith("/");
        if (selfClosing) rest = rest.substring(0, rest.length() - 1).trim();
        Map<String, String> attrs = parseHtmlAttributes(rest);
        StringBuilder out = new StringBuilder("<").append(name);
        appendSafeAttributes(out, name, attrs);
        if (isVoidHtmlElement(name)) out.append('>');
        else out.append(selfClosing ? "/>" : ">");
        return out.toString();
    }

    private static Map<String, String> parseHtmlAttributes(String raw) {
        LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) return attrs;
        int i = 0;
        while (i < raw.length()) {
            while (i < raw.length() && Character.isWhitespace(raw.charAt(i))) i++;
            int nameStart = i;
            while (i < raw.length()) {
                char c = raw.charAt(i);
                if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ':' || c == '.') i++;
                else break;
            }
            if (i <= nameStart) { i++; continue; }
            String name = raw.substring(nameStart, i).toLowerCase(Locale.ROOT);
            while (i < raw.length() && Character.isWhitespace(raw.charAt(i))) i++;
            String value = "";
            if (i < raw.length() && raw.charAt(i) == '=') {
                i++;
                while (i < raw.length() && Character.isWhitespace(raw.charAt(i))) i++;
                if (i < raw.length() && (raw.charAt(i) == '"' || raw.charAt(i) == '\'')) {
                    char quote = raw.charAt(i++);
                    int valueStart = i;
                    while (i < raw.length() && raw.charAt(i) != quote) i++;
                    value = raw.substring(valueStart, Math.min(i, raw.length()));
                    if (i < raw.length() && raw.charAt(i) == quote) i++;
                } else {
                    int valueStart = i;
                    while (i < raw.length() && !Character.isWhitespace(raw.charAt(i))) i++;
                    value = raw.substring(valueStart, i);
                }
            }
            if (!attrs.containsKey(name)) attrs.put(name, decodeBasicHtmlEntities(value));
        }
        return attrs;
    }

    private static void appendSafeAttributes(StringBuilder out, String tag, Map<String, String> attrs) {
        if (out == null || tag == null || attrs == null || attrs.isEmpty()) return;
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            String name = e.getKey();
            String value = e.getValue() == null ? "" : e.getValue();
            if (isSafeGlobalAttribute(name, value)) {
                appendHtmlAttribute(out, name, value);
                continue;
            }
            if (tag.equals("a") && name.equals("href") && safeUrl(value)) appendHtmlAttribute(out, name, value);
            else if (tag.equals("a") && name.equals("title")) appendHtmlAttribute(out, name, value);
            else if (tag.equals("img") && name.equals("src") && safeImageUrl(value)) appendHtmlAttribute(out, name, value);
            else if (tag.equals("img") && (name.equals("alt") || name.equals("title"))) appendHtmlAttribute(out, name, value);
            else if (tag.equals("img") && (name.equals("width") || name.equals("height")) && value.matches("[0-9]{1,5}|[0-9]{1,3}%")) appendHtmlAttribute(out, name, value);
            else if ((tag.equals("th") || tag.equals("td")) && name.equals("align") && isSafeTextAlignValue(value)) appendHtmlAttribute(out, name, value.toLowerCase(Locale.ROOT));
            else if ((tag.equals("th") || tag.equals("td")) && name.equals("style") && isSafeTextAlignStyle(value)) appendHtmlAttribute(out, name, normalizeTextAlignStyle(value));
            else if (tag.equals("ol") && name.equals("start") && value.matches("[0-9]{1,9}")) appendHtmlAttribute(out, name, value);
            else if (tag.equals("ol") && name.equals("reversed")) appendBooleanHtmlAttribute(out, name);
            else if (tag.equals("details") && name.equals("open")) appendBooleanHtmlAttribute(out, name);
            else if (tag.equals("input") && name.equals("type") && value.equalsIgnoreCase("checkbox")) appendHtmlAttribute(out, name, "checkbox");
            else if (tag.equals("input") && (name.equals("disabled") || name.equals("checked"))) appendBooleanHtmlAttribute(out, name);
        }
    }

    private static boolean isSafeGlobalAttribute(String name, String value) {
        if (name == null) return false;
        if (name.equals("id")) return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9_.:-]{0,96}");
        if (name.equals("class")) return value != null && value.matches("[A-Za-z0-9 _.:\\-]{0,200}");
        if (name.equals("title") || name.equals("aria-label")) return value != null && value.length() <= 512;
        return false;
    }

    private static void appendHtmlAttribute(StringBuilder out, String name, String value) {
        out.append(' ').append(name).append("=\"").append(escapeAttr(value == null ? "" : value)).append('"');
    }

    private static void appendBooleanHtmlAttribute(StringBuilder out, String name) {
        out.append(' ').append(name);
    }

    private static boolean isSafeTextAlignValue(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return v.equals("left") || v.equals("right") || v.equals("center");
    }

    private static boolean isSafeTextAlignStyle(String value) {
        return normalizeTextAlignStyle(value) != null;
    }

    private static String normalizeTextAlignStyle(String value) {
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        if (v.endsWith(";")) v = v.substring(0, v.length() - 1);
        if (v.equals("text-align:left")) return "text-align:left";
        if (v.equals("text-align:right")) return "text-align:right";
        if (v.equals("text-align:center")) return "text-align:center";
        return null;
    }

    private static String decodeBasicHtmlEntities(String value) {
        if (value == null || value.indexOf('&') < 0) return value == null ? "" : value;
        return value.replace("&quot;", "\"")
                .replace("&#34;", "\"")
                .replace("&#x22;", "\"")
                .replace("&#39;", "'")
                .replace("&#x27;", "'")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&#60;", "<")
                .replace("&#x3c;", "<")
                .replace("&gt;", ">")
                .replace("&#62;", ">")
                .replace("&#x3e;", ">")
                .replace("&amp;", "&");
    }

    private static boolean isAllowedHtmlElement(String name) {
        return ALLOWED_HTML_ELEMENTS.contains(name);
    }

    private static boolean isVoidHtmlElement(String name) {
        return name.equals("br") || name.equals("hr") || name.equals("img") || name.equals("input");
    }

    private static boolean isInlineHtmlElement(String name) {
        return name != null && (name.equals("a") || name.equals("abbr") || name.equals("br") || name.equals("code") || name.equals("em") || name.equals("strong")
                || name.equals("b") || name.equals("i") || name.equals("img") || name.equals("ins") || name.equals("del") || name.equals("s")
                || name.equals("kbd") || name.equals("mark") || name.equals("small") || name.equals("span") || name.equals("sub") || name.equals("sup")
                || name.equals("u"));
    }

    private static Set<String> allowedHtmlElements() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        String[] names = {
                "a", "abbr", "address", "article", "aside", "blockquote", "br", "code", "dd", "del", "details", "div", "dl", "dt", "em",
                "figcaption", "figure", "footer", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hr", "i", "img", "input", "ins",
                "kbd", "li", "main", "mark", "nav", "ol", "p", "pre", "s", "section", "small", "span", "strong", "sub", "summary",
                "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul"
        };
        for (String n : names) set.add(n);
        return set;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static int leadingSpaces(String s) {
        if (s == null) return 0;
        int count = 0;
        while (count < s.length() && s.charAt(count) == ' ') count++;
        return count;
    }

    private static boolean hasIndentAtLeast(String line, int count) {
        return leadingSpaces(line) >= count || (line != null && line.startsWith("\t"));
    }

    private static String stripIndent(String line, int count) {
        if (line == null || line.isEmpty()) return "";
        if (line.startsWith("\t")) return line.substring(1);
        int n = Math.min(count, leadingSpaces(line));
        return line.substring(n);
    }

    private static Fence parseFenceStart(String line) {
        if (line == null) return null;
        int i = 0;
        while (i < line.length() && i < 4 && line.charAt(i) == ' ') i++;
        if (i > 3 || i >= line.length()) return null;
        char ch = line.charAt(i);
        if (ch != '`' && ch != '~') return null;
        int j = i;
        while (j < line.length() && line.charAt(j) == ch) j++;
        int len = j - i;
        if (len < 3) return null;
        String info = line.substring(j).trim();
        if (ch == '`' && info.indexOf('`') >= 0) return null;
        return new Fence(ch, len, info);
    }

    private static boolean isFenceStart(String line) {
        return parseFenceStart(line) != null;
    }

    private static boolean isFenceClose(String line, Fence fence) {
        if (line == null || fence == null) return false;
        int i = 0;
        while (i < line.length() && i < 4 && line.charAt(i) == ' ') i++;
        if (i > 3 || i >= line.length() || line.charAt(i) != fence.ch) return false;
        int j = i;
        while (j < line.length() && line.charAt(j) == fence.ch) j++;
        if (j - i < fence.length) return false;
        while (j < line.length()) {
            if (!Character.isWhitespace(line.charAt(j))) return false;
            j++;
        }
        return true;
    }

    private static boolean isHr(String line) {
        return line != null && HR.matcher(line).matches();
    }

    private static String headingLineContent(String line) {
        String t = line == null ? "" : line.trim();
        Matcher m = Pattern.compile("^(#{1,6})\\s+(.+?)\\s*#*\\s*$").matcher(t);
        if (!m.matches()) return null;
        HeadingText heading = extractExplicitHeadingId(m.group(2));
        return m.group(1).length() + "\n" + heading.text + "\n" + heading.id;
    }

    private static HeadingText extractExplicitHeadingId(String raw) {
        String text = raw == null ? "" : raw.trim();
        Matcher id = Pattern.compile("^(.*?)\\s+\\{#([A-Za-z0-9_.:-]+)\\}\\s*$").matcher(text);
        if (id.matches()) return new HeadingText(id.group(1).trim(), id.group(2));
        return new HeadingText(text, "");
    }

    private static ListMarker parseListMarker(String line) {
        if (line == null) return null;
        Matcher m = Pattern.compile("^( *)([-+*]|\\d{1,9}[.)])([ \\t]+)(.*)$").matcher(line);
        if (!m.matches()) return null;
        int indent = m.group(1).length();
        String marker = m.group(2);
        boolean ordered = Character.isDigit(marker.charAt(0));
        int number = 1;
        if (ordered) {
            try { number = Integer.parseInt(marker.substring(0, marker.length() - 1)); }
            catch (NumberFormatException ignored) { number = 1; }
        }
        int contentIndent = m.group(1).length() + m.group(2).length() + m.group(3).length();
        return new ListMarker(indent, contentIndent, ordered, number, marker, m.group(4));
    }

    private static boolean isListLine(String line) {
        return parseListMarker(line) != null;
    }

    private static boolean isBlockStart(List<String> lines, int index) {
        if (index < 0 || index >= lines.size()) return false;
        String line = lines.get(index);
        if (isBlank(line)) return true;
        if (headingLineContent(line) != null || isFenceStart(line) || isHr(line) || isListLine(line)) return true;
        if (isBlockQuoteLine(line) || isHtmlBlockLine(line)) return true;
        if (index + 1 < lines.size() && isTableStart(lines, index)) return true;
        if (index + 1 < lines.size() && isSetextUnderline(lines.get(index + 1))) return true;
        if (line.startsWith("    ") || line.startsWith("\t")) return true;
        if (index + 1 < lines.size() && lines.get(index + 1).trim().startsWith(":")) return true;
        return false;
    }

    private static boolean isHtmlBlockLine(String line) {
        if (line == null) return false;
        if (leadingSpaces(line) > 3) return false;
        String t = line.trim();
        if (!t.startsWith("<") || t.length() < 2) return false;
        if (t.startsWith("<!--") || t.startsWith("<![CDATA[") || t.matches("(?i)^<![A-Z].*")) return true;
        if (t.startsWith("<?")) return true;
        Matcher m = Pattern.compile("^</?([A-Za-z][A-Za-z0-9-]*)\\b.*").matcher(t);
        if (!m.matches()) return false;
        String name = m.group(1).toLowerCase(Locale.ROOT);
        if (!isAllowedHtmlElement(name)) return false;
        return !isInlineHtmlElement(name) || name.equals("hr") || name.equals("br");
    }

    private static boolean isSetextUnderline(String line) {
        if (line == null) return false;
        String t = line.trim();
        return t.matches("=+\\s*") || t.matches("-+\\s*");
    }

    private static boolean isTableStart(List<String> lines, int i) {
        if (lines == null || i < 0 || i + 1 >= lines.size()) return false;
        List<String> header = splitTableRow(lines.get(i));
        return header.size() > 1 && parseTableAlignments(lines.get(i + 1), header.size()) != null;
    }

    private static List<String> splitTableRow(String line) {
        ArrayList<String> cells = new ArrayList<>();
        if (line == null) return cells;
        String s = line.trim();
        if (s.startsWith("|")) s = s.substring(1);
        if (s.endsWith("|")) s = s.substring(0, s.length() - 1);
        StringBuilder cell = new StringBuilder();
        boolean escape = false;
        char codeFence = 0;
        int codeFenceLen = 0;
        int activeCodeLen = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                cell.append(c);
                escape = false;
                continue;
            }
            if (c == '\\') {
                cell.append(c);
                escape = true;
                continue;
            }
            if (c == '`') {
                int j = i;
                while (j < s.length() && s.charAt(j) == '`') j++;
                int run = j - i;
                if (activeCodeLen == 0) {
                    codeFence = '`';
                    codeFenceLen = run;
                    activeCodeLen = run;
                } else if (codeFence == '`' && run == codeFenceLen) {
                    activeCodeLen = 0;
                }
                cell.append(s, i, j);
                i = j - 1;
                continue;
            }
            if (c == '|' && activeCodeLen == 0) {
                cells.add(cell.toString().trim());
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(cell.toString().trim());
        return cells;
    }

    private static List<String> parseTableAlignments(String separatorLine, int minColumns) {
        List<String> cells = splitTableRow(separatorLine);
        if (cells.size() < 2 || cells.size() < minColumns) return null;
        ArrayList<String> align = new ArrayList<>();
        for (String c : cells) {
            String s = c == null ? "" : c.trim();
            if (!s.matches(":?-{3,}:?")) return null;
            boolean left = s.startsWith(":");
            boolean right = s.endsWith(":");
            align.add(left && right ? "center" : (right ? "right" : "left"));
        }
        return align;
    }

    private static boolean isBlockQuoteLine(String line) {
        if (line == null) return false;
        int i = 0;
        while (i < line.length() && i < 4 && line.charAt(i) == ' ') i++;
        return i <= 3 && i < line.length() && line.charAt(i) == '>';
    }

    private static String stripBlockQuoteMarker(String line) {
        if (line == null) return "";
        int i = 0;
        while (i < line.length() && i < 4 && line.charAt(i) == ' ') i++;
        if (i < line.length() && line.charAt(i) == '>') i++;
        if (i < line.length() && line.charAt(i) == ' ') i++;
        return line.substring(Math.min(i, line.length()));
    }

    private static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder out = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&': out.append("&amp;"); break;
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&#39;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    private static String escapeAttr(String text) {
        return escapeHtml(text).replace("\n", " ").replace("\r", " ");
    }

    private static boolean safeUrl(String url) {
        if (url == null) return false;
        String u = url.trim();
        if (u.isEmpty()) return false;
        String lower = u.toLowerCase(Locale.ROOT);
        if (lower.startsWith("javascript:") || lower.startsWith("vbscript:") || lower.startsWith("data:")) return false;
        if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("mailto:") || lower.startsWith("tel:") || lower.startsWith("ftp:") || lower.startsWith("#")) return true;
        return !hasExplicitScheme(u);
    }

    private static boolean safeImageUrl(String url) {
        if (url == null) return false;
        String u = url.trim();
        if (u.isEmpty()) return false;
        String lower = u.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) return true;
        if (lower.startsWith("data:image/")) {
            return lower.matches("data:image/(png|jpe?g|gif|webp);base64,[a-z0-9+/=\\s]+$");
        }
        return !hasExplicitScheme(u);
    }

    private static boolean hasExplicitScheme(String url) {
        int colon = url.indexOf(':');
        if (colon <= 0) return false;
        int slash = url.indexOf('/');
        int question = url.indexOf('?');
        int hash = url.indexOf('#');
        int firstBreak = url.length();
        if (slash >= 0) firstBreak = Math.min(firstBreak, slash);
        if (question >= 0) firstBreak = Math.min(firstBreak, question);
        if (hash >= 0) firstBreak = Math.min(firstBreak, hash);
        if (colon > firstBreak) return false;
        return url.substring(0, colon).matches("[A-Za-z][A-Za-z0-9+.-]*");
    }

    private static String normalizeRefId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static boolean isEscapable(char c) {
        return "\\`*_{}[]()#+-.!|>~^=:".indexOf(c) >= 0;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static String unescapeMarkdown(String text) {
        if (text == null || text.indexOf('\\') < 0) return text == null ? "" : text;
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length() && isEscapable(text.charAt(i + 1))) {
                out.append(text.charAt(i + 1));
                i++;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static LinkParts parseLinkParts(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return new LinkParts("", "");
        String url;
        String rest;
        if (s.startsWith("<")) {
            int end = s.indexOf('>');
            if (end <= 0) return new LinkParts(unescapeMarkdown(s), "");
            url = s.substring(1, end).trim();
            rest = s.substring(end + 1).trim();
        } else {
            int split = -1;
            boolean escape = false;
            int parenDepth = 0;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (c == '(') parenDepth++;
                else if (c == ')' && parenDepth > 0) parenDepth--;
                else if (Character.isWhitespace(c) && parenDepth == 0) { split = i; break; }
            }
            if (split < 0) {
                url = s;
                rest = "";
            } else {
                url = s.substring(0, split).trim();
                rest = s.substring(split).trim();
            }
        }
        String title = parseOptionalTitle(rest);
        return new LinkParts(unescapeMarkdown(url), unescapeMarkdown(title));
    }

    private static String parseOptionalTitle(String rest) {
        String r = rest == null ? "" : rest.trim();
        if (r.length() < 2) return "";
        char open = r.charAt(0);
        char close = open == '(' ? ')' : open;
        if (open != '"' && open != '\'' && open != '(') return "";
        if (r.charAt(r.length() - 1) != close) return "";
        return r.substring(1, r.length() - 1);
    }

    private static final class HeadingText {
        final String text;
        final String id;
        HeadingText(String text, String id) { this.text = text == null ? "" : text; this.id = id == null ? "" : id; }
    }

    private static final class LinkParts {
        final String url;
        final String title;
        LinkParts(String url, String title) { this.url = url == null ? "" : url; this.title = title == null ? "" : title; }
    }

    private static final class LinkRef {
        final String url;
        final String title;
        LinkRef(String url, String title) { this.url = url == null ? "" : url; this.title = title == null ? "" : title; }
    }

    private static final class Fence {
        final char ch;
        final int length;
        final String info;
        Fence(char ch, int length, String info) { this.ch = ch; this.length = length; this.info = info == null ? "" : info; }
    }

    private static final class ListMarker {
        final int indent;
        final int contentIndent;
        final boolean ordered;
        final int number;
        final String marker;
        final String content;
        ListMarker(int indent, int contentIndent, boolean ordered, int number, String marker, String content) {
            this.indent = indent;
            this.contentIndent = Math.max(contentIndent, indent + 1);
            this.ordered = ordered;
            this.number = number;
            this.marker = marker == null ? "" : marker;
            this.content = content == null ? "" : content;
        }
    }

    private static final class CodeSpan {
        final String code;
        final int end;
        CodeSpan(String code, int end) { this.code = code == null ? "" : code; this.end = end; }
    }

    private static final class MarkdownRenderer {
        private final ArrayList<String> lines = new ArrayList<>();
        private final LinkedHashMap<String, LinkRef> refs = new LinkedHashMap<>();
        private final LinkedHashMap<String, ArrayList<String>> footnotes = new LinkedHashMap<>();
        private int inlineFootnoteCounter = 0;

        MarkdownRenderer(String raw) {
            String[] source = (raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n')).split("\n", -1);
            Pattern footPattern = Pattern.compile("^\\s{0,3}\\[\\^([^\\]]+)\\]:\\s*(.*)$");
            Pattern refPattern = Pattern.compile("^\\s{0,3}\\[([^\\]^][^\\]]*)\\]:\\s*(.*)$");
            int i = 0;
            while (i < source.length) {
                String line = source[i] == null ? "" : source[i];
                Matcher fm = footPattern.matcher(line);
                if (fm.matches()) {
                    String id = normalizeRefId(fm.group(1));
                    ArrayList<String> body = new ArrayList<>();
                    body.add(fm.group(2));
                    i++;
                    while (i < source.length) {
                        String next = source[i] == null ? "" : source[i];
                        if (isBlank(next)) {
                            body.add("");
                            i++;
                            continue;
                        }
                        if (hasIndentAtLeast(next, 4)) {
                            body.add(stripIndent(next, 4));
                            i++;
                            continue;
                        }
                        break;
                    }
                    footnotes.put(id, body);
                    continue;
                }
                Matcher rm = refPattern.matcher(line);
                if (rm.matches()) {
                    LinkParts parts = parseLinkParts(rm.group(2));
                    if (!parts.url.isEmpty()) {
                        String title = parts.title;
                        int extra = 0;
                        if (title.isEmpty() && i + 1 < source.length) {
                            String candidate = source[i + 1] == null ? "" : source[i + 1].trim();
                            String nextTitle = parseOptionalTitle(candidate);
                            if (!nextTitle.isEmpty()) {
                                title = unescapeMarkdown(nextTitle);
                                extra = 1;
                            }
                        }
                        refs.put(normalizeRefId(rm.group(1)), new LinkRef(parts.url, title));
                        i += 1 + extra;
                        continue;
                    }
                }
                lines.add(line);
                i++;
            }
        }

        String render() {
            StringBuilder out = new StringBuilder();
            renderBlocks(out, lines, 0, lines.size());
            if (!footnotes.isEmpty()) {
                out.append("<section class=\"footnotes\"><ol>");
                for (Map.Entry<String, ArrayList<String>> e : footnotes.entrySet()) {
                    out.append("<li id=\"fn-").append(escapeAttr(e.getKey())).append("\">");
                    StringBuilder body = new StringBuilder();
                    renderBlocks(body, e.getValue(), 0, e.getValue().size());
                    if (body.length() == 0 && !e.getValue().isEmpty()) body.append(renderInline(e.getValue().get(0)));
                    out.append(body);
                    out.append("<a class=\"footnote-backref\" href=\"#fnref-").append(escapeAttr(e.getKey())).append("\">↩</a>");
                    out.append("</li>");
                }
                out.append("</ol></section>");
            }
            return out.toString();
        }

        private void renderBlocks(StringBuilder out, List<String> src, int start, int end) {
            int i = start;
            while (i < end) {
                String line = src.get(i);
                if (isBlank(line)) { i++; continue; }

                if (i == 0 && line.trim().equals("---")) {
                    int j = i + 1;
                    while (j < end && !src.get(j).trim().equals("---")) j++;
                    if (j < end) {
                        StringBuilder yaml = new StringBuilder();
                        for (int k = i + 1; k < j; k++) yaml.append(src.get(k)).append('\n');
                        out.append("<pre class=\"frontmatter\"><code>").append(escapeHtml(yaml.toString())).append("</code></pre>");
                        i = j + 1;
                        continue;
                    }
                }

                if (isFenceStart(line)) {
                    i = renderFencedCode(out, src, i, end);
                    continue;
                }

                String heading = headingLineContent(line);
                if (heading != null) {
                    int nl = heading.indexOf('\n');
                    int lastNl = heading.lastIndexOf('\n');
                    int level = Integer.parseInt(heading.substring(0, nl));
                    String headingText = lastNl > nl ? heading.substring(nl + 1, lastNl) : heading.substring(nl + 1);
                    String headingId = lastNl > nl ? heading.substring(lastNl + 1) : "";
                    out.append("<h").append(level);
                    if (!headingId.isEmpty()) out.append(" id=\"").append(escapeAttr(headingId)).append("\"");
                    out.append('>').append(renderInline(headingText)).append("</h").append(level).append('>');
                    i++;
                    continue;
                }

                if (i + 1 < end && !isBlank(line) && isSetextUnderline(src.get(i + 1)) && !isTableStart(src, i)) {
                    int level = src.get(i + 1).trim().startsWith("=") ? 1 : 2;
                    HeadingText setextHeading = extractExplicitHeadingId(line.trim());
                    out.append("<h").append(level);
                    if (!setextHeading.id.isEmpty()) out.append(" id=\"").append(escapeAttr(setextHeading.id)).append("\"");
                    out.append('>').append(renderInline(setextHeading.text)).append("</h").append(level).append('>');
                    i += 2;
                    continue;
                }

                if (isHr(line)) {
                    out.append("<hr>");
                    i++;
                    continue;
                }

                if (isHtmlBlockLine(line)) {
                    i = renderHtmlBlock(out, src, i, end);
                    continue;
                }

                if (isTableStart(src, i)) {
                    i = renderTable(out, src, i, end);
                    continue;
                }

                if (isBlockQuoteLine(line)) {
                    i = renderBlockquote(out, src, i, end);
                    continue;
                }

                if (isListLine(line)) {
                    i = renderList(out, src, i, end);
                    continue;
                }

                if (line.startsWith("    ") || line.startsWith("\t")) {
                    i = renderIndentedCode(out, src, i, end);
                    continue;
                }

                if (i + 1 < end && src.get(i + 1).trim().startsWith(":")) {
                    i = renderDefinitionList(out, src, i, end);
                    continue;
                }

                i = renderParagraph(out, src, i, end);
            }
        }

        private int renderHtmlBlock(StringBuilder out, List<String> src, int i, int end) {
            StringBuilder html = new StringBuilder();
            int j = i;
            while (j < end && !isBlank(src.get(j))) {
                if (j > i && isBlockStart(src, j) && !isHtmlBlockLine(src.get(j))) break;
                if (html.length() > 0) html.append('\n');
                html.append(src.get(j).trim());
                j++;
            }
            out.append(sanitizeRenderedHtml(html.toString()));
            return j;
        }

        private int renderFencedCode(StringBuilder out, List<String> src, int i, int end) {
            Fence fence = parseFenceStart(src.get(i));
            if (fence == null) return renderParagraph(out, src, i, end);
            String lang = fence.info;
            int firstSpace = lang.indexOf(' ');
            if (firstSpace >= 0) lang = lang.substring(0, firstSpace);
            StringBuilder code = new StringBuilder();
            int j = i + 1;
            while (j < end) {
                String line = src.get(j);
                if (isFenceClose(line, fence)) { j++; break; }
                code.append(line);
                if (j < end - 1) code.append('\n');
                j++;
            }
            out.append("<pre><code");
            if (!lang.isEmpty()) out.append(" class=\"language-").append(escapeAttr(lang.replaceAll("\\s+", "-"))).append("\"");
            out.append('>').append(escapeHtml(code.toString())).append("</code></pre>");
            return j;
        }

        private int renderIndentedCode(StringBuilder out, List<String> src, int i, int end) {
            StringBuilder code = new StringBuilder();
            int j = i;
            while (j < end) {
                String line = src.get(j);
                if (line.startsWith("    ")) code.append(line.substring(4));
                else if (line.startsWith("\t")) code.append(line.substring(1));
                else if (isBlank(line)) code.append('\n');
                else break;
                if (j < end - 1) code.append('\n');
                j++;
            }
            out.append("<pre><code>").append(escapeHtml(code.toString())).append("</code></pre>");
            return j;
        }

        private int renderTable(StringBuilder out, List<String> src, int i, int end) {
            List<String> header = splitTableRow(src.get(i));
            List<String> align = parseTableAlignments(src.get(i + 1), header.size());
            int cols = Math.max(header.size(), align == null ? 0 : align.size());
            out.append("<table><thead><tr>");
            for (int c = 0; c < cols; c++) {
                String a = align != null && c < align.size() ? align.get(c) : "left";
                out.append("<th style=\"text-align:").append(a).append("\">").append(renderInline(c < header.size() ? header.get(c) : "")).append("</th>");
            }
            out.append("</tr></thead><tbody>");
            int j = i + 2;
            while (j < end && !isBlank(src.get(j)) && splitTableRow(src.get(j)).size() > 1) {
                if (parseTableAlignments(src.get(j), cols) != null) break;
                List<String> row = splitTableRow(src.get(j));
                out.append("<tr>");
                for (int c = 0; c < cols; c++) {
                    String a = align != null && c < align.size() ? align.get(c) : "left";
                    out.append("<td style=\"text-align:").append(a).append("\">").append(renderInline(c < row.size() ? row.get(c) : "")).append("</td>");
                }
                out.append("</tr>");
                j++;
            }
            out.append("</tbody></table>");
            return j;
        }

        private int renderBlockquote(StringBuilder out, List<String> src, int i, int end) {
            ArrayList<String> quote = new ArrayList<>();
            int j = i;
            boolean consumed = false;
            boolean afterBlank = false;
            while (j < end) {
                String line = src.get(j);
                if (isBlockQuoteLine(line)) {
                    quote.add(stripBlockQuoteMarker(line));
                    consumed = true;
                    afterBlank = false;
                    j++;
                    continue;
                }
                if (consumed && isBlank(line)) {
                    quote.add("");
                    afterBlank = true;
                    j++;
                    continue;
                }
                if (consumed && !afterBlank && !isBlockStart(src, j)) {
                    quote.add(line);
                    j++;
                    continue;
                }
                break;
            }
            String alert = "";
            if (!quote.isEmpty()) {
                Matcher m = Pattern.compile("^\\s*\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)\\]\\s*$", Pattern.CASE_INSENSITIVE).matcher(quote.get(0));
                if (m.matches()) {
                    alert = m.group(1).toLowerCase(Locale.ROOT);
                    quote.remove(0);
                    while (!quote.isEmpty() && isBlank(quote.get(0))) quote.remove(0);
                }
            }
            out.append("<blockquote");
            if (!alert.isEmpty()) out.append(" class=\"md-alert md-alert-").append(escapeAttr(alert)).append("\"");
            out.append('>');
            if (!alert.isEmpty()) out.append("<div class=\"md-alert-title\">").append(alertLabel(alert)).append("</div>");
            renderBlocks(out, quote, 0, quote.size());
            out.append("</blockquote>");
            return j;
        }

        private String alertLabel(String alert) {
            if ("tip".equals(alert)) return "Tipp";
            if ("important".equals(alert)) return "Wichtig";
            if ("warning".equals(alert)) return "Warnung";
            if ("caution".equals(alert)) return "Vorsicht";
            return "Hinweis";
        }

        private int renderList(StringBuilder out, List<String> src, int i, int end) {
            ListMarker first = parseListMarker(src.get(i));
            if (first == null) return i + 1;
            boolean ordered = first.ordered;
            String tag = ordered ? "ol" : "ul";
            int listIndent = first.indent;
            out.append('<').append(tag);
            if (ordered && first.number != 1) out.append(" start=\"").append(first.number).append("\"");
            out.append('>');
            int j = i;
            while (j < end) {
                ListMarker marker = parseListMarker(src.get(j));
                if (marker == null || marker.indent != listIndent || marker.ordered != ordered) break;
                ArrayList<String> itemLines = new ArrayList<>();
                String firstContent = marker.content;
                String taskHtml = "";
                Matcher taskM = Pattern.compile("^\\[([ xX])\\](?:\\s+(.*))?$").matcher(firstContent);
                if (taskM.matches()) {
                    boolean done = taskM.group(1).trim().equalsIgnoreCase("x");
                    taskHtml = "<input class=\"task-checkbox\" type=\"checkbox\" disabled" + (done ? " checked" : "") + "><span class=\"task " + (done ? "done" : "open") + "\">" + (done ? "☑" : "☐") + "</span>";
                    firstContent = taskM.group(2) == null ? "" : taskM.group(2);
                }
                itemLines.add(firstContent);
                int contentIndent = marker.contentIndent;
                boolean sawBlank = false;
                boolean lastWasBlank = false;
                j++;
                while (j < end) {
                    String line = src.get(j);
                    ListMarker next = parseListMarker(line);
                    if (next != null && next.indent == listIndent && next.ordered == ordered) break;
                    if (next != null && next.indent < listIndent) break;
                    if (isBlank(line)) {
                        itemLines.add("");
                        sawBlank = true;
                        lastWasBlank = true;
                        j++;
                        continue;
                    }
                    if (lastWasBlank && next == null && leadingSpaces(line) < contentIndent) break;
                    itemLines.add(stripListContinuation(line, contentIndent));
                    lastWasBlank = false;
                    j++;
                }
                while (!itemLines.isEmpty() && isBlank(itemLines.get(itemLines.size() - 1))) itemLines.remove(itemLines.size() - 1);
                sawBlank = false;
                for (String itemLine : itemLines) {
                    if (isBlank(itemLine)) { sawBlank = true; break; }
                }
                String itemHtml = renderBlocksToString(itemLines);
                if (!sawBlank) itemHtml = unwrapFirstParagraph(itemHtml);
                if (!taskHtml.isEmpty()) itemHtml = taskHtml + itemHtml;
                out.append("<li>").append(itemHtml).append("</li>");
            }
            out.append("</").append(tag).append('>');
            return j;
        }

        private String stripListContinuation(String line, int contentIndent) {
            if (line == null) return "";
            if (line.startsWith("\t")) return line.substring(1);
            int remove = Math.min(leadingSpaces(line), Math.max(0, contentIndent));
            return line.substring(remove);
        }

        private String renderBlocksToString(List<String> blockLines) {
            StringBuilder b = new StringBuilder();
            renderBlocks(b, blockLines, 0, blockLines.size());
            return b.toString();
        }

        private String unwrapFirstParagraph(String html) {
            if (html == null || !html.startsWith("<p>")) return html == null ? "" : html;
            int close = html.indexOf("</p>");
            if (close < 0) return html;
            return html.substring(3, close) + html.substring(close + 4);
        }

        private int renderDefinitionList(StringBuilder out, List<String> src, int i, int end) {
            out.append("<dl>");
            int j = i;
            while (j + 1 < end && !isBlank(src.get(j)) && src.get(j + 1).trim().startsWith(":")) {
                String term = src.get(j).trim();
                out.append("<dt>").append(renderInline(term)).append("</dt>");
                while (j + 1 < end && src.get(j + 1).trim().startsWith(":")) {
                    String def = src.get(j + 1).trim().substring(1).trim();
                    out.append("<dd>").append(renderInline(def)).append("</dd>");
                    j++;
                }
                j++;
                while (j < end && isBlank(src.get(j))) j++;
            }
            out.append("</dl>");
            return j;
        }

        private int renderParagraph(StringBuilder out, List<String> src, int i, int end) {
            StringBuilder para = new StringBuilder();
            int j = i;
            boolean previousHardBreak = false;
            while (j < end && !isBlank(src.get(j))) {
                if (j > i && isBlockStart(src, j)) break;
                String line = src.get(j);
                boolean hardBreak = line.endsWith("  ") || line.endsWith("\\");
                String text = line;
                if (line.endsWith("\\")) text = line.substring(0, line.length() - 1);
                else if (line.endsWith("  ")) text = line.replaceFirst("[ \\t]+$", "");
                text = text.trim();
                if (para.length() > 0 && !previousHardBreak) para.append(' ');
                para.append(text);
                if (hardBreak) para.append(HARD_BREAK);
                previousHardBreak = hardBreak;
                j++;
            }
            out.append("<p>").append(renderInlineWithHardBreaks(para.toString())).append("</p>");
            return j;
        }

        private String renderInlineWithHardBreaks(String text) {
            String[] parts = (text == null ? "" : text).split(Pattern.quote(HARD_BREAK), -1);
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) out.append("<br>");
                out.append(renderInline(parts[i]));
            }
            return out.toString();
        }

        private String renderInline(String text) {
            if (text == null || text.isEmpty()) return "";
            StringBuilder out = new StringBuilder(text.length() + 32);
            int i = 0;
            while (i < text.length()) {
                char c = text.charAt(i);
                if (c == '\\') {
                    if (i + 1 < text.length() && isEscapable(text.charAt(i + 1))) {
                        out.append(escapeHtml(String.valueOf(text.charAt(i + 1))));
                        i += 2;
                    } else {
                        out.append("\\");
                        i++;
                    }
                    continue;
                }
                if (c == '&') {
                    int entityEnd = entityEnd(text, i);
                    if (entityEnd > i) {
                        out.append(text, i, entityEnd + 1);
                        i = entityEnd + 1;
                    } else {
                        out.append("&amp;");
                        i++;
                    }
                    continue;
                }
                if (c == '`') {
                    CodeSpan span = parseCodeSpan(text, i);
                    if (span != null) {
                        out.append("<code>").append(escapeHtml(span.code)).append("</code>");
                        i = span.end;
                        continue;
                    }
                }
                if (startsWith(text, i, "^[")) {
                    int close = findMatchingBracket(text, i + 1);
                    if (close > i + 2) {
                        String id = "inline-" + (++inlineFootnoteCounter);
                        ArrayList<String> body = new ArrayList<>();
                        body.add(text.substring(i + 2, close));
                        footnotes.put(id, body);
                        out.append("<sup id=\"fnref-").append(escapeAttr(id)).append("\"><a href=\"#fn-").append(escapeAttr(id)).append("\">[").append(inlineFootnoteCounter).append("]</a></sup>");
                        i = close + 1;
                        continue;
                    }
                }
                if (c == '$') {
                    CodeSpan math = parseMathSpan(text, i);
                    if (math != null) {
                        out.append("<span class=\"math\">").append(escapeHtml(math.code)).append("</span>");
                        i = math.end;
                        continue;
                    }
                }
                if (startsWith(text, i, "![")) {
                    LinkToken token = parseLinkToken(text, i, true);
                    if (token != null) {
                        out.append(renderImageToken(token));
                        i = token.endIndex;
                        continue;
                    }
                }
                if (startsWith(text, i, "[^")) {
                    int close = findMatchingBracket(text, i + 1);
                    if (close > i + 2) {
                        String id = normalizeRefId(text.substring(i + 2, close));
                        out.append("<sup id=\"fnref-").append(escapeAttr(id)).append("\"><a href=\"#fn-").append(escapeAttr(id)).append("\">[").append(escapeHtml(id)).append("]</a></sup>");
                        i = close + 1;
                        continue;
                    }
                }
                if (c == '[') {
                    LinkToken token = parseLinkToken(text, i, false);
                    if (token != null) {
                        String labelHtml = renderInline(token.label);
                        if (safeUrl(token.url)) {
                            out.append("<a href=\"").append(escapeAttr(token.url)).append("\"");
                            if (!token.title.isEmpty()) out.append(" title=\"").append(escapeAttr(token.title)).append("\"");
                            out.append('>').append(labelHtml).append("</a>");
                        } else {
                            out.append(labelHtml);
                        }
                        i = token.endIndex;
                        continue;
                    }
                }
                if (c == '<') {
                    int close = text.indexOf('>', i + 1);
                    if (close > i) {
                        String inside = text.substring(i + 1, close).trim();
                        if (inside.matches("(?i)https?://[^\\s<>]+") || inside.matches("(?i)mailto:[^\\s<>]+")) {
                            out.append("<a href=\"").append(escapeAttr(inside)).append("\">").append(escapeHtml(inside)).append("</a>");
                            i = close + 1;
                            continue;
                        }
                        if (inside.matches("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}")) {
                            String href = "mailto:" + inside;
                            out.append("<a href=\"").append(escapeAttr(href)).append("\">").append(escapeHtml(inside)).append("</a>");
                            i = close + 1;
                            continue;
                        }
                        String safeTag = sanitizeInlineHtmlTag(inside);
                        if (safeTag != null) {
                            out.append(safeTag);
                            i = close + 1;
                            continue;
                        }
                    }
                }
                BareLink bare = parseBareLink(text, i);
                if (bare != null) {
                    out.append("<a href=\"").append(escapeAttr(bare.href)).append("\">").append(escapeHtml(bare.label)).append("</a>");
                    i = bare.end;
                    continue;
                }
                BareLink email = parseBareEmail(text, i);
                if (email != null) {
                    out.append("<a href=\"").append(escapeAttr(email.href)).append("\">").append(escapeHtml(email.label)).append("</a>");
                    i = email.end;
                    continue;
                }
                if (startsWith(text, i, "***") || startsWith(text, i, "___")) {
                    String token = text.substring(i, i + 3);
                    int close = findClosingDelimiter(text, i, token);
                    if (close > i + 3) {
                        out.append("<strong><em>").append(renderInline(text.substring(i + 3, close))).append("</em></strong>");
                        i = close + 3;
                        continue;
                    }
                }
                if (startsWith(text, i, "**") || startsWith(text, i, "__")) {
                    String token = text.substring(i, i + 2);
                    int close = findClosingDelimiter(text, i, token);
                    if (close > i + 2) {
                        out.append("<strong>").append(renderInline(text.substring(i + 2, close))).append("</strong>");
                        i = close + 2;
                        continue;
                    }
                }
                if (startsWith(text, i, "~~")) {
                    int close = findClosingDelimiter(text, i, "~~");
                    if (close > i + 2) {
                        out.append("<del>").append(renderInline(text.substring(i + 2, close))).append("</del>");
                        i = close + 2;
                        continue;
                    }
                }
                if (startsWith(text, i, "==")) {
                    int close = findClosingDelimiter(text, i, "==");
                    if (close > i + 2) {
                        out.append("<mark>").append(renderInline(text.substring(i + 2, close))).append("</mark>");
                        i = close + 2;
                        continue;
                    }
                }
                if (startsWith(text, i, "++")) {
                    int close = findClosingDelimiter(text, i, "++");
                    if (close > i + 2) {
                        out.append("<ins>").append(renderInline(text.substring(i + 2, close))).append("</ins>");
                        i = close + 2;
                        continue;
                    }
                }
                if (c == '^' && !startsWith(text, i, "^[")) {
                    int close = findClosingDelimiter(text, i, "^");
                    if (close > i + 1) {
                        out.append("<sup>").append(renderInline(text.substring(i + 1, close))).append("</sup>");
                        i = close + 1;
                        continue;
                    }
                }
                if (c == '~' && !startsWith(text, i, "~~")) {
                    int close = findClosingDelimiter(text, i, "~");
                    if (close > i + 1) {
                        out.append("<sub>").append(renderInline(text.substring(i + 1, close))).append("</sub>");
                        i = close + 1;
                        continue;
                    }
                }
                if (c == '*' || c == '_') {
                    String token = String.valueOf(c);
                    int close = findClosingDelimiter(text, i, token);
                    if (close > i + 1) {
                        out.append("<em>").append(renderInline(text.substring(i + 1, close))).append("</em>");
                        i = close + 1;
                        continue;
                    }
                }
                out.append(escapeHtml(String.valueOf(c)));
                i++;
            }
            return out.toString();
        }

        private int entityEnd(String text, int start) {
            int semi = text.indexOf(';', start + 1);
            if (semi < 0 || semi - start > 16) return -1;
            String body = text.substring(start + 1, semi);
            if (body.matches("#[0-9]{1,7}") || body.matches("#x[0-9A-Fa-f]{1,6}") || body.matches("[A-Za-z][A-Za-z0-9]{1,31}")) return semi;
            return -1;
        }

        private CodeSpan parseCodeSpan(String text, int start) {
            int run = countRun(text, start, '`');
            int i = start + run;
            while (i < text.length()) {
                if (text.charAt(i) == '`') {
                    int closeRun = countRun(text, i, '`');
                    if (closeRun == run) {
                        String code = text.substring(start + run, i).replace('\n', ' ').replace('\r', ' ');
                        if (code.length() >= 2 && code.startsWith(" ") && code.endsWith(" ") && code.trim().length() > 0) code = code.substring(1, code.length() - 1);
                        return new CodeSpan(code, i + closeRun);
                    }
                    i += closeRun;
                } else {
                    i++;
                }
            }
            return null;
        }

        private CodeSpan parseMathSpan(String text, int start) {
            int run = startsWith(text, start, "$$") ? 2 : 1;
            int bodyStart = start + run;
            if (bodyStart >= text.length() || Character.isWhitespace(text.charAt(bodyStart))) return null;
            int i = bodyStart;
            while (i <= text.length() - run) {
                if (text.charAt(i) == '\\') { i += 2; continue; }
                if (run == 2 && startsWith(text, i, "$$")) {
                    String body = text.substring(bodyStart, i).trim();
                    if (!body.isEmpty()) return new CodeSpan(body, i + 2);
                    return null;
                }
                if (run == 1 && text.charAt(i) == '$') {
                    if (i > bodyStart && !Character.isWhitespace(text.charAt(i - 1))) return new CodeSpan(text.substring(bodyStart, i), i + 1);
                    return null;
                }
                i++;
            }
            return null;
        }

        private int countRun(String text, int start, char ch) {
            int i = start;
            while (i < text.length() && text.charAt(i) == ch) i++;
            return i - start;
        }

        private int findMatchingBracket(String text, int openBracket) {
            int depth = 0;
            boolean escape = false;
            for (int i = openBracket + 1; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (c == '`') {
                    CodeSpan span = parseCodeSpan(text, i);
                    if (span != null) { i = span.end - 1; continue; }
                }
                if (c == '[') depth++;
                else if (c == ']') {
                    if (depth == 0) return i;
                    depth--;
                }
            }
            return -1;
        }

        private LinkToken parseLinkToken(String text, int start, boolean image) {
            int bracket = image ? start + 1 : start;
            if (bracket >= text.length() || text.charAt(bracket) != '[') return null;
            int close = findMatchingBracket(text, bracket);
            if (close <= bracket) return null;
            String label = text.substring(bracket + 1, close);
            int after = close + 1;
            if (after < text.length() && text.charAt(after) == '(') {
                int end = findClosingParen(text, after + 1);
                if (end > after) {
                    LinkParts parts = parseLinkParts(text.substring(after + 1, end));
                    if (!parts.url.isEmpty()) return new LinkToken(label, parts.url, parts.title, end + 1);
                }
            }
            if (after < text.length() && text.charAt(after) == '[') {
                int refClose = text.indexOf(']', after + 1);
                if (refClose > after) {
                    String ref = text.substring(after + 1, refClose);
                    LinkRef link = refs.get(normalizeRefId(ref.isEmpty() ? label : ref));
                    if (link != null) return new LinkToken(label, link.url, link.title, refClose + 1);
                }
            }
            LinkRef shortcut = refs.get(normalizeRefId(label));
            if (shortcut != null) return new LinkToken(label, shortcut.url, shortcut.title, close + 1);
            return null;
        }

        private int findClosingParen(String text, int start) {
            int depth = 0;
            boolean escape = false;
            boolean inSingle = false;
            boolean inDouble = false;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (c == '\'' && !inDouble) { inSingle = !inSingle; continue; }
                if (c == '"' && !inSingle) { inDouble = !inDouble; continue; }
                if (inSingle || inDouble) continue;
                if (c == '(') depth++;
                else if (c == ')') {
                    if (depth == 0) return i;
                    depth--;
                }
            }
            return -1;
        }

        private String renderImageToken(LinkToken token) {
            String altPlain = token.label.replaceAll("[\\[\\]`*_~#]", "").trim();
            if (safeImageUrl(token.url)) {
                StringBuilder img = new StringBuilder();
                img.append("<img class=\"md-image\" src=\"").append(escapeAttr(token.url)).append("\" alt=\"").append(escapeAttr(altPlain)).append("\"");
                if (!token.title.isEmpty()) img.append(" title=\"").append(escapeAttr(token.title)).append("\"");
                img.append(">");
                return img.toString();
            }
            if (altPlain.isEmpty()) return "<span class=\"md-image-alt\">[Bild]</span>";
            return "<span class=\"md-image-alt\">[Bild: " + renderInline(token.label) + "]</span>";
        }

        private String sanitizeInlineHtmlTag(String inside) {
            if (inside == null || inside.isEmpty()) return null;
            String s = inside.trim();
            boolean closing = s.startsWith("/");
            String nameSource = closing ? s.substring(1).trim() : s;
            int end = 0;
            while (end < nameSource.length() && (Character.isLetterOrDigit(nameSource.charAt(end)) || nameSource.charAt(end) == '-')) end++;
            if (end <= 0) return null;
            String name = nameSource.substring(0, end).toLowerCase(Locale.ROOT);
            if (!isInlineHtmlElement(name)) return null;
            return sanitizeHtmlTag(inside);
        }

        private BareLink parseBareLink(String text, int start) {
            if (start > 0) {
                char prev = text.charAt(start - 1);
                if (Character.isLetterOrDigit(prev) || prev == '@') return null;
            }
            String lower = text.substring(start).toLowerCase(Locale.ROOT);
            boolean hasScheme = lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("ftp://");
            boolean hasWww = lower.startsWith("www.");
            if (!hasScheme && !hasWww) return null;
            int end = start;
            while (end < text.length()) {
                char c = text.charAt(end);
                if (Character.isWhitespace(c) || c == '<' || c == '"' || c == '\'') break;
                end++;
            }
            while (end > start && ".,;:!?".indexOf(text.charAt(end - 1)) >= 0) end--;
            while (end > start && text.charAt(end - 1) == ')' && countChar(text.substring(start, end), '(') < countChar(text.substring(start, end), ')')) end--;
            if (end <= start) return null;
            String label = text.substring(start, end);
            String href = hasWww ? "https://" + label : label;
            if (!safeUrl(href)) return null;
            return new BareLink(label, href, end);
        }

        private BareLink parseBareEmail(String text, int start) {
            if (text == null || start < 0 || start >= text.length()) return null;
            if (start > 0) {
                char prev = text.charAt(start - 1);
                if (Character.isLetterOrDigit(prev) || "._%+-@".indexOf(prev) >= 0) return null;
            }
            Matcher m = Pattern.compile("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}").matcher(text.substring(start));
            if (!m.lookingAt()) return null;
            int end = start + m.end();
            while (end > start && ".,;:!?".indexOf(text.charAt(end - 1)) >= 0) end--;
            if (end <= start) return null;
            String label = text.substring(start, end);
            return new BareLink(label, "mailto:" + label, end);
        }

        private int countChar(String s, char ch) {
            int n = 0;
            for (int i = 0; i < s.length(); i++) if (s.charAt(i) == ch) n++;
            return n;
        }

        private int findClosingDelimiter(String text, int start, String token) {
            if (!canOpenDelimiter(text, start, token)) return -1;
            int i = start + token.length();
            while (i <= text.length() - token.length()) {
                char c = text.charAt(i);
                if (c == '\\') { i += 2; continue; }
                if (c == '`') {
                    CodeSpan span = parseCodeSpan(text, i);
                    if (span != null) { i = span.end; continue; }
                }
                if (text.startsWith(token, i) && canCloseDelimiter(text, i, token)) return i;
                i++;
            }
            return -1;
        }

        private boolean canOpenDelimiter(String text, int start, String token) {
            int afterIndex = start + token.length();
            if (afterIndex >= text.length()) return false;
            char after = text.charAt(afterIndex);
            if (Character.isWhitespace(after)) return false;
            if (token.indexOf('_') >= 0 && start > 0 && isWordChar(text.charAt(start - 1)) && isWordChar(after)) return false;
            return true;
        }

        private boolean canCloseDelimiter(String text, int start, String token) {
            if (start <= 0) return false;
            char before = text.charAt(start - 1);
            if (Character.isWhitespace(before)) return false;
            int afterIndex = start + token.length();
            if (token.indexOf('_') >= 0 && afterIndex < text.length() && isWordChar(text.charAt(afterIndex)) && isWordChar(before)) return false;
            return true;
        }

        private boolean startsWith(String text, int index, String token) {
            return text != null && token != null && index >= 0 && index + token.length() <= text.length() && text.startsWith(token, index);
        }
    }

    private static final class BareLink {
        final String label;
        final String href;
        final int end;
        BareLink(String label, String href, int end) { this.label = label == null ? "" : label; this.href = href == null ? "" : href; this.end = end; }
    }

    private static final class LinkToken {
        final String label;
        final String url;
        final String title;
        final int endIndex;
        LinkToken(String label, String url, String title, int endIndex) {
            this.label = label == null ? "" : label;
            this.url = url == null ? "" : url;
            this.title = title == null ? "" : title;
            this.endIndex = endIndex;
        }
    }
}
