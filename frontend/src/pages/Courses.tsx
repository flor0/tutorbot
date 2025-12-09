import { useEffect, useState } from 'react'
import {
  Box,
  Typography,
  Fab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Paper,
  CircularProgress,
  Alert,
  Stack,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import { useNavigate } from 'react-router-dom'

type Course = {
  id: string
  name: string
}

export default function Courses() {
  const [courses, setCourses] = useState<Course[] | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  const [dialogOpen, setDialogOpen] = useState(false)
  const [name, setName] = useState('')
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState<string | null>(null)

  const navigate = useNavigate()

  // helper that clears auth state and navigates to login when backend rejects us
  const handleUnauthorized = () => {
    try {
      localStorage.removeItem('user')
    } catch (e) {
      void e
    }
    try {
      window.dispatchEvent(new Event('authChange'))
    } catch (e) {
      void e
    }
    navigate('/login')
  }

  const fetchCourses = async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch('/api/courses', { credentials: 'include' })
      if (!res.ok) {
        // If the server indicates we're not authorized, clear local auth and redirect to login
        if (res.status === 401 || res.status === 403) {
          handleUnauthorized()
          return
        }
        throw new Error(`Failed to load courses: ${res.status}`)
      } else {
        const data = await res.json()
        setCourses(data ?? [])
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err ?? 'Failed to load courses')
      setError(message)
      setCourses([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void fetchCourses()
  }, [])

  const openDialog = () => {
    setName('')
    setCreateError(null)
    setDialogOpen(true)
  }
  const closeDialog = () => setDialogOpen(false)

  const handleCreate = async () => {
    if (!name.trim()) {
      setCreateError('Please enter a course name')
      return
    }
    setCreating(true)
    setCreateError(null)
    try {
      const res = await fetch('/api/courses/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ name: name.trim() }),
      })
      if (!res.ok) {
        if (res.status === 401 || res.status === 403) {
          handleUnauthorized()
          return
        }
        const text = await res.text().catch(() => '')
        throw new Error(text || `Create failed: ${res.status}`)
      }
      const created: Course = await res.json()
      // refresh the list (could also append)
      await fetchCourses()
      closeDialog()
      // navigate to the new course page (placeholder)
      if (created?.id) {
        navigate(`/courses/${created.id}`)
      }
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err ?? 'Create failed')
      setCreateError(message)
    } finally {
      setCreating(false)
    }
  }

  return (
    <Box sx={{ p: 2, pb: 10 }}>
      <Typography variant="h4">Courses</Typography>

      <Box sx={{ mt: 2 }}>
        {loading ? (
          <CircularProgress aria-label="loading-courses" />
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : courses && courses.length === 0 ? (
          <Typography>Uh, oh — no courses yet.</Typography>
        ) : (
          // Use Paper cards that span full width with rounded corners and hover highlighting
          // {/* expand this column to the full viewport width so each card fills the screen */}
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: 2,
              // centered responsive container — avoids 100vw overflow on portrait/narrow screens
              width: '100%',
              boxSizing: 'border-box',
              px: { xs: 4, sm: 4, md: 6 }, // xs:32px, sm:32px, md:48px
              maxWidth: { xs: '100%', sm: '900px', md: '1000px' },
              mx: 'auto',
            }}
          >
            {courses?.map((c) => (
              <Paper
                key={c.id}
                role="button"
                tabIndex={0}
                aria-label={`open-course-${c.id}`}
                onClick={() => navigate(`/courses/${c.id}`)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault()
                    navigate(`/courses/${c.id}`)
                  }
                }}
                elevation={1}
                sx={{
                  // card fills the container; container provides the side gutters
                  width: '100%',
                  p: { xs: 2, sm: 3 },
                  borderRadius: 2,
                  cursor: 'pointer',
                  transition: 'transform 0.08s ease, box-shadow 0.08s ease, background-color 0.12s ease',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  textAlign: 'center',
                  '&:hover': {
                    backgroundColor: 'action.hover',
                    transform: 'translateY(-3px)',
                    boxShadow: 3,
                  },
                }}
              >
                <Box sx={{ width: '100%' }}>
                  <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
                    {c.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    ID: {c.id}
                  </Typography>
                </Box>
              </Paper>
            ))}
          </Box>
        )}
      </Box>

      <Dialog open={dialogOpen} onClose={closeDialog} fullWidth maxWidth="sm">
        <DialogTitle>Create course</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {createError && <Alert severity="error">{createError}</Alert>}
            <TextField
              label="Course name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoFocus
              fullWidth
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault()
                  void handleCreate()
                }
              }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={closeDialog} disabled={creating}>
            Cancel
          </Button>
          <Button onClick={() => void handleCreate()} disabled={creating} variant="contained">
            {creating ? 'Creating...' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      <Fab
        color="primary"
        aria-label="create-course"
        onClick={openDialog}
        sx={{ position: 'fixed', bottom: 24, right: 24 }}
      >
        <AddIcon />
      </Fab>
    </Box>
  )
}
