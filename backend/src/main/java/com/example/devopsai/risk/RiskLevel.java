package com.example.devopsai.risk;

import java.util.Locale;

public enum RiskLevel {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int priority;

    RiskLevel(int priority) {
        this.priority = priority;
    }

    public boolean higherThan(RiskLevel other) {
        return priority > other.priority;
    }

    public static RiskLevel parse(String value) {
        if (value == null || value.isBlank()) {
            return LOW;
        }
        try {
            return RiskLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return LOW;
        }
    }

    public static String max(String left, String right) {
        var leftLevel = parse(left);
        var rightLevel = parse(right);
        return rightLevel.higherThan(leftLevel) ? rightLevel.name() : leftLevel.name();
    }
}
