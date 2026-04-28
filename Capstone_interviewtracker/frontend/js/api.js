// Config (constants)
const API_URL = "http://localhost:8080";
const STORAGE_KEYS = {
  USER: "user",
  INTERVIEW_ID: "interviewId",
  SELECTED_JD_ID: "selectedJdId"
};

function getStoredUser() {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.USER);
    if (!raw) return null;
    return JSON.parse(raw);
  } catch (e) {
    localStorage.removeItem(STORAGE_KEYS.USER);
    return null;
  }
}

function getAuthToken() {
  const user = getStoredUser();
  return user && user.token ? user.token : null;
}

function authHeaders(extra = {}) {
  const token = getAuthToken();
  const headers = { ...extra };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  return headers;
}

// Reusable fetch handler (centralized errors + auth)
async function fetchHandler(path, options = {}) {
  const {
    method = "GET",
    body,
    requireAuth = false,
    headers: extraHeaders = {}
  } = options;

  const headers = {
    ...(body instanceof FormData ? {} : { "Content-Type": "application/json" }),
    ...(requireAuth ? authHeaders() : {}),
    ...extraHeaders
  };

  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    body: body == null ? null : (body instanceof FormData ? body : JSON.stringify(body))
  });

  const contentType = res.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await res.json() : await res.text();

  if (!res.ok) {
    const msg = typeof payload === "string" ? payload : (payload.message || "Request failed");
    throw new Error(msg);
  }
  return payload;
}

// Actions (API calls live here, pages should call actions)
const authActions = {
  register: (data) => fetchHandler("/auth/register", { method: "POST", body: data }),
  login: (data) => fetchHandler("/auth/login", { method: "POST", body: data }),
  forgotPassword: (data) => fetchHandler("/auth/forgot-password", { method: "POST", body: data })
};

const jdActions = {
  list: () => fetchHandler("/jd"),
  create: (jd) => fetchHandler("/jd", { method: "POST", body: jd, requireAuth: true })
};

const candidateActions = {
  listByUser: (userId) => fetchHandler(`/candidates?userId=${userId}`, { requireAuth: true }),
  apply: (formData) => fetchHandler("/candidates", { method: "POST", body: formData, requireAuth: true })
};

const interviewActions = {
  listByPanel: (panelId) => fetchHandler(`/interviews?panelId=${panelId}`, { requireAuth: true }),
  getById: (id) => fetchHandler(`/interviews/${id}`, { requireAuth: true }),
  listPanels: () => fetchHandler("/interviews/panel", { requireAuth: true }),
  schedule: (payload) => fetchHandler("/interviews", { method: "POST", body: payload, requireAuth: true })
};

const feedbackActions = {
  submit: (feedback) => fetchHandler("/feedback", { method: "POST", body: feedback, requireAuth: true }),
  getByInterview: (interviewId) => fetchHandler(`/feedback/interview/${interviewId}`, { requireAuth: true })
};

const hrActions = {
  listCandidates: () => fetchHandler("/hr/candidates", { requireAuth: true }),
  advanceCandidate: (candidateId, comments) =>
    fetchHandler(`/hr/candidates/${candidateId}/advance`, { method: "POST", body: { comments }, requireAuth: true }),
  rejectCandidate: (candidateId, comments) =>
    fetchHandler(`/hr/candidates/${candidateId}/reject`, { method: "POST", body: { comments }, requireAuth: true }),
  selectCandidate: (candidateId, comments) =>
    fetchHandler(`/hr/candidates/${candidateId}/select`, { method: "POST", body: { comments }, requireAuth: true }),
  createPanel: (panel) => fetchHandler("/hr/panels", { method: "POST", body: panel, requireAuth: true })
};

