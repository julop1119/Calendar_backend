package com.example.calendar.controller;

import com.example.calendar.dto.NoteResponse;
import com.example.calendar.dto.NoteSendRequest;
import com.example.calendar.dto.NoteTrackingResponse;
import com.example.calendar.security.UserPrincipal;
import com.example.calendar.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse send(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody NoteSendRequest request) {
        return noteService.send(principal.id(), request);
    }

    @GetMapping("/sent")
    public List<NoteResponse> sent(@AuthenticationPrincipal UserPrincipal principal) {
        return noteService.sent(principal.id());
    }

    @GetMapping("/received")
    public List<NoteResponse> received(@AuthenticationPrincipal UserPrincipal principal) {
        return noteService.received(principal.id());
    }

    @GetMapping("/{noteId}/tracking")
    public NoteTrackingResponse tracking(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long noteId) {
        return noteService.tracking(noteId, principal.id());
    }
}
