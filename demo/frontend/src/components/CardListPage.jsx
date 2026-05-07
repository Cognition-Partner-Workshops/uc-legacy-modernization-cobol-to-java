import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { getCardsPaged } from '../api/cardApi';

const styles = {
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '24px',
  },
  title: { fontSize: '24px', fontWeight: 700, color: '#1a237e' },
  subtitle: { color: '#666', fontSize: '14px', marginTop: '4px' },
  searchBox: {
    display: 'flex',
    gap: '8px',
    alignItems: 'center',
  },
  input: {
    padding: '8px 12px',
    border: '1px solid #ccc',
    borderRadius: '4px',
    fontSize: '14px',
    width: '180px',
  },
  btn: {
    padding: '8px 16px',
    background: '#1a237e',
    color: '#fff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  btnSecondary: {
    padding: '8px 16px',
    background: '#e0e0e0',
    color: '#333',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  btnDisabled: {
    padding: '8px 16px',
    background: '#f0f0f0',
    color: '#aaa',
    border: 'none',
    borderRadius: '4px',
    cursor: 'not-allowed',
    fontSize: '14px',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    marginTop: '16px',
  },
  th: {
    textAlign: 'left',
    padding: '12px 16px',
    background: '#f5f5f5',
    borderBottom: '2px solid #e0e0e0',
    fontSize: '13px',
    fontWeight: 600,
    color: '#555',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  td: {
    padding: '12px 16px',
    borderBottom: '1px solid #eee',
    fontSize: '14px',
  },
  link: {
    color: '#1a237e',
    textDecoration: 'none',
    fontWeight: 500,
  },
  statusActive: {
    background: '#e8f5e9',
    color: '#2e7d32',
    padding: '2px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 600,
  },
  statusInactive: {
    background: '#fce4ec',
    color: '#c62828',
    padding: '2px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 600,
  },
  legacy: {
    background: '#fff3e0',
    color: '#e65100',
    fontSize: '11px',
    padding: '2px 8px',
    borderRadius: '4px',
    fontWeight: 600,
  },
  loading: { textAlign: 'center', padding: '40px', color: '#999' },
  error: { textAlign: 'center', padding: '40px', color: '#c62828' },
  info: {
    background: '#e3f2fd',
    color: '#1565c0',
    padding: '10px 16px',
    borderRadius: '4px',
    fontSize: '13px',
    marginBottom: '12px',
  },
  pagination: {
    display: 'flex',
    gap: '8px',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: '20px',
    fontSize: '14px',
    color: '#555',
  },
};

// Equivalent to COCRDLIC.cbl 7-row page display
const PAGE_SIZE = 7;

function CardListPage() {
  const [cards, setCards] = useState([]);
  const [accountFilter, setAccountFilter] = useState('');
  const [searchValue, setSearchValue] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [infoMessage, setInfoMessage] = useState(null);
  // Pagination state (equivalent to PF7/PF8 in COCRDLIC.cbl)
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchCards = useCallback(async (acctId, page = 0) => {
    setLoading(true);
    setError(null);
    setInfoMessage(null);
    try {
      const res = await getCardsPaged(acctId || undefined, page, PAGE_SIZE);
      const data = res.data;
      setCards(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
      setCurrentPage(data.page);

      // Equivalent to informational messages in COCRDLIC.cbl
      if (data.content.length === 0 && acctId) {
        setInfoMessage(`No cards found for account ${acctId}. Try a different account number.`);
      } else if (data.content.length === 0) {
        setInfoMessage('No cards found in the system.');
      } else if (acctId) {
        setInfoMessage(`Showing ${data.totalElements} card(s) for account ${acctId}.`);
      }
    } catch (err) {
      const msg = err.response?.data?.message;
      setError(msg || 'Failed to load cards. Is the backend running on port 8080?');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCards();
  }, [fetchCards]);

  const handleSearch = () => {
    // Equivalent to 2210-EDIT-ACCOUNT in COCRDSLC.cbl
    // Account ID is PIC 9(11) — zero-pad the input to match COBOL behavior.
    const padded = searchValue.trim() ? searchValue.trim().padStart(11, '0') : '';
    setAccountFilter(padded);
    setCurrentPage(0);
    fetchCards(padded, 0);
  };

  const handleClear = () => {
    setSearchValue('');
    setAccountFilter('');
    setCurrentPage(0);
    fetchCards(undefined, 0);
  };

  // Equivalent to PF7 (page back) in COCRDLIC.cbl
  const handlePrevPage = () => {
    if (currentPage > 0) {
      const newPage = currentPage - 1;
      setCurrentPage(newPage);
      fetchCards(accountFilter || undefined, newPage);
    }
  };

  // Equivalent to PF8 (page forward) in COCRDLIC.cbl
  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      const newPage = currentPage + 1;
      setCurrentPage(newPage);
      fetchCards(accountFilter || undefined, newPage);
    }
  };

  return (
    <div>
      <div style={styles.header}>
        <div>
          <div style={styles.title}>Credit Cards</div>
          <div style={styles.subtitle}>
            Replaces <span style={styles.legacy}>COCRDLI.bms / COCRDLIC.cbl</span>
          </div>
        </div>
        <div style={styles.searchBox}>
          <input
            style={styles.input}
            placeholder="Account Number"
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
          <button style={styles.btn} onClick={handleSearch}>Search</button>
          {accountFilter && (
            <button style={styles.btnSecondary} onClick={handleClear}>Clear</button>
          )}
        </div>
      </div>

      {infoMessage && <div style={styles.info}>{infoMessage}</div>}
      {loading && <div style={styles.loading}>Loading cards...</div>}
      {error && <div style={styles.error}>{error}</div>}
      {!loading && !error && (
        <>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Account Number</th>
                <th style={styles.th}>Card Number</th>
                <th style={styles.th}>Cardholder Name</th>
                <th style={styles.th}>Status</th>
                <th style={styles.th}>Expiry</th>
                <th style={styles.th}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {cards.length === 0 ? (
                <tr>
                  <td style={styles.td} colSpan="6">No cards found.</td>
                </tr>
              ) : (
                cards.map((card) => (
                  <tr key={card.cardNumber}>
                    <td style={styles.td}>{card.accountId}</td>
                    <td style={styles.td}>
                      <Link to={`/cards/${card.cardNumber}`} style={styles.link}>
                        {card.cardNumber}
                      </Link>
                    </td>
                    <td style={styles.td}>{card.embossedName}</td>
                    <td style={styles.td}>
                      <span style={card.activeStatus === 'Y' ? styles.statusActive : styles.statusInactive}>
                        {card.activeStatus === 'Y' ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td style={styles.td}>{card.expirationDate}</td>
                    <td style={styles.td}>
                      <Link to={`/cards/${card.cardNumber}`} style={styles.link}>View</Link>
                      {' | '}
                      <Link to={`/cards/${card.cardNumber}/edit`} style={styles.link}>Edit</Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>

          {/* Pagination — equivalent to PF7/PF8 in COCRDLIC.cbl (7-row pages) */}
          {totalPages > 1 && (
            <div style={styles.pagination}>
              <button
                style={currentPage > 0 ? styles.btnSecondary : styles.btnDisabled}
                onClick={handlePrevPage}
                disabled={currentPage === 0}
              >
                PF7 Prev
              </button>
              <span>Page {currentPage + 1} of {totalPages} ({totalElements} total)</span>
              <button
                style={currentPage < totalPages - 1 ? styles.btnSecondary : styles.btnDisabled}
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1}
              >
                PF8 Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default CardListPage;
