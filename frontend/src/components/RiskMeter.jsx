import { Box, Typography } from '@mui/material'
import { tokens } from '../theme'

const ZONES = [
  { from: 0, to: 35, color: tokens.low, label: 'LOW' },
  { from: 35, to: 70, color: tokens.medium, label: 'MEDIUM' },
  { from: 70, to: 100, color: tokens.high, label: 'HIGH' },
]

// A horizontal, zone-banded meter — reads like an instrument reading rather
// than a dashboard decoration. The marker sits at the exact computed score;
// the bands are drawn from the same LOW/MEDIUM/HIGH thresholds RiskService
// uses, so the visual and the number can never disagree.
export default function RiskMeter({ score }) {
  const clamped = Math.max(0, Math.min(100, score ?? 0))

  return (
    <Box sx={{ width: '100%' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 0.75 }}>
        <Typography variant="overline" sx={{ letterSpacing: 1, color: tokens.inkMuted }}>
          Risk score
        </Typography>
        <Typography
          sx={{ fontFamily: '"IBM Plex Mono", monospace', fontSize: '1.1rem', fontWeight: 600 }}
        >
          {clamped}
          <Box component="span" sx={{ color: tokens.inkMuted, fontSize: '0.8rem' }}>
            {' '}/100
          </Box>
        </Typography>
      </Box>

      <Box sx={{ position: 'relative', height: 10, borderRadius: 999, overflow: 'hidden', display: 'flex' }}>
        {ZONES.map((z) => (
          <Box
            key={z.label}
            sx={{
              flex: z.to - z.from,
              backgroundColor: z.color,
              opacity: 0.28,
            }}
          />
        ))}
        <Box
          sx={{
            position: 'absolute',
            top: -3,
            left: `${clamped}%`,
            transform: 'translateX(-50%)',
            width: 4,
            height: 16,
            borderRadius: 2,
            backgroundColor: tokens.ink,
          }}
        />
      </Box>

      <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 0.5 }}>
        {ZONES.map((z) => (
          <Typography
            key={z.label}
            variant="caption"
            sx={{ fontFamily: '"IBM Plex Mono", monospace', color: tokens.inkMuted }}
          >
            {z.label}
          </Typography>
        ))}
      </Box>
    </Box>
  )
}
