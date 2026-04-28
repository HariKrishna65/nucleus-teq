const user = JSON.parse(localStorage.getItem("user"));

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

      // Calculate stats
      let pending = 0, interview = 0, selected = 0;
      
      data.forEach(c => {
        if (c.status === "APPLIED") pending++;
        if (c.status === "L1" || c.status === "L2") interview++;
        if (c.status === "HR") selected++;
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
          <span class="status ${c.status}">${formatStatus(c.status)}</span>
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

function formatStatus(status) {
  const statusMap = {
    "APPLIED": "Applied",
    "L1": "L1 Interview",
    "L2": "L2 Interview",
    "HR": "HR Round",
    "REJECTED": "Rejected",
    "SELECTED": "Selected"
  };
  return statusMap[status] || status;
}

function logout() {
  if (confirm("Are you sure you want to logout?")) {
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "login.html";
  }
}

// Initialize
loadApplications();