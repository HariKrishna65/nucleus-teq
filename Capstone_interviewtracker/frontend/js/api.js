const authActions = {
  register: (data) => {
    if (data.password) {
      data.password = encryptPassword(data.password);
    }
    return fetchHandler("/auth/register", { method: "POST", body: data });
  },

  login: (data) => {
    if (data.password) {
      data.password = encryptPassword(data.password);
    }
    return fetchHandler("/auth/login", { method: "POST", body: data });
  },

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

  createReferralCandidate: (candidate) =>
    fetchHandler("/hr/referrals", { method: "POST", body: candidate, requireAuth: true }),

  assignPanel: (id, data) =>
    fetchHandler(`/hr/candidates/${id}/assign-panel`, {
      method: "POST",
      body: data,
      requireAuth: true
    })
};

function encryptPassword(password) {
  const utf8Bytes = new TextEncoder().encode(password);
  let binary = "";
  utf8Bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
}

function logout() {
  localStorage.removeItem(STORAGE_KEYS.USER);
  window.location.href = "login.html";
}
