import { Box, Button, TextField, Typography } from '@mui/material'
import React from 'react'

export default function Register() {
    // Only username and password are required per request
    const [username, setUsername] = React.useState('')
    const [password, setPassword] = React.useState('')
    const [loading, setLoading] = React.useState(false)
    const [error, setError] = React.useState<string | null>(null)
    const [success, setSuccess] = React.useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setError(null)
        setSuccess(false)
        setLoading(true)

        try {
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username,
                    password,
                }),
            })

            if (!res.ok) {
                const text = await res.text()
                throw new Error(text || `Registration failed with status ${res.status}`)
            }

            setSuccess(true)
            setUsername('')
            setPassword('')
        } catch (err: unknown) {
            const message = err instanceof Error ? err.message : String(err ?? 'Registration failed')
            setError(message)
        } finally {
            setLoading(false)
        }
    }

    return (
        <Box
            component="form"
            onSubmit={handleSubmit}
            sx={{ p: 2, maxWidth: 480 }}
        >
            <Typography variant="h5" gutterBottom>
                Register
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

            <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={loading}
            >
                {loading ? 'Creating…' : 'Create account'}
            </Button>

            {error && (
                <Typography color="error" sx={{ mt: 1 }}>
                    {error}
                </Typography>
            )}
            {success && (
                <Typography color="primary" sx={{ mt: 1 }}>
                    Account created successfully.
                </Typography>
            )}
        </Box>
    )
}
