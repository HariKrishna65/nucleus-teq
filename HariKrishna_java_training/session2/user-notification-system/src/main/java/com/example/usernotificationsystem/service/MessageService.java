package com.example.usernotificationsystem.service;

import com.example.usernotificationsystem.component.MessageFormatter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageService {

    private final Map<String, MessageFormatter> formatterMap;

    // Constructor Injection
    public MessageService(Map<String, MessageFormatter> formatterMap) {
        this.formatterMap = formatterMap;
    }

    public String getMessage(String type) {
        MessageFormatter formatter = formatterMap.get(type.toUpperCase());

        if (formatter == null) {
            throw new RuntimeException("Invalid message type");
        }

        return formatter.formatMessage();
    }
}