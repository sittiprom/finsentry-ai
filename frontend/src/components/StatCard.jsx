import { Paper, Typography, Box } from '@mui/material'
import { tokens } from '../theme'

export default function StatCard({ label, value, accentColor }) {
  return (
    <Paper sx={{ p: 2.25, flex: 1, minWidth: 140 }}>
      <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6 }}>
        {label}
      </Typography>
      <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.75, mt: 0.25 }}>
        <Typography
          sx={{
            fontFamily: '"Space Grotesk", sans-serif',
            fontWeight: 700,
            fontSize: '2rem',
            color: accentColor ?? tokens.ink,
            lineHeight: 1,
          }}
        >
          {value}
        </Typography>
      </Box>
    </Paper>
  )
}
