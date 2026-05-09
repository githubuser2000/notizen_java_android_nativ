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
import android.widget.RemoteViews;
import android.widget.Toast;

/** Native Android home-screen widget for a selected Notizen node. */
public final class NoteWidgetProvider extends AppWidgetProvider {
    private static final String PREFS = "notizen_java_android_widgets";
    private static final String PENDING_TITLE = "pending_title";
    private static final String PENDING_TEXT = "pending_text";
    private static final String PENDING_BG = "pending_bg";
    private static final String PENDING_FG = "pending_fg";

    public static void requestPinOrUpdate(Activity activity, String title, String text, int bgColor, int fgColor) {
        if (activity == null) return;
        Context context = activity.getApplicationContext();
        savePending(context, title, text, bgColor, fgColor);
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
                saveWidget(context, id, title, text, bgColor, fgColor);
                updateWidget(context, manager, id);
            }
            Toast.makeText(activity, "Vorhandene Notizen-Widgets aktualisiert.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(activity, "Dieser Launcher erlaubt kein automatisches Platzieren. Bitte das Notizen-Widget manuell hinzufügen.", Toast.LENGTH_LONG).show();
        }
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
            }
        }
        editor.apply();
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        SharedPreferences p = prefs(context);
        String title = p.getString(key(appWidgetId, "title"), "Notiz");
        String text = p.getString(key(appWidgetId, "text"), "");
        int bg = p.getInt(key(appWidgetId, "bg"), Color.rgb(255, 250, 205));
        int fg = p.getInt(key(appWidgetId, "fg"), Color.rgb(30, 30, 30));
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_note);
        views.setTextViewText(R.id.widget_title, title == null || title.trim().isEmpty() ? "Notiz" : title.trim());
        views.setTextViewText(R.id.widget_body, text == null || text.trim().isEmpty() ? " " : text.trim());
        views.setInt(R.id.widget_root, "setBackgroundColor", 0xff000000 | (bg & 0x00ffffff));
        views.setTextColor(R.id.widget_title, 0xff000000 | (fg & 0x00ffffff));
        views.setTextColor(R.id.widget_body, 0xff000000 | (fg & 0x00ffffff));

        Intent open = new Intent(context, MainActivity.class);
        open.setAction(Intent.ACTION_MAIN);
        open.addCategory(Intent.CATEGORY_LAUNCHER);
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
                p.getString(PENDING_TITLE, "Notiz"),
                p.getString(PENDING_TEXT, ""),
                p.getInt(PENDING_BG, Color.rgb(255, 250, 205)),
                p.getInt(PENDING_FG, Color.rgb(30, 30, 30)));
    }

    private static void savePending(Context context, String title, String text, int bgColor, int fgColor) {
        prefs(context).edit()
                .putString(PENDING_TITLE, title == null ? "Notiz" : title)
                .putString(PENDING_TEXT, text == null ? "" : text)
                .putInt(PENDING_BG, bgColor)
                .putInt(PENDING_FG, fgColor)
                .apply();
    }

    private static void saveWidget(Context context, int appWidgetId, String title, String text, int bgColor, int fgColor) {
        prefs(context).edit()
                .putString(key(appWidgetId, "title"), title == null ? "Notiz" : title)
                .putString(key(appWidgetId, "text"), text == null ? "" : text)
                .putInt(key(appWidgetId, "bg"), bgColor)
                .putInt(key(appWidgetId, "fg"), fgColor)
                .apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(int appWidgetId, String suffix) {
        return appWidgetId + "_" + suffix;
    }
}
