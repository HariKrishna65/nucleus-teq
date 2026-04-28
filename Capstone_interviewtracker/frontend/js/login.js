const BASE_URL = "http://localhost:8080";

function login() {

  const data = {
    email: document.getElementById("email").value,
    password: document.getElementById("password").value
  };

  fetch(BASE_URL + "/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  })
  .then(async (res) => {
    const text = await res.text();

    // 🔥 handle error properly
    if (!res.ok) {
      throw new Error(text);
    }

    return JSON.parse(text);
  })
  .then(data => {
    console.log("SUCCESS:", data);

    alert(data.message);

    // store user
    localStorage.setItem("user", JSON.stringify(data));

    // redirect
    window.location.href = "index.html";
    window.location.href = "jd.html";
  })
  .catch(err => {
    console.error(err);
    alert(err.message || "Login failed");
  });
}