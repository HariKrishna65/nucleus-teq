const BASE_URL = "http://localhost:8080";

function register() {

  const data = {
    name: document.getElementById("name").value,
    email: document.getElementById("email").value,
    password: document.getElementById("password").value,
    role: document.getElementById("role").value
  };

  fetch(BASE_URL + "/auth/register", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  })
  .then(res => res.text())
  .then(data => {
    alert(data);
    window.location.href = "login.html";
  })
  .catch(err => alert("Registration failed"));
}