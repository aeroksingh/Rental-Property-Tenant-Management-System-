export default function SelectField({ label, className = '', children, ...props }) {
  return (
    <label className={`block ${className}`}>
      {label && <span className="block text-sm font-medium text-ink-soft mb-1.5">{label}</span>}
      <select
        className="w-full rounded-md border border-border bg-white px-3 py-2 text-sm text-ink focus:border-pine focus:outline-none focus:ring-1 focus:ring-pine"
        {...props}
      >
        {children}
      </select>
    </label>
  );
}
