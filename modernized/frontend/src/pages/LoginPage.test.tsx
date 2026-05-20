import { render } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import { AuthProvider } from '../contexts/AuthContext';
import LoginPage from './LoginPage';

const renderWithProviders = (ui: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  );
};

describe('LoginPage', () => {
  it('renders login form', () => {
    const { getByLabelText, getByRole } = renderWithProviders(<LoginPage />);
    expect(getByLabelText(/user id/i)).toBeTruthy();
    expect(getByLabelText(/password/i)).toBeTruthy();
    expect(getByRole('button', { name: /sign in/i })).toBeTruthy();
  });

  it('renders title', () => {
    const { getByText } = renderWithProviders(<LoginPage />);
    expect(getByText(/carddemo login/i)).toBeTruthy();
  });
});
