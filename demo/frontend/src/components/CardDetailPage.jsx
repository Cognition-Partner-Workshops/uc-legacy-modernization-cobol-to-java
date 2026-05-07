import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCard } from '../api/cardApi';

const styles = {
  breadcrumb: { fontSize: '13px', color: '#999', marginBottom: '16px' },
  breadLink: { color: '#1a237e', textDecoration: 'none' },
  title: { fontSize: '24px', fontWeight: 700, color: '#1a237e', marginBottom: '4px' },
  subtitle: { color: '#666', fontSize: '14px', marginBottom: '24px' },
  legacy: {
    background: '#fff3e0',
    color: '#e65100',
    fontSize: '11px',
    padding: '2px 8px',
    borderRadius: '4px',
    fontWeight: 600,
  },
  card: {
    background: '#fff',
    border: '1px solid #e0e0e0',
    borderRadius: '8px',
    padding: '32px',
    maxWidth: '600px',
  },
  row: {
    display: 'flex',
    padding: '12px 0',
    borderBottom: '1px solid #f5f5f5',
  },
  label: {
    width: '200px',
    fontSize: '13px',
    fontWeight: 600,
    color: '#555',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  value: { fontSize: '15px', color: '#222' },
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
  actions: { marginTop: '24px', display: 'flex', gap: '12px' },
  btn: {
    padding: '10px 20px',
    background: '#1a237e',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
    textDecoration: 'none',
  },
  btnSecondary: {
    padding: '10px 20px',
    background: '#e0e0e0',
    color: '#333',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
    textDecoration: 'none',
  },
  loading: { textAlign: 'center', padding: '40px', color: '#999' },
  error: { textAlign: 'center', padding: '40px', color: '#c62828' },
};

function CardDetailPage() {
  const { cardNumber } = useParams();
  const [card, setCard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchCard = async () => {
      try {
        const res = await getCard(cardNumber);
        setCard(res.data);
      } catch (err) {
        setError('Card not found or backend unavailable.');
      } finally {
        setLoading(false);
      }
    };
    fetchCard();
  }, [cardNumber]);

  if (loading) return <div style={styles.loading}>Loading card details...</div>;
  if (error) return <div style={styles.error}>{error}</div>;
  if (!card) return null;

  return (
    <div>
      <div style={styles.breadcrumb}>
        <Link to="/" style={styles.breadLink}>Cards</Link> &rsaquo; Card Detail
      </div>
      <div style={styles.title}>Card Detail</div>
      <div style={styles.subtitle}>
        Replaces <span style={styles.legacy}>COCRDSL.bms / COCRDSLC.cbl</span>
      </div>

      <div style={styles.card}>
        <div style={styles.row}>
          <div style={styles.label}>Account Number</div>
          <div style={styles.value}>{card.accountId}</div>
        </div>
        <div style={styles.row}>
          <div style={styles.label}>Card Number</div>
          <div style={styles.value}>{card.cardNumber}</div>
        </div>
        <div style={styles.row}>
          <div style={styles.label}>Name on Card</div>
          <div style={styles.value}>{card.embossedName}</div>
        </div>
        <div style={styles.row}>
          <div style={styles.label}>Card Active</div>
          <div style={styles.value}>
            <span style={card.activeStatus === 'Y' ? styles.statusActive : styles.statusInactive}>
              {card.activeStatus === 'Y' ? 'Y — Active' : 'N — Inactive'}
            </span>
          </div>
        </div>
        <div style={styles.row}>
          <div style={styles.label}>Expiry Date</div>
          <div style={styles.value}>{card.expirationDate}</div>
        </div>

        <div style={styles.actions}>
          <Link to={`/cards/${card.cardNumber}/edit`} style={styles.btn}>Edit Card</Link>
          <Link to="/" style={styles.btnSecondary}>Back to List</Link>
        </div>
      </div>
    </div>
  );
}

export default CardDetailPage;
