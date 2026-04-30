const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let allRows = [];
let searchQuery = "";
let stageFilter = "ALL";

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

function buildItems(rows) {
  const items = [];
  (rows || []).forEach((row) => {
    const c = row.candidate || {};
    const feedbackList = Array.isArray(row.feedback) ? row.feedback : [];
    const stage = normalizeStage(c);

    feedbackList.forEach((fb) => {
      items.push({ candidate: c, stage, feedback: fb });
    });
  });
  return items;
}

function render(items) {
  const wrap = document.getElementById("feedbackList");
  const q = (searchQuery || "").trim().toLowerCase();

  const filtered = (items || []).filter((it) => {
    if (stageFilter !== "ALL" && it.stage !== stageFilter) return false;
    if (!q) return true;
    const c = it.candidate || {};
    const fb = it.feedback || {};
    const name = ((c.user && c.user.name) || c.fullName || "").toLowerCase();
    const email = ((c.user && c.user.email) || "").toLowerCase();
    const job = ((c.jd && c.jd.title) || "").toLowerCase();
    const panel = ((fb.panel && fb.panel.name) || "").toLowerCase();
    const comments = (fb.comments || "").toLowerCase();
    return name.includes(q) || email.includes(q) || job.includes(q) || panel.includes(q) || comments.includes(q);
  });

  if (!filtered.length) {
    wrap.innerHTML = '<div class="empty-state">No feedback available yet.</div>';
    return;
  }

  wrap.innerHTML = `
    <div class="candidate-table-wrap">
      <table class="candidate-table">
        <thead>
          <tr>
            <th>Candidate</th>
            <th>Email</th>
            <th>Job</th>
            <th>Stage</th>
            <th>Panel</th>
            <th>Rating</th>
            <th>Decision</th>
            <th>Comments</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map((it) => {
            const c = it.candidate || {};
            const fb = it.feedback || {};
            const name = (c.user && c.user.name) || c.fullName || "Candidate";
            const email = (c.user && c.user.email) || "N/A";
            const job = c.jd && c.jd.title ? c.jd.title : "Not mapped";
            const panel = (fb.panel && fb.panel.name) ? fb.panel.name : "Panel";
            const rating = fb.rating || "N/A";
            const decision = fb.status || "N/A";
            const comments = fb.comments || "-";
            return `
              <tr>
                <td>${name}</td>
                <td>${email}</td>
                <td>${job}</td>
                <td><span class="badge">${stageLabel(it.stage)}</span></td>
                <td>${panel}</td>
                <td>${rating}</td>
                <td><span class="badge">${decision}</span></td>
                <td style="white-space: normal; min-width: 280px;">${comments}</td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function load() {
  hrActions.listCandidates()
    .then((rows) => {
      allRows = Array.isArray(rows) ? rows : [];
      render(buildItems(allRows));
    })
    .catch((err) => {
      document.getElementById("feedbackList").innerHTML = '<div class="alert alert-error">Failed to load feedback.</div>';
    });
}

function wire() {
  const search = document.getElementById("feedbackSearch");
  const sel = document.getElementById("feedbackStageFilter");
  if (search) {
    search.addEventListener("input", () => {
      searchQuery = search.value || "";
      render(buildItems(allRows));
    });
  }
  if (sel) {
    sel.addEventListener("change", () => {
      stageFilter = sel.value || "ALL";
      render(buildItems(allRows));
    });
  }
}

function clearFeedbackFilters() {
  searchQuery = "";
  stageFilter = "ALL";
  const search = document.getElementById("feedbackSearch");
  const sel = document.getElementById("feedbackStageFilter");
  if (search) search.value = "";
  if (sel) sel.value = "ALL";
  render(buildItems(allRows));
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

window.clearFeedbackFilters = clearFeedbackFilters;
window.toggleSidebar = toggleSidebar;
window.logout = logout;

wire();
load();

