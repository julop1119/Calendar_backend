package com.example.calendar.dto;

import com.example.calendar.common.TransportMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NoteSendRequest(
        @NotNull(message = "받는 사람은 필수입니다")
        Long recipientId,
        @NotBlank(message = "쪽지 내용은 필수입니다")
        String content,
        @NotNull(message = "이동 수단은 필수입니다")
        TransportMode transportMode
) {
}
