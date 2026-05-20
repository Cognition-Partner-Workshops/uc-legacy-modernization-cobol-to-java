import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Button, Typography, Paper, Table, TableHead, TableRow,
  TableCell, TableBody, IconButton,
} from '@mui/material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import api from '../../api/authApi';

interface User {
  userId: string;
  firstName: string;
  lastName: string;
  role: string;
}

const UserListPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const navigate = useNavigate();

  const loadUsers = async () => {
    try {
      const response = await api.get<{ content: User[] }>('/users');
      setUsers(response.data.content || []);
    } catch {
      setUsers([]);
    }
  };

  useEffect(() => { loadUsers(); }, []);

  const handleDelete = async (userId: string) => {
    if (window.confirm(`Delete user ${userId}?`)) {
      await api.delete(`/users/${userId}`);
      loadUsers();
    }
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" mb={2}>
        <Typography variant="h5">User Management</Typography>
        <Button variant="contained" onClick={() => navigate('/admin/users/new')}>Add User</Button>
      </Box>
      <Paper>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>User ID</TableCell>
              <TableCell>First Name</TableCell>
              <TableCell>Last Name</TableCell>
              <TableCell>Role</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((u) => (
              <TableRow key={u.userId}>
                <TableCell>{u.userId}</TableCell>
                <TableCell>{u.firstName}</TableCell>
                <TableCell>{u.lastName}</TableCell>
                <TableCell>{u.role}</TableCell>
                <TableCell>
                  <Button size="small" onClick={() => navigate(`/admin/users/${u.userId}/edit`)}>Edit</Button>
                  <IconButton size="small" onClick={() => handleDelete(u.userId)}>
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
};

export default UserListPage;
