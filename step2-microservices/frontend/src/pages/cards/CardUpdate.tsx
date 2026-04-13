import { useState, useEffect } from 'react';
import type { FormEvent } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { cardApi } from '../../api/client';
import type { Card } from '../../types';

export default function CardUpdate() {
  const { cardNum } = useParams<{ cardNum: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<Partial<Card>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!cardNum) return;
    cardApi.get(decodeURIComponent(cardNum))
      .then(res => { setForm(res.data); setLoading(false); })
      .catch(() => { setError('Card not found'); setLoading(false); });
  }, [cardNum]);

  const handleChange = (field: keyof Card, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!cardNum) return;
    setSaving(true);
    setError('');
    try {
      await cardApi.update(decodeURIComponent(cardNum), form);
      setSuccess('Card updated successfully');
      setTimeout(() => navigate(`/cards/${cardNum}`), 1000);
    } catch {
      setError('Failed to update card');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Edit Card</h1>
        <Link to={`/cards/${cardNum}`} className="btn btn-secondary">Cancel</Link>
      </div>
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}
      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Card Number</label>
            <input value={form.cardNum || ''} disabled />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Account ID</label>
              <input type="number" value={form.cardAcctId ?? ''} onChange={e => handleChange('cardAcctId', e.target.value)} />
            </div>
            <div className="form-group">
              <label>CVV Code</label>
              <input type="number" value={form.cardCvvCd ?? ''} onChange={e => handleChange('cardCvvCd', e.target.value)} />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Embossed Name</label>
              <input value={form.cardEmbossedName || ''} onChange={e => handleChange('cardEmbossedName', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Status</label>
              <select value={form.cardActiveStatus || ''} onChange={e => handleChange('cardActiveStatus', e.target.value)}>
                <option value="Y">Active</option>
                <option value="N">Inactive</option>
              </select>
            </div>
          </div>
          <div className="form-group">
            <label>Expiration Date</label>
            <input value={form.cardExpiraionDate || ''} onChange={e => handleChange('cardExpiraionDate', e.target.value)} placeholder="YYYY-MM-DD" />
          </div>
          <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Changes'}
            </button>
            <Link to={`/cards/${cardNum}`} className="btn btn-secondary">Cancel</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
