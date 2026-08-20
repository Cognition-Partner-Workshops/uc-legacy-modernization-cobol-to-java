import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { api, Context, getToken, restoreToken, Role, setToken } from "./api";

type AuthState = {
  token: string | null;
  userId: string | null;
  role: Role | null;
  context: Context | null;
  signon: (userId: string, password: string) => Promise<string>;
  signout: () => void;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setCurrentToken] = useState<string | null>(() => restoreToken());
  const restoredClaims = claims(token);
  const [userId, setUserId] = useState<string | null>(restoredClaims?.userId ?? null);
  const [role, setRole] = useState<Role | null>(restoredClaims?.role ?? null);
  const [context, setContext] = useState<Context | null>(null);

  useEffect(() => {
    if (getToken()) setCurrentToken(getToken());
  }, []);

  async function signon(user: string, password: string): Promise<string> {
    const response = await api.signon({ userId: user, password });
    if (response.data) {
      setToken(response.data.token);
      setCurrentToken(response.data.token);
      setUserId(response.data.userId);
      setRole(response.data.role);
      setContext(response.context);
    }
    return response.message;
  }

  function signout() {
    setToken(null);
    setCurrentToken(null);
    setUserId(null);
    setRole(null);
    setContext(null);
  }

  const value = useMemo(
    () => ({ token, userId, role, context, signon, signout }),
    [token, userId, role, context],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

function claims(value: string | null): { userId: string; role: Role } | null {
  if (!value) return null;
  try {
    const encoded = value.split(".")[1].replaceAll("-", "+").replaceAll("_", "/");
    const payload = JSON.parse(atob(encoded.padEnd(Math.ceil(encoded.length / 4) * 4, "="))) as { sub?: string; role?: Role };
    return payload.sub && (payload.role === "ROLE_ADMIN" || payload.role === "ROLE_USER")
      ? { userId: payload.sub, role: payload.role }
      : null;
  } catch {
    return null;
  }
}

export function useAuth(): AuthState {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used inside AuthProvider");
  return value;
}
