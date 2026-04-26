const BASE_URL = "http://localhost:8080";

// 🔹 Get user from localStorage
const user = JSON.parse(localStorage.getItem("user"));

// 🔹 Hide form if not HR
if (user.role !== "HR") {
  document.getElementById("jdForm").style.display = "none";
}

// 🔹 Fetch all JDs
function loadJDs() {
  fetch(BASE_URL + "/jd")
    .then(res => res.json())
    .then(data => {
      const list = document.getElementById("jdList");
      list.innerHTML = "";

      data.forEach(jd => {
        const li = document.createElement("li");

        li.innerHTML = `
          <b>${jd.title}</b> - ${jd.description} <br>
          Skills: ${jd.skills} | Exp: ${jd.experience} yrs
        `;

        list.appendChild(li);
      });
    })
    .catch(err => console.error("Error loading JDs", err));
}

// 🔹 Create JD (HR only)
function createJD() {

  const jd = {
    title: document.getElementById("title").value,
    description: document.getElementById("description").value,
    skills: document.getElementById("skills").value,
    experience: parseInt(document.getElementById("experience").value)
  };

  fetch(BASE_URL + `/jd?role=${user.role}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(jd)
  })
  .then(res => res.json())
  .then(data => {
    alert("JD Created");
    loadJDs();
  })
  .catch(err => alert("Error creating JD"));
}

// 🔹 Load JDs on page load
loadJDs();