let currentToken = null;
let currentEmail = null;

const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

if (token) {
  currentToken = token;
  startVerificationAndPasswordFlow();
} else {
  showError("Invalid verification link. Please request a new verification email.");
}

function startVerificationAndPasswordFlow() {
  verifyEmail(currentToken);
}

function verifyEmail(token) {
  fetchHandler("/auth/verify-and-set-password", { method: "POST", body: { token } })
  .then((data) => {
    currentEmail = data.email;
    showPasswordStep();
  })
  .catch(err => {
    showError(err.message || "Verification failed. Please try again.");
  });
}

function showPasswordStep() {
  document.getElementById("verifyStep").classList.add("is-hidden");
  document.getElementById("passwordStep").classList.remove("is-hidden");
  document.getElementById("icon").className = "status-icon";
  document.getElementById("icon").textContent = "SEC";
  document.getElementById("title").textContent = "Set Your Password";
  document.getElementById("message").textContent = "Create a secure password to complete your registration.";
}

function handleSetPassword() {
  const password = document.getElementById("newPassword").value;
  const confirmPassword = document.getElementById("confirmPassword").value;

  if (!password || !confirmPassword) {
    alert("Please fill in all password fields");
    return;
  }

  if (password !== confirmPassword) {
    alert("Passwords do not match");
    return;
  }

  if (!isValidPassword(password)) {
    alert("Password does not meet requirements");
    return;
  }

  setPassword(password);
}

function setPassword(password) {
  const encryptedPassword = encryptPassword(password);
  const payload = {
    token: currentToken,
    newPassword: encryptedPassword,
    confirmPassword: encryptedPassword
  };

  fetchHandler("/auth/set-password", { method: "POST", body: payload })
  .then(() => {
    showSuccess("Password set successfully! You can now login.");
    setTimeout(() => {
      window.location.href = "login.html";
    }, 2000);
  })
  .catch(err => {
    showError(err.message || "Failed to set password. Please try again.");
  });
}

function togglePassword(fieldId, button) {
  const field = document.getElementById(fieldId);
  if (field.type === "password") {
    field.type = "text";
    button.textContent = "Hide";
  } else {
    field.type = "password";
    button.textContent = "Show";
  }
}

document.getElementById("newPassword")?.addEventListener("input", function() {
  const password = this.value;
  const requirements = {
    length: password.length >= 8,
    upper: /[A-Z]/.test(password),
    lower: /[a-z]/.test(password),
    number: /[0-9]/.test(password),
    special: /[!@#$%^&*]/.test(password)
  };

  updateRequirement("req-length", requirements.length);
  updateRequirement("req-upper", requirements.upper);
  updateRequirement("req-lower", requirements.lower);
  updateRequirement("req-number", requirements.number);
  updateRequirement("req-special", requirements.special);

  const strength = Object.values(requirements).filter(Boolean).length;
  const strengthFill = document.getElementById("strengthFill");
  const strengthText = document.getElementById("strengthText");

  strengthFill.className = "strength-fill";
  strengthText.textContent = "";

  if (strength === 0) {
    strengthText.textContent = "";
  } else if (strength <= 2) {
    strengthFill.classList.add("strength-weak");
    strengthText.textContent = "Weak";
  } else if (strength <= 4) {
    strengthFill.classList.add("strength-medium");
    strengthText.textContent = "Medium";
  } else {
    strengthFill.classList.add("strength-strong");
    strengthText.textContent = "Strong";
  }
});

function updateRequirement(elementId, met) {
  const element = document.getElementById(elementId);
  if (element) {
    if (met) {
      element.classList.add("met");
    } else {
      element.classList.remove("met");
    }
  }
}

function isValidPassword(password) {
  const pattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
  return pattern.test(password);
}

function showSuccess(message) {
  document.getElementById("passwordStep").classList.add("is-hidden");
  document.getElementById("successStep").classList.remove("is-hidden");
  document.getElementById("icon").className = "status-icon success-icon";
  document.getElementById("icon").textContent = "OK";
  document.getElementById("title").textContent = "Registration Complete!";
  document.getElementById("successMessage").textContent = message;
}

function showError(message) {
  document.getElementById("verifyStep").classList.add("is-hidden");
  document.getElementById("passwordStep").classList.add("is-hidden");
  document.getElementById("successStep").classList.add("is-hidden");
  document.getElementById("errorSection").classList.remove("is-hidden");
  document.getElementById("icon").className = "status-icon error-icon";
  document.getElementById("icon").textContent = "!";
  document.getElementById("title").textContent = "Registration Error";
  document.getElementById("errorMessage").textContent = message;
}

function resendVerification() {
  const email = prompt("Enter your email address:");
  if (!email) return;

  fetchHandler("/auth/resend-verification", { method: "POST", body: { email } })
  .then(() => {
    alert("Verification email resent! Check your inbox and click the link.");
  })
  .catch(err => {
    alert("Failed to resend email: " + (err.message || "Please try again."));
  });
}
