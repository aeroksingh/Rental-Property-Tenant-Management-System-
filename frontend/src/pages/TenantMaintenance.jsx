import { useEffect, useState } from 'react';
import * as maintenanceApi from '../api/maintenanceApi';
import * as tenantApi from '../api/tenantApi';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/axiosClient';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import TextField from '../components/TextField';
import SelectField from '../components/SelectField';
import Banner from '../components/Banner';
import EmptyState from '../components/EmptyState';
import StatusDot from '../components/StatusDot';

const PRIORITY_BORDER = {
  HIGH: 'border-l-rust',
  MEDIUM: 'border-l-gold',
  LOW: 'border-l-steel',
};

const emptyForm = { title: '', description: '', priority: 'MEDIUM' };

export default function TenantMaintenance() {
  const { user } = useAuth();
  const [propertyId, setPropertyId] = useState(null);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState(emptyForm);
  const [submitting, setSubmitting] = useState(false);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [propertyRes, requestsRes] = await Promise.all([
        tenantApi.getTenantProperty(user.id).catch((err) => {
          if (err.response?.status === 404) return { data: null };
          throw err;
        }),
        maintenanceApi.getMyRequests(),
      ]);
      setPropertyId(propertyRes.data?.id ?? null);
      setRequests(requestsRes.data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [user.id]);

  async function handleSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await maintenanceApi.createMaintenanceRequest({ propertyId, ...form });
      setForm(emptyForm);
      setSuccess('Request raised. Your landlord has been notified.');
      await load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <PageHeader title="Maintenance requests" subtitle="Raise issues and track their progress" />

      <Banner tone="error">{error}</Banner>
      <Banner tone="success">{success}</Banner>

      {!propertyId && !loading ? (
        <EmptyState
          title="Not assigned to a property yet"
          description="You'll be able to raise requests once your landlord assigns you to a unit."
        />
      ) : (
        <>
          <div className="bg-white border border-border rounded-lg p-6 mb-8 max-w-xl">
            <h2 className="font-display text-base font-semibold text-ink mb-4">Raise a new request</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <TextField
                label="Title"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="e.g. Leaking kitchen tap"
                required
              />
              <label className="block">
                <span className="block text-sm font-medium text-ink-soft mb-1.5">Description</span>
                <textarea
                  className="w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-ink focus:border-pine focus:outline-none focus:ring-1 focus:ring-pine"
                  rows={3}
                  value={form.description}
                  onChange={(e) => setForm({ ...form, description: e.target.value })}
                  placeholder="Any details that will help your landlord assess it"
                />
              </label>
              <SelectField
                label="Priority"
                value={form.priority}
                onChange={(e) => setForm({ ...form, priority: e.target.value })}
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
              </SelectField>
              <Button type="submit" disabled={submitting}>
                {submitting ? 'Submitting…' : 'Submit request'}
              </Button>
            </form>
          </div>

          <h2 className="font-display text-base font-semibold text-ink mb-4">Your requests</h2>
          {loading ? (
            <p className="text-sm text-ink-soft">Loading…</p>
          ) : requests.length === 0 ? (
            <EmptyState title="No requests yet" />
          ) : (
            <div className="space-y-3">
              {requests.map((r) => (
                <div
                  key={r.id}
                  className={`bg-white border border-border border-l-4 ${PRIORITY_BORDER[r.priority] || 'border-l-border'} rounded-md px-5 py-4`}
                >
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="font-medium text-ink">{r.title}</p>
                      {r.description && <p className="text-sm text-ink-soft mt-1">{r.description}</p>}
                      <p className="text-xs text-ink-soft mt-2">
                        Raised {new Date(r.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                    <StatusDot status={r.status} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
