import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { customerApi } from '../../api/client';
import type { Customer } from '../../types';

export default function CustomerList() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    customerApi.list()
      .then(res => setCustomers(res.data))
      .catch(() => setError('Failed to load customers'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="loading">Loading customers...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Customers</h1>
        <span className="badge badge-active">{customers.length} total</span>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Customer ID</th>
              <th>Name</th>
              <th>State</th>
              <th>Phone</th>
              <th>FICO Score</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {customers.map(c => (
              <tr key={c.custId}>
                <td><strong>{c.custId}</strong></td>
                <td>{[c.custFirstName, c.custMiddleName, c.custLastName].filter(Boolean).join(' ') || '-'}</td>
                <td>{c.custAddrStateCd || '-'}</td>
                <td>{c.custPhoneNum1 || '-'}</td>
                <td>{c.custFicoCreditScore ?? '-'}</td>
                <td className="actions">
                  <Link to={`/customers/${c.custId}`} className="btn btn-sm btn-primary">View</Link>
                  <Link to={`/customers/${c.custId}/edit`} className="btn btn-sm btn-secondary">Edit</Link>
                  <Link to={`/customers/${c.custId}/delete`} className="btn btn-sm btn-danger">Delete</Link>
                </td>
              </tr>
            ))}
            {customers.length === 0 && (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: 20 }}>No customers found</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
