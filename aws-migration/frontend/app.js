const API_BASE_URL = window.CARDDEMO_API_URL || '';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function formatCurrency(val) {
  const n = parseFloat(val) || 0;
  return '$' + n.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(d) {
  if (!d) return '-';
  const dt = new Date(d);
  if (isNaN(dt)) return d;
  return dt.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatTimestamp(ts) {
  if (!ts) return '-';
  const dt = new Date(ts);
  if (isNaN(dt)) return ts;
  return dt.toLocaleString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function maskCard(num) {
  if (!num || num.length < 4) return num || '-';
  return '****-****-****-' + num.slice(-4);
}

function statusBadge(status) {
  if (!status) return '';
  var s = String(status).toUpperCase().trim();
  if (s === 'Y' || s === 'ACTIVE') return '<span class="badge badge-active">Active</span>';
  if (s === 'N' || s === 'INACTIVE') return '<span class="badge badge-inactive">Inactive</span>';
  return '<span class="badge badge-pending">' + escapeHtml(status) + '</span>';
}

function escapeHtml(str) {
  if (str == null) return '';
  var div = document.createElement('div');
  div.textContent = String(str);
  return div.innerHTML;
}

function spinnerHtml() {
  return '<div class="spinner"></div>';
}

function emptyHtml(msg) {
  return '<div class="empty-state"><div class="icon">&#x1F4ED;</div><p>' + escapeHtml(msg) + '</p></div>';
}

// ---------------------------------------------------------------------------
// Toast notifications
// ---------------------------------------------------------------------------

function showToast(message, type) {
  var container = document.getElementById('toast-container');
  var toast = document.createElement('div');
  toast.className = 'toast toast-' + (type || 'success');
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(function () {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity .3s';
    setTimeout(function () { toast.remove(); }, 300);
  }, 3500);
}

// ---------------------------------------------------------------------------
// Modal helpers
// ---------------------------------------------------------------------------

function openModal(id) {
  document.getElementById(id).classList.add('open');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

// Close modals when clicking overlay
document.addEventListener('click', function (e) {
  if (e.target.classList.contains('modal-overlay') && e.target.classList.contains('open')) {
    e.target.classList.remove('open');
  }
});

// ---------------------------------------------------------------------------
// API
// ---------------------------------------------------------------------------

function api(method, path, body) {
  var opts = {
    method: method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (body) opts.body = JSON.stringify(body);
  return fetch(API_BASE_URL + path, opts)
    .then(function (r) {
      if (!r.ok) {
        return r.json().catch(function () { return {}; }).then(function (errJson) {
          var msg = (errJson && (errJson.error || errJson.message)) || ('HTTP error ' + r.status);
          throw new Error(msg);
        });
      }
      return r.json();
    })
    .then(function (json) {
      var payload = json.body || json;
      if (payload.error) throw new Error(payload.error);
      return payload;
    });
}

// ---------------------------------------------------------------------------
// Pagination state
// ---------------------------------------------------------------------------

var pageState = {
  accounts: { keys: [null], idx: 0 },
  customers: { keys: [null], idx: 0 },
  cards: { keys: [null], idx: 0 },
  transactions: { keys: [null], idx: 0 }
};

function resetPage(section) {
  pageState[section] = { keys: [null], idx: 0 };
}

// ---------------------------------------------------------------------------
// Navigation
// ---------------------------------------------------------------------------

var currentSection = 'dashboard';

document.querySelectorAll('.nav-item').forEach(function (btn) {
  btn.addEventListener('click', function () {
    navigate(btn.dataset.section);
  });
});

function navigate(section) {
  currentSection = section;
  document.querySelectorAll('.nav-item').forEach(function (b) { b.classList.remove('active'); });
  var active = document.querySelector('[data-section="' + section + '"]');
  if (active) active.classList.add('active');
  var titles = {
    dashboard: 'Dashboard',
    accounts: 'Accounts',
    customers: 'Customers',
    cards: 'Cards',
    transactions: 'Transactions',
    batch: 'Batch Processing',
    system: 'System Info'
  };
  document.getElementById('section-title').textContent = titles[section] || section;
  document.getElementById('header-actions').innerHTML = '';
  renderSection(section);
}

function renderSection(section) {
  var content = document.getElementById('content');
  content.innerHTML = spinnerHtml();
  switch (section) {
    case 'dashboard': renderDashboard(); break;
    case 'accounts': renderAccounts(); break;
    case 'customers': renderCustomers(); break;
    case 'cards': renderCards(); break;
    case 'transactions': renderTransactions(); break;
    case 'batch': renderBatch(); break;
    case 'system': renderSystem(); break;
    default: content.innerHTML = emptyHtml('Unknown section');
  }
}

// ---------------------------------------------------------------------------
// Dashboard
// ---------------------------------------------------------------------------

function renderDashboard() {
  var content = document.getElementById('content');
  content.innerHTML = spinnerHtml();

  api('GET', '/dashboard')
    .then(function (res) {
      var d = res.data || res;
      content.innerHTML =
        '<div class="stats-grid">' +
          statCard('&#x1F4C1;', 'accounts', d.total_accounts, 'Total Accounts') +
          statCard('&#x1F465;', 'customers', d.total_customers, 'Total Customers') +
          statCard('&#x1F4B3;', 'cards', d.total_cards, 'Total Cards') +
          statCard('&#x1F4B8;', 'transactions', d.total_transactions, 'Total Transactions') +
        '</div>' +
        '<div class="panel">' +
          '<div class="panel-header"><h3>Recent Transactions</h3></div>' +
          '<div class="panel-body" id="recent-tx">' + spinnerHtml() + '</div>' +
        '</div>';

      loadRecentTransactions();
    })
    .catch(function (err) {
      content.innerHTML =
        '<div class="stats-grid">' +
          statCard('&#x1F4C1;', 'accounts', '-', 'Total Accounts') +
          statCard('&#x1F465;', 'customers', '-', 'Total Customers') +
          statCard('&#x1F4B3;', 'cards', '-', 'Total Cards') +
          statCard('&#x1F4B8;', 'transactions', '-', 'Total Transactions') +
        '</div>' +
        '<div class="panel">' +
          '<div class="panel-header"><h3>Recent Transactions</h3></div>' +
          '<div class="panel-body">' +
            '<div class="error-msg">Could not load dashboard: ' + escapeHtml(err.message) + '</div>' +
          '</div>' +
        '</div>';
    });
}

function statCard(icon, cls, value, label) {
  return '<div class="stat-card">' +
    '<div class="stat-icon ' + cls + '">' + icon + '</div>' +
    '<div class="stat-info"><h3>' + escapeHtml(String(value != null ? value : '-')) + '</h3><p>' + escapeHtml(label) + '</p></div>' +
  '</div>';
}

function loadRecentTransactions() {
  api('GET', '/transactions')
    .then(function (res) {
      var items = (res.data || []).slice(0, 10);
      var el = document.getElementById('recent-tx');
      if (!el) return;
      if (items.length === 0) {
        el.innerHTML = emptyHtml('No transactions yet');
        return;
      }
      el.innerHTML = '<div class="table-wrapper"><table>' +
        '<thead><tr><th>ID</th><th>Type</th><th>Amount</th><th>Merchant</th><th>Date</th></tr></thead>' +
        '<tbody>' + items.map(function (t) {
          return '<tr>' +
            '<td>' + escapeHtml(t.tran_id) + '</td>' +
            '<td>' + escapeHtml(t.tran_type_cd) + '</td>' +
            '<td>' + formatCurrency(t.tran_amt) + '</td>' +
            '<td>' + escapeHtml(t.merchant_name || '-') + '</td>' +
            '<td>' + formatTimestamp(t.tran_orig_ts) + '</td>' +
          '</tr>';
        }).join('') + '</tbody></table></div>';
    })
    .catch(function () {
      var el = document.getElementById('recent-tx');
      if (el) el.innerHTML = emptyHtml('Unable to load recent transactions');
    });
}

// ---------------------------------------------------------------------------
// Accounts
// ---------------------------------------------------------------------------

function renderAccounts() {
  var content = document.getElementById('content');
  var ps = pageState.accounts;
  var lastKey = ps.keys[ps.idx];
  var url = '/accounts' + (lastKey ? '?last_key=' + encodeURIComponent(lastKey) : '');

  api('GET', url)
    .then(function (res) {
      var items = res.data || [];
      if (res.last_key && ps.keys.indexOf(res.last_key) === -1) {
        ps.keys.push(res.last_key);
      }
      if (items.length === 0) {
        content.innerHTML = emptyHtml('No accounts found');
        return;
      }
      content.innerHTML =
        '<div class="panel"><div class="table-wrapper"><table>' +
        '<thead><tr><th>Account ID</th><th>Status</th><th>Balance</th><th>Credit Limit</th><th>Open Date</th></tr></thead>' +
        '<tbody>' + items.map(function (a) {
          return '<tr onclick="showAccountDetail(\'' + escapeHtml(a.acct_id) + '\')">' +
            '<td>' + escapeHtml(a.acct_id) + '</td>' +
            '<td>' + statusBadge(a.active_status) + '</td>' +
            '<td>' + formatCurrency(a.curr_bal) + '</td>' +
            '<td>' + formatCurrency(a.credit_limit) + '</td>' +
            '<td>' + formatDate(a.open_date) + '</td>' +
          '</tr>';
        }).join('') + '</tbody></table></div>' +
        paginationHtml('accounts', ps) +
        '</div>';
    })
    .catch(function (err) {
      content.innerHTML = '<div class="error-msg">Failed to load accounts: ' + escapeHtml(err.message) + '</div>';
    });
}

function showAccountDetail(acctId) {
  var body = document.getElementById('account-modal-body');
  var footer = document.getElementById('account-modal-footer');
  body.innerHTML = spinnerHtml();
  footer.innerHTML = '';
  document.getElementById('account-modal-title').textContent = 'Account ' + acctId;
  openModal('account-modal');

  api('GET', '/accounts/' + acctId)
    .then(function (res) {
      var a = res.data || res;
      body.innerHTML =
        '<dl class="detail-grid">' +
          detailRow('Account ID', a.acct_id) +
          detailRow('Status', a.active_status === 'Y' ? 'Active' : 'Inactive') +
          detailRow('Group ID', a.group_id) +
          detailRow('Open Date', formatDate(a.open_date)) +
          detailRow('Expiration', formatDate(a.expiration_date)) +
        '</dl>' +
        '<hr style="margin:16px 0;border:none;border-top:1px solid var(--border)">' +
        '<h4 style="margin-bottom:12px;font-size:15px">Edit Financial Details</h4>' +
        '<div class="form-row">' +
          '<div class="form-group"><label>Current Balance</label><input class="form-control" id="edit-balance" value="' + escapeHtml(a.curr_bal || '') + '"></div>' +
          '<div class="form-group"><label>Credit Limit</label><input class="form-control" id="edit-credit-limit" value="' + escapeHtml(a.credit_limit || '') + '"></div>' +
        '</div>';
      footer.innerHTML =
        '<button class="btn btn-secondary" onclick="closeModal(\'account-modal\')">Cancel</button>' +
        '<button class="btn btn-primary" onclick="saveAccount(\'' + escapeHtml(acctId) + '\')">Save Changes</button>';
    })
    .catch(function (err) {
      body.innerHTML = '<div class="error-msg">' + escapeHtml(err.message) + '</div>';
      footer.innerHTML = '<button class="btn btn-secondary" onclick="closeModal(\'account-modal\')">Close</button>';
    });
}

function saveAccount(acctId) {
  var bal = document.getElementById('edit-balance').value;
  var limit = document.getElementById('edit-credit-limit').value;
  api('PUT', '/accounts/' + acctId, { curr_bal: bal, credit_limit: limit })
    .then(function () {
      showToast('Account updated successfully', 'success');
      closeModal('account-modal');
      renderAccounts();
    })
    .catch(function (err) {
      showToast('Update failed: ' + err.message, 'error');
    });
}

// ---------------------------------------------------------------------------
// Customers
// ---------------------------------------------------------------------------

function renderCustomers() {
  var content = document.getElementById('content');
  var ps = pageState.customers;
  var lastKey = ps.keys[ps.idx];
  var url = '/customers' + (lastKey ? '?last_key=' + encodeURIComponent(lastKey) : '');

  api('GET', url)
    .then(function (res) {
      var items = res.data || [];
      if (res.last_key && ps.keys.indexOf(res.last_key) === -1) {
        ps.keys.push(res.last_key);
      }
      if (items.length === 0) {
        content.innerHTML = emptyHtml('No customers found');
        return;
      }
      content.innerHTML =
        '<div class="panel"><div class="table-wrapper"><table>' +
        '<thead><tr><th>Customer ID</th><th>Name</th><th>State</th><th>Phone</th><th>FICO Score</th></tr></thead>' +
        '<tbody>' + items.map(function (c) {
          var name = [c.first_name, c.middle_name, c.last_name].filter(Boolean).join(' ');
          return '<tr onclick="showCustomerDetail(\'' + escapeHtml(c.cust_id) + '\')">' +
            '<td>' + escapeHtml(c.cust_id) + '</td>' +
            '<td>' + escapeHtml(name) + '</td>' +
            '<td>' + escapeHtml(c.state_cd || '-') + '</td>' +
            '<td>' + escapeHtml(c.phone_1 || '-') + '</td>' +
            '<td>' + escapeHtml(c.fico_score || '-') + '</td>' +
          '</tr>';
        }).join('') + '</tbody></table></div>' +
        paginationHtml('customers', ps) +
        '</div>';
    })
    .catch(function (err) {
      content.innerHTML = '<div class="error-msg">Failed to load customers: ' + escapeHtml(err.message) + '</div>';
    });
}

function showCustomerDetail(custId) {
  var body = document.getElementById('customer-modal-body');
  body.innerHTML = spinnerHtml();
  document.getElementById('customer-modal-title').textContent = 'Customer ' + custId;
  openModal('customer-modal');

  api('GET', '/customers/' + custId)
    .then(function (res) {
      var c = res.data || res;
      var name = [c.first_name, c.middle_name, c.last_name].filter(Boolean).join(' ');
      body.innerHTML =
        '<dl class="detail-grid">' +
          detailRow('Customer ID', c.cust_id) +
          detailRow('Name', name) +
          detailRow('SSN', c.ssn ? '***-**-' + String(c.ssn).slice(-4) : '-') +
          detailRow('Date of Birth', formatDate(c.dob)) +
          detailRow('Address', [c.addr_line_1, c.addr_line_2, c.addr_line_3].filter(Boolean).join(', ')) +
          detailRow('State', c.state_cd) +
          detailRow('Country', c.country_cd) +
          detailRow('ZIP', c.zip) +
          detailRow('Phone 1', c.phone_1) +
          detailRow('Phone 2', c.phone_2) +
          detailRow('FICO Score', c.fico_score) +
          detailRow('Govt ID', c.govt_issued_id) +
          detailRow('EFT Account', c.eft_account_id) +
          detailRow('Primary Holder', c.pri_card_holder_ind === 'Y' ? 'Yes' : 'No') +
        '</dl>';
    })
    .catch(function (err) {
      body.innerHTML = '<div class="error-msg">' + escapeHtml(err.message) + '</div>';
    });
}

// ---------------------------------------------------------------------------
// Cards
// ---------------------------------------------------------------------------

var cardsFilter = { acct_id: '' };

function renderCards() {
  var content = document.getElementById('content');
  var actions = document.getElementById('header-actions');
  actions.innerHTML =
    '<div class="filter-bar">' +
      '<input class="form-control" id="cards-acct-filter" placeholder="Filter by Account ID" value="' + escapeHtml(cardsFilter.acct_id) + '">' +
      '<button class="btn btn-primary" onclick="applyCardsFilter()">Filter</button>' +
      '<button class="btn btn-secondary" onclick="clearCardsFilter()">Clear</button>' +
    '</div>';

  var ps = pageState.cards;
  var lastKey = ps.keys[ps.idx];
  var params = [];
  if (cardsFilter.acct_id) params.push('acct_id=' + encodeURIComponent(cardsFilter.acct_id));
  if (lastKey) params.push('last_key=' + encodeURIComponent(lastKey));
  var url = '/cards' + (params.length ? '?' + params.join('&') : '');

  api('GET', url)
    .then(function (res) {
      var items = res.data || [];
      if (res.last_key && ps.keys.indexOf(res.last_key) === -1) {
        ps.keys.push(res.last_key);
      }
      if (items.length === 0) {
        content.innerHTML = emptyHtml('No cards found');
        return;
      }
      content.innerHTML =
        '<div class="panel"><div class="table-wrapper"><table>' +
        '<thead><tr><th>Card Number</th><th>Account ID</th><th>Status</th><th>Expiry</th></tr></thead>' +
        '<tbody>' + items.map(function (c) {
          return '<tr>' +
            '<td>' + escapeHtml(maskCard(c.card_num)) + '</td>' +
            '<td>' + escapeHtml(c.acct_id || '-') + '</td>' +
            '<td>' + statusBadge(c.card_active_status) + '</td>' +
            '<td>' + formatDate(c.expiry_date) + '</td>' +
          '</tr>';
        }).join('') + '</tbody></table></div>' +
        paginationHtml('cards', ps) +
        '</div>';
    })
    .catch(function (err) {
      content.innerHTML = '<div class="error-msg">Failed to load cards: ' + escapeHtml(err.message) + '</div>';
    });
}

function applyCardsFilter() {
  cardsFilter.acct_id = document.getElementById('cards-acct-filter').value.trim();
  resetPage('cards');
  renderCards();
}

function clearCardsFilter() {
  cardsFilter.acct_id = '';
  resetPage('cards');
  renderCards();
}

// ---------------------------------------------------------------------------
// Transactions
// ---------------------------------------------------------------------------

var txFilter = { acct_id: '', card_num: '' };

function renderTransactions() {
  var content = document.getElementById('content');
  var actions = document.getElementById('header-actions');
  actions.innerHTML =
    '<div class="filter-bar">' +
      '<input class="form-control" id="tx-acct-filter" placeholder="Account ID" value="' + escapeHtml(txFilter.acct_id) + '">' +
      '<input class="form-control" id="tx-card-filter" placeholder="Card Number" value="' + escapeHtml(txFilter.card_num) + '">' +
      '<button class="btn btn-primary" onclick="applyTxFilter()">Filter</button>' +
      '<button class="btn btn-secondary" onclick="clearTxFilter()">Clear</button>' +
      '<button class="btn btn-primary" onclick="openNewTransaction()">+ New Transaction</button>' +
    '</div>';

  var ps = pageState.transactions;
  var lastKey = ps.keys[ps.idx];
  var params = [];
  if (txFilter.acct_id) params.push('acct_id=' + encodeURIComponent(txFilter.acct_id));
  if (txFilter.card_num) params.push('card_num=' + encodeURIComponent(txFilter.card_num));
  if (lastKey) params.push('last_key=' + encodeURIComponent(lastKey));
  var url = '/transactions' + (params.length ? '?' + params.join('&') : '');

  api('GET', url)
    .then(function (res) {
      var items = res.data || [];
      if (res.last_key && ps.keys.indexOf(res.last_key) === -1) {
        ps.keys.push(res.last_key);
      }
      if (items.length === 0) {
        content.innerHTML = emptyHtml('No transactions found');
        return;
      }
      content.innerHTML =
        '<div class="panel"><div class="table-wrapper"><table>' +
        '<thead><tr><th>ID</th><th>Type</th><th>Amount</th><th>Merchant</th><th>Date</th></tr></thead>' +
        '<tbody>' + items.map(function (t) {
          return '<tr>' +
            '<td>' + escapeHtml(t.tran_id) + '</td>' +
            '<td>' + escapeHtml(t.tran_type_cd || '-') + '</td>' +
            '<td>' + formatCurrency(t.tran_amt) + '</td>' +
            '<td>' + escapeHtml(t.merchant_name || '-') + '</td>' +
            '<td>' + formatTimestamp(t.tran_orig_ts) + '</td>' +
          '</tr>';
        }).join('') + '</tbody></table></div>' +
        paginationHtml('transactions', ps) +
        '</div>';
    })
    .catch(function (err) {
      content.innerHTML = '<div class="error-msg">Failed to load transactions: ' + escapeHtml(err.message) + '</div>';
    });
}

function applyTxFilter() {
  txFilter.acct_id = document.getElementById('tx-acct-filter').value.trim();
  txFilter.card_num = document.getElementById('tx-card-filter').value.trim();
  resetPage('transactions');
  renderTransactions();
}

function clearTxFilter() {
  txFilter.acct_id = '';
  txFilter.card_num = '';
  resetPage('transactions');
  renderTransactions();
}

// ---------------------------------------------------------------------------
// New Transaction Modal
// ---------------------------------------------------------------------------

function openNewTransaction() {
  var body = document.getElementById('transaction-modal-body');
  body.innerHTML = spinnerHtml();
  openModal('transaction-modal');

  Promise.all([
    api('GET', '/cards'),
    api('GET', '/transaction-types'),
    api('GET', '/transaction-categories')
  ]).then(function (results) {
    var cards = results[0].data || [];
    var types = results[1].data || [];
    var categories = results[2].data || [];

    body.innerHTML =
      '<div class="form-group"><label>Card Number</label>' +
        '<select class="form-control" id="new-tx-card">' +
          '<option value="">Select a card</option>' +
          cards.map(function (c) { return '<option value="' + escapeHtml(c.card_num) + '">' + escapeHtml(maskCard(c.card_num)) + ' (Acct: ' + escapeHtml(c.acct_id) + ')</option>'; }).join('') +
        '</select></div>' +
      '<div class="form-row">' +
        '<div class="form-group"><label>Transaction Type</label>' +
          '<select class="form-control" id="new-tx-type">' +
            '<option value="">Select type</option>' +
            types.map(function (t) { return '<option value="' + escapeHtml(t.type_cd) + '">' + escapeHtml(t.type_cd + ' - ' + (t.type_desc || '')) + '</option>'; }).join('') +
          '</select></div>' +
        '<div class="form-group"><label>Category</label>' +
          '<select class="form-control" id="new-tx-cat">' +
            '<option value="">Select category</option>' +
            categories.map(function (c) { return '<option value="' + escapeHtml(c.cat_cd) + '">' + escapeHtml(c.cat_cd + ' - ' + (c.cat_desc || '')) + '</option>'; }).join('') +
          '</select></div>' +
      '</div>' +
      '<div class="form-group"><label>Amount ($)</label><input class="form-control" id="new-tx-amount" type="number" step="0.01" min="0" placeholder="0.00"></div>' +
      '<div class="form-group"><label>Merchant Name</label><input class="form-control" id="new-tx-merchant" placeholder="e.g. Amazon"></div>' +
      '<div class="form-group"><label>Description</label><input class="form-control" id="new-tx-desc" placeholder="e.g. Online purchase"></div>';

    document.getElementById('submit-transaction-btn').onclick = submitTransaction;
  }).catch(function (err) {
    body.innerHTML = '<div class="error-msg">Failed to load form data: ' + escapeHtml(err.message) + '</div>';
  });
}

function submitTransaction() {
  var cardNum = document.getElementById('new-tx-card').value;
  var typeCd = document.getElementById('new-tx-type').value;
  var catCd = document.getElementById('new-tx-cat').value;
  var amount = document.getElementById('new-tx-amount').value;
  var merchant = document.getElementById('new-tx-merchant').value;
  var desc = document.getElementById('new-tx-desc').value;

  if (!cardNum || !typeCd || !amount) {
    showToast('Please fill in required fields (Card, Type, Amount)', 'error');
    return;
  }

  var payload = {
    card_num: cardNum,
    tran_type_cd: typeCd,
    tran_cat_cd: catCd,
    tran_amt: amount,
    merchant_name: merchant,
    tran_desc: desc
  };

  api('POST', '/transactions', payload)
    .then(function () {
      showToast('Transaction created successfully', 'success');
      closeModal('transaction-modal');
      if (currentSection === 'transactions') {
        resetPage('transactions');
        renderTransactions();
      }
    })
    .catch(function (err) {
      showToast('Failed to create transaction: ' + err.message, 'error');
    });
}

// ---------------------------------------------------------------------------
// Batch Processing
// ---------------------------------------------------------------------------

function renderBatch() {
  var content = document.getElementById('content');
  content.innerHTML =
    '<div class="panel">' +
      '<div class="panel-body batch-panel" id="batch-content">' +
        '<div style="font-size:48px;margin-bottom:16px">&#x2699;</div>' +
        '<h3 style="margin-bottom:8px">Daily Batch Processing</h3>' +
        '<p>This operation ports the legacy <strong>CBTRN01C</strong> COBOL batch program. ' +
           'It processes all pending transactions for the day, updates account balances based on ' +
           'transaction amounts, recalculates credit utilization, and flags any accounts that exceed ' +
           'their credit limits. Transactions are grouped by account and processed sequentially to ' +
           'maintain consistency.</p>' +
        '<button class="btn btn-primary btn-lg" onclick="runBatch()">&#x25B6; Run Daily Batch</button>' +
      '</div>' +
    '</div>';
}

function runBatch() {
  var el = document.getElementById('batch-content');
  el.innerHTML = spinnerHtml() + '<p style="margin-top:16px;color:var(--text-secondary)">Processing transactions...</p>';

  api('POST', '/batch/process-daily')
    .then(function (res) {
      var d = res.data || res;
      el.innerHTML =
        '<div style="font-size:48px;margin-bottom:16px">&#x2705;</div>' +
        '<h3 style="margin-bottom:20px">Batch Complete</h3>' +
        '<div class="batch-results">' +
          '<div class="batch-result-card" style="background:var(--success-bg)">' +
            '<h4 style="color:var(--success)">' + escapeHtml(String(d.processed != null ? d.processed : 0)) + '</h4>' +
            '<p style="color:var(--success)">Processed</p></div>' +
          '<div class="batch-result-card" style="background:var(--warning-bg)">' +
            '<h4 style="color:var(--warning)">' + escapeHtml(String(d.skipped != null ? d.skipped : 0)) + '</h4>' +
            '<p style="color:var(--warning)">Skipped</p></div>' +
          '<div class="batch-result-card" style="background:var(--danger-bg)">' +
            '<h4 style="color:var(--danger)">' + escapeHtml(String(d.errors != null ? d.errors : 0)) + '</h4>' +
            '<p style="color:var(--danger)">Errors</p></div>' +
        '</div>' +
        '<button class="btn btn-secondary" style="margin-top:24px" onclick="renderBatch()">Run Again</button>';
    })
    .catch(function (err) {
      el.innerHTML =
        '<div class="error-msg">Batch processing failed: ' + escapeHtml(err.message) + '</div>' +
        '<button class="btn btn-primary btn-lg" onclick="runBatch()" style="margin-top:16px">&#x25B6; Retry</button>';
    });
}

// ---------------------------------------------------------------------------
// System Info
// ---------------------------------------------------------------------------

function renderSystem() {
  var content = document.getElementById('content');
  content.innerHTML =
    '<div class="panel">' +
      '<div class="panel-header"><h3>COBOL-to-AWS Migration Mapping</h3></div>' +
      '<div class="panel-body migration-table"><div class="table-wrapper"><table>' +
        '<thead><tr><th>COBOL Component</th><th>Program</th><th>AWS Service</th><th>Implementation</th></tr></thead>' +
        '<tbody>' +
          migrationRow('Online Programs', 'COSGN00C, COMEN01C, COADM01C', 'API Gateway + Lambda', 'REST API endpoints') +
          migrationRow('Account Management', 'COACTVWC, COACTUPC', 'Lambda + DynamoDB', 'accounts CRUD') +
          migrationRow('Customer Management', 'COCRDSLC, COCRDUPC', 'Lambda + DynamoDB', 'customers CRUD') +
          migrationRow('Card Management', 'COCRDLIC, COCRDUPC', 'Lambda + DynamoDB', 'cards CRUD + xref') +
          migrationRow('Transaction Processing', 'COTRN00C, COTRN01C, COTRN02C', 'Lambda + DynamoDB', 'transactions CRUD') +
          migrationRow('Batch Processing', 'CBTRN01C, CBTRN02C, CBTRN03C', 'Lambda (scheduled)', 'Daily batch trigger') +
          migrationRow('VSAM Data Files', 'KSDS/AIX clusters', 'DynamoDB Tables', '7 tables with GSIs') +
          migrationRow('BMS Screens', '3270 terminal maps', 'S3 Static Website', 'This web frontend') +
          migrationRow('JCL Jobs', 'DEFVSAM, REPVSAM', 'Terraform IaC', 'Infrastructure as Code') +
          migrationRow('CICS Runtime', 'Transaction server', 'API Gateway', 'HTTP request routing') +
          migrationRow('Copybooks', 'CVACT01Y, CVCUS01Y, etc.', 'Lambda models', 'DynamoDB schemas') +
        '</tbody>' +
      '</table></div></div>' +
    '</div>' +
    '<div class="panel">' +
      '<div class="panel-header"><h3>Architecture Overview</h3></div>' +
      '<div class="panel-body"><div class="architecture-block">' +
        '<h4>AWS Serverless Architecture</h4>' +
        '<ul>' +
          '<li><strong>Amazon API Gateway</strong> &mdash; RESTful API replacing CICS transaction routing</li>' +
          '<li><strong>AWS Lambda (Python)</strong> &mdash; Serverless functions replacing COBOL online &amp; batch programs</li>' +
          '<li><strong>Amazon DynamoDB</strong> &mdash; NoSQL database replacing VSAM KSDS/AIX data files</li>' +
          '<li><strong>Amazon S3</strong> &mdash; Static website hosting replacing BMS 3270 terminal screens</li>' +
          '<li><strong>Amazon CloudWatch</strong> &mdash; Monitoring &amp; scheduling replacing JES/Control-M</li>' +
          '<li><strong>AWS IAM</strong> &mdash; Security &amp; access control replacing RACF/ACF2</li>' +
          '<li><strong>Terraform</strong> &mdash; Infrastructure as Code replacing JCL IDCAMS definitions</li>' +
        '</ul>' +
      '</div></div>' +
    '</div>' +
    '<div class="panel">' +
      '<div class="panel-header"><h3>Source COBOL Programs</h3></div>' +
      '<div class="panel-body"><div class="table-wrapper"><table>' +
        '<thead><tr><th>Program</th><th>Description</th><th>Type</th></tr></thead>' +
        '<tbody>' +
          sourceRow('COSGN00C', 'Sign-on / Authentication', 'Online') +
          sourceRow('COMEN01C', 'Main Menu Navigation', 'Online') +
          sourceRow('COADM01C', 'Admin Menu', 'Online') +
          sourceRow('COACTVWC', 'Account View', 'Online') +
          sourceRow('COACTUPC', 'Account Update', 'Online') +
          sourceRow('COCRDSLC', 'Card List / Search', 'Online') +
          sourceRow('COCRDUPC', 'Card Update', 'Online') +
          sourceRow('COTRN00C', 'Transaction List', 'Online') +
          sourceRow('COTRN01C', 'Transaction Add', 'Online') +
          sourceRow('COTRN02C', 'Transaction Detail', 'Online') +
          sourceRow('CBTRN01C', 'Daily Transaction Batch', 'Batch') +
          sourceRow('CBTRN02C', 'Transaction Parse', 'Batch') +
          sourceRow('CBTRN03C', 'Transaction Post', 'Batch') +
          sourceRow('CBACT01C', 'Account File Batch', 'Batch') +
          sourceRow('CBCUS01C', 'Customer File Batch', 'Batch') +
          sourceRow('CBSTM03A', 'Statement Generation', 'Batch') +
        '</tbody>' +
      '</table></div></div>' +
    '</div>';
}

function migrationRow(component, program, aws, impl) {
  return '<tr><td><strong>' + escapeHtml(component) + '</strong></td><td>' +
    escapeHtml(program) + '</td><td>' + escapeHtml(aws) + '</td><td>' +
    escapeHtml(impl) + '</td></tr>';
}

function sourceRow(program, desc, type) {
  return '<tr><td><code>' + escapeHtml(program) + '</code></td><td>' +
    escapeHtml(desc) + '</td><td>' +
    (type === 'Batch' ? '<span class="badge badge-pending">' : '<span class="badge badge-active">') +
    escapeHtml(type) + '</span></td></tr>';
}

// ---------------------------------------------------------------------------
// Pagination helpers
// ---------------------------------------------------------------------------

function paginationHtml(section, ps) {
  var hasPrev = ps.idx > 0;
  var hasNext = ps.idx < ps.keys.length - 1;
  return '<div class="pagination">' +
    '<button class="btn btn-secondary" ' + (hasPrev ? 'onclick="paginate(\'' + section + '\', -1)"' : 'disabled') + '>&larr; Previous</button>' +
    '<span style="color:var(--text-muted);font-size:13px">Page ' + (ps.idx + 1) + '</span>' +
    '<button class="btn btn-secondary" ' + (hasNext ? 'onclick="paginate(\'' + section + '\', 1)"' : 'disabled') + '>Next &rarr;</button>' +
  '</div>';
}

function paginate(section, dir) {
  pageState[section].idx += dir;
  renderSection(section);
}

// ---------------------------------------------------------------------------
// Detail row helper
// ---------------------------------------------------------------------------

function detailRow(label, value) {
  return '<dt>' + escapeHtml(label) + '</dt><dd>' + escapeHtml(value != null ? String(value) : '-') + '</dd>';
}

// ---------------------------------------------------------------------------
// Init
// ---------------------------------------------------------------------------

navigate('dashboard');
