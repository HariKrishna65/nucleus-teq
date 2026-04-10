package com.example.usernotificationsystem.component;

import org.springframework.stereotype.Component;

@Component("LONG")
public class LongMessageFormatter implements MessageFormatter {

    @Override
    public String formatMessage() {
        return "This is a long detailed message.";
    }
}
