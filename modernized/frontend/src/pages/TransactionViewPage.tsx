import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Typography, Paper, Table, TableBody, TableRow, TableCell } from '@mui/material';
import { transactionApi, Transaction } from '../api/transactionApi';

const TransactionViewPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [txn, setTxn] = useState<Transaction | null>(null);

  useEffect(() => {
    if (id) transactionApi.getTransaction(id).then(setTxn);
  }, [id]);

  if (!txn) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Transaction Detail</Typography>
      <Paper sx={{ p: 2 }}>
        <Table>
          <TableBody>
            <TableRow><TableCell>Transaction ID</TableCell><TableCell>{txn.tranId}</TableCell></TableRow>
            <TableRow><TableCell>Type Code</TableCell><TableCell>{txn.typeCd}</TableCell></TableRow>
            <TableRow><TableCell>Category</TableCell><TableCell>{txn.catCd}</TableCell></TableRow>
            <TableRow><TableCell>Source</TableCell><TableCell>{txn.source}</TableCell></TableRow>
            <TableRow><TableCell>Description</TableCell><TableCell>{txn.description}</TableCell></TableRow>
            <TableRow><TableCell>Amount</TableCell><TableCell>${txn.amount?.toFixed(2)}</TableCell></TableRow>
            <TableRow><TableCell>Card Number</TableCell><TableCell>{txn.cardNum}</TableCell></TableRow>
            <TableRow><TableCell>Merchant</TableCell><TableCell>{txn.merchantName}</TableCell></TableRow>
            <TableRow><TableCell>Merchant City</TableCell><TableCell>{txn.merchantCity}</TableCell></TableRow>
            <TableRow><TableCell>Original Timestamp</TableCell><TableCell>{txn.origTs}</TableCell></TableRow>
            <TableRow><TableCell>Process Timestamp</TableCell><TableCell>{txn.procTs}</TableCell></TableRow>
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );
};

export default TransactionViewPage;
