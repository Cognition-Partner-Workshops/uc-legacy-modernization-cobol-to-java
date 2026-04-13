import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { customerApi } from '../../api/client';
import type { Customer } from '../../types';

export default function CustomerDelete() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    customerApi.get(Number(id))
      .then(res => setCustomer(res.data))
      .catch(() => setError('Customer not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDelete = async () => {
    if (!id) return;
    setDeleting(true);
    setError('');
    try {
      await customerApi.delete(Number(id));
      navigate('/customers');
    } catch {
      setError('Failed to delete customer');
      setDeleting(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (error && !customer) return <div className="alert alert-error">{error}</div>;
  if (!customer) return null;

  const fullName = [customer.custFirstName, customer.custMiddleName, customer.custLastName].filter(Boolean).join(' ');

  return (
    <div>
      <div className="page-header">
        <h1>Delete Customer</h1>
        <Link to={`/customers/${id}`} className="btn btn-secondary">Cancel</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <div className="confirm-dialog" style={{ width: '100%', padding: 0, background: 'transparent' }}>
          <h3>Are you sure you want to delete this customer?</h3>
          <p>This action cannot be undone. The following customer will be permanently deleted:</p>
          <div className="detail-grid" style={{ marginBottom: 20 }}>
            <div className="detail-item">
              <div className="detail-label">Customer ID</div>
              <div className="detail-value">{customer.custId}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">Name</div>
              <div className="detail-value">{fullName || '-'}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">State</div>
              <div className="detail-value">{customer.custAddrStateCd || '-'}</div>
            </div>
            <div className="detail-item">
              <div className="detail-label">FICO Score</div>
              <div className="detail-value">{customer.custFicoCreditScore ?? '-'}</div>
            </div>
          </div>
          <div className="confirm-actions" style={{ justifyContent: 'flex-start' }}>
            <button onClick={handleDelete} className="btn btn-danger" disabled={deleting}>
              {deleting ? 'Deleting...' : 'Delete Customer'}
            </button>
            <Link to={`/customers/${id}`} className="btn btn-secondary">Cancel</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
