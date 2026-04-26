# RAG-EVAL4 Partial Router Follow-up Priorities

## Slice

Capture the concrete follow-up queue from the `RAG-EVAL2` held-out pack after
marker-overlay diagnostics.

## Source

- Prompt pack: `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`
- Diagnostics:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/report.md`
- Marker scan:
  `artifacts/bench/corpus_marker_scan_20260425_1622_gd585_repair.md`

## Priority queue

| priority | prompt ids | guides | owner lane | action |
| ---: | --- | --- | --- | --- |
| 1 | `RE2-UP-001`, `RE2-UP-008`, `RE2-UP-010`, `RE2-BR-005` | `GD-024`, `GD-066`, `GD-108`, `GD-646` | prompt-budget/runtime packaging | Separate prompt-budget overflows from local completion server 500s before treating these rows as retrieval or ranking failures. |
| 2 | `RE2-UP-002`, `RE2-BR-004` | `GD-029`, `GD-649` | deterministic rule side effects | Tighten deterministic triggers so broad injury/electrical wording falls through unless the wound or stroke signature is explicit. |
| 3 | `RE2-BR-001`, `RE2-BR-002`, `RE2-BR-006` | `GD-634`, `GD-635`, `GD-636` | bridge metadata/routing | Strengthen bridge guide aliases/routing cues and reciprocal language from high-ranking neighbors. |
| 4 | `RE2-UP-005`, `RE2-UP-012`, `RE2-BR-003`, `RE2-BR-007` | `GD-239`, `GD-446`, `GD-648`, `GD-637` | metadata/routing/rerank | Expected owner is retrieved but not rank 1; inspect exact-task ownership and broad recurring distractors. |
| 5 | `RE2-UP-006`, `RE2-UP-008`, `RE2-BR-008` | `GD-064`, `GD-066`, `GD-961` | guide content / bridge audit | Repair top-1 unresolved partial sections in `GD-064` and `GD-066`; audit `GD-961` so mess-hall routing does not become a broad kitchen/procurement distractor. |

## Guardrails

- Do not change rerank behavior until prompt-budget and deterministic side
  effects have been separated.
- Do not count artifact-error rows as generation misses unless a successful
  rerun produces an answer body with unsupported claims.
- Do not convert bridge guides into broad catchalls; add only concrete query
  language that matches the held-out failure.

## Validation

After any follow-up slice, rerun the held-out pack and marker overlay:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\bench.py --prompts artifacts\prompts\adhoc\rag_eval_partial_router_holdouts_20260425.jsonl --output artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun.md
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1622_gd585_repair.json --output-dir artifacts\bench\rag_eval_partial_router_holdouts_20260425_rerun_diag
```

## Post-fix proof

After `RAG-EVAL5`, the GD-064/GD-066 partial repairs, and the bridge metadata
pass, a focused rerun wrote:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_post_fixes.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_post_fixes_diag/report.md`

Result:

- deterministic rows: `0` (the two hijacks now fall through)
- top-1 unresolved-partial rows: `0`
- top-1 bridge rows: `1`
- expected-owner top-k hit rate: `11/21`, up from `10/21`
- expected-owner top-3 hit rate: `8/21`, up from `7/21`
- bucket counts: `9` retrieval misses, `5` ranking misses, `4` artifact errors,
  `2` expected supported, `1` accepted `uncertain_fit`

Interpretation:

- The deterministic fixes succeeded, but `RE2-UP-002` and `RE2-BR-004` now
  expose real RAG ranking/retrieval work instead of being hidden by
  deterministic passes.
- GD-064/GD-066 content repairs succeeded: the marker overlay no longer shows
  top-1 unresolved partials.
- Bridge metadata improved guide metadata coverage but did not move `GD-634`,
  `GD-635`, or `GD-636` into retrieved candidates without re-ingest or deeper
  retrieval/ranking work.

## 2026-04-25 Owner Hint Rerank Proof

After the prompt-packaging repair, a narrow owner-hint rerank pass added
question-intent deltas for retrieved-but-not-top owners:

- unresolved-partial owners: `GD-024`, `GD-029`, `GD-239`, `GD-108`, `GD-446`
- bridge owners: `GD-648`, `GD-646`, `GD-637`

Fresh rerun:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_bridge_owner_hints.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_bridge_owner_hints_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- ranking misses: `0`
- expected-owner hit@1: `11/21`, up from `3/21` after context compaction
- expected-supported rows: `6`, up from `3`
- remaining buckets: `9` retrieval misses, `5` generation/citation misses, `1`
  accepted `uncertain_fit`

Remaining generation/citation misses now have the expected owner at rank 1 but
do not cite it:

- `RE2-UP-001` / `GD-024`
- `RE2-UP-002` / `GD-029`
- `RE2-UP-010` / `GD-108`
- `RE2-BR-003` / `GD-648`
- `RE2-BR-005` / `GD-646`

Next best slice is retrieval-miss repair, starting with metadata ingestion of
frontmatter `aliases`, `routing_cues`, and `applicability` into chunk metadata
before adding more guide-frontmatter churn.

## 2026-04-25 Metadata Visibility Proof

The next slice preserved frontmatter `aliases`, `routing_cues`, and
`applicability` in chunk metadata, exposed those fields to contextual retrieval
text, and let metadata reranking read them. After a full reingest, the held-out
pack wrote:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_metadata_visible.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_metadata_visible_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- retrieval misses: `7`, down from `9`
- ranking misses: `1`, up from `0`
- expected-supported rows: `7`, up from `6`
- expected-owner top-3/top-k hit rate: `13/21`, up from `11/21`
- expected-owner hit@1: `12/21`
- remaining buckets: `5` generation/citation misses, `1` accepted
  `uncertain_fit`

This proves the metadata path has value, but guide/frontmatter edits are still
needed for the remaining retrieval misses. For the next guide-only pass, prefer
incremental ingest instead of another full rebuild:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files path\to\changed-guide.md
```

`--files` requires an existing collection, preflights duplicate guide IDs, skips
unchanged SHA-256 matches from `chroma_db/ingest_manifest.json`, and deletes /
re-embeds only changed guide IDs in Chroma and the lexical index.

## 2026-04-25 Contextual Index Proof

The next slice moved contextual retrieval text from shadow-only evidence into
candidate selection while preserving raw chunk text as the stored document:

- Chroma embeddings are now generated from contextual retrieval text.
- The lexical FTS search column is populated from contextual retrieval text.
- Chroma documents and lexical `document` rows stay raw chunk text for answer
  context.
- `ingest.py --files ... --force-files` can re-embed selected guides even when
  guide SHA-256 values are unchanged, which avoids full rebuilds after ingest
  plumbing changes.

The six edited guides were re-embedded with:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\water-purification.md guides\burn-treatment.md guides\food-preservation.md guides\heat-illness-dehydration.md guides\electrical-system-bootstrap.md guides\alloy-decision-tree.md --force-files
```

Fresh proof after contextual indexing and narrow owner rerank hints:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_contextual_index_rerank2.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_contextual_index_rerank2_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- retrieval misses: `1`, down from `7` after metadata visibility
- expected-supported rows: `10`, up from `7`
- expected-owner hit@1: `17/21`
- expected-owner hit@3/hit@k: `19/21`
- expected-owner cited: `11/21`
- remaining buckets: `6` generation/citation misses, `2` ranking misses, `2`
  accepted `uncertain_fit`, `1` retrieval miss

Remaining retrieval miss:

- `RE2-UP-009` expects `GD-120`, but the prompt asks to sharpen a dull field
  blade and continues to retrieve/cite `GD-397` Tool Sharpening. Treat this as
  expectation drift unless the prompt is rewritten to ask about forging,
  reshaping, material repair, or heat treatment.

Remaining ranking misses:

- `RE2-UP-003` retrieves `GD-035` but still ranks flood/storm support above it.
- `RE2-BR-004` retrieves `GD-649` but still ranks storm preparedness above it.

Remaining high-value next slice is citation-owner behavior for expected owners
already at rank 1: `GD-024`, `GD-029`, `GD-052`, `GD-108`, `GD-648`, `GD-646`.

## 2026-04-25 Citation Priority Proof

The next slice made the citation contract source-order aware for all generated
answers: when a question is present, the prompt now asks the model to prefer the
earliest retrieved guide that directly supports the lead action, main sequence,
or main comparison before using lower-ranked support guides.

Fresh proof:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_citation_priority.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_citation_priority_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- expected-supported rows: `12`, up from `10`
- generation/citation misses: `4`, down from `6`
- expected-owner cited: `13/21`, up from `11/21`
- expected-owner hit@1: `17/21`
- expected-owner hit@3/hit@k: `19/21`
- remaining buckets: `2` accepted `uncertain_fit`, `2` ranking misses, `1`
  expectation-drift retrieval miss

Rows improved by citation priority:

- `RE2-UP-002` now cites `GD-029`.
- `RE2-UP-010` now cites `GD-108`.

Remaining generation/citation misses:

- `RE2-UP-001` / `GD-024`
- `RE2-UP-004` / `GD-052`
- `RE2-BR-003` / `GD-648`
- `RE2-BR-005` / `GD-646`

The remaining citation rows likely need deeper source packaging or answer-card
support rather than another broad prompt-contract tweak.

## 2026-04-25 GD-648 Minimum Operations Proof

The next guide-only slice added targeted lifecycle-owner language to
`guides/water-system-lifecycle.md` for small water systems with inconsistent
flow and limited or unavailable maintenance tools:

- frontmatter aliases for "minimum water system operations checklist",
  "stabilize small water system", "inconsistent water flow checklist", and
  "limited tools water operations";
- routing cues and applicability that keep `GD-648` responsible for the safe
  minimum operating checklist, while handing deep component repair to
  water-system-failure-analysis;
- a source-local "Safe Minimum Operations Checklist (Limited Tools)" under
  preventive maintenance.

The guide was re-embedded with forced incremental ingest:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\water-system-lifecycle.md --force-files
```

Fresh proof:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd648_minops.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd648_minops_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- expected-supported rows: `13`, up from `12`
- generation/citation misses: `3`, down from `4`
- `RE2-BR-003` / `GD-648` now retrieves and cites `GD-648` only
- remaining generation/citation misses: `GD-024`, `GD-052`, `GD-646`

## 2026-04-25 GD-646 Source-packaging Classification

Read-only classifier verdict: `RE2-BR-005` / `GD-646` is a
source-packaging repair, not a broad prompt/citation-contract issue. There is
some expectation tension, but not enough to reclassify the row as drift.

Evidence:

- Latest diagnostics retrieve `GD-646` at rank 1 but cite
  `GD-250|GD-039|GD-055`.
- The retrieved `GD-646` evidence is the introductory ecosystem chunk, while
  the relevant owner content is later in `Phase 3: Surgical Application and
  Medical Context`, `Surgical Instrument Sterilization Protocol`, and
  `Surgical Readiness Checklist`.
- The cited guides directly support the generated reusable-instrument workflow,
  so forcing the citation contract would be weaker than improving source
  packaging.

Queue item:

`RE2-BR-005` / `GD-646`: package or boost the procedural `GD-646` sections for
prompts about reusable instruments, medical use, field treatment, limited
fuel/supplies, and cleaning between patients. Rerun
`rag_eval_partial_router_holdouts` and require `GD-646` citation only when that
procedural `GD-646` evidence is present.

## 2026-04-25 GD-024 / GD-052 Source-packaging Proof

Two focused guide patches added source-local answer anchors:

- `guides/winter-survival-systems.md`: below-freezing lights-out aliases,
  routing cues, applicability, and a "Lights-Out Below-Freezing Shelter
  Priorities" sequence for protected zone, dry insulation, wet/dry separation,
  wind blocking, heater/fire caution, and hypothermia watch.
- `guides/burn-treatment.md`: a "Hot-Water Forearm Burn: First Aid Ladder" for
  cool running water, removing tight items, loose sterile covering, avoiding
  ice/home remedies, not popping blisters, and escalation signs.

The changed guides were re-embedded with forced incremental ingest:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\burn-treatment.md guides\winter-survival-systems.md --force-files
```

Fresh merged proof:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd024_gd052_packaging.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd024_gd052_packaging_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- expected-supported rows: `14`, up from `13`
- generation/citation misses: `2`, down from `3`
- `RE2-UP-001` / `GD-024` now retrieves and cites `GD-024` only
- `RE2-UP-004` / `GD-052` now retrieves and cites `GD-052`
- merged-run remaining generation/citation misses: `GD-029` and `GD-646`

Note: `GD-029` had passed in earlier citation-priority and GD-648 proof runs,
so treat its reappearance as a run-to-run citation miss before adding new guide
churn. `GD-646` remains the next clear source-packaging target.

## 2026-04-25 GD-646 Intro Anchor Proof

The `GD-646` repair moved the reusable-instrument decision path into an early
exact-match bridge section, `Field Treatment Reusable Instruments Cleaned
Between Patients`, and added frontmatter aliases/routing/applicability for
medical instrument reuse with limited fuel or supplies. The first attempt had
improved the source set but still cited only `GD-250`; the early intro anchor
made the procedural `GD-646` evidence reach the prompt.

The guide was re-embedded with forced incremental ingest:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B .\ingest.py --files guides\sterilization-ecosystem.md --force-files
```

Fresh proof:

- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd646_intro_anchor.json`
- `artifacts/bench/rag_eval_partial_router_holdouts_20260425_gd646_intro_anchor_diag/report.md`

Result:

- successful prompts: `21/21`
- artifact errors: `0`
- expected-supported rows: `15`, up from `14`
- generation/citation misses: `1`, down from `2`
- `RE2-BR-005` / `GD-646` now retrieves `GD-646|GD-250` and cites `GD-646`
- remaining generation/citation miss in this run: `GD-029`
- remaining ranking misses: `GD-035`, `GD-649`
- remaining expectation-drift retrieval miss: `GD-120` vs sharpen-a-blade prompt
