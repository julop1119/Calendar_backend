package com.example.calendar.repository;

import com.example.calendar.common.MemberStatus;
import com.example.calendar.domain.CalendarGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarGroupMemberRepository extends JpaRepository<CalendarGroupMember, Long> {

    Optional<CalendarGroupMember> findByCalendarGroupIdAndUserId(Long calendarGroupId, Long userId);

    List<CalendarGroupMember> findByCalendarGroupId(Long calendarGroupId);

    List<CalendarGroupMember> findByUserIdAndStatus(Long userId, MemberStatus status);

    long countByUserId(Long userId);

    boolean existsByCalendarGroupIdAndUserId(Long calendarGroupId, Long userId);
}
