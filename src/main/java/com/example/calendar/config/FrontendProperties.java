package com.example.calendar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {

    private String redirectUri = "http://localhost:3000/oauth/callback";
}
