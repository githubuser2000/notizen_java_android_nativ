package de.notizen.android.core;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Small editor behaviors carried over from the WinForms/PyQt ports. */
public final class LegacyEditorActions {
    public static final char LEGACY_BULLET_CHARACTER = '\u2022';

    private LegacyEditorActions() {}

    /** Exact old ToolStrip_dot insertion text: CR + bullet + three spaces. */
    public static String legacyClipboardBulletText() {
        return "\r" + LEGACY_BULLET_CHARACTER + "   ";
    }

    /** Android EditText/QTextCursor-friendly form of the same insertion. */
    public static String androidBulletInsertText() {
        return legacyClipboardBulletText().replace("\r\n", "\n").replace('\r', '\n');
    }

    /**
     * Exact WinForms context-menu date insertion from kontext_inhalt.vb:
     * space + day.month.year + space + hour:minute + space, without zero padding.
     */
    public static String legacyDateInsertText(Date when, TimeZone zone) {
        Calendar cal = Calendar.getInstance(zone == null ? TimeZone.getDefault() : zone, Locale.GERMANY);
        cal.setTime(when == null ? new Date() : when);
        return " " + cal.get(Calendar.DAY_OF_MONTH) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR) +
                " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + " ";
    }

    /** Android keeps the same visible value but normalizes nulls/time zone handling. */
    public static String androidDateInsertText(Date when) {
        return legacyDateInsertText(when, TimeZone.getDefault());
    }
}
