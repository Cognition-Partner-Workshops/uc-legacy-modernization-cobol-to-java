import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import CardListPage from './components/CardListPage';
import CardDetailPage from './components/CardDetailPage';
import CardUpdatePage from './components/CardUpdatePage';
import GreenScreenPreview from './components/GreenScreenPreview';

const navStyle = {
  background: '#1a237e',
  padding: '12px 24px',
  display: 'flex',
  alignItems: 'center',
  gap: '24px',
};

const linkStyle = {
  color: '#bbdefb',
  textDecoration: 'none',
  fontSize: '14px',
  fontWeight: 500,
};

const titleStyle = {
  color: '#fff',
  fontSize: '18px',
  fontWeight: 700,
  marginRight: 'auto',
};

const badgeStyle = {
  background: '#4caf50',
  color: '#fff',
  fontSize: '10px',
  padding: '2px 8px',
  borderRadius: '10px',
  marginLeft: '8px',
  fontWeight: 600,
};

function App() {
  return (
    <Router>
      <div>
        <nav style={navStyle}>
          <span style={titleStyle}>
            CardDemo
            <span style={badgeStyle}>MODERNIZED</span>
          </span>
          <Link to="/" style={linkStyle}>Card List</Link>
          <Link to="/green-screen" style={linkStyle}>Green Screen Preview</Link>
        </nav>
        <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
          <Routes>
            <Route path="/" element={<CardListPage />} />
            <Route path="/cards/:cardNumber" element={<CardDetailPage />} />
            <Route path="/cards/:cardNumber/edit" element={<CardUpdatePage />} />
            <Route path="/green-screen" element={<GreenScreenPreview />} />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
