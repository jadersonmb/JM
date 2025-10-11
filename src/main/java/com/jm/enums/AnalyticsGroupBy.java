package com.jm.enums;

import java.util.Locale;

public enum AnalyticsGroupBy {
    DAY,
    WEEK,
    MONTH;

    public static AnalyticsGroupBy from(String value) {
        if (value == null || value.isBlank()) {
            return DAY;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (AnalyticsGroupBy groupBy : values()) {
            if (groupBy.name().equals(normalized)) {
                return groupBy;
            }
        }
        return DAY;
    }
}
