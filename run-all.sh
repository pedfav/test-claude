#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
PASS=0; FAIL=0

ok()   { echo "  [OK] $1"; ((PASS++)); }
fail() { echo "  [FAIL] $1"; ((FAIL++)); }
section() { echo; echo "=== $1 ==="; }

# ── Backend ──────────────────────────────────────────────────────────────────
section "Backend tests + coverage"
if (cd "$ROOT/backend" && ./gradlew test jacocoTestCoverageVerification -q --no-daemon 2>&1); then
  ok "Backend tests & coverage (≥70%)"
else
  fail "Backend tests/coverage"
fi

# ── Frontend ─────────────────────────────────────────────────────────────────
section "Frontend install + tests"
(cd "$ROOT/frontend" && npm install --silent 2>&1 | tail -3)
if (cd "$ROOT/frontend" && npx vitest run --reporter=verbose 2>&1); then
  ok "Frontend tests"
else
  fail "Frontend tests"
fi

# ── Docker build ─────────────────────────────────────────────────────────────
section "Docker Compose build"
if docker compose -f "$ROOT/docker-compose.yml" build --quiet 2>&1; then
  ok "Docker images built"
else
  fail "Docker build"
fi

# ── Summary ──────────────────────────────────────────────────────────────────
section "Summary"
echo "  Passed: $PASS  Failed: $FAIL"
echo
[ "$FAIL" -eq 0 ] && echo "All checks passed." && exit 0
echo "Some checks failed." && exit 1
