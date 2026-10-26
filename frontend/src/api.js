
const API_BASE_URL = "/api";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.token
        ? { Authorization: `Bearer ${options.token}` }
        : {}),
      ...options.headers,
    },
  });

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  let data = null;

  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    const message =
      typeof data === "string"
        ? data
        : data?.message || data?.detail || `Request failed (${response.status})`;

    throw new Error(message);
  }

  return data;
}

export const api = {
  register: (email, password) =>
    request("/auth/register", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),

  login: (email, password) =>
  request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  }),

  getTasks: (token, params = {}) => {
  const query = new URLSearchParams(params);
  const suffix = query.toString() ? `?${query.toString()}` : "";

  return request(`/tasks${suffix}`, { token });
},

  getStats: (token) => request("/tasks/stats", { token }),

  createTask: (token, task) =>
    request("/tasks", {
      method: "POST",
      token,
      body: JSON.stringify(task),
    }),

  updateTask: (token, id, task) =>
    request(`/tasks/${id}`, {
      method: "PUT",
      token,
      body: JSON.stringify(task),
    }),

  deleteTask: (token, id) =>
    request(`/tasks/${id}`, {
      method: "DELETE",
      token,
    }),
};
