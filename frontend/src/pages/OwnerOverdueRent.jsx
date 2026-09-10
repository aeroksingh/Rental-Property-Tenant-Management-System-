import { useEffect, useState } from 'react';
import * as rentPaymentApi from '../api/rentPaymentApi';
import { extractErrorMessage } from '../api/axiosClient';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import TextField from '../components/TextField';
import Banner from '../components/Banner';
import Modal from '../components/Modal';
import EmptyState from '../components/EmptyState';
import StatusDot from '../components/StatusDot';

const emptyForm = { tenantId: '', amountDue: '', dueDate: '' };

export default function OwnerOverdueRent() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [markingPaidId, setMarkingPaidId] = useState(null);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const { data } = await rentPaymentApi.getOverduePayments();
      setPayments(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      await rentPaymentApi.createRentPayment({
        tenantId: Number(form.tenantId),
        amountDue: Number(form.amountDue),
        dueDate: form.dueDate,
      });
      setModalOpen(false);
      setForm(emptyForm);
      setSuccess('Rent payment created. It will show up here once overdue, or in the tenant\u2019s rent history now.');
      await load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleMarkPaid(paymentId) {
    setMarkingPaidId(paymentId);
    setError('');
    try {
      await rentPaymentApi.markPaid(paymentId);
      await load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setMarkingPaidId(null);
    }
  }

  return (
    <div>
      <PageHeader
        title="Overdue rent"
        subtitle="Payments past their due date, still unpaid"
        action={<Button onClick={() => setModalOpen(true)}>Create rent due entry</Button>}
      />

      <Banner tone="error">{error}</Banner>
      <Banner tone="success">{success}</Banner>

      {loading ? (
        <p className="text-sm text-ink-soft">Loading…</p>
      ) : payments.length === 0 ? (
        <EmptyState title="Nothing overdue" description="Every rent payment is either paid or not yet due." />
      ) : (
        <div className="bg-white border border-border rounded-lg overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-ink-soft">
                <th className="px-5 py-3 font-medium">Tenant ID</th>
                <th className="px-5 py-3 font-medium">Property ID</th>
                <th className="px-5 py-3 font-medium">Amount due</th>
                <th className="px-5 py-3 font-medium">Due date</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr key={p.id} className="border-b border-border last:border-0">
                  <td className="px-5 py-3.5">{p.tenantId}</td>
                  <td className="px-5 py-3.5 text-ink-soft">{p.propertyId}</td>
                  <td className="px-5 py-3.5 tabular font-medium">₹{Number(p.amountDue).toLocaleString('en-IN')}</td>
                  <td className="px-5 py-3.5 tabular text-ink-soft">{p.dueDate}</td>
                  <td className="px-5 py-3.5">
                    <StatusDot status={p.status} />
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <Button
                      variant="secondary"
                      onClick={() => handleMarkPaid(p.id)}
                      disabled={markingPaidId === p.id}
                    >
                      {markingPaidId === p.id ? 'Marking…' : 'Mark paid'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Create rent due entry">
        <form onSubmit={handleCreate} className="space-y-4">
          <TextField
            label="Tenant ID"
            type="number"
            value={form.tenantId}
            onChange={(e) => setForm({ ...form, tenantId: e.target.value })}
            required
          />
          <TextField
            label="Amount due (₹)"
            type="number"
            min="1"
            step="0.01"
            value={form.amountDue}
            onChange={(e) => setForm({ ...form, amountDue: e.target.value })}
            required
          />
          <TextField
            label="Due date"
            type="date"
            value={form.dueDate}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
            required
          />
          <Button type="submit" className="w-full" disabled={saving}>
            {saving ? 'Creating…' : 'Create'}
          </Button>
        </form>
      </Modal>
    </div>
  );
}
