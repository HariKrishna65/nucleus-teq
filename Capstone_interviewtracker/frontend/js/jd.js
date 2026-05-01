const user = getStoredUser();
const urlParams = new URLSearchParams(window.location.search);
const viewMode = (urlParams.get("view") || "").toLowerCase();

if (!user) {
  window.location.href = "login.html";
}

if (user && user.role === "HR") {
  document.getElementById("hrDashboardBtn").classList.remove("is-hidden");
  document.getElementById("availableJobsBtn").classList.remove("is-hidden");
  document.getElementById("createJobBtn").classList.remove("is-hidden");
}

function setView(mode) {
  const form = document.getElementById("jdForm");
  const title = document.getElementById("jobsTitle");
  const subtitle = document.getElementById("jobsSubtitle");
  const isCreate = String(mode || "").toLowerCase() === "create";

  if (user && user.role === "HR") {
    form.classList.toggle("is-hidden", !isCreate);
    title.textContent = isCreate ? "Create Job" : "Available Positions";
    subtitle.textContent = isCreate ? "Post a new job opening." : "Browse open roles and apply.";
  } else {
    form.classList.add("is-hidden");
  }

  const params = new URLSearchParams(window.location.search);
  params.set("view", isCreate ? "create" : "available");
  window.history.replaceState({}, "", `${window.location.pathname}?${params.toString()}`);
}

function loadJDs() {
  const list = document.getElementById("jdList");
  
  jdActions.list()
    .then(data => {
      list.innerHTML = "";

      if (data.length === 0) {
        list.innerHTML = `
          <div class="empty-state">
            <div class="icon">Jobs</div>
            <h3>No jobs available</h3>
            <p>Check back later for new opportunities</p>
          </div>
        `;
        return;
      }

      data.forEach(jd => {
        const card = document.createElement("div");
        card.className = "jd-card";

        const expMin = jd.experienceMin ?? jd.experience ?? 0;
        const expMax = jd.experienceMax ?? jd.experience ?? 0;
        const salaryMin = jd.salaryMin ?? null;
        const salaryMax = jd.salaryMax ?? null;
        const salaryLabel = (salaryMin != null && salaryMax != null) ? `${salaryMin} - ${salaryMax}` : (jd.salary || "N/A");
        
        const actionsHtml = (user && user.role === "CANDIDATE")
          ? `<div class="jd-actions"><button class="apply-jd-btn btn-small" onclick="applyToJob(${jd.id}, '${jd.title}')">Apply</button></div>`
          : ``;

        card.innerHTML = `
          <div class="jd-info">
            <b>${jd.title}</b>
            <p>${jd.description || 'No description provided'}</p>
            <div class="jd-meta">
              <span class="meta-item">${expMin} - ${expMax} yrs</span>
              <span class="meta-item">Salary: ${salaryLabel}</span>
              <span class="meta-item">${jd.skills || 'N/A'}</span>
            </div>
          </div>
          ${actionsHtml}
        `;

        list.appendChild(card);
      });
    })
    .catch(err => {
      console.error("Error loading JDs", err);
      list.innerHTML = `
        <div class="alert alert-error">
          Error loading jobs. Please try again.
        </div>
      `;
    });
}

function validateJD() {
  const title = document.getElementById("title");
  const description = document.getElementById("description");
  const skills = document.getElementById("skills");
  const experienceMin = document.getElementById("experienceMin");
  const experienceMax = document.getElementById("experienceMax");
  const salaryMin = document.getElementById("salaryMin");
  const salaryMax = document.getElementById("salaryMax");
  let isValid = true;

  [title, description, skills, experienceMin, experienceMax, salaryMin, salaryMax].forEach(el => {
    el.classList.remove("input-error");
    const err = el.nextElementSibling;
    if (err && err.classList.contains("error-message")) err.style.display = "none";
  });

  if (!title.value.trim()) {
    showError(title, "Job title is required");
    isValid = false;
  }

  if (!description.value.trim()) {
    showError(description, "Description is required");
    isValid = false;
  } else {
    const words = description.value.trim().split(/\s+/).filter(Boolean).length;
    if (words < 10 || words > 50) {
      showError(description, "Job description must be 10 to 50 words");
      isValid = false;
    }
  }

  if (!skills.value.trim()) {
    showError(skills, "Skills are required");
    isValid = false;
  } else if (skills.value.trim().length === 0) {
    showError(skills, "Skills cannot be empty or contain only spaces");
    isValid = false;
  } else if (skills.value.trim().split(/\s+/).length < 2) {
    showError(skills, "Please enter at least 2 skills separated by spaces");
    isValid = false;
  }

  if (!experienceMin.value) {
    showError(experienceMin, "Min experience is required");
    isValid = false;
  } else if (Number(experienceMin.value) <= 0) {
    showError(experienceMin, "Min experience must be greater than 0");
    isValid = false;
  }
  
  if (!experienceMax.value) {
    showError(experienceMax, "Max experience is required");
    isValid = false;
  } else if (Number(experienceMax.value) <= 0) {
    showError(experienceMax, "Max experience must be greater than 0");
    isValid = false;
  }
  
  if (experienceMin.value && experienceMax.value && Number(experienceMin.value) >= Number(experienceMax.value)) {
    showError(experienceMax, "Max experience must be greater than min experience");
    isValid = false;
  }

  if (!salaryMin.value) {
    showError(salaryMin, "Min salary is required");
    isValid = false;
  } else if (Number(salaryMin.value) <= 0) {
    showError(salaryMin, "Min salary must be greater than 0");
    isValid = false;
  }
  
  if (!salaryMax.value) {
    showError(salaryMax, "Max salary is required");
    isValid = false;
  } else if (Number(salaryMax.value) <= 0) {
    showError(salaryMax, "Max salary must be greater than 0");
    isValid = false;
  }
  
  if (salaryMin.value && salaryMax.value && Number(salaryMin.value) >= Number(salaryMax.value)) {
    showError(salaryMax, "Max salary must be greater than min salary");
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
  
  if (type === "success") {
    setTimeout(() => alert.remove(), 3000);
  }
}

// Create JD (HR only)
function createJD() {
  if (!validateJD()) return;

  showLoading(true);

  const jd = {
    title: document.getElementById("title").value.trim(),
    description: document.getElementById("description").value.trim(),
    skills: document.getElementById("skills").value.trim(),
    experienceMin: parseInt(document.getElementById("experienceMin").value),
    experienceMax: parseInt(document.getElementById("experienceMax").value),
    salaryMin: parseInt(document.getElementById("salaryMin").value),
    salaryMax: parseInt(document.getElementById("salaryMax").value)
  };

  jdActions.create(jd)
  .then(data => {
    showLoading(false);
    showAlert("Job posted successfully!", "success");
    
    // Clear form
    document.getElementById("title").value = "";
    document.getElementById("description").value = "";
    document.getElementById("skills").value = "";
    document.getElementById("experienceMin").value = "";
    document.getElementById("experienceMax").value = "";
    document.getElementById("salaryMin").value = "";
    document.getElementById("salaryMax").value = "";
    
    loadJDs();
  })
  .catch(err => {
    showLoading(false);
    showAlert(err.message || "Error creating job. Please try again.", "error");
  });
}

function showLoading(show) {
  const btn = document.querySelector("#jdForm button");
  if (!btn) return;
  
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Posting...';
    btn.disabled = true;
  } else {
    btn.textContent = "Post Job";
    btn.disabled = false;
  }
}

// Apply to job from JD list
function applyToJob(jdId, jdTitle) {
  if (confirm(`Apply for "${jdTitle}"?`)) {
    localStorage.setItem(STORAGE_KEYS.SELECTED_JD_ID, jdId);
    window.location.href = "apply.html";
  }
}

// Load JDs on page load
loadJDs();

// Init view
(() => {
  const initial = (viewMode === "create") ? "create" : "available";
  if (user && user.role === "HR") {
    setView(initial);
  }
})();