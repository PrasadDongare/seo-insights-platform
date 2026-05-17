package com.prasad.seoinsights.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seo_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SeoReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(nullable = false)
    private LocalDateTime analyzedAt;

    private int score;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_strengths", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "strength", columnDefinition = "TEXT")
    private List<String> strengths;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_weaknesses", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "weakness", columnDefinition = "TEXT")
    private List<String> weaknesses;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_recommendations", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private List<String> recommendations;

    @Column(length = 500)
    private String pageTitle;

    @Column(columnDefinition = "TEXT")
    private String metaDescription;
}