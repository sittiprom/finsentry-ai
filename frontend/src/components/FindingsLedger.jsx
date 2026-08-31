import { Box, Typography } from '@mui/material'
import { tokens } from '../theme'

// Findings read like itemized case-file entries rather than a plain bullet
// list — a monospace index, a left rule in the case's risk color, the
// finding text itself. It's a small structural device, but it encodes
// something true: these are numbered, ordered pieces of evidence.
export default function FindingsLedger({ findings, riskLevel }) {
  const ruleColor = { LOW: tokens.low, MEDIUM: tokens.medium, HIGH: tokens.high }[riskLevel] ?? tokens.line

  if (!findings || findings.length === 0) {
    return (
      <Typography variant="body2" sx={{ color: tokens.inkMuted, fontStyle: 'italic' }}>
        No findings were recorded for this transaction.
      </Typography>
    )
  }

  return (
    <Box component="ol" sx={{ listStyle: 'none', m: 0, p: 0 }}>
      {findings.map((finding, i) => (
        <Box
          key={i}
          sx={{
            display: 'flex',
            gap: 1.5,
            borderLeft: `2px solid ${ruleColor}`,
            pl: 1.5,
            py: 0.85,
          }}
        >
          <Typography
            sx={{
              fontFamily: '"IBM Plex Mono", monospace',
              fontSize: '0.72rem',
              color: tokens.inkMuted,
              minWidth: 30,
              pt: '2px',
            }}
          >
            F{String(i + 1).padStart(2, '0')}
          </Typography>
          <Typography variant="body2" sx={{ color: tokens.ink, lineHeight: 1.55 }}>
            {finding}
          </Typography>
        </Box>
      ))}
    </Box>
  )
}
