import { apiClient } from './axiosClient';

export function createRentPayment({ tenantId, amountDue, dueDate }) {
  return apiClient.post('/rent-payments', { tenantId, amountDue, dueDate });
}

export function getPaymentsForTenant(tenantId) {
  return apiClient.get(`/rent-payments/tenant/${tenantId}`);
}

export function getOverduePayments() {
  return apiClient.get('/rent-payments/overdue');
}

export function markPaid(paymentId, { amountPaid, paidDate } = {}) {
  return apiClient.put(`/rent-payments/${paymentId}/mark-paid`, { amountPaid, paidDate });
}
