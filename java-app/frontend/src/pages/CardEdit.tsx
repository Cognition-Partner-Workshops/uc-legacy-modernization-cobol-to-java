import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import {
  Box, Card, CardContent, TextField, Button, Typography, Alert, Grid,
  CircularProgress, MenuItem,
} from '@mui/material';
import { ArrowBack, Save } from '@mui/icons-material';

const CardEdit: React.FC = () => {
  const { cardNum } = useParams<{ cardNum: string }>();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ embossedName: '', expirationDate: '', activeStatus: '' });

  useEffect(() => {
    if (cardNum) {
      api.getCard(cardNum).then((c) => {
        setForm({ embossedName: c.embossedName || '', expirationDate: c.expirationDate || '', activeStatus: c.activeStatus || '' });
      }).catch((e) => setError(e.message)).finally(() => setLoading(false));
    }
  }, [cardNum]);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(''); setSuccess(''); setSaving(true);
    try {
      await api.updateCard(cardNum!, form);
      setSuccess('Card updated successfully.');
    } catch (err: any) { setError(err.message); }
    finally { setSaving(false); }
  };

  if (loading) return <CircularProgress />;

  return (
    <Box>
      <Box sx={{ display: 'flex', gap: 2, mb: 3, alignItems: 'center' }}>
        <Button startIcon={<ArrowBack />} onClick={() => navigate('/app/cards')}>Back to Cards</Button>
        <Typography variant="h5">Edit Card {cardNum}</Typography>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
      <Card>
        <CardContent>
          <Box component="form" onSubmit={handleSave}>
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Embossed Name" value={form.embossedName} onChange={(e) => setForm({ ...form, embossedName: e.target.value })} /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}><TextField fullWidth label="Expiration Date" value={form.expirationDate} onChange={(e) => setForm({ ...form, expirationDate: e.target.value })} placeholder="YYYY-MM-DD" /></Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField select fullWidth label="Status" value={form.activeStatus} onChange={(e) => setForm({ ...form, activeStatus: e.target.value })}>
                  <MenuItem value="Y">Active</MenuItem><MenuItem value="N">Inactive</MenuItem>
                </TextField>
              </Grid>
            </Grid>
            <Button type="submit" variant="contained" startIcon={<Save />} disabled={saving} sx={{ mt: 3 }}>
              {saving ? <CircularProgress size={20} /> : 'Save Changes'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default CardEdit;
