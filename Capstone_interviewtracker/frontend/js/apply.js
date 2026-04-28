const user = getStoredUser();
const preselectedJdId = localStorage.getItem(STORAGE_KEYS.SELECTED_JD_ID);

if (!user) {
  window.location.href = "login.html";
}

// Load JDs into dropdown
function loadJDs() {
  jdActions.list()
    .then(data => {
      const select = document.getElementById("jdSelect");
      select.innerHTML = '<option value="">-- Select a job --</option>';

      data.forEach(jd => {
        const option = document.createElement("option");
        option.value = jd.id;
        option.textContent = `${jd.title} - ${jd.experience} yrs exp`;
        option.dataset.title = jd.title;
        option.dataset.description = jd.description;
        option.dataset.skills = jd.skills;
        select.appendChild(option);
      });

      if (preselectedJdId) {
        const hasOption = data.some(jd => String(jd.id) === String(preselectedJdId));
        if (hasOption) {
          select.value = String(preselectedJdId);
          updateJdInfo();
        }
      }
    })
    .catch(err => {
      console.error(err);
      showAlert("Error loading jobs. Please refresh.", "error");
    });
}

function updateJdInfo() {
  const select = document.getElementById("jdSelect");
  const selectedOption = select.options[select.selectedIndex];
  const title = selectedOption.dataset.title || "Select a job";
  document.getElementById("jdTitle").textContent = title;
}

function updateFileName() {
  const file = document.getElementById("resume").files[0];
  const fileNameEl = document.getElementById("fileName");
  if (file) {
    fileNameEl.textContent = `Selected: ${file.name}`;
  } else {
    fileNameEl.textContent = "";
  }
}

function validateApply() {
  const jdSelect = document.getElementById("jdSelect");
  const phone = document.getElementById("phone");
  const experience = document.getElementById("experience");
  const resume = document.getElementById("resume");
  let isValid = true;

  // Clear errors
  [jdSelect, phone, experience].forEach(el => {
    el.classList.remove("input-error");
    const err = el.nextElementSibling;
    if (err && err.classList.contains("error-message")) err.style.display = "none";
  });

  if (!jdSelect.value) {
    showError(jdSelect, "Please select a job");
    isValid = false;
  }

  if (!phone.value.trim()) {
    showError(phone, "Phone number is required");
    isValid = false;
  } else if (!/^\d{10,15}$/.test(phone.value.replace(/\D/g, ''))) {
    showError(phone, "Please enter a valid phone number");
    isValid = false;
  }

  if (!experience.value) {
    showError(experience, "Experience is required");
    isValid = false;
  }

  if (!resume.files[0]) {
    showAlert("Please upload your resume", "error");
    isValid = false;
  } else {
    const file = resume.files[0];
    const isPdf = file.type === "application/pdf" || file.name.toLowerCase().endsWith(".pdf");
    if (!isPdf) {
      showAlert("Resume must be a PDF file", "error");
      isValid = false;
    } else if (file.size > 5 * 1024 * 1024) {
      showAlert("Resume file size must be 5MB or less", "error");
      isValid = false;
    }
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

// Apply to job
function applyJob() {
  if (!user || !user.userId) {
    showAlert("Session expired. Please login again.", "error");
    return;
  }
  if (!validateApply()) return;

  showLoading(true);

  const candidate = {
    phone: document.getElementById("phone").value.trim(),
    experience: parseInt(document.getElementById("experience").value),
    status: "APPLIED",
    user: {
      id: user.userId
    },
    jd: {
      id: document.getElementById("jdSelect").value
    }
  };

  const file = document.getElementById("resume").files[0];

  const formData = new FormData();
  formData.append("candidate", new Blob([JSON.stringify(candidate)], {
    type: "application/json"
  }));
  formData.append("file", file);

  candidateActions.apply(formData)
  .then(data => {
    showLoading(false);
    showAlert("Application submitted successfully!", "success");
    localStorage.removeItem(STORAGE_KEYS.SELECTED_JD_ID);
    setTimeout(() => {
      window.location.href = "dashboard.html";
    }, 1500);
  })
  .catch(err => {
    showLoading(false);
    console.error(err);
    showAlert(err.message || "Error submitting application. Please try again.", "error");
  });
}

function showLoading(show) {
  const btn = document.querySelector("button");
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Submitting...';
    btn.disabled = true;
  } else {
    btn.textContent = "Submit Application";
    btn.disabled = false;
  }
}

// Initialize
loadJDs();