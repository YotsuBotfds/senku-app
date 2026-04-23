# archive/

Files moved out of the active tree because they are no longer being used or actively edited.

## archive/scripts/ (2026-04-21)

OpenCode sidecar workflow scripts and the standalone Node sidecar server. Replaced by the GPT subagent lanes (`gpt-5.4 xhigh` / `gpt-5.4 high` / `gpt-5.3-codex-spark xhigh`) per `notes/SUBAGENT_WORKFLOW.md`. None of these were git-tracked.

- `abort_opencode_sidecar.ps1`
- `aggregate_sidecar_results.ps1`
- `enqueue_opencode_sidecar.ps1`
- `enqueue_opencode_sidecar_batch.ps1`
- `enqueue_qwen27_sidecar.ps1` (was OpenCode-backed local Qwen sidecar)
- `get_opencode_sidecar_result.ps1`
- `get_opencode_sidecar_status.ps1`
- `list_opencode_sidecar_tasks.ps1`
- `opencode_sidecar_common.ps1`
- `opencode_sidecar_server.mjs`
- `run_opencode_http_prompt.ps1`
- `run_opencode_prompt.ps1`
- `send_opencode_sidecar_input.ps1`
- `start_opencode_server.ps1`
- `start_opencode_sidecar_server.ps1`
- `start_sidecar_viewer.ps1`
- `watch_opencode_sidecar.ps1`

## archive/scripts/ - second batch (2026-04-21, post-cascade decision)

The orchestration layer that depended on the OpenCode sidecar scripts. All non-functional after the first batch landed; user confirmed "we use gpt subagents now" and instructed batch archive.

- `classify_swarm_task.ps1`
- `install_engineering_swarm_scheduled_task.ps1`
- `new_engineering_swarm_run.ps1`
- `overnight_continue_loop.ps1`
- `run_engineering_swarm.ps1`
- `run_guide_swarm_autopilot.ps1`
- `run_guide_swarm_manager.ps1`
- `run_swarm_validation_gate.ps1`
- `write_engineering_swarm_heartbeat.ps1`
- `write_swarm_heartbeat.ps1`

## archive/notes/ (2026-04-21)

- `SIDECAR_CODING_LANE_20260414.md` - dedicated workflow doc for the OpenCode sidecar coding lane. Superseded by `notes/SUBAGENT_WORKFLOW.md`.
- `ENGINEERING_SWARM_OPERATIONS_20260416.md` - operations notes for the generic engineering swarm lane. Lane archived.
- `ENGINEERING_SWARM_ARCHITECTURE_20260416.md` - architecture notes for the same lane.
- `SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` - cost-routing policy for OpenCode-based workflow. Was git-tracked (the only one); moved via `git mv` so history follows.
- `AGENT_MANAGEMENT_WORKFLOW_20260414.md` - cheapest-safe-lane workflow doc, OpenCode-coupled.
- `MINIHIGH_ORCHESTRATION_20260416.md` - OpenCode mini-high model orchestration notes.

## Restore

If anything here needs to come back, plain `mv` from `archive/<subdir>/<file>` to its original location. The only git-tracked move was `SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` - restore via `git mv` to preserve history.

## Cascade resolved 2026-04-21

The 6 cascade scripts flagged in the first batch are now in this second batch. Plus 4 additional `engineering_swarm` scripts that turned out to also depend on OpenCode (`classify_swarm_task.ps1`, `install_engineering_swarm_scheduled_task.ps1`, `new_engineering_swarm_run.ps1`, `write_engineering_swarm_heartbeat.ps1`). `notes/SWARM_INDEX.md` has been rewritten to drop dead references.
