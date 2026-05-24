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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.text.method.KeyListener;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.ImageSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
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
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
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
import java.util.Collections;
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
import de.notizen.android.core.LegacyAndroidSettingsUiModel;
import de.notizen.android.core.LegacyAndroidWidgetRegistry;
import de.notizen.android.core.LegacyExportShareModel;
import de.notizen.android.core.LegacyColors;
import de.notizen.android.core.LegacyDesktopNoteContextMenu;
import de.notizen.android.core.LegacyDialogModels;
import de.notizen.android.core.LegacyEditorActions;
import de.notizen.android.core.LegacyFeedback;
import de.notizen.android.core.LegacyFreshStartModel;
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
import de.notizen.android.core.LegacyInkPictureModel;
import de.notizen.android.core.LegacyOpenTarget;
import de.notizen.android.core.LegacyDocumentTitle;
import de.notizen.android.core.LegacyDiagnosticReport;
import de.notizen.android.core.LegacyDocumentLifecycle;
import de.notizen.android.core.LegacyTreeCreation;
import de.notizen.android.core.LegacyDesktopNoteTrayRegistry;
import de.notizen.android.core.LegacyTraySelectionModel;
import de.notizen.android.core.LegacyTouchZoomModel;
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
import de.notizen.android.core.LegacyMarkdownPreviewModel;
import de.notizen.android.core.LegacyPackagePermissionModel;
import de.notizen.android.core.LegacyRecentMenu;
import de.notizen.android.core.LegacyNodeExport;
import de.notizen.android.core.LegacyPrintLayout;
import de.notizen.android.core.LegacyRichTextToolbar;
import de.notizen.android.core.LegacyQuickSearchBar;
import de.notizen.android.core.LegacyRtfUndoModel;
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
import de.notizen.android.core.LegacyTreeLabelEditing;
import de.notizen.android.core.LegacyTreeUndoModel;
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
    private static final String APP_VERSION_NAME = "1.0.120-java-android-nativ-loose-table";
    private static final String RTF_IMAGE_CHAR = "\ufffc";
    private static final String NODE_TITLE_STYLE_ATTR = "androidTitleStyle";
    private static final String NODE_TITLE_FONT_ATTR = "androidTitleFont";
    private static final String NODE_TITLE_SIZE_ATTR = "androidTitleSizeSp";

    private static final int DEFAULT_TOOLBAR_BUTTON_DP = LegacyTouchZoomModel.DEFAULT_TOOLBAR_BUTTON_DP;
    private static final int TOOLBAR_BUTTON_MARGIN_DP = 1;
    private static final float DEFAULT_HEADER_TEXT_SP = LegacyTouchZoomModel.DEFAULT_HEADER_TEXT_SP;
    private static final float PINCH_STEP_FACTOR = 1.12f;
    private static final int PINCH_MAX_STEPS_PER_EVENT = 4;
    private static final int TREE_PANE_DEFAULT_DP = 280;
    private static final int TREE_PANE_MIN_DP = 24;
    private static final int EDITOR_PANE_MIN_DP = 56;
    private static final int CONTENT_HEADER_DP = 34;
    private static final int MAX_IMAGE_DISPLAY_LONG_EDGE_PX = 1600;
    private static final int MAX_EMBED_IMAGE_LONG_EDGE_PX = LegacyTouchZoomModel.MAX_EMBED_IMAGE_LONG_EDGE_PX;
    private static final int MAX_EMBEDDED_IMAGE_BYTES = LegacyTouchZoomModel.MAX_EMBEDDED_IMAGE_BYTES;
    private static final int MAX_READ_ALL_BYTES = 80 * 1024 * 1024;
    private static final int IMAGE_JPEG_QUALITY = 82;
    private static final long EDITOR_TYPING_UNDO_INTERVAL_MS = 900L;
    private static final int MAX_QUICK_SEARCH_RESULTS = 1000;
    private static final long RUNTIME_SNAPSHOT_DELAY_MS = 700L;
    private static final long UI_ZOOM_SAVE_DELAY_MS = 450L;
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
    private static final int REQ_EXPORT_GENERIC = 1016;

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
    private byte[] pendingGenericExportBytes;
    private String pendingGenericExportMime;
    private String pendingGenericExportFileName;
    private String pendingGenericExportStatus;
    private String pendingGenericExportShareText;
    private String pendingWidgetNodePath;
    private NoteNode internalClipboardNode;
    private SpannableStringBuilder internalEditorClipboard;
    private String internalEditorClipboardPlain = "";
    private LegacySettings settings = new LegacySettings();
    private FtpTarget currentFtpTarget;
    private ContinueCallback pendingAfterSaveAsCallback;
    private ExecutorService ioExecutor;
    private Handler mainHandler;
    private final SimpleDateFormat alarmDateFormat = new SimpleDateFormat(LegacyAlarmDialogModel.DATE_PATTERN, Locale.GERMANY);
    private final LegacySearchSession searchSession = new LegacySearchSession();
    private final ArrayList<EditorHistoryEntry> editorUndoStack = new ArrayList<>();
    private final ArrayList<EditorHistoryEntry> editorRedoStack = new ArrayList<>();
    private final ArrayList<LegacyTreeUndoModel.Snapshot> treeUndoStack = new ArrayList<>();
    private final ArrayList<LegacyTreeUndoModel.Snapshot> treeRedoStack = new ArrayList<>();
    private final ArrayList<SearchResult> quickSearchResults = new ArrayList<>();
    private int quickSearchIndex = -1;
    private LegacyQuickSearchBar.Scope quickSearchScope = LegacyQuickSearchBar.Scope.CURRENT_SUBTREE;

    private TreeListAdapter treeAdapter;
    private ListView treeList;
    private TextView rootTitleView;
    private EditText titleEdit;
    private EditText editor;
    private WebView markdownPreviewWebView;
    private LinearLayout quickSearchBar;
    private EditText quickSearchInput;
    private CheckBox quickSearchWholeTree;
    private CheckBox quickSearchWholeWords;
    private CheckBox quickSearchCaseSensitive;
    private CheckBox quickSearchIncludeTitles;
    private TextView quickSearchStatus;
    private TextView markdownPreviewButton;
    private WebView pendingPrintView;
    private Runnable autosaveRunnable;
    private Runnable runtimeSnapshotRunnable;
    private Runnable androidUiZoomSaveRunnable;
    private boolean toolbarButtonSizeApplyScheduled = false;
    private Thread.UncaughtExceptionHandler previousUncaughtExceptionHandler;
    private boolean autosaveInFlight = false;
    private boolean runtimeSnapshotRestored = false;

    private boolean loadingEditor = false;
    private boolean editorDirty = false;
    private boolean titleDirty = false;
    private boolean editorHorizontallyScrolling = false;
    private int treePaneWidthDp = TREE_PANE_DEFAULT_DP;
    private int toolbarButtonDp = DEFAULT_TOOLBAR_BUTTON_DP;
    private float headerTextSp = DEFAULT_HEADER_TEXT_SP;
    private FormatTarget lastFormatTarget = FormatTarget.RTF_EDITOR;
    private ActivePane lastActivePane = ActivePane.RTF_EDITOR;
    private LinearLayout contentLayout;
    private LinearLayout treePane;
    private LinearLayout editorPane;
    private View paneDivider;
    private final ArrayList<TextView> toolbarSquareViews = new ArrayList<>();
    private ScaleGestureDetector editorScaleDetector;
    private ScaleGestureDetector treeScaleDetector;
    private ScaleGestureDetector toolbarScaleDetector;
    private ScaleGestureDetector headerScaleDetector;
    private boolean editorPinchActive = false;
    private boolean treePinchActive = false;
    private boolean toolbarPinchActive = false;
    private boolean headerPinchActive = false;
    private boolean editorHistoryRestoring = false;
    private boolean editorHistorySnapshotLocked = false;
    private boolean treeHistoryRestoring = false;
    private boolean formatToolbarUpdateScheduled = false;
    private boolean markdownPreviewActive = false;
    private NoteNode markdownPreviewNode;
    private String markdownPreviewSourceRtf = "";
    private int markdownPreviewReturnSelectionStart = -1;
    private int markdownPreviewReturnSelectionEnd = -1;
    private int markdownPreviewReturnScrollX = 0;
    private int markdownPreviewReturnScrollY = 0;
    private KeyListener editorEditableKeyListener;
    private int editorEditableInputType = -1;
    private NoteNode treeDragSource;
    private NoteNode treeDragPreviewTarget;
    private TreeListAdapter.DropPreview treeDragPreviewMode = TreeListAdapter.DropPreview.NONE;
    private long lastEditorTypingUndoAt = 0L;
    private static final long DELAYED_CONTEXT_MENU_MS = 4000L;
    private Runnable delayedEditorContextRunnable;
    private Runnable delayedTreeContextRunnable;
    private boolean editorLongPressArmed = false;
    private boolean treeLongPressArmed = false;
    private float editorLongDownX = 0f;
    private float editorLongDownY = 0f;
    private float editorLastTouchX = 0f;
    private float editorLastTouchY = 0f;
    private float treeLongDownX = 0f;
    private float treeLongDownY = 0f;
    private NoteNode treeLongContextNode;
    private int treeLongContextPosition = AdapterView.INVALID_POSITION;
    private final Map<String, TextView> textToolbarActionButtons = new LinkedHashMap<>();

    private interface PasswordCallback { void onPassword(String password); }
    private interface ContinueCallback { void run(); }
    private interface BackgroundCallback<T> { T run() throws Exception; }
    private interface UiCallback<T> { void run(T value); }

    private enum FormatTarget { RTF_EDITOR, TREE_NODE }
    private enum ActivePane { RTF_EDITOR, TREE_NODE, TITLE_TEXT }

    private static final class RetainedState {
        NoteDocument document;
        NoteNode currentNode;
        Uri currentUri;
        File currentRawFile;
        String currentDisplayName;
        NoteNode internalClipboardNode;
        LegacySettings settings;
        FtpTarget currentFtpTarget;
        SpannableStringBuilder internalEditorClipboard;
        String internalEditorClipboardPlain;
        ArrayList<LegacyTreeUndoModel.Snapshot> treeUndoStack;
        ArrayList<LegacyTreeUndoModel.Snapshot> treeRedoStack;
        boolean editorHorizontallyScrolling;
        int selectionStart;
        int selectionEnd;
        int treePaneWidthDp;
        int toolbarButtonDp;
        float headerTextSp;
        FormatTarget lastFormatTarget;
        ActivePane lastActivePane;
        String quickSearchTerm;
        boolean quickSearchWholeTree;
        boolean quickSearchWholeWords;
        boolean quickSearchCaseSensitive;
        boolean quickSearchIncludeTitles;
        boolean quickSearchVisible;
        boolean markdownPreviewActive;
        NoteNode markdownPreviewNode;
        String markdownPreviewSourceRtf;
        int markdownPreviewReturnSelectionStart;
        int markdownPreviewReturnSelectionEnd;
        int markdownPreviewReturnScrollX;
        int markdownPreviewReturnScrollY;
    }

    /**
     * Own dispatching container for the three icon rows.
     * A normal OnTouchListener on the buttons only sees gestures that start on
     * one concrete child view.  The toolbar zoom must see the whole surface,
     * including gestures spanning two rows or empty space between icons.
     */
    private final class ToolbarZoomLayout extends LinearLayout {
        private boolean cancelSentToChildren = false;

        ToolbarZoomLayout(Context context) {
            super(context);
        }

        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            boolean consumeForZoom = handleToolbarPinchTouch(event);
            int action = event == null ? MotionEvent.ACTION_CANCEL : event.getActionMasked();
            if (consumeForZoom && !cancelSentToChildren && action != MotionEvent.ACTION_DOWN) {
                MotionEvent cancel = MotionEvent.obtain(event);
                cancel.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(cancel);
                cancel.recycle();
                cancelSentToChildren = true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) cancelSentToChildren = false;
            if (consumeForZoom) return true;
            return super.dispatchTouchEvent(event);
        }
    }

    private final class TrackingEditText extends EditText {
        TrackingEditText(Context context) { super(context); }
        @Override protected void onSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
            if (MainActivity.this.editor == this || MainActivity.this.titleEdit == this) scheduleFormatToolbarStateUpdate();
        }
    }

    private static final class InkPoint {
        final float x;
        final float y;
        final float widthPx;

        InkPoint(float x, float y, float widthPx) {
            this.x = x;
            this.y = y;
            this.widthPx = widthPx;
        }
    }

    private static final class InkStroke {
        final ArrayList<InkPoint> points = new ArrayList<>();
        final int color;

        InkStroke(int color) {
            this.color = color;
        }

        void add(float x, float y, float widthPx) {
            points.add(new InkPoint(x, y, widthPx));
        }
    }

    private final class InkCanvasView extends View {
        private final ArrayList<InkStroke> strokes = new ArrayList<>();
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private InkStroke currentStroke;
        private int activePointerId = -1;
        private boolean hasInk = false;

        InkCanvasView(Context context) {
            super(context);
            setBackgroundColor(Color.WHITE);
            setFocusable(true);
            setFocusableInTouchMode(true);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeCap(Paint.Cap.ROUND);
            strokePaint.setStrokeJoin(Paint.Join.ROUND);
            strokePaint.setColor(Color.rgb(18, 24, 35));
            framePaint.setStyle(Paint.Style.STROKE);
            framePaint.setStrokeWidth(Math.max(1f, dp(1)));
            framePaint.setColor(Color.rgb(185, 195, 210));
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.WHITE);
            drawInk(canvas);
            canvas.drawRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), framePaint);
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event == null) return false;
            requestFocus();
            ViewParentDisallow(true);
            int action = event.getActionMasked();
            int actionIndex = Math.max(0, Math.min(event.getActionIndex(), event.getPointerCount() - 1));
            if (action == MotionEvent.ACTION_DOWN) {
                beginStroke(event, actionIndex);
                return true;
            }
            if (action == MotionEvent.ACTION_POINTER_DOWN) {
                if (currentStroke == null) beginStroke(event, actionIndex);
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                continueStroke(event);
                return true;
            }
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                if (activePointerId == safePointerId(event, actionIndex)) endStroke(event, actionIndex);
                if (action == MotionEvent.ACTION_UP) ViewParentDisallow(false);
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL) {
                currentStroke = null;
                activePointerId = -1;
                ViewParentDisallow(false);
                invalidate();
                return true;
            }
            return true;
        }

        private void ViewParentDisallow(boolean disallow) {
            try {
                ViewParent parent = getParent();
                if (parent != null) parent.requestDisallowInterceptTouchEvent(disallow);
            } catch (Exception ignored) {}
        }

        private void beginStroke(MotionEvent event, int pointerIndex) {
            if (event == null || pointerIndex < 0 || pointerIndex >= event.getPointerCount()) return;
            activePointerId = safePointerId(event, pointerIndex);
            currentStroke = new InkStroke(Color.rgb(18, 24, 35));
            currentStroke.add(event.getX(pointerIndex), event.getY(pointerIndex), strokeWidthFor(event, pointerIndex, -1));
            strokes.add(currentStroke);
            hasInk = true;
            invalidate();
        }

        private void continueStroke(MotionEvent event) {
            if (event == null) return;
            int pointerIndex = activePointerIndex(event);
            if (pointerIndex < 0) {
                if (event.getPointerCount() > 0) beginStroke(event, 0);
                return;
            }
            if (currentStroke == null) beginStroke(event, pointerIndex);
            if (currentStroke == null) return;
            int history = event.getHistorySize();
            for (int i = 0; i < history; i++) {
                currentStroke.add(event.getHistoricalX(pointerIndex, i), event.getHistoricalY(pointerIndex, i), strokeWidthFor(event, pointerIndex, i));
            }
            currentStroke.add(event.getX(pointerIndex), event.getY(pointerIndex), strokeWidthFor(event, pointerIndex, -1));
            invalidate();
        }

        private void endStroke(MotionEvent event, int pointerIndex) {
            if (currentStroke != null && event != null && pointerIndex >= 0 && pointerIndex < event.getPointerCount()) {
                currentStroke.add(event.getX(pointerIndex), event.getY(pointerIndex), strokeWidthFor(event, pointerIndex, -1));
            }
            currentStroke = null;
            activePointerId = -1;
            invalidate();
        }

        private int activePointerIndex(MotionEvent event) {
            if (event == null || event.getPointerCount() <= 0) return -1;
            if (activePointerId < 0) return 0;
            int idx = event.findPointerIndex(activePointerId);
            return idx >= 0 ? idx : 0;
        }

        private int safePointerId(MotionEvent event, int pointerIndex) {
            try { return event.getPointerId(pointerIndex); } catch (Exception ignored) { return -1; }
        }

        private float strokeWidthFor(MotionEvent event, int pointerIndex, int historyIndex) {
            int tool = MotionEvent.TOOL_TYPE_UNKNOWN;
            try { tool = event.getToolType(pointerIndex); } catch (Exception ignored) {}
            float base = tool == MotionEvent.TOOL_TYPE_STYLUS ? dpf(2.8f) : dpf(4.0f);
            float pressure = 1f;
            try {
                pressure = historyIndex >= 0 ? event.getHistoricalPressure(pointerIndex, historyIndex) : event.getPressure(pointerIndex);
            } catch (Exception ignored) {
                try { pressure = event.getPressure(); } catch (Exception ignoredAgain) { pressure = 1f; }
            }
            if (pressure <= 0f) pressure = 1f;
            // Samsung S-Pen pressure is usually 0..1, but Android may report
            // values slightly outside that range on some devices.  Keep a
            // visible minimum and cap extreme spikes so one bad sample does not
            // create a huge blob.
            float factor = LegacyInkPictureModel.pressureWidthFactor(pressure);
            return Math.max(dpf(0.8f), base * factor);
        }

        private void drawInk(Canvas canvas) {
            for (InkStroke stroke : strokes) {
                if (stroke == null || stroke.points.isEmpty()) continue;
                strokePaint.setColor(stroke.color);
                InkPoint previous = null;
                for (InkPoint point : stroke.points) {
                    if (point == null) continue;
                    if (previous == null) {
                        strokePaint.setStrokeWidth(point.widthPx);
                        canvas.drawPoint(point.x, point.y, strokePaint);
                    } else {
                        strokePaint.setStrokeWidth(Math.max(dpf(0.8f), (previous.widthPx + point.widthPx) / 2f));
                        canvas.drawLine(previous.x, previous.y, point.x, point.y, strokePaint);
                    }
                    previous = point;
                }
            }
        }

        boolean hasInk() { return hasInk && !strokes.isEmpty(); }

        void clearInk() {
            strokes.clear();
            currentStroke = null;
            activePointerId = -1;
            hasInk = false;
            invalidate();
        }

        Bitmap renderBitmap() {
            int width = getWidth() > 8 ? getWidth() : dp(LegacyInkPictureModel.DEFAULT_WIDTH_DP);
            int height = getHeight() > 8 ? getHeight() : dp(LegacyInkPictureModel.DEFAULT_HEIGHT_DP);
            width = Math.max(dp(LegacyInkPictureModel.MIN_WIDTH_DP), Math.min(width, dp(LegacyInkPictureModel.MAX_WIDTH_DP)));
            height = Math.max(dp(LegacyInkPictureModel.MIN_HEIGHT_DP), Math.min(height, dp(LegacyInkPictureModel.MAX_HEIGHT_DP)));
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            drawInk(canvas);
            return bitmap;
        }
    }

    private static final class EditorHistoryEntry {
        final SpannableStringBuilder text;
        final int selectionStart;
        final int selectionEnd;
        final String signature;

        EditorHistoryEntry(CharSequence text, int selectionStart, int selectionEnd) {
            this.text = new SpannableStringBuilder(text == null ? "" : text);
            int len = this.text.length();
            this.selectionStart = Math.max(0, Math.min(selectionStart, len));
            this.selectionEnd = Math.max(0, Math.min(selectionEnd, len));
            this.signature = buildSignature(this.text);
        }

        private static String buildSignature(SpannableStringBuilder text) {
            if (text == null) return "";
            ArrayList<String> parts = new ArrayList<>();
            int len = text.length();
            Object[] spans = text.getSpans(0, len, Object.class);
            for (Object span : spans) {
                if (span == null) continue;
                String detail = spanDetail(span);
                if (detail.isEmpty()) continue;
                parts.add(text.getSpanStart(span) + ":" + text.getSpanEnd(span) + ":" + text.getSpanFlags(span) + ":" + detail);
            }
            Collections.sort(parts);
            StringBuilder out = new StringBuilder(text.toString());
            out.append("\n@@spans=");
            for (String part : parts) out.append(part).append('|');
            return out.toString();
        }

        private static String spanDetail(Object span) {
            if (span instanceof StyleSpan) return "style:" + ((StyleSpan) span).getStyle();
            if (span instanceof UnderlineSpan) return "underline";
            if (span instanceof StrikethroughSpan) return "strike";
            if (span instanceof ForegroundColorSpan) return "fg:" + ((ForegroundColorSpan) span).getForegroundColor();
            if (span instanceof BackgroundColorSpan) return "bg:" + ((BackgroundColorSpan) span).getBackgroundColor();
            if (span instanceof AbsoluteSizeSpan) return "size:" + ((AbsoluteSizeSpan) span).getSize() + ":" + ((AbsoluteSizeSpan) span).getDip();
            if (span instanceof RtfTypefaceSpan) return "font:" + ((RtfTypefaceSpan) span).familyName;
            if (span instanceof SuperscriptSpan) return "super";
            if (span instanceof SubscriptSpan) return "sub";
            if (span instanceof URLSpan) return "url:" + ((URLSpan) span).getURL();
            if (span instanceof RtfParagraphAlignmentSpan) return "align:" + ((RtfParagraphAlignmentSpan) span).legacyAlign + ":" + ((RtfParagraphAlignmentSpan) span).getAlignment();
            if (span instanceof AlignmentSpan) return "align:" + ((AlignmentSpan) span).getAlignment();
            if (span instanceof LeadingMarginSpan.Standard) return "margin:" + ((LeadingMarginSpan.Standard) span).getLeadingMargin(true) + ":" + ((LeadingMarginSpan.Standard) span).getLeadingMargin(false);
            if (span instanceof RtfImageSpan) {
                RtfImageSpan image = (RtfImageSpan) span;
                return "image:" + image.mimeType + ":" + image.imageData.length + ":" + image.widthTwips + ":" + image.heightTwips + ":" + sampledTextHash(image.rawRtf);
            }
            if (span instanceof RtfRawSpan) {
                RtfRawSpan raw = (RtfRawSpan) span;
                return "raw:" + raw.kind + ":" + raw.rawRtf.length() + ":" + sampledTextHash(raw.rawRtf);
            }
            return "";
        }

        private static int sampledTextHash(String text) {
            if (text == null || text.isEmpty()) return 0;
            int len = text.length();
            if (len <= 8192) return text.hashCode();
            int h = 146959810;
            h = 31 * h + len;
            for (int i = 0; i < 4096; i++) h = 31 * h + text.charAt(i);
            for (int i = Math.max(4096, len - 4096); i < len; i++) h = 31 * h + text.charAt(i);
            return h;
        }
    }

    private static final class RtfParagraphAlignmentSpan implements AlignmentSpan {
        final Layout.Alignment alignment;
        final String legacyAlign;
        RtfParagraphAlignmentSpan(Layout.Alignment alignment, String legacyAlign) {
            this.alignment = alignment == null ? Layout.Alignment.ALIGN_NORMAL : alignment;
            this.legacyAlign = legacyAlign == null ? "" : legacyAlign;
        }
        @Override public Layout.Alignment getAlignment() { return alignment; }
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

    private static final class PreparedImage {
        final byte[] bytes;
        final String mimeType;
        final String note;
        PreparedImage(byte[] bytes, String mimeType, String note) {
            this.bytes = bytes == null ? new byte[0] : bytes;
            this.mimeType = mimeType == null ? "" : mimeType;
            this.note = note == null ? "" : note;
        }
    }

    private static final class SizeRun {
        final int start;
        final int end;
        final int size;
        SizeRun(int start, int end, int size) {
            this.start = start;
            this.end = end;
            this.size = size;
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
            internalEditorClipboard = retained.internalEditorClipboard == null ? null : new SpannableStringBuilder(retained.internalEditorClipboard);
            internalEditorClipboardPlain = retained.internalEditorClipboardPlain == null ? "" : retained.internalEditorClipboardPlain;
            restoreTreeHistoryStacks(retained.treeUndoStack, retained.treeRedoStack);
            currentFtpTarget = retained.currentFtpTarget;
            settings = retained.settings == null ? AndroidSettingsStore.load(this) : retained.settings;
            editorHorizontallyScrolling = retained.editorHorizontallyScrolling;
            lastFormatTarget = retained.lastFormatTarget == null ? FormatTarget.RTF_EDITOR : retained.lastFormatTarget;
            lastActivePane = retained.lastActivePane == null ? (lastFormatTarget == FormatTarget.TREE_NODE ? ActivePane.TREE_NODE : ActivePane.RTF_EDITOR) : retained.lastActivePane;
            treePaneWidthDp = retained.treePaneWidthDp > 0 ? LegacySettings.normalizeAndroidTreePaneWidthDp(retained.treePaneWidthDp) : LegacySettings.normalizeAndroidTreePaneWidthDp(settings.androidTreePaneWidthDp);
            toolbarButtonDp = retained.toolbarButtonDp > 0 ? LegacySettings.normalizeAndroidToolbarButtonDp(retained.toolbarButtonDp) : LegacySettings.normalizeAndroidToolbarButtonDp(settings.androidToolbarButtonDp);
            headerTextSp = retained.headerTextSp > 0f ? LegacySettings.normalizeAndroidHeaderTextSp(retained.headerTextSp) : LegacySettings.normalizeAndroidHeaderTextSp(settings.androidHeaderTextSp);
            markdownPreviewActive = false;
            markdownPreviewNode = retained.markdownPreviewActive ? retained.markdownPreviewNode : null;
            markdownPreviewSourceRtf = retained.markdownPreviewSourceRtf == null ? "" : retained.markdownPreviewSourceRtf;
            markdownPreviewReturnSelectionStart = retained.markdownPreviewReturnSelectionStart;
            markdownPreviewReturnSelectionEnd = retained.markdownPreviewReturnSelectionEnd;
            markdownPreviewReturnScrollX = retained.markdownPreviewReturnScrollX;
            markdownPreviewReturnScrollY = retained.markdownPreviewReturnScrollY;
        } else {
            settings = AndroidSettingsStore.load(this);
            treePaneWidthDp = LegacySettings.normalizeAndroidTreePaneWidthDp(settings.androidTreePaneWidthDp);
            toolbarButtonDp = LegacySettings.normalizeAndroidToolbarButtonDp(settings.androidToolbarButtonDp);
            headerTextSp = LegacySettings.normalizeAndroidHeaderTextSp(settings.androidHeaderTextSp);
            if (!hasOpenIntent && settings.androidRestoreRuntimeSnapshot) runtimeSnapshotRestored = restoreRuntimeSnapshotIfPresent();
        }
        pendingWidgetNodePath = widgetNodePathFromIntent(getIntent());
        ioExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        installCrashSnapshotHandler();
        buildUi();
        if (retained != null) restoreQuickSearchUi(retained);
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
            if (retained.markdownPreviewActive && markdownPreviewNode == currentNode) activateMarkdownPreviewForCurrentNode(false);
            selectPendingWidgetNodeIfAny();
        } else {
            selectNode(currentNode == null ? document.ensureRoot() : currentNode, false);
            boolean openedByIntent = handleViewIntent(getIntent());
            if (!openedByIntent) consumeOpenOnceFileIfPresent();
            selectPendingWidgetNodeIfAny();
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
        retained.internalEditorClipboard = internalEditorClipboard == null ? null : new SpannableStringBuilder(internalEditorClipboard);
        retained.internalEditorClipboardPlain = internalEditorClipboardPlain == null ? "" : internalEditorClipboardPlain;
        retained.treeUndoStack = copyTreeHistoryStack(treeUndoStack);
        retained.treeRedoStack = copyTreeHistoryStack(treeRedoStack);
        retained.settings = settings;
        retained.currentFtpTarget = currentFtpTarget;
        retained.editorHorizontallyScrolling = editorHorizontallyScrolling;
        retained.selectionStart = editor == null ? 0 : Math.max(0, editor.getSelectionStart());
        retained.selectionEnd = editor == null ? retained.selectionStart : Math.max(retained.selectionStart, editor.getSelectionEnd());
        retained.treePaneWidthDp = currentTreePaneWidthDp();
        retained.toolbarButtonDp = toolbarButtonDp;
        retained.headerTextSp = headerTextSp;
        retained.lastFormatTarget = lastFormatTarget;
        retained.lastActivePane = lastActivePane;
        retained.quickSearchTerm = quickSearchInput == null ? searchSession.cachedTerm() : quickSearchInput.getText().toString();
        retained.quickSearchWholeTree = quickSearchWholeTree != null && quickSearchWholeTree.isChecked();
        retained.quickSearchWholeWords = quickSearchWholeWords != null && quickSearchWholeWords.isChecked();
        retained.quickSearchCaseSensitive = quickSearchCaseSensitive != null && quickSearchCaseSensitive.isChecked();
        retained.quickSearchIncludeTitles = quickSearchIncludeTitles == null || quickSearchIncludeTitles.isChecked();
        retained.quickSearchVisible = quickSearchBar != null && quickSearchBar.getVisibility() == View.VISIBLE;
        retained.markdownPreviewActive = markdownPreviewActive;
        retained.markdownPreviewNode = markdownPreviewNode;
        retained.markdownPreviewSourceRtf = markdownPreviewSourceRtf;
        retained.markdownPreviewReturnSelectionStart = markdownPreviewReturnSelectionStart;
        retained.markdownPreviewReturnSelectionEnd = markdownPreviewReturnSelectionEnd;
        retained.markdownPreviewReturnScrollX = markdownPreviewReturnScrollX;
        retained.markdownPreviewReturnScrollY = markdownPreviewReturnScrollY;
        return retained;
    }

    private void restoreQuickSearchUi(RetainedState retained) {
        if (retained == null) return;
        if (quickSearchInput != null && retained.quickSearchTerm != null) {
            quickSearchInput.setText(retained.quickSearchTerm);
            quickSearchInput.setSelection(quickSearchInput.getText().length());
        }
        if (quickSearchWholeTree != null) quickSearchWholeTree.setChecked(retained.quickSearchWholeTree);
        if (quickSearchWholeWords != null) quickSearchWholeWords.setChecked(retained.quickSearchWholeWords);
        if (quickSearchCaseSensitive != null) quickSearchCaseSensitive.setChecked(retained.quickSearchCaseSensitive);
        if (quickSearchIncludeTitles != null) quickSearchIncludeTitles.setChecked(retained.quickSearchIncludeTitles);
        if (quickSearchBar != null) quickSearchBar.setVisibility(retained.quickSearchVisible ? View.VISIBLE : View.GONE);
    }

    @Override protected void onPause() {
        cancelDelayedEditorContextMenu();
        cancelDelayedTreeContextMenu();
        flushAndroidUiZoomSettingsNow();
        saveRuntimeSnapshotNow(true);
        super.onPause();
    }

    @Override protected void onStop() {
        cancelDelayedEditorContextMenu();
        cancelDelayedTreeContextMenu();
        flushAndroidUiZoomSettingsNow();
        saveRuntimeSnapshotNow(true);
        super.onStop();
    }

    @Override protected void onDestroy() {
        flushAndroidUiZoomSettingsNow();
        saveRuntimeSnapshotNow(true);
        cancelDelayedEditorContextMenu();
        cancelDelayedTreeContextMenu();
        if (mainHandler != null && autosaveRunnable != null) mainHandler.removeCallbacks(autosaveRunnable);
        if (mainHandler != null && runtimeSnapshotRunnable != null) mainHandler.removeCallbacks(runtimeSnapshotRunnable);
        if (mainHandler != null && androidUiZoomSaveRunnable != null) mainHandler.removeCallbacks(androidUiZoomSaveRunnable);
        if (previousUncaughtExceptionHandler != null) Thread.setDefaultUncaughtExceptionHandler(previousUncaughtExceptionHandler);
        if (ioExecutor != null) ioExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (isWidgetOpenIntent(intent)) {
            pendingWidgetNodePath = widgetNodePathFromIntent(intent);
            selectPendingWidgetNodeIfAny();
            return;
        }
        handleViewIntent(intent);
    }

    private void buildUi() {
        toolbarSquareViews.clear();
        initPinchGestureDetectors();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(250, 250, 250));

        ToolbarZoomLayout toolbarPanel = new ToolbarZoomLayout(this);
        toolbarPanel.setOrientation(LinearLayout.VERTICAL);
        toolbarPanel.setPadding(dp(4), dp(1), dp(4), dp(2));
        toolbarPanel.setBackgroundColor(Color.rgb(246, 248, 252));
        root.addView(toolbarPanel, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout fileToolbar = addToolbarRow(toolbarPanel, "Datei");
        LinearLayout treeToolbar = addToolbarRow(toolbarPanel, "Baum/Text");
        LinearLayout textToolbar = addToolbarRow(toolbarPanel, "Text");

        addButton(fileToolbar, "Leer", v -> confirmDiscardThen(this::newEmptyDocument));
        addButton(fileToolbar, "Neu", v -> confirmDiscardThen(this::newDocument));
        addButton(fileToolbar, "Öffnen", v -> confirmDiscardThen(this::openDocument));
        addButton(fileToolbar, "Letzte", v -> showRecentFiles());
        addButton(fileToolbar, "Speichern", v -> saveDocument());
        addButton(fileToolbar, "Speichern unter", v -> saveDocumentAs());
        addButton(fileToolbar, "Sicherungen", v -> showBackups());
        addButton(fileToolbar, "Einstellungen", v -> showSettingsDialog());
        if (settings == null || settings.androidShowDiagnosticsToolbar) addButton(fileToolbar, "Config", v -> showConfigSnapshot());
        addButton(fileToolbar, "FTP öffnen", v -> showFtpDialog(false));
        addButton(fileToolbar, "FTP speichern", v -> showFtpDialog(true));
        addButton(fileToolbar, "Schließen", v -> closeDocumentLegacy());
        if (settings == null || settings.androidShowDiagnosticsToolbar) addButton(fileToolbar, "Status", v -> showLifecycleStatus());
        addButton(fileToolbar, "Vorschau", v -> showRichPreview());
        addButton(fileToolbar, "Drucken", v -> showPrintDialog());
        addButton(fileToolbar, "Export", v -> showExportHubDialog());
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
        if (settings == null || settings.androidShowDiagnosticsToolbar) {
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
        }
        addButton(fileToolbar, "Feedback", v -> showFeedbackDialog());
        addButton(fileToolbar, "Info", v -> showInfo());

        addButton(treeToolbar, "Kind", v -> newChildForActiveMiddleTarget());
        addButton(treeToolbar, "Daneben", v -> newNextForActiveMiddleTarget());
        addButton(treeToolbar, "Auf/Zu", v -> toggleExpandedForActiveMiddleTarget());
        addButton(treeToolbar, "Alle auf", v -> expandAllForActiveMiddleTarget());
        addButton(treeToolbar, "Alle zu", v -> collapseAllForActiveMiddleTarget());
        addButton(treeToolbar, "Löschen", v -> deleteForActiveMiddleTarget());
        addButton(treeToolbar, "Kopieren", v -> copyForActiveMiddleTarget());
        addButton(treeToolbar, "Ausschneiden", v -> cutForActiveMiddleTarget());
        addButton(treeToolbar, "Einfügen", v -> pasteForActiveMiddleTarget());
        addButton(treeToolbar, "Rauf", v -> moveUpForActiveMiddleTarget());
        addButton(treeToolbar, "Runter", v -> moveDownForActiveMiddleTarget());
        addButton(treeToolbar, "Einrücken", v -> indentForActiveMiddleTarget());
        addButton(treeToolbar, "Ausrücken", v -> outdentForActiveMiddleTarget());
        addButton(treeToolbar, "Vor Ziel", v -> moveBeforeForActiveMiddleTarget());
        addButton(treeToolbar, "Haftnotiz", v -> desktopNoteForActiveMiddleTarget());
        addButton(treeToolbar, "Haft Layout", v -> desktopNoteLayoutForActiveMiddleTarget());
        addButton(treeToolbar, "Haftliste", v -> desktopNoteTrayForActiveMiddleTarget());
        addButton(treeToolbar, "Haft weg", v -> clearDesktopNotesForActiveMiddleTarget());
        addButton(treeToolbar, "Wecker", v -> alarmForActiveMiddleTarget());

        addButton(textToolbar, "Datum", v -> insertDateForActiveTarget());
        addButton(textToolbar, "Punkt", v -> insertLegacyBulletForActiveTarget());
        addButton(textToolbar, "Rückgängig", v -> undoForActiveTarget());
        addButton(textToolbar, "Wiederholen", v -> redoForActiveTarget());
        addFormatButton(textToolbar, "Normal", "format_regular");
        addFormatButton(textToolbar, "Fett", "format_bold");
        addFormatButton(textToolbar, "Kursiv", "format_italic");
        addFormatButton(textToolbar, "Unterstrichen", "format_underline");
        addFormatButton(textToolbar, "Durchgestrichen", "format_strike");
        addFormatButton(textToolbar, "Textfarbe", "text_color");
        addFormatButton(textToolbar, "Hintergrund", "highlight_color");
        addFormatButton(textToolbar, "Links", "align_left");
        addFormatButton(textToolbar, "Mitte", "align_center");
        addFormatButton(textToolbar, "Rechts", "align_right");
        addFormatButton(textToolbar, "Blocksatz", "align_justify");
        addFormatButton(textToolbar, "Größer", "font_bigger");
        addFormatButton(textToolbar, "Kleiner", "font_smaller");
        addFormatButton(textToolbar, "Schriftart", "font_family");
        addFormatButton(textToolbar, "Größe", "font_size");
        addButton(textToolbar, "RTF Format", v -> showRtfFormatDialog());
        addButton(textToolbar, "Scroll", v -> cycleScrollbars());
        addButton(textToolbar, "Bild", v -> insertImage());
        addButton(textToolbar, "Stift", v -> insertInkDrawing());
        markdownPreviewButton = addButton(textToolbar, "Markdown", v -> toggleMarkdownPreview());
        addButton(textToolbar, "RTF Info", v -> showRtfInfoDialog());
        addButton(textToolbar, "HTML Import", v -> importHtmlNote());
        addButton(textToolbar, "TXT Import", v -> importTextIntoCurrent());
        addButton(textToolbar, "RTF Import", v -> importRtfIntoCurrent());
        addButton(textToolbar, "Farben", v -> showColorDialog());

        quickSearchBar = createQuickSearchBar();
        root.addView(quickSearchBar, new LinearLayout.LayoutParams(-1, -2));

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
        rootTitleView.setTextSize(headerTextSp);
        rootTitleView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) setActivePane(ActivePane.TREE_NODE);
            return handleHeaderPinchTouch(event);
        });
        left.addView(rootTitleView, new LinearLayout.LayoutParams(-1, dp(CONTENT_HEADER_DP)));
        treeList = new ListView(this);
        treeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        treeList.setFocusable(true);
        treeList.setFocusableInTouchMode(true);
        treeList.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setActivePane(ActivePane.TREE_NODE); });
        treeList.setBackground(roundedBackground(Color.WHITE, Color.rgb(220, 225, 232), 8));
        treeList.setDividerHeight(1);
        treeAdapter = new TreeListAdapter(this);
        treeAdapter.setDefaultTreeTextSizeSp(settings == null ? 16f : LegacySettings.normalizeAndroidTreeTextSp(settings.androidTreeTextSp));
        treeList.setAdapter(treeAdapter);
        final float[] lastTreeTouchX = new float[]{-1f};
        treeList.setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                lastTreeTouchX[0] = event.getX();
                setActivePane(ActivePane.TREE_NODE);
                armDelayedTreeContextMenu(event);
            } else if (action == MotionEvent.ACTION_MOVE) {
                updateDelayedTreeContextMenu(event);
            } else if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                cancelDelayedTreeContextMenu();
            }
            return handleTreePinchTouch(event);
        });
        treeList.setOnItemClickListener((parent, view, position, id) -> {
            TreeListAdapter.FlatNode row = treeAdapter.getItem(position);
            NoteNode node = row.node;
            int toggleEdge = dp(42 + row.depth * 22);
            if (!node.children.isEmpty() && lastTreeTouchX[0] >= 0f && lastTreeTouchX[0] <= toggleEdge) {
                setActivePane(ActivePane.TREE_NODE);
                toggleNodeExpanded(node, true);
            } else {
                setActivePane(ActivePane.TREE_NODE);
                treeList.requestFocus();
                selectNode(node, false);
            }
        });
        treeList.setOnItemLongClickListener((parent, view, position, id) -> {
            TreeListAdapter.FlatNode row = treeAdapter.getItem(position);
            NoteNode node = row.node;
            setActivePane(ActivePane.TREE_NODE);
            treeList.requestFocus();
            selectNode(node, false);
            toggleNodeExpanded(node, true);
            return true;
        });
        treeList.setOnDragListener((view, event) -> handleTreeDragEvent(event));
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
        titleEdit = new TrackingEditText(this);
        titleEdit.setSingleLine(true);
        titleEdit.setIncludeFontPadding(false);
        titleEdit.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        titleEdit.setMinHeight(0);
        titleEdit.setMinimumHeight(0);
        titleEdit.setTextSize(headerTextSp);
        titleEdit.setBackground(roundedBackground(Color.rgb(255, 250, 205), Color.rgb(226, 213, 145), 8));
        titleEdit.setPadding(dp(7), 0, dp(7), 0);
        titleEdit.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setActivePane(ActivePane.TITLE_TEXT); });
        titleEdit.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) setActivePane(ActivePane.TITLE_TEXT);
            return handleHeaderPinchTouch(event);
        });
        titleEdit.addTextChangedListener(new SimpleWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!loadingEditor && currentNode != null && !titleDirty) pushTreeUndoSnapshot("Knotentitel bearbeiten");
            }
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

        editor = new TrackingEditText(this);
        editorEditableKeyListener = editor.getKeyListener();
        editorEditableInputType = editor.getInputType();
        editor.setTextSize(settings == null ? 17f : LegacySettings.normalizeAndroidEditorTextSp(settings.androidEditorTextSp));
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setMinLines(12);
        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(false);
        restoreEditorSelectionVisuals();
        editor.setBackground(roundedBackground(Color.WHITE, Color.rgb(213, 219, 229), 8));
        applyEditorScrollbars();
        editor.setPadding(dp(10), dp(10), dp(10), dp(10));
        editor.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setActivePane(ActivePane.RTF_EDITOR); });
        editor.setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                setActivePane(ActivePane.RTF_EDITOR);
                armDelayedEditorContextMenu(event);
            } else if (action == MotionEvent.ACTION_MOVE) {
                updateDelayedEditorContextMenu(event);
            } else if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                cancelDelayedEditorContextMenu();
            }
            return handleEditorPinchTouch(event);
        });
        editor.setOnLongClickListener(view -> {
            setActivePane(ActivePane.RTF_EDITOR);
            editor.requestFocus();
            selectEditorWordAtLastTouch();
            return true;
        });
        editor.addTextChangedListener(new SimpleWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!loadingEditor && !editorHistoryRestoring) maybePushEditorTypingUndoSnapshot();
            }
            @Override public void afterTextChanged(Editable s) {
                if (loadingEditor) return;
                if (!editorHistoryRestoring) editorRedoStack.clear();
                editorDirty = true;
                markDocumentChanged();
                updateTitle();
                scheduleFormatToolbarStateUpdate();
            }
        });
        right.addView(editor, new LinearLayout.LayoutParams(-1, 0, 1));

        markdownPreviewWebView = new WebView(this);
        markdownPreviewWebView.setVisibility(View.GONE);
        markdownPreviewWebView.setBackgroundColor(Color.WHITE);
        markdownPreviewWebView.setFocusable(true);
        markdownPreviewWebView.setFocusableInTouchMode(true);
        markdownPreviewWebView.getSettings().setJavaScriptEnabled(false);
        markdownPreviewWebView.getSettings().setDomStorageEnabled(false);
        markdownPreviewWebView.setWebViewClient(new WebViewClient());
        right.addView(markdownPreviewWebView, new LinearLayout.LayoutParams(-1, 0, 1));

        content.addView(right, wide ? new LinearLayout.LayoutParams(0, -1, 1.0f) : new LinearLayout.LayoutParams(-1, 0, 1));
        if (wide) content.post(() -> applyTreePaneWidthPx(dp(treePaneWidthDp), false));
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);
        updateMarkdownPreviewButtonState();
        updateTitle();
        rebuildTree();
    }

    private boolean isOpenDocumentIntent(Intent intent) {
        return intent != null && intent.getData() != null && Intent.ACTION_VIEW.equals(intent.getAction());
    }

    private boolean isWidgetOpenIntent(Intent intent) {
        return intent != null && NoteWidgetProvider.ACTION_OPEN_WIDGET_NODE.equals(intent.getAction());
    }

    private String widgetNodePathFromIntent(Intent intent) {
        if (!isWidgetOpenIntent(intent)) return null;
        return intent.getStringExtra(NoteWidgetProvider.EXTRA_NODE_PATH);
    }

    private void selectPendingWidgetNodeIfAny() {
        if (pendingWidgetNodePath == null) return;
        String path = pendingWidgetNodePath;
        pendingWidgetNodePath = null;
        NoteNode target = LegacyAndroidWidgetRegistry.nodeByIndexPath(document == null ? null : document.ensureRoot(), path);
        if (target == null) target = document == null ? null : document.ensureRoot();
        if (target != null) {
            ensureAncestorsExpanded(target);
            rebuildTree();
            selectNode(target, true);
            status("Haftnotiz-Widget geöffnet: " + safeTitle(target));
        }
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
        } catch (Throwable ignored) {
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
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmp, false);
            out.write(data == null ? new byte[0] : data);
            out.getFD().sync();
        } finally {
            if (out != null) try { out.close(); } catch (Exception ignored) {}
        }
        if (!tmp.renameTo(file)) {
            FileOutputStream fallback = null;
            try {
                fallback = new FileOutputStream(file, false);
                fallback.write(data == null ? new byte[0] : data);
                fallback.getFD().sync();
            } finally {
                if (fallback != null) try { fallback.close(); } catch (Exception ignored) {}
                tmp.delete();
            }
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

    private abstract class StepScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float accumulated = 1f;

        @Override public boolean onScaleBegin(ScaleGestureDetector detector) {
            accumulated = 1f;
            return true;
        }

        @Override public boolean onScale(ScaleGestureDetector detector) {
            if (detector == null) return true;
            float factor = detector.getScaleFactor();
            if (Float.isNaN(factor) || Float.isInfinite(factor) || factor <= 0f) return true;
            accumulated *= factor;
            int steps = 0;
            while (accumulated >= PINCH_STEP_FACTOR && steps < PINCH_MAX_STEPS_PER_EVENT) {
                onStep(+1);
                accumulated /= PINCH_STEP_FACTOR;
                steps++;
            }
            float shrink = 1f / PINCH_STEP_FACTOR;
            steps = 0;
            while (accumulated <= shrink && steps < PINCH_MAX_STEPS_PER_EVENT) {
                onStep(-1);
                accumulated *= PINCH_STEP_FACTOR;
                steps++;
            }
            if (accumulated > PINCH_STEP_FACTOR * PINCH_STEP_FACTOR) accumulated = PINCH_STEP_FACTOR;
            if (accumulated < shrink * shrink) accumulated = shrink;
            return true;
        }

        @Override public void onScaleEnd(ScaleGestureDetector detector) {
            accumulated = 1f;
            onGestureFinished();
        }

        abstract void onStep(int direction);
        void onGestureFinished() {}
    }

    private void ensureMainHandler() {
        if (mainHandler == null) mainHandler = new Handler(Looper.getMainLooper());
    }

    private void armDelayedEditorContextMenu(MotionEvent event) {
        if (event == null || editor == null) return;
        cancelDelayedEditorContextMenu();
        editorLongPressArmed = true;
        editorLongDownX = event.getX();
        editorLongDownY = event.getY();
        editorLastTouchX = editorLongDownX;
        editorLastTouchY = editorLongDownY;
        ensureMainHandler();
        delayedEditorContextRunnable = () -> {
            if (!editorLongPressArmed || editor == null || editor.getVisibility() != View.VISIBLE) return;
            editorLongPressArmed = false;
            delayedEditorContextRunnable = null;
            setActivePane(ActivePane.RTF_EDITOR);
            editor.requestFocus();
            showEditorContextMenu();
        };
        mainHandler.postDelayed(delayedEditorContextRunnable, DELAYED_CONTEXT_MENU_MS);
    }

    private void updateDelayedEditorContextMenu(MotionEvent event) {
        if (!editorLongPressArmed || event == null) return;
        editorLastTouchX = event.getX();
        editorLastTouchY = event.getY();
        if (event.getPointerCount() > 1 || movedTooFar(event.getX(), event.getY(), editorLongDownX, editorLongDownY)) {
            cancelDelayedEditorContextMenu();
        }
    }

    private void cancelDelayedEditorContextMenu() {
        editorLongPressArmed = false;
        if (mainHandler != null && delayedEditorContextRunnable != null) mainHandler.removeCallbacks(delayedEditorContextRunnable);
        delayedEditorContextRunnable = null;
    }

    private boolean selectEditorWordAtLastTouch() {
        if (editor == null) return false;
        Editable text = editor.getText();
        int length = text == null ? 0 : text.length();
        if (length <= 0) return false;
        int offset;
        try {
            offset = editor.getOffsetForPosition(editorLastTouchX, editorLastTouchY);
        } catch (RuntimeException ex) {
            offset = editor.getSelectionStart();
        }
        if (offset < 0) offset = 0;
        if (offset >= length) offset = length - 1;
        if (!isEditorWordChar(text.charAt(offset)) && offset > 0 && isEditorWordChar(text.charAt(offset - 1))) offset--;
        if (!isEditorWordChar(text.charAt(offset))) {
            int cursor = Math.max(0, Math.min(length, offset));
            editor.setSelection(cursor);
            return false;
        }
        int start = offset;
        int end = offset + 1;
        while (start > 0 && isEditorWordChar(text.charAt(start - 1))) start--;
        while (end < length && isEditorWordChar(text.charAt(end))) end++;
        editor.setSelection(start, end);
        return true;
    }

    private boolean isEditorWordChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    private void armDelayedTreeContextMenu(MotionEvent event) {
        if (event == null || treeList == null || treeAdapter == null) return;
        cancelDelayedTreeContextMenu();
        int position = treeList.pointToPosition(Math.round(event.getX()), Math.round(event.getY()));
        if (position == AdapterView.INVALID_POSITION) return;
        TreeListAdapter.FlatNode row = treeAdapter.getItem(position);
        if (row == null || row.node == null) return;
        treeLongPressArmed = true;
        treeLongDownX = event.getX();
        treeLongDownY = event.getY();
        treeLongContextNode = row.node;
        treeLongContextPosition = position;
        ensureMainHandler();
        delayedTreeContextRunnable = () -> {
            if (!treeLongPressArmed || treeLongContextNode == null || treeList == null) return;
            NoteNode node = treeLongContextNode;
            treeLongPressArmed = false;
            delayedTreeContextRunnable = null;
            setActivePane(ActivePane.TREE_NODE);
            treeList.requestFocus();
            selectNode(node, false);
            showTreeContextMenu(node);
        };
        mainHandler.postDelayed(delayedTreeContextRunnable, DELAYED_CONTEXT_MENU_MS);
    }

    private void updateDelayedTreeContextMenu(MotionEvent event) {
        if (!treeLongPressArmed || event == null) return;
        if (event.getPointerCount() > 1 || movedTooFar(event.getX(), event.getY(), treeLongDownX, treeLongDownY)) {
            cancelDelayedTreeContextMenu();
            return;
        }
        if (treeList != null) {
            int position = treeList.pointToPosition(Math.round(event.getX()), Math.round(event.getY()));
            if (position == AdapterView.INVALID_POSITION || position != treeLongContextPosition) cancelDelayedTreeContextMenu();
        }
    }

    private void cancelDelayedTreeContextMenu() {
        treeLongPressArmed = false;
        treeLongContextNode = null;
        treeLongContextPosition = AdapterView.INVALID_POSITION;
        if (mainHandler != null && delayedTreeContextRunnable != null) mainHandler.removeCallbacks(delayedTreeContextRunnable);
        delayedTreeContextRunnable = null;
    }

    private boolean movedTooFar(float x, float y, float startX, float startY) {
        float dx = x - startX;
        float dy = y - startY;
        float limit = dp(36);
        return dx * dx + dy * dy > limit * limit;
    }

    private void initPinchGestureDetectors() {
        editorScaleDetector = new ScaleGestureDetector(this, new StepScaleListener() {
            @Override void onStep(int direction) { zoomRtfTextByStep(direction); }
        });
        treeScaleDetector = new ScaleGestureDetector(this, new StepScaleListener() {
            @Override void onStep(int direction) { zoomTreeTextByStep(direction); }
        });
        toolbarScaleDetector = new ScaleGestureDetector(this, new StepScaleListener() {
            @Override void onStep(int direction) { zoomToolbarButtonsByStep(direction); }
            @Override void onGestureFinished() { flushAndroidUiZoomSettingsNow(); }
        });
        headerScaleDetector = new ScaleGestureDetector(this, new StepScaleListener() {
            @Override void onStep(int direction) { zoomHeaderTextByStep(direction); }
            @Override void onGestureFinished() { flushAndroidUiZoomSettingsNow(); }
        });
    }

    private boolean handleEditorPinchTouch(MotionEvent event) {
        safeScaleGesture(editorScaleDetector, event, PinchArea.EDITOR);
        return updatePinchActive(event, PinchArea.EDITOR);
    }

    private boolean handleTreePinchTouch(MotionEvent event) {
        safeScaleGesture(treeScaleDetector, event, PinchArea.TREE);
        return updatePinchActive(event, PinchArea.TREE);
    }

    private boolean handleToolbarPinchTouch(MotionEvent event) {
        safeScaleGesture(toolbarScaleDetector, event, PinchArea.TOOLBAR);
        return updatePinchActive(event, PinchArea.TOOLBAR);
    }

    private boolean handleHeaderPinchTouch(MotionEvent event) {
        safeScaleGesture(headerScaleDetector, event, PinchArea.HEADER);
        return updatePinchActive(event, PinchArea.HEADER);
    }

    private void safeScaleGesture(ScaleGestureDetector detector, MotionEvent event, PinchArea area) {
        if (detector == null || event == null) return;
        try {
            detector.onTouchEvent(event);
        } catch (RuntimeException ex) {
            setPinchActive(area, false);
        }
    }

    private enum PinchArea { EDITOR, TREE, TOOLBAR, HEADER }

    private boolean updatePinchActive(MotionEvent event, PinchArea area) {
        if (event == null || area == null) return false;
        int action = event.getActionMasked();
        if (event.getPointerCount() >= 2 || action == MotionEvent.ACTION_POINTER_DOWN) setPinchActive(area, true);
        boolean active = isPinchActive(area);
        boolean consume = active || event.getPointerCount() >= 2;
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            consume = active;
            setPinchActive(area, false);
        }
        return consume;
    }

    private boolean isPinchActive(PinchArea area) {
        switch (area) {
            case EDITOR: return editorPinchActive;
            case TREE: return treePinchActive;
            case TOOLBAR: return toolbarPinchActive;
            case HEADER: return headerPinchActive;
            default: return false;
        }
    }

    private void setPinchActive(PinchArea area, boolean active) {
        switch (area) {
            case EDITOR: editorPinchActive = active; break;
            case TREE: treePinchActive = active; break;
            case TOOLBAR: toolbarPinchActive = active; break;
            case HEADER: headerPinchActive = active; break;
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
        caption.setTextSize(toolbarGlyphTextSize(toolbarRowGlyph(label)));
        caption.setTypeface(Typeface.DEFAULT_BOLD);
        caption.setTextColor(Color.rgb(72, 84, 102));
        caption.setContentDescription(label + "-Leiste");
        if (Build.VERSION.SDK_INT >= 26) caption.setTooltipText(label + "-Leiste");
        caption.setBackground(roundedBackground(Color.rgb(238, 242, 248), Color.rgb(210, 218, 230), 8));
        toolbarSquareViews.add(caption);
        LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(dp(toolbarButtonDp), dp(toolbarButtonDp));
        captionParams.setMargins(dp(TOOLBAR_BUTTON_MARGIN_DP), dp(1), dp(3), dp(1));
        row.addView(caption, captionParams);

        scroll.addView(row, new HorizontalScrollView.LayoutParams(-2, -2));
        parent.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }


    private View.OnClickListener safeClick(String label, View.OnClickListener listener) {
        return view -> {
            try {
                listener.onClick(view);
            } catch (Throwable t) {
                handleRecoverableUiFailure(label, t);
            }
        };
    }

    private ContinueCallback safeAction(String label, ContinueCallback action) {
        return () -> {
            try {
                action.run();
            } catch (Throwable t) {
                handleRecoverableUiFailure(label, t);
            }
        };
    }

    private void handleRecoverableUiFailure(String label, Throwable throwable) {
        try { saveRuntimeSnapshotNow(Looper.myLooper() == Looper.getMainLooper()); } catch (Throwable ignored) {}
        if (throwable instanceof OutOfMemoryError) {
            try { System.gc(); } catch (Throwable ignored) {}
        }
        String action = label == null || label.trim().isEmpty() ? "Aktion" : label;
        error(action, recoverableFailureMessage(throwable));
    }

    private String recoverableFailureMessage(Throwable throwable) {
        if (throwable instanceof OutOfMemoryError) return "Android hatte nicht genug freien Speicher. Der letzte sinnvolle Stand wurde soweit möglich gesichert. Bitte große Bilder/Dateien verkleinern oder die App neu öffnen.";
        if (throwable instanceof StackOverflowError) return "Der Baum oder RTF-Inhalt ist zu tief verschachtelt. Die Aktion wurde abgebrochen, damit die App weiterläuft.";
        String message = throwable == null ? "" : throwable.getMessage();
        if (message == null || message.trim().isEmpty()) message = throwable == null ? "Unbekannter Fehler." : throwable.getClass().getSimpleName();
        return message;
    }

    private TextView addButton(LinearLayout toolbar, String label, View.OnClickListener listener) {
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
        b.setOnClickListener(safeClick(label, listener));
        toolbarSquareViews.add(b);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(toolbarButtonDp), dp(toolbarButtonDp));
        params.setMargins(dp(TOOLBAR_BUTTON_MARGIN_DP), dp(1), dp(TOOLBAR_BUTTON_MARGIN_DP), dp(1));
        toolbar.addView(b, params);
        return b;
    }

    private void addFormatButton(LinearLayout toolbar, String label, String action) {
        TextView button = addButton(toolbar, label, v -> applyActiveFormatAction(LegacyRichTextToolbar.findByAction(action)));
        if (action != null && !action.isEmpty()) textToolbarActionButtons.put(action, button);
    }

    private LinearLayout createQuickSearchBar() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setPadding(dp(4), dp(2), dp(4), dp(2));
        outer.setBackgroundColor(Color.rgb(248, 250, 253));
        outer.setVisibility(View.GONE);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        quickSearchInput = new TrackingEditText(this);
        quickSearchInput.setSingleLine(true);
        quickSearchInput.setHint("Suchen");
        quickSearchInput.setText(searchSession.cachedTerm());
        quickSearchInput.setSelectAllOnFocus(false);
        quickSearchInput.setTextSize(14f);
        quickSearchInput.setMinWidth(dp(120));
        quickSearchInput.setBackground(roundedBackground(Color.WHITE, Color.rgb(205, 214, 226), 6));
        quickSearchInput.setPadding(dp(7), 0, dp(7), 0);
        quickSearchInput.setOnFocusChangeListener((view, hasFocus) -> { if (hasFocus) setActivePane(ActivePane.RTF_EDITOR); });
        quickSearchInput.setOnEditorActionListener((v, actionId, event) -> { quickSearchNext(); return true; });
        row.addView(quickSearchInput, new LinearLayout.LayoutParams(dp(170), dp(30)));

        TextView next = smallSearchButton("Weiter", "↓", v -> quickSearchNext());
        TextView all = smallSearchButton("Alle Treffer", "Alle", v -> quickSearchAllResults());
        TextView hide = smallSearchButton("Suche schließen", "×", v -> hideQuickSearchBar());
        row.addView(next);
        row.addView(all);
        row.addView(hide);

        quickSearchWholeTree = searchCheckBox("Ganz", searchSession.cachedTerm().isEmpty() || searchSession.cachedAllNodes(), "Ganzer Baum statt aktueller Teilbaum");
        quickSearchWholeWords = searchCheckBox("Wort", searchSession.cachedWholeWords(), "Ganze Wörter");
        quickSearchCaseSensitive = searchCheckBox("Aa", searchSession.cachedCaseSensitive(), "Groß-/Kleinschreibung beachten");
        quickSearchIncludeTitles = searchCheckBox("Titel", searchSession.cachedTerm().isEmpty() || searchSession.cachedIncludeTitles(), "Titel mitsuchen");
        row.addView(quickSearchWholeTree);
        row.addView(quickSearchIncludeTitles);
        row.addView(quickSearchWholeWords);
        row.addView(quickSearchCaseSensitive);

        quickSearchStatus = new TextView(this);
        quickSearchStatus.setSingleLine(true);
        quickSearchStatus.setTextSize(12f);
        quickSearchStatus.setTextColor(Color.rgb(75, 84, 98));
        quickSearchStatus.setPadding(dp(6), 0, dp(6), 0);
        quickSearchStatus.setText("aktueller Teilbaum / ganzer Baum wählbar");
        row.addView(quickSearchStatus, new LinearLayout.LayoutParams(dp(210), dp(30)));

        scroll.addView(row, new HorizontalScrollView.LayoutParams(-2, -2));
        outer.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        return outer;
    }

    private TextView smallSearchButton(String description, String glyph, View.OnClickListener listener) {
        TextView b = new TextView(this);
        b.setText(glyph);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setTextSize(glyph != null && glyph.length() > 1 ? 9f : 16f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.rgb(30, 42, 58));
        b.setBackground(toolbarButtonBackground());
        b.setContentDescription(description);
        if (Build.VERSION.SDK_INT >= 26) b.setTooltipText(description);
        b.setOnClickListener(safeClick(description, listener));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(34), dp(30));
        p.setMargins(dp(2), 0, dp(2), 0);
        b.setLayoutParams(p);
        return b;
    }

    private CheckBox searchCheckBox(String text, boolean checked, String description) {
        CheckBox c = new CheckBox(this);
        c.setText(text);
        c.setTextSize(11f);
        c.setSingleLine(true);
        c.setChecked(checked);
        c.setContentDescription(description);
        if (Build.VERSION.SDK_INT >= 26) c.setTooltipText(description);
        c.setPadding(0, 0, 0, 0);
        c.setMinHeight(0);
        c.setMinimumHeight(0);
        return c;
    }

    private float toolbarGlyphTextSize(String glyph) {
        int len = glyph == null ? 0 : glyph.codePointCount(0, glyph.length());
        float base;
        if (len >= 5) base = 5.5f;
        else if (len == 4) base = 6.5f;
        else if (len == 3) base = 7.5f;
        else if (len == 2) base = 9.5f;
        else base = 14.5f;
        return base * (toolbarButtonDp / (float) DEFAULT_TOOLBAR_BUTTON_DP);
    }

    private String toolbarRowGlyph(String label) {
        if ("Datei".equals(label)) return "≡";
        if ("Baum".equals(label) || "Baum/Text".equals(label)) return "▦";
        if ("Text".equals(label)) return "A";
        return label == null || label.isEmpty() ? "•" : label.substring(0, 1);
    }

    private Drawable toolbarButtonBackground() { return toolbarButtonBackground(false); }

    private Drawable toolbarButtonBackground(boolean active) {
        GradientDrawable base = new GradientDrawable();
        base.setShape(GradientDrawable.RECTANGLE);
        base.setColor(active ? Color.rgb(221, 236, 255) : Color.rgb(255, 255, 255));
        base.setCornerRadius(dp(7));
        base.setStroke(dp(1), active ? Color.rgb(74, 133, 216) : Color.rgb(198, 208, 222));

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
        if (target == null) return;
        lastFormatTarget = target;
        if (target == FormatTarget.RTF_EDITOR) lastActivePane = ActivePane.RTF_EDITOR;
        else if (lastActivePane == ActivePane.RTF_EDITOR || lastActivePane == null) lastActivePane = ActivePane.TREE_NODE;
        updateActivePaneChrome();
    }

    private void setActivePane(ActivePane pane) {
        if (pane == null) return;
        lastActivePane = pane;
        lastFormatTarget = pane == ActivePane.RTF_EDITOR ? FormatTarget.RTF_EDITOR : FormatTarget.TREE_NODE;
        updateActivePaneChrome();
        scheduleFormatToolbarStateUpdate();
    }

    private void updateActivePaneChrome() {
        if (treeAdapter != null) treeAdapter.setTreeActive(lastActivePane == ActivePane.TREE_NODE);
        if (rootTitleView != null) {
            boolean active = lastActivePane == ActivePane.TREE_NODE;
            rootTitleView.setBackground(roundedBackground(active ? Color.rgb(255, 245, 176) : Color.rgb(255, 250, 205), active ? Color.rgb(210, 150, 32) : Color.rgb(226, 213, 145), 8));
        }
        if (titleEdit != null) {
            boolean active = lastActivePane == ActivePane.TITLE_TEXT;
            titleEdit.setBackground(roundedBackground(active ? Color.rgb(255, 245, 176) : Color.rgb(255, 250, 205), active ? Color.rgb(210, 150, 32) : Color.rgb(226, 213, 145), 8));
        }
        if (editor != null) {
            boolean active = lastActivePane == ActivePane.RTF_EDITOR;
            if (markdownPreviewActive) {
                editor.setBackground(roundedBackground(Color.rgb(248, 251, 255), Color.rgb(80, 144, 210), 8));
            } else {
                editor.setBackground(roundedBackground(Color.WHITE, active ? Color.rgb(74, 133, 216) : Color.rgb(213, 219, 229), 8));
            }
        }
    }

    private ActivePane activePaneForMiddleToolbar() {
        if (editor != null && editor.hasFocus()) return ActivePane.RTF_EDITOR;
        if (titleEdit != null && titleEdit.hasFocus()) return ActivePane.TITLE_TEXT;
        if (treeList != null && treeList.hasFocus()) return ActivePane.TREE_NODE;
        if (lastActivePane != null) return lastActivePane;
        return lastFormatTarget == FormatTarget.TREE_NODE ? ActivePane.TREE_NODE : ActivePane.RTF_EDITOR;
    }

    private boolean shouldFormatTreeTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        return active == ActivePane.TREE_NODE || active == ActivePane.TITLE_TEXT;
    }


    private void scheduleFormatToolbarStateUpdate() {
        if (formatToolbarUpdateScheduled) return;
        formatToolbarUpdateScheduled = true;
        if (mainHandler == null) {
            formatToolbarUpdateScheduled = false;
            updateFormatToolbarState();
        } else {
            mainHandler.post(() -> {
                formatToolbarUpdateScheduled = false;
                updateFormatToolbarState();
            });
        }
    }

    private void updateFormatToolbarState() {
        if (textToolbarActionButtons.isEmpty()) return;
        boolean treeTarget = shouldFormatTreeTarget();
        for (Map.Entry<String, TextView> entry : textToolbarActionButtons.entrySet()) {
            String action = entry.getKey();
            TextView button = entry.getValue();
            if (button == null) continue;
            boolean active = treeTarget ? isTreeFormatActionActive(action) : isRtfFormatActionActive(action);
            button.setBackground(toolbarButtonBackground(active));
            button.setTextColor(active ? Color.rgb(20, 72, 145) : Color.rgb(30, 42, 58));
        }
    }

    private boolean isTreeFormatActionActive(String action) {
        if (currentNode == null || action == null) return false;
        String styles = currentNode.extraAttrs.get(NODE_TITLE_STYLE_ATTR);
        String normalized = styles == null ? "" : styles.toLowerCase(Locale.ROOT);
        if ("format_bold".equals(action)) return containsStyle(normalized, "bold");
        if ("format_italic".equals(action)) return containsStyle(normalized, "italic");
        if ("format_underline".equals(action)) return containsStyle(normalized, "underline");
        if ("format_strike".equals(action)) return containsStyle(normalized, "strike");
        if ("text_color".equals(action)) return currentNode.fgArgb != 0;
        if ("highlight_color".equals(action)) return currentNode.bgArgb != 0;
        if ("font_family".equals(action)) return currentNode.extraAttrs.containsKey(NODE_TITLE_FONT_ATTR);
        if ("font_size".equals(action)) return currentNode.extraAttrs.containsKey(NODE_TITLE_SIZE_ATTR);
        if ("format_regular".equals(action)) {
            return normalized.trim().isEmpty() && currentNode.fgArgb == 0 && currentNode.bgArgb == 0
                    && !currentNode.extraAttrs.containsKey(NODE_TITLE_FONT_ATTR)
                    && !currentNode.extraAttrs.containsKey(NODE_TITLE_SIZE_ATTR);
        }
        return false;
    }

    private boolean containsStyle(String csv, String style) {
        if (csv == null || style == null) return false;
        for (String part : csv.split(",")) if (style.equals(part.trim())) return true;
        return false;
    }

    private boolean isRtfFormatActionActive(String action) {
        if (editor == null || action == null) return false;
        Spannable text = editor.getText();
        if (text == null || text.length() == 0) return "format_regular".equals(action) || "align_left".equals(action);
        int pos = Math.max(0, Math.min(editor.getSelectionStart(), text.length() - 1));
        RtfTextStyle style = styleAt(text, pos);
        if ("format_bold".equals(action)) return style.bold;
        if ("format_italic".equals(action)) return style.italic;
        if ("format_underline".equals(action)) return style.underline;
        if ("format_strike".equals(action)) return style.strike;
        if ("text_color".equals(action)) return style.fgColor != null;
        if ("highlight_color".equals(action)) return style.bgColor != null;
        if ("font_family".equals(action)) return style.fontFamily != null;
        if ("font_size".equals(action)) return style.fontSizeHalfPoints != null;
        if ("format_regular".equals(action)) {
            return !style.bold && !style.italic && !style.underline && !style.strike
                    && style.fgColor == null && style.bgColor == null && style.fontFamily == null
                    && style.fontSizeHalfPoints == null && style.vertical == null;
        }
        String alignment = currentParagraphAlignmentAction(text, pos);
        if ("align_left".equals(action)) return "align_left".equals(alignment);
        if ("align_center".equals(action)) return "align_center".equals(alignment);
        if ("align_right".equals(action)) return "align_right".equals(alignment);
        if ("align_justify".equals(action)) return "align_justify".equals(alignment);
        return false;
    }

    private String currentParagraphAlignmentAction(Spannable text, int pos) {
        if (text == null || text.length() == 0) return "align_left";
        int start = Math.max(0, Math.min(pos, text.length() - 1));
        int end = Math.min(text.length(), start + 1);
        AlignmentSpan[] aligns = text.getSpans(start, end, AlignmentSpan.class);
        if (aligns.length == 0) return "align_left";
        AlignmentSpan span = aligns[aligns.length - 1];
        if (span instanceof RtfParagraphAlignmentSpan && "justify".equals(((RtfParagraphAlignmentSpan) span).legacyAlign)) return "align_justify";
        Layout.Alignment alignment = span.getAlignment();
        if (alignment == Layout.Alignment.ALIGN_CENTER) return "align_center";
        if (alignment == Layout.Alignment.ALIGN_OPPOSITE) return "align_right";
        return "align_left";
    }

    private void markDocumentChanged() {
        if (document != null) document.markChanged();
        if (!loadingEditor) scheduleRuntimeSnapshotSave();
    }

    private void toggleNodeExpanded(NoteNode node, boolean showMessage) {
        if (node == null || node.children.isEmpty()) return;
        pushTreeUndoSnapshot(node.expanded ? "Knoten zuklappen" : "Knoten aufklappen");
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

    private void toggleMarkdownPreview() {
        if (markdownPreviewActive) {
            deactivateMarkdownPreview(true);
            return;
        }
        activateMarkdownPreviewForCurrentNode(true);
    }

    private boolean activateMarkdownPreviewForCurrentNode(boolean showStatus) {
        if (currentNode == null || editor == null) return false;
        if (markdownPreviewActive && markdownPreviewNode == currentNode) return true;
        if (markdownPreviewActive) clearMarkdownPreviewState(false);

        captureMarkdownPreviewReturnState();
        // First commit real editor changes to the node as RTF.  The preview then
        // works from raw text extracted from that RTF, while the RTF itself stays
        // untouched for later saving.
        saveCurrentEditorToNode();
        String sourceRtf = currentNode.rtf == null ? "" : currentNode.rtf;
        String raw = LegacyMarkdownPreviewModel.rawMarkdownTextFromRtf(sourceRtf);
        if (raw.trim().isEmpty()) {
            if (showStatus) status("Keine Markdown-Vorschau: aktueller Knoten ist leer");
            return false;
        }

        String html = LegacyMarkdownPreviewModel.toHtmlDocument(raw);
        markdownPreviewActive = true;
        markdownPreviewNode = currentNode;
        markdownPreviewSourceRtf = sourceRtf;
        editorDirty = false;
        editorUndoStack.clear();
        editorRedoStack.clear();
        setEditorMarkdownReadOnly(true);
        showMarkdownPreviewSurface(html);
        updateMarkdownPreviewButtonState();
        updateActivePaneChrome();
        if (showStatus) status(LegacyMarkdownPreviewModel.statusForPreview(raw) + " · HTML/Tabellen-Vorschau · RTF bleibt erhalten");
        return true;
    }

    private void deactivateMarkdownPreview(boolean showStatus) {
        if (!markdownPreviewActive || editor == null) return;
        NoteNode node = markdownPreviewNode == null ? currentNode : markdownPreviewNode;
        String sourceRtf = node == null ? markdownPreviewSourceRtf : (node.rtf == null ? "" : node.rtf);
        int returnStart = markdownPreviewReturnSelectionStart;
        int returnEnd = markdownPreviewReturnSelectionEnd;
        int returnScrollX = markdownPreviewReturnScrollX;
        int returnScrollY = markdownPreviewReturnScrollY;
        clearMarkdownPreviewState(false);
        boolean oldLoading = loadingEditor;
        loadingEditor = true;
        try {
            editor.setText(rtfToEditorText(sourceRtf));
        } finally {
            loadingEditor = oldLoading;
        }
        editorDirty = false;
        editorUndoStack.clear();
        editorRedoStack.clear();
        setEditorMarkdownReadOnly(false);
        restoreEditorCursorAfterMarkdownPreview(returnStart, returnEnd, returnScrollX, returnScrollY);
        updateMarkdownPreviewButtonState();
        updateActivePaneChrome();
        if (showStatus) status("Markdown-Vorschau aus · ursprünglicher RTF-Inhalt wieder sichtbar und Cursor aktiv");
    }

    private void clearMarkdownPreviewState(boolean updateUi) {
        markdownPreviewActive = false;
        markdownPreviewNode = null;
        markdownPreviewSourceRtf = "";
        markdownPreviewReturnSelectionStart = -1;
        markdownPreviewReturnSelectionEnd = -1;
        markdownPreviewReturnScrollX = 0;
        markdownPreviewReturnScrollY = 0;
        hideMarkdownPreviewSurface();
        setEditorMarkdownReadOnly(false);
        if (updateUi) {
            updateMarkdownPreviewButtonState();
            updateActivePaneChrome();
        }
    }

    private void captureMarkdownPreviewReturnState() {
        if (editor == null) {
            markdownPreviewReturnSelectionStart = -1;
            markdownPreviewReturnSelectionEnd = -1;
            markdownPreviewReturnScrollX = 0;
            markdownPreviewReturnScrollY = 0;
            return;
        }
        int len = editor.getText() == null ? 0 : editor.getText().length();
        int start = Math.max(0, Math.min(editor.getSelectionStart(), len));
        int end = Math.max(start, Math.min(editor.getSelectionEnd(), len));
        markdownPreviewReturnSelectionStart = start;
        markdownPreviewReturnSelectionEnd = end;
        markdownPreviewReturnScrollX = editor.getScrollX();
        markdownPreviewReturnScrollY = editor.getScrollY();
    }

    private void showMarkdownPreviewSurface(String html) {
        if (editor != null) {
            editor.clearFocus();
            editor.setVisibility(View.GONE);
        }
        if (markdownPreviewWebView != null) {
            markdownPreviewWebView.setVisibility(View.VISIBLE);
            markdownPreviewWebView.loadDataWithBaseURL("https://notizen.local/", html == null ? "" : html, "text/html", "UTF-8", null);
            markdownPreviewWebView.requestFocus();
        }
    }

    private void hideMarkdownPreviewSurface() {
        if (markdownPreviewWebView != null) {
            markdownPreviewWebView.loadDataWithBaseURL("about:blank", "<html><body></body></html>", "text/html", "UTF-8", null);
            markdownPreviewWebView.setVisibility(View.GONE);
            markdownPreviewWebView.clearFocus();
        }
        if (editor != null) editor.setVisibility(View.VISIBLE);
    }

    private void restoreEditorCursorAfterMarkdownPreview(int start, int end, int scrollX, int scrollY) {
        if (editor == null) return;
        int len = editor.getText() == null ? 0 : editor.getText().length();
        int s = start >= 0 ? Math.max(0, Math.min(start, len)) : len;
        int e = end >= 0 ? Math.max(s, Math.min(end, len)) : s;
        editor.setVisibility(View.VISIBLE);
        restoreEditorEditableState();
        editor.requestFocus();
        if (editor.getParent() instanceof ViewGroup) {
            try { ((ViewGroup) editor.getParent()).requestChildFocus(editor, editor); } catch (Exception ignored) {}
        }
        try { editor.setSelection(s, e); } catch (Exception ignored) {}
        editor.scrollTo(Math.max(0, scrollX), Math.max(0, scrollY));
        restartEditorInputAndCursor();
        setFormatTarget(FormatTarget.RTF_EDITOR);
        editor.post(() -> {
            restoreEditorSelectionAfterMarkdownPost(s, e, scrollX, scrollY);
        });
        if (mainHandler != null) {
            mainHandler.postDelayed(() -> restoreEditorSelectionAfterMarkdownPost(s, e, scrollX, scrollY), 120L);
        }
    }

    private void restoreEditorSelectionAfterMarkdownPost(int s, int e, int scrollX, int scrollY) {
        if (editor == null) return;
        editor.setVisibility(View.VISIBLE);
        restoreEditorEditableState();
        editor.requestFocus();
        int postLen = editor.getText() == null ? 0 : editor.getText().length();
        int ps = Math.max(0, Math.min(s, postLen));
        int pe = Math.max(ps, Math.min(e, postLen));
        try { editor.setSelection(ps, pe); } catch (Exception ignored) {}
        editor.scrollTo(Math.max(0, scrollX), Math.max(0, scrollY));
        restartEditorInputAndCursor();
        editor.invalidate();
    }

    private void restoreEditorEditableState() {
        if (editor == null) return;
        editor.setEnabled(true);
        editor.setFocusable(true);
        editor.setFocusableInTouchMode(true);
        if (editorEditableInputType > 0) editor.setRawInputType(editorEditableInputType);
        if (editorEditableKeyListener != null) editor.setKeyListener(editorEditableKeyListener);
        restoreEditorSelectionVisuals();
        editor.setCursorVisible(true);
    }

    private void restoreEditorSelectionVisuals() {
        if (editor == null) return;
        editor.setHighlightColor(Color.argb(115, 74, 133, 216));
        if (Build.VERSION.SDK_INT >= 29) {
            GradientDrawable cursor = new GradientDrawable();
            cursor.setColor(Color.rgb(20, 72, 145));
            cursor.setSize(Math.max(1, dp(2)), Math.max(18, dp(24)));
            try { editor.setTextCursorDrawable(cursor); } catch (Exception ignored) {}
        }
    }

    private void restartEditorInputAndCursor() {
        if (editor == null) return;
        editor.setCursorVisible(false);
        editor.setCursorVisible(true);
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.restartInput(editor);
        } catch (Exception ignored) {}
    }

    private void setEditorMarkdownReadOnly(boolean readOnly) {
        if (editor == null) return;
        if (editorEditableKeyListener == null && editor.getKeyListener() != null) editorEditableKeyListener = editor.getKeyListener();
        if (editorEditableInputType <= 0) editorEditableInputType = editor.getInputType();
        if (readOnly) {
            // The actual Markdown preview is a separate WebView. Do not switch the
            // hidden EditText into Android's selectable/read-only mode: that mode
            // can leave the cursor and selection overlay invisible when returning.
            editor.setCursorVisible(false);
            editor.clearFocus();
        } else {
            restoreEditorEditableState();
        }
    }

    private void updateMarkdownPreviewButtonState() {
        if (markdownPreviewButton == null) return;
        markdownPreviewButton.setBackground(toolbarButtonBackground(markdownPreviewActive));
        markdownPreviewButton.setTextColor(markdownPreviewActive ? Color.rgb(20, 72, 145) : Color.rgb(30, 42, 58));
        markdownPreviewButton.setContentDescription(markdownPreviewActive
                ? "Markdown-Vorschau ausschalten und ursprünglichen RTF-Inhalt anzeigen"
                : "Markdown aus RTF-Rohtext als HTML mit Tabellen schreibgeschützt anzeigen");
    }

    private boolean blockEditorMutationWhileMarkdownPreview() {
        if (!markdownPreviewActive) return false;
        status("Markdown-Vorschau ist schreibgeschützt. Button MD noch einmal drücken, dann ist der Ursprungstext wieder bearbeitbar.");
        return true;
    }

    private SpannableStringBuilder renderMarkdownPreview(String raw) {
        SpannableStringBuilder out = new SpannableStringBuilder();
        String text = raw == null ? "" : raw.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = text.split("\n", -1);
        boolean inFence = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i] == null ? "" : lines[i];
            String trimmed = line.trim();
            if (isMarkdownFence(trimmed)) {
                inFence = !inFence;
                if (i < lines.length - 1 && out.length() > 0 && out.charAt(out.length() - 1) != '\n') out.append('\n');
                continue;
            }
            int start = out.length();
            if (inFence) {
                out.append(line);
                applyMarkdownCodeSpan(out, start, out.length());
            } else {
                appendMarkdownBlockLine(out, line);
            }
            if (i < lines.length - 1) out.append('\n');
        }
        return out;
    }

    private boolean isMarkdownFence(String trimmed) {
        if (trimmed == null) return false;
        return trimmed.startsWith("```") || trimmed.startsWith("~~~");
    }

    private void appendMarkdownBlockLine(SpannableStringBuilder out, String line) {
        String src = line == null ? "" : line;
        String trimmed = src.trim();
        if (trimmed.matches("^#{1,6}\\s+.*")) {
            int level = 0;
            while (level < trimmed.length() && trimmed.charAt(level) == '#') level++;
            String content = trimmed.substring(Math.min(trimmed.length(), level)).trim();
            int start = out.length();
            appendMarkdownInline(out, content);
            int end = out.length();
            if (end > start) {
                out.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                float scale = level <= 1 ? 1.45f : (level == 2 ? 1.30f : (level == 3 ? 1.18f : 1.08f));
                out.setSpan(new RelativeSizeSpan(scale), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return;
        }
        if (trimmed.matches("^([-*_])(?:\\s*\\1){2,}\\s*$")) {
            out.append("────────────────");
            return;
        }
        if (trimmed.startsWith(">")) {
            String content = trimmed.substring(1).trim();
            int start = out.length();
            appendMarkdownInline(out, content);
            int end = out.length();
            if (end > start) {
                out.setSpan(new QuoteSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new LeadingMarginSpan.Standard(dp(12), dp(12)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return;
        }
        Matcher ordered = Pattern.compile("^\\s*(\\d{1,4}[.)])\\s+(.*)$").matcher(src);
        if (ordered.matches()) {
            int start = out.length();
            out.append(ordered.group(1)).append(' ');
            appendMarkdownInline(out, ordered.group(2));
            int end = out.length();
            if (end > start) out.setSpan(new LeadingMarginSpan.Standard(dp(18), dp(18)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return;
        }
        Matcher unordered = Pattern.compile("^\\s*[-+*]\\s+(.*)$").matcher(src);
        if (unordered.matches()) {
            int start = out.length();
            out.append("• ");
            appendMarkdownInline(out, unordered.group(1));
            int end = out.length();
            if (end > start) {
                out.setSpan(new BulletSpan(dp(8)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                out.setSpan(new LeadingMarginSpan.Standard(dp(18), dp(18)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return;
        }
        appendMarkdownInline(out, src);
    }

    private void appendMarkdownInline(SpannableStringBuilder out, String text) {
        if (text == null || text.isEmpty()) return;
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                out.append(text.charAt(i + 1));
                i += 2;
                continue;
            }
            if (c == '!' && i + 1 < text.length() && text.charAt(i + 1) == '[') {
                int close = text.indexOf(']', i + 2);
                int endUrl = close >= 0 && close + 1 < text.length() && text.charAt(close + 1) == '(' ? text.indexOf(')', close + 2) : -1;
                if (endUrl > close) {
                    // Markdown images are ignored as images; show only their alt text.
                    appendMarkdownInline(out, text.substring(i + 2, close));
                    i = endUrl + 1;
                    continue;
                }
            }
            if (c == '[') {
                int close = text.indexOf(']', i + 1);
                int endUrl = close >= 0 && close + 1 < text.length() && text.charAt(close + 1) == '(' ? text.indexOf(')', close + 2) : -1;
                if (endUrl > close) {
                    String label = text.substring(i + 1, close);
                    String url = text.substring(close + 2, endUrl).trim();
                    int quote = url.indexOf(' ');
                    if (quote > 0) url = url.substring(0, quote).trim();
                    int start = out.length();
                    appendMarkdownInline(out, label);
                    int end = out.length();
                    if (end > start && !url.isEmpty()) out.setSpan(new URLSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = endUrl + 1;
                    continue;
                }
            }
            if (c == '`') {
                int close = text.indexOf('`', i + 1);
                if (close > i + 1) {
                    int start = out.length();
                    out.append(text.substring(i + 1, close));
                    applyMarkdownCodeSpan(out, start, out.length());
                    i = close + 1;
                    continue;
                }
            }
            if (startsWithAt(text, i, "**")) {
                int close = text.indexOf("**", i + 2);
                if (close > i + 2) {
                    int start = out.length();
                    appendMarkdownInline(out, text.substring(i + 2, close));
                    int end = out.length();
                    if (end > start) out.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = close + 2;
                    continue;
                }
            }
            if (startsWithAt(text, i, "__")) {
                int close = text.indexOf("__", i + 2);
                if (close > i + 2) {
                    int start = out.length();
                    appendMarkdownInline(out, text.substring(i + 2, close));
                    int end = out.length();
                    if (end > start) out.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = close + 2;
                    continue;
                }
            }
            if (startsWithAt(text, i, "~~")) {
                int close = text.indexOf("~~", i + 2);
                if (close > i + 2) {
                    int start = out.length();
                    appendMarkdownInline(out, text.substring(i + 2, close));
                    int end = out.length();
                    if (end > start) out.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = close + 2;
                    continue;
                }
            }
            if (c == '*' && !startsWithAt(text, i, "**")) {
                int close = text.indexOf('*', i + 1);
                if (close > i + 1 && !startsWithAt(text, close, "**")) {
                    int start = out.length();
                    appendMarkdownInline(out, text.substring(i + 1, close));
                    int end = out.length();
                    if (end > start) out.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = close + 1;
                    continue;
                }
            }
            if (c == '_' && !startsWithAt(text, i, "__")) {
                int close = text.indexOf('_', i + 1);
                if (close > i + 1 && !startsWithAt(text, close, "__")) {
                    int start = out.length();
                    appendMarkdownInline(out, text.substring(i + 1, close));
                    int end = out.length();
                    if (end > start) out.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    i = close + 1;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
    }

    private boolean startsWithAt(String text, int index, String token) {
        return text != null && token != null && index >= 0 && index + token.length() <= text.length() && text.startsWith(token, index);
    }

    private void applyMarkdownCodeSpan(SpannableStringBuilder out, int start, int end) {
        if (out == null || end <= start) return;
        out.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new BackgroundColorSpan(Color.rgb(235, 238, 244)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void newEmptyDocument() {
        // Entspricht Notizen .NET: alte TreeView vollständig weg, danach ein leerer Startknoten ohne RTF.
        resetToFreshStartDocument("Leere Datei");
    }

    private void newDocument() {
        resetToFreshStartDocument("Neue Datei");
    }

    private void resetToFreshStartDocument(String message) {
        clearMarkdownPreviewState(false);
        currentNode = null;
        loadingEditor = true;
        clearVisibleDocumentViewsForFreshStart();
        document = LegacyFreshStartModel.newFreshStartDocument();
        document.displayName = "unbenannt.alx";
        currentUri = null;
        currentRawFile = null;
        currentFtpTarget = null;
        currentDisplayName = "unbenannt.alx";
        runtimeSnapshotRestored = false;
        editorDirty = false;
        titleDirty = false;
        internalEditorClipboard = null;
        internalEditorClipboardPlain = "";
        clearTreeHistory();
        setFormatTarget(FormatTarget.RTF_EDITOR);
        clearRuntimeSnapshotIfBlankStart();
        loadingEditor = false;
        rebuildTree();
        selectNode(document.ensureRoot(), false);
        updateTitle();
        scheduleRuntimeSnapshotSave();
        status(message == null ? "Neue Datei" : message);
    }

    private void clearVisibleDocumentViewsForFreshStart() {
        if (treeAdapter != null) {
            treeAdapter.setRows(new ArrayList<>());
            treeAdapter.setSelected(null);
        }
        if (treeList != null) treeList.clearChoices();
        if (rootTitleView != null) rootTitleView.setText("");
        if (titleEdit != null) titleEdit.setText("");
        if (editor != null) editor.setText("");
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
        saveDocumentAs(null);
    }

    private void saveDocumentAs(ContinueCallback afterSave) {
        saveCurrentEditorToNode();
        pendingAfterSaveAsCallback = afterSave;
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
            updateAndroidWidgetsFromDocument();
            status("Gespeichert: " + currentDisplayName);
        } catch (Throwable e) {
            handleRecoverableUiFailure("Speichern fehlgeschlagen", e);
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
            try {
                out.write(payload);
                out.getFD().sync();
            } finally {
                try { out.close(); } catch (Exception ignored) {}
            }
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
            updateAndroidWidgetsFromDocument();
            status((autosave ? "Autosave" : "Gespeichert") + ": " + file.getAbsolutePath());
        } catch (Throwable e) {
            handleRecoverableUiFailure(autosave ? "Autosave fehlgeschlagen" : "Speichern fehlgeschlagen", e);
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
        } catch (Throwable e) {
            handleRecoverableUiFailure("Sicherung", e);
        }
    }

    private byte[] readFile(File file) throws Exception {
        if (file == null) throw new IllegalStateException("Dateipfad fehlt.");
        long length = file.length();
        if (length > MAX_READ_ALL_BYTES) throw new IllegalStateException("Datei ist zu groß für sicheres Laden auf Android (" + (length / (1024 * 1024)) + " MB).");
        FileInputStream in = new FileInputStream(file);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(length > 0 && length < Integer.MAX_VALUE ? (int) length : 8192);
            byte[] buf = new byte[8192];
            int n;
            int total = 0;
            while ((n = in.read(buf)) != -1) {
                if (total > MAX_READ_ALL_BYTES - n) throw new IllegalStateException("Datei ist zu groß für sicheres Laden auf Android.");
                out.write(buf, 0, n);
                total += n;
            }
            return out.toByteArray();
        } finally {
            try { in.close(); } catch (Exception ignored) {}
        }
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
        } catch (Throwable e) {
            handleRecoverableUiFailure("Öffnen fehlgeschlagen", e);
        }
    }

    private void showSettingsDialog() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        LegacySettingsDialogModel.ViewState state = LegacySettingsDialogModel.fromSettings(settings);
        LegacyAndroidSettingsUiModel.ViewState androidState = LegacyAndroidSettingsUiModel.fromSettings(settings);
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        scroll.addView(box, new ScrollView.LayoutParams(-1, -2));
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

        TextView androidHeader = new TextView(this);
        androidHeader.setText("Android-Oberfläche / Import / Wiederherstellung");
        androidHeader.setTypeface(Typeface.DEFAULT_BOLD);
        androidHeader.setPadding(0, dp(10), 0, dp(3));
        box.addView(androidHeader);
        EditText defaultDirectory = labeledEdit(box, "Standardordner / letzter Ordner", androidState.defaultDirectory);
        EditText toolbarDp = labeledEdit(box, "Icon-Button-Größe dp", androidState.toolbarButtonDp);
        EditText headerSp = labeledEdit(box, "gelbe Überschriften Textgröße sp", androidState.headerTextSp);
        EditText treeSp = labeledEdit(box, "Baum-Schriftgröße sp", androidState.treeTextSp);
        EditText editorSp = labeledEdit(box, "RTF-Editor Standard-Schriftgröße sp", androidState.editorTextSp);
        EditText widgetSp = labeledEdit(box, "Android-Widget Schriftgröße sp", androidState.widgetTextSp);
        EditText imageEdge = labeledEdit(box, "große Bilder verkleinern auf max. Kantenlänge px", androidState.largeImageLongEdgePx);
        CheckBox autoDownsample = new CheckBox(this);
        autoDownsample.setText("Große Bilder automatisch verkleinern");
        autoDownsample.setChecked(androidState.autoDownsampleLargeImages);
        box.addView(autoDownsample);
        CheckBox restoreSnapshot = new CheckBox(this);
        restoreSnapshot.setText("Nach Absturz letzten Stand wiederherstellen");
        restoreSnapshot.setChecked(androidState.restoreRuntimeSnapshot);
        box.addView(restoreSnapshot);
        CheckBox showDiagnosticsToolbar = new CheckBox(this);
        showDiagnosticsToolbar.setText("Diagnose-/Entwicklungsbuttons oben anzeigen");
        showDiagnosticsToolbar.setChecked(androidState.showDiagnosticsToolbar);
        box.addView(showDiagnosticsToolbar);

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
                .setView(scroll)
                .setNeutralButton("Config importieren", (d, which) -> importSettingsFile())
                .setNegativeButton("Abbrechen", null)
                .setPositiveButton("OK", (d, which) -> {
                    saveCurrentEditorToNode();
                    boolean oldShowDiagnostics = settings == null || settings.androidShowDiagnosticsToolbar;
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
                    LegacyAndroidSettingsUiModel.ApplyResult androidResult = LegacyAndroidSettingsUiModel.apply(
                            settings,
                            defaultDirectory.getText().toString(),
                            toolbarDp.getText().toString(),
                            headerSp.getText().toString(),
                            treeSp.getText().toString(),
                            editorSp.getText().toString(),
                            widgetSp.getText().toString(),
                            imageEdge.getText().toString(),
                            autoDownsample.isChecked(),
                            restoreSnapshot.isChecked(),
                            showDiagnosticsToolbar.isChecked());
                    settings = androidResult.settings;
                    toolbarButtonDp = LegacySettings.normalizeAndroidToolbarButtonDp(settings.androidToolbarButtonDp);
                    headerTextSp = LegacySettings.normalizeAndroidHeaderTextSp(settings.androidHeaderTextSp);
                    applyToolbarButtonSize();
                    applyHeaderTextSize();
                    if (treeAdapter != null) treeAdapter.setDefaultTreeTextSizeSp(LegacySettings.normalizeAndroidTreeTextSp(settings.androidTreeTextSp));
                    if (editor != null) editor.setTextSize(LegacySettings.normalizeAndroidEditorTextSp(settings.androidEditorTextSp));
                    AndroidSettingsStore.save(this, settings);
                    scheduleAutosaveTick();
                    boolean newShowDiagnostics = settings == null || settings.androidShowDiagnosticsToolbar;
                    if (oldShowDiagnostics != newShowDiagnostics) {
                        buildUi();
                        selectNode(currentNode == null ? document.ensureRoot() : currentNode, false);
                    }
                    ArrayList<String> warnings = new ArrayList<>();
                    warnings.addAll(result.warnings);
                    warnings.addAll(androidResult.warnings);
                    status(warnings.isEmpty() ? "Einstellungen gespeichert" : "Einstellungen gespeichert\n" + joinLines(warnings));
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
        saveDocumentToFtp(target, null);
    }

    private void saveDocumentToFtp(FtpTarget target, ContinueCallback afterSave) {
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
            updateAndroidWidgetsFromDocument();
            status("FTP gespeichert: " + result.safeDisplayUrl());
            if (afterSave != null && document != null && !document.changed) safeAction("Aktion nach Speichern", afterSave).run();
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
                mainHandler.post(() -> {
                    try {
                        done.run(result);
                    } catch (Throwable t) {
                        handleRecoverableUiFailure(title, t);
                    }
                });
            } catch (Throwable t) {
                mainHandler.post(() -> handleRecoverableUiFailure(title, t));
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
        pushTreeUndoSnapshot("Neuer Unterknoten");
        LegacyTreeCreation.CreationResult result = LegacyTreeCreation.newChild(document, currentNode, "...");
        rebuildTree();
        selectNode(result.node, true);
        status(result.status);
    }

    private void newNext() {
        if (document == null) document = NoteDocument.newDocument();
        if (currentNode == null) currentNode = document.ensureRoot();
        saveCurrentEditorToNode();
        pushTreeUndoSnapshot("Neuer Knoten daneben");
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
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Alle Knoten aufklappen");
        int changed = LegacyTreeExpansion.expandAll(document.ensureRoot());
        if (changed > 0) {
            commitTreeUndoSnapshot(before);
            markDocumentChanged();
        }
        rebuildTree();
        if (currentNode != null) selectNode(currentNode, true);
        status("Alle Knoten geöffnet");
    }

    private void collapseAllNodes() {
        if (document == null || document.root == null) return;
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Alle Knoten zuklappen");
        int changed = LegacyTreeExpansion.collapseAll(document.ensureRoot());
        if (changed > 0) {
            commitTreeUndoSnapshot(before);
            markDocumentChanged();
        }
        rebuildTree();
        selectNode(document.ensureRoot(), true);
        status("Alle Knoten geschlossen");
    }

    private void clearDesktopNotesInSubtree() {
        if (currentNode == null) return;
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Haftnotizen im Teilbaum entfernen");
        LegacyDesktopNoteTreeOps.ClearResult result = LegacyDesktopNoteTreeOps.clearDesktopNotes(currentNode);
        if (result.clearedDesktopNotes > 0) {
            commitTreeUndoSnapshot(before);
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
                    pushTreeUndoSnapshot("Knoten löschen");
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
            internalEditorClipboard = null;
            internalEditorClipboardPlain = "";
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
        pushTreeUndoSnapshot("Knoten ausschneiden");
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
        pushTreeUndoSnapshot("Knoten einfügen");
        NoteNode pasted = NoteTreeOps.legacyPasteClone(source, currentNode);
        ensureAncestorsExpanded(pasted);
        markDocumentChanged();
        rebuildTree();
        selectNode(pasted, true);
        status("Knoten eingefügt");
    }

    private void pasteNodeAsChild() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        NoteNode source = clipboardNodeFromSystem();
        if (source == null && internalClipboardNode != null) source = internalClipboardNode.cloneDeep(false);
        if (source == null) {
            error("Einfügen", "Die Zwischenablage enthält keinen Notizen-Knoten.");
            return;
        }
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Knoten als Unterknoten einfügen");
        NoteNode pasted = NoteTreeOps.legacyPasteCloneAsLastChild(source, currentNode);
        if (pasted == null) {
            status("Einfügen als Unterknoten nicht möglich");
            return;
        }
        ensureAncestorsExpanded(pasted);
        commitTreeUndoSnapshot(before);
        markDocumentChanged();
        rebuildTree();
        selectNode(pasted, true);
        status("Knoten als Unterknoten eingefügt");
    }

    private void saveCurrentRtfFromContext() {
        saveCurrentEditorToNode();
        rebuildTree();
        if (currentNode != null) selectNode(currentNode, false);
        status("Aktueller RTF-Inhalt in Knoten gespeichert");
    }

    private void renameCurrentNodeInline() {
        if (currentNode == null || treeAdapter == null) return;
        saveCurrentEditorToNode();
        setActivePane(ActivePane.TREE_NODE);
        if (treeList != null) treeList.requestFocus();
        int pos = flatIndexOf(currentNode);
        if (pos >= 0 && treeList != null) treeList.smoothScrollToPosition(pos);
        treeAdapter.beginInlineEdit(currentNode, new TreeListAdapter.InlineEditListener() {
            @Override public void onCommit(NoteNode node, String candidateTitle) {
                commitInlineTreeRename(node, candidateTitle);
            }
            @Override public void onCancel(NoteNode node) {
                status("Umbenennen abgebrochen");
            }
        });
        status("Knotentitel direkt im Baum bearbeiten");
    }

    private void commitInlineTreeRename(NoteNode node, String candidateTitle) {
        if (node == null) return;
        LegacyTreeLabelEditing.EditResult result = LegacyTreeLabelEditing.commit(node.title, candidateTitle);
        if (result.changed) pushTreeUndoSnapshot("Knoten umbenennen");
        node.title = result.newTitle;
        if (node == currentNode && titleEdit != null) {
            loadingEditor = true;
            titleEdit.setText(result.newTitle);
            titleEdit.setSelection(titleEdit.getText().length());
            loadingEditor = false;
            titleDirty = false;
        }
        if (result.changed) {
            markDocumentChanged();
            updateTitle();
            status(result.usedFallback ? "Knoten umbenannt: ..." : "Knoten umbenannt");
        } else {
            status("Knotentitel unverändert");
        }
        rebuildTree();
        if (treeAdapter != null) treeAdapter.setSelected(node);
        if (treeList != null) {
            int pos = flatIndexOf(node);
            if (pos >= 0) treeList.setItemChecked(pos, true);
            treeList.requestFocus();
        }
    }

    private void beginTreeDragFromRow(View rowView, NoteNode node) {
        if (rowView == null || node == null) return;
        if (node.parent == null) {
            toast("Wurzel kann nicht gezogen werden");
            status("Die Wurzel bleibt oben");
            return;
        }
        saveCurrentEditorToNode();
        treeDragSource = node;
        ClipData data = ClipData.newPlainText("Notizen-Knoten", safeTitle(node));
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(rowView);
        boolean started;
        if (Build.VERSION.SDK_INT >= 24) started = rowView.startDragAndDrop(data, shadow, node, 0);
        else started = rowView.startDrag(data, shadow, node, 0);
        if (started) {
            status("Knoten ziehen: oben=vor Ziel, Mitte=als Kind, unten=nach Ziel");
            toast("Ziehen: oben/vor · Mitte/Kind · unten/nach");
        } else {
            treeDragSource = null;
            status("Drag/Drop nicht gestartet; nutze 'Vor Ziel' im Menü");
            showMoveBeforeTargetDialog();
        }
    }

    private boolean handleTreeDragEvent(DragEvent event) {
        if (event == null) return false;
        Object local = event.getLocalState();
        boolean noteDrag = local instanceof NoteNode || treeDragSource != null;
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return noteDrag;
            case DragEvent.ACTION_DRAG_LOCATION:
                if (!noteDrag) return false;
                updateTreeDropPreview(event);
                return true;
            case DragEvent.ACTION_DROP:
                if (!noteDrag) return false;
                return finishTreeDrop(event);
            case DragEvent.ACTION_DRAG_ENDED:
                clearTreeDropPreview();
                treeDragSource = null;
                return true;
            default:
                return noteDrag;
        }
    }

    private static final class TreeDropTarget {
        final NoteNode node;
        final TreeListAdapter.DropPreview mode;
        TreeDropTarget(NoteNode node, TreeListAdapter.DropPreview mode) {
            this.node = node;
            this.mode = mode == null ? TreeListAdapter.DropPreview.NONE : mode;
        }
    }

    private TreeDropTarget treeDropTargetFromEvent(DragEvent event) {
        if (treeList == null || treeAdapter == null || event == null) return null;
        int position = treeList.pointToPosition((int) event.getX(), (int) event.getY());
        if (position < 0 || position >= treeAdapter.getCount()) return null;
        TreeListAdapter.FlatNode row = treeAdapter.getItem(position);
        if (row == null || row.node == null) return null;
        TreeListAdapter.DropPreview mode = TreeListAdapter.DropPreview.AS_CHILD;
        View child = treeList.getChildAt(position - treeList.getFirstVisiblePosition());
        if (child != null && child.getHeight() > 0) {
            float localY = event.getY() - child.getTop();
            float third = child.getHeight() / 3f;
            if (localY < third) mode = TreeListAdapter.DropPreview.BEFORE;
            else if (localY > third * 2f) mode = TreeListAdapter.DropPreview.AFTER;
        }
        if (row.node.parent == null && mode != TreeListAdapter.DropPreview.AS_CHILD) mode = TreeListAdapter.DropPreview.AS_CHILD;
        return new TreeDropTarget(row.node, mode);
    }

    private void updateTreeDropPreview(DragEvent event) {
        TreeDropTarget target = treeDropTargetFromEvent(event);
        if (target == null) {
            clearTreeDropPreview();
            return;
        }
        treeDragPreviewTarget = target.node;
        treeDragPreviewMode = target.mode;
        if (treeAdapter != null) treeAdapter.setDropPreview(target.node, target.mode);
    }

    private void clearTreeDropPreview() {
        treeDragPreviewTarget = null;
        treeDragPreviewMode = TreeListAdapter.DropPreview.NONE;
        if (treeAdapter != null) treeAdapter.clearDropPreview();
    }

    private boolean finishTreeDrop(DragEvent event) {
        NoteNode source = event.getLocalState() instanceof NoteNode ? (NoteNode) event.getLocalState() : treeDragSource;
        TreeDropTarget target = treeDropTargetFromEvent(event);
        clearTreeDropPreview();
        treeDragSource = null;
        if (source == null || target == null || target.node == null) {
            status("Kein gültiges Drop-Ziel");
            return false;
        }
        if (source == target.node) {
            status("Quelle und Ziel sind derselbe Knoten");
            return false;
        }
        saveCurrentEditorToNode();
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Knoten per Drag/Drop verschieben");
        NoteNode moved = null;
        String actionText = "verschoben";
        if (target.mode == TreeListAdapter.DropPreview.AS_CHILD) {
            moved = NoteTreeOps.legacyMoveAsLastChild(source, target.node);
            actionText = "als Unterknoten verschoben";
        } else if (target.mode == TreeListAdapter.DropPreview.AFTER) {
            moved = NoteTreeOps.legacyMoveAfterTarget(source, target.node);
            actionText = "nach Ziel verschoben";
        } else {
            moved = NoteTreeOps.legacyMoveBeforeTarget(source, target.node);
            actionText = "vor Ziel verschoben";
        }
        if (moved == null) {
            status("Dieses Drag/Drop-Ziel ist nicht gültig");
            toast("Ziel nicht gültig");
            return false;
        }
        ensureAncestorsExpanded(moved);
        commitTreeUndoSnapshot(before);
        markDocumentChanged();
        rebuildTree();
        selectNode(moved, false);
        setActivePane(ActivePane.TREE_NODE);
        status("Knoten " + actionText);
        return true;
    }

    private void showTreeContextMenu(NoteNode node) {
        if (node == null) return;
        setActivePane(ActivePane.TREE_NODE);
        if (treeList != null) treeList.requestFocus();
        if (node != currentNode) selectNode(node, false);
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<ContinueCallback> actions = new ArrayList<>();
        addMenuAction(labels, actions, "Rückgängig", this::undoTreeChange);
        addMenuAction(labels, actions, "Wiederholen", this::redoTreeChange);
        addMenuAction(labels, actions, "Neuer Unterknoten", this::newChild);
        addMenuAction(labels, actions, "Neuer Knoten daneben", this::newNext);
        addMenuAction(labels, actions, "Direkt im Baum umbenennen", this::renameCurrentNodeInline);
        addMenuAction(labels, actions, "Kopieren", () -> copyCurrentNode(false));
        addMenuAction(labels, actions, "Ausschneiden", this::cutCurrentNode);
        addMenuAction(labels, actions, "Einfügen", this::pasteNode);
        addMenuAction(labels, actions, "Einfügen als Unterknoten", this::pasteNodeAsChild);
        addMenuAction(labels, actions, "Löschen", this::deleteCurrent);
        addMenuAction(labels, actions, currentNode != null && currentNode.expanded ? "Zuklappen" : "Aufklappen", this::toggleExpanded);
        addMenuAction(labels, actions, "Alle aufklappen", this::expandAllNodes);
        addMenuAction(labels, actions, "Alle zuklappen", this::collapseAllNodes);
        addMenuAction(labels, actions, "Nach oben", this::moveCurrentUp);
        addMenuAction(labels, actions, "Nach unten", this::moveCurrentDown);
        addMenuAction(labels, actions, "Einrücken", this::indentCurrent);
        addMenuAction(labels, actions, "Ausrücken", this::outdentCurrent);
        addMenuAction(labels, actions, "Vor Ziel verschieben…", this::showMoveBeforeTargetDialog);
        addMenuAction(labels, actions, "Haftnotiz / Widget", this::showDesktopNoteDialog);
        addMenuAction(labels, actions, "Haftnotiz-Liste", this::showDesktopNoteTrayList);
        addMenuAction(labels, actions, "Wecker", this::showAlarmDialog);
        addMenuAction(labels, actions, "Knoten-Textfarbe", this::showTreeTextColorPalette);
        addMenuAction(labels, actions, "Knoten-Hintergrund", this::showTreeBackgroundPalette);
        addMenuAction(labels, actions, "Knotenformat normal", () -> applyTreeFormatAction(LegacyRichTextToolbar.findByAction("format_regular")));
        addMenuAction(labels, actions, "RTF in Knoten speichern", this::saveCurrentRtfFromContext);
        addMenuAction(labels, actions, "Knoten als TXT exportieren", this::exportCurrentNodeText);
        addMenuAction(labels, actions, "Knoten als RTF exportieren", this::exportCurrentNodeRtf);
        addMenuAction(labels, actions, "Teilbaum zusammenfassen", this::createUnifiedCurrentNote);
        new AlertDialog.Builder(this)
                .setTitle("Baum: " + safeTitle(currentNode))
                .setItems(labels.toArray(new String[0]), (d, which) -> actions.get(which).run())
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showEditorContextMenu() {
        if (editor == null) return;
        setActivePane(ActivePane.RTF_EDITOR);
        editor.requestFocus();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<ContinueCallback> actions = new ArrayList<>();
        addMenuAction(labels, actions, "Rückgängig", this::undoEditorChange);
        addMenuAction(labels, actions, "Wiederholen", this::redoEditorChange);
        addMenuAction(labels, actions, "Ausschneiden", () -> copyEditorSelectionToClipboard(true));
        addMenuAction(labels, actions, "Kopieren", () -> copyEditorSelectionToClipboard(false));
        addMenuAction(labels, actions, "Einfügen", this::pasteIntoEditorFromClipboard);
        addMenuAction(labels, actions, "Löschen", this::deleteEditorSelectionOrChar);
        addMenuAction(labels, actions, "Alles markieren", this::selectAllEditorText);
        addMenuAction(labels, actions, "Bild einfügen…", this::insertImage);
        addMenuAction(labels, actions, "Stiftbild einfügen…", this::insertInkDrawing);
        addMenuAction(labels, actions, markdownPreviewActive ? "Markdown-Vorschau ausschalten" : "Markdown-Vorschau", this::toggleMarkdownPreview);
        addMenuAction(labels, actions, "Datum einfügen", this::insertDate);
        addMenuAction(labels, actions, "Punkt einfügen", this::insertLegacyBullet);
        addMenuAction(labels, actions, "Normal", () -> applyRtfFormatAction(LegacyRichTextToolbar.findByAction("format_regular")));
        addMenuAction(labels, actions, "Fett", () -> applyRtfFormatAction(LegacyRichTextToolbar.findByAction("format_bold")));
        addMenuAction(labels, actions, "Kursiv", () -> applyRtfFormatAction(LegacyRichTextToolbar.findByAction("format_italic")));
        addMenuAction(labels, actions, "Unterstrichen", () -> applyRtfFormatAction(LegacyRichTextToolbar.findByAction("format_underline")));
        addMenuAction(labels, actions, "Durchgestrichen", () -> applyRtfFormatAction(LegacyRichTextToolbar.findByAction("format_strike")));
        addMenuAction(labels, actions, "Textfarbe…", () -> showRtfColorPalette(LegacyColorDialogModel.Role.RTF_TEXT));
        addMenuAction(labels, actions, "Hintergrund…", () -> showRtfColorPalette(LegacyColorDialogModel.Role.RTF_HIGHLIGHT));
        addMenuAction(labels, actions, "Schriftart…", this::showFontFamilyDialog);
        addMenuAction(labels, actions, "Schriftgröße…", this::showFontSizeDialog);
        addMenuAction(labels, actions, "Linksbündig", () -> applyAlignmentToSelection("align_left"));
        addMenuAction(labels, actions, "Zentriert", () -> applyAlignmentToSelection("align_center"));
        addMenuAction(labels, actions, "Rechtsbündig", () -> applyAlignmentToSelection("align_right"));
        addMenuAction(labels, actions, "Blocksatz", () -> applyAlignmentToSelection("align_justify"));
        addMenuAction(labels, actions, "Suchen", () -> showQuickSearchBar(true));
        new AlertDialog.Builder(this)
                .setTitle("RTF-Text")
                .setItems(labels.toArray(new String[0]), (d, which) -> actions.get(which).run())
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void addMenuAction(List<String> labels, List<ContinueCallback> actions, String label, ContinueCallback action) {
        if (labels == null || actions == null || label == null || action == null) return;
        labels.add(label);
        actions.add(safeAction(label, action));
    }

    private void selectAllEditorText() {
        if (editor == null) return;
        editor.requestFocus();
        editor.selectAll();
        setActivePane(ActivePane.RTF_EDITOR);
        status("RTF-Text markiert");
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

    private boolean requireTreePaneForMiddleAction(String action) {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.TREE_NODE) return true;
        String prefix = action == null || action.isEmpty() ? "Diese Funktion" : action;
        if (active == ActivePane.RTF_EDITOR) {
            status(prefix + " ist eine Baumfunktion. Bitte zuerst den Baum antippen.");
            toast("Baumfunktion: erst Baum antippen");
        } else {
            status(prefix + " ist eine Baumfunktion. Bitte zuerst den Baum statt der Titelzeile antippen.");
            toast("Baumfunktion: Baum antippen");
        }
        return false;
    }

    private void newChildForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Kind")) return;
        newChild();
    }

    private void newNextForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Daneben")) return;
        newNext();
    }

    private void toggleExpandedForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Auf/Zu")) return;
        toggleExpanded();
    }

    private void expandAllForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Alle auf")) return;
        expandAllNodes();
    }

    private void collapseAllForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Alle zu")) return;
        collapseAllNodes();
    }

    private void moveUpForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Rauf")) return;
        moveCurrentUp();
    }

    private void moveDownForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Runter")) return;
        moveCurrentDown();
    }

    private void moveBeforeForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Vor Ziel")) return;
        showMoveBeforeTargetDialog();
    }

    private void desktopNoteForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Haftnotiz")) return;
        showDesktopNoteDialog();
    }

    private void desktopNoteLayoutForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Haft Layout")) return;
        showDesktopNoteLayout();
    }

    private void desktopNoteTrayForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Haftliste")) return;
        showAndroidWidgetList();
    }

    private void clearDesktopNotesForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Haft weg")) return;
        clearDesktopNotesInSubtree();
    }

    private void alarmForActiveMiddleTarget() {
        if (!requireTreePaneForMiddleAction("Wecker")) return;
        showAlarmDialog();
    }

    private void copyForActiveMiddleTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.RTF_EDITOR) {
            copyEditorSelectionToClipboard(false);
        } else if (active == ActivePane.TITLE_TEXT) {
            copyEditTextSelectionToClipboard(titleEdit, false, "Titeltext kopiert");
        } else {
            copyCurrentNode(false);
        }
    }

    private void cutForActiveMiddleTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.RTF_EDITOR) {
            copyEditorSelectionToClipboard(true);
        } else if (active == ActivePane.TITLE_TEXT) {
            copyEditTextSelectionToClipboard(titleEdit, true, "Titeltext ausgeschnitten");
        } else {
            cutCurrentNode();
        }
    }

    private void pasteForActiveMiddleTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.RTF_EDITOR) {
            pasteIntoEditorFromClipboard();
        } else if (active == ActivePane.TITLE_TEXT) {
            pasteIntoEditTextFromClipboard(titleEdit, "Titeltext eingefügt");
        } else {
            pasteNode();
        }
    }

    private void deleteForActiveMiddleTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.RTF_EDITOR) {
            deleteEditorSelectionOrChar();
        } else if (active == ActivePane.TITLE_TEXT) {
            deleteEditTextSelectionOrChar(titleEdit, "Titeltext gelöscht");
        } else {
            deleteCurrent();
        }
    }

    private void indentForActiveMiddleTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.RTF_EDITOR) {
            applyEditorParagraphIndent(+1);
        } else if (active == ActivePane.TREE_NODE) {
            indentCurrent();
        } else {
            status("Einrücken: Titelzeile aktiv. Für Baum erst Baum antippen, für Text den RTF-Editor antippen.");
            toast("Baum oder RTF-Text antippen");
        }
    }

    private void outdentForActiveMiddleTarget() {
        ActivePane active = activePaneForMiddleToolbar();
        if (active == ActivePane.RTF_EDITOR) {
            applyEditorParagraphIndent(-1);
        } else if (active == ActivePane.TREE_NODE) {
            outdentCurrent();
        } else {
            status("Ausrücken: Titelzeile aktiv. Für Baum erst Baum antippen, für Text den RTF-Editor antippen.");
            toast("Baum oder RTF-Text antippen");
        }
    }

    private int[] editTextSelectionRange(EditText target) {
        int len = target == null || target.getText() == null ? 0 : target.getText().length();
        int start = target == null ? 0 : Math.max(0, Math.min(target.getSelectionStart(), len));
        int end = target == null ? start : Math.max(0, Math.min(target.getSelectionEnd(), len));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        return new int[]{start, end};
    }

    private void copyEditTextSelectionToClipboard(EditText target, boolean cut, String message) {
        if (target == null || target.getText() == null) return;
        int[] range = editTextSelectionRange(target);
        if (range[1] <= range[0]) {
            toast("Kein Text markiert");
            return;
        }
        Editable editable = target.getText();
        CharSequence selected = editable.subSequence(range[0], range[1]);
        setPlainSystemClipboard(selected);
        internalClipboardNode = null;
        internalEditorClipboard = null;
        internalEditorClipboardPlain = "";
        if (cut) {
            editable.delete(range[0], range[1]);
            if (target == titleEdit) {
                titleDirty = true;
                markDocumentChanged();
                rebuildTree();
                updateTitle();
            }
        }
        status(message == null || message.isEmpty() ? (cut ? "Text ausgeschnitten" : "Text kopiert") : message);
    }

    private void pasteIntoEditTextFromClipboard(EditText target, String message) {
        if (target == null || target.getText() == null) return;
        CharSequence payload = systemClipboardText();
        if (payload == null) {
            toast("Zwischenablage enthält keinen Text");
            return;
        }
        int[] range = editTextSelectionRange(target);
        target.getText().replace(range[0], range[1], payload);
        int cursor = Math.max(0, Math.min(range[0] + payload.length(), target.getText().length()));
        target.setSelection(cursor);
        if (target == titleEdit) {
            titleDirty = true;
            markDocumentChanged();
            rebuildTree();
            updateTitle();
        }
        status(message == null || message.isEmpty() ? "Text eingefügt" : message);
    }

    private void deleteEditTextSelectionOrChar(EditText target, String message) {
        if (target == null || target.getText() == null) return;
        Editable editable = target.getText();
        int[] range = editTextSelectionRange(target);
        if (range[1] > range[0]) {
            editable.delete(range[0], range[1]);
        } else if (range[0] < editable.length()) {
            editable.delete(range[0], range[0] + 1);
        } else {
            toast("Nichts zu löschen");
            return;
        }
        if (target == titleEdit) {
            titleDirty = true;
            markDocumentChanged();
            rebuildTree();
            updateTitle();
        }
        status(message == null || message.isEmpty() ? "Text gelöscht" : message);
    }

    private void copyEditorSelectionToClipboard(boolean cut) {
        if (cut && blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null || editor.getText() == null) return;
        int[] range = editorSelectionRange(false);
        if (range[1] <= range[0]) {
            toast("Kein RTF-Text markiert");
            return;
        }
        SpannableStringBuilder selected = new SpannableStringBuilder(editor.getText().subSequence(range[0], range[1]));
        internalClipboardNode = null;
        internalEditorClipboard = selected;
        internalEditorClipboardPlain = selected.toString();
        setPlainSystemClipboard(internalEditorClipboardPlain);
        if (cut) {
            pushEditorUndoSnapshot("cut");
            editor.getText().delete(range[0], range[1]);
            markEditorRichChanged("RTF-Text ausgeschnitten");
        } else {
            status("RTF-Text kopiert");
        }
    }

    private void pasteIntoEditorFromClipboard() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null || editor.getText() == null) return;
        CharSequence systemText = systemClipboardText();
        CharSequence payload = null;
        if (internalEditorClipboard != null && internalEditorClipboard.length() > 0) {
            String systemPlain = systemText == null ? "" : systemText.toString();
            if (systemPlain.equals(internalEditorClipboardPlain)) payload = new SpannableStringBuilder(internalEditorClipboard);
        }
        if (payload == null) payload = systemText;
        if (payload == null) {
            toast("Zwischenablage enthält keinen Text");
            return;
        }
        Editable editable = editor.getText();
        int[] range = editorSelectionRange(false);
        pushEditorUndoSnapshot("paste");
        editable.replace(range[0], range[1], payload);
        int cursor = Math.max(0, Math.min(range[0] + payload.length(), editable.length()));
        editor.setSelection(cursor);
        markEditorRichChanged("RTF-Text eingefügt");
    }

    private void deleteEditorSelectionOrChar() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null || editor.getText() == null) return;
        Editable editable = editor.getText();
        int[] range = editorSelectionRange(false);
        if (range[1] > range[0]) {
            pushEditorUndoSnapshot("delete");
            editable.delete(range[0], range[1]);
        } else if (range[0] < editable.length()) {
            pushEditorUndoSnapshot("delete");
            editable.delete(range[0], range[0] + 1);
        } else {
            toast("Nichts zu löschen");
            return;
        }
        markEditorRichChanged("RTF-Text gelöscht");
    }

    private void applyEditorParagraphIndent(int direction) {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null || editor.getText() == null) return;
        Spannable text = editor.getText();
        if (text.length() == 0) {
            toast("Kein RTF-Text vorhanden");
            return;
        }
        int[] selected = editorSelectionRange(true);
        int[] para = paragraphRange(text.toString(), selected[0], selected[1]);
        int current = 0;
        LeadingMarginSpan.Standard[] spans = text.getSpans(para[0], para[1], LeadingMarginSpan.Standard.class);
        for (LeadingMarginSpan.Standard span : spans) {
            current = Math.max(current, Math.max(span.getLeadingMargin(true), span.getLeadingMargin(false)));
        }
        pushEditorUndoSnapshot("indent");
        removeOverlappingSpans(text, para[0], para[1], LeadingMarginSpan.Standard.class);
        int step = dp(24);
        int next = Math.max(0, current + (direction < 0 ? -step : step));
        if (next > 0 && para[1] > para[0]) {
            text.setSpan(new LeadingMarginSpan.Standard(next, next), para[0], para[1], Spanned.SPAN_PARAGRAPH);
        }
        markEditorRichChanged(next > 0 ? "RTF-Absatz eingerückt" : "RTF-Absatzeinzug entfernt");
    }

    private CharSequence systemClipboardText() {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard == null || !clipboard.hasPrimaryClip()) return null;
            ClipData clip = clipboard.getPrimaryClip();
            if (clip == null || clip.getItemCount() == 0) return null;
            CharSequence text = clip.getItemAt(0).coerceToText(this);
            if (text == null) return null;
            return text;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void setPlainSystemClipboard(CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence safe = text == null ? "" : text;
        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("Notizen", safe));
    }

    private void moveCurrentUp() {
        if (currentNode == null || currentNode.parent == null) return;
        List<NoteNode> siblings = currentNode.parent.children;
        int index = siblings.indexOf(currentNode);
        if (index <= 0) {
            status("Knoten ist schon oben");
            return;
        }
        pushTreeUndoSnapshot("Knoten nach oben verschieben");
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
        pushTreeUndoSnapshot("Knoten nach unten verschieben");
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
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Knoten einrücken");
        if (NoteTreeOps.indentUnderPreviousSibling(currentNode) == null) {
            status("Einrücken hier nicht möglich");
            return;
        }
        commitTreeUndoSnapshot(before);
        markDocumentChanged();
        rebuildTree();
        selectNode(currentNode, true);
        status("Knoten eingerückt");
    }

    private void outdentCurrent() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Knoten ausrücken");
        if (NoteTreeOps.outdentAfterParent(currentNode) == null) {
            status("Ausrücken hier nicht möglich");
            return;
        }
        commitTreeUndoSnapshot(before);
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
                    LegacyTreeUndoModel.Snapshot before = captureTreeUndoSnapshot("Knoten vor Ziel verschieben");
                    if (LegacyTreeDragDrop.moveBefore(currentNode, target, document.ensureRoot()) == null) {
                        status("Dieses Ziel ist nicht gültig");
                        return;
                    }
                    ensureAncestorsExpanded(currentNode);
                    commitTreeUndoSnapshot(before);
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
        if (blockEditorMutationWhileMarkdownPreview()) return;
        String value = LegacyEditorActions.androidDateInsertText(new Date());
        int start = Math.max(0, editor.getSelectionStart());
        int end = Math.max(start, editor.getSelectionEnd());
        pushEditorUndoSnapshot("date");
        editor.getText().replace(start, end, value);
        markEditorRichChanged("Legacy-Datum eingefügt");
    }

    private void insertLegacyBullet() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null) return;
        int start = Math.max(0, editor.getSelectionStart());
        int end = Math.max(start, editor.getSelectionEnd());
        pushEditorUndoSnapshot("bullet");
        editor.getText().replace(start, end, LegacyEditorActions.androidBulletInsertText());
        markEditorRichChanged("Legacy-Aufzählungspunkt eingefügt");
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
        pushTreeUndoSnapshot("Knotentitel ergänzen");
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
        if ("format_bold".equals(action)) { toggleTreeTitleStyle("bold", "Baumtext fett", "Baumtext nicht fett"); return; }
        if ("format_italic".equals(action)) { toggleTreeTitleStyle("italic", "Baumtext kursiv", "Baumtext nicht kursiv"); return; }
        if ("format_underline".equals(action)) { toggleTreeTitleStyle("underline", "Baumtext unterstrichen", "Baumtext nicht unterstrichen"); return; }
        if ("format_strike".equals(action)) { toggleTreeTitleStyle("strike", "Baumtext durchgestrichen", "Baumtext nicht durchgestrichen"); return; }
        if ("font_bigger".equals(action)) { applyTreeFontSize(LegacyRichTextToolbar.nextFontSize(currentTreeTitleSize(), +1)); return; }
        if ("font_smaller".equals(action)) { applyTreeFontSize(LegacyRichTextToolbar.nextFontSize(currentTreeTitleSize(), -1)); return; }
        if (action.startsWith("align_")) { toast("Ausrichtung betrifft nur die RTF-Box."); return; }
        status(spec.tooltip == null || spec.tooltip.isEmpty() ? "Baumformat-Aktion" : spec.tooltip);
    }

    private void toggleTreeTitleStyle(String style, String enabledMessage, String disabledMessage) {
        if (currentNode == null || style == null || style.isEmpty()) return;
        pushTreeUndoSnapshot("Baumtext-Stil ändern");
        String raw = currentNode.extraAttrs.get(NODE_TITLE_STYLE_ATTR);
        LinkedHashMap<String, Boolean> values = new LinkedHashMap<>();
        if (raw != null) {
            for (String part : raw.split(",")) {
                String clean = part.trim().toLowerCase(Locale.ROOT);
                if (!clean.isEmpty()) values.put(clean, Boolean.TRUE);
            }
        }
        String cleanStyle = style.toLowerCase(Locale.ROOT);
        boolean enable = !values.containsKey(cleanStyle);
        if (enable) values.put(cleanStyle, Boolean.TRUE); else values.remove(cleanStyle);
        StringBuilder out = new StringBuilder();
        for (String key : values.keySet()) {
            if (out.length() > 0) out.append(',');
            out.append(key);
        }
        if (out.length() == 0) currentNode.extraAttrs.remove(NODE_TITLE_STYLE_ATTR);
        else currentNode.extraAttrs.put(NODE_TITLE_STYLE_ATTR, out.toString());
        markTreeTextFormatChanged(enable ? enabledMessage : disabledMessage);
    }

    private void clearTreeTextFormatting() {
        if (currentNode == null) return;
        pushTreeUndoSnapshot("Baumtext-Format löschen");
        currentNode.extraAttrs.remove(NODE_TITLE_STYLE_ATTR);
        currentNode.extraAttrs.remove(NODE_TITLE_FONT_ATTR);
        currentNode.extraAttrs.remove(NODE_TITLE_SIZE_ATTR);
        currentNode.fgArgb = 0;
        currentNode.bgArgb = 0;
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
        pushTreeUndoSnapshot("Baum-Schriftart ändern");
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
        pushTreeUndoSnapshot("Baum-Schriftgröße ändern");
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


    private void zoomTreeTextByStep(int direction) {
        if (currentNode == null || direction == 0) return;
        setActivePane(ActivePane.TREE_NODE);
        int next = LegacyTouchZoomModel.fontSizeByStep(currentTreeTitleSize(), direction);
        applyTreeFontSize(next);
    }

    private void zoomRtfTextByStep(int direction) {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null || direction == 0) return;
        setActivePane(ActivePane.RTF_EDITOR);
        Spannable text = editor.getText();
        if (text == null || text.length() == 0) return;
        int len = text.length();
        int start = Math.max(0, Math.min(editor.getSelectionStart(), len));
        int end = Math.max(0, Math.min(editor.getSelectionEnd(), len));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        boolean whole = start == end;
        if (whole) { start = 0; end = len; }
        if (end <= start) return;

        ArrayList<SizeRun> runs = new ArrayList<>();
        int pos = start;
        while (pos < end) {
            int next = Math.min(end, text.nextSpanTransition(pos, end, AbsoluteSizeSpan.class));
            if (next <= pos) next = pos + 1;
            int current = editorPointSizeAt(text, pos);
            runs.add(new SizeRun(pos, next, LegacyTouchZoomModel.fontSizeByStep(current, direction)));
            pos = next;
        }
        pushEditorUndoSnapshot("zoom");
        removeOverlappingSpans(text, start, end, AbsoluteSizeSpan.class);
        for (SizeRun run : runs) {
            if (run.end > run.start) text.setSpan(new AbsoluteSizeSpan(run.size, true), run.start, run.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        markEditorRichChanged((whole ? "RTF-Schrift komplett " : "RTF-Auswahl ") + (direction > 0 ? "vergrößert" : "verkleinert"));
    }

    private int editorPointSizeAt(Spanned text, int pos) {
        if (text == null || text.length() == 0) return 17;
        int safe = Math.max(0, Math.min(pos, text.length() - 1));
        AbsoluteSizeSpan[] spans = text.getSpans(safe, safe + 1, AbsoluteSizeSpan.class);
        if (spans != null && spans.length > 0) return Math.max(1, spans[spans.length - 1].getSize());
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
        if (scaledDensity <= 0f) return 17;
        return Math.max(1, Math.round(editor.getTextSize() / scaledDensity));
    }

    private void zoomToolbarButtonsByStep(int direction) {
        if (direction == 0) return;
        int next = LegacyTouchZoomModel.toolbarButtonDpByStep(toolbarButtonDp, direction);
        if (next == toolbarButtonDp) return;
        toolbarButtonDp = next;
        requestApplyToolbarButtonSize();
        scheduleAndroidUiZoomSettingsSave();
    }

    private void requestApplyToolbarButtonSize() {
        if (toolbarButtonSizeApplyScheduled) return;
        toolbarButtonSizeApplyScheduled = true;
        if (mainHandler != null) {
            mainHandler.post(() -> {
                toolbarButtonSizeApplyScheduled = false;
                applyToolbarButtonSize();
            });
        } else {
            toolbarButtonSizeApplyScheduled = false;
            applyToolbarButtonSize();
        }
    }

    private void applyToolbarButtonSize() {
        for (TextView view : toolbarSquareViews) {
            if (view == null) continue;
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null) {
                params.width = dp(toolbarButtonDp);
                params.height = dp(toolbarButtonDp);
                view.setLayoutParams(params);
            }
            view.setTextSize(toolbarGlyphTextSize(view.getText() == null ? "" : view.getText().toString()));
        }
    }

    private void zoomHeaderTextByStep(int direction) {
        if (direction == 0) return;
        float next = LegacyTouchZoomModel.headerTextSpByStep(headerTextSp, direction);
        if (Math.abs(next - headerTextSp) < 0.01f) return;
        headerTextSp = next;
        applyHeaderTextSize();
        scheduleAndroidUiZoomSettingsSave();
    }

    private void applyHeaderTextSize() {
        if (rootTitleView != null) rootTitleView.setTextSize(headerTextSp);
        if (titleEdit != null) titleEdit.setTextSize(headerTextSp);
    }

    private void scheduleAndroidUiZoomSettingsSave() {
        if (mainHandler == null) {
            saveAndroidUiZoomSettings();
            return;
        }
        if (androidUiZoomSaveRunnable == null) androidUiZoomSaveRunnable = this::saveAndroidUiZoomSettings;
        mainHandler.removeCallbacks(androidUiZoomSaveRunnable);
        mainHandler.postDelayed(androidUiZoomSaveRunnable, UI_ZOOM_SAVE_DELAY_MS);
    }

    private void flushAndroidUiZoomSettingsNow() {
        if (mainHandler != null && androidUiZoomSaveRunnable != null) mainHandler.removeCallbacks(androidUiZoomSaveRunnable);
        saveAndroidUiZoomSettings();
    }

    private void saveAndroidUiZoomSettings() {
        if (settings == null) settings = AndroidSettingsStore.load(this);
        settings.androidToolbarButtonDp = LegacySettings.normalizeAndroidToolbarButtonDp(toolbarButtonDp);
        settings.androidHeaderTextSp = LegacySettings.normalizeAndroidHeaderTextSp(headerTextSp);
        AndroidSettingsStore.save(this, settings);
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
        if (blockEditorMutationWhileMarkdownPreview()) return;
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
        pushEditorUndoSnapshot("format");
        if ("format_regular".equals(normalized)) {
            removeCharacterFormatting(text, range[0], range[1]);
            markEditorRichChanged("Zeichenformat zurückgesetzt");
        } else if ("format_bold".equals(normalized)) {
            if (isRtfFormatActionActive("format_bold")) {
                removeStyleFromRange(text, range[0], range[1], Typeface.BOLD);
                markEditorRichChanged("Fett entfernt");
            } else {
                text.setSpan(new StyleSpan(Typeface.BOLD), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markEditorRichChanged("Fett angewendet");
            }
        } else if ("format_italic".equals(normalized)) {
            if (isRtfFormatActionActive("format_italic")) {
                removeStyleFromRange(text, range[0], range[1], Typeface.ITALIC);
                markEditorRichChanged("Kursiv entfernt");
            } else {
                text.setSpan(new StyleSpan(Typeface.ITALIC), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markEditorRichChanged("Kursiv angewendet");
            }
        } else if ("format_underline".equals(normalized)) {
            if (isRtfFormatActionActive("format_underline")) {
                removeOverlappingSpans(text, range[0], range[1], UnderlineSpan.class);
                markEditorRichChanged("Unterstrichen entfernt");
            } else {
                text.setSpan(new UnderlineSpan(), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markEditorRichChanged("Unterstrichen angewendet");
            }
        } else if ("format_strike".equals(normalized)) {
            if (isRtfFormatActionActive("format_strike")) {
                removeOverlappingSpans(text, range[0], range[1], StrikethroughSpan.class);
                markEditorRichChanged("Durchgestrichen entfernt");
            } else {
                text.setSpan(new StrikethroughSpan(), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                markEditorRichChanged("Durchgestrichen angewendet");
            }
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
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || editor == null) return;
        String title = role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT ? "RTF-Hintergrundfarbe" : "RTF-Textfarbe";
        String[] palette = LegacyColorDialogModel.paletteLabels();
        String[] labels = new String[palette.length + 1];
        for (int i = 0; i < palette.length; i++) labels[i] = palette[i];
        labels[palette.length] = role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT ? "RTF-Hintergrund löschen" : "RTF-Textfarbe löschen";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, (d, which) -> {
                    if (which >= palette.length) clearRtfColorFromSelection(role == LegacyColorDialogModel.Role.RTF_HIGHLIGHT);
                    else applyRtfColorToSelection(LegacyColorDialogModel.choosePalette(role, which));
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void applyRtfColorToSelection(LegacyColorDialogModel.Decision decision) {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || editor == null || decision == null || !decision.valid) return;
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            toast("Kein Text zum Färben vorhanden");
            return;
        }
        Spannable text = editor.getText();
        try {
            int color = Color.parseColor(decision.css);
            pushEditorUndoSnapshot("color");
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
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || editor == null) return;
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            toast("Kein Text zum Färben vorhanden");
            return;
        }
        Spannable text = editor.getText();
        pushEditorUndoSnapshot("clear-color");
        if (background) {
            removeOverlappingSpans(text, range[0], range[1], BackgroundColorSpan.class);
            markEditorRichChanged("RTF-Hintergrundfarbe entfernt");
        } else {
            removeOverlappingSpans(text, range[0], range[1], ForegroundColorSpan.class);
            markEditorRichChanged("RTF-Textfarbe entfernt");
        }
    }

    private void showFontFamilyDialog() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
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
        if (blockEditorMutationWhileMarkdownPreview()) return;
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
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || editor == null) return;
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            status("Kein Text für Schriftart");
            return;
        }
        Spannable text = editor.getText();
        String clean = LegacyRichTextToolbar.normalizeFontFamily(family);
        pushEditorUndoSnapshot("font-family");
        removeOverlappingSpans(text, range[0], range[1], RtfTypefaceSpan.class);
        text.setSpan(new RtfTypefaceSpan(clean), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        markEditorRichChanged("Schriftart angewendet: " + clean);
    }

    private void applyFontSizeToSelection(int size) {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || editor == null) return;
        int clamped = LegacyRichTextToolbar.normalizeFontSize(size);
        int[] range = editorSelectionRange(true);
        if (range[1] <= range[0]) {
            status("Kein Text für Schriftgröße");
            return;
        }
        Spannable text = editor.getText();
        pushEditorUndoSnapshot("font-size");
        removeOverlappingSpans(text, range[0], range[1], AbsoluteSizeSpan.class);
        text.setSpan(new AbsoluteSizeSpan(clamped, true), range[0], range[1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        markEditorRichChanged("Schriftgröße angewendet: " + clamped + " pt");
    }

    private void applyAlignmentToSelection(String action) {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editor == null) return;
        Spannable text = editor.getText();
        if (text.length() == 0) return;
        int[] selected = editorSelectionRange(true);
        int[] para = paragraphRange(text.toString(), selected[0], selected[1]);
        pushEditorUndoSnapshot("align");
        removeOverlappingSpans(text, para[0], para[1], AlignmentSpan.class);
        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        String legacyAlign = "left";
        String message = "Linksbündig angewendet";
        if ("align_center".equals(action)) { alignment = Layout.Alignment.ALIGN_CENTER; legacyAlign = "center"; message = "Zentriert angewendet"; }
        else if ("align_right".equals(action)) { alignment = Layout.Alignment.ALIGN_OPPOSITE; legacyAlign = "right"; message = "Rechtsbündig angewendet"; }
        else if ("align_justify".equals(action)) { alignment = Layout.Alignment.ALIGN_NORMAL; legacyAlign = "justify"; message = "Blocksatz gespeichert"; }
        if (para[1] > para[0]) text.setSpan(new RtfParagraphAlignmentSpan(alignment, legacyAlign), para[0], para[1], Spanned.SPAN_PARAGRAPH);
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
        removeOverlappingSpans(text, para[0], para[1], AlignmentSpan.class);
        removeOverlappingSpans(text, para[0], para[1], LeadingMarginSpan.Standard.class);
    }

    private void removeStyleFromRange(Spannable text, int start, int end, int styleToRemove) {
        if (text == null || end <= start) return;
        StyleSpan[] spans = text.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : spans) {
            int style = span.getStyle();
            boolean hasTarget = styleToRemove == Typeface.BOLD
                    ? (style == Typeface.BOLD || style == Typeface.BOLD_ITALIC)
                    : (style == Typeface.ITALIC || style == Typeface.BOLD_ITALIC);
            if (!hasTarget) continue;
            int oldStart = text.getSpanStart(span);
            int oldEnd = text.getSpanEnd(span);
            if (oldEnd <= start || oldStart >= end) continue;
            int flags = text.getSpanFlags(span);
            text.removeSpan(span);
            if (oldStart < start) text.setSpan(new StyleSpan(style), oldStart, start, flags);
            if (oldEnd > end) text.setSpan(new StyleSpan(style), end, oldEnd, flags);
            int replacement = 0;
            if (style == Typeface.BOLD_ITALIC) replacement = styleToRemove == Typeface.BOLD ? Typeface.ITALIC : Typeface.BOLD;
            int overlapStart = Math.max(oldStart, start);
            int overlapEnd = Math.min(oldEnd, end);
            if (replacement != 0 && overlapEnd > overlapStart) text.setSpan(new StyleSpan(replacement), overlapStart, overlapEnd, flags);
        }
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
        if (span instanceof RtfParagraphAlignmentSpan) return new RtfParagraphAlignmentSpan(((RtfParagraphAlignmentSpan) span).getAlignment(), ((RtfParagraphAlignmentSpan) span).legacyAlign);
        if (span instanceof AlignmentSpan) return new AlignmentSpan.Standard(((AlignmentSpan) span).getAlignment());
        if (span instanceof LeadingMarginSpan.Standard) return new LeadingMarginSpan.Standard(((LeadingMarginSpan.Standard) span).getLeadingMargin(true), ((LeadingMarginSpan.Standard) span).getLeadingMargin(false));
        if (span instanceof RtfRawSpan) return new RtfRawSpan(((RtfRawSpan) span).rawRtf, ((RtfRawSpan) span).kind);
        return null;
    }

    private void pushEditorUndoSnapshot(String reason) {
        if (editor == null || loadingEditor || editorHistoryRestoring || editorHistorySnapshotLocked) return;
        EditorHistoryEntry entry = captureEditorHistoryEntry();
        if (entry == null) return;
        if (!editorUndoStack.isEmpty() && sameEditorHistory(editorUndoStack.get(editorUndoStack.size() - 1), entry)) return;
        editorUndoStack.add(entry);
        trimEditorHistory(editorUndoStack);
        editorRedoStack.clear();
        scheduleFormatToolbarStateUpdate();
    }

    private void maybePushEditorTypingUndoSnapshot() {
        long now = System.currentTimeMillis();
        if (!editorUndoStack.isEmpty() && now - lastEditorTypingUndoAt < EDITOR_TYPING_UNDO_INTERVAL_MS) return;
        pushEditorUndoSnapshot("typing");
        lastEditorTypingUndoAt = now;
    }

    private EditorHistoryEntry captureEditorHistoryEntry() {
        if (editor == null || editor.getText() == null) return null;
        int len = editor.getText().length();
        int start = Math.max(0, Math.min(editor.getSelectionStart(), len));
        int end = Math.max(0, Math.min(editor.getSelectionEnd(), len));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        return new EditorHistoryEntry(editor.getText(), start, end);
    }

    private boolean sameEditorHistory(EditorHistoryEntry a, EditorHistoryEntry b) {
        if (a == null || b == null) return false;
        return a.selectionStart == b.selectionStart && a.selectionEnd == b.selectionEnd
                && a.signature.equals(b.signature);
    }

    private void trimEditorHistory(ArrayList<EditorHistoryEntry> stack) {
        if (stack == null) return;
        int start = LegacyRtfUndoModel.trimStartIndex(stack.size(), LegacyRtfUndoModel.DEFAULT_LIMIT);
        if (start <= 0) return;
        for (int i = 0; i < start; i++) stack.remove(0);
    }

    private ArrayList<LegacyTreeUndoModel.Snapshot> copyTreeHistoryStack(ArrayList<LegacyTreeUndoModel.Snapshot> source) {
        ArrayList<LegacyTreeUndoModel.Snapshot> copy = new ArrayList<>();
        if (source == null) return copy;
        for (LegacyTreeUndoModel.Snapshot snapshot : source) {
            if (snapshot != null) copy.add(new LegacyTreeUndoModel.Snapshot(snapshot.root, snapshot.selectedPath, snapshot.reason));
        }
        return copy;
    }

    private void restoreTreeHistoryStacks(ArrayList<LegacyTreeUndoModel.Snapshot> undo, ArrayList<LegacyTreeUndoModel.Snapshot> redo) {
        treeUndoStack.clear();
        treeRedoStack.clear();
        if (undo != null) treeUndoStack.addAll(copyTreeHistoryStack(undo));
        if (redo != null) treeRedoStack.addAll(copyTreeHistoryStack(redo));
    }

    private void clearTreeHistory() {
        treeUndoStack.clear();
        treeRedoStack.clear();
    }

    private LegacyTreeUndoModel.Snapshot captureTreeUndoSnapshot(String reason) {
        if (document == null || treeHistoryRestoring) return null;
        saveCurrentEditorToNode();
        return LegacyTreeUndoModel.capture(document, currentNode, reason);
    }

    private void commitTreeUndoSnapshot(LegacyTreeUndoModel.Snapshot snapshot) {
        if (snapshot == null || treeHistoryRestoring) return;
        if (!treeUndoStack.isEmpty() && LegacyTreeUndoModel.sameSnapshot(treeUndoStack.get(treeUndoStack.size() - 1), snapshot)) return;
        treeUndoStack.add(snapshot);
        trimTreeHistory(treeUndoStack);
        treeRedoStack.clear();
        scheduleFormatToolbarStateUpdate();
    }

    private void pushTreeUndoSnapshot(String reason) {
        commitTreeUndoSnapshot(captureTreeUndoSnapshot(reason));
    }

    private void trimTreeHistory(ArrayList<LegacyTreeUndoModel.Snapshot> stack) {
        if (stack == null) return;
        int start = LegacyTreeUndoModel.trimStartIndex(stack.size(), LegacyTreeUndoModel.DEFAULT_LIMIT);
        if (start <= 0) return;
        for (int i = 0; i < start; i++) stack.remove(0);
    }

    private void undoTreeChange() {
        if (treeUndoStack.isEmpty()) {
            toast("Keine Baum-Änderung zum Rückgängigmachen.");
            return;
        }
        saveCurrentEditorToNode();
        LegacyTreeUndoModel.Snapshot current = LegacyTreeUndoModel.capture(document, currentNode, "redo");
        if (current != null) {
            treeRedoStack.add(current);
            trimTreeHistory(treeRedoStack);
        }
        LegacyTreeUndoModel.Snapshot snapshot = treeUndoStack.remove(treeUndoStack.size() - 1);
        restoreTreeHistory(snapshot, "Baum rückgängig");
    }

    private void redoTreeChange() {
        if (treeRedoStack.isEmpty()) {
            toast("Keine Baum-Änderung zum Wiederholen.");
            return;
        }
        saveCurrentEditorToNode();
        LegacyTreeUndoModel.Snapshot current = LegacyTreeUndoModel.capture(document, currentNode, "undo");
        if (current != null) {
            treeUndoStack.add(current);
            trimTreeHistory(treeUndoStack);
        }
        LegacyTreeUndoModel.Snapshot snapshot = treeRedoStack.remove(treeRedoStack.size() - 1);
        restoreTreeHistory(snapshot, "Baum wiederholt");
    }

    private void restoreTreeHistory(LegacyTreeUndoModel.Snapshot snapshot, String message) {
        if (snapshot == null || document == null) return;
        NoteNode root = snapshot.copyRoot();
        if (root == null) return;
        if (markdownPreviewActive) clearMarkdownPreviewState(false);
        treeHistoryRestoring = true;
        try {
            document.root = root;
            document.markChanged();
            NoteNode selected = LegacyTreeUndoModel.nodeAtPath(document.ensureRoot(), snapshot.selectedPath);
            if (selected == null) selected = document.ensureRoot();
            currentNode = null;
            rebuildTree();
            selectNode(selected, false);
            setActivePane(ActivePane.TREE_NODE);
            updateTitle();
            scheduleRuntimeSnapshotSave();
            toast(message == null || message.isEmpty() ? "Baum-Historie wiederhergestellt" : message);
        } finally {
            treeHistoryRestoring = false;
        }
    }

    private void undoForActiveTarget() {
        if (shouldFormatTreeTarget()) undoTreeChange();
        else undoEditorChange();
    }

    private void redoForActiveTarget() {
        if (shouldFormatTreeTarget()) redoTreeChange();
        else redoEditorChange();
    }

    private void undoEditorChange() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editorUndoStack.isEmpty()) {
            toast("Keine RTF-Änderung zum Rückgängigmachen.");
            return;
        }
        EditorHistoryEntry current = captureEditorHistoryEntry();
        if (current != null) {
            editorRedoStack.add(current);
            trimEditorHistory(editorRedoStack);
        }
        EditorHistoryEntry entry = editorUndoStack.remove(editorUndoStack.size() - 1);
        restoreEditorHistory(entry, "RTF rückgängig");
    }

    private void redoEditorChange() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (editorRedoStack.isEmpty()) {
            toast("Keine RTF-Änderung zum Wiederholen.");
            return;
        }
        EditorHistoryEntry current = captureEditorHistoryEntry();
        if (current != null) {
            editorUndoStack.add(current);
            trimEditorHistory(editorUndoStack);
        }
        EditorHistoryEntry entry = editorRedoStack.remove(editorRedoStack.size() - 1);
        restoreEditorHistory(entry, "RTF wiederholt");
    }

    private void restoreEditorHistory(EditorHistoryEntry entry, String message) {
        if (entry == null || editor == null) return;
        boolean oldLoading = loadingEditor;
        editorHistoryRestoring = true;
        loadingEditor = true;
        try {
            editor.setText(new SpannableStringBuilder(entry.text));
            int len = editor.getText().length();
            int start = Math.max(0, Math.min(entry.selectionStart, len));
            int end = Math.max(start, Math.min(entry.selectionEnd, len));
            editor.setSelection(start, end);
        } finally {
            loadingEditor = oldLoading;
            editorHistoryRestoring = false;
        }
        editorDirty = true;
        markDocumentChanged();
        updateTitle();
        scheduleFormatToolbarStateUpdate();
        toast(message);
    }

    private void markEditorRichChanged(String message) {
        if (markdownPreviewActive) {
            editorDirty = false;
            updateMarkdownPreviewButtonState();
            return;
        }
        editorDirty = true;
        if (!editorHistoryRestoring) editorRedoStack.clear();
        if (document != null) markDocumentChanged();
        updateTitle();
        scheduleFormatToolbarStateUpdate();
        status(message == null || message.isEmpty() ? "RTF-Format angewendet" : message);
    }

    private void insertImage() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null) return;
        startActivityForResult(intentForDialog(LegacyFileDialogModel.insertImage(currentDialogDirectory())), REQ_INSERT_IMAGE);
    }

    private void insertInkDrawing() {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || editor == null) return;
        setActivePane(ActivePane.RTF_EDITOR);
        final InkCanvasView ink = new InkCanvasView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(2), dp(10), 0);

        TextView hint = new TextView(this);
        hint.setText("Mit Samsung-Stift oder Finger schreiben. Beim Samsung-Stift wird die Druckstärke ausgewertet: stärkerer Druck zeichnet dicker. Beim Einfügen wird die Handschrift als Bild in den RTF-Text gespeichert.");
        hint.setTextSize(12f);
        hint.setTextColor(Color.rgb(60, 68, 80));
        hint.setPadding(0, 0, 0, dp(6));
        box.addView(hint, new LinearLayout.LayoutParams(-1, -2));

        int screenW = getResources().getDisplayMetrics().widthPixels;
        int padW = Math.max(dp(LegacyInkPictureModel.MIN_WIDTH_DP), Math.min(screenW - dp(48), dp(LegacyInkPictureModel.DEFAULT_WIDTH_DP)));
        int padH = Math.max(dp(220), Math.min(getResources().getDisplayMetrics().heightPixels / 2, dp(LegacyInkPictureModel.DEFAULT_HEIGHT_DP)));
        box.addView(ink, new LinearLayout.LayoutParams(padW, padH));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Stiftbild in RTF einfügen")
                .setView(box)
                .setNegativeButton("Abbrechen", null)
                .setNeutralButton("Leeren", null)
                .setPositiveButton("Einfügen", null)
                .create();
        dialog.setOnShowListener(d -> {
            Button clear = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (clear != null) clear.setOnClickListener(v -> ink.clearInk());
            Button insert = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (insert != null) insert.setOnClickListener(v -> {
                if (!ink.hasInk()) {
                    Toast.makeText(this, "Das Stiftfeld ist leer.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap bitmap = null;
                try {
                    bitmap = ink.renderBitmap();
                    PreparedImage prepared = compressBitmapForRtf(bitmap, "Stiftbild eingefügt und als RTF-Bild gespeichert");
                    if (insertPreparedImageIntoEditor(prepared, "Stiftbild einfügen")) dialog.dismiss();
                } catch (OutOfMemoryError oom) {
                    error("Stiftbild einfügen", "Das Stiftbild war zu groß für den verfügbaren Android-Speicher.");
                } catch (Exception ex) {
                    error("Stiftbild einfügen", ex.getMessage());
                } finally {
                    if (bitmap != null) {
                        try { bitmap.recycle(); } catch (Exception ignored) {}
                    }
                }
            });
        });
        dialog.show();
    }

    private void insertImageFromUri(Uri uri) {
        if (blockEditorMutationWhileMarkdownPreview()) return;
        if (currentNode == null || uri == null || editor == null) return;
        try {
            PreparedImage image = prepareImageForRtf(uri);
            insertPreparedImageIntoEditor(image, "Bild einfügen");
        } catch (OutOfMemoryError oom) {
            error("Bild einfügen", "Das Bild ist zu groß für den verfügbaren Android-Speicher. Es wurde nicht eingefügt, damit die Notizen-Datei nicht abstürzt.");
        } catch (Exception e) {
            error("Bild einfügen", e.getMessage());
        }
    }

    private boolean insertPreparedImageIntoEditor(PreparedImage image, String title) {
        String errorTitle = title == null || title.isEmpty() ? "Bild einfügen" : title;
        if (currentNode == null || editor == null || image == null || image.bytes.length == 0) return false;
        String rawPicture = RtfUtils.rtfPictureFromImage(image.bytes, image.mimeType);
        if (rawPicture == null || rawPicture.isEmpty()) {
            error(errorTitle, "Dieses Bildformat kann nicht als ALX/RTF-Bild gespeichert werden. Unterstützt sind PNG, JPEG und BMP. Große oder fremde Formate werden vorher verkleinert und als JPEG gespeichert.");
            return false;
        }
        Drawable drawable = imageDrawable(image.bytes, 0, 0);
        if (drawable == null) {
            error(errorTitle, "Das Bild konnte auch nach Speicher-Schutz nicht dekodiert werden.");
            return false;
        }
        Editable editable = editor.getText();
        Spannable text = editable;
        pushEditorUndoSnapshot("image");
        int start = Math.max(0, Math.min(editor.getSelectionStart(), text.length()));
        int end = Math.max(0, Math.min(editor.getSelectionEnd(), text.length()));
        if (end < start) { int tmp = start; start = end; end = tmp; }
        editable.replace(start, end, RTF_IMAGE_CHAR);
        text.setSpan(new RtfImageSpan(drawable, rawPicture, image.mimeType, image.bytes, 0, 0), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        editor.setSelection(start + 1);
        markEditorRichChanged(image.note.isEmpty() ? "Bild eingefügt und sichtbar eingebettet" : image.note);
        return true;
    }



    private PreparedImage prepareImageForRtf(Uri uri) throws Exception {
        String mime = getContentResolver().getType(uri);
        BitmapFactory.Options bounds = decodeImageBounds(uri);
        boolean hasBounds = bounds != null && bounds.outWidth > 0 && bounds.outHeight > 0;
        long sourceBytes = queryOpenableSize(uri);
        boolean supportedMime = isRtfSupportedImageMime(mime);
        int configuredLongEdge = settings == null ? MAX_EMBED_IMAGE_LONG_EDGE_PX : LegacySettings.normalizeAndroidLargeImageLongEdgePx(settings.androidLargeImageLongEdgePx);
        boolean autoDownsample = settings == null || settings.androidAutoDownsampleLargeImages;
        boolean tooLargeByBytes = sourceBytes > MAX_EMBEDDED_IMAGE_BYTES;
        boolean tooLargeByPixels = hasBounds && LegacyTouchZoomModel.safeImageLongEdge(bounds.outWidth, bounds.outHeight) > configuredLongEdge;
        if (hasBounds && (!supportedMime || tooLargeByBytes || (autoDownsample && tooLargeByPixels))) {
            return decodeCompressImageForRtf(uri, bounds, "Großes Bild speicherschonend verkleinert und eingefügt");
        }

        byte[] raw = readAllLimited(uri, MAX_EMBEDDED_IMAGE_BYTES + 1);
        if (raw.length <= MAX_EMBEDDED_IMAGE_BYTES) {
            String rawPicture = RtfUtils.rtfPictureFromImage(raw, mime);
            if (rawPicture != null && !rawPicture.isEmpty()) {
                return new PreparedImage(raw, mime, "Bild eingefügt und sichtbar eingebettet");
            }
        }
        if (hasBounds) {
            return decodeCompressImageForRtf(uri, bounds, "Bildformat/Größe für RTF angepasst und eingefügt");
        }
        throw new IllegalStateException("Das Bild ist zu groß oder kein dekodierbares Android-Bild. Bitte PNG, JPEG oder BMP verwenden.");
    }

    private BitmapFactory.Options decodeImageBounds(Uri uri) {
        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(uri);
            if (in == null) return null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            return options;
        } catch (Exception ignored) {
            return null;
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
        }
    }

    private PreparedImage decodeCompressImageForRtf(Uri uri, BitmapFactory.Options bounds, String note) throws Exception {
        Bitmap bitmap = decodeScaledBitmapFromUri(uri, bounds, MAX_EMBED_IMAGE_LONG_EDGE_PX);
        if (bitmap == null) throw new IllegalStateException("Das Bild konnte nicht speicherschonend dekodiert werden.");
        try {
            return compressBitmapForRtf(bitmap, note);
        } finally {
            try { if (!bitmap.isRecycled()) bitmap.recycle(); } catch (Exception ignored) {}
        }
    }

    private Bitmap decodeScaledBitmapFromUri(Uri uri, BitmapFactory.Options bounds, int maxLongEdge) throws Exception {
        int width = bounds == null ? 0 : bounds.outWidth;
        int height = bounds == null ? 0 : bounds.outHeight;
        int configuredLongEdge = settings == null ? maxLongEdge : LegacySettings.normalizeAndroidLargeImageLongEdgePx(settings.androidLargeImageLongEdgePx);
        int sample = calculateImageSampleSize(width, height, configuredLongEdge);
        for (int attempt = 0; attempt < 5; attempt++) {
            InputStream in = null;
            try {
                in = getContentResolver().openInputStream(uri);
                if (in == null) return null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = Math.max(1, sample);
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeStream(in, null, options);
            } catch (OutOfMemoryError oom) {
                sample = Math.max(sample + 1, sample * 2);
            } finally {
                try { if (in != null) in.close(); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private PreparedImage compressBitmapForRtf(Bitmap bitmap, String note) throws Exception {
        if (bitmap == null || bitmap.isRecycled()) throw new IllegalStateException("Das Bild konnte nicht verarbeitet werden.");
        Bitmap current = bitmap;
        int quality = IMAGE_JPEG_QUALITY;
        byte[] bytes = compressJpeg(current, quality);
        int safety = 0;
        while ((bytes.length == 0 || bytes.length > MAX_EMBEDDED_IMAGE_BYTES) && safety++ < 18) {
            if (bytes.length > MAX_EMBEDDED_IMAGE_BYTES && quality > 52) {
                quality -= 8;
                bytes = compressJpeg(current, quality);
                continue;
            }
            if (Math.max(current.getWidth(), current.getHeight()) <= 360) break;
            int nextWidth = Math.max(1, Math.round(current.getWidth() * 0.72f));
            int nextHeight = Math.max(1, Math.round(current.getHeight() * 0.72f));
            Bitmap scaled = null;
            try {
                scaled = Bitmap.createScaledBitmap(current, nextWidth, nextHeight, true);
            } catch (OutOfMemoryError oom) {
                try { System.gc(); } catch (Throwable ignored) {}
            } catch (RuntimeException ex) {
                break;
            }
            if (scaled == null || scaled.isRecycled()) break;
            if (current != bitmap) {
                try { current.recycle(); } catch (Exception ignored) {}
            }
            current = scaled;
            quality = IMAGE_JPEG_QUALITY;
            bytes = compressJpeg(current, quality);
        }
        if (current != bitmap) {
            try { current.recycle(); } catch (Exception ignored) {}
        }
        if (bytes.length == 0 || bytes.length > MAX_EMBEDDED_IMAGE_BYTES) {
            throw new IllegalStateException("Das Bild bleibt trotz Verkleinerung zu groß für ein stabiles RTF-Einbetten.");
        }
        return new PreparedImage(bytes, "image/jpeg", note);
    }

    private byte[] compressJpeg(Bitmap bitmap, int quality) {
        if (bitmap == null || bitmap.isRecycled()) return new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(MAX_EMBEDDED_IMAGE_BYTES + 1024, Math.max(8192, bitmap.getWidth() * bitmap.getHeight() / 8)));
        try {
            boolean ok = bitmap.compress(Bitmap.CompressFormat.JPEG, Math.max(45, Math.min(95, quality)), out);
            return ok ? out.toByteArray() : new byte[0];
        } catch (OutOfMemoryError oom) {
            try { System.gc(); } catch (Throwable ignored) {}
            return new byte[0];
        } catch (RuntimeException ex) {
            return new byte[0];
        }
    }

    private int calculateImageSampleSize(int width, int height, int maxLongEdge) {
        int sample = 1;
        int max = Math.max(width, height);
        int target = Math.max(320, maxLongEdge);
        while (max / sample > target && sample < 64) sample *= 2;
        return Math.max(1, sample);
    }

    private boolean isRtfSupportedImageMime(String mime) {
        String m = mime == null ? "" : mime.toLowerCase(Locale.ROOT);
        return m.equals("image/png") || m.equals("image/jpeg") || m.equals("image/jpg") || m.equals("image/bmp") || m.equals("image/x-ms-bmp");
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
                    showPrintPreview(job);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showPrintPreview(LegacyPrintLayout.PrintJob job) {
        if (job == null) return;
        WebView web = new WebView(this);
        String html = job.html == null || job.html.isEmpty() ? RtfUtils.rtfToHtmlDocument("") : job.html;
        web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        new AlertDialog.Builder(this)
                .setTitle("Druck-/PDF-Vorschau: " + job.title)
                .setView(web)
                .setPositiveButton("Drucken/PDF", (d, which) -> startPrintJob(job))
                .setNeutralButton("HTML teilen", (d, which) -> shareExportPayload(new LegacyExportShareModel.Payload(
                        LegacyExportShareModel.Scope.CURRENT_NOTE, LegacyExportShareModel.Format.HTML, job.title,
                        LegacyPrintLayout.safeFileName(job.title, ".html"), "text/html", html.getBytes(StandardCharsets.UTF_8),
                        RtfUtils.rtfToPlainText(html), "Druck-HTML geteilt")))
                .setNegativeButton("Schließen", null)
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
            pushTreeUndoSnapshot("HTML als Unterknoten importieren");
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
        pushTreeUndoSnapshot(background ? "Baum-Hintergrundfarbe ändern" : "Baum-Textfarbe ändern");
        if (background) currentNode.bgArgb = argb;
        else currentNode.fgArgb = argb;
        markDocumentChanged();
        rebuildTree();
        updateTitle();
        status(background ? "Hintergrund gesetzt" : "Textfarbe gesetzt");
    }

    private void requestAndroidWidgetForCurrentNode() {
        if (currentNode == null) return;
        saveCurrentEditorToNode();
        float textSp = settings == null ? 13f : LegacySettings.normalizeAndroidWidgetTextSp(settings.androidWidgetTextSp);
        LegacyAndroidWidgetRegistry.WidgetSpec spec = LegacyAndroidWidgetRegistry.fromNode(0, document, currentNode, currentDisplayName, textSp);
        NoteWidgetProvider.requestPinOrUpdate(this, spec.nodePath, spec.documentName, spec.title, spec.text, spec.bgArgb, spec.fgArgb, spec.textSizeSp, spec.images, spec.originalImageCount);
    }

    private void updateAndroidWidgetsFromDocument() {
        try {
            if (settings == null) settings = AndroidSettingsStore.load(this);
            NoteWidgetProvider.updateExistingWidgetsFromDocument(this, document, currentDisplayName, LegacySettings.normalizeAndroidWidgetTextSp(settings.androidWidgetTextSp));
        } catch (Exception ignored) {
            // Widgets must never interrupt normal ALX saving/editing.
        }
    }

    private void showAndroidWidgetList() {
        updateAndroidWidgetsFromDocument();
        new AlertDialog.Builder(this)
                .setTitle("Android-Haftnotiz-Widgets")
                .setMessage(NoteWidgetProvider.widgetListSummary(this))
                .setPositiveButton("OK", null)
                .show();
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
                    pushTreeUndoSnapshot("Haftnotiz-Metadaten entfernen");
                    currentNode.desktopNote = null;
                    markDocumentChanged();
                    updateTitle();
                    updateAndroidWidgetsFromDocument();
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
                    pushTreeUndoSnapshot("Haftnotiz-Metadaten ändern");
                    currentNode.desktopNote = state;
                    markDocumentChanged();
                    updateTitle();
                    updateAndroidWidgetsFromDocument();
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
                    pushTreeUndoSnapshot("Wecker löschen");
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
                    pushTreeUndoSnapshot("Wecker ändern");
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
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try { BitmapFactory.decodeByteArray(data, 0, data.length, bounds); } catch (Throwable ignored) {}
        int sourceWidth = bounds.outWidth > 0 ? bounds.outWidth : 0;
        int sourceHeight = bounds.outHeight > 0 ? bounds.outHeight : 0;
        int sample = calculateImageSampleSize(sourceWidth, sourceHeight, MAX_IMAGE_DISPLAY_LONG_EDGE_PX);
        Bitmap bitmap = null;
        for (int attempt = 0; attempt < 5 && bitmap == null; attempt++) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = Math.max(1, sample);
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            } catch (OutOfMemoryError oom) {
                sample = Math.max(sample + 1, sample * 2);
                try { System.gc(); } catch (Throwable ignored) {}
            } catch (RuntimeException ex) {
                return null;
            }
        }
        if (bitmap == null) return null;
        if (sourceWidth <= 0) sourceWidth = bitmap.getWidth() * Math.max(1, sample);
        if (sourceHeight <= 0) sourceHeight = bitmap.getHeight() * Math.max(1, sample);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = widthTwips > 0 ? Math.round((widthTwips / 15.0f) * dm.density) : sourceWidth;
        int height = heightTwips > 0 ? Math.round((heightTwips / 15.0f) * dm.density) : sourceHeight;
        if (width <= 0) width = bitmap.getWidth();
        if (height <= 0) height = bitmap.getHeight();
        int maxWidth = Math.max(dp(120), dm.widthPixels - dp(48));
        int maxHeight = Math.max(dp(180), dm.heightPixels * 2);
        float scale = 1f;
        if (width > maxWidth && width > 0) scale = Math.min(scale, maxWidth / (float) width);
        if (height > maxHeight && height > 0) scale = Math.min(scale, maxHeight / (float) height);
        if (scale < 1f) {
            width = Math.max(1, Math.round(width * scale));
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
            String legacyAlign = style.align;
            if ("center".equals(style.align)) alignment = Layout.Alignment.ALIGN_CENTER;
            else if ("right".equals(style.align)) alignment = Layout.Alignment.ALIGN_OPPOSITE;
            else if ("justify".equals(style.align)) alignment = Layout.Alignment.ALIGN_NORMAL;
            int[] para = paragraphRange(spannable.toString(), start, end);
            if (para[1] > para[0]) spannable.setSpan(new RtfParagraphAlignmentSpan(alignment, legacyAlign), para[0], para[1], Spanned.SPAN_PARAGRAPH);
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
        AlignmentSpan[] aligns = spanned.getSpans(pos, end, AlignmentSpan.class);
        if (aligns.length > 0) {
            AlignmentSpan last = aligns[aligns.length - 1];
            if (last instanceof RtfParagraphAlignmentSpan && "justify".equals(((RtfParagraphAlignmentSpan) last).legacyAlign)) style.align = "justify";
            else {
                Layout.Alignment a = last.getAlignment();
                if (a == Layout.Alignment.ALIGN_CENTER) style.align = "center";
                else if (a == Layout.Alignment.ALIGN_OPPOSITE) style.align = "right";
                else style.align = null;
            }
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
        if (markdownPreviewActive) clearMarkdownPreviewState(false);
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
        editorUndoStack.clear();
        editorRedoStack.clear();
        lastEditorTypingUndoAt = 0L;
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
        scheduleFormatToolbarStateUpdate();
    }

    private void saveCurrentEditorToNode() {
        if (currentNode == null) return;
        // LegacyEditorNodeSync.applyToNode bleibt das historische Plaintext-Modell; der native Android-Editor speichert hier zusätzlich Spans und Bilder als RTF.
        String oldTitle = LegacyEditorNodeSync.normalizeTitle(currentNode.title);
        String oldRtf = currentNode.rtf == null ? "" : currentNode.rtf;
        String nextTitle = titleDirty ? LegacyEditorNodeSync.normalizeTitle(titleEdit == null ? currentNode.title : titleEdit.getText().toString()) : oldTitle;
        String nextRtf = oldRtf;
        if (markdownPreviewActive && markdownPreviewNode == currentNode) {
            // The editor currently contains rendered Markdown preview text, not the
            // editable note body.  Never serialize that preview back to RTF.
            editorDirty = false;
        } else if (editorDirty) {
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
        treeAdapter.setTreeActive(lastActivePane == ActivePane.TREE_NODE);
        rootTitleView.setText(document.ensureRoot().title == null ? "start" : document.ensureRoot().title);
        updateActivePaneChrome();
    }

    private void finishLoadedDocument(String message) {
        runtimeSnapshotRestored = false;
        clearTreeHistory();
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

    private void showExportHubDialog() {
        if (document == null) return;
        saveCurrentEditorToNode();
        String[] labels = LegacyExportShareModel.hubLabels();
        new AlertDialog.Builder(this)
                .setTitle("Export / Teilen / Drucken")
                .setItems(labels, (d, which) -> {
                    if (which == 6) { showPrintDialog(); return; }
                    LegacyExportShareModel.Scope scope = LegacyExportShareModel.scopeForHubIndex(which);
                    boolean share = LegacyExportShareModel.hubIndexShares(which);
                    showExportFormatDialog(scope, share);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void showExportFormatDialog(LegacyExportShareModel.Scope scope, boolean share) {
        String title = (share ? "Teilen: " : "Speichern: ") + LegacyExportShareModel.scopeLabel(scope);
        String[] labels = LegacyExportShareModel.formatLabels();
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, (d, which) -> {
                    LegacyExportShareModel.Payload payload = LegacyExportShareModel.build(document, currentNode, scope, LegacyExportShareModel.formatForIndex(which));
                    if (share) shareExportPayload(payload);
                    else saveGenericExportPayload(payload);
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private void saveGenericExportPayload(LegacyExportShareModel.Payload payload) {
        if (payload == null) return;
        pendingGenericExportBytes = payload.bytes;
        pendingGenericExportMime = payload.mimeType;
        pendingGenericExportFileName = payload.fileName;
        pendingGenericExportStatus = payload.status;
        pendingGenericExportShareText = payload.textForShare;
        String ext = payload.fileName != null && payload.fileName.toLowerCase(Locale.ROOT).endsWith(".rtf") ? ".rtf" :
                (payload.fileName != null && payload.fileName.toLowerCase(Locale.ROOT).endsWith(".html") ? ".html" : ".txt");
        LegacyFileDialogModel.Kind kind = ".rtf".equals(ext) ? LegacyFileDialogModel.Kind.EXPORT_RTF :
                (".html".equals(ext) ? LegacyFileDialogModel.Kind.EXPORT_HTML : LegacyFileDialogModel.Kind.EXPORT_TXT_UTF8);
        String filter = ".rtf".equals(ext) ? "rtf Files|*.rtf" : (".html".equals(ext) ? "html Files|*.html" : "txt Files|*.txt");
        LegacyFileDialogModel.Spec spec = new LegacyFileDialogModel.Spec(kind, "Export speichern", filter, ext, payload.fileName,
                currentDialogDirectory(), payload.mimeType, new String[]{payload.mimeType}, false, true);
        startActivityForResult(intentForDialog(spec), REQ_EXPORT_GENERIC);
    }

    private void shareExportPayload(LegacyExportShareModel.Payload payload) {
        if (payload == null) return;
        try {
            Uri uri = ExportFileProvider.writeExport(this, payload.fileName, payload.bytes);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType(payload.mimeType);
            share.putExtra(Intent.EXTRA_SUBJECT, payload.fileName);
            if (payload.textForShare != null && !payload.textForShare.isEmpty()) share.putExtra(Intent.EXTRA_TEXT, payload.textForShare);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Notizen exportieren/teilen"));
            status("Teilen vorbereitet: " + payload.fileName);
        } catch (Exception e) {
            error("Teilen fehlgeschlagen", e.getMessage());
        }
    }

    private void askShareAfterGenericExport() {
        final byte[] bytes = pendingGenericExportBytes == null ? new byte[0] : pendingGenericExportBytes;
        final String fileName = pendingGenericExportFileName == null ? "notizen-export.txt" : pendingGenericExportFileName;
        final String mime = pendingGenericExportMime == null ? "text/plain" : pendingGenericExportMime;
        final String shareText = pendingGenericExportShareText == null ? "" : pendingGenericExportShareText;
        new AlertDialog.Builder(this)
                .setTitle("Export gespeichert")
                .setMessage((pendingGenericExportStatus == null ? "Export gespeichert" : pendingGenericExportStatus) + "\n\nJetzt über Android teilen?")
                .setPositiveButton("Teilen", (d, which) -> shareExportPayload(new LegacyExportShareModel.Payload(
                        LegacyExportShareModel.Scope.CURRENT_NOTE, LegacyExportShareModel.Format.TXT_UTF8, fileName, fileName, mime, bytes, shareText, "Export geteilt")))
                .setNegativeButton("OK", null)
                .show();
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
        pushTreeUndoSnapshot(scope == LegacyUnifiedNote.Scope.CURRENT_SUBTREE ? "Teilbaum zusammenfassen" : "Gesamten Baum zusammenfassen");
        LegacyUnifiedNote.UnifiedResult result = LegacyUnifiedNote.attach(document, currentNode, scope);
        rebuildTree();
        selectNode(result.created == null ? currentNode : result.created, true);
        status((scope == LegacyUnifiedNote.Scope.CURRENT_SUBTREE ? "Teilbaum" : "Gesamter Baum") + " zusammengefasst (" + result.sourceNodes + " Knoten)");
    }

    private void showSearchDialog() {
        showQuickSearchBar(true);
    }

    private void showQuickSearchBar(boolean focus) {
        if (quickSearchBar == null) return;
        quickSearchBar.setVisibility(View.VISIBLE);
        if (quickSearchInput != null) {
            if (quickSearchInput.getText().length() == 0 && !searchSession.cachedTerm().isEmpty()) quickSearchInput.setText(searchSession.cachedTerm());
            if (focus) {
                quickSearchInput.requestFocus();
                quickSearchInput.setSelection(quickSearchInput.getText().length());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.showSoftInput(quickSearchInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }
        updateQuickSearchStatus("Suchleiste bereit");
    }

    private void hideQuickSearchBar() {
        if (quickSearchBar != null) quickSearchBar.setVisibility(View.GONE);
        quickSearchResults.clear();
        quickSearchIndex = -1;
    }

    private LegacyQuickSearchBar.Options currentQuickSearchOptions() {
        String term = quickSearchInput == null ? searchSession.cachedTerm() : quickSearchInput.getText().toString();
        return LegacyQuickSearchBar.normalize(
                term,
                quickSearchWholeTree != null && quickSearchWholeTree.isChecked(),
                quickSearchWholeWords != null && quickSearchWholeWords.isChecked(),
                quickSearchCaseSensitive != null && quickSearchCaseSensitive.isChecked(),
                quickSearchIncludeTitles == null || quickSearchIncludeTitles.isChecked());
    }

    private void quickSearchNext() {
        LegacyQuickSearchBar.Options options = currentQuickSearchOptions();
        if (!options.accepted) { updateQuickSearchStatus(options.message); toast(options.message); return; }
        saveCurrentEditorToNode();
        quickSearchScope = options.scope;
        LegacySearchSession.SearchStep step = searchSession.begin(document.ensureRoot(), currentNode, options.term,
                options.scope == LegacyQuickSearchBar.Scope.WHOLE_TREE, options.wholeWords, options.caseSensitive, options.includeTitles,
                settings == null ? "Deutsch" : settings.language);
        quickSearchResults.clear();
        quickSearchResults.addAll(step.results);
        quickSearchIndex = step.selectedIndexOneBased - 1;
        if (step.selected == null) {
            updateQuickSearchStatus("Keine Treffer im " + LegacyQuickSearchBar.scopeLabel(options.scope));
            toast("Keine Treffer");
            return;
        }
        navigateToSearchResult(step.selected, quickSearchIndex, quickSearchResults.size(), options.scope);
    }

    private void quickSearchAllResults() {
        LegacyQuickSearchBar.Options options = currentQuickSearchOptions();
        if (!options.accepted) { updateQuickSearchStatus(options.message); toast(options.message); return; }
        saveCurrentEditorToNode();
        quickSearchScope = options.scope;
        LegacySearchSession.SearchStep step = searchSession.begin(document.ensureRoot(), currentNode, options.term,
                options.scope == LegacyQuickSearchBar.Scope.WHOLE_TREE, options.wholeWords, options.caseSensitive, options.includeTitles,
                settings == null ? "Deutsch" : settings.language);
        quickSearchResults.clear();
        quickSearchResults.addAll(step.results);
        quickSearchIndex = step.selectedIndexOneBased - 1;
        if (quickSearchResults.isEmpty()) {
            updateQuickSearchStatus("Keine Treffer im " + LegacyQuickSearchBar.scopeLabel(options.scope));
            toast("Keine Treffer");
            return;
        }
        int count = Math.min(quickSearchResults.size(), MAX_QUICK_SEARCH_RESULTS);
        String[] labels = new String[count];
        for (int i = 0; i < count; i++) labels[i] = quickSearchResults.get(i).label();
        String title = quickSearchResults.size() > count ? "Treffer: " + quickSearchResults.size() + " (erste " + count + ")" : "Treffer: " + quickSearchResults.size();
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(labels, (d, which) -> {
                    quickSearchIndex = which;
                    navigateToSearchResult(quickSearchResults.get(which), which, quickSearchResults.size(), options.scope);
                })
                .setPositiveButton("OK", null)
                .show();
        updateQuickSearchStatus(LegacyQuickSearchBar.status(Math.max(1, quickSearchIndex + 1), quickSearchResults.size(), options.scope));
    }

    private void navigateToSearchResult(SearchResult r, int indexZeroBased, int total, LegacyQuickSearchBar.Scope scope) {
        if (r == null || r.node == null) return;
        ensureAncestorsExpanded(r.node);
        rebuildTree();
        selectNode(r.node, true);
        if (r.titleMatch) {
            if (titleEdit != null) {
                titleEdit.requestFocus();
                int start = Math.max(0, Math.min(r.start, titleEdit.getText().length()));
                int end = Math.max(start, Math.min(start + r.length, titleEdit.getText().length()));
                titleEdit.setSelection(start, end);
                setActivePane(ActivePane.TITLE_TEXT);
            }
        } else if (editor != null) {
            editor.requestFocus();
            int start = Math.max(0, Math.min(r.start, editor.getText().length()));
            int end = Math.max(start, Math.min(start + r.length, editor.getText().length()));
            editor.setSelection(start, end);
            setActivePane(ActivePane.RTF_EDITOR);
        }
        updateQuickSearchStatus(LegacyQuickSearchBar.status(indexZeroBased + 1, total, scope));
    }

    private void updateQuickSearchStatus(String text) {
        if (quickSearchStatus != null) quickSearchStatus.setText(text == null ? "" : text);
    }

    private void showSearchDialogLegacy() {
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
        clearTreeHistory();
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
                "v103 korrigiert: der erste Leer/Neu-Start leert den Android-Baum wirklich, baut den Adapter neu auf und setzt die RTF-Box ohne alte Knotenreste zurück; Speichern-vorher läuft nach SAF-Speichern automatisch weiter.\n\n" +
                "v106 stabilisiert den Zwei-Finger-Zoom der drei oberen Symbolleisten: die gesamte Toolbar-Fläche ist ein gemeinsames Zoomziel, Kind-Buttons bekommen keine eigenen konkurrierenden Touch-Listener mehr, Layoutänderungen werden gebündelt und Config-Speichern wird verzögert.\n\n" +
                "v104 ergänzt: Zwei-Finger-Zoom für RTF-Auswahl/ganzen RTF-Text, selektierten Baumtext, obere quadratische Symbolbuttons und die beiden hellgelben Überschriftfelder; große Bilder werden beim Einfügen und Anzeigen speicherschonend verkleinert/dekodiert.\n\n" +
                "Bewusst mobil angepasst: Android nutzt keinen Windows-Tray und keine frei schwebenden Desktop-Haftnotiz-Fenster. Diese Metadaten bleiben im ALX erhalten und können mobil editiert werden. RTF wird als lesbarer Text angezeigt; vorhandenes RTF bleibt erhalten, solange die Notiz nicht bearbeitet wird.\n\n" +
                "Lizenz: GPLv3 wie die Ausgangsarchive.";
        new AlertDialog.Builder(this).setTitle(about.title + " " + about.version).setMessage(msg).setPositiveButton(about.closeLabel, null).show();
    }

    private void confirmDiscardThen(ContinueCallback callback) {
        saveCurrentEditorToNode();
        if (!LegacyDialogModels.shouldShowWannaSave(document.changed) || isBlankSingleNodeDocumentForPrompt(document)) {
            safeAction("Aktion fortsetzen", callback).run();
            return;
        }
        LegacyDialogModels.DialogSpec spec = LegacyDialogModels.wannaSave(settings == null ? "Deutsch" : settings.language);
        new AlertDialog.Builder(this)
                .setTitle(spec.title)
                .setMessage(spec.message)
                .setPositiveButton(spec.positive, (d, which) -> saveThenMaybeContinue(callback))
                .setNeutralButton(spec.neutral, (d, which) -> safeAction("Aktion ohne Speichern", callback).run())
                .setNegativeButton(spec.negative, null)
                .show();
    }

    private void saveThenMaybeContinue(ContinueCallback callback) {
        saveCurrentEditorToNode();
        if (!document.changed) {
            safeAction("Aktion nach Speichern", callback).run();
            return;
        }
        if (currentUri != null) {
            writeDocumentToUri(currentUri, currentDisplayName, false);
            if (!document.changed) safeAction("Aktion nach Speichern", callback).run();
            return;
        }
        if (currentRawFile != null) {
            writeDocumentToFile(currentRawFile, false);
            if (!document.changed) safeAction("Aktion nach Speichern", callback).run();
            return;
        }
        if (currentFtpTarget != null) {
            saveDocumentToFtp(currentFtpTarget, callback);
            status("FTP-Speichern gestartet; Aktion läuft danach weiter.");
            return;
        }
        saveDocumentAs(callback);
        status("Bitte speichern; die Aktion läuft danach automatisch weiter.");
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
            case KeyEvent.KEYCODE_Z: return "Z";
            case KeyEvent.KEYCODE_Y: return "Y";
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
            case "undo": undoForActiveTarget(); return true;
            case "redo": redoForActiveTarget(); return true;
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
            try { copy = readAll(uri); } catch (Throwable readError) { handleRecoverableUiFailure("Öffnen fehlgeschlagen", readError); return; }
            final byte[] data = copy;
            showPasswordDialog("", p -> openBytesAfterPassword(uri, data, p));
        } catch (Throwable e) {
            handleRecoverableUiFailure("Öffnen fehlgeschlagen", e);
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
        } catch (Throwable e) {
            handleRecoverableUiFailure("Öffnen fehlgeschlagen", e);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            if (requestCode == REQ_SAVE_AS && pendingAfterSaveAsCallback != null) {
                pendingAfterSaveAsCallback = null;
                status("Speichern abgebrochen; Aktion abgebrochen");
            }
            return;
        }
        Uri uri = data.getData();
        if (requestCode == REQ_OPEN) {
            openUri(uri, null);
        } else if (requestCode == REQ_SAVE_AS) {
            ContinueCallback afterSave = pendingAfterSaveAsCallback;
            pendingAfterSaveAsCallback = null;
            takeReadWritePermission(uri);
            writeDocumentToUri(uri, queryDisplayName(uri), true);
            if (afterSave != null && document != null && !document.changed) safeAction("Aktion nach Speichern", afterSave).run();
        } else if (requestCode == REQ_EXPORT_TEXT || requestCode == REQ_EXPORT_TEXT_ANSI || requestCode == REQ_EXPORT_TEXT_UNICODE) {
            try {
                writeAll(uri, pendingExportTextBytes == null ? (pendingExportText == null ? new byte[0] : pendingExportText.getBytes(java.nio.charset.StandardCharsets.UTF_8)) : pendingExportTextBytes);
                if (requestCode == REQ_EXPORT_TEXT_ANSI) status(LegacyTextExportModel.status(LegacyTextExportModel.Mode.ANSI));
                else if (requestCode == REQ_EXPORT_TEXT_UNICODE) status(LegacyTextExportModel.status(LegacyTextExportModel.Mode.UNICODE));
                else status(LegacyTextExportModel.status(LegacyTextExportModel.Mode.UTF8));
            } catch (Throwable e) { handleRecoverableUiFailure("Export fehlgeschlagen", e); }
        } else if (requestCode == REQ_EXPORT_HTML) {
            try {
                writeAll(uri, (pendingExportHtml == null ? "" : pendingExportHtml).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                status("HTML exportiert");
            } catch (Throwable e) { handleRecoverableUiFailure("Export fehlgeschlagen", e); }
        } else if (requestCode == REQ_EXPORT_RTF) {
            try {
                writeAll(uri, (pendingExportRtf == null ? "" : pendingExportRtf).getBytes(java.nio.charset.Charset.forName("windows-1252")));
                status("RTF exportiert");
            } catch (Throwable e) { handleRecoverableUiFailure("Export fehlgeschlagen", e); }
        } else if (requestCode == REQ_EXPORT_NODE_TEXT) {
            try {
                writeAll(uri, pendingExportTextBytes == null ? (pendingExportText == null ? new byte[0] : pendingExportText.getBytes(java.nio.charset.StandardCharsets.UTF_8)) : pendingExportTextBytes);
                status("Knoten-TXT exportiert");
            } catch (Throwable e) { handleRecoverableUiFailure("Knoten-Export fehlgeschlagen", e); }
        } else if (requestCode == REQ_EXPORT_NODE_RTF) {
            try {
                writeAll(uri, (pendingExportRtf == null ? "" : pendingExportRtf).getBytes(java.nio.charset.Charset.forName("windows-1252")));
                status("Knoten-RTF exportiert");
            } catch (Throwable e) { handleRecoverableUiFailure("Knoten-Export fehlgeschlagen", e); }
        } else if (requestCode == REQ_EXPORT_GENERIC) {
            try {
                writeAll(uri, pendingGenericExportBytes == null ? new byte[0] : pendingGenericExportBytes);
                status(pendingGenericExportStatus == null ? "Export gespeichert" : pendingGenericExportStatus);
                askShareAfterGenericExport();
            } catch (Throwable e) { handleRecoverableUiFailure("Export fehlgeschlagen", e); }
        } else if (requestCode == REQ_IMPORT_CONFIG) {
            try {
                applyImportedSettings(readAll(uri));
            } catch (Throwable e) { handleRecoverableUiFailure("Config-Import", e); }
        } else if (requestCode == REQ_INSERT_IMAGE) {
            insertImageFromUri(uri);
        } else if (requestCode == REQ_EXPORT_CONFIG) {
            try {
                writeAll(uri, pendingExportConfig == null ? new byte[0] : pendingExportConfig);
                status("Config exportiert");
            } catch (Throwable e) { handleRecoverableUiFailure("Config-Export", e); }
        } else if (requestCode == REQ_IMPORT_HTML) {
            importHtmlNoteFromUri(uri);
        } else if (requestCode == REQ_IMPORT_TEXT) {
            importTextFromUri(uri);
        } else if (requestCode == REQ_IMPORT_RTF) {
            importRtfFromUri(uri);
        }
    }

    private byte[] readAll(Uri uri) throws Exception {
        long size = queryOpenableSize(uri);
        if (size > MAX_READ_ALL_BYTES) throw new IllegalStateException("Datei ist zu groß für sicheres Laden auf Android (" + (size / (1024 * 1024)) + " MB).");
        return readAllChecked(uri, MAX_READ_ALL_BYTES);
    }

    private byte[] readAllChecked(Uri uri, int maxBytes) throws Exception {
        ContentResolver resolver = getContentResolver();
        InputStream in = resolver.openInputStream(uri);
        if (in == null) throw new IllegalStateException("Datei kann nicht gelesen werden.");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            int total = 0;
            while ((n = in.read(buf)) != -1) {
                if (total > maxBytes - n) throw new IllegalStateException("Datei ist zu groß für sicheres Laden auf Android.");
                out.write(buf, 0, n);
                total += n;
            }
            return out.toByteArray();
        } finally {
            try { in.close(); } catch (Exception ignored) {}
        }
    }

    private byte[] readAllLimited(Uri uri, int limit) throws Exception {
        ContentResolver resolver = getContentResolver();
        InputStream in = resolver.openInputStream(uri);
        if (in == null) throw new IllegalStateException("Datei kann nicht gelesen werden.");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            int total = 0;
            int cap = Math.max(1, limit);
            while ((n = in.read(buf)) != -1) {
                int allowed = Math.min(n, cap - total);
                if (allowed > 0) out.write(buf, 0, allowed);
                total += n;
                if (total >= cap) break;
            }
            return out.toByteArray();
        } finally {
            try { in.close(); } catch (Exception ignored) {}
        }
    }

    private long queryOpenableSize(Uri uri) {
        if (uri == null) return -1L;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (idx >= 0 && !cursor.isNull(idx)) return cursor.getLong(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return -1L;
    }

    private void writeAll(Uri uri, byte[] bytes) throws Exception {
        OutputStream out = getContentResolver().openOutputStream(uri, "wt");
        if (out == null) throw new IllegalStateException("Datei kann nicht geschrieben werden.");
        try {
            out.write(bytes == null ? new byte[0] : bytes);
        } finally {
            try { out.close(); } catch (Exception ignored) {}
        }
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            if (mainHandler == null) mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> error(title, message));
            return;
        }
        String safeTitle = title == null || title.trim().isEmpty() ? "Fehler" : title;
        String safeMessage = message == null || message.trim().isEmpty() ? "Unbekannter Fehler." : message;
        try {
            if (isFinishing() || (Build.VERSION.SDK_INT >= 17 && isDestroyed())) {
                Toast.makeText(this, safeTitle + ": " + safeMessage, Toast.LENGTH_LONG).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(safeTitle)
                    .setMessage(safeMessage)
                    .setPositiveButton("OK", null)
                    .show();
        } catch (Throwable ignored) {
            try { Toast.makeText(this, safeTitle + ": " + safeMessage, Toast.LENGTH_LONG).show(); } catch (Throwable ignoredToo) {}
        }
    }

    private float dpf(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
