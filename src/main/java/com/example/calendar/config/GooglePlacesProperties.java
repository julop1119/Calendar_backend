package com.example.calendar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.places")
public class GooglePlacesProperties {

    private String apiKey = "";

    private String baseUrl = "https://places.googleapis.com";

    private String languageCode = "ko";

    private int maxResultCount = 5;
}
