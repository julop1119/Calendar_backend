package com.example.calendar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {

    private String redirectUri = "http://localhost:3000/oauth/callback";

    /** 안드로이드/iOS 앱(시스템 브라우저)에서 로그인했을 때 돌아갈 커스텀 스킴 딥링크. */
    private String mobileRedirectUri = "com.example.calendar.app://oauth/callback";
}
