# SEO Insights Platform

An AI-powered full-stack SEO audit platform that scrapes website metadata,
analyzes it using Cohere AI, and delivers a structured report with scores,
strengths, weaknesses, and actionable recommendations — all in under 3 seconds.

🌐 **Live Demo:** [seo-insights-platform-production.up.railway.app](https://seo-insights-platform-production.up.railway.app)


---

## What It Does

- Input any public URL
- Backend scrapes 8+ on-page SEO signals using Jsoup
- Sends metadata to Cohere AI for analysis
- Returns a structured audit report with SEO score, strengths, weaknesses, and recommendations
- Saves every report to PostgreSQL database
- Displays recent report history on the dashboard

---

## Tech Stack

| Layer | Technology                                       |
|---|--------------------------------------------------|
| Backend | Java 22, Spring Boot 3.3.1                       |
| Web Scraping | Jsoup 1.17.2                                     |
| AI Integration | Cohere API (command-r model)                     |
| Database | PostgreSQL, Supabase, Spring Data JPA, Hibernate |
| Frontend | HTML5, CSS3, JavaScript                          |
| Testing | JUnit 5, Mockito, H2 in-memory database          |
| DevOps | Docker, Docker Compose, GitHub Actions CI/CD     |
| Deployment | Railway (live), Docker Hub                       |

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/seo/analyze?url=` | Analyze a URL and generate SEO report |
| GET | `/api/v1/seo/history` | Get 10 most recent reports |
| GET | `/api/v1/seo/reports/{id}` | Get a specific report by ID |

---

## CI/CD Pipeline

Every push to `main` branch triggers:  

Push to GitHub  
↓  
Job 1 — Run JUnit 5 Tests  
↓ (only if tests pass)  
Job 2 — Build Docker Image  
↓  
Push to Docker Hub  
↓  
Auto redeploy on Railway  

---

## Key Implementation Highlights

- **Multi-stage Docker build** — separates Maven build and JRE runtime stages, reducing final image size
- **IPv4 forced** via `JAVA_TOOL_OPTIONS=-Djava.net.preferIPv4Stack=true` to resolve Docker + Supabase networking issue
- **EAGER collection fetching** on JPA entities to prevent lazy initialization errors on history retrieval
- **H2 in-memory database** used in test environment to isolate tests from production Supabase
- **Session Pooler** connection string used for Supabase to ensure reliable connectivity from containerized environments
- **Relative API URLs** in frontend for seamless localhost and production compatibility

---

## Environment Variables

| Variable | Description |
|---|---|
| `COHERE_API_KEY` | Your Cohere API key |
| `DB_URL` | Supabase JDBC connection URL |
| `DB_USERNAME` | Database username |
| `Database_Password` | Database password |
| `JAVA_TOOL_OPTIONS` | JVM options (set to `-Djava.net.preferIPv4Stack=true`) |

---

## Author

**Prasad Dongare**
- GitHub: [github.com/PrasadDongare](https://github.com/PrasadDongare)