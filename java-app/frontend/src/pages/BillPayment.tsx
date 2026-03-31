import React, { useState } from 'react';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  CircularProgress,
} from '@mui/material';
import { Payment } from '@mui/icons-material';

const BillPayment: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [amount, setAmount] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess(''); setLoading(true);
    try {
      const result = await api.processPayment({ acctId: Number(acctId), amount: Number(amount) });
      setSuccess(result.message);
      setAcctId(''); setAmount('');
    } catch (err: any) { setError(err.message); }
    finally { setLoading(false); }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Bill Payment</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Card sx={{ maxWidth: 500 }}>
        <CardContent>
          <Box component="form" onSubmit={handleSubmit}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12 }}>
                <TextField fullWidth required label="Account ID" value={acctId}
                  onChange={(e) => setAcctId(e.target.value)} placeholder="e.g. 00000000011" />
              </Grid>
              <Grid size={{ xs: 12 }}>
                <TextField fullWidth required label="Payment Amount" type="number" value={amount}
                  onChange={(e) => setAmount(e.target.value)} inputProps={{ step: '0.01', min: '0.01' }} />
              </Grid>
            </Grid>
            <Button type="submit" variant="contained" startIcon={<Payment />} disabled={loading} sx={{ mt: 3 }} fullWidth>
              {loading ? <CircularProgress size={20} /> : 'Process Payment'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default BillPayment;
