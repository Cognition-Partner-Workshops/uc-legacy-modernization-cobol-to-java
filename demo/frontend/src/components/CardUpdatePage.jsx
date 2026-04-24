import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getCard, updateCard } from '../api/cardApi';

const styles = {
  breadcrumb: { fontSize: '13px', color: '#999', marginBottom: '16px' },
  breadLink: { color: '#1a237e', textDecoration: 'none' },
  title: { fontSize: '24px', fontWeight: 700, color: '#1a237e', marginBottom: '4px' },
  subtitle: { color: '#666', fontSize: '14px', marginBottom: '24px' },
  legacy: {
    background: '#fff3e0',
    color: '#e65100',
    fontSize: '11px',
    padding: '2px 8px',
    borderRadius: '4px',
    fontWeight: 600,
  },
  card: {
    background: '#fff',
    border: '1px solid #e0e0e0',
    borderRadius: '8px',
    padding: '32px',
    maxWidth: '600px',
  },
  fieldGroup: { marginBottom: '20px' },
  label: {
    display: 'block',
    fontSize: '13px',
    fontWeight: 600,
    color: '#555',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    marginBottom: '6px',
  },
  input: {
    width: '100%',
    padding: '10px 12px',
    border: '1px solid #ccc',
    borderRadius: '4px',
    fontSize: '14px',
    boxSizing: 'border-box',
  },
  inputReadonly: {
    width: '100%',
    padding: '10px 12px',
    border: '1px solid #eee',
    borderRadius: '4px',
    fontSize: '14px',
    background: '#f5f5f5',
    color: '#999',
    boxSizing: 'border-box',
  },
  inputError: {
    width: '100%',
    padding: '10px 12px',
    border: '2px solid #c62828',
    borderRadius: '4px',
    fontSize: '14px',
    boxSizing: 'border-box',
  },
  errorText: { color: '#c62828', fontSize: '12px', marginTop: '4px' },
  actions: { marginTop: '24px', display: 'flex', gap: '12px' },
  btn: {
    padding: '10px 20px',
    background: '#1a237e',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  btnCancel: {
    padding: '10px 20px',
    background: '#e0e0e0',
    color: '#333',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
    textDecoration: 'none',
  },
  success: {
    background: '#e8f5e9',
    color: '#2e7d32',
    padding: '12px 16px',
    borderRadius: '4px',
    marginBottom: '16px',
    fontSize: '14px',
  },
  serverError: {
    background: '#fce4ec',
    color: '#c62828',
    padding: '12px 16px',
    borderRadius: '4px',
    marginBottom: '16px',
    fontSize: '14px',
  },
  loading: { textAlign: 'center', padding: '40px', color: '#999' },
};

function CardUpdatePage() {
  const { cardNumber } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    accountId: '',
    cardNumber: '',
    embossedName: '',
    activeStatus: '',
    expirationDate: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(false);
  const [serverError, setServerError] = useState(null);

  useEffect(() => {
    const fetchCard = async () => {
      try {
        const res = await getCard(cardNumber);
        setForm(res.data);
      } catch (err) {
        setServerError('Card not found or backend unavailable.');
      } finally {
        setLoading(false);
      }
    };
    fetchCard();
  }, [cardNumber]);

  const validate = () => {
    const errs = {};
    if (!form.embossedName || form.embossedName.trim() === '') {
      errs.embossedName = 'Name on card is required';
    }
    if (form.activeStatus !== 'Y' && form.activeStatus !== 'N') {
      errs.activeStatus = 'Active status must be Y or N';
    }
    if (!form.expirationDate || !/^\d{4}-\d{2}-\d{2}$/.test(form.expirationDate)) {
      errs.expirationDate = 'Expiry date must be in YYYY-MM-DD format';
    } else {
      const month = parseInt(form.expirationDate.split('-')[1], 10);
      const year = parseInt(form.expirationDate.split('-')[0], 10);
      if (month < 1 || month > 12) errs.expirationDate = 'Month must be 01-12';
      if (year < 1950 || year > 2099) errs.expirationDate = 'Year must be 1950-2099';
    }
    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    setServerError(null);
    setSuccess(false);
    try {
      await updateCard(cardNumber, form);
      setSuccess(true);
      setTimeout(() => navigate(`/cards/${cardNumber}`), 1200);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Update failed';
      setServerError(typeof msg === 'string' ? msg : JSON.stringify(msg));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div style={styles.loading}>Loading card...</div>;

  return (
    <div>
      <div style={styles.breadcrumb}>
        <Link to="/" style={styles.breadLink}>Cards</Link> &rsaquo;{' '}
        <Link to={`/cards/${cardNumber}`} style={styles.breadLink}>Detail</Link> &rsaquo; Edit
      </div>
      <div style={styles.title}>Update Card</div>
      <div style={styles.subtitle}>
        Replaces <span style={styles.legacy}>COCRDUP.bms / COCRDUPC.cbl</span>
      </div>

      {success && <div style={styles.success}>Card updated successfully. Redirecting...</div>}
      {serverError && <div style={styles.serverError}>{serverError}</div>}

      <div style={styles.card}>
        <div style={styles.fieldGroup}>
          <label style={styles.label}>Account Number (read-only)</label>
          <input style={styles.inputReadonly} value={form.accountId} readOnly />
        </div>
        <div style={styles.fieldGroup}>
          <label style={styles.label}>Card Number (read-only)</label>
          <input style={styles.inputReadonly} value={form.cardNumber} readOnly />
        </div>
        <div style={styles.fieldGroup}>
          <label style={styles.label}>Name on Card</label>
          <input
            style={errors.embossedName ? styles.inputError : styles.input}
            value={form.embossedName}
            onChange={(e) => setForm({ ...form, embossedName: e.target.value })}
            maxLength={50}
          />
          {errors.embossedName && <div style={styles.errorText}>{errors.embossedName}</div>}
        </div>
        <div style={styles.fieldGroup}>
          <label style={styles.label}>Card Active (Y/N)</label>
          <input
            style={errors.activeStatus ? styles.inputError : styles.input}
            value={form.activeStatus}
            onChange={(e) => setForm({ ...form, activeStatus: e.target.value.toUpperCase() })}
            maxLength={1}
          />
          {errors.activeStatus && <div style={styles.errorText}>{errors.activeStatus}</div>}
        </div>
        <div style={styles.fieldGroup}>
          <label style={styles.label}>Expiry Date (YYYY-MM-DD)</label>
          <input
            style={errors.expirationDate ? styles.inputError : styles.input}
            value={form.expirationDate}
            onChange={(e) => setForm({ ...form, expirationDate: e.target.value })}
            placeholder="YYYY-MM-DD"
          />
          {errors.expirationDate && <div style={styles.errorText}>{errors.expirationDate}</div>}
        </div>

        <div style={styles.actions}>
          <button style={styles.btn} onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
          <Link to={`/cards/${cardNumber}`} style={styles.btnCancel}>Cancel</Link>
        </div>
      </div>
    </div>
  );
}

export default CardUpdatePage;
