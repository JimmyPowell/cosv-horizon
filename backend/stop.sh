#!/usr/bin/env bash
set -euo pipefail

# Stop the background Spring Boot backend started by start.sh

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
PID_FILE="$SCRIPT_DIR/backend.pid"
LOG_FILE="$SCRIPT_DIR/backend.log"

if [[ ! -f "$PID_FILE" ]]; then
  echo "No PID file found ($PID_FILE). Backend not running?"
  exit 0
fi

PID="$(cat "$PID_FILE")"
if ! kill -0 "$PID" >/dev/null 2>&1; then
  echo "No process with PID $PID. Removing stale PID file."
  rm -f "$PID_FILE"
  exit 0
fi

echo "Stopping backend (PID $PID)..."
kill "$PID" || true

# Wait up to 30s for graceful shutdown
for i in {1..30}; do
  if ! kill -0 "$PID" >/dev/null 2>&1; then
    echo "Backend stopped."
    rm -f "$PID_FILE"
    exit 0
  fi
  sleep 1
done

echo "Process still running, forcing termination..."
kill -9 "$PID" || true
rm -f "$PID_FILE"
echo "Backend force-stopped. Check logs if needed: $LOG_FILE"

