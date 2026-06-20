package com.ems.controllers;

import com.ems.services.NotificationService;
import io.javalin.http.Context;

public class NotificationController {
    private NotificationService notificationService;

    public NotificationController() {
        this.notificationService = new NotificationService();
    }

    public void getUserNotifications(Context ctx) {
        Integer userId = ctx.attribute("userId");
        if (userId != null) {
            ctx.json(notificationService.getNotificationsForUser(userId));
        } else {
            ctx.status(401).json("{\"message\": \"Unauthorized\"}");
        }
    }

    public void markAsRead(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        if (notificationService.markAsRead(id)) {
            ctx.json("{\"message\": \"Notification marked as read\"}");
        } else {
            ctx.status(400).json("{\"message\": \"Failed to update notification\"}");
        }
    }
}
