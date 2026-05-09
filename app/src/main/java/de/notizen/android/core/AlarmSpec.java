package de.notizen.android.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Portable reminder model derived from the old wecker.vb dialog. */
public final class AlarmSpec {
    public static final String RECURRENCE_NONE = "none";
    public static final String RECURRENCE_DAILY = "daily";
    public static final String RECURRENCE_WEEKLY = "weekly";
    public static final String RECURRENCE_MONTHLY = "monthly";
    public static final String RECURRENCE_YEARLY = "yearly";

    public long startMillis;
    public String message = "Notizen-Wecker";
    public String recurrence = RECURRENCE_NONE;
    public int interval = 1;
    /** Monday=0, Sunday=6, matching Python/PyQt tests and legacy labels. */
    public final ArrayList<Integer> weekdays = new ArrayList<>();
    public boolean enabled = true;

    public AlarmSpec(long startMillis) {
        this.startMillis = startMillis;
    }

    public AlarmSpec copyNormalized() {
        AlarmSpec n = new AlarmSpec(startMillis - Math.floorMod(startMillis, 1000L));
        n.message = message == null || message.isEmpty() ? "Notizen-Wecker" : message;
        n.recurrence = normalizeRecurrence(recurrence);
        n.interval = Math.max(1, interval);
        n.enabled = enabled;
        Set<Integer> unique = new LinkedHashSet<>();
        for (Integer day : weekdays) {
            if (day != null && day >= 0 && day <= 6) unique.add(day);
        }
        n.weekdays.addAll(unique);
        Collections.sort(n.weekdays);
        return n;
    }

    public String weekdaysCsv() {
        StringBuilder out = new StringBuilder();
        AlarmSpec n = copyNormalized();
        for (int i = 0; i < n.weekdays.size(); i++) {
            if (i > 0) out.append(',');
            out.append(n.weekdays.get(i));
        }
        return out.toString();
    }

    public static List<Integer> parseWeekdaysCsv(String csv) {
        ArrayList<Integer> out = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) return out;
        for (String part : csv.split("[,;\\s]+")) {
            if (part.isEmpty()) continue;
            try {
                int value = Integer.parseInt(part.trim());
                if (value >= 0 && value <= 6 && !out.contains(value)) out.add(value);
            } catch (NumberFormatException ignored) {}
        }
        Collections.sort(out);
        return out;
    }

    public static String normalizeRecurrence(String recurrence) {
        String r = recurrence == null ? RECURRENCE_NONE : recurrence.trim().toLowerCase(java.util.Locale.ROOT);
        if (r.equals(RECURRENCE_DAILY) || r.equals(RECURRENCE_WEEKLY) || r.equals(RECURRENCE_MONTHLY) || r.equals(RECURRENCE_YEARLY)) return r;
        return RECURRENCE_NONE;
    }
}
