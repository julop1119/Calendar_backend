package com.example.calendar.dto;

import com.example.calendar.common.NoteStatus;
import com.example.calendar.common.TransportMode;

public record NoteTrackingResponse(
        Long id,
        TransportMode transportMode,
        NoteStatus status,
        double distanceKm,
        double progressPercent,
        long elapsedSeconds,
        long remainingSeconds,
        Double currentLatitude,
        Double currentLongitude,
        double originLatitude,
        double originLongitude,
        double destinationLatitude,
        double destinationLongitude
) {
}
