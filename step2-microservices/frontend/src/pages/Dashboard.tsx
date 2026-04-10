import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { accountApi, cardApi, customerApi, transactionApi } from '../api/client';

export default function Dashboard() {
  const [stats, setStats] = useState({ accounts: 0, cards: 0, customers: 0, transactions: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [accts, cards, custs, txns] = await Promise.all([
          accountApi.list(),
          cardApi.list(),
          customerApi.list(),
          transactionApi.list(),
        ]);
        setStats({
          accounts: accts.data.length,
          cards: cards.data.length,
          customers: custs.data.length,
          transactions: txns.data.length,
        });
      } catch {
        // Services may not all be running
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Dashboard</h1>
      </div>
      <div className="stats-row">
        <Link to="/accounts" style={{ textDecoration: 'none' }}>
          <div className="stat-card">
            <div className="stat-value">{stats.accounts}</div>
            <div className="stat-label">Accounts</div>
          </div>
        </Link>
        <Link to="/cards" style={{ textDecoration: 'none' }}>
          <div className="stat-card">
            <div className="stat-value">{stats.cards}</div>
            <div className="stat-label">Cards</div>
          </div>
        </Link>
        <Link to="/customers" style={{ textDecoration: 'none' }}>
          <div className="stat-card">
            <div className="stat-value">{stats.customers}</div>
            <div className="stat-label">Customers</div>
          </div>
        </Link>
        <Link to="/transactions" style={{ textDecoration: 'none' }}>
          <div className="stat-card">
            <div className="stat-value">{stats.transactions}</div>
            <div className="stat-label">Transactions</div>
          </div>
        </Link>
      </div>
      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Quick Actions</h3>
        <div style={{ display: 'flex', gap: 8 }}>
          <Link to="/accounts" className="btn btn-primary">View Accounts</Link>
          <Link to="/cards" className="btn btn-secondary">View Cards</Link>
          <Link to="/customers" className="btn btn-secondary">View Customers</Link>
          <Link to="/transactions/new" className="btn btn-success">New Transaction</Link>
        </div>
      </div>
    </div>
  );
}
