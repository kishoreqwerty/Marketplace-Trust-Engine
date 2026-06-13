package com.offerup.trust.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offerup.trust.model.ListingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiListingAssistant {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.api-key:}")
    private String anthropicApiKey;

    private static final String ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";

    public Map<String, Object> improveListing(ListingRequest request) {
        if (anthropicApiKey == null || anthropicApiKey.isBlank()) {
            return Map.of("error", "ANTHROPIC_API_KEY not configured");
        }

        String prompt = buildPrompt(request);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", anthropicApiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> body = new HashMap<>();
            body.put("model", "claude-sonnet-4-6");
            body.put("max_tokens", 1024);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                ANTHROPIC_URL, HttpMethod.POST, entity, String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("content").get(0).path("text").asText();

            // Parse JSON from Claude response
            String jsonStr = content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1);
            return objectMapper.readValue(jsonStr, Map.class);

        } catch (Exception e) {
            log.error("Claude API call failed", e);
            return Map.of("error", "Failed to get AI suggestions: " + e.getMessage());
        }
    }

    private String buildPrompt(ListingRequest req) {
        return String.format("""
            You are a marketplace listing optimization assistant. Analyze this listing and suggest improvements to increase sales.

            Current listing:
            Title: %s
            Description: %s
            Price: $%s
            Category: %s
            Condition: %s

            Respond ONLY with a JSON object (no markdown) with these fields:
            {
              "improvedTitle": "better title here",
              "improvedDescription": "better description here",
              "pricingSuggestion": "pricing advice here",
              "trustSignals": ["signal1", "signal2"],
              "riskFlags": ["any concerning language or patterns"]
            }
            """,
            req.getTitle(),
            req.getDescription(),
            req.getPrice(),
            req.getCategory(),
            req.getCondition()
        );
    }
}
