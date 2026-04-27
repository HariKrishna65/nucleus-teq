const BASE_URL = "http://localhost:8080";

const user = JSON.parse(localStorage.getItem("user"));

// 🔹 Load applications
function loadApplications() {

  fetch(BASE_URL + `/candidates?userId=${user.userId}`)
    .then(res => res.json())
    .then(data => {

      const list = document.getElementById("applications");
      list.innerHTML = "";

      data.forEach(c => {

        const li = document.createElement("li");

        li.innerHTML = `
          <b>${c.jd.title}</b><br>
          Status: <span>${c.status}</span>
        `;

        list.appendChild(li);
      });
    })
    .catch(err => console.error(err));
}

loadApplications();