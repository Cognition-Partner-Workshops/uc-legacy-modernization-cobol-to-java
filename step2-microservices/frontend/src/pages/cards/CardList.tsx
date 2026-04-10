import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { cardApi } from '../../api/client';
import type { Card } from '../../types';

export default function CardList() {
  const [cards, setCards] = useState<Card[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    cardApi.list()
      .then(res => setCards(res.data))
      .catch(() => setError('Failed to load cards'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading cards...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Cards</h1>
        <span className="badge badge-active">{cards.length} total</span>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Card Number</th>
              <th>Account ID</th>
              <th>Embossed Name</th>
              <th>Status</th>
              <th>Expiration</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {cards.map(c => (
              <tr key={c.cardNum}>
                <td><strong>{c.cardNum}</strong></td>
                <td>{c.cardAcctId}</td>
                <td>{c.cardEmbossedName || '-'}</td>
                <td>
                  <span className={`badge ${c.cardActiveStatus === 'Y' ? 'badge-active' : 'badge-inactive'}`}>
                    {c.cardActiveStatus === 'Y' ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>{c.cardExpiraionDate || '-'}</td>
                <td className="actions">
                  <Link to={`/cards/${encodeURIComponent(c.cardNum)}`} className="btn btn-sm btn-primary">View</Link>
                  <Link to={`/cards/${encodeURIComponent(c.cardNum)}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                </td>
              </tr>
            ))}
            {cards.length === 0 && (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: 20 }}>No cards found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
