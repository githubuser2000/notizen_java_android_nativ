package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Portable dependency/validation model for the old settings dialog. */
public final class LegacySettingsDialogModel {
    public static final int DEFAULT_AUTOSAVE_SECONDS = 60;

    public static final class ViewState {
        public final String language;
        public final String backupKeepText;
        public final boolean autosaveEnabled;
        public final String autosaveSecondsText;
        public final boolean autosaveSecondsEnabled;
        public final boolean autorunEnabled;
        public final boolean autorunMinimized;
        public final boolean autorunMinimizedEnabled;
        public final boolean showInTaskbarWhenMinimized;
        public final boolean showDesknoteBorders;

        public ViewState(String language, String backupKeepText, boolean autosaveEnabled,
                         String autosaveSecondsText, boolean autosaveSecondsEnabled,
                         boolean autorunEnabled, boolean autorunMinimized, boolean autorunMinimizedEnabled,
                         boolean showInTaskbarWhenMinimized, boolean showDesknoteBorders) {
            this.language = language == null ? "Auto" : language;
            this.backupKeepText = backupKeepText == null ? "30" : backupKeepText;
            this.autosaveEnabled = autosaveEnabled;
            this.autosaveSecondsText = autosaveSecondsText == null ? Integer.toString(DEFAULT_AUTOSAVE_SECONDS) : autosaveSecondsText;
            this.autosaveSecondsEnabled = autosaveSecondsEnabled;
            this.autorunEnabled = autorunEnabled;
            this.autorunMinimized = autorunMinimized;
            this.autorunMinimizedEnabled = autorunMinimizedEnabled;
            this.showInTaskbarWhenMinimized = showInTaskbarWhenMinimized;
            this.showDesknoteBorders = showDesknoteBorders;
        }
    }

    public static final class ApplyResult {
        public final LegacySettings settings;
        public final int autosaveIntervalMillis;
        public final List<String> warnings;

        public ApplyResult(LegacySettings settings, int autosaveIntervalMillis, List<String> warnings) {
            this.settings = settings;
            this.autosaveIntervalMillis = autosaveIntervalMillis;
            this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings == null ? Collections.<String>emptyList() : warnings));
        }
    }

    private LegacySettingsDialogModel() {}

    public static ViewState fromSettings(LegacySettings settings) {
        LegacySettings s = settings == null ? new LegacySettings() : settings;
        boolean autosave = s.autosaveSeconds > 0;
        boolean autorun = s.autorunEnabled;
        return new ViewState(
                s.language,
                Integer.toString(s.backupKeep),
                autosave,
                Integer.toString(autosave ? s.autosaveSeconds : DEFAULT_AUTOSAVE_SECONDS),
                autosave,
                autorun,
                autorun && s.autorunMinimized,
                autorun,
                s.showInTaskbarWhenMinimized,
                s.showDesknoteBorders
        );
    }

    public static ViewState onAutosaveChanged(ViewState current, boolean checked) {
        ViewState v = current == null ? fromSettings(null) : current;
        String seconds = v.autosaveSecondsText;
        if (checked && LegacySettings.normalizeAutosaveSeconds(seconds) <= 0) seconds = Integer.toString(DEFAULT_AUTOSAVE_SECONDS);
        return new ViewState(v.language, v.backupKeepText, checked, seconds, checked,
                v.autorunEnabled, v.autorunMinimized, v.autorunMinimizedEnabled,
                v.showInTaskbarWhenMinimized, v.showDesknoteBorders);
    }

    public static ViewState onAutorunChanged(ViewState current, boolean checked) {
        ViewState v = current == null ? fromSettings(null) : current;
        return new ViewState(v.language, v.backupKeepText, v.autosaveEnabled, v.autosaveSecondsText, v.autosaveSecondsEnabled,
                checked, checked && v.autorunMinimized, checked,
                v.showInTaskbarWhenMinimized, v.showDesknoteBorders);
    }

    public static ApplyResult apply(LegacySettings existing, String language, String backupKeepText,
                                    boolean autosaveEnabled, String autosaveSecondsText,
                                    boolean autorunEnabled, boolean autorunMinimized,
                                    boolean showInTaskbarWhenMinimized, boolean showDesknoteBorders) {
        LegacySettings out = existing == null ? new LegacySettings() : existing.copy();
        ArrayList<String> warnings = new ArrayList<>();
        out.language = LegacyI18n.resolveLanguage(language == null || language.trim().isEmpty() ? "Auto" : language.trim());
        int oldBackup = out.backupKeep;
        try {
            out.backupKeep = Math.max(0, Integer.parseInt(backupKeepText == null ? "" : backupKeepText.trim()));
        } catch (Exception e) {
            out.backupKeep = oldBackup;
            warnings.add("Backup-Anzahl ungültig; alter Wert bleibt: " + oldBackup);
        }
        out.autosaveSeconds = autosaveEnabled ? LegacySettings.normalizeAutosaveSeconds(autosaveSecondsText) : 0;
        if (autosaveEnabled && out.autosaveSeconds <= 0) out.autosaveSeconds = DEFAULT_AUTOSAVE_SECONDS;
        out.autorunEnabled = autorunEnabled;
        out.autorunMinimized = autorunEnabled && autorunMinimized;
        out.showInTaskbarWhenMinimized = showInTaskbarWhenMinimized;
        out.showDesknoteBorders = showDesknoteBorders;
        return new ApplyResult(out, out.autosaveSeconds <= 0 ? 0 : out.autosaveSeconds * 1000, warnings);
    }
}
