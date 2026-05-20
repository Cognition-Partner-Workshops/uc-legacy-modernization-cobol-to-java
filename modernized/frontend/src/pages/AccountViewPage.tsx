import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, TextField, Button, Typography, Paper, Table, TableBody,
  TableCell, TableRow, Alert,
} from '@mui/material';
import { accountApi, Account } from '../api/accountApi';

const AccountViewPage: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [account, setAccount] = useState<Account | null>(null);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSearch = async () => {
    setError('');
    try {
      const data = await accountApi.getAccount(acctId);
      setAccount(data);
    } catch {
      setError('Account not found');
      setAccount(null);
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Account View</Typography>
      <Box display="flex" gap={2} mb={3}>
        <TextField
          label="Account Number"
          value={acctId}
          onChange={(e) => setAcctId(e.target.value)}
          inputProps={{ maxLength: 11 }}
        />
        <Button variant="contained" onClick={handleSearch}>Search</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {account && (
        <Paper sx={{ p: 2 }}>
          <Table>
            <TableBody>
              <TableRow><TableCell>Account ID</TableCell><TableCell>{account.acctId}</TableCell></TableRow>
              <TableRow><TableCell>Status</TableCell><TableCell>{account.activeStatus}</TableCell></TableRow>
              <TableRow><TableCell>Current Balance</TableCell><TableCell>${account.currBal?.toFixed(2)}</TableCell></TableRow>
              <TableRow><TableCell>Credit Limit</TableCell><TableCell>${account.creditLimit?.toFixed(2)}</TableCell></TableRow>
              <TableRow><TableCell>Cash Credit Limit</TableCell><TableCell>${account.cashCreditLimit?.toFixed(2)}</TableCell></TableRow>
              <TableRow><TableCell>Open Date</TableCell><TableCell>{account.openDate}</TableCell></TableRow>
              <TableRow><TableCell>Expiration Date</TableCell><TableCell>{account.expirationDate}</TableCell></TableRow>
              <TableRow><TableCell>Group ID</TableCell><TableCell>{account.groupId}</TableCell></TableRow>
              <TableRow><TableCell>ZIP</TableCell><TableCell>{account.addrZip}</TableCell></TableRow>
            </TableBody>
          </Table>
          <Button
            variant="outlined"
            sx={{ mt: 2 }}
            onClick={() => navigate(`/accounts/${account.acctId}/edit`)}
          >
            Edit Account
          </Button>
        </Paper>
      )}
    </Box>
  );
};

export default AccountViewPage;
