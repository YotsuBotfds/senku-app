# Active Work Tracker - 2026-04-26

Purpose: one durable queue surface for planner handoffs. This file separates
active execution from parked dispatches and historical notes so agents do not
re-triage `notes/dispatch/` every session.

Last checked before this tracker slice: 2026-04-26 12:19 -05:00. Latest pushed
HEAD at that check was `0b9fa29` (`normalize evidence parser categories`), and
the `Master Head Health` run for that HEAD was green.

## Active Queue

1. `notes/deep_research_evidence_backlog_20260426.md`
   - Status: active non-Android queue.
   - Current work style: narrow evidence/tooling guardrails, focused tests,
     commit, push, then check time and latest `Master Head Health`.
   - Do not treat historical proof runs in that file as current HEAD proof
     without checking `git log -1` and `gh run list`.

## Active Operating Rules

- Protected local planner handoffs matching `notes/PLANNER_HANDOFF*.md` are
  benign local context; do not commit or delete them unless explicitly asked.
- Android/emulator work is deferred unless the user explicitly reopens the
  Android lane.
- Guide-body edits require re-ingest before retrieval claims can be trusted.
- Artifact retention remains approval-only; do not delete or move artifacts
  from backlog triage alone.
- Use small commits with focused validation and push each commit.

## Parked But Valid Dispatches

These are not active by default. Queue one only if a human explicitly selects
the lane, and refresh baselines first because later RAG/card/routing work may
have superseded assumptions.

- `notes/dispatch/D48_wave_ew_urgent_nosebleed_deterministic.md`
- `notes/dispatch/D49_wave_ex_choking_food_obstruction_deterministic.md`
- `notes/dispatch/D50_wave_ey_meningitis_stiff_neck_rash_deterministic.md`
- `notes/dispatch/D51_wave_ez_newborn_sepsis_sick_infant_deterministic.md`

## Deferred Lanes

- Android reviewed-card/runtime dispatches: `RAG-A*`, Android `RAG-CH*`, and
  `RAG-A14*` remain a separate lane.
- Mojibake guide-body cleanup: use `notes/dispatch/RAG-TOOL8_mojibake_cleanup_queue.md`
  as a backlog reference only; any guide-body repair needs safety review and
  re-ingest.
- Artifact retention cleanup: approval-ready planning exists, but execution
  requires explicit approval and a fresh dry run.

## Historical / Superseded Dispatch Surfaces

- `notes/dispatch/README.md` is a historical dispatch ledger plus orientation
  doc, not the live execution queue.
- `notes/CP9_ACTIVE_QUEUE.md` is historical CP9/RAG status context. It should
  point agents here for current queue selection.
- `RAG-S1` through `RAG-S22`, `RAG-T*`, `RAG-EVAL*`, `RAG-META*`, and
  `RAG-CARD1` through `RAG-CARD5` are landed history unless a fresh regression
  artifact reopens one.
- `RAG-CARD5` mentions `RE8-TR-001` as a next item, but later evidence work
  handled/proof-hardened that row. Treat that breadcrumb as superseded.
- Older `D*`, `W-C*`, `A1`, `P5`, and probe notes are superseded, completed,
  cancelled, or stale records unless reselected deliberately.

## Refreshes Applied In This Slice

- `notes/dispatch/README.md` now points here.
- `notes/CP9_ACTIVE_QUEUE.md` now points here.
- `RAG-CARD5`'s `RE8-TR-001` breadcrumb is marked superseded.
- `RAG-TOOL8`'s "next active/now-running tranche" wording is marked stale.
