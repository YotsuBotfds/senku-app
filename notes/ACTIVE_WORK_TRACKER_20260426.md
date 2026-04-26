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

## Current Tool-Derived Queue

1. EVAL9 retrieval proof path:
   - Status: current-head retrieval proof complete at `e2dee41`.
   - `validate_prompt_expectations.py` passed on `artifacts/prompts/adhoc/rag_eval9_high_liability_compound_holdouts_20260426.jsonl` with `8` prompts and `0` warnings.
   - `evaluate_retrieval_pack.py --top-k 8` produced `8/8` expected hit@1 and `8/8` primary hit@1 with `0` retrieval errors.
   - Retrieval expectation cross-check passed with `--fail-on-errors --fail-on-warnings`.
   - No profile comparison needed unless later changes reopen weak EVAL9 rows.
2. Metadata/card surfacing:
   - Completed in this slice: reviewed answer cards for `GD-666`, `GD-732`, `GD-035`, `GD-024`, and `GD-636`; metadata audit now reports no gaps for those five guides.
   - Completed in follow-up medical slice: reviewed answer cards for `GD-298`, `GD-617`, `GD-635`, `GD-579`, and `GD-526`; metadata audit now reports no gaps for those five guides.
   - Completed in final near-finished slice: reviewed answer cards for `GD-054`, `GD-492`, `GD-602`, `GD-858`, and `GD-918`; metadata audit now reports no gaps for those five guides.
   - No near-finished card-only blockers remain; next card work should move to broad critical metadata/card gaps or safety-reviewed unresolved partials.
   - Completed first broad critical application slice: frontmatter metadata plus reviewed cards for `GD-037`, `GD-051`, `GD-396`, `GD-584`, and `GD-935`; focused card/audit/routing tests pass and metadata audit reports no gaps for those five guides.
   - Completed second broad critical application slice: frontmatter metadata plus reviewed cards for `GD-025`, `GD-039`, and `GD-227`; focused card/audit/routing tests pass and metadata audit reports no gaps for those three guides.
   - Completed narrow safety-review closures for `GD-058` and `GD-133`: both are boundary-only reviewed cards that avoid anesthesia synthesis/dosing/procedure detail and firearm manufacture/ammunition/procedural repair detail.
   - Completed broad medical safety-card tranche: metadata/card closure for `GD-900`, `GD-936`, `GD-299`, `GD-655`, `GD-916`, and `GD-915`, plus structured metadata/citation closure for existing reviewed `GD-380` and `GD-400` cards. Scope notes: seizure card stays first-aid/status-threshold only, asthma stays support/escalation only, withdrawal stays severe withdrawal/overdose safety only, radiation stays exposure/decontamination/ARS danger triage only, baby-discomfort card is bounded away from newborn sepsis/pediatric emergencies, and back-pain routing was narrowed away from stiff neck/sprained ankle/general trauma.
   - Safety-reviewed deferrals from the broad critical queue: `GD-044`, `GD-045`, `GD-059`, and `GD-152`. Do not quick-close these with metadata/card churn; route them to deliberate safety-review or source-boundary work because the guide bodies are procedure-dense and could make vaccine/pharma/insulin/explosive manufacture or dosing more actionable.
   - Remaining broad critical metadata/card gaps should be selected after excluding the explicit deferrals above. Treat pharma/manufacturing/weapons/explosives lanes as deliberate safety-review work, not quick card churn.
3. Corpus/content hygiene:
   - `51` unresolved partials remain actionable, with critical/high guides first.
   - Guide-body edits require safety review and re-ingest before retrieval claims.
4. Runtime/citation behavior:
   - Status: desktop card-backed answer composer now suppresses repeated inline citations for the same source while preserving `cited_guide_ids`.
   - Direct cardiac proof: GD-601 card-backed answer cites `[GD-601]` once and `_duplicate_citation_count(...)` returns `0`.
   - Follow-up review found broad `GD-298` pediatric card matching could crowd out the more specific choking card; `0591a5c` gates that broad card away from airway/newborn/meningitis prompts and restores the routing/card test slice.
   - Recent bench artifacts showed duplicate-citation hotspots in card-backed/runtime rows, especially RE6 cardiac, RE7 young-infant, and EVAL8 compound food/medical cases.
   - Re-check young-infant and contaminated-creek uncertain-fit rows after routing/card changes.

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
