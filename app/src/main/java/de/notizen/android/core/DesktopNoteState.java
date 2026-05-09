package de.notizen.android.core;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Persisted state of a legacy Notizen.NET desktop sticky note.
 * Android cannot recreate independent desktop windows, but the values are
 * preserved in ALX round-trips so old files are not damaged.
 */
public final class DesktopNoteState {
    public int x = 80;
    public int y = 80;
    public int width = 260;
    public int height = 220;
    public boolean visible = true;
    public double opacity = 0.85d;
    public Integer argb = null;
    public boolean legacySparse = false;
    public final Set<String> legacyAttrNames = new LinkedHashSet<>();

    public DesktopNoteState copy() {
        DesktopNoteState c = new DesktopNoteState();
        c.x = x;
        c.y = y;
        c.width = width;
        c.height = height;
        c.visible = visible;
        c.opacity = opacity;
        c.argb = argb;
        c.legacySparse = legacySparse;
        c.legacyAttrNames.addAll(legacyAttrNames);
        return c;
    }
}
