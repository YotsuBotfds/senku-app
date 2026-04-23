# Swarm / Model Routing Index

Single stop for multi-model orchestration. As of 2026-04-21, the OpenCode sidecar coding lane and the entire generic engineering swarm orchestration layer are archived (see `archive/README.md`); the active workflow is the GPT subagent split.

## Active workflow

- [`SUBAGENT_WORKFLOW.md`](./SUBAGENT_WORKFLOW.md): **authoritative** Codex-side role split (main `gpt-5.4 xhigh` / scout `gpt-5.3-codex-spark xhigh` / worker `gpt-5.4 high`); main agent owns delegation. Planner briefs main with slice files in [`dispatch/`](./dispatch).

## Qwen scout helpers (still active for cheap read-only scouting)

- [`../scripts/start_qwen27_scout_job.ps1`](../scripts/start_qwen27_scout_job.ps1): detached async scout job.
- [`../scripts/get_qwen27_scout_job.ps1`](../scripts/get_qwen27_scout_job.ps1): detached scout polling.

## MCP helpers

- [`../opencode.json`](../opencode.json): repo-local MCP server config (context7, git, sequential-thinking, puppeteer). MCP servers themselves stay useful even though the OpenCode sidecar workflow is archived.
- `context7`: docs grounding.
- `git`: history/diff context.
- `sequential-thinking`: structured decomposition.
- `puppeteer`: browser verification.

## Routing ladder

GPT subagent lanes first (main `gpt-5.4 xhigh`, worker `gpt-5.4 high`, scout `gpt-5.3-codex-spark xhigh`), then local compute fallback when needed (`GLM 5.1 > Spark > Qwen 27B > Qwen 9B`).

- Use a `gpt-5.4 high` worker for exact file edits and file-scoped review when main wants to offload.
- Use Qwen for cheap scouting, prompt ideas, and bounded read-only support.

## What's been archived (2026-04-21)

The following workflows are no longer active. See `archive/README.md` for the full list.

- OpenCode sidecar coding lane (17 scripts + 1 dedicated workflow doc).
- Generic engineering swarm orchestration layer (10 scripts + 5 notes including operations, architecture, cost-reduction, agent-management, and minihigh-orchestration docs).

If any of the archived workflows needs to come back, the files are at `archive/scripts/` and `archive/notes/` and can be restored with plain `mv`.
