package de.notizen.android;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.notizen.android.core.LegacyAndroidWidgetRegistry;
import de.notizen.android.core.NoteDocument;

/** Native Android home-screen widget for selected Notizen nodes. */
public final class NoteWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_OPEN_WIDGET_NODE = "de.notizen.android.OPEN_WIDGET_NODE";
    public static final String EXTRA_NODE_PATH = "de.notizen.android.extra.NODE_PATH";
    public static final String EXTRA_DISPLAY_NAME = "de.notizen.android.extra.DISPLAY_NAME";

    private static final String PREFS = "notizen_java_android_widgets";
    private static final String PENDING_TITLE = "pending_title";
    private static final String PENDING_TEXT = "pending_text";
    private static final String PENDING_BG = "pending_bg";
    private static final String PENDING_FG = "pending_fg";
    private static final String PENDING_PATH = "pending_path";
    private static final String PENDING_DISPLAY = "pending_display";
    private static final String PENDING_TEXT_SP = "pending_text_sp";

    public static void requestPinOrUpdate(Activity activity, String title, String text, int bgColor, int fgColor) {
        requestPinOrUpdate(activity, "", "", title, text, bgColor, fgColor, 13f);
    }

    public static void requestPinOrUpdate(Activity activity, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp) {
        if (activity == null) return;
        Context context = activity.getApplicationContext();
        savePending(context, nodePath, displayName, title, text, bgColor, fgColor, textSizeSp);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, NoteWidgetProvider.class);
        if (Build.VERSION.SDK_INT >= 26 && manager.isRequestPinAppWidgetSupported()) {
            manager.requestPinAppWidget(provider, null, null);
            Toast.makeText(activity, "Android-Widget wird vom Launcher angefragt.", Toast.LENGTH_LONG).show();
            return;
        }
        int[] ids = manager.getAppWidgetIds(provider);
        if (ids != null && ids.length > 0) {
            for (int id : ids) {
                saveWidget(context, id, nodePath, displayName, title, text, bgColor, fgColor, textSizeSp);
                updateWidget(context, manager, id);
            }
            Toast.makeText(activity, "Vorhandene Notizen-Widgets aktualisiert.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, "Dieser Launcher erlaubt kein automatisches Platzieren. Bitte das Notizen-Widget manuell hinzufügen.", Toast.LENGTH_LONG).show();
        }
    }

    public static void updateExistingWidgetsFromDocument(Context context, NoteDocument document, String displayName, float textSizeSp) {
        if (context == null || document == null) return;
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, NoteWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        if (ids == null || ids.length == 0) return;
        SharedPreferences p = prefs(context);
        for (int id : ids) {
            String path = p.getString(key(id, "path"), "");
            float storedTextSp = p.getFloat(key(id, "textSp"), textSizeSp);
            LegacyAndroidWidgetRegistry.WidgetSpec spec = LegacyAndroidWidgetRegistry.fromNodePath(id, document, path, displayName, storedTextSp);
            saveWidget(context, id, spec.nodePath, spec.documentName, spec.title, spec.text, spec.bgArgb, spec.fgArgb, spec.textSizeSp);
            updateWidget(context, manager, id);
        }
    }

    public static String widgetListSummary(Context context) {
        if (context == null) return "Keine Android-Haftnotiz-Widgets registriert.";
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, NoteWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(provider);
        if (ids == null || ids.length == 0) return "Keine Android-Haftnotiz-Widgets registriert.";
        SharedPreferences p = prefs(context);
        List<LegacyAndroidWidgetRegistry.WidgetSpec> specs = new ArrayList<>();
        for (int id : ids) {
            specs.add(new LegacyAndroidWidgetRegistry.WidgetSpec(
                    id,
                    p.getString(key(id, "display"), ""),
                    p.getString(key(id, "path"), ""),
                    p.getString(key(id, "title"), "Notiz"),
                    p.getString(key(id, "text"), ""),
                    p.getInt(key(id, "bg"), Color.rgb(255, 250, 205)),
                    p.getInt(key(id, "fg"), Color.rgb(30, 30, 30)),
                    p.getFloat(key(id, "textSp"), 13f)));
        }
        return LegacyAndroidWidgetRegistry.listSummary(specs);
    }

    @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (appWidgetIds == null) return;
        for (int id : appWidgetIds) {
            ensureWidgetData(context, id);
            updateWidget(context, appWidgetManager, id);
        }
    }

    @Override public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor editor = prefs(context).edit();
        if (appWidgetIds != null) {
            for (int id : appWidgetIds) {
                editor.remove(key(id, "title"));
                editor.remove(key(id, "text"));
                editor.remove(key(id, "bg"));
                editor.remove(key(id, "fg"));
                editor.remove(key(id, "path"));
                editor.remove(key(id, "display"));
                editor.remove(key(id, "textSp"));
            }
        }
        editor.apply();
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        SharedPreferences p = prefs(context);
        String title = p.getString(key(appWidgetId, "title"), "Notiz");
        String text = p.getString(key(appWidgetId, "text"), "");
        String path = p.getString(key(appWidgetId, "path"), "");
        String display = p.getString(key(appWidgetId, "display"), "");
        int bg = p.getInt(key(appWidgetId, "bg"), Color.rgb(255, 250, 205));
        int fg = p.getInt(key(appWidgetId, "fg"), Color.rgb(30, 30, 30));
        float textSp = p.getFloat(key(appWidgetId, "textSp"), 13f);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note);
        views.setTextViewText(R.id.widget_title, title == null || title.trim().isEmpty() ? "Notiz" : title.trim());
        views.setTextViewText(R.id.widget_body, text == null || text.trim().isEmpty() ? " " : text.trim());
        views.setInt(R.id.widget_root, "setBackgroundColor", 0xff000000 | (bg & 0x00ffffff));
        views.setTextColor(R.id.widget_title, 0xff000000 | (fg & 0x00ffffff));
        views.setTextColor(R.id.widget_body, 0xff000000 | (fg & 0x00ffffff));
        views.setTextViewTextSize(R.id.widget_body, TypedValue.COMPLEX_UNIT_SP, LegacyAndroidWidgetRegistry.normalizeWidgetTextSp(textSp));

        Intent open = new Intent(context, MainActivity.class);
        open.setAction(ACTION_OPEN_WIDGET_NODE);
        open.putExtra(EXTRA_NODE_PATH, path == null ? "" : path);
        open.putExtra(EXTRA_DISPLAY_NAME, display == null ? "" : display);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pending = PendingIntent.getActivity(context, appWidgetId, open, flags);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
        manager.updateAppWidget(appWidgetId, views);
    }

    private static void ensureWidgetData(Context context, int appWidgetId) {
        SharedPreferences p = prefs(context);
        if (p.contains(key(appWidgetId, "title"))) return;
        saveWidget(context,
                appWidgetId,
                p.getString(PENDING_PATH, ""),
                p.getString(PENDING_DISPLAY, ""),
                p.getString(PENDING_TITLE, "Notiz"),
                p.getString(PENDING_TEXT, ""),
                p.getInt(PENDING_BG, Color.rgb(255, 250, 205)),
                p.getInt(PENDING_FG, Color.rgb(30, 30, 30)),
                p.getFloat(PENDING_TEXT_SP, 13f));
    }

    private static void savePending(Context context, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp) {
        prefs(context).edit()
                .putString(PENDING_PATH, nodePath == null ? "" : nodePath)
                .putString(PENDING_DISPLAY, displayName == null ? "" : displayName)
                .putString(PENDING_TITLE, title == null ? "Notiz" : title)
                .putString(PENDING_TEXT, text == null ? "" : text)
                .putInt(PENDING_BG, bgColor)
                .putInt(PENDING_FG, fgColor)
                .putFloat(PENDING_TEXT_SP, LegacyAndroidWidgetRegistry.normalizeWidgetTextSp(textSizeSp))
                .apply();
    }

    private static void saveWidget(Context context, int appWidgetId, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp) {
        prefs(context).edit()
                .putString(key(appWidgetId, "path"), nodePath == null ? "" : nodePath)
                .putString(key(appWidgetId, "display"), displayName == null ? "" : displayName)
                .putString(key(appWidgetId, "title"), title == null ? "Notiz" : title)
                .putString(key(appWidgetId, "text"), text == null ? "" : text)
                .putInt(key(appWidgetId, "bg"), bgColor)
                .putInt(key(appWidgetId, "fg"), fgColor)
                .putFloat(key(appWidgetId, "textSp"), LegacyAndroidWidgetRegistry.normalizeWidgetTextSp(textSizeSp))
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(int appWidgetId, String suffix) {
        return appWidgetId + "_" + suffix;
    }
}
