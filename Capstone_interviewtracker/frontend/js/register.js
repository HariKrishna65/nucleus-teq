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
  const role = document.getElementById("role");
  let isValid = true;

  // Clear previous errors
  [name, email, role].forEach(el => {
    el.classList.remove("input-error");
    const err = el.nextElementSibling;
    if (err && err.classList.contains("error-message")) err.style.display = "none";
  });

  // Validate name
  if (!name.value.trim()) {
    showError(name, "Name is required");
    isValid = false;
  } else if (name.value.trim().length < 2) {
    showError(name, "Name must be at least 2 characters");
    isValid = false;
  }

  // Validate email
  if (!email.value.trim()) {
    showError(email, "Email is required");
    isValid = false;
  } else if (!isValidEmail(email.value)) {
    showError(email, "Please enter a valid email");
    isValid = false;
  }

  // Validate role
  if (!role.value) {
    showError(role, "Please select your role");
    isValid = false;
  }

  return isValid;
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
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
    role: document.getElementById("role").value,
    dateOfBirth: document.getElementById("dateOfBirth").value || null,
    gender: document.getElementById("gender").value || null,
    phone: document.getElementById("phone").value.trim() || null,
    address: document.getElementById("address").value.trim() || null,
    city: document.getElementById("city").value.trim() || null,
    state: document.getElementById("state").value.trim() || null,
    country: document.getElementById("country").value.trim() || null
    // Note: password is NOT included - will be set after email verification
  };

  authActions.register(data)
  .then(data => {
    showLoading(false);
    
    const msg = (data && data.message) ? data.message : String(data || "");
    // Check if registration was successful
    if (msg.includes("success") || msg.includes("verification") || msg.includes("email")) {
      showAlert("Registration successful! Please check your email to verify your account.", "success");
      // Redirect to login after delay
      setTimeout(() => window.location.href = "login.html", 3000);
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
    showAlert(msg, "error");
  });
}

function showLoading(show) {
  const btn = document.querySelector("button");
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Creating Account...';
    btn.disabled = true;
  } else {
    btn.textContent = "Register";
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
  container.insertBefore(alert, container.firstChild);
}

// Enter key support
document.addEventListener("keypress", function(e) {
  if (e.key === "Enter") register();
});