package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Portable, testable subset of the PyQt display-environment normalizer. */
public final class LegacyDisplayEnvironment {
    private static final List<String> VISIBLE_FLAGS = Arrays.asList("--show", "--visible", "--reset-window", "--no-tray");
    private static final List<String> SMOKE_FLAGS = Arrays.asList("--smoke-test");

    private LegacyDisplayEnvironment() {}

    public static final class DisplayEnvironmentDecision {
        public final boolean changed;
        public final String platformBefore;
        public final String platformAfter;
        public final String themeBefore;
        public final String themeAfter;
        public final String displayBefore;
        public final String displayAfter;
        public final String waylandBefore;
        public final String waylandAfter;
        public final String gdkBefore;
        public final String gdkAfter;
        public final List<String> notes;

        public DisplayEnvironmentDecision(boolean changed, String platformBefore, String platformAfter,
                                          String themeBefore, String themeAfter, String displayBefore, String displayAfter,
                                          String waylandBefore, String waylandAfter, String gdkBefore, String gdkAfter,
                                          List<String> notes) {
            this.changed = changed;
            this.platformBefore = nonNull(platformBefore);
            this.platformAfter = nonNull(platformAfter);
            this.themeBefore = nonNull(themeBefore);
            this.themeAfter = nonNull(themeAfter);
            this.displayBefore = nonNull(displayBefore);
            this.displayAfter = nonNull(displayAfter);
            this.waylandBefore = nonNull(waylandBefore);
            this.waylandAfter = nonNull(waylandAfter);
            this.gdkBefore = nonNull(gdkBefore);
            this.gdkAfter = nonNull(gdkAfter);
            this.notes = notes == null ? new ArrayList<>() : new ArrayList<>(notes);
        }

        public String summary() {
            StringBuilder out = new StringBuilder();
            out.append("changed=").append(changed ? 1 : 0)
                    .append(" | QT_QPA_PLATFORM=").append(platformAfter.isEmpty() ? "<unset>" : platformAfter)
                    .append(" | QT_QPA_PLATFORMTHEME=").append(themeAfter.isEmpty() ? "<unset>" : themeAfter)
                    .append(" | DISPLAY=").append(displayAfter.isEmpty() ? "<unset>" : displayAfter)
                    .append(" | WAYLAND_DISPLAY=").append(waylandAfter.isEmpty() ? "<unset>" : waylandAfter)
                    .append(" | GDK_BACKEND=").append(gdkAfter.isEmpty() ? "<unset>" : gdkAfter);
            if (!notes.isEmpty()) out.append(" | notes=").append(String.join("; ", notes));
            return out.toString();
        }
    }

    public static boolean visibleStartRequested(List<String> argv, Map<String, String> env) {
        if (argv != null) {
            for (String arg : argv) if (VISIBLE_FLAGS.contains(arg)) return true;
        }
        return truthy(get(env, "NOTIZEN_FORCE_VISIBLE")) || truthy(get(env, "NOTIZEN_RESET_WINDOW")) || truthy(get(env, "NOTIZEN_SAFE_DISPLAY"));
    }

    public static boolean applyGraphicalSessionEnvironment(Map<String, String> env, Map<String, String> session, List<String> notes) {
        if (env == null || session == null) return false;
        boolean changed = false;
        for (String key : new String[] {"XDG_RUNTIME_DIR", "WAYLAND_DISPLAY", "XDG_CURRENT_DESKTOP", "XDG_SESSION_DESKTOP"}) {
            String value = session.get(key);
            if (value != null && !value.isEmpty() && get(env, key).isEmpty()) {
                env.put(key, value);
                changed = true;
                if (notes != null) notes.add(key + " '<unset>' -> '" + value + "' from graphical session");
            }
        }
        String value = session.get("DISPLAY");
        String old = get(env, "DISPLAY");
        if (value != null && !value.isEmpty()) {
            if (old.isEmpty() || ((old.equals(":1") || old.equals(":1.0")) && !value.equals(":1") && !value.equals(":1.0"))) {
                if (!old.isEmpty()) env.putIfAbsent("NOTIZEN_ORIGINAL_DISPLAY", old);
                env.put("DISPLAY", value);
                changed = true;
                if (notes != null) notes.add("DISPLAY '" + (old.isEmpty() ? "<unset>" : old) + "' -> '" + value + "' from graphical session");
            }
        }
        return changed;
    }

    public static DisplayEnvironmentDecision normalizeQtDisplayEnvironment(List<String> argv, Map<String, String> env) {
        if (env == null) throw new IllegalArgumentException("env must not be null");
        String platformBefore = get(env, "QT_QPA_PLATFORM");
        String themeBefore = get(env, "QT_QPA_PLATFORMTHEME");
        String displayBefore = get(env, "DISPLAY");
        String waylandBefore = get(env, "WAYLAND_DISPLAY");
        String gdkBefore = get(env, "GDK_BACKEND");
        ArrayList<String> notes = new ArrayList<>();

        if (truthy(get(env, "NOTIZEN_KEEP_QT_ENV"))) {
            notes.add("kept by NOTIZEN_KEEP_QT_ENV");
            return new DisplayEnvironmentDecision(false, platformBefore, platformBefore, themeBefore, themeBefore,
                    displayBefore, displayBefore, waylandBefore, waylandBefore, gdkBefore, gdkBefore, notes);
        }

        boolean smoke = argv != null && argv.stream().anyMatch(SMOKE_FLAGS::contains) || truthy(get(env, "NOTIZEN_QT_SMOKE_TEST"));
        if (smoke) {
            setOrUnset(env, "QT_QPA_PLATFORM", "offscreen", notes);
            setOrUnset(env, "DISPLAY", null, notes);
            setOrUnset(env, "WAYLAND_DISPLAY", null, notes);
            setOrUnset(env, "GDK_BACKEND", null, notes);
            if (isGtkTheme(themeBefore)) setOrUnset(env, "QT_QPA_PLATFORMTHEME", null, notes);
        } else {
            boolean wantsVisible = visibleStartRequested(argv, env);
            boolean gnome = isGnome(env);
            boolean hasWayland = !get(env, "WAYLAND_DISPLAY").isEmpty();
            if (gnome && hasWayland && wantsVisible && (get(env, "DISPLAY").equals(":1") || get(env, "DISPLAY").equals(":1.0"))
                    && !truthy(get(env, "NOTIZEN_KEEP_DISPLAY")) && !truthy(get(env, "NOTIZEN_KEEP_SHELL_DISPLAY"))) {
                env.putIfAbsent("NOTIZEN_ORIGINAL_DISPLAY", get(env, "DISPLAY"));
                setOrUnset(env, "DISPLAY", ":0", notes);
            }
            String platformRoot = root(get(env, "QT_QPA_PLATFORM"));
            boolean hasX11 = !get(env, "DISPLAY").isEmpty();
            if (hasWayland && (wantsVisible || gnome)) {
                String desired = get(env, "NOTIZEN_QPA_PLATFORM").isEmpty() ? "wayland;xcb" : get(env, "NOTIZEN_QPA_PLATFORM");
                if (get(env, "QT_QPA_PLATFORM").isEmpty() || dangerousPlatform(platformRoot) || !get(env, "QT_QPA_PLATFORM").equals(desired)) {
                    setOrUnset(env, "QT_QPA_PLATFORM", desired, notes);
                }
                if (root(get(env, "GDK_BACKEND")).equals("x11")) setOrUnset(env, "GDK_BACKEND", null, notes);
            } else if (hasX11 && dangerousPlatform(platformRoot) && wantsVisible) {
                setOrUnset(env, "QT_QPA_PLATFORM", "xcb", notes);
            }
            if (hasWayland && (wantsVisible || gnome) && isGtkTheme(get(env, "QT_QPA_PLATFORMTHEME"))) {
                setOrUnset(env, "QT_QPA_PLATFORMTHEME", null, notes);
            }
        }

        String platformAfter = get(env, "QT_QPA_PLATFORM");
        String themeAfter = get(env, "QT_QPA_PLATFORMTHEME");
        String displayAfter = get(env, "DISPLAY");
        String waylandAfter = get(env, "WAYLAND_DISPLAY");
        String gdkAfter = get(env, "GDK_BACKEND");
        boolean changed = !platformBefore.equals(platformAfter) || !themeBefore.equals(themeAfter) || !displayBefore.equals(displayAfter)
                || !waylandBefore.equals(waylandAfter) || !gdkBefore.equals(gdkAfter);
        if (!notes.isEmpty()) env.put("NOTIZEN_DISPLAY_ENV_NOTES", String.join("; ", notes));
        return new DisplayEnvironmentDecision(changed, platformBefore, platformAfter, themeBefore, themeAfter,
                displayBefore, displayAfter, waylandBefore, waylandAfter, gdkBefore, gdkAfter, notes);
    }

    private static void setOrUnset(Map<String, String> env, String key, String value, List<String> notes) {
        String old = get(env, key);
        if (value == null) {
            if (env.containsKey(key)) {
                env.remove(key);
                notes.add(key + " '" + old + "' unset");
            }
        } else if (!old.equals(value)) {
            env.put(key, value);
            notes.add(key + " '" + (old.isEmpty() ? "<unset>" : old) + "' -> '" + value + "'");
        }
    }

    private static boolean isGnome(Map<String, String> env) {
        String d = (get(env, "XDG_CURRENT_DESKTOP") + " " + get(env, "XDG_SESSION_DESKTOP")).toLowerCase(Locale.ROOT);
        return d.contains("gnome");
    }

    private static boolean dangerousPlatform(String root) {
        return root.equals("offscreen") || root.equals("minimal") || root.equals("minimalegl") || root.equals("vnc")
                || root.equals("eglfs") || root.equals("linuxfb") || root.equals("directfb") || root.equals("webgl");
    }

    private static boolean isGtkTheme(String value) {
        String r = root(value);
        return r.equals("gtk2") || r.equals("gtk3");
    }

    private static String root(String value) {
        String v = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        int semi = v.indexOf(';');
        if (semi >= 0) v = v.substring(0, semi);
        int colon = v.indexOf(':');
        if (colon >= 0) v = v.substring(0, colon);
        return v;
    }

    private static boolean truthy(String value) {
        String v = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return v.equals("1") || v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("ja");
    }

    private static String get(Map<String, String> env, String key) { return env == null || env.get(key) == null ? "" : env.get(key); }
    private static String nonNull(String value) { return value == null ? "" : value; }
}
