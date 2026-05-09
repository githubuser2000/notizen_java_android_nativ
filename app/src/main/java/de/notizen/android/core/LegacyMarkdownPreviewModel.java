package de.notizen.android.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final Pattern FENCED_CODE = Pattern.compile("(?m)^\\s{0,3}(```|~~~).*$");
    private static final Pattern UNORDERED_LIST = Pattern.compile("(?m)^\\s{0,24}[-+*]\\s+\\S.*$");
    private static final Pattern TASK_LIST = Pattern.compile("(?m)^\\s{0,24}[-+*]\\s+\\[[ xX]\\]\\s+\\S.*$");
    private static final Pattern ORDERED_LIST = Pattern.compile("(?m)^\\s{0,24}\\d{1,9}[.)]\\s+\\S.*$");
    private static final Pattern BLOCKQUOTE = Pattern.compile("(?m)^\\s{0,3}>\\s*\\S.*$");
    private static final Pattern TABLE_ROW = Pattern.compile("(?m)^\\s*\\|?.+\\|.+\\|?\\s*$");
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("(?m)^\\s*\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?\\s*$");
    private static final Pattern INLINE_STRONG = Pattern.compile("(?s)(\\*\\*|__)[^\\r\\n].*?\\1");
    private static final Pattern INLINE_EMPHASIS = Pattern.compile("(?s)(?<!\\*)\\*[^\\s*][^\\r\\n]*?\\*(?!\\*)|(?<!_)_[^\\s_][^\\r\\n]*?_(?!_)");
    private static final Pattern INLINE_CODE = Pattern.compile("`[^`\\r\\n]+`");
    private static final Pattern INLINE_STRIKE = Pattern.compile("~~[^~\\r\\n]+~~");
    private static final Pattern INLINE_LINK = Pattern.compile("!?\\[[^\\]\\r\\n]+\\]\\([^\\s)]+(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern REFERENCE_LINK = Pattern.compile("(?m)^\\s*\\[[^\\]\\r\\n]+\\]:\\s*\\S+.*$");
    private static final Pattern FOOTNOTE = Pattern.compile("(?m)^\\s*\\[\\^[^\\]\\r\\n]+\\]:\\s+.*$");
    private static final Pattern HR = Pattern.compile("(?m)^\\s{0,3}([-*_])(?:\\s*\\1){2,}\\s*$");
    private static final Pattern FRONT_MATTER = Pattern.compile("(?s)^\\s*---\\R.+?\\R---\\R");

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
        if (INLINE_EMPHASIS.matcher(text).find()) score += 1;
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
            if (splitTableRow(lines[i]).size() > 1 && parseTableAlignments(lines[i + 1], splitTableRow(lines[i]).size()) != null) return true;
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
        String fragment = markdownToHtmlFragment(rawText);
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
                + "ul,ol{margin:.55em 0 .55em 1.4em;padding-left:1.1em;}li{margin:.22em 0;}"
                + "hr{border:0;border-top:1px solid #cfd6df;margin:.9em 0;}dl{margin:.55em 0;}dt{font-weight:700;}dd{margin:0 0 .45em 1.25em;}"
                + ".task{font-family:monospace;margin-right:.35em;}.md-image-alt{color:#6b7280;font-style:italic;}.footnotes{border-top:1px solid #d8e0ea;margin-top:1em;padding-top:.4em;font-size:.92em;color:#374151;}"
                + "</style></head><body>" + fragment + "</body></html>";
    }

    public static String markdownToHtmlFragment(String rawText) {
        MarkdownRenderer renderer = new MarkdownRenderer(cleanRawText(rawText));
        return renderer.render();
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

    private static boolean isFenceStart(String line) {
        String t = line == null ? "" : line.trim();
        return t.startsWith("```") || t.startsWith("~~~");
    }

    private static String fenceToken(String line) {
        String t = line == null ? "" : line.trim();
        if (t.startsWith("```")) return "```";
        if (t.startsWith("~~~")) return "~~~";
        return "";
    }

    private static boolean isHr(String line) {
        return line != null && HR.matcher(line).matches();
    }

    private static String headingLineContent(String line) {
        String t = line == null ? "" : line.trim();
        Matcher m = Pattern.compile("^(#{1,6})\\s+(.+?)\\s*#*\\s*$").matcher(t);
        if (!m.matches()) return null;
        return m.group(1).length() + "\n" + m.group(2);
    }

    private static boolean isListLine(String line) {
        if (line == null) return false;
        return Pattern.compile("^\\s*(?:[-+*]|\\d{1,9}[.)])\\s+.+$").matcher(line).matches();
    }

    private static boolean isBlockStart(List<String> lines, int index) {
        if (index < 0 || index >= lines.size()) return false;
        String line = lines.get(index);
        if (isBlank(line)) return true;
        if (headingLineContent(line) != null || isFenceStart(line) || isHr(line) || isListLine(line)) return true;
        String trimmed = line.trim();
        if (trimmed.startsWith(">")) return true;
        if (index + 1 < lines.size() && isTableStart(lines, index)) return true;
        if (index + 1 < lines.size() && isSetextUnderline(lines.get(index + 1))) return true;
        if (line.startsWith("    ") || line.startsWith("\t")) return true;
        if (index + 1 < lines.size() && lines.get(index + 1).trim().startsWith(":")) return true;
        return false;
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
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                cell.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '|') {
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
        String u = url.trim().toLowerCase(Locale.ROOT);
        return u.startsWith("http://") || u.startsWith("https://") || u.startsWith("mailto:") || u.startsWith("tel:") || u.startsWith("#");
    }

    private static String normalizeRefId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static final class LinkRef {
        final String url;
        final String title;
        LinkRef(String url, String title) { this.url = url == null ? "" : url; this.title = title == null ? "" : title; }
    }

    private static final class MarkdownRenderer {
        private final ArrayList<String> lines = new ArrayList<>();
        private final LinkedHashMap<String, LinkRef> refs = new LinkedHashMap<>();
        private final LinkedHashMap<String, String> footnotes = new LinkedHashMap<>();

        MarkdownRenderer(String raw) {
            String[] source = (raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n')).split("\n", -1);
            Pattern refPattern = Pattern.compile("^\\s*\\[([^\\]^][^\\]]*)\\]:\\s*(\\S+)(?:\\s+(?:\"([^\"]*)\"|'([^']*)'|\\(([^)]*)\\)))?\\s*$");
            Pattern footPattern = Pattern.compile("^\\s*\\[\\^([^\\]]+)\\]:\\s*(.*)$");
            for (String line : source) {
                Matcher fm = footPattern.matcher(line == null ? "" : line);
                if (fm.matches()) {
                    footnotes.put(normalizeRefId(fm.group(1)), fm.group(2));
                    continue;
                }
                Matcher rm = refPattern.matcher(line == null ? "" : line);
                if (rm.matches()) {
                    String title = rm.group(3) != null ? rm.group(3) : (rm.group(4) != null ? rm.group(4) : (rm.group(5) == null ? "" : rm.group(5)));
                    refs.put(normalizeRefId(rm.group(1)), new LinkRef(rm.group(2), title));
                    continue;
                }
                lines.add(line == null ? "" : line);
            }
        }

        String render() {
            StringBuilder out = new StringBuilder();
            renderBlocks(out, lines, 0, lines.size());
            if (!footnotes.isEmpty()) {
                out.append("<section class=\"footnotes\"><ol>");
                for (Map.Entry<String, String> e : footnotes.entrySet()) {
                    out.append("<li id=\"fn-").append(escapeAttr(e.getKey())).append("\">")
                            .append(renderInline(e.getValue())).append("</li>");
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
                    int level = Integer.parseInt(heading.substring(0, nl));
                    out.append("<h").append(level).append('>').append(renderInline(heading.substring(nl + 1))).append("</h").append(level).append('>');
                    i++;
                    continue;
                }

                if (i + 1 < end && !isBlank(line) && isSetextUnderline(src.get(i + 1))) {
                    int level = src.get(i + 1).trim().startsWith("=") ? 1 : 2;
                    out.append("<h").append(level).append('>').append(renderInline(line.trim())).append("</h").append(level).append('>');
                    i += 2;
                    continue;
                }

                if (isHr(line)) {
                    out.append("<hr>");
                    i++;
                    continue;
                }

                if (isTableStart(src, i)) {
                    i = renderTable(out, src, i, end);
                    continue;
                }

                if (line.trim().startsWith(">")) {
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

        private int renderFencedCode(StringBuilder out, List<String> src, int i, int end) {
            String first = src.get(i).trim();
            String token = fenceToken(first);
            String lang = first.length() > token.length() ? first.substring(token.length()).trim() : "";
            StringBuilder code = new StringBuilder();
            int j = i + 1;
            while (j < end) {
                String line = src.get(j);
                if (line.trim().startsWith(token)) { j++; break; }
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
                String a = c < align.size() ? align.get(c) : "left";
                out.append("<th style=\"text-align:").append(a).append("\">").append(renderInline(c < header.size() ? header.get(c) : "")).append("</th>");
            }
            out.append("</tr></thead><tbody>");
            int j = i + 2;
            while (j < end && !isBlank(src.get(j)) && splitTableRow(src.get(j)).size() > 1) {
                if (parseTableAlignments(src.get(j), cols) != null) break;
                List<String> row = splitTableRow(src.get(j));
                out.append("<tr>");
                for (int c = 0; c < cols; c++) {
                    String a = c < align.size() ? align.get(c) : "left";
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
            while (j < end) {
                String line = src.get(j);
                if (isBlank(line)) {
                    quote.add("");
                    j++;
                    continue;
                }
                String t = line.trim();
                if (!t.startsWith(">")) break;
                String q = t.substring(1);
                if (q.startsWith(" ")) q = q.substring(1);
                quote.add(q);
                j++;
            }
            out.append("<blockquote>");
            renderBlocks(out, quote, 0, quote.size());
            out.append("</blockquote>");
            return j;
        }

        private int renderList(StringBuilder out, List<String> src, int i, int end) {
            boolean ordered = src.get(i).trim().matches("^\\d{1,9}[.)]\\s+.*$");
            Matcher startM = Pattern.compile("^\\s*(\\d{1,9})[.)]\\s+.*$").matcher(src.get(i));
            String tag = ordered ? "ol" : "ul";
            out.append('<').append(tag);
            if (ordered && startM.matches()) {
                int start = Integer.parseInt(startM.group(1));
                if (start != 1) out.append(" start=\"").append(start).append("\"");
            }
            out.append('>');
            int j = i;
            Pattern itemPattern = Pattern.compile("^(\\s*)([-+*]|\\d{1,9}[.)])\\s+(.*)$");
            while (j < end) {
                Matcher m = itemPattern.matcher(src.get(j));
                if (!m.matches()) break;
                boolean thisOrdered = m.group(2).matches("\\d{1,9}[.)]");
                if (thisOrdered != ordered) break;
                int indent = leadingSpaces(m.group(1));
                String content = m.group(3);
                String task = "";
                Matcher taskM = Pattern.compile("^\\[([ xX])\\]\\s+(.*)$").matcher(content);
                if (taskM.matches()) {
                    task = "<span class=\"task\">" + (taskM.group(1).trim().equalsIgnoreCase("x") ? "☑" : "☐") + "</span>";
                    content = taskM.group(2);
                }
                out.append("<li");
                if (indent > 0) out.append(" style=\"margin-left:").append(Math.min(4.0f, indent / 2.0f)).append("em\"");
                out.append('>').append(task).append(renderInline(content));
                j++;
                while (j < end && !isBlank(src.get(j)) && !itemPattern.matcher(src.get(j)).matches() && !isBlockStart(src, j)) {
                    out.append("<br>").append(renderInline(src.get(j).trim()));
                    j++;
                }
                out.append("</li>");
            }
            out.append("</").append(tag).append('>');
            return j;
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
            while (j < end && !isBlank(src.get(j))) {
                if (j > i && isBlockStart(src, j)) break;
                String line = src.get(j);
                boolean hardBreak = line.endsWith("  ") || line.endsWith("\\");
                String trimmed = hardBreak ? line.trim() : line.trim();
                if (para.length() > 0) para.append(hardBreak ? "\n" : " ");
                para.append(trimmed);
                if (hardBreak) para.append("<br>");
                j++;
            }
            out.append("<p>").append(renderInlineWithHardBreaks(para.toString())).append("</p>");
            return j;
        }

        private String renderInlineWithHardBreaks(String text) {
            String[] parts = (text == null ? "" : text).split("<br>", -1);
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
                if (c == '\\' && i + 1 < text.length()) {
                    out.append(escapeHtml(String.valueOf(text.charAt(i + 1))));
                    i += 2;
                    continue;
                }
                if (c == '`') {
                    int close = findClosingCodeTick(text, i);
                    if (close > i) {
                        out.append("<code>").append(escapeHtml(text.substring(i + 1, close))).append("</code>");
                        i = close + 1;
                        continue;
                    }
                }
                if (startsWith(text, i, "![")) {
                    int close = text.indexOf(']', i + 2);
                    int endUrl = close >= 0 && close + 1 < text.length() && text.charAt(close + 1) == '(' ? findClosingParen(text, close + 2) : -1;
                    if (endUrl > close) {
                        String alt = text.substring(i + 2, close);
                        if (!alt.trim().isEmpty()) out.append("<span class=\"md-image-alt\">[Bild: ").append(renderInline(alt)).append("]</span>");
                        i = endUrl + 1;
                        continue;
                    }
                }
                if (startsWith(text, i, "[^")) {
                    int close = text.indexOf(']', i + 2);
                    if (close > i + 2) {
                        String id = normalizeRefId(text.substring(i + 2, close));
                        out.append("<sup><a href=\"#fn-").append(escapeAttr(id)).append("\">[").append(escapeHtml(id)).append("]</a></sup>");
                        i = close + 1;
                        continue;
                    }
                }
                if (c == '[') {
                    LinkToken token = parseLinkToken(text, i);
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
                    }
                }
                if (startsWith(text, i, "***") || startsWith(text, i, "___")) {
                    String token = text.substring(i, i + 3);
                    int close = text.indexOf(token, i + 3);
                    if (close > i + 3) {
                        out.append("<strong><em>").append(renderInline(text.substring(i + 3, close))).append("</em></strong>");
                        i = close + 3;
                        continue;
                    }
                }
                if (startsWith(text, i, "**") || startsWith(text, i, "__")) {
                    String token = text.substring(i, i + 2);
                    int close = text.indexOf(token, i + 2);
                    if (close > i + 2) {
                        out.append("<strong>").append(renderInline(text.substring(i + 2, close))).append("</strong>");
                        i = close + 2;
                        continue;
                    }
                }
                if (startsWith(text, i, "~~")) {
                    int close = text.indexOf("~~", i + 2);
                    if (close > i + 2) {
                        out.append("<del>").append(renderInline(text.substring(i + 2, close))).append("</del>");
                        i = close + 2;
                        continue;
                    }
                }
                if (c == '*' || c == '_') {
                    int close = text.indexOf(c, i + 1);
                    if (close > i + 1 && !Character.isWhitespace(text.charAt(i + 1))) {
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

        private int findClosingCodeTick(String text, int start) {
            return text.indexOf('`', start + 1);
        }

        private int findClosingParen(String text, int start) {
            int depth = 0;
            boolean escape = false;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (escape) { escape = false; continue; }
                if (c == '\\') { escape = true; continue; }
                if (c == '(') depth++;
                else if (c == ')') {
                    if (depth == 0) return i;
                    depth--;
                }
            }
            return -1;
        }

        private LinkToken parseLinkToken(String text, int start) {
            int close = text.indexOf(']', start + 1);
            if (close <= start + 1) return null;
            String label = text.substring(start + 1, close);
            if (close + 1 < text.length() && text.charAt(close + 1) == '(') {
                int end = findClosingParen(text, close + 2);
                if (end > close) {
                    String dest = text.substring(close + 2, end).trim();
                    String title = "";
                    Matcher m = Pattern.compile("^(\\S+)(?:\\s+(?:\"([^\"]*)\"|'([^']*)'|\\(([^)]*)\\)))?$").matcher(dest);
                    String url = dest;
                    if (m.matches()) {
                        url = m.group(1);
                        title = m.group(2) != null ? m.group(2) : (m.group(3) != null ? m.group(3) : (m.group(4) == null ? "" : m.group(4)));
                    }
                    return new LinkToken(label, stripAngleUrl(url), title, end + 1);
                }
            }
            if (close + 1 < text.length() && text.charAt(close + 1) == '[') {
                int refClose = text.indexOf(']', close + 2);
                if (refClose > close + 1) {
                    String ref = text.substring(close + 2, refClose);
                    LinkRef link = refs.get(normalizeRefId(ref.isEmpty() ? label : ref));
                    if (link != null) return new LinkToken(label, link.url, link.title, refClose + 1);
                }
            }
            LinkRef shortcut = refs.get(normalizeRefId(label));
            if (shortcut != null) return new LinkToken(label, shortcut.url, shortcut.title, close + 1);
            return null;
        }

        private String stripAngleUrl(String url) {
            String u = url == null ? "" : url.trim();
            if (u.startsWith("<") && u.endsWith(">") && u.length() > 2) return u.substring(1, u.length() - 1);
            return u;
        }

        private boolean startsWith(String text, int index, String token) {
            return text != null && token != null && index >= 0 && index + token.length() <= text.length() && text.startsWith(token, index);
        }
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
