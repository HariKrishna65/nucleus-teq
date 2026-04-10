package com.example.usernotificationsystem.controller;

import com.example.usernotificationsystem.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public String notifyUser() {
        return notificationService.triggerNotification();
    }
}