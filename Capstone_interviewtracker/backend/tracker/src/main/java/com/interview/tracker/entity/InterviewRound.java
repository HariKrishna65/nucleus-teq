package com.interview.tracker.entity;

/**
 * Enum representing valid interview round values.
 * Matches the DB constraint: interviews_round_check
 */
public enum InterviewRound {
    L1("L1"),
    L2("L2"),
    HR("HR");

    private final String value;

    InterviewRound(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Parse a string to InterviewRound enum.
     * Trims whitespace and is case-insensitive.
     * @param value the string to parse
     * @return the corresponding InterviewRound
     * @throws IllegalArgumentException if the value is invalid
     */
    public static InterviewRound fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Round value cannot be null or empty");
        }
        String trimmed = value.trim().toUpperCase();
        for (InterviewRound round : InterviewRound.values()) {
            if (round.name().equals(trimmed) || round.getValue().equals(trimmed)) {
                return round;
            }
        }
        throw new IllegalArgumentException(
            "Invalid round value: '" + value + "'. Valid values are: L1, L2, HR"
        );
    }

    /**
     * Check if a string is a valid round value.
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim().toUpperCase();
        for (InterviewRound round : InterviewRound.values()) {
            if (round.name().equals(trimmed) || round.getValue().equals(trimmed)) {
                return true;
            }
        }
        return false;
    }
}