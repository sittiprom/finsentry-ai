import { useState, useRef, useEffect } from 'react'
import {
  Drawer,
  Box,
  Typography,
  IconButton,
  TextField,
  Chip,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  CircularProgress,
} from '@mui/material'
import CloseIcon from '@mui/icons-material/Close'
import SendIcon from '@mui/icons-material/Send'
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline'
import AutoAwesomeOutlinedIcon from '@mui/icons-material/AutoAwesomeOutlined'
import { askCopilot } from '../api/client'
import { tokens } from '../theme'
import { useLocation } from 'react-router-dom'

const SUGGESTIONS = [
  'What policy applies to a high-value transfer from a new device?',
  'Show recent transactions for customer C132693135',
  'Explain POLICY-DEVICE-002',
]

const DRAWER_WIDTH = 420

function TableAnswer({ table }) {
  if (!table?.rows?.length) {
    return (
      <Typography variant="body2" sx={{ color: tokens.inkMuted, fontStyle: 'italic' }}>
        No rows returned.
      </Typography>
    )
  }
  return (
    <Box sx={{ overflowX: 'auto', border: `1px solid ${tokens.line}`, borderRadius: 1.5 }}>
      <Table size="small">
        <TableHead>
          <TableRow>
            {table.columns.map((col) => (
              <TableCell key={col} sx={{ fontWeight: 600, color: tokens.inkMuted, fontSize: '0.7rem', whiteSpace: 'nowrap' }}>
                {col.toUpperCase()}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {table.rows.map((row, i) => (
            <TableRow key={i}>
              {row.map((cell, j) => (
                <TableCell key={j} sx={{ fontFamily: '"IBM Plex Mono", monospace', fontSize: '0.78rem', whiteSpace: 'nowrap' }}>
                  {cell}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  )
}

function ReportAnswer({ report }) {
  if (!report?.fields?.length) return null
  return (
    <Box sx={{ border: `1px solid ${tokens.line}`, borderRadius: 1.5, overflow: 'hidden' }}>
      {report.title && (
        <Box sx={{ px: 1.5, py: 1, borderBottom: `1px solid ${tokens.line}`, backgroundColor: `${tokens.accent}14` }}>
          <Typography sx={{ fontSize: '0.8rem', fontWeight: 600, color: tokens.ink }}>{report.title}</Typography>
        </Box>
      )}
      {report.fields.map((f, i) => (
        <Box
          key={f.label}
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            px: 1.5,
            py: 0.85,
            borderTop: i > 0 ? `1px solid ${tokens.line}` : 'none',
          }}
        >
          <Typography variant="body2" sx={{ color: tokens.inkMuted }}>{f.label}</Typography>
          <Typography variant="body2" sx={{ color: tokens.ink, fontFamily: '"IBM Plex Mono", monospace', textAlign: 'right' }}>
            {f.value}
          </Typography>
        </Box>
      ))}
    </Box>
  )
}

function AssistantMessage({ data }) {
  return (
    <Box sx={{ display: 'flex', gap: 1, mb: 2.5 }}>
      <AutoAwesomeOutlinedIcon sx={{ fontSize: 18, color: tokens.accent, mt: '3px', flexShrink: 0 }} />
      <Box sx={{ flex: 1, minWidth: 0 }}>
        <Typography variant="body2" sx={{ color: tokens.ink, lineHeight: 1.55, mb: data.responseType !== 'TEXT' ? 1 : 0 }}>
          {data.answer}
        </Typography>
        {data.responseType === 'TABLE' && <TableAnswer table={data.table} />}
        {data.responseType === 'REPORT' && <ReportAnswer report={data.report} />}
      </Box>
    </Box>
  )
}

export default function CopilotDrawer({ open, onClose }) {
  const location = useLocation()
  const caseIdMatch = location.pathname.match(/^\/cases\/(\d+)$/)
  const caseId = caseIdMatch ? caseIdMatch[1] : null
  const [messages, setMessages] = useState([])
  const [question, setQuestion] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  async function send(q) {
    const text = q ?? question
    if (!text.trim() || loading) return
    setMessages((prev) => [...prev, { role: 'user', text }])
    setQuestion('')
    setLoading(true)
    try {
      const data = await askCopilot(text, caseId ? Number(caseId) : null)
      setMessages((prev) => [...prev, { role: 'assistant', data }])
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', data: { answer: 'Something went wrong answering that — try again.', responseType: 'TEXT' } },
      ])
    } finally {
      setLoading(false)
    }
  }

  function handleClose() {
  setMessages([])
  setQuestion('')
  onClose()
}



  return (
    <Drawer anchor="right" open={open} onClose={handleClose}>
      <Box sx={{ width: DRAWER_WIDTH, height: '100%', display: 'flex', flexDirection: 'column', backgroundColor: tokens.bg }}>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 2.5,
            py: 2,
            borderBottom: `1px solid ${tokens.line}`,
          }}
        >
          <Box>
            <Typography sx={{ fontWeight: 600, color: tokens.ink }}>Investigator Copilot</Typography>
            <Typography variant="caption" sx={{ color: tokens.inkMuted }}>
              {caseId ? `Context: Case #${caseId}` : 'Powered by FinSentry AI'}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', gap: 0.5 }}>
            <IconButton size="small" onClick={() => setMessages([])} sx={{ color: tokens.inkMuted }}>
              <DeleteOutlineIcon fontSize="small" />
            </IconButton>
            <IconButton size="small" onClick={onClose} sx={{ color: tokens.inkMuted }}>
              <CloseIcon fontSize="small" />
            </IconButton>
          </Box>
        </Box>

        <Box sx={{ flex: 1, overflowY: 'auto', px: 2.5, py: 2.5 }}>
          {messages.length === 0 && (
            <Box>
              <Typography variant="body2" sx={{ color: tokens.inkMuted, mb: 2 }}>
                Ask about policies, a specific customer, or a transaction — in natural language.
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                {SUGGESTIONS.map((s) => (
                  <Chip
                    key={s}
                    label={s}
                    onClick={() => send(s)}
                    sx={{
                      justifyContent: 'flex-start',
                      height: 'auto',
                      py: 1,
                      px: 0.5,
                      fontFamily: 'inherit',
                      fontSize: '0.8rem',
                      backgroundColor: tokens.paper,
                      border: `1px solid ${tokens.line}`,
                      color: tokens.ink,
                      '& .MuiChip-label': { whiteSpace: 'normal' },
                    }}
                  />
                ))}
              </Box>
            </Box>
          )}

          {messages.map((m, i) =>
            m.role === 'user' ? (
              <Box key={i} sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2.5 }}>
                <Box
                  sx={{
                    backgroundColor: tokens.accent,
                    color: tokens.bg,
                    borderRadius: 2,
                    px: 1.5,
                    py: 1,
                    maxWidth: '85%',
                  }}
                >
                  <Typography variant="body2" sx={{ fontWeight: 500 }}>{m.text}</Typography>
                </Box>
              </Box>
            ) : (
              <AssistantMessage key={i} data={m.data} />
            )
          )}

          {loading && (
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              <CircularProgress size={14} sx={{ color: tokens.accent }} />
              <Typography variant="caption" sx={{ color: tokens.inkMuted }}>Thinking…</Typography>
            </Box>
          )}
          <div ref={bottomRef} />
        </Box>

        <Box
          component="form"
          onSubmit={(e) => { e.preventDefault(); send() }}
          sx={{ display: 'flex', gap: 1, p: 2, borderTop: `1px solid ${tokens.line}` }}
        >
          <TextField
            fullWidth
            size="small"
            placeholder="Ask anything about policies, fraud, or investigations…"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            disabled={loading}
          />
          <IconButton type="submit" disabled={loading || !question.trim()} sx={{ color: tokens.accent }}>
            <SendIcon fontSize="small" />
          </IconButton>
        </Box>
      </Box>
    </Drawer>
  )
}