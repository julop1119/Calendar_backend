package com.example.calendar.service;

import com.example.calendar.config.GooglePlacesProperties;
import com.example.calendar.dto.PlaceCandidate;
import com.example.calendar.exception.AiParseException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class GooglePlacesClient {

    private static final String FIELD_MASK =
            "places.id,places.displayName,places.formattedAddress,places.location,places.rating";

    private final GooglePlacesProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GooglePlacesClient(GooglePlacesProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public List<PlaceCandidate> search(String textQuery) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw AiParseException.invalid("GOOGLE_PLACES_API_KEY가 설정되지 않았습니다");
        }
        ObjectNode body = objectMapper.createObjectNode();
        body.put("textQuery", textQuery);
        body.put("languageCode", properties.getLanguageCode());
        body.put("maxResultCount", properties.getMaxResultCount());

        JsonNode root;
        try {
            root = restClient.post()
                    .uri(baseUrl() + "/v1/places:searchText")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Goog-Api-Key", properties.getApiKey())
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw AiParseException.upstream(providerMessage(response));
                    })
                    .body(JsonNode.class);
        } catch (AiParseException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw AiParseException.upstream("Google Places 서버에 연결하지 못했습니다");
        }

        List<PlaceCandidate> candidates = new ArrayList<>();
        JsonNode places = root == null ? null : root.path("places");
        if (places != null && places.isArray()) {
            for (JsonNode place : places) {
                candidates.add(toCandidate(place));
            }
        }
        return candidates;
    }

    private PlaceCandidate toCandidate(JsonNode place) {
        String placeId = place.path("id").asText(null);
        String name = place.path("displayName").path("text").asText(null);
        String address = place.path("formattedAddress").asText(null);
        JsonNode location = place.path("location");
        Double latitude = location.has("latitude") ? location.path("latitude").asDouble() : null;
        Double longitude = location.has("longitude") ? location.path("longitude").asDouble() : null;
        Double rating = place.has("rating") ? place.path("rating").asDouble() : null;
        return new PlaceCandidate(placeId, name, address, latitude, longitude, rating);
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
        return "Google Places 요청이 실패했습니다. status=" + response.getStatusCode().value() + ", body=" + body;
    }

    private String baseUrl() {
        String base = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }
}
