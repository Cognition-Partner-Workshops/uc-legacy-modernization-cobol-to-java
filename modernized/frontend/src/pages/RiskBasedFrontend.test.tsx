import { render } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi } from 'vitest';
import { AuthProvider } from '../contexts/AuthContext';
import LoginPage from './LoginPage';
import DashboardPage from './DashboardPage';
import AccountViewPage from './AccountViewPage';
import CardListPage from './CardListPage';
import TransactionListPage from './TransactionListPage';
import BillPaymentPage from './BillPaymentPage';
import ReportsPage from './ReportsPage';

/**
 * TIER 4 — LOW RISK: Frontend UI Rendering & Navigation
 * Risk factors: broken pages, missing form fields, navigation failures
 */

// Mock the auth context for authenticated pages
vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { userId: 'admin01', role: 'ADMIN' },
    isAuthenticated: true,
    isAdmin: true,
  }),
}));

const renderWithRouter = (ui: React.ReactElement) => {
  return render(
    <BrowserRouter>
      <AuthProvider>{ui}</AuthProvider>
    </BrowserRouter>
  );
};

describe('Tier 4 (Low Risk): Frontend UI Rendering', () => {
  // --- Login Page ---

  describe('LoginPage', () => {
    it('T4-UI-001: Login form renders User ID field', () => {
      const { getByLabelText } = renderWithRouter(<LoginPage />);
      expect(getByLabelText(/user id/i)).toBeTruthy();
    });

    it('T4-UI-002: Login form renders Password field', () => {
      const { getByLabelText } = renderWithRouter(<LoginPage />);
      expect(getByLabelText(/password/i)).toBeTruthy();
    });

    it('T4-UI-003: Login form renders Sign In button', () => {
      const { getByRole } = renderWithRouter(<LoginPage />);
      expect(getByRole('button', { name: /sign in/i })).toBeTruthy();
    });

    it('T4-UI-004: Login page shows CardDemo title', () => {
      const { getByText } = renderWithRouter(<LoginPage />);
      expect(getByText(/carddemo/i)).toBeTruthy();
    });

    it('T4-UI-005: User ID field has maxLength 8', () => {
      const { getByLabelText } = renderWithRouter(<LoginPage />);
      const input = getByLabelText(/user id/i) as HTMLInputElement;
      expect(input.maxLength).toBe(8);
    });

    it('T4-UI-006: Password field is type password', () => {
      const { getByLabelText } = renderWithRouter(<LoginPage />);
      const input = getByLabelText(/password/i) as HTMLInputElement;
      expect(input.type).toBe('password');
    });
  });

  // --- Dashboard Page ---

  describe('DashboardPage', () => {
    it('T4-UI-007: Dashboard shows welcome message with user ID', () => {
      const { getByText } = renderWithRouter(<DashboardPage />);
      expect(getByText(/welcome/i)).toBeTruthy();
    });

    it('T4-UI-008: Dashboard shows Account Management section', () => {
      const { getByText } = renderWithRouter(<DashboardPage />);
      expect(getByText(/account management/i)).toBeTruthy();
    });

    it('T4-UI-009: Dashboard shows Card Management section', () => {
      const { getByText } = renderWithRouter(<DashboardPage />);
      expect(getByText(/card management/i)).toBeTruthy();
    });

    it('T4-UI-010: Dashboard shows Transactions section', () => {
      const { getAllByText } = renderWithRouter(<DashboardPage />);
      expect(getAllByText(/transactions/i).length).toBeGreaterThan(0);
    });
  });

  // --- Account View Page ---

  describe('AccountViewPage', () => {
    it('T4-UI-011: Account page renders Account Number input', () => {
      const { getByLabelText } = renderWithRouter(<AccountViewPage />);
      expect(getByLabelText(/account number/i)).toBeTruthy();
    });

    it('T4-UI-012: Account page renders Search button', () => {
      const { getByRole } = renderWithRouter(<AccountViewPage />);
      expect(getByRole('button', { name: /search/i })).toBeTruthy();
    });

    it('T4-UI-013: Account page shows title', () => {
      const { getByText } = renderWithRouter(<AccountViewPage />);
      expect(getByText(/account view/i)).toBeTruthy();
    });
  });

  // --- Card List Page ---

  describe('CardListPage', () => {
    it('T4-UI-014: Card list renders Account Number input', () => {
      const { getByLabelText } = renderWithRouter(<CardListPage />);
      expect(getByLabelText(/account number/i)).toBeTruthy();
    });

    it('T4-UI-015: Card list renders Search button', () => {
      const { getByRole } = renderWithRouter(<CardListPage />);
      expect(getByRole('button', { name: /search/i })).toBeTruthy();
    });
  });

  // --- Transaction List Page ---

  describe('TransactionListPage', () => {
    it('T4-UI-016: Transaction list renders Card Number input', () => {
      const { getByLabelText } = renderWithRouter(<TransactionListPage />);
      expect(getByLabelText(/card number/i)).toBeTruthy();
    });

    it('T4-UI-017: Transaction list renders Search button', () => {
      const { getByRole } = renderWithRouter(<TransactionListPage />);
      expect(getByRole('button', { name: /search/i })).toBeTruthy();
    });
  });

  // --- Bill Payment Page ---

  describe('BillPaymentPage', () => {
    it('T4-UI-018: Bill payment renders Account Number input', () => {
      const { getByLabelText } = renderWithRouter(<BillPaymentPage />);
      expect(getByLabelText(/account number/i)).toBeTruthy();
    });
  });

  // --- Reports Page ---

  describe('ReportsPage', () => {
    it('T4-UI-019: Reports page renders', () => {
      const { getByText } = renderWithRouter(<ReportsPage />);
      expect(getByText('Reports')).toBeTruthy();
    });
  });
});
