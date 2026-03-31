import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  CircularProgress,
} from '@mui/material';
import { Save, ArrowBack } from '@mui/icons-material';

const TransactionAdd: React.FC = () => {
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    cardNum: '', typeCd: '', catCd: '', source: 'ONLINE', description: '',
    amount: '', merchantId: '', merchantName: '', merchantCity: '', merchantZip: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess(''); setSaving(true);
    try {
      const txn = await api.addTransaction({
        ...form, catCd: Number(form.catCd), amount: Number(form.amount),
        merchantId: form.merchantId ? Number(form.merchantId) : null,
      });
      setSuccess(`Transaction ${txn.tranId} created successfully.`);
      setForm({ cardNum: '', typeCd: '', catCd: '', source: 'ONLINE', description: '', amount: '', merchantId: '', merchantName: '', merchantCity: '', merchantZip: '' });
    } catch (err: any) { setError(err.message); }
    finally { setSaving(false); }
  };

  const set = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => setForm({ ...form, [field]: e.target.value });

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/app/transactions')}>Back</Button>
        <Typography variant="h5">Add Transaction</Typography>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Card>
        <CardContent>
          <Box component="form" onSubmit={handleSubmit}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth required label="Card Number" value={form.cardNum} onChange={set('cardNum')} inputProps={{ maxLength: 16 }} helperText="16-digit card number" /></Grid>
              <Grid size={{ xs: 12, sm: 3 }}><TextField fullWidth required label="Type Code" value={form.typeCd} onChange={set('typeCd')} inputProps={{ maxLength: 2 }} /></Grid>
              <Grid size={{ xs: 12, sm: 3 }}><TextField fullWidth required label="Category Code" value={form.catCd} onChange={set('catCd')} type="number" /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Source" value={form.source} onChange={set('source')} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth required label="Amount" value={form.amount} onChange={set('amount')} type="number" inputProps={{ step: '0.01' }} /></Grid>
              <Grid size={{ xs: 12 }}><TextField fullWidth label="Description" value={form.description} onChange={set('description')} /></Grid>
              <Grid size={{ xs: 12, sm: 4 }}><TextField fullWidth label="Merchant Name" value={form.merchantName} onChange={set('merchantName')} /></Grid>
              <Grid size={{ xs: 12, sm: 4 }}><TextField fullWidth label="Merchant City" value={form.merchantCity} onChange={set('merchantCity')} /></Grid>
              <Grid size={{ xs: 12, sm: 4 }}><TextField fullWidth label="Merchant Zip" value={form.merchantZip} onChange={set('merchantZip')} /></Grid>
            </Grid>
            <Button type="submit" variant="contained" startIcon={<Save />} disabled={saving} sx={{ mt: 3 }}>
              {saving ? <CircularProgress size={20} /> : 'Create Transaction'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default TransactionAdd;
