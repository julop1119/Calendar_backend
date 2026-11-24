package com.example.calendar.repository;

import com.example.calendar.domain.CalendarGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarGroupRepository extends JpaRepository<CalendarGroup, Long> {
}
