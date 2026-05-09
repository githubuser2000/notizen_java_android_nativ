package de.notizen.android.core;

/** save_and_if_canceled/schliessen_ohne_fragen as a portable decision model. */
public final class LegacyCloseResetModel {
    public enum Choice { SAVE, DONT_SAVE, CANCEL }
    public static final class ClosePlan {
        public final boolean proceed;
        public final boolean saveFirst;
        public final boolean canceled;
        public final boolean wipePasswordFromMemory;
        public final boolean resetToUnnamed;
        public final String message;
        public ClosePlan(boolean proceed, boolean saveFirst, boolean canceled, boolean wipePasswordFromMemory, boolean resetToUnnamed, String message) {
            this.proceed = proceed; this.saveFirst = saveFirst; this.canceled = canceled; this.wipePasswordFromMemory = wipePasswordFromMemory; this.resetToUnnamed = resetToUnnamed; this.message = message == null ? "" : message;
        }
    }
    public static final class ResetState {
        public final String displayName;
        public final boolean changed;
        public final String versionName;
        public ResetState(String displayName, boolean changed, String versionName) { this.displayName = displayName; this.changed = changed; this.versionName = versionName; }
    }
    private LegacyCloseResetModel() {}
    public static ClosePlan plan(NoteDocument document, String currentPassword, Choice choice) {
        boolean changed = document != null && document.changed;
        boolean hasPassword = (currentPassword != null && !currentPassword.isEmpty()) || (document != null && document.password != null && !document.password.isEmpty());
        if (!changed) return new ClosePlan(true, false, false, hasPassword, true, "Ohne Nachfrage schließen");
        if (choice == Choice.CANCEL) return new ClosePlan(false, false, true, false, false, "Abgebrochen");
        if (choice == Choice.SAVE) return new ClosePlan(true, true, false, hasPassword, true, "Speichern und schließen");
        return new ClosePlan(true, false, false, hasPassword, true, "Ohne Speichern schließen");
    }
    public static ResetState afterClose(String versionName) {
        return new ResetState(LegacyPaths.LEGACY_DEFAULT_FILENAME, true, versionName == null ? "" : versionName);
    }
    public static NoteDocument newUnnamedDocument() {
        NoteDocument d = NoteDocument.newDocument();
        d.displayName = LegacyPaths.LEGACY_DEFAULT_FILENAME;
        return d;
    }
}
