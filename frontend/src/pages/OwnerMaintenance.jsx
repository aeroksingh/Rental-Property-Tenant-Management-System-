import { useEffect, useState } from 'react';
import * as propertyApi from '../api/propertyApi';
import * as maintenanceApi from '../api/maintenanceApi';
import { extractErrorMessage } from '../api/axiosClient';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import SelectField from '../components/SelectField';
import Banner from '../components/Banner';
import EmptyState from '../components/EmptyState';
import StatusDot from '../components/StatusDot';

const PRIORITY_BORDER = {
  HIGH: 'border-l-rust',
  MEDIUM: 'border-l-gold',
  LOW: 'border-l-steel',
};

const NEXT_STATUS = {
  RAISED: ['IN_PROGRESS', 'REJECTED'],
  IN_PROGRESS: ['RESOLVED', 'REJECTED'],
};

export default function OwnerMaintenance() {
  const [properties, setProperties] = useState([]);
  const [selectedPropertyId, setSelectedPropertyId] = useState('');
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    propertyApi
      .getMyProperties()
      .then(({ data }) => {
        setProperties(data);
        if (data.length > 0) setSelectedPropertyId(String(data[0].id));
      })
      .catch((err) => setError(extractErrorMessage(err)));
  }, []);

  async function loadRequests(propertyId) {
    if (!propertyId) return;
    setLoading(true);
    setError('');
    try {
      const { data } = await maintenanceApi.getRequestsForProperty(propertyId);
      setRequests(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadRequests(selectedPropertyId);
  }, [selectedPropertyId]);

  async function handleStatusChange(requestId, status) {
    setUpdatingId(requestId);
    setError('');
    try {
      await maintenanceApi.updateRequestStatus(requestId, status);
      await loadRequests(selectedPropertyId);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <div>
      <PageHeader title="Maintenance queue" subtitle="Requests raised by tenants, by property" />

      <Banner tone="error">{error}</Banner>

      {properties.length === 0 ? (
        <EmptyState title="No properties yet" description="Add a property first to see its maintenance queue." />
      ) : (
        <>
          <SelectField
            label="Property"
            value={selectedPropertyId}
            onChange={(e) => setSelectedPropertyId(e.target.value)}
            className="max-w-sm mb-6"
          >
            {properties.map((p) => (
              <option key={p.id} value={p.id}>
                {p.address}, {p.city}
              </option>
            ))}
          </SelectField>

          {loading ? (
            <p className="text-sm text-ink-soft">Loading…</p>
          ) : requests.length === 0 ? (
            <EmptyState title="No requests for this property" />
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
                        Raised {new Date(r.createdAt).toLocaleDateString()} · Priority {r.priority.toLowerCase()}
                      </p>
                    </div>
                    <div className="flex flex-col items-end gap-2 shrink-0">
                      <StatusDot status={r.status} />
                      {NEXT_STATUS[r.status] && (
                        <div className="flex gap-2">
                          {NEXT_STATUS[r.status].map((next) => (
                            <button
                              key={next}
                              onClick={() => handleStatusChange(r.id, next)}
                              disabled={updatingId === r.id}
                              className="text-xs font-medium text-pine hover:underline disabled:opacity-50"
                            >
                              {next === 'IN_PROGRESS' ? 'Start work' : next === 'RESOLVED' ? 'Resolve' : 'Reject'}
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
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
