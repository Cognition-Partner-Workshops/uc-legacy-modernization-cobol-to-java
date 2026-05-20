import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import AccountViewPage from './pages/AccountViewPage';
import AccountEditPage from './pages/AccountEditPage';
import CardListPage from './pages/CardListPage';
import CardDetailPage from './pages/CardDetailPage';
import CardEditPage from './pages/CardEditPage';
import TransactionListPage from './pages/TransactionListPage';
import TransactionViewPage from './pages/TransactionViewPage';
import TransactionAddPage from './pages/TransactionAddPage';
import BillPaymentPage from './pages/BillPaymentPage';
import ReportsPage from './pages/ReportsPage';
import StatementListPage from './pages/StatementListPage';
import UserListPage from './pages/admin/UserListPage';
import UserAddPage from './pages/admin/UserAddPage';
import UserEditPage from './pages/admin/UserEditPage';

const App: React.FC = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/accounts/search" element={<AccountViewPage />} />
          <Route path="/accounts/:id" element={<AccountViewPage />} />
          <Route path="/accounts/:id/edit" element={<AccountEditPage />} />
          <Route path="/cards" element={<CardListPage />} />
          <Route path="/cards/:num" element={<CardDetailPage />} />
          <Route path="/cards/:num/edit" element={<CardEditPage />} />
          <Route path="/transactions" element={<TransactionListPage />} />
          <Route path="/transactions/new" element={<TransactionAddPage />} />
          <Route path="/transactions/:id" element={<TransactionViewPage />} />
          <Route path="/bill-payment" element={<BillPaymentPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/statements" element={<StatementListPage />} />
          <Route path="/admin/users" element={<AdminRoute><UserListPage /></AdminRoute>} />
          <Route path="/admin/users/new" element={<AdminRoute><UserAddPage /></AdminRoute>} />
          <Route path="/admin/users/:id/edit" element={<AdminRoute><UserEditPage /></AdminRoute>} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

export default App;
