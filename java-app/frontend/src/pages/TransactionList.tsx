import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, Typography, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton, CircularProgress,
  TextField, Button, Pagination,
} from '@mui/material';
import { Visibility, Search } from '@mui/icons-material';

const TransactionList: React.FC = () => {
  const navigate = useNavigate();
  const [transactions, setTransactions] = useState<any[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [cardNumFilter, setCardNumFilter] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = async (p: number, cn?: string) => {
    setLoading(true); setError('');
    try {
      const data = await api.getTransactions(p, cn || undefined);
      setTransactions(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (e: any) { setError(e.message); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(page, cardNumFilter); }, [page]);

  const handleFilter = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    load(0, cardNumFilter);
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Transactions</Typography>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box component="form" onSubmit={handleFilter} sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <TextField label="Filter by Card Number" value={cardNumFilter}
              onChange={(e) => setCardNumFilter(e.target.value)} size="small" sx={{ width: 250 }} />
            <Button type="submit" variant="contained" startIcon={<Search />}>Filter</Button>
            <Button variant="outlined" onClick={() => navigate('/app/transactions/add')}>Add Transaction</Button>
          </Box>
        </CardContent>
      </Card>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <Card>
          <CardContent>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Transaction ID</TableCell><TableCell>Type</TableCell>
                    <TableCell>Category</TableCell><TableCell>Card Number</TableCell>
                    <TableCell>Description</TableCell><TableCell align="right">Amount</TableCell>
                    <TableCell>Date</TableCell><TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {transactions.map((t) => (
                    <TableRow key={t.tranId} hover>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{t.tranId}</TableCell>
                      <TableCell>{t.typeCd}</TableCell>
                      <TableCell>{t.catCd}</TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{t.cardNum}</TableCell>
                      <TableCell>{t.description}</TableCell>
                      <TableCell align="right" sx={{ color: t.amount < 0 ? 'success.main' : 'error.main' }}>
                        ${Math.abs(t.amount).toFixed(2)}
                      </TableCell>
                      <TableCell>{t.origTimestamp?.substring(0, 10)}</TableCell>
                      <TableCell align="center">
                        <IconButton size="small" onClick={() => navigate(`/app/transactions/${t.tranId}`)}><Visibility fontSize="small" /></IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {transactions.length === 0 && <TableRow><TableCell colSpan={8} align="center">No transactions found</TableCell></TableRow>}
                </TableBody>
              </Table>
            </TableContainer>
            {totalPages > 1 && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                <Pagination count={totalPages} page={page + 1} onChange={(_, p) => setPage(p - 1)} color="primary" />
              </Box>
            )}
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default TransactionList;
