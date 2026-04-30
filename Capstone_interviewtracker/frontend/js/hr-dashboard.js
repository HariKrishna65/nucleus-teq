const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let allRows = [];
let activeFilter = "ALL";
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
  const filtered = (rows || []).filter((row) => {
    if (activeFilter === "ALL") return true;
    const stage = normalizeStage((row && row.candidate) || {});
    return stage === activeFilter;
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
            const canAssignPanel = stage === "L1_TECH" || stage === "L2_TECH" || stage === "HR_ROUND";

            return `
              <tr>
                <td>${name}</td>
                <td>${email}</td>
                <td>${phone}</td>
                <td>${job}</td>
                <td><span class="badge">${stageLabel(stage)}</span></td>
                <td><span class="badge">${(c.stageStatus || "PENDING").replace("_", " ")}</span></td>
                <td>${feedbackText}</td>
                <td>
                  <div class="table-actions">
                    <button class="btn-small" onclick="advanceStage(${c.id})" ${isFinal ? "disabled" : ""}>Advance</button>
                    ${canAssignPanel ? `<button class="secondary-btn btn-small" onclick="openAssignPanel(${c.id})" ${isFinal ? "disabled" : ""}>Assign</button>` : ``}
                    <button class="btn-success btn-small" onclick="selectCandidate(${c.id})" ${isFinal ? "disabled" : ""}>Select</button>
                    <button class="btn-danger btn-small" onclick="rejectCandidate(${c.id})" ${isFinal ? "disabled" : ""}>Reject</button>
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
  document.querySelectorAll(".stat-card[data-filter]").forEach((card) => {
    card.classList.toggle("active", String(card.dataset.filter) === String(activeFilter));
  });
  renderCandidates(allRows);
}

function wireStatFilters() {
  const cards = document.querySelectorAll(".stat-card[data-filter]");
  cards.forEach((card) => {
    const filter = card.dataset.filter || "ALL";
    card.addEventListener("click", () => setActiveFilter(filter));
    card.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        setActiveFilter(filter);
      }
    });
  });
  setActiveFilter("ALL");
}

function loadCandidates() {
  hrActions.listCandidates()
    .then((rows) => {
      allRows = Array.isArray(rows) ? rows : [];
      renderStats(allRows);
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

function loadPanels() {
  return interviewActions.listPanels()
    .then((panels) => {
      allPanels = Array.isArray(panels) ? panels : [];
      renderPanelMembers(allPanels);
      return allPanels;
    })
    .catch(() => {
      allPanels = [];
      renderPanelMembers(allPanels);
      return [];
    });
}

function renderPanelMembers(panels) {
  const body = document.getElementById("panelMembersBody");
  if (!body) return;
  if (!panels || panels.length === 0) {
    body.innerHTML = '<tr><td colspan="6" class="empty-state">No panel members added yet.</td></tr>';
    return;
  }

  body.innerHTML = panels.map((p) => `
    <tr>
      <td>${p.name || "-"}</td>
      <td>${p.email || "-"}</td>
      <td>${p.mobile || "-"}</td>
      <td>${p.organization || "-"}</td>
      <td>${p.designation || "-"}</td>
      <td>${p.expertise || "-"}</td>
    </tr>
  `).join("");
}

function showPanelAlert(message, type) {
  const alert = document.getElementById("panelAlert");
  if (!alert) return;
  alert.className = `alert alert-${type}`;
  alert.textContent = message;
  alert.classList.remove("is-hidden");
}

function clearPanelForm() {
  document.getElementById("panelName").value = "";
  document.getElementById("panelEmail").value = "";
  document.getElementById("panelPhone").value = "";
  document.getElementById("panelOrganization").value = "";
  document.getElementById("panelDesignation").value = "";
  document.getElementById("panelExpertise").value = "";
}

function createPanelMember() {
  const payload = {
    name: document.getElementById("panelName").value.trim(),
    email: document.getElementById("panelEmail").value.trim(),
    phone: document.getElementById("panelPhone").value.trim(),
    organization: document.getElementById("panelOrganization").value.trim(),
    designation: document.getElementById("panelDesignation").value.trim(),
    expertise: document.getElementById("panelExpertise").value.trim()
  };

  if (!payload.name || !payload.email || !payload.organization || !payload.designation || !payload.expertise) {
    showPanelAlert("Please fill all required panel details.", "error");
    return;
  }

  const btn = document.getElementById("createPanelBtn");
  btn.disabled = true;
  btn.textContent = "Creating...";

  hrActions.createPanel(payload)
    .then(() => {
      showPanelAlert("Panel member created. Password setup email sent successfully.", "success");
      clearPanelForm();
      return loadPanels();
    })
    .catch((err) => {
      showPanelAlert(err.message || "Failed to create panel member.", "error");
    })
    .finally(() => {
      btn.disabled = false;
      btn.textContent = "Create Panel";
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

  if (!email1) {
    showAssignPanelError("Panel member 1 is required");
    return;
  }
  if (email2 && email2 === email1) {
    showAssignPanelError("Panel member 2 must be different from panel member 1");
    return;
  }

  const emails = [email1, email2].filter(Boolean);
  if (emails.length < 1 || emails.length > 2) {
    showAssignPanelError("Panel members must be between 1 and 2");
    return;
  }

  const btn = document.getElementById("assignPanelSubmitBtn");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Assigning...";
  }

  hrActions.assignPanel(assignCandidateId, emails)
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

function wireSidebarNavigation() {
  const links = Array.from(document.querySelectorAll(".sidebar-nav a"));
  links.forEach((link) => {
    link.addEventListener("click", () => {
      links.forEach((l) => l.classList.remove("active"));
      link.classList.add("active");
    });
  });
}

// Expose modal functions to HTML onclick handlers
window.closeAssignPanel = closeAssignPanel;
window.submitAssignPanel = submitAssignPanel;
window.openAssignPanel = openAssignPanel;
window.createPanelMember = createPanelMember;

wireStatFilters();
loadPanels();
loadCandidates();
wireSidebarNavigation();

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
