package de.notizen.android.markdown;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private static final String ENGINE_NAME = "commonmark-java 0.28.0/GFM";
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
        Node document = PARSER.parse(markdown == null ? "" : markdown);
        return RENDERER.render(document);
    }

    public static String engineName() {
        return ENGINE_NAME;
    }

    public static String healthCheck() {
        String html = render("| A | B |\n|---|---|\n| 1 | 2 |\n\n- [x] Task\n\n~~strike~~\n\n> [!NOTE]\n> Hinweis");
        String lower = html.toLowerCase();
        boolean strike = html.contains("<s>") || html.contains("<del>");
        if (!html.contains("<table") || !lower.contains("checkbox") || !strike || !lower.contains("alert")) {
            throw new IllegalStateException("CommonMark/GFM health check failed: " + html);
        }
        return ENGINE_NAME;
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
}
