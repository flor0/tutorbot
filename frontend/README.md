# AI Tutor — Frontend

This is the frontend for the AI Tutor project (React + Vite + TypeScript + MUI).

Quick overview
- React 19 + Vite
- Material UI (MUI) for theming and components (dark theme enabled)
- React Router for client routes
- Communicates with a Spring Boot backend under `/api/*` (see endpoints below)
- Uses cookie-based authentication; requests include credentials by default where necessary

Prerequisites
- Node.js 18+ (or compatible)
- npm (or pnpm/yarn) — the project uses the scripts in `package.json`

Install
1. From the `frontend` directory run:

```bash
npm install
```

Run (development)

```bash
npm run dev
```

Build

```bash
npm run build
```

Preview production build

```bash
npm run preview
```

Lint

```bash
npm run lint
```

Project structure (relevant files)
- `index.html`, `src/main.tsx` - app bootstrap
- `src/App.tsx` - top-level router and AppBar
- `src/theme.ts` - MUI theme (dark mode)
- `src/pages/` - page components
  - `Home.tsx` — landing page
  - `Register.tsx` — registration page (username + password)
  - `Login.tsx` — login page
  - `Courses.tsx` — list of courses
  - `CourseDetail.tsx` — course detail page (contains tabs: Topics / Quizzes / Materials)
  - `CourseTopics.tsx` — topics UI (list, select, create, delete)
  - `CourseMaterials.tsx` — materials UI (list, upload, delete)
  - `CourseQuizzes.tsx` — quizzes placeholder

Client routes (react-router)
- `/` — Home
- `/register` — Register page
- `/login` — Login page
- `/courses` — Courses list
- `/courses/:id` — Course detail (has internal navigation for Topics, Quizzes, Materials)

Backend endpoints used by the frontend
- POST /api/auth/register — register with JSON { username, password }
- POST /api/auth/login — login with JSON { username, password } (returns LoginResponse)
- POST /api/auth/logout — logs out (expects cookie-based session)
- GET /api/courses — list courses (credentials included)
- POST /api/courses/create — create a course
- GET /api/courses/{id} — get course detail
- GET /api/courses/{courseId}/materials — list course materials
- POST /api/courses/{courseId}/materials/upload — upload a PDF (multipart/form-data, field name `file`)
- DELETE /api/courses/{courseId}/materials/{materialId} — delete material
- GET /api/courses/{courseId}/topics — list topics for a course
- POST /api/courses/{courseId}/topics — create a topic
- DELETE /api/courses/{courseId}/topics/{id} — delete a topic

Notes and conventions
- Network requests that require authentication use `fetch(..., { credentials: 'include' })` to send cookies.
- The app uses a consistent dark MUI theme provided in `src/theme.ts` and wrapped by `ThemeProvider` in `src/main.tsx`/`src/App.tsx`.
- The `AppBar` is positioned at the top and set to be responsive — it contains navigation and a logout button (visible when a user session appears present).
- The code avoids reading plaintext session tokens; authentication is handled by the backend cookie/session.

Testing
- A small unit test was added to assert route rendering (see `src/__tests__` if present). Run your test command via your preferred test runner — this project does not include Jest/React Testing Library in the base dependencies; add them if you want to run unit tests locally.

Troubleshooting
- If layout overflows horizontally, avoid using `width: 100vw` for full-width elements that sit inside the page; prefer `width: 100%` and `box-sizing: border-box` to account for scrollbars and padding.
- If API endpoints changed on the backend, update the fetch URLs in `src/pages/*` accordingly; most course-related endpoints use the `/api/courses/{courseId}/...` prefix.

Further improvements
- Add client-side tests with React Testing Library + Jest.
- Add better error handling and toasts for network errors.
- Add form validation for register/login pages.

Maintainer
- Florian Rupp


