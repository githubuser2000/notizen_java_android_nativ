package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Java model of desknote_kontext.vb and desknote_kontext_opacy.vb. */
public final class LegacyDesktopNoteContextMenu {
    public static final class MenuItemSpec {
        public final int legacyIndex;
        public final String languageKey;
        public final String label;
        public final String action;

        public MenuItemSpec(int legacyIndex, String languageKey, String label, String action) {
            this.legacyIndex = legacyIndex;
            this.languageKey = languageKey == null ? "" : languageKey;
            this.label = label == null ? "" : label;
            this.action = action == null ? "" : action;
        }
    }

    public static final class OpacityOption {
        public final int legacyIndex;
        public final int transparencyPercent;
        public final double opacity;
        public final String label;
        public final String action;

        public OpacityOption(int legacyIndex, int transparencyPercent, double opacity, String label, String action) {
            this.legacyIndex = legacyIndex;
            this.transparencyPercent = transparencyPercent;
            this.opacity = opacity;
            this.label = label;
            this.action = action;
        }
    }

    private static final List<OpacityOption> OPACITY_OPTIONS;
    static {
        ArrayList<OpacityOption> opts = new ArrayList<>();
        int index = 0;
        for (int t = 90; t >= 0; t -= 10) {
            int opPercent = LegacyDesktopNote.opacityPercentForTransparencyPercent(t);
            String label = t == 0 ? " 0 %" : t + " %";
            opts.add(new OpacityOption(index, t, opPercent / 100.0d, label,
                    "desktop_note_opacity_" + (t < 10 ? "0" : "") + t));
            index++;
        }
        OPACITY_OPTIONS = Collections.unmodifiableList(opts);
    }

    private LegacyDesktopNoteContextMenu() {}

    public static List<MenuItemSpec> menuItems(String language) {
        ArrayList<MenuItemSpec> items = new ArrayList<>();
        items.add(new MenuItemSpec(0, "kontext8", LegacyI18n.tr(language, "kontext8", "Hintergrundfarbe"), "desktop_note_background_color"));
        items.add(new MenuItemSpec(1, "kontext9", LegacyI18n.tr(language, "kontext9", "Minimieren"), "desktop_note_hide"));
        items.add(new MenuItemSpec(2, "kontext10", LegacyI18n.tr(language, "kontext10", "Schließen"), "desktop_note_close"));
        return Collections.unmodifiableList(items);
    }

    public static List<OpacityOption> opacityOptions() { return OPACITY_OPTIONS; }

    public static OpacityOption opacityOptionForTransparency(int transparencyPercent) {
        for (OpacityOption option : OPACITY_OPTIONS) if (option.transparencyPercent == transparencyPercent) return option;
        return null;
    }

    public static DesktopNoteState applyHide(DesktopNoteState state) {
        DesktopNoteState out = state == null ? new DesktopNoteState() : state.copy();
        out.visible = false;
        return out;
    }

    public static DesktopNoteState applyOpacity(DesktopNoteState state, int transparencyPercent) {
        DesktopNoteState out = state == null ? new DesktopNoteState() : state.copy();
        out.opacity = LegacyDesktopNote.opacityPercentForTransparencyPercent(transparencyPercent) / 100.0d;
        return out;
    }

    /** Closing the floating form does not delete the tree node; Android preserves the metadata and hides it. */
    public static boolean closeActionKeepsNodeButHides() { return true; }
}
