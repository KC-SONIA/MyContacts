import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Navbar – top navigation bar with links and logout button.
 */
export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">📇 MyContacts</Link>
      </div>
      <div className="navbar-links">
        <Link to="/" className="nav-link">Dashboard</Link>
        <Link to="/contacts/new" className="nav-link">Add Contact</Link>
        <Link to="/bin" className="nav-link">🗑️ Bin</Link>
      </div>
      <div className="navbar-user">
        <span className="user-greeting">Hello, {user.username}</span>
        <button onClick={handleLogout} className="btn btn-logout">Logout</button>
      </div>
    </nav>
  );
}
