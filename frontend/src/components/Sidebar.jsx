import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ownerLinks = [
  { to: '/owner/properties', label: 'Properties' },
  { to: '/owner/overdue-rent', label: 'Overdue rent' },
  { to: '/owner/maintenance', label: 'Maintenance queue' },
];

const tenantLinks = [
  { to: '/tenant/home', label: 'My property' },
  { to: '/tenant/rent-history', label: 'Rent history' },
  { to: '/tenant/maintenance', label: 'Maintenance requests' },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const links = user?.role === 'OWNER' ? ownerLinks : tenantLinks;

  return (
    <aside className="w-64 shrink-0 bg-pine text-white flex flex-col h-screen sticky top-0">
      <div className="px-6 py-7">
        <span className="font-display text-2xl font-semibold tracking-tight">Ledger</span>
        <p className="text-sm text-white/60 mt-1">Rental management</p>
      </div>

      <nav className="flex-1 px-3 space-y-1">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            className={({ isActive }) =>
              `block rounded-md px-3 py-2.5 text-sm font-medium transition-colors ${
                isActive ? 'bg-white/15 text-white' : 'text-white/75 hover:bg-white/10 hover:text-white'
              }`
            }
          >
            {link.label}
          </NavLink>
        ))}
      </nav>

      <div className="px-6 py-5 border-t border-white/15">
        <p className="text-sm font-medium truncate">{user?.name}</p>
        <p className="text-xs text-white/60 truncate">{user?.email}</p>
        <button
          onClick={logout}
          className="mt-3 text-sm text-white/80 hover:text-white underline underline-offset-2"
        >
          Log out
        </button>
      </div>
    </aside>
  );
}
