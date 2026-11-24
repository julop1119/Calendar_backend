package com.example.calendar.service;

import com.example.calendar.config.AiProperties;
import com.example.calendar.dto.ParsedEventIntent;
import com.example.calendar.exception.AiParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Component
public class AiEventIntentParser implements EventIntentParser {

	private static final String RESPONSE_SCHEMA = """
			{
			  "type": "OBJECT",
			  "properties": {
			    "title": { "type": "STRING" },
			    "searchQuery": { "type": "STRING" },
			    "startTime": { "type": "STRING", "nullable": true },
			    "endTime": { "type": "STRING", "nullable": true }
			  },
			  "required": ["title", "searchQuery", "startTime", "endTime"]
			}
			""";

	private final AiProperties properties;

	private final ObjectMapper objectMapper;

	private final java.time.Clock clock;

	private final JsonNode responseSchema;

	private final RestClient restClient;

	@Autowired
	public AiEventIntentParser(AiProperties properties, ObjectMapper objectMapper) {
		this(properties, objectMapper, java.time.Clock.systemUTC());
	}

	AiEventIntentParser(AiProperties properties, ObjectMapper objectMapper, java.time.Clock clock) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		this.clock = clock;
		this.responseSchema = objectMapper.readTree(RESPONSE_SCHEMA);
		this.restClient = RestClient.builder().build();
	}

	@Override
	public ParsedEventIntent parse(String text) {
		if (text == null || text.isBlank()) {
			throw AiParseException.invalid("자연어 텍스트는 필수입니다");
		}
		String trimmed = text.trim();
		JsonNode eventJson = callGemini(trimmed);
		return fromModel(eventJson);
	}

	ParsedEventIntent fromModel(JsonNode node) {
		if (node == null || !node.isObject()) {
			throw AiParseException.invalid("AI 응답이 일정 객체가 아닙니다");
		}
		String title = textOrNull(node.get("title"));
		if (title == null) {
			throw AiParseException.invalid("AI가 일정 제목을 만들지 못했습니다");
		}
		String searchQuery = textOrNull(node.get("searchQuery"));
		if (searchQuery == null) {
			throw AiParseException.invalid("AI가 장소 검색어를 만들지 못했습니다");
		}
		LocalDateTime[] times = resolveTimes(
				parseDateTime(node.get("startTime")),
				parseDateTime(node.get("endTime"))
		);
		return new ParsedEventIntent(truncate(title, 255), searchQuery, times[0], times[1]);
	}

	private JsonNode callGemini(String text) {
		AiProperties.Gemini config = properties.getGemini();
		requireKey(config.getApiKey());

		JsonNode root = post(
				endpoint(config.getBaseUrl(), "/v1beta/models/" + config.getModel() + ":generateContent"),
				config.getApiKey(),
				requestBody(text)
		);

		JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
		if (textNode.isMissingNode() || textNode.isNull() || (textNode.isTextual() && textNode.asText().isBlank())) {
			throw AiParseException.invalid("Gemini 응답에 일정 JSON이 없습니다");
		}
		return readJson(textNode.asText());
	}

	private ObjectNode requestBody(String text) {
		ObjectNode root = objectMapper.createObjectNode();

		ObjectNode systemInstruction = root.putObject("systemInstruction");
		systemInstruction.putArray("parts").addObject().put("text", systemPrompt());

		ObjectNode content = root.putArray("contents").addObject();
		content.put("role", "user");
		content.putArray("parts").addObject().put("text", text);

		ObjectNode generationConfig = root.putObject("generationConfig");
		generationConfig.put("temperature", 0);
		generationConfig.put("responseMimeType", "application/json");
		generationConfig.set("responseSchema", responseSchema);

		return root;
	}

	private String systemPrompt() {
		ZoneId zone = zone();
		LocalDateTime now = LocalDateTime.now(clock.withZone(zone)).withNano(0);
		return """
				너는 모임/약속 일정에서 장소 추천에 쓸 정보를 추출하는 어시스턴트다. 사용자 문장 하나에서 필드만 추출한다.
				현재 시각: %s
				시간대: %s

				필드:
				- title: 짧은 일정 제목 (예: "한식당 약속", "저녁 식사"). 비우지 않는다.
				- searchQuery: Google Places 텍스트 검색에 바로 쓸 수 있는 검색어. 문장에 나온 지역/도시/동네와 음식 종류(또는 업종)를 모두 포함해서 만든다.
				  예: "벤쿠버 다운타운에서 한식집 7시 약속" -> "Korean restaurant Downtown Vancouver"
				  지역명은 Google Places가 잘 인식하도록 영어 지명으로 바꿀 수 있으면 바꾼다. 음식/업종이 불분명하면 "restaurant"처럼 일반적인 단어를 쓴다.
				- startTime, endTime: 타임존 없는 ISO-8601 로컬 시각 yyyy-MM-ddTHH:mm:ss. 현재 시각을 기준으로 내일, 오후 3시 같은 표현을 계산한다. endTime이 명시되지 않으면 null. 날짜와 시각이 전혀 없으면 둘 다 null.

				예시: "벤쿠버 다운타운에서 한식집 7시 약속"
				title=한식당 약속, searchQuery=Korean restaurant Downtown Vancouver, startTime=오늘 또는 가까운 미래의 19:00, endTime=null
				""".formatted(now, zone);
	}

	private JsonNode post(URI uri, String apiKey, JsonNode body) {
		try {
			return restClient.post()
					.uri(uri)
					.contentType(MediaType.APPLICATION_JSON)
					.header("x-goog-api-key", apiKey)
					.body(body)
					.retrieve()
					.onStatus(HttpStatusCode::isError, (request, response) -> {
						throw AiParseException.upstream(providerMessage(response));
					})
					.body(JsonNode.class);
		} catch (AiParseException exception) {
			throw exception;
		} catch (RestClientException exception) {
			throw AiParseException.upstream("Gemini 서버에 연결하지 못했습니다");
		}
	}

	private String providerMessage(ClientHttpResponse response) throws IOException {
		String body;
		try (InputStream input = response.getBody()) {
			body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
		body = body.replaceAll("\\s+", " ").trim();
		if (body.length() > 300) {
			body = body.substring(0, 300);
		}
		return "Gemini 요청이 실패했습니다. status=" + response.getStatusCode().value() + ", body=" + body;
	}

	private JsonNode readJson(String text) {
		String trimmed = text.trim();
		if (trimmed.startsWith("```")) {
			int firstLine = trimmed.indexOf('\n');
			int lastFence = trimmed.lastIndexOf("```");
			if (firstLine >= 0 && lastFence > firstLine) {
				trimmed = trimmed.substring(firstLine + 1, lastFence).trim();
			}
		}
		try {
			return objectMapper.readTree(trimmed);
		} catch (JacksonException exception) {
			throw AiParseException.invalid("AI 응답 JSON을 읽지 못했습니다");
		}
	}

	private LocalDateTime[] resolveTimes(LocalDateTime start, LocalDateTime end) {
		if (start == null && end == null) {
			start = LocalDateTime.now(clock.withZone(zone())).withSecond(0).withNano(0);
			end = start.plusHours(1);
		} else if (start == null) {
			start = end.minusHours(1);
		} else if (end == null) {
			end = start.plusHours(1);
		}
		if (end.isBefore(start)) {
			end = start.plusHours(1);
		}
		return new LocalDateTime[] { start, end };
	}

	private LocalDateTime parseDateTime(JsonNode node) {
		String value = textOrNull(node);
		if (value == null) {
			return null;
		}
		try {
			return LocalDateTime.parse(value);
		} catch (DateTimeParseException ignored) {
			// try the next format
		}
		try {
			return OffsetDateTime.parse(value).atZoneSameInstant(zone()).toLocalDateTime();
		} catch (DateTimeParseException ignored) {
			// try the next format
		}
		try {
			return ZonedDateTime.parse(value).withZoneSameInstant(zone()).toLocalDateTime();
		} catch (DateTimeParseException ignored) {
			// try the next format
		}
		try {
			return LocalDate.parse(value).atTime(9, 0);
		} catch (DateTimeParseException exception) {
			throw AiParseException.invalid("시간을 해석하지 못했습니다: " + value);
		}
	}

	private String textOrNull(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return null;
		}
		String value = node.asText(null);
		if (value == null) {
			return null;
		}
		value = value.trim();
		if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
			return null;
		}
		return value;
	}

	private String truncate(String value, int max) {
		if (value == null || value.length() <= max) {
			return value;
		}
		return value.substring(0, max);
	}

	private ZoneId zone() {
		try {
			return ZoneId.of(properties.getZone());
		} catch (DateTimeException exception) {
			throw AiParseException.invalid("calendar.ai.zone 값이 올바르지 않습니다: " + properties.getZone());
		}
	}

	private void requireKey(String apiKey) {
		if (apiKey == null || apiKey.isBlank()) {
			throw AiParseException.invalid("GEMINI_API_KEY가 설정되지 않았습니다");
		}
	}

	private URI endpoint(String baseUrl, String path) {
		String base = baseUrl == null ? "" : baseUrl.trim();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return URI.create(base + path);
	}

}
