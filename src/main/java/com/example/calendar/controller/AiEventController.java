package com.example.calendar.controller;

import com.example.calendar.dto.AiParseRequest;
import com.example.calendar.dto.AiParseResponse;
import com.example.calendar.dto.EventConfirmRequest;
import com.example.calendar.dto.EventResponse;
import com.example.calendar.security.UserPrincipal;
import com.example.calendar.service.AiEventPlannerService;
import com.example.calendar.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups/{groupId}/ai/events")
@RequiredArgsConstructor
public class AiEventController {

    private final AiEventPlannerService aiEventPlannerService;
    private final EventService eventService;

    @PostMapping("/parse")
    public AiParseResponse parse(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestBody AiParseRequest request
    ) {
        return aiEventPlannerService.parse(groupId, principal.id(), request.text());
    }

    @PostMapping("/confirm")
    public EventResponse confirm(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestBody EventConfirmRequest request
    ) {
        return eventService.createFromPlace(
                groupId,
                principal.id(),
                request.title(),
                request.description(),
                request.startTime(),
                request.endTime(),
                request.placeId(),
                request.placeName(),
                request.address(),
                request.latitude(),
                request.longitude()
        );
    }
}
