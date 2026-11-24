package com.example.calendar.controller;

import com.example.calendar.dto.EventRequest;
import com.example.calendar.dto.EventResponse;
import com.example.calendar.security.UserPrincipal;
import com.example.calendar.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/api/groups/{groupId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestBody EventRequest request
    ) {
        return eventService.create(groupId, principal.id(), request);
    }

    @GetMapping("/api/groups/{groupId}/events")
    public List<EventResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return eventService.list(groupId, principal.id(), from, to);
    }

    @GetMapping("/api/events/{eventId}")
    public EventResponse getById(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long eventId) {
        return eventService.getById(eventId, principal.id());
    }

    @PutMapping("/api/events/{eventId}")
    public EventResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest request
    ) {
        return eventService.update(eventId, principal.id(), request);
    }

    @DeleteMapping("/api/events/{eventId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long eventId) {
        eventService.delete(eventId, principal.id());
    }
}
