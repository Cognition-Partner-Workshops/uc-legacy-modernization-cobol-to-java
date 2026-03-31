import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  CircularProgress, MenuItem,
} from '@mui/material';
import { ArrowBack, Save } from '@mui/icons-material';

const UserForm: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const isEdit = !!userId;
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ userId: '', password: '', firstName: '', lastName: '', userType: 'U' });

  useEffect(() => {
    if (userId) {
      api.getUser(userId).then((u) => {
        setForm({ userId: u.userId, password: '', firstName: u.firstName || '', lastName: u.lastName || '', userType: u.userType || 'U' });
      }).catch((e) => setError(e.message)).finally(() => setLoading(false));
    }
  }, [userId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess(''); setSaving(true);
    try {
      if (isEdit) {
        await api.updateUser(userId!, form);
        setSuccess('User updated successfully.');
      } else {
        await api.createUser(form);
        setSuccess('User created successfully.');
        setForm({ userId: '', password: '', firstName: '', lastName: '', userType: 'U' });
      }
    } catch (err: any) { setError(err.message); }
    finally { setSaving(false); }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/app/admin/users')}>Back</Button>
        <Typography variant="h5">{isEdit ? 'Edit User' : 'Add User'}</Typography>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Card sx={{ maxWidth: 600 }}>
        <CardContent>
          <Box component="form" onSubmit={handleSubmit}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth required label="User ID" value={form.userId}
                  onChange={(e) => setForm({ ...form, userId: e.target.value })}
                  disabled={isEdit} inputProps={{ maxLength: 8 }} />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth label={isEdit ? 'New Password' : 'Password'} type="password"
                  value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required={!isEdit} inputProps={{ maxLength: 8 }}
                  helperText={isEdit ? 'Leave blank to keep current' : ''} />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth required label="First Name" value={form.firstName}
                  onChange={(e) => setForm({ ...form, firstName: e.target.value })} inputProps={{ maxLength: 20 }} />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField fullWidth required label="Last Name" value={form.lastName}
                  onChange={(e) => setForm({ ...form, lastName: e.target.value })} inputProps={{ maxLength: 20 }} />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField select fullWidth label="User Type" value={form.userType}
                  onChange={(e) => setForm({ ...form, userType: e.target.value })}>
                  <MenuItem value="U">User</MenuItem>
                  <MenuItem value="A">Admin</MenuItem>
                </TextField>
              </Grid>
            </Grid>
            <Button type="submit" variant="contained" startIcon={<Save />} disabled={saving} sx={{ mt: 3 }}>
              {saving ? <CircularProgress size={20} /> : isEdit ? 'Update User' : 'Create User'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default UserForm;
