const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let allRows = [];
let searchQuery = "";

function normalizeStage(candidate) {
  if (candidate.stage) return candidate.stage;
  if (candidate.status === "L1") return "L1_TECH";
  if (candidate.status === "L2") return "L2_TECH";
  if (candidate.status === "HR") return "HR_ROUND";
  if (candidate.status === "SELECTED") return "SELECTED";
  if (candidate.status === "REJECTED") return "REJECTED";
  return "PROFILING";
}

function renderOnboard(rows) {
  const wrap = document.getElementById("onboardList");
  const q = (searchQuery || "").trim().toLowerCase();
  const filtered = (rows || [])
    .filter(r => normalizeStage((r && r.candidate) || {}) === "HR_ROUND")
    .filter(r => {
      const c = (r && r.candidate) || {};
      const name = ((c.user && c.user.name) || c.fullName || "Candidate").toLowerCase();
      const email = ((c.user && c.user.email) || "").toLowerCase();
      const phone = String(c.phone || c.mobileNumber || "").toLowerCase();
      const job = ((c.jd && c.jd.title) || "").toLowerCase();
      return !q || name.includes(q) || email.includes(q) || phone.includes(q) || job.includes(q);
    });

  if (!filtered.length) {
    wrap.innerHTML = '<div class="empty-state"><h3>No HR round candidates</h3><p>Candidates in the HR round will appear here for final decision.</p></div>';
    return;
  }

  wrap.innerHTML = `
    <div class="candidate-table-wrap">
      <table class="candidate-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Job Title</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map((row) => {
            const c = row.candidate || {};
            const name = (c.user && c.user.name) || c.fullName || "Candidate";
            const email = (c.user && c.user.email) || "N/A";
            const phone = c.phone || c.mobileNumber || "N/A";
            const job = c.jd && c.jd.title ? c.jd.title : "Not mapped";
            const status = (c.stageStatus || "PENDING").replace("_", " ");
            return `
              <tr>
                <td>${name}</td>
                <td>${email}</td>
                <td>${phone}</td>
                <td>${job}</td>
                <td><span class="badge">${status}</span></td>
                <td>
                  <div class="table-actions">
                    <button class="btn-success btn-small" onclick="selectCandidate(${c.id})">Select</button>
                    <button class="btn-danger btn-small" onclick="rejectCandidate(${c.id})">Reject</button>
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

function askComments(actionLabel) {
  const comments = prompt(`${actionLabel} comments (required):`);
  if (!comments || !comments.trim()) {
    alert("Comments are required.");
    return null;
  }
  return comments.trim();
}

function selectCandidate(candidateId) {
  const comments = askComments("Selection");
  if (!comments) return;
  hrActions.selectCandidate(candidateId, comments)
    .then(() => load())
    .catch((err) => alert(err.message || "Failed to select candidate"));
}

function rejectCandidate(candidateId) {
  const comments = askComments("Rejection");
  if (!comments) return;
  hrActions.rejectCandidate(candidateId, comments)
    .then(() => load())
    .catch((err) => alert(err.message || "Failed to reject candidate"));
}

function wire() {
  const search = document.getElementById("onboardSearch");
  if (search) {
    search.addEventListener("input", () => {
      searchQuery = search.value || "";
      renderOnboard(allRows);
    });
  }
}

function clearOnboardFilters() {
  searchQuery = "";
  const search = document.getElementById("onboardSearch");
  if (search) search.value = "";
  renderOnboard(allRows);
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

function load() {
  hrActions.listCandidates()
    .then((rows) => {
      allRows = Array.isArray(rows) ? rows : [];
      renderOnboard(allRows);
    })
    .catch(() => {
      document.getElementById("onboardList").innerHTML = '<div class="alert alert-error">Failed to load candidates.</div>';
    });
}

window.selectCandidate = selectCandidate;
window.rejectCandidate = rejectCandidate;
window.clearOnboardFilters = clearOnboardFilters;
window.toggleSidebar = toggleSidebar;
window.logout = logout;

wire();
load();
