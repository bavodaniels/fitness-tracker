const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

function getToken() {
  const user = sessionStorage.getItem('user');
  if (!user) return null;
  return JSON.parse(user).token;
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...options.headers };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.error || data.message || 'Request failed');
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export const authApi = {
  register(username, password) {
    return request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },
  login(username, password) {
    return request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
  },
  logout() {
    return request('/auth/logout', { method: 'POST' });
  },
};

export const workoutApi = {
  create(workout) {
    return request('/workouts', {
      method: 'POST',
      body: JSON.stringify(workout),
    });
  },
  getAll() {
    return request('/workouts');
  },
  getById(id) {
    return request(`/workouts/${id}`);
  },
};
