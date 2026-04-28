const BASE_URL = "http://localhost:8080";

const interviewId = localStorage.getItem("interviewId");

// 🔹 Submit feedback
function submitFeedback() {

  const feedback = {
    rating: parseInt(document.getElementById("rating").value),
    comments: document.getElementById("comments").value,
    status: document.getElementById("status").value,
    interview: {
      id: interviewId
    }
  };

  fetch(BASE_URL + "/feedback", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(feedback)
  })
  .then(res => res.json())
  .then(data => {
    alert("Feedback submitted");
    window.location.href = "panel-dashboard.html";
  })
  .catch(err => alert("Error submitting feedback"));
}