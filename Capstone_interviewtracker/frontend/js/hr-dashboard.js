const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

function normalizeStage(candidate) {
  if (candidate.stage) return candidate.stage;
  if (candidate.status === "L1") return "L1_TECH";
  if (candidate.status === "L2") return "L2_TECH";
  if (candidate.status === "HR") return "HR_ROUND";
  if (candidate.status === "SELECTED") return "SELECTED";
  if (candidate.status === "REJECTED") return "REJECTED";
  return "PROFILING";
}

function stageLabel(stage) {
  const labels = {
    PROFILING: "Profiling",
    SCREENING: "Screening",
    L1_TECH: "L1 Technical",
    L2_TECH: "L2 Technical",
    HR_ROUND: "HR Round",
    SELECTED: "Selected",
    REJECTED: "Rejected"
  };
  return labels[stage] || stage;
}

function renderStats(rows) {
  const counts = {
    total: rows.length,
    profiling: 0,
    screening: 0,
    l1: 0,
    l2: 0,
    hr: 0,
    selected: 0,
    rejected: 0
  };

  rows.forEach((row) => {
    const stage = normalizeStage(row.candidate || {});
    if (stage === "PROFILING") counts.profiling++;
    if (stage === "SCREENING") counts.screening++;
    if (stage === "L1_TECH") counts.l1++;
    if (stage === "L2_TECH") counts.l2++;
    if (stage === "HR_ROUND") counts.hr++;
    if (stage === "SELECTED") counts.selected++;
    if (stage === "REJECTED") counts.rejected++;
  });

  document.getElementById("totalCount").textContent = counts.total;
  document.getElementById("profilingCount").textContent = counts.profiling;
  document.getElementById("screeningCount").textContent = counts.screening;
  document.getElementById("l1Count").textContent = counts.l1;
  document.getElementById("l2Count").textContent = counts.l2;
  document.getElementById("hrCount").textContent = counts.hr;
  document.getElementById("selectedCount").textContent = counts.selected;
  document.getElementById("rejectedCount").textContent = counts.rejected;
}

function feedbackHtml(feedback) {
  if (!feedback) {
    return '<div class="feedback-block"><div class="feedback-title">Latest Feedback</div><div class="meta">No feedback submitted yet</div></div>';
  }
  const panelName = feedback.panel && feedback.panel.name ? feedback.panel.name : "Panel";
  return `
    <div class="feedback-block">
      <div class="feedback-title">Latest Feedback</div>
      <div class="meta">${panelName} | Rating ${feedback.rating || "N/A"} | Decision ${feedback.status || "N/A"}</div>
      <div>${feedback.comments || "-"}</div>
    </div>
  `;
}

function renderCandidates(rows) {
  const list = document.getElementById("candidateList");
  if (!rows.length) {
    list.innerHTML = '<div class="empty-state">No candidates available yet.</div>';
    return;
  }

  list.innerHTML = rows.map((row) => {
    const c = row.candidate || {};
    const stage = normalizeStage(c);
    const name = (c.user && c.user.name) || c.fullName || "Candidate";
    const email = (c.user && c.user.email) || "N/A";
    const phone = c.phone || c.mobileNumber || "N/A";
    const job = c.jd && c.jd.title ? c.jd.title : "Not mapped";
    const latestFeedback = row.latestFeedback || null;
    const isFinal = stage === "SELECTED" || stage === "REJECTED";

    return `
      <div class="candidate-card">
        <div class="candidate-top">
          <div>
            <h4>${name}</h4>
            <div class="meta">${email} | ${phone}</div>
            <div class="meta">Job: ${job}</div>
          </div>
          <div>
            <span class="badge">${stageLabel(stage)}</span>
            <span class="badge">${(c.stageStatus || "PENDING").replace("_", " ")}</span>
          </div>
        </div>
        ${feedbackHtml(latestFeedback)}
        <div class="candidate-actions">
          <button onclick="advanceStage(${c.id})" ${isFinal ? "disabled" : ""}>Advance Stage</button>
          <button class="btn-success" onclick="selectCandidate(${c.id})" ${isFinal ? "disabled" : ""}>Select</button>
          <button class="btn-danger" onclick="rejectCandidate(${c.id})" ${isFinal ? "disabled" : ""}>Reject</button>
        </div>
      </div>
    `;
  }).join("");
}

function loadCandidates() {
  hrActions.listCandidates()
    .then((rows) => {
      renderStats(rows);
      renderCandidates(rows);
    })
    .catch((err) => {
      document.getElementById("candidateList").innerHTML = '<div class="alert alert-error">Failed to load candidates.</div>';
    });
}

function askComments(actionLabel) {
  const comments = prompt(`${actionLabel} comments (required):`);
  if (!comments || !comments.trim()) {
    alert("Comments are required.");
    return null;
  }
  return comments.trim();
}

function advanceStage(candidateId) {
  const comments = prompt("Optional HR comments for stage movement:") || "";
  hrActions.advanceCandidate(candidateId, comments)
    .then(() => loadCandidates())
    .catch((err) => alert(err.message || "Failed to advance stage"));
}

function selectCandidate(candidateId) {
  const comments = askComments("Selection");
  if (!comments) return;
  hrActions.selectCandidate(candidateId, comments)
    .then(() => loadCandidates())
    .catch((err) => alert(err.message || "Failed to select candidate"));
}

function rejectCandidate(candidateId) {
  const comments = askComments("Rejection");
  if (!comments) return;
  hrActions.rejectCandidate(candidateId, comments)
    .then(() => loadCandidates())
    .catch((err) => alert(err.message || "Failed to reject candidate"));
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}

loadCandidates();
