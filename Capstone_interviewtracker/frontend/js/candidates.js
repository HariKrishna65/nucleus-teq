// Wait for DOM to be loaded before checking authentication
document.addEventListener('DOMContentLoaded', function() {
  const user = getStoredUser();

  console.log("User data:", user);

  if (!user) {
    console.log("No user found, redirecting to login");
    window.location.href = "login.html";
    return;
  }
  if (user.role !== "HR") {
    console.log("User role is not HR, role is:", user.role);
    window.location.href = "index.html";
    return;
  }

  // Initialize the page after authentication check
  initializeCandidatesPage();
});

function initializeCandidatesPage() {

let allRows = [];
let activeFilter = "ALL";
let searchQuery = "";
let allPanels = [];
let assignCandidateId = null;

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

function getMeetingLink(row) {
  const link = row.interview?.meetingLink || row.meetingLink || row.meetingUrl || row.interview?.meetingUrl;
  if (!link) return 'N/A';
  const display = link.length > 40 ? `${link.slice(0, 40)}…` : link;
  return `<a href="${link}" target="_blank" rel="noopener noreferrer">${display}</a>`;
}

function buildLocalDateTime(interviewDate, interviewTime) {
  return `${interviewDate}T${interviewTime}:00`;
}

function renderCandidates(rows) {
  const list = document.getElementById("candidateList");
  const q = (searchQuery || "").trim().toLowerCase();
  const filtered = (rows || []).filter((row) => {
    const c = (row && row.candidate) || {};
    const stage = normalizeStage(c);
    const name = ((c.user && c.user.name) || c.fullName || "Candidate").toLowerCase();
    const email = ((c.user && c.user.email) || "").toLowerCase();
    const phone = String(c.phone || c.mobileNumber || "").toLowerCase();
    const job = ((c.jd && c.jd.title) || "").toLowerCase();

    const stageOk = (activeFilter === "ALL") ? true : stage === activeFilter;
    const searchOk = !q ? true : (name.includes(q) || email.includes(q) || phone.includes(q) || job.includes(q));
    return stageOk && searchOk;
  });

  if (!filtered.length) {
    list.innerHTML = '<div class="empty-state">No candidates available yet.</div>';
    return;
  }

  list.innerHTML = `
    <div class="candidate-table-wrap">
      <table class="candidate-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Job Title</th>
            <th>Stage</th>
            <th>Status</th>
            <th>Meeting</th>
            <th>Feedback</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map((row) => {
            const c = row.candidate || {};
            const stage = normalizeStage(c);
            const name = (c.user && c.user.name) || c.fullName || "Candidate";
            const email = (c.user && c.user.email) || "N/A";
            const phone = c.phone || c.mobileNumber || "N/A";
            const job = c.jd && c.jd.title ? c.jd.title : "Not mapped";
            const latestFeedback = row.latestFeedback || null;
            const feedbackText = latestFeedback
              ? `${(latestFeedback.panel && latestFeedback.panel.name) || "Panel"} | ${latestFeedback.status || "N/A"} | ${latestFeedback.rating || "N/A"}`
              : "No feedback";
            const isFinal = stage === "SELECTED" || stage === "REJECTED";
            const isPanelRound = stage === "L1_TECH" || stage === "L2_TECH" || stage === "HR_ROUND";
            const canAssignPanel = row.canAssignPanel !== undefined ? row.canAssignPanel : isPanelRound;
            const panelAssigned = !!row.panelAssignedForCurrentRound;
            const assignedPanelCount = row.assignedPanelCount || 0;
            const currentRoundFeedbackCount = row.currentRoundFeedbackCount || 0;
            const canAdvanceStage = row.canAdvanceStage !== undefined ? row.canAdvanceStage : true;
            const feedbackProgress = panelAssigned
              ? `Feedback ${currentRoundFeedbackCount}/${assignedPanelCount}`
              : feedbackText;

            return `
              <tr>
                <td><a href="candidate-detail.html?id=${c.id}" class="candidate-link">${name}</a></td>
                <td>${email}</td>
                <td>${phone}</td>
                <td>${job}</td>
                <td>${renderStageTracker(stage)}</td>
                <td><span class="badge">${(c.stageStatus || "PENDING").replace("_", " ")}</span></td>
                <td>${getMeetingLink(row)}</td>
                <td>${feedbackProgress}</td>
                <td>
                  <div class="table-actions">
                    <button class="btn-small" onclick="advanceStage(${c.id})" ${isFinal || (isPanelRound && panelAssigned && !canAdvanceStage) ? "disabled" : ""}>Move to Next Stage</button>
                    ${canAssignPanel ? `<button class="secondary-btn btn-small" onclick="openAssignPanel(${c.id})" ${isFinal ? "disabled" : ""}>Assign</button>` : ``}
                    <button class="btn-success btn-small" onclick="selectCandidate(${c.id})" ${isFinal ? "disabled" : ""}>Select</button>
                    <button class="btn-danger btn-small" onclick="rejectCandidate(${c.id})" ${isFinal ? "disabled" : ""}>Reject</button>
                    <button class="btn-danger btn-small" onclick="deleteCandidate(${c.id})">Delete</button>
                  </div>
                </td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function setActiveFilter(filter) {
  activeFilter = filter || "ALL";
  renderCandidates(allRows);
}

function wireFilters() {
  const search = document.getElementById("candidateSearch");
  if (search) {
    search.addEventListener("input", () => {
      searchQuery = search.value || "";
      renderCandidates(allRows);
    });
  }
}

function loadCandidates() {
  hrActions.listCandidates()
    .then((rows) => {
      allRows = Array.isArray(rows) ? rows : [];
      renderCandidates(allRows);
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
  // Find the candidate to check current stage
  const row = allRows.find(row => (row.candidate || {}).id === candidateId);
  const stage = row ? normalizeStage(row.candidate) : null;
  const isPanelRound = stage === 'L1_TECH' || stage === 'L2_TECH' || stage === 'HR_ROUND';
  
  if (isPanelRound && !row?.panelAssignedForCurrentRound) {
    window.location.href = `assign-panel.html?candidateId=${candidateId}`;
    return;
  }

  if (isPanelRound && row?.canAdvanceStage === false) {
    alert(`Waiting for feedback from all assigned panel members (${row.currentRoundFeedbackCount || 0}/${row.assignedPanelCount || 0}).`);
    return;
  }
  
  // For other stages, proceed with normal advancement
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

function deleteCandidate(candidateId) {
  if (!confirm("Delete this candidate application?")) return;
  hrActions.deleteCandidate(candidateId)
    .then(() => loadCandidates())
    .catch((err) => alert(err.message || "Failed to delete candidate"));
}

function clearCandidateFilters() {
  searchQuery = "";
  const search = document.getElementById("candidateSearch");
  if (search) search.value = "";
  renderCandidates(allRows);
}

function toggleSidebar() {
  const shell = document.querySelector(".dashboard-shell");
  if (!shell) return;
  shell.classList.toggle("sidebar-collapsed");
}

function loadPanels() {
  return interviewActions.listPanels()
    .then((panels) => {
      allPanels = Array.isArray(panels) ? panels : [];
      return allPanels;
    })
    .catch(() => {
      allPanels = [];
      return [];
    });
}

function openAssignPanel(candidateId) {
  assignCandidateId = candidateId;
  const modal = document.getElementById("assignPanelModal");
  const err = document.getElementById("assignPanelError");
  if (err) {
    err.textContent = "";
    err.classList.add("is-hidden");
  }
  modal.classList.remove("is-hidden");
  modal.setAttribute("aria-hidden", "false");

  // Populate selects
  const s1 = document.getElementById("panelEmail1");
  const s2 = document.getElementById("panelEmail2");
  const options = allPanels
    .filter(p => p && p.email)
    .map(p => ({ label: `${p.name || "Panel"} (${p.email})`, value: p.email }));

  s1.innerHTML = `<option value="">-- Select panel member --</option>` + options.map(o => `<option value="${o.value}">${o.label}</option>`).join("");
  s2.innerHTML = `<option value="">-- Optional second member --</option>` + options.map(o => `<option value="${o.value}">${o.label}</option>`).join("");
}

function closeAssignPanel() {
  const modal = document.getElementById("assignPanelModal");
  if (!modal) return;
  modal.classList.add("is-hidden");
  modal.setAttribute("aria-hidden", "true");
  assignCandidateId = null;
}

function showAssignPanelError(message) {
  const err = document.getElementById("assignPanelError");
  if (!err) return;
  err.textContent = message;
  err.classList.remove("is-hidden");
}

function submitAssignPanel() {
  if (!assignCandidateId) return;
  const email1 = (document.getElementById("panelEmail1").value || "").trim();
  const email2 = (document.getElementById("panelEmail2").value || "").trim();
  const interviewDate = document.getElementById("interviewDate").value;
  const interviewTime = document.getElementById("interviewTime").value;
  const focusArea = (document.getElementById("focusArea").value || "").trim();
  const interviewNotes = (document.getElementById("interviewNotes").value || "").trim();

  if (!email1) {
    showAssignPanelError("Panel member 1 is required");
    return;
  }
  if (email2 && email2 === email1) {
    showAssignPanelError("Panel member 2 must be different from panel member 1");
    return;
  }
  if (!interviewDate) {
    showAssignPanelError("Interview date is required");
    return;
  }
  if (!interviewTime) {
    showAssignPanelError("Interview time is required");
    return;
  }

  const emails = [email1, email2].filter(Boolean);
  if (emails.length < 1 || emails.length > 2) {
    showAssignPanelError("Panel members must be between 1 and 2");
    return;
  }

  // Combine date and time into a proper datetime
  const interviewDateTime = new Date(`${interviewDate}T${interviewTime}`);
  if (isNaN(interviewDateTime)) {
    showAssignPanelError("Invalid date or time");
    return;
  }
  if (interviewDateTime <= new Date()) {
    showAssignPanelError("Interview date and time must be in the future");
    return;
  }

  const btn = document.getElementById("assignPanelSubmitBtn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Assigning...";
  }

  const assignData = {
    panelEmails: emails,
    interviewTime: buildLocalDateTime(interviewDate, interviewTime),
    focusArea: focusArea || null,
    notes: interviewNotes || null
  };

  hrActions.assignPanel(assignCandidateId, assignData)
    .then(() => {
      closeAssignPanel();
      alert("Panel assigned and emails sent successfully.");
      loadCandidates();
    })
    .catch((err) => {
      showAssignPanelError(err.message || "Failed to assign panel");
    })
    .finally(() => {
      if (btn) {
        btn.disabled = false;
        btn.textContent = "Assign & Send Emails";
      }
    });
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}

// Expose modal functions to HTML onclick handlers
window.closeAssignPanel = closeAssignPanel;
window.submitAssignPanel = submitAssignPanel;
window.openAssignPanel = openAssignPanel;
window.deleteCandidate = deleteCandidate;
window.clearCandidateFilters = clearCandidateFilters;
window.toggleSidebar = toggleSidebar;
window.advanceStage = advanceStage;
window.selectCandidate = selectCandidate;
window.rejectCandidate = rejectCandidate;
window.logout = logout;

// Initialize page
wireFilters();
loadPanels();
loadCandidates();

// Ensure modal starts hidden + allow backdrop/Esc close
(() => {
  const modal = document.getElementById("assignPanelModal");
  if (!modal) return;
  modal.classList.add("is-hidden");
  modal.setAttribute("aria-hidden", "true");

  modal.addEventListener("click", (e) => {
    if (e.target === modal) closeAssignPanel();
  });
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !modal.classList.contains("is-hidden")) {
      closeAssignPanel();
    }
  });
})();

// Close the initializeCandidatesPage function
}
