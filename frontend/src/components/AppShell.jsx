import { useEffect, useState } from 'react'
import { Box, Typography, List, ListItemButton, ListItemIcon, ListItemText, IconButton, Avatar, Badge, ButtonBase } from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import ShieldOutlinedIcon from '@mui/icons-material/ShieldOutlined'
import GridViewOutlinedIcon from '@mui/icons-material/GridViewOutlined'
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined'
import LockOutlinedIcon from '@mui/icons-material/LockOutlined'
import NotificationsNoneOutlinedIcon from '@mui/icons-material/NotificationsNoneOutlined'
import { getConfig } from '../api/client'
import { tokens } from '../theme'
import CopilotDrawer from './CopilotDrawer'

const NAV = [
  { label: 'Dashboard', path: '/', icon: GridViewOutlinedIcon },
  { label: 'Investigate', path: '/investigate', icon: SearchOutlinedIcon },
  { label: 'Cases', path: '/cases', icon: FolderOutlinedIcon },
]

const SIDEBAR_WIDTH = 220
const HEADER_HEIGHT = 60

export default function AppShell({ children }) {
  const location = useLocation()
  const navigate = useNavigate()
  const [demoMode, setDemoMode] = useState(false)
  const [copilotOpen, setCopilotOpen] = useState(false)

  useEffect(() => {
    getConfig()
      .then((cfg) => setDemoMode(Boolean(cfg.demoMode)))
      .catch(() => setDemoMode(false))
  }, [])

  const isActive = (path) =>
    path === '/' ? location.pathname === '/' : location.pathname.startsWith(path)

  const activePage = NAV.find((n) => isActive(n.path))?.label ?? ''

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: tokens.bg }}>
      {/* Sidebar */}
      <Box
        sx={{
          width: SIDEBAR_WIDTH,
          flexShrink: 0,
          backgroundColor: tokens.sidebar,
          borderRight: `1px solid ${tokens.line}`,
          display: 'flex',
          flexDirection: 'column',
          py: 3,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 2.5, mb: 4 }}>
          <ShieldOutlinedIcon sx={{ color: tokens.accent }} />
          <Typography
            sx={{ fontFamily: '"Space Grotesk", sans-serif', fontWeight: 700, color: tokens.ink, fontSize: '1.05rem' }}
          >
            FinSentry AI
          </Typography>
        </Box>

        <List sx={{ px: 1.5 }}>
          {NAV.map(({ label, path, icon: Icon }) => {
            const active = isActive(path)
            return (
              <ListItemButton
                key={path}
                onClick={() => navigate(path)}
                selected={active}
                sx={{
                  borderRadius: 2,
                  mb: 0.5,
                  color: active ? tokens.ink : tokens.inkMuted,
                  '&.Mui-selected': { backgroundColor: `${tokens.accent}26` },
                  '&.Mui-selected:hover': { backgroundColor: `${tokens.accent}33` },
                  '&:hover': { backgroundColor: 'rgba(255,255,255,0.04)' },
                }}
              >
                <ListItemIcon sx={{ minWidth: 34, color: active ? tokens.accent : tokens.inkMuted }}>
                  <Icon fontSize="small" />
                </ListItemIcon>
                <ListItemText
                  primary={label}
                  primaryTypographyProps={{ fontSize: '0.9rem', fontWeight: active ? 600 : 500 }}
                />
              </ListItemButton>
            )
          })}
        </List>
      </Box>

      {/* Right column: header + content */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        {/* Top header bar */}
        <Box
          sx={{
            height: HEADER_HEIGHT,
            flexShrink: 0,
            borderBottom: `1px solid ${tokens.line}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 3,
          }}
        >
          <Typography variant="body2" sx={{ color: tokens.inkMuted }}>
            {activePage}
          </Typography>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconButton size="small" sx={{ color: tokens.inkMuted }}>
              <Badge variant="dot" color="error" overlap="circular">
                <NotificationsNoneOutlinedIcon fontSize="small" />
              </Badge>
            </IconButton>

            <Box sx={{ width: 1, height: 24, backgroundColor: tokens.line }} />

            <ButtonBase
              onClick={() => setCopilotOpen(true)}
              sx={{ display: 'flex', alignItems: 'center', gap: 1, borderRadius: 2, px: 1, py: 0.5 }}
            >
              <Typography variant="body2" sx={{ color: tokens.ink, fontWeight: 500 }}>
                Investigator
              </Typography>
              <Avatar
                sx={{
                  width: 32,
                  height: 32,
                  bgcolor: tokens.accent,
                  color: tokens.bg,
                  fontSize: '0.78rem',
                  fontWeight: 700,
                  fontFamily: '"Space Grotesk", sans-serif',
                }}
              >
                IN
              </Avatar>
            </ButtonBase>
          </Box>
        </Box>

        {demoMode && (
          <Box
            sx={{
              backgroundColor: `${tokens.medium}1F`,
              borderBottom: `1px solid ${tokens.medium}`,
              px: 3,
              py: 1,
              display: 'flex',
              alignItems: 'center',
              gap: 1,
            }}
          >
            <LockOutlinedIcon sx={{ fontSize: 16, color: tokens.medium }} />
            <Typography variant="caption" sx={{ color: tokens.ink }}>
              Read-only demo — new investigations are disabled on this deployment.
            </Typography>
          </Box>
        )}

        <Box sx={{ px: 4, py: 4, maxWidth: 1040, width: '100%' }}>{children}</Box>
      </Box>

      <CopilotDrawer open={copilotOpen} onClose={() => setCopilotOpen(false)} />
    </Box>
  )
}
