package com.example.todoapp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceClientTest {

    @Test
    void sendNotification_doesNotThrow() {
        NotificationServiceClient client = new NotificationServiceClient();

        assertDoesNotThrow(() -> client.sendNotification("Notification sent for new TODO"));
    }
}
