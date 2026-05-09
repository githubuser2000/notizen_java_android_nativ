package de.notizen.android.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Testable GNOME/AppIndicator tray-start rules from the PyQt port. */
public final class LegacyTraySupport {
    public static final String[] KNOWN_GNOME_TRAY_EXTENSION_UUIDS = new String[] {
            "appindicatorsupport@rgcjonas.gmail.com",
            "ubuntu-appindicators@ubuntu.com",
            "trayIconsReloaded@selfmade.pl",
            "status-icons@gnome-shell-extensions.gcampax.github.com",
            "statusicons@gnome-shell-extensions.gcampax.github.com"
    };

    private LegacyTraySupport() {}

    public static final class TrayDecision {
        public final boolean hideToTray;
        public final String reason;
        public final boolean gnomeSession;
        public final boolean trayExtensionDetected;

        public TrayDecision(boolean hideToTray, String reason, boolean gnomeSession, boolean trayExtensionDetected) {
            this.hideToTray = hideToTray;
            this.reason = reason == null ? "" : reason;
            this.gnomeSession = gnomeSession;
            this.trayExtensionDetected = trayExtensionDetected;
        }
    }

    public static boolean isGnomeSession(Map<String, String> env) {
        StringBuilder merged = new StringBuilder();
        for (String key : new String[] {"XDG_CURRENT_DESKTOP", "XDG_SESSION_DESKTOP", "DESKTOP_SESSION", "GNOME_DESKTOP_SESSION_ID"}) {
            if (env != null && env.get(key) != null) merged.append(env.get(key)).append(':');
        }
        return merged.toString().toLowerCase(Locale.ROOT).contains("gnome");
    }

    public static List<String> parseGnomeExtensionList(String output) {
        ArrayList<String> out = new ArrayList<>();
        if (output == null) return out;
        for (String line : output.split("\\R")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) out.add(trimmed);
        }
        return out;
    }

    public static boolean hasKnownGnomeTrayExtension(List<String> enabledExtensions) {
        Set<String> enabled = new LinkedHashSet<>();
        if (enabledExtensions != null) {
            for (String item : enabledExtensions) enabled.add(item.toLowerCase(Locale.ROOT));
        }
        for (String uuid : KNOWN_GNOME_TRAY_EXTENSION_UUIDS) {
            if (enabled.contains(uuid.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    public static TrayDecision decideStartupTrayVisibility(boolean trayIconCreated,
                                                           boolean showInTaskbarWhenMinimized,
                                                           boolean gnomeSafeStart,
                                                           Map<String, String> env,
                                                           List<String> enabledGnomeExtensions,
                                                           boolean forceHideToTray) {
        if (!trayIconCreated) return new TrayDecision(false, "Kein Qt-Tray verfügbar.", false, false);
        if (showInTaskbarWhenMinimized) return new TrayDecision(false, "Einstellung zeigt minimierte Fenster in der Taskleiste.", false, false);
        boolean gnome = isGnomeSession(env);
        if (forceHideToTray || envFlag(env, "NOTIZEN_FORCE_TRAY_START") == Boolean.TRUE) {
            return new TrayDecision(true, "Tray-Start wurde erzwungen.", gnome, false);
        }
        Boolean override = envFlag(env, "NOTIZEN_GNOME_SAFE_TRAY");
        if (override != null) gnomeSafeStart = override;
        if (gnome && gnomeSafeStart) {
            boolean ext = hasKnownGnomeTrayExtension(enabledGnomeExtensions);
            if (ext) {
                return new TrayDecision(false, "GNOME-Sitzung: Hauptfenster bleibt sichtbar; Tray-Start nur mit --force-tray-start.", true, true);
            }
            return new TrayDecision(false, "GNOME-Sitzung ohne erkannte Tray-/AppIndicator-Erweiterung; Hauptfenster bleibt sichtbar.", true, false);
        }
        return new TrayDecision(true, "Tray-Verbergen erlaubt.", gnome, hasKnownGnomeTrayExtension(enabledGnomeExtensions));
    }

    public static String gnomeTrayInstallHint() {
        return "GNOME zeigt klassische Trayicons oft erst mit einer AppIndicator/KStatusNotifier-Erweiterung. "
                + "Notizen startet dort standardmäßig mit sichtbarem Hauptfenster; Tray-Start nur bewusst per --force-tray-start.";
    }

    private static Boolean envFlag(Map<String, String> env, String key) {
        String value = env == null ? null : env.get(key);
        if (value == null) return null;
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (v.equals("1") || v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("ja")) return Boolean.TRUE;
        if (v.equals("0") || v.equals("false") || v.equals("no") || v.equals("off") || v.equals("nein")) return Boolean.FALSE;
        return null;
    }
}
