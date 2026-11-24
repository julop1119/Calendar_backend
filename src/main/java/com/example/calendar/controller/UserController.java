package com.example.calendar.controller;

import com.example.calendar.domain.User;
import com.example.calendar.dto.LocationUpdateRequest;
import com.example.calendar.dto.UserSummaryResponse;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.UserPrincipal;
import com.example.calendar.service.InviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final InviteService inviteService;
    private final UserRepository userRepository;

    @GetMapping("/search")
    public List<UserSummaryResponse> search(@AuthenticationPrincipal UserPrincipal principal, @RequestParam String q) {
        return inviteService.searchUsers(q, principal.id());
    }

    @PutMapping("/me/location")
    @Transactional
    public void updateMyLocation(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + principal.id()));
        user.updateLocation(request.latitude(), request.longitude());
    }
}
