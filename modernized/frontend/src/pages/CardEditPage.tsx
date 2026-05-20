import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, TextField, Button, Typography, Paper, MenuItem, Select, FormControl, InputLabel, Alert } from '@mui/material';
import { cardApi, Card } from '../api/cardApi';

const CardEditPage: React.FC = () => {
  const { num } = useParams<{ num: string }>();
  const navigate = useNavigate();
  const [card, setCard] = useState<Card | null>(null);
  const [embossedName, setEmbossedName] = useState('');
  const [activeStatus, setActiveStatus] = useState('Y');
  const [expMonth, setExpMonth] = useState(1);
  const [expYear, setExpYear] = useState(2025);
  const [error, setError] = useState('');

  useEffect(() => {
    if (num) {
      cardApi.getCard(num).then((data) => {
        setCard(data);
        setEmbossedName(data.embossedName || '');
        setActiveStatus(data.activeStatus || 'Y');
        if (data.expirationDate) {
          const parts = data.expirationDate.split('-');
          setExpYear(parseInt(parts[0]));
          setExpMonth(parseInt(parts[1]));
        }
      });
    }
  }, [num]);

  const handleSave = async () => {
    if (!/^[a-zA-Z ]+$/.test(embossedName)) {
      setError('Name must contain only alphabetic characters and spaces');
      return;
    }
    try {
      await cardApi.updateCard(num!, {
        embossedName,
        activeStatus,
        expirationMonth: expMonth,
        expirationYear: expYear,
        version: card!.version,
      });
      navigate(`/cards/${num}`);
    } catch {
      setError('Failed to update card');
    }
  };

  if (!card) return <Typography>Loading...</Typography>;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Edit Card: {num}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Paper sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" gap={2} maxWidth={400}>
          <TextField label="Name on Card" value={embossedName}
            onChange={(e) => setEmbossedName(e.target.value)} />
          <FormControl>
            <InputLabel>Status</InputLabel>
            <Select value={activeStatus} label="Status"
              onChange={(e) => setActiveStatus(e.target.value)}>
              <MenuItem value="Y">Active (Y)</MenuItem>
              <MenuItem value="N">Inactive (N)</MenuItem>
            </Select>
          </FormControl>
          <TextField label="Expiration Month" type="number" value={expMonth}
            onChange={(e) => setExpMonth(parseInt(e.target.value))}
            inputProps={{ min: 1, max: 12 }} />
          <TextField label="Expiration Year" type="number" value={expYear}
            onChange={(e) => setExpYear(parseInt(e.target.value))}
            inputProps={{ min: 1950, max: 2099 }} />
          <Box display="flex" gap={2}>
            <Button variant="contained" onClick={handleSave}>Save</Button>
            <Button variant="outlined" onClick={() => navigate(-1)}>Cancel</Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
};

export default CardEditPage;
