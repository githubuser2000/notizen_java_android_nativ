package de.notizen.android.core;

/** Portable save/open state decisions from Datei.vb and Notizen.vb. */
public final class LegacySaveWorkflow {
    public enum Action { SAVE_URI, SAVE_RAW_FILE, SAVE_FTP, SAVE_AS, NOTHING }

    public static final class SavePlan {
        public final Action action;
        public final boolean direct;
        public final String reason;
        public final String status;

        public SavePlan(Action action, boolean direct, String reason, String status) {
            this.action = action == null ? Action.NOTHING : action;
            this.direct = direct;
            this.reason = reason == null ? "" : reason;
            this.status = status == null ? "" : status;
        }
    }

    public static final class DirtyClosePlan {
        public final boolean ask;
        public final String dialogTitle;
        public final String message;
        public final String positive;
        public final String negative;
        public final String neutral;

        public DirtyClosePlan(boolean ask, String dialogTitle, String message, String positive, String negative, String neutral) {
            this.ask = ask;
            this.dialogTitle = dialogTitle == null ? "" : dialogTitle;
            this.message = message == null ? "" : message;
            this.positive = positive == null ? "" : positive;
            this.negative = negative == null ? "" : negative;
            this.neutral = neutral == null ? "" : neutral;
        }
    }

    private LegacySaveWorkflow() {}

    public static SavePlan planForAndroid(boolean hasUri, boolean hasRawFile, boolean hasFtp, boolean changed) {
        if (hasFtp) return new SavePlan(Action.SAVE_FTP, true, "ftpurl gesetzt", "FTP-Speichern");
        if (hasRawFile) return new SavePlan(Action.SAVE_RAW_FILE, true, "lokaler Dateipfad gesetzt", "Speichern");
        if (hasUri) return new SavePlan(Action.SAVE_URI, true, "Android-SAF-URI gesetzt", "Speichern");
        return new SavePlan(Action.SAVE_AS, false, changed ? "geändert ohne Speicherziel" : "kein Speicherziel", "Speichern unter");
    }

    public static SavePlan planForLegacyFileState(LegacyFileState state, boolean hasFtp, boolean changed) {
        if (hasFtp) return new SavePlan(Action.SAVE_FTP, true, "ftpurl gesetzt", "FTP-Speichern");
        boolean associated = state != null && state.fileExists();
        if (associated) return new SavePlan(Action.SAVE_RAW_FILE, true, "Datei.datei_da", "Speichern");
        return new SavePlan(Action.SAVE_AS, false, changed ? "Datei noch nicht gespeichert" : "Datei nicht zugeordnet", "Speichern unter");
    }

    public static DirtyClosePlan closePlan(boolean changed, String fileDisplayName) {
        if (!changed) return new DirtyClosePlan(false, "", "", "", "", "");
        String name = fileDisplayName == null || fileDisplayName.isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : fileDisplayName;
        return new DirtyClosePlan(true, "Speichern?", "Änderungen in " + name + " speichern?", "Speichern", "Nicht speichern", "Abbrechen");
    }

    public static String statusAfterSave(Action action, String displayName) {
        String name = displayName == null || displayName.isEmpty() ? LegacyPaths.LEGACY_DEFAULT_FILENAME : displayName;
        switch (action == null ? Action.NOTHING : action) {
            case SAVE_FTP: return "FTP gespeichert: " + name;
            case SAVE_RAW_FILE:
            case SAVE_URI: return "Gespeichert: " + name;
            case SAVE_AS: return "Speichern unter: " + name;
            default: return "Keine Speicherung nötig";
        }
    }
}
