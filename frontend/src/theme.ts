import { createTheme } from '@mui/material/styles'

// Centralized dark theme used across the app
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#90caf9', // soft blue
    },
    secondary: {
      main: '#f48fb1', // pink
    },
    background: {
      default: '#0b0f12', // near-black background
      paper: '#111315', // slightly lighter for surfaces
    },
    success: {
      main: '#66bb6a',
    },
  },
  typography: {
    // keep defaults but you can tweak fonts here
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: {
          backgroundColor: '#071018',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
    },
  },
})

export default theme

