document.addEventListener('DOMContentLoaded', function() {
  const user = getStoredUser();

  if (!user) {
    window.location.href = "login.html";
    return;
  }

  configureRoleLayout(user);

  wireSkillInput();

  if (user && user.role === "HR") {
    const availableJobsBtn = document.getElementById("availableJobsBtn");
    const createJobBtn = document.getElementById("createJobBtn");

    if (availableJobsBtn) availableJobsBtn.classList.remove("is-hidden");
    if (createJobBtn) createJobBtn.classList.remove("is-hidden");

    initializeJDPage();
  } else {
    ["createJobBtn", "availableJobsBtn"].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.style.display = "none";
    });
    loadJDs();
  }
});

let selectedSkills = [];

function wireSkillInput() {
  const entry = document.getElementById("skillEntry");
  const wrap = document.getElementById("skillInputWrap");
  if (!entry) return;

  entry.addEventListener("keydown", (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    addSkill(entry.value);
    entry.value = "";
  });

  entry.addEventListener("blur", () => {
    addSkill(entry.value);
    entry.value = "";
  });

  if (wrap) {
    wrap.addEventListener("click", () => entry.focus());
  }
}

function addSkill(value) {
  const skill = String(value || "").trim();
  if (!skill) return;
  if (selectedSkills.some(existing => existing.toLowerCase() === skill.toLowerCase())) return;
  selectedSkills.push(skill);
  renderSkills();
}

function removeSkill(skill) {
  selectedSkills = selectedSkills.filter(existing => existing !== skill);
  renderSkills();
}

function renderSkills() {
  const tags = document.getElementById("skillTags");
  const hidden = document.getElementById("skills");
  if (hidden) hidden.value = selectedSkills.join(", ");
  if (!tags) return;
  tags.innerHTML = selectedSkills.map(skill => `
    <span class="skill-tag">
      ${skill}
      <button type="button" aria-label="Remove ${skill}" onclick="removeSkill('${skill.replace(/'/g, "\\'")}')">&times;</button>
    </span>
  `).join("");
}

function clearSkills() {
  selectedSkills = [];
  renderSkills();
  const entry = document.getElementById("skillEntry");
  if (entry) entry.value = "";
}

function configureRoleLayout(user) {
  const role = user && user.role ? user.role : "CANDIDATE";
  const sidebarId = role === "HR" ? "hrSidebar" : role === "PANEL" ? "panelSidebar" : "candidateSidebar";
  const pageTitle = document.getElementById("pageTitle");
  const pageSubtitle = document.getElementById("pageSubtitle");

  document.body.classList.remove("page-candidate", "page-panel", "page-hr");
  document.body.classList.add(role === "HR" ? "page-hr" : role === "PANEL" ? "page-panel" : "page-candidate");
  document.querySelectorAll(".role-sidebar").forEach(sidebar => sidebar.classList.add("is-hidden"));
  const sidebar = document.getElementById(sidebarId);
  if (sidebar) sidebar.classList.remove("is-hidden");

  document.querySelectorAll("[data-user-name]").forEach(el => {
    el.textContent = user.name || (role === "PANEL" ? "Panel Member" : "User");
  });
  document.querySelectorAll("[data-user-avatar]").forEach(el => {
    el.textContent = (user.name || role || "U").charAt(0).toUpperCase();
  });

  if (pageTitle) pageTitle.textContent = role === "HR" ? "Job Descriptions" : "Available Jobs";
  if (pageSubtitle) {
    pageSubtitle.textContent = role === "HR"
      ? "Create and manage job descriptions."
      : role === "PANEL"
        ? "Review open roles without applying."
        : "Browse open roles and apply.";
  }
}

function initializeJDPage() {
  const urlParams = new URLSearchParams(window.location.search);
  const viewMode = (urlParams.get("view") || "").toLowerCase();

  if (viewMode === "create") {
    setView("create");
  } else {
    setView("available");
  }

  loadJDs();
}

function setView(mode) {
  const form = document.getElementById("jdForm");
  const title = document.getElementById("jobsTitle");
  const subtitle = document.getElementById("pageSubtitle");
  const isCreate = String(mode || "").toLowerCase() === "create";
  const user = getStoredUser();

  if (user && user.role === "HR") {
    form.classList.toggle("is-hidden", !isCreate);
    title.textContent = isCreate ? "Create Job" : "Available Jobs";
    if (subtitle) subtitle.textContent = isCreate ? "Post a new job opening." : "Create and manage job descriptions.";

    const params = new URLSearchParams(window.location.search);
    params.set("view", isCreate ? "create" : "available");
    window.history.replaceState({}, "", `${window.location.pathname}?${params.toString()}`);
  } else {
    form.classList.add("is-hidden");
  }
}

function loadJDs() {
  const list = document.getElementById("jdList");
  const user = getStoredUser();

  list.innerHTML = `<div class="loading"><div class="spinner"></div><p>Loading jobs...</p></div>`;

  jdActions.list()
    .then(data => {
      list.innerHTML = "";

      if (!data || data.length === 0) {
        list.innerHTML = `
          <div class="empty-state">
            <div class="icon">📋</div>
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
        const salaryLabel = (salaryMin != null && salaryMax != null)
          ? `INR ${salaryMin.toLocaleString()} - INR ${salaryMax.toLocaleString()}`
          : (jd.salary || "N/A");

        let actionsHtml = "";
        if (user && user.role === "CANDIDATE") {
          actionsHtml = `<div class="jd-actions"><button class="apply-jd-btn btn-small" onclick="applyToJob(${jd.id}, '${(jd.title || '').replace(/'/g, "\\'")}')">Apply</button></div>`;
        } else if (user && user.role === "HR") {
          actionsHtml = `<div class="jd-actions"><button class="btn-small btn-danger" onclick="deleteJD(${jd.id})">Delete</button></div>`;
        }

        card.innerHTML = `
          <div class="jd-info">
            <b>${jd.title}</b>
            <p>${jd.description || 'No description provided'}</p>
            <div class="jd-meta">
              <span class="meta-item">${expMin} - ${expMax} yrs</span>
              <span class="meta-item">${salaryLabel}</span>
              <span class="meta-item">${jd.skills || 'N/A'}</span>
            </div>
          </div>
          ${actionsHtml}
        `;

        list.appendChild(card);
      });
    })
    .catch(() => {
      list.innerHTML = `<div class="alert alert-error">Error loading jobs. Please try again.</div>`;
    });
}

function createJD() {
  addSkill(document.getElementById("skillEntry")?.value);
  if (!validateJDForm()) return;

  const jd = {
    title: document.getElementById("title").value.trim(),
    description: document.getElementById("description").value.trim(),
    skills: document.getElementById("skills").value.trim(),
    experienceMin: Number(document.getElementById("experienceMin").value),
    experienceMax: Number(document.getElementById("experienceMax").value),
    salaryMin: Number(document.getElementById("salaryMin").value),
    salaryMax: Number(document.getElementById("salaryMax").value)
  };

  const btn = document.querySelector(".create-jd-form button");
  if (btn) { btn.disabled = true; btn.textContent = "Posting..."; }

  jdActions.create(jd)
    .then(() => {
      alert("Job posted successfully!");
      setView("available");
      ["title", "description", "experienceMin", "experienceMax", "salaryMin", "salaryMax"]
        .forEach(id => { document.getElementById(id).value = ""; });
      clearSkills();
      loadJDs();
    })
    .catch(err => {
      alert("Error creating job: " + (err.message || "Please try again"));
    })
    .finally(() => {
      if (btn) { btn.disabled = false; btn.textContent = "Post Job"; }
    });
}

function deleteJD(id) {
  if (!confirm("Delete this job description? This cannot be undone.")) return;
  jdActions.delete(id)
    .then(() => { alert("Job deleted."); loadJDs(); })
    .catch(err => alert(err.message || "Failed to delete job"));
}

function validateJDForm() {
  const fields = ["title", "description", "skills", "skillEntry", "experienceMin", "experienceMax", "salaryMin", "salaryMax"];
  fields.forEach(id => {
    const el = document.getElementById(id);
    if (el) el.classList.remove("input-error");
  });

  const title = document.getElementById("title");
  const description = document.getElementById("description");
  const skills = document.getElementById("skills");
  const skillEntry = document.getElementById("skillEntry");
  const experienceMin = document.getElementById("experienceMin");
  const experienceMax = document.getElementById("experienceMax");
  const salaryMin = document.getElementById("salaryMin");
  const salaryMax = document.getElementById("salaryMax");

  let isValid = true;

  if (!title.value.trim() || title.value.trim().length < 3) {
    title.classList.add("input-error"); isValid = false;
  }
  if (!description.value.trim() || description.value.trim().length < 50) {
    description.classList.add("input-error"); isValid = false;
  }
  if (!skills.value.trim()) {
    if (skillEntry) skillEntry.classList.add("input-error");
    skills.classList.add("input-error"); isValid = false;
  }
  if (!experienceMin.value || Number(experienceMin.value) < 0) {
    experienceMin.classList.add("input-error"); isValid = false;
  }
  if (!experienceMax.value || Number(experienceMax.value) < 0) {
    experienceMax.classList.add("input-error"); isValid = false;
  }
  if (experienceMin.value && experienceMax.value && Number(experienceMin.value) > Number(experienceMax.value)) {
    experienceMin.classList.add("input-error");
    experienceMax.classList.add("input-error");
    alert("Min experience cannot be greater than max experience.");
    return false;
  }
  if (!salaryMin.value || Number(salaryMin.value) < 100000) {
    salaryMin.classList.add("input-error"); isValid = false;
  }
  if (!salaryMax.value || Number(salaryMax.value) < 100000) {
    salaryMax.classList.add("input-error"); isValid = false;
  }
  if (salaryMin.value && salaryMax.value && Number(salaryMin.value) > Number(salaryMax.value)) {
    salaryMax.classList.add("input-error"); isValid = false;
  }

  if (!isValid) alert("Please fill in all required fields correctly.");
  return isValid;
}

function applyToJob(jdId, jdTitle) {
  if (confirm(`Apply for "${jdTitle}"?`)) {
    localStorage.setItem(STORAGE_KEYS.SELECTED_JD_ID, jdId);
    window.location.href = "apply.html";
  }
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}

window.logout = logout;
window.removeSkill = removeSkill;
