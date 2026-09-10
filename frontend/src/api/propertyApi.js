import { apiClient } from './axiosClient';

export function getMyProperties() {
  return apiClient.get('/properties');
}

export function createProperty({ address, city, type, rentAmount }) {
  return apiClient.post('/properties', { address, city, type, rentAmount });
}

export function updateProperty(id, { address, city, type, rentAmount }) {
  return apiClient.put(`/properties/${id}`, { address, city, type, rentAmount });
}

export function deleteProperty(id) {
  return apiClient.delete(`/properties/${id}`);
}

export function assignTenant(propertyId, tenantId) {
  return apiClient.post(`/properties/${propertyId}/assign-tenant/${tenantId}`);
}
