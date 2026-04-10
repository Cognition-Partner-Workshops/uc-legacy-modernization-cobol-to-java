import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { customerApi } from '../../api/client';
import type { Customer } from '../../types';

export default function CustomerUpdate() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<Partial<Customer>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!id) return;
    customerApi.get(Number(id))
      .then(res => { setForm(res.data); setLoading(false); })
      .catch(() => { setError('Customer not found'); setLoading(false); });
  }, [id]);

  const handleChange = (field: keyof Customer, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await customerApi.update(Number(id), form);
      setSuccess('Customer updated successfully');
      setTimeout(() => navigate(`/customers/${id}`), 1000);
    } catch {
      setError('Failed to update customer');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Edit Customer #{id}</h1>
        <Link to={`/customers/${id}`} className="btn btn-secondary">Cancel</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      <div className="card">
        <form onSubmit={handleSubmit}>
          <h3 style={{ marginBottom: 16 }}>Personal Information</h3>
          <div className="form-row-3">
            <div className="form-group">
              <label>First Name</label>
              <input value={form.custFirstName || ''} onChange={e => handleChange('custFirstName', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Middle Name</label>
              <input value={form.custMiddleName || ''} onChange={e => handleChange('custMiddleName', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input value={form.custLastName || ''} onChange={e => handleChange('custLastName', e.target.value)} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Date of Birth</label>
              <input value={form.custDobYyyyMmDd || ''} onChange={e => handleChange('custDobYyyyMmDd', e.target.value)} placeholder="YYYY-MM-DD" />
            </div>
            <div className="form-group">
              <label>SSN</label>
              <input value={form.custSsn || ''} onChange={e => handleChange('custSsn', e.target.value)} maxLength={9} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Government ID</label>
              <input value={form.custGovtIssuedId || ''} onChange={e => handleChange('custGovtIssuedId', e.target.value)} />
            </div>
            <div className="form-group">
              <label>FICO Score</label>
              <input type="number" value={form.custFicoCreditScore ?? ''} onChange={e => handleChange('custFicoCreditScore', e.target.value)} />
            </div>
          </div>

          <h3 style={{ margin: '24px 0 16px' }}>Address</h3>
          <div className="form-group">
            <label>Address Line 1</label>
            <input value={form.custAddrLine1 || ''} onChange={e => handleChange('custAddrLine1', e.target.value)} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Address Line 2</label>
              <input value={form.custAddrLine2 || ''} onChange={e => handleChange('custAddrLine2', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Address Line 3</label>
              <input value={form.custAddrLine3 || ''} onChange={e => handleChange('custAddrLine3', e.target.value)} />
            </div>
          </div>
          <div className="form-row-3">
            <div className="form-group">
              <label>State</label>
              <input value={form.custAddrStateCd || ''} onChange={e => handleChange('custAddrStateCd', e.target.value)} maxLength={2} />
            </div>
            <div className="form-group">
              <label>Country</label>
              <input value={form.custAddrCountryCd || ''} onChange={e => handleChange('custAddrCountryCd', e.target.value)} maxLength={3} />
            </div>
            <div className="form-group">
              <label>ZIP</label>
              <input value={form.custAddrZip || ''} onChange={e => handleChange('custAddrZip', e.target.value)} maxLength={10} />
            </div>
          </div>

          <h3 style={{ margin: '24px 0 16px' }}>Contact</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Phone 1</label>
              <input value={form.custPhoneNum1 || ''} onChange={e => handleChange('custPhoneNum1', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Phone 2</label>
              <input value={form.custPhoneNum2 || ''} onChange={e => handleChange('custPhoneNum2', e.target.value)} />
            </div>
          </div>

          <div style={{ marginTop: 24, display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
            <Link to={`/customers/${id}`} className="btn btn-secondary">Cancel</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
