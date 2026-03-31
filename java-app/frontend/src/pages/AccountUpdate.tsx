import React, { useState } from 'react';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  CircularProgress, MenuItem,
} from '@mui/material';
import { Search, Save } from '@mui/icons-material';

const AccountUpdate: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [account, setAccount] = useState<any>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ activeStatus: '', creditLimit: '', cashCreditLimit: '', expirationDate: '', reissueDate: '', groupId: '' });

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess(''); setAccount(null); setLoading(true);
    try {
      const result = await api.getAccount(Number(acctId));
      const a = result.account;
      setAccount(a);
      setForm({
        activeStatus: a.activeStatus || '', creditLimit: String(a.creditLimit || ''),
        cashCreditLimit: String(a.cashCreditLimit || ''), expirationDate: a.expirationDate || '',
        reissueDate: a.reissueDate || '', groupId: a.groupId || '',
      });
    } catch (err: any) { setError(err.message); }
    finally { setLoading(false); }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess(''); setSaving(true);
    try {
      await api.updateAccount(Number(acctId), {
        activeStatus: form.activeStatus, creditLimit: Number(form.creditLimit),
        cashCreditLimit: Number(form.cashCreditLimit), expirationDate: form.expirationDate,
        reissueDate: form.reissueDate, groupId: form.groupId,
      });
      setSuccess('Account updated successfully.');
    } catch (err: any) { setError(err.message); }
    finally { setSaving(false); }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Account Update</Typography>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box component="form" onSubmit={handleSearch} sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <TextField label="Account ID" value={acctId} onChange={(e) => setAcctId(e.target.value)} size="small" sx={{ width: 250 }} />
            <Button type="submit" variant="contained" startIcon={<Search />} disabled={loading}>
              {loading ? <CircularProgress size={20} /> : 'Search'}
            </Button>
          </Box>
        </CardContent>
      </Card>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      {account && (
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>Edit Account {String(account.acctId).padStart(11, '0')}</Typography>
            <Box component="form" onSubmit={handleSave}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField select fullWidth label="Status" value={form.activeStatus} onChange={(e) => setForm({ ...form, activeStatus: e.target.value })}>
                    <MenuItem value="Y">Active</MenuItem><MenuItem value="N">Inactive</MenuItem>
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Credit Limit" type="number" value={form.creditLimit} onChange={(e) => setForm({ ...form, creditLimit: e.target.value })} /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Cash Credit Limit" type="number" value={form.cashCreditLimit} onChange={(e) => setForm({ ...form, cashCreditLimit: e.target.value })} /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Expiration Date" value={form.expirationDate} onChange={(e) => setForm({ ...form, expirationDate: e.target.value })} placeholder="YYYY-MM-DD" /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Reissue Date" value={form.reissueDate} onChange={(e) => setForm({ ...form, reissueDate: e.target.value })} placeholder="YYYY-MM-DD" /></Grid>
                <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Group ID" value={form.groupId} onChange={(e) => setForm({ ...form, groupId: e.target.value })} /></Grid>
              </Grid>
              <Button type="submit" variant="contained" startIcon={<Save />} disabled={saving} sx={{ mt: 3 }}>
                {saving ? <CircularProgress size={20} /> : 'Save Changes'}
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default AccountUpdate;
