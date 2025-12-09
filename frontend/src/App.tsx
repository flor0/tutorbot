import './App.css'
import { useState, useEffect } from 'react'
import {
  AppBar,
  Box,
  Button,
  Toolbar,
  Typography,
  IconButton,
  Menu,
  MenuItem,
} from '@mui/material'
import MenuIcon from '@mui/icons-material/Menu'
import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import Courses from './pages/Courses'
import CourseDetail from './pages/CourseDetail'

function App() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)
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

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget)
  }
  const handleMenuClose = () => setAnchorEl(null)

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
      handleMenuClose()
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
            AI Tutor
          </Typography>

          {/* Right-aligned navigation group; both nav variants are always rendered and CSS will show/hide */}
          <Box sx={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
            {/* Desktop nav - shown on wider screens via CSS */}
            <nav className="desktop-nav" aria-label="primary navigation">
              <Button color="inherit" component={Link} to="/">
                Home
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
              <Button color="inherit" component={Link} to="/courses">
                Courses
              </Button>

              {loggedIn && (
                <Button color="inherit" onClick={handleLogout}>
                  Logout
                </Button>
              )}
            </nav>

            {/* Mobile nav - shown on small screens via CSS. The Menu still uses state to open/close. */}
            <div className="mobile-nav">
              <IconButton
                color="inherit"
                aria-label="open navigation"
                onClick={handleMenuOpen}
                size="large"
              >
                <MenuIcon />
              </IconButton>
              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              >
                <MenuItem component={Link} to="/" onClick={handleMenuClose}>
                  Home
                </MenuItem>
                {!loggedIn && (
                  <>
                    <MenuItem component={Link} to="/login" onClick={handleMenuClose}>
                      Login
                    </MenuItem>
                    <MenuItem component={Link} to="/register" onClick={handleMenuClose}>
                      Register
                    </MenuItem>
                  </>
                )}
                <MenuItem component={Link} to="/courses" onClick={handleMenuClose}>
                  Courses
                </MenuItem>

                {loggedIn && (
                  <MenuItem
                    onClick={() => {
                      handleLogout()
                    }}
                  >
                    Logout
                  </MenuItem>
                )}
              </Menu>
            </div>
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
