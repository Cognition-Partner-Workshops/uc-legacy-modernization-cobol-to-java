import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { accountApi } from '../../api/client';
import type { Account } from '../../types';

export default function AccountList() {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    accountApi.list()
      .then(res => setAccounts(res.data))
      .catch(() => setError('Failed to load accounts'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading accounts...</div>;

  const fmt = (n: number | null) => '$' + (n ?? 0).toLocaleString('en-US', { minimumFractionDigits: 2 });

  return (
    <div>
      <div className="page-header">
        <h1>Accounts</h1>
        <span className="badge badge-active">{accounts.length} total</span>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Account ID</th>
              <th>Status</th>
              <th>Balance</th>
              <th>Credit Limit</th>
              <th>Open Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map(a => (
              <tr key={a.acctId}>
                <td><strong>{a.acctId}</strong></td>
                <td>
                  <span className={`badge ${a.acctActiveStatus === 'Y' ? 'badge-active' : 'badge-inactive'}`}>
                    {a.acctActiveStatus === 'Y' ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className={`money ${(a.acctCurrBal ?? 0) >= 0 ? 'positive' : 'negative'}`}>
                  {fmt(a.acctCurrBal)}
                </td>
                <td className="money">{fmt(a.acctCreditLimit)}</td>
                <td>{a.acctOpenDate || '-'}</td>
                <td className="actions">
                  <Link to={`/accounts/${a.acctId}`} className="btn btn-sm btn-primary">View</Link>
                  <Link to={`/accounts/${a.acctId}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                </td>
              </tr>
            ))}
            {accounts.length === 0 && (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: 20 }}>No accounts found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
