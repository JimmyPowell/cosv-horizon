#!/usr/bin/env bash
set -euo pipefail

# Reset git history to a single sanitized commit using an orphan branch, then force-push to origin main.
# IMPORTANT: This is destructive for remote history. Make sure you understand the impact.
# Usage:
#   ./scripts/repo/reset_history.sh

confirm() {
  read -r -p "This will OVERWRITE remote main history. Continue? [y/N] " resp
  case "$resp" in
    [yY][eE][sS]|[yY]) ;;
    *) echo "Aborted."; exit 1;;
  esac
}

confirm

echo "Creating orphan branch clean-main ..."
git checkout --orphan clean-main

echo "Removing index ..."
git rm -r --cached . >/dev/null 2>&1 || true

echo "Adding sanitized workspace ..."
git add .

echo "Committing initial sanitized import ..."
git commit -m "chore: initial import (sanitized, schema re-exported)"

echo "Replacing main with orphan branch ..."
git branch -D main || true
git branch -m main

echo "Force pushing to origin/main ..."
git push --force origin main

echo "Done. Collaborators must re-clone or reset to the new HEAD."

