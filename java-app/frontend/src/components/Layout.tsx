import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  AppBar, Box, CssBaseline, Divider, Drawer, IconButton, List, ListItem,
  ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography, Button, Chip,
} from '@mui/material';
import {
  Menu as MenuIcon, AccountBalance, CreditCard, Receipt, Payment,
  Assessment, People, PlayArrow, Logout, Dashboard,
} from '@mui/icons-material';

const DRAWER_WIDTH = 260;

const Layout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const menuItems = [
    { text: 'Dashboard', icon: <Dashboard />, path: '/app' },
    { text: 'Account View', icon: <AccountBalance />, path: '/app/accounts/view' },
    { text: 'Account Update', icon: <AccountBalance />, path: '/app/accounts/update' },
    { text: 'Credit Cards', icon: <CreditCard />, path: '/app/cards' },
    { text: 'Transactions', icon: <Receipt />, path: '/app/transactions' },
    { text: 'Add Transaction', icon: <Receipt />, path: '/app/transactions/add' },
    { text: 'Bill Payment', icon: <Payment />, path: '/app/payments' },
    { text: 'Reports', icon: <Assessment />, path: '/app/reports' },
  ];

  const adminItems = [
    { text: 'User Management', icon: <People />, path: '/app/admin/users' },
    { text: 'Batch Jobs', icon: <PlayArrow />, path: '/app/admin/batch' },
  ];

  const handleLogout = async () => {
    await logout();
    navigate('/app/login');
  };

  const drawer = (
    <Box>
      <Toolbar sx={{ justifyContent: 'center' }}>
        <Typography variant="h6" noWrap sx={{ fontWeight: 700, color: 'primary.main' }}>
          CardDemo
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => { navigate(item.path); setMobileOpen(false); }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: location.pathname === item.path ? 'primary.main' : 'inherit' }}>
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      {user?.isAdmin && (
        <>
          <Divider />
          <List subheader={
            <Typography variant="overline" sx={{ pl: 2, color: 'text.secondary' }}>Admin</Typography>
          }>
            {adminItems.map((item) => (
              <ListItem key={item.text} disablePadding>
                <ListItemButton
                  selected={location.pathname === item.path}
                  onClick={() => { navigate(item.path); setMobileOpen(false); }}
                >
                  <ListItemIcon sx={{ minWidth: 40, color: location.pathname === item.path ? 'primary.main' : 'inherit' }}>
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText primary={item.text} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </>
      )}
    </Box>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1, backgroundImage: 'none', backgroundColor: 'background.paper' }}>
        <Toolbar>
          <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(!mobileOpen)} sx={{ mr: 2, display: { md: 'none' } }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap sx={{ flexGrow: 1, fontWeight: 700 }}>
            CardDemo Application
          </Typography>
          <Chip label={user?.userId} color="primary" variant="outlined" size="small" sx={{ mr: 1 }} />
          {user?.isAdmin && <Chip label="ADMIN" color="secondary" size="small" sx={{ mr: 2 }} />}
          <Button color="inherit" startIcon={<Logout />} onClick={handleLogout}>
            Sign Off
          </Button>
        </Toolbar>
      </AppBar>
      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer variant="temporary" open={mobileOpen} onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', md: 'none' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH } }}>
          {drawer}
        </Drawer>
        <Drawer variant="permanent"
          sx={{ display: { xs: 'none', md: 'block' }, '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' } }}
          open>
          {drawer}
        </Drawer>
      </Box>
      <Box component="main" sx={{ flexGrow: 1, p: 3, width: { md: `calc(100% - ${DRAWER_WIDTH}px)` } }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
};

export default Layout;
