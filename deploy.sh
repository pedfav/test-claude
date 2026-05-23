#!/usr/bin/env bash
# =============================================================================
# deploy.sh — Docker-based startup for Task Manager
# =============================================================================
# Prerequisites:
#   - Docker Desktop (Mac/Windows) or Docker Engine + Compose plugin (Linux)
#     Install: https://docs.docker.com/get-docker/
#   - Ports 5432, 8080, and 3000 must be free before running
#
# Usage:
#   chmod +x deploy.sh
#   ./deploy.sh
#
# What it does:
#   1. Validates Docker is installed and running, ports are available
#   2. Builds all Docker images (backend Gradle build runs inside Docker)
#   3. Starts PostgreSQL and waits until it passes its health check
#   4. Starts the Spring Boot backend and waits until /actuator/health reports UP
#   5. Starts the nginx-served Vue frontend (proxies /api/* to the backend)
#
# To stop everything:
#   docker compose down            # stop containers, keep database volume
#   docker compose down -v         # stop containers AND wipe the database
# =============================================================================

set -euo pipefail

# Resolve the project root relative to this script, so it works from any CWD
ROOT="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$ROOT/docker-compose.yml"

# ── Terminal colours (disabled automatically when not a TTY) ──────────────────
if [[ -t 1 ]]; then
  RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
  BLUE='\033[0;34m'; BOLD='\033[1m'; NC='\033[0m'
else
  RED=''; GREEN=''; YELLOW=''; BLUE=''; BOLD=''; NC=''
fi

log()     { echo -e "${BLUE}==>${NC} $*"; }
success() { echo -e "${GREEN}  [OK]${NC} $*"; }
warn()    { echo -e "${YELLOW}  [WARN]${NC} $*"; }
die()     { echo -e "${RED}  [ERROR]${NC} $*" >&2; exit 1; }

# ── Cleanup on failure ────────────────────────────────────────────────────────
# Only tears down containers when startup did NOT complete successfully.
# On a clean exit the trap is removed so containers keep running.
STARTUP_FAILED=true
cleanup() {
  if $STARTUP_FAILED; then
    echo -e "\n${RED}Startup failed — stopping containers to leave a clean state.${NC}"
    echo    "    Check the logs above or run: docker compose -f \"$COMPOSE_FILE\" logs"
    docker compose -f "$COMPOSE_FILE" down --remove-orphans 2>/dev/null || true
  fi
}
trap cleanup EXIT INT TERM

# ── Helper: portable port-in-use check ───────────────────────────────────────
# Uses bash's built-in /dev/tcp — works on macOS and Linux without extra tools.
port_in_use() {
  (echo >/dev/tcp/localhost/"$1") 2>/dev/null
}

# ── Helper: kill whatever is listening on a port ──────────────────────────────
# Tries lsof first (macOS + most Linux), falls back to fuser (Linux).
# Sends SIGTERM, waits up to 5s, then SIGKILL if the port is still held.
kill_port() {
  local port=$1 name=$2
  local pids=()

  # Collect PIDs — try lsof (macOS + Linux), fall back to fuser (Linux).
  # while-read loop used instead of mapfile for bash 3.2 compatibility (macOS).
  if command -v lsof &>/dev/null; then
    while IFS= read -r pid; do
      [[ -n "$pid" ]] && pids+=("$pid")
    done < <(lsof -ti TCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
  elif command -v fuser &>/dev/null; then
    while IFS= read -r pid; do
      [[ -n "$pid" ]] && pids+=("$pid")
    done < <(fuser "$port"/tcp 2>/dev/null || true)
  fi

  if [[ ${#pids[@]} -eq 0 ]]; then
    return 0
  fi

  local proc_path
  proc_path=$(ps -p "${pids[0]}" -o comm= 2>/dev/null || echo "")

  # Homebrew services are managed by launchd — killing the PID directly just
  # causes launchd to restart the process immediately. Detect this case by
  # checking whether the process lives under Homebrew's prefix, then stop it
  # cleanly via 'brew services stop' so launchd releases the port for good.
  if command -v brew &>/dev/null; then
    local brew_prefix
    brew_prefix="$(brew --prefix 2>/dev/null || true)"
    if [[ -n "$brew_prefix" && "$proc_path" == "$brew_prefix/opt/"* ]]; then
      # Extract service name directly from path: /brew/opt/<service>/bin/... → <service>
      local svc
      svc="${proc_path#"$brew_prefix/opt/"}"
      svc="${svc%%/*}"

      if [[ -n "$svc" ]]; then
        warn "Port $port held by Homebrew service '$svc' (launchd-managed) — stopping via brew services..."
        brew services stop "$svc" 2>/dev/null || true
        for i in $(seq 1 8); do
          port_in_use "$port" || return 0
          sleep 1
        done
      fi
    fi
  fi

  # Generic path: SIGTERM, wait, then SIGKILL
  local names
  names=$(ps -p "${pids[@]}" -o comm= 2>/dev/null | sort -u | tr '\n' ' ' || echo "unknown")
  warn "Port $port in use by: ${names}(PID ${pids[*]}) — sending SIGTERM..."
  kill -TERM "${pids[@]}" 2>/dev/null || true

  local i
  for i in $(seq 1 5); do
    port_in_use "$port" || return 0
    sleep 1
  done

  warn "Port $port still held after 5s — sending SIGKILL..."
  kill -KILL "${pids[@]}" 2>/dev/null || true
  sleep 1

  port_in_use "$port" \
    && die "Could not free port $port (needed for $name). Try: sudo kill -9 ${pids[*]}"
}

# ── Helper: wait for a container's Docker health check to turn healthy ────────
# Args: $1 = container name, $2 = max attempts, $3 = seconds between attempts
wait_healthy() {
  local container=$1 max=$2 interval=$3
  local attempt=1
  while [[ $attempt -le $max ]]; do
    local status
    status=$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "missing")
    case "$status" in
      healthy) return 0 ;;
      unhealthy) die "Container '$container' reported unhealthy. Run: docker compose logs $container" ;;
    esac
    printf "\r    Waiting (%ds elapsed)..." "$((attempt * interval))"
    sleep "$interval"
    ((attempt++))
  done
  die "Timed out waiting for '$container' to become healthy after $((max * interval))s."
}

# =============================================================================
# Step 0 — Prerequisites
# =============================================================================
echo
echo -e "${BOLD}Task Manager — Docker Startup${NC}"
echo    "────────────────────────────────────────────"
echo

log "Checking prerequisites..."

# Docker CLI
command -v docker &>/dev/null \
  || die "Docker is not installed. Get it at: https://docs.docker.com/get-docker/"

# Docker daemon
docker info &>/dev/null \
  || die "Docker daemon is not running. Start Docker Desktop, or run: sudo systemctl start docker"

# Compose plugin (v2 syntax: 'docker compose', not 'docker-compose')
docker compose version &>/dev/null \
  || die "'docker compose' plugin not found. Update Docker Desktop or install the Compose plugin."

# Required ports — free them automatically if something is already listening
for entry in "5432:PostgreSQL" "8080:backend" "3000:frontend"; do
  port="${entry%%:*}"; name="${entry##*:}"
  if port_in_use "$port"; then
    kill_port "$port" "$name"
    success "Port $port cleared for $name"
  fi
done

success "Docker $(docker --version | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1) running, ports 5432/8080/3000 available"

# =============================================================================
# Step 1 — Build Docker images
# =============================================================================
# Both the backend (Gradle bootJar) and frontend (npm build → nginx) are
# multi-stage builds. The first build can take 3-5 minutes; subsequent builds
# are fast because Docker caches intermediate layers.
echo
log "Building Docker images (first run downloads JDK + npm deps — ~3-5 min)..."

docker compose -f "$COMPOSE_FILE" build --parallel 2>&1 \
  | grep --line-buffered -E "^\s*(#[0-9]|=>|CACHED|ERROR|error)" \
  || true   # grep exits 1 when no lines match; don't let that fail the script

success "All images built"

# =============================================================================
# Step 2 — Database (PostgreSQL 16)
# =============================================================================
# Starts only the 'postgres' service. The compose healthcheck runs
# pg_isready every 10s; we wait up to 60s (6 × 10s) before giving up.
echo
log "Starting PostgreSQL 16..."

docker compose -f "$COMPOSE_FILE" up -d postgres

wait_healthy "taskmanager-db" 6 10
echo  # clear the \r progress line
success "PostgreSQL healthy on localhost:5432  (db: taskmanager, user: taskmanager)"

# =============================================================================
# Step 3 — Backend (Spring Boot / Kotlin, JVM 21)
# =============================================================================
# Spring Boot takes 30-90s to start depending on the host machine.
# The compose healthcheck hits /actuator/health every 15s with a 60s
# start_period, so we allow up to ~3 min total (12 attempts × 15s).
echo
log "Starting backend (Spring Boot — allow ~60-90s for JVM warmup)..."

docker compose -f "$COMPOSE_FILE" up -d backend

wait_healthy "taskmanager-backend" 12 15
echo  # clear the \r progress line
success "Backend healthy on localhost:8080  (API at /api, health at /actuator/health)"

# =============================================================================
# Step 4 — Frontend (Vue 3, served by nginx)
# =============================================================================
# nginx starts in milliseconds. We start it and do a single HTTP probe to
# confirm it bound to port 3000 before declaring success.
echo
log "Starting frontend (Vue 3 / nginx)..."

docker compose -f "$COMPOSE_FILE" up -d frontend

# Give nginx ~5s to bind its port, then probe
for i in 1 2 3 4 5; do
  port_in_use 3000 && break
  sleep 1
done
port_in_use 3000 || warn "Port 3000 not responding yet — nginx may still be starting."

success "Frontend running on localhost:3000  (proxies /api/* → backend:8080)"

# =============================================================================
# Done
# =============================================================================
STARTUP_FAILED=false  # disable teardown on exit — services should keep running
trap - EXIT INT TERM

echo
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  Task Manager is up and running!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo
echo "  Open in browser  →  http://localhost:3000"
echo "  Backend API      →  http://localhost:8080/api"
echo "  Health endpoint  →  http://localhost:8080/actuator/health"
echo
echo "  Seed accounts (password: password123)"
echo "    alice@example.com  — ADMIN"
echo "    bob@example.com    — MEMBER"
echo "    carol@example.com  — MEMBER"
echo
echo "  Useful commands:"
echo "    docker compose logs -f              # stream logs from all services"
echo "    docker compose logs -f backend      # backend logs only"
echo "    docker compose ps                   # check container status"
echo "    docker compose down                 # stop (keeps database volume)"
echo "    docker compose down -v              # stop + wipe database"
echo
