import { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import type { LoginResponse } from './types';
import Layout from './components/Layout';
import Login from './pages/auth/Login';
import Dashboard from './pages/Dashboard';
import AccountList from './pages/accounts/AccountList';
import AccountView from './pages/accounts/AccountView';
import AccountUpdate from './pages/accounts/AccountUpdate';
import CardList from './pages/cards/CardList';
import CardView from './pages/cards/CardView';
import CardUpdate from './pages/cards/CardUpdate';
import CustomerList from './pages/customers/CustomerList';
import CustomerView from './pages/customers/CustomerView';
import CustomerUpdate from './pages/customers/CustomerUpdate';
import CustomerDelete from './pages/customers/CustomerDelete';
import TransactionList from './pages/transactions/TransactionList';
import TransactionView from './pages/transactions/TransactionView';
import TransactionCreate from './pages/transactions/TransactionCreate';

function App() {
  const [user, setUser] = useState<LoginResponse | null>(() => {
    const saved = sessionStorage.getItem('carddemo_user');
    return saved ? JSON.parse(saved) : null;
  });

  const handleLogin = (loginUser: LoginResponse) => {
    setUser(loginUser);
    sessionStorage.setItem('carddemo_user', JSON.stringify(loginUser));
  };

  const handleLogout = () => {
    setUser(null);
    sessionStorage.removeItem('carddemo_user');
  };

  if (!user) {
    return (
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login onLogin={handleLogin} />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    );
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Navigate to="/" replace />} />
        <Route element={<Layout user={{ userId: user.userId, userType: user.userType }} onLogout={handleLogout} />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/accounts" element={<AccountList />} />
          <Route path="/accounts/:id" element={<AccountView />} />
          <Route path="/accounts/:id/edit" element={<AccountUpdate />} />
          <Route path="/cards" element={<CardList />} />
          <Route path="/cards/:cardNum" element={<CardView />} />
          <Route path="/cards/:cardNum/edit" element={<CardUpdate />} />
          <Route path="/customers" element={<CustomerList />} />
          <Route path="/customers/:id" element={<CustomerView />} />
          <Route path="/customers/:id/edit" element={<CustomerUpdate />} />
          <Route path="/customers/:id/delete" element={<CustomerDelete />} />
          <Route path="/transactions" element={<TransactionList />} />
          <Route path="/transactions/new" element={<TransactionCreate />} />
          <Route path="/transactions/:id" element={<TransactionView />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
