import { useEffect, useMemo, useState } from 'react'
import { Box, Typography, Paper, Button, Table, TableHead, TableBody, TableRow, TableCell, CircularProgress } from '@mui/material'
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline'
import { useNavigate } from 'react-router-dom'
import { getInvestigations } from '../api/client'
import { tokens } from '../theme'
import StatCard from '../components/StatCard'
import StatusBadge from '../components/StatusBadge'
import RiskBadge from '../components/RiskBadge'
import RiskDistributionBar from '../components/RiskDistributionBar'

export default function DashboardPage() {
  const [cases, setCases] = useState(null)
  const [error, setError] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    getInvestigations()
      .then(setCases)
      .catch(() => setError(true))
  }, [])

  const stats = useMemo(() => {
    if (!cases) return null
    const openCases = cases.filter((c) => c.status === 'PENDING' || c.status === 'IN_PROGRESS').length
    const highRisk = cases.filter((c) => c.riskLevel === 'HIGH').length
    const pendingReview = cases.filter((c) => c.recommendation === 'REVIEW').length
    const completed = cases.filter((c) => c.status === 'COMPLETED').length
    const distribution = { LOW: 0, MEDIUM: 0, HIGH: 0 }
    cases.forEach((c) => {
      if (c.riskLevel) distribution[c.riskLevel] = (distribution[c.riskLevel] ?? 0) + 1
    })
    const recent = [...cases]
      .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
      .slice(0, 8)
    return { openCases, highRisk, pendingReview, completed, distribution, recent }
  }, [cases])

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ mb: 0.5 }}>
            Dashboard
          </Typography>
          <Typography variant="body2" sx={{ color: tokens.inkMuted }}>
            An overview of investigation activity.
          </Typography>
        </Box>
        <Button
          variant="contained"
          disableElevation
          startIcon={<AddCircleOutlineIcon />}
          onClick={() => navigate('/investigate')}
        >
          Investigate transaction
        </Button>
      </Box>

      {error && (
        <Paper sx={{ p: 3, mb: 3, borderColor: tokens.high, backgroundColor: `${tokens.high}26` }}>
          <Typography sx={{ color: tokens.high, fontWeight: 600 }}>Couldn't load dashboard data</Typography>
          <Typography variant="body2" sx={{ color: tokens.ink, mt: 0.5 }}>
            GET /api/investigations didn't return a result. Make sure the backend is running and
            the endpoint has been implemented.
          </Typography>
        </Paper>
      )}

      {!cases && !error && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress size={22} />
        </Box>
      )}

      {stats && (
        <>
          <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
            <StatCard label="Open cases" value={stats.openCases} accentColor={tokens.accent} />
            <StatCard label="High risk cases" value={stats.highRisk} accentColor={tokens.high} />
            <StatCard label="Recommended for review" value={stats.pendingReview} accentColor={tokens.medium} />
            <StatCard label="Completed investigations" value={stats.completed} accentColor={tokens.low} />
          </Box>

          <Paper sx={{ p: 2.5, mb: 3 }}>
            <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6, mb: 1.5, display: 'block' }}>
              Risk distribution
            </Typography>
            <RiskDistributionBar counts={stats.distribution} />
          </Paper>

          <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6, mb: 1, display: 'block' }}>
            Recent investigation cases
          </Typography>

          {stats.recent.length === 0 ? (
            <Paper sx={{ p: 3 }}>
              <Typography sx={{ color: tokens.inkMuted }}>No investigations yet.</Typography>
            </Paper>
          ) : (
            <Paper sx={{ overflow: 'hidden' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    {['Case', 'Transaction', 'Risk', 'Status'].map((h) => (
                      <TableCell key={h} sx={{ fontWeight: 600, color: tokens.inkMuted, fontSize: '0.75rem' }}>
                        {h.toUpperCase()}
                      </TableCell>
                    ))}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {stats.recent.map((c) => (
                    <TableRow key={c.caseId} hover onClick={() => navigate(`/cases/${c.caseId}`)} sx={{ cursor: 'pointer' }}>
                      <TableCell sx={{ fontFamily: '"IBM Plex Mono", monospace' }}>#{c.caseId}</TableCell>
                      <TableCell sx={{ fontFamily: '"IBM Plex Mono", monospace' }}>{c.transactionId}</TableCell>
                      <TableCell>{c.riskLevel ? <RiskBadge level={c.riskLevel} /> : '—'}</TableCell>
                      <TableCell><StatusBadge status={c.status} /></TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Paper>
          )}
        </>
      )}
    </Box>
  )
}
