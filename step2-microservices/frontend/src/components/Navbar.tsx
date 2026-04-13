import { NavLink, useNavigate } from 'react-router-dom';

interface NavbarProps {
  user: { userId: string; userType: string } | null;
  onLogout: () => void;
}

export default function Navbar({ user, onLogout }: NavbarProps) {
  const navigate = useNavigate();

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  return (
    <nav className="sidebar">
      <div className="sidebar-header">
        <h2>CardDemo</h2>
        <div className="subtitle">Microservices Dashboard</div>
      </div>
      <div className="sidebar-nav">
        <NavLink to="/" end>
          <span className="nav-icon">&#127968;</span> Dashboard
        </NavLink>
        <NavLink to="/accounts">
          <span className="nav-icon">&#128179;</span> Accounts
        </NavLink>
        <NavLink to="/cards">
          <span className="nav-icon">&#128179;</span> Cards
        </NavLink>
        <NavLink to="/customers">
          <span className="nav-icon">&#128100;</span> Customers
        </NavLink>
        <NavLink to="/transactions">
          <span className="nav-icon">&#128176;</span> Transactions
        </NavLink>
      </div>
      {user && (
        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{user.userId.charAt(0).toUpperCase()}</div>
            <div>
              <div style={{ color: '#e8eaed', fontWeight: 500 }}>{user.userId}</div>
              <div>{user.userType === 'A' ? 'Admin' : 'User'}</div>
            </div>
          </div>
          <button onClick={handleLogout}>Sign Out</button>
        </div>
      )}
    </nav>
  );
}
