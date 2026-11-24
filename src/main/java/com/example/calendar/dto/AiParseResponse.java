package com.example.calendar.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiParseResponse(
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<PlaceCandidate> candidates
) {
}
