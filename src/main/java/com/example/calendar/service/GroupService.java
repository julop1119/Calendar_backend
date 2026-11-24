package com.example.calendar.service;

import com.example.calendar.common.MemberRole;
import com.example.calendar.common.MemberStatus;
import com.example.calendar.domain.CalendarGroup;
import com.example.calendar.domain.CalendarGroupMember;
import com.example.calendar.domain.User;
import com.example.calendar.dto.GroupResponse;
import com.example.calendar.dto.MemberResponse;
import com.example.calendar.exception.ForbiddenException;
import com.example.calendar.exception.GroupNotFoundException;
import com.example.calendar.repository.CalendarGroupMemberRepository;
import com.example.calendar.repository.CalendarGroupRepository;
import com.example.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final CalendarGroupRepository calendarGroupRepository;
    private final CalendarGroupMemberRepository calendarGroupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse createGroup(Long userId, String title) {
        User user = getUser(userId);
        CalendarGroup group = calendarGroupRepository.save(new CalendarGroup(title.trim()));
        calendarGroupMemberRepository.save(CalendarGroupMember.owner(user, group));
        return GroupResponse.of(group, MemberRole.OWNER, 1);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> myGroups(Long userId) {
        return calendarGroupMemberRepository.findByUserIdAndStatus(userId, MemberStatus.ACCEPTED)
                .stream()
                .map(member -> GroupResponse.of(
                        member.getCalendarGroup(),
                        member.getRole(),
                        calendarGroupMemberRepository.findByCalendarGroupId(member.getCalendarGroup().getId()).size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> members(Long groupId, Long requesterId) {
        requireAcceptedMember(groupId, requesterId);
        return calendarGroupMemberRepository.findByCalendarGroupId(groupId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CalendarGroupMember requireAcceptedMember(Long groupId, Long userId) {
        CalendarGroupMember member = calendarGroupMemberRepository.findByCalendarGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ForbiddenException("이 캘린더에 접근할 권한이 없습니다"));
        if (!member.isAccepted()) {
            throw new ForbiddenException("이 캘린더에 접근할 권한이 없습니다");
        }
        return member;
    }

    @Transactional(readOnly = true)
    public CalendarGroup getGroupOrThrow(Long groupId) {
        return calendarGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + userId));
    }
}
