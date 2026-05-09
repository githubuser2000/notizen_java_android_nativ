package de.notizen.android.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Extra Android settings surfaced in the native port. */
public final class LegacyAndroidSettingsUiModel {
    private LegacyAndroidSettingsUiModel() {}

    public static final class ViewState {
        public final String defaultDirectory;
        public final String toolbarButtonDp;
        public final String headerTextSp;
        public final String treeTextSp;
        public final String editorTextSp;
        public final String widgetTextSp;
        public final String largeImageLongEdgePx;
        public final boolean autoDownsampleLargeImages;
        public final boolean restoreRuntimeSnapshot;
        public final boolean showDiagnosticsToolbar;

        public ViewState(String defaultDirectory, String toolbarButtonDp, String headerTextSp, String treeTextSp, String editorTextSp,
                         String widgetTextSp, String largeImageLongEdgePx, boolean autoDownsampleLargeImages,
                         boolean restoreRuntimeSnapshot, boolean showDiagnosticsToolbar) {
            this.defaultDirectory = defaultDirectory == null ? "" : defaultDirectory;
            this.toolbarButtonDp = toolbarButtonDp == null ? "" : toolbarButtonDp;
            this.headerTextSp = headerTextSp == null ? "" : headerTextSp;
            this.treeTextSp = treeTextSp == null ? "" : treeTextSp;
            this.editorTextSp = editorTextSp == null ? "" : editorTextSp;
            this.widgetTextSp = widgetTextSp == null ? "" : widgetTextSp;
            this.largeImageLongEdgePx = largeImageLongEdgePx == null ? "" : largeImageLongEdgePx;
            this.autoDownsampleLargeImages = autoDownsampleLargeImages;
            this.restoreRuntimeSnapshot = restoreRuntimeSnapshot;
            this.showDiagnosticsToolbar = showDiagnosticsToolbar;
        }
    }

    public static final class ApplyResult {
        public final LegacySettings settings;
        public final List<String> warnings;

        public ApplyResult(LegacySettings settings, List<String> warnings) {
            this.settings = settings == null ? new LegacySettings() : settings;
            this.warnings = warnings == null ? new ArrayList<String>() : warnings;
        }
    }

    public static ViewState fromSettings(LegacySettings settings) {
        LegacySettings s = settings == null ? new LegacySettings() : settings;
        return new ViewState(
                s.lastDirectory,
                String.valueOf(LegacySettings.normalizeAndroidToolbarButtonDp(s.androidToolbarButtonDp)),
                trimFloat(LegacySettings.normalizeAndroidHeaderTextSp(s.androidHeaderTextSp)),
                trimFloat(LegacySettings.normalizeAndroidTreeTextSp(s.androidTreeTextSp)),
                trimFloat(LegacySettings.normalizeAndroidEditorTextSp(s.androidEditorTextSp)),
                trimFloat(LegacySettings.normalizeAndroidWidgetTextSp(s.androidWidgetTextSp)),
                String.valueOf(LegacySettings.normalizeAndroidLargeImageLongEdgePx(s.androidLargeImageLongEdgePx)),
                s.androidAutoDownsampleLargeImages,
                s.androidRestoreRuntimeSnapshot,
                s.androidShowDiagnosticsToolbar);
    }

    public static ApplyResult apply(LegacySettings settings, String defaultDirectory, String toolbarDp, String headerSp, String treeSp,
                                    String editorSp, String widgetSp, String imageEdgePx, boolean autoDownsample,
                                    boolean restoreSnapshot, boolean showDiagnostics) {
        LegacySettings s = settings == null ? new LegacySettings() : settings;
        ArrayList<String> warnings = new ArrayList<>();
        s.lastDirectory = defaultDirectory == null ? "" : defaultDirectory.trim();
        s.androidToolbarButtonDp = parseInt(toolbarDp, s.androidToolbarButtonDp, "Toolbar-Größe", warnings, LegacySettings::normalizeAndroidToolbarButtonDp);
        s.androidHeaderTextSp = parseFloat(headerSp, s.androidHeaderTextSp, "Überschriftgröße", warnings, LegacySettings::normalizeAndroidHeaderTextSp);
        s.androidTreeTextSp = parseFloat(treeSp, s.androidTreeTextSp, "Baumschrift", warnings, LegacySettings::normalizeAndroidTreeTextSp);
        s.androidEditorTextSp = parseFloat(editorSp, s.androidEditorTextSp, "Editor-Schrift", warnings, LegacySettings::normalizeAndroidEditorTextSp);
        s.androidWidgetTextSp = parseFloat(widgetSp, s.androidWidgetTextSp, "Widget-Schrift", warnings, LegacySettings::normalizeAndroidWidgetTextSp);
        s.androidLargeImageLongEdgePx = parseInt(imageEdgePx, s.androidLargeImageLongEdgePx, "Bild-Maximalgröße", warnings, LegacySettings::normalizeAndroidLargeImageLongEdgePx);
        s.androidAutoDownsampleLargeImages = autoDownsample;
        s.androidRestoreRuntimeSnapshot = restoreSnapshot;
        s.androidShowDiagnosticsToolbar = showDiagnostics;
        return new ApplyResult(s, warnings);
    }

    public static String summary(LegacySettings settings) {
        ViewState v = fromSettings(settings);
        return "Android-UI: Toolbar " + v.toolbarButtonDp + " dp, Kopf " + v.headerTextSp + " sp, Baum " + v.treeTextSp + " sp, Editor " + v.editorTextSp + " sp\n" +
                "Widgets: " + v.widgetTextSp + " sp · Bilder max. " + v.largeImageLongEdgePx + " px · Downsample " + yesNo(v.autoDownsampleLargeImages) + "\n" +
                "Wiederherstellung: " + yesNo(v.restoreRuntimeSnapshot) + " · Diagnosebuttons: " + yesNo(v.showDiagnosticsToolbar);
    }

    private interface IntNormalizer { int normalize(int value); }
    private interface FloatNormalizer { float normalize(float value); }

    private static int parseInt(String text, int fallback, String label, List<String> warnings, IntNormalizer normalizer) {
        try {
            int raw = Integer.parseInt((text == null ? "" : text.trim()));
            int normalized = normalizer.normalize(raw);
            if (normalized != raw) warnings.add(label + " wurde auf " + normalized + " begrenzt.");
            return normalized;
        } catch (Exception e) {
            warnings.add(label + " ungültig; alter Wert bleibt.");
            return normalizer.normalize(fallback);
        }
    }

    private static float parseFloat(String text, float fallback, String label, List<String> warnings, FloatNormalizer normalizer) {
        try {
            float raw = Float.parseFloat((text == null ? "" : text.trim()).replace(',', '.'));
            float normalized = normalizer.normalize(raw);
            if (Math.abs(normalized - raw) > 0.01f) warnings.add(label + " wurde auf " + trimFloat(normalized) + " begrenzt.");
            return normalized;
        } catch (Exception e) {
            warnings.add(label + " ungültig; alter Wert bleibt.");
            return normalizer.normalize(fallback);
        }
    }

    private static String yesNo(boolean value) { return value ? "ein" : "aus"; }

    private static String trimFloat(float value) {
        if (Math.abs(value - Math.round(value)) < 0.001f) return String.valueOf(Math.round(value));
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
