import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

export default function AppLayout() {
  return (
    <div className="flex min-h-screen bg-paper">
      <Sidebar />
      <main className="flex-1 px-10 py-9 max-w-6xl">
        <Outlet />
      </main>
    </div>
  );
}
