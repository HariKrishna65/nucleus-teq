function validateLogin() {
  const email = document.getElementById("email");
  const password = document.getElementById("password");
  let isValid = true;

  email.classList.remove("input-error");
  password.classList.remove("input-error");

  if (!email.value.trim()) {
    showError(email, "Email is required");
    isValid = false;
  } else if (!isValidEmail(email.value)) {
    showError(email, "Please enter a valid email");
    isValid = false;
  }

  if (!password.value) {
    showError(password, "Password is required");
    isValid = false;
  } else if (password.value.length < 8) {
    showError(password, "Password must be at least 8 characters");
    isValid = false;
  }

  return isValid;
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function showError(input, message) {
  input.classList.add("input-error");
  let errorDiv = input.parentElement.querySelector(".error-message");
  if (!errorDiv) {
    errorDiv = document.createElement("div");
    errorDiv.className = "error-message";
    input.parentElement.appendChild(errorDiv);
  }
  errorDiv.textContent = message;
  errorDiv.style.display = "block";
}

function login() {
  if (!validateLogin()) return;

  showLoading(true);

  const data = {
    email: document.getElementById("email").value.trim(),
    password: document.getElementById("password").value
  };

  authActions.login(data)
    .then(data => {
      showLoading(false);
      localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(data));
      showAlert("Login successful. Redirecting...", "success");

      setTimeout(() => {
        if (data.role === "HR") {
          window.location.href = "hr-main-dashboard.html";
        } else if (data.role === "PANEL") {
          window.location.href = "panel-dashboard.html";
        } else {
          window.location.href = "dashboard.html";
        }
      }, 700);
    })
    .catch(err => {
      showLoading(false);
      const errorMsg = err.message || "Login failed";

      if (errorMsg.includes("verify") || errorMsg.includes("verified")) {
        document.getElementById("verificationNotice").classList.remove("is-hidden");
        showAlert(errorMsg, "error");
      } else if (errorMsg.includes("Password not set") || errorMsg.includes("password")) {
        showAlert(errorMsg + " Use Forgot password to reset.", "error");
      } else {
        showAlert(errorMsg, "error");
      }
    });
}

function showForgotPassword() {
  let email = document.getElementById("email").value.trim();

  if (!email) {
    email = prompt("Enter your email address to reset password:");
    if (!email) return;
  }

  if (!isValidEmail(email)) {
    showAlert("Please enter a valid email", "error");
    return;
  }

  if (confirm(`Send password reset link to ${email}?`)) {
    showLoading(true);

    authActions.forgotPassword({ email })
      .then(() => {
        showLoading(false);
        showAlert("Password reset link sent to your email.", "success");
      })
      .catch(() => {
        showLoading(false);
        showAlert("Failed to send reset email. Please try again.", "error");
      });
  }
}

function togglePassword(inputId, btn) {
  const input = document.getElementById(inputId);
  if (!input) return;
  const isHidden = input.type === "password";
  input.type = isHidden ? "text" : "password";
  btn.textContent = isHidden ? "Hide" : "Show";
}

function showLoading(show) {
  const btn = document.getElementById("loginBtn");
  if (!btn) return;
  if (show) {
    btn.innerHTML = '<span class="spinner"></span> Loading...';
    btn.disabled = true;
  } else {
    btn.textContent = "Sign in";
    btn.disabled = false;
  }
}

function showAlert(message, type) {
  const existing = document.querySelector(".alert:not(#verificationNotice)");
  if (existing) existing.remove();

  const alert = document.createElement("div");
  alert.className = `alert alert-${type}`;
  alert.textContent = message;

  const container = document.querySelector(".auth-card");
  container.insertBefore(alert, container.firstChild);
}

document.addEventListener("keypress", function(e) {
  if (e.key === "Enter") login();
});

const existingUser = getStoredUser();
if (existingUser && existingUser.token) {
  if (existingUser.role === "HR") window.location.href = "hr-main-dashboard.html";
  else if (existingUser.role === "PANEL") window.location.href = "panel-dashboard.html";
  else window.location.href = "dashboard.html";
} else if (existingUser) {
  localStorage.removeItem(STORAGE_KEYS.USER);
}
