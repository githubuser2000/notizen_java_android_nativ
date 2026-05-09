package de.notizen.android.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class LegacySettings {
    public static final String[] RECENT_FILE_SLOTS = new String[]{"a", "b", "c", "d"};
    public static final String LEGACY_DEFAULT_FILENAME = LegacyPaths.LEGACY_DEFAULT_FILENAME;

    public String language = "Auto";
    public String lastDirectory = "";
    public String lastFile = LEGACY_DEFAULT_FILENAME;
    public final List<String> recentFiles = new ArrayList<>();
    public int backupKeep = 30;
    public int autosaveSeconds = 60;
    public int scrollbarsChoice = 3;
    public String openOnceFile = "";
    public String openOnceTimestamp = "";
    public boolean autorunEnabled = false;
    public boolean autorunMinimized = true;
    public boolean showDesknoteBorders = true;
    public boolean showInTaskbarWhenMinimized = false;
    public boolean gnomeSafeTrayStart = true;
    public String ftpHost = "";
    public String ftpPath = "";
    public String ftpUsername = "";
    public String ftpPassword = "";
    public int windowX = 60;
    public int windowY = 60;
    public int windowWidth = 1000;
    public int windowHeight = 700;
    public String windowState = "Normal";
    public int androidTreePaneWidthDp = 280;
    public long feedbackDayTicks = 0L;
    public int feedbackCount = 0;
    public final Map<String, int[]> toolstripPositions = defaultToolstripPositions();
    public final Map<String, String> legacyRootAttributes = new LinkedHashMap<>();
    public final Map<String, Map<String, String>> legacyKnownElementAttrs = new LinkedHashMap<>();
    public final Map<String, Map<String, String>> legacyKnownNestedElementAttrs = new LinkedHashMap<>();
    public final List<String> legacyPassthroughElements = new ArrayList<>();

    public LegacySettings copy() {
        LegacySettings s = new LegacySettings();
        s.language = language;
        s.lastDirectory = lastDirectory;
        s.lastFile = lastFile;
        s.recentFiles.clear();
        s.recentFiles.addAll(recentFiles);
        s.backupKeep = backupKeep;
        s.autosaveSeconds = autosaveSeconds;
        s.scrollbarsChoice = scrollbarsChoice;
        s.openOnceFile = openOnceFile;
        s.openOnceTimestamp = openOnceTimestamp;
        s.autorunEnabled = autorunEnabled;
        s.autorunMinimized = autorunMinimized;
        s.showDesknoteBorders = showDesknoteBorders;
        s.showInTaskbarWhenMinimized = showInTaskbarWhenMinimized;
        s.gnomeSafeTrayStart = gnomeSafeTrayStart;
        s.ftpHost = ftpHost;
        s.ftpPath = ftpPath;
        s.ftpUsername = ftpUsername;
        s.ftpPassword = ftpPassword;
        s.windowX = windowX;
        s.windowY = windowY;
        s.windowWidth = windowWidth;
        s.windowHeight = windowHeight;
        s.windowState = windowState;
        s.androidTreePaneWidthDp = androidTreePaneWidthDp;
        s.feedbackDayTicks = feedbackDayTicks;
        s.feedbackCount = feedbackCount;
        s.toolstripPositions.clear();
        for (Map.Entry<String, int[]> e : toolstripPositions.entrySet()) s.toolstripPositions.put(e.getKey(), new int[]{e.getValue()[0], e.getValue()[1]});
        s.legacyRootAttributes.clear();
        s.legacyRootAttributes.putAll(legacyRootAttributes);
        copyNestedStringMap(legacyKnownElementAttrs, s.legacyKnownElementAttrs);
        copyNestedStringMap(legacyKnownNestedElementAttrs, s.legacyKnownNestedElementAttrs);
        s.legacyPassthroughElements.clear();
        s.legacyPassthroughElements.addAll(legacyPassthroughElements);
        return s;
    }

    public static int normalizeAutosaveSeconds(Object value) {
        int seconds;
        try {
            if (value == null) return 0;
            seconds = Integer.parseInt(String.valueOf(value).trim());
        } catch (Exception e) {
            return 0;
        }
        if (seconds <= 0) return 0;
        return Math.max(5, seconds);
    }

    public static int normalizeAndroidTreePaneWidthDp(int value) {
        if (value <= 0) return 280;
        return Math.max(24, Math.min(1200, value));
    }

    public static String normalizeWindowState(String value) {
        String s = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (Arrays.asList("maximized", "maximise", "maximize", "maximiert").contains(s)) return "Maximized";
        if (Arrays.asList("minimized", "minimised", "minimize", "minimieren", "minimiert").contains(s)) return "Minimized";
        return "Normal";
    }

    public static boolean legacyAutosaveShouldSave(boolean rootExists, boolean fileAssociated, boolean fileStillExists, boolean changed) {
        return rootExists && fileAssociated && fileStillExists && changed;
    }

    public static List<String> recentFilesFromSlots(Map<String, String> slots) {
        List<String> out = new ArrayList<>();
        for (String key : RECENT_FILE_SLOTS) {
            String value = slots == null ? null : slots.get(key);
            if (value != null && !value.isEmpty()) out.add(value);
        }
        return out;
    }

    public static Map<String, String> recentSlotsFromFiles(List<String> recentFiles) {
        ArrayList<String> values = new ArrayList<>();
        if (recentFiles != null) for (String entry : recentFiles) if (entry != null && !entry.isEmpty()) values.add(entry);
        while (values.size() > RECENT_FILE_SLOTS.length) values.remove(0);
        Map<String, String> out = new LinkedHashMap<>();
        for (int i = 0; i < RECENT_FILE_SLOTS.length; i++) out.put(RECENT_FILE_SLOTS[i], i < values.size() ? values.get(i) : "");
        return out;
    }

    public void rememberFile(String path) {
        if (path == null || path.isEmpty()) return;
        recentFiles.remove(path);
        recentFiles.add(path);
        while (recentFiles.size() > RECENT_FILE_SLOTS.length) recentFiles.remove(0);
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (slash >= 0) {
            lastDirectory = path.substring(0, slash);
            lastFile = path.substring(slash + 1);
        } else {
            lastFile = path;
        }
    }

    public String activateRecentFile(int index) {
        if (index < 0 || index >= recentFiles.size()) return null;
        String selected = recentFiles.get(index);
        rememberFile(selected);
        return selected;
    }

    public String activateRecentFile(String pathOrIndex) {
        if (pathOrIndex == null) return null;
        try { return activateRecentFile(Integer.parseInt(pathOrIndex.trim())); } catch (Exception ignored) {}
        int index = recentFiles.indexOf(pathOrIndex);
        if (index < 0) return null;
        return activateRecentFile(index);
    }

    public static List<String> legacyRememberRecentFile(List<String> recentFiles, String path) {
        LegacySettings tmp = new LegacySettings();
        if (recentFiles != null) tmp.recentFiles.addAll(recentFiles);
        tmp.rememberFile(path);
        return new ArrayList<>(tmp.recentFiles);
    }

    public static String legacyActivateRecentFile(List<String> recentFiles, int index, List<String> rotatedOut) {
        LegacySettings tmp = new LegacySettings();
        if (recentFiles != null) tmp.recentFiles.addAll(recentFiles);
        String selected = tmp.activateRecentFile(index);
        if (rotatedOut != null) { rotatedOut.clear(); rotatedOut.addAll(tmp.recentFiles); }
        return selected;
    }

    public static Map<String, int[]> defaultToolstripPositions() {
        Map<String, int[]> out = new LinkedHashMap<>();
        out.put("haupt", new int[]{0, 0});
        out.put("elements", new int[]{0, 0});
        out.put("font", new int[]{0, 0});
        out.put("cutpastecopy", new int[]{0, 0});
        return out;
    }

    public static LegacySettings fromXmlBytes(byte[] bytes) throws Exception {
        if (bytes == null) return new LegacySettings();
        String xml = decodeXml(bytes);
        return fromXmlString(xml);
    }

    public static LegacySettings fromXmlString(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(false);
        try { f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); } catch (Exception ignored) {}
        try { f.setFeature("http://xml.org/sax/features/external-general-entities", false); } catch (Exception ignored) {}
        try { f.setFeature("http://xml.org/sax/features/external-parameter-entities", false); } catch (Exception ignored) {}
        Document doc = f.newDocumentBuilder().parse(new InputSource(new StringReader(xml == null ? "" : xml)));
        LegacySettings settings = new LegacySettings();
        settings.apply(doc.getDocumentElement());
        return settings;
    }

    private static String decodeXml(byte[] data) {
        Charset[] charsets = new Charset[]{StandardCharsets.UTF_16, StandardCharsets.UTF_8, Charset.forName("windows-1252")};
        for (Charset cs : charsets) {
            String s = new String(data, cs);
            String t = stripBom(s).trim();
            if (t.startsWith("<")) return stripBom(s);
        }
        return stripBom(new String(data, StandardCharsets.UTF_8));
    }

    private static String stripBom(String s) { return s != null && !s.isEmpty() && s.charAt(0) == '\ufeff' ? s.substring(1) : (s == null ? "" : s); }

    public void apply(Element root) {
        if (root == null) return;
        captureLegacyPassthrough(root);
        Element scrolls = child(root, "scrolls");
        if (scrolls != null) scrollbarsChoice = LegacyScrollbars.normalizeChoice(asInt(scrolls.getAttribute("choice"), scrollbarsChoice));
        Element languageEl = child(root, "language");
        if (languageEl != null && languageEl.hasAttribute("choice")) language = languageEl.getAttribute("choice");
        Element open = child(root, "open");
        if (open != null) {
            String directory = open.getAttribute("directory");
            String file = open.getAttribute("file");
            if (directory != null && directory.trim().length() >= 2) lastDirectory = directory;
            if (file != null && !file.isEmpty()) lastFile = file;
            Element once = child(open, "once-opened");
            if (once != null) {
                openOnceFile = attr(once, "file", openOnceFile);
                openOnceTimestamp = attr(once, "timestamp", openOnceTimestamp);
            }
        }
        Element files = child(root, "files");
        if (files != null) {
            Map<String, String> slots = new LinkedHashMap<>();
            for (String slot : RECENT_FILE_SLOTS) slots.put(slot, files.getAttribute(slot));
            recentFiles.clear();
            recentFiles.addAll(recentFilesFromSlots(slots));
        }
        Element ftp = child(root, "ftp");
        if (ftp != null) {
            ftpHost = attr(ftp, "host", ftpHost);
            ftpPath = attr(ftp, "path", ftpPath);
            ftpUsername = attr(ftp, "name", ftpUsername);
            ftpPassword = attr(ftp, "pass", ftpPassword);
        }
        Element backups = child(root, "saftycopies");
        if (backups != null) backupKeep = asInt(backups.getAttribute("amount"), backupKeep);
        Element autosave = child(root, "autosave");
        Element feedback = child(root, "x");
        Element autosaveSource = autosave == null ? feedback : autosave;
        if (autosaveSource != null) autosaveSeconds = normalizeAutosaveSeconds(attr(autosaveSource, "seconds", attr(autosaveSource, "a", String.valueOf(autosaveSeconds))));
        if (feedback != null) {
            feedbackDayTicks = asLong(feedback.getAttribute("y"), feedbackDayTicks);
            feedbackCount = asInt(feedback.getAttribute("z"), feedbackCount);
        }
        Element toolStripes = child(root, "tool-stripes");
        if (toolStripes != null) {
            for (String name : new String[]{"haupt", "elements", "font", "cutpastecopy"}) {
                Element el = child(toolStripes, name);
                int[] pos = toolstripPositions.get(name);
                if (el != null && pos != null) toolstripPositions.put(name, new int[]{asInt(el.getAttribute("x"), pos[0]), asInt(el.getAttribute("y"), pos[1])});
            }
        }
        Element autorun = child(root, "autorun");
        if (autorun != null) {
            autorunEnabled = asBool(autorun.getAttribute("if"), autorunEnabled);
            autorunMinimized = asBool(autorun.getAttribute("minimized"), autorunMinimized);
        }
        Element desknotes = child(root, "desknotes");
        if (desknotes != null) showDesknoteBorders = asBool(desknotes.getAttribute("show_desknote_borders"), showDesknoteBorders);
        Element minimized = child(root, "minimized-show-in");
        if (minimized != null) showInTaskbarWhenMinimized = asBool(minimized.getAttribute("taskbar"), showInTaskbarWhenMinimized);
        Element tray = child(root, "tray");
        if (tray != null) gnomeSafeTrayStart = asBool(attr(tray, "gnome-safe-start", attr(tray, "gnome_safe_start", String.valueOf(gnomeSafeTrayStart))), gnomeSafeTrayStart);
        Element main = child(root, "main-form");
        if (main != null) {
            windowX = asInt(main.getAttribute("x"), windowX);
            windowY = asInt(main.getAttribute("y"), windowY);
            windowWidth = asInt(main.getAttribute("width"), windowWidth);
            windowHeight = asInt(main.getAttribute("height"), windowHeight);
            windowState = normalizeWindowState(main.getAttribute("windowstate"));
        }
        Element androidUi = child(root, "android-ui");
        if (androidUi != null) {
            androidTreePaneWidthDp = normalizeAndroidTreePaneWidthDp(asInt(androidUi.getAttribute("treepane-width-dp"), androidTreePaneWidthDp));
        }
    }

    public byte[] toXmlBytes() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        Document doc = f.newDocumentBuilder().newDocument();
        Element root = doc.createElement("notizen-alx");
        for (Map.Entry<String, String> e : legacyRootAttributes.entrySet()) root.setAttribute(e.getKey(), e.getValue() == null ? "" : e.getValue());
        doc.appendChild(root);
        append(doc, root, "scrolls", attrsFor("scrolls", map("choice", String.valueOf(LegacyScrollbars.normalizeChoice(scrollbarsChoice)))));
        append(doc, root, "saftycopies", attrsFor("saftycopies", map("amount", String.valueOf(backupKeep))));
        append(doc, root, "autorun", attrsFor("autorun", map("if", yesNo(autorunEnabled), "minimized", yesNo(autorunMinimized))));
        append(doc, root, "ftp", attrsFor("ftp", map("name", ftpUsername, "pass", ftpPassword, "host", ftpHost, "path", ftpPath)));
        append(doc, root, "files", attrsFor("files", recentSlotsFromFiles(recentFiles)));
        append(doc, root, "language", attrsFor("language", map("choice", language)));
        Element open = append(doc, root, "open", attrsFor("open", map("file", lastFile, "directory", lastDirectory)));
        append(doc, open, "once-opened", attrsFor("open/once-opened", map("file", openOnceFile, "timestamp", openOnceTimestamp)));
        append(doc, root, "main-form", attrsFor("main-form", map("x", String.valueOf(windowX), "y", String.valueOf(windowY), "width", String.valueOf(windowWidth), "height", String.valueOf(windowHeight), "windowstate", normalizeWindowState(windowState))));
        append(doc, root, "minimized-show-in", attrsFor("minimized-show-in", map("taskbar", yesNo(showInTaskbarWhenMinimized))));
        append(doc, root, "tray", attrsFor("tray", map("gnome-safe-start", yesNo(gnomeSafeTrayStart))));
        append(doc, root, "desknotes", attrsFor("desknotes", map("show_desknote_borders", yesNo(showDesknoteBorders))));
        append(doc, root, "android-ui", attrsFor("android-ui", map("treepane-width-dp", String.valueOf(normalizeAndroidTreePaneWidthDp(androidTreePaneWidthDp)))));
        Element stripes = append(doc, root, "tool-stripes", attrsFor("tool-stripes", new LinkedHashMap<String, String>()));
        for (String name : new String[]{"haupt", "elements", "font", "cutpastecopy"}) {
            int[] pos = toolstripPositions.get(name);
            if (pos == null) pos = new int[]{0, 0};
            append(doc, stripes, name, attrsFor("tool-stripes/" + name, map("x", String.valueOf(pos[0]), "y", String.valueOf(pos[1]))));
        }
        append(doc, root, "x", attrsFor("x", map("y", String.valueOf(feedbackDayTicks), "z", String.valueOf(feedbackCount), "a", String.valueOf(normalizeAutosaveSeconds(autosaveSeconds)))));
        appendLegacyPassthroughElements(doc, root);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-16");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        return out.toByteArray();
    }

    public String summary() {
        return "Sprache: " + language + "\n" +
                "Backups behalten: " + backupKeep + "\n" +
                "Autosave: " + autosaveSeconds + " s\n" +
                "FTP: " + (ftpHost == null || ftpHost.isEmpty() ? "-" : ftpHost + ftpPath) + "\n" +
                "Zuletzt: " + recentFiles;
    }

    private static void copyNestedStringMap(Map<String, Map<String, String>> src, Map<String, Map<String, String>> dst) {
        dst.clear();
        for (Map.Entry<String, Map<String, String>> e : src.entrySet()) dst.put(e.getKey(), new LinkedHashMap<>(e.getValue()));
    }

    private void captureLegacyPassthrough(Element root) {
        legacyRootAttributes.clear();
        legacyKnownElementAttrs.clear();
        legacyKnownNestedElementAttrs.clear();
        legacyPassthroughElements.clear();
        NamedNodeMap rootAttrs = root.getAttributes();
        for (int i = 0; i < rootAttrs.getLength(); i++) {
            Node a = rootAttrs.item(i);
            legacyRootAttributes.put(a.getNodeName(), a.getNodeValue());
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (!(n instanceof Element)) continue;
            Element element = (Element) n;
            String tag = element.getTagName();
            if (!knownTopLevelTags().contains(tag)) {
                legacyPassthroughElements.add(nodeToString(element));
                continue;
            }
            captureExtraAttrs(element, tag, legacyKnownElementAttrs);
            NodeList nested = element.getChildNodes();
            for (int j = 0; j < nested.getLength(); j++) {
                Node cn = nested.item(j);
                if (!(cn instanceof Element)) continue;
                Element child = (Element) cn;
                String path = tag + "/" + child.getTagName();
                if (knownNestedPaths().contains(path)) captureExtraAttrs(child, path, legacyKnownNestedElementAttrs);
            }
        }
    }

    private static void captureExtraAttrs(Element element, String path, Map<String, Map<String, String>> target) {
        Set<String> known = knownAttrs(path);
        LinkedHashMap<String, String> extras = new LinkedHashMap<>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node a = attrs.item(i);
            if (!known.contains(a.getNodeName())) extras.put(a.getNodeName(), a.getNodeValue());
        }
        if (!extras.isEmpty()) target.put(path, extras);
    }

    private Map<String, String> attrsFor(String path, Map<String, String> attrs) {
        LinkedHashMap<String, String> merged = new LinkedHashMap<>();
        Map<String, String> extras = path.contains("/") ? legacyKnownNestedElementAttrs.get(path) : legacyKnownElementAttrs.get(path);
        if (extras != null) merged.putAll(extras);
        if (attrs != null) merged.putAll(attrs);
        return merged;
    }

    private static Set<String> knownTopLevelTags() {
        return new LinkedHashSet<>(Arrays.asList(
                "scrolls", "language", "open", "files", "ftp", "saftycopies", "autosave", "x", "tool-stripes",
                "autorun", "desknotes", "minimized-show-in", "tray", "main-form", "android-ui"
        ));
    }

    private static Set<String> knownNestedPaths() {
        return new LinkedHashSet<>(Arrays.asList(
                "open/once-opened", "tool-stripes/haupt", "tool-stripes/elements", "tool-stripes/font", "tool-stripes/cutpastecopy"
        ));
    }

    private static Set<String> knownAttrs(String path) {
        if ("scrolls".equals(path)) return new LinkedHashSet<>(Arrays.asList("choice"));
        if ("language".equals(path)) return new LinkedHashSet<>(Arrays.asList("choice"));
        if ("open".equals(path)) return new LinkedHashSet<>(Arrays.asList("file", "directory"));
        if ("files".equals(path)) return new LinkedHashSet<>(Arrays.asList(RECENT_FILE_SLOTS));
        if ("ftp".equals(path)) return new LinkedHashSet<>(Arrays.asList("name", "pass", "host", "path"));
        if ("saftycopies".equals(path)) return new LinkedHashSet<>(Arrays.asList("amount"));
        if ("autosave".equals(path)) return new LinkedHashSet<>(Arrays.asList("seconds"));
        if ("x".equals(path)) return new LinkedHashSet<>(Arrays.asList("y", "z", "a"));
        if ("tool-stripes".equals(path)) return new LinkedHashSet<>();
        if ("autorun".equals(path)) return new LinkedHashSet<>(Arrays.asList("if", "minimized"));
        if ("desknotes".equals(path)) return new LinkedHashSet<>(Arrays.asList("show_desknote_borders"));
        if ("minimized-show-in".equals(path)) return new LinkedHashSet<>(Arrays.asList("taskbar"));
        if ("tray".equals(path)) return new LinkedHashSet<>(Arrays.asList("gnome-safe-start", "gnome_safe_start"));
        if ("main-form".equals(path)) return new LinkedHashSet<>(Arrays.asList("x", "y", "width", "height", "windowstate"));
        if ("android-ui".equals(path)) return new LinkedHashSet<>(Arrays.asList("treepane-width-dp"));
        if ("open/once-opened".equals(path)) return new LinkedHashSet<>(Arrays.asList("file", "timestamp"));
        if (path.startsWith("tool-stripes/")) return new LinkedHashSet<>(Arrays.asList("x", "y"));
        return new LinkedHashSet<>();
    }

    private void appendLegacyPassthroughElements(Document doc, Element root) {
        Set<String> existing = new LinkedHashSet<>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n instanceof Element) existing.add(((Element) n).getTagName());
        }
        for (String fragment : legacyPassthroughElements) {
            if (fragment == null || fragment.trim().isEmpty()) continue;
            try {
                Document parsed = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(fragment)));
                Element element = parsed.getDocumentElement();
                if (element == null || existing.contains(element.getTagName())) continue;
                root.appendChild(doc.importNode(element, true));
                existing.add(element.getTagName());
            } catch (Exception ignored) {
                // Keep settings save robust when future/unknown XML is malformed.
            }
        }
    }

    private static String nodeToString(Node node) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            t.transform(new DOMSource(node), new StreamResult(out));
            return out.toString("UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private static Element child(Element parent, String tag) {
        NodeList list = parent.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n instanceof Element && ((Element) n).getTagName().equals(tag)) return (Element) n;
        }
        return null;
    }

    private static Element append(Document doc, Element parent, String tag, Map<String, String> attrs) {
        Element el = doc.createElement(tag);
        for (Map.Entry<String, String> e : attrs.entrySet()) el.setAttribute(e.getKey(), e.getValue() == null ? "" : e.getValue());
        parent.appendChild(el);
        return el;
    }

    private static Map<String, String> map(String... values) {
        Map<String, String> out = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) out.put(values[i], values[i + 1]);
        return out;
    }

    private static String attr(Element el, String name, String fallback) { return el.hasAttribute(name) ? el.getAttribute(name) : fallback; }
    private static int asInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception e) { return fallback; } }
    private static long asLong(String value, long fallback) { try { return Long.parseLong(value); } catch (Exception e) { return fallback; } }
    private static boolean asBool(String value, boolean fallback) {
        if (value == null || value.isEmpty()) return fallback;
        String v = value.trim().toLowerCase(Locale.ROOT);
        return v.equals("yes") || v.equals("true") || v.equals("1") || v.equals("on") || v.equals("ja");
    }
    private static String yesNo(boolean value) { return value ? "yes" : "no"; }
}
