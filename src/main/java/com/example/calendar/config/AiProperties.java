package com.example.calendar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "calendar.ai")
public class AiProperties {

	private String zone = "Asia/Seoul";

	private Gemini gemini = new Gemini();

	@Getter
	@Setter
	public static class Gemini {

		private String apiKey = "";

		private String model = "gemini-3.5-flash-lite";

		private String baseUrl = "https://generativelanguage.googleapis.com";

	}

}
