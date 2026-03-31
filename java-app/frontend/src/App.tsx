import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline } from '@mui/material';
import theme from './theme';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AccountView from './pages/AccountView';
import AccountUpdate from './pages/AccountUpdate';
import CardList from './pages/CardList';
import CardDetail from './pages/CardDetail';
import CardEdit from './pages/CardEdit';
import TransactionList from './pages/TransactionList';
import TransactionDetail from './pages/TransactionDetail';
import TransactionAdd from './pages/TransactionAdd';
import BillPayment from './pages/BillPayment';
import Reports from './pages/Reports';
import UserList from './pages/UserList';
import UserForm from './pages/UserForm';
import BatchJobs from './pages/BatchJobs';
import { CircularProgress, Box } from '@mui/material';

const ProtectedRoute: React.FC<{ children: React.ReactNode; adminOnly?: boolean }> = ({ children, adminOnly }) => {
  const { user, loading } = useAuth();
  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}><CircularProgress /></Box>;
  if (!user) return <Navigate to="/app/login" replace />;
  if (adminOnly && !user.isAdmin) return <Navigate to="/app" replace />;
  return <>{children}</>;
};

const AppRoutes: React.FC = () => {
  const { user, loading } = useAuth();

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}><CircularProgress /></Box>;
  }

  return (
    <Routes>
      <Route path="/app/login" element={user ? <Navigate to="/app" replace /> : <Login />} />
      <Route path="/app" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="accounts/view" element={<AccountView />} />
        <Route path="accounts/update" element={<AccountUpdate />} />
        <Route path="cards" element={<CardList />} />
        <Route path="cards/:cardNum" element={<CardDetail />} />
        <Route path="cards/:cardNum/edit" element={<CardEdit />} />
        <Route path="transactions" element={<TransactionList />} />
        <Route path="transactions/add" element={<TransactionAdd />} />
        <Route path="transactions/:tranId" element={<TransactionDetail />} />
        <Route path="payments" element={<BillPayment />} />
        <Route path="reports" element={<Reports />} />
        <Route path="admin/users" element={<ProtectedRoute adminOnly><UserList /></ProtectedRoute>} />
        <Route path="admin/users/add" element={<ProtectedRoute adminOnly><UserForm /></ProtectedRoute>} />
        <Route path="admin/users/:userId/edit" element={<ProtectedRoute adminOnly><UserForm /></ProtectedRoute>} />
        <Route path="admin/batch" element={<ProtectedRoute adminOnly><BatchJobs /></ProtectedRoute>} />
      </Route>
      <Route path="*" element={<Navigate to="/app" replace />} />
    </Routes>
  );
};

const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ThemeProvider>
  );
};

export default App;
