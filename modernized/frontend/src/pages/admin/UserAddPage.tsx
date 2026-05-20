import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, Alert, MenuItem, Select, FormControl, InputLabel } from '@mui/material';
import api from '../../api/authApi';

const UserAddPage: React.FC = () => {
  const navigate = useNavigate();
  const [userId, setUserId] = useState('');
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('USER');
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    try {
      await api.post('/users', { userId, firstName, lastName, password, role });
      navigate('/admin/users');
    } catch {
      setError('Failed to create user');
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Add User</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" gap={2} maxWidth={400}>
          <TextField label="User ID" value={userId} onChange={(e) => setUserId(e.target.value)}
            inputProps={{ maxLength: 8 }} required />
          <TextField label="First Name" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
          <TextField label="Last Name" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
          <TextField label="Password" type="password" value={password}
            onChange={(e) => setPassword(e.target.value)} inputProps={{ maxLength: 8 }} required />
          <FormControl>
            <InputLabel>Role</InputLabel>
            <Select value={role} label="Role" onChange={(e) => setRole(e.target.value)}>
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </Select>
          </FormControl>
          <Box display="flex" gap={2}>
            <Button variant="contained" onClick={handleSubmit}>Create</Button>
            <Button variant="outlined" onClick={() => navigate('/admin/users')}>Cancel</Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default UserAddPage;
