package com.cgr.codrinterraerp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.cgr.codrinterraerp.db.CGRTerraERPDatabase;
import com.cgr.codrinterraerp.db.entities.ApiLogs;
import com.cgr.codrinterraerp.helper.PreferenceManager;
import com.cgr.codrinterraerp.utils.AppLogger;
import com.cgr.codrinterraerp.utils.CommonUtils;
import com.cgr.codrinterraerp.worker.LogCleanupWorker;
import com.cgr.codrinterraerp.worker.SyncReminderWorker;
import com.cgr.codrinterraerp.worker.TransactionDataCleanupWorker;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.HiltAndroidApp;
import devliving.online.securedpreferencestore.DefaultRecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

@HiltAndroidApp
public class CodrinTerraErpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize preferences
        initSecureSharedPref();

        // Log Cleanup
        scheduleLogCleanup(this);

        // Transaction Cleanup
        scheduleReceptionCleanup(this);

        // Sync Reminder
        startSyncReminderWorker();

        // Initialize Logger
        CGRTerraERPDatabase db = CGRTerraERPDatabase.getInstance(this);

        AppLogger.init(db.apiLogsDao());

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {

            try {

                ApiLogs log = new ApiLogs();

                log.success = false;
                log.createdAt = System.currentTimeMillis();

                Throwable rootCause = throwable;

                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }

                log.exceptionType = rootCause.getClass().getSimpleName();

                log.type = CommonUtils.classifyError(rootCause);

                log.errorMessage = rootCause.getMessage() != null ? rootCause.getMessage() : rootCause.toString();

                String stackTrace = Log.getStackTraceString(rootCause);

                if (stackTrace.length() > 5000) {
                    stackTrace = stackTrace.substring(0, 5000) + "...";
                }

                log.responseBody = stackTrace;

                StackTraceElement[] trace = rootCause.getStackTrace();

                log.tag = "UNKNOWN";
                log.methodName = "UNKNOWN";

                for (StackTraceElement element : trace) {
                    String className = element.getClassName();

                    if (className.startsWith("com.cgr.codrinterraerp")) {

                        log.tag = className.substring(className.lastIndexOf(".") + 1);
                        log.methodName = element.getMethodName();
                        break;
                    }
                }

                db.apiLogsDao().insertApiLogs(log);

                FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
                crashlytics.log(log.errorMessage);
                crashlytics.setCustomKey("screen", log.tag);
                crashlytics.setCustomKey("method", log.methodName);
                crashlytics.setCustomKey("user_id", String.valueOf(PreferenceManager.INSTANCE.getUserId()));
                crashlytics.setCustomKey("name", PreferenceManager.INSTANCE.getName());

                crashlytics.recordException(rootCause);

            } catch (Exception e) {
                Log.e("CRASH_HANDLER", "Failed to save crash log", e);
            }

            // VERY IMPORTANT
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    private void initSecureSharedPref() {
        try {
            String prefName = "CGR_DIGITAL_TERRA_ERP_PREF";
            String prefix = "cgr_digital";
            byte[] seed = "cgr_digital".getBytes();
            SecuredPreferenceStore.init(getApplicationContext(), prefName, prefix, seed, new DefaultRecoveryHandler());
            SecuredPreferenceStore.setRecoveryHandler(new DefaultRecoveryHandler() {
                @Override
                protected boolean recover(Exception e, KeyStore keyStore, List<String> keyAliases, SharedPreferences preferences) {
                    return super.recover(e, keyStore, keyAliases, preferences);
                }
            });

        } catch (Exception e) {
            AppLogger.e(getClass(), "initSecureSharedPref", e);
        }
    }

    private void scheduleLogCleanup(Context context) {

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest work =
                new PeriodicWorkRequest.Builder(LogCleanupWorker.class, 12, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setInitialDelay(1, TimeUnit.HOURS) // 🔥 add this
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "log_cleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                work
        );
    }

    private void scheduleReceptionCleanup(Context context) {

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest work =
                new PeriodicWorkRequest.Builder(TransactionDataCleanupWorker.class, 1, TimeUnit.DAYS)
                        .setConstraints(constraints)
                        .setInitialDelay(2, TimeUnit.HOURS).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "transaction_cleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                work
        );
    }

    private void startSyncReminderWorker() {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(SyncReminderWorker.class, 3, TimeUnit.HOURS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("SYNC_REMINDER_WORK", ExistingPeriodicWorkPolicy.KEEP, request);
    }
}