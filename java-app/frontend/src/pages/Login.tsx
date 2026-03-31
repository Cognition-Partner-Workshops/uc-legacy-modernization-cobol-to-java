import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Avatar,
  Container, CircularProgress,
} from '@mui/material';
import { LockOutlined } from '@mui/icons-material';

const Login: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/app');
    } catch (err: any) {
      setError(err.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ mt: 12, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Avatar sx={{ m: 1, bgcolor: 'primary.main', width: 56, height: 56 }}>
          <LockOutlined fontSize="large" />
        </Avatar>
        <Typography variant="h4" sx={{ mb: 1, fontWeight: 700 }}>CardDemo</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Credit Card Management System
        </Typography>
        <Card sx={{ width: '100%', maxWidth: 420 }}>
          <CardContent sx={{ p: 4 }}>
            <Typography variant="h5" align="center" sx={{ mb: 3 }}>Sign In</Typography>
            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
            <Box component="form" onSubmit={handleSubmit}>
              <TextField fullWidth label="User ID" value={username}
                onChange={(e) => setUsername(e.target.value)} margin="normal" autoFocus required />
              <TextField fullWidth label="Password" type="password" value={password}
                onChange={(e) => setPassword(e.target.value)} margin="normal" required />
              <Button type="submit" fullWidth variant="contained" size="large"
                disabled={loading} sx={{ mt: 3, mb: 2, py: 1.5 }}>
                {loading ? <CircularProgress size={24} /> : 'Sign In'}
              </Button>
            </Box>
            <Typography variant="caption" color="text.secondary" align="center" display="block">
              Default: ADMIN001 / PASSWORD or USER0001 / PASSWORD
            </Typography>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default Login;
