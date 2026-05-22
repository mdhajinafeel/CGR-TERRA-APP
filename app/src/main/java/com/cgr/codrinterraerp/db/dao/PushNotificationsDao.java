package com.cgr.codrinterraerp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.PushNotifications;

import java.util.List;

@Dao
public interface PushNotificationsDao {

    @Insert
    long insert(PushNotifications entity);

    @Query("SELECT * FROM push_notifications ORDER BY createdAt DESC")
    LiveData<List<PushNotifications>> getAll();

    @Query("SELECT * FROM push_notifications WHERE type = :type ORDER BY createdAt DESC")
    LiveData<List<PushNotifications>> getAllByType(String type);

    @Query("SELECT COUNT(*) FROM push_notifications WHERE isRead = 0")
    LiveData<Integer> getUnreadCount();

    @Query("UPDATE push_notifications SET isRead = 1")
    void markAllRead();

    @Query("DELETE FROM push_notifications")
    void clearAll();

    @Query("DELETE FROM push_notifications WHERE id = :id AND createdAt = :createdAt")
    void clearNotification(int id, long createdAt);

    @Query("DELETE FROM push_notifications WHERE type = :type")
    void clearNotificationsByType(String type);
}