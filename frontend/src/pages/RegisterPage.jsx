import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/axiosClient';
import Button from '../components/Button';
import TextField from '../components/TextField';
import Banner from '../components/Banner';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'OWNER' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await register(form);
      navigate(user.role === 'OWNER' ? '/owner/properties' : '/tenant/home');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4 py-10">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <h1 className="font-display text-3xl font-semibold text-ink">Ledger</h1>
          <p className="text-sm text-ink-soft mt-1">Create your account</p>
        </div>

        <div className="bg-white border border-border rounded-lg p-7">
          <Banner tone="error">{error}</Banner>

          <div className="flex rounded-md border border-border p-1 mb-5">
            {['OWNER', 'TENANT'].map((role) => (
              <button
                key={role}
                type="button"
                onClick={() => update('role', role)}
                className={`flex-1 rounded py-1.5 text-sm font-medium transition-colors ${
                  form.role === role ? 'bg-pine text-white' : 'text-ink-soft hover:text-ink'
                }`}
              >
                {role === 'OWNER' ? 'Landlord' : 'Tenant'}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <TextField
              label="Full name"
              value={form.name}
              onChange={(e) => update('name', e.target.value)}
              required
            />
            <TextField
              label="Email"
              type="email"
              value={form.email}
              onChange={(e) => update('email', e.target.value)}
              required
            />
            <TextField
              label="Phone"
              value={form.phone}
              onChange={(e) => update('phone', e.target.value)}
            />
            <TextField
              label="Password"
              type="password"
              minLength={6}
              value={form.password}
              onChange={(e) => update('password', e.target.value)}
              required
            />
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Creating account…' : 'Create account'}
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-ink-soft mt-5">
          Already have an account?{' '}
          <Link to="/login" className="text-pine font-medium hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
