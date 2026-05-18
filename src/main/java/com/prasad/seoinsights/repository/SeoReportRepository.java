package com.prasad.seoinsights.repository;

import com.prasad.seoinsights.model.SeoReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeoReportRepository extends JpaRepository<SeoReport, Long> {
    List<SeoReport> findByUrl(String url);
    List<SeoReport> findTop10ByOrderByAnalyzedAtDesc();
}