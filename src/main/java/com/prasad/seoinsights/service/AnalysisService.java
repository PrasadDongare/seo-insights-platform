package com.prasad.seoinsights.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prasad.seoinsights.model.AnalysisResult;
import com.prasad.seoinsights.model.WebMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    @Value("${cohere.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.cohere.com/v2/chat";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AnalysisService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }
    public AnalysisResult analyze(WebMetadata metadata) throws Exception {
        log.info("Calling Cohere Chat API...");

        String prompt = buildPrompt(metadata);

        String requestBody = objectMapper.writeValueAsString(
                objectMapper.createObjectNode()
                        .put("model", "command-a-03-2025")
                        .<com.fasterxml.jackson.databind.node.ObjectNode>set("messages",
                                objectMapper.createArrayNode()
                                        .add(objectMapper.createObjectNode()
                                                .put("role", "user")
                                                .put("content", prompt)))
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        log.info("Cohere responded with status: {}", response.statusCode());

        if (response.statusCode() != 200) {
            log.error("Cohere error: {}", response.body());
            throw new RuntimeException("Cohere API error: " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String aiText = root
                .path("message")
                .path("content").get(0)
                .path("text")
                .asText();

        log.info("Parsing AI response...");
        return parseAiResponse(aiText);
    }

    private String buildPrompt(WebMetadata metadata) {
        return """
                You are a professional SEO analyst. Analyze this website metadata and return a JSON SEO audit.

                Website Data:
                - URL: %s
                - Title: "%s" (%d chars)
                - Meta Description: "%s" (%d chars)
                - H1 Tags: %d found → %s
                - H2 Tags: %d found → %s
                - Canonical Tag: %s
                - OG Image: %s
                - Images Without Alt Text: %d
                - Internal Links: %d

                Return ONLY this JSON structure, no markdown, no extra text:
                {
                  "score": <0-100>,
                  "summary": "<overall assessment>",
                  "strengths": ["<item>", "<item>"],
                  "weaknesses": ["<item>", "<item>"],
                  "recommendations": ["<fix>", "<fix>"]
                }
                """.formatted(
                metadata.url(),
                metadata.title(), metadata.title().length(),
                metadata.metaDescription(), metadata.metaDescription().length(),
                metadata.h1Tags().size(), metadata.h1Tags(),
                metadata.h2Tags().size(), metadata.h2Tags(),
                metadata.hasCanonicalTag() ? "Yes" : "No",
                metadata.hasOgImage() ? "Yes" : "No",
                metadata.imagesWithoutAltCount(),
                metadata.internalLinkCount()
        );
    }

    private AnalysisResult parseAiResponse(String jsonText) {
        try {
            String clean = jsonText
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            int start = clean.indexOf("{");
            int end = clean.lastIndexOf("}");
            if (start != -1 && end != -1) {
                clean = clean.substring(start, end + 1);
            }

            JsonNode root = objectMapper.readTree(clean);
            return new AnalysisResult(
                    root.path("score").asInt(50),
                    root.path("summary").asText("Analysis complete."),
                    parseStringArray(root.path("strengths")),
                    parseStringArray(root.path("weaknesses")),
                    parseStringArray(root.path("recommendations"))
            );
        } catch (Exception e) {
            log.error("Failed to parse response: {}", e.getMessage());
            return new AnalysisResult(0, "Parse error.",
                    List.of(), List.of("Could not parse response."), List.of());
        }
    }

    private List<String> parseStringArray(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) node.forEach(n -> list.add(n.asText()));
        return list;
    }
}