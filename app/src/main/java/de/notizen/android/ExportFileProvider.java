package de.notizen.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Minimal private provider for sharing exported Notizen files without exposing file:// URIs. */
public final class ExportFileProvider extends ContentProvider {
    public static final String AUTHORITY = "de.notizen.android.exportfileprovider";

    public static Uri writeExport(Context context, String fileName, byte[] bytes) throws IOException {
        if (context == null) throw new IOException("Context fehlt.");
        File dir = exportDir(context);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Export-Cache konnte nicht angelegt werden.");
        File file = new File(dir, sanitize(fileName));
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(bytes == null ? new byte[0] : bytes);
        out.close();
        return new Uri.Builder().scheme("content").authority(AUTHORITY).appendPath(file.getName()).build();
    }

    @Override public boolean onCreate() { return true; }

    @Override public String getType(Uri uri) {
        String name = fileNameFromUri(uri);
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot + 1 < name.length()) ext = name.substring(dot + 1).toLowerCase();
        if ("rtf".equals(ext)) return "application/rtf";
        if ("html".equals(ext) || "htm".equals(ext)) return "text/html";
        if ("txt".equals(ext)) return "text/plain";
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime == null ? "application/octet-stream" : mime;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws java.io.FileNotFoundException {
        if (mode != null && mode.contains("w")) throw new java.io.FileNotFoundException("Nur Lesen erlaubt.");
        File file = fileForUri(uri);
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        File file;
        try {
            file = fileForUri(uri);
        } catch (java.io.FileNotFoundException e) {
            file = new File(fileNameFromUri(uri));
        }
        String[] cols = projection == null ? new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE} : projection;
        MatrixCursor cursor = new MatrixCursor(cols, 1);
        Object[] values = new Object[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(cols[i])) values[i] = file.getName();
            else if (OpenableColumns.SIZE.equals(cols[i])) values[i] = file.isFile() ? file.length() : 0L;
            else values[i] = null;
        }
        cursor.addRow(values);
        return cursor;
    }

    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }

    private static File exportDir(Context context) { return new File(context.getCacheDir(), "shared-exports"); }

    private File fileForUri(Uri uri) throws java.io.FileNotFoundException {
        Context context = getContext();
        if (context == null) throw new java.io.FileNotFoundException("Context fehlt.");
        File file = new File(exportDir(context), sanitize(fileNameFromUri(uri)));
        if (!file.isFile()) throw new java.io.FileNotFoundException(file.getName());
        return file;
    }

    private static String fileNameFromUri(Uri uri) {
        if (uri == null) return "notizen-export.bin";
        java.util.List<String> segments = uri.getPathSegments();
        if (segments == null || segments.isEmpty()) return "notizen-export.bin";
        return segments.get(segments.size() - 1);
    }

    private static String sanitize(String fileName) {
        String name = fileName == null ? "" : fileName.trim();
        if (name.isEmpty()) name = "notizen-export.bin";
        name = name.replaceAll("[\\\\/:*?\"<>|]+", "_");
        while (name.startsWith(".")) name = name.substring(1);
        if (name.isEmpty()) name = "notizen-export.bin";
        return name;
    }
}
