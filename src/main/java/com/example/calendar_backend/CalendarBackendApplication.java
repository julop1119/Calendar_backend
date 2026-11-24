package com.example.calendar_backend;

import com.example.calendar.config.AiProperties;
import com.example.calendar.config.DotenvLoader;
import com.example.calendar.config.FrontendProperties;
import com.example.calendar.config.GooglePlacesProperties;
import com.example.calendar.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
		"com.example.calendar_backend",
		"com.example.calendar"
})
@EntityScan(basePackages = "com.example.calendar")
@EnableJpaRepositories(basePackages = "com.example.calendar")
@EnableConfigurationProperties({AiProperties.class, JwtProperties.class, FrontendProperties.class, GooglePlacesProperties.class})
public class CalendarBackendApplication {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(CalendarBackendApplication.class, args);
	}

}
