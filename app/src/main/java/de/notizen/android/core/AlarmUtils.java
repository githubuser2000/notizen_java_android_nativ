package de.notizen.android.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/** Recurrence calculations and ALX attribute bridge for Notizen reminders. */
public final class AlarmUtils {
    public static final long NO_OCCURRENCE = -1L;

    public static final String ATTR_ALARM_ENABLED = "notizen_alarm_enabled";
    public static final String ATTR_ALARM_START = "notizen_alarm_start_ms";
    public static final String ATTR_ALARM_MESSAGE = "notizen_alarm_message";
    public static final String ATTR_ALARM_RECURRENCE = "notizen_alarm_recurrence";
    public static final String ATTR_ALARM_INTERVAL = "notizen_alarm_interval";
    public static final String ATTR_ALARM_WEEKDAYS = "notizen_alarm_weekdays";

    /** Old WinForms checkbox names from wecker.vb, ordered Monday..Sunday. */
    public static final String[] LEGACY_WECKER_WEEKDAY_CHECKBOXES = new String[]{
            "CheckBox15", "CheckBox12", "CheckBox9", "CheckBox14", "CheckBox11", "CheckBox10", "CheckBox13"
    };

    private AlarmUtils() {}

    public static AlarmSpec fromNode(NoteNode node) {
        if (node == null || !node.extraAttrs.containsKey(ATTR_ALARM_START)) return null;
        try {
            AlarmSpec spec = new AlarmSpec(Long.parseLong(node.extraAttrs.get(ATTR_ALARM_START)));
            spec.message = node.extraAttrs.get(ATTR_ALARM_MESSAGE);
            spec.recurrence = node.extraAttrs.get(ATTR_ALARM_RECURRENCE);
            try { spec.interval = Integer.parseInt(node.extraAttrs.get(ATTR_ALARM_INTERVAL)); } catch (Exception ignored) { spec.interval = 1; }
            spec.enabled = !"false".equalsIgnoreCase(node.extraAttrs.get(ATTR_ALARM_ENABLED));
            spec.weekdays.addAll(AlarmSpec.parseWeekdaysCsv(node.extraAttrs.get(ATTR_ALARM_WEEKDAYS)));
            return spec.copyNormalized();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void writeToNode(NoteNode node, AlarmSpec spec) {
        if (node == null) return;
        if (spec == null) {
            clearFromNode(node);
            return;
        }
        AlarmSpec n = spec.copyNormalized();
        node.extraAttrs.put(ATTR_ALARM_ENABLED, Boolean.toString(n.enabled));
        node.extraAttrs.put(ATTR_ALARM_START, Long.toString(n.startMillis));
        node.extraAttrs.put(ATTR_ALARM_MESSAGE, n.message);
        node.extraAttrs.put(ATTR_ALARM_RECURRENCE, n.recurrence);
        node.extraAttrs.put(ATTR_ALARM_INTERVAL, Integer.toString(n.interval));
        node.extraAttrs.put(ATTR_ALARM_WEEKDAYS, n.weekdaysCsv());
    }

    public static void clearFromNode(NoteNode node) {
        if (node == null) return;
        node.extraAttrs.remove(ATTR_ALARM_ENABLED);
        node.extraAttrs.remove(ATTR_ALARM_START);
        node.extraAttrs.remove(ATTR_ALARM_MESSAGE);
        node.extraAttrs.remove(ATTR_ALARM_RECURRENCE);
        node.extraAttrs.remove(ATTR_ALARM_INTERVAL);
        node.extraAttrs.remove(ATTR_ALARM_WEEKDAYS);
    }

    public static long nextOccurrenceMillis(AlarmSpec spec, long afterMillis, TimeZone zone) {
        if (spec == null) return NO_OCCURRENCE;
        AlarmSpec n = spec.copyNormalized();
        if (!n.enabled) return NO_OCCURRENCE;
        TimeZone tz = zone == null ? TimeZone.getDefault() : zone;
        long after = floorSecond(afterMillis);
        long start = floorSecond(n.startMillis);
        if (AlarmSpec.RECURRENCE_NONE.equals(n.recurrence)) return start > after ? start : NO_OCCURRENCE;
        if (start > after) return start;

        if (AlarmSpec.RECURRENCE_DAILY.equals(n.recurrence)) {
            return addUntilAfter(start, after, Calendar.DAY_OF_YEAR, n.interval, tz, 20000);
        }
        if (AlarmSpec.RECURRENCE_MONTHLY.equals(n.recurrence)) {
            return addUntilAfter(start, after, Calendar.MONTH, n.interval, tz, 2400);
        }
        if (AlarmSpec.RECURRENCE_YEARLY.equals(n.recurrence)) {
            return addUntilAfter(start, after, Calendar.YEAR, n.interval, tz, 400);
        }
        if (AlarmSpec.RECURRENCE_WEEKLY.equals(n.recurrence)) {
            return nextWeekly(n, after, tz);
        }
        return NO_OCCURRENCE;
    }

    private static long addUntilAfter(long start, long after, int field, int interval, TimeZone tz, int maxSteps) {
        Calendar c = Calendar.getInstance(tz, Locale.ROOT);
        c.setTimeInMillis(start);
        for (int i = 0; i < maxSteps && c.getTimeInMillis() <= after; i++) c.add(field, interval);
        long value = c.getTimeInMillis();
        return value > after ? value : NO_OCCURRENCE;
    }

    private static long nextWeekly(AlarmSpec spec, long after, TimeZone tz) {
        ArrayList<Integer> days = new ArrayList<>(spec.weekdays);
        if (days.isEmpty()) days.add(toMondayZero(calendarWeekday(spec.startMillis, tz)));
        long cursor = beginningOfDay(after, tz);
        Calendar time = Calendar.getInstance(tz, Locale.ROOT);
        time.setTimeInMillis(spec.startMillis);
        Calendar candidate = Calendar.getInstance(tz, Locale.ROOT);
        Calendar anchor = Calendar.getInstance(tz, Locale.ROOT);
        anchor.setTimeInMillis(spec.startMillis);
        setStartOfDay(anchor);
        while (toMondayZero(anchor.get(Calendar.DAY_OF_WEEK)) != 0) anchor.add(Calendar.DAY_OF_YEAR, -1);
        for (int offset = 0; offset < Math.max(3700, spec.interval * 7 * 32); offset++) {
            candidate.setTimeInMillis(cursor);
            candidate.add(Calendar.DAY_OF_YEAR, offset);
            candidate.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            candidate.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            candidate.set(Calendar.SECOND, time.get(Calendar.SECOND));
            candidate.set(Calendar.MILLISECOND, 0);
            int mondayZero = toMondayZero(candidate.get(Calendar.DAY_OF_WEEK));
            if (!days.contains(mondayZero)) continue;
            long weeks = (beginningOfDay(candidate.getTimeInMillis(), tz) - anchor.getTimeInMillis()) / (7L * 24L * 60L * 60L * 1000L);
            if (weeks < 0 || weeks % spec.interval != 0) continue;
            long value = candidate.getTimeInMillis();
            if (value > after && value >= spec.startMillis) return value;
        }
        return NO_OCCURRENCE;
    }

    private static int calendarWeekday(long millis, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz, Locale.ROOT);
        c.setTimeInMillis(millis);
        return c.get(Calendar.DAY_OF_WEEK);
    }

    private static int toMondayZero(int calendarDayOfWeek) {
        // Calendar: Sunday=1, Monday=2 ... Saturday=7. Wanted: Monday=0 ... Sunday=6.
        return (calendarDayOfWeek + 5) % 7;
    }

    private static long beginningOfDay(long millis, TimeZone tz) {
        Calendar c = Calendar.getInstance(tz, Locale.ROOT);
        c.setTimeInMillis(millis);
        setStartOfDay(c);
        return c.getTimeInMillis();
    }

    private static void setStartOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private static long floorSecond(long millis) {
        return millis - Math.floorMod(millis, 1000L);
    }

    public static String describeRecurrence(AlarmSpec spec) {
        if (spec == null) return "kein Wecker";
        AlarmSpec n = spec.copyNormalized();
        if (!n.enabled) return "deaktiviert";
        if (AlarmSpec.RECURRENCE_NONE.equals(n.recurrence)) return "einmalig";
        String label;
        if (AlarmSpec.RECURRENCE_DAILY.equals(n.recurrence)) label = "täglich";
        else if (AlarmSpec.RECURRENCE_WEEKLY.equals(n.recurrence)) label = "wöchentlich";
        else if (AlarmSpec.RECURRENCE_MONTHLY.equals(n.recurrence)) label = "monatlich";
        else label = "jährlich";
        if (n.interval > 1) label += " alle " + n.interval;
        if (AlarmSpec.RECURRENCE_WEEKLY.equals(n.recurrence) && !n.weekdays.isEmpty()) {
            String[] names = {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
            StringBuilder b = new StringBuilder(label).append(" (");
            for (int i = 0; i < n.weekdays.size(); i++) {
                if (i > 0) b.append(", ");
                b.append(names[n.weekdays.get(i)]);
            }
            b.append(')');
            label = b.toString();
        }
        return label;
    }

    public static int legacyWeckerWeekdayForCheckbox(String checkboxName) {
        if (checkboxName == null) return -1;
        for (int i = 0; i < LEGACY_WECKER_WEEKDAY_CHECKBOXES.length; i++) {
            if (LEGACY_WECKER_WEEKDAY_CHECKBOXES[i].equalsIgnoreCase(checkboxName.trim())) return i;
        }
        return -1;
    }

    public static String[] legacyWeckerWeekdayLabels() {
        return new String[]{"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};
    }

    public static String legacyWeckerIntervalUnit(String recurrence) {
        if (AlarmSpec.RECURRENCE_DAILY.equals(recurrence)) return "Tage";
        if (AlarmSpec.RECURRENCE_WEEKLY.equals(recurrence)) return "Wochen";
        if (AlarmSpec.RECURRENCE_MONTHLY.equals(recurrence)) return "Monate";
        if (AlarmSpec.RECURRENCE_YEARLY.equals(recurrence)) return "Jahre";
        return "einmalig";
    }

    public static String formatMillis(long millis) {
        if (millis == NO_OCCURRENCE) return "nie";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY).format(new java.util.Date(millis));
    }
}
