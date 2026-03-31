import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, Typography, Alert, Table, TableBody, TableCell,
  TableRow, Button, CircularProgress,
} from '@mui/material';
import { ArrowBack } from '@mui/icons-material';

const TransactionDetail: React.FC = () => {
  const { tranId } = useParams<{ tranId: string }>();
  const navigate = useNavigate();
  const [txn, setTxn] = useState<any>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (tranId) api.getTransaction(tranId).then(setTxn).catch((e) => setError(e.message)).finally(() => setLoading(false));
  }, [tranId]);

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/app/transactions')}>Back</Button>
        <Typography variant="h5">Transaction Detail</Typography>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {txn && (
        <Card>
          <CardContent>
            <Table>
              <TableBody>
                <TableRow><TableCell sx={{ fontWeight: 600, width: 200 }}>Transaction ID</TableCell><TableCell sx={{ fontFamily: 'monospace' }}>{txn.tranId}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Type Code</TableCell><TableCell>{txn.typeCd}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Category Code</TableCell><TableCell>{txn.catCd}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Source</TableCell><TableCell>{txn.source}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Description</TableCell><TableCell>{txn.description}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Amount</TableCell><TableCell sx={{ fontWeight: 600, color: txn.amount < 0 ? 'success.main' : 'error.main' }}>${Math.abs(txn.amount).toFixed(2)}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Card Number</TableCell><TableCell sx={{ fontFamily: 'monospace' }}>{txn.cardNum}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Merchant</TableCell><TableCell>{txn.merchantName} - {txn.merchantCity} {txn.merchantZip}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Timestamp</TableCell><TableCell>{txn.origTimestamp}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Processed</TableCell><TableCell>{txn.procTimestamp}</TableCell></TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default TransactionDetail;
