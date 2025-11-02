#!/usr/bin/env bash
set -euo pipefail

# Simple daemon starter for the Spring Boot backend
# - Runs the packaged JAR in background
# - Writes stdout/stderr to ./backend.log
# - Stores PID in ./backend.pid

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
PID_FILE="$SCRIPT_DIR/backend.pid"
LOG_FILE="$SCRIPT_DIR/backend.log"

JAR_GLOB="$SCRIPT_DIR/target/backend-*.jar"

if [[ -f "$PID_FILE" ]]; then
  if kill -0 "$(cat "$PID_FILE")" >/dev/null 2>&1; then
    echo "Backend already running with PID $(cat "$PID_FILE")."
    echo "Logs: $LOG_FILE"
    exit 0
  else
    echo "Stale PID file detected. Removing $PID_FILE."
    rm -f "$PID_FILE"
  fi
fi

# Always rebuild the JAR (skip tests) before starting, unless SKIP_BUILD=true
if [[ "${SKIP_BUILD:-false}" != "true" ]]; then
  if [[ -x "$SCRIPT_DIR/mvnw" ]]; then
    echo "Building backend JAR (skip tests)..."
    (cd "$SCRIPT_DIR" && ./mvnw -q -DskipTests package)
  else
    echo "WARNING: mvnw not found. Skipping rebuild."
  fi
else
  echo "SKIP_BUILD=true; skipping rebuild."
fi

# Locate the latest built JAR
JAR_PATH="$(ls -t $JAR_GLOB 2>/dev/null | head -1 || true)"

if [[ -z "${JAR_PATH}" ]]; then
  echo "ERROR: Cannot find built JAR (expected at $JAR_GLOB)."
  echo "Please run: (cd $SCRIPT_DIR && ./mvnw -DskipTests package)"
  exit 1
fi

JAVA_BIN=${JAVA_BIN:-java}
JAVA_OPTS=${JAVA_OPTS:-}

# Optional runtime overrides via env vars
# - SERVER_PORT: override Spring server.port
# - SPRING_PROFILES_ACTIVE: set active profiles
EXTRA_ARGS=()
if [[ -n "${SERVER_PORT:-}" ]]; then
  EXTRA_ARGS+=("--server.port=${SERVER_PORT}")
fi
if [[ -n "${SPRING_PROFILES_ACTIVE:-}" ]]; then
  EXTRA_ARGS+=("--spring.profiles.active=${SPRING_PROFILES_ACTIVE}")
fi

echo "Starting backend as daemon..."
echo "  JAR:  $JAR_PATH"
echo "  LOG:  $LOG_FILE"
# Expand EXTRA_ARGS only if defined to avoid nounset issues
nohup "$JAVA_BIN" $JAVA_OPTS -jar "$JAR_PATH" ${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"} >> "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

sleep 1
if kill -0 "$(cat "$PID_FILE")" >/dev/null 2>&1; then
  echo "Backend started. PID $(cat "$PID_FILE")."
  echo "Follow logs: tail -f $LOG_FILE"
else
  echo "ERROR: Backend failed to start. See $LOG_FILE for details."
  exit 1
fi
