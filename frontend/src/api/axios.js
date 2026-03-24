import axios from 'axios';

/**
 * Axios instance configured for the backend API.
 * withCredentials ensures session cookies are sent with every request.
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;
