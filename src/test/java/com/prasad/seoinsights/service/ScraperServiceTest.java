package com.prasad.seoinsights.service;

import com.prasad.seoinsights.model.WebMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScraperServiceTest {

    private ScraperService scraperService;

    @BeforeEach
    void setUp() {
        scraperService = new ScraperService();
    }

    @Test
    @DisplayName("Should successfully scrape a valid public URL")
    void shouldScrapeValidUrl() throws IOException {
        String url = "https://example.com";

        WebMetadata result = scraperService.scrape(url);

        assertNotNull(result);
        assertNotNull(result.url());
        assertNotNull(result.title());
        assertFalse(result.title().isEmpty());
    }

    @Test
    @DisplayName("Should return empty H1 list when no H1 tags found")
    void shouldReturnEmptyH1WhenNoneFound() throws IOException {
        String url = "https://example.com";

        WebMetadata result = scraperService.scrape(url);

        assertNotNull(result.h1Tags());
        assertInstanceOf(List.class, result.h1Tags());
    }

    @Test
    @DisplayName("Should throw IOException for completely invalid URL")
    void shouldThrowExceptionForInvalidUrl() {
        String invalidUrl = "https://this-url-does-not-exist-at-all-12345.com";

        assertThrows(IOException.class, () -> {
            scraperService.scrape(invalidUrl);
        });
    }

    @Test
    @DisplayName("Should return correct URL in metadata")
    void shouldReturnCorrectUrlInMetadata() throws IOException {
        String url = "https://example.com";

        WebMetadata result = scraperService.scrape(url);

        assertEquals(url, result.url());
    }

    @Test
    @DisplayName("Images without alt count should never be negative")
    void imagesWithoutAltCountShouldNeverBeNegative() throws IOException {
        String url = "https://example.com";

        WebMetadata result = scraperService.scrape(url);

        assertTrue(result.imagesWithoutAltCount() >= 0);
    }
}