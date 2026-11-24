package com.example.calendar.security;

import com.example.calendar.config.FrontendProperties;
import com.example.calendar.domain.CalendarGroup;
import com.example.calendar.domain.CalendarGroupMember;
import com.example.calendar.domain.User;
import com.example.calendar.repository.CalendarGroupMemberRepository;
import com.example.calendar.repository.CalendarGroupRepository;
import com.example.calendar.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CalendarGroupRepository calendarGroupRepository;
    private final CalendarGroupMemberRepository calendarGroupMemberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FrontendProperties frontendProperties;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String googleId = oidcUser.getAttribute("sub");

        User user = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new IllegalStateException("로그인 처리 중 사용자를 찾지 못했습니다: " + googleId));

        ensureDefaultCalendar(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getName());
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendProperties.getRedirectUri())
                .queryParam("token", token)
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    private void ensureDefaultCalendar(User user) {
        if (calendarGroupMemberRepository.countByUserId(user.getId()) > 0) {
            return;
        }
        CalendarGroup defaultGroup = calendarGroupRepository.save(new CalendarGroup(user.getName() + "의 캘린더"));
        calendarGroupMemberRepository.save(CalendarGroupMember.owner(user, defaultGroup));
    }
}
