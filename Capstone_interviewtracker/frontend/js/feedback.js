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
  showAlert("No interview selected", "error");
  setTimeout(() => window.location.href = "panel-dashboard.html", 2000);
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
      if (existing.status) selectStatus(existing.status);
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
    status: document.getElementById("status").value,
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
    showAlert("Error submitting feedback. Please try again.", "error");
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

// Initialize
loadInterviewDetails();