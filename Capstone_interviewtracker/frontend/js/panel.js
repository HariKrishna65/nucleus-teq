const BASE_URL = "http://localhost:8080";

const user = JSON.parse(localStorage.getItem("user"));

// 🔹 Load interviews assigned to panel
function loadInterviews() {

  fetch(BASE_URL + `/interviews?panelId=${user.userId}`)
    .then(res => res.json())
    .then(data => {

      const list = document.getElementById("interviewList");
      list.innerHTML = "";

      data.forEach(i => {

        const li = document.createElement("li");

        li.innerHTML = `
          <b>${i.candidate.user.name}</b><br>
          Round: ${i.round} <br>
          Focus: ${i.focusArea} <br>
          Time: ${i.interviewTime} <br>

          <button onclick="giveFeedback(${i.id})">
            Give Feedback
          </button>
        `;

        list.appendChild(li);
      });
    })
    .catch(err => console.error(err));
}

// 🔹 Redirect to feedback page
function giveFeedback(interviewId) {
  localStorage.setItem("interviewId", interviewId);
  window.location.href = "feedback.html";
}

loadInterviews();