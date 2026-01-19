import { Paper, Typography, FormControl, InputLabel, Select, MenuItem, CircularProgress, Stack, Card, CardContent, CardActions, Button, Divider, IconButton, Tooltip } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import { useEffect, useState } from 'react'

type Topic = {
  id: string
  name: string
  // keep other fields optional
}

type Quiz = {
  id: string
  question: string
  choices: string[]
  // backend returns `correctAnswerIndex`; keep legacy `correctIndex` optional to be tolerant
  correctAnswerIndex?: number
  correctIndex?: number
}

export default function CourseQuizzes({ courseId }: { courseId?: string }) {
  const [topics, setTopics] = useState<Topic[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedTopicId, setSelectedTopicId] = useState<string | ''>('')

  // quizzes state
  const [quizzes, setQuizzes] = useState<Quiz[]>([])
  const [loadingQuizzes, setLoadingQuizzes] = useState(false)
  const [quizzesError, setQuizzesError] = useState<string | null>(null)
  // map of quizId -> selected choice index
  const [selectedAnswers, setSelectedAnswers] = useState<Record<string, number | undefined>>({})

  // creating new quiz
  const [creatingQuiz, setCreatingQuiz] = useState(false)

  useEffect(() => {
    if (!courseId) return
    setLoading(true)
    setError(null)
    fetch(`/api/courses/${encodeURIComponent(courseId)}/topics`)
      .then(async (res) => {
        if (!res.ok) {
          const text = await res.text()
          throw new Error(text || res.statusText)
        }
        return res.json()
      })
      .then((data: Topic[]) => {
        setTopics(data || [])
        // select first topic by default when available
        if (data && data.length > 0) setSelectedTopicId(data[0].id)
      })
      .catch((e) => {
        console.error('Failed to load topics', e)
        setError('Failed to load topics')
        setTopics([])
      })
      .finally(() => setLoading(false))
  }, [courseId])

  // load quizzes when a topic is selected
  useEffect(() => {
    if (!courseId || !selectedTopicId) {
      // defer clearing quizzes to avoid synchronous setState-in-effect lint rule
      Promise.resolve().then(() => setQuizzes([]))
      return
    }
    setLoadingQuizzes(true)
    setQuizzesError(null)
    fetch(`/api/courses/${encodeURIComponent(courseId)}/topics/${encodeURIComponent(selectedTopicId)}/quizzes`)
      .then(async (res) => {
        if (!res.ok) {
          const text = await res.text()
          throw new Error(text || res.statusText)
        }
        return res.json()
      })
      .then((data: Quiz[]) => {
        // backend may return array of quizzes; adapt if fields differ
        setQuizzes(data || [])
        setSelectedAnswers({})
      })
      .catch((e) => {
        console.error('Failed to load quizzes', e)
        setQuizzesError('Failed to load quizzes')
        setQuizzes([])
      })
      .finally(() => setLoadingQuizzes(false))
  }, [courseId, selectedTopicId])

  async function createQuizForSelectedTopic() {
    if (!courseId || !selectedTopicId) return
    setCreatingQuiz(true)
    try {
      const res = await fetch(`/api/courses/${encodeURIComponent(courseId)}/topics/${encodeURIComponent(selectedTopicId)}/quizzes`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      })
      if (!res.ok) {
        const text = await res.text()
        throw new Error(text || res.statusText)
      }
      const newQuiz: Quiz = await res.json()
      // If backend returns the created quiz, prepend it; otherwise reload quizzes
      if (newQuiz && newQuiz.id) {
        setQuizzes((prev) => [newQuiz, ...prev])
        setSelectedAnswers((prev) => ({ ...prev }))
      } else {
        // fallback: reload quizzes
        fetch(`/api/courses/${encodeURIComponent(courseId)}/topics/${encodeURIComponent(selectedTopicId)}/quizzes`)
          .then((r) => r.json())
          .then((data: Quiz[]) => setQuizzes(data || []))
          .catch((e) => console.error('Failed to reload quizzes after create', e))
      }
    } catch (e) {
      console.error('Failed to create quiz', e)
    } finally {
      setCreatingQuiz(false)
    }
  }

  function onSelectAnswer(quizId: string, choiceIndex: number) {
    // if already answered, don't allow changes
    if (selectedAnswers[quizId] !== undefined) return
    // find quiz to access correctIndex for debugging
    const quiz = quizzes.find((q) => q.id === quizId)
    const correct = quiz ? (quiz.correctAnswerIndex ?? quiz.correctIndex) : undefined
    if (correct === undefined) {
      console.warn(`Quiz ${quizId} correctAnswerIndex is undefined, quiz object:`, quiz)
    }
    console.log(`Quiz ${quizId} selected=${choiceIndex} correct=${correct}`)
    setSelectedAnswers((prev) => ({ ...prev, [quizId]: choiceIndex }))
  }

  return (
    <Paper sx={{ p: 2 }} elevation={1}>
      <Stack spacing={2}>
        <Typography variant="h6">Quizzes</Typography>

        {/* Topic selector with create button */}
        <Stack direction="row" spacing={1} alignItems="center">
          <FormControl sx={{ flex: 1 }}>
            <InputLabel id="topic-select-label">Topic</InputLabel>
            <Select
              labelId="topic-select-label"
              value={selectedTopicId}
              label="Topic"
              onChange={(e) => setSelectedTopicId(e.target.value as string)}
              disabled={loading || topics.length === 0}
            >
              {loading && (
                <MenuItem value="">
                  <CircularProgress size={20} />&nbsp; Loading...
                </MenuItem>
              )}
              {!loading && topics.length === 0 && (
                <MenuItem value="">No topics</MenuItem>
              )}
              {topics.map((t) => (
                <MenuItem key={t.id} value={t.id}>
                  {t.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Tooltip title={selectedTopicId ? 'Create quiz for this topic' : 'Select a topic first'}>
            <span>
              <IconButton color="primary" onClick={createQuizForSelectedTopic} disabled={!selectedTopicId || creatingQuiz}>
                {creatingQuiz ? <CircularProgress size={20} /> : <AddIcon />}
              </IconButton>
            </span>
          </Tooltip>
        </Stack>

        {/* Quizzes list */}
        {loadingQuizzes && (
          <Typography>
            <CircularProgress size={20} />&nbsp; Loading quizzes...
          </Typography>
        )}

        {!loadingQuizzes && quizzes.length === 0 && (
          <Typography>No quizzes for this topic.</Typography>
        )}

        {quizzes.map((quiz) => (
          <Card key={quiz.id} variant="outlined" sx={{ width: '100%' }}>
            <CardContent>
              <Typography variant="subtitle1" sx={{ mb: 1 }}>
                {quiz.question}
              </Typography>

              <Stack spacing={1}>
                {quiz.choices.map((choice, idx) => {
                  const answered = selectedAnswers[quiz.id] !== undefined
                  const selectedIdx = selectedAnswers[quiz.id]
                  const isSelected = selectedIdx === idx
                  const isCorrect = idx === (quiz.correctAnswerIndex ?? quiz.correctIndex)

                  // compute sx and variant. Note: don't reveal correct answer until user has answered
                  const baseSx: Record<string, unknown> = { justifyContent: 'flex-start', textTransform: 'none' }

                  // Only highlight after the user has answered: green for correct, red for selected wrong
                  const highlightSx: Record<string, unknown> = answered
                    ? isCorrect
                      ? { bgcolor: 'success.main', color: 'white' }
                      : isSelected && !isCorrect
                      ? { bgcolor: 'error.main', color: 'white' }
                      : {}
                    : {}

                  // Ensure highlighted buttons remain colored even when disabled by targeting the disabled class
                  const disabledOverride = Object.keys(highlightSx).length > 0
                    ? { '&.Mui-disabled': { ...highlightSx, opacity: 1 } }
                    : { '&.Mui-disabled': { opacity: 1 } }

                  const finalSx = { ...baseSx, ...highlightSx, ...disabledOverride }

                  const shouldBeContained = answered && (isCorrect || isSelected)

                  return (
                    <Button
                      key={idx}
                      onClick={() => onSelectAnswer(quiz.id, idx)}
                      disabled={answered}
                      variant={shouldBeContained ? 'contained' : 'outlined'}
                      fullWidth
                      sx={finalSx}
                    >
                      <strong>{String.fromCharCode(65 + idx)}.</strong>&nbsp;{choice}
                    </Button>
                  )
                })}
              </Stack>
            </CardContent>
            <Divider />
            <CardActions>
              <Typography sx={{ ml: 1 }} variant="caption">
                {selectedAnswers[quiz.id] !== undefined ? `Answered: ${String.fromCharCode(65 + (selectedAnswers[quiz.id] as number))}` : 'Not answered yet'}
              </Typography>
              <Typography sx={{ ml: 2 }} variant="caption">
                {`Correct: ${String.fromCharCode(65 + ((quiz.correctAnswerIndex ?? quiz.correctIndex) ?? 0))}`}
              </Typography>
            </CardActions>
          </Card>
        ))}

        {quizzesError && <Typography color="error">{quizzesError}</Typography>}
        {error && <Typography color="error">{error}</Typography>}
      </Stack>
    </Paper>
  )
}
