package de.notizen.android.core;

/** Model of the old ToolStrip menu toggles. */
public final class LegacyToolbarToggleModel {
    public enum MenuToggle {
        FONT("ToolStrip_fontstyle", "Schrift-ToolStrip"),
        EDIT("ToolStrip_bearbeiten", "Bearbeiten-ToolStrip"),
        NEW_NODE("ToolStrip_neuelement", "Neu-ToolStrip"),
        FILE("ToolStrip_datei", "Datei-ToolStrip");
        public final String toolstripName;
        public final String label;
        MenuToggle(String toolstripName, String label) { this.toolstripName = toolstripName; this.label = label; }
    }

    public static final class ToggleState {
        public final MenuToggle toggle;
        public final String toolstripName;
        public final boolean checked;
        public final boolean visible;
        public final int x;
        public final String action;
        public ToggleState(MenuToggle toggle, boolean checked, boolean visible, int x, String action) {
            this.toggle = toggle == null ? MenuToggle.FONT : toggle;
            this.toolstripName = this.toggle.toolstripName;
            this.checked = checked;
            this.visible = visible;
            this.x = x;
            this.action = action == null ? "" : action;
        }
        public String summary() { return toolstripName + ": " + action + ", checked=" + checked + ", visible=" + visible + ", x=" + x; }
    }

    private LegacyToolbarToggleModel() {}

    public static ToggleState toggle(MenuToggle toggle, boolean currentlyVisible, boolean contentEnabled, int requestedX) {
        MenuToggle t = toggle == null ? MenuToggle.FONT : toggle;
        if ((t == MenuToggle.EDIT || t == MenuToggle.NEW_NODE) && !contentEnabled) {
            return new ToggleState(t, false, false, Math.max(0, requestedX), "blocked-content-disabled");
        }
        boolean nextVisible = !currentlyVisible;
        return new ToggleState(t, nextVisible, nextVisible, Math.max(0, requestedX), nextVisible ? "show" : "hide");
    }

    public static ToggleState state(MenuToggle toggle, boolean visible, int x) {
        MenuToggle t = toggle == null ? MenuToggle.FONT : toggle;
        return new ToggleState(t, visible, visible, Math.max(0, x), visible ? "shown" : "hidden");
    }
}
