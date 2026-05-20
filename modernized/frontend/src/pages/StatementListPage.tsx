import React, { useState } from 'react';
import {
  Box, TextField, Button, Typography, Paper, Table, TableHead,
  TableRow, TableCell, TableBody,
} from '@mui/material';
import api from '../api/authApi';

interface Statement {
  id: number;
  acctId: string;
  periodStart: string;
  periodEnd: string;
  filePath: string;
  htmlPath: string;
  generatedAt: string;
}

const StatementListPage: React.FC = () => {
  const [acctId, setAcctId] = useState('');
  const [statements, setStatements] = useState<Statement[]>([]);

  const handleSearch = async () => {
    const response = await api.get<Statement[]>('/statements', { params: { acctId } });
    setStatements(response.data);
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>Statements</Typography>
      <Box display="flex" gap={2} mb={3}>
        <TextField label="Account Number" value={acctId}
          onChange={(e) => setAcctId(e.target.value)} />
        <Button variant="contained" onClick={handleSearch}>Search</Button>
      </Box>
      {statements.length > 0 && (
        <Paper>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Period</TableCell>
                <TableCell>Generated</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {statements.map((s) => (
                <TableRow key={s.id}>
                  <TableCell>{s.id}</TableCell>
                  <TableCell>{s.periodStart} - {s.periodEnd}</TableCell>
                  <TableCell>{s.generatedAt}</TableCell>
                  <TableCell>
                    <Button size="small">PDF</Button>
                    <Button size="small">HTML</Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  );
};

export default StatementListPage;
