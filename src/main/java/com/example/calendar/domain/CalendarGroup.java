package com.example.calendar.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class CalendarGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 예: "우리 커플 캘린더", "팀 프로젝트"

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "calendarGroup", cascade = CascadeType.ALL)
    private List<CalendarGroupMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "calendarGroup", cascade = CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    public CalendarGroup(String title) {
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }
}
