import { useState } from 'react';
import type { FormEvent } from 'react';
import { authApi } from '../../api/client';
import type { LoginResponse } from '../../types';

interface LoginProps {
  onLogin: (user: LoginResponse) => void;
}

export default function Login({ onLogin }: LoginProps) {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    if (!userId.trim() || !password.trim()) {
      setError('Please enter both User ID and Password');
      return;
    }
    setLoading(true);
    try {
      const res = await authApi.login(userId.trim(), password.trim());
      onLogin(res.data);
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: { error?: string } } };
        setError(axiosErr.response?.data?.error || 'Login failed');
      } else {
        setError('Connection failed. Is the API Gateway running?');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>CardDemo</h1>
        <p className="subtitle">Microservices Platform - Sign In</p>
        {error && <div className="alert alert-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>User ID</label>
            <input
              type="text"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              placeholder="e.g. ADMIN001"
              autoFocus
            />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
            />
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>
        <div style={{ marginTop: 16, fontSize: 12, color: '#5f6368', textAlign: 'center' }}>
          Test: ADMIN001 / ADMIN001 or USER0001 / USER0001
        </div>
      </div>
    </div>
  );
}
