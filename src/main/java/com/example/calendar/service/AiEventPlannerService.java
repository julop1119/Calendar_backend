package com.example.calendar.service;

import com.example.calendar.dto.AiParseResponse;
import com.example.calendar.dto.ParsedEventIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiEventPlannerService {

    private final EventIntentParser eventIntentParser;
    private final GooglePlacesClient googlePlacesClient;
    private final GroupService groupService;

    @Transactional(readOnly = true)
    public AiParseResponse parse(Long groupId, Long userId, String text) {
        groupService.requireAcceptedMember(groupId, userId);
        ParsedEventIntent intent = eventIntentParser.parse(text);
        var candidates = googlePlacesClient.search(intent.searchQuery());
        return new AiParseResponse(intent.title(), intent.startTime(), intent.endTime(), candidates);
    }
}
