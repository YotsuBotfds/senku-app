# RAG-TOOL3 Live Queue Monitor

## Why This Slice

Concurrent work is moving through dispatch notes, bench artifacts, and git
status quickly. A local browser monitor gives agents a cheap shared dashboard
without asking the main RAG merge lane to keep repeating status checks.

## Scope

Add a stdlib-only local webapp under `scripts/` that serves:

- an HTML dashboard at `/`
- a JSON snapshot at `/status.json`
- git status summary and latest commits
- active dispatch and tooling note pointers
- newest bench diagnostics and artifacts
- a snapshot timestamp

## Usage

```powershell
& .\.venvs\senku-validate\Scripts\python.exe scripts\live_queue_monitor.py --host 127.0.0.1 --port 8765
```

Open `http://127.0.0.1:8765/`. The browser fetches fresh JSON about every 20
seconds.

## Guardrails

- Do not touch active guide files as part of this tooling slice.
- Do not edit `notes/dispatch/RAG-EVAL4_partial_router_followup_priorities.md`.
- Do not edit `notes/PLANNER_HANDOFF_2026-04-25_FAST_MODE.md`.
- Keep monitor tests server-independent where possible.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_live_queue_monitor -v
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\live_queue_monitor.py tests\test_live_queue_monitor.py
```
