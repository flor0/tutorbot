import { Box, Card, CardMedia, Typography } from '@mui/material'

export default function Home() {
  return (
    <Box
      sx={{
        p: 2,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 'calc(100vh - 64px)',
      }}
    >
      <Card
        variant="outlined"
        sx={{
          p: 4,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
          maxWidth: 480,
          width: '100%',
        }}
      >
        <CardMedia
          component="img"
          alt="AI Tutor logo"
          src="/assets/react.svg"
          sx={{ width: 120, height: 120, borderRadius: 2 }}
        />

        <Typography variant="h4" component="h1" align="center" fontWeight="700">
          AI Tutor
        </Typography>

        <Typography variant="body1" color="text.secondary" align="center">
          A lightweight assistant to help you learn and review course material.
        </Typography>
      </Card>
    </Box>
  )
}
