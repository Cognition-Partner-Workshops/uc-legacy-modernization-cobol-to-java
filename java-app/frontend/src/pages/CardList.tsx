import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, Typography, Alert, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Chip, IconButton, CircularProgress,
} from '@mui/material';
import { Visibility, Edit } from '@mui/icons-material';

const CardList: React.FC = () => {
  const navigate = useNavigate();
  const [cards, setCards] = useState<any[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getCards().then(setCards).catch((e) => setError(e.message)).finally(() => setLoading(false));
  }, []);

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Credit Cards</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {loading ? <CircularProgress /> : (
        <Card>
          <CardContent>
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Card Number</TableCell><TableCell>Account ID</TableCell>
                    <TableCell>Customer ID</TableCell><TableCell>Status</TableCell>
                    <TableCell>Embossed Name</TableCell><TableCell>Expiration</TableCell>
                    <TableCell align="center">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {cards.map((c) => (
                    <TableRow key={c.cardNum} hover>
                      <TableCell sx={{ fontFamily: 'monospace' }}>{c.cardNum}</TableCell>
                      <TableCell>{c.acctId}</TableCell>
                      <TableCell>{c.custId}</TableCell>
                      <TableCell><Chip label={c.activeStatus === 'Y' ? 'Active' : 'Inactive'} color={c.activeStatus === 'Y' ? 'success' : 'error'} size="small" /></TableCell>
                      <TableCell>{c.embossedName}</TableCell>
                      <TableCell>{c.expirationDate}</TableCell>
                      <TableCell align="center">
                        <IconButton size="small" onClick={() => navigate(`/app/cards/${c.cardNum}`)} title="View"><Visibility fontSize="small" /></IconButton>
                        <IconButton size="small" onClick={() => navigate(`/app/cards/${c.cardNum}/edit`)} title="Edit"><Edit fontSize="small" /></IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                  {cards.length === 0 && <TableRow><TableCell colSpan={7} align="center">No cards found</TableCell></TableRow>}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default CardList;
