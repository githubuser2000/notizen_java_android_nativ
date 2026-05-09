package de.notizen.android.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Portable model of the old four-entry Recent-Files menu from the WinForms app.
 * The legacy menu stored a visible text separately from the real path/tag and
 * rotated the clicked entry to the newest slot.
 */
public final class LegacyRecentMenu {
    public static final int MAX_RECENT = 4;

    public static final class Slot {
        public final int slotNumber; // 1..4, matching File1..File4 style menus.
        public final String key;     // a/b/c/d legacy config key.
        public final String text;    // visible menu text.
        public final String tag;     // real open target.
        public final boolean visible;

        public Slot(int slotNumber, String key, String text, String tag, boolean visible) {
            this.slotNumber = Math.max(1, Math.min(MAX_RECENT, slotNumber));
            this.key = clean(key);
            this.text = clean(text);
            this.tag = clean(tag);
            this.visible = visible && !this.tag.isEmpty();
        }

        public Slot withTag(String newTag) {
            String tagValue = clean(newTag);
            return new Slot(slotNumber, key, labelForPath(tagValue), tagValue, !tagValue.isEmpty());
        }
    }

    public static final class Activation {
        public final boolean allowed;
        public final String selectedPath;
        public final List<Slot> slots;
        public final int openThis; // 1..4 after legacy rotation, 0 if blocked.
        public final String message;

        public Activation(boolean allowed, String selectedPath, List<Slot> slots, int openThis, String message) {
            this.allowed = allowed;
            this.selectedPath = clean(selectedPath);
            this.slots = Collections.unmodifiableList(new ArrayList<>(slots == null ? Collections.<Slot>emptyList() : slots));
            this.openThis = Math.max(0, openThis);
            this.message = clean(message);
        }
    }

    private LegacyRecentMenu() {}

    public static List<Slot> emptySlots() {
        ArrayList<Slot> out = new ArrayList<>();
        String[] keys = LegacySettings.RECENT_FILE_SLOTS;
        for (int i = 0; i < MAX_RECENT; i++) out.add(new Slot(i + 1, i < keys.length ? keys[i] : String.valueOf(i + 1), "", "", false));
        return out;
    }

    public static List<Slot> fromPaths(List<String> paths) {
        ArrayList<String> normalized = normalizePaths(paths);
        ArrayList<Slot> out = new ArrayList<>();
        String[] keys = LegacySettings.RECENT_FILE_SLOTS;
        for (int i = 0; i < MAX_RECENT; i++) {
            String path = i < normalized.size() ? normalized.get(i) : "";
            out.add(new Slot(i + 1, i < keys.length ? keys[i] : String.valueOf(i + 1), labelForPath(path), path, !path.isEmpty()));
        }
        return out;
    }

    public static String[] labelsFromPaths(List<String> paths) {
        ArrayList<String> labels = new ArrayList<>();
        for (Slot slot : fromPaths(paths)) if (slot.visible) labels.add(slot.text);
        return labels.toArray(new String[0]);
    }

    public static List<String> toPaths(List<Slot> slots) {
        ArrayList<String> out = new ArrayList<>();
        if (slots != null) {
            for (Slot slot : slots) if (slot != null && slot.visible && !slot.tag.isEmpty()) out.add(slot.tag);
        }
        while (out.size() > MAX_RECENT) out.remove(0);
        return out;
    }

    public static List<Slot> remember(List<Slot> slots, String fullPath) {
        ArrayList<String> paths = normalizePaths(toPaths(slots));
        String path = clean(fullPath);
        if (!path.isEmpty()) {
            paths.remove(path);
            paths.add(path);
            while (paths.size() > MAX_RECENT) paths.remove(0);
        }
        return fromPaths(paths);
    }

    public static List<Slot> remember(List<Slot> slots, String directory, String file) {
        return remember(slots, joinDirectoryFile(directory, file));
    }

    public static Activation activate(List<Slot> slots, int visibleIndex, boolean fileExists) {
        List<Slot> safe = slots == null ? emptySlots() : slots;
        ArrayList<Slot> visible = new ArrayList<>();
        for (Slot s : safe) if (s != null && s.visible) visible.add(s);
        if (visibleIndex < 0 || visibleIndex >= visible.size()) {
            return new Activation(false, "", safe, 0, "Recent-Eintrag existiert nicht.");
        }
        Slot selected = visible.get(visibleIndex);
        if (!fileExists) {
            return new Activation(false, selected.tag, safe, 0, "File does not exist: " + selected.tag);
        }
        List<Slot> rotated = remember(safe, selected.tag);
        return new Activation(true, selected.tag, rotated, toPaths(rotated).size(), "");
    }

    public static boolean pathExistsForLegacyClick(String path) {
        LegacyOpenTarget target = LegacyOpenTarget.parse(path);
        if (target.isFtp() || target.isAndroidUri()) return true;
        return target.isLocalFile() && new File(target.normalized).exists();
    }

    public static String labelForPath(String path) {
        String p = clean(path);
        if (p.isEmpty()) return "";
        LegacyOpenTarget t = LegacyOpenTarget.parse(p);
        String normalized = t.normalized.isEmpty() ? p : t.normalized;
        if (t.isFtp() || t.isAndroidUri()) return compactRemoteLabel(normalized);
        int slash = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        if (slash < 0) return normalized;
        String file = slash + 1 < normalized.length() ? normalized.substring(slash + 1) : normalized;
        String dir = normalized.substring(0, slash);
        if (file.isEmpty()) return normalized;
        if (dir.isEmpty()) return file;
        return file + " — " + dir;
    }

    private static String compactRemoteLabel(String value) {
        String v = clean(value);
        int slash = v.lastIndexOf('/');
        if (slash <= "ftp://".length() || slash + 1 >= v.length()) return v;
        String file = v.substring(slash + 1);
        String host = v.substring(0, slash);
        int proto = host.indexOf("://");
        if (proto >= 0) {
            int at = host.indexOf('@', proto + 3);
            if (at >= 0) host = host.substring(0, proto + 3) + host.substring(at + 1);
        }
        return file + " — " + host;
    }

    private static ArrayList<String> normalizePaths(List<String> paths) {
        ArrayList<String> out = new ArrayList<>();
        if (paths != null) {
            for (String p : paths) {
                String value = clean(p);
                if (value.isEmpty()) continue;
                out.remove(value);
                out.add(value);
                while (out.size() > MAX_RECENT) out.remove(0);
            }
        }
        return out;
    }

    public static String joinDirectoryFile(String directory, String file) {
        String d = clean(directory);
        String f = clean(file);
        if (d.isEmpty()) return f;
        if (f.isEmpty()) return d;
        if (d.endsWith("/") || d.endsWith("\\")) return d + f;
        return d + File.separator + f;
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
