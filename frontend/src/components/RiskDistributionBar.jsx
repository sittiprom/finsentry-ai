import { Box, Typography } from '@mui/material'
import { tokens } from '../theme'

const LEVELS = [
  { key: 'LOW', color: tokens.low, label: 'Low' },
  { key: 'MEDIUM', color: tokens.medium, label: 'Medium' },
  { key: 'HIGH', color: tokens.high, label: 'High' },
]

export default function RiskDistributionBar({ counts }) {
  const total = LEVELS.reduce((sum, l) => sum + (counts[l.key] ?? 0), 0)

  return (
    <Box>
      <Box sx={{ display: 'flex', height: 12, borderRadius: 999, overflow: 'hidden' }}>
        {LEVELS.map((l) => {
          const count = counts[l.key] ?? 0
          const pct = total > 0 ? (count / total) * 100 : 0
          return pct > 0 ? (
            <Box key={l.key} sx={{ width: `${pct}%`, backgroundColor: l.color }} />
          ) : null
        })}
        {total === 0 && <Box sx={{ width: '100%', backgroundColor: tokens.line }} />}
      </Box>

      <Box sx={{ display: 'flex', gap: 3, mt: 1.5 }}>
        {LEVELS.map((l) => (
          <Box key={l.key} sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
            <Box sx={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: l.color }} />
            <Typography variant="body2" sx={{ color: tokens.inkMuted }}>
              {l.label}{' '}
              <Box component="span" sx={{ color: tokens.ink, fontWeight: 600, fontFamily: '"IBM Plex Mono", monospace' }}>
                {counts[l.key] ?? 0}
              </Box>
            </Typography>
          </Box>
        ))}
      </Box>
    </Box>
  )
}
