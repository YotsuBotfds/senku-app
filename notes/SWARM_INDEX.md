# Swarm / Model Routing Index

Use this as the first stop for multi-model orchestration. The active workflow is
the GPT subagent split plus bounded local/sidecar scouts; the OpenCode sidecar
coding lane and the generic engineering swarm layer are archived and tracked in
[`../archive/README.md`](../archive/README.md).

## Active workflow

- [`SUBAGENT_WORKFLOW.md`](./SUBAGENT_WORKFLOW.md): authoritative Codex-side
  role split and delegation contract. Default posture is medium reasoning for
  general scouts/workers and high only for delicate safety, ownership, or
  integration work. When the user explicitly grants the separate Spark lane,
  use `gpt-5.3-codex-spark` high for bounded scouts/workers.
- [`AGENT_TOOLING_UPGRADES_20260425.md`](./AGENT_TOOLING_UPGRADES_20260425.md): near-future tooling lane for repo-local summarizers, run manifests, and RAG trend gates before optional global tooling.
- [`../scripts/start_qwen27_scout_job.ps1`](../scripts/start_qwen27_scout_job.ps1): detached async scout job
- [`../scripts/get_qwen27_scout_job.ps1`](../scripts/get_qwen27_scout_job.ps1): detached scout polling
- [`../opencode.json`](../opencode.json): repo-local MCP server config (context7, git, sequential-thinking, puppeteer)

## Routing ladder

GPT subagent lanes first, then local compute fallback when needed
(`GLM 5.1 > Spark > Qwen 27B > Qwen 9B`).

- Use medium reasoning for ordinary scout/worker tasks.
- Use high reasoning for exact file edits, safety-sensitive review, ambiguous
  ownership, or integration work.
- Use the explicitly granted `gpt-5.3-codex-spark` high lane for bounded
  scouts/workers when conserving the main usage pool matters.
- Use Qwen for cheap scouting, prompt ideas, and bounded read-only support.

## Archived workflow context

- [`../archive/README.md`](../archive/README.md): tracked archive index for the OpenCode sidecar scripts and the archived engineering swarm notes
- `ENGINEERING_SWARM_ARCHITECTURE_20260416.md` remains archive-only historical context; this index does not restore or recreate it.
- If an archived workflow needs to come back, restore from `archive/scripts/` or `archive/notes/` as described in the archive index.
