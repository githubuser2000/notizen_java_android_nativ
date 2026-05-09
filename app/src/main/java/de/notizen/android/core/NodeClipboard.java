package de.notizen.android.core;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/** XML subtree clipboard format ported from Notizen PyQt. */
public final class NodeClipboard {
    public static final String NODE_MIME_TYPE = "application/x-notizen-pyqt-node+xml";

    private static final Set<String> NOTE_KNOWN_ATTRS = new LinkedHashSet<>(Arrays.asList(
            "name", "title", "isexpanded", "bgcolor", "fgcolor", "visible", "x", "y", "width", "height", "opacity", "argb"
    ));
    private static final Set<String> DESKTOP_ATTRS = new LinkedHashSet<>(Arrays.asList(
            "visible", "x", "y", "width", "height", "opacity", "argb"
    ));

    private NodeClipboard() {}

    public static String nodeToClipboardXml(NoteNode node) throws AlxException {
        return nodeToClipboardXml(node, false);
    }

    public static String nodeToClipboardXml(NoteNode node, boolean includeDesktopNote) throws AlxException {
        if (node == null) throw new AlxException("Kein Knoten zum Kopieren.");
        try {
            Document xml = newDocument();
            Element root = xml.createElement("notizen-node");
            xml.appendChild(root);
            root.appendChild(elementFromNode(xml, node, includeDesktopNote));
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter out = new StringWriter();
            t.transform(new DOMSource(xml), new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            throw new AlxException("Knoten konnte nicht in die Zwischenablage geschrieben werden.", e);
        }
    }

    public static NoteNode nodeFromClipboardXml(String xmlText) throws AlxException {
        return nodeFromClipboardXml(xmlText, false);
    }

    public static NoteNode nodeFromClipboardXml(String xmlText, boolean includeDesktopNote) throws AlxException {
        try {
            Document xml = parseXml(xmlText == null ? "" : xmlText.trim());
            Element root = xml.getDocumentElement();
            Element note;
            if ("Notiz".equals(root.getTagName())) {
                note = root;
            } else if ("notizen-node".equals(root.getTagName()) || "notizen-alx2".equals(root.getTagName())) {
                note = firstDirectNotiz(root);
            } else {
                throw new AlxException("Zwischenablage enthält keinen Notizen-Knoten: " + root.getTagName());
            }
            if (note == null) throw new AlxException("Zwischenablage enthält kein Notiz-Element.");
            return nodeFromElement(note, includeDesktopNote);
        } catch (AlxException e) {
            throw e;
        } catch (Exception e) {
            throw new AlxException("Zwischenablage enthält keinen gültigen Notizen-Knoten.", e);
        }
    }

    public static boolean looksLikeNodeClipboardXml(String text) {
        String stripped = text == null ? "" : text.trim();
        return stripped.startsWith("<notizen-node") || stripped.startsWith("<Notiz") || stripped.startsWith("<notizen-alx2");
    }

    private static Document newDocument() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        return f.newDocumentBuilder().newDocument();
    }

    private static Document parseXml(String text) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        try { f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) {}
        try { f.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) {}
        try { f.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) {}
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(text)));
    }

    private static Element firstDirectNotiz(Element root) {
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element && "Notiz".equals(((Element) n).getTagName())) return (Element) n;
        }
        return null;
    }

    private static Element elementFromNode(Document xml, NoteNode node, boolean includeDesktopNote) {
        Element element = xml.createElement("Notiz");
        for (java.util.Map.Entry<String, String> attr : node.extraAttrs.entrySet()) element.setAttribute(attr.getKey(), attr.getValue());
        element.setAttribute("name", node.title == null ? "..." : node.title);
        element.setAttribute("isexpanded", node.expanded ? "True" : "False");
        element.setAttribute("bgcolor", Integer.toString(node.bgArgb));
        element.setAttribute("fgcolor", Integer.toString(node.fgArgb));
        if (includeDesktopNote && node.desktopNote != null) {
            DesktopNoteState d = node.desktopNote;
            element.setAttribute("visible", d.visible ? "True" : "False");
            element.setAttribute("x", Integer.toString(d.x));
            element.setAttribute("y", Integer.toString(d.y));
            element.setAttribute("width", Integer.toString(d.width));
            element.setAttribute("height", Integer.toString(d.height));
            if (!d.legacySparse || d.legacyAttrNames.contains("opacity") || d.opacity != 0.85d) element.setAttribute("opacity", Double.toString(d.opacity));
            if (d.argb != null) element.setAttribute("argb", Integer.toString(d.argb));
        }
        if (node.rtf != null && !node.rtf.isEmpty()) element.appendChild(xml.createTextNode(node.rtf));
        for (NoteNode child : node.children) element.appendChild(elementFromNode(xml, child, includeDesktopNote));
        return element;
    }

    private static NoteNode nodeFromElement(Element element, boolean includeDesktopNote) throws Exception {
        NoteNode node = new NoteNode(nonEmpty(element.getAttribute("name"), nonEmpty(element.getAttribute("title"), "...")), directLeadingText(element));
        node.expanded = boolAttr(element.getAttribute("isexpanded"), true);
        node.bgArgb = intAttr(element.getAttribute("bgcolor"), 0);
        node.fgArgb = intAttr(element.getAttribute("fgcolor"), 0);
        if (includeDesktopNote) node.desktopNote = desktopStateFromElement(element);
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            if (!NOTE_KNOWN_ATTRS.contains(a.getNodeName())) node.extraAttrs.put(a.getNodeName(), a.getNodeValue());
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element && "Notiz".equals(((Element) n).getTagName())) node.addChild(nodeFromElement((Element) n, includeDesktopNote));
        }
        return node;
    }

    private static String nonEmpty(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static String directLeadingText(Element element) {
        StringBuilder b = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) break;
            if (n.getNodeType() == Node.TEXT_NODE || n instanceof CDATASection) b.append(n.getNodeValue());
        }
        return b.toString();
    }

    private static DesktopNoteState desktopStateFromElement(Element element) {
        if (!element.hasAttribute("x") || element.getAttribute("x").isEmpty()) return null;
        DesktopNoteState d = new DesktopNoteState();
        d.x = intAttr(element.getAttribute("x"), 80);
        d.y = intAttr(element.getAttribute("y"), 80);
        d.width = intAttr(element.getAttribute("width"), 260);
        d.height = intAttr(element.getAttribute("height"), 220);
        d.visible = boolAttr(element.getAttribute("visible"), true);
        d.opacity = floatAttr(element.getAttribute("opacity"), 0.85d);
        if (element.hasAttribute("argb") && !element.getAttribute("argb").isEmpty()) d.argb = intAttr(element.getAttribute("argb"), 0);
        d.legacySparse = true;
        for (String key : DESKTOP_ATTRS) if (element.hasAttribute(key) && !element.getAttribute(key).isEmpty()) d.legacyAttrNames.add(key);
        return d;
    }

    private static boolean boolAttr(String value, boolean def) {
        if (value == null || value.isEmpty()) return def;
        String v = value.trim().toLowerCase(java.util.Locale.ROOT);
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("ja");
    }

    private static int intAttr(String value, int def) {
        if (value == null || value.isEmpty()) return def;
        try { return (int) Double.parseDouble(value.replace(',', '.')); } catch (NumberFormatException e) { return def; }
    }

    private static double floatAttr(String value, double def) {
        if (value == null || value.isEmpty()) return def;
        try { return Double.parseDouble(value.replace(',', '.')); } catch (NumberFormatException e) { return def; }
    }
}
