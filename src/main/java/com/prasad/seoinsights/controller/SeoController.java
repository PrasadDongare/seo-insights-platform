package com.prasad.seoinsights.controller;

import com.prasad.seoinsights.model.AnalysisResult;
import com.prasad.seoinsights.model.SeoReport;
import com.prasad.seoinsights.model.WebMetadata;
import com.prasad.seoinsights.repository.SeoReportRepository;
import com.prasad.seoinsights.service.AnalysisService;
import com.prasad.seoinsights.service.ScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seo")
@CrossOrigin(origins = "*")
public class SeoController {

    private static final Logger log = LoggerFactory.getLogger(SeoController.class);

    private final ScraperService scraperService;
    private final AnalysisService analysisService;
    private final SeoReportRepository reportRepository;

    public SeoController(ScraperService scraperService,
                         AnalysisService analysisService,
                         SeoReportRepository reportRepository) {
        this.scraperService = scraperService;
        this.analysisService = analysisService;
        this.reportRepository = reportRepository;
    }

    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeSeo(@RequestParam String url) {
        log.info("Analyze request for: {}", url);

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {

            WebMetadata metadata = scraperService.scrape(url);

            AnalysisResult analysis = analysisService.analyze(metadata);

            SeoReport report = SeoReport.builder()
                    .url(url)
                    .analyzedAt(LocalDateTime.now())
                    .score(analysis.score())
                    .summary(analysis.summary())
                    .strengths(analysis.strengths())
                    .weaknesses(analysis.weaknesses())
                    .recommendations(analysis.recommendations())
                    .pageTitle(metadata.title())
                    .metaDescription(metadata.metaDescription())
                    .build();

            SeoReport saved = reportRepository.save(report);
            log.info("Report saved with ID: {}", saved.getId());

            return ResponseEntity.ok(Map.of(
                    "reportId",   saved.getId(),
                    "url",        url,
                    "analyzedAt", saved.getAnalyzedAt().toString(),
                    "metadata",   metadata,
                    "analysis",   analysis
            ));

        } catch (java.io.IOException e) {
            log.error("Scraping failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Could not reach the URL. Make sure it is public and correct."));

        } catch (Exception e) {
            log.error("Analysis failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<SeoReport>> getHistory() {
        return ResponseEntity.ok(reportRepository.findTop10ByOrderByAnalyzedAtDesc());
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        return reportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}