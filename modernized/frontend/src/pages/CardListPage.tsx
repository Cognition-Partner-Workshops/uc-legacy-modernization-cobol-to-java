import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, TextField, Button, Typography, Table, TableHead, TableRow,
  TableCell, TableBody, Paper, TablePagination,
} from '@mui/material';
import { cardApi, CardListItem } from '../api/cardApi';

const CardListPage: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [cards, setCards] = useState<CardListItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const navigate = useNavigate();

  const handleSearch = async (p = 0) => {
    const data = await cardApi.listByAccount(acctId, p, 7);
    setCards(data.content);
    setTotalElements(data.totalElements);
    setPage(p);
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Card List</Typography>
      <Box display="flex" gap={2} mb={3}>
        <TextField label="Account Number" value={acctId}
          onChange={(e) => setAcctId(e.target.value)} inputProps={{ maxLength: 11 }} />
        <Button variant="contained" onClick={() => handleSearch(0)}>Search</Button>
      </Box>
      {cards.length > 0 && (
        <Paper>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Account Number</TableCell>
                <TableCell>Card Number</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {cards.map((card) => (
                <TableRow key={card.cardNum}>
                  <TableCell>{card.acctId}</TableCell>
                  <TableCell>{card.cardNum}</TableCell>
                  <TableCell>{card.activeStatus}</TableCell>
                  <TableCell>
                    <Button size="small" onClick={() => navigate(`/cards/${card.cardNum}`)}>Detail</Button>
                    <Button size="small" onClick={() => navigate(`/cards/${card.cardNum}/edit`)}>Update</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div"
            count={totalElements}
            page={page}
            rowsPerPage={7}
            rowsPerPageOptions={[7]}
            onPageChange={(_, p) => handleSearch(p)}
          />
        </Paper>
      )}
    </Box>
  );
};

export default CardListPage;
