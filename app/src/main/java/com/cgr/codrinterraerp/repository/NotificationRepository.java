package com.cgr.codrinterraerp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.cgr.codrinterraerp.db.CGRTerraERPDatabase;
import com.cgr.codrinterraerp.db.dao.PushNotificationsDao;
import com.cgr.codrinterraerp.db.entities.PushNotifications;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationRepository {

    private final PushNotificationsDao pushNotificationsDao;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NotificationRepository(Context context) {
        CGRTerraERPDatabase db = CGRTerraERPDatabase.getInstance(context);
        this.pushNotificationsDao = db.pushNotificationsDao();
    }

    public NotificationRepository(PushNotificationsDao pushNotificationsDao) {
        this.pushNotificationsDao = pushNotificationsDao;
    }

    public void insertNotification(PushNotifications pushNotifications) {
        pushNotificationsDao.insert(pushNotifications);
    }

    public LiveData<List<PushNotifications>> getAllNotifications() {
        return pushNotificationsDao.getAll();
    }

    public LiveData<List<PushNotifications>> getAllNotificationsByType(String type) {
        return pushNotificationsDao.getAllByType(type);
    }

    public void clearNotificationsByType(String type) {

        if ("ALL".equalsIgnoreCase(type)) {
            executor.execute(pushNotificationsDao::clearAll);
        } else {
            executor.execute(() ->
                     pushNotificationsDao.clearNotificationsByType(type)
            );
        }
    }

    public void clearNotification(int id, long createdAt) {
        executor.execute(() ->
                pushNotificationsDao.clearNotification(id, createdAt)
        );
    }
}