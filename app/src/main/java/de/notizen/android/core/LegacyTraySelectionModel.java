package de.notizen.android.core;

/** Interprets the old tray_kontext_Closing menu index convention. */
public final class LegacyTraySelectionModel {
    public enum Action { NONE, EXIT, TOGGLE_MAIN, SHOW_DESKTOP_NOTE }
    public static final class Decision {
        public final Action action;
        public final int menuIndex;
        public final int desktopNoteIndex;
        public final NoteNode node;
        public final boolean reloadSmallWindow;
        public final String message;
        public Decision(Action action, int menuIndex, int desktopNoteIndex, NoteNode node, boolean reloadSmallWindow, String message) {
            this.action = action;
            this.menuIndex = menuIndex;
            this.desktopNoteIndex = desktopNoteIndex;
            this.node = node;
            this.reloadSmallWindow = reloadSmallWindow;
            this.message = message == null ? "" : message;
        }
    }
    private LegacyTraySelectionModel() {}
    public static Decision decide(LegacyDesktopNoteTrayRegistry.Registry registry, int menuIndex) {
        if (menuIndex == 0) return new Decision(Action.EXIT, menuIndex, -1, null, false, "Beenden");
        if (menuIndex == 1) return new Decision(Action.TOGGLE_MAIN, menuIndex, -1, null, false, "Hauptfenster anzeigen/verstecken");
        if (registry == null || menuIndex < 3) return new Decision(Action.NONE, menuIndex, -1, null, false, "Keine Aktion");
        int noteIndex = menuIndex - 3;
        if (noteIndex < 0 || noteIndex >= registry.desktopNodes.size()) return new Decision(Action.NONE, menuIndex, -1, null, false, "Keine Haftnotiz");
        NoteNode node = registry.desktopNodes.get(noteIndex);
        DesktopNoteState d = node.desktopNote;
        boolean reload = d != null && (d.width < 64 || d.height < 32);
        return new Decision(Action.SHOW_DESKTOP_NOTE, menuIndex, noteIndex, node, reload, reload ? "Haftnotiz neu laden" : "Haftnotiz anzeigen");
    }
}
