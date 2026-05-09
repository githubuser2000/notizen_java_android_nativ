package de.notizen.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import de.notizen.android.core.AlarmSpec;

public final class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIZEN_ALARM = "de.notizen.android.ACTION_NOTIZEN_ALARM";
    public static final String EXTRA_REQUEST_CODE = "request_code";
    public static final String EXTRA_NODE_TITLE = "node_title";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_START_MS = "start_ms";
    public static final String EXTRA_RECURRENCE = "recurrence";
    public static final String EXTRA_INTERVAL = "interval";
    public static final String EXTRA_WEEKDAYS = "weekdays";
    public static final String EXTRA_ENABLED = "enabled";
    private static final String CHANNEL_ID = "notizen_alarm";

    @Override public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || !ACTION_NOTIZEN_ALARM.equals(intent.getAction())) return;
        int requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
        String title = intent.getStringExtra(EXTRA_NODE_TITLE);
        String message = intent.getStringExtra(EXTRA_MESSAGE);
        showNotification(context, requestCode, title, message);

        AlarmSpec spec = new AlarmSpec(intent.getLongExtra(EXTRA_START_MS, System.currentTimeMillis()));
        spec.message = message;
        spec.recurrence = intent.getStringExtra(EXTRA_RECURRENCE);
        spec.interval = intent.getIntExtra(EXTRA_INTERVAL, 1);
        spec.enabled = intent.getBooleanExtra(EXTRA_ENABLED, true);
        spec.weekdays.addAll(AlarmSpec.parseWeekdaysCsv(intent.getStringExtra(EXTRA_WEEKDAYS)));
        AndroidAlarmScheduler.scheduleFromReceiver(context, spec, title, requestCode);
    }

    private static void showNotification(Context context, int id, String nodeTitle, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notizen-Wecker", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent content = PendingIntent.getActivity(context, id, open, flags);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, CHANNEL_ID) : new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(nodeTitle == null || nodeTitle.isEmpty() ? "Notizen-Wecker" : nodeTitle)
                .setContentText(message == null || message.isEmpty() ? "Notizen-Wecker" : message)
                .setContentIntent(content)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);
        manager.notify(id, builder.build());
    }
}
