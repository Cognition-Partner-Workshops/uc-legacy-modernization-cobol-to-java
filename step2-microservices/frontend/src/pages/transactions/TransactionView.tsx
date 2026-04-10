import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { transactionApi } from '../../api/client';
import type { Transaction } from '../../types';

export default function TransactionView() {
  const { id } = useParams<{ id: string }>();
  const [txn, setTxn] = useState<Transaction | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    transactionApi.get(id)
      .then(res => setTxn(res.data))
      .catch(() => setError('Transaction not found'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading">Loading transaction...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!txn) return null;

  const fmt = (n: number | null) => '$' + (n ?? 0).toLocaleString('en-US', { minimumFractionDigits: 2 });

  return (
    <div>
      <div className="page-header">
        <h1>Transaction #{txn.tranId}</h1>
        <Link to="/transactions" className="btn btn-secondary">Back</Link>
      </div>
      <div className="card">
        <h3 style={{ marginBottom: 16 }}>Transaction Details</h3>
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Transaction ID</div>
            <div className="detail-value">{txn.tranId}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Type Code</div>
            <div className="detail-value">{txn.tranTypeCd || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Category Code</div>
            <div className="detail-value">{txn.tranCatCd ?? '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Amount</div>
            <div className={`detail-value money ${(txn.tranAmt ?? 0) >= 0 ? 'positive' : 'negative'}`}>
              {fmt(txn.tranAmt)}
            </div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Source</div>
            <div className="detail-value">{txn.tranSource || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Description</div>
            <div className="detail-value">{txn.tranDesc || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Card Number</div>
            <div className="detail-value">
              {txn.tranCardNum ? (
                <Link to={`/cards/${encodeURIComponent(txn.tranCardNum)}`}>{txn.tranCardNum}</Link>
              ) : '-'}
            </div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Original Timestamp</div>
            <div className="detail-value">{txn.tranOrigTs || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Processed Timestamp</div>
            <div className="detail-value">{txn.tranProcTs || '-'}</div>
          </div>
        </div>
      </div>
      <div className="card" style={{ marginTop: 16 }}>
        <h3 style={{ marginBottom: 16 }}>Merchant Information</h3>
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Merchant ID</div>
            <div className="detail-value">{txn.tranMerchantId ?? '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Merchant Name</div>
            <div className="detail-value">{txn.tranMerchantName || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Merchant City</div>
            <div className="detail-value">{txn.tranMerchantCity || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Merchant ZIP</div>
            <div className="detail-value">{txn.tranMerchantZip || '-'}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
