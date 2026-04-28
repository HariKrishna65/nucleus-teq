const user = JSON.parse(localStorage.getItem("user"));

// Check authentication
if (!user) {
  window.location.href = "login.html";
}

// Hide form if not HR
if (user.role === "HR") {
  document.getElementById("jdForm").style.display = "block";
}

// Fetch all JDs
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
        const li = document.createElement("li");
        li.className = "jd-card";
        
        li.innerHTML = `
          <div class="jd-info">
            <b>${jd.title}</b>
            <p>${jd.description || 'No description provided'}</p>
            <div class="jd-meta">
              <span class="meta-item">${jd.experience} years</span>
              <span class="meta-item">${jd.skills || 'N/A'}</span>
            </div>
          </div>
          ${user.role === 'CANDIDATE' ? `<button class="apply-jd-btn" onclick="applyToJob(${jd.id}, '${jd.title}')">Apply</button>` : ''}
        `;

        list.appendChild(li);
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
  const experience = document.getElementById("experience");
  let isValid = true;

  [title, description, skills, experience].forEach(el => {
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
  }

  if (!skills.value.trim()) {
    showError(skills, "Skills are required");
    isValid = false;
  }

  if (!experience.value) {
    showError(experience, "Experience is required");
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
    experience: parseInt(document.getElementById("experience").value)
  };

  jdActions.create(jd)
  .then(data => {
    showLoading(false);
    showAlert("Job posted successfully!", "success");
    
    // Clear form
    document.getElementById("title").value = "";
    document.getElementById("description").value = "";
    document.getElementById("skills").value = "";
    document.getElementById("experience").value = "";
    
    loadJDs();
  })
  .catch(err => {
    showLoading(false);
    showAlert("Error creating job. Please try again.", "error");
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
    // Store the JD ID and redirect to apply page
    localStorage.setItem("selectedJdId", jdId);
    window.location.href = "apply.html";
  }
}

// Load JDs on page load
loadJDs();