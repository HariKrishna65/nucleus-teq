const user = getStoredUser();

// Check authentication
if (!user) {
  window.location.href = "login.html";
}

// Update user info in UI
document.getElementById("userName").textContent = user.name || "User";
document.getElementById("userAvatar").textContent = (user.name || "U").charAt(0).toUpperCase();

// Load applications with stats
function loadApplications() {
  const list = document.getElementById("applications");
  if (!user || !user.userId) {
    list.innerHTML = `
      <div class="alert alert-error">
        Session expired. Please login again.
      </div>
    `;
    return;
  }
  
  candidateActions.listByUser(user.userId)
    .then(data => {
      list.innerHTML = "";

      if (data.length === 0) {
        list.innerHTML = `
          <div class="empty-state">
            <div class="icon">Applications</div>
            <h3>No applications yet</h3>
            <p>Start applying to jobs to see them here</p>
            <a href="apply.html" class="btn apply-btn">Apply for Jobs</a>
          </div>
        `;
        return;
      }

      // Calculate stats using stage model (fallback to legacy status)
      let pending = 0, interview = 0, selected = 0;
      
      data.forEach(c => {
        const stage = normalizeStage(c);
        if (stage === "PROFILING" || stage === "SCREENING") pending++;
        if (stage === "L1_TECH" || stage === "L2_TECH" || stage === "HR_ROUND") interview++;
        if (stage === "SELECTED") selected++;
      });

      document.getElementById("totalApps").textContent = data.length;
      document.getElementById("pendingApps").textContent = pending;
      document.getElementById("interviewApps").textContent = interview;
      document.getElementById("selectedApps").textContent = selected;

      data.forEach(c => {
        const li = document.createElement("li");
        li.className = "application-card";
        
        li.innerHTML = `
          <div class="application-info">
            <b>${c.jd ? c.jd.title : 'Unknown Position'}</b>
            <span>Applied on: ${formatDate(c.applicationDate)}</span>
          </div>
          <span class="status ${normalizeStage(c)}">${formatStatus(c)}</span>
        `;

        list.appendChild(li);
      });
    })
    .catch(err => {
      console.error(err);
      list.innerHTML = `
        <div class="alert alert-error">
          Error loading applications. Please try again.
        </div>
      `;
    });
}

function formatDate(dateStr) {
  if (!dateStr) return "N/A";
  const date = new Date(dateStr);
  return date.toLocaleDateString("en-US", { year: 'numeric', month: 'short', day: 'numeric' });
}

function normalizeStage(candidate) {
  if (candidate.stage) return candidate.stage;
  if (candidate.status === "APPLIED") return "PROFILING";
  if (candidate.status === "L1") return "L1_TECH";
  if (candidate.status === "L2") return "L2_TECH";
  if (candidate.status === "HR") return "HR_ROUND";
  if (candidate.status === "SELECTED") return "SELECTED";
  if (candidate.status === "REJECTED") return "REJECTED";
  return "PROFILING";
}

function formatStatus(candidate) {
  const stage = normalizeStage(candidate);
  const stageStatus = candidate.stageStatus || "";
  const statusMap = {
    "PROFILING": "Profiling",
    "SCREENING": "Screening",
    "L1_TECH": "L1 Interview",
    "L2_TECH": "L2 Interview",
    "HR_ROUND": "HR Round",
    "REJECTED": "Rejected",
    "SELECTED": "Selected"
  };
  const label = statusMap[stage] || stage;
  if (stage === "REJECTED" || stage === "SELECTED") return label;
  return stageStatus ? `${label} (${String(stageStatus).replace("_", " ")})` : label;
}

function logout() {
  if (confirm("Are you sure you want to logout?")) {
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "login.html";
  }
}

// Initialize
loadApplications();