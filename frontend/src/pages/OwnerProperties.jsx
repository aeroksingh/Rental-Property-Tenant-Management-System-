import { useEffect, useState } from 'react';
import * as propertyApi from '../api/propertyApi';
import { extractErrorMessage } from '../api/axiosClient';
import PageHeader from '../components/PageHeader';
import Button from '../components/Button';
import TextField from '../components/TextField';
import SelectField from '../components/SelectField';
import Banner from '../components/Banner';
import Modal from '../components/Modal';
import EmptyState from '../components/EmptyState';

const PROPERTY_TYPES = ['APARTMENT', 'HOUSE', 'COMMERCIAL'];

const emptyForm = { address: '', city: '', type: 'APARTMENT', rentAmount: '' };

export default function OwnerProperties() {
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [assignModalProperty, setAssignModalProperty] = useState(null);
  const [tenantIdInput, setTenantIdInput] = useState('');
  const [assigning, setAssigning] = useState(false);

  async function loadProperties() {
    setLoading(true);
    setError('');
    try {
      const { data } = await propertyApi.getMyProperties();
      setProperties(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProperties();
  }, []);

  function openCreateModal() {
    setEditingId(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEditModal(property) {
    setEditingId(property.id);
    setForm({
      address: property.address,
      city: property.city,
      type: property.type,
      rentAmount: property.rentAmount,
    });
    setModalOpen(true);
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { ...form, rentAmount: Number(form.rentAmount) };
      if (editingId) {
        await propertyApi.updateProperty(editingId, payload);
      } else {
        await propertyApi.createProperty(payload);
      }
      setModalOpen(false);
      await loadProperties();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Delete this property? This cannot be undone.')) return;
    setError('');
    try {
      await propertyApi.deleteProperty(id);
      await loadProperties();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleAssign(e) {
    e.preventDefault();
    setAssigning(true);
    setError('');
    try {
      await propertyApi.assignTenant(assignModalProperty.id, Number(tenantIdInput));
      setAssignModalProperty(null);
      setTenantIdInput('');
      await loadProperties();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setAssigning(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Properties"
        subtitle="Everything you own, at a glance"
        action={<Button onClick={openCreateModal}>Add property</Button>}
      />

      <Banner tone="error">{error}</Banner>

      {loading ? (
        <p className="text-sm text-ink-soft">Loading…</p>
      ) : properties.length === 0 ? (
        <EmptyState
          title="No properties yet"
          description="Add your first property to start tracking rent and maintenance."
        />
      ) : (
        <div className="bg-white border border-border rounded-lg overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-ink-soft">
                <th className="px-5 py-3 font-medium">Address</th>
                <th className="px-5 py-3 font-medium">City</th>
                <th className="px-5 py-3 font-medium">Type</th>
                <th className="px-5 py-3 font-medium">Rent</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium"></th>
              </tr>
            </thead>
            <tbody>
              {properties.map((property) => (
                <tr key={property.id} className="border-b border-border last:border-0">
                  <td className="px-5 py-3.5 font-medium">{property.address}</td>
                  <td className="px-5 py-3.5 text-ink-soft">{property.city}</td>
                  <td className="px-5 py-3.5 text-ink-soft">{property.type}</td>
                  <td className="px-5 py-3.5 tabular">₹{Number(property.rentAmount).toLocaleString('en-IN')}</td>
                  <td className="px-5 py-3.5">
                    <span
                      className={`inline-flex items-center gap-1.5 text-sm font-medium ${
                        property.isOccupied ? 'text-moss' : 'text-ink-soft'
                      }`}
                    >
                      <span
                        className={`h-1.5 w-1.5 rounded-full ${
                          property.isOccupied ? 'bg-moss' : 'bg-ink-soft/40'
                        }`}
                      />
                      {property.isOccupied ? 'Occupied' : 'Vacant'}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-right space-x-3 whitespace-nowrap">
                    {!property.isOccupied && (
                      <button
                        onClick={() => setAssignModalProperty(property)}
                        className="text-sm text-pine font-medium hover:underline"
                      >
                        Assign tenant
                      </button>
                    )}
                    <button
                      onClick={() => openEditModal(property)}
                      className="text-sm text-ink-soft font-medium hover:text-ink"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(property.id)}
                      className="text-sm text-rust font-medium hover:underline"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editingId ? 'Edit property' : 'Add property'}>
        <form onSubmit={handleSave} className="space-y-4">
          <TextField
            label="Address"
            value={form.address}
            onChange={(e) => setForm({ ...form, address: e.target.value })}
            required
          />
          <TextField
            label="City"
            value={form.city}
            onChange={(e) => setForm({ ...form, city: e.target.value })}
            required
          />
          <SelectField
            label="Type"
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            {PROPERTY_TYPES.map((t) => (
              <option key={t} value={t}>
                {t.charAt(0) + t.slice(1).toLowerCase()}
              </option>
            ))}
          </SelectField>
          <TextField
            label="Monthly rent (₹)"
            type="number"
            min="1"
            step="0.01"
            value={form.rentAmount}
            onChange={(e) => setForm({ ...form, rentAmount: e.target.value })}
            required
          />
          <Button type="submit" className="w-full" disabled={saving}>
            {saving ? 'Saving…' : 'Save'}
          </Button>
        </form>
      </Modal>

      <Modal
        open={!!assignModalProperty}
        onClose={() => setAssignModalProperty(null)}
        title={`Assign tenant to ${assignModalProperty?.address ?? ''}`}
      >
        <form onSubmit={handleAssign} className="space-y-4">
          <TextField
            label="Tenant ID"
            type="number"
            value={tenantIdInput}
            onChange={(e) => setTenantIdInput(e.target.value)}
            placeholder="e.g. 2"
            required
          />
          <p className="text-xs text-ink-soft -mt-2">
            Ask your tenant for the ID shown after they register, or find it via their confirmation email.
          </p>
          <Button type="submit" className="w-full" disabled={assigning}>
            {assigning ? 'Assigning…' : 'Assign'}
          </Button>
        </form>
      </Modal>
    </div>
  );
}
