import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, Alert, MenuItem, Select, FormControl, InputLabel } from '@mui/material';
import api from '../../api/authApi';

const UserEditPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [role, setRole] = useState('USER');
  const [error, setError] = useState('');

  useEffect(() => {
    if (id) {
      api.get(`/users/${id}`).then((res) => {
        setFirstName(res.data.firstName || '');
        setLastName(res.data.lastName || '');
        setRole(res.data.role || 'USER');
      });
    }
  }, [id]);

  const handleSubmit = async () => {
    try {
      await api.put(`/users/${id}`, { firstName, lastName, role });
      navigate('/admin/users');
    } catch {
      setError('Failed to update user');
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Edit User: {id}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" gap={2} maxWidth={400}>
          <TextField label="First Name" value={firstName} onChange={(e) => setFirstName(e.target.value)} />
          <TextField label="Last Name" value={lastName} onChange={(e) => setLastName(e.target.value)} />
          <FormControl>
            <InputLabel>Role</InputLabel>
            <Select value={role} label="Role" onChange={(e) => setRole(e.target.value)}>
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </Select>
          </FormControl>
          <Box display="flex" gap={2}>
            <Button variant="contained" onClick={handleSubmit}>Save</Button>
            <Button variant="outlined" onClick={() => navigate('/admin/users')}>Cancel</Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default UserEditPage;
