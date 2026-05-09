package de.notizen.android.core;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Privacy-light validation helpers ported from the PyQt legacy_validation module. */
public final class LegacyValidation {
    private LegacyValidation() {}

    public static final class LegacyAlxSummary {
        public final int nodeCount;
        public final int maxDepth;
        public final int desktopNoteCount;
        public final int visibleDesktopNoteCount;
        public final int rtfCharacterCount;
        public final int plainTextCharacterCount;
        public final int embeddedImageCount;
        public final String treeShapeHash;
        public final String contentHash;

        public LegacyAlxSummary(int nodeCount, int maxDepth, int desktopNoteCount, int visibleDesktopNoteCount,
                                int rtfCharacterCount, int plainTextCharacterCount, int embeddedImageCount,
                                String treeShapeHash, String contentHash) {
            this.nodeCount = nodeCount;
            this.maxDepth = maxDepth;
            this.desktopNoteCount = desktopNoteCount;
            this.visibleDesktopNoteCount = visibleDesktopNoteCount;
            this.rtfCharacterCount = rtfCharacterCount;
            this.plainTextCharacterCount = plainTextCharacterCount;
            this.embeddedImageCount = embeddedImageCount;
            this.treeShapeHash = treeShapeHash == null ? "" : treeShapeHash;
            this.contentHash = contentHash == null ? "" : contentHash;
        }

        public String asLegacyText() {
            return "Knoten: " + nodeCount + "\n" +
                    "Max. Tiefe: " + maxDepth + "\n" +
                    "Desktop-Haftnotizen: " + desktopNoteCount + "\n" +
                    "Sichtbare Desktop-Haftnotizen: " + visibleDesktopNoteCount + "\n" +
                    "RTF-Zeichen: " + rtfCharacterCount + "\n" +
                    "Plaintext-Zeichen: " + plainTextCharacterCount + "\n" +
                    "Bilder: " + embeddedImageCount + "\n" +
                    "Baum-Hash: " + treeShapeHash + "\n" +
                    "Inhalt-Hash: " + contentHash;
        }

        @Override public boolean equals(Object o) {
            if (!(o instanceof LegacyAlxSummary)) return false;
            LegacyAlxSummary other = (LegacyAlxSummary) o;
            return nodeCount == other.nodeCount && maxDepth == other.maxDepth &&
                    desktopNoteCount == other.desktopNoteCount && visibleDesktopNoteCount == other.visibleDesktopNoteCount &&
                    rtfCharacterCount == other.rtfCharacterCount && plainTextCharacterCount == other.plainTextCharacterCount &&
                    embeddedImageCount == other.embeddedImageCount && treeShapeHash.equals(other.treeShapeHash) &&
                    contentHash.equals(other.contentHash);
        }

        @Override public int hashCode() {
            return Arrays.hashCode(new Object[]{nodeCount, maxDepth, desktopNoteCount, visibleDesktopNoteCount,
                    rtfCharacterCount, plainTextCharacterCount, embeddedImageCount, treeShapeHash, contentHash});
        }
    }

    public static final class LegacyAlxRoundtripResult {
        public final boolean ok;
        public final LegacyAlxSummary before;
        public final LegacyAlxSummary after;
        public final List<String> differences;

        public LegacyAlxRoundtripResult(boolean ok, LegacyAlxSummary before, LegacyAlxSummary after, List<String> differences) {
            this.ok = ok;
            this.before = before;
            this.after = after;
            this.differences = Collections.unmodifiableList(new ArrayList<>(differences == null ? Collections.<String>emptyList() : differences));
        }
    }

    public static LegacyAlxSummary summarizeDocument(NoteDocument document) {
        NoteNode root = document == null ? null : document.ensureRoot();
        if (root == null) root = new NoteNode("start", "");
        try {
            MessageDigest shape = MessageDigest.getInstance("SHA-256");
            MessageDigest content = MessageDigest.getInstance("SHA-256");
            Counter c = new Counter();
            visit(root, 0, new ArrayList<Integer>(), shape, content, c);
            return new LegacyAlxSummary(c.nodeCount, c.maxDepth, c.desktopNoteCount, c.visibleDesktopNoteCount,
                    c.rtfCharacterCount, c.plainTextCharacterCount, c.embeddedImageCount,
                    hex(shape.digest()), hex(content.digest()));
        } catch (Exception e) {
            throw new IllegalStateException("Legacy-ALX-Zusammenfassung konnte nicht berechnet werden.", e);
        }
    }

    public static LegacyAlxSummary summarizeAlxBytes(byte[] data, String password) throws AlxException {
        return summarizeDocument(AlxIo.load(data, password));
    }

    public static LegacyAlxSummary summarizeAlxFile(File file, String password) throws Exception {
        return summarizeAlxBytes(Files.readAllBytes(file.toPath()), password);
    }

    public static LegacyAlxRoundtripResult validateAlxRoundtripBytes(byte[] data, String password) throws AlxException {
        NoteDocument document = AlxIo.load(data, password);
        LegacyAlxSummary before = summarizeDocument(document);
        NoteDocument roundtripped = AlxIo.load(AlxIo.dump(document, password), password);
        LegacyAlxSummary after = summarizeDocument(roundtripped);
        List<String> differences = differences(before, after);
        return new LegacyAlxRoundtripResult(differences.isEmpty(), before, after, differences);
    }

    public static LegacyAlxRoundtripResult validateDocumentRoundtrip(NoteDocument document, String password) throws AlxException {
        // The legacy validator compares the persisted ALX form before/after a save cycle.
        // A live Android/Java node may lack loader-only bookkeeping such as legacyAttrNames,
        // so we first serialize it and then run the same load -> dump -> load check used for files.
        return validateAlxRoundtripBytes(AlxIo.dump(document, password), password);
    }

    private static void visit(NoteNode node, int depth, List<Integer> indexes, MessageDigest shape, MessageDigest content, Counter c) {
        c.nodeCount++;
        c.maxDepth = Math.max(c.maxDepth, depth);
        if (node.desktopNote != null) {
            c.desktopNoteCount++;
            if (node.desktopNote.visible) c.visibleDesktopNoteCount++;
        }
        String rtf = node.rtf == null ? "" : node.rtf;
        String plain = RtfUtils.rtfToPlainText(rtf);
        int images = RtfUtils.countImages(rtf);
        c.rtfCharacterCount += rtf.length();
        c.plainTextCharacterCount += plain.length();
        c.embeddedImageCount += images;

        update(shape, pythonTuple(indexes));
        update(shape, Integer.toString(node.children.size()));
        shape.update(digest(node.title == null ? "" : node.title));
        update(shape, Boolean.toString(node.expanded));
        update(shape, Integer.toString(node.bgArgb));
        update(shape, Integer.toString(node.fgArgb));
        update(shape, desktopSignature(node.desktopNote));
        for (Map.Entry<String, String> entry : sortedEntries(node.extraAttrs)) {
            update(shape, entry.getKey());
            update(shape, entry.getValue());
        }
        content.update(digest(rtf));
        content.update(digest(plain));

        for (int i = 0; i < node.children.size(); i++) {
            indexes.add(i);
            visit(node.children.get(i), depth + 1, indexes, shape, content, c);
            indexes.remove(indexes.size() - 1);
        }
    }

    private static List<Map.Entry<String, String>> sortedEntries(Map<String, String> map) {
        ArrayList<Map.Entry<String, String>> entries = new ArrayList<>();
        if (map != null) entries.addAll(map.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));
        return entries;
    }

    private static String desktopSignature(DesktopNoteState state) {
        if (state == null) return "-";
        ArrayList<String> names = new ArrayList<>(state.legacyAttrNames);
        Collections.sort(names);
        return (state.visible ? "1" : "0") + "|" + state.x + "|" + state.y + "|" + state.width + "|" + state.height + "|" +
                String.format(java.util.Locale.ROOT, "%.4f", state.opacity) + "|" + (state.argb == null ? "" : state.argb.toString()) + "|" +
                (state.legacySparse ? "s" : "n") + "|" + join(names, ",");
    }

    private static List<String> differences(LegacyAlxSummary a, LegacyAlxSummary b) {
        ArrayList<String> out = new ArrayList<>();
        addDiff(out, "node_count", a.nodeCount, b.nodeCount);
        addDiff(out, "max_depth", a.maxDepth, b.maxDepth);
        addDiff(out, "desktop_note_count", a.desktopNoteCount, b.desktopNoteCount);
        addDiff(out, "visible_desktop_note_count", a.visibleDesktopNoteCount, b.visibleDesktopNoteCount);
        addDiff(out, "rtf_character_count", a.rtfCharacterCount, b.rtfCharacterCount);
        addDiff(out, "plain_text_character_count", a.plainTextCharacterCount, b.plainTextCharacterCount);
        addDiff(out, "embedded_image_count", a.embeddedImageCount, b.embeddedImageCount);
        addDiff(out, "tree_shape_hash", a.treeShapeHash, b.treeShapeHash);
        addDiff(out, "content_hash", a.contentHash, b.contentHash);
        return out;
    }

    private static void addDiff(List<String> out, String field, Object oldValue, Object newValue) {
        if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) out.add(field + ": " + oldValue + " -> " + newValue);
    }

    private static byte[] digest(String text) {
        try {
            MessageDigest d = MessageDigest.getInstance("SHA-256");
            return d.digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void update(MessageDigest digest, String value) { digest.update((value == null ? "" : value).getBytes(StandardCharsets.UTF_8)); }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) out.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
        return out.toString();
    }

    private static String pythonTuple(List<Integer> values) {
        if (values == null || values.isEmpty()) return "()";
        StringBuilder out = new StringBuilder("(");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(values.get(i));
        }
        if (values.size() == 1) out.append(',');
        out.append(')');
        return out.toString();
    }

    private static String join(List<String> items, String sep) {
        StringBuilder out = new StringBuilder();
        if (items != null) for (String item : items) {
            if (out.length() > 0) out.append(sep);
            out.append(item == null ? "" : item);
        }
        return out.toString();
    }

    private static final class Counter {
        int nodeCount;
        int maxDepth;
        int desktopNoteCount;
        int visibleDesktopNoteCount;
        int rtfCharacterCount;
        int plainTextCharacterCount;
        int embeddedImageCount;
    }
}
