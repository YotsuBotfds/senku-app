# Agent Tooling Upgrades - 2026-04-25

Purpose: make Senku's agent/RAG workflow faster without hiding risk. Prefer
repo-local helpers first, then optional global tools only after the repeated
friction is obvious.

## Current Friction

- Manual bench-summary spelunking: repeated `Get-Content` / CSV table commands
  for `artifacts/bench/*` comparisons.
- Agent handoff drift: scouts/workers return useful findings, but there is no
  durable run manifest tying task, model, files touched, validation, and commit.
- Worktree readability: user wants clean diffs after slices, and the repo has
  enough artifacts that status hygiene matters.
- RAG evaluation: the project now has diagnostics and shadow retrieval, but no
  single dashboard or gate that trends retrieval, evidence, card, and app
  acceptance metrics together.

## Phase 1 - Repo-Local, Immediate

1. Add compact artifact summarizers.
   - First target: a shadow-comparison summarizer that reads
     `compare_contextual_shadow_summary.json` directories and prints one table.
   - Keep this stdlib-only and checked into `scripts/`.
   - This directly replaces the manual work used for S15/S16 EX/EZ reads.
   - First dispatch: `notes/dispatch/RAG-T1_shadow_comparison_summarizer.md`.

2. Build a RAG trend table.
   - Inputs: existing analyzer `diagnostics.json` files, shadow comparator
     summaries, and bench metadata.
   - Outputs: a Markdown trend table per artifact family or selected run list.
   - First metrics: expected-guide hit@1/hit@3/hit@k, cited owner, answer-card
     status, claim support, app acceptance, generated-vs-shadow gaps, and
     generated/reviewed-card workload.
   - First dispatch: `notes/dispatch/RAG-T2_rag_diagnostics_trend_summarizer.md`.
   - Follow-up utility: `scripts/rag_trend.py` prints the startup panel the
     user asked for: app acceptance, answer-card status, claim support,
     generation workload, and evidence nugget coverage side by side.

3. Add agent run manifests.
   - Write JSONL records under ignored `artifacts/runs/` with:
     task, role, model, prompt label, files changed, diff stat, validation, and
     resulting commit.
   - This mirrors the repo's existing handoff culture without requiring a new
     service.
   - Utility: `scripts/write_run_manifest.py`, defaulting to
     `artifacts/runs/run_manifest.jsonl`.

4. Formalize subagent roles in notes.
   - Roles: scout, worker, validator, writer.
   - Keep write ownership explicit and disjoint.
   - Treat workers as patch producers, not silent owners of product direction.

## Phase 2 - Optional Local Services

1. Add a local eval-gate runner.
   - Ragas documents a RAG evaluation quickstart that stores experiment CSVs
     and supports custom metrics; use it as inspiration, not as a required
     dependency yet: <https://docs.ragas.io/en/stable/howtos/cli/rag_eval/>.
   - Phoenix has RAG evaluation/tracing workflows that could become an optional
     dashboard when local deterministic metrics are stable:
     <https://arize.com/docs/phoenix/cookbook/evaluation/evaluate-rag>.

2. Add repo-local MCP / docs connectors only when they remove real lookup cost.
   - OpenAI's Docs MCP is read-only and can be configured for developer docs:
     <https://platform.openai.com/docs/docs-mcp>.
   - OpenAI's MCP docs also call out prompt-injection risk for custom MCP
     servers, so this should stay allowlisted and minimal:
     <https://platform.openai.com/docs/mcp>.

3. Keep model-backend selection explicit.
   - The current repo already has split generation and embedding defaults.
   - Do not let a broad desktop model silently replace the LiteRT host lane.
   - Future helper should print active generation URL/model and embedding
     URL/model before any bench run.

## Phase 3 - Global Tools, Only If Earned

These are allowed by the user's current permission, but should stay optional:

- `git worktree` for parallel branches when multiple workers need real file
  ownership. Official docs: <https://git-scm.com/docs/git-worktree>.
- `pre-commit` for local quality gates after the repo has agreed hooks:
  <https://pre-commit.com/>.
- `uvx` for temporary pinned tool execution without polluting PATH:
  <https://docs.astral.sh/uv/guides/tools/>.
- `delta` for clearer diffs: <https://dandavison.github.io/delta/usage.html>.
- `lazygit` for fast branch/status inspection:
  <https://lazygit.dev/docs/getting-started/>.

Do not install global shell/theme tooling just because it is nice. Install it
when it saves repeated task time inside this repo.

## Adoption Order

1. Land the shadow-comparison summarizer.
2. Add a RAG trend summary over existing bench/analyzer artifacts.
3. Add an ignored `artifacts/runs/` manifest convention and a tiny writer.
4. Only then consider optional dashboards or global terminal tools.

## Current Quick Commands

Startup answer/card panel:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_trend.py artifacts\bench\rag_diagnostics_20260424_1654_rags12_meningitis_compare_final artifacts\bench\rag_diagnostics_20260424_1750_rags13_code_health_final_smoke --label rags12-gap --label rags13-final
```

Append a run manifest entry:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\write_run_manifest.py --task RAG-T --lane tooling --role main --label startup-trend --command "scripts\rag_trend.py ..." --output artifacts\runs\run_manifest.jsonl --validation "tests.test_rag_trend passed"
```

## Guardrail

Tooling is successful only if it reduces manual archaeology and keeps
production behavior more measurable. If it creates another surface that needs
care and feeding before answering guide questions, it is the wrong tool.
