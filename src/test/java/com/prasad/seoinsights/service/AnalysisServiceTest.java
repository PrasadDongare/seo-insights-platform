package com.prasad.seoinsights.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prasad.seoinsights.model.AnalysisResult;
import com.prasad.seoinsights.model.WebMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisServiceTest {

    private AnalysisService analysisService;

    @SuppressWarnings("unchecked")
    private final HttpClient httpClient = mock(HttpClient.class);

    @SuppressWarnings("unchecked")
    private final HttpResponse<String> httpResponse = mock(HttpResponse.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService(httpClient, objectMapper);
        ReflectionTestUtils.setField(analysisService, "apiKey", "test-api-key");
    }

    private WebMetadata sampleMetadata() {
        return new WebMetadata(
                "https://example.com",
                "Example Domain",
                "This is an example website",
                List.of("Example Heading"),
                List.of("Sub Heading"),
                true,
                true,
                0,
                5
        );
    }

    private String buildMockBody(String aiText) {
        String escaped = aiText.replace("\"", "\\\"");
        return "{\"message\": {\"content\": [{\"type\": \"text\", \"text\": \""
                + escaped + "\"}]}}";
    }

    @Test
    @DisplayName("Should parse valid AI response correctly")
    @SuppressWarnings("unchecked")
    void shouldParseValidAiResponse() throws Exception {
        String aiText = "{\"score\": 78, \"summary\": \"Good SEO\","
                + "\"strengths\": [\"Good title\"],"
                + "\"weaknesses\": [\"Missing OG\"],"
                + "\"recommendations\": [\"Add OG image\"]}";

        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buildMockBody(aiText));

        AnalysisResult result = analysisService.analyze(sampleMetadata());

        assertNotNull(result);
        assertEquals(78, result.score());
        assertEquals("Good SEO", result.summary());
        assertEquals(1, result.strengths().size());
        assertEquals(1, result.weaknesses().size());
        assertEquals(1, result.recommendations().size());
    }

    @Test
    @DisplayName("Should throw exception when API returns 429")
    @SuppressWarnings("unchecked")
    void shouldThrowExceptionOnApiError() throws Exception {
        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());
        when(httpResponse.statusCode()).thenReturn(429);
        when(httpResponse.body()).thenReturn("{\"error\": \"quota exceeded\"}");

        assertThrows(RuntimeException.class, () ->
                analysisService.analyze(sampleMetadata())
        );
    }

    @Test
    @DisplayName("Score should be between 0 and 100")
    @SuppressWarnings("unchecked")
    void scoreShouldBeBetweenZeroAndHundred() throws Exception {
        String aiText = "{\"score\": 95, \"summary\": \"Excellent\","
                + "\"strengths\": [\"Perfect\"],"
                + "\"weaknesses\": [],"
                + "\"recommendations\": []}";

        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(buildMockBody(aiText));

        AnalysisResult result = analysisService.analyze(sampleMetadata());

        assertTrue(result.score() >= 0 && result.score() <= 100);
    }

    @Test
    @DisplayName("Should return fallback when AI returns malformed JSON")
    @SuppressWarnings("unchecked")
    void shouldReturnFallbackOnMalformedJson() throws Exception {
        doReturn(httpResponse).when(httpClient).send(any(HttpRequest.class), any());
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(
                buildMockBody("this is not valid json at all")
        );

        AnalysisResult result = analysisService.analyze(sampleMetadata());

        assertNotNull(result);
        assertNotNull(result.summary());
    }
}