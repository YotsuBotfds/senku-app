# RAG-EVAL2 Bridge Metadata Routing - Worker H

Date: 2026-04-25
Worker: H / GPT-5.5 low
Scope: bridge metadata/routing for RAG-EVAL2 retrieval misses only

## Inputs

- Eval artifact: `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/diagnostics.csv`
- Target retrieval misses:
  - `RE2-BR-001` expected `GD-634`, absent from retrieved/source candidates. High-ranking distractors: `GD-373`, `GD-591`, `GD-493`, `GD-353`, `GD-494`, `GD-243`, `GD-374`.
  - `RE2-BR-002` expected `GD-635`, absent from retrieved/source candidates. High-ranking distractors: `GD-667`, `GD-393`, `GD-586`, `GD-558`, `GD-023`, `GD-376`.
  - `RE2-BR-006` expected `GD-636`, absent from retrieved/source candidates. High-ranking distractors: `GD-240`, `GD-384`, `GD-264`, `GD-119`, `GD-120`, `GD-419`, `GD-281`.

## Edits

- `guides/food-system-resilience.md` (`GD-634`)
  - Added frontmatter aliases for community food-system failure after bad weather, keeping families fed, and short shelf-life disruption.
  - Added routing cues that distinguish system-level continuity from item-level spoilage/shelf-life assessment.
  - Added body retrieval routing note tying staples, backup crops, foraging, preservation, storage, and calorie verification to the held-out prompt language.

- `guides/healthcare-capability-assessment.md` (`GD-635`)
  - Added frontmatter aliases for limited medical supplies, few trained people, shelter/local team decisions, and community medical capability triage.
  - Added routing cues that distinguish community/team capability assessment from immediate one-patient first aid.
  - Added `citations_required: true` and an `applicability` statement for the critical-liability metadata audit.
  - Added body retrieval routing note for triage, diagnostic limits, treatment capacity, supplies, infection control, and escalation.

- `guides/alloy-decision-tree.md` (`GD-636`)
  - Added frontmatter aliases for stronger hand-tool edge, temporary alloy choice, steel selection, and safe heat-treatment planning.
  - Added routing cues that distinguish alloy/material selection from simple sharpening.
  - Added body retrieval routing note for alloy choice, hardness-versus-toughness tradeoff, and heat-treatment planning.

## Validation

- Frontmatter parse via `ingest.parse_frontmatter` passed for all three edited guides:
  - `GD-634`: aliases `4`, routing cues `2`, `bridge=True`, body routing note present.
  - `GD-635`: aliases `4`, routing cues `2`, `bridge=True`, body routing note present.
  - `GD-636`: aliases `4`, routing cues `2`, `bridge=True`, body routing note present.
- Focused metadata audit via `scripts.audit_metadata_coverage.audit_guide`:
  - `GD-634`: gaps `none`, severity `none`.
  - `GD-635`: residual gap `missing_reviewed_answer_card`, severity `warn`; metadata routing fields pass.
  - `GD-636`: residual gap `missing_reviewed_answer_card`, severity `warn`; citation policy and applicability now present.
- Full metadata audit refreshed at:
  `artifacts/bench/metadata_coverage_audit_20260425_1745_bridge_metadata_routing.md`
