package com.example.calendar.controller;

import com.example.calendar.dto.MemberResponse;
import com.example.calendar.security.UserPrincipal;
import com.example.calendar.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    @GetMapping("/me")
    public List<MemberResponse> myInvites(@AuthenticationPrincipal UserPrincipal principal) {
        return inviteService.myPendingInvites(principal.id());
    }

    @PostMapping("/{memberId}/accept")
    public MemberResponse accept(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long memberId) {
        return inviteService.respond(memberId, principal.id(), true);
    }

    @PostMapping("/{memberId}/decline")
    public MemberResponse decline(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long memberId) {
        return inviteService.respond(memberId, principal.id(), false);
    }
}
