import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Context } from "./api";
import { FieldSpec, MapSpec } from "./maps";

const PROGRAM_ROUTES: Record<string, string> = {
  COSGN00C: "/signon", COSGN00: "/signon", COMEN01C: "/menu", COMEN01: "/menu",
  COADM01C: "/admin/menu", COADM01: "/admin/menu", COACTVWC: "/accounts/view",
  COACTUPC: "/accounts/update", COCRDLIC: "/cards", COCRDSLC: "/cards/detail",
  COCRDUPC: "/cards/update", COTRN00C: "/transactions", COTRN01C: "/transactions/view",
  COTRN02C: "/transactions/add", COBIL00C: "/bill-payment", CORPT00C: "/reports",
  COUSR00C: "/admin/users", COUSR01C: "/admin/users/add", COUSR02C: "/admin/users/update",
  COUSR03C: "/admin/users/delete",
};

export function routeForProgram(program?: string): string {
  return (program && PROGRAM_ROUTES[program]) || "/menu";
}

export function usePfKeys(
  context: Context | null | undefined,
  paging?: { up?: () => void; down?: () => void },
) {
  const navigate = useNavigate();
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key === "F3") {
        event.preventDefault();
        navigate(routeForProgram(context?.fromProgram));
      } else if (event.key === "F7" && paging?.up) {
        event.preventDefault();
        paging.up();
      } else if (event.key === "F8" && paging?.down) {
        event.preventDefault();
        paging.down();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [context, navigate, paging]);
}

export function BmsScreen({
  map,
  message,
  context,
  children,
  onSubmit,
  onBack,
  onPageUp,
  onPageDown,
}: {
  map: MapSpec;
  message: string;
  context?: Context | null;
  children: React.ReactNode;
  onSubmit?: () => void;
  onBack?: () => void;
  onPageUp?: () => void;
  onPageDown?: () => void;
}) {
  const navigate = useNavigate();
  usePfKeys(context, { up: onPageUp, down: onPageDown });
  return (
    <main className="terminal-shell">
      <header className="terminal-header">
        <span>{map.code}</span>
        <strong>{map.title}</strong>
        <span>{new Date().toISOString().slice(0, 10)}</span>
      </header>
      <div className="terminal-subheader">
        <span>CARDDEMO</span>
        <span>3270 ONLINE APPLICATION</span>
        <span>USER: {context?.userId || "SIGNON"}</span>
      </div>
      <form className="screen-body" onSubmit={(event) => { event.preventDefault(); onSubmit?.(); }}>
        {children}
        <div className="message-line" role="alert" aria-live="polite">{message}</div>
      </form>
      <nav className="pf-bar" aria-label="PF key controls">
        <button type="button" onClick={onSubmit} disabled={!onSubmit}>ENTER <kbd>Enter</kbd></button>
        <button type="button" onClick={onBack ?? (() => navigate(routeForProgram(context?.fromProgram)))}>PF3 <kbd>F3</kbd> Back</button>
        {onPageUp && <button type="button" onClick={onPageUp}>PF7 <kbd>F7</kbd> Page Up</button>}
        {onPageDown && <button type="button" onClick={onPageDown}>PF8 <kbd>F8</kbd> Page Down</button>}
      </nav>
    </main>
  );
}

export function MapField({
  spec,
  value,
  onChange,
}: {
  spec: FieldSpec;
  value: string;
  onChange: (value: string) => void;
}) {
  const screenValue =
    value ||
    (spec.name === "trnname" ? "CARD" :
      spec.name === "title01" ? "CARDDEMO" :
        spec.name === "title02" ? "CARD DEMONSTRATION APPLICATION" :
          spec.name === "pgmname" ? "CARDDEMO" :
            spec.name === "curdate" ? new Date().toISOString().slice(0, 10).replaceAll("-", "") :
              spec.name === "curtime" ? new Date().toTimeString().slice(0, 8) : "");
  return (
    <label className="map-field" style={{ gridRow: spec.row, gridColumn: `${Math.max(1, spec.col)} / span ${Math.min(spec.length + 1, 24)}` }}>
      <span>{spec.label}</span>
      <input
        aria-label={spec.label}
        name={spec.name}
        value={screenValue}
        maxLength={spec.length}
        readOnly={spec.protected}
        disabled={spec.protected}
        type={spec.password ? "password" : "text"}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

export function MapFields({
  map,
  values,
  setValue,
  exclude = [],
}: {
  map: MapSpec;
  values: Record<string, string>;
  setValue: (name: string, value: string) => void;
  exclude?: string[];
}) {
  return (
    <div className="map-grid">
      {map.fields.filter((field) => !exclude.includes(field.name)).map((field) => (
        <MapField key={`${map.code}-${field.name}`} spec={field} value={values[field.name] ?? ""} onChange={(value) => setValue(field.name, value)} />
      ))}
    </div>
  );
}

export function RecordTable({ rows }: { rows: Record<string, unknown>[] }) {
  if (!rows.length) return <p className="empty-state">No records found.</p>;
  const columns = Object.keys(rows[0]).filter((key) => !key.startsWith("_")).slice(0, 8);
  return (
    <div className="record-table">
      <table>
        <thead><tr>{columns.map((column) => <th key={column}>{column}</th>)}</tr></thead>
        <tbody>{rows.map((row, index) => <tr key={String(row.id ?? row.tranId ?? row.cardNum ?? index)}>{columns.map((column) => <td key={column}>{String(row[column] ?? "")}</td>)}</tr>)}</tbody>
      </table>
    </div>
  );
}
