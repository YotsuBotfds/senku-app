#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Bundle root: $ROOT_DIR"

if [[ ! -d venv ]]; then
  echo "Missing venv/"
  exit 1
fi

source venv/bin/activate

echo "Python: $(python --version 2>&1)"
echo "Venv: $VIRTUAL_ENV"

python - <<'PY'
import chromadb
import requests
import rich
import yaml
print("Python imports: OK")
PY

python -m py_compile bench.py query.py ingest.py
echo "Py compile: OK"

if [[ ! -d db ]]; then
  echo "Missing db/"
  exit 1
fi

if [[ ! -d guides ]]; then
  echo "Missing guides/"
  exit 1
fi

echo "db/: OK"
echo "guides/: OK"

echo "Checking LM Studio..."
curl -s http://localhost:1234/v1/models | sed -n '1,120p'

echo "Runtime check complete."
