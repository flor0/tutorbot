import './App.css'
import { useState, useEffect } from 'react'
import {
  AppBar,
  Box,
  Button,
  Toolbar,
  Typography,
} from '@mui/material'
import HomeSharpIcon from '@mui/icons-material/HomeSharp';
import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import Courses from './pages/Courses'
import CourseDetail from './pages/CourseDetail'

function App() {
  const [loggedIn, setLoggedIn] = useState<boolean>(() => {
    try {
      return Boolean(localStorage.getItem('user'))
    } catch {
      return false
    }
  })
  const navigate = useNavigate()

  useEffect(() => {
    const handler = () => {
      try {
        setLoggedIn(Boolean(localStorage.getItem('user')))
      } catch {
        setLoggedIn(false)
      }
    }
    window.addEventListener('authChange', handler)
    return () => window.removeEventListener('authChange', handler)
  }, [])

  const handleLogout = async () => {
    try {
      // send the logout POST; include credentials so any HttpOnly cookie is sent
      await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' })
    } catch (err) {
      // ignore network errors, but reference the error to satisfy lint rules
      try {
        console.debug('Logout request failed:', err)
      } catch (e) {
        void e
      }
    } finally {
      try {
        localStorage.removeItem('user')
      } catch (e) {
        void e
      }
      // notify other windows/tabs
      try {
        window.dispatchEvent(new Event('authChange'))
      } catch (e) {
        void e
      }
      setLoggedIn(false)
      navigate('/login')
    }
  }

  return (
    <Box>
      <AppBar
        position="fixed"
        sx={{
          width: '100%',
          zIndex: (t) => t.zIndex.drawer + 1,
        }}
      >
        {/* Toolbar is flush to the viewport edges; CSS will handle spacing for content */}
        <Toolbar sx={{ px: 0 }}>
          {/* Title flush to the left */}
          <Typography
            variant="h6"
            component={Link}
            to="/"
            sx={{
              textAlign: 'left',
              color: 'inherit',
              textDecoration: 'none',
              // keep the title at the very left of the Toolbar
              ml: 1,
            }}
          >
            TutorBot
          </Typography>

          {/* Right-aligned navigation group; both nav variants are always rendered and CSS will show/hide */}
          <Box sx={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
            {/* Desktop nav - shown on wider screens via CSS */}
            <nav className="desktop-nav" aria-label="primary navigation">
              <Button color="inherit" component={Link} to="/">
                  <HomeSharpIcon />
              </Button>
              {!loggedIn && (
                <>
                  <Button color="inherit" component={Link} to="/login">
                    Login
                  </Button>
                  <Button color="inherit" component={Link} to="/register">
                    Register
                  </Button>
                </>
              )}

              {loggedIn && (
                <Button color="inherit" component={Link} to="/courses">
                  Courses
                </Button>
              )}

              {loggedIn && (
                <Button color="inherit" onClick={handleLogout}>
                  Logout
                </Button>
              )}
            </nav>
          </Box>
        </Toolbar>
      </AppBar>

      {/* Toolbar spacer pushes page content below the fixed AppBar */}
      <Toolbar />

      <Box sx={{ mt: 2 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/courses" element={<Courses />} />
          <Route path="/courses/:id" element={<CourseDetail />} />
        </Routes>
      </Box>
    </Box>
  )
}

export default App
