import { useEffect, useMemo, useState } from 'react'
import {
  Box,
  Paper,
  Typography,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  TextField,
  MenuItem,
  CircularProgress,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { getInvestigations } from '../api/client'
import { tokens } from '../theme'
import RiskBadge from '../components/RiskBadge'
import StatusBadge from '../components/StatusBadge'

const RISK_OPTIONS = ['', 'LOW', 'MEDIUM', 'HIGH']
const STATUS_OPTIONS = ['', 'PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED']

export default function CasesPage() {
  const [cases, setCases] = useState(null)
  const [error, setError] = useState(false)
  const [search, setSearch] = useState('')
  const [risk, setRisk] = useState('')
  const [status, setStatus] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    getInvestigations()
      .then(setCases)
      .catch(() => setError(true))
  }, [])

  const filtered = useMemo(() => {
    if (!cases) return []
    return cases.filter((c) => {
      if (risk && c.riskLevel !== risk) return false
      if (status && c.status !== status) return false
      if (search) {
        const q = search.trim().toLowerCase()
        const matches =
          String(c.caseId).includes(q) || String(c.transactionId).toLowerCase().includes(q)
        if (!matches) return false
      }
      return true
    })
  }, [cases, search, risk, status])

  return (
    <Box>
      <Typography variant="h4" sx={{ mb: 0.5 }}>
        Investigation cases
      </Typography>
      <Typography variant="body2" sx={{ color: tokens.inkMuted, mb: 3 }}>
        All investigations, past and in progress.
      </Typography>

      <Paper sx={{ p: 2, mb: 2, display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
        <TextField
          size="small"
          label="Search"
          placeholder="Case or transaction ID"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{ minWidth: 220 }}
        />
        <TextField select size="small" label="Risk" value={risk} onChange={(e) => setRisk(e.target.value)} sx={{ minWidth: 150 }}>
          {RISK_OPTIONS.map((r) => (
            <MenuItem key={r} value={r}>{r || 'All'}</MenuItem>
          ))}
        </TextField>
        <TextField select size="small" label="Status" value={status} onChange={(e) => setStatus(e.target.value)} sx={{ minWidth: 170 }}>
          {STATUS_OPTIONS.map((s) => (
            <MenuItem key={s} value={s}>{s ? s.replace('_', ' ') : 'All'}</MenuItem>
          ))}
        </TextField>
      </Paper>

      {error && (
        <Paper sx={{ p: 3, borderColor: tokens.high, backgroundColor: `${tokens.high}26` }}>
          <Typography sx={{ color: tokens.high, fontWeight: 600 }}>Couldn't load cases</Typography>
          <Typography variant="body2" sx={{ color: tokens.ink, mt: 0.5 }}>
            GET /api/investigations didn't return a result. Make sure the backend is running.
          </Typography>
        </Paper>
      )}

      {!cases && !error && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress size={22} />
        </Box>
      )}

      {cases && !error && filtered.length === 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography sx={{ fontWeight: 600 }}>No cases match these filters</Typography>
        </Paper>
      )}

      {cases && !error && filtered.length > 0 && (
        <Paper sx={{ overflow: 'hidden' }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                {['Case', 'Transaction', 'Risk', 'Score', 'Recommendation', 'Status'].map((h) => (
                  <TableCell key={h} sx={{ fontWeight: 600, color: tokens.inkMuted, fontSize: '0.75rem' }}>
                    {h.toUpperCase()}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((c) => (
                <TableRow key={c.caseId} hover onClick={() => navigate(`/cases/${c.caseId}`)} sx={{ cursor: 'pointer' }}>
                  <TableCell sx={{ fontFamily: '"IBM Plex Mono", monospace' }}>#{c.caseId}</TableCell>
                  <TableCell sx={{ fontFamily: '"IBM Plex Mono", monospace' }}>{c.transactionId}</TableCell>
                  <TableCell>{c.riskLevel ? <RiskBadge level={c.riskLevel} /> : '—'}</TableCell>
                  <TableCell sx={{ fontFamily: '"IBM Plex Mono", monospace' }}>{c.riskScore ?? '—'}</TableCell>
                  <TableCell sx={{ fontSize: '0.8rem' }}>{c.recommendation ?? '—'}</TableCell>
                  <TableCell><StatusBadge status={c.status} /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  )
}
