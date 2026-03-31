import React, { useState } from 'react';
import { api } from '../services/api';
import {
  Box, Card, CardContent, Typography, Alert, Button, Grid, CircularProgress,
} from '@mui/material';
import { PlayArrow } from '@mui/icons-material';

interface JobResult { status: string; message: string }

const BatchJobs: React.FC = () => {
  const [results, setResults] = useState<Record<string, JobResult>>({});
  const [running, setRunning] = useState<Record<string, boolean>>({});
  const [error, setError] = useState('');

  const runJob = async (name: string, fn: () => Promise<any>) => {
    setRunning((r) => ({ ...r, [name]: true }));
    setError('');
    try {
      const result = await fn();
      setResults((r) => ({ ...r, [name]: result }));
    } catch (err: any) {
      setResults((r) => ({ ...r, [name]: { status: 'FAILED', message: err.message } }));
    } finally {
      setRunning((r) => ({ ...r, [name]: false }));
    }
  };

  const jobs = [
    { name: 'Post Transactions', desc: 'Process daily transactions, validate cards, update balances', fn: api.postTransactions },
    { name: 'Calculate Interest', desc: 'Compute monthly interest charges based on disclosure rates', fn: api.calculateInterest },
    { name: 'Generate Statements', desc: 'Generate account statements for all cardholders', fn: api.generateStatements },
  ];

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 3 }}>Batch Jobs</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Grid container spacing={3}>
        {jobs.map((job) => (
          <Grid size={{ xs: 12, md: 4 }} key={job.name}>
            <Card sx={{ height: '100%' }}>
              <CardContent sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                <Typography variant="h6" sx={{ mb: 1 }}>{job.name}</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2, flexGrow: 1 }}>{job.desc}</Typography>
                {results[job.name] && (
                  <Alert severity={results[job.name].status === 'SUCCESS' ? 'success' : 'error'} sx={{ mb: 2 }}>
                    {results[job.name].message}
                  </Alert>
                )}
                <Button variant="contained" startIcon={running[job.name] ? <CircularProgress size={20} /> : <PlayArrow />}
                  onClick={() => runJob(job.name, job.fn)} disabled={running[job.name]} fullWidth>
                  {running[job.name] ? 'Running...' : 'Run Job'}
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default BatchJobs;
