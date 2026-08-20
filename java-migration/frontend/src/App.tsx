import { useEffect, useRef, useState } from "react";
import { Navigate, Route, Routes, useNavigate, useSearchParams } from "react-router-dom";
import { api, Context, MenuData } from "./api";
import { AuthProvider, useAuth } from "./auth";
import { BmsScreen, MapFields, routeForProgram } from "./components";
import { MAPS, MapSpec } from "./maps";

function Guard({ children, admin = false }: { children: React.ReactNode; admin?: boolean }) {
  const { token, role } = useAuth();
  if (!token) return <Navigate to="/signon" replace />;
  if (admin && role !== "ROLE_ADMIN") return <Navigate to="/menu" replace />;
  return <>{children}</>;
}

function ScreenState({ map, children, onSubmit, context, message = "", onBack, onPageUp, onPageDown }: { map: MapSpec; children: React.ReactNode; onSubmit?: () => void; context?: Context | null; message?: string; onBack?: () => void; onPageUp?: () => void; onPageDown?: () => void }) {
  return <BmsScreen map={map} message={message} context={context} onSubmit={onSubmit} onBack={onBack} onPageUp={onPageUp} onPageDown={onPageDown}>{children}</BmsScreen>;
}

function text(value: unknown): string {
  return value === null || value === undefined ? "" : String(value);
}

function money(value: unknown): string {
  const raw = text(value).replace(/,/g, "").trim();
  if (!raw) return "";
  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed.toFixed(2) : text(value);
}

function displayDate(value: unknown): string {
  const raw = text(value);
  if (/^\d{8}$/.test(raw)) return `${raw.slice(0, 4)}-${raw.slice(4, 6)}-${raw.slice(6, 8)}`;
  return /^\d{4}-\d{2}-\d{2}/.test(raw) ? raw.slice(0, 10) : raw;
}

function datePart(value: unknown, part: "year" | "month" | "day"): string {
  const date = text(value);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return "";
  return part === "year" ? date.slice(0, 4) : part === "month" ? date.slice(5, 7) : date.slice(8, 10);
}

function accountValues(payload: Record<string, unknown>, update: boolean): Record<string, string> {
  const account = (payload.account as Record<string, unknown> | undefined) ?? payload;
  const customer = (payload.customer as Record<string, unknown> | undefined) ?? payload;
  const values: Record<string, string> = {
    accountId: text(account.acctId ?? payload.accountId),
    activeStatus: text(account.activeStatus),
    status: text(account.activeStatus),
    openDate: text(account.openDate),
    expirationDate: text(account.expiraionDate),
    reissueDate: text(account.reissueDate),
    creditLimit: money(account.creditLimit),
    cashLimit: money(account.cashCreditLimit),
    currentBalance: money(account.currBal),
    cycleCredit: money(account.currCycCredit),
    cycleDebit: money(account.currCycDebit),
    groupId: text(account.groupId),
    customerNumber: text(customer.custId),
    ssn: text(customer.ssn),
    dateOfBirth: text(customer.dobYyyyMmDd),
    ficoScore: text(customer.ficoCreditScore),
    firstName: text(customer.firstName),
    middleName: text(customer.middleName),
    lastName: text(customer.lastName),
    addressLine1: text(customer.addrLine1),
    addressLine2: text(customer.addrLine2),
    city: text(customer.addrLine3),
    state: text(customer.addrStateCd),
    zip: text(customer.addrZip),
    country: text(customer.addrCountryCd),
    phone1: text(customer.phoneNum1),
    phone2: text(customer.phoneNum2),
    governmentId: text(customer.govtIssuedId),
    eftAccountId: text(customer.eftAccountId),
    primaryCardHolder: text(customer.priCardHolderInd),
  };
  if (update) {
    values.openYear = datePart(account.openDate, "year");
    values.openMonth = datePart(account.openDate, "month");
    values.openDay = datePart(account.openDate, "day");
    values.expirationYear = datePart(account.expiraionDate, "year");
    values.expirationMonth = datePart(account.expiraionDate, "month");
    values.expirationDay = datePart(account.expiraionDate, "day");
    values.reissueYear = datePart(account.reissueDate, "year");
    values.reissueMonth = datePart(account.reissueDate, "month");
    values.reissueDay = datePart(account.reissueDate, "day");
  }
  return values;
}

function cardValues(payload: Record<string, unknown>): Record<string, string> {
  const expiration = text(payload.expiraionDate);
  return {
    accountId: text(payload.acctId),
    cardNumber: text(payload.cardNum),
    cardName: text(payload.embossedName),
    status: text(payload.activeStatus),
    expirationDate: expiration,
    expirationYear: datePart(expiration, "year"),
    expirationMonth: datePart(expiration, "month"),
    expirationDay: datePart(expiration, "day"),
  };
}

function composeDate(values: Record<string, string>, prefix: string): string {
  const year = values[`${prefix}Year`] ?? "";
  const month = values[`${prefix}Month`] ?? "";
  const day = values[`${prefix}Day`] ?? "";
  return year && month && day ? `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}` : "";
}

function transactionValues(payload: Record<string, unknown>): Record<string, string> {
  return {
    transactionId: text(payload.tranId),
    cardNumber: text(payload.cardNum),
    typeCode: text(payload.typeCd),
    categoryCode: text(payload.catCd),
    source: text(payload.source),
    description: text(payload.tranDesc),
    amount: money(payload.amt),
    origDate: displayDate(payload.origTs),
    procDate: displayDate(payload.procTs),
    merchantId: text(payload.merchantId),
    merchantName: text(payload.merchantName),
    merchantCity: text(payload.merchantCity),
    merchantZip: text(payload.merchantZip),
  };
}

function authorizationValues(payload: Record<string, unknown>): Record<string, string> {
  return {
    authorizationId: text(payload.id),
    accountId: text(payload.acctId),
    cardNumber: text(payload.cardNum),
    transactionId: text(payload.transactionId),
    amount: money(payload.approvedAmt ?? payload.transactionAmt),
    responseCode: text(payload.authRespCode),
    matchStatus: text(payload.matchStatus),
    fraud: text(payload.authFraud),
  };
}

function accountRequest(values: Record<string, string>, preImage: Record<string, string>, context: Context | null) {
  return {
    accountId: values.accountId ? Number(values.accountId) : null,
    activeStatus: values.activeStatus,
    creditLimit: values.creditLimit,
    cashCreditLimit: values.cashLimit,
    openDate: composeDate(values, "open"),
    expiraionDate: composeDate(values, "expiration"),
    reissueDate: composeDate(values, "reissue"),
    groupId: values.groupId,
    firstName: values.firstName,
    middleName: values.middleName,
    lastName: values.lastName,
    addressLine1: values.addressLine1,
    addressLine2: values.addressLine2,
    city: values.city,
    state: values.state,
    country: values.country,
    zip: values.zip,
    phone1: values.phone1,
    phone2: values.phone2,
    eftAccountId: values.eftAccountId,
    primaryCardHolder: values.primaryCardHolder || "Y",
    dob: values.dateOfBirth,
    ficoScore: values.ficoScore,
    preImage: {
      activeStatus: preImage.activeStatus,
      currBal: preImage.currentBalance,
      creditLimit: preImage.creditLimit,
      cashCreditLimit: preImage.cashLimit,
      openDate: preImage.openDate,
      expiraionDate: preImage.expirationDate,
      reissueDate: preImage.reissueDate,
      currCycCredit: preImage.cycleCredit,
      currCycDebit: preImage.cycleDebit,
      groupId: preImage.groupId,
      firstName: preImage.firstName,
      middleName: preImage.middleName,
      lastName: preImage.lastName,
      addressLine1: preImage.addressLine1,
      addressLine2: preImage.addressLine2,
      city: preImage.city,
      state: preImage.state,
      country: preImage.country,
      zip: preImage.zip,
      phone1: preImage.phone1,
      phone2: preImage.phone2,
      eftAccountId: preImage.eftAccountId,
      primaryCardHolder: preImage.primaryCardHolder,
      dob: preImage.dateOfBirth,
      ficoScore: preImage.ficoScore,
    },
    confirm: values.confirm === "Y",
    context,
  };
}

function Signon() {
  const { signon, authMessage } = useAuth();
  const navigate = useNavigate();
  const [values, setValues] = useState({ userId: "", password: "" });
  const [message, setMessage] = useState(authMessage);
  const set = (key: string, value: string) => setValues((old) => ({ ...old, [key]: value }));
  async function submit() {
    if (!values.userId.trim()) return setMessage("Please enter User ID ...");
    if (!values.password.trim()) return setMessage("Please enter Password ...");
    try {
      const response = await signon(values.userId, values.password);
      if (response.message) setMessage(response.message);
      else navigate(routeForProgram(response.nextRoute));
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to verify the User ...");
    }
  }
  return <ScreenState map={MAPS.COSGN00} message={message} onSubmit={submit}><div className="form-stack narrow">
    <label>User ID<input aria-label="User ID" name="userId" maxLength={8} value={values.userId} onChange={(e) => set("userId", e.target.value)} autoFocus /></label>
    <label>Password<input aria-label="Password" name="password" type="password" maxLength={8} value={values.password} onChange={(e) => set("password", e.target.value)} /></label>
  </div></ScreenState>;
}

function Menu({ admin = false }: { admin?: boolean }) {
  const { context, role, signout } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState<MenuData | null>(null);
  const [message, setMessage] = useState("");
  const [option, setOption] = useState("");
  useEffect(() => {
    (admin ? api.adminMenu(context ?? undefined) : api.menu(context ?? undefined)).then((response) => {
      setData(response.data);
      setMessage(response.message);
    }).catch((error: Error) => setMessage(error.message));
  }, [admin, context]);
  function choose(number: number, program: string) {
    const routes = routeForProgram(program);
    if (admin && role !== "ROLE_ADMIN") return setMessage("No access to this option ...");
    navigate(routes);
  }
  return <ScreenState map={admin ? MAPS.COADM01 : MAPS.COMEN01} message={message} context={context} onSubmit={() => {
    const selected = data?.options.find((item) => item.number === Number(option));
    if (selected) choose(selected.number, selected.program);
    else setMessage("Please enter a valid option number...");
  }} onBack={() => { signout(); navigate("/signon"); }}>
    <section className="menu-options">
      {data?.options.filter((item) => admin ? item.userType === "A" && role === "ROLE_ADMIN" : item.userType === "U").map((item) => <button type="button" key={item.number} onClick={() => choose(item.number, item.program)}><b>{String(item.number).padStart(2, " ")}</b> {item.name}</button>)}
    </section>
    <label className="option-input">Option<input aria-label="Option" maxLength={2} value={option} onChange={(e) => setOption(e.target.value)} /></label>
  </ScreenState>;
}

function AccountView({ update = false }: { update?: boolean }) {
  const map = update ? MAPS.COACTUP : MAPS.COACTVW;
  const { context } = useAuth();
  const [values, setValues] = useState<Record<string, string>>({ accountId: context?.accountId?.toString() ?? "" });
  const [preImage, setPreImage] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const [loaded, setLoaded] = useState(false);
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  async function submit() {
    if (!values.accountId || !/^\d+$/.test(values.accountId) || Number(values.accountId) <= 0) return setMessage("Account Number if supplied must be a 11 digit Non-Zero Number");
    if (!loaded) {
      const response = await api.account(values.accountId, context ?? undefined);
      setMessage(response.message);
      if (response.data) {
        const normalized = accountValues(response.data as Record<string, unknown>, update);
        setValues((old) => ({ ...old, ...normalized }));
        setPreImage(normalized);
        setLoaded(true);
      }
      return;
    }
    if (update) {
      const response = await api.updateAccount(values.accountId, accountRequest(values, preImage, context));
      setMessage(response.message);
      if (response.data) {
        const normalized = accountValues(response.data as Record<string, unknown>, true);
        setValues((old) => ({ ...old, ...normalized }));
        setPreImage(normalized);
      }
    }
  }
  return <ScreenState map={map} message={message} context={context} onSubmit={submit}><MapFields map={map} values={values} setValue={set} exclude={["message"]} /></ScreenState>;
}

function CardList() {
  const { context } = useAuth();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<{ cards: Record<string, unknown>[]; nextPage: boolean; previousPage: boolean } | null>(null);
  const [values, setValues] = useState({ accountId: "", cardNumber: "" });
  const [message, setMessage] = useState("");
  const navigate = useNavigate();
  const load = (nextPage: number) => api.cards({ ...values, page: nextPage, context: context ?? undefined }).then((response) => { setData(response.data); setMessage(response.message); setPage(nextPage); }).catch((error: Error) => setMessage(error.message));
  useEffect(() => { load(0); }, []);
  return <ScreenState map={MAPS.COCRDLI} message={message} context={context} onSubmit={() => load(0)} onPageUp={() => load(Math.max(0, page - 1))} onPageDown={() => data?.nextPage && load(page + 1)}>
    <MapFields map={MAPS.COCRDLI} values={values} setValue={(name, value) => setValues((old) => ({ ...old, [name]: value }))} exclude={["message", ...Array.from({ length: 7 }, (_, index) => `select${index + 1}`)]} />
    <div className="record-table">
      <table aria-label="Credit card list">
        <thead><tr><th>Select</th><th>Account Number</th><th>Card Number</th><th>Active</th></tr></thead>
        <tbody>{Array.from({ length: 7 }, (_, index) => data?.cards?.[index] ?? null).map((card, index) => (
          <tr key={card ? text(card.cardNum) : `empty-${index}`}>
            <td><button type="button" disabled={!card} onClick={() => card && navigate(`/cards/detail?cardNumber=${encodeURIComponent(text(card.cardNum))}`)}>Select</button></td>
            <td>{card ? text(card.acctId) : ""}</td><td>{card ? text(card.cardNum) : ""}</td><td>{card ? text(card.activeStatus) : ""}</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
    <div className="paging"><button type="button" disabled={!data?.previousPage} onClick={() => load(Math.max(0, page - 1))}>PF7 Page Up</button><span>Page {page + 1}</span><button type="button" disabled={!data?.nextPage} onClick={() => load(page + 1)}>PF8 Page Down</button></div>
  </ScreenState>;
}

function CardScreen({ update = false }: { update?: boolean }) {
  const map = update ? MAPS.COCRDUP : MAPS.COCRDSL;
  const [searchParams] = useSearchParams();
  const cardNumber = searchParams.get("cardNumber") ?? "";
  const [values, setValues] = useState<Record<string, string>>({ cardNumber });
  const [preImage, setPreImage] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const [loaded, setLoaded] = useState(false);
  const requestInFlight = useRef(false);
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  async function submit() {
    if (!values.cardNumber) return setMessage("CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER");
    if (requestInFlight.current) return;
    requestInFlight.current = true;
    try {
      if (update && !loaded) {
        const initial = await api.card(values.cardNumber);
        setMessage(initial.message);
        if (initial.data) {
          const normalized = cardValues(initial.data as Record<string, unknown>);
          setValues((old) => ({ ...old, ...normalized }));
          setPreImage({ embossedName: normalized.cardName, activeStatus: normalized.status, expirationDate: normalized.expirationDate });
          setLoaded(true);
        }
        return;
      }
      const response = update
        ? await api.updateCard(values.cardNumber, {
            embossedName: values.cardName,
            activeStatus: values.status,
            expirationMonth: values.expirationMonth,
            expirationYear: values.expirationYear,
            preImage,
            confirm: values.confirm === "Y",
          })
        : await api.card(values.cardNumber);
      setMessage(response.message);
      if (response.data) {
        const normalized = cardValues(response.data as Record<string, unknown>);
        setValues((old) => ({ ...old, ...normalized }));
        setPreImage({ embossedName: normalized.cardName, activeStatus: normalized.status, expirationDate: normalized.expirationDate });
        setLoaded(true);
      }
    } finally {
      requestInFlight.current = false;
    }
  }
  useEffect(() => {
    if (cardNumber) void submit();
  }, [cardNumber, update]);
  return <ScreenState map={map} message={message} onSubmit={submit}><MapFields map={map} values={values} setValue={set} exclude={["message"]} /></ScreenState>;
}

function TransactionList() {
  const { context } = useAuth();
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<{ transactions: Record<string, unknown>[]; nextPage: boolean; previousPage: boolean } | null>(null);
  const [message, setMessage] = useState("");
  const load = (next: number) => api.transactions({ page: next, direction: "FORWARD", context }).then((r) => { setData(r.data); setMessage(r.message); setPage(next); }).catch((e: Error) => setMessage(e.message));
  useEffect(() => { load(0); }, []);
  return <ScreenState map={MAPS.COTRN00} message={message} context={context} onPageUp={() => load(Math.max(page - 1, 0))} onPageDown={() => data?.nextPage && load(page + 1)}>
    <div className="record-table">
      <table aria-label="Transaction list">
        <thead><tr><th>Select</th><th>Transaction ID</th><th>Date</th><th>Description</th><th>Amount</th></tr></thead>
        <tbody>{(data?.transactions ?? []).map((row) => (
          <tr key={text(row.tranId)}>
            <td><button type="button" onClick={() => navigate(`/transactions/view?transactionId=${encodeURIComponent(text(row.tranId))}`)}>Select</button></td>
            <td>{text(row.tranId)}</td><td>{displayDate(row.origTs)}</td><td>{text(row.tranDesc)}</td><td>{money(row.amt)}</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
    <div className="paging"><button type="button" disabled={!data?.previousPage} onClick={() => load(Math.max(page - 1, 0))}>PF7 Page Up</button><span>Page {page + 1}</span><button type="button" disabled={!data?.nextPage} onClick={() => load(page + 1)}>PF8 Page Down</button></div>
  </ScreenState>;
}

function TransactionView() {
  const [searchParams] = useSearchParams();
  const transactionId = searchParams.get("transactionId") ?? "";
  const [values, setValues] = useState<Record<string, string>>({ transactionId });
  const [message, setMessage] = useState("");
  useEffect(() => {
    if (transactionId) {
      api.transaction(transactionId).then((r) => {
        setMessage(r.message);
        if (r.data) setValues((old) => ({ ...old, ...transactionValues(r.data as Record<string, unknown>) }));
      }).catch((e: Error) => setMessage(e.message));
    }
  }, [transactionId]);
  return <ScreenState map={MAPS.COTRN01} message={message} onSubmit={() => api.transaction(values.transactionId).then((r) => { setMessage(r.message); if (r.data) setValues((old) => ({ ...old, ...transactionValues(r.data as Record<string, unknown>) })); }).catch((e: Error) => setMessage(e.message))}><MapFields map={MAPS.COTRN01} values={values} setValue={(name, value) => setValues((old) => ({ ...old, [name]: value }))} exclude={["message"]} /></ScreenState>;
}

function TransactionAdd() {
  const { context } = useAuth();
  const [values, setValues] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  function validate() {
    if (values.accountId && !/^\d+$/.test(values.accountId)) return ["Account ID must be Numeric...", "accountId"];
    const required: [string, string][] = [["accountId", "Account or Card Number must be entered..."], ["cardNumber", "Account or Card Number must be entered..."], ["typeCode", "Type CD can NOT be empty..."], ["categoryCode", "Category CD can NOT be empty..."], ["source", "Source can NOT be empty..."], ["description", "Description can NOT be empty..."], ["amount", "Amount can NOT be empty..."], ["origDate", "Orig Date can NOT be empty..."], ["procDate", "Proc Date can NOT be empty..."], ["merchantId", "Merchant ID can NOT be empty..."], ["merchantName", "Merchant Name can NOT be empty..."], ["merchantCity", "Merchant City can NOT be empty..."], ["merchantZip", "Merchant Zip can NOT be empty..."]];
    const missing = required.find(([field]) => !values[field]?.trim());
    return missing ? [missing[1], missing[0]] : null;
  }
  async function submit() {
    const error = validate();
    if (error) return setMessage(error[0]);
    const response = await api.addTransaction({ ...values, confirm: values.confirm === "Y", context });
    setMessage(response.message);
    if (response.data) setValues((old) => ({ ...old, ...transactionValues(response.data as Record<string, unknown>) }));
  }
  return <ScreenState map={MAPS.COTRN02} message={message} context={context} onSubmit={submit}><MapFields map={MAPS.COTRN02} values={values} setValue={set} exclude={["message"]} /></ScreenState>;
}

function BillPayment() {
  const { context } = useAuth();
  const [values, setValues] = useState({ accountId: "", confirm: "" });
  const [message, setMessage] = useState("");
  return <ScreenState map={MAPS.COBIL00} message={message} context={context} onSubmit={() => api.billPayment({ accountId: values.accountId ? Number(values.accountId) : null, confirm: values.confirm.toUpperCase() === "Y", context }).then((r) => { setMessage(r.message); if (r.data) setValues((old) => ({ ...old, currentBalance: money((r.data as Record<string, unknown>).remainingBalance) })); }).catch((e: Error) => setMessage(e.message))}><MapFields map={MAPS.COBIL00} values={values} setValue={(name, value) => setValues((old) => ({ ...old, [name]: value }))} exclude={["message"]} /></ScreenState>;
}

function Reports() {
  const { context } = useAuth();
  const [values, setValues] = useState({ reportType: "monthly", startDate: "", endDate: "", confirm: "" });
  const [message, setMessage] = useState("");
  return <ScreenState map={MAPS.CORPT00} message={message} context={context} onSubmit={() => api.report({ ...values, confirm: values.confirm.toUpperCase() === "Y", context }).then((r) => setMessage(r.message)).catch((e: Error) => setMessage(e.message))}><div className="form-stack"><label>Report Type<select aria-label="Report Type" value={values.reportType} onChange={(e) => setValues((old) => ({ ...old, reportType: e.target.value }))}><option value="monthly">Monthly</option><option value="yearly">Yearly</option><option value="custom">Custom</option></select></label><label>Start Date<input aria-label="Start Date" type="date" value={values.startDate} onChange={(e) => setValues((old) => ({ ...old, startDate: e.target.value }))} /></label><label>End Date<input aria-label="End Date" type="date" value={values.endDate} onChange={(e) => setValues((old) => ({ ...old, endDate: e.target.value }))} /></label><label>Confirm<input aria-label="Confirm" maxLength={1} value={values.confirm} onChange={(e) => setValues((old) => ({ ...old, confirm: e.target.value }))} /></label></div></ScreenState>;
}

function Users({ mode = "list" }: { mode?: "list" | "add" | "update" | "delete" }) {
  const { context } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [page, setPage] = useState(0);
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [values, setValues] = useState<Record<string, string>>({ userId: searchParams.get("userId") ?? "" });
  const [message, setMessage] = useState("");
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  useEffect(() => { if (mode === "list") api.users(page).then((r) => { setRows(r.data?.users ?? []); setMessage(r.message); }).catch((e: Error) => setMessage(e.message)); }, [mode, page]);
  async function submit() {
    const response = mode === "add" ? await api.addUser({ ...values, context }) : mode === "update" ? await api.updateUser(values.userId, { ...values, confirm: values.confirm === "Y", context }) : await api.deleteUser(values.userId, { confirm: values.confirm === "Y", context });
    setMessage(response.message);
    if (response.data) {
      const data = response.data as Record<string, unknown>;
      setValues((old) => ({ ...old, userId: text(data.secUsrId ?? old.userId), firstName: text(data.secUsrFname ?? old.firstName), lastName: text(data.secUsrLname ?? old.lastName), userType: text(data.secUsrType ?? old.userType) }));
    }
  }
  const map = mode === "list" ? MAPS.COUSR00 : mode === "add" ? MAPS.COUSR01 : mode === "update" ? MAPS.COUSR02 : MAPS.COUSR03;
  return <ScreenState map={map} message={message} context={context} onSubmit={mode === "list" ? undefined : submit} onPageUp={mode === "list" ? () => setPage(Math.max(page - 1, 0)) : undefined} onPageDown={mode === "list" ? () => setPage(page + 1) : undefined}>
    <MapFields map={map} values={values} setValue={set} exclude={mode === "list" ? ["message", ...Array.from({ length: 10 }, (_, index) => `select${index + 1}`)] : ["message"]} />
    {mode === "list" && <div className="record-table"><table aria-label="User list"><thead><tr><th>Select</th><th>User ID</th><th>First Name</th><th>Last Name</th><th>Type</th></tr></thead><tbody>{rows.map((row) => <tr key={text(row.secUsrId)}><td><button type="button" onClick={() => navigate(`/admin/users/update?userId=${encodeURIComponent(text(row.secUsrId))}`)}>Select</button></td><td>{text(row.secUsrId)}</td><td>{text(row.secUsrFname)}</td><td>{text(row.secUsrLname)}</td><td>{text(row.secUsrType)}</td></tr>)}</tbody></table></div>}
  </ScreenState>;
}

function ExtensionScreen({
  update = false,
  transactionTypes = false,
}: {
  update?: boolean;
  transactionTypes?: boolean;
}) {
  const { context } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const authorizationId = searchParams.get("id") ?? "";
  const initialAccountId = context?.accountId?.toString() ?? searchParams.get("accountId") ?? "";
  const map = transactionTypes
    ? update
      ? MAPS.COTRTUP
      : MAPS.COTRTLI
    : update
      ? MAPS.COPAU01
      : MAPS.COPAU00;
  const [values, setValues] = useState<Record<string, string>>({
    authorizationId,
    accountId: initialAccountId,
  });
  const [message, setMessage] = useState("");
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [page, setPage] = useState(0);
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  async function submit(targetPage = page) {
    try {
      if (transactionTypes && update) {
        const response = await api.saveTransactionType({
          type: values.type ?? "",
          description: values.description ?? "",
        });
        setMessage(response.message);
        if (response.data) {
          const data = response.data as Record<string, unknown>;
          setValues((old) => ({ ...old, type: text(data.typeCd), description: text(data.typeDesc) }));
        }
      } else if (transactionTypes) {
        const response = await api.transactionTypes(targetPage);
        setRows(response.data ?? []);
        setMessage(response.message);
      } else if (update) {
        const response = await api.authorization(values.authorizationId ?? values.id ?? "");
        if (response.data) {
          setRows([response.data]);
          setValues((old) => ({ ...old, ...authorizationValues(response.data as Record<string, unknown>) }));
        } else setRows([]);
        setMessage(response.message);
      } else {
        const response = await api.authorizations(values.accountId ?? "", targetPage);
        setRows(response.data ?? []);
        setMessage(response.message);
      }
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Unable to process request");
    }
  }
  useEffect(() => {
    if (update && authorizationId) void submit();
    else if (!update && transactionTypes) void submit(0);
    else if (!update && initialAccountId) void submit(0);
  }, [update, transactionTypes, authorizationId, initialAccountId]);
  return <ScreenState map={map} message={message} context={context} onSubmit={submit}
    onPageUp={!update ? () => { const target = Math.max(0, page - 1); setPage(target); void submit(target); } : undefined}
    onPageDown={!update ? () => { const target = page + 1; setPage(target); void submit(target); } : undefined}>
    <MapFields
      map={map}
      values={values}
      setValue={set}
      exclude={
        transactionTypes
          ? update
            ? ["message"]
            : ["message", ...Array.from({ length: 7 }, (_, index) => `select${index + 1}`), ...Array.from({ length: 7 }, (_, index) => `type${index + 1}`), ...Array.from({ length: 7 }, (_, index) => `description${index + 1}`)]
          : update
            ? ["message"]
            : ["message", ...Array.from({ length: 5 }, (_, index) => `select${index + 1}`), ...["transactionId", "authDate", "authTime", "authType", "approved", "status", "amount"].flatMap((name) => [1, 2, 3, 4, 5].map((row) => `${name}${row}`))]
      }
    />
    {rows.length > 0 && (transactionTypes ? (
      <div className="record-table"><table aria-label="Transaction type list"><thead><tr><th>Select</th><th>Type</th><th>Description</th></tr></thead><tbody>{rows.map((row) => <tr key={text(row.typeCd)}><td><button type="button" onClick={() => navigate(`/admin/transaction-types/update?type=${encodeURIComponent(text(row.typeCd))}`)}>Select</button></td><td>{text(row.typeCd)}</td><td>{text(row.typeDesc)}</td></tr>)}</tbody></table></div>
    ) : (
      <div className="record-table"><table aria-label="Authorization summary"><thead><tr><th>Select</th><th>Auth Date</th><th>Auth Time</th><th>Card Number</th><th>Transaction ID</th><th>Amount</th><th>Status</th></tr></thead><tbody>{rows.map((row) => <tr key={text(row.id)}><td><button type="button" onClick={() => navigate(`/authorizations/detail?id=${encodeURIComponent(text(row.id))}`)}>Select</button></td><td>{displayDate(row.authDate)}</td><td>{text(row.authTime)}</td><td>{text(row.cardNum)}</td><td>{text(row.transactionId)}</td><td>{money(row.approvedAmt)}</td><td>{text(row.matchStatus)}</td></tr>)}</tbody></table></div>
    ))}
    {!transactionTypes && !update && rows.length > 0 && (
      <div className="extension-controls">
        {rows.map((row, index) => (
          <button
            key={String(row.id ?? row.authorizationId ?? index)}
            type="button"
            onClick={() => {
              setValues((old) => ({
                ...old,
                authorizationId: String(row.id ?? row.authorizationId ?? ""),
              }));
              navigate(`/authorizations/detail?id=${encodeURIComponent(String(row.id ?? row.authorizationId ?? ""))}`);
            }}
          >
            Select row {index + 1}
          </button>
        ))}
      </div>
    )}
    {!transactionTypes && update && rows.length > 0 && (
      <button
        type="button"
        onClick={async () => {
          const id = values.authorizationId ?? values.id ?? "";
          const response = await api.markAuthorizationFraud(id, values.fraud?.toUpperCase() === "Y");
          setMessage(response.message);
        }}
      >
        PF5 Mark/Remove Fraud
      </button>
    )}
    {transactionTypes && !update && (
      <div className="extension-controls">
        <button type="button" onClick={() => navigate("/admin/transaction-types/update")}>
          PF2 Add
        </button>
        <button type="button" onClick={() => void submit()}>F10 Save</button>
      </div>
    )}
  </ScreenState>;
}

function AppRoutes() {
  return <Routes>
    <Route path="/signon" element={<Signon />} />
    <Route path="/menu" element={<Guard><Menu /></Guard>} />
    <Route path="/admin/menu" element={<Guard admin><Menu admin /></Guard>} />
    <Route path="/accounts/view" element={<Guard><AccountView /></Guard>} />
    <Route path="/accounts/update" element={<Guard><AccountView update /></Guard>} />
    <Route path="/cards" element={<Guard><CardList /></Guard>} />
    <Route path="/cards/detail" element={<Guard><CardScreen /></Guard>} />
    <Route path="/cards/update" element={<Guard><CardScreen update /></Guard>} />
    <Route path="/transactions" element={<Guard><TransactionList /></Guard>} />
    <Route path="/transactions/view" element={<Guard><TransactionView /></Guard>} />
    <Route path="/transactions/add" element={<Guard><TransactionAdd /></Guard>} />
    <Route path="/bill-payment" element={<Guard><BillPayment /></Guard>} />
    <Route path="/reports" element={<Guard><Reports /></Guard>} />
    <Route path="/admin/users" element={<Guard admin><Users /></Guard>} />
    <Route path="/admin/users/add" element={<Guard admin><Users mode="add" /></Guard>} />
    <Route path="/admin/users/update" element={<Guard admin><Users mode="update" /></Guard>} />
    <Route path="/admin/users/delete" element={<Guard admin><Users mode="delete" /></Guard>} />
    <Route path="/authorizations" element={<Guard><ExtensionScreen /></Guard>} />
    <Route path="/authorizations/detail" element={<Guard><ExtensionScreen update /></Guard>} />
    <Route path="/admin/transaction-types" element={<Guard admin><ExtensionScreen transactionTypes /></Guard>} />
    <Route path="/admin/transaction-types/update" element={<Guard admin><ExtensionScreen update transactionTypes /></Guard>} />
    <Route path="*" element={<Navigate to="/signon" replace />} />
  </Routes>;
}

export default function App() {
  return <AuthProvider><AppRoutes /></AuthProvider>;
}
