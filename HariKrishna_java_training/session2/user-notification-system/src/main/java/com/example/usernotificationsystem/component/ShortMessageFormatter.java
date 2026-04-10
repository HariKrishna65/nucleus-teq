package com.example.usernotificationsystem.component;

import org.springframework.stereotype.Component;

@Component("SHORT")
public class ShortMessageFormatter implements MessageFormatter {

    @Override
    public String formatMessage() {
        return "Short Message";
    }
}