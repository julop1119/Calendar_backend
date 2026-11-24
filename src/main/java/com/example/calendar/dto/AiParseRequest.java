package com.example.calendar.dto;

import jakarta.validation.constraints.NotBlank;

public record AiParseRequest(
        @NotBlank(message = "자연어 텍스트는 필수입니다")
        String text
) {
}
