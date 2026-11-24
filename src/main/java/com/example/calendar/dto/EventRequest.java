package com.example.calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank(message = "제목은 필수입니다")
        String title,
        String description,
        String location,
        @NotNull(message = "시작 시간은 필수입니다")
        LocalDateTime startTime,
        @NotNull(message = "종료 시간은 필수입니다")
        LocalDateTime endTime
) {
}
