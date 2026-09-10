const STATUS_STYLES = {
  PAID: { color: 'bg-moss', text: 'text-moss' },
  RESOLVED: { color: 'bg-moss', text: 'text-moss' },
  PENDING: { color: 'bg-gold', text: 'text-gold' },
  RAISED: { color: 'bg-gold', text: 'text-gold' },
  OVERDUE: { color: 'bg-rust', text: 'text-rust' },
  REJECTED: { color: 'bg-rust', text: 'text-rust' },
  IN_PROGRESS: { color: 'bg-steel', text: 'text-steel' },
};

function toLabel(status) {
  return status.charAt(0) + status.slice(1).toLowerCase().replace('_', ' ');
}

export default function StatusDot({ status }) {
  const style = STATUS_STYLES[status] || { color: 'bg-ink-soft', text: 'text-ink-soft' };
  return (
    <span className="inline-flex items-center gap-1.5 text-sm font-medium">
      <span className={`h-1.5 w-1.5 rounded-full ${style.color}`} />
      <span className={style.text}>{toLabel(status)}</span>
    </span>
  );
}
