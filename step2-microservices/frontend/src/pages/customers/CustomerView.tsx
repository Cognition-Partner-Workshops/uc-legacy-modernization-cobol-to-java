import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { customerApi } from '../../api/client';
import type { Customer } from '../../types';

export default function CustomerView() {
  const { id } = useParams<{ id: string }>();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    customerApi.get(Number(id))
      .then(res => setCustomer(res.data))
      .catch(() => setError('Customer not found'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="loading">Loading customer...</div>;
  if (error) return <div className="alert alert-error">{error}</div>;
  if (!customer) return null;

  const fullName = [customer.custFirstName, customer.custMiddleName, customer.custLastName].filter(Boolean).join(' ');

  return (
    <div>
      <div className="page-header">
        <h1>Customer: {fullName || `#${customer.custId}`}</h1>
        <div style={{ display: 'flex', gap: 8 }}>
          <Link to={`/customers/${customer.custId}/edit`} className="btn btn-primary">Edit</Link>
          <Link to={`/customers/${customer.custId}/delete`} className="btn btn-danger">Delete</Link>
          <Link to="/customers" className="btn btn-secondary">Back</Link>
        </div>
      </div>
      <div className="card">
        <h3 style={{ marginBottom: 16 }}>Personal Information</h3>
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Customer ID</div>
            <div className="detail-value">{customer.custId}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">First Name</div>
            <div className="detail-value">{customer.custFirstName || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Middle Name</div>
            <div className="detail-value">{customer.custMiddleName || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Last Name</div>
            <div className="detail-value">{customer.custLastName || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Date of Birth</div>
            <div className="detail-value">{customer.custDobYyyyMmDd || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">SSN</div>
            <div className="detail-value">{customer.custSsn || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Government ID</div>
            <div className="detail-value">{customer.custGovtIssuedId || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">FICO Score</div>
            <div className="detail-value">{customer.custFicoCreditScore ?? '-'}</div>
          </div>
        </div>
      </div>
      <div className="card" style={{ marginTop: 16 }}>
        <h3 style={{ marginBottom: 16 }}>Address & Contact</h3>
        <div className="detail-grid">
          <div className="detail-item">
            <div className="detail-label">Address Line 1</div>
            <div className="detail-value">{customer.custAddrLine1 || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Address Line 2</div>
            <div className="detail-value">{customer.custAddrLine2 || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Address Line 3</div>
            <div className="detail-value">{customer.custAddrLine3 || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">State</div>
            <div className="detail-value">{customer.custAddrStateCd || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Country</div>
            <div className="detail-value">{customer.custAddrCountryCd || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">ZIP</div>
            <div className="detail-value">{customer.custAddrZip || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Phone 1</div>
            <div className="detail-value">{customer.custPhoneNum1 || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Phone 2</div>
            <div className="detail-value">{customer.custPhoneNum2 || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">EFT Account</div>
            <div className="detail-value">{customer.custEftAccountId || '-'}</div>
          </div>
          <div className="detail-item">
            <div className="detail-label">Primary Card Holder</div>
            <div className="detail-value">{customer.custPriCardHolderInd === 'Y' ? 'Yes' : 'No'}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
