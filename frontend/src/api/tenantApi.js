import { apiClient } from './axiosClient';

export function getTenant(id) {
  return apiClient.get(`/tenants/${id}`);
}

export function getTenantProperty(id) {
  return apiClient.get(`/tenants/${id}/property`);
}
