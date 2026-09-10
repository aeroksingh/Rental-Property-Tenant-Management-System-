export default function Banner({ tone = 'error', children }) {
  if (!children) return null;

  const styles = {
    error: 'bg-rust-light text-rust border-rust/20',
    success: 'bg-moss-light text-moss border-moss/20',
  };

  return (
    <div className={`rounded-md border px-4 py-2.5 text-sm mb-5 ${styles[tone]}`}>{children}</div>
  );
}
