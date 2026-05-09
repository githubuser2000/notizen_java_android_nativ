package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Java model of the old toolstrip_breite / append-position calculations. */
public final class LegacyToolstripLayout {
    public static final class Strip {
        public final String name;
        public final int width;
        public final boolean visible;
        public final int x;
        public final int y;

        public Strip(String name, int width, boolean visible, int x, int y) {
            this.name = name == null ? "" : name;
            this.width = Math.max(0, width);
            this.visible = visible;
            this.x = Math.max(0, x);
            this.y = Math.max(0, y);
        }
    }

    public static final class Placement {
        public final int x;
        public final int y;
        public final String afterName;

        public Placement(int x, int y, String afterName) {
            this.x = Math.max(0, x);
            this.y = Math.max(0, y);
            this.afterName = afterName == null ? "" : afterName;
        }
    }

    private LegacyToolstripLayout() {}

    public static List<Strip> defaultStrips(boolean mainVisible, boolean fontVisible) {
        ArrayList<Strip> out = new ArrayList<>();
        out.add(new Strip("ToolStrip1", 310, mainVisible, 0, 0));
        out.add(new Strip("ToolStrip2", 250, true, 310, 0));
        out.add(new Strip("ToolStrip3", 365, fontVisible, 560, 0));
        out.add(new Strip("ToolStrip4", 180, true, 925, 0));
        return Collections.unmodifiableList(out);
    }

    public static int visibleWidth(List<Strip> strips) {
        int width = 0;
        if (strips != null) for (Strip s : strips) if (s != null && s.visible) width += s.width;
        return width;
    }

    public static Placement appendAfterVisible(List<Strip> strips, String afterName) {
        return new Placement(visibleWidth(strips), 0, afterName);
    }

    public static int[] positionForSettings(LegacySettings settings, String group, Placement fallback) {
        if (settings != null && group != null) {
            int[] value = settings.toolstripPositions.get(group);
            if (value != null && value.length >= 2) return new int[]{Math.max(0, value[0]), Math.max(0, value[1])};
        }
        return new int[]{fallback == null ? 0 : fallback.x, fallback == null ? 0 : fallback.y};
    }
}
