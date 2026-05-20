import React, { useState } from 'react';
import {
  Box, TextField, Button, Typography, Paper, Table, TableHead,
  TableRow, TableCell, TableBody, Alert,
} from '@mui/material';
import api from '../api/authApi';
import { Transaction } from '../api/transactionApi';

const ReportsPage: React.FC = () => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [results, setResults] = useState<Transaction[]>([]);
  const [error, setError] = useState('');

  const handleGenerate = async () => {
    setError('');
    try {
      const response = await api.post<Transaction[]>('/transactions/report', { startDate, endDate });
      setResults(response.data);
    } catch {
      setError('Failed to generate report');
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Reports</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box display="flex" gap={2} alignItems="center">
          <TextField label="Start Date" value={startDate}
            onChange={(e) => setStartDate(e.target.value)} placeholder="YYYY-MM-DD" />
          <TextField label="End Date" value={endDate}
            onChange={(e) => setEndDate(e.target.value)} placeholder="YYYY-MM-DD" />
          <Button variant="contained" onClick={handleGenerate}>Generate Report</Button>
        </Box>
      </Paper>
      {results.length > 0 && (
        <Paper>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Amount</TableCell>
                <TableCell>Card</TableCell>
                <TableCell>Date</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {results.map((txn) => (
                <TableRow key={txn.tranId}>
                  <TableCell>{txn.tranId}</TableCell>
                  <TableCell>{txn.typeCd}</TableCell>
                  <TableCell>${txn.amount?.toFixed(2)}</TableCell>
                  <TableCell>{txn.cardNum}</TableCell>
                  <TableCell>{txn.origTs?.substring(0, 10)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  );
};

export default ReportsPage;
