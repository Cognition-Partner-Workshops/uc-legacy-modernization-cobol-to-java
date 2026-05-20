import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, Alert } from '@mui/material';
import { transactionApi } from '../api/transactionApi';

const TransactionAddPage: React.FC = () => {
  const navigate = useNavigate();
  const [cardNum, setCardNum] = useState('');
  const [typeCd, setTypeCd] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [merchantName, setMerchantName] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async () => {
    if (cardNum.length !== 16 || !/^\d+$/.test(cardNum)) {
      setError('Card number must be 16 digits');
      return;
    }
    try {
      await transactionApi.addTransaction({
        cardNum,
        typeCd,
        amount: parseFloat(amount),
        description,
        merchantName,
      });
      navigate('/transactions');
    } catch {
      setError('Failed to add transaction');
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Add Transaction</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" gap={2} maxWidth={400}>
          <TextField label="Card Number" value={cardNum}
            onChange={(e) => setCardNum(e.target.value)} inputProps={{ maxLength: 16 }} required />
          <TextField label="Transaction Type" value={typeCd}
            onChange={(e) => setTypeCd(e.target.value)} inputProps={{ maxLength: 2 }} required />
          <TextField label="Amount" type="number" value={amount}
            onChange={(e) => setAmount(e.target.value)} required />
          <TextField label="Description" value={description}
            onChange={(e) => setDescription(e.target.value)} />
          <TextField label="Merchant Name" value={merchantName}
            onChange={(e) => setMerchantName(e.target.value)} />
          <Box display="flex" gap={2}>
            <Button variant="contained" onClick={handleSubmit}>Submit</Button>
            <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default TransactionAddPage;
