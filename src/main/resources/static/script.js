const API_BASE = "http://localhost:8080/api/v1/seo";

async function startAnalysis() {
  const input = document.getElementById("urlInput");
  let url = input.value.trim();

  if (!url) {
    showError("Please enter a URL to analyze.");
    return;
  }

  if (!url.startsWith("http://") && !url.startsWith("https://")) {
    url = "https://" + url;
    input.value = url;
  }

  hideError();
  hideReport();
  showLoader();
  runStepAnimation();

  try {
    const res = await fetch(`${API_BASE}/analyze?url=${encodeURIComponent(url)}`);

    if (!res.ok) {
      const err = await res.json().catch(() => ({ error: "Server returned an error." }));
      throw new Error(err.error || `Error ${res.status}`);
    }

    const data = await res.json();
    renderReport(data);
    loadHistory();

  } catch (err) {
    showError(err.message || "Could not connect to the server. Make sure the backend is running.");
  } finally {
    hideLoader();
  }
}

function renderReport(data) {
  const { url, analyzedAt, metadata, analysis } = data;

  document.getElementById("reportUrl").href = url;
  document.getElementById("reportUrlText").textContent = url;
  document.getElementById("reportTs").textContent = new Date(analyzedAt).toLocaleString();

  const score = analysis.score;
  const circumference = 2 * Math.PI * 60;
  const offset = circumference - (score / 100) * circumference;
  const color = scoreColor(score);

  const arc = document.getElementById("scoreArc");
  arc.style.stroke = color;
  setTimeout(() => { arc.style.strokeDashoffset = offset; }, 80);

  animateCount("scoreNum", 0, score, 1000);

  const badge = document.getElementById("scoreBadge");
  badge.textContent = scoreLabel(score);
  badge.style.color = color;
  badge.style.background = color + "18";

  document.getElementById("summaryText").textContent = analysis.summary;
  document.getElementById("qsStrengths").textContent = analysis.strengths.length;
  document.getElementById("qsWeaknesses").textContent = analysis.weaknesses.length;
  document.getElementById("qsRecommendations").textContent = analysis.recommendations.length;

  buildMetaGrid(metadata);

  fillList("listStrengths", "countStrengths", analysis.strengths);
  fillList("listWeaknesses", "countWeaknesses", analysis.weaknesses);
  fillList("listRecommendations", "countRecommendations", analysis.recommendations);

  const report = document.getElementById("report");
  report.style.display = "flex";
  report.scrollIntoView({ behavior: "smooth", block: "start" });
}

function buildMetaGrid(metadata) {
  const grid = document.getElementById("metaGrid");
  grid.innerHTML = "";

  const items = [
    {
      icon: "📄",
      label: "Page Title",
      value: metadata.title || "Missing",
      good: metadata.title && metadata.title.length >= 30 && metadata.title.length <= 60
    },
    {
      icon: "📝",
      label: `Meta Description (${metadata.metaDescription?.length || 0} chars)`,
      value: metadata.metaDescription ? metadata.metaDescription.substring(0, 52) + "..." : "Missing",
      good: metadata.metaDescription?.length >= 120 && metadata.metaDescription?.length <= 160
    },
    {
      icon: "#️⃣",
      label: "H1 Tags",
      value: metadata.h1Tags.length > 0 ? `${metadata.h1Tags.length} tag(s) — "${metadata.h1Tags[0]}"` : "None found",
      good: metadata.h1Tags.length === 1
    },
    {
      icon: "🔗",
      label: "Canonical Tag",
      value: metadata.hasCanonicalTag ? "Present" : "Missing",
      good: metadata.hasCanonicalTag
    },
    {
      icon: "🖼️",
      label: "Images Missing Alt",
      value: metadata.imagesWithoutAltCount === 0 ? "None — all good" : `${metadata.imagesWithoutAltCount} image(s) affected`,
      good: metadata.imagesWithoutAltCount === 0
    },
    {
      icon: "📱",
      label: "Open Graph Image",
      value: metadata.hasOgImage ? "Present" : "Missing",
      good: metadata.hasOgImage
    }
  ];

  items.forEach(({ icon, label, value, good }) => {
    const el = document.createElement("div");
    el.className = "meta-item";
    el.innerHTML = `
      <span class="mi-icon">${icon}</span>
      <div class="mi-body">
        <div class="mi-label">${label}</div>
        <div class="mi-value">${value}</div>
      </div>
      <span class="mi-status">${good ? "✅" : "❌"}</span>
    `;
    grid.appendChild(el);
  });
}

function fillList(listId, countId, items) {
  const ul = document.getElementById(listId);
  const countEl = document.getElementById(countId);
  ul.innerHTML = "";
  countEl.textContent = items.length;

  if (!items.length) {
    const li = document.createElement("li");
    li.style.opacity = "0.4";
    li.style.fontStyle = "italic";
    li.textContent = "Nothing found.";
    ul.appendChild(li);
    return;
  }

  items.forEach(text => {
    const li = document.createElement("li");
    li.textContent = text;
    ul.appendChild(li);
  });
}

async function loadHistory() {
  try {
    const res = await fetch(`${API_BASE}/history`);
    if (!res.ok) return;

    const history = await res.json();
    if (!history.length) return;

    const section = document.getElementById("historySection");
    const row = document.getElementById("historyRow");
    row.innerHTML = "";
    section.style.display = "block";

    history.forEach(item => {
      const color = scoreColor(item.score);
      const card = document.createElement("div");
      card.className = "history-card";
      card.innerHTML = `
        <span class="h-badge" style="color:${color};background:${color}18">Score: ${item.score}</span>
        <div class="h-title">${item.pageTitle || "Untitled"}</div>
        <div class="h-url">${item.url}</div>
        <div class="h-date">${new Date(item.analyzedAt).toLocaleDateString()}</div>
      `;
      card.addEventListener("click", () => {
        document.getElementById("urlInput").value = item.url;
        startAnalysis();
      });
      row.appendChild(card);
    });

  } catch {
    // silently skip
  }
}

function runStepAnimation() {
  const steps = ["step1", "step2", "step3"];
  steps.forEach(id => {
    const el = document.getElementById(id);
    el.classList.remove("active", "done");
  });

  let i = 0;
  function next() {
    if (i > 0) {
      const prev = document.getElementById(steps[i - 1]);
      prev.classList.remove("active");
      prev.classList.add("done");
    }
    if (i < steps.length) {
      document.getElementById(steps[i]).classList.add("active");
      i++;
      setTimeout(next, 2200);
    }
  }
  next();
}

function animateCount(elId, from, to, duration) {
  const el = document.getElementById(elId);
  const start = performance.now();
  function tick(now) {
    const progress = Math.min((now - start) / duration, 1);
    el.textContent = Math.round(from + (to - from) * progress);
    if (progress < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}

function scoreColor(score) {
  if (score >= 80) return "#4ade80";
  if (score >= 60) return "#fbbf24";
  if (score >= 40) return "#f97316";
  return "#f87171";
}

function scoreLabel(score) {
  if (score >= 80) return "Excellent";
  if (score >= 60) return "Good";
  if (score >= 40) return "Needs Work";
  return "Poor";
}

function showLoader() {
  document.getElementById("loader").style.display = "flex";
  document.getElementById("analyzeBtn").disabled = true;
  document.getElementById("analyzeBtn").textContent = "Analyzing...";
}

function hideLoader() {
  document.getElementById("loader").style.display = "none";
  document.getElementById("analyzeBtn").disabled = false;
  document.getElementById("analyzeBtn").textContent = "Analyze";
}

function hideReport() {
  document.getElementById("report").style.display = "none";
}

function showError(msg) {
  const el = document.getElementById("errorMsg");
  el.textContent = msg;
  el.style.display = "block";
}

function hideError() {
  document.getElementById("errorMsg").style.display = "none";
}

document.getElementById("urlInput").addEventListener("keydown", e => {
  if (e.key === "Enter") startAnalysis();
});

loadHistory();