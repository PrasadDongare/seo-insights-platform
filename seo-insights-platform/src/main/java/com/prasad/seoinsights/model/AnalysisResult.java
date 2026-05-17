package com.prasad.seoinsights.model;

import java.util.List;

public record AnalysisResult(

        int score,
        String summary,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations
) {}