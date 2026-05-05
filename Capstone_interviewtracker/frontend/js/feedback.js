const user = getStoredUser();
let interviewStatus = "PENDING";
let interviewId = localStorage.getItem(STORAGE_KEYS.INTERVIEW_ID);

if (!user) {
  window.location.href = "login.html";
}

if (user && user.role === "HR") {
  window.location.href = "hr-feedback.html";
}

if (user && user.role !== "PANEL") {
  window.location.href = "index.html";
}

window.addEventListener("DOMContentLoaded", function () {
  if (!interviewId) {
    showInterviewSelection();
  } else {
    loadInterviewDetails();
  }
});

function showInterviewSelection() {
  const container = document.querySelector(".container");
  if (!container) return;

  container.innerHTML = `
    <div class="header">
      <div>
        <h2>Select an Interview</h2>
        <p class="pipeline-note">Choose one of your assigned interviews to submit or review feedback.</p>
      </div>
      <div class="header-actions">
        <a class="btn btn-outline" href="panel-dashboard.html">My Interviews</a>
      </div>
    </div>

    <div id="interviewList">
      <div class="loading">
        <div class="spinner"></div>
        <p>Loading interviews...</p>
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
            <div class="icon">Interviews</div>
            <h3>No interviews available</h3>
            <p>You don't have any assigned interviews.</p>
            <a href="panel-dashboard.html" class="btn">Back to Dashboard</a>
          </div>
        `;
        return;
      }

      const pending = interviews.filter(i => i.status !== "COMPLETED");
      const completed = interviews.filter(i => i.status === "COMPLETED");

      list.innerHTML = `
        ${pending.length > 0 ? `
          <h3 style="margin-bottom:12px; color:#1f2937;">Pending Feedback</h3>
          ${pending.map(interview => `
            <div class="interview-card selectable" onclick="selectInterview(${interview.id})">
              <div class="interview-info">
                <b>${interview.candidate?.user?.name || "Unknown Candidate"}</b>
                <span>Round: ${interview.round || "N/A"}</span>
                <span>Focus: ${interview.focusArea || "General"}</span>
                <span>Time: ${formatDateTime(interview.interviewTime)}</span>
                <div class="interview-meta">
                  <span class="meta-item">Pending</span>
                </div>
              </div>
              <button class="btn btn-small">Submit Feedback</button>
            </div>
          `).join("")}
        ` : '<p style="color:#6b7280;">No pending interviews.</p>'}

        ${completed.length > 0 ? `
          <h3 style="margin:20px 0 12px; color:#1f2937;">Completed</h3>
          ${completed.map(interview => `
            <div class="interview-card" style="opacity:0.7;">
              <div class="interview-info">
                <b>${interview.candidate?.user?.name || "Unknown Candidate"}</b>
                <span>Round: ${interview.round || "N/A"}</span>
                <span>Time: ${formatDateTime(interview.interviewTime)}</span>
                <div class="interview-meta">
                  <span class="meta-item">Completed</span>
                </div>
              </div>
              <button class="btn btn-small btn-secondary" onclick="selectInterview(${interview.id})">View Feedback</button>
            </div>
          `).join("")}
        ` : ""}
      `;
    })
    .catch(() => {
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

function isInterviewAssignedToCurrentUser(interview) {
  if (!interview || !user) return false;
  const currentUserEmail = (user.email || user.username || "").toLowerCase();
  const currentUserId = user.panelId || user.userId;

  if (currentUserId && (interview.panelId === currentUserId || interview.panel?.id === currentUserId)) {
    return true;
  }

  if (Array.isArray(interview.panelEmails) && currentUserEmail) {
    return interview.panelEmails.some(email => String(email || "").toLowerCase() === currentUserEmail);
  }

  if (interview.panel?.email && currentUserEmail) {
    return String(interview.panel.email).toLowerCase() === currentUserEmail;
  }

  return false;
}

function loadInterviewDetails() {
  if (!user || !user.userId) {
    showAlert("Session expired. Please login again.", "error");
    return;
  }

  interviewActions.getById(interviewId)
    .then(data => {
      if (!isInterviewAssignedToCurrentUser(data)) {
        showAlert("You are not assigned to this interview. Only assigned panel members may view or submit feedback.", "error");
        localStorage.removeItem(STORAGE_KEYS.INTERVIEW_ID);
        setTimeout(() => {
          window.location.href = "panel-dashboard.html";
        }, 2200);
        return;
      }

      interviewStatus = data.status || "PENDING";

      const candidateNameEl = document.getElementById("candidateName");
      const roundInfoEl = document.getElementById("roundInfo");
      const focusAreaEl = document.getElementById("focusArea");

      if (candidateNameEl) {
        candidateNameEl.textContent = data.candidate?.user?.name || "Unknown";
      }
      if (roundInfoEl) {
        roundInfoEl.textContent = data.round || "N/A";
      }
      if (focusAreaEl) {
        focusAreaEl.textContent = data.focusArea || "General";
      }

      if (interviewStatus === "COMPLETED") {
        loadExistingFeedback();
      }
    })
    .catch(() => {
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
      const submitBtn = document.getElementById("submitFeedbackBtn");
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = "Feedback Already Submitted";
      }

      showAlert("Feedback already submitted. View only.", "success");
    })
    .catch(() => {});
}

function selectRating(value) {
  document.querySelectorAll(".rating-option").forEach(el => {
    el.classList.remove("selected");
  });
  const selected = document.querySelector(`.rating-option[data-value="${value}"]`);
  if (selected) selected.classList.add("selected");
  document.getElementById("rating").value = value;
}

function selectStatus(value) {
  const statusInput = document.getElementById("status");
  if (!statusInput) return;
  document.querySelectorAll(".status-option").forEach(el => {
    el.classList.remove("selected");
  });
  const selected = document.querySelector(`.status-option[data-value="${value}"]`);
  if (selected) selected.classList.add("selected");
  statusInput.value = value;
}

function validateFeedback() {
  const rating = document.getElementById("rating").value;
  const comments = document.getElementById("comments").value.trim();
  let isValid = true;

  if (!rating) {
    showAlert("Please select a rating", "error");
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
    rating: parseInt(document.getElementById("rating").value, 10),
    comments: document.getElementById("comments").value.trim(),
    result: "SUBMITTED",
    interview: {
      id: parseInt(interviewId, 10)
    }
  };

  feedbackActions.submit(feedback)
    .then(() => {
      showLoading(false);
      showAlert("Feedback submitted successfully.", "success");
      localStorage.removeItem(STORAGE_KEYS.INTERVIEW_ID);
      setTimeout(() => {
        window.location.href = "panel-dashboard.html";
      }, 1500);
    })
    .catch(err => {
      showLoading(false);
      showAlert(err.message || "Error submitting feedback.", "error");
    });
}

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

function logout() {
  if (confirm("Are you sure you want to logout?")) {
    localStorage.removeItem(STORAGE_KEYS.USER);
    window.location.href = "login.html";
  }
}

window.selectRating = selectRating;
window.selectStatus = selectStatus;
window.submitFeedback = submitFeedback;
window.logout = logout;
window.selectInterview = selectInterview;
