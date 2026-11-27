package com.example.calendar.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * 모바일 앱(시스템 브라우저)에서 시작된 로그인인지 구분하기 위해,
 * /oauth2/authorization/google?platform=android 요청의 platform 파라미터를
 * 세션에 저장해 둔다. Google 콜백 이후 OAuth2LoginSuccessHandler가 이 값을
 * 읽어서 최종 리다이렉트 대상을 웹/앱으로 분기한다.
 */
@Component
public class PlatformAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final String SESSION_ATTRIBUTE = "oauth_platform";

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public PlatformAwareAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        capturePlatform(request);
        return delegate.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        capturePlatform(request);
        return delegate.resolve(request, registrationId);
    }

    private void capturePlatform(HttpServletRequest request) {
        String platform = request.getParameter("platform");
        if (platform != null && !platform.isBlank()) {
            request.getSession().setAttribute(SESSION_ATTRIBUTE, platform);
        }
    }
}
