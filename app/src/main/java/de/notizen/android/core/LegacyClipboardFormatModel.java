package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Clipboard format priority from WinForms/PyQt bridges. */
public final class LegacyClipboardFormatModel {
    public enum Format { NOTIZEN_NODE_XML, RTF, HTML, IMAGE, TEXT, NONE }
    public static final class Formats {
        public final boolean nodeXml;
        public final boolean rtf;
        public final boolean html;
        public final boolean image;
        public final boolean text;
        public Formats(boolean nodeXml, boolean rtf, boolean html, boolean image, boolean text) {
            this.nodeXml = nodeXml; this.rtf = rtf; this.html = html; this.image = image; this.text = text;
        }
    }
    public static final class Preferred {
        public final Format format;
        public final List<Format> priority;
        public Preferred(Format format, List<Format> priority) { this.format = format; this.priority = Collections.unmodifiableList(priority); }
    }
    public static final class PasteDecision {
        public final Format format;
        public final boolean pasteIntoTree;
        public final boolean pasteIntoEditor;
        public final String message;
        public PasteDecision(Format format, boolean pasteIntoTree, boolean pasteIntoEditor, String message) {
            this.format = format; this.pasteIntoTree = pasteIntoTree; this.pasteIntoEditor = pasteIntoEditor; this.message = message;
        }
    }
    private LegacyClipboardFormatModel() {}
    public static Preferred preferred(Formats formats) {
        ArrayList<Format> p = new ArrayList<>();
        p.add(Format.NOTIZEN_NODE_XML); p.add(Format.RTF); p.add(Format.HTML); p.add(Format.IMAGE); p.add(Format.TEXT);
        if (formats == null) return new Preferred(Format.NONE, p);
        if (formats.nodeXml) return new Preferred(Format.NOTIZEN_NODE_XML, p);
        if (formats.rtf) return new Preferred(Format.RTF, p);
        if (formats.html) return new Preferred(Format.HTML, p);
        if (formats.image) return new Preferred(Format.IMAGE, p);
        if (formats.text) return new Preferred(Format.TEXT, p);
        return new Preferred(Format.NONE, p);
    }
    public static PasteDecision decide(Formats formats, boolean focusTree) {
        Format f = preferred(formats).format;
        if (f == Format.NONE) return new PasteDecision(f, false, false, "Zwischenablage leer");
        if (focusTree) {
            boolean ok = f == Format.NOTIZEN_NODE_XML || f == Format.TEXT;
            return new PasteDecision(f, ok, false, ok ? "In Baum einfügen" : "Format passt nicht in den Baum");
        }
        return new PasteDecision(f, false, f != Format.NOTIZEN_NODE_XML, f == Format.NOTIZEN_NODE_XML ? "Knotenformat nicht in Editor einfügen" : "In Inhalt einfügen");
    }
    public static Formats fromMimeTypes(List<String> mimeTypes, boolean hasImage, boolean hasText) {
        boolean node = false, rtf = false, html = false, text = hasText;
        if (mimeTypes != null) for (String mt : mimeTypes) {
            String s = mt == null ? "" : mt.toLowerCase(Locale.ROOT);
            if (s.contains("notizen") || s.contains("x-notizen-node")) node = true;
            if (s.contains("rtf")) rtf = true;
            if (s.contains("html")) html = true;
            if (s.startsWith("text/")) text = true;
        }
        return new Formats(node, rtf, html, hasImage, text);
    }
}
