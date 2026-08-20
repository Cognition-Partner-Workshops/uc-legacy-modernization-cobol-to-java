import { useEffect, useState } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { api, Context, MenuData } from "./api";
import { AuthProvider, useAuth } from "./auth";
import { BmsScreen, MapFields, RecordTable, routeForProgram } from "./components";
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

function Signon() {
  const { signon } = useAuth();
  const navigate = useNavigate();
  const [values, setValues] = useState({ userId: "", password: "" });
  const [message, setMessage] = useState("");
  const set = (key: string, value: string) => setValues((old) => ({ ...old, [key]: value }));
  async function submit() {
    if (!values.userId.trim()) return setMessage("Please enter User ID ...");
    if (!values.password.trim()) return setMessage("Please enter Password ...");
    try {
      const responseMessage = await signon(values.userId, values.password);
      if (responseMessage) setMessage(responseMessage);
      else navigate("/menu");
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
      {data?.options.filter((item) => admin ? item.userType === "A" && role === "ROLE_ADMIN" : item.userType === "U").map((item) => <button type="button" key={item.number} onClick={() => choose(item.number, item.program)}><b>{String(item.number).padStart(2, " ")}</b> {item.label}</button>)}
    </section>
    <label className="option-input">Option<input aria-label="Option" maxLength={2} value={option} onChange={(e) => setOption(e.target.value)} /></label>
  </ScreenState>;
}

function AccountView({ update = false }: { update?: boolean }) {
  const map = update ? MAPS.COACTUP : MAPS.COACTVW;
  const { context } = useAuth();
  const [values, setValues] = useState<Record<string, string>>({ accountId: context?.accountId?.toString() ?? "" });
  const [message, setMessage] = useState("");
  const [loaded, setLoaded] = useState(false);
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  async function submit() {
    if (!values.accountId || !/^\d+$/.test(values.accountId)) return setMessage("Account Number if supplied must be a 11 digit Non-Zero Number");
    if (!loaded) {
      const response = await api.account(values.accountId, context ?? undefined);
      setMessage(response.message);
      if (response.data) {
        const payload = response.data as { account?: Record<string, unknown>; customer?: Record<string, unknown>; card?: Record<string, unknown> };
        const flattened = { ...(payload.account ?? {}), ...(payload.customer ?? {}), ...(payload.card ?? {}), ...payload };
        setValues((old) => ({ ...old, ...Object.fromEntries(Object.entries(flattened).map(([key, value]) => [key, String(value ?? "")])) }));
        setLoaded(true);
      }
      return;
    }
    if (update) {
      const response = await api.updateAccount(values.accountId, { ...values, confirm: values.confirm === "Y", context });
      setMessage(response.message);
      if (!response.message) setLoaded(true);
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
  const load = (nextPage: number) => api.cards({ ...values, page: nextPage, context: context ?? undefined }).then((response) => { setData(response.data); setMessage(response.message); setPage(nextPage); }).catch((error: Error) => setMessage(error.message));
  useEffect(() => { load(0); }, []);
  return <ScreenState map={MAPS.COCRDLI} message={message} context={context} onSubmit={() => load(0)} onPageUp={() => load(Math.max(0, page - 1))} onPageDown={() => data?.nextPage && load(page + 1)}>
    <MapFields map={MAPS.COCRDLI} values={values} setValue={(name, value) => setValues((old) => ({ ...old, [name]: value }))} exclude={["message"]} />
    <RecordTable rows={data?.cards ?? []} />
    <div className="paging"><button type="button" disabled={!data?.previousPage} onClick={() => load(Math.max(0, page - 1))}>PF7 Page Up</button><span>Page {page + 1}</span><button type="button" disabled={!data?.nextPage} onClick={() => load(page + 1)}>PF8 Page Down</button></div>
  </ScreenState>;
}

function CardScreen({ update = false }: { update?: boolean }) {
  const map = update ? MAPS.COCRDUP : MAPS.COCRDSL;
  const [values, setValues] = useState<Record<string, string>>({ cardNumber: "" });
  const [message, setMessage] = useState("");
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  async function submit() {
    if (!values.cardNumber) return setMessage("CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER");
    const response = update
      ? await api.updateCard(values.cardNumber, {
        embossedName: values.cardName,
        activeStatus: values.status,
        expirationMonth: values.expirationMonth,
        expirationYear: values.expirationYear,
        preImage: {
          embossedName: values.cardName,
          activeStatus: values.status,
          expirationMonth: values.expirationMonth,
          expirationYear: values.expirationYear,
        },
        confirm: values.confirm === "Y",
      })
      : await api.card(values.cardNumber);
    setMessage(response.message);
    if (response.data) setValues((old) => ({ ...old, ...Object.fromEntries(Object.entries(response.data ?? {}).map(([key, value]) => [key, String(value ?? "")])) }));
  }
  return <ScreenState map={map} message={message} onSubmit={submit}><MapFields map={map} values={values} setValue={set} exclude={["message"]} /></ScreenState>;
}

function TransactionList() {
  const { context } = useAuth();
  const [page, setPage] = useState(0);
  const [data, setData] = useState<{ transactions: Record<string, unknown>[]; nextPage: boolean; previousPage: boolean } | null>(null);
  const [message, setMessage] = useState("");
  const load = (next: number) => api.transactions({ page: next, direction: "FORWARD", context }).then((r) => { setData(r.data); setMessage(r.message); setPage(next); }).catch((e: Error) => setMessage(e.message));
  useEffect(() => { load(0); }, []);
  return <ScreenState map={MAPS.COTRN00} message={message} context={context} onPageUp={() => load(Math.max(page - 1, 0))} onPageDown={() => data?.nextPage && load(page + 1)}>
    <RecordTable rows={data?.transactions ?? []} />
    <div className="paging"><button type="button" disabled={!data?.previousPage} onClick={() => load(Math.max(page - 1, 0))}>PF7 Page Up</button><span>Page {page + 1}</span><button type="button" disabled={!data?.nextPage} onClick={() => load(page + 1)}>PF8 Page Down</button></div>
  </ScreenState>;
}

function TransactionView() {
  const [id, setId] = useState("");
  const [data, setData] = useState<Record<string, unknown> | null>(null);
  const [message, setMessage] = useState("");
  return <ScreenState map={MAPS.COTRN01} message={message} onSubmit={() => api.transaction(id).then((r) => { setMessage(r.message); setData(r.data); }).catch((e: Error) => setMessage(e.message))}><label>Transaction ID<input aria-label="Transaction ID" maxLength={16} value={id} onChange={(e) => setId(e.target.value)} /></label>{data && <RecordTable rows={[data]} />}</ScreenState>;
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
  }
  return <ScreenState map={MAPS.COTRN02} message={message} context={context} onSubmit={submit}><MapFields map={MAPS.COTRN02} values={values} setValue={set} exclude={["message"]} /></ScreenState>;
}

function BillPayment() {
  const { context } = useAuth();
  const [values, setValues] = useState({ accountId: "", confirm: "" });
  const [message, setMessage] = useState("");
  return <ScreenState map={MAPS.COBIL00} message={message} context={context} onSubmit={() => api.billPayment({ accountId: values.accountId ? Number(values.accountId) : null, confirm: values.confirm.toUpperCase() === "Y", context }).then((r) => setMessage(r.message)).catch((e: Error) => setMessage(e.message))}><MapFields map={MAPS.COBIL00} values={values} setValue={(name, value) => setValues((old) => ({ ...old, [name]: value }))} exclude={["message"]} /></ScreenState>;
}

function Reports() {
  const { context } = useAuth();
  const [values, setValues] = useState({ reportType: "monthly", startDate: "", endDate: "", confirm: "" });
  const [message, setMessage] = useState("");
  return <ScreenState map={MAPS.CORPT00} message={message} context={context} onSubmit={() => api.report({ ...values, confirm: values.confirm.toUpperCase() === "Y", context }).then((r) => setMessage(r.message)).catch((e: Error) => setMessage(e.message))}><div className="form-stack"><label>Report Type<select aria-label="Report Type" value={values.reportType} onChange={(e) => setValues((old) => ({ ...old, reportType: e.target.value }))}><option value="monthly">Monthly</option><option value="yearly">Yearly</option><option value="custom">Custom</option></select></label><label>Start Date<input aria-label="Start Date" type="date" value={values.startDate} onChange={(e) => setValues((old) => ({ ...old, startDate: e.target.value }))} /></label><label>End Date<input aria-label="End Date" type="date" value={values.endDate} onChange={(e) => setValues((old) => ({ ...old, endDate: e.target.value }))} /></label><label>Confirm<input aria-label="Confirm" maxLength={1} value={values.confirm} onChange={(e) => setValues((old) => ({ ...old, confirm: e.target.value }))} /></label></div></ScreenState>;
}

function Users({ mode = "list" }: { mode?: "list" | "add" | "update" | "delete" }) {
  const { context } = useAuth();
  const [page, setPage] = useState(0);
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [values, setValues] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  useEffect(() => { if (mode === "list") api.users(page).then((r) => { setRows(r.data?.users ?? []); setMessage(r.message); }).catch((e: Error) => setMessage(e.message)); }, [mode, page]);
  async function submit() {
    const response = mode === "add" ? await api.addUser({ ...values, context }) : mode === "update" ? await api.updateUser(values.userId, { ...values, confirm: values.confirm === "Y", context }) : await api.deleteUser(values.userId, { confirm: values.confirm === "Y", context });
    setMessage(response.message);
  }
  const map = mode === "list" ? MAPS.COUSR00 : mode === "add" ? MAPS.COUSR01 : mode === "update" ? MAPS.COUSR02 : MAPS.COUSR03;
  return <ScreenState map={map} message={message} context={context} onSubmit={mode === "list" ? undefined : submit} onPageUp={mode === "list" ? () => setPage(Math.max(page - 1, 0)) : undefined} onPageDown={mode === "list" ? () => setPage(page + 1) : undefined}><MapFields map={map} values={values} setValue={set} exclude={["message"]} />{mode === "list" && <RecordTable rows={rows} />}</ScreenState>;
}

function ExtensionScreen({
  update = false,
  transactionTypes = false,
}: {
  update?: boolean;
  transactionTypes?: boolean;
}) {
  const map = transactionTypes
    ? update
      ? MAPS.COTRTUP
      : MAPS.COTRTLI
    : update
      ? MAPS.COPAU01
      : MAPS.COPAU00;
  const [values, setValues] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const set = (name: string, value: string) => setValues((old) => ({ ...old, [name]: value }));
  return <ScreenState map={map} message={message} onSubmit={() => setMessage(update ? "Transaction type update ready..." : "Enter an account to display authorizations...")}><MapFields map={map} values={values} setValue={set} exclude={["message"]} /></ScreenState>;
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
