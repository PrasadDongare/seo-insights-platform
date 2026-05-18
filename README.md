# SEO Insights Platform

A full-stack SEO analysis tool that scrapes website metadata and generates an AI-powered audit report.

## Tech Stack
- **Backend:** Java 22, Spring Boot 3.3.1, Jsoup
- **AI:** Cohere API (command-a-03-2025)
- **Database:** Supabase (PostgreSQL)
- **Frontend:** HTML, CSS, JavaScript

## Features
- Scrapes Title, Meta Description, H1 tags, Canonical tag, OG Image, Alt text
- AI-generated SEO score out of 100
- Strengths, weaknesses and recommendations
- Report history saved to PostgreSQL
- Clean responsive dashboard UI

## Getting Started

### Prerequisites

- Java 22
- Maven
- Supabase account
- Cohere API key

### Setup

1. Clone the repository

2. Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://YOUR_SUPABASE_URL:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
cohere.api.key=YOUR_COHERE_API_KEY
```

3. Run the application

4. Open in browser -
http://localhost:8080


## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/seo/analyze?url=` | Analyze a URL |
| GET | `/api/v1/seo/history` | Get recent 10 reports |
| GET | `/api/v1/seo/reports/{id}` | Get report by ID |


## Author

Prasad Dongare — [GitHub](https://github.com/PrasadDongare)