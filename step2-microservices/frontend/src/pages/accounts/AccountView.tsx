import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { accountApi } from '../../api/client';
import type { Account } from '../../types';

export default function AccountView() {
  const { id } = useParams<{ id: string }>();
  const [account, setAccount] = useState<Account | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    accountApi.get(Number(id))
      .then(res => setAccount(res.data))
      .catch(() => setError('Account not found'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading">Loading account...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!account) return null;

  const fmt = (n: number | null) => '$' + (n ?? 0).toLocaleString('en-US', { minimumFractionDigits: 2 });

  return (
    <div>
      <div className="page-header">
        <h1>Account #{account.acctId}</h1>
        <div style={{ display: 'flex', gap: 8 }}>
          <Link to={`/accounts/${account.acctId}/edit`} className="btn btn-primary">Edit</Link>
          <Link to="/accounts" className="btn btn-secondary">Back</Link>
        </div>
      </div>
      <div className="card">
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Account ID</div>
            <div className="detail-value">{account.acctId}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Status</div>
            <div className="detail-value">
              <span className={`badge ${account.acctActiveStatus === 'Y' ? 'badge-active' : 'badge-inactive'}`}>
                {account.acctActiveStatus === 'Y' ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Current Balance</div>
            <div className={`detail-value money ${(account.acctCurrBal ?? 0) >= 0 ? 'positive' : 'negative'}`}>
              {fmt(account.acctCurrBal)}
            </div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Credit Limit</div>
            <div className="detail-value money">{fmt(account.acctCreditLimit)}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Cash Credit Limit</div>
            <div className="detail-value money">{fmt(account.acctCashCreditLimit)}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Group ID</div>
            <div className="detail-value">{account.acctGroupId || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Open Date</div>
            <div className="detail-value">{account.acctOpenDate || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Expiration Date</div>
            <div className="detail-value">{account.acctExpiraionDate || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Reissue Date</div>
            <div className="detail-value">{account.acctReissueDate || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Cycle Credit</div>
            <div className="detail-value money">{fmt(account.acctCurrCycCredit)}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Cycle Debit</div>
            <div className="detail-value money">{fmt(account.acctCurrCycDebit)}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
