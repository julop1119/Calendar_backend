package com.example.calendar.dto;

import com.example.calendar.common.MemberRole;
import com.example.calendar.domain.CalendarGroup;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String title,
        MemberRole myRole,
        int memberCount,
        LocalDateTime createdAt
) {

    public static GroupResponse of(CalendarGroup group, MemberRole myRole, int memberCount) {
        return new GroupResponse(group.getId(), group.getTitle(), myRole, memberCount, group.getCreatedAt());
    }
}
