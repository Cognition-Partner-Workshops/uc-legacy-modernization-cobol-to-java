import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, TextField, Button, Typography, Table, TableHead, TableRow,
  TableCell, TableBody, Paper, TablePagination,
} from '@mui/material';
import { transactionApi, Transaction } from '../api/transactionApi';

const TransactionListPage: React.FC = () => {
  const [cardNum, setCardNum] = useState('');
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const navigate = useNavigate();

  const handleSearch = async (p = 0) => {
    const data = await transactionApi.listByCard(cardNum, p, 20);
    setTransactions(data.content);
    setTotalElements(data.totalElements);
    setPage(p);
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Transaction List</Typography>
      <Box display="flex" gap={2} mb={3}>
        <TextField label="Card Number" value={cardNum}
          onChange={(e) => setCardNum(e.target.value)} inputProps={{ maxLength: 16 }} />
        <Button variant="contained" onClick={() => handleSearch(0)}>Search</Button>
        <Button variant="outlined" onClick={() => navigate('/transactions/new')}>Add Transaction</Button>
      </Box>
      {transactions.length > 0 && (
        <Paper>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Transaction ID</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Card</TableCell>
                <TableCell>Date</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((txn) => (
                <TableRow key={txn.tranId} hover
                  onClick={() => navigate(`/transactions/${txn.tranId}`)} sx={{ cursor: 'pointer' }}>
                  <TableCell>{txn.tranId}</TableCell>
                  <TableCell>{txn.typeCd}</TableCell>
                  <TableCell>${txn.amount?.toFixed(2)}</TableCell>
                  <TableCell>{txn.cardNum}</TableCell>
                  <TableCell>{txn.origTs?.substring(0, 10)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div" count={totalElements} page={page}
            rowsPerPage={20} rowsPerPageOptions={[20]}
            onPageChange={(_, p) => handleSearch(p)}
          />
        </Paper>
      )}
    </Box>
  );
};

export default TransactionListPage;
