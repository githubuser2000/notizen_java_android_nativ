package de.notizen.android.core;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class AlxIo {
    public static final byte[] GZIP_MAGIC = new byte[]{0x1f, (byte) 0x8b};
    public static final String BLANK_PASSWORD_24 = "                        ";
    private static final Set<String> NOTE_KNOWN_ATTRS = new LinkedHashSet<>(Arrays.asList(
            "name", "title", "isexpanded", "bgcolor", "fgcolor", "visible", "x", "y", "width", "height", "opacity", "argb"
    ));
    private static final Set<String> DESKTOP_ATTRS = new LinkedHashSet<>(Arrays.asList(
            "visible", "x", "y", "width", "height", "opacity", "argb"
    ));

    private AlxIo() {}

    public static String normalizePassword(String password) {
        String p = password == null ? "" : password;
        if (p.length() > 24) return p.substring(0, 24);
        StringBuilder b = new StringBuilder(p);
        while (b.length() < 24) b.append(' ');
        return b.toString();
    }

    public static NoteDocument load(byte[] data, String password) throws AlxException {
        byte[] xmlBytes = maybeDecompress(data, password);
        String xml = decodeXml(xmlBytes);
        NoteDocument document = parseAlxXml(xml);
        document.password = password == null ? "" : password;
        document.changed = false;
        return document;
    }

    public static byte[] dump(NoteDocument document, String password) throws AlxException {
        try {
            byte[] xml = documentToXmlBytes(document);
            byte[] gz = gzip(xml);
            String p = password != null ? password : (document == null ? "" : document.password);
            return encryptLegacyBytes(gz, p);
        } catch (IOException e) {
            throw new AlxException("ALX-Datei konnte nicht geschrieben werden.", e);
        }
    }

    public static NoteDocument parseAlxXml(String xmlText) throws AlxException {
        try {
            Document xml = parseXmlDocument(xmlText);
            Element root = xml.getDocumentElement();
            NoteDocument document = new NoteDocument();
            String tag = root.getTagName();
            if ("notizen-alx2".equals(tag)) {
                Element first = captureAlx2RootPassthrough(root, document);
                if (first != null) document.root = parseNotiz(first);
            } else if ("notes_doc".equals(tag)) {
                Element first = firstChildElement(root, "node", "leaf");
                if (first != null) document.root = parseLegacyNode(first);
            } else {
                throw new AlxException("Nicht unterstütztes Notizen-XML-Wurzelelement: " + tag);
            }
            if (document.root == null) document.root = new NoteNode("start", "");
            document.changed = false;
            return document;
        } catch (AlxException e) {
            throw e;
        } catch (Exception e) {
            throw new AlxException("ALX-XML konnte nicht gelesen werden.", e);
        }
    }

    private static byte[] maybeDecompress(byte[] data, String password) throws AlxException {
        if (data == null) throw new AlxException("Leere Datei.");
        if (startsWith(data, GZIP_MAGIC)) {
            try { return gunzip(data); } catch (IOException e) { throw new AlxException("GZip-Daten konnten nicht entpackt werden.", e); }
        }
        if (looksLikeRawXml(data)) return data;
        if (password == null || password.isEmpty()) {
            throw new AlxException.PasswordRequired("Diese ALX-Datei ist verschlüsselt. Bitte das Notizen.NET-Passwort eingeben.");
        }
        try {
            byte[] decrypted = decryptLegacyBytes(data, password);
            return gunzip(decrypted);
        } catch (Exception e) {
            throw new AlxException.InvalidPassword("ALX-Datei konnte mit dem angegebenen Passwort nicht entschlüsselt oder entpackt werden.", e);
        }
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) if (data[i] != prefix[i]) return false;
        return true;
    }

    private static boolean looksLikeRawXml(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xff) == 0xef && (data[1] & 0xff) == 0xbb && (data[2] & 0xff) == 0xbf) return true;
        if (data.length >= 4 && (data[0] & 0xff) == 0xff && (data[1] & 0xff) == 0xfe && data[2] == '<') return true;
        if (data.length >= 4 && (data[0] & 0xff) == 0xfe && (data[1] & 0xff) == 0xff && data[3] == '<') return true;
        for (byte b : data) {
            int c = b & 0xff;
            if (c == '<') return true;
            if (!Character.isWhitespace((char)c)) return false;
        }
        return false;
    }

    private static String decodeXml(byte[] data) {
        Charset[] charsets = new Charset[]{StandardCharsets.UTF_16, StandardCharsets.UTF_8, Charset.forName("windows-1252")};
        for (Charset cs : charsets) {
            String s = new String(data, cs);
            String trimmed = stripBom(s).trim();
            if (trimmed.startsWith("<")) return stripBom(s);
        }
        return stripBom(new String(data, StandardCharsets.UTF_8));
    }

    private static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\ufeff') return s.substring(1);
        return s == null ? "" : s;
    }

    private static byte[] gzip(byte[] input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gz = new GZIPOutputStream(out);
        gz.write(input);
        gz.close();
        return out.toByteArray();
    }

    private static byte[] gunzip(byte[] input) throws IOException {
        GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(input));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = gz.read(buf)) != -1) out.write(buf, 0, n);
        gz.close();
        return out.toByteArray();
    }

    private static byte[][] passwordKeys(String password) {
        String p = normalizePassword(password);
        return new byte[][]{
                asciiKey(p.substring(0, 8)),
                asciiKey(p.substring(7, 15)),
                asciiKey(p.substring(15, 23))
        };
    }

    private static byte[] asciiKey(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    private static byte[] decryptLegacyBytes(byte[] data, String password) throws GeneralSecurityException {
        byte[] out = data;
        for (byte[] key : passwordKeys(password)) out = des(false, out, key);
        return out;
    }

    private static byte[] encryptLegacyBytes(byte[] data, String password) throws AlxException {
        String p = normalizePassword(password);
        if (BLANK_PASSWORD_24.equals(p)) return data;
        try {
            byte[][] keys = passwordKeys(p);
            byte[] out = data;
            for (int i = keys.length - 1; i >= 0; i--) out = des(true, out, keys[i]);
            return out;
        } catch (GeneralSecurityException e) {
            throw new AlxException("ALX-Datei konnte nicht verschlüsselt werden.", e);
        }
    }

    private static byte[] des(boolean encrypt, byte[] data, byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        SecretKeySpec spec = new SecretKeySpec(key, "DES");
        cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, spec, new IvParameterSpec(key));
        return cipher.doFinal(data);
    }

    private static Document parseXmlDocument(String xmlText) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        try { f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) {}
        try { f.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) {}
        try { f.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) {}
        DocumentBuilder b = f.newDocumentBuilder();
        return b.parse(new InputSource(new StringReader(xmlText)));
    }

    private static Document newXmlDocument() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        return f.newDocumentBuilder().newDocument();
    }

    private static Element firstChildElement(Element parent, String tag1, String tag2) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) {
                String tag = ((Element)n).getTagName();
                if (tag.equals(tag1) || (tag2 != null && tag.equals(tag2))) return (Element)n;
            }
        }
        return null;
    }


    private static Element captureAlx2RootPassthrough(Element root, NoteDocument document) throws Exception {
        if (root == null || document == null) return null;
        NamedNodeMap attrs = root.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            document.rootAttrs.put(a.getNodeName(), a.getNodeValue());
        }
        Element firstNote = null;
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) continue;
            Element child = (Element) n;
            if ("Notiz".equals(child.getTagName()) && firstNote == null) firstNote = child;
            else document.extraRootXml.add(nodeToString(child));
        }
        return firstNote;
    }

    private static NoteNode parseNotiz(Element element) throws Exception {
        String title = nonEmpty(element.getAttribute("name"), nonEmpty(element.getAttribute("title"), "..."));
        NoteNode node = new NoteNode(title, directLeadingText(element));
        node.expanded = boolAttr(element.getAttribute("isexpanded"), true);
        node.bgArgb = intAttr(element.getAttribute("bgcolor"), 0);
        node.fgArgb = intAttr(element.getAttribute("fgcolor"), 0);
        node.desktopNote = desktopStateFromElement(element);

        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            if (!NOTE_KNOWN_ATTRS.contains(a.getNodeName())) node.extraAttrs.put(a.getNodeName(), a.getNodeValue());
        }

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) {
                Element child = (Element)n;
                if ("Notiz".equals(child.getTagName())) node.addChild(parseNotiz(child));
                else node.extraChildXml.add(nodeToString(child));
            }
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

    private static NoteNode parseLegacyNode(Element element) {
        NoteNode node = new NoteNode(nonEmpty(element.getAttribute("title"), nonEmpty(element.getAttribute("name"), "...")), "");
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) continue;
            Element child = (Element)n;
            String tag = child.getTagName();
            if ("leaf_text".equals(tag)) node.rtf = parseLegacyLeafText(child);
            else if ("node".equals(tag) || "leaf".equals(tag)) node.addChild(parseLegacyNode(child));
        }
        return node;
    }

    private static String parseLegacyLeafText(Element element) {
        StringBuilder out = new StringBuilder();
        NodeList paragraphs = element.getElementsByTagName("p");
        for (int i = 0; i < paragraphs.getLength(); i++) {
            String text = paragraphs.item(i).getTextContent();
            if (text != null && !text.isEmpty()) {
                if (out.length() > 0) out.append('\n');
                out.append(text);
            }
        }
        if (out.length() == 0) out.append(element.getTextContent() == null ? "" : element.getTextContent());
        return RtfUtils.plainTextToRtf(out.toString());
    }

    private static boolean boolAttr(String value, boolean def) {
        if (value == null || value.isEmpty()) return def;
        String v = value.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("ja");
    }

    private static int intAttr(String value, int def) {
        if (value == null || value.isEmpty()) return def;
        try { return (int) Double.parseDouble(value.replace(',', '.')); }
        catch (NumberFormatException e) { return def; }
    }

    private static double floatAttr(String value, double def) {
        if (value == null || value.isEmpty()) return def;
        try { return Double.parseDouble(value.replace(',', '.')); }
        catch (NumberFormatException e) { return def; }
    }

    public static byte[] documentToXmlBytes(NoteDocument document) throws AlxException {
        try {
            Document xml = newXmlDocument();
            Element root = xml.createElement("notizen-alx2");
            if (document != null) {
                for (java.util.Map.Entry<String, String> attr : document.rootAttrs.entrySet()) root.setAttribute(attr.getKey(), attr.getValue() == null ? "" : attr.getValue());
            }
            xml.appendChild(root);
            root.appendChild(elementFromNote(xml, document == null ? null : document.ensureRoot()));
            if (document != null) for (String fragment : document.extraRootXml) appendFragment(xml, root, fragment);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(xml), new StreamResult(out));
            return out.toByteArray();
        } catch (Exception e) {
            throw new AlxException("ALX-XML konnte nicht erzeugt werden.", e);
        }
    }

    private static Element elementFromNote(Document xml, NoteNode node) {
        if (node == null) node = new NoteNode("start", "");
        Element element = xml.createElement("Notiz");
        for (java.util.Map.Entry<String, String> attr : node.extraAttrs.entrySet()) element.setAttribute(attr.getKey(), attr.getValue());
        element.setAttribute("name", node.title == null ? "..." : node.title);
        element.setAttribute("isexpanded", node.expanded ? "True" : "False");
        element.setAttribute("bgcolor", Integer.toString(node.bgArgb));
        element.setAttribute("fgcolor", Integer.toString(node.fgArgb));
        if (node.desktopNote != null) {
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
        for (String fragment : node.extraChildXml) appendFragment(xml, element, fragment);
        for (NoteNode child : node.children) element.appendChild(elementFromNote(xml, child));
        return element;
    }

    private static void appendFragment(Document target, Element parent, String fragment) {
        if (fragment == null || fragment.trim().isEmpty()) return;
        try {
            Document parsed = parseXmlDocument(fragment);
            Node imported = target.importNode(parsed.getDocumentElement(), true);
            parent.appendChild(imported);
        } catch (Exception ignored) {
            // Keep ordinary save robust if an unknown/future fragment is malformed.
        }
    }

    private static String nodeToString(Node node) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        t.transform(new DOMSource(node), new StreamResult(out));
        return out.toString("UTF-8");
    }
}
