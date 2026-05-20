import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, Paper, Table, TableBody, TableRow, TableCell, Button } from '@mui/material';
import { cardApi, Card } from '../api/cardApi';

const CardDetailPage: React.FC = () => {
  const { num } = useParams<{ num: string }>();
  const [card, setCard] = useState<Card | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (num) cardApi.getCard(num).then(setCard);
  }, [num]);

  if (!card) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Card Detail</Typography>
      <Paper sx={{ p: 2 }}>
        <Table>
          <TableBody>
            <TableRow><TableCell>Card Number</TableCell><TableCell>{card.cardNum}</TableCell></TableRow>
            <TableRow><TableCell>Account ID</TableCell><TableCell>{card.acctId}</TableCell></TableRow>
            <TableRow><TableCell>Name on Card</TableCell><TableCell>{card.embossedName}</TableCell></TableRow>
            <TableRow><TableCell>CVV</TableCell><TableCell>{card.cvvCd}</TableCell></TableRow>
            <TableRow><TableCell>Expiration Date</TableCell><TableCell>{card.expirationDate}</TableCell></TableRow>
            <TableRow><TableCell>Status</TableCell><TableCell>{card.activeStatus}</TableCell></TableRow>
          </TableBody>
        </Table>
        <Button variant="outlined" sx={{ mt: 2 }} onClick={() => navigate(`/cards/${num}/edit`)}>Edit</Button>
      </Paper>
    </Box>
  );
};

export default CardDetailPage;
