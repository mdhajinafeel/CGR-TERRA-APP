package com.cgr.codrinterraerp.worker;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.cgr.codrinterraerp.R;
import com.cgr.codrinterraerp.db.entities.PushNotifications;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.repository.NotificationRepository;
import com.cgr.codrinterraerp.repository.SyncRepository;
import com.cgr.codrinterraerp.ui.activities.MainActivity;
import com.cgr.codrinterraerp.ui.activities.SplashActivity;
import com.cgr.codrinterraerp.utils.AppLogger;

public class SyncReminderWorker extends Worker {

    private static final String CHANNEL_ID = "sync_reminder_channel";
    private final SyncRepository syncRepository;
    private final NotificationRepository notificationRepository;

    public SyncReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        syncRepository = new SyncRepository(context);
        notificationRepository = new NotificationRepository(context);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {

            // =============================
            // BATTERY CHECK
            // =============================
            if (isPowerSaveModeEnabled()) {
                return Result.success();
            }

            // =============================
            // LAST SYNC
            // =============================
            long lastSyncTime = PreferenceManager.INSTANCE.getLastSyncTime();
            long currentTime = System.currentTimeMillis();
            long diffHours = (currentTime - lastSyncTime) / (1000 * 60 * 60);

            // =============================
            // UNSYNCED DATA
            // =============================
            if (!syncRepository.hasUnsyncedData()) {
                return Result.success();
            }

            // =============================
            // PREVENT SPAM
            // =============================
            long lastReminderTime = PreferenceManager.INSTANCE.getLastReminderTime();
            long reminderDiffHours = (currentTime - lastReminderTime) / (1000 * 60 * 60);

            if (reminderDiffHours < 1) {
                return Result.success();
            }

            // =============================
            // MESSAGE
            // =============================
            String title = getApplicationContext().getString(R.string.sync_reminder);

            String message = null;
            boolean silent = true;
            String status = "INFO";

            if (diffHours >= 12) {
                message = getApplicationContext().getString(R.string.sync_overdue_please_sync_your_pending_data_immediately);
                silent = false;
                status = "CRITICAL";
            } else if (diffHours >= 6) {
                message = getApplicationContext().getString(R.string.your_data_has_not_been_synced_for_several_hours);
                status = "WARNING";
            } else if (diffHours >= 2) {
                message = getApplicationContext().getString(R.string.you_have_pending_data_waiting_for_sync);
            }

            // =============================
            // SHOW
            // =============================
            if (message != null) {

                PushNotifications pushNotifications = new PushNotifications();
                pushNotifications.title = title;
                pushNotifications.message = message;
                pushNotifications.type = "REMINDER";
                pushNotifications.status = status;
                pushNotifications.createdAt = System.currentTimeMillis();
                notificationRepository.insertNotification(pushNotifications);

                showNotification(title, message, silent);
                PreferenceManager.INSTANCE.setLastReminderTime(currentTime);
            }

            return Result.success();

        } catch (Exception e) {
            AppLogger.e(getClass(), "SyncReminder", e);
            return Result.failure();
        }
    }

    private boolean isPowerSaveModeEnabled() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        return pm != null && pm.isPowerSaveMode();
    }

    private void showNotification(String title, String message, boolean silent) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }

        // =============================
        // CHANNEL
        // =============================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getApplicationContext().getString(R.string.sync_reminder), NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // =====================================
        // INTENT
        // =====================================
        Intent intent;
        if (isAppInForeground()) {
            // APP OPEN
            intent = new Intent(getApplicationContext(), MainActivity.class);
        } else {
            // APP CLOSED/KILLED
            intent = new Intent(getApplicationContext(), SplashActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("IsSyncReminderClicked", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(),
                        intent, PendingIntent.FLAG_UPDATE_CURRENT |  PendingIntent.FLAG_IMMUTABLE);

        // =============================
        // NOTIFICATION
        // =============================
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSilent(silent)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        manager.notify(9999, builder.build());
    }

    private boolean isAppInForeground() {

        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
    }
}