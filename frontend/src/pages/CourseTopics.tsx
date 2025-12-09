import { Paper, Typography, Stack, Box, TextField, Button, IconButton, List, ListItem, ListItemText, ListItemButton, CircularProgress, Alert } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import { useEffect, useState, useCallback } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

type Topic = {
  id: string
  name: string
  summary?: string
}

export default function CourseTopics({ courseId: propCourseId }: { courseId?: string } = {}) {
  const [topics, setTopics] = useState<Topic[] | null>(null)
  const [selectedTopicId, setSelectedTopicId] = useState<string | null>(null)
  const [selectedSummary, setSelectedSummary] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [deletingIds, setDeletingIds] = useState<Record<string, boolean>>({})
  const navigate = useNavigate()
  const params = useParams()
  const courseId = propCourseId ?? params.courseId

  const loadTopics = useCallback(async () => {
    if (!courseId) {
      // If no courseId available, bail silently (parent may not want topics loaded yet)
      setTopics([])
      return
    }
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}/topics`, { method: 'GET', credentials: 'include', headers: { Accept: 'application/json' } })
      if (res.status === 403) {
        try { localStorage.removeItem('user') } catch (e) { void e }
        try { window.dispatchEvent(new Event('authChange')) } catch (e) { void e }
        navigate('/login')
        return
      }
      if (!res.ok) {
        const t = await res.text().catch(() => '')
        setError(t || `HTTP ${res.status}`)
        return
      }
      const data: Topic[] = await res.json()
      setTopics(data ?? [])
      // clear selection if the selected topic no longer exists
      setSelectedTopicId((prev) => {
        if (!prev) return prev
        const found = (data ?? []).some((d) => d.id === prev)
        if (!found) return null
        return prev
      })
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      setError(msg || 'Failed to load topics')
    } finally {
      setLoading(false)
    }
  }, [navigate, courseId])

  useEffect(() => { void loadTopics() }, [loadTopics, courseId])

  // keep selectedSummary in sync when topics or selection changes
  useEffect(() => {
    if (!selectedTopicId || !topics) {
      setSelectedSummary('')
      return
    }
    const t = topics.find((x) => x.id === selectedTopicId)
    setSelectedSummary(t?.summary ?? '')
  }, [selectedTopicId, topics])

  const handleCreate = useCallback(async () => {
    if (!name.trim()) return
    setCreateError(null)
    setCreating(true)
    try {
      if (!courseId) {
        setCreateError('Missing courseId')
        return
      }
      const body = { name: name.trim() }
      const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}/topics`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
      })
      if (res.status === 403) {
        try { localStorage.removeItem('user') } catch (e) { void e }
        try { window.dispatchEvent(new Event('authChange')) } catch (e) { void e }
        navigate('/login')
        return
      }
      if (res.status === 409) {
        const t = await res.text().catch(() => '')
        setCreateError(t || 'Topic with this name already exists')
        return
      }
      if (!res.ok) {
        const t = await res.text().catch(() => '')
        setCreateError(t || `Create failed: HTTP ${res.status}`)
        return
      }
      // created, refresh
      setName('')
      await loadTopics()
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      setCreateError(msg || 'Failed to create topic')
    } finally {
      setCreating(false)
    }
  }, [name, loadTopics, navigate, courseId])

  const handleDelete = useCallback(async (id: string) => {
    setCreateError(null)
    setDeletingIds((s) => ({ ...s, [id]: true }))
    try {
      if (!courseId) {
        setCreateError('Missing courseId')
        setDeletingIds((s) => { const copy = { ...s }; delete copy[id]; return copy })
        return
      }
      const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}/topics/${encodeURIComponent(id)}`, { method: 'DELETE', credentials: 'include' })
      if (res.status === 403) {
        try { localStorage.removeItem('user') } catch (e) { void e }
        try { window.dispatchEvent(new Event('authChange')) } catch (e) { void e }
        navigate('/login')
        return
      }
      if (!res.ok && res.status !== 204) {
        const t = await res.text().catch(() => '')
        setCreateError(t || `Delete failed: HTTP ${res.status}`)
        return
      }
      await loadTopics()
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      setCreateError(msg || 'Failed to delete topic')
    } finally {
      setDeletingIds((s) => { const copy = { ...s }; delete copy[id]; return copy })
      // if deleted topic was selected, clear selection
      setSelectedTopicId((prev) => (prev === id ? null : prev))
    }
  }, [loadTopics, navigate, courseId])

  const handleSelect = (t: Topic) => {
    setSelectedTopicId((prev) => (prev === t.id ? null : t.id))
    // selectedSummary is synced in useEffect; but set immediately for snappy UX
    setSelectedSummary(t.summary ?? '')
  }

  const handleSummaryChange = (value: string) => {
    setSelectedSummary(value)
    // update local topics state so UI reflects the change
    setTopics((prev) => (prev ? prev.map((p) => (p.id === selectedTopicId ? { ...p, summary: value } : p)) : prev))
  }

  return (
    <Paper sx={{ p: 2 }} elevation={1}>
      <Typography variant="h6">Topics</Typography>

      <Box sx={{ mt: 2 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} alignItems="center">
          <TextField label="New topic" value={name} onChange={(e) => setName(e.target.value)} size="small" sx={{ flex: 1 }} />
          <Button variant="contained" onClick={() => void handleCreate()} disabled={!name.trim() || creating}>
            {creating ? <CircularProgress size={18} /> : 'Create'}
          </Button>
        </Stack>
        {createError && <Alert severity="error" sx={{ mt: 1 }}>{createError}</Alert>}
      </Box>

      <Box sx={{ mt: 2 }}>
        {loading && <CircularProgress size={20} />}
        {error && <Alert severity="error">{error}</Alert>}

        {!loading && !error && topics && (
          <List>
            {topics.map((t) => (
              <ListItem key={t.id} disablePadding secondaryAction={
                <IconButton edge="end" aria-label={`delete ${t.name}`} onClick={() => void handleDelete(t.id)} disabled={deletingIds[t.id]}>
                  <DeleteIcon />
                </IconButton>
              }>
                <ListItemButton selected={t.id === selectedTopicId} onClick={() => handleSelect(t)}>
                  <ListItemText primary={t.name} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        )}

        {!loading && !error && topics && topics.length === 0 && (
          <Typography sx={{ mt: 1 }}>No topics yet. Create one above.</Typography>
        )}
      </Box>

      {/* Summary editor for the selected topic */}
      <Box sx={{ mt: 2 }}>
        <Paper variant="outlined" sx={{ p: 2 }}>
          <Typography variant="subtitle1">Summary</Typography>
          <TextField
            multiline
            minRows={6}
            fullWidth
            value={selectedSummary}
            onChange={(e) => handleSummaryChange(e.target.value)}
            placeholder={selectedTopicId ? 'Enter a summary for the selected topic...' : 'Select a topic to view its summary.'}
            disabled={!selectedTopicId}
            sx={{ mt: 1 }}
          />
        </Paper>
      </Box>
    </Paper>
  )
}
