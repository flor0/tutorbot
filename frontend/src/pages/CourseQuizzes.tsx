import { Paper, Typography } from '@mui/material'

export default function CourseQuizzes({ courseId }: { courseId?: string }) {
  return (
    <Paper sx={{ p: 2 }} elevation={1}>
      <Typography variant="h6">Quizzes</Typography>
      <Typography sx={{ mt: 1 }}>Quizzes related to course {courseId} will appear here.</Typography>
    </Paper>
  )
}

