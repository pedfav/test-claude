#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
PIDS=()

cleanup() {
  echo
  echo "Stopping..."
  for pid in "${PIDS[@]:-}"; do
    kill "$pid" 2>/dev/null || true
  done
  brew services stop postgresql@16 2>/dev/null || true
}
trap cleanup EXIT INT TERM

# ── PostgreSQL ────────────────────────────────────────────────────────────────
echo "==> Setting up PostgreSQL..."
if ! brew list postgresql@16 &>/dev/null; then
  echo "    Installing postgresql@16 (this may take a minute)..."
  brew install postgresql@16 -q
fi

brew services start postgresql@16
sleep 2

# Create DB and user if they don't exist
PG="$(brew --prefix)/opt/postgresql@16/bin"
"$PG/psql" postgres -tc "SELECT 1 FROM pg_roles WHERE rolname='taskmanager'" \
  | grep -q 1 || "$PG/psql" postgres -c "CREATE USER taskmanager WITH PASSWORD 'taskmanager';"
"$PG/psql" postgres -tc "SELECT 1 FROM pg_database WHERE datname='taskmanager'" \
  | grep -q 1 || "$PG/psql" postgres -c "CREATE DATABASE taskmanager OWNER taskmanager;"

echo "    PostgreSQL ready."

# ── Backend ───────────────────────────────────────────────────────────────────
echo "==> Starting backend on http://localhost:8080 ..."
(cd "$ROOT/backend" && ./gradlew bootRun -q --no-daemon \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager \
  -Dspring.datasource.username=taskmanager \
  -Dspring.datasource.password=taskmanager \
  2>&1 | sed 's/^/[backend] /') &
PIDS+=($!)

# Wait for backend to be ready
echo "    Waiting for backend..."
for i in $(seq 1 30); do
  curl -sf http://localhost:8080/actuator/health &>/dev/null && break
  sleep 2
done

echo "    Backend ready."

# ── Frontend ──────────────────────────────────────────────────────────────────
echo "==> Starting frontend on http://localhost:5173 ..."
(cd "$ROOT/frontend" && npm install -q && npm run dev 2>&1 | sed 's/^/[frontend] /') &
PIDS+=($!)

echo
echo "  App running:"
echo "    Frontend  →  http://localhost:5173"
echo "    Backend   →  http://localhost:8080"
echo
echo "  Press Ctrl+C to stop."
echo

wait
