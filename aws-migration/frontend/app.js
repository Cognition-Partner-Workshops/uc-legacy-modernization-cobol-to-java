/* ===========================================================================
 * CardDemo SPA — vanilla JS, no build step.
 * Talks to the serverless API described in aws-migration/API_CONTRACT.md.
 * Base URL comes from window.CARDDEMO_API_URL (set in config.js at deploy time).
 * ========================================================================= */
"use strict";

const API = (window.CARDDEMO_API_URL || "").replace(/\/+$/, "");
const PAGE_LIMIT = 25;

/* ---------- tiny DOM helpers ---------- */
const $ = (sel, root = document) => root.querySelector(sel);
const el = (tag, attrs = {}, children = []) => {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (v == null) continue;
    if (k === "class") node.className = v;
    else if (k === "html") node.innerHTML = v;
    else if (k === "text") node.textContent = v;
    else if (k.startsWith("on") && typeof v === "function") node.addEventListener(k.slice(2), v);
    else if (k === "dataset") Object.assign(node.dataset, v);
    else node.setAttribute(k, v);
  }
  for (const c of [].concat(children)) {
    if (c == null) continue;
    node.append(c.nodeType ? c : document.createTextNode(String(c)));
  }
  return node;
};

/* ---------- formatting ---------- */
const esc = (s) => String(s == null ? "" : s).replace(/[&<>"']/g, (c) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

function money(v) {
  if (v == null || v === "") return "—";
  const n = Number(v);
  if (Number.isNaN(n)) return esc(v);
  return n.toLocaleString("en-US", { style: "currency", currency: "USD" });
}
function num(v) {
  if (v == null || v === "") return "—";
  const n = Number(v);
  return Number.isNaN(n) ? esc(v) : n.toLocaleString("en-US");
}
function maskCard(card) {
  const s = String(card || "");
  if (s.length <= 4) return esc(s);
  return "•".repeat(s.length - 4) + s.slice(-4);
}
function statusBadge(s) {
  const active = s === "Y" || s === "y";
  return el("span", { class: `badge ${active ? "green" : "red"}`, text: active ? "Active" : "Inactive" });
}
const dash = (v) => (v == null || v === "" ? "—" : esc(v));

/* ---------- API layer ---------- */
class ApiError extends Error {}

async function apiFetch(path, opts = {}) {
  if (!API) throw new ApiError("API URL not configured");
  const res = await fetch(API + path, {
    headers: { "Content-Type": "application/json" },
    ...opts,
  });
  let body = null;
  const txt = await res.text();
  if (txt) {
    try { body = JSON.parse(txt); } catch { body = txt; }
  }
  if (!res.ok) {
    const msg = body && body.error ? body.error : `Request failed (${res.status})`;
    throw new ApiError(msg);
  }
  return body;
}

// list endpoints return { data: [...], last_key: "..."|null }
function buildQuery(params) {
  const q = new URLSearchParams();
  for (const [k, v] of Object.entries(params || {})) {
    if (v != null && v !== "") q.set(k, v);
  }
  const s = q.toString();
  return s ? `?${s}` : "";
}

/* ---------- view shell helpers ---------- */
const view = () => $("#view");
function setTitle(title, subtitle = "") {
  $("#pageTitle").textContent = title;
  $("#pageSubtitle").textContent = subtitle;
}
function loadingState(msg = "Loading…") {
  return el("div", { class: "state" }, [el("div", { class: "spinner" }), el("div", { text: msg })]);
}
function errorState(msg, onRetry) {
  const node = el("div", { class: "state error" }, [
    el("div", { text: "⚠ " + msg }),
  ]);
  if (onRetry) node.append(el("div", { style: "margin-top:14px" },
    el("button", { class: "btn sm", onclick: onRetry, text: "Retry" })));
  return node;
}
function emptyState(msg = "No records found.") {
  return el("div", { class: "state", text: msg });
}

/* ---------- pager (cursor based via last_key) ---------- */
function makePager(state, onChange) {
  const info = el("span", { class: "page-info",
    text: `Page ${state.pageIndex + 1}${state.count != null ? ` · ${state.count} shown` : ""}` });
  const prev = el("button", { class: "btn sm", text: "‹ Prev",
    disabled: state.pageIndex === 0 ? "disabled" : null,
    onclick: () => onChange("prev") });
  const next = el("button", { class: "btn sm", text: "Next ›",
    disabled: state.lastKey ? null : "disabled",
    onclick: () => onChange("next") });
  return el("div", { class: "pager" }, [
    info,
    el("div", { class: "pager-btns" }, [prev, next]),
  ]);
}

/* =========================================================================
 * Generic paginated table section
 * ======================================================================= */
function paginatedSection(cfg) {
  // cfg: { path, columns, onRowClick, filters, emptyMsg }
  const root = view();
  const state = { pageIndex: 0, lastKey: null, cursors: [null], count: null, filters: {} };

  const body = el("div");
  const panelHead = el("div", { class: "panel-head" }, [el("h2", { text: cfg.title })]);
  if (cfg.toolsRender) panelHead.append(cfg.toolsRender(() => load(true)));
  const panel = el("div", { class: "panel" }, [panelHead, body]);

  root.replaceChildren(panel);

  async function load(resetCursor) {
    if (resetCursor) { state.pageIndex = 0; state.cursors = [null]; }
    body.replaceChildren(loadingState());
    const params = { limit: PAGE_LIMIT, ...cfg.getFilters() };
    const cursor = state.cursors[state.pageIndex];
    if (cursor) params.last_key = cursor;
    try {
      const res = await apiFetch(cfg.path + buildQuery(params));
      const rows = (res && res.data) || [];
      state.lastKey = (res && res.last_key) || null;
      state.count = rows.length;
      if (!rows.length) {
        body.replaceChildren(emptyState(cfg.emptyMsg));
        return;
      }
      renderTable(rows);
    } catch (e) {
      body.replaceChildren(errorState(e.message, () => load(false)));
    }
  }

  function renderTable(rows) {
    const thead = el("thead", {}, el("tr", {},
      cfg.columns.map((c) => el("th", { class: c.num ? "num" : null, text: c.label }))));
    const tbody = el("tbody", {}, rows.map((row) => {
      const tr = el("tr", cfg.onRowClick ? { class: "clickable",
        onclick: () => cfg.onRowClick(row) } : {},
        cfg.columns.map((c) => {
          const cell = c.render ? c.render(row) : dash(row[c.key]);
          const td = el("td", { class: [c.num ? "num" : null, c.mono ? "mono" : null].filter(Boolean).join(" ") || null });
          if (cell && cell.nodeType) td.append(cell); else td.innerHTML = cell;
          return td;
        }));
      return tr;
    }));
    const pager = makePager(state, (dir) => {
      if (dir === "next" && state.lastKey) {
        state.pageIndex += 1;
        state.cursors[state.pageIndex] = state.lastKey;
        load(false);
      } else if (dir === "prev" && state.pageIndex > 0) {
        state.pageIndex -= 1;
        load(false);
      }
    });
    body.replaceChildren(
      el("div", { class: "table-wrap" }, el("table", { class: "data" }, [thead, tbody])),
      pager,
    );
  }

  load(true);
  return { reload: () => load(true) };
}

/* =========================================================================
 * Modal
 * ======================================================================= */
function openModal(title, contentNode) {
  $("#modalTitle").textContent = title;
  $("#modalBody").replaceChildren(contentNode);
  $("#modalRoot").classList.remove("hidden");
}
function closeModal() { $("#modalRoot").classList.add("hidden"); }
document.addEventListener("click", (e) => {
  if (e.target.closest("[data-modal-close]")) closeModal();
});
document.addEventListener("keydown", (e) => { if (e.key === "Escape") closeModal(); });

function detailList(pairs) {
  const dl = el("dl", { class: "dl" });
  for (const [label, value] of pairs) {
    dl.append(el("dt", { text: label }));
    const dd = el("dd");
    if (value && value.nodeType) dd.append(value); else dd.innerHTML = value == null || value === "" ? "—" : esc(value);
    dl.append(dd);
  }
  return dl;
}

/* =========================================================================
 * Section: Dashboard
 * ======================================================================= */
async function renderDashboard() {
  setTitle("Dashboard", "Overview of the migrated CardDemo estate");
  const root = view();
  root.replaceChildren(loadingState("Loading dashboard…"));
  try {
    const res = await apiFetch("/dashboard");
    const d = (res && res.data) || {};
    const cards = el("div", { class: "stat-grid" }, [
      statCard("Accounts", num(d.accounts), "sky", "carddemo-accounts"),
      statCard("Customers", num(d.customers), "green", "carddemo-customers"),
      statCard("Cards", num(d.cards), "amber", "carddemo-cards"),
      statCard("Transactions", num(d.transactions), "sky", "carddemo-transactions"),
      statCard("Total Balance", money(d.total_balance), "green", "Σ curr_bal"),
    ]);

    const recent = (d.recent_transactions || []);
    const tbody = el("tbody", {}, recent.length ? recent.map((t) =>
      el("tr", {}, [
        el("td", { class: "mono", text: dash(t.tran_id) }),
        el("td", { text: dash(t.tran_desc) }),
        el("td", { class: "mono", html: maskCard(t.card_num) }),
        el("td", { class: "num", html: money(t.amt) }),
        el("td", { text: dash(t.tran_orig_ts) }),
      ])) : [el("tr", {}, el("td", { colspan: "5" },
        el("div", { class: "state", text: "No recent transactions." })))]);

    const recentPanel = el("div", { class: "panel" }, [
      el("div", { class: "panel-head" }, el("h2", { text: "Recent Transactions" })),
      el("div", { class: "table-wrap" }, el("table", { class: "data" }, [
        el("thead", {}, el("tr", {}, [
          el("th", { text: "Tran ID" }), el("th", { text: "Description" }),
          el("th", { text: "Card" }), el("th", { class: "num", text: "Amount" }),
          el("th", { text: "Timestamp" }),
        ])),
        tbody,
      ])),
    ]);

    root.replaceChildren(cards, recentPanel);
  } catch (e) {
    root.replaceChildren(errorState(e.message, renderDashboard));
  }
}
function statCard(label, value, dot, foot) {
  return el("div", { class: "stat-card" }, [
    el("div", { class: "stat-label" }, [el("span", { class: `stat-dot ${dot}` }), label]),
    el("div", { class: "stat-value", text: value }),
    el("div", { class: "stat-foot", text: foot || "" }),
  ]);
}

/* =========================================================================
 * Section: Accounts (with edit modal -> PUT)
 * ======================================================================= */
function renderAccounts() {
  setTitle("Accounts", "Account master — click a row to view & edit");
  paginatedSection({
    title: "Accounts",
    path: "/accounts",
    getFilters: () => ({}),
    emptyMsg: "No accounts found.",
    columns: [
      { label: "Account ID", key: "acct_id", mono: true },
      { label: "Status", render: (r) => statusBadge(r.acct_active_status) },
      { label: "Balance", num: true, render: (r) => money(r.curr_bal) },
      { label: "Credit Limit", num: true, render: (r) => money(r.credit_limit) },
      { label: "Cash Limit", num: true, render: (r) => money(r.cash_credit_limit) },
      { label: "ZIP", key: "addr_zip", mono: true },
      { label: "Open Date", key: "open_date" },
    ],
    onRowClick: openAccountEditor,
  });
}

function openAccountEditor(acct) {
  const id = acct.acct_id;
  const form = el("form", { class: "form-grid" });
  const fields = [
    { key: "acct_id", label: "Account ID", readonly: true },
    { key: "acct_active_status", label: "Active Status (Y/N)", editable: true },
    { key: "credit_limit", label: "Credit Limit", editable: true },
    { key: "cash_credit_limit", label: "Cash Credit Limit", editable: true },
    { key: "curr_bal", label: "Current Balance", readonly: true },
    { key: "addr_zip", label: "Address ZIP", editable: true },
    { key: "open_date", label: "Open Date", readonly: true },
    { key: "expiration_date", label: "Expiration Date", readonly: true },
    { key: "group_id", label: "Group ID", readonly: true },
  ];
  const inputs = {};
  for (const f of fields) {
    const input = el("input", {
      type: "text", value: acct[f.key] == null ? "" : acct[f.key],
      disabled: f.editable ? null : "disabled",
      name: f.key,
    });
    inputs[f.key] = input;
    form.append(el("div", { class: "field" }, [el("label", { text: f.label }), input]));
  }
  const msg = el("div", { class: "form-msg" });
  const saveBtn = el("button", { type: "submit", class: "btn primary", text: "Save changes" });
  const actions = el("div", { class: "modal-actions" }, [
    el("button", { type: "button", class: "btn", text: "Cancel", onclick: closeModal }),
    saveBtn,
  ]);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {};
    for (const f of fields) {
      if (!f.editable) continue;
      const v = inputs[f.key].value.trim();
      if (v !== (acct[f.key] == null ? "" : String(acct[f.key]))) payload[f.key] = v;
    }
    if (!Object.keys(payload).length) {
      msg.className = "form-msg"; msg.textContent = "No changes to save.";
      return;
    }
    saveBtn.disabled = true; msg.className = "form-msg"; msg.textContent = "Saving…";
    try {
      const res = await apiFetch(`/accounts/${encodeURIComponent(id)}`, {
        method: "PUT", body: JSON.stringify(payload),
      });
      msg.className = "form-msg ok"; msg.textContent = "Saved successfully.";
      const updated = (res && res.data) || { ...acct, ...payload };
      Object.assign(acct, updated);
      for (const f of fields) inputs[f.key].value = acct[f.key] == null ? "" : acct[f.key];
      setTimeout(() => { closeModal(); renderAccounts(); }, 700);
    } catch (err) {
      msg.className = "form-msg error"; msg.textContent = err.message;
      saveBtn.disabled = false;
    }
  });

  form.append(el("div", { style: "grid-column:1/-1" }, [msg, actions]));
  openModal(`Account ${esc(id)}`, form);
}

/* =========================================================================
 * Section: Customers (read-only detail)
 * ======================================================================= */
function renderCustomers() {
  setTitle("Customers", "Customer master — click a row for details");
  paginatedSection({
    title: "Customers",
    path: "/customers",
    getFilters: () => ({}),
    emptyMsg: "No customers found.",
    columns: [
      { label: "Customer ID", key: "cust_id", mono: true },
      { label: "Name", render: (r) => esc([r.first_name, r.middle_name, r.last_name]
          .filter((x) => x && String(x).trim()).join(" ")) || "—" },
      { label: "State", key: "addr_state_cd" },
      { label: "ZIP", key: "addr_zip", mono: true },
      { label: "Phone", key: "phone_num_1", mono: true },
      { label: "FICO", key: "fico_credit_score", num: true },
    ],
    onRowClick: openCustomerDetail,
  });
}
function openCustomerDetail(c) {
  const name = [c.first_name, c.middle_name, c.last_name].filter((x) => x && String(x).trim()).join(" ");
  const addr = [c.addr_line_1, c.addr_line_2, c.addr_line_3]
    .filter((x) => x && String(x).trim()).join(", ");
  openModal(`Customer ${esc(c.cust_id)}`, detailList([
    ["Customer ID", c.cust_id],
    ["Name", name],
    ["Address", addr],
    ["State", c.addr_state_cd],
    ["Country", c.addr_country_cd],
    ["ZIP", c.addr_zip],
    ["Phone 1", c.phone_num_1],
    ["Phone 2", c.phone_num_2],
    ["SSN", c.ssn],
    ["Govt ID", c.govt_issued_id],
    ["Date of Birth", c.dob],
    ["EFT Account", c.eft_account_id],
    ["Primary Card Holder", c.pri_card_holder_ind],
    ["FICO Score", c.fico_credit_score],
  ]));
}

/* =========================================================================
 * Section: Cards (masked, account filter)
 * ======================================================================= */
function renderCards() {
  setTitle("Cards", "Card master — filter by account; numbers are masked");
  let acctFilter = "";
  const section = paginatedSection({
    title: "Cards",
    path: "/cards",
    getFilters: () => (acctFilter ? { acct_id: acctFilter } : {}),
    emptyMsg: "No cards found.",
    toolsRender: (reload) => {
      const input = el("input", { class: "input-sm", placeholder: "Filter by Account ID", value: "" });
      const apply = () => { acctFilter = input.value.trim(); reload(); };
      input.addEventListener("keydown", (e) => { if (e.key === "Enter") apply(); });
      return el("div", { class: "panel-tools" }, [
        input,
        el("button", { class: "btn sm primary", text: "Filter", onclick: apply }),
        el("button", { class: "btn sm ghost", text: "Clear",
          onclick: () => { input.value = ""; acctFilter = ""; reload(); } }),
      ]);
    },
    columns: [
      { label: "Card Number", mono: true, render: (r) => maskCard(r.card_num) },
      { label: "Account ID", key: "acct_id", mono: true },
      { label: "Embossed Name", key: "embossed_name" },
      { label: "Expiration", key: "expiration_date" },
      { label: "Status", render: (r) => statusBadge(r.active_status) },
    ],
    onRowClick: openCardDetail,
  });
  return section;
}
function openCardDetail(c) {
  openModal("Card details", detailList([
    ["Card Number", maskCard(c.card_num)],
    ["Account ID", c.acct_id],
    ["Embossed Name", c.embossed_name],
    ["Expiration Date", c.expiration_date],
    ["CVV", c.cvv_cd ? "•••" : "—"],
    ["Status", statusBadge(c.active_status)],
  ]));
}

/* =========================================================================
 * Section: Transactions (filters + New Transaction modal -> POST)
 * ======================================================================= */
function renderTransactions() {
  setTitle("Transactions", "Transaction history — filter and create new entries");
  let filters = { acct_id: "", card_num: "" };
  const section = paginatedSection({
    title: "Transactions",
    path: "/transactions",
    getFilters: () => {
      const f = {};
      if (filters.acct_id) f.acct_id = filters.acct_id;
      else if (filters.card_num) f.card_num = filters.card_num;
      return f;
    },
    emptyMsg: "No transactions found.",
    toolsRender: (reload) => {
      const acct = el("input", { class: "input-sm", placeholder: "Account ID" });
      const card = el("input", { class: "input-sm", placeholder: "Card Number" });
      const apply = () => { filters = { acct_id: acct.value.trim(), card_num: card.value.trim() }; reload(); };
      [acct, card].forEach((i) => i.addEventListener("keydown", (e) => { if (e.key === "Enter") apply(); }));
      return el("div", { class: "panel-tools" }, [
        acct, card,
        el("button", { class: "btn sm primary", text: "Filter", onclick: apply }),
        el("button", { class: "btn sm ghost", text: "Clear",
          onclick: () => { acct.value = ""; card.value = ""; filters = { acct_id: "", card_num: "" }; reload(); } }),
        el("button", { class: "btn sm primary", text: "+ New Transaction",
          onclick: () => openNewTransaction(reload) }),
      ]);
    },
    columns: [
      { label: "Tran ID", key: "tran_id", mono: true },
      { label: "Type", key: "type_cd", mono: true },
      { label: "Cat", key: "cat_cd", mono: true },
      { label: "Description", key: "tran_desc" },
      { label: "Card", mono: true, render: (r) => maskCard(r.card_num) },
      { label: "Amount", num: true, render: (r) => money(r.amt) },
      { label: "Merchant", key: "merchant_name" },
      { label: "Timestamp", key: "tran_orig_ts" },
    ],
    onRowClick: openTransactionDetail,
  });
  return section;
}
function openTransactionDetail(t) {
  openModal(`Transaction ${esc(t.tran_id)}`, detailList([
    ["Tran ID", t.tran_id],
    ["Origin Timestamp", t.tran_orig_ts],
    ["Processed Timestamp", t.tran_proc_ts],
    ["Type Code", t.type_cd],
    ["Category Code", t.cat_cd],
    ["Source", t.tran_source],
    ["Description", t.tran_desc],
    ["Amount", money(t.amt)],
    ["Card Number", maskCard(t.card_num)],
    ["Account ID", t.acct_id],
    ["Merchant ID", t.merchant_id],
    ["Merchant Name", t.merchant_name],
    ["Merchant City", t.merchant_city],
    ["Merchant ZIP", t.merchant_zip],
    ["Batch Processed", t.batch_processed ? "Yes" : "No"],
  ]));
}
function openNewTransaction(reload) {
  const form = el("form", {});
  const grid = el("div", { class: "form-grid" });
  const spec = [
    { key: "card_num", label: "Card Number *", required: true },
    { key: "amt", label: "Amount *", placeholder: "12.34", required: true },
    { key: "type_cd", label: "Type Code *", placeholder: "01", required: true },
    { key: "cat_cd", label: "Category Code *", placeholder: "0001", required: true },
    { key: "tran_desc", label: "Description" },
    { key: "tran_source", label: "Source", placeholder: "POS" },
    { key: "merchant_id", label: "Merchant ID" },
    { key: "merchant_name", label: "Merchant Name" },
    { key: "merchant_city", label: "Merchant City" },
    { key: "merchant_zip", label: "Merchant ZIP" },
  ];
  const inputs = {};
  for (const s of spec) {
    const input = el("input", { type: "text", placeholder: s.placeholder || "", name: s.key });
    inputs[s.key] = input;
    grid.append(el("div", { class: "field" }, [el("label", { text: s.label }), input]));
  }
  const msg = el("div", { class: "form-msg" });
  const submit = el("button", { type: "submit", class: "btn primary", text: "Create transaction" });
  const actions = el("div", { class: "modal-actions" }, [
    el("button", { type: "button", class: "btn", text: "Cancel", onclick: closeModal }),
    submit,
  ]);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const payload = {};
    for (const s of spec) {
      const v = inputs[s.key].value.trim();
      if (v) payload[s.key] = v;
    }
    const missing = ["card_num", "amt", "type_cd", "cat_cd"].filter((k) => !payload[k]);
    if (missing.length) {
      msg.className = "form-msg error"; msg.textContent = "Required: " + missing.join(", ");
      return;
    }
    submit.disabled = true; msg.className = "form-msg"; msg.textContent = "Creating…";
    try {
      await apiFetch("/transactions", { method: "POST", body: JSON.stringify(payload) });
      msg.className = "form-msg ok"; msg.textContent = "Transaction created.";
      setTimeout(() => { closeModal(); if (reload) reload(); }, 700);
    } catch (err) {
      msg.className = "form-msg error"; msg.textContent = err.message;
      submit.disabled = false;
    }
  });

  form.append(grid, msg, actions);
  openModal("New Transaction", form);
}

/* =========================================================================
 * Section: Batch Processing
 * ======================================================================= */
function renderBatch() {
  setTitle("Batch Processing", "Daily transaction posting — replaces JCL batch jobs");
  const root = view();

  const explanation = el("div", { class: "note-box", html:
    `<strong>What this does.</strong> The original COBOL batch program
     <code>CBTRN01C</code> (with <code>CBTRN02C</code>/<code>CBTRN03C</code>) ran
     overnight via JCL to post the day's transactions to account balances. It read
     the daily transaction file, looked up each card's account, validated it, and
     updated the running balance and cycle credit/debit totals.<br><br>
     Here that job is a Lambda invoked by <code>POST /batch/process-daily</code>.
     It scans for unprocessed transactions, applies them to the account
     (<em>Purchase</em> decreases the balance; <em>Payment/Credit/Refund</em>
     increases it), writes the results back, and marks each transaction processed.`,
  });

  const runBtn = el("button", { class: "btn primary", text: "Run Daily Batch" });
  const resultBox = el("div");
  const head = el("div", { class: "panel-head" }, [
    el("h2", { text: "Daily Batch Job (CBTRN01C/02C/03C)" }),
    el("div", { class: "panel-tools" }, runBtn),
  ]);
  const panel = el("div", { class: "panel" }, [head, el("div", { style: "padding:20px" }, [explanation, resultBox])]);
  root.replaceChildren(panel);

  runBtn.addEventListener("click", async () => {
    runBtn.disabled = true;
    resultBox.replaceChildren(loadingState("Running daily batch…"));
    try {
      const res = await apiFetch("/batch/process-daily", { method: "POST" });
      const d = (res && res.data) || {};
      resultBox.replaceChildren(el("div", { class: "batch-result" }, [
        statCard("Processed", num(d.transactions_processed), "green", "transactions posted"),
        statCard("Skipped", num(d.skipped), "amber", "account missing"),
        statCard("Errors", num(d.errors), "sky", "unexpected failures"),
      ]));
    } catch (e) {
      resultBox.replaceChildren(errorState(e.message));
    } finally {
      runBtn.disabled = false;
    }
  });
}

/* =========================================================================
 * Section: System Info (COBOL -> AWS mapping)
 * ======================================================================= */
function renderSystem() {
  setTitle("System Info", "COBOL → AWS serverless migration mapping");
  const root = view();

  const mapping = [
    ["VSAM ACCTDAT (KSDS)", "DynamoDB carddemo-accounts"],
    ["VSAM CUSTDAT (KSDS)", "DynamoDB carddemo-customers"],
    ["VSAM CARDDAT (KSDS) + AIX", "DynamoDB carddemo-cards + acct_id-index"],
    ["VSAM CXACAIX (xref)", "DynamoDB carddemo-card-xref"],
    ["VSAM TRANSACT (KSDS)", "DynamoDB carddemo-transactions + GSIs"],
    ["VSAM TRANTYPE / TRANCATG", "DynamoDB transaction-types / -categories"],
    ["CICS online programs (COACTVWC, COCRDLIC, COTRN00C…)", "API Lambda routes (handler.py)"],
    ["JCL batch (CBTRN01C/02C/03C)", "Batch Lambda daily_processor.py"],
    ["BMS 3270 maps", "S3-hosted SPA frontend"],
    ["IDCAMS / data load", "data-migration/load_data.py → DynamoDB"],
    ["JCL job scheduling", "EventBridge (optional daily trigger)"],
    ["COMMAREA / pseudo-conversational state", "Stateless HTTP requests + DynamoDB"],
  ];
  const rows = mapping.map(([cobol, aws]) => el("tr", {}, [
    el("td", {}, el("span", { class: "badge gray", text: cobol })),
    el("td", {}, el("span", { class: "badge blue", text: aws })),
  ]));
  const mappingPanel = el("div", { class: "panel" }, [
    el("div", { class: "panel-head" }, el("h2", { text: "COBOL / Mainframe → AWS" })),
    el("div", { class: "table-wrap" }, el("table", { class: "data" }, [
      el("thead", {}, el("tr", {}, [
        el("th", { text: "Legacy (Mainframe)" }), el("th", { text: "AWS Replacement" }),
      ])),
      el("tbody", {}, rows),
    ])),
  ]);

  const arch =
`                    ┌──────────────────────────┐
   Browser ───────► │ S3 static website (SPA)  │
                    │  index.html / app.js     │
                    └────────────┬─────────────┘
                                 │ fetch (CORS)
                                 ▼
                    ┌──────────────────────────┐
                    │ HTTP API Gateway v2      │
                    │  stage: prod · 14 routes │
                    └────────────┬─────────────┘
                                 │ AWS_PROXY
                ┌────────────────┴────────────────┐
                ▼                                  ▼
   ┌────────────────────┐            ┌────────────────────────┐
   │ API Lambda (py3.12)│            │ Batch Lambda (py3.12)  │
   │ handler.py + db.py │            │ daily_processor.py     │
   │ 14 REST routes     │            │ CBTRN01C/02C/03C logic │
   └─────────┬──────────┘            └───────────┬────────────┘
             └───────────────┬───────────────────┘
                             ▼
              ┌──────────────────────────────┐
              │ DynamoDB (7 tables, on-demand)│
              └──────────────────────────────┘`;

  const archPanel = el("div", { class: "panel" }, [
    el("div", { class: "panel-head" }, el("h2", { text: "Architecture Overview" })),
    el("div", { style: "padding:20px" }, [
      el("div", { class: "arch-box", text: arch }),
      el("div", { class: "note-box", style: "margin-top:18px", html:
        `Fully serverless: <strong>DynamoDB</strong> (on-demand) replaces VSAM,
         <strong>Lambda</strong> (Python 3.12) replaces CICS online programs and
         JCL batch jobs, <strong>API Gateway v2</strong> exposes 14 REST routes,
         and an <strong>S3</strong> static website serves this single-page app —
         all provisioned with Terraform. No servers to manage; pay-per-use scaling.` }),
    ]),
  ]);

  root.replaceChildren(el("div", { class: "info-grid" }, [mappingPanel, archPanel]));
}

/* =========================================================================
 * Router
 * ======================================================================= */
const ROUTES = {
  dashboard: renderDashboard,
  accounts: renderAccounts,
  customers: renderCustomers,
  cards: renderCards,
  transactions: renderTransactions,
  batch: renderBatch,
  system: renderSystem,
};

function currentRoute() {
  const hash = (location.hash || "#dashboard").replace(/^#/, "");
  return ROUTES[hash] ? hash : "dashboard";
}
function navigate() {
  closeModal();
  const route = currentRoute();
  for (const item of document.querySelectorAll(".nav-item")) {
    item.classList.toggle("active", item.dataset.route === route);
  }
  ROUTES[route]();
}

/* ---------- API status indicator + missing-config banner ---------- */
function renderApiStatus() {
  const statusEl = $("#apiStatus");
  const banner = $("#apiBanner");
  if (!API) {
    statusEl.textContent = "API: not configured";
    statusEl.className = "api-status bad";
    banner.classList.remove("hidden");
    banner.innerHTML =
      `<strong>API URL not configured.</strong> Set
       <code>window.CARDDEMO_API_URL</code> in <code>config.js</code> to your
       deployed API Gateway base URL (the deploy step does this automatically).
       Until then, live data cannot be loaded — the layout below is a preview.`;
  } else {
    statusEl.textContent = "API: configured";
    statusEl.className = "api-status ok";
    banner.classList.add("hidden");
  }
}

/* ---------- boot ---------- */
window.addEventListener("hashchange", navigate);
document.addEventListener("DOMContentLoaded", () => {
  renderApiStatus();
  navigate();
});
