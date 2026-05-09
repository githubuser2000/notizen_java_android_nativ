package de.notizen.android;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import de.notizen.android.core.LegacySettings;

public final class AndroidSettingsStore {
    private AndroidSettingsStore() {}

    public static File settingsFile(Context context) {
        return new File(context.getFilesDir(), "notizen.config.xml");
    }

    public static LegacySettings load(Context context) {
        File file = settingsFile(context);
        if (!file.exists()) {
            LegacySettings settings = new LegacySettings();
            save(context, settings);
            return settings;
        }
        try {
            return LegacySettings.fromXmlBytes(readFile(file));
        } catch (Exception e) {
            return new LegacySettings();
        }
    }

    public static void save(Context context, LegacySettings settings) {
        try {
            FileOutputStream out = new FileOutputStream(settingsFile(context));
            out.write((settings == null ? new LegacySettings() : settings).toXmlBytes());
            out.close();
        } catch (Exception ignored) {
        }
    }

    public static byte[] readFile(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        in.close();
        return out.toByteArray();
    }
}
