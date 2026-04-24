import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { getCards } from '../api/cardApi';

const styles = {
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
  },
  title: { fontSize: '24px', fontWeight: 700, color: '#1a237e' },
  subtitle: { color: '#666', fontSize: '14px', marginTop: '4px' },
  searchBox: {
    display: 'flex',
    gap: '8px',
    alignItems: 'center',
  },
  input: {
    padding: '8px 12px',
    border: '1px solid #ccc',
    borderRadius: '4px',
    fontSize: '14px',
    width: '180px',
  },
  btn: {
    padding: '8px 16px',
    background: '#1a237e',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  btnSecondary: {
    padding: '8px 16px',
    background: '#e0e0e0',
    color: '#333',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    marginTop: '16px',
  },
  th: {
    textAlign: 'left',
    padding: '12px 16px',
    background: '#f5f5f5',
    borderBottom: '2px solid #e0e0e0',
    fontSize: '13px',
    fontWeight: 600,
    color: '#555',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  td: {
    padding: '12px 16px',
    borderBottom: '1px solid #eee',
    fontSize: '14px',
  },
  link: {
    color: '#1a237e',
    textDecoration: 'none',
    fontWeight: 500,
  },
  statusActive: {
    background: '#e8f5e9',
    color: '#2e7d32',
    padding: '2px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 600,
  },
  statusInactive: {
    background: '#fce4ec',
    color: '#c62828',
    padding: '2px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 600,
  },
  legacy: {
    background: '#fff3e0',
    color: '#e65100',
    fontSize: '11px',
    padding: '2px 8px',
    borderRadius: '4px',
    fontWeight: 600,
  },
  loading: { textAlign: 'center', padding: '40px', color: '#999' },
  error: { textAlign: 'center', padding: '40px', color: '#c62828' },
};

function CardListPage() {
  const [cards, setCards] = useState([]);
  const [accountFilter, setAccountFilter] = useState('');
  const [searchValue, setSearchValue] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchCards = useCallback(async (acctId) => {
    setLoading(true);
    setError(null);
    try {
      const res = await getCards(acctId || undefined);
      setCards(res.data);
    } catch (err) {
      setError('Failed to load cards. Is the backend running on port 8080?');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCards();
  }, [fetchCards]);

  const handleSearch = () => {
    setAccountFilter(searchValue);
    fetchCards(searchValue);
  };

  const handleClear = () => {
    setSearchValue('');
    setAccountFilter('');
    fetchCards();
  };

  return (
    <div>
      <div style={styles.header}>
        <div>
          <div style={styles.title}>Credit Cards</div>
          <div style={styles.subtitle}>
            Replaces <span style={styles.legacy}>COCRDLI.bms / COCRDLIC.cbl</span>
          </div>
        </div>
        <div style={styles.searchBox}>
          <input
            style={styles.input}
            placeholder="Account Number"
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
          <button style={styles.btn} onClick={handleSearch}>Search</button>
          {accountFilter && (
            <button style={styles.btnSecondary} onClick={handleClear}>Clear</button>
          )}
        </div>
      </div>

      {loading && <div style={styles.loading}>Loading cards...</div>}
      {error && <div style={styles.error}>{error}</div>}
      {!loading && !error && (
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>Account Number</th>
              <th style={styles.th}>Card Number</th>
              <th style={styles.th}>Cardholder Name</th>
              <th style={styles.th}>Status</th>
              <th style={styles.th}>Expiry</th>
              <th style={styles.th}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {cards.length === 0 ? (
              <tr>
                <td style={styles.td} colSpan="6">No cards found.</td>
              </tr>
            ) : (
              cards.map((card) => (
                <tr key={card.cardNumber}>
                  <td style={styles.td}>{card.accountId}</td>
                  <td style={styles.td}>
                    <Link to={`/cards/${card.cardNumber}`} style={styles.link}>
                      {card.cardNumber}
                    </Link>
                  </td>
                  <td style={styles.td}>{card.embossedName}</td>
                  <td style={styles.td}>
                    <span style={card.activeStatus === 'Y' ? styles.statusActive : styles.statusInactive}>
                      {card.activeStatus === 'Y' ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={styles.td}>{card.expirationDate}</td>
                  <td style={styles.td}>
                    <Link to={`/cards/${card.cardNumber}`} style={styles.link}>View</Link>
                    {' | '}
                    <Link to={`/cards/${card.cardNumber}/edit`} style={styles.link}>Edit</Link>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default CardListPage;
