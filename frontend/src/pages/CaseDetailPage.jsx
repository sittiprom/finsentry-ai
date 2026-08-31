import { useEffect, useState } from 'react'
import { Box, Paper, Typography, CircularProgress, Divider } from '@mui/material'
import { useParams } from 'react-router-dom'
import { getInvestigation } from '../api/client'
import { tokens } from '../theme'
import RiskBadge from '../components/RiskBadge'
import RiskMeter from '../components/RiskMeter'
import StatusBadge from '../components/StatusBadge'
import FindingsLedger from '../components/FindingsLedger'
import PolicyChips from '../components/PolicyChips'
import RecommendationBanner from '../components/RecommendationBanner'
import IndicatorChips from '../components/IndicatorChips'

export default function CaseDetailPage() {
  const { caseId } = useParams()
  const [report, setReport] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    setReport(null)
    setError(false)
    getInvestigation(caseId)
      .then(setReport)
      .catch(() => setError(true))
  }, [caseId])

  if (error) {
    return (
      <Paper sx={{ p: 3, borderColor: tokens.high, backgroundColor: `${tokens.high}26` }}>
        <Typography sx={{ color: tokens.high, fontWeight: 600 }}>Couldn't load case #{caseId}</Typography>
        <Typography variant="body2" sx={{ color: tokens.ink, mt: 0.5 }}>
          GET /api/investigations/{caseId} didn't return a result.
        </Typography>
      </Paper>
    )
  }

  if (!report) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
        <CircularProgress size={22} />
      </Box>
    )
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4">Case #{report.caseId}</Typography>
          <Typography
            variant="body2"
            sx={{ color: tokens.inkMuted, fontFamily: '"IBM Plex Mono", monospace', mt: 0.25 }}
          >
            Transaction {report.transactionId}
          </Typography>
        </Box>
        <StatusBadge status={report.status} />
      </Box>

      <Box sx={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: 2, mb: 3 }}>
        <Paper sx={{ p: 2.5, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 1 }}>
          <Typography sx={{ fontFamily: '"Space Grotesk", sans-serif', fontWeight: 700, fontSize: '2.25rem' }}>
            {report.riskScore ?? '—'}
          </Typography>
          {report.riskLevel && <RiskBadge level={report.riskLevel} />}
        </Paper>

        <Paper sx={{ p: 2.5 }}>
          <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6 }}>
            Investigation summary
          </Typography>
          <Typography variant="body1" sx={{ mt: 1, lineHeight: 1.6 }}>
            {report.summary ?? 'No summary available.'}
          </Typography>
        </Paper>
      </Box>

      {report.riskIndicators && (
        <Paper sx={{ p: 2.5, mb: 3 }}>
          <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6, mb: 1.5, display: 'block' }}>
            Key risk indicators
          </Typography>
          <IndicatorChips riskIndicators={report.riskIndicators} />
        </Paper>
      )}

      {report.riskScore != null && (
        <Paper sx={{ p: 2.5, mb: 3 }}>
          <RiskMeter score={report.riskScore} />
        </Paper>
      )}

      <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2, mb: 3 }}>
        <Paper sx={{ p: 2.5 }}>
          <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6, mb: 1.5, display: 'block' }}>
            Evidence
          </Typography>
          <FindingsLedger findings={report.findings} riskLevel={report.riskLevel} />
        </Paper>

        <Paper sx={{ p: 2.5 }}>
          <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6, mb: 1.5, display: 'block' }}>
            Policy matches
          </Typography>
          <PolicyChips policyMatches={report.policyMatches} />
        </Paper>
      </Box>

      {report.recommendation && (
        <>
          <Divider sx={{ mb: 3 }} />
          <RecommendationBanner recommendation={report.recommendation} />
        </>
      )}
    </Box>
  )
}
