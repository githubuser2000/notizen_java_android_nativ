import de.notizen.android.core.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.zip.GZIPInputStream;

public final class TestCore {
    public static void main(String[] args) throws Exception {
        testRtf();
        testAlxRoundtrip();
        testAlxExpansionAliasesAndAndroidUiSetting();
        testEncryptedAlx();
        testSearchAndExport();
        testNodeClipboard();
        testAlarmRules();
        testLegacyColorsAndRtfExport();
        testLegacySettingsAndFtp();
        testRtfImagesHtmlAndRichExport();
        testHtmlToRtfRichRoundtrip();
        testStyledRtfHtmlRendering();
        testRtfFieldObjectHtmlRoundtrip();
        testLegacyEditorAndShortcuts();
        testLegacySettingsPassthrough();
        testTreeStatsExtended();
        testTreeIndentOutdent();
        testLegacyI18n();
        testLegacyFeedback();
        testLegacyStartupPathsAndDesktopGeometry();
        testLegacyWindowVisibility();
        testLegacyBackupManagement();
        testLegacyValidationSummaryAndRoundtrip();
        testRtfContentPartsListsAndTables();
        testRtfParagraphDesktopHtmlAndSegments();
        testRtfCssFontAndColorParity();
        testRichCombinedRtfExportPreservesStylesAndParts();
        testLegacyToolbarPrintAndDesktopLayoutModels();
        testLegacyRtfSelectionFormatter();
        testLegacyTxtRtfImporters();
        testLegacyTrayDisplayAndSystemIntegration();
        testLegacyAutosaveRecentMoveWeckerAndBuildRegression();
        testLegacyAutostartOpenTargetsAndRtfTables();
        testLegacyDesktopNoteAutoResizeDecision();
        testLegacyToolbarPresentation();
        testLegacyContextMenusDateAndCurrentNodeExport();
        testLegacyDialogDesktopContextAndFontToolbar();
        testLegacyPasswordFtpSettingsAboutSearchAndAndroidBuildFixes();
        testLegacyAlarmFontSearchEditorFileV17();
        testLegacyScrollExpansionUnifiedMenusDesktopV23();
        testLegacyRichTextBoxCssFocusTreeV29();
        testLegacyRecentWindowDiagnosticsV35();
        testLegacyColorFileDialogsTextExportV47();
        testLegacyConfigPaintSaveSearchV59();
        testLegacyLifecycleTreeTrayV65();
        testLegacyRuntimeLayoutPackagingV71();
        testLegacyActivationToolbarChromeAutosaveBuildV83();
        testLegacyFontWindowStreamPipelineV95();
        testAndroidTreeExpansionResizeAndToolbarV98();
        testAndroidV100RuntimeColorPromptLayout();
        testAndroidV101WidgetAndActiveFormatting();
        testAndroidV102MiddleToolbarActivePane();
        testAndroidV103BlankStartAndExpansionRoundtrip();
        testAndroidV104TouchZoomAndImageSafety();
        testAndroidV105ToolbarZoomStability();
        testAndroidV106RtfToolbarAndQuickSearch();
        testAndroidV107ContextMenusAndTreeDragDrop();
        testAndroidV108WidgetsExportSettings();
        System.out.println("Core tests OK");
        System.exit(0);
    }

    private static void testRtf() {
        String text = "Hallo ä € 😀\n• Punkt";
        String rtf = RtfUtils.plainTextToRtf(text);
        String plain = RtfUtils.rtfToPlainText(rtf);
        if (!text.equals(plain)) throw new AssertionError("RTF roundtrip failed: " + plain);
        String styled = "{\\rtf1\\ansi{\\fonttbl{\\f0 Arial;}}\\pard sicht\\b bar\\par {\\field{\\*\\fldinst PAGE}{\\fldrslt 12}}}";
        String styledPlain = RtfUtils.rtfToPlainText(styled);
        if (!styledPlain.contains("sichtbar") || !styledPlain.contains("12") || styledPlain.contains("PAGE")) {
            throw new AssertionError("RTF field/plain failed: " + styledPlain);
        }
    }

    private static void testAlxRoundtrip() throws Exception {
        NoteDocument d = NoteDocument.newDocument();
        d.root.title = "root";
        d.root.rtf = RtfUtils.plainTextToRtf("root text");
        d.root.expanded = false;
        d.root.extraAttrs.put("future", "keep");
        NoteNode child = d.root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("child body")));
        child.bgArgb = -1;
        byte[] payload = AlxIo.dump(d, "");
        NoteDocument loaded = AlxIo.load(payload, "");
        if (!"root".equals(loaded.root.title)) throw new AssertionError("root title lost");
        if (loaded.root.expanded) throw new AssertionError("expanded lost");
        if (!"keep".equals(loaded.root.extraAttrs.get("future"))) throw new AssertionError("extra attr lost");
        if (!"child body".equals(RtfUtils.rtfToPlainText(loaded.root.children.get(0).rtf))) throw new AssertionError("child rtf lost");

        String futureXml = "<notizen-alx2 future=\"root\"><meta x=\"y\"/><Notiz name=\"root\">body</Notiz><future-node z=\"1\"/></notizen-alx2>";
        NoteDocument future = AlxIo.parseAlxXml(futureXml);
        String serialized = new String(AlxIo.documentToXmlBytes(future), StandardCharsets.UTF_16);
        if (!serialized.contains("future=\"root\"") || !serialized.contains("<meta") || !serialized.contains("future-node")) {
            throw new AssertionError("ALX root passthrough failed: " + serialized);
        }
    }

    private static void testAlxExpansionAliasesAndAndroidUiSetting() throws Exception {
        String xml = "<notizen-alx2><Notiz name=\"root\" IsExpanded=\"False\"><Notiz name=\"kind\" expanded=\"yes\"><Notiz name=\"enkel\" isExpanded=\"0\" /></Notiz></Notiz></notizen-alx2>";
        NoteDocument d = AlxIo.parseAlxXml(xml);
        if (d.root.expanded) throw new AssertionError("IsExpanded alias not honored");
        if (!d.root.children.get(0).expanded) throw new AssertionError("expanded alias not honored");
        if (d.root.children.get(0).children.get(0).expanded) throw new AssertionError("isExpanded numeric false not honored");
        String serialized = new String(AlxIo.documentToXmlBytes(d), StandardCharsets.UTF_16);
        if (!serialized.contains("isexpanded=\"False\"") || serialized.contains("IsExpanded=\"False\"")) {
            throw new AssertionError("canonical isexpanded write failed: " + serialized);
        }

        LegacySettings settings = new LegacySettings();
        settings.androidTreePaneWidthDp = 333;
        LegacySettings loaded = LegacySettings.fromXmlBytes(settings.toXmlBytes());
        if (loaded.androidTreePaneWidthDp != 333) throw new AssertionError("android-ui tree width setting lost: " + loaded.androidTreePaneWidthDp);
        LegacySettings clamped = LegacySettings.fromXmlString("<notizen-alx><android-ui treepane-width-dp=\"5\" /></notizen-alx>");
        if (clamped.androidTreePaneWidthDp != 24) throw new AssertionError("tree width clamp failed: " + clamped.androidTreePaneWidthDp);
    }

    private static void testEncryptedAlx() throws Exception {
        NoteDocument d = NoteDocument.newDocument();
        d.root.rtf = RtfUtils.plainTextToRtf("secret");
        byte[] enc = AlxIo.dump(d, "secret123");
        boolean required = false;
        try { AlxIo.load(enc, ""); } catch (AlxException.PasswordRequired expected) { required = true; }
        if (!required) throw new AssertionError("password should be required");
        NoteDocument loaded = AlxIo.load(enc, "secret123");
        if (!"secret".equals(RtfUtils.rtfToPlainText(loaded.root.rtf))) throw new AssertionError("encrypted read failed");
    }

    private static void testSearchAndExport() {
        NoteNode root = new NoteNode("root", RtfUtils.plainTextToRtf("eins zwei"));
        NoteNode child = root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("zwei-drei zwei")));
        List<SearchResult> all = Search.searchNodes(root, "zwei", false, false, false);
        if (all.size() != 3) throw new AssertionError("search count " + all.size());
        List<SearchResult> whole = Search.searchNodes(root, "zwei", true, false, false);
        if (whole.size() != 2) throw new AssertionError("whole word search count " + whole.size());
        String label = all.get(0).label();
        if (!label.contains("root") || !label.contains(" · ") || !label.contains(": ")) throw new AssertionError("search label failed: " + label);
        String compact = Search.snippet("links\n\tmitte   rechts", 7, 5);
        if (compact.contains("\n") || compact.contains("  ")) throw new AssertionError("search snippet compact failed: " + compact);
        String txt = Exporters.treeToPlainText(root, new ExportOptions());
        if (!txt.contains("1. child") || !txt.contains("zwei-drei")) throw new AssertionError("export failed: " + txt);
        byte[] bytes = Exporters.treeToTextBytes(root, "utf-8");
        if (!new String(bytes, StandardCharsets.UTF_8).contains("\r\n")) throw new AssertionError("CRLF export failed");
        String html = Exporters.treeToHtml(root);
        if (!html.contains("<html lang=\"de\">") || !html.contains("class=\"notizen-node\"") || !html.contains("<h2>1. child</h2>") || !html.contains("white-space:pre-wrap")) {
            throw new AssertionError("HTML export style/numbering failed: " + html);
        }
    }


    private static void testNodeClipboard() throws Exception {
        NoteNode root = new NoteNode("root", RtfUtils.plainTextToRtf("hello"));
        root.expanded = false;
        root.bgArgb = -1;
        root.fgArgb = -2;
        DesktopNoteState desk = new DesktopNoteState();
        desk.argb = -3;
        root.desktopNote = desk;
        root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("child body")));
        String xml = NodeClipboard.nodeToClipboardXml(root);
        if (!NodeClipboard.looksLikeNodeClipboardXml(xml)) throw new AssertionError("clipboard XML not recognized");
        NoteNode loaded = NodeClipboard.nodeFromClipboardXml(xml);
        if (!"root".equals(loaded.title) || loaded.expanded) throw new AssertionError("clipboard node attrs failed");
        if (loaded.desktopNote != null) throw new AssertionError("desktop state must be dropped by default");
        if (loaded.children.size() != 1 || loaded.children.get(0).parent != loaded) throw new AssertionError("clipboard child failed");
        if (!"child body".equals(RtfUtils.rtfToPlainText(loaded.children.get(0).rtf))) throw new AssertionError("clipboard rtf failed");
        String xmlWithDesk = NodeClipboard.nodeToClipboardXml(root, true);
        if (NodeClipboard.nodeFromClipboardXml(xmlWithDesk, true).desktopNote == null) throw new AssertionError("desktop state should be optional");
    }

    private static void testAlarmRules() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        java.util.Calendar c = java.util.Calendar.getInstance(utc);
        c.set(2026, java.util.Calendar.APRIL, 30, 8, 0, 0); c.set(java.util.Calendar.MILLISECOND, 0);
        long start = c.getTimeInMillis();
        AlarmSpec once = new AlarmSpec(start);
        if (AlarmUtils.nextOccurrenceMillis(once, start - 1, utc) != start) throw new AssertionError("one-shot before failed");
        if (AlarmUtils.nextOccurrenceMillis(once, start, utc) != AlarmUtils.NO_OCCURRENCE) throw new AssertionError("one-shot expired failed");
        AlarmSpec daily = new AlarmSpec(start);
        daily.recurrence = AlarmSpec.RECURRENCE_DAILY;
        daily.interval = 2;
        c.set(2026, java.util.Calendar.APRIL, 30, 8, 0, 0);
        long next = AlarmUtils.nextOccurrenceMillis(daily, c.getTimeInMillis(), utc);
        c.set(2026, java.util.Calendar.MAY, 2, 8, 0, 0);
        if (next != c.getTimeInMillis()) throw new AssertionError("daily alarm failed: " + AlarmUtils.formatMillis(next));
        AlarmSpec weekly = new AlarmSpec(start);
        weekly.recurrence = AlarmSpec.RECURRENCE_WEEKLY;
        weekly.weekdays.add(0);
        weekly.weekdays.add(2);
        if (!AlarmUtils.describeRecurrence(weekly).contains("Mo")) throw new AssertionError("weekly description failed");
        NoteNode node = new NoteNode("alarm", "");
        AlarmUtils.writeToNode(node, weekly);
        if (AlarmUtils.fromNode(node) == null) throw new AssertionError("alarm attrs failed");
    }

    private static void testLegacyColorsAndRtfExport() {
        if (LegacyColors.LEGACY_RANDOM_LIGHT_COLOR_ARGB.length != 14) throw new AssertionError("legacy color range failed");
        if (LegacyColors.LEGACY_RANDOM_LIGHT_COLOR_ARGB[0] != LegacyColors.LIGHT_COLOR_ARGB[0]) throw new AssertionError("legacy colors failed");
        NoteNode root = new NoteNode("root", RtfUtils.plainTextToRtf("body ä"));
        String rtf = Exporters.treeToRtf(root);
        if (!rtf.startsWith("{\\rtf1") || !RtfUtils.rtfToPlainText(rtf).contains("body ä")) throw new AssertionError("rtf export failed: " + rtf);
    }


    private static void testLegacySettingsAndFtp() throws Exception {
        if (LegacySettings.normalizeAutosaveSeconds(1) != 5) throw new AssertionError("autosave minimum failed");
        if (LegacySettings.normalizeAutosaveSeconds(0) != 0) throw new AssertionError("autosave disabled failed");
        if (!"Maximized".equals(LegacySettings.normalizeWindowState("maximiert"))) throw new AssertionError("window state failed");
        Map<String, String> slots = new LinkedHashMap<>();
        slots.put("a", "eins.alx");
        slots.put("b", "zwei.alx");
        slots.put("c", "");
        slots.put("d", "vier.alx");
        if (LegacySettings.recentFilesFromSlots(slots).size() != 3) throw new AssertionError("recent slots parse failed");
        LegacySettings settings = new LegacySettings();
        settings.language = "de";
        settings.backupKeep = 7;
        settings.autosaveSeconds = 2;
        settings.ftpHost = "example.org";
        settings.ftpPath = "/daten/datei.alx";
        settings.ftpUsername = "alex user";
        settings.ftpPassword = "pass!";
        settings.rememberFile("content://notizen/eins.alx");
        byte[] xml = settings.toXmlBytes();
        LegacySettings loaded = LegacySettings.fromXmlBytes(xml);
        if (!"de".equals(loaded.language) || loaded.backupKeep != 7 || loaded.autosaveSeconds != 5) throw new AssertionError("settings roundtrip failed");
        if (!"example.org".equals(loaded.ftpHost) || !"/daten/datei.alx".equals(loaded.ftpPath)) throw new AssertionError("ftp settings roundtrip failed");
        FtpTarget target = FtpTarget.fromFields("ftp://alex%20user:pass%21@example.org/Ordner/alte%20Datei.alx", "", "", "");
        if (!"example.org".equals(target.host)) throw new AssertionError("ftp host parse failed");
        if (!"alex user".equals(target.username) || !"pass!".equals(target.password)) throw new AssertionError("ftp credentials parse failed");
        if (!"/Ordner/alte Datei.alx".equals(target.remotePath)) throw new AssertionError("ftp path parse failed: " + target.remotePath);
        if (!"ftp://alex%20user@example.org/Ordner/alte%20Datei.alx".equals(target.safeDisplayUrl())) throw new AssertionError("ftp safe URL failed: " + target.safeDisplayUrl());
        boolean rejected = false;
        try { FtpTarget.fromFields("example.org", "/datei.txt", "", ""); } catch (FtpSyncError expected) { rejected = true; }
        if (!rejected) throw new AssertionError("ftp non-alx path must be rejected");
    }


    private static void testRtfImagesHtmlAndRichExport() {
        byte[] png = onePixelPng();
        String pic = RtfUtils.rtfPictureFromImage(png, "image/png");
        if (pic == null || !pic.contains("\\pngblip")) throw new AssertionError("png picture rtf failed: " + pic);
        String rtf = RtfUtils.appendImageToRtf(RtfUtils.plainTextToRtf("mit Bild"), png, "image/png");
        if (RtfUtils.extractImages(rtf).size() != 1) throw new AssertionError("extract image failed");
        if (!RtfUtils.rtfToPlainText(rtf).contains("mit Bild")) throw new AssertionError("plain text around image failed");
        String html = RtfUtils.rtfToHtml(rtf);
        if (!html.contains("data:image/png;base64,")) throw new AssertionError("html image missing: " + html);

        String link = "{\\rtf1\\ansi Text {\\field{\\*\\fldinst HYPERLINK \"https://example.org\"}{\\fldrslt Beispiel}}\\par}";
        String linkHtml = RtfUtils.rtfToHtml(link);
        if (!linkHtml.contains("href=\"https://example.org\"") || !linkHtml.contains("Beispiel")) throw new AssertionError("hyperlink html failed: " + linkHtml);

        String object = "{\\rtf1\\ansi {\\object\\objclass Package;{\\result Objekt}}}";
        String objectHtml = RtfUtils.rtfToHtml(object);
        if (!objectHtml.contains(RtfUtils.LEGACY_OBJECT_PLACEHOLDER)) throw new AssertionError("object placeholder failed: " + objectHtml);

        NoteNode root = new NoteNode("root", rtf);
        root.addChild(new NoteNode("link", link));
        String exported = Exporters.treeToRtf(root);
        if (!exported.contains("\\pngblip") || !exported.contains("HYPERLINK")) throw new AssertionError("rich combined rtf lost special group: " + exported);
    }

    private static void testHtmlToRtfRichRoundtrip() {
        String png64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";
        String html = "<p><b>Hallo</b> <i>Welt</i> " +
                "<span style=\"color:#ff0000; background-color:#ffff00; font-size:14pt; font-family: Arial; text-decoration: underline line-through\">rot</span> 😀" +
                "<img src=\"data:image/png;base64," + png64 + "\"/></p>";
        String rtf = RtfUtils.htmlToRtf(html);
        if (!rtf.contains("\\b") || !rtf.contains("\\i") || !rtf.contains("\\ul") || !rtf.contains("\\strike")) throw new AssertionError("html rich controls lost: " + rtf);
        if (!rtf.contains("\\cf") || !rtf.contains("\\highlight") || !rtf.contains("Arial") || !rtf.contains("\\pngblip")) throw new AssertionError("html color/font/image controls lost: " + rtf);
        String plain = RtfUtils.rtfToPlainText(rtf);
        if (!plain.contains("Hallo Welt rot 😀")) throw new AssertionError("html plain roundtrip failed: " + plain);
        String rendered = RtfUtils.rtfToHtml(rtf);
        if (!rendered.contains("font-weight:700") || !rendered.contains("font-style:italic") || !rendered.contains("text-decoration:underline line-through")) throw new AssertionError("rendered formatting lost: " + rendered);
        if (!rendered.contains("color:#ff0000") || !rendered.contains("background-color:#ffff00") || !rendered.contains("font-family:&quot;Arial&quot;") || !rendered.contains("data:image/png;base64")) throw new AssertionError("rendered rich attrs lost: " + rendered);
    }

    private static void testStyledRtfHtmlRendering() {
        String rtf = "{\\rtf1\\ansi{\\fonttbl{\\f0 Arial;}{\\f1 Courier New;}}{\\colortbl ;\\red1\\green2\\blue3;\\red255\\green255\\blue224;}" +
                "\\pard\\qc\\li720\\sl360\\slmult1 Titel {\\b Fett}\\plain Normal\\par " +
                "{\\up6 Hoch}{\\dn6 Tief} {\\ulwave Welle} {\\cbpat2 Hintergrund} {\\f1 Code}\\par}";
        String html = RtfUtils.rtfToHtml(rtf);
        if (!html.contains("text-align:center") || !html.contains("margin-left:36pt") || !html.contains("line-height:1.5")) throw new AssertionError("paragraph css lost: " + html);
        if (!html.contains("font-weight:700") || !html.contains("Normal")) throw new AssertionError("plain reset/html text failed: " + html);
        if (!html.contains("vertical-align:super") || !html.contains("vertical-align:sub")) throw new AssertionError("super/sub css lost: " + html);
        if (!html.contains("background-color:#ffffe0") || !html.contains("font-family:&quot;Courier New&quot;")) throw new AssertionError("bg/font css lost: " + html);
        String hidden = RtfUtils.htmlToRtf("<span style=\"display:none\">versteckt</span>");
        if (!hidden.contains("\\v") || RtfUtils.rtfToPlainText(hidden).contains("versteckt")) throw new AssertionError("hidden text not honored: " + hidden + " / " + RtfUtils.rtfToPlainText(hidden));
    }

    private static void testRtfFieldObjectHtmlRoundtrip() {
        String field = "{\\field{\\*\\fldinst PAGE}{\\fldrslt 7}}";
        String object = "{\\object{\\*\\objclass Package}{\\objdata 010203}}";
        String html = RtfUtils.rtfToHtml("{\\rtf1\\ansi vor " + field + " " + object + " nach}");
        if (!html.contains("data-notizen-rtf-field=") || !html.contains("data-notizen-rtf-object=") || !html.contains("7") || !html.contains(RtfUtils.LEGACY_OBJECT_PLACEHOLDER)) throw new AssertionError("field/object html failed: " + html);
        String roundtrip = RtfUtils.htmlToRtf(html);
        if (!roundtrip.contains("\\fldinst PAGE") || !roundtrip.contains("\\object") || !roundtrip.contains("\\objdata 010203")) throw new AssertionError("field/object html roundtrip failed: " + roundtrip);
    }


    private static void testLegacyEditorAndShortcuts() {
        if (!"\r•   ".equals(LegacyEditorActions.legacyClipboardBulletText())) throw new AssertionError("legacy bullet text changed");
        if (!"\n•   ".equals(LegacyEditorActions.androidBulletInsertText())) throw new AssertionError("android bullet text changed");
        LegacyShortcuts.Shortcut save = LegacyShortcuts.resolve("S", true, false, false, false, false);
        if (save == null || !"save".equals(save.action)) throw new AssertionError("ctrl+s shortcut failed");
        if (LegacyShortcuts.resolve("Insert", false, false, false, false, false) != null) throw new AssertionError("tree insert must require tree focus");
        LegacyShortcuts.Shortcut child = LegacyShortcuts.resolve("Insert", false, false, false, true, false);
        if (child == null || !"add_child".equals(child.action) || !child.requiresTreeFocus) throw new AssertionError("tree insert shortcut failed");
        LegacyShortcuts.Shortcut paste = LegacyShortcuts.resolve("Insert", false, true, false, true, false);
        if (paste == null || !"paste_node".equals(paste.action)) throw new AssertionError("shift insert shortcut failed");
        if (LegacyShortcuts.resolve("+", true, false, false, false, false) != null) throw new AssertionError("font shortcut must require editor focus");
        LegacyShortcuts.Shortcut bigger = LegacyShortcuts.resolve("+", true, false, false, false, true);
        if (bigger == null || !"font_bigger".equals(bigger.action) || !bigger.requiresEditorFocus) throw new AssertionError("font shortcut failed");
        if (LegacyShortcuts.resolve("S", true, false, true, false, false) != null) throw new AssertionError("alt shortcut should be ignored");
    }

    private static void testLegacySettingsPassthrough() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-16\"?>" +
                "<notizen-alx future=\"root\">" +
                "<language choice=\"de\" extra=\"keep-lang\"/>" +
                "<open file=\"notizen.alx\" directory=\"C:\\\\alt\" custom=\"keep-open\">" +
                "<once-opened file=\"once.alx\" timestamp=\"123\" nested=\"keep-once\"/>" +
                "</open>" +
                "<mystery a=\"b\"><child>c</child></mystery>" +
                "</notizen-alx>";
        LegacySettings settings = LegacySettings.fromXmlBytes(xml.getBytes(StandardCharsets.UTF_16));
        String out = new String(settings.toXmlBytes(), StandardCharsets.UTF_16);
        if (!out.contains("future=\"root\"")) throw new AssertionError("root passthrough lost: " + out);
        if (!out.contains("extra=\"keep-lang\"")) throw new AssertionError("known attr passthrough lost: " + out);
        if (!out.contains("custom=\"keep-open\"")) throw new AssertionError("open attr passthrough lost: " + out);
        if (!out.contains("nested=\"keep-once\"")) throw new AssertionError("nested attr passthrough lost: " + out);
        if (!out.contains("<mystery") || !out.contains("<child>c</child>")) throw new AssertionError("unknown element passthrough lost: " + out);
    }

    private static void testTreeStatsExtended() {
        NoteNode root = new NoteNode("root", RtfUtils.appendImageToRtf(RtfUtils.plainTextToRtf("eins zwei"), onePixelPng(), "image/png"));
        DesktopNoteState desk = new DesktopNoteState();
        desk.visible = true;
        root.desktopNote = desk;
        NoteNode child = root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("drei\nvier")));
        child.addChild(new NoteNode("leaf", RtfUtils.plainTextToRtf("fünf")));
        TreeStats stats = TreeStats.collect(root);
        if (stats.nodes != 3) throw new AssertionError("stats nodes failed");
        if (stats.leaves != 1) throw new AssertionError("stats leaves failed: " + stats.leaves);
        if (stats.maxDepth != 3) throw new AssertionError("stats depth failed: " + stats.maxDepth);
        if (stats.desktopNotes != 1) throw new AssertionError("stats desktop notes failed");
        if (stats.images != 1) throw new AssertionError("stats image count failed: " + stats.images);
        if (!stats.asLegacyText().contains("Bilder: 1")) throw new AssertionError("stats legacy text failed: " + stats.asLegacyText());
    }

    private static byte[] onePixelPng() {
        return new byte[]{
                (byte)0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
                0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1f, 0x15, (byte)0xc4, (byte)0x89,
                0x00, 0x00, 0x00, 0x0a, 0x49, 0x44, 0x41, 0x54,
                0x78, (byte)0x9c, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
                0x00, 0x01, 0x0d, 0x0a, 0x2d, (byte)0xb4, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4e, 0x44, (byte)0xae, 0x42,
                0x60, (byte)0x82
        };
    }


    private static void testTreeIndentOutdent() {
        NoteNode root = new NoteNode("root", "");
        NoteNode a = root.addChild(new NoteNode("a", ""));
        NoteNode b = root.addChild(new NoteNode("b", ""));
        NoteNode c = root.addChild(new NoteNode("c", ""));
        if (NoteTreeOps.indentUnderPreviousSibling(a) != null) throw new AssertionError("first child must not indent");
        if (NoteTreeOps.indentUnderPreviousSibling(b) != b) throw new AssertionError("indent failed");
        if (root.children.size() != 2 || a.children.size() != 1 || a.children.get(0) != b || b.parent != a) throw new AssertionError("indent structure failed");
        if (!a.expanded) throw new AssertionError("indent should expand target sibling");
        if (NoteTreeOps.outdentAfterParent(b) != b) throw new AssertionError("outdent failed");
        if (root.children.size() != 3 || root.children.get(1) != b || b.parent != root || a.children.size() != 0 || root.children.get(2) != c) throw new AssertionError("outdent structure failed");
    }


    private static void testLegacyI18n() {
        if (LegacyI18n.keyCount() != 118) throw new AssertionError("legacy key count failed: " + LegacyI18n.keyCount());
        if (!"Strip1_1".equals(LegacyI18n.keyForIndex(0)) || !"scroll".equals(LegacyI18n.keyForIndex(117)) || LegacyI18n.keyForIndex(999) != null) throw new AssertionError("legacy key order failed");
        if (!"français".equals(LegacyI18n.resolveLanguage("french"))) throw new AssertionError("french alias failed");
        if (!"spanish".equals(LegacyI18n.resolveLanguage("es"))) throw new AssertionError("spanish alias failed");
        if (!"russian".equals(LegacyI18n.resolveLanguage("Auto", "ru_RU"))) throw new AssertionError("auto ru failed");
        if (!"Nouveau fichier Ctrl + N".equals(LegacyI18n.tr("french", "Strip1_2"))) throw new AssertionError("french translation failed");
        if (!"Configuración".equals(LegacyI18n.tr("spanish", "Strip1_7"))) throw new AssertionError("spanish translation failed");
        if (!"Результат:".equals(LegacyI18n.tr("ru", "suche5"))) throw new AssertionError("russian translation failed");
        if (!"ok".equals(LegacyI18n.languageValues("spanish")[28])) throw new AssertionError("language array order failed");
        if (!LegacyI18n.availableLanguageLabels().contains("Deutsch")) throw new AssertionError("language labels failed");
    }

    private static void testLegacyFeedback() throws Exception {
        long epoch = LegacyFeedback.dotnetDateTicks(1, 1, 1);
        long day2 = LegacyFeedback.dotnetDateTicks(1, 1, 2);
        if (epoch != 0L || day2 != 24L * 60L * 60L * 10_000_000L) throw new AssertionError("dotnet ticks failed");
        long today = LegacyFeedback.dotnetDateTicks(2026, 5, 4);
        if (LegacyFeedback.decision("kurz", 0L, 0, today).allowed) throw new AssertionError("short feedback should block");
        if (!LegacyFeedback.decision("lange genug", 0L, 99, today).allowed) throw new AssertionError("new-day feedback should allow");
        if (!LegacyFeedback.decision("lange genug", today, 20, today).allowed) throw new AssertionError("same-day feedback should allow");
        LegacyFeedback.FeedbackDecision blocked = LegacyFeedback.decision("lange genug", today, 30, today);
        if (blocked.allowed || !"no-send".equals(blocked.reason)) throw new AssertionError("feedback throttle failed");
        if (LegacyFeedback.nextState(0L, 30, today).count != 0) throw new AssertionError("feedback reset failed");
        if (LegacyFeedback.nextState(today, 10, today).count != 10 + LegacyFeedback.LEGACY_FEEDBACK_DAILY_INCREMENT) throw new AssertionError("feedback increment failed");
        File dir = new File(System.getProperty("java.io.tmpdir"), "notizen-feedback-test-" + System.nanoTime());
        File written = LegacyFeedback.writeLocalFeedbackArchive("Hallo Feedback", dir, new java.util.Date(1777894205000L));
        GZIPInputStream gz = new GZIPInputStream(new java.io.FileInputStream(written));
        byte[] bytes = readAll(gz); gz.close();
        if (!"Hallo Feedback".equals(new String(bytes, java.nio.charset.Charset.forName("UTF-16LE")))) throw new AssertionError("feedback gzip text failed");
        written.delete(); dir.delete();
        LegacySettings settings = LegacySettings.fromXmlString("<notizen-alx><x y=\"638819712000000000\" z=\"20\" a=\"60\" extra=\"keep\" /></notizen-alx>");
        if (settings.feedbackDayTicks != 638819712000000000L || settings.feedbackCount != 20) throw new AssertionError("settings feedback long failed");
        String out = new String(settings.toXmlBytes(), StandardCharsets.UTF_16);
        if (!out.contains("y=\"638819712000000000\"") || !out.contains("extra=\"keep\"")) throw new AssertionError("settings feedback passthrough failed: " + out);
    }

    private static byte[] readAll(java.io.InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buf = new byte[4096]; int n; while ((n = in.read(buf)) != -1) out.write(buf, 0, n); return out.toByteArray();
    }

    private static void testLegacyStartupPathsAndDesktopGeometry() {
        LegacyStartup.StartupOptions options = LegacyStartup.parseLegacyStartupArgs("/min", "C:\\Users\\me\\demo.alx", "--password", "secret", "--modern");
        if (!options.minimized || !"C:\\Users\\me\\demo.alx".equals(options.file)) throw new AssertionError("startup parse failed");
        if (!options.cleanedArgs.contains("--password") || !options.cleanedArgs.contains("secret") || !options.cleanedArgs.contains("--modern")) throw new AssertionError("startup cleaned args failed: " + options.cleanedArgs);
        LegacyStartup.StartupTargetValidation missing = LegacyStartup.validateLegacyStartupTarget(options, path -> false);
        if (missing.options.file != null || missing.missingFile == null) throw new AssertionError("startup missing validation failed");
        LegacyStartup.StartupOptions ftp = LegacyStartup.parseLegacyStartupArgs("-min", "ftp://example.invalid/notizen.alx");
        if (LegacyStartup.validateLegacyStartupTarget(ftp, path -> false).options.file == null) throw new AssertionError("ftp startup must not be local-validated");
        java.util.List<String> recent = Arrays.asList("C:\\old.alx", "", "ftp://example.invalid/notizen.alx", "C:\\newest.alx");
        if (!"C:\\newest.alx".equals(LegacyStartup.legacyAutostartTargetFile(recent))) throw new AssertionError("autostart target failed");
        java.util.List<String> args = LegacyStartup.legacyAutostartArguments(true, true, recent);
        if (!args.equals(Arrays.asList("-min", "C:\\newest.alx"))) throw new AssertionError("autostart args failed: " + args);
        String command = LegacyStartup.buildAutostartCommand("C:\\Python311\\python.exe", "notizen_py_qt", args);
        if (!command.contains("notizen_py_qt") || !command.contains("-min") || !command.contains("C:\\newest.alx")) throw new AssertionError("autostart command failed: " + command);
        String[] split = LegacyPaths.splitLegacyFileLocation("C:\\Users\\me\\Notizen\\demo.alx", "fallback");
        if (!"C:\\Users\\me\\Notizen".equals(split[0]) || !"demo.alx".equals(split[1])) throw new AssertionError("path split failed");
        String[] empty = LegacyPaths.splitLegacyFileLocation("", "/home/me/Documents/Notizen");
        if (!"unbenannt.alx".equals(empty[1])) throw new AssertionError("default filename failed");
        ArrayList<String> rotated = new ArrayList<>();
        String selected = LegacySettings.legacyActivateRecentFile(Arrays.asList("a", "b", "c", "d"), 1, rotated);
        if (!"b".equals(selected) || !rotated.equals(Arrays.asList("a", "c", "d", "b"))) throw new AssertionError("recent activation failed: " + rotated);
        LegacyDesktopNote.Rect rect = new LegacyDesktopNote.Rect(100, 200, 300, 400);
        LegacyDesktopNote.Rect expanded = LegacyDesktopNote.hoverGeometry(rect);
        if (!expanded.equals(new LegacyDesktopNote.Rect(88, 168, 326, 448))) throw new AssertionError("desktop hover geometry failed: " + expanded);
        if (!LegacyDesktopNote.hiddenBorderGeometry(expanded).equals(rect)) throw new AssertionError("desktop hidden geometry failed");
        if (LegacyDesktopNote.opacityForActive(0.35) != 1.0) throw new AssertionError("active opacity failed");
        if (Math.abs(LegacyDesktopNote.opacityForInactive("0.35") - 0.35) > 0.0001) throw new AssertionError("inactive opacity failed");
        if (!"hide".equals(LegacyDesktopNote.titleHitAction(10, 10, 240))) throw new AssertionError("hide hit failed");
        if (!"close".equals(LegacyDesktopNote.titleHitAction(230, 10, 240))) throw new AssertionError("close hit failed");
        if (!"resize".equals(LegacyDesktopNote.mouseMoveAction(230, 390, 240, 400))) throw new AssertionError("resize zone failed");
        if (LegacyDesktopNote.opacityPercentForTransparencyPercent(80) != 20) throw new AssertionError("transparency mapping failed");
    }

    private static void testLegacyWindowVisibility() {
        if (LegacyWindowVisibility.legacyWindowStateIsRestorable(0, 60)) throw new AssertionError("zero window should not restore");
        if (!LegacyWindowVisibility.legacyWindowStateIsRestorable(60, 60)) throw new AssertionError("nonzero window should restore");
        if (LegacyWindowVisibility.shouldStartMinimized(true, false, "Normal", true, false, true)) throw new AssertionError("force-visible override failed");
        if (!LegacyWindowVisibility.shouldStartMinimized(false, false, "Minimized", false, false, true)) throw new AssertionError("stored minimized failed");
        LegacyWindowVisibility.VisibleWindowGeometry negative = LegacyWindowVisibility.sanitizeLegacyWindowGeometry(-5000, -3000, 900, 600, 0, 0, 1920, 1080, false);
        if (!negative.reset || negative.x < 0 || negative.y < 0) throw new AssertionError("negative geometry failed");
        LegacyWindowVisibility.VisibleWindowGeometry forced = LegacyWindowVisibility.sanitizeLegacyWindowGeometry(400, 300, 900, 600, 10, 20, 1280, 800, true);
        if (!forced.reset || forced.x < 10 || forced.y < 20) throw new AssertionError("forced geometry failed");
        java.util.Map<String, String> env = new java.util.LinkedHashMap<>(); env.put("NOTIZEN_FORCE_VISIBLE", "1");
        if (!LegacyWindowVisibility.envRequestsWindowReset(env)) throw new AssertionError("env reset failed");
    }

    private static void testLegacyBackupManagement() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"), "notizen-backup-test-" + System.nanoTime());
        if (!dir.mkdirs()) throw new AssertionError("tmp backup dir failed");
        File target = new File(dir, "notes.alx");
        java.nio.file.Files.write(target.toPath(), "new content".getBytes(StandardCharsets.UTF_8));
        File backupDir = LegacyBackup.backupDirectoryFor(target);
        if (!backupDir.getName().equals("notes")) throw new AssertionError("backup dir failed: " + backupDir);
        if (!"notes-*.alx".equals(LegacyBackup.backupFilePattern(target))) throw new AssertionError("backup pattern failed");
        if (!backupDir.mkdirs()) throw new AssertionError("backup subdir failed");
        String[] names = {"notes-2024-01-01-01-01-01-001.alx", "notes-2024-01-02-01-01-01-001.alx", "notes-2024-01-03-01-01-01-001.alx"};
        for (String name : names) java.nio.file.Files.write(new File(backupDir, name).toPath(), name.getBytes(StandardCharsets.UTF_8));
        if (LegacyBackup.parseLegacyBackupTimestamp(new File(backupDir, names[0]), target) == null) throw new AssertionError("backup timestamp parse failed");
        if (LegacyBackup.parseLegacyBackupTimestamp(new File(backupDir, "other-2024-01-01-01-01-01-001.alx"), target) != null) throw new AssertionError("foreign backup should not parse");
        File created = LegacyBackup.createBackup(target, 2);
        if (created == null || !created.exists() || created.length() != "new content".getBytes(StandardCharsets.UTF_8).length) throw new AssertionError("backup create failed");
        java.util.List<LegacyBackup.BackupEntry> backups = LegacyBackup.listBackups(target);
        if (backups.size() != 2 || backups.get(backups.size() - 1).path == null) throw new AssertionError("backup prune/list failed: " + backups.size());
        java.util.List<File> removed = LegacyBackup.pruneBackups(target, 0);
        if (removed.size() != 2 || !LegacyBackup.listBackups(target).isEmpty()) throw new AssertionError("backup prune zero failed");
        target.delete(); backupDir.delete(); dir.delete();
    }

    private static void testLegacyValidationSummaryAndRoundtrip() throws Exception {
        NoteDocument d = NoteDocument.newDocument();
        d.root.title = "root";
        d.root.rtf = RtfUtils.plainTextToRtf("root text");
        NoteNode child = d.root.addChild(new NoteNode("child", RtfUtils.appendImageToRtf(RtfUtils.plainTextToRtf("child body"), onePixelPng(), "image/png")));
        child.expanded = false;
        child.bgArgb = 123;
        child.extraAttrs.put("future", "keep");
        child.desktopNote = new DesktopNoteState();
        child.desktopNote.visible = true;
        child.desktopNote.x = 12;
        child.desktopNote.y = 34;
        LegacyValidation.LegacyAlxSummary summary = LegacyValidation.summarizeDocument(d);
        if (summary.nodeCount != 2 || summary.maxDepth != 1 || summary.desktopNoteCount != 1 || summary.visibleDesktopNoteCount != 1) throw new AssertionError("legacy validation counters failed");
        if (summary.embeddedImageCount != 1 || summary.treeShapeHash.length() != 64 || summary.contentHash.length() != 64) throw new AssertionError("legacy validation hashes/images failed: " + summary.asLegacyText());
        LegacyValidation.LegacyAlxRoundtripResult direct = LegacyValidation.validateDocumentRoundtrip(d, "");
        if (!direct.ok || !direct.differences.isEmpty()) throw new AssertionError("document validation roundtrip failed: " + direct.differences);
        byte[] payload = AlxIo.dump(d, "");
        LegacyValidation.LegacyAlxRoundtripResult bytes = LegacyValidation.validateAlxRoundtripBytes(payload, "");
        if (!bytes.ok || !bytes.before.equals(bytes.after)) throw new AssertionError("byte validation roundtrip failed: " + bytes.differences);
    }


    private static void testRtfContentPartsListsAndTables() {
        String legacyList = "{\\rtf1\\ansi{\\*\\pntext\\f0 \\'b7\\tab}Legacy item\\par{\\*\\listtext\\tab}Next\\par}";
        String listPlain = RtfUtils.rtfToPlainText(legacyList);
        if (listPlain.isEmpty() || listPlain.charAt(0) != '\u00b7' || !listPlain.contains("\tLegacy item") || !listPlain.contains("\tNext")) {
            throw new AssertionError("legacy list text failed: " + listPlain.replace("\t", "<tab>"));
        }

        String html = "<ol><li>Alpha</li><li>Beta</li></ol>" +
                "<table><tr><td>A</td><td>B</td></tr><tr><td>C</td><td>D</td></tr></table>";
        String fromHtml = RtfUtils.htmlToRtf(html);
        String plain = RtfUtils.rtfToPlainText(fromHtml);
        if (!plain.contains("1. Alpha") || !plain.contains("2. Beta") || !plain.contains("A\tB") || !plain.contains("C\tD")) {
            throw new AssertionError("HTML list/table to RTF failed: " + plain.replace("\t", "<tab>"));
        }

        String fieldObject = "{\\rtf1\\ansi" +
                "{\\field{\\*\\fldinst SYMBOL 55}{\\fldrslt 7}}" +
                "{\\field{\\*\\fldinst HYPERLINK \"https://example.invalid\"}{\\fldrslt Link}}" +
                "{\\object\\objclass Paint.Picture {\\*\\objdata 0102}}}";
        List<RtfContentPart> parts = RtfUtils.rtfToContentParts(fieldObject);
        boolean field = false;
        boolean link = false;
        boolean object = false;
        for (RtfContentPart part : parts) {
            if (part instanceof RtfField && "7".equals(part.text)) field = true;
            if (part instanceof RtfHyperlink && "https://example.invalid".equals(((RtfHyperlink) part).url) && "Link".equals(part.text)) link = true;
            if (part instanceof RtfObject && "Paint.Picture".equals(((RtfObject) part).className)) object = true;
        }
        if (!field || !link || !object) throw new AssertionError("content part field/link/object failed: " + parts.size());
    }

    private static void testRtfParagraphDesktopHtmlAndSegments() {
        String rtf = "{\\rtf1\\ansi{\\fonttbl{\\f0 MS Sans Serif;}{\\f1 Courier New;}}" +
                "{\\colortbl ;\\red255\\green0\\blue0;\\red0\\green255\\blue0;}" +
                "\\pard\\qc\\li720\\ri360\\fi-180\\sb120\\sa240\\sl360\\slmult1\\rtlpar" +
                "\\f1\\fs28\\cf1\\highlight2\\b Kopf\\super 2\\nosupersub\\line Weiter\\par}";
        String html = RtfUtils.rtfToHtml(rtf);
        if (!html.contains("text-align:center") || !html.contains("margin-left:36pt") || !html.contains("text-indent:-9pt")
                || !html.contains("line-height:1.5") || !html.contains("direction:rtl") || !html.contains("vertical-align:super")
                || !html.contains("<br/>") || !html.contains("font-family:&quot;Courier New&quot;")) {
            throw new AssertionError("paragraph/styled HTML failed: " + html);
        }
        String document = RtfUtils.rtfToHtmlDocument(rtf);
        if (!document.startsWith("<!doctype html>") || !document.contains("white-space:pre-wrap")) throw new AssertionError("html document wrapper failed");
        String plain = RtfUtils.rtfToPlainText(rtf);
        if (!plain.contains("Kopf2\nWeiter")) throw new AssertionError("line/super plain failed: " + plain.replace("\n", "<n>"));

        List<RtfTextSegment> segments = RtfUtils.rtfToTextSegments(rtf);
        RtfTextStyle base = null;
        boolean superSegment = false;
        for (RtfTextSegment segment : segments) {
            if (segment.text.contains("Kopf")) base = segment.style;
            if (segment.text.contains("2") && "super".equals(segment.style.vertical)) superSegment = true;
        }
        if (base == null || !base.bold || !"center".equals(base.align) || base.leftIndentTwips != 720 || base.rightIndentTwips != 360
                || base.firstIndentTwips != -180 || base.spaceBeforeTwips != 120 || base.spaceAfterTwips != 240
                || base.lineSpacingTwips != 360 || !base.lineSpacingMultiple || !"rtl".equals(base.direction)
                || !"Courier New".equals(base.fontFamily) || base.fontSizeHalfPoints == null || base.fontSizeHalfPoints != 28
                || !"#ff0000".equals(base.fgColor) || !"#00ff00".equals(base.bgColor) || !superSegment) {
            throw new AssertionError("RTF text segment styles failed");
        }

        String desktop = RtfUtils.rtfToDesktopHtml("{\\rtf1\\ansi Erste\\line Zweite\\par Dritte\\par}");
        if (!desktop.contains("Erste<br/>Zweite<br/>Dritte") || desktop.contains("<p")) {
            throw new AssertionError("desktop compact html failed: " + desktop);
        }
    }

    private static void testRtfCssFontAndColorParity() {
        String tableRtf = "{\\rtf1\\ansi{\\fonttbl{\\f0\\fnil MS Sans Serif;}{\\f1\\fnil WordPad{\\*\\falt Courier New};}}" +
                "{\\colortbl \\red1\\green2\\blue3;\\red4\\green5\\blue6;} Text}";
        Map<Integer, String> fonts = RtfUtils.extractFontTable(tableRtf);
        if (!"MS Sans Serif".equals(fonts.get(0)) || !"WordPad".equals(fonts.get(1))) throw new AssertionError("font table/falt failed: " + fonts);
        Map<Integer, String> colors = RtfUtils.extractColorTable(tableRtf);
        if (!"".equals(colors.get(0)) || !"#010203".equals(colors.get(1)) || !"#040506".equals(colors.get(2)) || colors.containsKey(3)) {
            throw new AssertionError("color table no automatic slot failed: " + colors);
        }

        String cssHtml = "<body text='#102030' bgcolor='#ffeecc'><p style='text-align:right; margin-left:36pt; margin-right:18pt; " +
                "text-indent:-9pt; margin-top:6pt; margin-bottom:12pt; line-height:150%; direction:rtl'>" +
                "<span style='font-weight:700; font-style:italic; text-decoration: underline line-through; " +
                "font-variant: small-caps; text-transform: uppercase; letter-spacing:2pt; display:none'>hide</span>" +
                "<span style='color:#112233; background-color:#445566; font-family:\"Courier New\"; font-size:14pt'>show</span>" +
                "<sup>x</sup><sub>y</sub></p></body>";
        String cssRtf = RtfUtils.htmlToRtf(cssHtml);
        if (!cssRtf.contains("\\qr") || !cssRtf.contains("\\li720") || !cssRtf.contains("\\ri360") || !cssRtf.contains("\\fi-180")
                || !cssRtf.contains("\\sb120") || !cssRtf.contains("\\sa240") || !cssRtf.contains("\\sl360\\slmult1")
                || !cssRtf.contains("\\rtlpar\\rtlch") || !cssRtf.contains("\\scaps") || !cssRtf.contains("\\caps")
                || !cssRtf.contains("\\expndtw40") || !cssRtf.contains("\\v") || !cssRtf.contains("\\fs28")
                || !cssRtf.contains("\\up6") || !cssRtf.contains("\\dn6")) {
            throw new AssertionError("CSS to RTF controls failed: " + cssRtf);
        }
        String visible = RtfUtils.rtfToPlainText(cssRtf);
        if (visible.contains("hide") || !visible.contains("showxy")) throw new AssertionError("hidden/CSS plain text failed: " + visible);
    }

    private static void testRichCombinedRtfExportPreservesStylesAndParts() {
        String rich = "{\\rtf1\\ansi{\\fonttbl{\\f0 MS Sans Serif;}{\\f1 Courier New;}}" +
                "{\\colortbl ;\\red17\\green34\\blue51;\\red68\\green85\\blue102;}" +
                "\\pard\\qc\\li720\\ri360\\fi-180\\sb120\\sa240\\sl360\\slmult1\\rtlpar" +
                "\\f1\\fs28\\cf1\\highlight2\\b\\i\\ul\\strike Formatiert " +
                "\\super 2\\nosupersub\\par" +
                "{\\field{\\*\\fldinst PAGE}{\\fldrslt 7}}" +
                "{\\object\\objclass Package {\\*\\objdata 0102}}" +
                "}";
        NoteNode root = new NoteNode("Root", rich);
        String combined = Exporters.treeToRtf(root);
        if (!combined.contains("Courier New") || !combined.contains("\\red17\\green34\\blue51") || !combined.contains("\\red68\\green85\\blue102")) {
            throw new AssertionError("combined RTF font/color tables failed: " + combined);
        }
        for (String token : new String[]{"\\qc", "\\li720", "\\ri360", "\\fi-180", "\\sb120", "\\sa240", "\\sl360", "\\slmult1", "\\rtlpar\\rtlch", "\\f1", "\\b", "\\i", "\\ul", "\\strike", "\\super", "\\fs28", "\\cf1", "\\highlight2"}) {
            if (!combined.contains(token)) throw new AssertionError("combined RTF lost token " + token + ": " + combined);
        }
        if (!combined.contains("\\fldinst PAGE") || !combined.contains("\\object") || !combined.contains("\\objdata 0102")) {
            throw new AssertionError("combined RTF lost raw field/object: " + combined);
        }
        String plain = RtfUtils.rtfToPlainText(combined);
        if (!plain.contains("Root") || !plain.contains("Formatiert 2") || !plain.contains("7") || !plain.contains(RtfUtils.LEGACY_OBJECT_PLACEHOLDER)) {
            throw new AssertionError("combined RTF plain failed: " + plain);
        }

        byte[] bmp = RtfUtils.dibToBmpBytes(tinyDib());
        String bmpRtf = RtfUtils.htmlToRtf("<p>Vor <img src=\"data:image/bmp;base64," + java.util.Base64.getEncoder().encodeToString(bmp) + "\"/> Nach</p>");
        String bmpCombined = Exporters.treeToRtf(new NoteNode("BMP", bmpRtf));
        if (!bmpCombined.contains("\\dibitmap0") || !bmpCombined.contains(tinyDib().length == 0 ? "never" : bytesToHex(tinyDib()).substring(0, 24))) {
            throw new AssertionError("combined RTF lost legacy BMP DIB payload: " + bmpCombined);
        }
    }

    private static byte[] tinyDib() {
        return new byte[]{
                40, 0, 0, 0,
                1, 0, 0, 0,
                1, 0, 0, 0,
                1, 0,
                24, 0,
                0, 0, 0, 0,
                4, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, (byte)0xff, 0
        };
    }

    private static String bytesToHex(byte[] data) {
        StringBuilder out = new StringBuilder(data.length * 2);
        for (byte b : data) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }

    private static void testLegacyToolbarPrintAndDesktopLayoutModels() {
        if (!LegacyRichTextToolbar.TOOLBAR_OBJECT_NAME.equals("ToolStrip_fontstyle")) throw new AssertionError("toolbar object name failed");
        if (LegacyRichTextToolbar.fontActions().size() < 15) throw new AssertionError("toolbar action count failed");
        LegacyRichTextToolbar.ActionSpec bold = LegacyRichTextToolbar.findByObjectName("ToolStrip_bold");
        if (bold == null || !bold.checkable || !"format_bold".equals(bold.action) || !"Ctrl+B".equals(bold.shortcut)) throw new AssertionError("bold toolbar mapping failed");
        if (LegacyRichTextToolbar.findByAction("legacy_bullet") == null || !LegacyRichTextToolbar.iconOnlyDescription(bold).contains("Fett")) throw new AssertionError("toolbar lookup/icon description failed");
        if (LegacyRichTextToolbar.normalizeFontSize(1) != 6 || LegacyRichTextToolbar.normalizeFontSize(1000) != 99) throw new AssertionError("toolbar font size clamp failed");

        NoteNode root = new NoteNode("Root: bad/name", RtfUtils.plainTextToRtf("Root body"));
        NoteNode child = root.addChild(new NoteNode("Child", RtfUtils.plainTextToRtf("Child body")));
        NoteDocument doc = new NoteDocument();
        doc.root = root;
        LegacyPrintLayout.PrintJob current = LegacyPrintLayout.currentNote(child);
        LegacyPrintLayout.PrintJob subtree = LegacyPrintLayout.subtree(root);
        LegacyPrintLayout.PrintJob all = LegacyPrintLayout.root(doc);
        if (current.scope != LegacyPrintLayout.Scope.CURRENT_NOTE || !current.html.contains("Child body")) throw new AssertionError("current print job failed");
        if (subtree.scope != LegacyPrintLayout.Scope.CURRENT_SUBTREE || !subtree.html.contains("Root") || !subtree.html.contains("Child body")) throw new AssertionError("subtree print job failed");
        if (all.scope != LegacyPrintLayout.Scope.ROOT || !all.html.startsWith("<!doctype html>")) throw new AssertionError("root print job failed");
        if (!"Root_bad_name.html".equals(LegacyPrintLayout.safeFileName("Root: bad/name", ".html"))) throw new AssertionError("print safe filename failed: " + LegacyPrintLayout.safeFileName("Root: bad/name", ".html"));
        if (!"Gesamter Baum".equals(LegacyPrintLayout.labelForScope(LegacyPrintLayout.Scope.ROOT))) throw new AssertionError("print label failed");

        DesktopNoteState state = new DesktopNoteState();
        state.argb = 0x80ffcc00;
        state.opacity = 0.5;
        state.visible = true;
        String css = LegacyDesktopNoteRendering.androidCardCss(state);
        if (!css.contains("rgba(255,204,0,0.251)") || !css.contains("padding:0px") || !css.contains("line-height:100%")) throw new AssertionError("desktop css failed: " + css);
        String compact = LegacyDesktopNoteRendering.compactHtml(new NoteNode("desk", "{\\rtf1\\ansi Erste\\line Zweite\\par Dritte\\par}"));
        if (!compact.contains("line-height:100%") || compact.contains("<p") || !compact.endsWith("Erste<br/>Zweite<br/>Dritte</body></html>")) throw new AssertionError("desktop compact html failed: " + compact);
        if (!LegacyDesktopNoteRendering.shouldUseBottomHint(state) || !LegacyDesktopNoteRendering.minimizedSemantics().contains("showMinimized")) throw new AssertionError("desktop semantics failed");
    }

    private static void testLegacyRtfSelectionFormatter() {
        String bold = LegacyRtfSelectionFormatter.applyToSelection("eins zwei drei", 5, 9, "format_bold");
        if (!bold.contains("{\\b zwei}") || !"eins zwei drei".equals(RtfUtils.rtfToPlainText(bold))) throw new AssertionError("selection bold failed: " + bold);
        String italicAll = LegacyRtfSelectionFormatter.applyToSelection("alles", 2, 2, "format_italic");
        if (!italicAll.contains("\\i alles")) throw new AssertionError("zero selection format-all failed: " + italicAll);
        String centered = LegacyRtfSelectionFormatter.applyToSelection("absatz", 0, 6, "align_center");
        if (!centered.contains("\\pard\\qc") || !RtfUtils.rtfToHtml(centered).contains("text-align:center")) throw new AssertionError("align center failed: " + centered);
        String size = LegacyRtfSelectionFormatter.applyFontSizeToSelection("klein groß", 6, 10, 120);
        if (!size.contains("\\fs198")) throw new AssertionError("font size clamp/halfpoint failed: " + size);
        String family = LegacyRtfSelectionFormatter.applyFontFamilyToSelection("abc def", 4, 7, "Courier New");
        if (!family.contains("Courier New") || !RtfUtils.rtfToHtml(family).contains("font-family:&quot;Courier New&quot;")) throw new AssertionError("font family selection failed: " + family);
    }

    private static void testLegacyTxtRtfImporters() {
        byte[] utf8Bom = new byte[]{(byte)0xef, (byte)0xbb, (byte)0xbf, 'H', 'i'};
        if (!"Hi".equals(LegacyImporters.decodeTextBytes(utf8Bom))) throw new AssertionError("utf8 bom import failed");
        byte[] cp1252 = new byte[]{(byte)0xe4, ' ', (byte)0x80};
        if (!"ä €".equals(LegacyImporters.decodeTextBytes(cp1252, true))) throw new AssertionError("ansi prefer import failed: " + LegacyImporters.decodeTextBytes(cp1252, true));
        String txtRtf = LegacyImporters.txtBytesToRtf("Hallo".getBytes(StandardCharsets.UTF_8));
        if (!RtfUtils.rtfToPlainText(txtRtf).equals("Hallo")) throw new AssertionError("txt import rtf failed");
        String sourceRtf = "{\\rtf1\\ansi\\b Fett}";
        if (!LegacyImporters.rtfBytesToRtf(sourceRtf.getBytes(java.nio.charset.Charset.forName("windows-1252"))).contains("\\b Fett")) throw new AssertionError("rtf import preserve failed");
        String fallback = LegacyImporters.rtfBytesToRtf("kein rtf".getBytes(StandardCharsets.UTF_8));
        if (!RtfUtils.looksLikeRtf(fallback) || !RtfUtils.rtfToPlainText(fallback).contains("kein rtf")) throw new AssertionError("rtf import fallback failed");
    }

    private static void testLegacyTrayDisplayAndSystemIntegration() {
        LinkedHashMap<String, String> env = new LinkedHashMap<>();
        env.put("XDG_CURRENT_DESKTOP", "GNOME");
        if (!LegacyTraySupport.isGnomeSession(env)) throw new AssertionError("gnome session detection failed");
        List<String> exts = LegacyTraySupport.parseGnomeExtensionList("appindicatorsupport@rgcjonas.gmail.com\nother@example\n");
        if (!LegacyTraySupport.hasKnownGnomeTrayExtension(exts)) throw new AssertionError("gnome extension detection failed");
        LegacyTraySupport.TrayDecision safe = LegacyTraySupport.decideStartupTrayVisibility(true, false, true, env, exts, false);
        if (safe.hideToTray || !safe.gnomeSession || !safe.trayExtensionDetected) throw new AssertionError("gnome safe tray decision failed: " + safe.reason);
        LegacyTraySupport.TrayDecision forced = LegacyTraySupport.decideStartupTrayVisibility(true, false, true, env, exts, true);
        if (!forced.hideToTray) throw new AssertionError("forced tray decision failed");

        LinkedHashMap<String, String> display = new LinkedHashMap<>();
        display.put("XDG_CURRENT_DESKTOP", "GNOME");
        display.put("WAYLAND_DISPLAY", "wayland-0");
        display.put("DISPLAY", ":1");
        display.put("GDK_BACKEND", "x11");
        display.put("QT_QPA_PLATFORM", "xcb");
        display.put("QT_QPA_PLATFORMTHEME", "gtk3");
        LegacyDisplayEnvironment.DisplayEnvironmentDecision decision = LegacyDisplayEnvironment.normalizeQtDisplayEnvironment(Arrays.asList("--show", "--no-tray"), display);
        if (!decision.changed || !"wayland;xcb".equals(display.get("QT_QPA_PLATFORM")) || !":0".equals(display.get("DISPLAY"))
                || display.containsKey("GDK_BACKEND") || display.containsKey("QT_QPA_PLATFORMTHEME") || !":1".equals(display.get("NOTIZEN_ORIGINAL_DISPLAY"))) {
            throw new AssertionError("display normalization failed: " + decision.summary());
        }
        LinkedHashMap<String, String> smoke = new LinkedHashMap<>();
        smoke.put("DISPLAY", ":0");
        smoke.put("WAYLAND_DISPLAY", "wayland-0");
        smoke.put("GDK_BACKEND", "x11");
        LegacyDisplayEnvironment.DisplayEnvironmentDecision smokeDecision = LegacyDisplayEnvironment.normalizeQtDisplayEnvironment(Arrays.asList("--smoke-test"), smoke);
        if (!smokeDecision.changed || !"offscreen".equals(smoke.get("QT_QPA_PLATFORM")) || smoke.containsKey("DISPLAY") || smoke.containsKey("WAYLAND_DISPLAY")) {
            throw new AssertionError("smoke normalization failed: " + smokeDecision.summary());
        }

        String cmd = LegacySystemIntegration.buildWindowsModuleOpenCommand("C:\\Program Files\\Python\\python.exe", "notizen_py_qt", true, true, true);
        if (!cmd.startsWith("\"C:\\Program Files\\Python\\python.exe\" -m notizen_py_qt") || !cmd.endsWith("\"%1\"")) {
            throw new AssertionError("windows module command failed: " + cmd);
        }
        List<LegacySystemIntegration.WindowsRegistryEntry> entries = LegacySystemIntegration.legacyWindowsAlxRegistryEntries(cmd, "C:\\Notizen\\notizen.ico", null, null);
        if (entries.size() != 10 || !"Notizenfile".equals(entries.get(0).value) || !"notizenfile".equals(entries.get(4).name)) {
            throw new AssertionError("windows association entries failed: " + entries.size());
        }
        List<String> lines = LegacySystemIntegration.windowsAssociationPreviewLines(entries);
        if (!lines.get(8).contains("Shell\\Open\\Command") || !lines.get(9).contains("DefaultIcon")) throw new AssertionError("preview lines failed: " + lines);
        String linuxExec = LegacySystemIntegration.buildLinuxDesktopExec("notizen_py_qt", "python3", true, "notizen-py-qt", true, true, true, "%f");
        if (!"env NOTIZEN_RESET_WINDOW=1 RESOURCE_NAME=notizen-py-qt python3 -m notizen_py_qt --show --no-tray --reset-window %f".equals(linuxExec)) {
            throw new AssertionError("linux desktop exec failed: " + linuxExec);
        }
    }


    private static void testLegacyAutosaveRecentMoveWeckerAndBuildRegression() throws Exception {
        LegacyAutosave.Decision save = LegacyAutosave.decide(true, true, 60, LegacyAutosave.TargetKind.ANDROID_URI);
        if (!save.shouldSave || save.intervalSeconds != 60 || save.targetKind != LegacyAutosave.TargetKind.ANDROID_URI) {
            throw new AssertionError("autosave positive decision failed: " + save.summary());
        }
        if (LegacyAutosave.decide(true, false, 60, LegacyAutosave.TargetKind.ANDROID_URI).shouldSave) throw new AssertionError("autosave unchanged should skip");
        if (LegacyAutosave.decide(true, true, 0, LegacyAutosave.TargetKind.ANDROID_URI).shouldSave) throw new AssertionError("autosave disabled should skip");
        if (LegacyAutosave.decide(true, true, 60, LegacyAutosave.TargetKind.NONE).shouldSave) throw new AssertionError("autosave without target should skip");
        if (LegacyAutosave.normalizedDelayMillis(1) != 5000L) throw new AssertionError("autosave minimum delay failed");
        if (LegacyAutosave.targetKindFromPath("ftp://user@example.org/x.alx") != LegacyAutosave.TargetKind.FTP) throw new AssertionError("ftp target kind failed");
        if (LegacyAutosave.targetKindFromPath("content://notizen/x.alx") != LegacyAutosave.TargetKind.ANDROID_URI) throw new AssertionError("content target kind failed");
        if (LegacyAutosave.androidTargetKind(false, true, false) != LegacyAutosave.TargetKind.FILE) throw new AssertionError("raw file target kind failed");
        if (!LegacyAutosave.decideForLegacyFile(true, true, true, true, 30).shouldSave) throw new AssertionError("legacy file autosave failed");
        if (LegacyAutosave.decideForLegacyFile(true, true, false, true, 30).shouldSave) throw new AssertionError("missing file autosave should skip");

        LegacySettings settings = new LegacySettings();
        settings.rememberFile("eins.alx");
        settings.rememberFile("zwei.alx");
        settings.rememberFile("drei.alx");
        String active = settings.activateRecentFile(0);
        if (!"eins.alx".equals(active) || !"eins.alx".equals(settings.recentFiles.get(settings.recentFiles.size() - 1))) {
            throw new AssertionError("recent activation rotation failed: " + settings.recentFiles);
        }
        ArrayList<String> rotated = new ArrayList<>();
        String selected = LegacySettings.legacyActivateRecentFile(Arrays.asList("a.alx", "b.alx", "c.alx"), 1, rotated);
        if (!"b.alx".equals(selected) || !"b.alx".equals(rotated.get(rotated.size() - 1))) throw new AssertionError("legacy activate recent failed: " + rotated);

        NoteNode root = new NoteNode("root", "");
        NoteNode a = root.addChild(new NoteNode("a", ""));
        NoteNode b = root.addChild(new NoteNode("b", ""));
        NoteNode c = root.addChild(new NoteNode("c", ""));
        a.addChild(new NoteNode("a-child", ""));
        if (!NoteTreeOps.legacyCanMoveBeforeTarget(c, a)) throw new AssertionError("move-before should be allowed");
        if (NoteTreeOps.legacyMoveBeforeTarget(c, a) != c || root.children.get(0) != c) throw new AssertionError("move-before failed");
        if (NoteTreeOps.legacyCanMoveBeforeTarget(a, a.children.get(0))) throw new AssertionError("descendant target should be blocked");
        if (NoteTreeOps.legacyCanMoveBeforeTarget(root, b)) throw new AssertionError("root source should be blocked");

        if (AlarmUtils.legacyWeckerWeekdayForCheckbox("CheckBox15") != 0) throw new AssertionError("legacy Monday checkbox failed");
        if (AlarmUtils.legacyWeckerWeekdayForCheckbox("CheckBox13") != 6) throw new AssertionError("legacy Sunday checkbox failed");
        if (!"Wochen".equals(AlarmUtils.legacyWeckerIntervalUnit(AlarmSpec.RECURRENCE_WEEKLY))) throw new AssertionError("legacy interval unit failed");
        if (!"So".equals(AlarmUtils.legacyWeckerWeekdayLabels()[6])) throw new AssertionError("legacy weekday labels failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (text.contains("isHorizontallyScrolling()")) throw new AssertionError("old EditText getter regression returned");
            if (!text.contains("showRecentFiles()") || !text.contains("performAutosaveTick()") || !text.contains("showMoveBeforeTargetDialog()")) {
                throw new AssertionError("v10 Android bridge methods missing");
            }
        }
    }


    private static void testLegacyAutostartOpenTargetsAndRtfTables() throws Exception {
        File tmp = new File(System.getProperty("java.io.tmpdir"), "notizen-v11-" + System.nanoTime());
        if (!tmp.mkdirs()) throw new AssertionError("temp dir failed: " + tmp);
        try {
            List<String> recent = Arrays.asList("C:\\Users\\me\\old.alx", "", "ftp://example.invalid/notizen.alx", "C:\\Users\\me\\newest.alx");
            LegacyStartup.AutostartResult created = LegacyStartup.applyWindowsAutostartScript(true, true, recent, "C:\\Python311\\python.exe", tmp);
            if (!created.changed || created.path == null || !created.path.isFile()) throw new AssertionError("autostart create failed");
            String script = readUtf8File(created.path);
            if (!script.contains("start \"\"") || !script.contains("notizen_py_qt") || !script.contains("-min") || !script.contains("newest.alx")) {
                throw new AssertionError("autostart script content failed: " + script);
            }
            LegacyStartup.AutostartResult unchanged = LegacyStartup.applyWindowsAutostartScript(true, true, recent, "C:\\Python311\\python.exe", tmp);
            if (unchanged.changed) throw new AssertionError("unchanged autostart should not rewrite");
            LegacyStartup.AutostartResult removed = LegacyStartup.applyWindowsAutostartScript(false, false, recent, "C:\\Python311\\python.exe", tmp);
            if (!removed.changed || created.path.exists()) throw new AssertionError("autostart remove failed");

            LegacyOpenTarget file = LegacyOpenTarget.parse("file:///C:/Users/me/Notizen%20A.alx");
            if (file.kind != LegacyOpenTarget.Kind.FILE || !"C:/Users/me/Notizen A.alx".equals(file.normalized) || !"Notizen A.alx".equals(file.displayName())) {
                throw new AssertionError("file target parse failed: " + file);
            }
            if (LegacyOpenTarget.parse("content://de.notizen/tree/1").kind != LegacyOpenTarget.Kind.ANDROID_URI) throw new AssertionError("content target failed");
            if (LegacyOpenTarget.parse("ftp://user@example.invalid/demo.alx").autosaveKind() != LegacyAutosave.TargetKind.FTP) throw new AssertionError("ftp autosave target failed");
            if (LegacyOpenTarget.parse("  ").isUsable()) throw new AssertionError("empty target should not be usable");

            String tableRtf = "{\\rtf1\\ansi{\\trowd A\\cell B\\cell\\row\\trowd C\\cell D\\cell\\row}}";
            String plain = RtfUtils.rtfToPlainText(tableRtf);
            if (!plain.contains("A\tB") || !plain.contains("C\tD")) throw new AssertionError("table plain failed: " + plain);
            String html = RtfUtils.rtfToHtml(tableRtf);
            if (!html.contains("<table class=\"notizen-rtf-table\">") || !html.contains("<td>A</td>") || !html.contains("<td>B</td>") || !html.contains("<td>C</td>") || !html.contains("<td>D</td>")) {
                throw new AssertionError("semantic RTF table HTML failed: " + html);
            }
            String styledCell = RtfUtils.rtfToHtml("{\\rtf1\\ansi{\\trowd \\b Fett\\b0\\cell Normal\\cell\\row}} ");
            if (!styledCell.contains("font-weight:700") || !styledCell.contains("Normal")) throw new AssertionError("styled cell failed: " + styledCell);
            String exportedHtml = Exporters.treeToHtml(new NoteNode("tab", tableRtf));
            if (!exportedHtml.contains(".notizen-rtf-table") || !exportedHtml.contains("<td>A</td>")) throw new AssertionError("export table css failed");
        } finally {
            deleteRec(tmp);
        }
    }

    private static void deleteRec(File file) {
        if (file == null || !file.exists()) return;
        if (file.isDirectory()) {
            File[] kids = file.listFiles();
            if (kids != null) for (File kid : kids) deleteRec(kid);
        }
        file.delete();
    }


    private static void testLegacyDesktopNoteAutoResizeDecision() {
        LegacyDesktopNote.Rect rect = new LegacyDesktopNote.Rect(100, 50, 200, 120);
        if (LegacyDesktopNoteAutoResize.mayRun(false, false, 1399, 1000)) throw new AssertionError("idle guard too early");
        if (!LegacyDesktopNoteAutoResize.mayRun(false, false, 1401, 1000)) throw new AssertionError("idle guard should pass");
        if (LegacyDesktopNoteAutoResize.mayRun(true, false, 2000, 1000)) throw new AssertionError("manual resize should block");
        if (LegacyDesktopNoteAutoResize.mayRun(false, true, 2000, 1000)) throw new AssertionError("scroll manual should block");

        LegacyDesktopNoteAutoResize.Decision shrink = LegacyDesktopNoteAutoResize.decideStep(rect, 190, 200, 100, 100, 400, 300, false, false, 2000, 1000);
        if (shrink.action != LegacyDesktopNoteAutoResize.Action.SHRINK || !shrink.rect.equals(new LegacyDesktopNote.Rect(100, 50, 190, 111)) || !shrink.shouldStoreWindow || !shrink.scrollManualAfterPass) {
            throw new AssertionError("shrink decision failed: " + shrink.reason + " " + shrink.rect);
        }
        LegacyDesktopNoteAutoResize.Decision growBoth = LegacyDesktopNoteAutoResize.decideStep(rect, 210, 200, 100, 100, 400, 300, false, false, 2000, 1000);
        if (growBoth.action != LegacyDesktopNoteAutoResize.Action.GROW_BOTH || !growBoth.rect.equals(new LegacyDesktopNote.Rect(100, 50, 210, 130))) {
            throw new AssertionError("grow both decision failed: " + growBoth.reason + " " + growBoth.rect);
        }
        LegacyDesktopNoteAutoResize.Decision growWidth = LegacyDesktopNoteAutoResize.decideStep(rect, 206, 200, 210, 200, 400, 300, false, false, 2000, 1000);
        if (growWidth.action != LegacyDesktopNoteAutoResize.Action.GROW_WIDTH || !growWidth.rect.equals(new LegacyDesktopNote.Rect(100, 50, 210, 120))) {
            throw new AssertionError("grow width decision failed: " + growWidth.reason + " " + growWidth.rect);
        }
        LegacyDesktopNoteAutoResize.Decision skip = LegacyDesktopNoteAutoResize.decideStep(rect, 210, 200, 210, 200, 400, 300, false, true, 2000, 1000);
        if (skip.action != LegacyDesktopNoteAutoResize.Action.SKIP || skip.shouldStoreWindow) throw new AssertionError("skip decision failed");
    }

    private static void testLegacyToolbarPresentation() {
        LegacyToolbarPresentation.ButtonSpec save = LegacyToolbarPresentation.forLabel("Speichern");
        if (!"file_save".equals(save.action) || !"💾".equals(save.displayText(true))) {
            throw new AssertionError("toolbar save mapping failed");
        }
        if (!save.contentDescription().contains("Ctrl+S")) throw new AssertionError("toolbar shortcut missing");
        LegacyToolbarPresentation.ButtonSpec indent = LegacyToolbarPresentation.forLabel("Einrücken");
        if (!"elements".equals(indent.toolstripGroup) || !"↳".equals(indent.iconGlyph)) throw new AssertionError("toolbar indent mapping failed");
        LegacyToolbarPresentation.ButtonSpec unknown = LegacyToolbarPresentation.forLabel("Sonder Menü!");
        if (!"sonder_menu".equals(unknown.action) || !"Sonder Menü!".equals(unknown.displayText(true))) {
            throw new AssertionError("toolbar fallback failed: " + unknown.action + " / " + unknown.displayText(true));
        }
        if (!"format_bold".equals(LegacyToolbarPresentation.forLabel("Fett").action) || !"𝐁".equals(LegacyToolbarPresentation.forLabel("Fett").displayText(true))) throw new AssertionError("format toolbar mapping failed");
        if (LegacyToolbarPresentation.specsByLabel().size() < 40) throw new AssertionError("toolbar catalog too small");
        File manifest = new File("app/src/main/AndroidManifest.xml");
        if (!manifest.isFile()) manifest = new File("../app/src/main/AndroidManifest.xml");
        if (manifest.isFile()) {
            try {
                String xml = readUtf8File(manifest);
                if (!xml.contains("application/x-notizen-alx") || !xml.contains("application/octet-stream") || !xml.contains(".*\\\\.ALX")) {
                    throw new AssertionError("Android ALX intent filter missing: " + xml);
                }
            } catch (Exception e) {
                if (e instanceof RuntimeException) throw (RuntimeException) e;
                throw new AssertionError("manifest read failed", e);
            }
        }
    }

    private static void testLegacyContextMenusDateAndCurrentNodeExport() throws Exception {
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        cal.set(2026, java.util.Calendar.MAY, 7, 4, 5, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        String legacyDate = LegacyEditorActions.legacyDateInsertText(cal.getTime(), TimeZone.getTimeZone("UTC"));
        if (!" 7.5.2026 4:5 ".equals(legacyDate)) throw new AssertionError("legacy date insert failed: " + legacyDate);
        if (!"\n•   ".equals(LegacyEditorActions.androidBulletInsertText())) throw new AssertionError("android bullet regressed");

        if (LegacyContextMenus.editorItems().size() != 23 || LegacyContextMenus.treeItems().size() != 22) {
            throw new AssertionError("context menu item counts failed: " + LegacyContextMenus.editorItems().size() + "/" + LegacyContextMenus.treeItems().size());
        }
        if (!"editor_insert_date".equals(LegacyContextMenus.byLegacyIndex(LegacyContextMenus.MenuKind.EDITOR, 4).action)) {
            throw new AssertionError("editor date context action failed");
        }
        if (!"tree_save_current_rtf".equals(LegacyContextMenus.byLegacyIndex(LegacyContextMenus.MenuKind.TREE, 7).action)) {
            throw new AssertionError("tree save context action failed");
        }
        if (!"Einfügen".equals(LegacyContextMenus.normalizedLegacyTreeLabel("Einfuegen"))
                || !"Löschen".equals(LegacyContextMenus.normalizedLegacyTreeLabel("Loechen"))) {
            throw new AssertionError("legacy tree label normalization failed");
        }
        if (!"tree_new_next".equals(LegacyContextMenus.actionFromLegacyLabel(LegacyContextMenus.MenuKind.TREE, "Neu", 1))) {
            throw new AssertionError("duplicate Neu action failed");
        }
        if (!LegacyContextMenus.byAction("tree_save_current_rtf").accessibilityLabel().equals("RTF speichern…")) {
            throw new AssertionError("context accessibility label failed");
        }

        NoteNode current = new NoteNode("Aktuelle/Notiz:*", "{\\rtf1\\ansi A\\par B}");
        if (!"A\r\nB".equals(LegacyNodeExport.currentNodePlainText(current))) {
            throw new AssertionError("current node txt export CRLF failed: " + LegacyNodeExport.currentNodePlainText(current).replace("\r", "<r>").replace("\n", "<n>"));
        }
        String name = LegacyNodeExport.defaultFileName(current, ".rtf");
        if (!"Aktuelle_Notiz.rtf".equals(name)) throw new AssertionError("node export file name failed: " + name);
        String raw = LegacyNodeExport.currentNodeRtf(current);
        if (!raw.contains("\\rtf1") || !raw.contains("A")) throw new AssertionError("node rtf raw failed: " + raw);
        NoteNode plain = new NoteNode("plain", "nur text");
        if (!RtfUtils.looksLikeRtf(LegacyNodeExport.currentNodeRtf(plain)) || !"nur text".equals(RtfUtils.rtfToPlainText(LegacyNodeExport.currentNodeRtf(plain)))) {
            throw new AssertionError("plain node rtf fallback failed");
        }
        if (!"unbenannt.rtf".equals(LegacyNodeExport.defaultFileName(new NoteNode("///", ""), "rtf"))) {
            throw new AssertionError("unbenannt filename fallback failed");
        }

        LegacyToolbarPresentation.ButtonSpec nodeRtf = LegacyToolbarPresentation.forLabel("Knoten RTF");
        if (!"export_node_rtf".equals(nodeRtf.action) || !nodeRtf.contentDescription().contains("Aktuellen Knoten")) {
            throw new AssertionError("node toolbar mapping failed");
        }
    }

    private static void testLegacyDialogDesktopContextAndFontToolbar() throws Exception {
        LegacyDialogModels.DialogSpec save = LegacyDialogModels.wannaSave("Deutsch");
        if (save.kind != LegacyDialogModels.DialogKind.WANNA_SAVE || !save.title.contains("Speichern") || !save.message.contains("speichern")) {
            throw new AssertionError("wanna-save dialog failed: " + save.title + " / " + save.message);
        }
        if (LegacyDialogModels.choiceForWannaSaveButton("positive") != LegacyDialogModels.SavePromptChoice.SAVE
                || LegacyDialogModels.choiceForWannaSaveButton("neutral") != LegacyDialogModels.SavePromptChoice.DISCARD
                || LegacyDialogModels.choiceForWannaSaveButton("negative") != LegacyDialogModels.SavePromptChoice.CANCEL) {
            throw new AssertionError("wanna-save choice mapping failed");
        }
        if (!LegacyDialogModels.shouldShowWannaSave(true) || LegacyDialogModels.shouldShowWannaSave(false)) throw new AssertionError("wanna-save guard failed");
        if (!LegacyDialogModels.describeWannaSaveFlow("Deutsch", LegacyDialogModels.SavePromptChoice.DISCARD).contains("ohne Speichern")) {
            throw new AssertionError("wanna-save flow description failed");
        }
        LegacyDialogModels.DialogSpec restart = LegacyDialogModels.wannaRestart("English");
        if (restart.kind != LegacyDialogModels.DialogKind.WANNA_RESTART || restart.message.indexOf('\n') < 0 || restart.positive.isEmpty() || restart.negative.isEmpty()) {
            throw new AssertionError("wanna-restart dialog failed");
        }

        List<LegacyDesktopNoteContextMenu.MenuItemSpec> menu = LegacyDesktopNoteContextMenu.menuItems("Deutsch");
        if (menu.size() != 3 || !"kontext8".equals(menu.get(0).languageKey) || !"desktop_note_hide".equals(menu.get(1).action)) {
            throw new AssertionError("desktop-note context menu failed");
        }
        List<LegacyDesktopNoteContextMenu.OpacityOption> opts = LegacyDesktopNoteContextMenu.opacityOptions();
        if (opts.size() != 10 || opts.get(0).transparencyPercent != 90 || Math.abs(opts.get(0).opacity - 0.1d) > 0.0001d
                || opts.get(9).transparencyPercent != 0 || Math.abs(opts.get(9).opacity - 1.0d) > 0.0001d) {
            throw new AssertionError("desktop-note opacity menu failed");
        }
        DesktopNoteState state = new DesktopNoteState();
        state.visible = true;
        DesktopNoteState hidden = LegacyDesktopNoteContextMenu.applyHide(state);
        if (hidden.visible || !state.visible) throw new AssertionError("desktop-note hide action failed");
        DesktopNoteState op = LegacyDesktopNoteContextMenu.applyOpacity(state, 60);
        if (Math.abs(op.opacity - 0.4d) > 0.0001d || !LegacyDesktopNoteContextMenu.closeActionKeepsNodeButHides()) throw new AssertionError("desktop-note opacity action failed");

        LegacyRichTextToolbar.ActionSpec family = LegacyRichTextToolbar.findByObjectName("ToolStrip_fonts");
        LegacyRichTextToolbar.ActionSpec size = LegacyRichTextToolbar.findByObjectName("ToolStrip_fontsizenumber");
        if (family == null || !"font_family".equals(family.action) || size == null || !"font_size".equals(size.action)) {
            throw new AssertionError("font toolbar controls missing");
        }
        if (!LegacyRichTextToolbar.legacyFontFamilies().contains("Courier New")) throw new AssertionError("font families missing Courier New");
        if (!"Bad".equals(LegacyRichTextToolbar.normalizeFontFamily(" Bad;{}"))) throw new AssertionError("font family sanitize failed");
        if (LegacyRichTextToolbar.nearestLegacyFontSize(13) != 12 || LegacyRichTextToolbar.nextFontSize(12, 2) != 16) {
            throw new AssertionError("legacy font sizes failed");
        }
        String sized = LegacyRtfSelectionFormatter.applyFontSizeToSelection("Hallo Welt", 6, 10, 16);
        String fonted = LegacyRtfSelectionFormatter.applyFontFamilyToSelection("Hallo Welt", 0, 5, "Courier New");
        if (!sized.contains("\\fs32") || !fonted.contains("Courier New")) throw new AssertionError("font selection RTF failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("exportCurrentNodeRtf()") || !text.contains("REQ_EXPORT_NODE_RTF")
                    || !text.contains("LegacyEditorActions.androidDateInsertText") || !text.contains("LegacyDialogModels.wannaSave")
                    || !text.contains("showFontFamilyDialog") || !text.contains("showFontSizeDialog")) {
                throw new AssertionError("v13 Android bridge methods missing");
            }
        }
    }



    private static void testLegacyPasswordFtpSettingsAboutSearchAndAndroidBuildFixes() throws Exception {
        if (!LegacyPasswordDialogModel.BLANK_PASSWORD_24.equals(LegacyPasswordDialogModel.toLegacy24(""))) throw new AssertionError("blank password normalization failed");
        if (!"abc                     ".equals(LegacyPasswordDialogModel.toLegacy24("abc"))) throw new AssertionError("password pad failed");
        if (!"abcdefghijklmnopqrstuvwx".equals(LegacyPasswordDialogModel.toLegacy24("abcdefghijklmnopqrstuvwxyz"))) throw new AssertionError("password truncate failed");
        if (LegacyPasswordDialogModel.oldPasswordFieldEnabled("")) throw new AssertionError("blank old password should be disabled");
        LegacyPasswordDialogModel.PromptSpec ps = LegacyPasswordDialogModel.changePrompt("Deutsch", "secret");
        if (!ps.oldPasswordEnabled || ps.maxChars != 24 || !ps.newPasswordLabel.contains("neu")) throw new AssertionError("password prompt failed");
        LegacyPasswordDialogModel.PasswordChangeDecision wrongOld = LegacyPasswordDialogModel.validateChange("secret", "bad", "neu", "neu", "Deutsch");
        if (wrongOld.accepted || wrongOld.error != LegacyPasswordDialogModel.ErrorKind.OLD_PASSWORD_WRONG || !"passerror2".equals(wrongOld.messageKey)) throw new AssertionError("wrong old password failed");
        LegacyPasswordDialogModel.PasswordChangeDecision mismatch = LegacyPasswordDialogModel.validateChange("", "", "eins", "zwei", "English");
        if (mismatch.accepted || mismatch.error != LegacyPasswordDialogModel.ErrorKind.NEW_PASSWORDS_DIFFER) throw new AssertionError("password mismatch failed");
        LegacyPasswordDialogModel.PasswordChangeDecision tooLong = LegacyPasswordDialogModel.validateChange("", "", "abcdefghijklmnopqrstuvwxy", "abcdefghijklmnopqrstuvwxy", "Deutsch");
        if (tooLong.accepted || tooLong.error != LegacyPasswordDialogModel.ErrorKind.TOO_LONG) throw new AssertionError("too long password failed");
        LegacyPasswordDialogModel.PasswordChangeDecision ok = LegacyPasswordDialogModel.validateChange("secret", "secret", "neu", "neu", "Deutsch");
        if (!ok.accepted || !"neu".equals(ok.newPassword)) throw new AssertionError("password ok failed");
        if (!LegacyPasswordDialogModel.wrongPasswordMessage("Deutsch", "x").contains("Error: x")) throw new AssertionError("wrong password text failed");

        LegacyFtpDialogModel.FtpDialogFields fields = LegacyFtpDialogModel.normalizeFields("example.org///", "dir/ä.alx", "user", "pw");
        if (!"example.org".equals(fields.host) || !"/dir/ä.alx".equals(fields.path)) throw new AssertionError("ftp normalize failed: " + fields.host + " " + fields.path);
        String legacyUrl = LegacyFtpDialogModel.buildLegacyUrl("example.org", "/dir/file.alx", "user", "pw");
        if (!"ftp://user:pw@example.org/dir/file.alx".equals(legacyUrl)) throw new AssertionError("legacy ftp url failed: " + legacyUrl);
        if (!LegacyFtpDialogModel.buildSafeLegacyUrl("example.org", "/dir/file.alx", "user", "pw").contains(":***@")) throw new AssertionError("safe ftp url failed");
        LegacyFtpDialogModel.FtpDialogDecision ftpOpen = LegacyFtpDialogModel.decide(LegacyFtpDialogModel.Action.OPEN, "ftp://alice:secret@example.org:2121/path/old.alx", "", "", "");
        if (!ftpOpen.accepted || ftpOpen.target == null || ftpOpen.target.port != 2121 || !"alice".equals(ftpOpen.username) || !"/path/old.alx".equals(ftpOpen.normalizedPath)) {
            throw new AssertionError("ftp decision from URL failed: " + ftpOpen.message + " " + ftpOpen.safeLegacyUrl);
        }
        LegacyFtpDialogModel.FtpDialogDecision ftpBad = LegacyFtpDialogModel.decide(LegacyFtpDialogModel.Action.OPEN, "x", "bad.txt", "", "");
        if (ftpBad.accepted || ftpBad.message.isEmpty()) throw new AssertionError("bad ftp should fail");
        LegacySettings ftpSettings = new LegacySettings();
        LegacyFtpDialogModel.applyToSettings(ftpSettings, ftpOpen);
        if (!ftpSettings.ftpHost.contains("example.org") || !"/path/old.alx".equals(ftpSettings.ftpPath) || !"secret".equals(ftpSettings.ftpPassword)) throw new AssertionError("ftp settings apply failed");

        LegacySettings set = new LegacySettings();
        set.autosaveSeconds = 0;
        set.autorunEnabled = false;
        LegacySettingsDialogModel.ViewState view = LegacySettingsDialogModel.fromSettings(set);
        LegacySettingsDialogModel.ViewState autoOn = LegacySettingsDialogModel.onAutosaveChanged(view, true);
        if (!autoOn.autosaveSecondsEnabled || !"60".equals(autoOn.autosaveSecondsText)) throw new AssertionError("autosave checkbox default failed");
        LegacySettingsDialogModel.ViewState autorunOff = LegacySettingsDialogModel.onAutorunChanged(view, false);
        if (autorunOff.autorunMinimizedEnabled || autorunOff.autorunMinimized) throw new AssertionError("autorun off dependency failed");
        LegacySettingsDialogModel.ApplyResult applied = LegacySettingsDialogModel.apply(set, "english", "abc", true, "3", true, true, true, true);
        if (!"English".equals(applied.settings.language) || !applied.settings.showInTaskbarWhenMinimized || !applied.settings.showDesknoteBorders || !applied.settings.autorunEnabled || !applied.settings.autorunMinimized) {
            throw new AssertionError("settings booleans failed");
        }
        if (applied.settings.autosaveSeconds != 5 || applied.autosaveIntervalMillis != 5000 || applied.warnings.isEmpty()) throw new AssertionError("settings autosave/warning failed");
        LegacySettingsDialogModel.ApplyResult off = LegacySettingsDialogModel.apply(applied.settings, "Auto", "7", false, "90", false, true, false, false);
        if (off.settings.autorunMinimized || off.settings.autosaveSeconds != 0 || off.settings.backupKeep != 7 || off.settings.showDesknoteBorders) throw new AssertionError("settings off failed");

        LegacyAboutHelp.AboutSpec de = LegacyAboutHelp.spec("Deutsch", "1.0.17-java-native");
        LegacyAboutHelp.AboutSpec en = LegacyAboutHelp.spec("English", "1.0.17-java-native");
        if (!de.description.contains("Haftnotiz") || !de.feedbackLabel.contains("Fehlermeldung") || !"schließen".equals(de.closeLabel)) throw new AssertionError("German about failed");
        if (!en.description.contains("desknote") || !en.feedbackLabel.contains("bug report") || !LegacyAboutHelp.LEGACY_WEB_URL.equals(en.webUrl)) throw new AssertionError("English about failed");
        if (!LegacyAboutHelp.androidPortAppendix("1.0.17-java-native").contains("Android-Port") || !LegacyAboutHelp.feedbackTooShortMessage("Deutsch").contains("10 Zeichen")) throw new AssertionError("about appendix failed");

        LegacySearchDialogModel.SearchOptions empty = LegacySearchDialogModel.normalize("   ", false, false, true, "Deutsch");
        if (empty.accepted || !empty.message.contains("Suchbegriff")) throw new AssertionError("empty search should be rejected");
        LegacySearchDialogModel.SearchOptions opt = LegacySearchDialogModel.normalize("  hallo  ", true, true, false, "English");
        if (!opt.accepted || !"hallo".equals(opt.term) || !opt.wholeWords || !opt.caseSensitive || opt.includeTitles) throw new AssertionError("search normalize failed");
        if (!LegacySearchDialogModel.resultsTitle(3, "English").equals("Results: 3") || !LegacySearchDialogModel.statusForResult(2, 3, "Deutsch").equals("Treffer 2 von 3")) throw new AssertionError("search labels failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("showPasswordChangeDialog") || !text.contains("LegacyPasswordDialogModel.validateChange")
                    || !text.contains("LegacyFtpDialogModel.decide") || !text.contains("LegacySettingsDialogModel.apply")
                    || !text.contains("LegacyAboutHelp.spec") || !text.contains("LegacySearchDialogModel.normalize")) {
                throw new AssertionError("v15 Android bridge methods missing");
            }
            if (countOccurrences(text, "private String pendingExportHtml;") != 1) throw new AssertionError("pendingExportHtml duplicate regressed");
            if (countOccurrences(text, "case KeyEvent.KEYCODE_O") != 1) throw new AssertionError("KEYCODE_O duplicate regressed");
            if (text.contains("Uri uri = intent.getData();\n        Uri uri = intent.getData();")) throw new AssertionError("duplicate Uri declaration regressed");
            if (text.contains("callback.run();\n            callback.run();")) throw new AssertionError("confirmDiscardThen double callback regressed");
        }
    }


    private static void testLegacyAlarmFontSearchEditorFileV17() throws Exception {
        AlarmSpec alarm = new AlarmSpec(1704110400000L); // 2024-01-01 13:00 in Europe-ish zones; exact display not asserted.
        alarm.recurrence = AlarmSpec.RECURRENCE_WEEKLY;
        alarm.interval = 2;
        alarm.weekdays.add(0);
        alarm.weekdays.add(2);
        LegacyAlarmDialogModel.ViewState alarmState = LegacyAlarmDialogModel.fromSpec(alarm, alarm.startMillis - 1000L, TimeZone.getTimeZone("UTC"));
        if (!LegacyAlarmDialogModel.RB_WEEKLY.equals(alarmState.radioButton) || !alarmState.intervalVisible || !alarmState.weekdaysVisible || !"Wochen".equals(alarmState.unitLabel)) throw new AssertionError("alarm weekly view failed");
        if (!Boolean.TRUE.equals(alarmState.weekdayCheckboxes.get("CheckBox15")) || !Boolean.TRUE.equals(alarmState.weekdayCheckboxes.get("CheckBox9"))) throw new AssertionError("alarm weekday checkbox mapping failed");
        if (!LegacyAlarmDialogModel.RB_YEARLY.equals(LegacyAlarmDialogModel.radioForRecurrence(AlarmSpec.RECURRENCE_YEARLY))) throw new AssertionError("alarm radio mapping failed");
        if (!AlarmSpec.RECURRENCE_MONTHLY.equals(LegacyAlarmDialogModel.recurrenceForRadio(LegacyAlarmDialogModel.RB_MONTHLY))) throw new AssertionError("alarm recurrence mapping failed");
        LegacyAlarmDialogModel.ApplyResult invalidAlarm = LegacyAlarmDialogModel.apply(true, "01.01.2024", "", AlarmSpec.RECURRENCE_DAILY, "1", "", TimeZone.getTimeZone("UTC"));
        if (invalidAlarm.accepted || !invalidAlarm.message.contains(LegacyAlarmDialogModel.DATE_PATTERN)) throw new AssertionError("invalid alarm date failed");
        java.util.LinkedHashMap<String, Boolean> checks = new java.util.LinkedHashMap<>();
        checks.put("CheckBox15", Boolean.TRUE);
        checks.put("CheckBox12", Boolean.FALSE);
        checks.put("CheckBox9", Boolean.TRUE);
        LegacyAlarmDialogModel.ApplyResult validAlarm = LegacyAlarmDialogModel.apply(true, "2024-01-01 13:00", "", AlarmSpec.RECURRENCE_WEEKLY, "3", checks, TimeZone.getTimeZone("UTC"));
        if (!validAlarm.accepted || validAlarm.spec.interval != 3 || !"0,2".equals(validAlarm.spec.weekdaysCsv()) || !"Notizen-Wecker".equals(validAlarm.spec.message)) throw new AssertionError("valid alarm apply failed");

        LegacyFontSizeEntry.State empty = LegacyFontSizeEntry.onTextChanged("", "12");
        if (!empty.emptyIntermediate || !"12".equals(empty.lastValidText)) throw new AssertionError("fontsize empty failed");
        LegacyFontSizeEntry.State bad = LegacyFontSizeEntry.onTextChanged("abc", "14");
        if (!"14".equals(bad.text) || bad.numericValue != 14) throw new AssertionError("fontsize bad fallback failed");
        LegacyFontSizeEntry.State capped = LegacyFontSizeEntry.onEnter("123", "8");
        if (!capped.apply || capped.numericValue != 99 || !"99".equals(capped.text)) throw new AssertionError("fontsize cap/enter failed");

        NoteNode node = new NoteNode("Titel", RtfUtils.plainTextToRtf("Alter Text"));
        LegacyEditorNodeSync.LoadState loaded = LegacyEditorNodeSync.load(node);
        if (!"Titel".equals(loaded.titleText) || !"Alter Text".equals(loaded.editorPlainText)) throw new AssertionError("editor load failed");
        node.desktopNote = new DesktopNoteState();
        LegacyEditorNodeSync.SaveState saved = LegacyEditorNodeSync.applyToNode(node, " Neu ", "Neuer Text", true, true);
        if (!saved.changed || !saved.reloadDesktopNote || !"Neu".equals(node.title) || !"Neuer Text".equals(RtfUtils.rtfToPlainText(node.rtf))) throw new AssertionError("editor save failed");
        if (!"...".equals(LegacyEditorNodeSync.normalizeTitle("   "))) throw new AssertionError("editor title normalize failed");

        LegacyFileState file = new LegacyFileState("/home/me/Documents/Notizen");
        file.setDirectory("");
        if (!"/home/me/Documents/Notizen".equals(file.directory())) throw new AssertionError("file default dir failed");
        file.setSaveName("/tmp/Test");
        if (!"/tmp".equals(file.directory()) || !"Test".equals(file.saveName()) || !file.displayNameEnsuringAlx().equals("Test.alx") || !file.fileExists()) throw new AssertionError("file save path failed");

        NoteNode root = new NoteNode("root", RtfUtils.plainTextToRtf("root alpha"));
        NoteNode child = root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("child alpha alpha")));
        LegacySearchSession session = new LegacySearchSession();
        LegacySearchSession.SearchStep currentOnly = session.begin(root, child, "alpha", false, false, false, false, "Deutsch");
        if (currentOnly.results.size() != 2 || currentOnly.selected == null || currentOnly.selected.node != child || !currentOnly.status.equals("Treffer 1 von 2")) throw new AssertionError("search session current failed");
        LegacySearchSession.SearchStep next = session.begin(root, child, "alpha", false, false, false, false, "Deutsch");
        if (next.rebuilt || next.selectedIndexOneBased != 2) throw new AssertionError("search session cache/next failed");
        LegacySearchSession.SearchStep all = session.begin(root, child, "alpha", true, false, false, false, "English");
        if (!all.rebuilt || all.results.size() != 3 || !all.resultCountLabel.equals("Results: 3")) throw new AssertionError("search session all failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("LegacyAlarmDialogModel.fromSpec") || !text.contains("LegacyFontSizeEntry.onEnter") || !text.contains("LegacyEditorNodeSync.applyToNode") || !text.contains("searchSession.begin")) throw new AssertionError("v17 Android bridges missing");
            if (text.contains("EditText.isHorizontallyScrolling()")) throw new AssertionError("old EditText getter regression");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("-sourcepath \"$JAVA_SRC:$BUILD_DIR/gen\"")) throw new AssertionError("termux sourcepath/version failed");
        }
    }

    private static void testLegacyScrollExpansionUnifiedMenusDesktopV23() throws Exception {
        if (LegacyScrollbars.normalizeChoice(null) != 3 || LegacyScrollbars.normalizeChoice("horizontal") != 1 || LegacyScrollbars.normalizeChoice("vertikal") != 2 || LegacyScrollbars.normalizeChoice("keine") != 0) throw new AssertionError("scroll normalize failed");
        int c = 0;
        c = LegacyScrollbars.nextChoice(c);
        if (c != 1 || !LegacyScrollbars.modeForChoice(c).horizontal || LegacyScrollbars.modeForChoice(c).vertical) throw new AssertionError("scroll horizontal failed");
        c = LegacyScrollbars.nextChoice(c);
        if (c != 2 || LegacyScrollbars.modeForChoice(c).horizontal || !LegacyScrollbars.modeForChoice(c).vertical) throw new AssertionError("scroll vertical failed");
        c = LegacyScrollbars.nextChoice(c);
        if (c != 3 || !LegacyScrollbars.modeForChoice(c).horizontal || !LegacyScrollbars.modeForChoice(c).vertical) throw new AssertionError("scroll both failed");
        c = LegacyScrollbars.nextChoice(c);
        if (c != 0 || !"Keine".equals(LegacyScrollbars.label(c))) throw new AssertionError("scroll cycle failed");
        LegacySettings scrollSettings = LegacySettings.fromXmlString("<settings><scrolls choice=\"99\"/></settings>");
        if (scrollSettings.scrollbarsChoice != 3 || !new String(scrollSettings.toXmlBytes(), java.nio.charset.StandardCharsets.UTF_16).contains("choice=\"3\"")) throw new AssertionError("scroll settings normalize failed");

        NoteDocument doc = NoteDocument.newDocument();
        doc.markSaved();
        doc.root.title = "root";
        doc.root.rtf = RtfUtils.plainTextToRtf("root body");
        NoteNode a = doc.root.addChild(new NoteNode("a", "{\\rtf1\\ansi{\\fonttbl{\\f0 Arial;}}\\pard \\b bold\\b0  plain\\par}"));
        NoteNode b = doc.root.addChild(new NoteNode("b", RtfUtils.plainTextToRtf("b body")));
        NoteNode aa = a.addChild(new NoteNode("aa", RtfUtils.plainTextToRtf("aa body")));
        a.desktopNote = new DesktopNoteState();
        aa.desktopNote = new DesktopNoteState();
        aa.desktopNote.visible = false;
        doc.root.expanded = false; a.expanded = false; b.expanded = false; aa.expanded = false;
        int expanded = LegacyTreeExpansion.expandAll(doc.root);
        if (expanded < 3 || !doc.root.expanded || !a.expanded || !aa.expanded) throw new AssertionError("expand all failed");
        int collapsed = LegacyTreeExpansion.collapseAll(doc.root);
        if (collapsed < 3 || doc.root.expanded || a.expanded || aa.expanded) throw new AssertionError("collapse all failed");

        List<LegacyTreeTraversal.Entry> pre = LegacyTreeTraversal.preorder(doc.root);
        if (pre.size() != 4 || !"1.1.1".equals(pre.get(2).path) || pre.get(2).depth != 2 || !"aa".equals(pre.get(2).title()) || !"1.1.1".equals(LegacyTreeTraversal.pathOf(aa))) throw new AssertionError("tree traversal failed");
        List<LegacyTreeTraversal.Entry> enterExit = LegacyTreeTraversal.enterExit(doc.root);
        if (enterExit.size() != 8 || !enterExit.get(0).enter || enterExit.get(enterExit.size() - 1).enter) throw new AssertionError("tree enter/exit failed");

        if (LegacyTreeDragDrop.decision(doc.root, a, doc.root).allowed) throw new AssertionError("drag root source allowed");
        if (LegacyTreeDragDrop.decision(a, aa, doc.root).allowed) throw new AssertionError("drag into descendant allowed");
        if (!LegacyTreeDragDrop.decision(b, a, doc.root).allowed) throw new AssertionError("drag valid failed");
        if (LegacyTreeDragDrop.moveBefore(b, a, doc.root) != b || doc.root.children.get(0) != b) throw new AssertionError("drag move failed");

        NoteNode unified = Exporters.unifiedNote(doc.root, "Gesamt");
        if (!unified.rtf.startsWith("{\\rtf1") || !unified.rtf.contains("\\b") || !RtfUtils.rtfToPlainText(unified.rtf).contains("bold plain")) throw new AssertionError("rich unified export failed: " + unified.rtf);
        doc.markSaved();
        LegacyUnifiedNote.UnifiedResult res = LegacyUnifiedNote.attach(doc, a, LegacyUnifiedNote.Scope.CURRENT_SUBTREE);
        if (res.created == null || res.parent != a || !"Teilbaum".equals(res.created.title) || !doc.changed || res.sourceNodes != 2) throw new AssertionError("unified attach current failed");

        if (LegacyDesktopNoteTreeOps.countDesktopNotes(a) != 2 || LegacyDesktopNoteTreeOps.countVisibleDesktopNotes(a) != 1) throw new AssertionError("desktop note count failed");
        LegacyDesktopNoteTreeOps.ClearResult clear = LegacyDesktopNoteTreeOps.clearDesktopNotes(a);
        if (clear.clearedDesktopNotes != 2 || clear.visitedNodes < 3 || LegacyDesktopNoteTreeOps.countDesktopNotes(a) != 0 || !LegacyDesktopNoteTreeOps.clearStatusText(clear).contains("2 Haftnotizen")) throw new AssertionError("desktop note clear failed");

        NoteNode delRoot = new NoteNode("root", "");
        NoteNode delA = delRoot.addChild(new NoteNode("a", ""));
        NoteNode delB = delRoot.addChild(new NoteNode("b", ""));
        NoteNode delAA = delA.addChild(new NoteNode("aa", ""));
        delA.expanded = true; delAA.desktopNote = new DesktopNoteState();
        LegacyTreeDelete.DeletePlan plan = LegacyTreeDelete.plan(delAA);
        if (plan.deletesRoot || plan.fallback != delA || plan.desktopNotesToClose != 1 || !LegacyTreeDelete.confirmationText(plan).contains("Haftnotiz")) throw new AssertionError("delete plan failed");
        NoteNode fallback = LegacyTreeDelete.delete(delAA);
        if (fallback != delA || delA.children.contains(delAA)) throw new AssertionError("delete execution failed");
        if (!LegacyTreeDelete.plan(delRoot).deletesRoot) throw new AssertionError("delete root plan failed");

        LegacySettings menuSettings = new LegacySettings();
        menuSettings.rememberFile("C:\\Notizen\\eins.alx");
        menuSettings.rememberFile("/tmp/zwei.alx");
        List<LegacyStartMenuModel.Item> menu = LegacyStartMenuModel.startMenu(menuSettings);
        if (LegacyStartMenuModel.findByAction(menu, "unify_root") == null || LegacyStartMenuModel.findByAction(menu, "export_txt_ansi") == null || LegacyStartMenuModel.findByAction(menu, "recent_1") == null) throw new AssertionError("start menu model failed");
        if (!"eins.alx".equals(LegacyStartMenuModel.displayName("C:\\Notizen\\eins.alx"))) throw new AssertionError("start menu display failed");
        if (LegacyStartMenuModel.trayMenu(menuSettings).size() < 5) throw new AssertionError("tray menu model failed");

        LegacyActionState.State rootState = LegacyActionState.from(doc, doc.root, false, false, false);
        if (LegacyActionState.isEnabled("tree_delete", rootState)) throw new AssertionError("root delete enabled");
        if (!LegacyActionState.isEnabled("tree_expand_all", rootState) || !LegacyActionState.isEnabled("unify_root", rootState)) throw new AssertionError("root action disabled");
        LegacyActionState.State childState = LegacyActionState.from(doc, a, true, true, true);
        if (!LegacyActionState.isEnabled("tree_delete", childState) || !LegacyActionState.isEnabled("tree_paste", childState) || !LegacyActionState.isEnabled("cycle_scrollbars", childState)) throw new AssertionError("child action disabled");

        LegacyToolbarPresentation.ButtonSpec expandSpec = LegacyToolbarPresentation.forLabel("Alle auf");
        LegacyToolbarPresentation.ButtonSpec currentSpec = LegacyToolbarPresentation.forLabel("Teilbaum");
        LegacyToolbarPresentation.ButtonSpec clearSpec = LegacyToolbarPresentation.forLabel("Haft weg");
        if (!"tree_expand_all".equals(expandSpec.action) || !"unify_current".equals(currentSpec.action) || !"desktop_notes_clear_subtree".equals(clearSpec.action) || !LegacyToolbarPresentation.specsByLabel().containsKey("Scroll")) throw new AssertionError("toolbar v23 mappings failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("LegacyScrollbars.modeForChoice") || !text.contains("expandAllNodes") || !text.contains("LegacyUnifiedNote.attach") || !text.contains("LegacyTreeDragDrop.moveBefore") || !text.contains("LegacyDesktopNoteTreeOps.clearDesktopNotes")) throw new AssertionError("v23 Android bridges missing");
            if (text.contains("EditText.isHorizontallyScrolling()")) throw new AssertionError("old EditText getter regression v23");
        }
    }


    private static void testLegacyRichTextBoxCssFocusTreeV29() throws Exception {
        String rtf = "{\\rtf1\\ansi{\\fonttbl{\\f0 Arial;}}{\\colortbl;\\red10\\green20\\blue30;}\\pard\\chcbpat1 bg\\expnd4 wide\\expndtw80 wider\\super sup\\super0 norm\\sub sub\\sub0 norm\\line soft\\tab tab{\\field{\\*\\fldinst HYPERLINK \"https://example.org\"}{\\fldrslt link}}\\par}";
        String html = RtfUtils.rtfToHtml(rtf);
        if (!html.contains("background-color:#0a141e") || !html.contains("letter-spacing:1pt") || !html.contains("letter-spacing:4pt") || !html.contains("vertical-align:super") || !html.contains("vertical-align:sub") || !html.contains("<br/>") || !html.contains("https://example.org")) throw new AssertionError("RTF v29 HTML bridge failed: " + html);
        String docHtml = RtfUtils.rtfToHtmlDocument(rtf);
        if (!docHtml.contains("Microsoft Sans Serif") || !docHtml.contains("notizen-object") || !docHtml.contains("white-space:pre-wrap")) throw new AssertionError("RichTextBox document CSS failed: " + docHtml);
        String desktopHtml = RtfUtils.rtfToDesktopHtml(rtf);
        if (!desktopHtml.contains("line-height:100%") || !desktopHtml.contains("background:transparent")) throw new AssertionError("desktop note CSS failed: " + desktopHtml);

        String htmlRtf = RtfUtils.htmlToRtf("<body text=\"#112233\" bgcolor=\"#80445566\" dir=\"rtl\"><p style=\"padding-left:12pt; visibility:hidden; color:rebeccapurple\">x</p><kbd>key</kbd><samp>sample</samp></body>");
        if (!htmlRtf.contains("\\red17\\green34\\blue51") || !htmlRtf.contains("\\red68\\green85\\blue102") || !htmlRtf.contains("\\red102\\green51\\blue153")) throw new AssertionError("HTML color parsing v29 failed: " + htmlRtf);
        if (!htmlRtf.contains("\\li240") || !htmlRtf.contains("\\v") || !htmlRtf.contains("\\rtlpar") || !htmlRtf.contains("\\rtlch") || !htmlRtf.contains("Courier New")) throw new AssertionError("HTML controls v29 failed: " + htmlRtf);
        String colorRtf = RtfUtils.htmlToRtf("<span style=\"color:rgba(50%, 0%, 100%, .5); background:chocolate\">x</span>");
        if (!colorRtf.contains("\\red127\\green0\\blue255") || !colorRtf.contains("\\red210\\green105\\blue30")) throw new AssertionError("RGBA/named color v29 failed: " + colorRtf);
        String escaped = RtfUtils.rtfEscapeMultiline("a\u2028b\nc");
        if (!escaped.contains("\\line ") || !escaped.contains("\\par\n")) throw new AssertionError("soft line escape failed: " + escaped);

        LegacyRichTextBoxSemantics.Metrics metrics = LegacyRichTextBoxSemantics.inspectRtf(rtf);
        if (metrics.hardParagraphs < 1 || metrics.softLineBreaks < 1 || metrics.tabs < 1 || metrics.fields < 1 || metrics.hyperlinks < 1 || !metrics.summaryGerman().contains("Hyperlinks: 1")) throw new AssertionError("RTF metrics failed: " + metrics.summaryGerman());
        if (!LegacyRichTextBoxSemantics.looksLikeLegacyRichTextBoxRtf(rtf)) throw new AssertionError("RichTextBox recognition failed");
        if (LegacyRichTextBoxSemantics.preferredClipboardFormat(true, true, true, true) != LegacyRichTextBoxSemantics.ClipboardFormat.NODE_XML) throw new AssertionError("clipboard node priority failed");
        if (LegacyRichTextBoxSemantics.preferredClipboardFormat(false, true, true, true) != LegacyRichTextBoxSemantics.ClipboardFormat.RTF) throw new AssertionError("clipboard rtf priority failed");

        LegacyClipboardFocus.Decision editorCopy = LegacyClipboardFocus.decide(LegacyClipboardFocus.Action.COPY, false, true, true, true, false, false, true);
        if (!LegacyClipboardFocus.shouldUseEditor(editorCopy)) throw new AssertionError("editor copy focus failed");
        LegacyClipboardFocus.Decision rootCut = LegacyClipboardFocus.decide(LegacyClipboardFocus.Action.CUT, true, false, false, true, true, true, true);
        if (rootCut.allowed || rootCut.area != LegacyClipboardFocus.Area.TREE) throw new AssertionError("root cut block failed");
        LegacyClipboardFocus.Decision treePaste = LegacyClipboardFocus.decide(LegacyClipboardFocus.Action.PASTE, true, false, false, true, false, true, false);
        if (!LegacyClipboardFocus.shouldUseTree(treePaste)) throw new AssertionError("tree paste focus failed");

        LegacyTreeLabelEditing.EditResult empty = LegacyTreeLabelEditing.commit("alt", "   ");
        if (!"...".equals(empty.newTitle) || !empty.usedFallback || !empty.changed) throw new AssertionError("tree label fallback failed");
        LegacyTreeLabelEditing.EditResult label = LegacyTreeLabelEditing.commit("alt", " neu ");
        if (!"neu".equals(label.newTitle) || !label.changed) throw new AssertionError("tree label trim failed");
        if (!"rtf_info".equals(LegacyToolbarPresentation.forLabel("RTF Info").action)) throw new AssertionError("toolbar rtf info failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("showRtfInfoDialog") || !text.contains("LegacyRichTextBoxSemantics.decorateHtmlDocument") || !text.contains("1.0.108-java-android-nativ")) throw new AssertionError("v35 Android bridge missing");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ")) throw new AssertionError("termux v35 version failed");
        }
    }



    private static void testLegacyRecentWindowDiagnosticsV35() throws Exception {
        List<String> paths = Arrays.asList("C:\\A\\eins.alx", "C:\\B\\zwei.alx", "ftp://user:pass@host/three.alx");
        List<LegacyRecentMenu.Slot> slots = LegacyRecentMenu.fromPaths(paths);
        if (slots.size() != 4 || !slots.get(0).visible || slots.get(3).visible) throw new AssertionError("recent slot load failed");
        String[] labels = LegacyRecentMenu.labelsFromPaths(paths);
        if (labels.length != 3 || !labels[1].contains("zwei.alx") || !labels[2].contains("three.alx") || labels[2].contains("pass")) throw new AssertionError("recent labels failed: " + Arrays.toString(labels));
        slots = LegacyRecentMenu.remember(slots, "C:\\D", "vier.alx");
        slots = LegacyRecentMenu.remember(slots, "C:\\E", "fuenf.alx");
        List<String> shifted = LegacyRecentMenu.toPaths(slots);
        if (shifted.size() != 4 || shifted.get(0).contains("eins.alx") || !shifted.get(3).contains("fuenf.alx")) throw new AssertionError("recent shift failed: " + shifted);
        LegacyRecentMenu.Activation activation = LegacyRecentMenu.activate(slots, 1, true);
        if (!activation.allowed || activation.openThis != 4 || !activation.selectedPath.contains("three.alx")) throw new AssertionError("recent activate failed: " + activation.selectedPath + " / " + activation.openThis);
        if (!LegacyRecentMenu.toPaths(activation.slots).get(3).contains("three.alx")) throw new AssertionError("recent click rotation failed: " + LegacyRecentMenu.toPaths(activation.slots));
        LegacyRecentMenu.Activation missing = LegacyRecentMenu.activate(slots, 1, false);
        if (missing.allowed || !missing.message.contains("File does not exist")) throw new AssertionError("recent missing file guard failed");

        LegacyDocumentTitle.State unsaved = LegacyDocumentTitle.build("Notizen Java Android Nativ", "", true, "", "", "");
        if (!unsaved.appTitle.contains("unbenannt.alx *") || unsaved.hasSaveTarget || !unsaved.sourceLine.contains("Noch nicht gespeichert")) throw new AssertionError("document title unsaved failed");
        LegacyDocumentTitle.State ftp = LegacyDocumentTitle.build("App", "demo.alx", false, "ftp://user@host/demo.alx", "", "content://x/y");
        if (!ftp.hasSaveTarget || !ftp.sourceLine.startsWith("ftp://") || !LegacyDocumentTitle.legacyDesktopTitle("demo.alx", "C:\\Notizen").equals("demo.alx in C:\\Notizen")) throw new AssertionError("document title target failed");

        LegacyWindowToggle.Result restored = LegacyWindowToggle.changeWinState(false, LegacyWindowToggle.WindowState.HIDDEN, true, false);
        if (!restored.visible || restored.state != LegacyWindowToggle.WindowState.MAXIMIZED || !"restore".equals(restored.action)) throw new AssertionError("window restore failed");
        LegacyWindowToggle.Result hidden = LegacyWindowToggle.changeWinState(true, LegacyWindowToggle.WindowState.NORMAL, false, false);
        if (hidden.visible || hidden.state != LegacyWindowToggle.WindowState.HIDDEN) throw new AssertionError("window hide failed");
        if (!LegacyWindowToggle.rememberMaximizedFromLocationChange(LegacyWindowToggle.WindowState.MAXIMIZED, false)) throw new AssertionError("window remember max failed");
        LegacyWindowToggle.Result minimizedStart = LegacyWindowToggle.visibleChangedForMinimizedStart(true, true, 0, true);
        if (minimizedStart.state != LegacyWindowToggle.WindowState.MINIMIZED || minimizedStart.minimizedStartedCount != 1) throw new AssertionError("minimized start failed");

        List<LegacyToolstripLayout.Strip> strips = LegacyToolstripLayout.defaultStrips(true, true);
        LegacyToolstripLayout.Placement placement = LegacyToolstripLayout.appendAfterVisible(strips, "ToolStrip3");
        if (LegacyToolstripLayout.visibleWidth(strips) <= 900 || placement.x != LegacyToolstripLayout.visibleWidth(strips)) throw new AssertionError("toolstrip width failed");
        LegacySettings settings = new LegacySettings();
        settings.toolstripPositions.put("font", new int[]{12, 3});
        int[] pos = LegacyToolstripLayout.positionForSettings(settings, "font", placement);
        if (pos[0] != 12 || pos[1] != 3) throw new AssertionError("toolstrip settings position failed");

        LegacyFtpRetryPolicy.Decision retry = LegacyFtpRetryPolicy.afterFailure(7, 2);
        LegacyFtpRetryPolicy.Decision fail = LegacyFtpRetryPolicy.afterFailure(7, 0);
        if (!retry.retry || retry.attemptsRemaining != 1 || fail.retry || LegacyFtpRetryPolicy.normalizeAttempts(99) != 10) throw new AssertionError("ftp retry policy failed");

        NoteDocument doc = NoteDocument.newDocument();
        doc.root.title = "root";
        doc.root.rtf = RtfUtils.plainTextToRtf("secret diagnostic text");
        doc.root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("other")));
        doc.markChanged();
        String report = LegacyDiagnosticReport.build(doc, doc.root, unsaved, settings, "1.0.108-java-android-nativ");
        if (!report.contains("Version: 1.0.108-java-android-nativ") || !report.contains("Knoten: 2") || !report.contains("Geändert: ja") || !report.contains("Hinweis")) throw new AssertionError("diagnostic report failed: " + report);
        if (report.contains("secret diagnostic text")) throw new AssertionError("diagnostic report leaked note text");
        if (!"diagnostics".equals(LegacyToolbarPresentation.forLabel("Diagnose").action)) throw new AssertionError("diagnose toolbar mapping failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("showDiagnostics") || !text.contains("LegacyRecentMenu.labelsFromPaths") || !text.contains("LegacyDocumentTitle.build") || !text.contains("1.0.108-java-android-nativ")) throw new AssertionError("v35 Android bridge missing");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ")) throw new AssertionError("termux v35 version failed");
        }
    }

    private static int countOccurrences(String text, String needle) {
        if (text == null || needle == null || needle.isEmpty()) return 0;
        int count = 0;
        for (int pos = 0; (pos = text.indexOf(needle, pos)) >= 0; pos += needle.length()) count++;
        return count;
    }


    private static void testLegacyColorFileDialogsTextExportV47() throws Exception {
        LegacyColorDialogModel.Decision textColor = LegacyColorDialogModel.chooseCss(LegacyColorDialogModel.Role.RTF_TEXT, "#123456");
        if (!textColor.valid || textColor.red != 0x12 || textColor.green != 0x34 || textColor.blue != 0x56 || !textColor.rtfPrefix.equals("\\cf1")) throw new AssertionError("text color parse failed");
        LegacyColorDialogModel.Decision hl = LegacyColorDialogModel.chooseCss(LegacyColorDialogModel.Role.RTF_HIGHLIGHT, "rgba(50%,0%,100%,.5)");
        if (!hl.valid || hl.red != 128 || hl.green != 0 || hl.blue != 255 || !hl.rtfPrefix.equals("\\highlight1")) throw new AssertionError("highlight rgba parse failed: " + hl.css);
        if (LegacyColorDialogModel.parseCssColor("#80112233") != 0xff112233) throw new AssertionError("argb css parse failed");
        if (LegacyColorDialogModel.legacyRandomReachableCount() != 14 || LegacyColorDialogModel.paletteLabels().length < 15) throw new AssertionError("legacy palette failed");

        String rtfColor = LegacyRtfSelectionFormatter.applyColorToSelection("abc def", 4, 7, textColor);
        if (!rtfColor.contains("{\\colortbl ;\\red18\\green52\\blue86;}" ) || !rtfColor.contains("\\cf1 def\\cf0")) throw new AssertionError("RTF text color failed: " + rtfColor);
        String rtfHl = LegacyRtfSelectionFormatter.applyColorToSelection("abc def", 0, 3, hl);
        if (!rtfHl.contains("\\highlight1 abc\\highlight0")) throw new AssertionError("RTF highlight failed: " + rtfHl);
        String html = RtfUtils.rtfToHtmlDocument(rtfColor);
        if (!html.toLowerCase(Locale.ROOT).contains("#123456") && !html.contains("rgb(18")) throw new AssertionError("RTF color HTML bridge failed: " + html);

        LegacyFileDialogModel.Spec open = LegacyFileDialogModel.openAlx("/tmp", "notes.alx");
        if (!open.open || open.create || !open.legacyFilter.contains("*.alx") || !open.androidMimeType.equals("*/*")) throw new AssertionError("open dialog failed");
        LegacyFileDialogModel.Spec save = LegacyFileDialogModel.saveAlx("/tmp", "notes");
        if (!save.create || !save.defaultFileName.equals("notes.alx")) throw new AssertionError("save alx extension failed: " + save.defaultFileName);
        if (!LegacyFileDialogModel.exportTxtAnsi("/tmp").legacyFilter.contains("ANSI") || !LegacyFileDialogModel.exportTxtUtf8("/tmp").legacyFilter.contains("UTF-8") || !LegacyFileDialogModel.exportTxtUnicode("/tmp").legacyFilter.contains("Unicode")) throw new AssertionError("text export dialog labels failed");
        if (!LegacyFileDialogModel.importHtml("/tmp").legacyFilter.contains("html") || !LegacyFileDialogModel.insertImage("/tmp").androidMimeType.equals("image/*")) throw new AssertionError("import/image dialog failed");

        String mixed = "A\nB\rC\r\nD€";
        if (!LegacyTextExportModel.normalizeCrLf(mixed).equals("A\r\nB\r\nC\r\nD€")) throw new AssertionError("CRLF normalization failed");
        byte[] ansi = LegacyTextExportModel.encode("Ä€", LegacyTextExportModel.Mode.ANSI);
        if (ansi.length != 2 || (ansi[0] & 0xff) != 0xc4 || (ansi[1] & 0xff) != 0x80) throw new AssertionError("ANSI bytes failed");
        byte[] unicode = LegacyTextExportModel.encode("A", LegacyTextExportModel.Mode.UNICODE);
        if (unicode.length != 4 || (unicode[0] & 0xff) != 0xff || (unicode[1] & 0xff) != 0xfe || unicode[2] != 65 || unicode[3] != 0) throw new AssertionError("Unicode BOM bytes failed");
        NoteNode root = new NoteNode("root", RtfUtils.plainTextToRtf("eins\nzwei"));
        LegacyTextExportModel.Result result = LegacyTextExportModel.forTree(root, LegacyTextExportModel.Mode.UNICODE, "/tmp");
        if (result.mode != LegacyTextExportModel.Mode.UNICODE || result.bytes.length < 4 || result.dialogSpec.kind != LegacyFileDialogModel.Kind.EXPORT_TXT_UNICODE) throw new AssertionError("text export model failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("LegacyColorDialogModel") || !android.contains("showRtfColorPalette") || !android.contains("applyRtfColorToSelection")) throw new AssertionError("v47 Android color bridge missing");
            if (!android.contains("LegacyFileDialogModel") || !android.contains("intentForDialog") || !android.contains("currentDialogDirectory")) throw new AssertionError("v47 Android file dialog bridge missing");
            if (!android.contains("exportTextAnsi") || !android.contains("exportTextUnicode") || !android.contains("pendingExportTextBytes") || !android.contains("LegacyTextExportModel.forTree")) throw new AssertionError("v47 Android text export bridge missing");
            if (android.contains("new Intent(Intent.ACTION_CREATE_DOCUMENT)") || android.contains("new Intent(Intent.ACTION_OPEN_DOCUMENT)")) throw new AssertionError("raw SAF intents should use LegacyFileDialogModel bridge");
            if (android.contains("text_color\\\".equals(spec.action) || \\\"highlight_color")) throw new AssertionError("RTF color actions still skipped");
            if (!android.contains("1.0.108-java-android-nativ")) throw new AssertionError("v47 version missing in Android source");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ")) throw new AssertionError("termux v47 version failed");
        }
    }


    private static void testLegacyConfigPaintSaveSearchV59() throws Exception {
        LegacySettings settings = new LegacySettings();
        settings.lastDirectory = "/docs";
        settings.lastFile = "demo.alx";
        settings.language = "Deutsch";
        settings.showInTaskbarWhenMinimized = true;
        settings.showDesknoteBorders = false;
        settings.scrollbarsChoice = 2;
        settings.windowX = 5000;
        settings.windowY = 5000;
        settings.windowWidth = 640;
        settings.windowHeight = 480;
        settings.windowState = "Minimized";
        settings.openOnceFile = "once.alx";
        settings.openOnceTimestamp = "123";
        settings.toolstripPositions.put("cutpastecopy", new int[]{-12, 7});

        LegacyConfigSnapshot.Snapshot snap = LegacyConfigSnapshot.fromSettings(settings, 800, 600);
        if (!"/docs".equals(snap.directory) || !"demo.alx".equals(snap.file) || snap.scrollbarsChoice != 2) throw new AssertionError("config snapshot fields failed");
        if (snap.window.locationAccepted || snap.window.location.x != 60 || !snap.window.minimizedTaskbarPulse || snap.window.showInTaskbar) throw new AssertionError("config window load guard failed: " + snap.window.summary());
        if (!snap.toolstrips.containsKey("haupt") || snap.toolstrips.get("cutpastecopy").x != 0 || snap.toolstrips.get("cutpastecopy").y != 7) throw new AssertionError("toolstrip point failed");
        if (!snap.summary().contains("Haftnotiz-Ränder: nein") || !snap.summary().contains("Position verworfen")) throw new AssertionError("config summary failed: " + snap.summary());
        LegacyConfigSnapshot.WindowData normal = new LegacyConfigSnapshot.WindowData(new LegacyConfigSnapshot.Point(10, 20), new LegacyConfigSnapshot.Point(700, 500), "Normal", true, false, 0, true);
        Map<String, LegacyConfigSnapshot.Point> strips = new LinkedHashMap<>();
        strips.put("haupt", new LegacyConfigSnapshot.Point(3, 4));
        LegacySettings exit = LegacyConfigSnapshot.applyOnExit(settings, normal, "/next", "next.alx", strips, 99, true);
        if (exit.windowX != 10 || exit.windowY != 20 || exit.windowWidth != 700 || exit.windowHeight != 500 || exit.scrollbarsChoice != 3 || !exit.showDesknoteBorders) throw new AssertionError("config on_exit failed");
        if (!"/next".equals(exit.lastDirectory) || !"next.alx".equals(exit.lastFile) || exit.toolstripPositions.get("haupt")[0] != 3) throw new AssertionError("config save fields failed");
        LegacySettings consumed = LegacyConfigSnapshot.consumeOpenOnce(settings);
        if (!consumed.openOnceFile.isEmpty() || !consumed.openOnceTimestamp.isEmpty()) throw new AssertionError("openOnce consume failed");

        LegacyDesktopNotePaint.PaintModel paint = LegacyDesktopNotePaint.expandedModel(240, 160, 500, 22);
        if (paint.editorRect.x != 12 || paint.editorRect.y != 32 || paint.labelRect.x != 37 || paint.labelMaxWidth != 170 || paint.commands.size() != 9) throw new AssertionError("desktop paint geometry failed: " + paint.summary());
        if (!paint.commands.get(0).kind.equals("image:aa") || !paint.commands.get(7).text.equals("x") || !paint.commands.get(8).text.equals("_")) throw new AssertionError("desktop paint commands failed");
        if (!"collapse-border".equals(LegacyDesktopNotePaint.mouseLeaveDecision(true, paint.window, paint.window.width + 4, paint.window.height + 4, true))) throw new AssertionError("desktop mouse leave failed");
        DesktopNoteState dns = new DesktopNoteState();
        dns.width = 300;
        dns.height = 180;
        if (!LegacyDesktopNotePaint.forState(dns, "Titel").summary().contains("Editor")) throw new AssertionError("desktop state summary failed");

        if (LegacySaveWorkflow.planForAndroid(false, false, false, true).action != LegacySaveWorkflow.Action.SAVE_AS) throw new AssertionError("save-as plan failed");
        if (LegacySaveWorkflow.planForAndroid(false, false, true, true).action != LegacySaveWorkflow.Action.SAVE_FTP) throw new AssertionError("ftp plan failed");
        if (LegacySaveWorkflow.planForAndroid(false, true, false, true).action != LegacySaveWorkflow.Action.SAVE_RAW_FILE) throw new AssertionError("raw file plan failed");
        if (LegacySaveWorkflow.planForAndroid(true, false, false, true).action != LegacySaveWorkflow.Action.SAVE_URI) throw new AssertionError("uri plan failed");
        LegacySaveWorkflow.DirtyClosePlan close = LegacySaveWorkflow.closePlan(true, "demo.alx");
        if (!close.ask || !close.message.contains("demo.alx") || !close.neutral.equals("Abbrechen")) throw new AssertionError("dirty close plan failed");
        if (!LegacySaveWorkflow.statusAfterSave(LegacySaveWorkflow.Action.SAVE_FTP, "demo.alx").contains("FTP gespeichert")) throw new AssertionError("save status failed");

        NoteNode root = new NoteNode("root", RtfUtils.plainTextToRtf("root"));
        NoteNode child = root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("abc abc")));
        List<SearchResult> raw = new ArrayList<>();
        raw.add(new SearchResult(root, 1, 3, true, "roo"));
        raw.add(new SearchResult(child, 4, 3, false, "abc"));
        LegacySearchResultNavigator.State nav = LegacySearchResultNavigator.fromResults(raw);
        if (nav.entries.size() != 2 || !nav.statusGerman().equals("Treffer 1 von 2")) throw new AssertionError("search navigator initial failed");
        nav = LegacySearchResultNavigator.next(nav);
        if (nav.current().node != child || !nav.statusGerman().equals("Treffer 2 von 2")) throw new AssertionError("search navigator next failed");
        nav = LegacySearchResultNavigator.next(nav);
        if (nav.current().node != root) throw new AssertionError("search navigator wrap failed");
        nav = LegacySearchResultNavigator.previous(nav);
        if (nav.current().node != child) throw new AssertionError("search navigator previous failed");

        if (!LegacyToolbarPresentation.forLabel("Config").action.equals("config_snapshot")) throw new AssertionError("toolbar config action failed");
        if (!LegacyToolbarPresentation.forLabel("Haft Layout").action.equals("desktop_note_layout")) throw new AssertionError("toolbar desktop layout action failed");
        if (!LegacyToolbarPresentation.forLabel("Export ANSI").action.equals("export_txt_ansi")) throw new AssertionError("toolbar ansi action failed");
        if (!LegacyToolbarPresentation.forLabel("Export Unicode").action.equals("export_txt_unicode")) throw new AssertionError("toolbar unicode action failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("showConfigSnapshot") || !android.contains("LegacyConfigSnapshot.fromSettings")) throw new AssertionError("v59 config Android bridge missing");
            if (!android.contains("showDesktopNoteLayout") || !android.contains("LegacyDesktopNotePaint.forState")) throw new AssertionError("v59 desktop paint Android bridge missing");
            if (!android.contains("LegacySaveWorkflow.planForAndroid")) throw new AssertionError("v59 save workflow Android bridge missing");
            if (!android.contains("LegacySearchResultNavigator.fromResults")) throw new AssertionError("v59 search navigator Android bridge missing");
            if (!android.contains("1.0.108-java-android-nativ")) throw new AssertionError("v59 version missing in Android source");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ")) throw new AssertionError("termux v59 version failed");
        }
    }

    private static void testLegacyLifecycleTreeTrayV65() throws Exception {
        NoteDocument doc = new NoteDocument();
        LegacyTreeCreation.CreationResult rootResult = LegacyTreeCreation.ensureRoot(doc, "");
        if (rootResult.node == null || !"...".equals(rootResult.node.title) || rootResult.kind != LegacyTreeCreation.Kind.ROOT) throw new AssertionError("tree root creation failed");
        LegacyTreeCreation.CreationResult child = LegacyTreeCreation.newChild(doc, rootResult.node, " child ");
        LegacyTreeCreation.CreationResult next = LegacyTreeCreation.newNext(doc, child.node, "");
        if (!"child".equals(child.node.title) || !"...".equals(next.node.title) || rootResult.node.children.size() != 2 || next.index != 1) throw new AssertionError("tree creation failed");

        doc.displayName = "demo.alx";
        doc.password = "secret";
        doc.markChanged();
        LegacyDocumentLifecycle.Status state = LegacyDocumentLifecycle.status(doc, child.node, true, false);
        if (!state.changed || !state.canSave || state.defaultSaveDecision != LegacyDocumentLifecycle.SaveDecision.SAVE || !state.windowTitle("Notizen").contains("*")) throw new AssertionError("document lifecycle failed");
        if (!LegacyDocumentLifecycle.haveChangedFile(doc) || !LegacyDocumentLifecycle.setUnchanged(doc) || doc.changed) throw new AssertionError("changed/set unchanged failed");
        if (LegacyDocumentLifecycle.saveAnyway(doc, false, true, false) != LegacyDocumentLifecycle.SaveDecision.SAVE_FTP) throw new AssertionError("save anyway ftp failed");

        DesktopNoteState small = new DesktopNoteState();
        small.width = 20; small.height = 20;
        child.node.desktopNote = small;
        DesktopNoteState normal = new DesktopNoteState();
        normal.width = 200; normal.height = 120;
        next.node.desktopNote = normal;
        LegacyDesktopNoteTrayRegistry.Registry registry = LegacyDesktopNoteTrayRegistry.fromTree(rootResult.node);
        if (registry.desktopCount != 2 || registry.menuItems.size() != 5 || !registry.summary().contains("Haftnotizen: 2")) throw new AssertionError("desktop tray registry failed");
        LegacyTraySelectionModel.Decision exit = LegacyTraySelectionModel.decide(registry, 0);
        LegacyTraySelectionModel.Decision toggle = LegacyTraySelectionModel.decide(registry, 1);
        LegacyTraySelectionModel.Decision show = LegacyTraySelectionModel.decide(registry, 3);
        if (exit.action != LegacyTraySelectionModel.Action.EXIT || toggle.action != LegacyTraySelectionModel.Action.TOGGLE_MAIN || show.action != LegacyTraySelectionModel.Action.SHOW_DESKTOP_NOTE || !show.reloadSmallWindow) throw new AssertionError("tray selection failed");

        LegacyAppDataConfigPath.Paths paths = LegacyAppDataConfigPath.build("C:\\Users\\me\\AppData\\Roaming\\Notizen", "C:\\Users\\me\\Documents");
        if (!paths.configFile.endsWith("Notizen\\notizen.config.xml") || paths.configDirectory.contains("Notizen\\Notizen") || !paths.defaultAlxFile.endsWith("Notizen\\unbenannt.alx")) throw new AssertionError("appdata path failed: " + paths.configFile + " / " + paths.defaultAlxFile);
        LegacyClipboardFormatModel.Preferred pref = LegacyClipboardFormatModel.preferred(new LegacyClipboardFormatModel.Formats(false, true, true, true, true));
        if (pref.format != LegacyClipboardFormatModel.Format.RTF || LegacyClipboardFormatModel.decide(new LegacyClipboardFormatModel.Formats(true, false, false, false, false), false).pasteIntoEditor) throw new AssertionError("clipboard priority failed");

        doc.markChanged();
        LegacyCloseResetModel.ClosePlan cancel = LegacyCloseResetModel.plan(doc, doc.password, LegacyCloseResetModel.Choice.CANCEL);
        LegacyCloseResetModel.ClosePlan save = LegacyCloseResetModel.plan(doc, doc.password, LegacyCloseResetModel.Choice.SAVE);
        if (!cancel.canceled || cancel.proceed || !save.saveFirst || !save.wipePasswordFromMemory) throw new AssertionError("close plan failed");
        LegacyCloseResetModel.ResetState reset = LegacyCloseResetModel.afterClose("1.0.108-java-android-nativ");
        if (!"unbenannt.alx".equals(reset.displayName) || !reset.changed) throw new AssertionError("reset state failed");

        LegacySettings settings = new LegacySettings();
        settings.windowX = 99999; settings.windowY = 99999; settings.windowWidth = 100; settings.windowHeight = 100; settings.windowState = "Maximized";
        LegacyConfigLifecycle.WindowData loaded = LegacyConfigLifecycle.onLoad(settings, 800, 600);
        if (loaded.x != 60 || loaded.y != 60 || loaded.width < 320 || loaded.height < 240 || loaded.norm0OrMax1 != 1) throw new AssertionError("config on_load failed");
        LegacyConfigLifecycle.ExitSnapshot snap = LegacyConfigLifecycle.onExit(settings, "/tmp", "demo.alx");
        LegacyConfigLifecycle.applyExitSnapshot(settings, snap);
        if (!"/tmp".equals(settings.lastDirectory) || !"demo.alx".equals(settings.lastFile)) throw new AssertionError("config on_exit failed");

        if (!"file_close".equals(LegacyToolbarPresentation.forLabel("Schließen").action) || !"legacy_status".equals(LegacyToolbarPresentation.forLabel("Status").action) || !"desktop_note_tray".equals(LegacyToolbarPresentation.forLabel("Haftliste").action)) throw new AssertionError("v65 toolbar specs failed");
        LegacyActionState.State actionState = new LegacyActionState.State(true, true, false, false, false, false, true, false);
        if (!LegacyActionState.isEnabled("file_close", actionState) || !LegacyActionState.isEnabled("legacy_status", actionState) || !LegacyActionState.isEnabled("desktop_note_tray", actionState)) throw new AssertionError("v65 action enablement failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("closeDocumentLegacy") || !android.contains("showLifecycleStatus") || !android.contains("showDesktopNoteTrayList") || !android.contains("LegacyTreeCreation.newChild") || !android.contains("1.0.108-java-android-nativ")) throw new AssertionError("v65 Android bridge missing");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ")) throw new AssertionError("termux v65-to-v71 version failed");
        }
    }



    private static void testLegacyRuntimeLayoutPackagingV71() throws Exception {
        if (!LegacyRuntimeIdentity.APP_DESKTOP_ID.equals("notizen-py-qt")) throw new AssertionError("desktop id failed");
        if (!LegacyRuntimeIdentity.linuxLauncherEnvironment().get("RESOURCE_NAME").equals("notizen-py-qt")) throw new AssertionError("resource env failed");
        String exec = LegacyRuntimeIdentity.linuxDesktopExec();
        if (!exec.equals("env NOTIZEN_RESET_WINDOW=1 RESOURCE_NAME=notizen-py-qt python3 -m notizen_py_qt --show --no-tray --reset-window %f")) throw new AssertionError("desktop exec failed: " + exec);
        String entry = LegacyRuntimeIdentity.linuxDesktopEntry();
        if (!entry.contains("Icon=notizen-py-qt") || !entry.contains("StartupWMClass=notizen-py-qt") || entry.contains("Exec=sh ") || entry.contains("Exec=bash ")) throw new AssertionError("desktop entry failed: " + entry);
        String ident = LegacyRuntimeIdentity.summary("1.0.108-java-android-nativ");
        if (!ident.contains("de.notizen.android") || !ident.contains("1.0.108-java-android-nativ") || !ident.contains("notizen_py_qt")) throw new AssertionError("runtime identity summary failed: " + ident);

        LegacyMainLayoutModel.Layout wide = LegacyMainLayoutModel.compute(800, 500, LegacyMainLayoutModel.ANDROID_ORIENTATION_LANDSCAPE);
        LegacyMainLayoutModel.Layout narrow = LegacyMainLayoutModel.compute(360, 780, LegacyMainLayoutModel.ANDROID_ORIENTATION_PORTRAIT);
        if (!wide.wide || wide.treeWeight != 1 || wide.editorWeight != 2 || !wide.orientation.equals("horizontal")) throw new AssertionError("wide layout failed: " + wide.summary());
        if (narrow.wide || narrow.treeWeight != 0 || narrow.editorWeight != 1 || !narrow.orientation.equals("vertical")) throw new AssertionError("narrow layout failed: " + narrow.summary());
        if (!wide.treeHeaderHidden || !wide.titleLabelsHidden || !wide.toolbarIconOnly || !wide.summary().contains("Baum/Editor-Gewicht")) throw new AssertionError("layout flags failed");

        if (LegacyPackagePermissionModel.unixZipModeForPath("scripts/install_linux_launcher.sh", false) != 0755) throw new AssertionError("script mode failed");
        if (LegacyPackagePermissionModel.unixZipModeForPath("scripts/probe.py", false) != 0755) throw new AssertionError("python script mode failed");
        if (LegacyPackagePermissionModel.unixZipModeForPath("legacy_build_metadata/old/scripts/probe.py", false) != 0644) throw new AssertionError("legacy metadata mode failed");
        if (LegacyPackagePermissionModel.unixZipModeForPath("Notizen PyQt.desktop", false) != 0755) throw new AssertionError("desktop mode failed");
        if (!LegacyPackagePermissionModel.describe("src/notizen_py_qt/app.py", false).endsWith("644")) throw new AssertionError("mode description failed");

        if (!"layout_diagnostics".equals(LegacyToolbarPresentation.forLabel("Layout").action)
                || !"runtime_identity".equals(LegacyToolbarPresentation.forLabel("Launcher").action)
                || !"export_txt_ansi".equals(LegacyToolbarPresentation.forLabel("Export ANSI").action)
                || !"export_txt_unicode".equals(LegacyToolbarPresentation.forLabel("Export Unicode").action)) {
            throw new AssertionError("v71 toolbar mapping failed");
        }
        LegacyActionState.State noDoc = new LegacyActionState.State(false, false, false, false, false, false, false, false);
        if (!LegacyActionState.isEnabled("layout_diagnostics", noDoc) || !LegacyActionState.isEnabled("runtime_identity", noDoc)) throw new AssertionError("v71 action enablement failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("showLayoutDiagnostics") || !android.contains("LegacyMainLayoutModel.compute") || !android.contains("showRuntimeIdentity") || !android.contains("LegacyRuntimeIdentity.summary")) throw new AssertionError("v71 Android runtime/layout bridge missing");
            if (!android.contains("LegacyPackagePermissionModel.describe") || !android.contains("1.0.108-java-android-nativ")) throw new AssertionError("v71 Android version/packaging bridge missing");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ")) throw new AssertionError("termux v71 version failed");
        }
    }


    private static void testLegacyActivationToolbarChromeAutosaveBuildV83() throws Exception {
        LegacyActivationDialogFocus.Plan empty = LegacyActivationDialogFocus.onMainActivated(LegacyActivationDialogFocus.stateFromVisibleNames());
        if (empty.blockMainWindowMove || empty.finalActiveDialog != null) throw new AssertionError("empty activation failed");
        LegacyActivationDialogFocus.Plan plan = LegacyActivationDialogFocus.onMainActivated(LegacyActivationDialogFocus.stateFromVisibleNames("suche", "ftpkram", "passwort_dialog"));
        if (!plan.blockMainWindowMove || plan.activationOrder.size() != 3 || plan.activationOrder.get(0) != LegacyActivationDialogFocus.Dialog.FTP || plan.finalActiveDialog != LegacyActivationDialogFocus.Dialog.SEARCH) {
            throw new AssertionError("dialog focus order failed: " + plan.summary());
        }

        LegacyToolbarToggleModel.ToggleState showFont = LegacyToolbarToggleModel.toggle(LegacyToolbarToggleModel.MenuToggle.FONT, false, true, 123);
        LegacyToolbarToggleModel.ToggleState hideEdit = LegacyToolbarToggleModel.toggle(LegacyToolbarToggleModel.MenuToggle.EDIT, true, true, 200);
        LegacyToolbarToggleModel.ToggleState blockedEdit = LegacyToolbarToggleModel.toggle(LegacyToolbarToggleModel.MenuToggle.EDIT, false, false, 200);
        if (!showFont.checked || !showFont.visible || showFont.x != 123 || !showFont.toolstripName.equals("ToolStrip_fontstyle")) throw new AssertionError("font toggle failed");
        if (hideEdit.checked || hideEdit.visible || !hideEdit.action.equals("hide")) throw new AssertionError("edit hide failed");
        if (!blockedEdit.action.equals("blocked-content-disabled")) throw new AssertionError("edit blocked failed");

        java.util.List<LegacyMainWindowChrome.PaintCommand> chrome = LegacyMainWindowChrome.paintCommands("Notizen Java Android Nativ", 800, 600);
        if (chrome.size() < 7 || !chrome.get(0).type.equals("image-left-bb") || !LegacyMainWindowChrome.summary("Andere Datei", 400, 300).contains("title")) throw new AssertionError("chrome paint failed");
        if (LegacyMainWindowChrome.hitTest(10, 10, 800, 600, false, false).action != LegacyMainWindowChrome.HitAction.MINIMIZE) throw new AssertionError("chrome minimize hit failed");
        if (LegacyMainWindowChrome.hitTest(799, 599, 800, 600, true, false).action != LegacyMainWindowChrome.HitAction.RESIZE_BOTTOM_RIGHT) throw new AssertionError("chrome resize hit failed");
        if (!LegacyMainWindowChrome.hitTest(100, 20, 800, 600, true, true).blocked) throw new AssertionError("chrome block failed");

        LegacyAutosaveTimerTick.TickPlan tick = LegacyAutosaveTimerTick.decide(true, true, true, true);
        LegacyAutosaveTimerTick.TickPlan missing = LegacyAutosaveTimerTick.decide(true, true, false, true);
        if (!tick.shouldSave || !tick.reason.equals("legacy-save-anyway") || missing.shouldSave || !missing.reason.equals("file-missing")) throw new AssertionError("autosave tick failed");
        NoteDocument doc = NoteDocument.newDocument();
        doc.markChanged();
        if (!LegacyAutosaveTimerTick.fromAndroid(doc, false, false, true).shouldSave) throw new AssertionError("android autosave target failed");

        LegacyCTextState.State c0 = LegacyCTextState.create();
        LegacyCTextState.State c1 = LegacyCTextState.setText(c0, "abc", true, true);
        if (!c1.mark || !c1.text.equals("abc")) throw new AssertionError("ctext set failed");
        NoteNode withDesk = new NoteNode("desk", RtfUtils.plainTextToRtf("desk"));
        withDesk.desktopNote = new DesktopNoteState();
        if (!LegacyCTextState.fromNode(withDesk).hasDesktopNote) throw new AssertionError("ctext node failed");

        LegacyApkBuildPipeline.Params params = LegacyApkBuildPipeline.defaultParams();
        String build = LegacyApkBuildPipeline.summary(params);
        if (params.versionCode != 108 || !params.versionName.equals("1.0.108-java-android-nativ") || !build.contains("d8 --lib android.jar") || !build.contains("NotizenJavaAndroidNativ-v108-debug.apk")) throw new AssertionError("apk pipeline failed: " + build);
        if (!"dialog_focus".equals(LegacyToolbarPresentation.forLabel("Dialoge").action) || !"apk_build_plan".equals(LegacyToolbarPresentation.forLabel("Buildplan").action)) throw new AssertionError("v83 toolbar mapping failed");
        LegacyActionState.State noDoc = new LegacyActionState.State(false, false, false, false, false, false, false, false);
        if (!LegacyActionState.isEnabled("dialog_focus", noDoc) || !LegacyActionState.isEnabled("apk_build_plan", noDoc)) throw new AssertionError("v83 action enablement failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("showActivationFocusModel") || !android.contains("LegacyMainWindowChrome.summary") || !android.contains("showApkBuildPlan") || !android.contains("1.0.108-java-android-nativ")) throw new AssertionError("v83 Android bridge missing");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ") || !script.contains("--lib \"$ANDROID_JAR\"")) throw new AssertionError("termux v83 version/d8 lib failed");
        }
    }

    private static void testLegacyFontWindowStreamPipelineV95() throws Exception {
        LegacyFontSetModel.SelectionPlan sel = LegacyFontSetModel.selection(10, 3, 0);
        if (!sel.selectedAllBecauseEmpty || sel.applyStart != 0 || sel.applyLength != 10 || sel.restoreStart != 3 || sel.restoreLength != 0) throw new AssertionError("font selection all failed");
        LegacyFontSetModel.FontPlan editor = LegacyFontSetModel.plan(true, false, 20, 2, 5, LegacyFontSetModel.Function.STYLE, "bold", "Arial", 12, 0, true);
        if (editor.target != LegacyFontSetModel.Target.EDITOR_SELECTION || !editor.rtfAction.equals("format_bold") || !editor.desktopNoteReload || !editor.nodeRtfChanged) throw new AssertionError("font editor plan failed: " + editor.summary());
        LegacyFontSetModel.FontPlan tree = LegacyFontSetModel.plan(true, true, 20, 2, 5, LegacyFontSetModel.Function.STYLE, "italic", "", 12, 0, false);
        if (tree.target != LegacyFontSetModel.Target.TREE_NODES || !tree.treeStyle.equals("italic") || !tree.nodeRtfChanged) throw new AssertionError("font tree plan failed: " + tree.summary());
        LegacyFontSetModel.FontPlan fam = LegacyFontSetModel.plan(true, true, 20, 2, 5, LegacyFontSetModel.Function.FAMILY, "", "Times New Roman", 12, 0, false);
        if (fam.target != LegacyFontSetModel.Target.EDITOR_SELECTION || !fam.rtfAction.contains("font_family")) throw new AssertionError("font family plan failed: " + fam.summary());
        if (LegacyFontSetModel.plan(true, false, 20, 1, 1, LegacyFontSetModel.Function.SIZE_BIGGER, "", "", 100, 0, false).resultingSize != 100) throw new AssertionError("font bigger boundary failed");
        if (LegacyFontSetModel.plan(true, false, 20, 1, 1, LegacyFontSetModel.Function.SIZE_SMALLER, "", "", 8, 0, false).resultingSize != 8) throw new AssertionError("font smaller boundary failed");
        if (LegacyFontSetModel.plan(false, false, 1, 0, 0, LegacyFontSetModel.Function.STYLE, "", "", 10, 0, false).target != LegacyFontSetModel.Target.BLOCKED_NO_DOCUMENT) throw new AssertionError("font no doc failed");

        LegacyWindowMoveResizeModel.Rect r = new LegacyWindowMoveResizeModel.Rect(10, 20, 300, 200);
        LegacyWindowMoveResizeModel.DragState move = LegacyWindowMoveResizeModel.mouseDown(r, 100, 10, 200, 100, true, false);
        LegacyWindowMoveResizeModel.Update moved = LegacyWindowMoveResizeModel.mouseMove(move, 250, 150, 100, 80);
        if (move.mode != LegacyWindowMoveResizeModel.Mode.MOVE || moved.rect.x != 60 || moved.rect.y != 70) throw new AssertionError("window move failed: " + moved.summary());
        LegacyWindowMoveResizeModel.DragState resize = LegacyWindowMoveResizeModel.mouseDown(r, 299, 199, 309, 219, true, false);
        LegacyWindowMoveResizeModel.Update resized = LegacyWindowMoveResizeModel.mouseMove(resize, 450, 380, 120, 100);
        if (resize.mode != LegacyWindowMoveResizeModel.Mode.RESIZE || resized.rect.width != 440 || resized.rect.height != 360) throw new AssertionError("window resize failed: " + resized.summary());
        if (LegacyWindowMoveResizeModel.mouseDown(r, 5, 5, 15, 25, true, false).mode != LegacyWindowMoveResizeModel.Mode.MINIMIZE) throw new AssertionError("window minimize failed");
        if (LegacyWindowMoveResizeModel.mouseDown(r, 100, 20, 200, 100, true, true).mode != LegacyWindowMoveResizeModel.Mode.BLOCKED) throw new AssertionError("window block failed");

        String pw = "abcdefghijklmnopqrstuvwx";
        LegacyAlxStreamPipeline.Plan save = LegacyAlxStreamPipeline.savePlan("/tmp/a.alx", true, 3, pw);
        if (save.target != LegacyAlxStreamPipeline.Target.LOCAL_FILE || !save.backupBeforeWrite || !save.encrypted || !save.layers.toString().contains("DES1") || !save.layers.toString().contains("GZipStream")) throw new AssertionError("alx encrypted local save failed: " + save.summary());
        LegacyAlxStreamPipeline.Plan ftp = LegacyAlxStreamPipeline.savePlan("ftp://example.org/a.alx", true, 3, "                        ");
        if (ftp.target != LegacyAlxStreamPipeline.Target.FTP || ftp.backupBeforeWrite || ftp.encrypted) throw new AssertionError("alx ftp save failed: " + ftp.summary());
        LegacyAlxStreamPipeline.Plan open = LegacyAlxStreamPipeline.openPlan("content://notizen/a", pw);
        if (open.target != LegacyAlxStreamPipeline.Target.ANDROID_URI || !open.encrypted || !open.closeOrder.toString().contains("CryptoStreams")) throw new AssertionError("alx uri open failed: " + open.summary());
        if (!LegacyAlxStreamPipeline.hasLegacyPassword(pw) || LegacyAlxStreamPipeline.hasLegacyPassword("                        ")) throw new AssertionError("legacy password state failed");

        if (!"font_set_plan".equals(LegacyToolbarPresentation.forLabel("Fontplan").action)
                || !"window_move_resize".equals(LegacyToolbarPresentation.forLabel("Maus").action)
                || !"alx_stream_pipeline".equals(LegacyToolbarPresentation.forLabel("ALX Pipe").action)) throw new AssertionError("v95 toolbar mapping failed");
        LegacyActionState.State noDoc = new LegacyActionState.State(false, false, false, false, false, false, false, false);
        if (!LegacyActionState.isEnabled("font_set_plan", noDoc) || !LegacyActionState.isEnabled("window_move_resize", noDoc) || !LegacyActionState.isEnabled("alx_stream_pipeline", noDoc)) throw new AssertionError("v95 action enablement failed");

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("showLegacyFontSetModel") || !android.contains("showWindowMoveResizeModel") || !android.contains("showAlxPipelineModel") || !android.contains("1.0.108-java-android-nativ")) throw new AssertionError("v95 Android bridge missing");
            if (android.contains("settings.autosaveEnabled")) throw new AssertionError("MainActivity still references missing LegacySettings.autosaveEnabled field");
            if (!android.contains("settings == null || settings.autosaveSeconds > 0")) throw new AssertionError("autosave enabled bridge must use LegacySettings.autosaveSeconds");
        }
        File termux = new File("tools/notizen-build-apk-termux.sh");
        if (!termux.isFile()) termux = new File("../tools/notizen-build-apk-termux.sh");
        if (termux.isFile()) {
            String script = readUtf8File(termux);
            if (!script.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !script.contains("1.0.108-java-android-nativ") || !script.contains("--lib \"$ANDROID_JAR\"")) throw new AssertionError("termux v95 version failed");
        }
    }


    private static void testAndroidTreeExpansionResizeAndToolbarV98() throws Exception {
        NoteDocument d = NoteDocument.newDocument();
        d.root.title = "root";
        d.root.expanded = false;
        NoteNode child = d.root.addChild(new NoteNode("child", RtfUtils.plainTextToRtf("body")));
        child.expanded = true;
        NoteNode leaf = child.addChild(new NoteNode("leaf", ""));
        leaf.expanded = false;
        NoteDocument round = AlxIo.load(AlxIo.dump(d, ""), "");
        if (round.root.expanded || !round.root.children.get(0).expanded || round.root.children.get(0).children.get(0).expanded) {
            throw new AssertionError("v98 expansion roundtrip failed");
        }
        String xml = "<notizen-alx2><Notiz name=\"root\" expanded=\"no\"><Notiz name=\"child\" IsExpanded=\"True\" /></Notiz></notizen-alx2>";
        NoteDocument aliases = AlxIo.parseAlxXml(xml);
        if (aliases.root.expanded || !aliases.root.children.get(0).expanded) throw new AssertionError("v98 expansion aliases failed");

        LegacySettings settings = new LegacySettings();
        settings.androidTreePaneWidthDp = 5;
        LegacySettings loaded = LegacySettings.fromXmlBytes(settings.toXmlBytes());
        if (loaded.androidTreePaneWidthDp != 24 || LegacySettings.normalizeAndroidTreePaneWidthDp(1800) != 1200) {
            throw new AssertionError("v98 tree pane width normalization failed: " + loaded.androidTreePaneWidthDp);
        }
        if (!"font_bigger".equals(LegacyToolbarPresentation.forLabel("Größer").action)
                || !"font_size".equals(LegacyToolbarPresentation.forLabel("Größe").action)) {
            throw new AssertionError("v98 text icon toolbar mapping failed");
        }

        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String android = readUtf8File(source);
            if (!android.contains("finishLoadedDocument")
                    || !android.contains("treeExpansionSummary")
                    || !android.contains("createPaneDivider")
                    || !android.contains("applyTreePaneWidthPx")
                    || !android.contains("addToolbarRow(toolbarPanel, \"Datei\")")
                    || !android.contains("addToolbarRow(toolbarPanel, \"Baum/Text\")")
                    || !android.contains("addToolbarRow(toolbarPanel, \"Text\")")
                    || !android.contains("DEFAULT_TOOLBAR_BUTTON_DP")
                    || !android.contains("toolbarRowGlyph")
                    || !android.contains("new LinearLayout.LayoutParams(dp(toolbarButtonDp), dp(toolbarButtonDp))")) {
                throw new AssertionError("v98 Android resize/toolbar bridge missing");
            }
            if (android.contains("TextView header = new TextView") || android.contains("fileView = new TextView") || android.contains("statusView = new TextView") || android.contains("Ansicht wiederhergestellt")) {
                throw new AssertionError("v99 compact UI still has removed header/file/status bars");
            }
        }
        File treeAdapter = new File("app/src/main/java/de/notizen/android/TreeListAdapter.java");
        if (!treeAdapter.isFile()) treeAdapter = new File("../app/src/main/java/de/notizen/android/TreeListAdapter.java");
        if (treeAdapter.isFile()) {
            String adapter = readUtf8File(treeAdapter);
            if (!adapter.contains("rowBackground") || !adapter.contains("GradientDrawable") || !adapter.contains("▾ ") || !adapter.contains("▸ ")) {
                throw new AssertionError("v98 tree row presentation missing");
            }
        }
    }

    private static void testAndroidV100RuntimeColorPromptLayout() throws Exception {
        File source = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!source.isFile()) source = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (source.isFile()) {
            String text = readUtf8File(source);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("CONTENT_HEADER_DP")
                    || !text.contains("yellowBridge")
                    || !text.contains("RUNTIME_SNAPSHOT_FILE")
                    || !text.contains("installCrashSnapshotHandler")
                    || !text.contains("restoreRuntimeSnapshotIfPresent")
                    || !text.contains("isBlankSingleNodeDocumentForPrompt")
                    || !text.contains("RTF-Textfarbe")
                    || !text.contains("ForegroundColorSpan")) {
                throw new AssertionError("v100 Android runtime/color/layout bridge missing");
            }
        }
        File gradle = new File("app/build.gradle");
        if (!gradle.isFile()) gradle = new File("../app/build.gradle");
        if (gradle.isFile()) {
            String build = readUtf8File(gradle);
            if (!build.contains("versionCode 108") || !build.contains("1.0.108-java-android-nativ")) throw new AssertionError("v100 Gradle version missing");
        }
    }

    private static void testAndroidV101WidgetAndActiveFormatting() throws Exception {
        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        File adapter = new File("app/src/main/java/de/notizen/android/TreeListAdapter.java");
        if (!adapter.isFile()) adapter = new File("../app/src/main/java/de/notizen/android/TreeListAdapter.java");
        File widget = new File("app/src/main/java/de/notizen/android/NoteWidgetProvider.java");
        if (!widget.isFile()) widget = new File("../app/src/main/java/de/notizen/android/NoteWidgetProvider.java");
        File manifest = new File("app/src/main/AndroidManifest.xml");
        if (!manifest.isFile()) manifest = new File("../app/src/main/AndroidManifest.xml");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("newEmptyDocument")
                    || !text.contains("applyActiveFormatAction")
                    || !text.contains("FormatTarget")
                    || !text.contains("requestAndroidWidgetForCurrentNode")
                    || !text.contains("wouldOverwriteUsefulRuntimeSnapshotWithBlankDocument")
                    || !text.contains("shouldIgnoreTransientBlankEditorOverwrite")
                    || !text.contains("NODE_TITLE_STYLE_ATTR")) {
                throw new AssertionError("v101 MainActivity active formatting/widget guards missing");
            }
        }
        if (adapter.isFile()) {
            String text = readUtf8File(adapter);
            if (!text.contains("androidTitleStyle") || !text.contains("UNDERLINE_TEXT_FLAG") || !text.contains("androidTitleFont")) {
                throw new AssertionError("v101 tree adapter title formatting missing");
            }
        }
        if (widget.isFile()) {
            String text = readUtf8File(widget);
            if (!text.contains("extends AppWidgetProvider") || !text.contains("requestPinAppWidget") || !text.contains("RemoteViews")) {
                throw new AssertionError("v101 widget provider missing");
            }
        }
        if (manifest.isFile()) {
            String text = readUtf8File(manifest);
            if (!text.contains(".NoteWidgetProvider") || !text.contains("android.appwidget.provider")) {
                throw new AssertionError("v101 widget manifest missing");
            }
        }
    }

    private static void testAndroidV102MiddleToolbarActivePane() throws Exception {
        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        File script = new File("tools/notizen-build-apk-termux.sh");
        if (!script.isFile()) script = new File("../tools/notizen-build-apk-termux.sh");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("ActivePane")
                    || !text.contains("addToolbarRow(toolbarPanel, \"Baum/Text\")")
                    || !text.contains("activePaneForMiddleToolbar")
                    || !text.contains("requireTreePaneForMiddleAction")
                    || !text.contains("copyForActiveMiddleTarget")
                    || !text.contains("pasteIntoEditorFromClipboard")
                    || !text.contains("deleteForActiveMiddleTarget")
                    || !text.contains("internalEditorClipboardPlain")
                    || !text.contains("applyEditorParagraphIndent")) {
                throw new AssertionError("v102 middle toolbar active-pane bridge missing");
            }
        }
        if (script.isFile()) {
            String text = readUtf8File(script);
            if (!text.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"") || !text.contains("1.0.108-java-android-nativ")) {
                throw new AssertionError("v102 termux version missing");
            }
        }
    }

    private static void testAndroidV103BlankStartAndExpansionRoundtrip() throws Exception {
        NoteDocument fresh = LegacyFreshStartModel.newFreshStartDocument();
        if (!LegacyFreshStartModel.isFreshStartShape(fresh) || fresh.root.children.size() != 0 || fresh.root.rtf.length() != 0 || !fresh.changed) {
            throw new AssertionError("v103 fresh-start model does not match Notizen .NET");
        }

        NoteDocument doc = NoteDocument.newDocument();
        doc.root.title = "root";
        doc.root.expanded = true;
        NoteNode closed = doc.root.addChild(new NoteNode("zu", RtfUtils.plainTextToRtf("Text zu")));
        closed.expanded = false;
        NoteNode open = doc.root.addChild(new NoteNode("offen", ""));
        open.expanded = true;
        NoteNode nested = closed.addChild(new NoteNode("nested", ""));
        nested.expanded = false;
        String xml = new String(AlxIo.documentToXmlBytes(doc), StandardCharsets.UTF_16);
        if (!xml.contains("isexpanded=\"True\"") || !xml.contains("isexpanded=\"False\"")) {
            throw new AssertionError("v103 ALX expansion write missing: " + xml);
        }
        NoteDocument loaded = AlxIo.parseAlxXml(xml);
        if (!loaded.root.expanded
                || loaded.root.children.get(0).expanded
                || !loaded.root.children.get(1).expanded
                || loaded.root.children.get(0).children.get(0).expanded) {
            throw new AssertionError("v103 ALX expansion reload failed");
        }

        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("resetToFreshStartDocument")
                    || !text.contains("LegacyFreshStartModel.newFreshStartDocument")
                    || !text.contains("clearVisibleDocumentViewsForFreshStart")
                    || !text.contains("treeAdapter.setRows(new ArrayList<>())")
                    || !text.contains("rebuildTree();\n        selectNode(document.ensureRoot(), false)")
                    || !text.contains("pendingAfterSaveAsCallback")
                    || !text.contains("saveDocumentAs(callback)")) {
                throw new AssertionError("v103 fresh-start Android bridge missing");
            }
        }
    }

    private static void testAndroidV104TouchZoomAndImageSafety() throws Exception {
        if (LegacyTouchZoomModel.toolbarButtonDpByStep(24, +1) != 28
                || LegacyTouchZoomModel.toolbarButtonDpByStep(48, +1) != 48
                || LegacyTouchZoomModel.toolbarButtonDpByStep(24, -1) != 24) {
            throw new AssertionError("v104 toolbar pinch size model failed");
        }
        if (Math.abs(LegacyTouchZoomModel.headerTextSpByStep(14f, +1) - 15f) > 0.01f
                || Math.abs(LegacyTouchZoomModel.headerTextSpByStep(10f, -1) - 10f) > 0.01f) {
            throw new AssertionError("v104 header pinch size model failed");
        }
        if (LegacyTouchZoomModel.fontSizeByStep(12, +1) <= 12 || LegacyTouchZoomModel.fontSizeByStep(12, -1) >= 12) {
            throw new AssertionError("v104 font pinch model failed");
        }
        if (!LegacyTouchZoomModel.shouldDownsampleForRtf(4000, 1000, 100, true)
                || !LegacyTouchZoomModel.shouldDownsampleForRtf(800, 600, LegacyTouchZoomModel.MAX_EMBEDDED_IMAGE_BYTES + 1L, true)
                || !LegacyTouchZoomModel.shouldDownsampleForRtf(800, 600, 100, false)
                || LegacyTouchZoomModel.shouldDownsampleForRtf(800, 600, 100, true)) {
            throw new AssertionError("v104 image downsample policy failed");
        }

        LegacySettings settings = new LegacySettings();
        settings.androidTreePaneWidthDp = 333;
        settings.androidToolbarButtonDp = 44;
        settings.androidHeaderTextSp = 18f;
        LegacySettings loaded = LegacySettings.fromXmlBytes(settings.toXmlBytes());
        if (loaded.androidTreePaneWidthDp != 333 || loaded.androidToolbarButtonDp != 44 || Math.abs(loaded.androidHeaderTextSp - 18f) > 0.01f) {
            throw new AssertionError("v104 android-ui zoom settings lost");
        }
        LegacySettings clamped = LegacySettings.fromXmlString("<notizen-alx><android-ui toolbar-button-dp=\"99\" header-text-sp=\"99\" /></notizen-alx>");
        if (clamped.androidToolbarButtonDp != LegacyTouchZoomModel.MAX_TOOLBAR_BUTTON_DP
                || Math.abs(clamped.androidHeaderTextSp - LegacyTouchZoomModel.MAX_HEADER_TEXT_SP) > 0.01f) {
            throw new AssertionError("v104 android-ui zoom settings clamp failed");
        }

        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("ScaleGestureDetector")
                    || !text.contains("zoomRtfTextByStep")
                    || !text.contains("zoomTreeTextByStep")
                    || !text.contains("zoomToolbarButtonsByStep")
                    || !text.contains("zoomHeaderTextByStep")
                    || !text.contains("prepareImageForRtf")
                    || !text.contains("decodeScaledBitmapFromUri")
                    || !text.contains("MAX_EMBEDDED_IMAGE_BYTES")) {
                throw new AssertionError("v104 Android pinch/image bridge missing");
            }
        }
    }


    private static void testAndroidV105ToolbarZoomStability() throws Exception {
        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("ToolbarZoomLayout extends LinearLayout")
                    || !text.contains("dispatchTouchEvent(MotionEvent event)")
                    || !text.contains("scheduleAndroidUiZoomSettingsSave")
                    || !text.contains("UI_ZOOM_SAVE_DELAY_MS")
                    || !text.contains("PINCH_MAX_STEPS_PER_EVENT")
                    || !text.contains("safeScaleGesture")
                    || !text.contains("cancelSentToChildren")) {
                throw new AssertionError("v106 toolbar zoom stability bridge missing");
            }
            if (text.contains("b.setOnTouchListener((view, event) -> handleToolbarPinchTouch(event))")
                    || text.contains("caption.setOnTouchListener((view, event) -> handleToolbarPinchTouch(event))")
                    || text.contains("row.setOnTouchListener((view, event) -> handleToolbarPinchTouch(event))")) {
                throw new AssertionError("v106 toolbar child touch listeners still compete with panel pinch");
            }
        }

        File script = new File("tools/notizen-build-apk-termux.sh");
        if (!script.isFile()) script = new File("../tools/notizen-build-apk-termux.sh");
        if (script.isFile()) {
            String scriptText = readUtf8File(script);
            if (!scriptText.contains("VERSION_CODE=\"${VERSION_CODE:-108}\"")
                    || !scriptText.contains("1.0.108-java-android-nativ")) {
                throw new AssertionError("v106 termux version failed");
            }
        }
    }


    private static void testAndroidV106RtfToolbarAndQuickSearch() throws Exception {
        NoteDocument doc = NoteDocument.newDocument();
        doc.root.title = "Wurzel";
        doc.root.rtf = RtfUtils.plainTextToRtf("oben");
        NoteNode a = doc.root.addChild(new NoteNode("Alpha", RtfUtils.plainTextToRtf("eins gesucht")));
        NoteNode b = doc.root.addChild(new NoteNode("Beta", RtfUtils.plainTextToRtf("außen gesucht")));
        a.addChild(new NoteNode("Gamma", RtfUtils.plainTextToRtf("innen gesucht")));
        List<SearchResult> subtree = Search.searchSubtree(a, "gesucht", false, false, true);
        List<SearchResult> all = Search.searchNodes(doc.root, "gesucht", false, false, true);
        if (subtree.size() != 2 || all.size() != 3) throw new AssertionError("v106 subtree quick search failed: " + subtree.size() + "/" + all.size());

        LegacyQuickSearchBar.Options empty = LegacyQuickSearchBar.normalize("   ", false, false, false, true);
        LegacyQuickSearchBar.Options opts = LegacyQuickSearchBar.normalize("Alpha", true, true, true, false);
        if (empty.accepted || !empty.message.contains("Suchbegriff") || !opts.accepted
                || opts.scope != LegacyQuickSearchBar.Scope.WHOLE_TREE
                || !LegacyQuickSearchBar.status(2, 5, LegacyQuickSearchBar.Scope.CURRENT_SUBTREE).contains("Treffer 2 von 5")) {
            throw new AssertionError("v106 quick search options/status failed");
        }
        if (LegacyRtfUndoModel.trimStartIndex(40, LegacyRtfUndoModel.DEFAULT_LIMIT) != 8
                || !new LegacyRtfUndoModel.State(1, 0, 32).canUndo()
                || new LegacyRtfUndoModel.State(0, 0, 32).canRedo()) {
            throw new AssertionError("v106 rtf undo model failed");
        }
        if (LegacyRichTextToolbar.findByAction("format_strike") == null
                || LegacyRichTextToolbar.findByAction("align_justify") == null
                || LegacyRichTextToolbar.findByAction("text_color") == null) {
            throw new AssertionError("v106 rich text toolbar actions missing");
        }
        if (!LegacyToolbarPresentation.forLabel("Durchgestrichen").displayText(true).contains("S")
                || !LegacyToolbarPresentation.forLabel("Blocksatz").displayText(true).contains("▤")) {
            throw new AssertionError("v106 toolbar presentation missing");
        }

        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("createQuickSearchBar")
                    || !text.contains("quickSearchNext")
                    || !text.contains("quickSearchAllResults")
                    || !text.contains("LegacyQuickSearchBar.normalize")
                    || !text.contains("EditorHistoryEntry")
                    || !text.contains("undoEditorChange")
                    || !text.contains("redoEditorChange")
                    || !text.contains("textToolbarActionButtons")
                    || !text.contains("format_strike")
                    || !text.contains("align_justify")
                    || !text.contains("RtfParagraphAlignmentSpan")) {
                throw new AssertionError("v106 Android RTF/search bridge missing");
            }
        }
    }


    private static void testAndroidV107ContextMenusAndTreeDragDrop() throws Exception {
        if (!"tree_paste_as_child".equals(LegacyContextMenus.byLegacyIndex(LegacyContextMenus.MenuKind.TREE, 11).action)
                || !"tree_move_before_target".equals(LegacyContextMenus.byLegacyIndex(LegacyContextMenus.MenuKind.TREE, 19).action)
                || !"editor_select_all".equals(LegacyContextMenus.byLegacyIndex(LegacyContextMenus.MenuKind.EDITOR, 9).action)
                || !LegacyContextMenus.byAction("editor_text_color").accessibilityLabel().equals("Textfarbe…")) {
            throw new AssertionError("v107 expanded context menu model failed");
        }

        NoteDocument doc = NoteDocument.newDocument();
        NoteNode a = doc.root.addChild(new NoteNode("A", ""));
        NoteNode b = doc.root.addChild(new NoteNode("B", ""));
        NoteNode c = doc.root.addChild(new NoteNode("C", ""));
        NoteNode child = a.addChild(new NoteNode("Kind", ""));
        if (!LegacyTreeDragDrop.decision(a, c, doc.root, LegacyTreeDragDrop.DropMode.AFTER).allowed
                || !"move-after-target".equals(LegacyTreeDragDrop.decision(a, c, doc.root, LegacyTreeDragDrop.DropMode.AFTER).reason)) {
            throw new AssertionError("v107 drag/drop decision failed");
        }
        NoteNode movedAfter = LegacyTreeDragDrop.moveAfter(a, c, doc.root);
        if (movedAfter != a || doc.root.children.get(2) != a) throw new AssertionError("v107 move-after failed");
        NoteNode movedChild = LegacyTreeDragDrop.moveAsChild(c, b, doc.root);
        if (movedChild != c || c.parent != b || b.children.get(b.children.size() - 1) != c || !b.expanded) throw new AssertionError("v107 move-as-child failed");
        if (NoteTreeOps.legacyMoveAsLastChild(a, child) != null) throw new AssertionError("v107 prevented descendant target failed");
        NoteNode pasted = NoteTreeOps.legacyPasteCloneAsLastChild(a, b);
        if (pasted == null || pasted.parent != b || !"A".equals(pasted.title) || pasted == a) throw new AssertionError("v107 paste-as-child clone failed");

        LegacyTreeLabelEditing.EditResult rename = LegacyTreeLabelEditing.commit("Alt", "   ");
        if (!rename.changed || !rename.usedFallback || !"...".equals(rename.newTitle)) throw new AssertionError("v107 inline rename fallback failed");

        File adapter = new File("app/src/main/java/de/notizen/android/TreeListAdapter.java");
        if (!adapter.isFile()) adapter = new File("../app/src/main/java/de/notizen/android/TreeListAdapter.java");
        if (adapter.isFile()) {
            String text = readUtf8File(adapter);
            if (!text.contains("InlineEditListener")
                    || !text.contains("beginInlineEdit")
                    || !text.contains("DropPreview")
                    || !text.contains("setDropPreview")
                    || !text.contains("IME_ACTION_DONE")) {
                throw new AssertionError("v107 adapter inline/drop bridge missing");
            }
        }

        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("showTreeContextMenu")
                    || !text.contains("showEditorContextMenu")
                    || !text.contains("beginTreeDragFromRow")
                    || !text.contains("handleTreeDragEvent")
                    || !text.contains("pasteNodeAsChild")
                    || !text.contains("renameCurrentNodeInline")
                    || !text.contains("updateActivePaneChrome")) {
                throw new AssertionError("v107 Android context/tree bridge missing");
            }
        }
    }


    private static void testAndroidV108WidgetsExportSettings() throws Exception {
        NoteDocument doc = NoteDocument.newDocument();
        doc.root.title = "Root";
        NoteNode a = doc.root.addChild(new NoteNode("Alpha Notiz", RtfUtils.plainTextToRtf("Alpha Text")));
        NoteNode b = doc.root.addChild(new NoteNode("Beta", RtfUtils.plainTextToRtf("Beta Text")));
        a.bgArgb = 0x00112233;
        a.fgArgb = 0x00445566;
        LegacyAndroidWidgetRegistry.WidgetSpec widget = LegacyAndroidWidgetRegistry.fromNode(7, doc, a, "datei.alx", 15f);
        if (!"0".equals(widget.nodePath) || !widget.title.equals("Alpha Notiz") || !widget.text.contains("Alpha Text")
                || widget.bgArgb != 0xff112233 || widget.fgArgb != 0xff445566 || Math.abs(widget.textSizeSp - 15f) > 0.01f) {
            throw new AssertionError("v108 widget spec failed: " + widget.summary());
        }
        if (LegacyAndroidWidgetRegistry.nodeByIndexPath(doc.root, "1") != b || LegacyAndroidWidgetRegistry.nodeByIndexPath(doc.root, "9") != doc.root) {
            throw new AssertionError("v108 widget node path resolution failed");
        }
        java.util.List<LegacyAndroidWidgetRegistry.WidgetSpec> refreshed = LegacyAndroidWidgetRegistry.refreshExisting(doc, "datei2.alx", java.util.Arrays.asList(widget), 13f);
        if (refreshed.size() != 1 || !refreshed.get(0).documentName.equals("datei2.alx") || !LegacyAndroidWidgetRegistry.listSummary(refreshed).contains("Alpha")) {
            throw new AssertionError("v108 widget refresh/list failed");
        }

        LegacyExportShareModel.Payload html = LegacyExportShareModel.build(doc, a, LegacyExportShareModel.Scope.CURRENT_SUBTREE, LegacyExportShareModel.Format.HTML);
        if (!html.fileName.endsWith(".html") || !html.mimeType.equals("text/html") || !new String(html.bytes, StandardCharsets.UTF_8).contains("Alpha Text")) {
            throw new AssertionError("v108 HTML export payload failed");
        }
        LegacyExportShareModel.Payload rtf = LegacyExportShareModel.build(doc, a, LegacyExportShareModel.Scope.CURRENT_NOTE, LegacyExportShareModel.Format.RTF);
        if (!rtf.fileName.endsWith(".rtf") || !rtf.mimeType.equals("application/rtf") || !rtf.textForShare.contains("Alpha Text")) {
            throw new AssertionError("v108 RTF export payload failed");
        }
        LegacyExportShareModel.Payload unicode = LegacyExportShareModel.build(doc, doc.root, LegacyExportShareModel.Scope.ROOT, LegacyExportShareModel.Format.TXT_UNICODE);
        if (!unicode.fileName.endsWith("-unicode.txt") || unicode.bytes.length < 4 || (unicode.bytes[0] & 0xff) != 0xff || !LegacyExportShareModel.hubIndexShares(4)) {
            throw new AssertionError("v108 Unicode/share export payload failed");
        }

        LegacySettings settings = new LegacySettings();
        settings.androidTreeTextSp = 19f;
        settings.androidEditorTextSp = 21f;
        settings.androidWidgetTextSp = 16f;
        settings.androidLargeImageLongEdgePx = 2048;
        settings.androidAutoDownsampleLargeImages = false;
        settings.androidRestoreRuntimeSnapshot = false;
        settings.androidShowDiagnosticsToolbar = false;
        LegacySettings loaded = LegacySettings.fromXmlBytes(settings.toXmlBytes());
        if (Math.abs(loaded.androidTreeTextSp - 19f) > 0.01f || Math.abs(loaded.androidEditorTextSp - 21f) > 0.01f
                || Math.abs(loaded.androidWidgetTextSp - 16f) > 0.01f || loaded.androidLargeImageLongEdgePx != 2048
                || loaded.androidAutoDownsampleLargeImages || loaded.androidRestoreRuntimeSnapshot || loaded.androidShowDiagnosticsToolbar) {
            throw new AssertionError("v108 Android settings roundtrip failed: " + loaded.summary());
        }
        LegacyAndroidSettingsUiModel.ApplyResult applied = LegacyAndroidSettingsUiModel.apply(new LegacySettings(), "/tmp", "999", "9", "7", "99", "2", "99", true, true, false);
        if (applied.settings.androidToolbarButtonDp != LegacyTouchZoomModel.MAX_TOOLBAR_BUTTON_DP
                || applied.settings.androidTreeTextSp != 8f
                || applied.settings.androidEditorTextSp != 48f
                || applied.settings.androidWidgetTextSp != 8f
                || applied.settings.androidLargeImageLongEdgePx != 480
                || applied.settings.androidShowDiagnosticsToolbar) {
            throw new AssertionError("v108 Android settings apply/clamp failed: " + LegacyAndroidSettingsUiModel.summary(applied.settings));
        }

        File main = new File("app/src/main/java/de/notizen/android/MainActivity.java");
        if (!main.isFile()) main = new File("../app/src/main/java/de/notizen/android/MainActivity.java");
        if (main.isFile()) {
            String text = readUtf8File(main);
            if (!text.contains("1.0.108-java-android-nativ")
                    || !text.contains("showExportHubDialog")
                    || !text.contains("shareExportPayload")
                    || !text.contains("REQ_EXPORT_GENERIC")
                    || !text.contains("showPrintPreview")
                    || !text.contains("updateAndroidWidgetsFromDocument")
                    || !text.contains("LegacyAndroidSettingsUiModel")
                    || !text.contains("androidShowDiagnosticsToolbar")
                    || !text.contains("NoteWidgetProvider.ACTION_OPEN_WIDGET_NODE")) {
                throw new AssertionError("v108 Android widgets/export/settings bridge missing");
            }
        }
        File widgetProvider = new File("app/src/main/java/de/notizen/android/NoteWidgetProvider.java");
        if (!widgetProvider.isFile()) widgetProvider = new File("../app/src/main/java/de/notizen/android/NoteWidgetProvider.java");
        if (widgetProvider.isFile()) {
            String text = readUtf8File(widgetProvider);
            if (!text.contains("updateExistingWidgetsFromDocument") || !text.contains("EXTRA_NODE_PATH") || !text.contains("setTextViewTextSize")) {
                throw new AssertionError("v108 widget provider update/path/textsize missing");
            }
        }
        File provider = new File("app/src/main/java/de/notizen/android/ExportFileProvider.java");
        if (!provider.isFile()) provider = new File("../app/src/main/java/de/notizen/android/ExportFileProvider.java");
        if (provider.isFile()) {
            String text = readUtf8File(provider);
            if (!text.contains("extends ContentProvider") || !text.contains("openFile") || !text.contains("OpenableColumns")) {
                throw new AssertionError("v108 export file provider missing");
            }
        }
        File manifest = new File("app/src/main/AndroidManifest.xml");
        if (!manifest.isFile()) manifest = new File("../app/src/main/AndroidManifest.xml");
        if (manifest.isFile()) {
            String text = readUtf8File(manifest);
            if (!text.contains(".ExportFileProvider") || !text.contains("grantUriPermissions")) {
                throw new AssertionError("v108 export provider manifest missing");
            }
        }
    }


    private static String readUtf8File(File file) throws Exception {
        java.io.FileInputStream in = new java.io.FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        in.close();
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

}
