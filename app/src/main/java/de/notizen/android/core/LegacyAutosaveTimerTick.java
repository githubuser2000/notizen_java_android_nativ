package de.notizen.android.core;

/** Timer-tick decision around the old autosave event. */
public final class LegacyAutosaveTimerTick {
    public static final class TickPlan {
        public final boolean shouldSave;
        public final String reason;
        public final boolean fileAssociated;
        public final boolean changed;
        public TickPlan(boolean shouldSave, String reason, boolean fileAssociated, boolean changed) {
            this.shouldSave = shouldSave;
            this.reason = reason == null ? "" : reason;
            this.fileAssociated = fileAssociated;
            this.changed = changed;
        }
        public String summary() { return "autosave=" + shouldSave + ", reason=" + reason + ", fileAssociated=" + fileAssociated + ", changed=" + changed; }
    }

    private LegacyAutosaveTimerTick() {}

    public static TickPlan decide(boolean autosaveEnabled, boolean changed, boolean fileAssociated, boolean saveAnywayLegacyFlag) {
        if (!autosaveEnabled) return new TickPlan(false, "autosave-disabled", fileAssociated, changed);
        if (!changed) return new TickPlan(false, "unchanged", fileAssociated, changed);
        if (!fileAssociated) return new TickPlan(false, "file-missing", fileAssociated, changed);
        return new TickPlan(true, saveAnywayLegacyFlag ? "legacy-save-anyway" : "changed-associated", fileAssociated, changed);
    }

    public static TickPlan fromAndroid(NoteDocument doc, boolean hasUri, boolean hasRawFile, boolean hasFtp) {
        boolean changed = doc != null && doc.changed;
        boolean associated = hasUri || hasRawFile || hasFtp;
        return decide(true, changed, associated, true);
    }
}
