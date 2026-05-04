const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let candidateId = null;
let candidateData = null;

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

function stageIndex(stage) {
  const order = ["PROFILING", "SCREENING", "L1_TECH", "L2_TECH", "HR_ROUND", "SELECTED"];
  const idx = order.indexOf(stage);
  return idx === -1 ? 0 : idx;
}

function renderStageTracker(stage) {
  const steps = ["PROFILING", "SCREENING", "L1_TECH", "L2_TECH", "HR_ROUND", "SELECTED"];
  const current = stageIndex(stage);
  const isRejected = stage === "REJECTED";

  if (isRejected) {
    return `<span class="badge">Rejected</span>`;
  }

  return `
    <div class="stage-tracker" title="${stageLabel(stage)}">
      ${steps.map((s, i) => {
        const done = i < current;
        const active = i === current;
        const circleClass = done ? "done" : (active ? "active" : "");
        const lineClass = i < current ? "done" : "";
        const line = i < steps.length - 1 ? `<span class="stage-line ${lineClass}"></span>` : "";
        return `
          <span class="stage-step">
            <span class="stage-circle ${circleClass}"></span>
            ${line}
          </span>
        `;
      }).join("")}
    </div>
  `;
}

function formatDate(dateString) {
  if (!dateString) return "N/A";
  const date = new Date(dateString);
  return date.toLocaleDateString() + " " + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function renderCandidateDetails() {
  const container = document.getElementById("candidateDetail");
  if (!candidateData) {
    container.innerHTML = '<div class="alert alert-error">Candidate not found.</div>';
    return;
  }

  const c = candidateData.candidate || {};
  const stage = normalizeStage(c);
  const name = (c.user && c.user.name) || c.fullName || "Candidate";
  const email = (c.user && c.user.email) || "N/A";
  const phone = c.phone || c.mobileNumber || "N/A";
  const job = c.jd && c.jd.title ? c.jd.title : "Not mapped";
  const experience = c.experience || "N/A";
  const skills = c.skills || "N/A";
  const education = c.education || "N/A";
  const location = c.location || "N/A";
  const resume = c.resumePath ? `<a href="#" onclick="viewResume('${c.resumePath}')" class="btn-small">View Resume</a>` : "N/A";
  const createdDate = formatDate(c.createdAt);
  const updatedDate = formatDate(c.updatedAt);

  document.getElementById("candidateName").textContent = name;

  container.innerHTML = `
    <div class="detail-grid">
      <div class="detail-card">
        <h3>Personal Information</h3>
        <div class="detail-row">
          <span class="detail-label">Name</span>
          <span class="detail-value">${name}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Email</span>
          <span class="detail-value">${email}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Phone</span>
          <span class="detail-value">${phone}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Location</span>
          <span class="detail-value">${location}</span>
        </div>
      </div>

      <div class="detail-card">
        <h3>Professional Information</h3>
        <div class="detail-row">
          <span class="detail-label">Job Title</span>
          <span class="detail-value">${job}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Experience</span>
          <span class="detail-value">${experience}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Skills</span>
          <span class="detail-value">${skills}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Education</span>
          <span class="detail-value">${education}</span>
        </div>
      </div>

      <div class="detail-card">
        <h3>Application Status</h3>
        <div class="detail-row">
          <span class="detail-label">Current Stage</span>
          <span class="detail-value">${renderStageTracker(stage)}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Stage Status</span>
          <span class="detail-value"><span class="badge">${(c.stageStatus || "PENDING").replace("_", " ")}</span></span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Applied On</span>
          <span class="detail-value">${createdDate}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Last Updated</span>
          <span class="detail-value">${updatedDate}</span>
        </div>
      </div>

      <div class="detail-card">
        <h3>Documents</h3>
        <div class="detail-row">
          <span class="detail-label">Resume</span>
          <span class="detail-value">${resume}</span>
        </div>
      </div>
    </div>

    <div class="detail-actions">
      <button class="btn-small" onclick="advanceStage(${c.id})" ${stage === "SELECTED" || stage === "REJECTED" ? "disabled" : ""}>Advance Stage</button>
      <button class="btn-success btn-small" onclick="selectCandidate(${c.id})" ${stage === "SELECTED" || stage === "REJECTED" ? "disabled" : ""}>Select Candidate</button>
      <button class="btn-danger btn-small" onclick="rejectCandidate(${c.id})" ${stage === "SELECTED" || stage === "REJECTED" ? "disabled" : ""}>Reject Candidate</button>
      <button class="secondary-btn btn-small" onclick="window.location.href='candidates.html'">Back to Candidates</button>
    </div>

    ${renderFeedbackHistory()}
    ${renderInterviewHistory()}
  `;
}

function renderFeedbackHistory() {
  const feedback = candidateData.feedbackHistory || [];
  if (!feedback.length) return "";

  return `
    <div class="feedback-history">
      <h3>Feedback History</h3>
      ${feedback.map(f => `
        <div class="feedback-item">
          <div class="feedback-header">
            <div class="feedback-panel">${f.panel && f.panel.name ? f.panel.name : "Panel"}</div>
            <div class="feedback-meta">Rating: ${f.rating || "N/A"} | Decision: ${f.status || "N/A"} | ${formatDate(f.createdAt)}</div>
          </div>
          <div class="feedback-comments">${f.comments || "No comments provided"}</div>
        </div>
      `).join("")}
    </div>
  `;
}

function renderInterviewHistory() {
  const interviews = candidateData.interviewHistory || [];
  if (!interviews.length) return "";

  return `
    <div class="interview-history">
      <h3>Interview History</h3>
      ${interviews.map(i => `
        <div class="interview-item">
          <div class="interview-header">
            <div class="interview-round">${i.round || "N/A"}</div>
            <div class="interview-date">${formatDate(i.scheduledDate)}</div>
          </div>
          <div class="interview-details">
            <div><strong>Panel:</strong> ${i.panel && i.panel.name ? i.panel.name : "N/A"}</div>
            <div><strong>Status:</strong> ${i.status || "N/A"}</div>
            <div><strong>Duration:</strong> ${i.duration || "N/A"}</div>
            <div><strong>Mode:</strong> ${i.mode || "N/A"}</div>
          </div>
        </div>
      `).join("")}
    </div>
  `;
}

function loadCandidateDetails() {
  const urlParams = new URLSearchParams(window.location.search);
  candidateId = urlParams.get('id');
  
  if (!candidateId) {
    document.getElementById("candidateDetail").innerHTML = '<div class="alert alert-error">No candidate ID provided.</div>';
    return;
  }

  hrActions.getCandidateDetails(candidateId)
    .then((data) => {
      candidateData = data;
      renderCandidateDetails();
    })
    .catch((err) => {
      document.getElementById("candidateDetail").innerHTML = '<div class="alert alert-error">Failed to load candidate details.</div>';
    });
}

function advanceStage(candidateId) {
  const comments = prompt("Optional HR comments for stage movement:") || "";
  hrActions.(candidateId, comments)
    .then(() => loadCandidateDetails())
    .catch((err) => alert(err.message || "Failed to advance stage"));
}

function selectCandidate(candidateId) {
  const comments = prompt("Selection comments (required):");
  if (!comments || !comments.trim()) {
    alert("Comments are required.");
    return;
  }
  hrActions.selectCandidate(candidateId, comments.trim())
    .then(() => loadCandidateDetails())
    .catch((err) => alert(err.message || "Failed to select candidate"));
}

function rejectCandidate(candidateId) {
  const comments = prompt("Rejection comments (required):");
  if (!comments || !comments.trim()) {
    alert("Comments are required.");
    return;
  }
  hrActions.rejectCandidate(candidateId, comments.trim())
    .then(() => loadCandidateDetails())
    .catch((err) => alert(err.message || "Failed to reject candidate"));
}

function viewResume(resumePath) {
  alert("Resume viewing functionality to be implemented");
}

function toggleSidebar() {
  const shell = document.querySelector(".dashboard-shell");
  if (!shell) return;
  shell.classList.toggle("sidebar-collapsed");
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}

window.advanceStage = advanceStage;
window.selectCandidate = selectCandidate;
window.rejectCandidate = rejectCandidate;
window.viewResume = viewResume;
window.toggleSidebar = toggleSidebar;
window.logout = logout;

loadCandidateDetails();
