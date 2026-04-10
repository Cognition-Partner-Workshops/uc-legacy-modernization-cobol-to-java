import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { cardApi } from '../../api/client';
import type { Card, CardXref } from '../../types';

export default function CardView() {
  const { cardNum } = useParams<{ cardNum: string }>();
  const [card, setCard] = useState<Card | null>(null);
  const [xref, setXref] = useState<CardXref | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!cardNum) return;
    const decoded = decodeURIComponent(cardNum);
    Promise.all([
      cardApi.get(decoded).catch(() => null),
      cardApi.getXref(decoded).catch(() => null),
    ]).then(([cardRes, xrefRes]) => {
      if (cardRes) setCard(cardRes.data);
      else setError('Card not found');
      if (xrefRes) setXref(xrefRes.data);
    }).finally(() => setLoading(false));
  }, [cardNum]);

  if (loading) return <div className="loading">Loading card...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!card) return null;

  return (
    <div>
      <div className="page-header">
        <h1>Card Details</h1>
        <div style={{ display: 'flex', gap: 8 }}>
          <Link to={`/cards/${encodeURIComponent(card.cardNum)}/edit`} className="btn btn-primary">Edit</Link>
          <Link to="/cards" className="btn btn-secondary">Back</Link>
        </div>
      </div>
      <div className="card">
        <h3 style={{ marginBottom: 16 }}>Card Information</h3>
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Card Number</div>
            <div className="detail-value">{card.cardNum}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Account ID</div>
            <div className="detail-value">
              <Link to={`/accounts/${card.cardAcctId}`}>{card.cardAcctId}</Link>
            </div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Embossed Name</div>
            <div className="detail-value">{card.cardEmbossedName || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">CVV Code</div>
            <div className="detail-value">{card.cardCvvCd ?? '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Status</div>
            <div className="detail-value">
              <span className={`badge ${card.cardActiveStatus === 'Y' ? 'badge-active' : 'badge-inactive'}`}>
                {card.cardActiveStatus === 'Y' ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Expiration Date</div>
            <div className="detail-value">{card.cardExpiraionDate || '-'}</div>
          </div>
        </div>
      </div>
      {xref && (
        <div className="card" style={{ marginTop: 16 }}>
          <h3 style={{ marginBottom: 16 }}>Cross Reference</h3>
          <div className="detail-grid">
            <div className="detail-item">
              <div className="detail-label">Customer ID</div>
              <div className="detail-value">
                <Link to={`/customers/${xref.xrefCustId}`}>{xref.xrefCustId}</Link>
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Account ID</div>
              <div className="detail-value">
                <Link to={`/accounts/${xref.xrefAcctId}`}>{xref.xrefAcctId}</Link>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
