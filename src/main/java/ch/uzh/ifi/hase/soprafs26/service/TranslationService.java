package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslationService {

    @Value("${deepl.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String translate(String text, String targetLang) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Translation service not configured");
        }
        String cacheKey = targetLang.toUpperCase() + ":" + text;
        return cache.computeIfAbsent(cacheKey, k -> callDeepL(text, targetLang.toUpperCase()));
    }

    private String callDeepL(String text, String targetLang) {
        try {
            String url = apiKey.endsWith(":fx")
                    ? "https://api-free.deepl.com/v2/translate"
                    : "https://api.deepl.com/v2/translate";

            String body = objectMapper.writeValueAsString(Map.of(
                    "text", List.of(text),
                    "target_lang", targetLang
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "DeepL-Auth-Key " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "DeepL API error: " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("translations").get(0).path("text").asText();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Translation failed: " + e.getMessage());
        }
    }
}
