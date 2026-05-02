const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let candidateId = null;
let candidateData = null;
let allPanels = [];

function getCandidateId() {
  const urlParams = new URLSearchParams(window.location.search);
  return urlParams.get('candidateId');
}

function loadCandidateDetails() {
  candidateId = getCandidateId();
  if (!candidateId) {
    showError("No candidate ID provided");
    return;
  }

  hrActions.getCandidateDetails(candidateId)
    .then(data => {
      candidateData = data;
      displayCandidateInfo();
      loadPanels();
    })
    .catch(err => {
      console.error("Error loading candidate details:", err);
      showError("Failed to load candidate details");
    });
}

function displayCandidateInfo() {
  const detailsDiv = document.getElementById('candidateDetails');
  const candidateName = document.getElementById('candidateName');

  if (!candidateData) return;

  const candidate = candidateData.candidate || {};
  const name = (candidate.user && candidate.user.name) || candidate.fullName || "Unknown";
  const email = (candidate.user && candidate.user.email) || "N/A";
  const phone = candidate.phone || candidate.mobileNumber || "N/A";
  const job = candidate.jd && candidate.jd.title ? candidate.jd.title : "Not mapped";
  const stage = normalizeStage(candidate);

  candidateName.textContent = `Assign Panel - ${name}`;

  detailsDiv.innerHTML = `
    <div class="candidate-info-grid">
      <div class="info-item">
        <label>Name:</label>
        <span>${name}</span>
      </div>
      <div class="info-item">
        <label>Email:</label>
        <span>${email}</span>
      </div>
      <div class="info-item">
        <label>Phone:</label>
        <span>${phone}</span>
      </div>
      <div class="info-item">
        <label>Job Title:</label>
        <span>${job}</span>
      </div>
      <div class="info-item">
        <label>Current Stage:</label>
        <span class="badge">${stage.replace('_', ' ')}</span>
      </div>
    </div>
  `;
}

// Load available panel members
function loadPanels() {
  interviewActions.listPanels()
    .then(panels => {
      allPanels = Array.isArray(panels) ? panels : [];
      populatePanelSelects();
    })
    .catch(err => {
      console.error("Error loading panels:", err);
      allPanels = [];
    });
}

// Populate panel member select dropdowns
function populatePanelSelects() {
  const select1 = document.getElementById('panelEmail1');
  const select2 = document.getElementById('panelEmail2');

  const panelOptions = allPanels
    .filter(panel => panel && panel.email)
    .map(panel => ({
      value: panel.email,
      label: `${panel.name || 'Panel Member'} (${panel.email})`
    }));

  const optionsHTML = panelOptions.map(option =>
    `<option value="${option.value}">${option.label}</option>`
  ).join('');

  select1.innerHTML = '<option value="">-- Select panel member --</option>' + optionsHTML;
  select2.innerHTML = '<option value="">-- Optional second member --</option>' + optionsHTML;
}

// Normalize stage names
function normalizeStage(candidate) {
  if (candidate.stage) return candidate.stage;
  if (candidate.status === "L1") return "L1_TECH";
  if (candidate.status === "L2") return "L2_TECH";
  if (candidate.status === "HR") return "HR_ROUND";
  return candidate.stage || "UNKNOWN";
}

// ✅ Get round based on candidate stage
function getRoundFromStage(candidate) {
  const stage = normalizeStage(candidate);
  switch (stage) {
    case "L1_TECH": return "L1";
    case "L2_TECH": return "L2";
    case "HR_ROUND": return "HR";
    default: return "L1";
  }
}

// Validate form inputs
function validateForm() {
  const panelEmail1 = document.getElementById('panelEmail1').value.trim();
  const panelEmail2 = document.getElementById('panelEmail2').value.trim();
  const interviewDate = document.getElementById('interviewDate').value;
  const interviewTime = document.getElementById('interviewTime').value;

  if (!panelEmail1) {
    showError("Panel member 1 is required");
    return false;
  }

  if (panelEmail2 && panelEmail2 === panelEmail1) {
    showError("Panel member 2 must be different from panel member 1");
    return false;
  }

  if (!interviewDate) {
    showError("Interview date is required");
    return false;
  }

  if (!interviewTime) {
    showError("Interview time is required");
    return false;
  }

  // Validate date is not in the past
  const selectedDate = new Date(`${interviewDate}T${interviewTime}`);
  const now = new Date();
  if (selectedDate <= now) {
    showError("Interview date and time must be in the future");
    return false;
  }

  return true;
}

// Submit panel assignment
function submitAssignPanel() {
  if (!validateForm()) {
    return;
  }

  const panelEmail1 = document.getElementById('panelEmail1').value.trim();
  const panelEmail2 = document.getElementById('panelEmail2').value.trim();
  const interviewDate = document.getElementById('interviewDate').value;
  const interviewTime = document.getElementById('interviewTime').value;
  const duration = document.getElementById('interviewDuration').value;
  const interviewType = document.getElementById('interviewType').value;
  const focusArea = document.getElementById('focusArea').value.trim();
  const interviewNotes = document.getElementById('interviewNotes').value.trim();

  // Combine date and time
  const interviewDateTime = new Date(`${interviewDate}T${interviewTime}`);

  const panelEmails = [panelEmail1, panelEmail2].filter(Boolean);

  // ✅ Get round from candidate stage
  const candidate = candidateData ? (candidateData.candidate || {}) : {};
  const round = getRoundFromStage(candidate);

  const assignData = {
    panelEmails: panelEmails,
    interviewTime: interviewDateTime.toISOString(),
    duration: parseInt(duration),
    interviewType: interviewType,
    focusArea: focusArea || null,
    notes: interviewNotes || null,
    round: round  // ✅ Round auto-set from stage
  };

  console.log("Assigning panel with data:", assignData);

  const submitBtn = document.getElementById('assignPanelSubmitBtn');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Assigning...';

  hrActions.assignPanel(candidateId, assignData)
    .then(() => {
      alert(`Panel assigned successfully! Round: ${round}. Emails sent to panel members and candidate.`);
      window.location.href = 'candidates.html';
    })
    .catch(err => {
      console.error('Error assigning panel:', err);
      showError(err.message || 'Failed to assign panel. Please try again.');
    })
    .finally(() => {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Assign Panel & Send Emails';
    });
}

// Show error message
function showError(message) {
  const errorDiv = document.getElementById('assignPanelError');
  if (errorDiv) {
    errorDiv.textContent = message;
    errorDiv.classList.remove('is-hidden');
  }
}

// Clear error message
function clearError() {
  const errorDiv = document.getElementById('assignPanelError');
  if (errorDiv) {
    errorDiv.classList.add('is-hidden');
  }
}

// Logout function
function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}

window.logout = logout;

// Initialize page
document.addEventListener('DOMContentLoaded', function () {
  loadCandidateDetails();

  // Set minimum date to today
  const dateInput = document.getElementById('interviewDate');
  if (dateInput) {
    const today = new Date().toISOString().split('T')[0];
    dateInput.setAttribute('min', today);
  }

  // Clear error on input change
  const inputs = document.querySelectorAll('input, select, textarea');
  inputs.forEach(input => {
    input.addEventListener('change', clearError);
  });
});