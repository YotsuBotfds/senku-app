# Swarm / Model Routing Index

Use this as the first stop for multi-model orchestration. The active workflow is the GPT subagent split; the OpenCode sidecar coding lane and the generic engineering swarm layer are archived and tracked in [`../archive/README.md`](../archive/README.md).

## Active workflow

- [`SUBAGENT_WORKFLOW.md`](./SUBAGENT_WORKFLOW.md): authoritative Codex-side role split (main `gpt-5.4 xhigh` / scout `gpt-5.3-codex-spark xhigh` / worker `gpt-5.4 high`); main agent owns delegation and takes slice briefs from [`dispatch/`](./dispatch).
- [`../scripts/start_qwen27_scout_job.ps1`](../scripts/start_qwen27_scout_job.ps1): detached async scout job
- [`../scripts/get_qwen27_scout_job.ps1`](../scripts/get_qwen27_scout_job.ps1): detached scout polling
- [`../opencode.json`](../opencode.json): repo-local MCP server config (context7, git, sequential-thinking, puppeteer)

## Routing ladder

GPT subagent lanes first (main `gpt-5.4 xhigh`, worker `gpt-5.4 high`, scout `gpt-5.3-codex-spark xhigh`), then local compute fallback when needed (`GLM 5.1 > Spark > Qwen 27B > Qwen 9B`).

- Use a `gpt-5.4 high` worker for exact file edits and file-scoped review when main wants to offload.
- Use Qwen for cheap scouting, prompt ideas, and bounded read-only support.

## Archived workflow context

- [`../archive/README.md`](../archive/README.md): tracked archive index for the OpenCode sidecar scripts and the archived engineering swarm notes
- `ENGINEERING_SWARM_ARCHITECTURE_20260416.md` remains archive-only historical context; this index does not restore or recreate it.
- If an archived workflow needs to come back, restore from `archive/scripts/` or `archive/notes/` as described in the archive index.
