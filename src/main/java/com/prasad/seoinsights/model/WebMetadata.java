package com.prasad.seoinsights.model;

import java.util.List;
public record WebMetadata(
        String url,
        String title,
        String metaDescription,
        List<String> h1Tags,
        List<String> h2Tags,
        boolean hasCanonicalTag,
        boolean hasOgImage,
        int imagesWithoutAltCount,
        int internalLinkCount
) {}