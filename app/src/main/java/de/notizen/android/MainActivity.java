package de.notizen.android;

import android.app.Activity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.notizen.android.core.AlarmSpec;
import de.notizen.android.core.AlarmUtils;
import de.notizen.android.core.LegacyAlarmDialogModel;
import de.notizen.android.core.AlxException;
import de.notizen.android.core.AlxIo;
import de.notizen.android.core.DesktopNoteState;
import de.notizen.android.core.Exporters;
import de.notizen.android.core.FtpSyncError;
import de.notizen.android.core.FtpTarget;
import de.notizen.android.core.LegacyAutosave;
import de.notizen.android.core.LegacyColors;
import de.notizen.android.core.LegacyDesktopNoteContextMenu;
import de.notizen.android.core.LegacyDialogModels;
import de.notizen.android.core.LegacyEditorActions;
import de.notizen.android.core.LegacyFeedback;
import de.notizen.android.core.LegacyFontSizeEntry;
import de.notizen.android.core.LegacyEditorNodeSync;
import de.notizen.android.core.LegacySearchSession;
import de.notizen.android.core.LegacyAboutHelp;
import de.notizen.android.core.LegacyFtpDialogModel;
import de.notizen.android.core.LegacyPasswordDialogModel;
import de.notizen.android.core.LegacySearchDialogModel;
import de.notizen.android.core.LegacySettingsDialogModel;
import de.notizen.android.core.LegacyI18n;
import de.notizen.android.core.LegacyImporters;
import de.notizen.android.core.LegacyOpenTarget;
import de.notizen.android.core.LegacyDocumentTitle;
import de.notizen.android.core.LegacyDiagnosticReport;
import de.notizen.android.core.LegacyDocumentLifecycle;
import de.notizen.android.core.LegacyTreeCreation;
import de.notizen.android.core.LegacyDesktopNoteTrayRegistry;
import de.notizen.android.core.LegacyTraySelectionModel;
import de.notizen.android.core.LegacyAppDataConfigPath;
import de.notizen.android.core.LegacyClipboardFormatModel;
import de.notizen.android.core.LegacyCloseResetModel;
import de.notizen.android.core.LegacyConfigLifecycle;
import de.notizen.android.core.LegacyRuntimeIdentity;
import de.notizen.android.core.LegacyActivationDialogFocus;
import de.notizen.android.core.LegacyToolbarToggleModel;
import de.notizen.android.core.LegacyMainWindowChrome;
import de.notizen.android.core.LegacyAutosaveTimerTick;
import de.notizen.android.core.LegacyCTextState;
import de.notizen.android.core.LegacyApkBuildPipeline;
import de.notizen.android.core.LegacyFontSetModel;
import de.notizen.android.core.LegacyWindowMoveResizeModel;
import de.notizen.android.core.LegacyAlxStreamPipeline;
import de.notizen.android.core.LegacyMainLayoutModel;
import de.notizen.android.core.LegacyPackagePermissionModel;
import de.notizen.android.core.LegacyRecentMenu;
import de.notizen.android.core.LegacyNodeExport;
import de.notizen.android.core.LegacyPrintLayout;
import de.notizen.android.core.LegacyRichTextToolbar;
import de.notizen.android.core.LegacyRichTextBoxSemantics;
import de.notizen.android.core.LegacyToolbarPresentation;
import de.notizen.android.core.LegacyRtfSelectionFormatter;
import de.notizen.android.core.LegacyTextExportModel;
import de.notizen.android.core.LegacyFileDialogModel;
import de.notizen.android.core.LegacyColorDialogModel;
import de.notizen.android.core.LegacySearchResultNavigator;
import de.notizen.android.core.LegacySaveWorkflow;
import de.notizen.android.core.LegacyDesktopNotePaint;
import de.notizen.android.core.LegacyConfigSnapshot;
import de.notizen.android.core.LegacyScrollbars;
import de.notizen.android.core.LegacyTreeDragDrop;
import de.notizen.android.core.LegacyTreeExpansion;
import de.notizen.android.core.LegacyTreeDelete;
import de.notizen.android.core.LegacyUnifiedNote;
import de.notizen.android.core.LegacyDesktopNoteTreeOps;
import de.notizen.android.core.LegacyShortcuts;
import de.notizen.android.core.LegacySettings;
import de.notizen.android.core.LegacyValidation;
import de.notizen.android.core.NodeClipboard;
import de.notizen.android.core.NoteDocument;
import de.notizen.android.core.NoteNode;
import de.notizen.android.core.NoteTreeOps;
import de.notizen.android.core.RtfUtils;
import de.notizen.android.core.Search;
import de.notizen.android.core.SearchResult;
import de.notizen.android.core.SimpleFtpClient;
import de.notizen.android.core.TreeStats;

public final class MainActivity extends Activity {
    private static final int REQ_OPEN = 1001;
    private static final int REQ_SAVE_AS = 1002;
    private static final int REQ_EXPORT_TEXT = 1003;
    private static final int REQ_EXPORT_HTML = 1004;
    private static final int REQ_EXPORT_RTF = 1005;
    private static final int REQ_IMPORT_CONFIG = 1006;
    private static final int REQ_INSERT_IMAGE = 1007;
    private static final int REQ_EXPORT_CONFIG = 1008;
    private static final int REQ_IMPORT_HTML = 1009;
    private static final int REQ_IMPORT_TEXT = 1010;
    private static final int REQ_IMPORT_RTF = 1011;
    private static final int REQ_EXPORT_NODE_TEXT = 1012;
    private static final int REQ_EXPORT_NODE_RTF = 1013;
    private static final int REQ_EXPORT_TEXT_ANSI = 1014;
    private static final int REQ_EXPORT_TEXT_UNICODE = 1015;

    private NoteDocument document = NoteDocument.newDocument();
    private NoteNode currentNode;
    private Uri currentUri;
    private File currentRawFile;
    private String currentDisplayName = "unbenannt.alx";
    private String pendingExportText;
    private byte[] pendingExportTextBytes;
    private String pendingExportHtml;
    private String pendingExportRtf;
    private byte[] pendingExportConfig;
    private NoteNode internalClipboardNode;
    private LegacySettings settings = new LegacySettings();
    private FtpTarget currentFtpTarget;
    private ExecutorService ioExecutor;
    private Handler mainHandler;
    private final SimpleDateFormat alarmDateFormat = new SimpleDateFormat(LegacyAlarmDialogModel.DATE_PATTERN, Locale.GERMANY);
    private final LegacySearchSession searchSession = new LegacySearchSession();

    private TreeListAdapter treeAdapter;
    private ListView treeList;
    private TextView rootTitleView;
    private EditText titleEdit;
    private EditText editor;
    private TextView statusView;
    private TextView fileView;
    private WebView pendingPrintView;
    private Runnable autosaveRunnable;
    private boolean autosaveInFlight = false;

    private boolean loadingEditor = false;
    private boolean editorDirty = false;
    private boolean titleDirty = false;
    private boolean editorHorizontallyScrolling = false;

    private interface PasswordCallback { void onPassword(String password); }
    private interface ContinueCallback { void run(); }
    private interface BackgroundCallback<T> { T run() throws Exception; }
    private interface UiCallback<T> { void run(T value); }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        settings = AndroidSettingsStore.load(this);
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        buildUi();
        requestNotificationPermissionIfUseful();
        selectNode(document.ensureRoot(), false);
        boolean openedByIntent = handleViewIntent(getIntent());
        if (!openedByIntent) consumeOpenOnceFileIfPresent();
        scheduleAutosaveTick();
    }

    @Override protected void onDestroy() {
        if (mainHandler != null && autosaveRunnable != null) mainHandler.removeCallbacks(autosaveRunnable);
        if (ioExecutor != null) ioExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleViewIntent(intent);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(250, 250, 250));

        TextView header = new TextView(this);
        header.setText("Notizen Android");
        header.setTextSize(20f);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(10), dp(12), dp(6));
        header.setTextColor(Color.rgb(25, 25, 25));
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        HorizontalScrollView toolbarScroll = new HorizontalScrollView(this);
        toolbarScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setPadding(dp(6), dp(4), dp(6), dp(4));
        toolbarScroll.addView(toolbar, new HorizontalScrollView.LayoutParams(-2, -2));
        root.addView(toolbarScroll, new LinearLayout.LayoutParams(-1, -2));

        addButton(toolbar, "Neu", v -> confirmDiscardThen(this::newDocument));
        addButton(toolbar, "Öffnen", v -> confirmDiscardThen(this::openDocument));
        addButton(toolbar, "Letzte", v -> showRecentFiles());
        addButton(toolbar, "Speichern", v -> saveDocument());
        addButton(toolbar, "Speichern unter", v -> saveDocumentAs());
        addButton(toolbar, "Sicherungen", v -> showBackups());
        addButton(toolbar, "Einstellungen", v -> showSettingsDialog());
        addButton(toolbar, "Config", v -> showConfigSnapshot());
        addButton(toolbar, "FTP öffnen", v -> showFtpDialog(false));
        addButton(toolbar, "FTP speichern", v -> showFtpDialog(true));
        addButton(toolbar, "Schließen", v -> closeDocumentLegacy());
        addButton(toolbar, "Status", v -> showLifecycleStatus());
        addButton(toolbar, "Kind", v -> newChild());
        addButton(toolbar, "Daneben", v -> newNext());
        addButton(toolbar, "Auf/Zu", v -> toggleExpanded());
        addButton(toolbar, "Alle auf", v -> expandAllNodes());
        addButton(toolbar, "Alle zu", v -> collapseAllNodes());
        addButton(toolbar, "Löschen", v -> deleteCurrent());
        addButton(toolbar, "Kopieren", v -> copyCurrentNode(false));
        addButton(toolbar, "Ausschneiden", v -> cutCurrentNode());
        addButton(toolbar, "Einfügen", v -> pasteNode());
        addButton(toolbar, "Rauf", v -> moveCurrentUp());
        addButton(toolbar, "Runter", v -> moveCurrentDown());
        addButton(toolbar, "Einrücken", v -> indentCurrent());
        addButton(toolbar, "Ausrücken", v -> outdentCurrent());
        addButton(toolbar, "Vor Ziel", v -> showMoveBeforeTargetDialog());
        addButton(toolbar, "Datum", v -> insertDate());
        addButton(toolbar, "Punkt", v -> insertLegacyBullet());
        addButton(toolbar, "RTF Format", v -> showRtfFormatDialog());
        addButton(toolbar, "Scroll", v -> cycleScrollbars());
        addButton(toolbar, "Bild", v -> insertImage());
        addButton(toolbar, "Vorschau", v -> showRichPreview());
        addButton(toolbar, "RTF Info", v -> showRtfInfoDialog());
        addButton(toolbar, "Drucken", v -> showPrintDialog());
        addButton(toolbar, "HTML Import", v -> importHtmlNote());
        addButton(toolbar, "TXT Import", v -> importTextIntoCurrent());
        addButton(toolbar, "RTF Import", v -> importRtfIntoCurrent());
        addButton(toolbar, "Farben", v -> showColorDialog());
        addButton(toolbar, "Haftnotiz", v -> showDesktopNoteDialog());
        addButton(toolbar, "Haft Layout", v -> showDesktopNoteLayout());
        addButton(toolbar, "Haftliste", v -> showDesktopNoteTrayList());
        addButton(toolbar, "Haft weg", v -> clearDesktopNotesInSubtree());
        addButton(toolbar, "Wecker", v -> showAlarmDialog());
        addButton(toolbar, "Suche", v -> showSearchDialog());
        addButton(toolbar, "Export TXT", v -> exportText());
        addButton(toolbar, "Export ANSI", v -> exportTextAnsi());
        addButton(toolbar, "Export Unicode", v -> exportTextUnicode());
        addButton(toolbar, "Export HTML", v -> exportHtml());
        addButton(toolbar, "Export RTF", v -> exportRtf());
        addButton(toolbar, "Knoten TXT", v -> exportCurrentNodeText());
        addButton(toolbar, "Knoten RTF", v -> exportCurrentNodeRtf());
        addButton(toolbar, "Teilbaum", v -> createUnifiedCurrentNote());
        addButton(toolbar, "Gesamt", v -> createUnifiedRootNote());
        addButton(toolbar, "Passwort", v -> showPasswordChangeDialog());
        addButton(toolbar, "Statistik", v -> showStats());
        addButton(toolbar, "Validieren", v -> showValidationSummary());
        addButton(toolbar, "Diagnose", v -> showDiagnostics());
        addButton(toolbar, "Layout", v -> showLayoutDiagnostics());
        addButton(toolbar, "Launcher", v -> showRuntimeIdentity());
        addButton(toolbar, "Dialoge", v -> showActivationFocusModel());
        addButton(toolbar, "Toolbars", v -> showToolbarToggleModel());
        addButton(toolbar, "Fenster", v -> showWindowChromeModel());
        addButton(toolbar, "Autosave", v -> showAutosaveTickModel());
        addButton(toolbar, "Fontplan", v -> showLegacyFontSetModel());
        addButton(toolbar, "Maus", v -> showWindowMoveResizeModel());
        addButton(toolbar, "ALX Pipe", v -> showAlxPipelineModel());
        addButton(toolbar, "Buildplan", v -> showApkBuildPlan());
        addButton(toolbar, "Feedback", v -> showFeedbackDialog());
        addButton(toolbar, "Info", v -> showInfo());

        fileView = new TextView(this);
        fileView.setPadding(dp(10), dp(2), dp(10), dp(4));
        fileView.setTextSize(12f);
        fileView.setTextColor(Color.rgb(90, 90, 90));
        root.addView(fileView, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout content = new LinearLayout(this);
        int widthDp = getResources().getConfiguration().screenWidthDp;
        boolean wide = widthDp >= 700 || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        content.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        content.setPadding(dp(6), dp(0), dp(6), dp(6));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setPadding(0, 0, wide ? dp(5) : 0, wide ? 0 : dp(5));
        rootTitleView = new TextView(this);
        rootTitleView.setBackgroundColor(Color.rgb(255, 255, 192));
        rootTitleView.setTextColor(Color.rgb(30, 30, 30));
        rootTitleView.setPadding(dp(8), dp(7), dp(8), dp(7));
        rootTitleView.setTextSize(15f);
        left.addView(rootTitleView, new LinearLayout.LayoutParams(-1, -2));
        treeList = new ListView(this);
        treeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        treeAdapter = new TreeListAdapter(this);
        treeList.setAdapter(treeAdapter);
        treeList.setOnItemClickListener((parent, view, position, id) -> selectNode(treeAdapter.getItem(position).node, true));
        treeList.setOnItemLongClickListener((parent, view, position, id) -> {
            NoteNode node = treeAdapter.getItem(position).node;
            node.expanded = !node.expanded;
            document.markChanged();
            rebuildTree();
            return true;
        });
        left.addView(treeList, new LinearLayout.LayoutParams(-1, wide ? -1 : dp(210), 1));
        content.addView(left, wide ? new LinearLayout.LayoutParams(0, -1, 1.0f) : new LinearLayout.LayoutParams(-1, -2));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        titleEdit = new EditText(this);
        titleEdit.setSingleLine(true);
        titleEdit.setTextSize(16f);
        titleEdit.setBackgroundColor(Color.rgb(255, 255, 192));
        titleEdit.setPadding(dp(8), 0, dp(8), 0);
        titleEdit.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (loadingEditor || currentNode == null) return;
                currentNode.title = s.toString().isEmpty() ? "..." : s.toString();
                titleDirty = true;
                document.markChanged();
                treeAdapter.notifyDataSetChanged();
                updateTitle();
            }
        });
        right.addView(titleEdit, new LinearLayout.LayoutParams(-1, dp(48)));

        editor = new EditText(this);
        editor.setTextSize(17f);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setMinLines(12);
        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(false);
        applyEditorScrollbars();
        editor.setPadding(dp(10), dp(10), dp(10), dp(10));
        editor.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (loadingEditor) return;
                editorDirty = true;
                document.markChanged();
                updateTitle();
            }
        });
        right.addView(editor, new LinearLayout.LayoutParams(-1, 0, 1));
        content.addView(right, wide ? new LinearLayout.LayoutParams(0, -1, 2.1f) : new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        statusView = new TextView(this);
        statusView.setText("Bereit");
        statusView.setTextSize(12f);
        statusView.setTextColor(Color.rgb(70, 70, 70));
        statusView.setPadding(dp(10), dp(4), dp(10), dp(8));
        root.addView(statusView, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
        updateTitle();
        rebuildTree();
    }

    private void requestNotificationPermissionIfUseful() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2001);
        }
    }

    private void addButton(LinearLayout toolbar, String label, View.OnClickListener listener) {
        LegacyToolbarPresentation.ButtonSpec spec = LegacyToolbarPresentation.forLabel(label);
        Button b = new Button(this);
        b.setText(spec.displayText(true));
        b.setAllCaps(false);
        b.setMinWidth(dp(44));
        b.setMinHeight(dp(44));
        b.setContentDescription(spec.contentDescription());
        if (Build.VERSION.SDK_INT >= 26) b.setTooltipText(spec.contentDescription());
        b.setOnClickListener(listener);
        toolbar.addView(b, new LinearLayout.LayoutParams(-2, dp(LegacyToolbarPresentation.MAIN_TOOLBAR_BUTTON_HEIGHT_DP)));
    }

    private void applyEditorScrollbars() {
        if (editor == null) return;
        if (settings == null) settings = AndroidSettingsStore.load(this);
        LegacyScrollbars.Mode mode = LegacyScrollbars.modeForChoice(settings.scrollbarsChoice);
        editorHorizontallyScrolling = mode.horizontal;
        editor.setHorizontallyScrolling(mode.horizontal);
        editor.setHorizontalScrollBarEnabled(mode.horizontal);
        editor.setVerticalScrollBarEnabled(mode.vertical);
        editor.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
    }

    private void cycleScrollbars() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        settings.scrollbarsChoice = LegacyScrollbars.nextChoice(settings.scrollbarsChoice);
        AndroidSettingsStore.save(this, settings);
        applyEditorScrollbars();
        status("Scrollleisten: " + LegacyScrollbars.label(settings.scrollbarsChoice));
    }

    private void newDocument() {
        document = NoteDocument.newDocument();
        currentUri = null;
        currentRawFile = null;
        currentFtpTarget = null;
        currentDisplayName = "unbenannt.alx";
        editorDirty = false;
        titleDirty = false;
        selectNode(document.ensureRoot(), false);
        status("Neue Datei");
    }

    private void openDocument() {
        startActivityForResult(intentForDialog(LegacyFileDialogModel.openAlx(currentDialogDirectory(), currentDisplayName)), REQ_OPEN);
    }

    private Intent intentForDialog(LegacyFileDialogModel.Spec spec) {
        LegacyFileDialogModel.Spec dialog = spec == null
                ? LegacyFileDialogModel.openAlx(currentDialogDirectory(), currentDisplayName)
                : spec;
        Intent intent = new Intent(dialog.create ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(dialog.androidMimeType);
        if (dialog.androidMimeTypes.length > 0) intent.putExtra(Intent.EXTRA_MIME_TYPES, dialog.androidMimeTypes);
        if (dialog.create && dialog.defaultFileName != null && !dialog.defaultFileName.isEmpty()) intent.putExtra(Intent.EXTRA_TITLE, dialog.defaultFileName);
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION;
        if (dialog.create || dialog.kind == LegacyFileDialogModel.Kind.OPEN_ALX) flags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        intent.addFlags(flags);
        return intent;
    }

    private void saveDocument() {
        saveCurrentEditorToNode();
        LegacySaveWorkflow.SavePlan plan = LegacySaveWorkflow.planForAndroid(
                currentUri != null, currentRawFile != null, currentFtpTarget != null, document != null && document.changed);
        switch (plan.action) {
            case SAVE_FTP:
                saveDocumentToFtp(currentFtpTarget);
                return;
            case SAVE_RAW_FILE:
                writeDocumentToFile(currentRawFile, false);
                return;
            case SAVE_URI:
                writeDocumentToUri(currentUri, currentDisplayName, false);
                return;
            case SAVE_AS:
            default:
                saveDocumentAs();
                return;
        }
    }

    private void saveDocumentAs() {
        saveCurrentEditorToNode();
        startActivityForResult(intentForDialog(LegacyFileDialogModel.saveAlx(currentDialogDirectory(), ensureAlxName(currentDisplayName))), REQ_SAVE_AS);
    }

    private void writeDocumentToUri(Uri uri, String displayName, boolean fromSaveAs) {
        try {
            byte[] previous = null;
            if (!fromSaveAs) {
                try { previous = readAll(uri); } catch (Exception ignored) {}
            }
            byte[] payload = AlxIo.dump(document, document.password);
            writeAll(uri, payload);
            AndroidBackupStore.createBackup(this, displayName, previous, settings == null ? 30 : settings.backupKeep);
            currentUri = uri;
            currentRawFile = null;
            currentFtpTarget = null;
            currentDisplayName = ensureAlxName(displayName);
            document.displayName = currentDisplayName;
            rememberCurrentFile(uri == null ? currentDisplayName : uri.toString());
            document.markSaved();
            editorDirty = false;
            titleDirty = false;
            updateTitle();
            status("Gespeichert: " + currentDisplayName);
        } catch (Exception e) {
            error("Speichern fehlgeschlagen", e.getMessage());
        }
    }


    private void writeDocumentToFile(File file, boolean autosave) {
        try {
            if (file == null) throw new IllegalStateException("Kein Dateipfad gesetzt.");
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            byte[] previous = null;
            try { if (file.isFile()) previous = readFile(file); } catch (Exception ignored) {}
            byte[] payload = AlxIo.dump(document, document.password);
            FileOutputStream out = new FileOutputStream(file, false);
            out.write(payload);
            out.close();
            AndroidBackupStore.createBackup(this, file.getName(), previous, settings == null ? 30 : settings.backupKeep);
            currentUri = null;
            currentRawFile = file;
            currentFtpTarget = null;
            currentDisplayName = ensureAlxName(file.getName());
            document.displayName = currentDisplayName;
            rememberCurrentFile(file.getAbsolutePath());
            document.markSaved();
            editorDirty = false;
            titleDirty = false;
            updateTitle();
            status((autosave ? "Autosave" : "Gespeichert") + ": " + file.getAbsolutePath());
        } catch (Exception e) {
            error(autosave ? "Autosave fehlgeschlagen" : "Speichern fehlgeschlagen", e.getMessage());
        }
    }


    private void showBackups() {
        File[] backups = AndroidBackupStore.listBackups(this, currentDisplayName);
        if (backups.length == 0) {
            new AlertDialog.Builder(this).setTitle("Sicherungen").setMessage("Für diese Datei gibt es noch keine app-internen Sicherungen.").setPositiveButton("OK", null).show();
            return;
        }
        String[] labels = new String[backups.length];
        for (int i = 0; i < backups.length; i++) labels[i] = AndroidBackupStore.describe(backups[i]);
        new AlertDialog.Builder(this)
                .setTitle("Sicherung öffnen")
                .setItems(labels, (d, which) -> confirmDiscardThen(() -> openBackupFile(backups[which], null)))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void openBackupFile(File file, String password) {
        try {
            byte[] data = readFile(file);
            NoteDocument loaded = AlxIo.load(data, password == null ? document.password : password);
            document = loaded;
            currentUri = null;
            currentRawFile = null;
            currentFtpTarget = null;
            currentDisplayName = ensureAlxName(file.getName());
            document.displayName = currentDisplayName;
            document.changed = true;
            editorDirty = false;
            titleDirty = false;
            rebuildTree();
            selectNode(document.ensureRoot(), false);
            updateTitle();
            status("Sicherung geöffnet: bitte mit 'Speichern unter' sichern");
        } catch (AlxException.PasswordRequired e) {
            showPasswordDialog("", p -> openBackupFile(file, p));
        } catch (Exception e) {
            error("Sicherung", e.getMessage());
        }
    }

    private byte[] readFile(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        in.close();
        return out.toByteArray();
    }


    private void rememberCurrentFile(String path) {
        if (settings == null) settings = new LegacySettings();
        settings.rememberFile(path);
        AndroidSettingsStore.save(this, settings);
    }


    private void showRecentFiles() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        if (settings.recentFiles.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Letzte Dateien").setMessage("Keine gespeicherten Einträge.").setPositiveButton("OK", null).show();
            return;
        }
        final ArrayList<String> entries = new ArrayList<>(settings.recentFiles);
        String[] labels = LegacyRecentMenu.labelsFromPaths(entries);
        new AlertDialog.Builder(this)
                .setTitle("Letzte Dateien")
                .setItems(labels, (d, which) -> confirmDiscardThen(() -> openStoredPath(entries.get(which))))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void openStoredPath(String path) {
        if (path == null || path.trim().isEmpty()) return;
        if (settings == null) settings = AndroidSettingsStore.load(this);
        String selected = settings.activateRecentFile(path);
        if (selected == null) selected = path.trim();
        AndroidSettingsStore.save(this, settings);
        LegacyOpenTarget targetInfo = LegacyOpenTarget.parse(selected);
        try {
            if (targetInfo.isFtp()) {
                FtpTarget target = FtpTarget.fromFields(targetInfo.normalized, "", settings.ftpUsername, settings.ftpPassword);
                openDocumentFromFtp(target, null);
            } else if (targetInfo.isAndroidUri()) {
                openUri(Uri.parse(targetInfo.normalized), null);
            } else if (targetInfo.isLocalFile()) {
                openRawFile(new File(targetInfo.normalized), null);
            } else {
                throw new IllegalStateException("Unbekanntes Dateiziel: " + selected);
            }
        } catch (Exception e) {
            error("Letzte Datei", e.getMessage());
        }
    }

    private void openRawFile(File file, String password) {
        try {
            if (file == null) throw new IllegalStateException("Dateipfad fehlt.");
            byte[] data = readFile(file);
            NoteDocument loaded = AlxIo.load(data, password == null ? "" : password);
            document = loaded;
            currentUri = null;
            currentRawFile = file;
            currentFtpTarget = null;
            currentDisplayName = ensureAlxName(file.getName());
            document.displayName = currentDisplayName;
            rememberCurrentFile(file.getAbsolutePath());
            document.markSaved();
            editorDirty = false;
            titleDirty = false;
            rebuildTree();
            selectNode(document.ensureRoot(), false);
            updateTitle();
            status("Geöffnet: " + file.getAbsolutePath());
        } catch (AlxException.PasswordRequired e) {
            showPasswordDialog("", p -> openRawFile(file, p));
        } catch (Exception e) {
            error("Öffnen fehlgeschlagen", e.getMessage());
        }
    }

    private void showSettingsDialog() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        LegacySettingsDialogModel.ViewState state = LegacySettingsDialogModel.fromSettings(settings);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText language = labeledEdit(box, "Sprache", state.language);
        TextView languages = new TextView(this);
        languages.setText("Sprachen: " + LegacyI18n.availableLanguageLabels());
        languages.setTextSize(12f);
        languages.setPadding(0, 0, 0, dp(6));
        box.addView(languages);

        EditText backupKeep = labeledEdit(box, "Sicherungen behalten", state.backupKeepText);
        CheckBox autosaveEnabled = new CheckBox(this);
        autosaveEnabled.setText("Autosave aktiv");
        autosaveEnabled.setChecked(state.autosaveEnabled);
        box.addView(autosaveEnabled);
        EditText autosaveSeconds = labeledEdit(box, "Autosave Sekunden", state.autosaveSecondsText);
        autosaveSeconds.setEnabled(state.autosaveSecondsEnabled);

        CheckBox autorunEnabled = new CheckBox(this);
        autorunEnabled.setText("Autostart aktiv (Legacy-Modell)");
        autorunEnabled.setChecked(state.autorunEnabled);
        box.addView(autorunEnabled);
        CheckBox autorunMinimized = new CheckBox(this);
        autorunMinimized.setText("Autostart minimiert");
        autorunMinimized.setChecked(state.autorunMinimized);
        autorunMinimized.setEnabled(state.autorunMinimizedEnabled);
        box.addView(autorunMinimized);

        CheckBox showTaskbar = new CheckBox(this);
        showTaskbar.setText("Minimiert in Taskbar anzeigen");
        showTaskbar.setChecked(state.showInTaskbarWhenMinimized);
        box.addView(showTaskbar);
        CheckBox showBorders = new CheckBox(this);
        showBorders.setText("Haftnotiz-Ränder anzeigen");
        showBorders.setChecked(state.showDesknoteBorders);
        box.addView(showBorders);

        autosaveEnabled.setOnCheckedChangeListener((button, checked) -> {
            LegacySettingsDialogModel.ViewState next = LegacySettingsDialogModel.onAutosaveChanged(
                    new LegacySettingsDialogModel.ViewState(language.getText().toString(), backupKeep.getText().toString(),
                            button.isChecked(), autosaveSeconds.getText().toString(), autosaveSeconds.isEnabled(),
                            autorunEnabled.isChecked(), autorunMinimized.isChecked(), autorunMinimized.isEnabled(),
                            showTaskbar.isChecked(), showBorders.isChecked()), checked);
            autosaveSeconds.setText(next.autosaveSecondsText);
            autosaveSeconds.setEnabled(next.autosaveSecondsEnabled);
        });
        autorunEnabled.setOnCheckedChangeListener((button, checked) -> {
            LegacySettingsDialogModel.ViewState next = LegacySettingsDialogModel.onAutorunChanged(
                    new LegacySettingsDialogModel.ViewState(language.getText().toString(), backupKeep.getText().toString(),
                            autosaveEnabled.isChecked(), autosaveSeconds.getText().toString(), autosaveSeconds.isEnabled(),
                            checked, autorunMinimized.isChecked(), autorunMinimized.isEnabled(),
                            showTaskbar.isChecked(), showBorders.isChecked()), checked);
            autorunMinimized.setChecked(next.autorunMinimized);
            autorunMinimized.setEnabled(next.autorunMinimizedEnabled);
        });

        Button exportConfig = new Button(this);
        exportConfig.setAllCaps(false);
        exportConfig.setText("Config als XML exportieren");
        exportConfig.setOnClickListener(v -> exportSettingsFile());
        box.addView(exportConfig);
        TextView recent = new TextView(this);
        recent.setText("Letzte Dateien:\n" + (settings.recentFiles.isEmpty() ? "-" : settings.recentFiles.toString()));
        recent.setPadding(0, dp(8), 0, 0);
        box.addView(recent);
        new AlertDialog.Builder(this)
                .setTitle("Einstellungen")
                .setView(box)
                .setNeutralButton("Config importieren", (d, which) -> importSettingsFile())
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", (d, which) -> {
                    LegacySettingsDialogModel.ApplyResult result = LegacySettingsDialogModel.apply(
                            settings,
                            language.getText().toString(),
                            backupKeep.getText().toString(),
                            autosaveEnabled.isChecked(),
                            autosaveSeconds.getText().toString(),
                            autorunEnabled.isChecked(),
                            autorunMinimized.isChecked(),
                            showTaskbar.isChecked(),
                            showBorders.isChecked());
                    settings = result.settings;
                    AndroidSettingsStore.save(this, settings);
                    scheduleAutosaveTick();
                    status(result.warnings.isEmpty() ? "Einstellungen gespeichert" : "Einstellungen gespeichert\n" + joinLines(result.warnings));
                })
                .show();
    }

    private void importSettingsFile() {
        startActivityForResult(intentForDialog(LegacyFileDialogModel.importConfig(currentDialogDirectory())), REQ_IMPORT_CONFIG);
    }

    private void exportSettingsFile() {
        try {
            if (settings == null) settings = AndroidSettingsStore.load(this);
            pendingExportConfig = settings.toXmlBytes();
            startActivityForResult(intentForDialog(LegacyFileDialogModel.exportConfig(currentDialogDirectory())), REQ_EXPORT_CONFIG);
        } catch (Exception e) {
            error("Config-Export", e.getMessage());
        }
    }

    private void applyImportedSettings(byte[] data) throws Exception {
        LegacySettings imported = LegacySettings.fromXmlBytes(data);
        settings = imported;
        AndroidSettingsStore.save(this, settings);
        scheduleAutosaveTick();
        status("Legacy-Einstellungen importiert");
        new AlertDialog.Builder(this)
                .setTitle("Einstellungen importiert")
                .setMessage(settings.summary())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showFtpDialog(boolean upload) {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText host = labeledEdit(box, "Host oder ftp://user:pass@host/pfad.alx", settings.ftpHost);
        EditText path = labeledEdit(box, "Pfad zur .alx", settings.ftpPath);
        EditText user = labeledEdit(box, "Benutzer", settings.ftpUsername);
        EditText pass = labeledEdit(box, "Passwort", settings.ftpPassword);
        TextView hint = new TextView(this);
        hint.setText(upload ? "Speichert die aktuelle ALX-Datei per passivem FTP." : "Lädt eine ALX-Datei per passivem FTP.");
        hint.setTextSize(12f);
        box.addView(hint);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(upload ? "FTP speichern" : "FTP öffnen")
                .setView(box)
                .setNeutralButton("Nur Daten speichern", null)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton(upload ? "Speichern" : "Öffnen", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                LegacyFtpDialogModel.FtpDialogDecision decision = LegacyFtpDialogModel.decide(
                        LegacyFtpDialogModel.Action.STORE,
                        host.getText().toString(), path.getText().toString(), user.getText().toString(), pass.getText().toString());
                if (!decision.accepted) { error("FTP", decision.message); return; }
                LegacyFtpDialogModel.applyToSettings(settings, decision);
                AndroidSettingsStore.save(this, settings);
                status("FTP-Daten gespeichert: " + decision.safeLegacyUrl);
                dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                LegacyFtpDialogModel.FtpDialogDecision decision = LegacyFtpDialogModel.decide(
                        upload ? LegacyFtpDialogModel.Action.UPLOAD : LegacyFtpDialogModel.Action.OPEN,
                        host.getText().toString(), path.getText().toString(), user.getText().toString(), pass.getText().toString());
                if (!decision.accepted || decision.target == null) { error("FTP", decision.message); return; }
                LegacyFtpDialogModel.applyToSettings(settings, decision);
                AndroidSettingsStore.save(this, settings);
                dialog.dismiss();
                if (upload) saveDocumentToFtp(decision.target);
                else confirmDiscardThen(() -> openDocumentFromFtp(decision.target, null));
            });
        });
        dialog.show();
    }

    private void saveDocumentToFtp(FtpTarget target) {
        if (target == null) return;
        saveCurrentEditorToNode();
        final byte[] payload;
        try {
            payload = AlxIo.dump(document, document.password);
        } catch (Exception e) {
            error("FTP speichern", e.getMessage());
            return;
        }
        status("FTP-Upload läuft: " + target.safeDisplayUrl());
        runBackground("FTP speichern", () -> {
            SimpleFtpClient.upload(target, payload);
            return target;
        }, result -> {
            currentUri = null;
            currentRawFile = null;
            currentFtpTarget = result;
            currentDisplayName = ensureAlxName(result.filename());
            document.displayName = currentDisplayName;
            document.markSaved();
            rememberCurrentFile(result.safeDisplayUrl());
            editorDirty = false;
            titleDirty = false;
            updateTitle();
            status("FTP gespeichert: " + result.safeDisplayUrl());
        });
    }

    private void openDocumentFromFtp(FtpTarget target, String password) {
        status("FTP-Download läuft: " + target.safeDisplayUrl());
        runBackground("FTP öffnen", () -> SimpleFtpClient.download(target), data -> openFtpBytes(target, data, password));
    }

    private void openFtpBytes(FtpTarget target, byte[] data, String password) {
        try {
            NoteDocument loaded = AlxIo.load(data, password == null ? "" : password);
            document = loaded;
            currentUri = null;
            currentRawFile = null;
            currentFtpTarget = target;
            currentDisplayName = ensureAlxName(target.filename());
            document.displayName = currentDisplayName;
            document.markSaved();
            rememberCurrentFile(target.safeDisplayUrl());
            editorDirty = false;
            titleDirty = false;
            rebuildTree();
            selectNode(document.ensureRoot(), false);
            updateTitle();
            status("FTP geöffnet: " + target.safeDisplayUrl());
        } catch (AlxException.PasswordRequired e) {
            final byte[] copy = data;
            showPasswordDialog("", p -> openFtpBytes(target, copy, p));
        } catch (Exception e) {
            error("FTP öffnen", e.getMessage());
        }
    }

    private <T> void runBackground(String title, BackgroundCallback<T> work, UiCallback<T> done) {
        if (ioExecutor == null) ioExecutor = Executors.newSingleThreadExecutor();
        if (mainHandler == null) mainHandler = new Handler(Looper.getMainLooper());
        ioExecutor.execute(() -> {
            try {
                T result = work.run();
                mainHandler.post(() -> done.run(result));
            } catch (Exception e) {
                mainHandler.post(() -> error(title, e.getMessage()));
            }
        });
    }


    private void scheduleAutosaveTick() {
        if (mainHandler == null) mainHandler = new Handler(Looper.getMainLooper());
        if (autosaveRunnable != null) mainHandler.removeCallbacks(autosaveRunnable);
        long delay = LegacyAutosave.normalizedDelayMillis(settings == null ? 0 : settings.autosaveSeconds);
        if (delay <= 0L) return;
        autosaveRunnable = () -> {
            performAutosaveTick();
            scheduleAutosaveTick();
        };
        mainHandler.postDelayed(autosaveRunnable, delay);
    }

    private void performAutosaveTick() {
        if (autosaveInFlight || document == null) return;
        saveCurrentEditorToNode();
        LegacyAutosave.TargetKind targetKind = LegacyAutosave.androidTargetKind(currentUri != null, currentRawFile != null, currentFtpTarget != null);
        LegacyAutosave.Decision decision = LegacyAutosave.decide(document.root != null, document.changed, settings == null ? 0 : settings.autosaveSeconds, targetKind);
        if (!decision.shouldSave) return;
        autosaveInFlight = true;
        if (currentFtpTarget != null) {
            autosaveFtp(currentFtpTarget);
        } else if (currentUri != null) {
            writeDocumentToUri(currentUri, currentDisplayName, false);
            autosaveInFlight = false;
        } else if (currentRawFile != null) {
            writeDocumentToFile(currentRawFile, true);
            autosaveInFlight = false;
        } else {
            autosaveInFlight = false;
        }
    }

    private void autosaveFtp(FtpTarget target) {
        final byte[] payload;
        try {
            payload = AlxIo.dump(document, document.password);
        } catch (Exception e) {
            autosaveInFlight = false;
            error("Autosave FTP", e.getMessage());
            return;
        }
        if (ioExecutor == null) ioExecutor = Executors.newSingleThreadExecutor();
        if (mainHandler == null) mainHandler = new Handler(Looper.getMainLooper());
        ioExecutor.execute(() -> {
            try {
                SimpleFtpClient.upload(target, payload);
                mainHandler.post(() -> {
                    currentUri = null;
                    currentRawFile = null;
                    currentFtpTarget = target;
                    currentDisplayName = ensureAlxName(target.filename());
                    document.displayName = currentDisplayName;
                    document.markSaved();
                    rememberCurrentFile(target.safeDisplayUrl());
                    editorDirty = false;
                    titleDirty = false;
                    autosaveInFlight = false;
                    updateTitle();
                    status("Autosave FTP: " + target.safeDisplayUrl());
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    autosaveInFlight = false;
                    error("Autosave FTP", e.getMessage());
                });
            }
        });
    }

    private void consumeOpenOnceFileIfPresent() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        if (settings.openOnceFile == null || settings.openOnceFile.trim().isEmpty()) return;
        String once = settings.openOnceFile.trim();
        settings.openOnceFile = "";
        settings.openOnceTimestamp = "";
        AndroidSettingsStore.save(this, settings);
        status("Einmaliges Startziel: " + once);
        openStoredPath(once);
    }

    private void newChild() {
        if (document == null) document = NoteDocument.newDocument();
        if (currentNode == null) currentNode = document.ensureRoot();
        saveCurrentEditorToNode();
        LegacyTreeCreation.CreationResult result = LegacyTreeCreation.newChild(document, currentNode, "...");
        rebuildTree();
        selectNode(result.node, true);
        status(result.status);
    }

    private void newNext() {
        if (document == null) document = NoteDocument.newDocument();
        if (currentNode == null) currentNode = document.ensureRoot();
        saveCurrentEditorToNode();
        LegacyTreeCreation.CreationResult result = LegacyTreeCreation.newNext(document, currentNode, "...");
        rebuildTree();
        selectNode(result.node, true);
        status(result.status);
    }

    private void toggleExpanded() {
        if (currentNode == null) return;
        currentNode.expanded = !currentNode.expanded;
        document.markChanged();
        rebuildTree();
        status(currentNode.expanded ? "Knoten geöffnet" : "Knoten geschlossen");
    }

    private void expandAllNodes() {
        if (document == null || document.root == null) return;
        int changed = LegacyTreeExpansion.expandAll(document.ensureRoot());
        if (changed > 0) document.markChanged();
        rebuildTree();
        if (currentNode != null) selectNode(currentNode, true);
        status("Alle Knoten geöffnet");
    }

    private void collapseAllNodes() {
        if (document == null || document.root == null) return;
        int changed = LegacyTreeExpansion.collapseAll(document.ensureRoot());
        if (changed > 0) document.markChanged();
        rebuildTree();
        selectNode(document.ensureRoot(), true);
        status("Alle Knoten geschlossen");
    }

    private void clearDesktopNotesInSubtree() {
        if (currentNode == null) return;
        LegacyDesktopNoteTreeOps.ClearResult result = LegacyDesktopNoteTreeOps.clearDesktopNotes(currentNode);
        if (result.clearedDesktopNotes > 0) {
            document.markChanged();
            rebuildTree();
            selectNode(currentNode, true);
        }
        status(LegacyDesktopNoteTreeOps.clearStatusText(result));
    }

    private void deleteCurrent() {
        if (currentNode == null) return;
        LegacyTreeDelete.DeletePlan plan = LegacyTreeDelete.plan(currentNode);
        new AlertDialog.Builder(this)
                .setTitle("Löschen")
                .setMessage(LegacyTreeDelete.confirmationText(plan))
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Löschen", (d, which) -> {
                    if (currentNode.parent == null) {
                        newDocument();
                        return;
                    }
                    LegacyTreeDelete.DeletePlan p = LegacyTreeDelete.plan(currentNode);
                    NoteNode fallback = LegacyTreeDelete.delete(currentNode);
                    document.markChanged();
                    rebuildTree();
                    selectNode(fallback == null ? document.ensureRoot() : fallback, true);
                    status("Knoten gelöscht" + (p.desktopNotesToClose > 0 ? " (Haftnotizen geschlossen: " + p.desktopNotesToClose + ")" : ""));
                }).show();
    }



    private void copyCurrentNode(boolean includeDesktopNote) {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        try {
            internalClipboardNode = currentNode.cloneDeep(includeDesktopNote);
            String xml = NodeClipboard.nodeToClipboardXml(currentNode, includeDesktopNote);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText(NodeClipboard.NODE_MIME_TYPE, xml));
            status("Knoten kopiert: " + safeTitle(currentNode));
        } catch (Exception e) {
            error("Kopieren fehlgeschlagen", e.getMessage());
        }
    }

    private void cutCurrentNode() {
        if (currentNode == null) return;
        if (currentNode.parent == null) {
            error("Ausschneiden", "Die Wurzel kann nicht ausgeschnitten werden.");
            return;
        }
        copyCurrentNode(false);
        LegacyTreeDelete.DeletePlan p = LegacyTreeDelete.plan(currentNode);
        NoteNode fallback = LegacyTreeDelete.delete(currentNode);
        document.markChanged();
        rebuildTree();
        selectNode(fallback == null ? document.ensureRoot() : fallback, true);
        status("Knoten ausgeschnitten" + (p.desktopNotesToClose > 0 ? " (Haftnotizen geschlossen: " + p.desktopNotesToClose + ")" : ""));
    }

    private void pasteNode() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        NoteNode source = clipboardNodeFromSystem();
        if (source == null && internalClipboardNode != null) source = internalClipboardNode.cloneDeep(false);
        if (source == null) {
            error("Einfügen", "Die Zwischenablage enthält keinen Notizen-Knoten.");
            return;
        }
        NoteNode pasted = NoteTreeOps.legacyPasteClone(source, currentNode);
        ensureAncestorsExpanded(pasted);
        document.markChanged();
        rebuildTree();
        selectNode(pasted, true);
        status("Knoten eingefügt");
    }

    private NoteNode clipboardNodeFromSystem() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null || !clipboard.hasPrimaryClip()) return null;
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return null;
            CharSequence text = clip.getItemAt(0).coerceToText(this);
            if (text == null || !NodeClipboard.looksLikeNodeClipboardXml(text.toString())) return null;
            return NodeClipboard.nodeFromClipboardXml(text.toString(), false);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void moveCurrentUp() {
        if (currentNode == null || currentNode.parent == null) return;
        List<NoteNode> siblings = currentNode.parent.children;
        int index = siblings.indexOf(currentNode);
        if (index <= 0) {
            status("Knoten ist schon oben");
            return;
        }
        siblings.remove(index);
        siblings.add(index - 1, currentNode);
        document.markChanged();
        rebuildTree();
        selectNode(currentNode, true);
        status("Knoten nach oben verschoben");
    }

    private void moveCurrentDown() {
        if (currentNode == null || currentNode.parent == null) return;
        List<NoteNode> siblings = currentNode.parent.children;
        int index = siblings.indexOf(currentNode);
        if (index < 0 || index >= siblings.size() - 1) {
            status("Knoten ist schon unten");
            return;
        }
        siblings.remove(index);
        siblings.add(index + 1, currentNode);
        document.markChanged();
        rebuildTree();
        selectNode(currentNode, true);
        status("Knoten nach unten verschoben");
    }

    private void indentCurrent() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        if (NoteTreeOps.indentUnderPreviousSibling(currentNode) == null) {
            status("Einrücken hier nicht möglich");
            return;
        }
        document.markChanged();
        rebuildTree();
        selectNode(currentNode, true);
        status("Knoten eingerückt");
    }

    private void outdentCurrent() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        if (NoteTreeOps.outdentAfterParent(currentNode) == null) {
            status("Ausrücken hier nicht möglich");
            return;
        }
        document.markChanged();
        rebuildTree();
        selectNode(currentNode, true);
        status("Knoten ausgerückt");
    }


    private void showMoveBeforeTargetDialog() {
        if (currentNode == null || currentNode.parent == null) {
            status("Wurzel kann nicht verschoben werden");
            return;
        }
        saveCurrentEditorToNode();
        final List<NoteNode> visible = NoteTreeOps.legacyVisibleWalk(document.ensureRoot());
        final ArrayList<NoteNode> targets = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        for (NoteNode n : visible) {
            if (!LegacyTreeDragDrop.decision(currentNode, n, document.ensureRoot()).allowed) continue;
            targets.add(n);
            labels.add(indentLabel(n));
        }
        if (targets.isEmpty()) {
            status("Kein gültiges Ziel zum Verschieben");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Knoten vor Ziel verschieben")
                .setItems(labels.toArray(new String[0]), (d, which) -> {
                    NoteNode target = targets.get(which);
                    if (LegacyTreeDragDrop.moveBefore(currentNode, target, document.ensureRoot()) == null) {
                        status("Dieses Ziel ist nicht gültig");
                        return;
                    }
                    ensureAncestorsExpanded(currentNode);
                    document.markChanged();
                    rebuildTree();
                    selectNode(currentNode, true);
                    status("Knoten vor Ziel verschoben");
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private String indentLabel(NoteNode node) {
        int depth = Math.max(0, NoteTreeOps.depth(node));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < depth; i++) b.append("  ");
        b.append(depth == 0 ? "▣ " : "↳ ");
        b.append(safeTitle(node));
        return b.toString();
    }

    private void insertDate() {
        String value = LegacyEditorActions.androidDateInsertText(new Date());
        int start = Math.max(0, editor.getSelectionStart());
        int end = Math.max(start, editor.getSelectionEnd());
        editor.getText().replace(start, end, value);
        status("Legacy-Datum eingefügt");
    }

    private void insertLegacyBullet() {
        if (editor == null) return;
        int start = Math.max(0, editor.getSelectionStart());
        int end = Math.max(start, editor.getSelectionEnd());
        editor.getText().replace(start, end, LegacyEditorActions.androidBulletInsertText());
        status("Legacy-Aufzählungspunkt eingefügt");
    }


    private void showRtfFormatDialog() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        List<LegacyRichTextToolbar.ActionSpec> all = LegacyRichTextToolbar.fontActions();
        ArrayList<LegacyRichTextToolbar.ActionSpec> actions = new ArrayList<>();
        for (LegacyRichTextToolbar.ActionSpec spec : all) {
            actions.add(spec);
        }
        String[] labels = new String[actions.size()];
        for (int i = 0; i < actions.size(); i++) labels[i] = LegacyRichTextToolbar.iconOnlyDescription(actions.get(i));
        new AlertDialog.Builder(this)
                .setTitle("RTF-Formatierung")
                .setItems(labels, (d, which) -> applyRtfFormatAction(actions.get(which)))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void applyRtfFormatAction(LegacyRichTextToolbar.ActionSpec spec) {
        if (spec == null || currentNode == null) return;
        if ("legacy_bullet".equals(spec.action)) {
            insertLegacyBullet();
            return;
        }
        if ("cycle_scrollbars".equals(spec.action)) {
            cycleScrollbars();
            return;
        }
        if ("text_color".equals(spec.action)) {
            showRtfColorPalette(LegacyColorDialogModel.Role.RTF_TEXT);
            return;
        }
        if ("highlight_color".equals(spec.action)) {
            showRtfColorPalette(LegacyColorDialogModel.Role.RTF_HIGHLIGHT);
            return;
        }
        if ("font_family".equals(spec.action)) {
            showFontFamilyDialog();
            return;
        }
        if ("font_size".equals(spec.action)) {
            showFontSizeDialog();
            return;
        }
        String plain = editor.getText().toString();
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        currentNode.rtf = LegacyRtfSelectionFormatter.applyToSelection(plain, start, end, spec.action);
        editorDirty = false;
        document.markChanged();
        updateTitle();
        status("RTF-Format gespeichert: " + spec.tooltip + " — Vorschau zeigt das Ergebnis");
    }

    private void showRtfColorPalette(LegacyColorDialogModel.Role role) {
        if (currentNode == null || editor == null) return;
        String title = role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT ? "RTF-Hintergrundfarbe" : "RTF-Textfarbe";
        String[] labels = LegacyColorDialogModel.paletteLabels();
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, (d, which) -> applyRtfColorToSelection(LegacyColorDialogModel.choosePalette(role, which)))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void applyRtfColorToSelection(LegacyColorDialogModel.Decision decision) {
        if (currentNode == null || editor == null || decision == null || !decision.valid) return;
        String plain = editor.getText().toString();
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        currentNode.rtf = LegacyRtfSelectionFormatter.applyColorToSelection(plain, start, end, decision);
        editorDirty = false;
        document.markChanged();
        updateTitle();
        status((decision.role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT ? "RTF-Hervorhebung" : "RTF-Textfarbe") + " gespeichert: " + decision.css);
    }

    private void showFontFamilyDialog() {
        if (currentNode == null || editor == null) return;
        List<String> families = LegacyRichTextToolbar.legacyFontFamilies();
        String[] labels = families.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Schriftart")
                .setItems(labels, (d, which) -> {
                    String family = families.get(which);
                    String plain = editor.getText().toString();
                    int start = editor.getSelectionStart();
                    int end = editor.getSelectionEnd();
                    currentNode.rtf = LegacyRtfSelectionFormatter.applyFontFamilyToSelection(plain, start, end, family);
                    editorDirty = false;
                    document.markChanged();
                    updateTitle();
                    status("Schriftart gespeichert: " + family + " — Vorschau zeigt das Ergebnis");
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showFontSizeDialog() {
        if (currentNode == null || editor == null) return;
        int[] sizes = LegacyRichTextToolbar.legacyFontSizeItems();
        String[] labels = new String[sizes.length + 1];
        for (int i = 0; i < sizes.length; i++) labels[i] = sizes[i] + " pt";
        labels[sizes.length] = "Eigene Größe…";
        new AlertDialog.Builder(this)
                .setTitle("Schriftgröße")
                .setItems(labels, (d, which) -> {
                    if (which >= sizes.length) {
                        showCustomFontSizeDialog();
                    } else {
                        applyFontSizeToSelection(sizes[which]);
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showCustomFontSizeDialog() {
        if (currentNode == null || editor == null) return;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText value = labeledEdit(box, "Schriftgröße", LegacyFontSizeEntry.DEFAULT_TEXT);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Eigene Schriftgröße")
                .setView(box)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            LegacyFontSizeEntry.State state = LegacyFontSizeEntry.onEnter(value.getText().toString(), LegacyFontSizeEntry.DEFAULT_TEXT);
            value.setText(state.text);
            value.setSelection(value.getText().length());
            if (!state.apply) {
                error("Schriftgröße", "Bitte eine Zahl zwischen 0 und 99 eingeben.");
                return;
            }
            dialog.dismiss();
            applyFontSizeToSelection(state.numericValue);
        }));
        dialog.show();
    }

    private void applyFontSizeToSelection(int size) {
        if (currentNode == null || editor == null) return;
        String plain = editor.getText().toString();
        int start = editor.getSelectionStart();
        int end = editor.getSelectionEnd();
        currentNode.rtf = LegacyRtfSelectionFormatter.applyFontSizeToSelection(plain, start, end, size);
        editorDirty = false;
        document.markChanged();
        updateTitle();
        status("Schriftgröße gespeichert: " + size + " pt — Vorschau zeigt das Ergebnis");
    }

    private void insertImage() {
        if (currentNode == null) return;
        startActivityForResult(intentForDialog(LegacyFileDialogModel.insertImage(currentDialogDirectory())), REQ_INSERT_IMAGE);
    }

    private void insertImageFromUri(Uri uri) {
        if (currentNode == null || uri == null) return;
        try {
            saveCurrentEditorToNode();
            String before = currentNode.rtf == null ? "" : currentNode.rtf;
            String after = RtfUtils.appendImageToRtf(before, readAll(uri), getContentResolver().getType(uri));
            if (after.equals(before)) {
                error("Bild einfügen", "Dieses Bildformat kann nicht als ALX/RTF-Bild gespeichert werden. Unterstützt sind PNG, JPEG und BMP.");
                return;
            }
            currentNode.rtf = after;
            document.markChanged();
            editorDirty = false;
            selectNode(currentNode, false);
            updateTitle();
            status("Bild eingefügt");
        } catch (Exception e) {
            error("Bild einfügen", e.getMessage());
        }
    }


    private void showPrintDialog() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        String[] labels = new String[]{
                LegacyPrintLayout.labelForScope(LegacyPrintLayout.Scope.CURRENT_NOTE),
                LegacyPrintLayout.labelForScope(LegacyPrintLayout.Scope.CURRENT_SUBTREE),
                LegacyPrintLayout.labelForScope(LegacyPrintLayout.Scope.ROOT)
        };
        new AlertDialog.Builder(this)
                .setTitle("Drucken")
                .setItems(labels, (dialog, which) -> {
                    LegacyPrintLayout.PrintJob job;
                    if (which == 1) job = LegacyPrintLayout.subtree(currentNode);
                    else if (which == 2) job = LegacyPrintLayout.root(document);
                    else job = LegacyPrintLayout.currentNote(currentNode);
                    startPrintJob(job);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void startPrintJob(LegacyPrintLayout.PrintJob job) {
        if (job == null) return;
        if (Build.VERSION.SDK_INT < 19) {
            error("Drucken", "Android-Druck wird erst ab Android 4.4 unterstützt.");
            return;
        }
        try {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            if (printManager == null) {
                error("Drucken", "Kein Android-Druckdienst verfügbar.");
                return;
            }
            final String docName = LegacyPrintLayout.safeFileName(job.title, ".pdf").replaceAll("\\.pdf$", "");
            final String html = job.html == null || job.html.isEmpty() ? RtfUtils.rtfToHtmlDocument("") : job.html;
            pendingPrintView = new WebView(this);
            pendingPrintView.setWebViewClient(new WebViewClient() {
                @Override public void onPageFinished(WebView view, String url) {
                    try {
                        PrintDocumentAdapter adapter = Build.VERSION.SDK_INT >= 21
                                ? view.createPrintDocumentAdapter(docName)
                                : view.createPrintDocumentAdapter();
                        PrintAttributes attrs = new PrintAttributes.Builder().build();
                        printManager.print(docName, adapter, attrs);
                        status("Druckauftrag erstellt: " + docName);
                    } catch (Exception e) {
                        error("Drucken", e.getMessage());
                    }
                }
            });
            pendingPrintView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            error("Drucken", e.getMessage());
        }
    }

    private void showRichPreview() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        WebView web = new WebView(this);
        String html = LegacyRichTextBoxSemantics.decorateHtmlDocument(RtfUtils.rtfToHtml(currentNode.rtf == null ? "" : currentNode.rtf), false);
        web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        new AlertDialog.Builder(this)
                .setTitle("Vorschau: " + safeTitle(currentNode))
                .setView(web)
                .setPositiveButton("OK", null)
                .show();
    }


    private void showRtfInfoDialog() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        LegacyRichTextBoxSemantics.Metrics metrics = LegacyRichTextBoxSemantics.inspectRtf(currentNode.rtf == null ? "" : currentNode.rtf);
        String message = metrics.summaryGerman()
                + "\n\nLegacy-RichTextBox: "
                + (LegacyRichTextBoxSemantics.looksLikeLegacyRichTextBoxRtf(currentNode.rtf) ? "erkannt" : "nicht eindeutig")
                + "\nStandardfont: " + LegacyRichTextBoxSemantics.DEFAULT_FONT
                + "\nHTML-Brücke: WebView-kompatibel";
        new AlertDialog.Builder(this)
                .setTitle("RTF Info: " + safeTitle(currentNode))
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }


    private void importTextIntoCurrent() {
        if (currentNode == null) return;
        startActivityForResult(intentForDialog(LegacyFileDialogModel.importText(currentDialogDirectory())), REQ_IMPORT_TEXT);
    }

    private void importRtfIntoCurrent() {
        if (currentNode == null) return;
        startActivityForResult(intentForDialog(LegacyFileDialogModel.importRtf(currentDialogDirectory())), REQ_IMPORT_RTF);
    }

    private void importTextFromUri(Uri uri) {
        if (currentNode == null || uri == null) return;
        try {
            replaceCurrentNoteRtf(LegacyImporters.txtBytesToRtf(readAll(uri)), "TXT importiert: " + queryDisplayName(uri));
        } catch (Exception e) {
            error("TXT-Import", e.getMessage());
        }
    }

    private void importRtfFromUri(Uri uri) {
        if (currentNode == null || uri == null) return;
        try {
            replaceCurrentNoteRtf(LegacyImporters.rtfBytesToRtf(readAll(uri)), "RTF importiert: " + queryDisplayName(uri));
        } catch (Exception e) {
            error("RTF-Import", e.getMessage());
        }
    }

    private void replaceCurrentNoteRtf(String rtf, String message) {
        if (currentNode == null) return;
        editorDirty = false;
        currentNode.rtf = rtf == null ? "" : rtf;
        document.markChanged();
        selectNode(currentNode, true);
        updateTitle();
        if (message != null && !message.isEmpty()) status(message);
    }

    private void importHtmlNote() {
        if (currentNode == null) return;
        startActivityForResult(intentForDialog(LegacyFileDialogModel.importHtml(currentDialogDirectory())), REQ_IMPORT_HTML);
    }

    private void importHtmlNoteFromUri(Uri uri) {
        if (currentNode == null || uri == null) return;
        try {
            saveCurrentEditorToNode();
            String html = decodeTextBytes(readAll(uri));
            String title = baseNameWithoutExtension(queryDisplayName(uri));
            if (title.trim().isEmpty()) title = "HTML Import";
            NoteNode imported = new NoteNode(title, RtfUtils.htmlToRtf(html));
            currentNode.addChild(imported);
            currentNode.expanded = true;
            document.markChanged();
            rebuildTree();
            selectNode(imported, true);
            updateTitle();
            status("HTML importiert: " + safeTitle(imported));
        } catch (Exception e) {
            error("HTML-Import", e.getMessage());
        }
    }

    private String decodeTextBytes(byte[] data) {
        return LegacyImporters.decodeTextBytes(data, false);
    }

    private void showColorDialog() {
        if (currentNode == null) return;
        String[] actions = {"Hintergrundfarbe wählen", "Textfarbe wählen", "Hintergrund löschen", "Textfarbe löschen"};
        new AlertDialog.Builder(this)
                .setTitle("Knotenfarben")
                .setItems(actions, (d, which) -> {
                    if (which == 0) showPalette(true);
                    else if (which == 1) showPalette(false);
                    else if (which == 2) applyNodeColor(true, 0);
                    else applyNodeColor(false, 0);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showPalette(boolean background) {
        String[] labels = new String[LegacyColors.LIGHT_COLOR_ARGB.length];
        for (int i = 0; i < labels.length; i++) labels[i] = i + ": " + LegacyColors.LIGHT_COLOR_NAMES[i] + " " + LegacyColors.toCssRgb(LegacyColors.LIGHT_COLOR_ARGB[i]);
        new AlertDialog.Builder(this)
                .setTitle(background ? "Hintergrund" : "Textfarbe")
                .setItems(labels, (d, which) -> applyNodeColor(background, LegacyColors.LIGHT_COLOR_ARGB[which]))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void applyNodeColor(boolean background, int argb) {
        if (currentNode == null) return;
        if (background) currentNode.bgArgb = argb;
        else currentNode.fgArgb = argb;
        document.markChanged();
        rebuildTree();
        updateTitle();
        status(background ? "Hintergrund gesetzt" : "Textfarbe gesetzt");
    }

    private void showDesktopNoteDialog() {
        if (currentNode == null) return;
        DesktopNoteState existing = currentNode.desktopNote == null ? new DesktopNoteState() : currentNode.desktopNote.copy();
        if (existing.argb == null) existing.argb = LegacyColors.legacyLightColorArgb(null);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        CheckBox visible = new CheckBox(this);
        visible.setText("sichtbar im Legacy-Desktop");
        visible.setChecked(existing.visible);
        box.addView(visible);
        EditText x = labeledEdit(box, "X", Integer.toString(existing.x));
        EditText y = labeledEdit(box, "Y", Integer.toString(existing.y));
        EditText width = labeledEdit(box, "Breite", Integer.toString(existing.width));
        EditText height = labeledEdit(box, "Höhe", Integer.toString(existing.height));
        EditText opacity = labeledEdit(box, "Deckkraft 0.0-1.0", Double.toString(existing.opacity));
        EditText argb = labeledEdit(box, "ARGB-Farbe", Integer.toString(existing.argb));
        TextView contextInfo = new TextView(this);
        contextInfo.setText(desktopNoteContextSummary());
        contextInfo.setTextSize(12f);
        contextInfo.setPadding(0, dp(8), 0, 0);
        box.addView(contextInfo);
        new AlertDialog.Builder(this)
                .setTitle("Haftnotiz-Metadaten")
                .setView(box)
                .setNeutralButton("Entfernen", (d, which) -> {
                    currentNode.desktopNote = null;
                    document.markChanged();
                    updateTitle();
                    status("Haftnotiz-Metadaten entfernt");
                })
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", (d, which) -> {
                    DesktopNoteState state = new DesktopNoteState();
                    state.visible = visible.isChecked();
                    state.x = parseInt(x.getText().toString(), 80);
                    state.y = parseInt(y.getText().toString(), 80);
                    state.width = Math.max(80, parseInt(width.getText().toString(), 260));
                    state.height = Math.max(60, parseInt(height.getText().toString(), 220));
                    state.opacity = Math.max(0.05d, Math.min(1.0d, parseDouble(opacity.getText().toString(), 0.85d)));
                    state.argb = parseInt(argb.getText().toString(), LegacyColors.legacyLightColorArgb(null));
                    state.legacySparse = false;
                    currentNode.desktopNote = state;
                    document.markChanged();
                    updateTitle();
                    status("Haftnotiz-Metadaten gesetzt");
                })
                .show();
    }

    private String desktopNoteContextSummary() {
        String language = settings == null ? "Deutsch" : settings.language;
        StringBuilder b = new StringBuilder("Legacy-Haftnotiz-Kontext: ");
        List<LegacyDesktopNoteContextMenu.MenuItemSpec> items = LegacyDesktopNoteContextMenu.menuItems(language);
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) b.append(" · ");
            b.append(items.get(i).label);
        }
        b.append("\nTransparenz-Menü: ");
        List<LegacyDesktopNoteContextMenu.OpacityOption> opts = LegacyDesktopNoteContextMenu.opacityOptions();
        for (int i = 0; i < opts.size(); i++) {
            if (i > 0) b.append(", ");
            b.append(opts.get(i).label.trim());
        }
        return b.toString();
    }

    private void showAlarmDialog() {
        if (currentNode == null) return;
        AlarmSpec existing = AlarmUtils.fromNode(currentNode);
        if (existing == null) existing = new AlarmSpec(System.currentTimeMillis() + 60L * 60L * 1000L);
        LegacyAlarmDialogModel.ViewState state = LegacyAlarmDialogModel.fromSpec(existing, System.currentTimeMillis(), java.util.TimeZone.getDefault());
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        CheckBox enabled = new CheckBox(this);
        enabled.setText("aktiv");
        enabled.setChecked(state.enabled);
        box.addView(enabled);
        EditText when = labeledEdit(box, "Zeitpunkt " + LegacyAlarmDialogModel.DATE_PATTERN, state.dateText);
        EditText message = labeledEdit(box, "Meldung", state.message);

        TextView recurrenceLabel = new TextView(this);
        recurrenceLabel.setText("Wiederholung");
        recurrenceLabel.setTextSize(12f);
        box.addView(recurrenceLabel);
        RadioGroup recurrenceGroup = new RadioGroup(this);
        recurrenceGroup.setOrientation(RadioGroup.VERTICAL);
        addAlarmRadio(recurrenceGroup, "Einmalig", AlarmSpec.RECURRENCE_NONE, state.recurrence);
        addAlarmRadio(recurrenceGroup, "Täglich", AlarmSpec.RECURRENCE_DAILY, state.recurrence);
        addAlarmRadio(recurrenceGroup, "Wöchentlich", AlarmSpec.RECURRENCE_WEEKLY, state.recurrence);
        addAlarmRadio(recurrenceGroup, "Monatlich", AlarmSpec.RECURRENCE_MONTHLY, state.recurrence);
        addAlarmRadio(recurrenceGroup, "Jährlich", AlarmSpec.RECURRENCE_YEARLY, state.recurrence);
        box.addView(recurrenceGroup);

        LinearLayout intervalRow = new LinearLayout(this);
        intervalRow.setOrientation(LinearLayout.HORIZONTAL);
        EditText interval = new EditText(this);
        interval.setSingleLine(true);
        interval.setText(state.intervalText.isEmpty() ? "1" : state.intervalText);
        interval.setEms(5);
        TextView intervalLabel = new TextView(this);
        intervalLabel.setText(state.unitLabel);
        intervalLabel.setPadding(dp(8), 0, 0, 0);
        intervalRow.addView(interval);
        intervalRow.addView(intervalLabel);
        box.addView(intervalRow);

        String[] weekdayLabels = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
        List<String> weekdayNames = LegacyAlarmDialogModel.weekdayCheckboxNames();
        CheckBox[] weekdayChecks = new CheckBox[weekdayNames.size()];
        LinearLayout weekdayRow = new LinearLayout(this);
        weekdayRow.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < weekdayNames.size(); i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(weekdayLabels[i]);
            cb.setTag(weekdayNames.get(i));
            Boolean checked = state.weekdayCheckboxes.get(weekdayNames.get(i));
            cb.setChecked(Boolean.TRUE.equals(checked));
            weekdayChecks[i] = cb;
            weekdayRow.addView(cb);
        }
        box.addView(weekdayRow);

        TextView nextView = new TextView(this);
        nextView.setText(state.nextText);
        box.addView(nextView);
        updateAlarmDialogVisibility(state.recurrence, intervalRow, intervalLabel, weekdayChecks);
        recurrenceGroup.setOnCheckedChangeListener((group, checkedId) -> updateAlarmDialogVisibility(selectedAlarmRecurrence(group), intervalRow, intervalLabel, weekdayChecks));

        new AlertDialog.Builder(this)
                .setTitle("Wecker")
                .setView(box)
                .setNeutralButton("Löschen", (d, which) -> {
                    AndroidAlarmScheduler.cancelNodeAlarm(this, currentNode);
                    AlarmUtils.clearFromNode(currentNode);
                    document.markChanged();
                    updateTitle();
                    status("Wecker gelöscht");
                })
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", (d, which) -> {
                    java.util.LinkedHashMap<String, Boolean> checked = new java.util.LinkedHashMap<>();
                    for (CheckBox cb : weekdayChecks) checked.put(String.valueOf(cb.getTag()), cb.isChecked());
                    LegacyAlarmDialogModel.ApplyResult result = LegacyAlarmDialogModel.apply(
                            enabled.isChecked(), when.getText().toString(), message.getText().toString(),
                            selectedAlarmRecurrence(recurrenceGroup), interval.getText().toString(), checked, java.util.TimeZone.getDefault());
                    if (!result.accepted) {
                        error(result.title, result.message);
                        return;
                    }
                    AndroidAlarmScheduler.cancelNodeAlarm(this, currentNode);
                    AlarmUtils.writeToNode(currentNode, result.spec);
                    document.markChanged();
                    updateTitle();
                    boolean scheduled = AndroidAlarmScheduler.scheduleNodeAlarm(this, currentNode);
                    status(LegacyAlarmDialogModel.statusAfterSave(result, scheduled));
                })
                .show();
    }

    private void addAlarmRadio(RadioGroup group, String label, String recurrence, String selected) {
        RadioButton rb = new RadioButton(this);
        rb.setText(label);
        rb.setTag(recurrence);
        rb.setChecked(AlarmSpec.normalizeRecurrence(recurrence).equals(AlarmSpec.normalizeRecurrence(selected)));
        group.addView(rb);
    }

    private String selectedAlarmRecurrence(RadioGroup group) {
        int checked = group == null ? -1 : group.getCheckedRadioButtonId();
        if (checked >= 0) {
            View v = group.findViewById(checked);
            Object tag = v == null ? null : v.getTag();
            if (tag != null) return AlarmSpec.normalizeRecurrence(String.valueOf(tag));
        }
        return AlarmSpec.RECURRENCE_NONE;
    }

    private void updateAlarmDialogVisibility(String recurrence, View intervalRow, TextView unitLabel, CheckBox[] weekdayChecks) {
        LegacyAlarmDialogModel.ViewState next = LegacyAlarmDialogModel.forRecurrence(recurrence);
        intervalRow.setVisibility(next.intervalVisible ? View.VISIBLE : View.GONE);
        unitLabel.setText(next.unitLabel);
        boolean weekdays = next.weekdaysVisible;
        if (weekdayChecks != null) {
            for (CheckBox cb : weekdayChecks) cb.setVisibility(weekdays ? View.VISIBLE : View.GONE);
        }
    }

    private EditText labeledEdit(LinearLayout box, String label, String value) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(12f);
        box.addView(tv);
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setText(value == null ? "" : value);
        box.addView(edit);
        return edit;
    }

    private int parseInt(String text, int fallback) {
        try { return (int) Double.parseDouble((text == null ? "" : text.trim()).replace(',', '.')); }
        catch (Exception e) { return fallback; }
    }

    private double parseDouble(String text, double fallback) {
        try { return Double.parseDouble((text == null ? "" : text.trim()).replace(',', '.')); }
        catch (Exception e) { return fallback; }
    }

    private String safeTitle(NoteNode node) {
        return node == null || node.title == null || node.title.isEmpty() ? "..." : node.title;
    }

    private void selectNode(NoteNode node, boolean focusEditor) {
        if (node == null) return;
        saveCurrentEditorToNode();
        currentNode = node;
        LegacyEditorNodeSync.LoadState loaded = LegacyEditorNodeSync.load(node);
        loadingEditor = true;
        rootTitleView.setText(document.ensureRoot().title == null ? "start" : document.ensureRoot().title);
        titleEdit.setText(loaded.titleText);
        editor.setText(loaded.editorPlainText);
        editor.setSelection(editor.getText().length());
        loadingEditor = false;
        editorDirty = false;
        titleDirty = false;
        treeAdapter.setSelected(node);
        int pos = flatIndexOf(node);
        if (pos >= 0) {
            treeList.setItemChecked(pos, true);
            treeList.smoothScrollToPosition(pos);
        }
        if (focusEditor) {
            editor.requestFocus();
        }
    }

    private void saveCurrentEditorToNode() {
        if (currentNode == null) return;
        LegacyEditorNodeSync.SaveState saved = LegacyEditorNodeSync.applyToNode(
                currentNode,
                titleEdit == null ? currentNode.title : titleEdit.getText().toString(),
                editor == null ? RtfUtils.rtfToPlainText(currentNode.rtf == null ? "" : currentNode.rtf) : editor.getText().toString(),
                editorDirty,
                titleDirty);
        if (saved.changed) {
            document.markChanged();
            if (saved.reloadDesktopNote) status("Haftnotiz-Daten aktualisiert");
        }
        editorDirty = false;
        titleDirty = false;
    }

    private void rebuildTree() {
        ArrayList<TreeListAdapter.FlatNode> rows = new ArrayList<>();
        buildRows(document.ensureRoot(), 0, rows);
        treeAdapter.setRows(rows);
        treeAdapter.setSelected(currentNode);
        rootTitleView.setText(document.ensureRoot().title == null ? "start" : document.ensureRoot().title);
    }

    private void buildRows(NoteNode node, int depth, ArrayList<TreeListAdapter.FlatNode> rows) {
        rows.add(new TreeListAdapter.FlatNode(node, depth));
        if (node.expanded) for (NoteNode child : node.children) buildRows(child, depth + 1, rows);
    }

    private int flatIndexOf(NoteNode node) {
        List<TreeListAdapter.FlatNode> rows = treeAdapter.getRows();
        for (int i = 0; i < rows.size(); i++) if (rows.get(i).node == node) return i;
        return -1;
    }

    private void exportText() {
        exportTextWithMode(LegacyTextExportModel.Mode.UTF8, REQ_EXPORT_TEXT);
    }

    private void exportTextAnsi() {
        exportTextWithMode(LegacyTextExportModel.Mode.ANSI, REQ_EXPORT_TEXT_ANSI);
    }

    private void exportTextUnicode() {
        exportTextWithMode(LegacyTextExportModel.Mode.UNICODE, REQ_EXPORT_TEXT_UNICODE);
    }

    private void exportTextWithMode(LegacyTextExportModel.Mode mode, int requestCode) {
        saveCurrentEditorToNode();
        LegacyTextExportModel.Result result = LegacyTextExportModel.forTree(document.ensureRoot(), mode, currentDialogDirectory());
        pendingExportText = result.text;
        pendingExportTextBytes = result.bytes;
        startActivityForResult(intentForDialog(result.dialogSpec), requestCode);
    }

    private void exportHtml() {
        saveCurrentEditorToNode();
        pendingExportHtml = Exporters.treeToHtml(document.ensureRoot());
        startActivityForResult(intentForDialog(LegacyFileDialogModel.exportHtml(currentDialogDirectory(), currentDisplayName)), REQ_EXPORT_HTML);
    }


    private void exportRtf() {
        saveCurrentEditorToNode();
        pendingExportRtf = Exporters.treeToRtf(document.ensureRoot());
        startActivityForResult(intentForDialog(LegacyFileDialogModel.exportRtf(currentDialogDirectory())), REQ_EXPORT_RTF);
    }

    private void exportCurrentNodeText() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        pendingExportText = LegacyNodeExport.currentNodePlainText(currentNode);
        pendingExportTextBytes = LegacyTextExportModel.encode(pendingExportText, LegacyTextExportModel.Mode.UTF8);
        LegacyFileDialogModel.Spec spec = new LegacyFileDialogModel.Spec(
                LegacyFileDialogModel.Kind.EXPORT_TXT_UTF8,
                "Knoten TXT exportieren",
                "txt Files|*.txt",
                ".txt",
                LegacyNodeExport.defaultFileName(currentNode, ".txt"),
                currentDialogDirectory(),
                "text/plain",
                new String[]{"text/plain"},
                false,
                true);
        startActivityForResult(intentForDialog(spec), REQ_EXPORT_NODE_TEXT);
    }

    private void exportCurrentNodeRtf() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        pendingExportRtf = LegacyNodeExport.currentNodeRtf(currentNode);
        LegacyFileDialogModel.Spec spec = new LegacyFileDialogModel.Spec(
                LegacyFileDialogModel.Kind.EXPORT_RTF,
                "Knoten RTF exportieren",
                "rtf Files|*.rtf",
                ".rtf",
                LegacyNodeExport.defaultFileName(currentNode, ".rtf"),
                currentDialogDirectory(),
                "application/rtf",
                new String[]{"text/rtf", "application/rtf"},
                false,
                true);
        startActivityForResult(intentForDialog(spec), REQ_EXPORT_NODE_RTF);
    }

    private void createUnifiedCurrentNote() {
        createUnifiedNote(LegacyUnifiedNote.Scope.CURRENT_SUBTREE);
    }

    private void createUnifiedRootNote() {
        createUnifiedNote(LegacyUnifiedNote.Scope.ROOT);
    }

    private void createUnifiedNote(LegacyUnifiedNote.Scope scope) {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        LegacyUnifiedNote.UnifiedResult result = LegacyUnifiedNote.attach(document, currentNode, scope);
        rebuildTree();
        selectNode(result.created == null ? currentNode : result.created, true);
        status((scope == LegacyUnifiedNote.Scope.CURRENT_SUBTREE ? "Teilbaum" : "Gesamter Baum") + " zusammengefasst (" + result.sourceNodes + " Knoten)");
    }

    private void showSearchDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("Suchbegriff");
        input.setText(searchSession.cachedTerm());
        CheckBox allNodes = new CheckBox(this);
        allNodes.setText("Alle Knoten durchsuchen");
        allNodes.setChecked(searchSession.cachedTerm().isEmpty() || searchSession.cachedAllNodes());
        CheckBox whole = new CheckBox(this);
        whole.setText("Ganze Wörter (Legacy: Trennung nur Space/CR/LF)");
        whole.setChecked(searchSession.cachedWholeWords());
        CheckBox sensitive = new CheckBox(this);
        sensitive.setText("Groß-/Kleinschreibung beachten");
        sensitive.setChecked(searchSession.cachedCaseSensitive());
        CheckBox titles = new CheckBox(this);
        titles.setText("Titel mitsuchen");
        titles.setChecked(searchSession.cachedTerm().isEmpty() || searchSession.cachedIncludeTitles());
        box.addView(input);
        box.addView(allNodes);
        box.addView(whole);
        box.addView(sensitive);
        box.addView(titles);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(LegacySearchDialogModel.dialogTitle(settings == null ? "Deutsch" : settings.language))
                .setView(box)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton(LegacySearchDialogModel.searchButton(settings == null ? "Deutsch" : settings.language), null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                LegacySearchDialogModel.SearchOptions options = LegacySearchDialogModel.normalize(
                        input.getText().toString(), whole.isChecked(), sensitive.isChecked(), titles.isChecked(),
                        settings == null ? "Deutsch" : settings.language);
                if (!options.accepted) { error(options.title, options.message); return; }
                dialog.dismiss();
                runSearch(options.term, allNodes.isChecked(), options.wholeWords, options.caseSensitive, options.includeTitles);
            });
            input.requestFocus();
            if (dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        });
        dialog.show();
    }

    private void runSearch(String term, boolean allNodes, boolean whole, boolean sensitive, boolean titles) {
        saveCurrentEditorToNode();
        LegacySearchSession.SearchStep step = searchSession.begin(document.ensureRoot(), currentNode, term, allNodes, whole, sensitive, titles, settings == null ? "Deutsch" : settings.language);
        List<SearchResult> results = step.results;
        if (results.isEmpty()) {
            status(step.status);
            new AlertDialog.Builder(this).setTitle(LegacySearchDialogModel.dialogTitle(settings == null ? "Deutsch" : settings.language)).setMessage("Keine Treffer.").setPositiveButton("OK", null).show();
            return;
        }
        String[] labels = new String[results.size()];
        for (int i = 0; i < results.size(); i++) labels[i] = results.get(i).label();
        LegacySearchResultNavigator.State navigator = LegacySearchResultNavigator.fromResults(results);
        status(navigator.statusGerman());
        new AlertDialog.Builder(this)
                .setTitle(step.resultCountLabel)
                .setItems(labels, (d, which) -> {
                    SearchResult r = results.get(which);
                    ensureAncestorsExpanded(r.node);
                    rebuildTree();
                    selectNode(r.node, true);
                    if (!r.titleMatch) {
                        int start = Math.max(0, Math.min(r.start, editor.getText().length()));
                        int end = Math.max(start, Math.min(start + r.length, editor.getText().length()));
                        editor.setSelection(start, end);
                    } else {
                        titleEdit.requestFocus();
                        titleEdit.setSelection(0, titleEdit.getText().length());
                    }
                    status(LegacySearchDialogModel.statusForResult(which + 1, results.size(), settings == null ? "Deutsch" : settings.language));
                })
                .setPositiveButton("OK", null)
                .show();
    }

    private void ensureAncestorsExpanded(NoteNode node) {
        for (NoteNode n = node; n != null; n = n.parent) n.expanded = true;
    }

    private void showStats() {
        saveCurrentEditorToNode();
        TreeStats s = TreeStats.collect(document.ensureRoot());
        new AlertDialog.Builder(this)
                .setTitle("Statistik")
                .setMessage(s.asLegacyText())
                .setPositiveButton("OK", null)
                .show();
    }


    private void showLifecycleStatus() {
        saveCurrentEditorToNode();
        LegacyDocumentLifecycle.Status state = LegacyDocumentLifecycle.status(document, currentNode, currentUri != null || currentRawFile != null, currentFtpTarget != null);
        LegacyAppDataConfigPath.Paths paths = LegacyAppDataConfigPath.build("%APPDATA%", "Documents");
        LegacyConfigLifecycle.WindowData window = LegacyConfigLifecycle.onLoad(settings, 1920, 1080);
        LegacyClipboardFormatModel.Preferred clip = LegacyClipboardFormatModel.preferred(new LegacyClipboardFormatModel.Formats(internalClipboardNode != null, false, false, false, false));
        String msg = "Titel: " + state.windowTitle("Notizen Android") +
                "\nGeändert: " + (state.changed ? "ja" : "nein") +
                "\nSpeicherentscheidung: " + state.defaultSaveDecision +
                "\nKann speichern: " + (state.canSave ? "ja" : "nein") +
                "\nPasswort gesetzt: " + (state.passwordSet ? "ja" : "nein") +
                "\nConfig: " + paths.configFile +
                "\nStandarddatei: " + paths.defaultAlxFile +
                "\nFensterstatus: " + window.windowState + " " + window.width + "x" + window.height +
                "\nClipboard-Priorität: " + clip.format;
        new AlertDialog.Builder(this).setTitle("Legacy-Status").setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void showDesktopNoteTrayList() {
        saveCurrentEditorToNode();
        LegacyDesktopNoteTrayRegistry.Registry registry = LegacyDesktopNoteTrayRegistry.fromTree(document == null ? null : document.root);
        StringBuilder msg = new StringBuilder(registry.summary());
        if (!registry.menuItems.isEmpty()) {
            msg.append("\n\nTray-Menü:");
            for (LegacyDesktopNoteTrayRegistry.MenuItem item : registry.menuItems) {
                msg.append("\n").append(item.index).append(": ").append(item.label).append(" → ").append(item.action);
            }
        }
        LegacyTraySelectionModel.Decision example = LegacyTraySelectionModel.decide(registry, registry.desktopCount > 0 ? 3 : 1);
        msg.append("\n\nAuswahlmodell: ").append(example.action).append(" — ").append(example.message);
        new AlertDialog.Builder(this).setTitle("Haftnotiz-Traymodell").setMessage(msg.toString()).setPositiveButton("OK", null).show();
    }

    private void closeDocumentLegacy() {
        saveCurrentEditorToNode();
        if (document == null) { newDocument(); return; }
        if (!document.changed) {
            resetAfterLegacyClose(LegacyCloseResetModel.plan(document, document.password, LegacyCloseResetModel.Choice.DONT_SAVE));
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Schließen")
                .setMessage("Die aktuelle Datei wurde geändert.")
                .setPositiveButton("Speichern", (d, w) -> {
                    LegacyCloseResetModel.ClosePlan plan = LegacyCloseResetModel.plan(document, document.password, LegacyCloseResetModel.Choice.SAVE);
                    saveDocument();
                    resetAfterLegacyClose(plan);
                })
                .setNegativeButton("Nicht speichern", (d, w) -> resetAfterLegacyClose(LegacyCloseResetModel.plan(document, document.password, LegacyCloseResetModel.Choice.DONT_SAVE)))
                .setNeutralButton("Abbrechen", null)
                .show();
    }

    private void resetAfterLegacyClose(LegacyCloseResetModel.ClosePlan plan) {
        if (plan == null || !plan.proceed) return;
        LegacyCloseResetModel.ResetState reset = LegacyCloseResetModel.afterClose("1.0.96-java-native");
        document = LegacyCloseResetModel.newUnnamedDocument();
        currentUri = null;
        currentRawFile = null;
        currentFtpTarget = null;
        currentDisplayName = reset.displayName;
        editorDirty = false;
        titleDirty = false;
        rebuildTree();
        selectNode(document.ensureRoot(), false);
        updateTitle();
        status(plan.message + (plan.wipePasswordFromMemory ? " · Passwortspeicher geleert" : ""));
    }

    private void showValidationSummary() {
        saveCurrentEditorToNode();
        try {
            LegacyValidation.LegacyAlxRoundtripResult result = LegacyValidation.validateDocumentRoundtrip(document, document == null ? "" : document.password);
            String msg = "Privacy-light ALX-Roundtripprüfung: " + (result.ok ? "OK" : "Unterschiede") + "\n\n" +
                    result.before.asLegacyText() +
                    (result.differences.isEmpty() ? "" : "\n\nUnterschiede:\n" + joinLines(result.differences));
            new AlertDialog.Builder(this).setTitle("ALX validieren").setMessage(msg).setPositiveButton("OK", null).show();
        } catch (Exception e) {
            error("ALX validieren", e.getMessage());
        }
    }

    private void showConfigSnapshot() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        LegacyConfigSnapshot.Snapshot snapshot = LegacyConfigSnapshot.fromSettings(settings, w, h);
        new AlertDialog.Builder(this)
                .setTitle("Legacy-Config")
                .setMessage(snapshot.summary())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDesktopNoteLayout() {
        if (currentNode == null) return;
        DesktopNoteState state = currentNode.desktopNote == null ? new DesktopNoteState() : currentNode.desktopNote;
        LegacyDesktopNotePaint.PaintModel model = LegacyDesktopNotePaint.forState(state, safeTitle(currentNode));
        StringBuilder b = new StringBuilder(model.summary());
        b.append("\n\nZeichenbefehle:");
        for (LegacyDesktopNotePaint.DrawCommand cmd : model.commands) b.append("\n").append(cmd.toString());
        boolean borders = settings == null || settings.showDesknoteBorders;
        b.append("\n\nMouseLeave: ").append(LegacyDesktopNotePaint.mouseLeaveDecision(
                borders, model.window, model.window.width + 4, model.window.height + 4, true));
        new AlertDialog.Builder(this)
                .setTitle("Haftnotiz-Layout")
                .setMessage(b.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showDiagnostics() {
        saveCurrentEditorToNode();
        String msg = LegacyDiagnosticReport.build(document, currentNode, currentDocumentTitleState(), settings, "1.0.96-java-native");
        new AlertDialog.Builder(this).setTitle("Diagnose").setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void showLayoutDiagnostics() {
        Configuration c = getResources().getConfiguration();
        LegacyMainLayoutModel.Layout layout = LegacyMainLayoutModel.compute(c.screenWidthDp, c.screenHeightDp, c.orientation);
        new AlertDialog.Builder(this).setTitle("Legacy-Layout")
                .setMessage(layout.summary())
                .setPositiveButton("OK", null).show();
    }

    private void showRuntimeIdentity() {
        String msg = LegacyRuntimeIdentity.summary("1.0.96-java-native")
                + "\n\nZIP-Modus-Beispiele:"
                + "\n" + LegacyPackagePermissionModel.describe("scripts/install_linux_launcher.sh", false)
                + "\n" + LegacyPackagePermissionModel.describe("Notizen PyQt.desktop", false)
                + "\n" + LegacyPackagePermissionModel.describe("src/notizen_py_qt/app.py", false);
        new AlertDialog.Builder(this).setTitle("Launcher/Identity")
                .setMessage(msg)
                .setPositiveButton("OK", null).show();
    }


    private void showActivationFocusModel() {
        LegacyActivationDialogFocus.DialogState state = LegacyActivationDialogFocus.stateFromVisibleNames("suche", currentFtpTarget != null ? "ftpkram" : "");
        LegacyActivationDialogFocus.Plan plan = LegacyActivationDialogFocus.onMainActivated(state);
        new AlertDialog.Builder(this).setTitle("Legacy-Dialogfokus").setMessage(plan.summary()).setPositiveButton("OK", null).show();
    }

    private void showToolbarToggleModel() {
        StringBuilder b = new StringBuilder();
        b.append(LegacyToolbarToggleModel.toggle(LegacyToolbarToggleModel.MenuToggle.FONT, false, true, dp(12)).summary());
        b.append("\n").append(LegacyToolbarToggleModel.toggle(LegacyToolbarToggleModel.MenuToggle.EDIT, true, currentNode != null, dp(160)).summary());
        b.append("\n").append(LegacyToolbarToggleModel.toggle(LegacyToolbarToggleModel.MenuToggle.NEW_NODE, false, document != null, dp(260)).summary());
        new AlertDialog.Builder(this).setTitle("Legacy-Toolbars").setMessage(b.toString()).setPositiveButton("OK", null).show();
    }

    private void showWindowChromeModel() {
        Configuration c = getResources().getConfiguration();
        int w = Math.max(320, c.screenWidthDp);
        int h = Math.max(240, c.screenHeightDp);
        String title = currentDocumentTitleState().appTitle;
        String msg = LegacyMainWindowChrome.summary(title, w, h)
                + "\n\nHit links oben: " + LegacyMainWindowChrome.hitTest(8, 8, w, h, true, false).summary()
                + "\nHit unten rechts: " + LegacyMainWindowChrome.hitTest(w - 1, h - 1, w, h, true, false).summary()
                + "\nDialog blockiert: " + LegacyMainWindowChrome.hitTest(120, 12, w, h, true, true).summary();
        new AlertDialog.Builder(this).setTitle("Legacy-Fensterchrome").setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void showAutosaveTickModel() {
        boolean hasTarget = currentUri != null || currentRawFile != null || currentFtpTarget != null;
        LegacyAutosaveTimerTick.TickPlan plan = LegacyAutosaveTimerTick.decide(settings == null || settings.autosaveSeconds > 0, document != null && document.changed, hasTarget, true);
        String msg = plan.summary() + "\nIntervall: " + (settings == null ? 60 : settings.autosaveSeconds) + " Sekunden";
        new AlertDialog.Builder(this).setTitle("Legacy-Autosave-Tick").setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void showLegacyFontSetModel() {
        int start = editor == null ? 0 : editor.getSelectionStart();
        int end = editor == null ? 0 : editor.getSelectionEnd();
        if (start < 0) start = 0;
        if (end < start) end = start;
        int len = editor == null || editor.getText() == null ? 0 : editor.getText().length();
        LegacyFontSetModel.FontPlan bold = LegacyFontSetModel.plan(document != null, false, len, start, end - start, LegacyFontSetModel.Function.STYLE, "bold", "", 12, 0, currentNode != null && currentNode.desktopNote != null);
        LegacyFontSetModel.FontPlan family = LegacyFontSetModel.plan(document != null, true, len, start, end - start, LegacyFontSetModel.Function.FAMILY, "", "Microsoft Sans Serif", 12, 0, currentNode != null && currentNode.desktopNote != null);
        new AlertDialog.Builder(this).setTitle("Legacy-font_set").setMessage(bold.summary() + "\n\n" + family.summary()).setPositiveButton("OK", null).show();
    }

    private void showWindowMoveResizeModel() {
        Configuration c = getResources().getConfiguration();
        LegacyWindowMoveResizeModel.Rect rect = new LegacyWindowMoveResizeModel.Rect(80, 80, Math.max(320, c.screenWidthDp), Math.max(240, c.screenHeightDp));
        LegacyWindowMoveResizeModel.DragState move = LegacyWindowMoveResizeModel.mouseDown(rect, 100, 10, 200, 100, true, false);
        LegacyWindowMoveResizeModel.Update moved = LegacyWindowMoveResizeModel.mouseMove(move, 260, 150, 120, 100);
        LegacyWindowMoveResizeModel.DragState resize = LegacyWindowMoveResizeModel.mouseDown(rect, rect.width - 1, rect.height - 1, 400, 300, true, false);
        LegacyWindowMoveResizeModel.Update resized = LegacyWindowMoveResizeModel.mouseMove(resize, 450, 360, 120, 100);
        String msg = move.summary() + "\n" + moved.summary() + "\n\n" + resize.summary() + "\n" + resized.summary();
        new AlertDialog.Builder(this).setTitle("Legacy-Mausmodell").setMessage(msg).setPositiveButton("OK", null).show();
    }

    private void showAlxPipelineModel() {
        String target = currentTargetString();
        String password = document == null ? "" : document.password;
        LegacyAlxStreamPipeline.Plan open = LegacyAlxStreamPipeline.openPlan(target, password);
        LegacyAlxStreamPipeline.Plan save = LegacyAlxStreamPipeline.savePlan(target, document != null && document.changed, settings == null ? 0 : settings.backupKeep, password);
        new AlertDialog.Builder(this).setTitle("Legacy-ALX-Pipeline").setMessage("Öffnen:\n" + open.summary() + "\n\nSpeichern:\n" + save.summary()).setPositiveButton("OK", null).show();
    }

    private void showApkBuildPlan() {
        LegacyApkBuildPipeline.Params params = LegacyApkBuildPipeline.defaultParams();
        new AlertDialog.Builder(this).setTitle("Termux-APK-Buildplan").setMessage(LegacyApkBuildPipeline.summary(params)).setPositiveButton("OK", null).show();
    }

    private String currentTargetString() {
        if (currentFtpTarget != null) return currentFtpTarget.safeDisplayUrl();
        if (currentRawFile != null) return currentRawFile.getAbsolutePath();
        if (currentUri != null) return currentUri.toString();
        return currentDisplayName == null ? "unbenannt.alx" : currentDisplayName;
    }

    private String joinLines(java.util.List<String> lines) {
        StringBuilder out = new StringBuilder();
        if (lines != null) for (String line : lines) {
            if (out.length() > 0) out.append('\n');
            out.append(line == null ? "" : line);
        }
        return out.toString();
    }

    private void showPasswordDialog(String initial, PasswordCallback callback) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(initial == null ? "" : initial);
        input.setHint("Leer = unverschlüsselt speichern");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
                .setTitle("ALX-Passwort")
                .setMessage("Kompatibel zur alten Notizen.NET-ALX-Verschlüsselung. Das Passwort wird nur in dieser App-Sitzung im Dokument gehalten.")
                .setView(input)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", (d, which) -> callback.onPassword(input.getText().toString()))
                .show();
    }

    private void showPasswordChangeDialog() {
        if (document == null) return;
        String lang = settings == null ? "Deutsch" : settings.language;
        LegacyPasswordDialogModel.PromptSpec spec = LegacyPasswordDialogModel.changePrompt(lang, document.password);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText oldPass = labeledPasswordEdit(box, spec.oldPasswordLabel, "");
        oldPass.setEnabled(spec.oldPasswordEnabled);
        EditText newPass1 = labeledPasswordEdit(box, spec.newPasswordLabel, "");
        EditText newPass2 = labeledPasswordEdit(box, spec.repeatPasswordLabel, "");
        TextView help = new TextView(this);
        help.setText(spec.bottomInfo + "\nMaximal " + spec.maxChars + " Zeichen; alte DES-Kompatibilität bleibt erhalten.");
        help.setTextSize(12f);
        box.addView(help);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(spec.title)
                .setView(box)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            LegacyPasswordDialogModel.PasswordChangeDecision decision = LegacyPasswordDialogModel.validateChange(
                    document.password, oldPass.getText().toString(), newPass1.getText().toString(), newPass2.getText().toString(), lang);
            if (!decision.accepted) {
                error(spec.title, decision.message);
                return;
            }
            document.password = decision.newPassword;
            document.markChanged();
            updateTitle();
            status(decision.newPassword.isEmpty() ? "Passwort entfernt" : "Passwort gesetzt");
            dialog.dismiss();
        }));
        dialog.show();
    }

    private EditText labeledPasswordEdit(LinearLayout box, String label, String value) {
        EditText edit = labeledEdit(box, label, value);
        edit.setSingleLine(true);
        edit.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return edit;
    }

    private void showFeedbackDialog() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        EditText input = new EditText(this);
        input.setMinLines(5);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setHint("Meinung, Fehlermeldung oder Feature-Vorschlag");
        new AlertDialog.Builder(this)
                .setTitle("Feedback")
                .setMessage("Das alte Online-Feedback wird im Android-Port sicher lokal als UTF-16-GZip-Datei archiviert. Mindestlänge: 10 Zeichen; Tageslimit wie im Legacy-Code.")
                .setView(input)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("Speichern", (d, which) -> {
                    String text = input.getText().toString();
                    long today = LegacyFeedback.dotnetTodayTicks();
                    LegacyFeedback.FeedbackDecision decision = LegacyFeedback.decision(text, settings.feedbackDayTicks, settings.feedbackCount, today);
                    if (!decision.allowed) {
                        error("Feedback", LegacyFeedback.reasonText(decision.reason));
                        return;
                    }
                    try {
                        File written = AndroidFeedbackStore.write(this, text);
                        LegacyFeedback.FeedbackThrottleState next = LegacyFeedback.nextState(settings.feedbackDayTicks, settings.feedbackCount, today);
                        settings.feedbackDayTicks = next.dayTicks;
                        settings.feedbackCount = next.count;
                        AndroidSettingsStore.save(this, settings);
                        status("Feedback lokal gespeichert: " + written.getName());
                    } catch (Exception e) {
                        error("Feedback", e.getMessage());
                    }
                })
                .show();
    }

    private void showInfo() {
        String version = "1.0.96-java-native";
        String lang = settings == null ? "Deutsch" : settings.language;
        LegacyAboutHelp.AboutSpec about = LegacyAboutHelp.spec(lang, version);
        String msg = about.description + "\n\n" +
                LegacyAboutHelp.androidPortAppendix(version) + "\n\n" +
                "Feedback: " + about.feedbackLabel + "\n" +
                "Web: " + about.webUrl + "\n\n" +
                "Portiert: ALX öffnen/speichern, Legacy-DES-Passwort, GZip/UTF-16-XML, Baumknoten, Expansion, Löschen, Verschieben, Knoten-Zwischenablage, Suche, TXT/HTML/RTF-Export, Gesamtnotiz, Statistik, Farben, Haftnotiz-Metadaten, Wecker-Regeln, FTP, Legacy-Config, Legacy-Tastaturkürzel und app-interne Sicherungen vor Überschreiben.\n\n" +
                "v29 ergänzt: erweiterte RTF/CSS-Brücke, RichTextBox-Semantik, RTF-Info, Clipboard-Fokusmodell und TreeView-Labelregeln.\n\n" +
                "v35 ergänzt: Legacy-Recent-Menü, Dokumenttitelmodell, Fensterstatusmodell, ToolStrip-Breitenlogik, FTP-Retry-Regel und lokale Diagnose.\n\n" +
                "v47 ergänzt: Legacy-Farb-/Dateidialogmodelle, RTF-Farbformatierung und ANSI/Unicode-TXT-Export.\n\n" +
                "v59 ergänzt: Legacy-Config-Snapshot, Haftnotiz-Paint-/Layoutmodell, Save-Workflow und Suchergebnisnavigator.\n\n" +
                "v65 ergänzt: Dokument-Lifecycle, Schließen-/Reset-Modell, Baum-Neuanlage, Clipboard-Formatpriorität, AppData-/Config-Lifecycle und Haftnotiz-Trayliste.\n\n" +
                "v71 ergänzt: Runtime-/Launcher-Identität, Layoutdiagnose und Paketberechtigungsmodell.\n\n" +
                "v83 ergänzt: Dialog-Fokusmodell, ToolStrip-Toggles, Hauptfenster-Chrome, Autosave-Tick, CText-Semantik und Termux-APK-Buildplan.\n\n" +
                "v95 ergänzt: font_set-Entscheidungsmodell, Move-/Resize-Mausmodell und ALX-Stream-Pipeline für lokale Datei, SAF und FTP.\n\n" +
                "Bewusst mobil angepasst: Android nutzt keinen Windows-Tray und keine frei schwebenden Desktop-Haftnotiz-Fenster. Diese Metadaten bleiben im ALX erhalten und können mobil editiert werden. RTF wird als lesbarer Text angezeigt; vorhandenes RTF bleibt erhalten, solange die Notiz nicht bearbeitet wird.\n\n" +
                "Lizenz: GPLv3 wie die Ausgangsarchive.";
        new AlertDialog.Builder(this).setTitle(about.title + " " + about.version).setMessage(msg).setPositiveButton(about.closeLabel, null).show();
    }

    private void confirmDiscardThen(ContinueCallback callback) {
        saveCurrentEditorToNode();
        if (!LegacyDialogModels.shouldShowWannaSave(document.changed)) {
            callback.run();
            return;
        }
        LegacyDialogModels.DialogSpec spec = LegacyDialogModels.wannaSave(settings == null ? "Deutsch" : settings.language);
        new AlertDialog.Builder(this)
                .setTitle(spec.title)
                .setMessage(spec.message)
                .setPositiveButton(spec.positive, (d, which) -> saveThenMaybeContinue(callback))
                .setNeutralButton(spec.neutral, (d, which) -> callback.run())
                .setNegativeButton(spec.negative, null)
                .show();
    }

    private void saveThenMaybeContinue(ContinueCallback callback) {
        saveCurrentEditorToNode();
        if (!document.changed) {
            callback.run();
            return;
        }
        if (currentUri != null) {
            writeDocumentToUri(currentUri, currentDisplayName, false);
            if (!document.changed) callback.run();
            return;
        }
        if (currentRawFile != null) {
            writeDocumentToFile(currentRawFile, false);
            if (!document.changed) callback.run();
            return;
        }
        if (currentFtpTarget != null) {
            saveDocumentToFtp(currentFtpTarget);
            status("FTP-Speichern gestartet; Aktion danach erneut ausführen.");
            return;
        }
        saveDocumentAs();
        status("Bitte nach dem Speichern die Aktion erneut ausführen.");
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        String keyName = legacyKeyName(keyCode);
        LegacyShortcuts.Shortcut shortcut = LegacyShortcuts.resolve(
                keyName,
                event != null && event.isCtrlPressed(),
                event != null && event.isShiftPressed(),
                event != null && event.isAltPressed(),
                treeList != null && treeList.hasFocus(),
                editor != null && editor.hasFocus());
        if (shortcut != null && handleLegacyShortcut(shortcut.action)) return true;
        return super.onKeyDown(keyCode, event);
    }

    private String legacyKeyName(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SPACE: return "Space";
            case KeyEvent.KEYCODE_S: return "S";
            case KeyEvent.KEYCODE_O: return "O";
            case KeyEvent.KEYCODE_N: return "N";
            case KeyEvent.KEYCODE_Q: return "Q";
            case KeyEvent.KEYCODE_C: return "C";
            case KeyEvent.KEYCODE_V: return "V";
            case KeyEvent.KEYCODE_X: return "X";
            case KeyEvent.KEYCODE_U: return "U";
            case KeyEvent.KEYCODE_F: return "F";
            case KeyEvent.KEYCODE_PLUS: return "+";
            case KeyEvent.KEYCODE_EQUALS: return "=";
            case KeyEvent.KEYCODE_MINUS: return "Minus";
            case KeyEvent.KEYCODE_NUMPAD_ADD: return "Add";
            case KeyEvent.KEYCODE_NUMPAD_SUBTRACT: return "Subtract";
            case KeyEvent.KEYCODE_INSERT: return "Insert";
            case KeyEvent.KEYCODE_FORWARD_DEL: return "Delete";
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER: return "Enter";
            default: return "";
        }
    }

    private boolean handleLegacyShortcut(String action) {
        if (action == null) return false;
        switch (action) {
            case "save": saveDocument(); return true;
            case "open": confirmDiscardThen(this::openDocument); return true;
            case "new_document": confirmDiscardThen(this::newDocument); return true;
            case "quit": confirmDiscardThen(this::finish); return true;
            case "search": showSearchDialog(); return true;
            case "rename":
                if (titleEdit != null) {
                    titleEdit.requestFocus();
                    titleEdit.selectAll();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.showSoftInput(titleEdit, InputMethodManager.SHOW_IMPLICIT);
                }
                return true;
            case "alarm": showAlarmDialog(); return true;
            case "copy":
                if (editor != null && editor.hasFocus()) return false;
                copyCurrentNode(false);
                return true;
            case "cut":
                if (editor != null && editor.hasFocus()) return false;
                cutCurrentNode();
                return true;
            case "paste":
                if (editor != null && editor.hasFocus()) return false;
                pasteNode();
                return true;
            case "paste_node": pasteNode(); return true;
            case "cut_node": cutCurrentNode(); return true;
            case "add_child": newChild(); return true;
            case "add_sibling": newNext(); return true;
            case "delete_node": deleteCurrent(); return true;
            case "font_bigger": adjustEditorFont(1.0f); return true;
            case "font_smaller": adjustEditorFont(-1.0f); return true;
            default: return false;
        }
    }

    private void adjustEditorFont(float deltaSp) {
        if (editor == null) return;
        float next = Math.max(10f, Math.min(32f, editor.getTextSize() / getResources().getDisplayMetrics().scaledDensity + deltaSp));
        editor.setTextSize(next);
        status("Editor-Schriftgröße: " + Math.round(next) + " sp");
    }

    private boolean handleViewIntent(Intent intent) {
        if (intent == null) return false;
        Uri uri = intent.getData();
        if (uri != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            if ("file".equalsIgnoreCase(uri.getScheme())) openRawFile(new File(uri.getPath() == null ? "" : uri.getPath()), null);
            else openUri(uri, null);
            return true;
        }
        return false;
    }

    private void openUri(Uri uri, String password) {
        try {
            takeReadWritePermission(uri);
            byte[] data = readAll(uri);
            NoteDocument loaded = AlxIo.load(data, password == null ? "" : password);
            document = loaded;
            currentUri = uri;
            currentRawFile = null;
            currentFtpTarget = null;
            currentDisplayName = ensureAlxName(queryDisplayName(uri));
            document.displayName = currentDisplayName;
            rememberCurrentFile(uri.toString());
            document.markSaved();
            editorDirty = false;
            titleDirty = false;
            rebuildTree();
            selectNode(document.ensureRoot(), false);
            updateTitle();
            status("Geöffnet: " + currentDisplayName);
        } catch (AlxException.PasswordRequired e) {
            byte[] copy;
            try { copy = readAll(uri); } catch (Exception readError) { error("Öffnen fehlgeschlagen", readError.getMessage()); return; }
            final byte[] data = copy;
            showPasswordDialog("", p -> openBytesAfterPassword(uri, data, p));
        } catch (Exception e) {
            error("Öffnen fehlgeschlagen", e.getMessage());
        }
    }

    private void openBytesAfterPassword(Uri uri, byte[] data, String password) {
        try {
            NoteDocument loaded = AlxIo.load(data, password);
            document = loaded;
            currentUri = uri;
            currentRawFile = null;
            currentFtpTarget = null;
            currentDisplayName = ensureAlxName(queryDisplayName(uri));
            document.displayName = currentDisplayName;
            rememberCurrentFile(uri.toString());
            document.markSaved();
            editorDirty = false;
            titleDirty = false;
            rebuildTree();
            selectNode(document.ensureRoot(), false);
            updateTitle();
            status("Geöffnet: " + currentDisplayName);
        } catch (Exception e) {
            error("Öffnen fehlgeschlagen", e.getMessage());
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        if (requestCode == REQ_OPEN) {
            openUri(uri, null);
        } else if (requestCode == REQ_SAVE_AS) {
            takeReadWritePermission(uri);
            writeDocumentToUri(uri, queryDisplayName(uri), true);
        } else if (requestCode == REQ_EXPORT_TEXT || requestCode == REQ_EXPORT_TEXT_ANSI || requestCode == REQ_EXPORT_TEXT_UNICODE) {
            try {
                writeAll(uri, pendingExportTextBytes == null ? (pendingExportText == null ? new byte[0] : pendingExportText.getBytes(java.nio.charset.StandardCharsets.UTF_8)) : pendingExportTextBytes);
                if (requestCode == REQ_EXPORT_TEXT_ANSI) status(LegacyTextExportModel.status(LegacyTextExportModel.Mode.ANSI));
                else if (requestCode == REQ_EXPORT_TEXT_UNICODE) status(LegacyTextExportModel.status(LegacyTextExportModel.Mode.UNICODE));
                else status(LegacyTextExportModel.status(LegacyTextExportModel.Mode.UTF8));
            } catch (Exception e) { error("Export fehlgeschlagen", e.getMessage()); }
        } else if (requestCode == REQ_EXPORT_HTML) {
            try {
                writeAll(uri, (pendingExportHtml == null ? "" : pendingExportHtml).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                status("HTML exportiert");
            } catch (Exception e) { error("Export fehlgeschlagen", e.getMessage()); }
        } else if (requestCode == REQ_EXPORT_RTF) {
            try {
                writeAll(uri, (pendingExportRtf == null ? "" : pendingExportRtf).getBytes(java.nio.charset.Charset.forName("windows-1252")));
                status("RTF exportiert");
            } catch (Exception e) { error("Export fehlgeschlagen", e.getMessage()); }
        } else if (requestCode == REQ_EXPORT_NODE_TEXT) {
            try {
                writeAll(uri, pendingExportTextBytes == null ? (pendingExportText == null ? new byte[0] : pendingExportText.getBytes(java.nio.charset.StandardCharsets.UTF_8)) : pendingExportTextBytes);
                status("Knoten-TXT exportiert");
            } catch (Exception e) { error("Knoten-Export fehlgeschlagen", e.getMessage()); }
        } else if (requestCode == REQ_EXPORT_NODE_RTF) {
            try {
                writeAll(uri, (pendingExportRtf == null ? "" : pendingExportRtf).getBytes(java.nio.charset.Charset.forName("windows-1252")));
                status("Knoten-RTF exportiert");
            } catch (Exception e) { error("Knoten-Export fehlgeschlagen", e.getMessage()); }
        } else if (requestCode == REQ_IMPORT_CONFIG) {
            try {
                applyImportedSettings(readAll(uri));
            } catch (Exception e) { error("Config-Import", e.getMessage()); }
        } else if (requestCode == REQ_INSERT_IMAGE) {
            insertImageFromUri(uri);
        } else if (requestCode == REQ_EXPORT_CONFIG) {
            try {
                writeAll(uri, pendingExportConfig == null ? new byte[0] : pendingExportConfig);
                status("Config exportiert");
            } catch (Exception e) { error("Config-Export", e.getMessage()); }
        } else if (requestCode == REQ_IMPORT_HTML) {
            importHtmlNoteFromUri(uri);
        } else if (requestCode == REQ_IMPORT_TEXT) {
            importTextFromUri(uri);
        } else if (requestCode == REQ_IMPORT_RTF) {
            importRtfFromUri(uri);
        }
    }

    private byte[] readAll(Uri uri) throws Exception {
        ContentResolver resolver = getContentResolver();
        InputStream in = resolver.openInputStream(uri);
        if (in == null) throw new IllegalStateException("Datei kann nicht gelesen werden.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        in.close();
        return out.toByteArray();
    }

    private void writeAll(Uri uri, byte[] bytes) throws Exception {
        OutputStream out = getContentResolver().openOutputStream(uri, "wt");
        if (out == null) throw new IllegalStateException("Datei kann nicht geschrieben werden.");
        out.write(bytes == null ? new byte[0] : bytes);
        out.close();
    }

    private void takeReadWritePermission(Uri uri) {
        try {
            int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, flags);
        } catch (Exception ignored) {}
    }

    private String queryDisplayName(Uri uri) {
        if (uri == null) return currentDisplayName == null ? "unbenannt.alx" : currentDisplayName;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    String value = cursor.getString(idx);
                    if (value != null && !value.isEmpty()) return value;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        String last = uri.getLastPathSegment();
        return last == null || last.isEmpty() ? "unbenannt.alx" : last;
    }

    private String currentDialogDirectory() {
        if (currentRawFile != null && currentRawFile.getParentFile() != null) return currentRawFile.getParentFile().getAbsolutePath();
        if (settings != null && !settings.recentFiles.isEmpty()) {
            String first = settings.recentFiles.get(0);
            LegacyOpenTarget t = LegacyOpenTarget.parse(first);
            if (t.kind == LegacyOpenTarget.Kind.FILE && t.normalized != null && !t.normalized.isEmpty()) {
                File f = new File(t.normalized);
                File parent = f.getParentFile();
                if (parent != null) return parent.getAbsolutePath();
            }
        }
        return getFilesDir() == null ? "" : getFilesDir().getAbsolutePath();
    }

    private String ensureAlxName(String name) {
        String n = name == null || name.isEmpty() ? "unbenannt.alx" : name;
        return n.toLowerCase().endsWith(".alx") ? n : n + ".alx";
    }

    private String baseName(String name) {
        String n = name == null || name.isEmpty() ? "notizen" : name;
        if (n.toLowerCase().endsWith(".alx")) return n.substring(0, n.length() - 4);
        return n;
    }

    private String baseNameWithoutExtension(String name) {
        String n = name == null || name.isEmpty() ? "notiz" : name;
        int slash = Math.max(n.lastIndexOf('/'), n.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < n.length()) n = n.substring(slash + 1);
        int dot = n.lastIndexOf('.');
        if (dot > 0) n = n.substring(0, dot);
        return n.trim().isEmpty() ? "notiz" : n.trim();
    }

    private LegacyDocumentTitle.State currentDocumentTitleState() {
        return LegacyDocumentTitle.build(
                "Notizen Android",
                currentDisplayName,
                document != null && document.changed,
                currentFtpTarget == null ? "" : currentFtpTarget.safeDisplayUrl(),
                currentRawFile == null ? "" : currentRawFile.getAbsolutePath(),
                currentUri == null ? "" : currentUri.toString());
    }

    private void updateTitle() {
        LegacyDocumentTitle.State state = currentDocumentTitleState();
        setTitle(state.appTitle);
        if (fileView != null) fileView.setText(state.sourceLine);
    }

    private void status(String text) {
        if (statusView != null) statusView.setText(text == null ? "" : text);
    }

    private void toast(String text) { Toast.makeText(this, text, Toast.LENGTH_SHORT).show(); }

    private void error(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title == null ? "Fehler" : title)
                .setMessage(message == null || message.isEmpty() ? "Unbekannter Fehler." : message)
                .setPositiveButton("OK", null)
                .show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
