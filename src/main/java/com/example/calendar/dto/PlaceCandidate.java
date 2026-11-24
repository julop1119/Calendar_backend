package com.example.calendar.dto;

public record PlaceCandidate(
        String placeId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        Double rating
) {
}
