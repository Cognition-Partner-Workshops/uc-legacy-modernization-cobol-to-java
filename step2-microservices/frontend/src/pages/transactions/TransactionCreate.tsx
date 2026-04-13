import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { transactionApi } from '../../api/client';
import type { Transaction } from '../../types';

export default function TransactionCreate() {
  const navigate = useNavigate();
  const [form, setForm] = useState<Partial<Transaction>>({
    tranTypeCd: '',
    tranCatCd: 0,
    tranSource: '',
    tranDesc: '',
    tranAmt: 0,
    tranCardNum: '',
    tranMerchantId: 0,
    tranMerchantName: '',
    tranMerchantCity: '',
    tranMerchantZip: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (field: keyof Transaction, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');

    if (!form.tranTypeCd) {
      setError('Transaction type is required');
      return;
    }
    if (!form.tranCardNum) {
      setError('Card number is required');
      return;
    }

    setSaving(true);
    try {
      const now = new Date().toISOString();
      const payload = {
        ...form,
        tranOrigTs: now,
        tranProcTs: now,
      };
      const res = await transactionApi.create(payload);
      navigate(`/transactions/${res.data.tranId}`);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { message?: string } } };
        setError(axiosErr.response?.data?.message || 'Failed to create transaction');
      } else {
        setError('Failed to create transaction');
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1>New Transaction</h1>
        <Link to="/transactions" className="btn btn-secondary">Cancel</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="card">
        <form onSubmit={handleSubmit}>
          <h3 style={{ marginBottom: 16 }}>Transaction Details</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Transaction ID</label>
              <input value={form.tranId || ''} onChange={e => handleChange('tranId', e.target.value)} placeholder="Auto-generated if empty" />
            </div>
            <div className="form-group">
              <label>Card Number *</label>
              <input value={form.tranCardNum || ''} onChange={e => handleChange('tranCardNum', e.target.value)} placeholder="e.g. 4111111111111111" required />
            </div>
          </div>
          <div className="form-row-3">
            <div className="form-group">
              <label>Type Code *</label>
              <input value={form.tranTypeCd || ''} onChange={e => handleChange('tranTypeCd', e.target.value)} placeholder="e.g. SA" maxLength={2} required />
            </div>
            <div className="form-group">
              <label>Category Code</label>
              <input type="number" value={form.tranCatCd ?? ''} onChange={e => handleChange('tranCatCd', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Amount *</label>
              <input type="number" step="0.01" value={form.tranAmt ?? ''} onChange={e => handleChange('tranAmt', e.target.value)} required />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Source</label>
              <input value={form.tranSource || ''} onChange={e => handleChange('tranSource', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Description</label>
              <input value={form.tranDesc || ''} onChange={e => handleChange('tranDesc', e.target.value)} />
            </div>
          </div>

          <h3 style={{ margin: '24px 0 16px' }}>Merchant Information</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Merchant ID</label>
              <input type="number" value={form.tranMerchantId ?? ''} onChange={e => handleChange('tranMerchantId', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Merchant Name</label>
              <input value={form.tranMerchantName || ''} onChange={e => handleChange('tranMerchantName', e.target.value)} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Merchant City</label>
              <input value={form.tranMerchantCity || ''} onChange={e => handleChange('tranMerchantCity', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Merchant ZIP</label>
              <input value={form.tranMerchantZip || ''} onChange={e => handleChange('tranMerchantZip', e.target.value)} />
            </div>
          </div>

          <div style={{ marginTop: 24, display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-success" disabled={saving}>
              {saving ? 'Creating...' : 'Create Transaction'}
            </button>
            <Link to="/transactions" className="btn btn-secondary">Cancel</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
