import axios from 'axios';

// default url for android emulator. 
// if using a physical device, change to your computer's local ip network address (e.g., 192.168.1.x)
const API_BASE_URL = 'http://10.0.2.2:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // required to support the so2_refresh http-only cookie from the backend
  withCredentials: true,
});

// request interceptor to inject access tokens later
apiClient.interceptors.request.use(
  (config) => {
    // token injection logic will be implemented here
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);