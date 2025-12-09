import {
  Paper,
  Typography,
  CircularProgress,
  Alert,
  Stack,
  Box,
  IconButton,
  Button,
} from '@mui/material'
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf'
import DeleteIcon from '@mui/icons-material/Delete'
import { useEffect, useState, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'

type CourseMaterial = {
  id: string
  userId?: number
  courseId?: string
  filename: string
}

export default function CourseMaterials({ courseId }: { courseId?: string }) {
  const [materials, setMaterials] = useState<CourseMaterial[] | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [uploadLoading, setUploadLoading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [deletingIds, setDeletingIds] = useState<Record<string, boolean>>({})
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const fileInputRef = useRef<HTMLInputElement | null>(null)
  const navigate = useNavigate()

  const loadMaterials = useCallback(async () => {
    if (!courseId) return
    let mounted = true
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}/materials`, {
        method: 'GET',
        credentials: 'include',
        headers: { Accept: 'application/json' },
      })

      if (res.status === 403) {
        // unauthorized: clear auth and redirect to login
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
        return
      }

      if (!res.ok) {
        const t = await res.text().catch(() => '')
        if (mounted) setError(t || `HTTP ${res.status}`)
        return
      }

      const data: CourseMaterial[] = await res.json()
      if (mounted) setMaterials(data ?? [])
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      if (mounted) setError(msg || 'Failed to load materials')
    } finally {
      if (mounted) setLoading(false)
    }
    return () => {
      mounted = false
    }
  }, [courseId, navigate])

  const handleDelete = useCallback(
    async (materialId: string) => {
      if (!courseId) return
      setDeleteError(null)
      setDeletingIds((s) => ({ ...s, [materialId]: true }))
      try {
        const res = await fetch(
          `/api/courses/${encodeURIComponent(courseId)}/materials/${encodeURIComponent(materialId)}`,
          {
            method: 'DELETE',
            credentials: 'include',
          }
        )

        if (res.status === 403) {
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
          return
        }

        if (res.status === 404) {
          setDeleteError('No such course material found')
          return
        }

        if (!res.ok) {
          const t = await res.text().catch(() => '')
          setDeleteError(t || `Delete failed: HTTP ${res.status}`)
          return
        }

        // success
        await loadMaterials()
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err)
        setDeleteError(msg || 'Delete failed')
      } finally {
        setDeletingIds((s) => {
          const copy = { ...s }
          delete copy[materialId]
          return copy
        })
      }
    },
    [courseId, loadMaterials, navigate]
  )

  useEffect(() => {
    void loadMaterials()
  }, [loadMaterials])

  // Upload handlers for choose + upload buttons
  const handleChooseClick = () => {
    fileInputRef.current?.click()
  }

  const handleFileChange = (ev: React.ChangeEvent<HTMLInputElement>) => {
    const f = ev.target.files?.[0] ?? null
    if (!f) return setSelectedFile(null)
    if (f.type !== 'application/pdf' && !f.name.toLowerCase().endsWith('.pdf')) {
      setUploadError('Only PDF files are accepted')
      setSelectedFile(null)
      return
    }
    setUploadError(null)
    setSelectedFile(f)
  }

  const handleUpload = useCallback(async () => {
    if (!courseId || !selectedFile) return
    setUploadError(null)
    setUploadLoading(true)
    try {
      const form = new FormData()
      form.append('file', selectedFile)

      const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}/materials/upload`, {
        method: 'POST',
        credentials: 'include',
        body: form,
      })

      if (res.status === 403) {
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
        return
      }

      if (!res.ok) {
        const t = await res.text().catch(() => '')
        setUploadError(t || `Upload failed: HTTP ${res.status}`)
        return
      }

      // success - clear selection and refresh list
      setSelectedFile(null)
      if (fileInputRef.current) fileInputRef.current.value = ''
      await loadMaterials()
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err)
      setUploadError(msg || 'Upload failed')
    } finally {
      setUploadLoading(false)
    }
  }, [courseId, selectedFile, navigate, loadMaterials])

  return (
    <Paper sx={{ p: 2 }} elevation={1}>
      <Typography variant="h6">Materials</Typography>

      {loading && (
        <div style={{ marginTop: 12 }}>
          <CircularProgress size={20} />
        </div>
      )}

      {error && (
        <Alert severity="error" sx={{ mt: 1 }}>
          {error}
        </Alert>
      )}

      {!loading && !error && materials && materials.length === 0 && (
        <Typography sx={{ mt: 1 }}>No materials uploaded for this course.</Typography>
      )}

      {!loading && !error && materials && materials.length > 0 && (
        <Stack spacing={1} sx={{ mt: 1 }}>
          {materials.map((m) => (
            <Paper
              key={m.id}
              variant="outlined"
              sx={{
                p: 1,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 2,
                borderRadius: 1,
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, minWidth: 0 }}>
                <PictureAsPdfIcon color="error" />
                <Typography noWrap sx={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {m.filename}
                </Typography>
              </Box>

              <IconButton
                edge="end"
                aria-label={`delete ${m.filename}`}
                onClick={() => void handleDelete(m.id)}
                disabled={!!deletingIds[m.id]}
              >
                <DeleteIcon color="error" />
              </IconButton>
            </Paper>
          ))}
        </Stack>
      )}

      {deleteError && (
        <Alert severity="error" sx={{ mt: 1 }}>
          {deleteError}
        </Alert>
      )}

      {/* Upload controls: always visible when not loading or errored */}
      {!loading && !error && (
        <Box sx={{ mt: 1 }}>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <input
              ref={fileInputRef}
              type="file"
              accept="application/pdf"
              style={{ display: 'none' }}
              onChange={handleFileChange}
              aria-label="Upload PDF"
            />
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
              <Button variant="outlined" onClick={handleChooseClick} disabled={uploadLoading}>
                Choose PDF
              </Button>
              <Typography noWrap sx={{ maxWidth: 360 }}>
                {selectedFile ? selectedFile.name : 'No file selected'}
              </Typography>
              <Button variant="contained" onClick={handleUpload} disabled={!selectedFile || uploadLoading}>
                Upload
                {uploadLoading && <CircularProgress size={18} sx={{ ml: 1 }} />}
              </Button>
            </Box>
            {uploadError && (
              <Typography color="error" variant="body2" sx={{ mt: 1 }}>
                {uploadError}
              </Typography>
            )}
          </Paper>
        </Box>
      )}

      {!loading && !error && materials === null && (
        <Typography sx={{ mt: 1 }}>Materials for course {courseId} will appear here.</Typography>
      )}
    </Paper>
  )
}
