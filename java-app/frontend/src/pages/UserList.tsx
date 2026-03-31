import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, Typography, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Chip, IconButton, Button,
  CircularProgress, Dialog, DialogTitle, DialogContent, DialogActions,
} from '@mui/material';
import { Edit, Delete, PersonAdd } from '@mui/icons-material';

const UserList: React.FC = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState<any[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const load = () => {
    setLoading(true);
    api.getUsers().then(setUsers).catch((e) => setError(e.message)).finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleDelete = async () => {
    if (!deleteId) return;
    setDeleting(true); setError('');
    try {
      await api.deleteUser(deleteId);
      setSuccess(`User ${deleteId} deleted successfully.`);
      setDeleteId(null);
      load();
    } catch (err: any) { setError(err.message); }
    finally { setDeleting(false); }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3, alignItems: 'center' }}>
        <Typography variant="h5">User Management</Typography>
        <Button variant="contained" startIcon={<PersonAdd />} onClick={() => navigate('/app/admin/users/add')}>
          Add User
        </Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccess('')}>{success}</Alert>}
      {loading ? <CircularProgress /> : (
        <Card>
          <CardContent>
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>User ID</TableCell><TableCell>First Name</TableCell>
                    <TableCell>Last Name</TableCell><TableCell>Type</TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {users.map((u) => (
                    <TableRow key={u.userId} hover>
                      <TableCell sx={{ fontWeight: 600 }}>{u.userId}</TableCell>
                      <TableCell>{u.firstName}</TableCell>
                      <TableCell>{u.lastName}</TableCell>
                      <TableCell><Chip label={u.userType === 'A' ? 'Admin' : 'User'} color={u.userType === 'A' ? 'secondary' : 'primary'} size="small" /></TableCell>
                      <TableCell align="center">
                        <IconButton size="small" onClick={() => navigate(`/app/admin/users/${u.userId}/edit`)} title="Edit"><Edit fontSize="small" /></IconButton>
                        <IconButton size="small" color="error" onClick={() => setDeleteId(u.userId)} title="Delete"><Delete fontSize="small" /></IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}
      <Dialog open={!!deleteId} onClose={() => setDeleteId(null)}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>Are you sure you want to delete user <strong>{deleteId}</strong>?</DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteId(null)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleDelete} disabled={deleting}>
            {deleting ? <CircularProgress size={20} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default UserList;
