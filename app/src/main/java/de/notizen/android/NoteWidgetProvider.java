package de.notizen.android;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static final String PENDING_IMAGE_PATH = "pending_image_path";
    private static final String PENDING_IMAGE_COUNT = "pending_image_count";
    private static final int WIDGET_PREVIEW_LONG_EDGE = 640;
    private static final int WIDGET_PREVIEW_CELL = 320;
    private static final int WIDGET_PREVIEW_QUALITY = 82;

    public static void requestPinOrUpdate(Activity activity, String title, String text, int bgColor, int fgColor) {
        requestPinOrUpdate(activity, "", "", title, text, bgColor, fgColor, 13f, null, 0);
    }

    public static void requestPinOrUpdate(Activity activity, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp) {
        requestPinOrUpdate(activity, nodePath, displayName, title, text, bgColor, fgColor, textSizeSp, null, 0);
    }

    public static void requestPinOrUpdate(Activity activity, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp, List<LegacyAndroidWidgetRegistry.WidgetImage> images, int originalImageCount) {
        if (activity == null) return;
        Context context = activity.getApplicationContext();
        savePending(context, nodePath, displayName, title, text, bgColor, fgColor, textSizeSp, images, originalImageCount);
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
                saveWidget(context, id, nodePath, displayName, title, text, bgColor, fgColor, textSizeSp, images, originalImageCount);
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
            saveWidget(context, id, spec.nodePath, spec.documentName, spec.title, spec.text, spec.bgArgb, spec.fgArgb, spec.textSizeSp, spec.images, spec.originalImageCount);
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
                    p.getFloat(key(id, "textSp"), 13f),
                    null,
                    p.getInt(key(id, "imageCount"), 0)));
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
                editor.remove(key(id, "imagePath"));
                editor.remove(key(id, "imageCount"));
                deleteWidgetPreview(context, Integer.toString(id));
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
        String imagePath = p.getString(key(appWidgetId, "imagePath"), "");
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
        Bitmap preview = decodeFilePreview(imagePath);
        if (preview != null) {
            views.setViewVisibility(R.id.widget_image, View.VISIBLE);
            views.setImageViewBitmap(R.id.widget_image, preview);
        } else {
            views.setViewVisibility(R.id.widget_image, View.GONE);
        }

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
        String copiedImagePath = copyPendingPreviewToWidget(context, appWidgetId, p.getString(PENDING_IMAGE_PATH, ""));
        prefs(context).edit()
                .putString(key(appWidgetId, "path"), p.getString(PENDING_PATH, ""))
                .putString(key(appWidgetId, "display"), p.getString(PENDING_DISPLAY, ""))
                .putString(key(appWidgetId, "title"), p.getString(PENDING_TITLE, "Notiz"))
                .putString(key(appWidgetId, "text"), p.getString(PENDING_TEXT, ""))
                .putInt(key(appWidgetId, "bg"), p.getInt(PENDING_BG, Color.rgb(255, 250, 205)))
                .putInt(key(appWidgetId, "fg"), p.getInt(PENDING_FG, Color.rgb(30, 30, 30)))
                .putFloat(key(appWidgetId, "textSp"), LegacyAndroidWidgetRegistry.normalizeWidgetTextSp(p.getFloat(PENDING_TEXT_SP, 13f)))
                .putString(key(appWidgetId, "imagePath"), copiedImagePath)
                .putInt(key(appWidgetId, "imageCount"), p.getInt(PENDING_IMAGE_COUNT, 0))
                .apply();
    }

    private static void savePending(Context context, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp, List<LegacyAndroidWidgetRegistry.WidgetImage> images, int originalImageCount) {
        String previewPath = writeWidgetPreviewImage(context, "pending", images);
        prefs(context).edit()
                .putString(PENDING_PATH, nodePath == null ? "" : nodePath)
                .putString(PENDING_DISPLAY, displayName == null ? "" : displayName)
                .putString(PENDING_TITLE, title == null ? "Notiz" : title)
                .putString(PENDING_TEXT, text == null ? "" : text)
                .putInt(PENDING_BG, bgColor)
                .putInt(PENDING_FG, fgColor)
                .putFloat(PENDING_TEXT_SP, LegacyAndroidWidgetRegistry.normalizeWidgetTextSp(textSizeSp))
                .putString(PENDING_IMAGE_PATH, previewPath)
                .putInt(PENDING_IMAGE_COUNT, Math.max(0, originalImageCount))
                .apply();
    }

    private static void saveWidget(Context context, int appWidgetId, String nodePath, String displayName, String title, String text, int bgColor, int fgColor, float textSizeSp, List<LegacyAndroidWidgetRegistry.WidgetImage> images, int originalImageCount) {
        String previewPath = writeWidgetPreviewImage(context, Integer.toString(appWidgetId), images);
        prefs(context).edit()
                .putString(key(appWidgetId, "path"), nodePath == null ? "" : nodePath)
                .putString(key(appWidgetId, "display"), displayName == null ? "" : displayName)
                .putString(key(appWidgetId, "title"), title == null ? "Notiz" : title)
                .putString(key(appWidgetId, "text"), text == null ? "" : text)
                .putInt(key(appWidgetId, "bg"), bgColor)
                .putInt(key(appWidgetId, "fg"), fgColor)
                .putFloat(key(appWidgetId, "textSp"), LegacyAndroidWidgetRegistry.normalizeWidgetTextSp(textSizeSp))
                .putString(key(appWidgetId, "imagePath"), previewPath)
                .putInt(key(appWidgetId, "imageCount"), Math.max(0, originalImageCount))
                .apply();
    }

    private static String writeWidgetPreviewImage(Context context, String tag, List<LegacyAndroidWidgetRegistry.WidgetImage> images) {
        File file = widgetPreviewFile(context, tag);
        if (context == null || file == null) return "";
        if (images == null || images.isEmpty()) {
            deleteQuietly(file);
            return "";
        }
        Bitmap preview = null;
        try {
            preview = composeWidgetPreview(images);
            if (preview == null) {
                deleteQuietly(file);
                return "";
            }
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            try {
                preview.compress(Bitmap.CompressFormat.JPEG, WIDGET_PREVIEW_QUALITY, out);
            } finally {
                try { out.close(); } catch (Exception ignored) {}
            }
            return file.getAbsolutePath();
        } catch (OutOfMemoryError oom) {
            deleteQuietly(file);
            return "";
        } catch (Exception ignored) {
            deleteQuietly(file);
            return "";
        } finally {
            if (preview != null) {
                try { preview.recycle(); } catch (Exception ignored) {}
            }
        }
    }

    private static Bitmap composeWidgetPreview(List<LegacyAndroidWidgetRegistry.WidgetImage> images) {
        ArrayList<Bitmap> decoded = new ArrayList<>();
        try {
            for (LegacyAndroidWidgetRegistry.WidgetImage image : images) {
                if (image == null || image.data == null || image.data.length == 0) continue;
                Bitmap bitmap = decodePreviewBytes(image.data, WIDGET_PREVIEW_LONG_EDGE);
                if (bitmap != null) decoded.add(bitmap);
                if (decoded.size() >= LegacyAndroidWidgetRegistry.MAX_WIDGET_RTF_IMAGES) break;
            }
            if (decoded.isEmpty()) return null;
            if (decoded.size() == 1) {
                Bitmap one = decoded.get(0);
                Bitmap copy = Bitmap.createBitmap(one.getWidth(), one.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(copy);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(one, 0, 0, null);
                return copy;
            }
            int count = decoded.size();
            int cols = 2;
            int rows = (count + 1) / 2;
            Bitmap out = Bitmap.createBitmap(cols * WIDGET_PREVIEW_CELL, rows * WIDGET_PREVIEW_CELL, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(out);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            for (int i = 0; i < count; i++) {
                int row = i / cols;
                int col = i % cols;
                Rect cell = new Rect(col * WIDGET_PREVIEW_CELL, row * WIDGET_PREVIEW_CELL, (col + 1) * WIDGET_PREVIEW_CELL, (row + 1) * WIDGET_PREVIEW_CELL);
                drawBitmapFitCenter(canvas, decoded.get(i), cell, paint);
            }
            return out;
        } finally {
            for (Bitmap b : decoded) {
                try { b.recycle(); } catch (Exception ignored) {}
            }
        }
    }

    private static Bitmap decodePreviewBytes(byte[] data, int maxLongEdge) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
            int width = bounds.outWidth;
            int height = bounds.outHeight;
            if (width <= 0 || height <= 0) return null;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = previewSampleSize(width, height, maxLongEdge);
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap decoded = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
            if (decoded == null) return null;
            int longEdge = Math.max(decoded.getWidth(), decoded.getHeight());
            if (longEdge <= maxLongEdge) return decoded;
            float scale = maxLongEdge / (float) longEdge;
            int newWidth = Math.max(1, Math.round(decoded.getWidth() * scale));
            int newHeight = Math.max(1, Math.round(decoded.getHeight() * scale));
            Bitmap scaled = Bitmap.createScaledBitmap(decoded, newWidth, newHeight, true);
            try { decoded.recycle(); } catch (Exception ignored) {}
            return scaled;
        } catch (OutOfMemoryError oom) {
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Bitmap decodeFilePreview(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        File file = new File(path);
        if (!file.exists() || !file.isFile()) return null;
        try {
            return decodePreviewBytes(readSmallFile(file), WIDGET_PREVIEW_LONG_EDGE);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static byte[] readSmallFile(File file) throws Exception {
        long size = file.length();
        if (size <= 0 || size > 2 * 1024 * 1024) return new byte[0];
        byte[] data = new byte[(int) size];
        FileInputStream in = new FileInputStream(file);
        try {
            int off = 0;
            while (off < data.length) {
                int n = in.read(data, off, data.length - off);
                if (n < 0) break;
                off += n;
            }
        } finally {
            try { in.close(); } catch (Exception ignored) {}
        }
        return data;
    }

    private static int previewSampleSize(int width, int height, int maxLongEdge) {
        int sample = 1;
        int longEdge = Math.max(width, height);
        while (longEdge / sample > maxLongEdge && sample < 32) sample *= 2;
        return Math.max(1, sample);
    }

    private static void drawBitmapFitCenter(Canvas canvas, Bitmap bitmap, Rect cell, Paint paint) {
        if (canvas == null || bitmap == null || cell == null) return;
        float scale = Math.min(cell.width() / (float) bitmap.getWidth(), cell.height() / (float) bitmap.getHeight());
        int width = Math.max(1, Math.round(bitmap.getWidth() * scale));
        int height = Math.max(1, Math.round(bitmap.getHeight() * scale));
        int left = cell.left + (cell.width() - width) / 2;
        int top = cell.top + (cell.height() - height) / 2;
        Rect dest = new Rect(left, top, left + width, top + height);
        canvas.drawBitmap(bitmap, null, dest, paint);
    }

    private static String copyPendingPreviewToWidget(Context context, int appWidgetId, String pendingPath) {
        if (context == null || pendingPath == null || pendingPath.trim().isEmpty()) return "";
        File src = new File(pendingPath);
        if (!src.exists() || !src.isFile()) return "";
        File dst = widgetPreviewFile(context, Integer.toString(appWidgetId));
        if (dst == null) return "";
        try {
            File dir = dst.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            InputStream in = new FileInputStream(src);
            try {
                OutputStream out = new FileOutputStream(dst);
                try {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
                } finally {
                    try { out.close(); } catch (Exception ignored) {}
                }
            } finally {
                try { in.close(); } catch (Exception ignored) {}
            }
            return dst.getAbsolutePath();
        } catch (Exception ignored) {
            return "";
        }
    }

    private static File widgetPreviewFile(Context context, String tag) {
        if (context == null) return null;
        File dir = new File(context.getFilesDir(), "widget-images");
        String safe = tag == null || tag.trim().isEmpty() ? "empty" : tag.replaceAll("[^A-Za-z0-9_.-]", "_");
        return new File(dir, "widget_" + safe + ".jpg");
    }

    private static void deleteWidgetPreview(Context context, String tag) {
        File f = widgetPreviewFile(context, tag);
        if (f != null) deleteQuietly(f);
    }

    private static void deleteQuietly(File file) {
        try { if (file != null && file.exists()) file.delete(); } catch (Exception ignored) {}
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(int appWidgetId, String suffix) {
        return appWidgetId + "_" + suffix;
    }
}
