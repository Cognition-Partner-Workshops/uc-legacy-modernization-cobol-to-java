import React, { useState } from 'react';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip,
  CircularProgress,
} from '@mui/material';
import { Search } from '@mui/icons-material';

const AccountView: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [data, setData] = useState<any>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setData(null); setLoading(true);
    try {
      const result = await api.getAccount(Number(acctId));
      setData(result);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const account = data?.account;
  const customer = data?.customer;
  const cardXrefs = data?.cardXrefs || [];

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Account View</Typography>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box component="form" onSubmit={handleSearch} sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <TextField label="Account ID" value={acctId} onChange={(e) => setAcctId(e.target.value)}
              placeholder="e.g. 00000000011" size="small" sx={{ width: 250 }} />
            <Button type="submit" variant="contained" startIcon={<Search />} disabled={loading}>
              {loading ? <CircularProgress size={20} /> : 'Search'}
            </Button>
          </Box>
        </CardContent>
      </Card>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {account && (
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" sx={{ mb: 2 }}>Account Details</Typography>
                <Table size="small">
                  <TableBody>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Account ID</TableCell><TableCell>{String(account.acctId).padStart(11, '0')}</TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Status</TableCell><TableCell><Chip label={account.activeStatus === 'Y' ? 'Active' : 'Inactive'} color={account.activeStatus === 'Y' ? 'success' : 'error'} size="small" /></TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Current Balance</TableCell><TableCell>${account.currBal?.toFixed(2)}</TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Credit Limit</TableCell><TableCell>${account.creditLimit?.toFixed(2)}</TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Cash Credit Limit</TableCell><TableCell>${account.cashCreditLimit?.toFixed(2)}</TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Open Date</TableCell><TableCell>{account.openDate}</TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Expiration Date</TableCell><TableCell>{account.expirationDate}</TableCell></TableRow>
                    <TableRow><TableCell sx={{ fontWeight: 600 }}>Group ID</TableCell><TableCell>{account.groupId}</TableCell></TableRow>
                  </TableBody>
                </Table>
              </CardContent>
            </Card>
          </Grid>
          {customer && (
            <Grid size={{ xs: 12, md: 6 }}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Customer Information</Typography>
                  <Table size="small">
                    <TableBody>
                      <TableRow><TableCell sx={{ fontWeight: 600 }}>Customer ID</TableCell><TableCell>{customer.custId}</TableCell></TableRow>
                      <TableRow><TableCell sx={{ fontWeight: 600 }}>Name</TableCell><TableCell>{customer.firstName} {customer.middleName} {customer.lastName}</TableCell></TableRow>
                      <TableRow><TableCell sx={{ fontWeight: 600 }}>Address</TableCell><TableCell>{customer.addressLine1}<br />{customer.addressLine2 && <>{customer.addressLine2}<br /></>}{customer.stateCode} {customer.zipCode}</TableCell></TableRow>
                      <TableRow><TableCell sx={{ fontWeight: 600 }}>Phone</TableCell><TableCell>{customer.phone1}{customer.phone2 && <><br />{customer.phone2}</>}</TableCell></TableRow>
                      <TableRow><TableCell sx={{ fontWeight: 600 }}>SSN</TableCell><TableCell>***-**-{customer.ssn?.slice(-4)}</TableCell></TableRow>
                      <TableRow><TableCell sx={{ fontWeight: 600 }}>FICO Score</TableCell><TableCell>{customer.ficoScore}</TableCell></TableRow>
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </Grid>
          )}
          {cardXrefs.length > 0 && (
            <Grid size={{ xs: 12 }}>
              <Card>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>Linked Cards</Typography>
                  <TableContainer component={Paper} variant="outlined">
                    <Table size="small">
                      <TableHead><TableRow><TableCell>Card Number</TableCell><TableCell>Customer ID</TableCell><TableCell>Account ID</TableCell></TableRow></TableHead>
                      <TableBody>
                        {cardXrefs.map((x: any) => (
                          <TableRow key={x.cardNum}><TableCell>{x.cardNum}</TableCell><TableCell>{x.custId}</TableCell><TableCell>{x.acctId}</TableCell></TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </CardContent>
              </Card>
            </Grid>
          )}
        </Grid>
      )}
    </Box>
  );
};

export default AccountView;
