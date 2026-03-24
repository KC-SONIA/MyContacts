import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import LoadingSpinner from '../components/LoadingSpinner';

/**
 * DashboardPage – displays contacts with search and pagination.
 */
export default function DashboardPage() {
  const [contacts, setContacts] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchContacts = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/contacts', { params: { page, size: 10 } });
      setContacts(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch (err) {
      setError('Failed to load contacts');
    } finally {
      setLoading(false);
    }
  }, [page]);

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      fetchContacts();
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await api.get('/contacts/search', { params: { query: searchQuery } });
      setContacts(res.data);
      setTotalPages(0); // Search results are not paginated
    } catch (err) {
      setError('Search failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!searchQuery.trim()) {
      fetchContacts();
    }
  }, [fetchContacts, searchQuery]);

  const handleDelete = async (id) => {
    if (!window.confirm('Move this contact to bin?')) return;
    try {
      await api.delete(`/contacts/${id}`);
      fetchContacts();
    } catch (err) {
      setError('Failed to delete contact');
    }
  };

  const clearSearch = () => {
    setSearchQuery('');
    setPage(0);
  };

  if (loading && contacts.length === 0) return <LoadingSpinner message="Loading contacts..." />;

  return (
    <div className="dashboard-page">
      <div className="page-header">
        <h2>My Contacts</h2>
        <Link to="/contacts/new" className="btn btn-primary">
          + Add Contact
        </Link>
      </div>

      {/* Search Bar */}
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by name or phone number..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button onClick={handleSearch} className="btn btn-secondary">Search</button>
        {searchQuery && (
          <button onClick={clearSearch} className="btn btn-ghost">Clear</button>
        )}
      </div>

      {error && <div className="error-message">{error}</div>}

      {/* Contacts List */}
      {contacts.length === 0 ? (
        <div className="empty-state">
          <p>📋 No contacts found</p>
          <Link to="/contacts/new" className="btn btn-primary">Add your first contact</Link>
        </div>
      ) : (
        <div className="contacts-grid">
          {contacts.map((contact) => (
            <div key={contact.id} className="contact-card">
              <div className="contact-avatar">
                {contact.firstName.charAt(0).toUpperCase()}
                {contact.lastName ? contact.lastName.charAt(0).toUpperCase() : ''}
              </div>
              <div className="contact-info">
                <h3>{contact.firstName} {contact.lastName || ''}</h3>
                <div className="contact-details">
                  {contact.phoneNumbers.map((p, i) => (
                    <span key={i} className="detail-chip phone">📱 {p.phoneNumber}</span>
                  ))}
                  {contact.emails.map((e, i) => (
                    <span key={i} className="detail-chip email">✉️ {e.email}</span>
                  ))}
                  {contact.birthday && (
                    <span className="detail-chip birthday">🎂 {contact.birthday}</span>
                  )}
                </div>
              </div>
              <div className="contact-actions">
                <Link to={`/contacts/edit/${contact.id}`} className="btn btn-icon" title="Edit">
                  ✏️
                </Link>
                <button onClick={() => handleDelete(contact.id)} className="btn btn-icon btn-danger" title="Delete">
                  🗑️
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="btn btn-secondary"
          >
            ← Previous
          </button>
          <span className="page-info">Page {page + 1} of {totalPages}</span>
          <button
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="btn btn-secondary"
          >
            Next →
          </button>
        </div>
      )}
    </div>
  );
}
