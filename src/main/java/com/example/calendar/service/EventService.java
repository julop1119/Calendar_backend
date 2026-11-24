package com.example.calendar.service;

import com.example.calendar.domain.CalendarGroup;
import com.example.calendar.domain.CalendarGroupMember;
import com.example.calendar.domain.Event;
import com.example.calendar.domain.User;
import com.example.calendar.dto.EventRequest;
import com.example.calendar.dto.EventResponse;
import com.example.calendar.exception.EventNotFoundException;
import com.example.calendar.exception.ForbiddenException;
import com.example.calendar.repository.EventRepository;
import com.example.calendar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GroupService groupService;

    @Transactional
    public EventResponse create(Long groupId, Long userId, EventRequest request) {
        groupService.requireAcceptedMember(groupId, userId);
        validatePeriod(request.startTime(), request.endTime());
        CalendarGroup group = groupService.getGroupOrThrow(groupId);
        User creator = getUser(userId);
        Event event = Event.builder()
                .title(request.title().trim())
                .description(blankToNull(request.description()))
                .location(blankToNull(request.location()))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .calendarGroup(group)
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .build();
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional
    public EventResponse createFromPlace(
            Long groupId,
            Long userId,
            String title,
            String description,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String placeId,
            String placeName,
            String address,
            Double latitude,
            Double longitude
    ) {
        groupService.requireAcceptedMember(groupId, userId);
        validatePeriod(startTime, endTime);
        CalendarGroup group = groupService.getGroupOrThrow(groupId);
        User creator = getUser(userId);
        Event event = Event.builder()
                .title(blankToNull(title) != null ? title.trim() : placeName)
                .description(blankToNull(description))
                .location(address != null ? address : placeName)
                .placeId(placeId)
                .latitude(latitude)
                .longitude(longitude)
                .startTime(startTime)
                .endTime(endTime)
                .calendarGroup(group)
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .build();
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> list(Long groupId, Long userId, LocalDateTime from, LocalDateTime to) {
        groupService.requireAcceptedMember(groupId, userId);
        List<Event> events = (from != null && to != null)
                ? eventRepository.findByCalendarGroupIdAndStartTimeBetweenOrderByStartTimeAsc(groupId, from, to)
                : eventRepository.findByCalendarGroupIdOrderByStartTimeAsc(groupId);
        return events.stream().map(EventResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getById(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        groupService.requireAcceptedMember(event.getCalendarGroup().getId(), userId);
        return EventResponse.from(event);
    }

    @Transactional
    public EventResponse update(Long eventId, Long userId, EventRequest request) {
        validatePeriod(request.startTime(), request.endTime());
        Event event = getEvent(eventId);
        requireEditable(event, userId);
        event.setTitle(request.title().trim());
        event.setDescription(blankToNull(request.description()));
        event.setLocation(blankToNull(request.location()));
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());
        return EventResponse.from(event);
    }

    @Transactional
    public void delete(Long eventId, Long userId) {
        Event event = getEvent(eventId);
        requireEditable(event, userId);
        eventRepository.delete(event);
    }

    private void requireEditable(Event event, Long userId) {
        CalendarGroupMember member = groupService.requireAcceptedMember(event.getCalendarGroup().getId(), userId);
        boolean isCreator = event.getCreator().getId().equals(userId);
        if (!isCreator && !member.isOwner()) {
            throw new ForbiddenException("일정을 수정하거나 삭제할 권한이 없습니다");
        }
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + userId));
    }

    private void validatePeriod(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 빠를 수 없습니다");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
