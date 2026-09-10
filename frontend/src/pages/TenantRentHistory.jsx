import { useEffect, useState } from 'react';
import * as rentPaymentApi from '../api/rentPaymentApi';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/axiosClient';
import PageHeader from '../components/PageHeader';
import Banner from '../components/Banner';
import EmptyState from '../components/EmptyState';
import StatusDot from '../components/StatusDot';

export default function TenantRentHistory() {
  const { user } = useAuth();
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError('');
      try {
        const { data } = await rentPaymentApi.getPaymentsForTenant(user.id);
        // Most recent due date first.
        setPayments([...data].sort((a, b) => new Date(b.dueDate) - new Date(a.dueDate)));
      } catch (err) {
        setError(extractErrorMessage(err));
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [user.id]);

  return (
    <div>
      <PageHeader title="Rent history" subtitle="Every payment due entry raised by your landlord" />

      <Banner tone="error">{error}</Banner>

      {loading ? (
        <p className="text-sm text-ink-soft">Loading…</p>
      ) : payments.length === 0 ? (
        <EmptyState title="No rent payments yet" description="Your landlord hasn't created any due entries yet." />
      ) : (
        <div className="bg-white border border-border rounded-lg overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-ink-soft">
                <th className="px-5 py-3 font-medium">Due date</th>
                <th className="px-5 py-3 font-medium">Amount due</th>
                <th className="px-5 py-3 font-medium">Amount paid</th>
                <th className="px-5 py-3 font-medium">Paid on</th>
                <th className="px-5 py-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id} className="border-b border-border last:border-0">
                  <td className="px-5 py-3.5 tabular">{p.dueDate}</td>
                  <td className="px-5 py-3.5 tabular font-medium">₹{Number(p.amountDue).toLocaleString('en-IN')}</td>
                  <td className="px-5 py-3.5 tabular text-ink-soft">
                    ₹{Number(p.amountPaid).toLocaleString('en-IN')}
                  </td>
                  <td className="px-5 py-3.5 tabular text-ink-soft">{p.paidDate || '—'}</td>
                  <td className="px-5 py-3.5">
                    <StatusDot status={p.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
