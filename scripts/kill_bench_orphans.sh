#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

raw_pids="$(pgrep -f "$ROOT_DIR/bench.py" || true)"
pids=()
if [[ -n "$raw_pids" ]]; then
  pids=("${(@f)raw_pids}")
fi

if (( ${#pids[@]} == 0 )); then
  echo "No bench.py processes found."
  exit 0
fi

echo "Killing bench.py PIDs: ${pids[*]}"
kill "${pids[@]}" 2>/dev/null || true
sleep 1

raw_still_running="$(pgrep -f "$ROOT_DIR/bench.py" || true)"
still_running=()
if [[ -n "$raw_still_running" ]]; then
  still_running=("${(@f)raw_still_running}")
fi
if (( ${#still_running[@]} > 0 )); then
  echo "Force killing remaining PIDs: ${still_running[*]}"
  kill -KILL "${still_running[@]}" 2>/dev/null || true
fi

echo "bench.py cleanup complete."
