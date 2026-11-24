package com.example.calendar.dto;

import com.example.calendar.domain.User;

public record UserSummaryResponse(
        Long id,
        String email,
        String name,
        String profileImageUrl
) {

    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl());
    }
}
