import { Box, Button, TextField, Typography } from '@mui/material'
import React from 'react'
import { useNavigate } from 'react-router-dom'

export default function Login() {
  const [username, setUsername] = React.useState('')
  const [password, setPassword] = React.useState('')
  const [loading, setLoading] = React.useState(false)
  const [error, setError] = React.useState<string | null>(null)
  const [success, setSuccess] = React.useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(false)
    setLoading(true)

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username, password }),
      })

      if (!res.ok) {
        const text = await res.text()
        throw new Error(text || `Login failed with status ${res.status}`)
      }

      // Parse backend response: { username, authorities }
      let data: unknown
      try {
        data = await res.json()
      } catch {
        // if parsing fails, continue with null
        data = null
      }

      // Persist user info for later pages (simple approach)
      try {
        if (data) localStorage.setItem('user', JSON.stringify(data))
      } catch {
        // ignore storage errors
      }

      // notify other windows/tabs and the App about auth change
      try {
        window.dispatchEvent(new Event('authChange'))
      } catch {}

      setSuccess(true)
      setUsername('')
      setPassword('')

      // Redirect to courses page
      navigate('/courses')
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : String(err ?? 'Login failed')
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box component="form" onSubmit={handleSubmit} sx={{ p: 2, maxWidth: 480 }}>
      <Typography variant="h5" gutterBottom>
        Login
      </Typography>

      <TextField
        label="Username"
        fullWidth
        margin="normal"
        value={username}
        onChange={(e) => setUsername(e.target.value)}
      />

      <TextField
        label="Password"
        type="password"
        fullWidth
        margin="normal"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />

      <Button type="submit" variant="contained" color="primary" disabled={loading}>
        {loading ? 'Signing in…' : 'Sign in'}
      </Button>

      {error && (
        <Typography color="error" sx={{ mt: 1 }}>
          {error}
        </Typography>
      )}

      {success && (
        <Typography color="primary" sx={{ mt: 1 }}>
          Signed in successfully.
        </Typography>
      )}
    </Box>
  )
}
