import { apiClient } from './axiosClient';

export function register({ name, email, password, phone, role }) {
  return apiClient.post('/auth/register', { name, email, password, phone, role });
}

export function login({ email, password }) {
  return apiClient.post('/auth/login', { email, password });
}
