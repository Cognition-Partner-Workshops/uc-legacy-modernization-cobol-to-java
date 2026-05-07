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
  hint: { color: '#888', fontSize: '11px', marginTop: '2px' },
  confirmOverlay: {
    position: 'fixed',
    top: 0, left: 0, right: 0, bottom: 0,
    background: 'rgba(0,0,0,0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  confirmBox: {
    background: '#fff',
    borderRadius: '8px',
    padding: '32px',
    maxWidth: '420px',
    width: '90%',
    boxShadow: '0 4px 24px rgba(0,0,0,0.2)',
  },
  confirmTitle: {
    fontSize: '18px',
    fontWeight: 700,
    color: '#1a237e',
    marginBottom: '12px',
  },
  confirmText: { fontSize: '14px', color: '#555', marginBottom: '20px' },
  confirmActions: { display: 'flex', gap: '12px', justifyContent: 'flex-end' },
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
  // Equivalent to 2000-DECIDE-ACTION PF5 confirmation in COCRDUPC.cbl
  const [showConfirm, setShowConfirm] = useState(false);

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

  // Validation — equivalent to 1230-EDIT-NAME, 1240-EDIT-CARDSTATUS,
  // 1250-EDIT-EXPIRY-MON, 1260-EDIT-EXPIRY-YEAR in COCRDUPC.cbl
  const validate = () => {
    const errs = {};

    // Equivalent to 1230-EDIT-NAME in COCRDUPC.cbl — name is required
    if (!form.embossedName || form.embossedName.trim() === '') {
      errs.embossedName = 'Name on card is required';
    } else if (!/^[A-Za-z ]+$/.test(form.embossedName.trim())) {
      // Equivalent to INSPECT CONVERTING in 1230-EDIT-NAME — alpha + space only
      errs.embossedName = 'Name must contain only letters and spaces';
    }

    // Equivalent to 1240-EDIT-CARDSTATUS — FLG-YES-NO-VALID VALUES 'Y', 'N'
    if (form.activeStatus !== 'Y' && form.activeStatus !== 'N') {
      errs.activeStatus = 'Active status must be Y or N';
    }

    // Equivalent to 1250-EDIT-EXPIRY-MON + 1260-EDIT-EXPIRY-YEAR in COCRDUPC.cbl
    if (!form.expirationDate || !/^\d{4}-\d{2}-\d{2}$/.test(form.expirationDate)) {
      errs.expirationDate = 'Expiry date must be in YYYY-MM-DD format';
    } else {
      const parts = form.expirationDate.split('-');
      const year = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10);
      const day = parseInt(parts[2], 10);
      if (year < 1950 || year > 2099) errs.expirationDate = 'Year must be 1950-2099';
      else if (month < 1 || month > 12) errs.expirationDate = 'Month must be 01-12';
      // Equivalent to EXPDAY field (DRK,PROT) in COCRDUP.bms — day must be "01"
      else if (day !== 1) errs.expirationDate = 'Day must be 01 (first of month per COBOL convention)';
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  // Step 1: Validate, then show confirmation dialog (equivalent to PF5 in COCRDUPC.cbl)
  const handleSaveClick = () => {
    if (!validate()) return;
    setShowConfirm(true);
  };

  // Step 2: User confirms — proceed with actual save
  const handleConfirmedSave = async () => {
    setShowConfirm(false);
    setSaving(true);
    setServerError(null);
    setSuccess(false);
    try {
      await updateCard(cardNumber, form);
      setSuccess(true);
      setTimeout(() => navigate(`/cards/${cardNumber}`), 1200);
    } catch (err) {
      const data = err.response?.data;
      const msg = data?.message || data || 'Update failed';
      if (err.response?.status === 409) {
        setServerError('Record was modified by another user. Please refresh and try again.');
      } else {
        setServerError(typeof msg === 'string' ? msg : JSON.stringify(msg));
      }
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
          <div style={styles.hint}>Letters and spaces only (per COBOL INSPECT CONVERTING rule)</div>
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
          <label style={styles.label}>Expiry Date (YYYY-MM-01)</label>
          <input
            style={errors.expirationDate ? styles.inputError : styles.input}
            value={form.expirationDate}
            onChange={(e) => setForm({ ...form, expirationDate: e.target.value })}
            placeholder="YYYY-MM-01"
          />
          <div style={styles.hint}>Day must be 01 (COBOL EXPDAY field is dark/protected)</div>
          {errors.expirationDate && <div style={styles.errorText}>{errors.expirationDate}</div>}
        </div>

        <div style={styles.actions}>
          <button style={styles.btn} onClick={handleSaveClick} disabled={saving}>
            {saving ? 'Saving...' : 'Save Changes'}
          </button>
          <Link to={`/cards/${cardNumber}`} style={styles.btnCancel}>Cancel</Link>
        </div>
      </div>

      {/* Equivalent to 2000-DECIDE-ACTION PF5 confirmation in COCRDUPC.cbl */}
      {showConfirm && (
        <div style={styles.confirmOverlay}>
          <div style={styles.confirmBox}>
            <div style={styles.confirmTitle}>Confirm Update</div>
            <div style={styles.confirmText}>
              Are you sure you want to update card <strong>{cardNumber}</strong>?
              This is equivalent to pressing PF5 in the COBOL CICS application.
            </div>
            <div style={styles.confirmActions}>
              <button
                style={styles.btnCancel}
                onClick={() => setShowConfirm(false)}
              >
                Cancel
              </button>
              <button style={styles.btn} onClick={handleConfirmedSave}>
                Confirm Update
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default CardUpdatePage;
