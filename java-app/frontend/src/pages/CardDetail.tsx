import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, Typography, Alert, Table, TableBody, TableCell,
  TableRow, Button, Chip, CircularProgress,
} from '@mui/material';
import { ArrowBack, Edit } from '@mui/icons-material';

const CardDetail: React.FC = () => {
  const { cardNum } = useParams<{ cardNum: string }>();
  const navigate = useNavigate();
  const [card, setCard] = useState<any>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (cardNum) api.getCard(cardNum).then(setCard).catch((e) => setError(e.message)).finally(() => setLoading(false));
  }, [cardNum]);

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/app/cards')}>Back to Cards</Button>
        <Typography variant="h5" sx={{ flexGrow: 1 }}>Card Detail</Typography>
        {card && <Button variant="contained" startIcon={<Edit />} onClick={() => navigate(`/app/cards/${cardNum}/edit`)}>Edit</Button>}
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {card && (
        <Card>
          <CardContent>
            <Table>
              <TableBody>
                <TableRow><TableCell sx={{ fontWeight: 600, width: 200 }}>Card Number</TableCell><TableCell sx={{ fontFamily: 'monospace' }}>{card.cardNum}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Account ID</TableCell><TableCell>{card.acctId}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Customer ID</TableCell><TableCell>{card.custId}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Status</TableCell><TableCell><Chip label={card.activeStatus === 'Y' ? 'Active' : 'Inactive'} color={card.activeStatus === 'Y' ? 'success' : 'error'} size="small" /></TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Embossed Name</TableCell><TableCell>{card.embossedName}</TableCell></TableRow>
                <TableRow><TableCell sx={{ fontWeight: 600 }}>Expiration Date</TableCell><TableCell>{card.expirationDate}</TableCell></TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default CardDetail;
