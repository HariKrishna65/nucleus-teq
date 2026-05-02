const user = getStoredUser();

if (!user) {
  window.location.href = "login.html";
}
if (user.role !== "HR") {
  window.location.href = "index.html";
}

let allPanels = [];

function loadPanelMembers() {
  interviewActions.listPanels()
    .then(panels => {
      allPanels = Array.isArray(panels) ? panels : [];
      renderPanelMembers();
    })
    .catch(err => {
      console.error("Error loading panels:", err);
      document.getElementById("panelMembersBody").innerHTML =
        '<tr><td colspan="6" class="empty-state">Error loading panel members</td></tr>';
    });
}

function renderPanelMembers() {
  const tbody = document.getElementById("panelMembersBody");

  if (!allPanels || allPanels.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No panel members found</td></tr>';
    return;
  }

  tbody.innerHTML = allPanels.map(panel => `
    <tr>
      <td>${panel.name || 'N/A'}</td>
      <td>${panel.email || 'N/A'}</td>
      <td>${panel.mobile || panel.phone || 'N/A'}</td>
      <td>${panel.organization || 'N/A'}</td>
      <td>${panel.designation || 'N/A'}</td>
      <td>${panel.expertise || 'N/A'}</td>
    </tr>
  `).join('');
}

function createPanelMember() {
  const name = document.getElementById("panelName").value.trim();
  const email = document.getElementById("panelEmail").value.trim();
  const phone = document.getElementById("panelPhone").value.trim();
  const organization = document.getElementById("panelOrganization").value.trim();
  const designation = document.getElementById("panelDesignation").value.trim();
  const expertise = document.getElementById("panelExpertise").value.trim();

  if (!name || !email || !phone || !organization || !designation || !expertise) {
    showPanelAlert("All fields are required", "error");
    return;
  }

  if (!isValidEmail(email)) {
    showPanelAlert("Please enter a valid email address", "error");
    return;
  }

  if (!isValidPhone(phone)) {
    showPanelAlert("Please enter a valid 10-digit phone number", "error");
    return;
  }

  const btn = document.getElementById("createPanelBtn");
  btn.disabled = true;
  btn.textContent = "Creating...";

  const panelData = { name, email, phone, organization, designation, expertise };

  hrActions.createPanel(panelData)
    .then(() => {
      showPanelAlert("Panel member created successfully! Password setup email sent.", "success");
      clearPanelForm();
      loadPanelMembers();
    })
    .catch(err => {
      showPanelAlert(err.message || "Failed to create panel member", "error");
    })
    .finally(() => {
      btn.disabled = false;
      btn.textContent = "Create Panel";
    });
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPhone(phone) {
  return /^[0-9]{10}$/.test(phone.replace(/[^0-9]/g, ''));
}

function showPanelAlert(message, type) {
  const alertDiv = document.getElementById("panelAlert");
  alertDiv.textContent = message;
  alertDiv.className = `alert alert-${type}`;
  alertDiv.classList.remove("is-hidden");

  if (type === "success") {
    setTimeout(() => alertDiv.classList.add("is-hidden"), 4000);
  }
}

function clearPanelForm() {
  ["panelName", "panelEmail", "panelPhone", "panelOrganization", "panelDesignation", "panelExpertise"]
    .forEach(id => { document.getElementById(id).value = ""; });
}

function toggleSidebar() {
  const shell = document.querySelector(".dashboard-shell");
  if (!shell) return;
  shell.classList.toggle("sidebar-collapsed");
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}

window.createPanelMember = createPanelMember;
window.toggleSidebar = toggleSidebar;
window.logout = logout;

document.addEventListener('DOMContentLoaded', loadPanelMembers);