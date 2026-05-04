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

  const res = await fetch(`${API_URL}${path}`, {
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
}
