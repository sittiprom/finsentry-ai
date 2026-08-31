import { Box, Chip } from '@mui/material'
import { tokens } from '../theme'

// Renders only the indicators that actually fired — matches the mockup's
// "Key Risk Indicators" row. Gracefully renders nothing if the backend
// doesn't include riskIndicators on this case (it's an optional field).
export default function IndicatorChips({ riskIndicators }) {
  if (!riskIndicators) return null

  const active = []
  if (riskIndicators.newDevice) active.push('New device')
  if (riskIndicators.unusualCountry) active.push('Unusual country')
  if (riskIndicators.rapidTransactions) active.push('Rapid transactions')
  if (riskIndicators.balanceDrained) active.push('Balance drained')
  if (riskIndicators.amountAnomalyMultiplier && riskIndicators.amountAnomalyMultiplier >= 5) {
    active.push(`Amount ${riskIndicators.amountAnomalyMultiplier}× average`)
  }

  if (active.length === 0) return null

  return (
    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
      {active.map((label) => (
        <Chip
          key={label}
          label={label}
          size="small"
          sx={{ backgroundColor: `${tokens.high}26`, color: tokens.high, border: `1px solid ${tokens.high}` }}
        />
      ))}
    </Box>
  )
}
