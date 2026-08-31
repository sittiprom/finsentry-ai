import { Box, Typography } from '@mui/material'
import { tokens } from '../theme'

const CONFIG = {
  PENDING: { color: tokens.inkMuted, label: 'PENDING' },
  IN_PROGRESS: { color: tokens.accent, label: 'IN PROGRESS' },
  COMPLETED: { color: tokens.low, label: 'COMPLETED' },
  FAILED: { color: tokens.high, label: 'FAILED' },
}

export default function StatusBadge({ status }) {
  const cfg = CONFIG[status] ?? { color: tokens.inkMuted, label: status ?? 'UNKNOWN' }
  return (
    <Box
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 0.75,
        px: 1.25,
        py: 0.5,
        borderRadius: 999,
        border: `1px solid ${cfg.color}`,
        backgroundColor: `${cfg.color}26`,
      }}
    >
      <Box sx={{ width: 7, height: 7, borderRadius: '50%', backgroundColor: cfg.color }} />
      <Typography sx={{ fontFamily: '"IBM Plex Mono", monospace', fontSize: '0.72rem', fontWeight: 600, color: cfg.color }}>
        {cfg.label}
      </Typography>
    </Box>
  )
}
