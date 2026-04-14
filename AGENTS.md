# AGENTS.md

This file is the fast landing page for agents working in this repo.

Keep it light and referential. When process or status details grow, move them into focused markdown files and link them here.

## Repository Layout

- Historical benchmark outputs and progress logs live under [`artifacts/bench`](./artifacts/bench)
- Ad hoc benchmark prompt packs and temporary batch files live under [`artifacts/prompts`](./artifacts/prompts)
- One-off handoffs, research notes, and review notes live under [`notes`](./notes)
- The Android app subproject lives under [`android-app`](./android-app)

## Quick Start

PowerShell:
```powershell
. .\venv\Scripts\Activate.ps1
python query.py
python ingest.py --stats
```

POSIX:
```bash
source venv/bin/activate
python3 query.py
python3 ingest.py --stats
```

## Core Entry Points

Desktop:
- [`config.py`](./config.py): runtime config, models, prompt, `TOP_K`
- [`query.py`](./query.py): query decomposition, retrieval, prompt build, streaming
- [`bench.py`](./bench.py): benchmark runner
- [`ingest.py`](./ingest.py): guide ingestion into ChromaDB
- [`deterministic_special_case_registry.py`](./deterministic_special_case_registry.py): declarative deterministic rule registry
- [`special_case_builders.py`](./special_case_builders.py): deterministic/control-path response templates
- [`GUIDE_PLAN.md`](./GUIDE_PLAN.md): living guide-direction to-do; separates true KB gaps from guide-expansion and retrieval-surfacing work
- [`guideupdates.md`](./guideupdates.md): concrete open guide defects only; do not use it as a speculative backlog
- OpenCode sidecar workflow: use `$opencode-sidecar` for async review/delegation and polling-based sidecar tasks

Shared support:
- [`lmstudio_utils.py`](./lmstudio_utils.py): LM Studio retry and embedding fallback helpers
- [`guide_catalog.py`](./guide_catalog.py): live guide-id/slug catalog used for citation validation
- [`token_estimation.py`](./token_estimation.py): chunk-sizing helper

Mobile:
- [`android-app/README.md`](./android-app/README.md): Android build/install notes
- [`scripts/export_mobile_pack.py`](./scripts/export_mobile_pack.py): mobile pack export entry point
- [`mobile_pack.py`](./mobile_pack.py): mobile pack builder/exporter
- [`scripts/push_mobile_pack_to_android.ps1`](./scripts/push_mobile_pack_to_android.ps1): hot-swap a refreshed mobile pack into a debuggable emulator/device without rebuilding the APK
- [`scripts/run_android_prompt.ps1`](./scripts/run_android_prompt.ps1): emulator prompt automation harness
- [`scripts/run_android_search_log_only.ps1`](./scripts/run_android_search_log_only.ps1): long Android retrieval checks from logcat without repeated UI-dump polling
- [`scripts/run_android_detail_followup_logged.ps1`](./scripts/run_android_detail_followup_logged.ps1): single follow-up runner with automatic logcat capture
- [`scripts/run_android_followup_matrix.ps1`](./scripts/run_android_followup_matrix.ps1): multi-case follow-up batch runner with JSONL/CSV summaries
- [`scripts/stop_android_harness_runs.ps1`](./scripts/stop_android_harness_runs.ps1): stop orphaned Android prompt/follow-up harness processes before a clean replay
- [`scripts/run_mobile_headless_preflight.py`](./scripts/run_mobile_headless_preflight.py): emulator-free family-pack and metadata-audit preflight
- [`scripts/run_mobile_headless_answers.py`](./scripts/run_mobile_headless_answers.py): off-emulator mobile-pack retrieval/context/prompt replay with optional host generation
- [`scripts/validate_agent_state.py`](./scripts/validate_agent_state.py): schema validator for the machine-readable multi-agent state file
- [`scripts/run_guide_prompt_validation.ps1`](./scripts/run_guide_prompt_validation.ps1): run the targeted guide-direction prompt packs (`wave_w`, `wave_x`, `wave_y`) after a clean re-ingest
- [`scripts/start_fastembed_server.ps1`](./scripts/start_fastembed_server.ps1): start the lightweight OpenAI-compatible embedding host for the Nomic model without waking the desktop LM Studio lane
- [`scripts/build_guide_graph.py`](./scripts/build_guide_graph.py): extract a machine-readable guide relationship graph from frontmatter `related:` links and explicit guide-to-guide references
- [`scripts/extract_guide_invariants.py`](./scripts/extract_guide_invariants.py): extract numeric/unit-bearing guide lines as a first-pass invariant audit surface for contradiction review
- [`scripts/find_guide_audit_hotspots.py`](./scripts/find_guide_audit_hotspots.py): rank the most audit-worthy guides and connected guide pairs from the graph plus invariant surface
- [`scripts/find_invariant_conflict_candidates.py`](./scripts/find_invariant_conflict_candidates.py): generate a trimmed heuristic contradiction-review queue from hotspot guides plus invariant overlap

## Local MCP Helpers

- [`opencode.json`](./opencode.json): enables repo-local MCP servers for OpenCode
- `context7`: use for library/docs grounding when you want current API or setup details without generic web-search drift
- `git`: use for repo history, diff, branch, and git-aware reasoning inside OpenCode; it only becomes useful in a real git checkout with a `.git` root
- `sequential-thinking`: use for stepwise planning, decomposition, and “think before acting” prompts
- `puppeteer`: use for browser-based UI testing, screenshots, navigation, and repeatable web checks
- Keep these as helper lanes; prefer the existing OpenCode sidecar, Qwen scout, and Spark workers for the actual swarm workflow
- Context7’s free tier is good enough to start; add an API key later only if you need higher limits or fewer rate caps
- Keep the Context7 key out of the repo; load it from your local user environment only
- Use `sequential-thinking` when a task needs more structure than a plain prompt
- Use `puppeteer` when you need to verify browser behavior instead of relying on description alone
- Do not treat either MCP as a replacement for repo-local validation, Android harnesses, or sidecar-backed coding work

## Testing

- Read [`TESTING_METHODOLOGY.md`](./TESTING_METHODOLOGY.md) for the living desktop/mobile validation workflow
- Focused local tests: `python3 -m unittest discover -s tests -v`
- Deterministic citation/routing guard: `python3 scripts/validate_special_cases.py`
- Current prompt suite: `test_prompts.txt` with 171 prompts across 24 sections

## Active Workstreams

- Desktop retrieval/routing remains the quality reference
- Android app is the active parity project: retrieval quality, prompt shaping, and chat/session continuity
- Mobile now targets both `E2B` and `E4B`: `E2B` is the practical floor, `E4B` is the quality tier to keep evaluating
- Session-aware follow-up behavior is part of normal validation now, not a later extra
- Guide direction now has a living backlog in [`GUIDE_PLAN.md`](./GUIDE_PLAN.md); keep true content gaps separate from retrieval-language fixes
- Guide-direction execution now has an explicit worklog plus validation log; keep backlog, worklog, and review state in sync so another agent can continue cleanly
- Guide-family merge decisions should follow [`notes/GUIDE_RAG_SURFACING_20260413.md`](./notes/GUIDE_RAG_SURFACING_20260413.md) so we optimize for RAG answerability, not just fewer files
- OpenCode sidecars are now a practical coding lane for Android/UI work, not just review; prefer staggered batch submission over true simultaneous bursts
- Multi-model cost posture: keep Codex thin and strategic; push cheap scouting to local Qwen, bounded patches to Spark, and longer async work to OpenCode sidecars first

## Mobile Entry Points

- [`android-app/README.md`](./android-app/README.md): current APK build/install path
- [`TESTING_METHODOLOGY.md`](./TESTING_METHODOLOGY.md): search-only, ask-offline, and session-follow-up validation flow
- [`scripts/run_android_search_log_only.ps1`](./scripts/run_android_search_log_only.ps1): preferred harness for long search/retrieval checks where logcat is enough
- [`notes/AGENT_STATE.yaml`](./notes/AGENT_STATE.yaml): machine-readable state snapshot for emulator lanes, subagents, active artifacts, and immediate next actions
- [`notes/ACTIVE_WORK_LOG_20260412.md`](./notes/ACTIVE_WORK_LOG_20260412.md): current continuation floor, snapshot paths, and latest validated checkpoints
- [`notes/ANDROID_ROADMAP_20260412.md`](./notes/ANDROID_ROADMAP_20260412.md): compact roadmap spine for the Android parity push
- [`notes/UI_SECOND_OPINION_20260413.md`](./notes/UI_SECOND_OPINION_20260413.md): product/IA second opinion on the current Android knowledge-hub direction
- [`notes/SIDECAR_CODING_LANE_20260414.md`](./notes/SIDECAR_CODING_LANE_20260414.md): how the OpenCode sidecar is now being used for real coding work, including staggered batch submission and registry-lock cautions
- [`notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md`](./notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md): practical multi-model lane policy for reducing Codex usage by routing more work to GLM sidecars, Spark workers, and local Qwen scouts
- [`notes/AGENT_MANAGEMENT_WORKFLOW_20260414.md`](./notes/AGENT_MANAGEMENT_WORKFLOW_20260414.md): compact practical workflow for routing tasks to the cheapest safe lane, keeping Codex thin, and avoiding overlapping agent work
- [`notes/METADATA_AUDIT_20260412.md`](./notes/METADATA_AUDIT_20260412.md): pack-audit findings and metadata repair direction
- [`notes/GUIDE_DIRECTION_WORKLOG_20260413.md`](./notes/GUIDE_DIRECTION_WORKLOG_20260413.md): current guide-direction execution trail, landed waves, and next preferred batch
- [`notes/GUIDE_VALIDATION_LOG_20260413.md`](./notes/GUIDE_VALIDATION_LOG_20260413.md): which new guide-direction additions have received explicit quality review and what fixes were applied
- [`notes/GUIDE_EXECUTION_SPINE_20260413.md`](./notes/GUIDE_EXECUTION_SPINE_20260413.md): compact `do next / validate first / gated` execution order plus the current RAG-family clusters
- [`notes/GUIDE_VALIDATION_SCORECARD_20260413.md`](./notes/GUIDE_VALIDATION_SCORECARD_20260413.md): quick `clear win / weak acceptable / miss` rubric plus expected family winners for `wave_w`, `wave_x`, and `wave_y`
- [`notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md`](./notes/GUIDE_PROMPT_VALIDATION_QUEUE_20260413.md): targeted prompt-pack order and failure-triage loop for the latest guide-direction wave
- [`notes/GUIDE_RAG_SURFACING_20260413.md`](./notes/GUIDE_RAG_SURFACING_20260413.md): merge-vs-link rules for guide families so retrieval stays precise
- [`notes/GUIDE_CONTRADICTION_TRIAGE_20260413.md`](./notes/GUIDE_CONTRADICTION_TRIAGE_20260413.md): human triage decisions for the machine-generated contradiction queue so later agents do not re-open the same false positives
- [`notes/HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md`](./notes/HIGH_RISK_GUIDE_REVIEW_LANE_20260413.md): guardrails and source expectations before drafting suicide-prevention or abuse-response guides
- [`notes/FUTURE_GUIDE_RESEARCH_20260413.md`](./notes/FUTURE_GUIDE_RESEARCH_20260413.md): official-source starting points for lower-priority future guide candidates after current prompt validation
- [`artifacts/guide_conflict_candidates/guide_conflict_candidates.md`](./artifacts/guide_conflict_candidates/guide_conflict_candidates.md): current machine-generated contradiction-review queue for cross-guide consistency checks
- [`notes/ANDROID_EMULATOR_SWEEP_20260411.md`](./notes/ANDROID_EMULATOR_SWEEP_20260411.md): latest broad emulator sweep
- [`notes/NEXT_AGENT_HANDOFF_20260411.md`](./notes/NEXT_AGENT_HANDOFF_20260411.md): current project state and recommended next steps

## Current Baseline

- Desktop retrieval uses hybrid retrieval plus metadata-aware reranking and structured session state in [`query.py`](./query.py)
- The mobile pack currently reports `96` deterministic rules
- The active Android debug APK path is `android-app/app/build/outputs/apk/debug/app-debug.apk`
- Emulator inference currently works on the CPU path with Gemma 4 `E2B`; keep testing `E4B` as hardware improves
- Exact host inference with the phone artifact is an active validation path via `http://10.0.2.2:1235/v1` using `gemma-4-e2b-it-litert`
- Android follow-up validation now has dedicated harness support; prefer the logged single-run and matrix scripts over ad hoc multi-turn tapping
- Long Android search validation now has a log-only lane; prefer it over `uiautomator`-polled runs for slow retrieval checks
- Family-pack validation now has a headless preflight lane; use it to triage breadth work before spending emulator time
- Pack-only Android iterations now have a fast lane via [`scripts/push_mobile_pack_to_android.ps1`](./scripts/push_mobile_pack_to_android.ps1); prefer it over rebuild/reinstall when code has not changed
- [`scripts/run_android_prompt.ps1`](./scripts/run_android_prompt.ps1) and [`scripts/run_android_detail_followup.ps1`](./scripts/run_android_detail_followup.ps1) now accept `-PushPackDir` to hot-swap a pack and launch the replay in one command
- Explicit gravity-fed water-distribution prompts now have a dedicated Android `water_distribution` lane; latest `5556` replay anchored on `GD-270`

## Practical Cautions

- Re-ingest after guide edits before trusting desktop retrieval results
- Distinguish true knowledge gaps from retrieval-language issues before adding guides
- For safety-critical guide edits, grep the repo for shared formulas, thresholds, dose-like statements, and other invariants before validation
- Keep [`GUIDE_PLAN.md`](./GUIDE_PLAN.md) as the running backlog and [`guideupdates.md`](./guideupdates.md) as the concrete active-fix tracker
- For guide-family cleanup, prefer stronger routing blocks and reciprocal links before merging files; use [`notes/GUIDE_RAG_SURFACING_20260413.md`](./notes/GUIDE_RAG_SURFACING_20260413.md)
- If a mobile test run is interrupted, verify there are no orphaned harness processes before retrying
- Let slow emulator runs finish unless logs show no phase progress; diagnose retrieval stalls separately from generation stalls
- For fresh long search-log traces, use `scripts/run_android_search_log_only.ps1 -ClearLogcatBeforeLaunch` so stale logcat lines do not get mistaken for the current run
- Android runtime is currently logging `fts.unavailable` / `no such module: fts5`; do not assume current lexical timings reflect the intended FTS-backed path
- For guide-direction work, treat drafting and validation as separate steps; consult [`notes/GUIDE_VALIDATION_LOG_20260413.md`](./notes/GUIDE_VALIDATION_LOG_20260413.md) before assuming a new guide is fully signed off
- Prompt-pack validation should use an explicit LiteRT generation host plus a separate embedding-capable endpoint; do not silently fall back to the desktop LM Studio 26B lane
- LiteRT guide-validation smoke now works with `top_k=8`; treat the desktop `top_k=24` default as too large for the current 4096-token LiteRT context window
- Run the graph/invariant/hotspot/conflict candidate lane before opening a broad new prompt wave so numeric/unit contradictions are surfaced before reranking effort.
- For long or interruptible bench runs, prefer `zsh scripts/run_bench_guarded.sh ...`; if a prior session was interrupted, clean up with `zsh scripts/kill_bench_orphans.sh`
- For wide sidecar coding waves, prefer [`scripts/enqueue_opencode_sidecar_batch.ps1`](./scripts/enqueue_opencode_sidecar_batch.ps1) with spaced submission (for example `60s`) to avoid `artifacts/opencode/sidecar/index.jsonl` contention and flaky simultaneous-enqueue failures
