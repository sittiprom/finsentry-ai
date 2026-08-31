import { Box, Typography } from '@mui/material'
import { tokens } from '../theme'

const COLOR = { LOW: tokens.low, MEDIUM: tokens.medium, HIGH: tokens.high }

export default function RiskBadge({ level }) {
  const color = COLOR[level] ?? tokens.inkMuted
  return (
    <Box
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 0.75,
        px: 1.25,
        py: 0.5,
        borderRadius: 999,
        border: `1px solid ${color}`,
        backgroundColor: `${color}26`,
      }}
    >
      <Box sx={{ width: 7, height: 7, borderRadius: '50%', backgroundColor: color }} />
      <Typography
        sx={{ fontFamily: '"IBM Plex Mono", monospace', fontSize: '0.75rem', fontWeight: 600, color }}
      >
        {level ?? 'UNKNOWN'}
      </Typography>
    </Box>
  )
}
