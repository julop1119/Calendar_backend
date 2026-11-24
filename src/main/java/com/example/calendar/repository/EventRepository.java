package com.example.calendar.repository;

import com.example.calendar.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCalendarGroupIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long calendarGroupId, LocalDateTime from, LocalDateTime to);

    List<Event> findByCalendarGroupIdOrderByStartTimeAsc(Long calendarGroupId);
}
