import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { transactionApi } from '../../api/client';
import type { Transaction } from '../../types';

export default function TransactionList() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    transactionApi.list()
      .then(res => setTransactions(res.data))
      .catch(() => setError('Failed to load transactions'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading transactions...</div>;

  const fmt = (n: number | null) => '$' + (n ?? 0).toLocaleString('en-US', { minimumFractionDigits: 2 });

  return (
    <div>
      <div className="page-header">
        <h1>Transactions</h1>
        <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
          <span className="badge badge-active">{transactions.length} total</span>
          <Link to="/transactions/new" className="btn btn-success">New Transaction</Link>
        </div>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Transaction ID</th>
              <th>Type</th>
              <th>Category</th>
              <th>Amount</th>
              <th>Card Number</th>
              <th>Merchant</th>
              <th>Timestamp</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map(t => (
              <tr key={t.tranId}>
                <td><strong>{t.tranId}</strong></td>
                <td>{t.tranTypeCd || '-'}</td>
                <td>{t.tranCatCd ?? '-'}</td>
                <td className={`money ${(t.tranAmt ?? 0) >= 0 ? 'positive' : 'negative'}`}>
                  {fmt(t.tranAmt)}
                </td>
                <td>{t.tranCardNum || '-'}</td>
                <td>{t.tranMerchantName || '-'}</td>
                <td>{t.tranOrigTs ? new Date(t.tranOrigTs).toLocaleString() : '-'}</td>
                <td className="actions">
                  <Link to={`/transactions/${t.tranId}`} className="btn btn-sm btn-primary">View</Link>
                </td>
              </tr>
            ))}
            {transactions.length === 0 && (
              <tr><td colSpan={8} style={{ textAlign: 'center', padding: 20 }}>No transactions found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
