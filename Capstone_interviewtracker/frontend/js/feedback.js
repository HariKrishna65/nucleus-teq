const user = getStoredUser();
let interviewStatus = "PENDING";
let interviewId = localStorage.getItem(STORAGE_KEYS.INTERVIEW_ID);

// Check authentication
if (!user) {
  window.location.href = "login.html";
}

if (user && user.role === "HR") {
  window.location.href = "hr-feedback.html";
}

if (user && user.role !== "PANEL") {
  window.location.href = "index.html";
}

// ✅ FIX: return ki jagah yeh use karo
window.addEventListener("DOMContentLoaded", function () {
  if (!interviewId) {
    showInterviewSelection();
  } else {
    loadInterviewDetails();
  }
});

// ==========================================
// INTERVIEW SELECTION PAGE
// ==========================================

function showInterviewSelection() {
  const container = document.querySelector(".container");
  if (!container) return;

  container.innerHTML = `
    <div class="header">
      <div class="user-info">
        <div class="user-avatar">${(user.name || "P").charAt(0).toUpperCase()}</div>
        <div class="user-details">
          <h3>${user.name || "Panel Member"}</h3>
          <p>Interview Panel</p>
        </div>
      </div>
      <div class="header-actions">
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

      if (!list) return;

      if (interviews.length === 0) {
        list.innerHTML = `
          <div class="empty-state">
            <div class="icon">📋</div>
            <h3>No interviews available</h3>
            <p>You don't have any assigned interviews.</p>
            <a href="panel-dashboard.html" class="btn">Back to Dashboard</a>
          </div>
        `;
        return;
      }

      // ✅ FIX: Only show PENDING interviews for feedback
      const pending = interviews.filter(i => i.status !== "COMPLETED");
      const completed = interviews.filter(i => i.status === "COMPLETED");

      list.innerHTML = `
        ${pending.length > 0 ? `
          <h3 style="margin-bottom:12px; color:#1f2937;">Pending Feedback</h3>
          ${pending.map(interview => `
            <div class="interview-card selectable" onclick="selectInterview(${interview.id})">
              <div class="interview-info">
                <b>${interview.candidate?.user?.name || 'Unknown Candidate'}</b>
                <span>Round: ${interview.round || 'N/A'}</span>
                <span>Focus: ${interview.focusArea || 'General'}</span>
                <span>Time: ${formatDateTime(interview.interviewTime)}</span>
                <div class="interview-meta">
                  <span class="meta-item">⏳ Pending</span>
                </div>
              </div>
              <button class="btn btn-small">Submit Feedback</button>
            </div>
          `).join('')}
        ` : '<p style="color:#6b7280;">No pending interviews.</p>'}

        ${completed.length > 0 ? `
          <h3 style="margin:20px 0 12px; color:#1f2937;">Completed</h3>
          ${completed.map(interview => `
            <div class="interview-card" style="opacity:0.7;">
              <div class="interview-info">
                <b>${interview.candidate?.user?.name || 'Unknown Candidate'}</b>
                <span>Round: ${interview.round || 'N/A'}</span>
                <span>Time: ${formatDateTime(interview.interviewTime)}</span>
                <div class="interview-meta">
                  <span class="meta-item">✅ Completed</span>
                </div>
              </div>
              <button class="btn btn-small btn-secondary" 
                onclick="selectInterview(${interview.id})">View Feedback</button>
            </div>
          `).join('')}
        ` : ''}
      `;
    })
    .catch(err => {
      console.error(err);
      const list = document.getElementById("interviewList");
      if (list) {
        list.innerHTML = `
          <div class="alert alert-error">Error loading interviews. Please try again.</div>
        `;
      }
    });
}

function selectInterview(id) {
  localStorage.setItem(STORAGE_KEYS.INTERVIEW_ID, id);
  interviewId = id;
  loadInterviewDetails();
}

// ==========================================
// FEEDBACK FORM
// ==========================================

function loadInterviewDetails() {
  if (!user || !user.userId) {
    showAlert("Session expired. Please login again.", "error");
    return;
  }

  interviewActions.getById(interviewId)
    .then(data => {
      interviewStatus = data.status || "PENDING";

      // ✅ FIX: Check elements exist before setting
      const candidateNameEl = document.getElementById("candidateName");
      const roundInfoEl = document.getElementById("roundInfo");
      const focusAreaEl = document.getElementById("focusArea");

      if (candidateNameEl) {
        candidateNameEl.textContent =
          data.candidate?.user?.name || "Unknown";
      }
      if (roundInfoEl) {
        roundInfoEl.textContent = data.round || "N/A";
      }
      if (focusAreaEl) {
        focusAreaEl.textContent = data.focusArea || "General";
      }

      // ✅ Show form if elements exist
      const form = document.getElementById("feedbackForm");
      if (form) form.classList.remove("is-hidden");

      if (interviewStatus === "COMPLETED") {
        loadExistingFeedback();
      }
    })
    .catch(err => {
      console.error(err);
      showAlert("Error loading interview details.", "error");
    });
}

function loadExistingFeedback() {
  feedbackActions.getByInterview(interviewId)
    .then(existing => {
      if (!existing) return;

      if (existing.rating) selectRating(existing.rating);
      if (existing.result || existing.status) {
        selectStatus(existing.result || existing.status);
      }

      const commentsEl = document.getElementById("comments");
      if (commentsEl) {
        commentsEl.value = existing.comments || "";
        commentsEl.setAttribute("readonly", "readonly");
      }

      document.querySelectorAll(".rating-option").forEach(el => {
        el.style.pointerEvents = "none";
      });
      document.querySelectorAll(".status-option").forEach(el => {
        el.style.pointerEvents = "none";
      });

      // ✅ FIX: Correct button selector
      const submitBtn = document.getElementById("submitFeedbackBtn");
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = "Feedback Already Submitted";
      }

      showAlert("Feedback already submitted — view only.", "success");
    })
    .catch(() => {
      // No feedback yet — form stays enabled
    });
}

// ==========================================
// RATING & STATUS SELECTION
// ==========================================

function selectRating(value) {
  document.querySelectorAll(".rating-option").forEach(el => {
    el.classList.remove("selected");
  });
  const selected = document.querySelector(`.rating-option[data-value="${value}"]`);
  if (selected) selected.classList.add("selected");
  document.getElementById("rating").value = value;
}

function selectStatus(value) {
  document.querySelectorAll(".status-option").forEach(el => {
    el.classList.remove("selected");
  });
  const selected = document.querySelector(`.status-option[data-value="${value}"]`);
  if (selected) selected.classList.add("selected");
  document.getElementById("status").value = value;
}

// ==========================================
// SUBMIT FEEDBACK
// ==========================================

function validateFeedback() {
  const rating = document.getElementById("rating").value;
  const status = document.getElementById("status").value;
  const comments = document.getElementById("comments").value.trim();
  let isValid = true;

  if (!rating) {
    showAlert("Please select a rating", "error");
    isValid = false;
  } else if (!status) {
    showAlert("Please select a decision", "error");
    isValid = false;
  } else if (!comments) {
    showAlert("Please provide comments", "error");
    isValid = false;
  } else if (comments.length < 10) {
    showAlert("Comments must be at least 10 characters", "error");
    isValid = false;
  }

  return isValid;
}

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
    .then(() => {
      showLoading(false);
      showAlert("Feedback submitted successfully! ✅", "success");
      localStorage.removeItem(STORAGE_KEYS.INTERVIEW_ID);
      setTimeout(() => {
        window.location.href = "panel-dashboard.html";
      }, 1500);
    })
    .catch(err => {
      showLoading(false);
      console.error("Feedback error:", err);
      showAlert(err.message || "Error submitting feedback.", "error");
    });
}

// ==========================================
// HELPERS
// ==========================================

function showLoading(show) {
  const btn = document.getElementById("submitFeedbackBtn");
  if (!btn) return;
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Submitting...';
    btn.disabled = true;
  } else {
    btn.textContent = "Submit Feedback";
    btn.disabled = false;
  }
}

function showAlert(message, type) {
  const existing = document.querySelector(".alert");
  if (existing) existing.remove();

  const alert = document.createElement("div");
  alert.className = `alert alert-${type}`;
  alert.textContent = message;

  const container = document.querySelector(".container");
  if (container) container.insertBefore(alert, container.firstChild);

  if (type === "error") {
    setTimeout(() => alert.remove(), 5000);
  }
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
  const map = {
    PENDING: "Pending",
    IN_PROGRESS: "In Progress",
    COMPLETED: "Completed",
    CANCELLED: "Cancelled"
  };
  return map[status] || status || "Pending";
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

window.selectRating = selectRating;
window.selectStatus = selectStatus;
window.submitFeedback = submitFeedback;
window.logout = logout;
window.toggleSidebar = toggleSidebar;
window.selectInterview = selectInterview;