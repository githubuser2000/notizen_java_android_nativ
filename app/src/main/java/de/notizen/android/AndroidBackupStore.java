package de.notizen.android;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public final class AndroidBackupStore {
    private AndroidBackupStore() {}

    public static File createBackup(Context context, String displayName, byte[] previousBytes, int keep) {
        if (context == null || previousBytes == null || previousBytes.length == 0 || keep <= 0) return null;
        try {
            File dir = backupDir(context, displayName);
            if (!dir.exists() && !dir.mkdirs()) return null;
            String stem = stem(displayName);
            String stamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(new Date());
            File out = new File(dir, stem + "-" + stamp + ".alx");
            FileOutputStream fos = new FileOutputStream(out);
            fos.write(previousBytes);
            fos.close();
            prune(dir, keep);
            return out;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static File[] listBackups(Context context, String displayName) {
        if (context == null) return new File[0];
        File dir = backupDir(context, displayName);
        File[] files = dir.listFiles(file -> file.isFile() && file.getName().toLowerCase(Locale.ROOT).endsWith(".alx"));
        if (files == null) return new File[0];
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        return files;
    }

    public static String describe(File file) {
        if (file == null) return "";
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY).format(new Date(file.lastModified()));
        return file.getName() + "  (" + date + ", " + file.length() + " Bytes)";
    }

    private static File backupDir(Context context, String displayName) {
        return new File(new File(context.getFilesDir(), "backups"), stem(displayName));
    }

    private static String stem(String displayName) {
        String safe = sanitize(displayName == null || displayName.isEmpty() ? "unbenannt.alx" : displayName);
        return safe.toLowerCase(Locale.ROOT).endsWith(".alx") ? safe.substring(0, safe.length() - 4) : safe;
    }

    private static void prune(File dir, int keep) {
        File[] files = dir.listFiles();
        if (files == null || files.length <= keep) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i < files.length - keep; i++) files[i].delete();
    }

    private static String sanitize(String text) {
        return text.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
