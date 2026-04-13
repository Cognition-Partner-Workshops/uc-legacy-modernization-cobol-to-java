import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { accountApi } from '../../api/client';
import type { Account } from '../../types';

export default function AccountUpdate() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<Partial<Account>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!id) return;
    accountApi.get(Number(id))
      .then(res => { setForm(res.data); setLoading(false); })
      .catch(() => { setError('Account not found'); setLoading(false); });
  }, [id]);

  const handleChange = (field: keyof Account, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await accountApi.update(Number(id), form);
      setSuccess('Account updated successfully');
      setTimeout(() => navigate(`/accounts/${id}`), 1000);
    } catch {
      setError('Failed to update account');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Edit Account #{id}</h1>
        <Link to={`/accounts/${id}`} className="btn btn-secondary">Cancel</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Status</label>
              <select value={form.acctActiveStatus || ''} onChange={e => handleChange('acctActiveStatus', e.target.value)}>
                <option value="Y">Active</option>
                <option value="N">Inactive</option>
              </select>
            </div>
            <div className="form-group">
              <label>Group ID</label>
              <input value={form.acctGroupId || ''} onChange={e => handleChange('acctGroupId', e.target.value)} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Current Balance</label>
              <input type="number" step="0.01" value={form.acctCurrBal ?? ''} onChange={e => handleChange('acctCurrBal', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Credit Limit</label>
              <input type="number" step="0.01" value={form.acctCreditLimit ?? ''} onChange={e => handleChange('acctCreditLimit', e.target.value)} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Cash Credit Limit</label>
              <input type="number" step="0.01" value={form.acctCashCreditLimit ?? ''} onChange={e => handleChange('acctCashCreditLimit', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Expiration Date</label>
              <input value={form.acctExpiraionDate || ''} onChange={e => handleChange('acctExpiraionDate', e.target.value)} placeholder="YYYY-MM-DD" />
            </div>
          </div>
          <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
            <Link to={`/accounts/${id}`} className="btn btn-secondary">Cancel</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
