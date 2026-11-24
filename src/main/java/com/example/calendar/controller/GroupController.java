package com.example.calendar.controller;

import com.example.calendar.dto.GroupRequest;
import com.example.calendar.dto.GroupResponse;
import com.example.calendar.dto.InviteRequest;
import com.example.calendar.dto.MemberResponse;
import com.example.calendar.security.UserPrincipal;
import com.example.calendar.service.GroupService;
import com.example.calendar.service.InviteService;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final InviteService inviteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(@AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody GroupRequest request) {
        return groupService.createGroup(principal.id(), request.title());
    }

    @GetMapping("/me")
    public List<GroupResponse> myGroups(@AuthenticationPrincipal UserPrincipal principal) {
        return groupService.myGroups(principal.id());
    }

    @GetMapping("/{groupId}/members")
    public List<MemberResponse> members(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long groupId) {
        return groupService.members(groupId, principal.id());
    }

    @PostMapping("/{groupId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse invite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long groupId,
            @Valid @RequestBody InviteRequest request
    ) {
        return inviteService.invite(groupId, principal.id(), request.email());
    }
}
