import { useEffect, useState } from 'react'
import { Box, Paper, TextField, Button, Typography, CircularProgress, List, ListItem, ListItemIcon, ListItemText } from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import { useNavigate } from 'react-router-dom'
import { investigateTransaction, getConfig } from '../api/client'
import { tokens } from '../theme'

const CHECKS = [
  'Transaction details',
  'Customer context',
  'Login & device activity',
  'Risk indicators',
  'Relevant fraud policies',
]

export default function InvestigatePage() {
  const [transactionId, setTransactionId] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [demoMode, setDemoMode] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    getConfig()
      .then((cfg) => setDemoMode(Boolean(cfg.demoMode)))
      .catch(() => setDemoMode(false)) // if /api/config isn't implemented yet, don't block anything
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    if (!transactionId.trim() || demoMode) return
    setLoading(true)
    setError(null)
    try {
      const data = await investigateTransaction(transactionId.trim())
      navigate(`/cases/${data.caseId}`)
    } catch (err) {
      const status = err?.response?.status
      if (status === 403) {
        setError(err?.response?.data?.message ?? 'New investigations are disabled on this deployment.')
      } else if (status === 404) {
        setError(`Transaction ${transactionId} was not found.`)
      } else {
        setError('The investigation could not be completed. Check that the backend is running and try again.')
      }
      setLoading(false)
    }
  }

  return (
    <Box sx={{ maxWidth: 560 }}>
      <Typography variant="h4" sx={{ mb: 0.5 }}>
        Investigate a transaction
      </Typography>
      <Typography variant="body2" sx={{ color: tokens.inkMuted, mb: 3 }}>
        Enter a transaction ID to gather evidence and generate a structured investigation report.
      </Typography>

      {demoMode && (
        <Paper sx={{ p: 2.5, mb: 3, borderColor: tokens.medium, backgroundColor: `${tokens.medium}26` }}>
          <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
            <LockOutlinedIcon sx={{ color: tokens.medium, mt: '2px' }} />
            <Box>
              <Typography sx={{ fontWeight: 600, color: tokens.ink }}>Read-only demo</Typography>
              <Typography variant="body2" sx={{ color: tokens.inkMuted, mt: 0.25 }}>
                New investigations are disabled on this public deployment to avoid unexpected API
                costs. Browse the existing cases from the sidebar to see full investigation reports.
              </Typography>
            </Box>
          </Box>
        </Paper>
      )}

      <Paper sx={{ p: 2.5, mb: 3 }}>
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', gap: 1.5 }}>
          <TextField
            fullWidth
            size="small"
            label="Transaction ID"
            placeholder="e.g. 1237396"
            value={transactionId}
            onChange={(e) => setTransactionId(e.target.value)}
            InputProps={{ sx: { fontFamily: '"IBM Plex Mono", monospace' } }}
            disabled={demoMode}
            autoFocus
          />
          <Button
            type="submit"
            variant="contained"
            disableElevation
            disabled={loading || demoMode}
            startIcon={loading ? <CircularProgress size={16} color="inherit" /> : <SearchIcon />}
            sx={{ minWidth: 150 }}
          >
            {loading ? 'Investigating' : 'Investigate'}
          </Button>
        </Box>
      </Paper>

      {error && (
        <Paper sx={{ p: 2.5, mb: 3, borderColor: tokens.high, backgroundColor: `${tokens.high}26` }}>
          <Typography sx={{ color: tokens.high, fontWeight: 600 }}>Investigation failed</Typography>
          <Typography variant="body2" sx={{ color: tokens.ink, mt: 0.5 }}>
            {error}
          </Typography>
        </Paper>
      )}

      <Paper sx={{ p: 2.5 }}>
        <Typography variant="overline" sx={{ color: tokens.inkMuted, letterSpacing: 0.6 }}>
          What FinSentry will check
        </Typography>
        <List dense sx={{ mt: 0.5 }}>
          {CHECKS.map((label) => (
            <ListItem key={label} disableGutters sx={{ py: 0.4 }}>
              <ListItemIcon sx={{ minWidth: 30 }}>
                <CheckCircleOutlineIcon sx={{ fontSize: 18, color: tokens.low }} />
              </ListItemIcon>
              <ListItemText primary={label} primaryTypographyProps={{ variant: 'body2' }} />
            </ListItem>
          ))}
        </List>
      </Paper>
    </Box>
  )
}
