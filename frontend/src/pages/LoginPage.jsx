import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/axiosClient';
import Button from '../components/Button';
import TextField from '../components/TextField';
import Banner from '../components/Banner';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login({ email, password });
      navigate(user.role === 'OWNER' ? '/owner/properties' : '/tenant/home');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <h1 className="font-display text-3xl font-semibold text-ink">Ledger</h1>
          <p className="text-sm text-ink-soft mt-1">Sign in to manage your rentals</p>
        </div>

        <div className="bg-white border border-border rounded-lg p-7">
          <Banner tone="error">{error}</Banner>
          <form onSubmit={handleSubmit} className="space-y-4">
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-ink-soft mt-5">
          Don&apos;t have an account?{' '}
          <Link to="/register" className="text-pine font-medium hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}
