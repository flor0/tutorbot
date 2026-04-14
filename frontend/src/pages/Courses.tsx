import { useEffect, useState, useMemo, useCallback } from 'react'
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
  InputAdornment,
  IconButton,
} from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import SearchIcon from '@mui/icons-material/Search'
import CloseIcon from '@mui/icons-material/Close'
import { useNavigate } from 'react-router-dom'
import ErrorOutlineSharpIcon from '@mui/icons-material/ErrorOutlineSharp'
import ForwardSharpIcon from '@mui/icons-material/ForwardSharp'
import DeleteOutlineSharpIcon from '@mui/icons-material/DeleteOutlineSharp'

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

  const [query, setQuery] = useState('')
  const [debouncedQuery, setDebouncedQuery] = useState('')

  const navigate = useNavigate()

  // track course ids currently being deleted
  const [deletingIds, setDeletingIds] = useState<string[]>([])

  // helper that clears auth state and navigates to login when backend rejects us
  const handleUnauthorized = useCallback(() => {
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
  }, [navigate])

  const fetchCourses = useCallback(async () => {
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
        // set error instead of throwing to avoid local throw warning
        const msg = `Failed to load courses: ${res.status}`
        setError(msg)
        setCourses([])
        return
      }
      const data = await res.json()
      setCourses(data ?? [])
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err ?? 'Failed to load courses')
      setError(message)
      setCourses([])
    } finally {
      setLoading(false)
    }
  }, [handleUnauthorized])

  useEffect(() => {
    void fetchCourses()
  }, [fetchCourses])

  // simple debounce so typing doesn't re-render aggressively
  useEffect(() => {
    const id = setTimeout(() => setDebouncedQuery(query.trim()), 180)
    return () => clearTimeout(id)
  }, [query])

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
        setCreateError(text || `Create failed: ${res.status}`)
        setCreating(false)
        return
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

  const handleDelete = useCallback(
    async (courseId: string) => {
      if (!courseId) return
      const ok = window.confirm('Delete this course? This action cannot be undone.')
      if (!ok) return
      setDeletingIds((s) => [...s, courseId])
      try {
        const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}`, {
          method: 'DELETE',
          credentials: 'include',
        })
        if (res.status === 401 || res.status === 403) {
          handleUnauthorized()
          return
        }
        if (!res.ok) {
          const text = await res.text().catch(() => '')
          setError(text || `Failed to delete course: ${res.status}`)
          return
        }
        // remove from local list
        setCourses((prev) => (prev ? prev.filter((c) => c.id !== courseId) : prev))
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err ?? 'Delete failed')
        setError(message)
      } finally {
        setDeletingIds((s) => s.filter((id) => id !== courseId))
      }
    },
    [handleUnauthorized]
  )

  // derive visible courses: matches on top, then alphabetic
  const visibleCourses = useMemo(() => {
    if (!courses) return []
    const q = debouncedQuery.toLowerCase()
    const list = courses.slice()
    if (!q) return list.sort((a, b) => a.name.localeCompare(b.name))
    return list.sort((a, b) => {
      const aMatch = a.name.toLowerCase().includes(q)
      const bMatch = b.name.toLowerCase().includes(q)
      if (aMatch && !bMatch) return -1
      if (!aMatch && bMatch) return 1
      return a.name.localeCompare(b.name)
    })
  }, [courses, debouncedQuery])

  const matchIndex = (name: string) => {
    const q = debouncedQuery.toLowerCase()
    if (!q) return -1
    return name.toLowerCase().indexOf(q)
  }

  return (
    <Box sx={{ p: 2, pb: 10 }}>
      <Typography variant="h4">Courses</Typography>

      <Box sx={{ mt: 2, display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
        <TextField
          size="small"
          variant="outlined"
          placeholder="Search courses"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                {query ? (
                  <IconButton
                    onClick={() => setQuery('')}
                    size="small"
                    aria-label="clear search"
                  >
                    <CloseIcon fontSize="small" />
                  </IconButton>
                ) : null}
              </InputAdornment>
            ),
          }}
          sx={{ width: { xs: '100%', sm: 320 } }}
          inputProps={{ 'aria-label': 'Search courses' }}
        />

        <Typography variant="body2" color="text.secondary">
          {courses ? `${visibleCourses.length}/${courses.length} shown` : ''}
        </Typography>
      </Box>

      <Box sx={{ mt: 2 }}>
        {loading ? (
          <CircularProgress aria-label="loading-courses" />
        ) : error ? (
          <Alert severity="error">{error}</Alert>
        ) : courses && courses.length === 0 ? (
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mt: 1 }}>
            <ErrorOutlineSharpIcon />
            <Typography>No courses for this user. Click the plus button below to create your first course.</Typography>
          </Box>
        ) : (
          // scrollable column container
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: 2,
              width: '100%',
              boxSizing: 'border-box',
              px: { xs: 2, sm: 4 },
              maxWidth: { xs: '100%', sm: '900px', md: '1000px' },
              mx: 'auto',
              // make it scrollable while keeping header/search visible
              maxHeight: { xs: '60vh', sm: '65vh', md: '70vh' },
              overflowY: 'auto',
              py: 1,
            }}
          >
            {visibleCourses.length === 0 ? (
              <Typography color="text.secondary">No courses match your search.</Typography>
            ) : (
              visibleCourses.map((c) => {
                const idx = matchIndex(c.name)
                const before = idx >= 0 ? c.name.slice(0, idx) : c.name
                const match = idx >= 0 ? c.name.slice(idx, idx + debouncedQuery.length) : ''
                const after = idx >= 0 ? c.name.slice(idx + debouncedQuery.length) : ''
                return (
                  <Paper
                    key={c.id}
                    elevation={2}
                    sx={{
                      width: '100%',
                      p: { xs: 2, sm: 3 },
                      borderRadius: 2,
                      // keep a normal cursor because the row itself is not clickable
                      cursor: 'default',
                      transition: 'box-shadow 0.12s ease, background-color 0.12s ease',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      '&:hover': {
                        backgroundColor: 'action.hover',
                        boxShadow: 6,
                      },
                    }}
                  >
                    <Box sx={{ textAlign: 'left', minWidth: 0 }}>
                      <Typography variant="h6" component="div" sx={{ fontWeight: 700 }}>
                        {idx >= 0 ? (
                          <>
                            <Box component="span">{before}</Box>
                            <Box
                              component="span"
                              sx={{ bgcolor: 'primary.main', color: 'primary.contrastText', px: 0.5, borderRadius: 0.5 }}
                            >
                              {match}
                            </Box>
                            <Box component="span">{after}</Box>
                          </>
                        ) : (
                          c.name
                        )}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" noWrap>
                        ID: {c.id}
                      </Typography>
                    </Box>

                    <Box sx={{ ml: 2, flexShrink: 0, display: 'flex', gap: 1, alignItems: 'center' }}>
                      <IconButton
                        aria-label={`delete-course-${c.id}`}
                        size="small"
                        sx={{ color: 'text.secondary' }}
                        onClick={(e) => {
                          e.stopPropagation()
                          void handleDelete(c.id)
                        }}
                        disabled={deletingIds.includes(c.id)}
                      >
                        <DeleteOutlineSharpIcon fontSize="small" />
                      </IconButton>

                      <IconButton
                        aria-label={`open-course-${c.id}`}
                        size="small"
                        color="primary"
                        onClick={() => navigate(`/courses/${c.id}`)}
                      >
                        <ForwardSharpIcon fontSize="small" />
                      </IconButton>
                    </Box>
                  </Paper>
                )
              })
            )}
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
