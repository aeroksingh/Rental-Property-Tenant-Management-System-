export default function EmptyState({ title, description }) {
  return (
    <div className="rounded-lg border border-dashed border-border bg-white/50 px-6 py-12 text-center">
      <p className="font-display text-base font-medium text-ink">{title}</p>
      {description && <p className="text-sm text-ink-soft mt-1">{description}</p>}
    </div>
  );
}
