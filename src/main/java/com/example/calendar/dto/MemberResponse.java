package com.example.calendar.dto;

import com.example.calendar.common.MemberRole;
import com.example.calendar.common.MemberStatus;
import com.example.calendar.domain.CalendarGroupMember;

public record MemberResponse(
        Long id,
        UserSummaryResponse user,
        MemberRole role,
        MemberStatus status,
        Long groupId,
        String groupTitle
) {

    public static MemberResponse from(CalendarGroupMember member) {
        return new MemberResponse(
                member.getId(),
                UserSummaryResponse.from(member.getUser()),
                member.getRole(),
                member.getStatus(),
                member.getCalendarGroup().getId(),
                member.getCalendarGroup().getTitle()
        );
    }
}
