import { Box, Typography } from '@mui/material'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline'
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined'
import ReportProblemOutlinedIcon from '@mui/icons-material/ReportProblemOutlined'
import { tokens } from '../theme'

const CONFIG = {
  NO_ACTION: {
    color: tokens.low,
    icon: CheckCircleOutlineIcon,
    label: 'No action',
    copy: 'No significant anomalies were found. No further review is required.',
  },
  REVIEW: {
    color: tokens.medium,
    icon: VisibilityOutlinedIcon,
    label: 'Review',
    copy: 'One or more unusual characteristics were found. An analyst should review this case.',
  },
  ESCALATE_FOR_MANUAL_REVIEW: {
    color: tokens.high,
    icon: ReportProblemOutlinedIcon,
    label: 'Escalate for manual review',
    copy: 'Multiple high-risk indicators or a strong policy match were found. Escalate to a senior investigator.',
  },
}

// Deliberately does not say "fraud" or make an accusation — this reflects
// the system's actual role: surface evidence, recommend a next step, leave
// the determination to a human investigator.
export default function RecommendationBanner({ recommendation }) {
  const cfg = CONFIG[recommendation] ?? CONFIG.REVIEW
  const Icon = cfg.icon

  return (
    <Box
      sx={{
        display: 'flex',
        gap: 1.5,
        alignItems: 'flex-start',
        p: 2,
        borderRadius: 2,
        border: `1px solid ${cfg.color}`,
        backgroundColor: `${cfg.color}26`,
      }}
    >
      <Icon sx={{ color: cfg.color, mt: '2px' }} />
      <Box>
        <Typography sx={{ fontWeight: 600, color: tokens.ink }}>{cfg.label}</Typography>
        <Typography variant="body2" sx={{ color: tokens.inkMuted, mt: 0.25 }}>
          {cfg.copy}
        </Typography>
      </Box>
    </Box>
  )
}
