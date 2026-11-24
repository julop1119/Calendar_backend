package com.example.calendar.dto;

import jakarta.validation.constraints.NotBlank;

public record GroupRequest(
        @NotBlank(message = "캘린더 이름은 필수입니다")
        String title
) {
}
