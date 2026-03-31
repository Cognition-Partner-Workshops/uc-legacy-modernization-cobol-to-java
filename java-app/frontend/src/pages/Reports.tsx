import React, { useState } from 'react';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
  CircularProgress, Chip,
} from '@mui/material';
import { Assessment } from '@mui/icons-material';

const Reports: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [cardNum, setCardNum] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [report, setReport] = useState<any>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setReport(null); setLoading(true);
    try {
      const data = await api.getReport({
        acctId: acctId ? Number(acctId) : undefined,
        cardNum: cardNum || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      });
      setReport(data);
    } catch (err: any) { setError(err.message); }
    finally { setLoading(false); }
  };

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Transaction Reports</Typography>
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box component="form" onSubmit={handleSubmit}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}><TextField fullWidth label="Account ID" value={acctId} onChange={(e) => setAcctId(e.target.value)} size="small" /></Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}><TextField fullWidth label="Card Number" value={cardNum} onChange={(e) => setCardNum(e.target.value)} size="small" /></Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}><TextField fullWidth label="Start Date" value={startDate} onChange={(e) => setStartDate(e.target.value)} size="small" placeholder="YYYY-MM-DD" /></Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }}><TextField fullWidth label="End Date" value={endDate} onChange={(e) => setEndDate(e.target.value)} size="small" placeholder="YYYY-MM-DD" /></Grid>
            </Grid>
            <Button type="submit" variant="contained" startIcon={<Assessment />} disabled={loading} sx={{ mt: 2 }}>
              {loading ? <CircularProgress size={20} /> : 'Generate Report'}
            </Button>
          </Box>
        </CardContent>
      </Card>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {report && (
        <Card>
          <CardContent>
            <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
              <Chip label={`Total Transactions: ${report.totalCount}`} color="primary" />
              <Chip label={`Total Amount: $${Number(report.totalAmount).toFixed(2)}`} color="secondary" />
            </Box>
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Transaction ID</TableCell><TableCell>Type</TableCell>
                    <TableCell>Card Number</TableCell><TableCell>Description</TableCell>
                    <TableCell align="right">Amount</TableCell><TableCell>Date</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {(report.transactions || []).map((t: any) => (
                    <TableRow key={t.tranId} hover>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{t.tranId}</TableCell>
                      <TableCell>{t.typeCd}</TableCell>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{t.cardNum}</TableCell>
                      <TableCell>{t.description}</TableCell>
                      <TableCell align="right">${Math.abs(t.amount).toFixed(2)}</TableCell>
                      <TableCell>{t.origTimestamp?.substring(0, 10)}</TableCell>
                    </TableRow>
                  ))}
                  {(!report.transactions || report.transactions.length === 0) && (
                    <TableRow><TableCell colSpan={6} align="center">No transactions found</TableCell></TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default Reports;
