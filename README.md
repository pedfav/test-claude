# TaskFlow — Full-Stack Task Manager

A production-ready Kanban-style task management application.

**Stack:** Kotlin + Spring Boot · Vue 3 + TypeScript · PostgreSQL · Docker

---

## Quick Start

```bash
git clone <repo-url>
cd task-manager
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- API Docs (health): http://localhost:8080/actuator/health

**Sample accounts** (password: `password123`):

| Email | Role |
|---|---|
| alice@example.com | Admin |
| bob@example.com | Member |
| carol@example.com | Member |
| dave@example.com | Member |

---

## Project Structure

```
task-manager/
├── backend/                # Kotlin + Spring Boot API
│   ├── src/main/kotlin/    # Application code
│   ├── src/test/kotlin/    # Tests
│   └── Dockerfile
├── frontend/               # Vue 3 + TypeScript SPA
│   ├── src/
│   │   ├── api/            # API client + types
│   │   ├── components/     # Reusable components
│   │   ├── store/          # Pinia stores
│   │   ├── views/          # Page components
│   │   └── router/         # Vue Router
│   └── Dockerfile
├── scripts/
│   └── seed.sql            # Sample data
├── .github/workflows/
│   └── ci.yml              # GitHub Actions CI
└── docker-compose.yml
```

---

## API Endpoints

### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, get JWT |

### Boards
| Method | Path | Description |
|---|---|---|
| GET | `/api/boards` | List accessible boards |
| POST | `/api/boards` | Create board |
| GET | `/api/boards/:id` | Get board with columns & tasks |
| PUT | `/api/boards/:id` | Update board |
| DELETE | `/api/boards/:id` | Delete board |
| POST | `/api/boards/:id/columns` | Add column |
| PUT | `/api/boards/columns/:id` | Update column |
| DELETE | `/api/boards/columns/:id` | Delete column |

### Tasks
| Method | Path | Description |
|---|---|---|
| GET | `/api/boards/:boardId/tasks` | List board tasks |
| POST | `/api/boards/:boardId/tasks` | Create task |
| GET | `/api/tasks/:id` | Get task |
| PUT | `/api/tasks/:id` | Update task |
| PATCH | `/api/tasks/:id/move` | Move task to column/position |
| DELETE | `/api/tasks/:id` | Delete task |

### Teams
| Method | Path | Description |
|---|---|---|
| GET | `/api/teams` | List user's teams |
| POST | `/api/teams` | Create team |
| GET | `/api/teams/:id` | Get team |
| PUT | `/api/teams/:id` | Update team |
| DELETE | `/api/teams/:id` | Delete team |
| POST | `/api/teams/:id/members` | Add member |
| DELETE | `/api/teams/:id/members/:userId` | Remove member |

All endpoints (except `/api/auth/**`) require `Authorization: Bearer <token>`.

---

## Local Development (without Docker)

### Backend

```bash
cd backend

# Requires PostgreSQL running locally
export DB_URL=jdbc:postgresql://localhost:5432/taskmanager
export DB_USERNAME=taskmanager
export DB_PASSWORD=taskmanager
export JWT_SECRET=local-dev-secret-min-256-bits-long-here

./gradlew bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev
# Opens at http://localhost:5173
```

---

## Running Tests

### Backend

```bash
cd backend
./gradlew test                        # Run all tests
./gradlew jacocoTestReport            # Generate coverage report
./gradlew jacocoTestCoverageVerification  # Verify 70% threshold
```

Reports: `backend/build/reports/tests/test/index.html`

### Frontend

```bash
cd frontend
npm test                   # Run once
npm run test:watch         # Watch mode
npm run test:coverage      # With coverage report
```

---

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) runs on every push:

1. **Backend Tests** — JUnit 5 + JaCoCo coverage gate (70%)
2. **Backend Build** — Builds JAR + Docker image
3. **Frontend Tests** — Vitest + type checking
4. **Frontend Build** — Builds static assets + Docker image
5. **Integration Tests** — Full backend tests against PostgreSQL
6. **Docker Compose Smoke** — Starts the stack and health-checks the API

---

## Configuration

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskmanager` | Database URL |
| `DB_USERNAME` | `taskmanager` | Database user |
| `DB_PASSWORD` | `taskmanager` | Database password |
| `JWT_SECRET` | *(see app yml)* | **Change in production** |
| `JWT_EXPIRATION_MS` | `86400000` (24h) | Token lifetime |
| `SERVER_PORT` | `8080` | Backend port |
| `VITE_API_URL` | `` (proxied) | Frontend API base URL |

---

## Database Migrations

Flyway manages schema migrations. Migration files live in:
`backend/src/main/resources/db/migration/`

Naming convention: `V{version}__{description}.sql`

---

## Architecture Notes

- **JWT authentication** — stateless, no session storage
- **Flyway migrations** — schema versioned and reproducible
- **CORS** — configured to allow all origins in dev (tighten in prod)
- **Drag-and-drop** — uses `vue-draggable-plus` (SortableJS wrapper)
- **Polling** — board state is fetched on page load; add WebSocket for real-time if needed
- **Access control** — board access: owner OR team member; task CRUD mirrors board access
