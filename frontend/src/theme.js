import { createTheme } from '@mui/material/styles'

// ---- Design tokens — dark, forensic/console aesthetic ------------------
// Near-black workspace, slightly-raised card surfaces, a vivid teal accent
// that reads clearly against dark backgrounds, and the same dedicated
// three-color risk scale as before (brightened for dark-bg contrast).
export const tokens = {
  bg: '#0B0E17',         // page background
  sidebar: '#0B0E17',    // sidebar — same as page bg, cards provide the contrast
  paper: '#131A2A',      // card surfaces
  line: '#232B40',       // hairline borders — visible against dark bg
  ink: '#E7EAF3',        // high-emphasis text (near-white, not pure white)
  inkMuted: '#8993AC',   // secondary text
  accent: '#14B8A6',     // primary action / links — brightened teal for dark bg
  low: '#22C55E',
  medium: '#F59E0B',
  high: '#EF4444',
}

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: tokens.accent },
    background: { default: tokens.bg, paper: tokens.paper },
    text: { primary: tokens.ink, secondary: tokens.inkMuted },
    divider: tokens.line,
  },
  typography: {
    fontFamily: '"Inter", "Helvetica Neue", Arial, sans-serif',
    h1: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 600 },
    h2: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 600 },
    h3: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 600 },
    h4: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 600 },
    h5: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 600 },
    h6: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 10 },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: `1px solid ${tokens.line}`,
          boxShadow: 'none',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8, paddingInline: 18 },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { borderRadius: 6, fontFamily: '"IBM Plex Mono", monospace', fontSize: '0.75rem' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: { borderColor: tokens.line },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        notchedOutline: { borderColor: tokens.line },
      },
    },
  },
})

export default theme
