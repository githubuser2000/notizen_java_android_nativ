package de.notizen.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.notizen.android.core.AlarmSpec;
import de.notizen.android.core.AlarmUtils;
import de.notizen.android.core.NoteNode;

public final class AndroidAlarmScheduler {
    private AndroidAlarmScheduler() {}

    public static boolean scheduleNodeAlarm(Context context, NoteNode node) {
        AlarmSpec spec = AlarmUtils.fromNode(node);
        if (context == null || spec == null) return false;
        long next = AlarmUtils.nextOccurrenceMillis(spec, System.currentTimeMillis(), java.util.TimeZone.getDefault());
        if (next == AlarmUtils.NO_OCCURRENCE) return false;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return false;
        PendingIntent pi = pendingIntent(context, node, spec, requestCodeFor(node, spec));
        if (Build.VERSION.SDK_INT >= 23) manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi);
        else manager.set(AlarmManager.RTC_WAKEUP, next, pi);
        return true;
    }

    public static void cancelNodeAlarm(Context context, NoteNode node) {
        AlarmSpec spec = AlarmUtils.fromNode(node);
        if (context == null || spec == null) return;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        manager.cancel(pendingIntent(context, node, spec, requestCodeFor(node, spec)));
    }

    static void scheduleFromReceiver(Context context, AlarmSpec spec, String title, int requestCode) {
        if (context == null || spec == null) return;
        long next = AlarmUtils.nextOccurrenceMillis(spec, System.currentTimeMillis(), java.util.TimeZone.getDefault());
        if (next == AlarmUtils.NO_OCCURRENCE) return;
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        PendingIntent pi = pendingIntent(context, title, spec, requestCode);
        if (Build.VERSION.SDK_INT >= 23) manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi);
        else manager.set(AlarmManager.RTC_WAKEUP, next, pi);
    }

    private static PendingIntent pendingIntent(Context context, NoteNode node, AlarmSpec spec, int requestCode) {
        return pendingIntent(context, node == null ? "Notiz" : node.title, spec, requestCode);
    }

    private static PendingIntent pendingIntent(Context context, String nodeTitle, AlarmSpec spec, int requestCode) {
        AlarmSpec n = spec.copyNormalized();
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_NOTIZEN_ALARM);
        intent.putExtra(AlarmReceiver.EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(AlarmReceiver.EXTRA_NODE_TITLE, nodeTitle == null || nodeTitle.isEmpty() ? "Notiz" : nodeTitle);
        intent.putExtra(AlarmReceiver.EXTRA_MESSAGE, n.message);
        intent.putExtra(AlarmReceiver.EXTRA_START_MS, n.startMillis);
        intent.putExtra(AlarmReceiver.EXTRA_RECURRENCE, n.recurrence);
        intent.putExtra(AlarmReceiver.EXTRA_INTERVAL, n.interval);
        intent.putExtra(AlarmReceiver.EXTRA_WEEKDAYS, n.weekdaysCsv());
        intent.putExtra(AlarmReceiver.EXTRA_ENABLED, n.enabled);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    private static int requestCodeFor(NoteNode node, AlarmSpec spec) {
        String title = node == null || node.title == null ? "" : node.title;
        AlarmSpec n = spec.copyNormalized();
        return (title + "|" + n.startMillis + "|" + n.recurrence + "|" + n.message).hashCode();
    }
}
