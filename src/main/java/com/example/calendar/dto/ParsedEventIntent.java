package com.example.calendar.dto;

import java.time.LocalDateTime;

public record ParsedEventIntent(
        String title,
        String searchQuery,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
