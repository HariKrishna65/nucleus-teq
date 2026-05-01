const user = getStoredUser();
const interviewId = localStorage.getItem(STORAGE_KEYS.INTERVIEW_ID);
let interviewStatus = "PENDING";

// Check authentication
if (!user) {
  window.location.href = "login.html";
}

// Only panel can use this page. HR should use the HR feedback page.
if (user && user.role === "HR") {
  window.location.href = "hr-feedback.html";
}
if (user && user.role !== "PANEL") {
  window.location.href = "index.html";
}

if (!interviewId) {
  // Show interview selection interface instead of redirecting
  showInterviewSelection();
  return;
}

// Show interview selection interface
function showInterviewSelection() {
  const container = document.querySelector(".container");
  container.innerHTML = `
    <div class="header">
      <div class="user-info">
        <div class="user-avatar" id="userAvatar">P</div>
        <div class="user-details">
          <h3 id="userName">${user.name || "Panel Member"}</h3>
          <p>Interview Panel</p>
        </div>
      </div>
      <div class="header-actions">
        <button class="icon-btn" type="button" onclick="toggleSidebar()" aria-label="Toggle sidebar">☰</button>
        <a class="btn apply-more-btn" href="panel-dashboard.html">My Interviews</a>
        <button class="logout-btn" onclick="logout()">Logout</button>
      </div>
    </div>

    <div class="interview-selection">
      <h2>Select an Interview for Feedback</h2>
      <div id="interviewList">
        <div class="loading">
          <div class="spinner"></div>
          <p>Loading interviews...</p>
        </div>
      </div>
    </div>
  `;
  
  // Load interviews for selection
  loadInterviewsForSelection();
}

function loadInterviewsForSelection() {
  if (!user || !user.userId) {
    document.getElementById("interviewList").innerHTML = `
      <div class="alert alert-error">Session expired. Please login again.</div>
    `;
    return;
  }
  
  const panelId = user.panelId || user.userId;
  
  interviewActions.listByPanel(panelId)
    .then(data => {
      const interviews = Array.isArray(data) ? data : [];
      const list = document.getElementById("interviewList");
      
      if (interviews.length === 0) {
        list.innerHTML = `
          <div class="empty-state">
            <div class="icon">Interviews</div>
            <h3>No interviews available</h3>
            <p>You don't have any assigned interviews that need feedback.</p>
            <a href="panel-dashboard.html" class="btn">Back to Dashboard</a>
          </div>
        `;
        return;
      }
      
      list.innerHTML = interviews.map(interview => `
        <div class="interview-card selectable" onclick="selectInterview(${interview.id})">
          <div class="interview-info">
            <b>${interview.candidate ? interview.candidate.user ? interview.candidate.user.name : 'Unknown' : 'Unknown Candidate'}</b>
            <span>Round: ${interview.round || 'N/A'}</span>
            <span>Focus: ${interview.focusArea || 'General'}</span>
            <span>Time: ${formatDateTime(interview.interviewTime)}</span>
            <div class="interview-meta">
              <span class="meta-item status ${interview.status || 'PENDING'}">${formatStatus(interview.status)}</span>
            </div>
          </div>
          <button class="btn btn-small">Select</button>
        </div>
      `).join('');
    })
    .catch(err => {
      console.error(err);
      document.getElementById("interviewList").innerHTML = `
        <div class="alert alert-error">
          Error loading interviews. Please try again.
        </div>
      `;
    });
}

function selectInterview(id) {
  localStorage.setItem("interviewId", id);
  window.location.reload(); // Reload to show feedback form
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

// Load interview details
function loadInterviewDetails() {
  if (!user || !user.userId) {
    showAlert("Session expired. Please login again.", "error");
    return;
  }
  interviewActions.getById(interviewId)
    .then(data => {
      interviewStatus = data.status || "PENDING";
      document.getElementById("candidateName").textContent = 
        data.candidate && data.candidate.user ? data.candidate.user.name : "Unknown";
      document.getElementById("roundInfo").textContent = data.round || "N/A";
      document.getElementById("focusArea").textContent = data.focusArea || "General";
      if (interviewStatus === "COMPLETED") {
        loadExistingFeedback();
      }
    })
    .catch(err => {
      console.error(err);
      document.getElementById("candidateName").textContent = "Error loading";
    });
}

function loadExistingFeedback() {
  feedbackActions.getByInterview(interviewId)
    .then(existing => {
      if (!existing) return;
      if (existing.rating) selectRating(existing.rating);
      if (existing.result || existing.status) selectStatus(existing.result || existing.status);
      document.getElementById("comments").value = existing.comments || "";

      document.getElementById("comments").setAttribute("readonly", "readonly");
      document.querySelectorAll(".rating-option").forEach(el => {
        el.style.pointerEvents = "none";
      });
      document.querySelectorAll(".status-option").forEach(el => {
        el.style.pointerEvents = "none";
      });

      const submitBtn = document.querySelector(".container > button");
      submitBtn.disabled = true;
      submitBtn.textContent = "Feedback Already Submitted";
      showAlert("Existing feedback loaded in view mode.", "success");
    })
    .catch(() => {
      // Keep form enabled if no previous feedback exists.
    });
}

function selectRating(value) {
  // Update UI
  document.querySelectorAll(".rating-option").forEach(el => {
    el.classList.remove("selected");
  });
  document.querySelector(`.rating-option[data-value="${value}"]`).classList.add("selected");
  
  // Set hidden input
  document.getElementById("rating").value = value;
}

function selectStatus(value) {
  // Update UI
  document.querySelectorAll(".status-option").forEach(el => {
    el.classList.remove("selected");
  });
  document.querySelector(`.status-option[data-value="${value}"]`).classList.add("selected");
  
  // Set hidden input
  document.getElementById("status").value = value;
}

function validateFeedback() {
  const rating = document.getElementById("rating");
  const status = document.getElementById("status");
  const comments = document.getElementById("comments");
  let isValid = true;

  // Clear errors
  [rating, status, comments].forEach(el => el.classList.remove("input-error"));

  if (!rating.value) {
    showError(rating, "Please select a rating");
    isValid = false;
  }

  if (!status.value) {
    showError(status, "Please select a decision");
    isValid = false;
  }

  if (!comments.value.trim()) {
    showError(comments, "Please provide comments");
    isValid = false;
  } else if (comments.value.trim().length < 10) {
    showError(comments, "Please provide more detailed feedback (at least 10 characters)");
    isValid = false;
  }

  return isValid;
}

function showError(input, message) {
  input.classList.add("input-error");
  let errorDiv = input.nextElementSibling;
  if (!errorDiv || !errorDiv.classList.contains("error-message")) {
    errorDiv = document.createElement("div");
    errorDiv.className = "error-message";
    input.parentNode.insertBefore(errorDiv, input.nextSibling);
  }
  errorDiv.textContent = message;
  errorDiv.style.display = "block";
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

// Submit feedback
function submitFeedback() {
  if (interviewStatus === "COMPLETED") {
    showAlert("This interview is already completed. Feedback is view-only.", "error");
    return;
  }
  if (!validateFeedback()) return;

  showLoading(true);

  const feedback = {
    rating: parseInt(document.getElementById("rating").value),
    comments: document.getElementById("comments").value.trim(),
    result: document.getElementById("status").value,
    interview: {
      id: parseInt(interviewId)
    }
  };

  feedbackActions.submit(feedback)
  .then(data => {
    showLoading(false);
    showAlert("Feedback submitted successfully!", "success");
    
    // Clear interview ID
    localStorage.removeItem(STORAGE_KEYS.INTERVIEW_ID);
    
    // Redirect after delay
    setTimeout(() => {
      window.location.href = "panel-dashboard.html";
    }, 1500);
  })
  .catch(err => {
    showLoading(false);
    console.error("Feedback submission error:", err);
    const errorMessage = err.message || "Error submitting feedback. Please try again.";
    showAlert(errorMessage, "error");
  });
}

function showLoading(show) {
  const btn = document.querySelector("button");
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Submitting...';
    btn.disabled = true;
  } else {
    btn.textContent = "Submit Feedback";
    btn.disabled = false;
  }
}

function logout() {
  if (confirm("Are you sure you want to logout?")) {
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "login.html";
  }
}

function toggleSidebar() {
  const shell = document.querySelector(".dashboard-shell");
  if (!shell) return;
  shell.classList.toggle("sidebar-collapsed");
}

// Initialize
if (interviewId) {
  loadInterviewDetails();
}