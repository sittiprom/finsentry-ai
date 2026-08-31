import { Box, Chip, Typography } from '@mui/material'
import GavelOutlinedIcon from '@mui/icons-material/GavelOutlined'
import { tokens } from '../theme'

export default function PolicyChips({ policyMatches }) {
  if (!policyMatches || policyMatches.length === 0) {
    return (
      <Typography variant="body2" sx={{ color: tokens.inkMuted, fontStyle: 'italic' }}>
        No policy sections were matched.
      </Typography>
    )
  }

  return (
    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
      {policyMatches.map((id) => (
        <Chip
          key={id}
          icon={<GavelOutlinedIcon sx={{ fontSize: 15 }} />}
          label={id}
          variant="outlined"
          size="small"
          sx={{ borderColor: tokens.accent, color: tokens.accent }}
        />
      ))}
    </Box>
  )
}
