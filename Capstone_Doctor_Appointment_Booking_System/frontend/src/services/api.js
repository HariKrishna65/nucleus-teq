// Centralizes HTTP requests to the backend API and attaches auth headers.
import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://127.0.0.1:8000',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && localStorage.getItem('accessToken')) {
      localStorage.removeItem('accessToken');
      window.dispatchEvent(new Event('session-expired'));
    }
    return Promise.reject(error);
  },
);
