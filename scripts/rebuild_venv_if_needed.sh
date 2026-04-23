#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -d venv ]]; then
  mv venv "venv.broken.$(date +%Y%m%d_%H%M%S)"
fi

python3 -m venv venv
source venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -r requirements.txt

echo "Rebuilt venv at $ROOT_DIR/venv"
