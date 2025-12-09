import { Box, Typography, Button, ButtonGroup } from '@mui/material'
import { useParams } from 'react-router-dom'
import { useState, useEffect } from 'react'
import CourseTopics from './CourseTopics'
import CourseQuizzes from './CourseQuizzes'
import CourseMaterials from './CourseMaterials'

export default function CourseDetail() {
  const { id } = useParams()
  const [tab, setTab] = useState<'topics' | 'quizzes' | 'materials'>('topics')
  const [course, setCourse] = useState<{ id: string; name: string; userId?: number } | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    let mounted = true
    const fetchCourse = async () => {
      setLoading(true)
      setError(null)
      try {
        const res = await fetch(`/api/courses/${encodeURIComponent(id)}`, {
          method: 'GET',
          credentials: 'include',
          headers: { Accept: 'application/json' },
        })
        if (!res.ok) {
          // set an error and stop
          const text = await res.text().catch(() => '')
          if (mounted) setError(text || `HTTP ${res.status}`)
          return
        }
        const data = await res.json()
        if (mounted) setCourse(data)
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err)
        if (mounted) setError(msg || 'Failed to load course')
      } finally {
        if (mounted) setLoading(false)
      }
    }
    fetchCourse()
    return () => {
      mounted = false
    }
  }, [id])

  return (
    <>
      {/* Fixed, full-bleed button strip so it's not constrained by #root */}
      <Box
        component="section"
        aria-label="course top navigation"
        sx={{
          position: 'fixed',
          left: 0,
          right: 0,
          // place directly under the AppBar toolbar (MUI toolbar heights: 56 on xs, 64 on sm+)
          top: { xs: '56px', sm: '64px' },
          // make full viewport width
          width: '100%',
          // sit below AppBar but above content
          zIndex: (t) => (t.zIndex.appBar ? t.zIndex.appBar - 1 : 1200),
          bgcolor: 'background.default',
          // inner horizontal padding to visually align with content
          px: { xs: '0.75rem', sm: '2rem' },
          py: 1,
          borderBottom: 1,
          borderColor: 'divider',
          // keep the button group centered within the common app width
          display: 'flex',
          justifyContent: 'center',
        }}
      >
        <ButtonGroup
          variant="outlined"
          aria-label="course navigation"
          sx={{ display: 'flex', gap: 1, width: '100%', maxWidth: 1280 }}
        >
          <Button
            onClick={() => setTab('topics')}
            variant={tab === 'topics' ? 'contained' : 'outlined'}
            aria-pressed={tab === 'topics'}
            sx={{ flex: 1, '&:not(:last-of-type)': { borderRight: 1, borderColor: 'divider' } }}
          >
            Topics
          </Button>
          <Button
            onClick={() => setTab('quizzes')}
            variant={tab === 'quizzes' ? 'contained' : 'outlined'}
            aria-pressed={tab === 'quizzes'}
            sx={{ flex: 1, '&:not(:last-of-type)': { borderRight: 1, borderColor: 'divider' } }}
          >
            Quizzes
          </Button>
          <Button
            onClick={() => setTab('materials')}
            variant={tab === 'materials' ? 'contained' : 'outlined'}
            aria-pressed={tab === 'materials'}
            sx={{ flex: 1, borderColor: 'divider' }}
          >
            Materials
          </Button>
        </ButtonGroup>
      </Box>

      {/* Full-bleed content that doesn't overflow: use left:50% with negative 50vw margins
          (avoids width:100vw which includes scrollbar width). */}
      <Box
        sx={{
          position: 'relative',
          left: '50%',
          right: '50%',
          marginLeft: '-50vw',
          marginRight: '-50vw',
          // ensure the element never extends past the viewport (prevents scrollbar-caused overflow)
          maxWidth: '100vw',
          overflowX: 'hidden',
          boxSizing: 'border-box',
          // vertical padding and responsive horizontal padding to match app spacing
          py: 2,
          px: { xs: '0.75rem', sm: '2rem' },
        }}
        aria-label={'course detail content'}
      >
        <Typography variant="h4">{course?.name ?? (loading ? 'Loading…' : 'Course')}</Typography>
        {error && (
          <Typography variant="body2" color="error" sx={{ mt: 1 }}>
            {error}
          </Typography>
        )}

        {/* Section content */}
        <Box sx={{ mt: 2 }}>
          {tab === 'topics' && <CourseTopics courseId={id} />}
          {tab === 'quizzes' && <CourseQuizzes courseId={id} />}
          {tab === 'materials' && <CourseMaterials courseId={id} />}
        </Box>
      </Box>
    </>
  )
}
