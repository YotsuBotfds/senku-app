# Open In Codex

This bundle is intended to be extracted directly to:

- `/Users/tbronson/Desktop/senku_local_testing_bundle_20260410`

If you want to use the bundled `venv`, do not rename the folder after extraction.

## What Is Included

- the runnable Senku codebase needed for local testing
- `AGENTS.md` and `CLAUDE.md`
- `guides/`
- `db/`
- `scripts/`
- `tests/`
- `notes/`
- `artifacts/prompts/`
- `artifacts/bench/`
- a Desktop-path-patched `venv/`
- local LM Studio model snapshot from `2026-04-10`

## What Is Not Included

This zip does not include the LM Studio app itself or the actual local model weights outside this workspace.

If this is opened on a different machine, Codex should verify:

```bash
curl -s http://localhost:1234/v1/models
```

and confirm that the intended local models are loaded.

## Suggested First Prompt For Codex

`Read AGENTS.md, README_OPEN_IN_CODEX.md, and CURRENT_LOCAL_TESTING_STATE_20260410.md first, then continue the local-only Senku model comparison on this machine. Start with the smoke pack, use qwen/qwen3.5-9b and gemma-4-e4b-it as the main laptop candidates, and only test google/gemma-4-26b-a4b if runtime looks practical.`

## Quick Start

```bash
cd /Users/tbronson/Desktop/senku_local_testing_bundle_20260410
source venv/bin/activate
python -m py_compile bench.py query.py ingest.py
zsh scripts/verify_local_runtime.sh
```

If the bundled `venv` fails:

```bash
zsh scripts/rebuild_venv_if_needed.sh
```
