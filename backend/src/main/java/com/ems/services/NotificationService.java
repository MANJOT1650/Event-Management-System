package com.ems.services;

import com.ems.models.Notification;
import com.ems.repositories.NotificationRepository;

import java.util.List;

public class NotificationService {
    private NotificationRepository notificationRepository;

    public NotificationService() {
        this.notificationRepository = new NotificationRepository();
    }

    public List<Notification> getNotificationsForUser(int userId) {
        return notificationRepository.getNotificationsForUser(userId);
    }

    public boolean markAsRead(int notificationId) {
        return notificationRepository.markAsRead(notificationId);
    }
}
