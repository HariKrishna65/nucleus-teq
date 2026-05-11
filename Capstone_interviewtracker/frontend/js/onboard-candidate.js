const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let allRows = [];
let searchQuery = "";
let allJobs = [];

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
    .filter(r => (((r && r.candidate) || {}).source || "").toLowerCase() === "referral")
    .filter(r => {
      const c = (r && r.candidate) || {};
      const name = ((c.user && c.user.name) || c.fullName || "Candidate").toLowerCase();
      const email = ((c.user && c.user.email) || "").toLowerCase();
      const phone = String(c.phone || c.mobileNumber || "").toLowerCase();
      const job = ((c.jd && c.jd.title) || "").toLowerCase();
      return !q || name.includes(q) || email.includes(q) || phone.includes(q) || job.includes(q);
    });

  if (!filtered.length) {
    wrap.innerHTML = '<div class="empty-state"><h3>No referral candidates</h3></div>';
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
            <th>Stage</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          ${filtered.map((row) => {
            const c = row.candidate || {};
            const name = (c.user && c.user.name) || c.fullName || "Candidate";
            const email = (c.user && c.user.email) || "N/A";
            const phone = c.phone || c.mobileNumber || "N/A";
            const job = c.jd && c.jd.title ? c.jd.title : "Not mapped";
            const stage = normalizeStage(c).replace("_", " ");
            const status = (c.stageStatus || "PENDING").replace("_", " ");
            return `
              <tr>
                <td>${name}</td>
                <td>${email}</td>
                <td>${phone}</td>
                <td>${job}</td>
                <td><span class="badge">${stage}</span></td>
                <td><span class="badge">${status}</span></td>
              </tr>
            `;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function showReferralAlert(message, type) {
  const alert = document.getElementById("referralAlert");
  if (!alert) return;
  alert.className = `alert alert-${type}`;
  alert.textContent = message;
  alert.classList.remove("is-hidden");
}

function clearReferralForm() {
  ["refName", "refEmail", "refPhone", "refExperience", "refSource"].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });
  const jd = document.getElementById("refJd");
  if (jd) jd.value = "";
}

function loadJobs() {
  const select = document.getElementById("refJd");
  jdActions.list()
    .then((jobs) => {
      allJobs = Array.isArray(jobs) ? jobs : [];
      if (!select) return;
      select.innerHTML = '<option value="">Select job description</option>' +
        allJobs.map(job => `<option value="${job.id}">${job.title || "Untitled Job"}</option>`).join("");
    })
    .catch(() => {
      if (select) select.innerHTML = '<option value="">Failed to load jobs</option>';
    });
}

function createReferralCandidate() {
  const payload = {
    name: document.getElementById("refName").value.trim(),
    email: document.getElementById("refEmail").value.trim(),
    phone: document.getElementById("refPhone").value.trim(),
    jdId: Number(document.getElementById("refJd").value),
    experience: Number(document.getElementById("refExperience").value || 0),
    source: document.getElementById("refSource").value.trim() || "Referral"
  };

  if (!payload.name || !payload.email || !payload.phone || !payload.jdId) {
    showReferralAlert("Please fill name, email, phone, and job description.", "error");
    return;
  }

  const btn = document.getElementById("createReferralBtn");
  btn.disabled = true;
  btn.textContent = "Adding...";

  hrActions.createReferralCandidate(payload)
    .then(() => {
      showReferralAlert("Candidate added. Password setup email sent successfully.", "success");
      clearReferralForm();
      load();
    })
    .catch((err) => {
      showReferralAlert(err.message || "Failed to add referral candidate.", "error");
    })
    .finally(() => {
      btn.disabled = false;
      btn.textContent = "Add Candidate & Send Email";
    });
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

window.createReferralCandidate = createReferralCandidate;
window.clearOnboardFilters = clearOnboardFilters;
window.toggleSidebar = toggleSidebar;
window.logout = logout;

wire();
loadJobs();
load();
