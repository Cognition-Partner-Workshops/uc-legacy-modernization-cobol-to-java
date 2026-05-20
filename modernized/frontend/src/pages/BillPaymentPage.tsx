import React, { useState } from 'react';
import { Box, TextField, Button, Typography, Paper, Alert, Dialog, DialogTitle, DialogActions } from '@mui/material';
import { accountApi, Account } from '../api/accountApi';
import { transactionApi } from '../api/transactionApi';

const BillPaymentPage: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [account, setAccount] = useState<Account | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleLookup = async () => {
    setError(''); setSuccess('');
    try {
      const data = await accountApi.getAccount(acctId);
      setAccount(data);
    } catch {
      setError('Account not found');
    }
  };

  const handlePay = async () => {
    setConfirmOpen(false);
    try {
      await transactionApi.billPayment(acctId);
      setSuccess('Payment processed successfully');
      setAccount(null);
    } catch {
      setError('Payment failed');
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Bill Payment</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" gap={2} mb={2}>
          <TextField label="Account Number" value={acctId}
            onChange={(e) => setAcctId(e.target.value)} />
          <Button variant="contained" onClick={handleLookup}>Look Up</Button>
        </Box>
        {account && (
          <Box>
            <Typography>Current Balance: ${account.currBal?.toFixed(2)}</Typography>
            <Button variant="contained" color="primary" sx={{ mt: 2 }}
              onClick={() => setConfirmOpen(true)}>Pay Full Balance</Button>
          </Box>
        )}
      </Paper>
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirm payment of ${account?.currBal?.toFixed(2)}?</DialogTitle>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Cancel</Button>
          <Button onClick={handlePay} variant="contained">Confirm Payment</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default BillPaymentPage;
