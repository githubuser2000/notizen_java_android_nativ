package de.notizen.android.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Platform-neutral home-screen widget model for the Android port.
 *
 * The old .NET/PyQt versions could keep many sticky notes as separate desktop
 * windows. Android cannot do that directly without overlays, so the native port
 * stores one widget snapshot per launcher widget and binds it to a node index
 * path inside the current ALX tree.
 */
public final class LegacyAndroidWidgetRegistry {
    public static final int DEFAULT_BG = 0xfffffacd;
    public static final int DEFAULT_FG = 0xff202020;
    public static final int MAX_WIDGET_RTF_IMAGES = 4;
    public static final int MAX_WIDGET_SOURCE_IMAGE_BYTES = 4 * 1024 * 1024;
    public static final int MAX_WIDGET_TOTAL_IMAGE_BYTES = 7 * 1024 * 1024;

    private LegacyAndroidWidgetRegistry() {}

    public static final class WidgetImage {
        public final String mimeType;
        public final byte[] data;

        public WidgetImage(String mimeType, byte[] data) {
            this.mimeType = mimeType == null ? "" : mimeType;
            this.data = data == null ? new byte[0] : data;
        }
    }

    public static final class WidgetSpec {
        public final int widgetId;
        public final String documentName;
        public final String nodePath;
        public final String title;
        public final String text;
        public final int bgArgb;
        public final int fgArgb;
        public final float textSizeSp;
        public final List<WidgetImage> images;
        public final int originalImageCount;

        public WidgetSpec(int widgetId, String documentName, String nodePath, String title, String text, int bgArgb, int fgArgb, float textSizeSp) {
            this(widgetId, documentName, nodePath, title, text, bgArgb, fgArgb, textSizeSp, null, 0);
        }

        public WidgetSpec(int widgetId, String documentName, String nodePath, String title, String text, int bgArgb, int fgArgb, float textSizeSp, List<WidgetImage> images, int originalImageCount) {
            this.widgetId = widgetId;
            this.documentName = documentName == null ? "" : documentName;
            this.nodePath = nodePath == null ? "" : nodePath;
            this.title = cleanTitle(title);
            this.text = text == null ? "" : text;
            this.bgArgb = forceOpaque(bgArgb == 0 ? DEFAULT_BG : bgArgb);
            this.fgArgb = forceOpaque(fgArgb == 0 ? DEFAULT_FG : fgArgb);
            this.textSizeSp = normalizeWidgetTextSp(textSizeSp);
            this.images = copyImages(images);
            this.originalImageCount = Math.max(originalImageCount, this.images.size());
        }

        public String summary() {
            String imagePart = originalImageCount <= 0 ? "" : ", " + originalImageCount + " Bild" + (originalImageCount == 1 ? "" : "er");
            return "#" + widgetId + " " + title + " [" + (nodePath.isEmpty() ? "root" : nodePath) + "] " + text.length() + " Zeichen" + imagePart;
        }
    }

    public static WidgetSpec fromNode(int widgetId, NoteDocument document, NoteNode node, String documentName, float textSizeSp) {
        NoteNode safe = node == null ? (document == null ? null : document.ensureRoot()) : node;
        String path = indexPath(safe);
        return fromNodePath(widgetId, document, path, documentName, textSizeSp);
    }

    public static WidgetSpec fromNodePath(int widgetId, NoteDocument document, String nodePath, String documentName, float textSizeSp) {
        NoteNode node = nodeByIndexPath(document == null ? null : document.root, nodePath);
        if (node == null && document != null) node = document.ensureRoot();
        String title = node == null ? "Notiz" : cleanTitle(node.title);
        String rtf = node == null ? "" : (node.rtf == null ? "" : node.rtf);
        int imageCount = RtfUtils.countImageGroups(rtf);
        String text = RtfUtils.rtfToPlainText(rtf).trim();
        if (imageCount > 0) text = text.replace(RtfUtils.LEGACY_IMAGE_PLACEHOLDER, "").trim();
        List<RtfUtils.RtfImage> rtfImages = RtfUtils.extractImagesLimited(rtf, MAX_WIDGET_RTF_IMAGES, MAX_WIDGET_SOURCE_IMAGE_BYTES, MAX_WIDGET_TOTAL_IMAGE_BYTES);
        List<WidgetImage> previewImages = previewImagesFromRtf(rtfImages);
        int bg = DEFAULT_BG;
        int fg = DEFAULT_FG;
        if (node != null) {
            if (node.desktopNote != null && node.desktopNote.argb != null) bg = forceOpaque(node.desktopNote.argb);
            else if (node.bgArgb != 0) bg = forceOpaque(node.bgArgb);
            if (node.fgArgb != 0) fg = forceOpaque(node.fgArgb);
        }
        return new WidgetSpec(widgetId, documentName, nodePath == null ? "" : nodePath.trim(), title, text, bg, fg, textSizeSp, previewImages, imageCount);
    }

    public static List<WidgetSpec> refreshExisting(NoteDocument document, String documentName, List<WidgetSpec> existing, float fallbackTextSizeSp) {
        ArrayList<WidgetSpec> out = new ArrayList<>();
        if (existing == null) return out;
        for (WidgetSpec old : existing) {
            if (old == null) continue;
            out.add(fromNodePath(old.widgetId, document, old.nodePath, documentName == null || documentName.isEmpty() ? old.documentName : documentName,
                    old.textSizeSp > 0f ? old.textSizeSp : fallbackTextSizeSp));
        }
        return out;
    }

    public static String indexPath(NoteNode node) {
        if (node == null) return "";
        ArrayList<Integer> parts = new ArrayList<>();
        NoteNode n = node;
        while (n != null && n.parent != null) {
            parts.add(0, Math.max(0, n.indexInParent()));
            n = n.parent;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) out.append('.');
            out.append(parts.get(i));
        }
        return out.toString();
    }

    public static NoteNode nodeByIndexPath(NoteNode root, String path) {
        if (root == null) return null;
        String clean = path == null ? "" : path.trim();
        if (clean.isEmpty()) return root;
        NoteNode n = root;
        String[] parts = clean.split("\\.");
        for (String part : parts) {
            if (part == null || part.trim().isEmpty()) continue;
            int index;
            try { index = Integer.parseInt(part.trim()); } catch (Exception e) { return root; }
            if (index < 0 || index >= n.children.size()) return root;
            n = n.children.get(index);
        }
        return n;
    }

    public static float normalizeWidgetTextSp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value) || value <= 0f) return 13f;
        return Math.max(8f, Math.min(28f, value));
    }

    public static int forceOpaque(int argb) {
        return 0xff000000 | (argb & 0x00ffffff);
    }

    public static String cleanTitle(String title) {
        String t = title == null ? "" : title.trim();
        return t.isEmpty() ? "Notiz" : t;
    }

    public static String listSummary(List<WidgetSpec> specs) {
        if (specs == null || specs.isEmpty()) return "Keine Android-Haftnotiz-Widgets registriert.";
        StringBuilder out = new StringBuilder();
        for (WidgetSpec spec : specs) {
            if (spec == null) continue;
            if (out.length() > 0) out.append('\n');
            out.append(spec.summary());
        }
        return out.toString();
    }

    public static String shortColor(int argb) {
        return String.format(Locale.ROOT, "#%06x", argb & 0x00ffffff);
    }

    private static List<WidgetImage> previewImagesFromRtf(List<RtfUtils.RtfImage> images) {
        ArrayList<WidgetImage> out = new ArrayList<>();
        if (images == null || images.isEmpty()) return out;
        int total = 0;
        for (RtfUtils.RtfImage image : images) {
            if (image == null || image.data == null || image.data.length == 0) continue;
            if (out.size() >= MAX_WIDGET_RTF_IMAGES) break;
            if (image.data.length > MAX_WIDGET_SOURCE_IMAGE_BYTES) continue;
            if (total + image.data.length > MAX_WIDGET_TOTAL_IMAGE_BYTES) break;
            out.add(new WidgetImage(image.mimeType, image.data));
            total += image.data.length;
        }
        return out;
    }

    private static List<WidgetImage> copyImages(List<WidgetImage> images) {
        ArrayList<WidgetImage> out = new ArrayList<>();
        if (images == null) return out;
        for (WidgetImage image : images) {
            if (image == null || image.data == null || image.data.length == 0) continue;
            byte[] copy = new byte[image.data.length];
            System.arraycopy(image.data, 0, copy, 0, image.data.length);
            out.add(new WidgetImage(image.mimeType, copy));
            if (out.size() >= MAX_WIDGET_RTF_IMAGES) break;
        }
        return out;
    }
}
