import { useEffect, useState } from 'react';
import * as tenantApi from '../api/tenantApi';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/axiosClient';
import PageHeader from '../components/PageHeader';
import Banner from '../components/Banner';
import EmptyState from '../components/EmptyState';

export default function TenantHome() {
  const { user } = useAuth();
  const [property, setProperty] = useState(null);
  const [tenant, setTenant] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError('');
      try {
        const [propertyRes, tenantRes] = await Promise.all([
          tenantApi.getTenantProperty(user.id),
          tenantApi.getTenant(user.id),
        ]);
        setProperty(propertyRes.data);
        setTenant(tenantRes.data);
      } catch (err) {
        // 404 just means "not yet assigned to a property" - not a real error.
        if (err.response?.status === 404) {
          setProperty(null);
        } else {
          setError(extractErrorMessage(err));
        }
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [user.id]);

  const daysUntilLeaseEnd = tenant?.leaseEndDate
    ? Math.ceil((new Date(tenant.leaseEndDate) - new Date()) / (1000 * 60 * 60 * 24))
    : null;

  return (
    <div>
      <PageHeader title="My property" subtitle={`Welcome back, ${user.name}`} />

      <Banner tone="error">{error}</Banner>

      {loading ? (
        <p className="text-sm text-ink-soft">Loading…</p>
      ) : !property ? (
        <EmptyState
          title="Not assigned to a property yet"
          description="Share your tenant ID with your landlord so they can assign you to your unit."
        />
      ) : (
        <div className="bg-white border border-border rounded-lg p-6 max-w-lg">
          <p className="font-display text-xl font-semibold text-ink">{property.address}</p>
          <p className="text-sm text-ink-soft mt-1">{property.city}</p>

          <div className="grid grid-cols-2 gap-6 mt-6 pt-6 border-t border-border">
            <div>
              <p className="text-xs text-ink-soft">Type</p>
              <p className="text-sm font-medium mt-0.5">{property.type}</p>
            </div>
            <div>
              <p className="text-xs text-ink-soft">Monthly rent</p>
              <p className="text-sm font-medium mt-0.5 tabular">
                ₹{Number(property.rentAmount).toLocaleString('en-IN')}
              </p>
            </div>
            {tenant?.leaseStartDate && (
              <div>
                <p className="text-xs text-ink-soft">Lease start</p>
                <p className="text-sm font-medium mt-0.5">{tenant.leaseStartDate}</p>
              </div>
            )}
            {tenant?.leaseEndDate && (
              <div>
                <p className="text-xs text-ink-soft">Lease end</p>
                <p className="text-sm font-medium mt-0.5">{tenant.leaseEndDate}</p>
              </div>
            )}
          </div>

          {daysUntilLeaseEnd !== null && daysUntilLeaseEnd <= 30 && daysUntilLeaseEnd >= 0 && (
            <div className="mt-5 rounded-md bg-gold-light text-gold text-sm px-4 py-2.5">
              Your lease ends in {daysUntilLeaseEnd} day{daysUntilLeaseEnd === 1 ? '' : 's'}. Reach out to your
              landlord about renewing if you plan to stay.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
