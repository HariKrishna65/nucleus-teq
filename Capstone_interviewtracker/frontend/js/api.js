const API_URL = "http://localhost:8080";

const STORAGE_KEYS = {
  USER: "user",
  INTERVIEW_ID: "interviewId",
  SELECTED_JD_ID: "selectedJdId"
};

function getStoredUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.USER);
    return raw ? JSON.parse(raw) : null;
  } catch {
    localStorage.removeItem(STORAGE_KEYS.USER);
    return null;
  }
}

function getAuthToken() {
  const user = getStoredUser();
  return user?.token || null;
}

function authHeaders() {
  const token = getAuthToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function fetchHandler(path, options = {}) {
  const {
    method = "GET",
    body = null,
    requireAuth = false,
    headers: extraHeaders = {}
  } = options;

  const headers = {
    ...(body instanceof FormData ? {} : { "Content-Type": "application/json" }),
    ...(requireAuth ? authHeaders() : {}),
    ...extraHeaders
  };

  const url = `${API_URL}${path}`;
  console.log("📡 API CALL:", method, url);

  try {
    const res = await fetch(url, {
      method,
      headers,
      body: body instanceof FormData ? body : body ? JSON.stringify(body) : null
    });

    const contentType = res.headers.get("content-type") || "";
    const data = contentType.includes("application/json")
      ? await res.json()
      : await res.text();

    if (!res.ok) {
      const message = typeof data === "string" ? data : data.message || `Error ${res.status}`;
      throw new Error(message);
    }

    return data;
  } catch (error) {
    console.error("❌ API ERROR:", error.message);
    throw error;
  }
}

const authActions = {
  register: (data) =>
    fetchHandler("/auth/register", { method: "POST", body: data }),

  login: (data) =>
    fetchHandler("/auth/login", { method: "POST", body: data }),

  forgotPassword: (data) =>
    fetchHandler("/auth/forgot-password", { method: "POST", body: data })
};

const jdActions = {
  list: () => fetchHandler("/jd"),

  create: (jd) =>
    fetchHandler("/jd", { method: "POST", body: jd, requireAuth: true }),

  getById: (id) => fetchHandler(`/jd/${id}`),

  delete: (id) =>
    fetchHandler(`/jd/${id}`, { method: "DELETE", requireAuth: true })
};

const candidateActions = {
  listByUser: (userId) =>
    fetchHandler(`/candidates?userId=${userId}`, { requireAuth: true }),

  apply: (formData) =>
    fetchHandler("/candidates", { method: "POST", body: formData, requireAuth: true })
};

const interviewActions = {
  listByPanel: (panelId) =>
    fetchHandler(`/interviews?panelId=${panelId}`, { requireAuth: true }),

  getById: (id) =>
    fetchHandler(`/interviews/${id}`, { requireAuth: true }),

  listPanels: () =>
    fetchHandler("/interviews/panel", { requireAuth: true }),

  schedule: (payload) =>
    fetchHandler("/interviews", { method: "POST", body: payload, requireAuth: true })
};

const feedbackActions = {
  submit: (feedback) =>
    fetchHandler("/feedback", { method: "POST", body: feedback, requireAuth: true }),

  getByInterview: (id) =>
    fetchHandler(`/feedback/interview/${id}`, { requireAuth: true })
};

const hrActions = {
  listCandidates: () =>
    fetchHandler("/hr/candidates", { requireAuth: true }),

  getCandidateDetails: (id) =>
    fetchHandler(`/hr/candidates/${id}`, { requireAuth: true }),

  advanceCandidate: (id, comments) =>
    fetchHandler(`/hr/candidates/${id}/advance`, {
      method: "POST",
      body: { comments },
      requireAuth: true
    }),

  rejectCandidate: (id, comments) =>
    fetchHandler(`/hr/candidates/${id}/reject`, {
      method: "POST",
      body: { comments },
      requireAuth: true
    }),

  selectCandidate: (id, comments) =>
    fetchHandler(`/hr/candidates/${id}/select`, {
      method: "POST",
      body: { comments },
      requireAuth: true
    }),

  deleteCandidate: (id) =>
    fetchHandler(`/hr/candidates/${id}`, { method: "DELETE", requireAuth: true }),

  createPanel: (panel) =>
    fetchHandler("/hr/panels", { method: "POST", body: panel, requireAuth: true }),

  assignPanel: (id, data) =>
    fetchHandler(`/hr/candidates/${id}/assign-panel`, {
      method: "POST",
      body: data,
      requireAuth: true
    })
};

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}