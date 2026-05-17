package com.prasad.seoinsights.service;

import com.prasad.seoinsights.model.WebMetadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ScraperService {

    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    private static final int TIMEOUT_MS = 10_000;
    /**
     * @param url
     * @return
     * @throws IOException
     */
    public WebMetadata scrape(String url) throws IOException {
        log.info("Starting scrape for URL: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (compatible; SEO-Insights-Bot/1.0)")
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .get();

        log.info("Successfully fetched page: {}", doc.title());

        String title = doc.title();

        Element metaDescElement = doc.select("meta[name=description]").first();
        String metaDescription = (metaDescElement != null)
                ? metaDescElement.attr("content")
                : "";

        Elements h1Elements = doc.select("h1");
        List<String> h1Tags = h1Elements.eachText();

        Elements h2Elements = doc.select("h2");
        List<String> h2Tags = h2Elements.eachText();

        boolean hasCanonicalTag = !doc.select("link[rel=canonical]").isEmpty();

        boolean hasOgImage = !doc.select("meta[property=og:image]").isEmpty();

        Elements allImages = doc.select("img");
        long imagesWithoutAlt = allImages.stream()
                .filter(img -> img.attr("alt").trim().isEmpty())
                .count();

        String domain = extractDomain(url);
        long internalLinks = doc.select("a[href]").stream()
                .filter(a -> a.attr("abs:href").contains(domain))
                .count();

        log.info("Scrape complete. Title: '{}', H1s found: {}, Images missing alt: {}",
                title, h1Tags.size(), imagesWithoutAlt);

        return new WebMetadata(
                url,
                title,
                metaDescription,
                h1Tags,
                h2Tags,
                hasCanonicalTag,
                hasOgImage,
                (int) imagesWithoutAlt,
                (int) internalLinks
        );
    }

    private String extractDomain(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();

            return (host != null && host.startsWith("www.")) ? host.substring(4) : host;
        } catch (Exception e) {
            return url;
        }
    }
}