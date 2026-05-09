package de.notizen.android.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Testable Java model for the old WinForms wecker.vb dialog.
 *
 * RadioButton1 = once, RadioButton2 = daily, RadioButton3 = weekly,
 * RadioButton4 = monthly, RadioButton5 = yearly.  TextBox2 and Label6 are
 * visible for every recurring mode; the weekday checkboxes are only visible
 * for weekly reminders.
 */
public final class LegacyAlarmDialogModel {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm";
    public static final String RB_ONCE = "RadioButton1";
    public static final String RB_DAILY = "RadioButton2";
    public static final String RB_WEEKLY = "RadioButton3";
    public static final String RB_MONTHLY = "RadioButton4";
    public static final String RB_YEARLY = "RadioButton5";

    private static final String[] WEEKDAY_CHECKBOXES = {
            "CheckBox15", "CheckBox12", "CheckBox9", "CheckBox14", "CheckBox11", "CheckBox10", "CheckBox13"
    };

    private LegacyAlarmDialogModel() {}

    public static final class ViewState {
        public final boolean enabled;
        public final String dateText;
        public final String message;
        public final String recurrence;
        public final String radioButton;
        public final boolean intervalVisible;
        public final String intervalText;
        public final String unitLabel;
        public final boolean weekdaysVisible;
        public final Map<String, Boolean> weekdayCheckboxes;
        public final String weekdaysCsv;
        public final String nextText;

        public ViewState(boolean enabled, String dateText, String message, String recurrence, String radioButton,
                         boolean intervalVisible, String intervalText, String unitLabel, boolean weekdaysVisible,
                         Map<String, Boolean> weekdayCheckboxes, String weekdaysCsv, String nextText) {
            this.enabled = enabled;
            this.dateText = clean(dateText);
            this.message = clean(message);
            this.recurrence = AlarmSpec.normalizeRecurrence(recurrence);
            this.radioButton = clean(radioButton);
            this.intervalVisible = intervalVisible;
            this.intervalText = clean(intervalText);
            this.unitLabel = clean(unitLabel);
            this.weekdaysVisible = weekdaysVisible;
            this.weekdayCheckboxes = Collections.unmodifiableMap(new LinkedHashMap<>(weekdayCheckboxes == null ? Collections.<String, Boolean>emptyMap() : weekdayCheckboxes));
            this.weekdaysCsv = clean(weekdaysCsv);
            this.nextText = clean(nextText);
        }
    }

    public static final class ApplyResult {
        public final boolean accepted;
        public final AlarmSpec spec;
        public final String title;
        public final String message;

        public ApplyResult(boolean accepted, AlarmSpec spec, String title, String message) {
            this.accepted = accepted;
            this.spec = spec;
            this.title = clean(title);
            this.message = clean(message);
        }
    }

    public static ViewState fromSpec(AlarmSpec spec, long nowMillis, TimeZone zone) {
        AlarmSpec n = spec == null ? new AlarmSpec(nowMillis + 60L * 60L * 1000L) : spec.copyNormalized();
        String recurrence = AlarmSpec.normalizeRecurrence(n.recurrence);
        Map<String, Boolean> weekdays = checkboxesFromWeekdays(n.weekdays);
        long next = AlarmUtils.nextOccurrenceMillis(n, nowMillis, zone == null ? TimeZone.getDefault() : zone);
        return new ViewState(
                n.enabled,
                dateFormat(zone).format(new Date(n.startMillis)),
                n.message,
                recurrence,
                radioForRecurrence(recurrence),
                intervalVisible(recurrence),
                intervalVisible(recurrence) ? Integer.toString(Math.max(1, n.interval)) : "",
                unitLabel(recurrence),
                weekdaysVisible(recurrence),
                weekdays,
                n.weekdaysCsv(),
                next > 0L ? "Nächster Termin: " + AlarmUtils.formatMillis(next) : "Nächster Termin: -");
    }

    public static ViewState forRecurrence(String recurrence) {
        String r = AlarmSpec.normalizeRecurrence(recurrence);
        return new ViewState(true, "", "", r, radioForRecurrence(r), intervalVisible(r), intervalVisible(r) ? "1" : "",
                unitLabel(r), weekdaysVisible(r), checkboxesFromWeekdays(Collections.<Integer>emptyList()), "", "");
    }

    public static ApplyResult apply(boolean enabled, String dateText, String messageText, String recurrenceText,
                                    String intervalText, String weekdayCsv, TimeZone zone) {
        SimpleDateFormat fmt = dateFormat(zone);
        Date parsed;
        try {
            parsed = fmt.parse(clean(dateText));
        } catch (ParseException e) {
            return error("Wecker", "Datum bitte im Format " + DATE_PATTERN + " eingeben.");
        }
        if (parsed == null) return error("Wecker", "Datum bitte im Format " + DATE_PATTERN + " eingeben.");
        String recurrence = AlarmSpec.normalizeRecurrence(recurrenceText);
        int interval = intervalVisible(recurrence) ? parsePositiveInt(intervalText, 1) : 1;
        if (interval < 1) return error("Wecker", "Intervall muss mindestens 1 sein.");
        AlarmSpec spec = new AlarmSpec(parsed.getTime());
        spec.enabled = enabled;
        spec.message = clean(messageText).isEmpty() ? "Notizen-Wecker" : clean(messageText);
        spec.recurrence = recurrence;
        spec.interval = interval;
        if (weekdaysVisible(recurrence)) spec.weekdays.addAll(AlarmSpec.parseWeekdaysCsv(weekdayCsv));
        return new ApplyResult(true, spec.copyNormalized(), "", "");
    }

    public static ApplyResult apply(boolean enabled, String dateText, String messageText, String recurrenceText,
                                    String intervalText, Map<String, Boolean> checkboxes, TimeZone zone) {
        return apply(enabled, dateText, messageText, recurrenceText, intervalText, weekdaysCsvFromChecks(checkboxes), zone);
    }

    public static String radioForRecurrence(String recurrence) {
        String r = AlarmSpec.normalizeRecurrence(recurrence);
        if (AlarmSpec.RECURRENCE_DAILY.equals(r)) return RB_DAILY;
        if (AlarmSpec.RECURRENCE_WEEKLY.equals(r)) return RB_WEEKLY;
        if (AlarmSpec.RECURRENCE_MONTHLY.equals(r)) return RB_MONTHLY;
        if (AlarmSpec.RECURRENCE_YEARLY.equals(r)) return RB_YEARLY;
        return RB_ONCE;
    }

    public static String recurrenceForRadio(String radioButton) {
        String r = clean(radioButton);
        if (RB_DAILY.equals(r)) return AlarmSpec.RECURRENCE_DAILY;
        if (RB_WEEKLY.equals(r)) return AlarmSpec.RECURRENCE_WEEKLY;
        if (RB_MONTHLY.equals(r)) return AlarmSpec.RECURRENCE_MONTHLY;
        if (RB_YEARLY.equals(r)) return AlarmSpec.RECURRENCE_YEARLY;
        return AlarmSpec.RECURRENCE_NONE;
    }

    public static boolean intervalVisible(String recurrence) {
        return !AlarmSpec.RECURRENCE_NONE.equals(AlarmSpec.normalizeRecurrence(recurrence));
    }

    public static boolean weekdaysVisible(String recurrence) {
        return AlarmSpec.RECURRENCE_WEEKLY.equals(AlarmSpec.normalizeRecurrence(recurrence));
    }

    public static String unitLabel(String recurrence) {
        String r = AlarmSpec.normalizeRecurrence(recurrence);
        if (AlarmSpec.RECURRENCE_DAILY.equals(r)) return "Tage";
        if (AlarmSpec.RECURRENCE_WEEKLY.equals(r)) return "Wochen";
        if (AlarmSpec.RECURRENCE_MONTHLY.equals(r)) return "Monate";
        if (AlarmSpec.RECURRENCE_YEARLY.equals(r)) return "Jahre";
        return "";
    }

    public static List<String> weekdayCheckboxNames() {
        ArrayList<String> out = new ArrayList<>();
        Collections.addAll(out, WEEKDAY_CHECKBOXES);
        return Collections.unmodifiableList(out);
    }

    public static Map<String, Boolean> checkboxesFromWeekdays(List<Integer> weekdays) {
        LinkedHashMap<String, Boolean> out = new LinkedHashMap<>();
        for (String name : WEEKDAY_CHECKBOXES) out.put(name, Boolean.FALSE);
        if (weekdays != null) {
            for (Integer day : weekdays) {
                if (day != null && day >= 0 && day < WEEKDAY_CHECKBOXES.length) out.put(WEEKDAY_CHECKBOXES[day], Boolean.TRUE);
            }
        }
        return out;
    }

    public static String weekdaysCsvFromChecks(Map<String, Boolean> checks) {
        if (checks == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < WEEKDAY_CHECKBOXES.length; i++) {
            Boolean checked = checks.get(WEEKDAY_CHECKBOXES[i]);
            if (Boolean.TRUE.equals(checked)) {
                if (out.length() > 0) out.append(',');
                out.append(i);
            }
        }
        return out.toString();
    }

    public static String statusAfterSave(ApplyResult result, boolean scheduled) {
        if (result == null || !result.accepted || result.spec == null) return "Wecker nicht geändert";
        long next = AlarmUtils.nextOccurrenceMillis(result.spec, System.currentTimeMillis(), TimeZone.getDefault());
        return (scheduled ? "Wecker geplant: " : "Wecker gespeichert: ") + AlarmUtils.formatMillis(next);
    }

    private static ApplyResult error(String title, String message) { return new ApplyResult(false, null, title, message); }

    private static SimpleDateFormat dateFormat(TimeZone zone) {
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_PATTERN, Locale.GERMANY);
        fmt.setLenient(false);
        fmt.setTimeZone(zone == null ? TimeZone.getDefault() : zone);
        return fmt;
    }

    private static int parsePositiveInt(String text, int fallback) {
        try { return Math.max(1, (int) Double.parseDouble(clean(text).replace(',', '.'))); }
        catch (Exception e) { return fallback; }
    }

    private static String clean(String s) { return s == null ? "" : s.trim(); }
}
