#!/bin/zsh

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -d venv ]]; then
  echo "venv not found in $ROOT_DIR" >&2
  exit 1
fi

source venv/bin/activate

child_pid=""
raw_existing_bench_pids="$(pgrep -f "bench.py" || true)"
existing_bench_pids=()
if [[ -n "$raw_existing_bench_pids" ]]; then
  existing_bench_pids=("${(@f)raw_existing_bench_pids}")
fi

if (( ${#existing_bench_pids[@]} > 0 )); then
  echo "Refusing to start: existing bench.py process(es) detected: ${existing_bench_pids[*]}" >&2
  echo "Run: zsh scripts/kill_bench_orphans.sh" >&2
  exit 1
fi

cleanup() {
  local exit_code=$?
  trap - EXIT INT TERM HUP

  if [[ -n "$child_pid" ]] && kill -0 "$child_pid" 2>/dev/null; then
    kill -TERM "$child_pid" 2>/dev/null || true
    sleep 1
    if kill -0 "$child_pid" 2>/dev/null; then
      kill -KILL "$child_pid" 2>/dev/null || true
    fi
    wait "$child_pid" 2>/dev/null || true
  fi

  exit "$exit_code"
}

trap cleanup EXIT INT TERM HUP

python3 bench.py "$@" &
child_pid=$!
wait "$child_pid"
bench_status=$?
child_pid=""
exit "$bench_status"
