package com.javatechdebasis.ecommerce.notification.controller;

import com.javatechdebasis.ecommerce.notification.entity.Notification;
import com.javatechdebasis.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.findAll();
    }
}
