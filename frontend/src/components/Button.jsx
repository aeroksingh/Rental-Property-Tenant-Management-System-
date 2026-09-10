const VARIANTS = {
  primary: 'bg-pine text-white hover:bg-pine-hover',
  secondary: 'bg-white text-ink border border-border hover:bg-paper',
  danger: 'bg-white text-rust border border-rust/40 hover:bg-rust-light',
};

export default function Button({ variant = 'primary', className = '', children, ...props }) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed ${VARIANTS[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
