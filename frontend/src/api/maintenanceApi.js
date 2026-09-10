import { apiClient } from './axiosClient';

export function createMaintenanceRequest({ propertyId, title, description, priority }) {
  return apiClient.post('/maintenance-requests', { propertyId, title, description, priority });
}

export function getRequestsForProperty(propertyId) {
  return apiClient.get(`/maintenance-requests/property/${propertyId}`);
}

export function getMyRequests() {
  return apiClient.get('/maintenance-requests/my-requests');
}

export function updateRequestStatus(requestId, status) {
  return apiClient.put(`/maintenance-requests/${requestId}/status`, { status });
}
