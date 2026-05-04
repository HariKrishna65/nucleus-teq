function selectGender(value) {
  document.querySelectorAll(".gender-option").forEach(el => {
    el.classList.remove("selected");
  });
  document.querySelector(`.gender-option[data-value="${value}"]`).classList.add("selected");
  document.getElementById("gender").value = value;
}

function validateRegister() {
  const name = document.getElementById("name");
  const email = document.getElementById("email");
  const phone = document.getElementById("phone");
  let isValid = true;

  [name, email, phone].forEach(el => {
    el.classList.remove("input-error");
    const err = el.nextElementSibling;
    if (err && err.classList.contains("error-message")) err.style.display = "none";
  });

  if (!name.value.trim()) {
    showError(name, "Name is required");
    isValid = false;
  } else if (name.value.trim().length < 2) {
    showError(name, "Name must be at least 2 characters");
    isValid = false;
  }

  if (!email.value.trim()) {
    showError(email, "Email is required");
    isValid = false;
  } else if (!isValidEmail(email.value)) {
    showError(email, "Please enter a valid email");
    isValid = false;
  }

  if (!phone.value.trim()) {
    showError(phone, "Phone number is required");
    isValid = false;
  } else if (!isValidPhone(phone.value.trim())) {
    showError(phone, "Please enter a valid phone number");
    isValid = false;
  }

  return isValid;
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidPhone(phone) {
  return /^[0-9+\-()\s]{7,20}$/.test(phone);
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

function register() {
  if (!validateRegister()) return;

  showLoading(true);

  const data = {
    name: document.getElementById("name").value.trim(),
    email: document.getElementById("email").value.trim(),
    role: "CANDIDATE",
    dateOfBirth: document.getElementById("dateOfBirth").value || null,
    gender: document.getElementById("gender").value || null,
    phone: document.getElementById("phone").value.trim(),
    address: document.getElementById("address").value.trim() || null,
    city: document.getElementById("city").value.trim() || null,
    state: document.getElementById("state").value.trim() || null,
    country: document.getElementById("country").value.trim() || null
  };

  authActions.register(data)
  .then(data => {
    showLoading(false);
    
    const msg = (data && data.message) ? data.message : String(data || "");
    if (msg.includes("success") || msg.includes("verification") || msg.includes("email")) {
      showAlert("Registration successful! Please check your email to verify your account.", "success");
      setTimeout(() => window.location.href = "login.html", 800);
    } else if (msg.includes("exists") || msg.includes("already")) {
      showAlert("Email already registered. Please login or use forgot password.", "error");
    } else {
      showAlert(msg, msg.includes("error") || msg.includes("failed") ? "error" : "success");
    }
  })
  .catch(err => {
    showLoading(false);
    const msg = (err && err.message) ? err.message : "Registration failed. Please try again.";
    if (msg.toLowerCase().includes("email already")) {
      showAlert("Email already registered. Please login or use forgot password.", "error");
      return;
    }
    if (msg.toLowerCase().includes("phone already")) {
      showAlert("Phone number already registered. Please use another phone number.", "error");
      return;
    }
    showAlert(msg, "error");
  });
}

function showLoading(show) {
  const btn = document.getElementById("registerBtn");
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Creating Account...';
    btn.disabled = true;
  } else {
    btn.textContent = "Create account";
    btn.disabled = false;
  }
}

function showAlert(message, type) {
  const existing = document.querySelector(".alert");
  if (existing) existing.remove();

  const alert = document.createElement("div");
  alert.className = `alert alert-${type}`;
  alert.textContent = message;

  const container = document.querySelector(".auth-card");
  container.insertBefore(alert, container.firstChild);
}

document.addEventListener("keypress", function(e) {
  if (e.key === "Enter") register();
});
