export default function TextField({ label, className = '', ...props }) {
  return (
    <label className={`block ${className}`}>
      {label && <span className="block text-sm font-medium text-ink-soft mb-1.5">{label}</span>}
      <input
        className="w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-ink placeholder:text-ink-soft/50 focus:border-pine focus:outline-none focus:ring-1 focus:ring-pine"
        {...props}
      />
    </label>
  );
}
