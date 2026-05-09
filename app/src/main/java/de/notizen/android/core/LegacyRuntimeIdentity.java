package de.notizen.android.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Runtime identity and launcher metadata from the PyQt taskbar-icon pass,
 * plus the Android package identity used by this native Java port.
 */
public final class LegacyRuntimeIdentity {
    public static final String PYQT_MODULE = "notizen_py_qt";
    public static final String APP_DESKTOP_ID = "notizen-py-qt";
    public static final String APP_DISPLAY_NAME = "Notizen PyQt";
    public static final String ANDROID_PACKAGE = "de.notizen.android";
    public static final String ANDROID_DISPLAY_NAME = "Notizen Java Android Nativ";
    public static final String ANDROID_ACTIVITY = "de.notizen.android.MainActivity";

    private LegacyRuntimeIdentity() {}

    public static Map<String, String> linuxLauncherEnvironment() {
        LinkedHashMap<String, String> env = new LinkedHashMap<>();
        env.put("NOTIZEN_RESET_WINDOW", "1");
        env.put("RESOURCE_NAME", APP_DESKTOP_ID);
        return env;
    }

    public static String linuxDesktopExec() {
        return LegacySystemIntegration.buildLinuxDesktopExec(PYQT_MODULE, "python3", true,
                APP_DESKTOP_ID, true, true, true, "%f");
    }

    public static String linuxDesktopEntry() {
        return "[Desktop Entry]\n"
                + "Type=Application\n"
                + "Name=" + APP_DISPLAY_NAME + "\n"
                + "Exec=" + linuxDesktopExec() + "\n"
                + "Icon=" + APP_DESKTOP_ID + "\n"
                + "StartupWMClass=" + APP_DESKTOP_ID + "\n";
    }

    public static String androidIdentity(String versionName) {
        return ANDROID_DISPLAY_NAME
                + "\nPackage: " + ANDROID_PACKAGE
                + "\nActivity: " + ANDROID_ACTIVITY
                + "\nVersion: " + (versionName == null ? "" : versionName);
    }

    public static String summary(String versionName) {
        return androidIdentity(versionName)
                + "\n\nDesktop-ID: " + APP_DESKTOP_ID
                + "\nDesktop-Name: " + APP_DISPLAY_NAME
                + "\nPython-Modul: " + PYQT_MODULE
                + "\nLinux Exec: " + linuxDesktopExec()
                + "\n\nDesktop Entry:\n" + linuxDesktopEntry();
    }
}
