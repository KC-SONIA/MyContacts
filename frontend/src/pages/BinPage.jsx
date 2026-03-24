import { useState, useEffect } from 'react';
import api from '../api/axios';
import LoadingSpinner from '../components/LoadingSpinner';

/**
 * BinPage – shows soft-deleted contacts with restore/permanent delete options.
 */
export default function BinPage() {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchBin = async () => {
    setLoading(true);
    try {
      const res = await api.get('/contacts/bin');
      setContacts(res.data);
    } catch {
      setError('Failed to load bin');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBin();
  }, []);

  const handleRestore = async (id) => {
    try {
      await api.put(`/contacts/restore/${id}`);
      fetchBin();
    } catch {
      setError('Failed to restore contact');
    }
  };

  const handlePermanentDelete = async (id) => {
    if (!window.confirm('Permanently delete this contact? This cannot be undone.')) return;
    try {
      await api.delete(`/contacts/permanent/${id}`);
      fetchBin();
    } catch {
      setError('Failed to permanently delete contact');
    }
  };

  if (loading) return <LoadingSpinner message="Loading bin..." />;

  return (
    <div className="dashboard-page">
      <div className="page-header">
        <h2>🗑️ Bin</h2>
        <p className="subtitle">Deleted contacts can be restored or permanently removed</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      {contacts.length === 0 ? (
        <div className="empty-state">
          <p>🎉 Bin is empty!</p>
        </div>
      ) : (
        <div className="contacts-grid">
          {contacts.map((contact) => (
            <div key={contact.id} className="contact-card bin-card">
              <div className="contact-avatar deleted">
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
                </div>
              </div>
              <div className="contact-actions">
                <button onClick={() => handleRestore(contact.id)} className="btn btn-success" title="Restore">
                  ♻️ Restore
                </button>
                <button onClick={() => handlePermanentDelete(contact.id)} className="btn btn-danger" title="Delete Forever">
                  💀 Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
