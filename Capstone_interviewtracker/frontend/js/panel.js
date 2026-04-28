const user = getStoredUser();

// Check authentication
if (!user) {
  window.location.href = "login.html";
}

// Check role
if (user && user.role !== "PANEL") {
  showAlert("Access denied. Panel members only.", "error");
  setTimeout(() => window.location.href = "index.html", 2000);
}

// Update user info
if (user) {
  document.getElementById("userName").textContent = user.name || "Panel Member";
  document.getElementById("userAvatar").textContent = (user.name || "P").charAt(0).toUpperCase();
}

// Load interviews assigned to panel
function loadInterviews() {
  const list = document.getElementById("interviewList");
  if (!user || !user.userId) {
    list.innerHTML = `<div class="alert alert-error">Session expired. Please login again.</div>`;
    return;
  }
  const panelId = user.panelId || user.userId;
  
  interviewActions.listByPanel(panelId)
    .then(data => {
      list.innerHTML = "";

      if (data.length === 0) {
        list.innerHTML = `
          <div class="empty-state">
            <div class="icon">Interviews</div>
            <h3>No interviews assigned</h3>
            <p>You don't have any interviews to conduct yet</p>
          </div>
        `;
        return;
      }

      // Calculate stats
      let pending = 0, completed = 0;
      
      data.forEach(i => {
        if (i.status === "PENDING") pending++;
        if (i.status === "COMPLETED") completed++;
      });

      document.getElementById("totalInterviews").textContent = data.length;
      document.getElementById("pendingInterviews").textContent = pending;
      document.getElementById("completedInterviews").textContent = completed;

      data.forEach(i => {
        const li = document.createElement("li");
        li.className = "interview-card";
        const hasFeedback = i.status === "COMPLETED";
        
        li.innerHTML = `
          <div class="interview-info">
            <b>${i.candidate ? i.candidate.user ? i.candidate.user.name : 'Unknown' : 'Unknown Candidate'}</b>
            <span>Round: ${i.round || 'N/A'}</span>
            <span>Focus: ${i.focusArea || 'General'}</span>
            <span>Time: ${formatDateTime(i.interviewTime)}</span>
            <div class="interview-meta">
              <span class="meta-item status ${i.status || 'PENDING'}">${formatStatus(i.status)}</span>
            </div>
          </div>
          <button class="btn btn-small" onclick="giveFeedback(${i.id})">
            ${hasFeedback ? 'View Feedback' : 'Submit Feedback'}
          </button>
        `;

        list.appendChild(li);
      });
    })
    .catch(err => {
      console.error(err);
      list.innerHTML = `
        <div class="alert alert-error">
          Error loading interviews. Please try again.
        </div>
      `;
    });
}

function formatDateTime(dateTime) {
  if (!dateTime) return "Not scheduled";
  const date = new Date(dateTime);
  return date.toLocaleString("en-US", { 
    month: "short", 
    day: "numeric", 
    hour: "2-digit", 
    minute: "2-digit" 
  });
}

function formatStatus(status) {
  const statusMap = {
    "PENDING": "Pending",
    "IN_PROGRESS": "In Progress",
    "COMPLETED": "Completed",
    "CANCELLED": "Cancelled"
  };
  return statusMap[status] || status || "Pending";
}

function showAlert(message, type) {
  const existing = document.querySelector(".alert");
  if (existing) existing.remove();

  const alert = document.createElement("div");
  alert.className = `alert alert-${type}`;
  alert.textContent = message;

  const container = document.querySelector(".container");
  container.insertBefore(alert, container.firstChild);
  
  if (type === "error") {
    setTimeout(() => alert.remove(), 5000);
  }
}

// Redirect to feedback page
function giveFeedback(interviewId) {
  localStorage.setItem("interviewId", interviewId);
  window.location.href = "feedback.html";
}

function logout() {
  if (confirm("Are you sure you want to logout?")) {
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "login.html";
  }
}

// Initialize
loadInterviews();