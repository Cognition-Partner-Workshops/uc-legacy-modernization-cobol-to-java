import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';

interface LayoutProps {
  user: { userId: string; userType: string } | null;
  onLogout: () => void;
}

export default function Layout({ user, onLogout }: LayoutProps) {
  return (
    <div className="app-layout">
      <Navbar user={user} onLogout={onLogout} />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
