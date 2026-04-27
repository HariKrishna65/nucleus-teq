const BASE_URL = "http://localhost:8080";

const user = JSON.parse(localStorage.getItem("user"));

// 🔹 Load JDs into dropdown
function loadJDs() {
  fetch(BASE_URL + "/jd")
    .then(res => res.json())
    .then(data => {
      const select = document.getElementById("jdSelect");
      select.innerHTML = "";

      data.forEach(jd => {
        const option = document.createElement("option");
        option.value = jd.id;
        option.textContent = jd.title;
        select.appendChild(option);
      });
    });
}

// 🔹 Apply to job
function applyJob() {

  const candidate = {
    phone: document.getElementById("phone").value,
    experience: parseInt(document.getElementById("experience").value),
    status: "APPLIED",
    user: {
      id: user.userId
    },
    jd: {
      id: document.getElementById("jdSelect").value
    }
  };

  const file = document.getElementById("resume").files[0];

  const formData = new FormData();

  // IMPORTANT: send JSON as Blob
  formData.append("candidate", new Blob([JSON.stringify(candidate)], {
    type: "application/json"
  }));

  formData.append("file", file);

  fetch(BASE_URL + "/candidates", {
    method: "POST",
    body: formData
  })
  .then(res => res.json())
  .then(data => {
    alert("Applied successfully!");
    window.location.href = "dashboard.html";
  })
  .catch(err => {
    console.error(err);
    alert("Error applying");
  });
}

loadJDs();