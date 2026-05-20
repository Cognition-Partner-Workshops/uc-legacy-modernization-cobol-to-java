import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, TextField, Button, Typography, Paper, MenuItem, Select,
  FormControl, InputLabel, Alert, Dialog, DialogTitle, DialogActions,
} from '@mui/material';
import { accountApi, Account, UpdateAccountRequest } from '../api/accountApi';

const AccountEditPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [account, setAccount] = useState<Account | null>(null);
  const [form, setForm] = useState<UpdateAccountRequest>({ version: 0 });
  const [error, setError] = useState('');
  const [confirmOpen, setConfirmOpen] = useState(false);

  useEffect(() => {
    if (id) {
      accountApi.getAccount(id).then((data) => {
        setAccount(data);
        setForm({
          activeStatus: data.activeStatus,
          creditLimit: data.creditLimit,
          cashCreditLimit: data.cashCreditLimit,
          openDate: data.openDate,
          expirationDate: data.expirationDate,
          reissueDate: data.reissueDate,
          groupId: data.groupId,
          addrZip: data.addrZip,
          version: data.version,
        });
      });
    }
  }, [id]);

  const handleSave = async () => {
    setConfirmOpen(false);
    try {
      await accountApi.updateAccount(id!, form);
      navigate(`/accounts/search`);
    } catch {
      setError('Failed to update account. It may have been modified by another user.');
    }
  };

  if (!account) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Edit Account: {id}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" gap={2} maxWidth={400}>
          <FormControl>
            <InputLabel>Active Status</InputLabel>
            <Select
              value={form.activeStatus || ''}
              label="Active Status"
              onChange={(e) => setForm({ ...form, activeStatus: e.target.value })}
            >
              <MenuItem value="Y">Active (Y)</MenuItem>
              <MenuItem value="N">Inactive (N)</MenuItem>
            </Select>
          </FormControl>
          <TextField label="Credit Limit" type="number" value={form.creditLimit ?? ''}
            onChange={(e) => setForm({ ...form, creditLimit: parseFloat(e.target.value) })} />
          <TextField label="Cash Credit Limit" type="number" value={form.cashCreditLimit ?? ''}
            onChange={(e) => setForm({ ...form, cashCreditLimit: parseFloat(e.target.value) })} />
          <TextField label="Open Date" value={form.openDate ?? ''}
            onChange={(e) => setForm({ ...form, openDate: e.target.value })} placeholder="YYYY-MM-DD" />
          <TextField label="Expiration Date" value={form.expirationDate ?? ''}
            onChange={(e) => setForm({ ...form, expirationDate: e.target.value })} placeholder="YYYY-MM-DD" />
          <TextField label="Group ID" value={form.groupId ?? ''}
            onChange={(e) => setForm({ ...form, groupId: e.target.value })} />
          <TextField label="ZIP Code" value={form.addrZip ?? ''}
            onChange={(e) => setForm({ ...form, addrZip: e.target.value })} />
          <Box display="flex" gap={2}>
            <Button variant="contained" onClick={() => setConfirmOpen(true)}>Save</Button>
            <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
          </Box>
        </Box>
      </Paper>
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirm account update?</DialogTitle>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">Confirm</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AccountEditPage;
