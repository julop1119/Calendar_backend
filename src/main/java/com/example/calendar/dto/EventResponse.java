package com.example.calendar.dto;

import com.example.calendar.domain.Event;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        String placeId,
        Double latitude,
        Double longitude,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Long groupId,
        UserSummaryResponse creator
) {

    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getPlaceId(),
                event.getLatitude(),
                event.getLongitude(),
                event.getStartTime(),
                event.getEndTime(),
                event.getCalendarGroup().getId(),
                UserSummaryResponse.from(event.getCreator())
        );
    }
}
