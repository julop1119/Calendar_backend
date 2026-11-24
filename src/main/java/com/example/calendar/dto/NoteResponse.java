package com.example.calendar.dto;

import com.example.calendar.common.NoteStatus;
import com.example.calendar.common.TransportMode;

import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        UserSummaryResponse sender,
        UserSummaryResponse recipient,
        TransportMode transportMode,
        double distanceKm,
        LocalDateTime sentAt,
        NoteStatus status,
        double progressPercent,
        String content
) {
}
