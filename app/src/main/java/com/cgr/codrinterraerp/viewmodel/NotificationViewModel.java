package com.cgr.codrinterraerp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.cgr.codrinterraerp.db.entities.PushNotifications;
import com.cgr.codrinterraerp.repository.NotificationRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotificationViewModel extends ViewModel {

    private final NotificationRepository notificationRepository;
    private final LiveData<List<PushNotifications>> notificationList;
    private final MutableLiveData<String> filterType = new MutableLiveData<>("ALL");

    @Inject
    public NotificationViewModel(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;

        notificationList =
                Transformations.switchMap(filterType, type -> {

                    if ("ALL".equalsIgnoreCase(type)) {
                        return this.notificationRepository.getAllNotifications();
                    }

                    return this.notificationRepository.getAllNotificationsByType(type);
                });
    }

    public LiveData<List<PushNotifications>> getNotificationList() {
        return notificationList;
    }

    public void setFilter(String type) {
        filterType.setValue(type);
    }

    public void clearNotification(String type) {
        notificationRepository.clearNotificationsByType(type);
    }

    public void clearNotification(int id, long createdAt) {
        notificationRepository.clearNotification(id, createdAt);
    }
}