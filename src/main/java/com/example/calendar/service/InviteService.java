package com.example.calendar.service;

import com.example.calendar.common.MemberStatus;
import com.example.calendar.domain.CalendarGroup;
import com.example.calendar.domain.CalendarGroupMember;
import com.example.calendar.domain.User;
import com.example.calendar.dto.MemberResponse;
import com.example.calendar.dto.UserSummaryResponse;
import com.example.calendar.exception.ForbiddenException;
import com.example.calendar.exception.UserNotFoundException;
import com.example.calendar.repository.CalendarGroupMemberRepository;
import com.example.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteService {

    private static final int SEARCH_LIMIT = 20;

    private final UserRepository userRepository;
    private final CalendarGroupMemberRepository calendarGroupMemberRepository;
    private final GroupService groupService;

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> searchUsers(String keyword, Long requesterId) {
        return userRepository.searchByKeyword(keyword, requesterId, PageRequest.of(0, SEARCH_LIMIT)).stream()
                .map(UserSummaryResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse invite(Long groupId, Long inviterId, String inviteeEmail) {
        groupService.requireAcceptedMember(groupId, inviterId);
        CalendarGroup group = groupService.getGroupOrThrow(groupId);
        User invitee = userRepository.findByEmail(inviteeEmail)
                .orElseThrow(() -> new UserNotFoundException(inviteeEmail));
        if (calendarGroupMemberRepository.existsByCalendarGroupIdAndUserId(groupId, invitee.getId())) {
            throw new IllegalArgumentException("이미 초대되었거나 캘린더에 참여 중인 사용자입니다");
        }
        CalendarGroupMember member = calendarGroupMemberRepository.save(CalendarGroupMember.invite(invitee, group));
        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> myPendingInvites(Long userId) {
        return calendarGroupMemberRepository.findByUserIdAndStatus(userId, MemberStatus.PENDING).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse respond(Long memberId, Long userId, boolean accept) {
        CalendarGroupMember member = calendarGroupMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다: " + memberId));
        if (!member.getUser().getId().equals(userId)) {
            throw new ForbiddenException("본인에게 온 초대만 응답할 수 있습니다");
        }
        if (accept) {
            member.accept();
        } else {
            member.decline();
        }
        return MemberResponse.from(member);
    }
}
