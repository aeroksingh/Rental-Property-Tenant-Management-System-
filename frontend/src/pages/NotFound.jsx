import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-paper text-center px-4">
      <h1 className="font-display text-3xl font-semibold text-ink">Page not found</h1>
      <p className="text-sm text-ink-soft mt-2">The page you're looking for doesn't exist.</p>
      <Link to="/" className="mt-5 text-pine font-medium hover:underline">
        Back to home
      </Link>
    </div>
  );
}
