package com.example.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventConfirmRequest(
        String title,
        String description,
        @NotNull(message = "시작 시간은 필수입니다")
        LocalDateTime startTime,
        @NotNull(message = "종료 시간은 필수입니다")
        LocalDateTime endTime,
        @NotBlank(message = "장소(placeId)는 필수입니다")
        String placeId,
        @NotBlank(message = "장소명은 필수입니다")
        String placeName,
        String address,
        Double latitude,
        Double longitude
) {
}
