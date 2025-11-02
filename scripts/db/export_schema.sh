#!/usr/bin/env bash
set -euo pipefail

# Export MySQL DDL (schema only) and overwrite backend/sql/schema.sql
# Usage:
#   DB_HOST=localhost DB_PORT=3306 DB_NAME=cosv_horizon DB_USER=root DB_PASSWORD=xxx \
#   ./scripts/db/export_schema.sh

: "${DB_HOST:?DB_HOST is required}"
: "${DB_PORT:?DB_PORT is required}"
: "${DB_NAME:?DB_NAME is required}"
: "${DB_USER:?DB_USER is required}"
: "${DB_PASSWORD:?DB_PASSWORD is required}"

ROOT_DIR=$(cd "$(dirname "$0")/../.." && pwd)
OUT_DIR="$ROOT_DIR/backend/sql"
OUT_FILE="$OUT_DIR/schema.sql"
BAK_FILE="$OUT_DIR/schema_legacy.sql"

mkdir -p "$OUT_DIR"

if [[ -f "$OUT_FILE" ]]; then
  cp -f "$OUT_FILE" "$BAK_FILE"
  echo "Backed up existing schema to $BAK_FILE"
fi

echo "Exporting DDL from $DB_HOST:$DB_PORT/$DB_NAME ..."
mysqldump \
  -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" \
  --no-data \
  --single-transaction \
  --routines --events \
  --databases "$DB_NAME" \
  --set-gtid-purged=OFF \
  --add-drop-table \
  > "$OUT_FILE"

echo "Exported to $OUT_FILE"
echo "Done. Please review schema.sql and commit changes."

