package de.notizen.android.markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.footnotes.FootnotesExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.alerts.AlertsExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Android/APK bridge for the real commonmark-java renderer.
 *
 * <p>This class intentionally lives outside de.notizen.android.core so the
 * portable core tests can still compile without external JARs. The core loads
 * this bridge reflectively when the APK build includes the CommonMark/GFM
 * dependencies.</p>
 */
public final class CommonmarkMarkdownRenderer {
    private static final String ENGINE_NAME = "commonmark-java 0.28.0/GFM + Notizen-Extras";
    private static final Pattern EXPLICIT_HEADING_ID = Pattern.compile("(?is)<h([1-6])([^>]*)>(.*?)\\s+\\{#([A-Za-z0-9_.:-]+)\\}\\s*</h\\1>");
    private static final Pattern HEADING_ID_ATTR = Pattern.compile("(?is)\\s+id\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]+)");
    private static final Pattern PARAGRAPH = Pattern.compile("(?is)<p>(.*?)</p>");
    private static final Pattern HTML_BR = Pattern.compile("(?i)<br\\s*/?>");
    private static final List<Extension> EXTENSIONS = buildExtensions();
    private static final Parser PARSER = Parser.builder()
            .extensions(EXTENSIONS)
            .build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder()
            .extensions(EXTENSIONS)
            .sanitizeUrls(true)
            .escapeHtml(false)
            .build();

    private CommonmarkMarkdownRenderer() {}

    public static String render(String markdown) {
        String prepared = preprocessExtendedMarkdown(markdown == null ? "" : markdown);
        Node document = PARSER.parse(prepared);
        String html = RENDERER.render(document);
        return postprocessExtendedHtml(html == null ? "" : html);
    }

    public static String engineName() {
        return ENGINE_NAME;
    }

    public static String healthCheck() {
        String html = render("A &amp; B\n\n"
                + "## Sinnvolle Badezusätze nur zur Linderung\n"
                + "| Ziel | Badezusatz | Einschätzung |\n"
                + "|---|---|---|\n"
                + "| Juckreiz beruhigen | **Kühles/lauwarmes Bad**, ggf. mit **kolloidalem Hafermehl** | Kann gereizte, juckende Haut beruhigen; tötet keine Milben. |\n\n"
                + "| A | B |\n|---|---|\n| `a|b` | A \\| B |\n\n"
                + "｜ Vollbreite Pipe ｜ Unicode-Trennzeile ｜\n｜－－－｜－－－｜\n｜ eins ｜ zwei ｜\n\n"
                + "- [x] Task\n\n~~strike~~\n\n> [!NOTE]\n> Hinweis\n\n## **Titel** {#anker}\n\n"
                + "Begriff\n: **Definition**\n\nH~2~O x^2^ ==mark== $a+b$\n\n$$\nc=d\n$$");
        String lower = html.toLowerCase(Locale.ROOT);
        boolean strike = html.contains("<s>") || html.contains("<del>");
        boolean entityDecodedAndEscapedAgain = html.contains("A &amp; B");
        String looseTable = postprocessExtendedHtml("<p>| Ziel | Badezusatz | Einschätzung |\n"
                + "|---|---|---|\n"
                + "| Juckreiz beruhigen | <strong>Bad</strong> | ok |</p>");
        if (!entityDecodedAndEscapedAgain
                || countOccurrences(lower, "<table") < 3
                || !html.contains("<code>a|b</code>")
                || !html.contains("A | B")
                || !html.contains("Kühles/lauwarmes Bad")
                || !html.contains("Juckreiz beruhigen")
                || !lower.contains("checkbox")
                || !strike
                || !lower.contains("alert")
                || !html.contains("<h2 id=\"anker\">")
                || !html.contains("<dl>")
                || !html.contains("<strong>Definition</strong>")
                || !html.contains("H<sub>2</sub>O")
                || !html.contains("x<sup>2</sup>")
                || !html.contains("<mark>mark</mark>")
                || !html.contains("<span class=\"math\">a+b</span>")
                || !html.contains("<div class=\"math\">c=d</div>")
                || !looseTable.contains("<table")
                || !looseTable.contains("<strong>Bad</strong>")) {
            throw new IllegalStateException("CommonMark/GFM health check failed: " + html + "\nloose=" + looseTable);
        }
        return ENGINE_NAME;
    }

    public static void main(String[] args) {
        if (args != null && args.length > 0 && "--health-check".equals(args[0])) {
            System.out.print(healthCheck());
            return;
        }
        String markdown = args == null || args.length == 0 ? "" : args[0];
        System.out.print(render(markdown));
    }

    private static List<Extension> buildExtensions() {
        ArrayList<Extension> extensions = new ArrayList<>();
        extensions.add(AutolinkExtension.create());
        extensions.add(StrikethroughExtension.create());
        extensions.add(TablesExtension.create());
        extensions.add(AlertsExtension.create());
        extensions.add(FootnotesExtension.builder().inlineFootnotes(true).build());
        extensions.add(HeadingAnchorExtension.create());
        extensions.add(InsExtension.create());
        extensions.add(TaskListItemsExtension.create());
        extensions.add(ImageAttributesExtension.create());
        extensions.add(YamlFrontMatterExtension.create());
        return Collections.unmodifiableList(extensions);
    }

    /**
     * commonmark-java intentionally implements CommonMark plus selected
     * extensions, not every popular Markdown-extra dialect.  The legacy Notizen
     * preview already supported a few small Markdown-extra constructs.  Keep
     * those constructs before the real parser so switching to commonmark-java
     * does not regress them.
     */
    private static String preprocessExtendedMarkdown(String markdown) {
        String[] lines = normalizeMarkdownInput(markdown == null ? "" : markdown).split("\n", -1);
        StringBuilder out = new StringBuilder(markdown == null ? 32 : markdown.length() + 96);
        boolean inFence = false;
        char fenceChar = 0;
        int fenceLength = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i];
            String emit;
            Fence fence = parseFenceStart(line);
            if (!inFence && fence != null) {
                inFence = true;
                fenceChar = fence.ch;
                fenceLength = fence.length;
                emit = line;
            } else if (inFence) {
                emit = line;
                if (isFenceClose(line, fenceChar, fenceLength)) {
                    inFence = false;
                    fenceChar = 0;
                    fenceLength = 0;
                }
            } else {
                TableBlock table = parseTableAt(lines, i);
                if (table != null) {
                    emit = htmlBlockForMarkdown(table.html);
                    i = table.endIndex;
                } else if (trimControl(line).equals("$$")) {
                    StringBuilder math = new StringBuilder();
                    int j = i + 1;
                    while (j < lines.length && !trimControl(lines[j]).equals("$$")) {
                        if (math.length() > 0) math.append('\n');
                        math.append(lines[j]);
                        j++;
                    }
                    if (j < lines.length) {
                        emit = htmlBlockForMarkdown("<div class=\"math\">" + escapeHtml(math.toString().trim()) + "</div>");
                        i = j;
                    } else {
                        emit = processInlineExtras(line);
                    }
                } else if (isIndentedCodeLine(line)) {
                    emit = line;
                } else {
                    emit = processInlineExtras(line);
                }
            }
            out.append(emit);
            if (i < lines.length - 1) out.append('\n');
        }
        return out.toString();
    }

    private static String htmlBlockForMarkdown(String html) {
        return "\n" + (html == null ? "" : html) + "\n";
    }

    private static String normalizeMarkdownInput(String text) {
        if (text == null || text.isEmpty()) return "";
        String out = text.replace("\r\n", "\n").replace('\r', '\n');
        out = out.replace('\u2028', '\n').replace('\u2029', '\n').replace('\u0085', '\n');
        out = out.replace('\u00a0', ' ').replace('\u2007', ' ').replace('\u202f', ' ');
        return out;
    }

    private static TableBlock parseTableAt(String[] lines, int index) {
        if (lines == null || index < 0 || index + 1 >= lines.length) return null;
        String headerLine = lines[index] == null ? "" : lines[index];
        List<String> header = splitTableRow(headerLine);
        if (header.size() < 1 || !hasUnescapedTablePipe(headerLine)) return null;
        List<String> align = parseTableAlignments(lines[index + 1], header.size());
        if (align == null) return null;

        int cols = Math.max(header.size(), align.size());
        StringBuilder html = new StringBuilder();
        html.append("<table><thead><tr>");
        for (int c = 0; c < cols; c++) {
            String a = c < align.size() ? align.get(c) : "left";
            html.append("<th style=\"text-align:").append(a).append("\">")
                    .append(renderInlineMarkdownFragment(c < header.size() ? header.get(c) : ""))
                    .append("</th>");
        }
        html.append("</tr></thead><tbody>");
        int j = index + 2;
        while (j < lines.length && !isBlank(lines[j]) && hasUnescapedTablePipe(lines[j])) {
            if (parseTableAlignments(lines[j], cols) != null) break;
            List<String> row = splitTableRow(lines[j]);
            html.append("<tr>");
            for (int c = 0; c < cols; c++) {
                String a = c < align.size() ? align.get(c) : "left";
                html.append("<td style=\"text-align:").append(a).append("\">")
                        .append(renderInlineMarkdownFragment(c < row.size() ? row.get(c) : ""))
                        .append("</td>");
            }
            html.append("</tr>");
            j++;
        }
        html.append("</tbody></table>");
        return new TableBlock(html.toString(), j - 1);
    }

    private static String renderInlineMarkdownFragment(String raw) {
        String prepared = processInlineExtras(raw == null ? "" : raw);
        String html = RENDERER.render(PARSER.parse(prepared)).trim();
        if (html.startsWith("<p>") && html.endsWith("</p>") && html.indexOf("</p>") == html.length() - 4) {
            return html.substring(3, html.length() - 4);
        }
        return html;
    }

    private static List<String> splitTableRow(String line) {
        ArrayList<String> cells = new ArrayList<>();
        if (line == null) return cells;
        String s = stripOuterTablePipes(trimTableSyntax(line));
        StringBuilder cell = new StringBuilder();
        boolean escape = false;
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
                if (activeCodeLen == 0) activeCodeLen = run;
                else if (run == activeCodeLen) activeCodeLen = 0;
                cell.append(s, i, j);
                i = j - 1;
                continue;
            }
            if (isTablePipeChar(c) && activeCodeLen == 0) {
                cells.add(trimTableSyntax(cell.toString()));
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(trimTableSyntax(cell.toString()));
        return cells;
    }

    private static boolean hasUnescapedTablePipe(String line) {
        if (line == null) return false;
        String s = trimTableSyntax(line);
        boolean escape = false;
        int activeCodeLen = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '`') {
                int j = i;
                while (j < s.length() && s.charAt(j) == '`') j++;
                int run = j - i;
                if (activeCodeLen == 0) activeCodeLen = run;
                else if (run == activeCodeLen) activeCodeLen = 0;
                i = j - 1;
                continue;
            }
            if (isTablePipeChar(c) && activeCodeLen == 0) return true;
        }
        return false;
    }

    private static List<String> parseTableAlignments(String separatorLine, int minColumns) {
        List<String> cells = splitTableRow(separatorLine);
        if (cells.size() < 1 || cells.size() < minColumns) return null;
        ArrayList<String> align = new ArrayList<>();
        for (String c : cells) {
            String s = normalizeTableSeparatorCell(c);
            if (s == null || !s.matches(":?-{3,}:?")) return null;
            boolean left = s.startsWith(":");
            boolean right = s.endsWith(":");
            align.add(left && right ? "center" : (right ? "right" : "left"));
        }
        return align;
    }

    private static String processInlineExtras(String text) {
        if (text == null || text.isEmpty()) return text == null ? "" : text;
        StringBuilder out = new StringBuilder(text.length() + 24);
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\') {
                if (i + 1 < text.length()) {
                    out.append(text, i, i + 2);
                    i += 2;
                } else {
                    out.append(c);
                    i++;
                }
                continue;
            }
            if (c == '`') {
                int end = findCodeSpanEnd(text, i);
                if (end > i) {
                    out.append(text, i, end);
                    i = end;
                } else {
                    out.append(c);
                    i++;
                }
                continue;
            }
            if (c == '<') {
                int tagEnd = findHtmlTagEnd(text, i + 1);
                if (tagEnd > i) {
                    out.append(text, i, tagEnd + 1);
                    i = tagEnd + 1;
                } else {
                    out.append(c);
                    i++;
                }
                continue;
            }
            if (startsDelimiter(text, i, "==")) {
                int end = findClosingDelimiter(text, i + 2, "==");
                if (end >= 0 && end > i + 2) {
                    out.append("<mark>").append(text, i + 2, end).append("</mark>");
                    i = end + 2;
                    continue;
                }
            }
            if (c == '~' && isSingleDelimiter(text, i, '~')) {
                int end = findClosingSingleDelimiter(text, i + 1, '~');
                if (end >= 0 && end > i + 1 && isTightInlineSpan(text, i + 1, end)) {
                    out.append("<sub>").append(text, i + 1, end).append("</sub>");
                    i = end + 1;
                    continue;
                }
            }
            if (c == '^' && isSingleDelimiter(text, i, '^')) {
                int end = findClosingSingleDelimiter(text, i + 1, '^');
                if (end >= 0 && end > i + 1 && isTightInlineSpan(text, i + 1, end)) {
                    out.append("<sup>").append(text, i + 1, end).append("</sup>");
                    i = end + 1;
                    continue;
                }
            }
            if (c == '$' && isSingleDelimiter(text, i, '$')) {
                int end = findClosingSingleDelimiter(text, i + 1, '$');
                if (end >= 0 && end > i + 1 && isTightInlineMath(text, i + 1, end)) {
                    out.append("<span class=\"math\">").append(escapeHtml(text.substring(i + 1, end))).append("</span>");
                    i = end + 1;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static String postprocessExtendedHtml(String html) {
        String out = applyExplicitHeadingIds(html == null ? "" : html);
        out = convertLooseTableParagraphs(out);
        out = convertDefinitionListParagraphs(out);
        return out;
    }

    private static String applyExplicitHeadingIds(String html) {
        Matcher m = EXPLICIT_HEADING_ID.matcher(html == null ? "" : html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String level = m.group(1);
            String attrs = m.group(2) == null ? "" : m.group(2);
            String body = m.group(3) == null ? "" : m.group(3).trim();
            String id = m.group(4) == null ? "" : m.group(4);
            String cleanAttrs = HEADING_ID_ATTR.matcher(attrs).replaceAll("").trim();
            String replacement = "<h" + level + (cleanAttrs.isEmpty() ? "" : " " + cleanAttrs) + " id=\"" + escapeAttr(id) + "\">" + body + "</h" + level + ">";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String convertLooseTableParagraphs(String html) {
        Matcher m = PARAGRAPH.matcher(html == null ? "" : html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String body = m.group(1) == null ? "" : m.group(1);
            String replacement = tableHtmlFromLooseParagraphBody(body);
            if (replacement == null) replacement = m.group(0);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String tableHtmlFromLooseParagraphBody(String body) {
        if (body == null || !containsTablePipeChar(body)) return null;
        String normalized = normalizeHtmlParagraphBreaks(body);
        String[] lines = normalized.split("\n", -1);
        if (lines.length < 2) return null;

        StringBuilder out = new StringBuilder(body.length() + 64);
        ArrayList<String> paragraphLines = new ArrayList<>();
        boolean changed = false;
        int i = 0;
        while (i < lines.length) {
            RenderedTableBlock table = parseRenderedTableAt(lines, i);
            if (table != null) {
                appendLooseParagraph(out, paragraphLines);
                paragraphLines.clear();
                out.append(table.html);
                changed = true;
                i = table.endIndex + 1;
                continue;
            }
            if (isBlank(lines[i])) {
                appendLooseParagraph(out, paragraphLines);
                paragraphLines.clear();
            } else {
                paragraphLines.add(lines[i]);
            }
            i++;
        }
        appendLooseParagraph(out, paragraphLines);
        return changed ? out.toString() : null;
    }

    private static String normalizeHtmlParagraphBreaks(String body) {
        String out = HTML_BR.matcher(body == null ? "" : body).replaceAll("\n");
        return normalizeMarkdownInput(out);
    }

    private static void appendLooseParagraph(StringBuilder out, List<String> lines) {
        if (lines == null || lines.isEmpty()) return;
        out.append("<p>");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) out.append("<br>\n");
            out.append(lines.get(i));
        }
        out.append("</p>");
    }

    private static RenderedTableBlock parseRenderedTableAt(String[] lines, int index) {
        if (lines == null || index < 0 || index + 1 >= lines.length) return null;
        String headerLine = lines[index] == null ? "" : lines[index];
        List<String> header = splitRenderedTableRow(headerLine);
        if (header.size() < 1 || !hasRenderedTablePipe(headerLine)) return null;
        List<String> align = parseRenderedTableAlignments(lines[index + 1], header.size());
        if (align == null) return null;

        int cols = Math.max(header.size(), align.size());
        StringBuilder html = new StringBuilder();
        html.append("<table><thead><tr>");
        for (int c = 0; c < cols; c++) {
            String a = c < align.size() ? align.get(c) : "left";
            html.append("<th style=\"text-align:").append(a).append("\">")
                    .append(c < header.size() ? trimTableSyntax(header.get(c)) : "")
                    .append("</th>");
        }
        html.append("</tr></thead><tbody>");
        int j = index + 2;
        while (j < lines.length && !isBlank(lines[j]) && hasRenderedTablePipe(lines[j])) {
            if (parseRenderedTableAlignments(lines[j], cols) != null) break;
            List<String> row = splitRenderedTableRow(lines[j]);
            html.append("<tr>");
            for (int c = 0; c < cols; c++) {
                String a = c < align.size() ? align.get(c) : "left";
                html.append("<td style=\"text-align:").append(a).append("\">")
                        .append(c < row.size() ? trimTableSyntax(row.get(c)) : "")
                        .append("</td>");
            }
            html.append("</tr>");
            j++;
        }
        html.append("</tbody></table>");
        return new RenderedTableBlock(html.toString(), j - 1);
    }

    private static List<String> splitRenderedTableRow(String line) {
        ArrayList<String> cells = new ArrayList<>();
        if (line == null) return cells;
        String s = stripOuterTablePipes(trimTableSyntax(line));
        StringBuilder cell = new StringBuilder();
        boolean inCode = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                int tagEnd = findHtmlTagEnd(s, i + 1);
                if (tagEnd > i) {
                    String tag = s.substring(i, tagEnd + 1);
                    String lowerTag = tag.toLowerCase(Locale.ROOT);
                    if (lowerTag.matches("<code(?:\\s[^>]*)?>")) inCode = true;
                    else if (lowerTag.matches("</code\\s*>")) inCode = false;
                    cell.append(tag);
                    i = tagEnd;
                    continue;
                }
            }
            if (isTablePipeChar(c) && !inCode) {
                cells.add(trimTableSyntax(cell.toString()));
                cell.setLength(0);
            } else {
                cell.append(c);
            }
        }
        cells.add(trimTableSyntax(cell.toString()));
        return cells;
    }

    private static boolean hasRenderedTablePipe(String line) {
        if (line == null) return false;
        String s = trimTableSyntax(line);
        boolean inCode = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '<') {
                int tagEnd = findHtmlTagEnd(s, i + 1);
                if (tagEnd > i) {
                    String tag = s.substring(i, tagEnd + 1).toLowerCase(Locale.ROOT);
                    if (tag.matches("<code(?:\\s[^>]*)?>")) inCode = true;
                    else if (tag.matches("</code\\s*>")) inCode = false;
                    i = tagEnd;
                    continue;
                }
            }
            if (isTablePipeChar(c) && !inCode) return true;
        }
        return false;
    }

    private static List<String> parseRenderedTableAlignments(String separatorLine, int minColumns) {
        List<String> cells = splitRenderedTableRow(separatorLine);
        if (cells.size() < 1 || cells.size() < minColumns) return null;
        ArrayList<String> align = new ArrayList<>();
        for (String c : cells) {
            String s = normalizeTableSeparatorCell(c);
            if (s == null || !s.matches(":?-{3,}:?")) return null;
            boolean left = s.startsWith(":");
            boolean right = s.endsWith(":");
            align.add(left && right ? "center" : (right ? "right" : "left"));
        }
        return align;
    }

    private static String convertDefinitionListParagraphs(String html) {
        Matcher m = PARAGRAPH.matcher(html == null ? "" : html);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String body = m.group(1) == null ? "" : m.group(1);
            String replacement = definitionListHtmlFromParagraphBody(body);
            if (replacement == null) replacement = m.group(0);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String definitionListHtmlFromParagraphBody(String body) {
        if (body == null || body.indexOf('\n') < 0 || body.indexOf("\n:") < 0) return null;
        String[] lines = body.split("\n");
        if (lines.length < 2) return null;
        StringBuilder out = new StringBuilder(body.length() + 24);
        int i = 0;
        boolean any = false;
        out.append("<dl>");
        while (i < lines.length) {
            String term = lines[i] == null ? "" : lines[i].trim();
            if (term.isEmpty() || term.startsWith(":")) return null;
            if (i + 1 >= lines.length || !lines[i + 1].trim().startsWith(":")) return null;
            out.append("<dt>").append(term).append("</dt>");
            i++;
            while (i < lines.length && lines[i].trim().startsWith(":")) {
                String def = lines[i].trim().substring(1).trim();
                out.append("<dd>").append(def).append("</dd>");
                any = true;
                i++;
            }
        }
        out.append("</dl>");
        return any ? out.toString() : null;
    }

    private static boolean startsDelimiter(String text, int index, String delimiter) {
        return text != null && delimiter != null && index >= 0 && index + delimiter.length() <= text.length() && text.startsWith(delimiter, index);
    }

    private static int findClosingDelimiter(String text, int start, String delimiter) {
        if (text == null || delimiter == null || delimiter.isEmpty()) return -1;
        int i = Math.max(0, start);
        while (i + delimiter.length() <= text.length()) {
            int next = text.indexOf(delimiter, i);
            if (next < 0) return -1;
            if (!isEscaped(text, next)) return next;
            i = next + delimiter.length();
        }
        return -1;
    }

    private static int findClosingSingleDelimiter(String text, int start, char delimiter) {
        if (text == null) return -1;
        for (int i = Math.max(0, start); i < text.length(); i++) {
            if (text.charAt(i) == delimiter && !isEscaped(text, i) && isSingleDelimiter(text, i, delimiter)) return i;
        }
        return -1;
    }

    private static boolean isSingleDelimiter(String text, int index, char delimiter) {
        if (text == null || index < 0 || index >= text.length() || text.charAt(index) != delimiter) return false;
        return (index == 0 || text.charAt(index - 1) != delimiter) && (index + 1 >= text.length() || text.charAt(index + 1) != delimiter);
    }

    private static boolean isTightInlineSpan(String text, int start, int end) {
        if (text == null || start >= end) return false;
        if (Character.isWhitespace(text.charAt(start)) || Character.isWhitespace(text.charAt(end - 1))) return false;
        for (int i = start; i < end; i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r' || c == '<' || c == '>') return false;
        }
        return true;
    }

    private static boolean isTightInlineMath(String text, int start, int end) {
        if (!isTightInlineSpan(text, start, end)) return false;
        for (int i = start; i < end; i++) if (text.charAt(i) == '$') return false;
        return true;
    }

    private static boolean isEscaped(String text, int index) {
        if (text == null || index <= 0 || index > text.length()) return false;
        int slash = 0;
        int i = index - 1;
        while (i >= 0 && text.charAt(i) == '\\') { slash++; i--; }
        return (slash % 2) == 1;
    }

    private static int findCodeSpanEnd(String text, int start) {
        if (text == null || start < 0 || start >= text.length() || text.charAt(start) != '`') return -1;
        int runEnd = start;
        while (runEnd < text.length() && text.charAt(runEnd) == '`') runEnd++;
        int runLen = runEnd - start;
        int i = runEnd;
        while (i < text.length()) {
            if (text.charAt(i) == '`') {
                int j = i;
                while (j < text.length() && text.charAt(j) == '`') j++;
                if (j - i == runLen) return j;
                i = j;
            } else {
                i++;
            }
        }
        return -1;
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

    private static boolean isIndentedCodeLine(String line) {
        return line != null && (line.startsWith("    ") || line.startsWith("\t"));
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
        return new Fence(ch, len);
    }

    private static boolean isFenceClose(String line, char fenceChar, int fenceLength) {
        if (line == null || fenceLength < 3) return false;
        int i = 0;
        while (i < line.length() && i < 4 && line.charAt(i) == ' ') i++;
        if (i > 3 || i >= line.length() || line.charAt(i) != fenceChar) return false;
        int j = i;
        while (j < line.length() && line.charAt(j) == fenceChar) j++;
        if (j - i < fenceLength) return false;
        while (j < line.length()) {
            if (!isBlankChar(line.charAt(j))) return false;
            j++;
        }
        return true;
    }

    private static boolean isBlank(String s) {
        if (s == null || s.isEmpty()) return true;
        for (int i = 0; i < s.length(); i++) if (!isBlankChar(s.charAt(i))) return false;
        return true;
    }

    private static String trimControl(String s) {
        return trimTableSyntax(s == null ? "" : s);
    }

    private static String trimTableSyntax(String s) {
        if (s == null || s.isEmpty()) return "";
        int start = 0;
        int end = s.length();
        while (start < end && isBlankChar(s.charAt(start))) start++;
        while (end > start && isBlankChar(s.charAt(end - 1))) end--;
        return s.substring(start, end);
    }

    private static String stripOuterTablePipes(String s) {
        String out = s == null ? "" : s;
        if (!out.isEmpty() && isTablePipeChar(out.charAt(0))) out = out.substring(1);
        if (!out.isEmpty() && isTablePipeChar(out.charAt(out.length() - 1))) out = out.substring(0, out.length() - 1);
        return out;
    }

    private static String normalizeTableSeparatorCell(String cell) {
        if (cell == null) return null;
        String s = stripHtmlTags(cell);
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (isTableColonChar(c)) out.append(':');
            else if (isTableDashChar(c)) out.append('-');
            else if (isBlankChar(c)) {
                // Ignore spacing around separator markers.
            } else {
                return null;
            }
        }
        return out.toString();
    }

    private static String stripHtmlTags(String text) {
        if (text == null || text.indexOf('<') < 0) return text == null ? "" : text;
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '<') {
                int end = findHtmlTagEnd(text, i + 1);
                if (end > i) {
                    i = end;
                    continue;
                }
            }
            out.append(text.charAt(i));
        }
        return out.toString();
    }

    private static boolean containsTablePipeChar(String text) {
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++) if (isTablePipeChar(text.charAt(i))) return true;
        return false;
    }

    private static boolean isBlankChar(char c) {
        return Character.isWhitespace(c) || c == '\u00a0' || c == '\u2007' || c == '\u202f' || c == '\ufeff' || c == '\u200b';
    }

    private static boolean isTablePipeChar(char c) {
        return c == '|'
                || c == '\uff5c' // fullwidth vertical line
                || c == '\uffe8' // halfwidth forms light vertical
                || c == '\u2223' // divides
                || c == '\u23d0' // vertical line extension
                || c == '\u2758' // light vertical bar
                || c == '\u2502' // box drawings light vertical
                || c == '\u01c0'; // latin letter dental click, often pasted as a pipe lookalike
    }

    private static boolean isTableDashChar(char c) {
        return c == '-'
                || c == '\u2010'
                || c == '\u2011'
                || c == '\u2012'
                || c == '\u2013'
                || c == '\u2014'
                || c == '\u2015'
                || c == '\u2212'
                || c == '\ufe58'
                || c == '\ufe63'
                || c == '\uff0d';
    }

    private static boolean isTableColonChar(char c) {
        return c == ':' || c == '\uff1a';
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

    private static int countOccurrences(String haystack, String needle) {
        if (haystack == null || needle == null || needle.isEmpty()) return 0;
        int count = 0;
        int i = 0;
        while ((i = haystack.indexOf(needle, i)) >= 0) {
            count++;
            i += needle.length();
        }
        return count;
    }

    private static final class Fence {
        final char ch;
        final int length;
        Fence(char ch, int length) { this.ch = ch; this.length = length; }
    }

    private static final class TableBlock {
        final String html;
        final int endIndex;
        TableBlock(String html, int endIndex) { this.html = html == null ? "" : html; this.endIndex = endIndex; }
    }

    private static final class RenderedTableBlock {
        final String html;
        final int endIndex;
        RenderedTableBlock(String html, int endIndex) { this.html = html == null ? "" : html; this.endIndex = endIndex; }
    }
}
