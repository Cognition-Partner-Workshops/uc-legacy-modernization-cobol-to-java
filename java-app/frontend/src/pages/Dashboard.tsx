import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box, Card, CardActionArea, CardContent, Grid, Typography,
} from '@mui/material';
import {
  AccountBalance, CreditCard, Receipt, Payment, Assessment, People, PlayArrow,
} from '@mui/icons-material';

const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const items = [
    { title: 'Account View', desc: 'Look up account details', icon: <AccountBalance sx={{ fontSize: 48 }} />, path: '/app/accounts/view', color: '#42a5f5' },
    { title: 'Account Update', desc: 'Modify account settings', icon: <AccountBalance sx={{ fontSize: 48 }} />, path: '/app/accounts/update', color: '#26c6da' },
    { title: 'Credit Cards', desc: 'View and manage cards', icon: <CreditCard sx={{ fontSize: 48 }} />, path: '/app/cards', color: '#ab47bc' },
    { title: 'Transactions', desc: 'Browse transaction history', icon: <Receipt sx={{ fontSize: 48 }} />, path: '/app/transactions', color: '#ef5350' },
    { title: 'Add Transaction', desc: 'Record a new transaction', icon: <Receipt sx={{ fontSize: 48 }} />, path: '/app/transactions/add', color: '#ff7043' },
    { title: 'Bill Payment', desc: 'Make a bill payment', icon: <Payment sx={{ fontSize: 48 }} />, path: '/app/payments', color: '#66bb6a' },
    { title: 'Reports', desc: 'Generate transaction reports', icon: <Assessment sx={{ fontSize: 48 }} />, path: '/app/reports', color: '#ffa726' },
  ];

  const adminItems = [
    { title: 'User Management', desc: 'Manage system users', icon: <People sx={{ fontSize: 48 }} />, path: '/app/admin/users', color: '#5c6bc0' },
    { title: 'Batch Jobs', desc: 'Run batch processes', icon: <PlayArrow sx={{ fontSize: 48 }} />, path: '/app/admin/batch', color: '#78909c' },
  ];

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 1 }}>Welcome, {user?.userId}</Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        CardDemo Credit Card Management System
      </Typography>
      <Grid container spacing={3}>
        {items.map((item) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={item.title}>
            <Card sx={{ height: '100%', '&:hover': { transform: 'translateY(-4px)', transition: '0.2s' } }}>
              <CardActionArea onClick={() => navigate(item.path)} sx={{ height: '100%', p: 2 }}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Box sx={{ color: item.color, mb: 2 }}>{item.icon}</Box>
                  <Typography variant="h6">{item.title}</Typography>
                  <Typography variant="body2" color="text.secondary">{item.desc}</Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
        {user?.isAdmin && adminItems.map((item) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={item.title}>
            <Card sx={{ height: '100%', border: '1px solid', borderColor: 'secondary.dark', '&:hover': { transform: 'translateY(-4px)', transition: '0.2s' } }}>
              <CardActionArea onClick={() => navigate(item.path)} sx={{ height: '100%', p: 2 }}>
                <CardContent sx={{ textAlign: 'center' }}>
                  <Box sx={{ color: item.color, mb: 2 }}>{item.icon}</Box>
                  <Typography variant="h6">{item.title}</Typography>
                  <Typography variant="body2" color="text.secondary">{item.desc}</Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default Dashboard;
