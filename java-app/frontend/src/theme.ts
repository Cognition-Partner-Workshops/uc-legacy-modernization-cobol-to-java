import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#42a5f5',
      light: '#80d6ff',
      dark: '#0077c2',
    },
    secondary: {
      main: '#66bb6a',
      light: '#98ee99',
      dark: '#338a3e',
    },
    background: {
      default: '#0a1929',
      paper: '#132f4c',
    },
    error: {
      main: '#f44336',
    },
    warning: {
      main: '#ffa726',
    },
    success: {
      main: '#66bb6a',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: { fontWeight: 600 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-head': {
            fontWeight: 700,
            backgroundColor: '#1a3a5c',
          },
        },
      },
    },
  },
});

export default theme;
