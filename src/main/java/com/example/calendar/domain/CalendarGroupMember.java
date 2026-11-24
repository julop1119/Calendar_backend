package com.example.calendar.domain;

import com.example.calendar.common.MemberRole;
import com.example.calendar.common.MemberStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private CalendarGroup calendarGroup;

    @Enumerated(EnumType.STRING)
    private MemberRole role; // OWNER, MEMBER

    @Enumerated(EnumType.STRING)
    private MemberStatus status; // PENDING, ACCEPTED, DECLINED

    private LocalDateTime createdAt;

    private CalendarGroupMember(User user, CalendarGroup calendarGroup, MemberRole role, MemberStatus status) {
        this.user = user;
        this.calendarGroup = calendarGroup;
        this.role = role;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public static CalendarGroupMember owner(User user, CalendarGroup calendarGroup) {
        return new CalendarGroupMember(user, calendarGroup, MemberRole.OWNER, MemberStatus.ACCEPTED);
    }

    public static CalendarGroupMember invite(User user, CalendarGroup calendarGroup) {
        return new CalendarGroupMember(user, calendarGroup, MemberRole.MEMBER, MemberStatus.PENDING);
    }

    public void accept() {
        this.status = MemberStatus.ACCEPTED;
    }

    public void decline() {
        this.status = MemberStatus.DECLINED;
    }

    public boolean isAccepted() {
        return this.status == MemberStatus.ACCEPTED;
    }

    public boolean isOwner() {
        return this.role == MemberRole.OWNER;
    }
}
