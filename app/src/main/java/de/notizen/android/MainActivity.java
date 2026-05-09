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
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import de.notizen.android.core.RtfContentPart;
import de.notizen.android.core.RtfField;
import de.notizen.android.core.RtfHyperlink;
import de.notizen.android.core.RtfImagePart;
import de.notizen.android.core.RtfObject;
import de.notizen.android.core.RtfTextStyle;
import de.notizen.android.core.RtfUtils;
import de.notizen.android.core.Search;
import de.notizen.android.core.SearchResult;
import de.notizen.android.core.SimpleFtpClient;
import de.notizen.android.core.TreeStats;

public final class MainActivity extends Activity {
    private static final String APP_DISPLAY_NAME = "Notizen Java Android Nativ";
    private static final String APP_VERSION_NAME = "1.0.101-java-android-nativ";
    private static final String RTF_IMAGE_CHAR = "\ufffc";
    private static final String NODE_TITLE_STYLE_ATTR = "androidTitleStyle";
    private static final String NODE_TITLE_FONT_ATTR = "androidTitleFont";
    private static final String NODE_TITLE_SIZE_ATTR = "androidTitleSizeSp";

    private static final int TOOLBAR_BUTTON_DP = 24;
    private static final int TOOLBAR_BUTTON_MARGIN_DP = 1;
    private static final int TREE_PANE_DEFAULT_DP = 280;
    private static final int TREE_PANE_MIN_DP = 24;
    private static final int EDITOR_PANE_MIN_DP = 56;
    private static final int CONTENT_HEADER_DP = 34;
    private static final long RUNTIME_SNAPSHOT_DELAY_MS = 700L;
    private static final String RUNTIME_SNAPSHOT_FILE = "notizen.runtime-snapshot.xml";
    private static final String RUNTIME_SNAPSHOT_META_FILE = "notizen.runtime-snapshot.meta";

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
    private WebView pendingPrintView;
    private Runnable autosaveRunnable;
    private Runnable runtimeSnapshotRunnable;
    private Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
    private boolean autosaveInFlight = false;
    private boolean runtimeSnapshotRestored = false;

    private boolean loadingEditor = false;
    private boolean editorDirty = false;
    private boolean titleDirty = false;
    private boolean editorHorizontallyScrolling = false;
    private int treePaneWidthDp = TREE_PANE_DEFAULT_DP;
    private FormatTarget lastFormatTarget = FormatTarget.RTF_EDITOR;
    private LinearLayout contentLayout;
    private LinearLayout treePane;
    private LinearLayout editorPane;
    private View paneDivider;

    private interface PasswordCallback { void onPassword(String password); }
    private interface ContinueCallback { void run(); }
    private interface BackgroundCallback<T> { T run() throws Exception; }
    private interface UiCallback<T> { void run(T value); }

    private enum FormatTarget { RTF_EDITOR, TREE_NODE }

    private static final class RetainedState {
        NoteDocument document;
        NoteNode currentNode;
        Uri currentUri;
        File currentRawFile;
        String currentDisplayName;
        NoteNode internalClipboardNode;
        LegacySettings settings;
        FtpTarget currentFtpTarget;
        boolean editorHorizontallyScrolling;
        int selectionStart;
        int selectionEnd;
        int treePaneWidthDp;
        FormatTarget lastFormatTarget;
    }

    private static final class RtfTypefaceSpan extends TypefaceSpan {
        final String familyName;
        RtfTypefaceSpan(String family) {
            super(androidTypefaceFamily(family));
            familyName = LegacyRichTextToolbar.normalizeFontFamily(family);
        }
    }

    private static final class RtfImageSpan extends ImageSpan {
        final String rawRtf;
        final String mimeType;
        final byte[] imageData;
        final int widthTwips;
        final int heightTwips;
        RtfImageSpan(Drawable drawable, String rawRtf, String mimeType, byte[] imageData, int widthTwips, int heightTwips) {
            super(drawable, "notizen-rtf-image", ImageSpan.ALIGN_BOTTOM);
            this.rawRtf = rawRtf == null ? "" : rawRtf;
            this.mimeType = mimeType == null ? "" : mimeType;
            this.imageData = imageData == null ? new byte[0] : imageData;
            this.widthTwips = Math.max(0, widthTwips);
            this.heightTwips = Math.max(0, heightTwips);
        }
    }

    private static final class RtfRawSpan {
        final String rawRtf;
        final String kind;
        RtfRawSpan(String rawRtf, String kind) {
            this.rawRtf = rawRtf == null ? "" : rawRtf;
            this.kind = kind == null ? "" : kind;
        }
    }

    private static String androidTypefaceFamily(String family) {
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family).toLowerCase(Locale.ROOT);
        if (clean.contains("courier") || clean.contains("consolas") || clean.contains("mono")) return "monospace";
        if (clean.contains("times") || clean.contains("georgia") || clean.contains("serif")) return "serif";
        return "sans-serif";
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        RetainedState retained = null;
        Object last = getLastNonConfigurationInstance();
        if (last instanceof RetainedState) retained = (RetainedState) last;
        boolean hasOpenIntent = isOpenDocumentIntent(getIntent());
        if (retained != null) {
            document = retained.document == null ? NoteDocument.newDocument() : retained.document;
            currentNode = retained.currentNode;
            currentUri = retained.currentUri;
            currentRawFile = retained.currentRawFile;
            currentDisplayName = retained.currentDisplayName == null ? "unbenannt.alx" : retained.currentDisplayName;
            internalClipboardNode = retained.internalClipboardNode;
            currentFtpTarget = retained.currentFtpTarget;
            settings = retained.settings == null ? AndroidSettingsStore.load(this) : retained.settings;
            editorHorizontallyScrolling = retained.editorHorizontallyScrolling;
            lastFormatTarget = retained.lastFormatTarget == null ? FormatTarget.RTF_EDITOR : retained.lastFormatTarget;
            treePaneWidthDp = retained.treePaneWidthDp > 0 ? LegacySettings.normalizeAndroidTreePaneWidthDp(retained.treePaneWidthDp) : LegacySettings.normalizeAndroidTreePaneWidthDp(settings.androidTreePaneWidthDp);
        } else {
            settings = AndroidSettingsStore.load(this);
            treePaneWidthDp = LegacySettings.normalizeAndroidTreePaneWidthDp(settings.androidTreePaneWidthDp);
            if (!hasOpenIntent) runtimeSnapshotRestored = restoreRuntimeSnapshotIfPresent();
        }
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        installCrashSnapshotHandler();
        buildUi();
        requestNotificationPermissionIfUseful();
        if (retained != null) {
            NoteNode restored = currentNode == null ? document.ensureRoot() : currentNode;
            selectNode(restored, false);
            if (editor != null) {
                int start = Math.max(0, Math.min(retained.selectionStart, editor.getText().length()));
                int end = Math.max(start, Math.min(retained.selectionEnd, editor.getText().length()));
                editor.setSelection(start, end);
            }
            updateTitle();
        } else {
            selectNode(currentNode == null ? document.ensureRoot() : currentNode, false);
            boolean openedByIntent = handleViewIntent(getIntent());
            if (!openedByIntent) consumeOpenOnceFileIfPresent();
        }
        scheduleAutosaveTick();
        scheduleRuntimeSnapshotSave();
    }

    @Override public Object onRetainNonConfigurationInstance() {
        saveCurrentEditorToNode();
        saveRuntimeSnapshotNow(false);
        RetainedState retained = new RetainedState();
        retained.document = document;
        retained.currentNode = currentNode;
        retained.currentUri = currentUri;
        retained.currentRawFile = currentRawFile;
        retained.currentDisplayName = currentDisplayName;
        retained.internalClipboardNode = internalClipboardNode;
        retained.settings = settings;
        retained.currentFtpTarget = currentFtpTarget;
        retained.editorHorizontallyScrolling = editorHorizontallyScrolling;
        retained.selectionStart = editor == null ? 0 : Math.max(0, editor.getSelectionStart());
        retained.selectionEnd = editor == null ? retained.selectionStart : Math.max(retained.selectionStart, editor.getSelectionEnd());
        retained.treePaneWidthDp = currentTreePaneWidthDp();
        retained.lastFormatTarget = lastFormatTarget;
        return retained;
    }

    @Override protected void onPause() {
        saveRuntimeSnapshotNow(true);
        super.onPause();
    }

    @Override protected void onStop() {
        saveRuntimeSnapshotNow(true);
        super.onStop();
    }

    @Override protected void onDestroy() {
        saveRuntimeSnapshotNow(true);
        if (mainHandler != null && autosaveRunnable != null) mainHandler.removeCallbacks(autosaveRunnable);
        if (mainHandler != null && runtimeSnapshotRunnable != null) mainHandler.removeCallbacks(runtimeSnapshotRunnable);
        if (previousUncaughtExceptionHandler != null) Thread.setDefaultUncaughtExceptionHandler(previousUncaughtExceptionHandler);
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

        LinearLayout toolbarPanel = new LinearLayout(this);
        toolbarPanel.setOrientation(LinearLayout.VERTICAL);
        toolbarPanel.setPadding(dp(4), dp(1), dp(4), dp(2));
        toolbarPanel.setBackgroundColor(Color.rgb(246, 248, 252));
        root.addView(toolbarPanel, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout fileToolbar = addToolbarRow(toolbarPanel, "Datei");
        LinearLayout treeToolbar = addToolbarRow(toolbarPanel, "Baum");
        LinearLayout textToolbar = addToolbarRow(toolbarPanel, "Text");

        addButton(fileToolbar, "Leer", v -> confirmDiscardThen(this::newEmptyDocument));
        addButton(fileToolbar, "Neu", v -> confirmDiscardThen(this::newDocument));
        addButton(fileToolbar, "Öffnen", v -> confirmDiscardThen(this::openDocument));
        addButton(fileToolbar, "Letzte", v -> showRecentFiles());
        addButton(fileToolbar, "Speichern", v -> saveDocument());
        addButton(fileToolbar, "Speichern unter", v -> saveDocumentAs());
        addButton(fileToolbar, "Sicherungen", v -> showBackups());
        addButton(fileToolbar, "Einstellungen", v -> showSettingsDialog());
        addButton(fileToolbar, "Config", v -> showConfigSnapshot());
        addButton(fileToolbar, "FTP öffnen", v -> showFtpDialog(false));
        addButton(fileToolbar, "FTP speichern", v -> showFtpDialog(true));
        addButton(fileToolbar, "Schließen", v -> closeDocumentLegacy());
        addButton(fileToolbar, "Status", v -> showLifecycleStatus());
        addButton(fileToolbar, "Vorschau", v -> showRichPreview());
        addButton(fileToolbar, "Drucken", v -> showPrintDialog());
        addButton(fileToolbar, "Suche", v -> showSearchDialog());
        addButton(fileToolbar, "Export TXT", v -> exportText());
        addButton(fileToolbar, "Export ANSI", v -> exportTextAnsi());
        addButton(fileToolbar, "Export Unicode", v -> exportTextUnicode());
        addButton(fileToolbar, "Export HTML", v -> exportHtml());
        addButton(fileToolbar, "Export RTF", v -> exportRtf());
        addButton(fileToolbar, "Knoten TXT", v -> exportCurrentNodeText());
        addButton(fileToolbar, "Knoten RTF", v -> exportCurrentNodeRtf());
        addButton(fileToolbar, "Teilbaum", v -> createUnifiedCurrentNote());
        addButton(fileToolbar, "Gesamt", v -> createUnifiedRootNote());
        addButton(fileToolbar, "Passwort", v -> showPasswordChangeDialog());
        addButton(fileToolbar, "Statistik", v -> showStats());
        addButton(fileToolbar, "Validieren", v -> showValidationSummary());
        addButton(fileToolbar, "Diagnose", v -> showDiagnostics());
        addButton(fileToolbar, "Layout", v -> showLayoutDiagnostics());
        addButton(fileToolbar, "Launcher", v -> showRuntimeIdentity());
        addButton(fileToolbar, "Dialoge", v -> showActivationFocusModel());
        addButton(fileToolbar, "Toolbars", v -> showToolbarToggleModel());
        addButton(fileToolbar, "Fenster", v -> showWindowChromeModel());
        addButton(fileToolbar, "Autosave", v -> showAutosaveTickModel());
        addButton(fileToolbar, "Fontplan", v -> showLegacyFontSetModel());
        addButton(fileToolbar, "Maus", v -> showWindowMoveResizeModel());
        addButton(fileToolbar, "ALX Pipe", v -> showAlxPipelineModel());
        addButton(fileToolbar, "Buildplan", v -> showApkBuildPlan());
        addButton(fileToolbar, "Feedback", v -> showFeedbackDialog());
        addButton(fileToolbar, "Info", v -> showInfo());

        addButton(treeToolbar, "Kind", v -> newChild());
        addButton(treeToolbar, "Daneben", v -> newNext());
        addButton(treeToolbar, "Auf/Zu", v -> toggleExpanded());
        addButton(treeToolbar, "Alle auf", v -> expandAllNodes());
        addButton(treeToolbar, "Alle zu", v -> collapseAllNodes());
        addButton(treeToolbar, "Löschen", v -> deleteCurrent());
        addButton(treeToolbar, "Kopieren", v -> copyCurrentNode(false));
        addButton(treeToolbar, "Ausschneiden", v -> cutCurrentNode());
        addButton(treeToolbar, "Einfügen", v -> pasteNode());
        addButton(treeToolbar, "Rauf", v -> moveCurrentUp());
        addButton(treeToolbar, "Runter", v -> moveCurrentDown());
        addButton(treeToolbar, "Einrücken", v -> indentCurrent());
        addButton(treeToolbar, "Ausrücken", v -> outdentCurrent());
        addButton(treeToolbar, "Vor Ziel", v -> showMoveBeforeTargetDialog());
        addButton(treeToolbar, "Haftnotiz", v -> showDesktopNoteDialog());
        addButton(treeToolbar, "Haft Layout", v -> showDesktopNoteLayout());
        addButton(treeToolbar, "Haftliste", v -> showDesktopNoteTrayList());
        addButton(treeToolbar, "Haft weg", v -> clearDesktopNotesInSubtree());
        addButton(treeToolbar, "Wecker", v -> showAlarmDialog());

        addButton(textToolbar, "Datum", v -> insertDateForActiveTarget());
        addButton(textToolbar, "Punkt", v -> insertLegacyBulletForActiveTarget());
        addButton(textToolbar, "Normal", v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction("format_regular")));
        addButton(textToolbar, "Fett", v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction("format_bold")));
        addButton(textToolbar, "Kursiv", v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction("format_italic")));
        addButton(textToolbar, "Unterstrichen", v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction("format_underline")));
        addButton(textToolbar, "Größer", v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction("font_bigger")));
        addButton(textToolbar, "Kleiner", v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction("font_smaller")));
        addButton(textToolbar, "Schriftart", v -> showFontFamilyDialog());
        addButton(textToolbar, "Größe", v -> showFontSizeDialog());
        addButton(textToolbar, "RTF Format", v -> showRtfFormatDialog());
        addButton(textToolbar, "Scroll", v -> cycleScrollbars());
        addButton(textToolbar, "Bild", v -> insertImage());
        addButton(textToolbar, "RTF Info", v -> showRtfInfoDialog());
        addButton(textToolbar, "HTML Import", v -> importHtmlNote());
        addButton(textToolbar, "TXT Import", v -> importTextIntoCurrent());
        addButton(textToolbar, "RTF Import", v -> importRtfIntoCurrent());
        addButton(textToolbar, "Farben", v -> showColorDialog());

        LinearLayout content = new LinearLayout(this);
        contentLayout = content;
        int widthDp = getResources().getConfiguration().screenWidthDp;
        boolean wide = widthDp >= 700 || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        content.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        content.setPadding(dp(4), 0, dp(4), dp(4));

        LinearLayout left = new LinearLayout(this);
        treePane = left;
        left.setOrientation(LinearLayout.VERTICAL);
        left.setPadding(0, 0, 0, wide ? 0 : dp(5));
        rootTitleView = new TextView(this);
        rootTitleView.setSingleLine(true);
        rootTitleView.setIncludeFontPadding(false);
        rootTitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        rootTitleView.setBackground(roundedBackground(Color.rgb(255, 250, 205), Color.rgb(226, 213, 145), 8));
        rootTitleView.setTextColor(Color.rgb(30, 30, 30));
        rootTitleView.setPadding(dp(7), 0, dp(7), 0);
        rootTitleView.setTextSize(14f);
        left.addView(rootTitleView, new LinearLayout.LayoutParams(-1, dp(CONTENT_HEADER_DP)));
        treeList = new ListView(this);
        treeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        treeList.setFocusable(true);
        treeList.setFocusableInTouchMode(true);
        treeList.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setFormatTarget(FormatTarget.TREE_NODE); });
        treeList.setBackground(roundedBackground(Color.WHITE, Color.rgb(220, 225, 232), 8));
        treeList.setDividerHeight(1);
        treeAdapter = new TreeListAdapter(this);
        treeList.setAdapter(treeAdapter);
        final float[] lastTreeTouchX = new float[]{-1f};
        treeList.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastTreeTouchX[0] = event.getX();
                setFormatTarget(FormatTarget.TREE_NODE);
            }
            return false;
        });
        treeList.setOnItemClickListener((parent, view, position, id) -> {
            TreeListAdapter.FlatNode row = treeAdapter.getItem(position);
            NoteNode node = row.node;
            int toggleEdge = dp(42 + row.depth * 22);
            if (!node.children.isEmpty() && lastTreeTouchX[0] >= 0f && lastTreeTouchX[0] <= toggleEdge) {
                setFormatTarget(FormatTarget.TREE_NODE);
                toggleNodeExpanded(node, true);
            } else {
                setFormatTarget(FormatTarget.TREE_NODE);
                treeList.requestFocus();
                selectNode(node, false);
            }
        });
        treeList.setOnItemLongClickListener((parent, view, position, id) -> {
            toggleNodeExpanded(treeAdapter.getItem(position).node, true);
            return true;
        });
        left.addView(treeList, new LinearLayout.LayoutParams(-1, wide ? -1 : dp(210), 1));
        if (wide) {
            int initialTreePx = dp(normalizedInitialTreePaneWidthDp(widthDp));
            content.addView(left, new LinearLayout.LayoutParams(initialTreePx, -1));
            paneDivider = createPaneDivider();
            content.addView(paneDivider, new LinearLayout.LayoutParams(dp(16), -1));
        } else {
            paneDivider = null;
            content.addView(left, new LinearLayout.LayoutParams(-1, -2));
        }

        LinearLayout right = new LinearLayout(this);
        editorPane = right;
        right.setOrientation(LinearLayout.VERTICAL);
        titleEdit = new EditText(this);
        titleEdit.setSingleLine(true);
        titleEdit.setIncludeFontPadding(false);
        titleEdit.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        titleEdit.setMinHeight(0);
        titleEdit.setMinimumHeight(0);
        titleEdit.setTextSize(15f);
        titleEdit.setBackground(roundedBackground(Color.rgb(255, 250, 205), Color.rgb(226, 213, 145), 8));
        titleEdit.setPadding(dp(7), 0, dp(7), 0);
        titleEdit.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setFormatTarget(FormatTarget.TREE_NODE); });
        titleEdit.setOnTouchListener((view, event) -> { if (event.getActionMasked() == MotionEvent.ACTION_DOWN) setFormatTarget(FormatTarget.TREE_NODE); return false; });
        titleEdit.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (loadingEditor || currentNode == null) return;
                currentNode.title = s.toString().isEmpty() ? "..." : s.toString();
                titleDirty = true;
                markDocumentChanged();
                treeAdapter.notifyDataSetChanged();
                updateTitle();
            }
        });
        right.addView(titleEdit, new LinearLayout.LayoutParams(-1, dp(CONTENT_HEADER_DP)));

        editor = new EditText(this);
        editor.setTextSize(17f);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setMinLines(12);
        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(false);
        editor.setBackground(roundedBackground(Color.WHITE, Color.rgb(213, 219, 229), 8));
        applyEditorScrollbars();
        editor.setPadding(dp(10), dp(10), dp(10), dp(10));
        editor.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setFormatTarget(FormatTarget.RTF_EDITOR); });
        editor.setOnTouchListener((view, event) -> { if (event.getActionMasked() == MotionEvent.ACTION_DOWN) setFormatTarget(FormatTarget.RTF_EDITOR); return false; });
        editor.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable s) {
                if (loadingEditor) return;
                editorDirty = true;
                markDocumentChanged();
                updateTitle();
            }
        });
        right.addView(editor, new LinearLayout.LayoutParams(-1, 0, 1));
        content.addView(right, wide ? new LinearLayout.LayoutParams(0, -1, 1.0f) : new LinearLayout.LayoutParams(-1, 0, 1));
        if (wide) content.post(() -> applyTreePaneWidthPx(dp(treePaneWidthDp), false));
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        updateTitle();
        rebuildTree();
    }

    private boolean isOpenDocumentIntent(Intent intent) {
        return intent != null && intent.getData() != null && Intent.ACTION_VIEW.equals(intent.getAction());
    }

    private void installCrashSnapshotHandler() {
        if (previousUncaughtExceptionHandler != null) return;
        previousUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try { saveRuntimeSnapshotNow(Looper.myLooper() == Looper.getMainLooper()); } catch (Throwable ignored) {}
            if (previousUncaughtExceptionHandler != null) previousUncaughtExceptionHandler.uncaughtException(thread, throwable);
        });
    }

    private void scheduleRuntimeSnapshotSave() {
        if (mainHandler == null) return;
        if (runtimeSnapshotRunnable != null) mainHandler.removeCallbacks(runtimeSnapshotRunnable);
        runtimeSnapshotRunnable = () -> {
            runtimeSnapshotRunnable = null;
            saveRuntimeSnapshotNow(true);
        };
        mainHandler.postDelayed(runtimeSnapshotRunnable, RUNTIME_SNAPSHOT_DELAY_MS);
    }

    private void saveRuntimeSnapshotNow(boolean captureEditor) {
        try {
            if (captureEditor && Looper.myLooper() == Looper.getMainLooper()) saveCurrentEditorToNode();
            if (!hasRecoverableRuntimeContent(document)) return;
            if (wouldOverwriteUsefulRuntimeSnapshotWithBlankDocument()) return;
            writeAtomic(runtimeSnapshotFile(), AlxIo.documentToXmlBytes(document));
            writeAtomic(runtimeSnapshotMetaFile(), runtimeSnapshotMetaText().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            // Runtime snapshots must never interrupt editing, saving or shutdown.
        }
    }

    private boolean wouldOverwriteUsefulRuntimeSnapshotWithBlankDocument() {
        try {
            if (!isBlankSingleNodeDocumentForPrompt(document)) return false;
            File file = runtimeSnapshotFile();
            if (!file.isFile() || file.length() <= 0L) return false;
            NoteDocument old = AlxIo.load(AndroidSettingsStore.readFile(file), "");
            return old != null && !isBlankSingleNodeDocumentForPrompt(old) && hasRecoverableRuntimeContent(old);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean restoreRuntimeSnapshotIfPresent() {
        try {
            File file = runtimeSnapshotFile();
            if (!file.isFile() || file.length() <= 0L) return false;
            NoteDocument loaded = AlxIo.load(AndroidSettingsStore.readFile(file), "");
            if (!hasRecoverableRuntimeContent(loaded)) return false;
            document = loaded;
            String meta = "";
            try { meta = new String(AndroidSettingsStore.readFile(runtimeSnapshotMetaFile()), StandardCharsets.UTF_8); } catch (Exception ignored) {}
            currentUri = null;
            currentRawFile = null;
            currentFtpTarget = null;
            currentDisplayName = metaValue(meta, "displayName", "Wiederhergestellt.alx");
            if (currentDisplayName == null || currentDisplayName.trim().isEmpty()) currentDisplayName = "Wiederhergestellt.alx";
            document.displayName = currentDisplayName;
            String rawPath = metaValue(meta, "rawFile", "");
            if (rawPath != null && !rawPath.trim().isEmpty()) {
                File raw = new File(rawPath.trim());
                if (raw.isFile()) currentRawFile = raw;
            }
            String uriText = metaValue(meta, "uri", "");
            if (currentRawFile == null && uriText != null && !uriText.trim().isEmpty()) {
                try { currentUri = Uri.parse(uriText.trim()); } catch (Exception ignored) {}
            }
            boolean wasChanged = Boolean.parseBoolean(metaValue(meta, "changed", "true"));
            document.changed = wasChanged || (currentUri == null && currentRawFile == null && currentFtpTarget == null);
            currentNode = nodeByIndexPath(document.ensureRoot(), metaValue(meta, "selectedPath", ""));
            if (currentNode == null) currentNode = document.ensureRoot();
            editorDirty = false;
            titleDirty = false;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private File runtimeSnapshotFile() {
        return new File(getFilesDir(), RUNTIME_SNAPSHOT_FILE);
    }

    private File runtimeSnapshotMetaFile() {
        return new File(getFilesDir(), RUNTIME_SNAPSHOT_META_FILE);
    }

    private void writeAtomic(File file, byte[] data) throws Exception {
        if (file == null) return;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        File tmp = new File(file.getParentFile() == null ? getFilesDir() : file.getParentFile(), file.getName() + ".tmp");
        FileOutputStream out = new FileOutputStream(tmp, false);
        out.write(data == null ? new byte[0] : data);
        out.getFD().sync();
        out.close();
        if (!tmp.renameTo(file)) {
            FileOutputStream fallback = new FileOutputStream(file, false);
            fallback.write(data == null ? new byte[0] : data);
            fallback.close();
            tmp.delete();
        }
    }

    private String runtimeSnapshotMetaText() {
        StringBuilder out = new StringBuilder();
        out.append("displayName=").append(escapeMeta(currentDisplayName)).append('\n');
        out.append("selectedPath=").append(escapeMeta(nodeIndexPath(currentNode))).append('\n');
        out.append("changed=").append(document != null && document.changed ? "true" : "false").append('\n');
        out.append("rawFile=").append(escapeMeta(currentRawFile == null ? "" : currentRawFile.getAbsolutePath())).append('\n');
        out.append("uri=").append(escapeMeta(currentUri == null ? "" : currentUri.toString())).append('\n');
        out.append("ftp=").append(escapeMeta(currentFtpTarget == null ? "" : currentFtpTarget.safeDisplayUrl())).append('\n');
        out.append("restored=").append(runtimeSnapshotRestored ? "true" : "false").append('\n');
        out.append("time=").append(System.currentTimeMillis()).append('\n');
        return out.toString();
    }

    private String escapeMeta(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String unescapeMeta(String value) {
        if (value == null || value.isEmpty()) return "";
        StringBuilder out = new StringBuilder(value.length());
        boolean slash = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (slash) {
                if (c == 'n') out.append('\n');
                else if (c == 'r') out.append('\r');
                else out.append(c);
                slash = false;
            } else if (c == '\\') {
                slash = true;
            } else {
                out.append(c);
            }
        }
        if (slash) out.append('\\');
        return out.toString();
    }

    private String metaValue(String meta, String key, String fallback) {
        if (meta == null || key == null) return fallback;
        String prefix = key + "=";
        String[] lines = meta.split("\n");
        for (String line : lines) if (line.startsWith(prefix)) return unescapeMeta(line.substring(prefix.length()));
        return fallback;
    }

    private String nodeIndexPath(NoteNode node) {
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

    private NoteNode nodeByIndexPath(NoteNode root, String path) {
        if (root == null) return null;
        String clean = path == null ? "" : path.trim();
        if (clean.isEmpty()) return root;
        NoteNode n = root;
        String[] parts = clean.split("\\.");
        for (String part : parts) {
            if (part.isEmpty()) continue;
            int index;
            try { index = Integer.parseInt(part); } catch (Exception e) { return root; }
            if (index < 0 || index >= n.children.size()) return root;
            n = n.children.get(index);
        }
        return n;
    }

    private boolean hasRecoverableRuntimeContent(NoteDocument doc) {
        if (doc == null || doc.root == null) return false;
        if (currentUri != null || currentRawFile != null || currentFtpTarget != null) return true;
        for (NoteNode node : doc.walk()) {
            if (node == null) continue;
            if (!isDefaultTitle(node.title)) return true;
            if (rtfHasVisibleText(node.rtf)) return true;
            if (node.bgArgb != 0 || node.fgArgb != 0 || node.desktopNote != null) return true;
            if (!node.extraAttrs.isEmpty() || !node.extraChildXml.isEmpty()) return true;
        }
        return doc.walk().size() > 1 || !doc.rootAttrs.isEmpty() || !doc.extraRootXml.isEmpty();
    }

    private boolean isBlankSingleNodeDocumentForPrompt(NoteDocument doc) {
        if (doc == null || doc.root == null) return true;
        List<NoteNode> nodes = doc.walk();
        if (nodes.size() > 1) return false;
        for (NoteNode node : nodes) if (rtfHasVisibleText(node.rtf)) return false;
        return true;
    }

    private boolean rtfHasVisibleText(String rtf) {
        String plain = RtfUtils.rtfToPlainText(rtf == null ? "" : rtf);
        if (plain != null && !plain.trim().isEmpty()) return true;
        return rtf != null && (rtf.contains("\\pict") || rtf.contains("\\object") || rtf.contains(RtfUtils.LEGACY_IMAGE_PLACEHOLDER));
    }

    private boolean isDefaultTitle(String title) {
        String t = title == null ? "" : title.trim();
        return t.isEmpty() || "start".equalsIgnoreCase(t) || "...".equals(t);
    }

    private void clearRuntimeSnapshotIfBlankStart() {
        try {
            File snapshot = runtimeSnapshotFile();
            File meta = runtimeSnapshotMetaFile();
            if (snapshot.isFile()) snapshot.delete();
            if (meta.isFile()) meta.delete();
        } catch (Exception ignored) {
        }
    }

    private void requestNotificationPermissionIfUseful() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 2001);
        }
    }

    private LinearLayout addToolbarRow(LinearLayout parent, String label) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(1), 0, dp(1));

        TextView caption = new TextView(this);
        caption.setText(toolbarRowGlyph(label));
        caption.setGravity(Gravity.CENTER);
        caption.setTextSize(12f);
        caption.setTypeface(Typeface.DEFAULT_BOLD);
        caption.setTextColor(Color.rgb(72, 84, 102));
        caption.setContentDescription(label + "-Leiste");
        if (Build.VERSION.SDK_INT >= 26) caption.setTooltipText(label + "-Leiste");
        caption.setBackground(roundedBackground(Color.rgb(238, 242, 248), Color.rgb(210, 218, 230), 8));
        LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(dp(TOOLBAR_BUTTON_DP), dp(TOOLBAR_BUTTON_DP));
        captionParams.setMargins(dp(TOOLBAR_BUTTON_MARGIN_DP), dp(1), dp(3), dp(1));
        row.addView(caption, captionParams);

        scroll.addView(row, new HorizontalScrollView.LayoutParams(-2, -2));
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }

    private void addButton(LinearLayout toolbar, String label, View.OnClickListener listener) {
        LegacyToolbarPresentation.ButtonSpec spec = LegacyToolbarPresentation.forLabel(label);
        TextView b = new TextView(this);
        b.setText(spec.displayText(true));
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setIncludeFontPadding(false);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextSize(toolbarGlyphTextSize(spec.displayText(true)));
        b.setTextColor(Color.rgb(30, 42, 58));
        b.setPadding(0, 0, 0, dp(1));
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setBackground(toolbarButtonBackground());
        if (Build.VERSION.SDK_INT >= 21) b.setElevation(dp(1));
        b.setClickable(true);
        b.setFocusable(true);
        b.setContentDescription(spec.contentDescription());
        if (Build.VERSION.SDK_INT >= 26) b.setTooltipText(spec.contentDescription());
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(TOOLBAR_BUTTON_DP), dp(TOOLBAR_BUTTON_DP));
        params.setMargins(dp(TOOLBAR_BUTTON_MARGIN_DP), dp(1), dp(TOOLBAR_BUTTON_MARGIN_DP), dp(1));
        toolbar.addView(b, params);
    }

    private float toolbarGlyphTextSize(String glyph) {
        int len = glyph == null ? 0 : glyph.codePointCount(0, glyph.length());
        if (len >= 5) return 5.5f;
        if (len == 4) return 6.5f;
        if (len == 3) return 7.5f;
        if (len == 2) return 9.5f;
        return 14.5f;
    }

    private String toolbarRowGlyph(String label) {
        if ("Datei".equals(label)) return "≡";
        if ("Baum".equals(label)) return "▦";
        if ("Text".equals(label)) return "A";
        return label == null || label.isEmpty() ? "•" : label.substring(0, 1);
    }

    private Drawable toolbarButtonBackground() {
        GradientDrawable base = new GradientDrawable();
        base.setShape(GradientDrawable.RECTANGLE);
        base.setColor(Color.rgb(255, 255, 255));
        base.setCornerRadius(dp(7));
        base.setStroke(dp(1), Color.rgb(198, 208, 222));

        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.RECTANGLE);
        mask.setColor(Color.WHITE);
        mask.setCornerRadius(dp(7));
        return new RippleDrawable(ColorStateList.valueOf(Color.rgb(198, 219, 255)), base, mask);
    }

    private Drawable roundedBackground(int fillColor, int strokeColor, int radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setColor(fillColor);
        bg.setCornerRadius(dp(radiusDp));
        bg.setStroke(dp(1), strokeColor);
        return bg;
    }

    private View createPaneDivider() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setContentDescription("Trenner: mit dem Finger ziehen, um Baum und RTF-Text breiter oder schmaler zu machen");
        if (Build.VERSION.SDK_INT >= 26) column.setTooltipText("Breite zwischen Baum und RTF ziehen");

        View yellowBridge = new View(this);
        yellowBridge.setBackground(roundedBackground(Color.rgb(255, 250, 205), Color.rgb(226, 213, 145), 8));
        column.addView(yellowBridge, new LinearLayout.LayoutParams(-1, dp(CONTENT_HEADER_DP)));

        TextView handle = new TextView(this);
        handle.setText("⋮");
        handle.setGravity(Gravity.CENTER);
        handle.setTextSize(26f);
        handle.setTextColor(Color.rgb(90, 104, 124));
        handle.setBackground(roundedBackground(Color.rgb(232, 236, 242), Color.rgb(205, 214, 226), 9));
        column.addView(handle, new LinearLayout.LayoutParams(-1, 0, 1));

        View.OnTouchListener dragListener = new View.OnTouchListener() {
            final float[] startRawX = new float[]{0f};
            final int[] startWidth = new int[]{0};
            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startRawX[0] = event.getRawX();
                        startWidth[0] = treePane == null ? dp(treePaneWidthDp) : treePane.getWidth();
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        applyTreePaneWidthPx(startWidth[0] + Math.round(event.getRawX() - startRawX[0]), false);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        applyTreePaneWidthPx(startWidth[0] + Math.round(event.getRawX() - startRawX[0]), true);
                        status("Baumbreite: " + treePaneWidthDp + " dp");
                        return true;
                    default:
                        return true;
                }
            }
        };
        column.setOnTouchListener(dragListener);
        handle.setOnTouchListener(dragListener);
        return column;
    }

    private int normalizedInitialTreePaneWidthDp(int screenWidthDp) {
        int base = LegacySettings.normalizeAndroidTreePaneWidthDp(treePaneWidthDp);
        int max = Math.max(TREE_PANE_MIN_DP, screenWidthDp - EDITOR_PANE_MIN_DP - 36);
        return Math.max(TREE_PANE_MIN_DP, Math.min(base, max));
    }

    private int currentTreePaneWidthDp() {
        if (treePane != null && treePane.getWidth() > 0) {
            return LegacySettings.normalizeAndroidTreePaneWidthDp(Math.round(treePane.getWidth() / getResources().getDisplayMetrics().density));
        }
        return LegacySettings.normalizeAndroidTreePaneWidthDp(treePaneWidthDp);
    }

    private void applyTreePaneWidthPx(int requestedPx, boolean persist) {
        if (treePane == null || contentLayout == null) return;
        int total = contentLayout.getWidth();
        if (total <= 0) total = getResources().getDisplayMetrics().widthPixels - dp(12);
        int dividerWidth = paneDivider == null || paneDivider.getWidth() <= 0 ? dp(16) : paneDivider.getWidth();
        int minTree = dp(TREE_PANE_MIN_DP);
        int minEditor = dp(EDITOR_PANE_MIN_DP);
        int maxTree = Math.max(minTree, total - dividerWidth - minEditor);
        int clamped = Math.max(minTree, Math.min(requestedPx, maxTree));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) treePane.getLayoutParams();
        params.width = clamped;
        params.weight = 0f;
        treePane.setLayoutParams(params);
        treePaneWidthDp = LegacySettings.normalizeAndroidTreePaneWidthDp(Math.round(clamped / getResources().getDisplayMetrics().density));
        if (persist) saveTreePaneWidthSetting();
    }

    private void saveTreePaneWidthSetting() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        settings.androidTreePaneWidthDp = LegacySettings.normalizeAndroidTreePaneWidthDp(treePaneWidthDp);
        AndroidSettingsStore.save(this, settings);
    }

    private void setFormatTarget(FormatTarget target) {
        if (target != null) lastFormatTarget = target;
    }

    private boolean shouldFormatTreeTarget() {
        if (editor != null && editor.hasFocus()) return false;
        if (treeList != null && treeList.hasFocus()) return true;
        if (titleEdit != null && titleEdit.hasFocus()) return true;
        return lastFormatTarget == FormatTarget.TREE_NODE;
    }

    private void markDocumentChanged() {
        if (document != null) document.markChanged();
        if (!loadingEditor) scheduleRuntimeSnapshotSave();
    }

    private void toggleNodeExpanded(NoteNode node, boolean showMessage) {
        if (node == null || node.children.isEmpty()) return;
        node.expanded = !node.expanded;
        markDocumentChanged();
        rebuildTree();
        treeAdapter.setSelected(currentNode);
        if (showMessage) status(node.expanded ? "Knoten geöffnet" : "Knoten geschlossen");
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

    private void newEmptyDocument() {
        document = new NoteDocument();
        document.root = new NoteNode("...", "");
        document.changed = true;
        currentUri = null;
        currentRawFile = null;
        currentFtpTarget = null;
        currentDisplayName = "unbenannt.alx";
        runtimeSnapshotRestored = false;
        clearRuntimeSnapshotIfBlankStart();
        currentNode = null;
        editorDirty = false;
        titleDirty = false;
        setFormatTarget(FormatTarget.RTF_EDITOR);
        selectNode(document.ensureRoot(), false);
        status("Leere Datei");
    }

    private void newDocument() {
        document = NoteDocument.newDocument();
        currentUri = null;
        currentRawFile = null;
        currentFtpTarget = null;
        currentDisplayName = "unbenannt.alx";
        runtimeSnapshotRestored = false;
        clearRuntimeSnapshotIfBlankStart();
        currentNode = null;
        editorDirty = false;
        titleDirty = false;
        setFormatTarget(FormatTarget.RTF_EDITOR);
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
            saveRuntimeSnapshotNow(false);
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
            saveRuntimeSnapshotNow(false);
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
            finishLoadedDocument("Sicherung geöffnet: bitte mit 'Speichern unter' sichern");
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
            finishLoadedDocument("Geöffnet: " + file.getAbsolutePath());
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
            saveRuntimeSnapshotNow(false);
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
            finishLoadedDocument("FTP geöffnet: " + target.safeDisplayUrl());
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
                    saveRuntimeSnapshotNow(false);
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
        if (currentNode.children.isEmpty()) {
            status("Dieser Knoten hat keine Unterknoten");
            return;
        }
        toggleNodeExpanded(currentNode, true);
    }

    private void expandAllNodes() {
        if (document == null || document.root == null) return;
        int changed = LegacyTreeExpansion.expandAll(document.ensureRoot());
        if (changed > 0) markDocumentChanged();
        rebuildTree();
        if (currentNode != null) selectNode(currentNode, true);
        status("Alle Knoten geöffnet");
    }

    private void collapseAllNodes() {
        if (document == null || document.root == null) return;
        int changed = LegacyTreeExpansion.collapseAll(document.ensureRoot());
        if (changed > 0) markDocumentChanged();
        rebuildTree();
        selectNode(document.ensureRoot(), true);
        status("Alle Knoten geschlossen");
    }

    private void clearDesktopNotesInSubtree() {
        if (currentNode == null) return;
        LegacyDesktopNoteTreeOps.ClearResult result = LegacyDesktopNoteTreeOps.clearDesktopNotes(currentNode);
        if (result.clearedDesktopNotes > 0) {
            markDocumentChanged();
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
                    markDocumentChanged();
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
        markDocumentChanged();
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
        markDocumentChanged();
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
        markDocumentChanged();
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
        markDocumentChanged();
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
        markDocumentChanged();
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
        markDocumentChanged();
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
                    markDocumentChanged();
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

    private void insertDateForActiveTarget() {
        if (shouldFormatTreeTarget()) appendToCurrentNodeTitle(LegacyEditorActions.androidDateInsertText(new Date()).trim());
        else insertDate();
    }

    private void insertLegacyBulletForActiveTarget() {
        if (shouldFormatTreeTarget()) appendToCurrentNodeTitle("•");
        else insertLegacyBullet();
    }

    private void appendToCurrentNodeTitle(String value) {
        if (currentNode == null || value == null || value.isEmpty()) return;
        saveCurrentEditorToNode();
        String oldTitle = titleEdit == null ? currentNode.title : titleEdit.getText().toString();
        String base = oldTitle == null || oldTitle.trim().isEmpty() || "...".equals(oldTitle.trim()) ? "" : oldTitle.trim();
        currentNode.title = LegacyEditorNodeSync.normalizeTitle(base.isEmpty() ? value : base + " " + value);
        loadingEditor = true;
        if (titleEdit != null) {
            titleEdit.setText(currentNode.title);
            titleEdit.setSelection(titleEdit.getText().length());
        }
        loadingEditor = false;
        titleDirty = false;
        markDocumentChanged();
        rebuildTree();
        updateTitle();
    }

    private void applyActiveFormatAction(LegacyRichTextToolbar.ActionSpec spec) {
        if (shouldFormatTreeTarget()) applyTreeFormatAction(spec);
        else applyRtfFormatAction(spec);
    }

    private void applyTreeFormatAction(LegacyRichTextToolbar.ActionSpec spec) {
        if (spec == null || currentNode == null) return;
        String action = spec.action == null ? "" : spec.action;
        if ("legacy_bullet".equals(action)) { appendToCurrentNodeTitle("•"); return; }
        if ("cycle_scrollbars".equals(action)) { cycleScrollbars(); return; }
        if ("text_color".equals(action)) { showTreeTextColorPalette(); return; }
        if ("highlight_color".equals(action)) { showTreeBackgroundPalette(); return; }
        if ("font_family".equals(action)) { showTreeFontFamilyDialog(); return; }
        if ("font_size".equals(action)) { showTreeFontSizeDialog(); return; }
        if ("format_regular".equals(action)) { clearTreeTextFormatting(); return; }
        if ("format_bold".equals(action)) { addTreeTitleStyle("bold", "Baumtext fett"); return; }
        if ("format_italic".equals(action)) { addTreeTitleStyle("italic", "Baumtext kursiv"); return; }
        if ("format_underline".equals(action)) { addTreeTitleStyle("underline", "Baumtext unterstrichen"); return; }
        if ("format_strike".equals(action)) { addTreeTitleStyle("strike", "Baumtext durchgestrichen"); return; }
        if ("font_bigger".equals(action)) { applyTreeFontSize(LegacyRichTextToolbar.nextFontSize(currentTreeTitleSize(), +1)); return; }
        if ("font_smaller".equals(action)) { applyTreeFontSize(LegacyRichTextToolbar.nextFontSize(currentTreeTitleSize(), -1)); return; }
        if (action.startsWith("align_")) { toast("Ausrichtung betrifft nur die RTF-Box."); return; }
        status(spec.tooltip == null || spec.tooltip.isEmpty() ? "Baumformat-Aktion" : spec.tooltip);
    }

    private void addTreeTitleStyle(String style, String message) {
        if (currentNode == null || style == null || style.isEmpty()) return;
        String raw = currentNode.extraAttrs.get(NODE_TITLE_STYLE_ATTR);
        LinkedHashMap<String, Boolean> values = new LinkedHashMap<>();
        if (raw != null) {
            for (String part : raw.split(",")) {
                String clean = part.trim().toLowerCase(Locale.ROOT);
                if (!clean.isEmpty()) values.put(clean, Boolean.TRUE);
            }
        }
        values.put(style.toLowerCase(Locale.ROOT), Boolean.TRUE);
        StringBuilder out = new StringBuilder();
        for (String key : values.keySet()) {
            if (out.length() > 0) out.append(',');
            out.append(key);
        }
        currentNode.extraAttrs.put(NODE_TITLE_STYLE_ATTR, out.toString());
        markTreeTextFormatChanged(message);
    }

    private void clearTreeTextFormatting() {
        if (currentNode == null) return;
        currentNode.extraAttrs.remove(NODE_TITLE_STYLE_ATTR);
        currentNode.extraAttrs.remove(NODE_TITLE_FONT_ATTR);
        currentNode.extraAttrs.remove(NODE_TITLE_SIZE_ATTR);
        currentNode.fgArgb = 0;
        markTreeTextFormatChanged("Baumtext normal");
    }

    private void showTreeFontFamilyDialog() {
        if (currentNode == null) return;
        List<String> families = LegacyRichTextToolbar.legacyFontFamilies();
        String[] labels = families.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Baum-Schriftart")
                .setItems(labels, (d, which) -> applyTreeFontFamily(families.get(which)))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void applyTreeFontFamily(String family) {
        if (currentNode == null) return;
        currentNode.extraAttrs.put(NODE_TITLE_FONT_ATTR, LegacyRichTextToolbar.normalizeFontFamily(family));
        markTreeTextFormatChanged("Baum-Schriftart gesetzt");
    }

    private void showTreeFontSizeDialog() {
        if (currentNode == null) return;
        int[] sizes = LegacyRichTextToolbar.legacyFontSizeItems();
        String[] labels = new String[sizes.length + 1];
        for (int i = 0; i < sizes.length; i++) labels[i] = sizes[i] + " pt";
        labels[sizes.length] = "Eigene Größe…";
        new AlertDialog.Builder(this)
                .setTitle("Baum-Schriftgröße")
                .setItems(labels, (d, which) -> {
                    if (which >= sizes.length) showCustomTreeFontSizeDialog();
                    else applyTreeFontSize(sizes[which]);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showCustomTreeFontSizeDialog() {
        if (currentNode == null) return;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText value = labeledEdit(box, "Baum-Schriftgröße", Integer.toString(currentTreeTitleSize()));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Eigene Baum-Schriftgröße")
                .setView(box)
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            LegacyFontSizeEntry.State state = LegacyFontSizeEntry.onEnter(value.getText().toString(), Integer.toString(currentTreeTitleSize()));
            value.setText(state.text);
            value.setSelection(value.getText().length());
            if (!state.apply) {
                error("Baum-Schriftgröße", "Bitte eine Zahl zwischen 0 und 99 eingeben.");
                return;
            }
            dialog.dismiss();
            applyTreeFontSize(state.numericValue);
        }));
        dialog.show();
    }

    private int currentTreeTitleSize() {
        if (currentNode == null) return 16;
        try { return LegacyRichTextToolbar.normalizeFontSize(Integer.parseInt(String.valueOf(currentNode.extraAttrs.get(NODE_TITLE_SIZE_ATTR)).trim())); }
        catch (Exception ignored) { return 16; }
    }

    private void applyTreeFontSize(int size) {
        if (currentNode == null) return;
        int clamped = LegacyRichTextToolbar.normalizeFontSize(size);
        currentNode.extraAttrs.put(NODE_TITLE_SIZE_ATTR, Integer.toString(clamped));
        markTreeTextFormatChanged("Baum-Schriftgröße: " + clamped + " pt");
    }

    private void showTreeTextColorPalette() {
        String[] labels = new String[LegacyColors.LIGHT_COLOR_ARGB.length + 1];
        for (int i = 0; i < LegacyColors.LIGHT_COLOR_ARGB.length; i++) labels[i] = i + ": " + LegacyColors.LIGHT_COLOR_NAMES[i] + " " + LegacyColors.toCssRgb(LegacyColors.LIGHT_COLOR_ARGB[i]);
        labels[labels.length - 1] = "Baum-Textfarbe löschen";
        new AlertDialog.Builder(this)
                .setTitle("Baum-Textfarbe")
                .setItems(labels, (d, which) -> {
                    if (which >= LegacyColors.LIGHT_COLOR_ARGB.length) applyNodeColor(false, 0);
                    else applyNodeColor(false, LegacyColors.LIGHT_COLOR_ARGB[which]);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showTreeBackgroundPalette() {
        String[] labels = new String[LegacyColors.LIGHT_COLOR_ARGB.length + 1];
        for (int i = 0; i < LegacyColors.LIGHT_COLOR_ARGB.length; i++) labels[i] = i + ": " + LegacyColors.LIGHT_COLOR_NAMES[i] + " " + LegacyColors.toCssRgb(LegacyColors.LIGHT_COLOR_ARGB[i]);
        labels[labels.length - 1] = "Baum-Hintergrund löschen";
        new AlertDialog.Builder(this)
                .setTitle("Baum-Hintergrund")
                .setItems(labels, (d, which) -> {
                    if (which >= LegacyColors.LIGHT_COLOR_ARGB.length) applyNodeColor(true, 0);
                    else applyNodeColor(true, LegacyColors.LIGHT_COLOR_ARGB[which]);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void markTreeTextFormatChanged(String message) {
        markDocumentChanged();
        rebuildTree();
        if (treeAdapter != null) treeAdapter.setSelected(currentNode);
        updateTitle();
        status(message == null || message.isEmpty() ? "Baumtext formatiert" : message);
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
                .setItems(labels, (d, which) -> applyActiveFormatAction(actions.get(which)))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void applyRtfFormatAction(LegacyRichTextToolbar.ActionSpec spec) {
        if (spec == null || currentNode == null || editor == null) return;
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
        applyEditorFormatAction(spec.action, spec.tooltip);
    }

    private void applyEditorFormatAction(String action, String label) {
        if (editor == null) return;
        Spannable text = editor.getText();
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            status("Kein Text zum Formatieren");
            return;
        }
        String normalized = action == null ? "" : action;
        if ("format_regular".equals(normalized)) {
            removeCharacterFormatting(text, range[0], range[1]);
            markEditorRichChanged("Zeichenformat zurückgesetzt");
        } else if ("format_bold".equals(normalized)) {
            text.setSpan(new StyleSpan(Typeface.BOLD), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            markEditorRichChanged("Fett angewendet");
        } else if ("format_italic".equals(normalized)) {
            text.setSpan(new StyleSpan(Typeface.ITALIC), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            markEditorRichChanged("Kursiv angewendet");
        } else if ("format_underline".equals(normalized)) {
            text.setSpan(new UnderlineSpan(), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            markEditorRichChanged("Unterstrichen angewendet");
        } else if ("format_strike".equals(normalized)) {
            text.setSpan(new StrikethroughSpan(), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            markEditorRichChanged("Durchgestrichen angewendet");
        } else if ("font_bigger".equals(normalized)) {
            int next = LegacyRichTextToolbar.nextFontSize(currentSelectionPointSize(), +1);
            applyFontSizeToSelection(next);
        } else if ("font_smaller".equals(normalized)) {
            int next = LegacyRichTextToolbar.nextFontSize(currentSelectionPointSize(), -1);
            applyFontSizeToSelection(next);
        } else if (normalized.startsWith("align_")) {
            applyAlignmentToSelection(normalized);
        } else {
            status(label == null || label.isEmpty() ? "RTF-Aktion nicht umgesetzt" : label);
        }
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
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            toast("Kein Text zum Färben vorhanden");
            return;
        }
        Spannable text = editor.getText();
        try {
            int color = Color.parseColor(decision.css);
            if (decision.role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT) {
                removeOverlappingSpans(text, range[0], range[1], BackgroundColorSpan.class);
                text.setSpan(new BackgroundColorSpan(color), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markEditorRichChanged("RTF-Hervorhebung angewendet: " + decision.css);
            } else {
                removeOverlappingSpans(text, range[0], range[1], ForegroundColorSpan.class);
                text.setSpan(new ForegroundColorSpan(color), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markEditorRichChanged("RTF-Textfarbe angewendet: " + decision.css);
            }
        } catch (Exception e) {
            error("RTF-Farbe", e.getMessage());
        }
    }

    private void clearRtfColorFromSelection(boolean background) {
        if (currentNode == null || editor == null) return;
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            toast("Kein Text zum Färben vorhanden");
            return;
        }
        Spannable text = editor.getText();
        if (background) {
            removeOverlappingSpans(text, range[0], range[1], BackgroundColorSpan.class);
            markEditorRichChanged("RTF-Hintergrundfarbe entfernt");
        } else {
            removeOverlappingSpans(text, range[0], range[1], ForegroundColorSpan.class);
            markEditorRichChanged("RTF-Textfarbe entfernt");
        }
    }

    private void showFontFamilyDialog() {
        if (currentNode == null || editor == null) return;
        if (shouldFormatTreeTarget()) {
            showTreeFontFamilyDialog();
            return;
        }
        List<String> families = LegacyRichTextToolbar.legacyFontFamilies();
        String[] labels = families.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("RTF-Schriftart")
                .setItems(labels, (d, which) -> applyFontFamilyToSelection(families.get(which)))
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showFontSizeDialog() {
        if (currentNode == null || editor == null) return;
        if (shouldFormatTreeTarget()) {
            showTreeFontSizeDialog();
            return;
        }
        int[] sizes = LegacyRichTextToolbar.legacyFontSizeItems();
        String[] labels = new String[sizes.length + 1];
        for (int i = 0; i < sizes.length; i++) labels[i] = sizes[i] + " pt";
        labels[sizes.length] = "Eigene Größe…";
        new AlertDialog.Builder(this)
                .setTitle("RTF-Schriftgröße")
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

    private void applyFontFamilyToSelection(String family) {
        if (currentNode == null || editor == null) return;
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            status("Kein Text für Schriftart");
            return;
        }
        Spannable text = editor.getText();
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family);
        removeOverlappingSpans(text, range[0], range[1], RtfTypefaceSpan.class);
        text.setSpan(new RtfTypefaceSpan(clean), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        markEditorRichChanged("Schriftart angewendet: " + clean);
    }

    private void applyFontSizeToSelection(int size) {
        if (currentNode == null || editor == null) return;
        int clamped = LegacyRichTextToolbar.normalizeFontSize(size);
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            status("Kein Text für Schriftgröße");
            return;
        }
        Spannable text = editor.getText();
        removeOverlappingSpans(text, range[0], range[1], AbsoluteSizeSpan.class);
        text.setSpan(new AbsoluteSizeSpan(clamped, true), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        markEditorRichChanged("Schriftgröße angewendet: " + clamped + " pt");
    }

    private void applyAlignmentToSelection(String action) {
        if (editor == null) return;
        Spannable text = editor.getText();
        if (text.length() == 0) return;
        int[] selected = editorSelectionRange(true);
        int[] para = paragraphRange(text.toString(), selected[0], selected[1]);
        removeOverlappingSpans(text, para[0], para[1], AlignmentSpan.Standard.class);
        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        String message = "Linksbündig angewendet";
        if ("align_center".equals(action)) { alignment = Layout.Alignment.ALIGN_CENTER; message = "Zentriert angewendet"; }
        else if ("align_right".equals(action)) { alignment = Layout.Alignment.ALIGN_OPPOSITE; message = "Rechtsbündig angewendet"; }
        else if ("align_justify".equals(action)) { alignment = Layout.Alignment.ALIGN_NORMAL; message = "Blocksatz mobil als Linksbündig gespeichert"; }
        if (para[1] > para[0]) text.setSpan(new AlignmentSpan.Standard(alignment), para[0], para[1], Spanned.SPAN_PARAGRAPH);
        markEditorRichChanged(message);
    }

    private int[] editorSelectionRange(boolean wholeIfEmpty) {
        int len = editor == null || editor.getText() == null ? 0 : editor.getText().length();
        int start = editor == null ? 0 : Math.max(0, Math.min(editor.getSelectionStart(), len));
        int end = editor == null ? start : Math.max(0, Math.min(editor.getSelectionEnd(), len));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        if (start == end && wholeIfEmpty) { start = 0; end = len; }
        return new int[]{start, end};
    }

    private int currentSelectionPointSize() {
        if (editor == null || editor.getText() == null || editor.getText().length() == 0) return 12;
        int pos = Math.max(0, Math.min(editor.getSelectionStart(), editor.getText().length() - 1));
        AbsoluteSizeSpan[] spans = editor.getText().getSpans(pos, pos + 1, AbsoluteSizeSpan.class);
        if (spans.length > 0) return Math.max(1, spans[spans.length - 1].getSize());
        return 12;
    }

    private void removeCharacterFormatting(Spannable text, int start, int end) {
        removeOverlappingSpans(text, start, end, StyleSpan.class);
        removeOverlappingSpans(text, start, end, UnderlineSpan.class);
        removeOverlappingSpans(text, start, end, StrikethroughSpan.class);
        removeOverlappingSpans(text, start, end, ForegroundColorSpan.class);
        removeOverlappingSpans(text, start, end, BackgroundColorSpan.class);
        removeOverlappingSpans(text, start, end, AbsoluteSizeSpan.class);
        removeOverlappingSpans(text, start, end, RtfTypefaceSpan.class);
        removeOverlappingSpans(text, start, end, SuperscriptSpan.class);
        removeOverlappingSpans(text, start, end, SubscriptSpan.class);
        int[] para = paragraphRange(text.toString(), start, end);
        removeOverlappingSpans(text, para[0], para[1], AlignmentSpan.Standard.class);
        removeOverlappingSpans(text, para[0], para[1], LeadingMarginSpan.Standard.class);
    }

    private void removeOverlappingSpans(Spannable text, int start, int end, Class<?> spanClass) {
        if (text == null || end <= start) return;
        Object[] spans = text.getSpans(start, end, spanClass);
        for (Object span : spans) {
            int oldStart = text.getSpanStart(span);
            int oldEnd = text.getSpanEnd(span);
            if (oldEnd <= start || oldStart >= end) continue;
            int flags = text.getSpanFlags(span);
            Object left = cloneEditorSpan(span);
            Object right = cloneEditorSpan(span);
            text.removeSpan(span);
            if (oldStart < start && left != null) text.setSpan(left, oldStart, start, flags);
            if (oldEnd > end && right != null) text.setSpan(right, end, oldEnd, flags);
        }
    }

    private Object cloneEditorSpan(Object span) {
        if (span instanceof StyleSpan) return new StyleSpan(((StyleSpan) span).getStyle());
        if (span instanceof UnderlineSpan) return new UnderlineSpan();
        if (span instanceof StrikethroughSpan) return new StrikethroughSpan();
        if (span instanceof ForegroundColorSpan) return new ForegroundColorSpan(((ForegroundColorSpan) span).getForegroundColor());
        if (span instanceof BackgroundColorSpan) return new BackgroundColorSpan(((BackgroundColorSpan) span).getBackgroundColor());
        if (span instanceof AbsoluteSizeSpan) return new AbsoluteSizeSpan(((AbsoluteSizeSpan) span).getSize(), ((AbsoluteSizeSpan) span).getDip());
        if (span instanceof RtfTypefaceSpan) return new RtfTypefaceSpan(((RtfTypefaceSpan) span).familyName);
        if (span instanceof SuperscriptSpan) return new SuperscriptSpan();
        if (span instanceof SubscriptSpan) return new SubscriptSpan();
        if (span instanceof URLSpan) return new URLSpan(((URLSpan) span).getURL());
        if (span instanceof AlignmentSpan.Standard) return new AlignmentSpan.Standard(((AlignmentSpan.Standard) span).getAlignment());
        if (span instanceof LeadingMarginSpan.Standard) return new LeadingMarginSpan.Standard(((LeadingMarginSpan.Standard) span).getLeadingMargin(true), ((LeadingMarginSpan.Standard) span).getLeadingMargin(false));
        if (span instanceof RtfRawSpan) return new RtfRawSpan(((RtfRawSpan) span).rawRtf, ((RtfRawSpan) span).kind);
        return null;
    }

    private void markEditorRichChanged(String message) {
        editorDirty = true;
        if (document != null) markDocumentChanged();
        updateTitle();
        status(message == null || message.isEmpty() ? "RTF-Format angewendet" : message);
    }

    private void insertImage() {
        if (currentNode == null) return;
        startActivityForResult(intentForDialog(LegacyFileDialogModel.insertImage(currentDialogDirectory())), REQ_INSERT_IMAGE);
    }

    private void insertImageFromUri(Uri uri) {
        if (currentNode == null || uri == null || editor == null) return;
        try {
            byte[] bytes = readAll(uri);
            String mime = getContentResolver().getType(uri);
            String rawPicture = RtfUtils.rtfPictureFromImage(bytes, mime);
            if (rawPicture == null || rawPicture.isEmpty()) {
                error("Bild einfügen", "Dieses Bildformat kann nicht als ALX/RTF-Bild gespeichert werden. Unterstützt sind PNG, JPEG und BMP.");
                return;
            }
            Drawable drawable = imageDrawable(bytes, 0, 0);
            if (drawable == null) {
                error("Bild einfügen", "Das Bild konnte nicht dekodiert werden.");
                return;
            }
            Editable editable = editor.getText();
            Spannable text = editable;
            int start = Math.max(0, Math.min(editor.getSelectionStart(), text.length()));
            int end = Math.max(0, Math.min(editor.getSelectionEnd(), text.length()));
            if (end < start) { int tmp = start; start = end; end = tmp; }
            editable.replace(start, end, RTF_IMAGE_CHAR);
            text.setSpan(new RtfImageSpan(drawable, rawPicture, mime, bytes, 0, 0), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            editor.setSelection(start + 1);
            markEditorRichChanged("Bild eingefügt und sichtbar eingebettet");
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
        markDocumentChanged();
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
            markDocumentChanged();
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
        if (shouldFormatTreeTarget()) {
            showNodeColorDialog();
            return;
        }
        String[] palette = LegacyColorDialogModel.paletteLabels();
        String[] labels = new String[palette.length + 4];
        for (int i = 0; i < palette.length; i++) labels[i] = palette[i];
        labels[palette.length] = "RTF-Textfarbe löschen";
        labels[palette.length + 1] = "RTF-Hintergrundfarbe wählen…";
        labels[palette.length + 2] = "RTF-Hintergrund löschen";
        labels[palette.length + 3] = "Knotenfarben…";
        new AlertDialog.Builder(this)
                .setTitle("RTF-Textfarbe")
                .setItems(labels, (d, which) -> {
                    if (which < palette.length) applyRtfColorToSelection(LegacyColorDialogModel.choosePalette(LegacyColorDialogModel.Role.RTF_TEXT, which));
                    else if (which == palette.length) clearRtfColorFromSelection(false);
                    else if (which == palette.length + 1) showRtfColorPalette(LegacyColorDialogModel.Role.RTF_HIGHLIGHT);
                    else if (which == palette.length + 2) clearRtfColorFromSelection(true);
                    else showNodeColorDialog();
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showNodeColorDialog() {
        if (currentNode == null) return;
        String[] actions = {"Knoten-Hintergrund wählen", "Knoten-Textfarbe wählen", "Knoten-Hintergrund löschen", "Knoten-Textfarbe löschen"};
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
        markDocumentChanged();
        rebuildTree();
        updateTitle();
        status(background ? "Hintergrund gesetzt" : "Textfarbe gesetzt");
    }

    private void requestAndroidWidgetForCurrentNode() {
        if (currentNode == null) return;
        String title = safeTitle(currentNode);
        String text = RtfUtils.rtfToPlainText(currentNode.rtf == null ? "" : currentNode.rtf);
        int bg = Color.rgb(255, 250, 205);
        int fg = Color.rgb(30, 30, 30);
        if (currentNode.desktopNote != null && currentNode.desktopNote.argb != null) bg = 0xff000000 | (currentNode.desktopNote.argb & 0x00ffffff);
        else if (currentNode.bgArgb != 0) bg = 0xff000000 | (currentNode.bgArgb & 0x00ffffff);
        if (currentNode.fgArgb != 0) fg = 0xff000000 | (currentNode.fgArgb & 0x00ffffff);
        NoteWidgetProvider.requestPinOrUpdate(this, title, text, bg, fg);
    }

    private void showDesktopNoteDialog() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        requestAndroidWidgetForCurrentNode();
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
                    markDocumentChanged();
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
                    markDocumentChanged();
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
                    markDocumentChanged();
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
                    markDocumentChanged();
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


    private SpannableStringBuilder rtfToEditorText(String rtf) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        List<RtfContentPart> parts = RtfUtils.rtfToContentParts(rtf == null ? "" : rtf);
        if (parts.isEmpty()) return out;
        for (RtfContentPart part : parts) {
            if (part instanceof RtfImagePart) {
                appendEditorImage(out, (RtfImagePart) part);
            } else if (part instanceof RtfHyperlink) {
                RtfHyperlink link = (RtfHyperlink) part;
                int start = out.length();
                out.append(link.text == null || link.text.isEmpty() ? link.url : link.text);
                int end = out.length();
                applyRtfStyleSpan(out, start, end, link.style);
                if (end > start && link.url != null && !link.url.isEmpty()) {
                    out.setSpan(new URLSpan(link.url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else if (part instanceof RtfField) {
                int start = out.length();
                String text = part.text == null || part.text.isEmpty() ? "[Feld]" : part.text;
                out.append(text);
                int end = out.length();
                applyRtfStyleSpan(out, start, end, part.style);
                if (end > start && part.rtf != null && !part.rtf.isEmpty()) out.setSpan(new RtfRawSpan(part.rtf, "field"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (part instanceof RtfObject) {
                int start = out.length();
                String text = part.text == null || part.text.isEmpty() ? RtfUtils.LEGACY_OBJECT_PLACEHOLDER : part.text;
                out.append(text);
                int end = out.length();
                applyRtfStyleSpan(out, start, end, part.style);
                if (end > start && part.rtf != null && !part.rtf.isEmpty()) out.setSpan(new RtfRawSpan(part.rtf, "object"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                int start = out.length();
                out.append(part.text == null ? "" : part.text);
                applyRtfStyleSpan(out, start, out.length(), part.style);
            }
        }
        return out;
    }

    private void appendEditorImage(SpannableStringBuilder out, RtfImagePart part) {
        int start = out.length();
        Drawable drawable = imageDrawable(part.imageData, part.widthTwips, part.heightTwips);
        if (drawable == null) {
            out.append(RtfUtils.LEGACY_IMAGE_PLACEHOLDER);
            applyRtfStyleSpan(out, start, out.length(), part.style);
            if (out.length() > start && part.rtf != null && !part.rtf.isEmpty()) out.setSpan(new RtfRawSpan(part.rtf, "image"), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return;
        }
        out.append(RTF_IMAGE_CHAR);
        out.setSpan(new RtfImageSpan(drawable, part.rtf, part.mimeType, part.imageData, part.widthTwips, part.heightTwips), start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private Drawable imageDrawable(byte[] data, int widthTwips, int heightTwips) {
        if (data == null || data.length == 0) return null;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (bitmap == null) return null;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = widthTwips > 0 ? Math.round((widthTwips / 15.0f) * dm.density) : bitmap.getWidth();
        int height = heightTwips > 0 ? Math.round((heightTwips / 15.0f) * dm.density) : bitmap.getHeight();
        if (width <= 0) width = bitmap.getWidth();
        if (height <= 0) height = bitmap.getHeight();
        int maxWidth = Math.max(dp(120), dm.widthPixels - dp(48));
        if (width > maxWidth && width > 0) {
            float scale = maxWidth / (float) width;
            width = maxWidth;
            height = Math.max(1, Math.round(height * scale));
        }
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        drawable.setBounds(0, 0, Math.max(1, width), Math.max(1, height));
        return drawable;
    }

    private void applyRtfStyleSpan(Spannable spannable, int start, int end, RtfTextStyle style) {
        if (spannable == null || style == null || end <= start) return;
        if (style.bold) spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (style.italic) spannable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (style.underline) spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (style.strike) spannable.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (style.fgColor != null) {
            try { spannable.setSpan(new ForegroundColorSpan(Color.parseColor(style.fgColor)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); } catch (Exception ignored) {}
        }
        if (style.bgColor != null) {
            try { spannable.setSpan(new BackgroundColorSpan(Color.parseColor(style.bgColor)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); } catch (Exception ignored) {}
        }
        if (style.fontFamily != null && !style.fontFamily.trim().isEmpty()) spannable.setSpan(new RtfTypefaceSpan(style.fontFamily), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (style.fontSizeHalfPoints != null && style.fontSizeHalfPoints > 0) spannable.setSpan(new AbsoluteSizeSpan(Math.max(1, Math.round(style.fontSizeHalfPoints / 2.0f)), true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if ("super".equals(style.vertical)) spannable.setSpan(new SuperscriptSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if ("sub".equals(style.vertical)) spannable.setSpan(new SubscriptSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (style.align != null && !style.align.isEmpty()) {
            Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
            if ("center".equals(style.align)) alignment = Layout.Alignment.ALIGN_CENTER;
            else if ("right".equals(style.align)) alignment = Layout.Alignment.ALIGN_OPPOSITE;
            int[] para = paragraphRange(spannable.toString(), start, end);
            if (para[1] > para[0]) spannable.setSpan(new AlignmentSpan.Standard(alignment), para[0], para[1], Spanned.SPAN_PARAGRAPH);
        }
        if (style.leftIndentTwips > 0 || style.firstIndentTwips != 0) {
            int first = twipsToPx(style.leftIndentTwips + style.firstIndentTwips);
            int rest = twipsToPx(style.leftIndentTwips);
            int[] para = paragraphRange(spannable.toString(), start, end);
            if (para[1] > para[0]) spannable.setSpan(new LeadingMarginSpan.Standard(Math.max(0, first), Math.max(0, rest)), para[0], para[1], Spanned.SPAN_PARAGRAPH);
        }
    }

    private String editorContentToRtf() {
        if (editor == null) return "";
        Editable editable = editor.getText();
        if (!(editable instanceof Spanned)) return RtfUtils.plainTextToRtf(editable == null ? "" : editable.toString());
        return spannedToRtf((Spanned) editable);
    }

    private String spannedToRtf(Spanned text) {
        Map<String, Integer> fonts = collectEditorFonts(text);
        Map<String, Integer> colors = collectEditorColors(text);
        StringBuilder out = new StringBuilder(text == null ? 128 : text.length() * 2 + 128);
        out.append("{\\rtf1\\ansi\\ansicpg1252\\deff0");
        out.append(editorRtfFontTable(fonts));
        out.append(editorRtfColorTable(colors));
        out.append("\\viewkind4\\uc1\\pard ");
        out.append(emitEditorRtfBody(text, fonts, colors));
        out.append("\\par}");
        return out.toString();
    }

    private Map<String, Integer> collectEditorFonts(Spanned text) {
        LinkedHashMap<String, Integer> fonts = new LinkedHashMap<>();
        if (text == null) return fonts;
        RtfTypefaceSpan[] spans = text.getSpans(0, text.length(), RtfTypefaceSpan.class);
        for (RtfTypefaceSpan span : spans) addFont(fonts, span.familyName);
        return fonts;
    }

    private Map<String, Integer> collectEditorColors(Spanned text) {
        LinkedHashMap<String, Integer> colors = new LinkedHashMap<>();
        if (text == null) return colors;
        for (ForegroundColorSpan span : text.getSpans(0, text.length(), ForegroundColorSpan.class)) addColor(colors, colorToCss(span.getForegroundColor()));
        for (BackgroundColorSpan span : text.getSpans(0, text.length(), BackgroundColorSpan.class)) addColor(colors, colorToCss(span.getBackgroundColor()));
        return colors;
    }

    private void addFont(Map<String, Integer> fonts, String family) {
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family);
        if (!fonts.containsKey(clean)) fonts.put(clean, fonts.size() + 1);
    }

    private void addColor(Map<String, Integer> colors, String color) {
        if (color == null || !color.matches("#[0-9a-fA-F]{6}")) return;
        String clean = color.toLowerCase(Locale.ROOT);
        if (!colors.containsKey(clean)) colors.put(clean, colors.size() + 1);
    }

    private String editorRtfFontTable(Map<String, Integer> fonts) {
        StringBuilder out = new StringBuilder();
        out.append("{\\fonttbl{\\f0\\fnil\\fcharset0 Microsoft Sans Serif;}");
        for (Map.Entry<String, Integer> entry : fonts.entrySet()) {
            out.append("{\\f").append(entry.getValue()).append("\\fnil\\fcharset0 ").append(cleanRtfFontName(entry.getKey())).append(";}");
        }
        out.append("}");
        return out.toString();
    }

    private String editorRtfColorTable(Map<String, Integer> colors) {
        if (colors == null || colors.isEmpty()) return "";
        StringBuilder out = new StringBuilder("{\\colortbl ;");
        for (String color : colors.keySet()) {
            int r = Integer.parseInt(color.substring(1, 3), 16);
            int g = Integer.parseInt(color.substring(3, 5), 16);
            int b = Integer.parseInt(color.substring(5, 7), 16);
            out.append("\\red").append(r).append("\\green").append(g).append("\\blue").append(b).append(';');
        }
        out.append('}');
        return out.toString();
    }

    private String emitEditorRtfBody(Spanned spanned, Map<String, Integer> fonts, Map<String, Integer> colors) {
        if (spanned == null || spanned.length() == 0) return "";
        String plain = spanned.toString();
        StringBuilder out = new StringBuilder(plain.length() * 2);
        int pos = 0;
        while (pos < plain.length()) {
            RtfImageSpan image = imageSpanAt(spanned, pos);
            if (image != null) {
                String raw = image.rawRtf == null || image.rawRtf.isEmpty() ? RtfUtils.rtfPictureFromImage(image.imageData, image.mimeType) : image.rawRtf;
                out.append(raw == null || raw.isEmpty() ? RtfUtils.rtfEscapeText(RtfUtils.LEGACY_IMAGE_PLACEHOLDER) : raw);
                pos = Math.max(pos + 1, spanned.getSpanEnd(image));
                continue;
            }
            if (plain.startsWith(RTF_IMAGE_CHAR, pos)) {
                out.append(RtfUtils.rtfEscapeText(RtfUtils.LEGACY_IMAGE_PLACEHOLDER));
                pos++;
                continue;
            }
            RtfRawSpan raw = rawSpanAt(spanned, pos);
            if (raw != null && raw.rawRtf != null && !raw.rawRtf.isEmpty() && spanned.getSpanStart(raw) == pos) {
                out.append(raw.rawRtf);
                pos = Math.max(pos + 1, spanned.getSpanEnd(raw));
                continue;
            }
            int next = spanned.nextSpanTransition(pos, plain.length(), CharacterStyle.class);
            next = Math.min(next, spanned.nextSpanTransition(pos, plain.length(), ParagraphStyle.class));
            next = Math.min(next, spanned.nextSpanTransition(pos, plain.length(), RtfRawSpan.class));
            int nextImage = plain.indexOf(RTF_IMAGE_CHAR, pos + 1);
            if (nextImage >= 0) next = Math.min(next, nextImage);
            if (next <= pos) next = pos + 1;
            String segment = plain.substring(pos, next);
            RtfTextStyle style = styleAt(spanned, pos);
            URLSpan link = urlSpanAt(spanned, pos);
            if (link != null && link.getURL() != null && !link.getURL().isEmpty() && spanned.getSpanStart(link) <= pos && spanned.getSpanEnd(link) >= next) {
                out.append(rtfHyperlinkField(link.getURL(), segment, rtfStylePrefix(style, colors, fonts)));
            } else {
                out.append(emitRtfText(segment, style, colors, fonts));
            }
            pos = next;
        }
        return out.toString();
    }

    private RtfImageSpan imageSpanAt(Spanned spanned, int pos) {
        int end = Math.min(spanned.length(), pos + 1);
        if (end <= pos) return null;
        RtfImageSpan[] spans = spanned.getSpans(pos, end, RtfImageSpan.class);
        for (RtfImageSpan span : spans) {
            if (spanned.getSpanStart(span) <= pos && spanned.getSpanEnd(span) > pos) return span;
        }
        return null;
    }

    private RtfRawSpan rawSpanAt(Spanned spanned, int pos) {
        int end = Math.min(spanned.length(), pos + 1);
        if (end <= pos) return null;
        RtfRawSpan[] spans = spanned.getSpans(pos, end, RtfRawSpan.class);
        for (RtfRawSpan span : spans) {
            if (spanned.getSpanStart(span) <= pos && spanned.getSpanEnd(span) > pos) return span;
        }
        return null;
    }

    private URLSpan urlSpanAt(Spanned spanned, int pos) {
        int end = Math.min(spanned.length(), pos + 1);
        if (end <= pos) return null;
        URLSpan[] spans = spanned.getSpans(pos, end, URLSpan.class);
        for (URLSpan span : spans) {
            if (spanned.getSpanStart(span) <= pos && spanned.getSpanEnd(span) > pos) return span;
        }
        return null;
    }

    private RtfTextStyle styleAt(Spanned spanned, int pos) {
        RtfTextStyle style = new RtfTextStyle();
        if (spanned == null || spanned.length() == 0) return style;
        int end = Math.min(spanned.length(), pos + 1);
        for (StyleSpan span : spanned.getSpans(pos, end, StyleSpan.class)) {
            int s = span.getStyle();
            if ((s & Typeface.BOLD) != 0) style.bold = true;
            if ((s & Typeface.ITALIC) != 0) style.italic = true;
        }
        if (spanned.getSpans(pos, end, UnderlineSpan.class).length > 0) style.underline = true;
        if (spanned.getSpans(pos, end, StrikethroughSpan.class).length > 0) style.strike = true;
        ForegroundColorSpan[] fg = spanned.getSpans(pos, end, ForegroundColorSpan.class);
        if (fg.length > 0) style.fgColor = colorToCss(fg[fg.length - 1].getForegroundColor());
        BackgroundColorSpan[] bg = spanned.getSpans(pos, end, BackgroundColorSpan.class);
        if (bg.length > 0) style.bgColor = colorToCss(bg[bg.length - 1].getBackgroundColor());
        RtfTypefaceSpan[] fonts = spanned.getSpans(pos, end, RtfTypefaceSpan.class);
        if (fonts.length > 0) style.fontFamily = fonts[fonts.length - 1].familyName;
        AbsoluteSizeSpan[] sizes = spanned.getSpans(pos, end, AbsoluteSizeSpan.class);
        if (sizes.length > 0) style.fontSizeHalfPoints = Math.max(1, sizes[sizes.length - 1].getSize()) * 2;
        if (spanned.getSpans(pos, end, SuperscriptSpan.class).length > 0) style.vertical = "super";
        if (spanned.getSpans(pos, end, SubscriptSpan.class).length > 0) style.vertical = "sub";
        AlignmentSpan.Standard[] aligns = spanned.getSpans(pos, end, AlignmentSpan.Standard.class);
        if (aligns.length > 0) {
            Layout.Alignment a = aligns[aligns.length - 1].getAlignment();
            if (a == Layout.Alignment.ALIGN_CENTER) style.align = "center";
            else if (a == Layout.Alignment.ALIGN_OPPOSITE) style.align = "right";
            else style.align = null;
        }
        LeadingMarginSpan.Standard[] margins = spanned.getSpans(pos, end, LeadingMarginSpan.Standard.class);
        if (margins.length > 0) {
            int rest = margins[margins.length - 1].getLeadingMargin(false);
            int first = margins[margins.length - 1].getLeadingMargin(true);
            style.leftIndentTwips = pxToTwips(rest);
            style.firstIndentTwips = pxToTwips(first - rest);
        }
        return style;
    }

    private String emitRtfText(String text, RtfTextStyle style, Map<String, Integer> colors, Map<String, Integer> fonts) {
        if (text == null || text.isEmpty()) return "";
        String escaped = RtfUtils.rtfEscapeMultiline(text);
        String prefix = rtfStylePrefix(style, colors, fonts);
        if (prefix.isEmpty()) return escaped;
        return "{" + prefix + " " + escaped + "}";
    }

    private String rtfStylePrefix(RtfTextStyle style, Map<String, Integer> colors, Map<String, Integer> fonts) {
        if (style == null) return "";
        StringBuilder out = new StringBuilder();
        if (style.align != null) {
            if ("center".equals(style.align)) out.append("\\qc");
            else if ("right".equals(style.align)) out.append("\\qr");
            else if ("justify".equals(style.align)) out.append("\\qj");
            else out.append("\\ql");
        }
        if (style.bold) out.append("\\b");
        if (style.italic) out.append("\\i");
        if (style.underline) out.append("\\ul");
        if (style.strike) out.append("\\strike");
        if (style.fontFamily != null) {
            Integer idx = fonts == null ? null : fonts.get(LegacyRichTextToolbar.normalizeFontFamily(style.fontFamily));
            if (idx != null) out.append("\\f").append(idx);
        }
        if (style.fontSizeHalfPoints != null && style.fontSizeHalfPoints > 0) out.append("\\fs").append(style.fontSizeHalfPoints);
        if (style.fgColor != null) {
            Integer idx = colors == null ? null : colors.get(style.fgColor.toLowerCase(Locale.ROOT));
            if (idx != null) out.append("\\cf").append(idx);
        }
        if (style.bgColor != null) {
            Integer idx = colors == null ? null : colors.get(style.bgColor.toLowerCase(Locale.ROOT));
            if (idx != null) out.append("\\highlight").append(idx);
        }
        if (style.leftIndentTwips > 0) out.append("\\li").append(style.leftIndentTwips);
        if (style.firstIndentTwips != 0) out.append("\\fi").append(style.firstIndentTwips);
        if ("super".equals(style.vertical)) out.append("\\super");
        if ("sub".equals(style.vertical)) out.append("\\sub");
        return out.toString();
    }

    private String rtfHyperlinkField(String url, String label, String stylePrefix) {
        String safeUrl = (url == null ? "" : url).replace("\\", "\\\\").replace("\"", "\\\"");
        String safeLabel = label == null || label.isEmpty() ? safeUrl : label;
        String prefix = stylePrefix == null ? "" : stylePrefix;
        String result = prefix.isEmpty() ? RtfUtils.rtfEscapeMultiline(safeLabel) : "{" + prefix + " " + RtfUtils.rtfEscapeMultiline(safeLabel) + "}";
        return "{\\field{\\*\\fldinst HYPERLINK \"" + safeUrl + "\"}{\\fldrslt " + result + "}}";
    }

    private String cleanRtfFontName(String family) {
        return LegacyRichTextToolbar.normalizeFontFamily(family).replace('\\', ' ').replace('{', ' ').replace('}', ' ').replace(';', ' ').trim();
    }

    private String colorToCss(int color) {
        return String.format(Locale.ROOT, "#%06x", color & 0x00ffffff);
    }

    private int[] paragraphRange(String text, int start, int end) {
        String value = text == null ? "" : text;
        int len = value.length();
        int s = Math.max(0, Math.min(start, len));
        int e = Math.max(s, Math.min(end, len));
        int ps = value.lastIndexOf('\n', Math.max(0, s - 1));
        ps = ps < 0 ? 0 : ps + 1;
        int pe = value.indexOf('\n', e);
        if (pe < 0) pe = len;
        else pe = pe + 1;
        return new int[]{ps, pe};
    }

    private int twipsToPx(int twips) {
        return Math.round((twips / 1440.0f) * getResources().getDisplayMetrics().xdpi);
    }

    private int pxToTwips(int px) {
        float xdpi = getResources().getDisplayMetrics().xdpi;
        if (xdpi <= 0) xdpi = 160f;
        return Math.round((px / xdpi) * 1440.0f);
    }

    private void selectNode(NoteNode node, boolean focusEditor) {
        if (node == null) return;
        saveCurrentEditorToNode();
        currentNode = node;
        LegacyEditorNodeSync.LoadState loaded = LegacyEditorNodeSync.load(node);
        loadingEditor = true;
        rootTitleView.setText(document.ensureRoot().title == null ? "start" : document.ensureRoot().title);
        titleEdit.setText(loaded.titleText);
        editor.setText(rtfToEditorText(loaded.rawRtf));
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
            setFormatTarget(FormatTarget.RTF_EDITOR);
            editor.requestFocus();
        }
    }

    private void saveCurrentEditorToNode() {
        if (currentNode == null) return;
        // LegacyEditorNodeSync.applyToNode bleibt das historische Plaintext-Modell; der native Android-Editor speichert hier zusätzlich Spans und Bilder als RTF.
        String oldTitle = LegacyEditorNodeSync.normalizeTitle(currentNode.title);
        String oldRtf = currentNode.rtf == null ? "" : currentNode.rtf;
        String nextTitle = titleDirty ? LegacyEditorNodeSync.normalizeTitle(titleEdit == null ? currentNode.title : titleEdit.getText().toString()) : oldTitle;
        String nextRtf = oldRtf;
        if (editorDirty) {
            if (shouldIgnoreTransientBlankEditorOverwrite(oldRtf)) {
                editorDirty = false;
            } else {
                nextRtf = editorContentToRtf();
            }
        }
        boolean changed = !oldTitle.equals(nextTitle) || !oldRtf.equals(nextRtf);
        if (changed) {
            currentNode.title = nextTitle;
            currentNode.rtf = nextRtf;
            markDocumentChanged();
            if (currentNode.desktopNote != null) status("Haftnotiz-Daten aktualisiert");
        }
        editorDirty = false;
        titleDirty = false;
    }

    private boolean shouldIgnoreTransientBlankEditorOverwrite(String oldRtf) {
        if (editor == null || editor.getText() == null) return rtfHasVisibleText(oldRtf);
        if (!rtfHasVisibleText(oldRtf)) return false;
        String visible = editor.getText().toString();
        if (visible != null && !visible.trim().isEmpty()) return false;
        if (editor.hasFocus() || lastFormatTarget == FormatTarget.RTF_EDITOR) return false;
        return true;
    }

    private void rebuildTree() {
        ArrayList<TreeListAdapter.FlatNode> rows = new ArrayList<>();
        buildRows(document.ensureRoot(), 0, rows);
        treeAdapter.setRows(rows);
        treeAdapter.setSelected(currentNode);
        rootTitleView.setText(document.ensureRoot().title == null ? "start" : document.ensureRoot().title);
    }

    private void finishLoadedDocument(String message) {
        runtimeSnapshotRestored = false;
        currentNode = null;
        rebuildTree();
        selectNode(document.ensureRoot(), false);
        updateTitle();
        scheduleRuntimeSnapshotSave();
        status((message == null ? "Geöffnet" : message) + " · " + treeExpansionSummary());
    }

    private String treeExpansionSummary() {
        int[] counts = new int[]{0, 0};
        countTreeExpansion(document == null ? null : document.ensureRoot(), counts);
        return "ALX-Baumzustand: " + counts[0] + " offen, " + counts[1] + " zu";
    }

    private void countTreeExpansion(NoteNode node, int[] counts) {
        if (node == null || counts == null) return;
        if (!node.children.isEmpty()) {
            if (node.expanded) counts[0]++;
            else counts[1]++;
        }
        for (NoteNode child : node.children) countTreeExpansion(child, counts);
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
        String msg = "Titel: " + state.windowTitle(APP_DISPLAY_NAME) +
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
        LegacyCloseResetModel.ResetState reset = LegacyCloseResetModel.afterClose(APP_VERSION_NAME);
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
        String msg = LegacyDiagnosticReport.build(document, currentNode, currentDocumentTitleState(), settings, APP_VERSION_NAME);
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
        String msg = LegacyRuntimeIdentity.summary(APP_VERSION_NAME)
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
            markDocumentChanged();
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
        String version = APP_VERSION_NAME;
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
        if (!LegacyDialogModels.shouldShowWannaSave(document.changed) || isBlankSingleNodeDocumentForPrompt(document)) {
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
            finishLoadedDocument("Geöffnet: " + currentDisplayName);
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
            finishLoadedDocument("Geöffnet: " + currentDisplayName);
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
                APP_DISPLAY_NAME,
                currentDisplayName,
                document != null && document.changed,
                currentFtpTarget == null ? "" : currentFtpTarget.safeDisplayUrl(),
                currentRawFile == null ? "" : currentRawFile.getAbsolutePath(),
                currentUri == null ? "" : currentUri.toString());
    }

    private void updateTitle() {
        LegacyDocumentTitle.State state = currentDocumentTitleState();
        setTitle(state.appTitle);
    }

    private void status(String text) {
        // Keine dauerhafte Statusleiste mehr: Rückmeldungen bleiben in Dialogen/Ansichten.
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
