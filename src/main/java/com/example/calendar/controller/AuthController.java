package com.example.calendar.controller;

import com.example.calendar.dto.UserSummaryResponse;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserSummaryResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserSummaryResponse.from(userRepository.findById(principal.id())
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + principal.id())));
    }
}
