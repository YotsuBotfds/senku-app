# RAG-S16 Section-Family RAPTOR-lite Shadow Export

- **Status:** landed as shadow-only analyzer/export tooling.

## Slice

Shadow-only section-family RAPTOR-lite export for later retrieval-comparison
experiments.

## Role

Main agent / docs and pipeline-measurement lane. Keep this analysis-only until a
follow-up slice explicitly promotes outputs to runtime.

## Preconditions

- `RAG-S15` (contextual chunk shadow export) and `RAG-S15b`
  (retrieval comparator) have produced non-production data for the same bench
  corpus.
- This lane must not be used to ship or expose product-facing retrieval behavior.
- Existing guide corpus, routing rules, and Android app behavior remain the
  production baseline.

## Outcome

Landed `scripts/export_section_family_shadow.py`, a **shadow-only** export lane
that produces records for a section-family RAPTOR-lite retrieval experiment
without changing any live retrieval path.

Expected record shape (JSONL):

- `chunk_id`
- `section_family` (or equivalent family key used for chunk grouping)
- `document`
- `contextual_retrieval_text`
- `raptor_lite_text`
- `metadata`

Each section-family record groups adjacent guide sections with
`--family-window` (default `2`) and includes extractive evidence fragments from
the source chunks. The record shape stays compatible with
`scripts/compare_contextual_shadow_retrieval.py`; the comparator also accepts
`raptor_lite_text` when `contextual_retrieval_text` is absent.

## Boundaries

- No production `query.py` routing/query-time behavior changes.
- No production `Chroma`/embedding/lexical/index write path changes.
- No guide content edits or guide-selection policy changes.
- No Android behavior or pack schema changes.
- Only non-production artifacts are produced; usage remains opt-in and offline.

## Acceptance

- Dedicated shadow export script emits only the records above.
- Export does not trigger manifest writes, embedding calls, Chroma setup,
  lexical upserts, or Android-facing outputs.
- Shadow output artifacts are deterministic for replay and can be consumed by
  the S15b comparator to score hit@1/hit@3/hit@k deltas.
- Comparator loader remains backward-compatible with S15 contextual JSONL and
  forward-compatible with S16 `raptor_lite_text` records.

## Validation

Focused validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\export_section_family_shadow.py scripts\compare_contextual_shadow_retrieval.py tests\test_export_section_family_shadow.py tests\test_compare_contextual_shadow_retrieval.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_export_section_family_shadow tests.test_compare_contextual_shadow_retrieval -v
```

Live smoke also passed on the EX airway-family subset:

- Exported `35` section-family records from first-aid / airway / pediatric
  airway guides.
- Comparator wrote `6` EX comparison rows under
  `artifacts/bench/section_family_compare_ex_airway_20260425/`.
- Shadow expected-guide hit@1/hit@3/hit@k was `6/6` on the EX airway proof
  rows; the older baseline had ranked retrieval candidates for only `2`
  comparable generated rows and was already `2/2` there.

Repro smoke:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\export_section_family_shadow.py --out artifacts\tmp\section_family_ex_airway.jsonl --files first-aid.md emergency-airway-management.md pediatric-emergency-medicine.md pediatric-emergencies-field.md infant-child-care.md --family-window 2 --max-fragments 6
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\compare_contextual_shadow_retrieval.py --shadow-jsonl artifacts\tmp\section_family_ex_airway.jsonl --bench artifacts\bench\guide_wave_ex_20260424_1410_child_choking_gate.json --out-dir artifacts\bench\section_family_compare_ex_airway_20260425 --top-k 8 --embed-url http://127.0.0.1:1234/v1
```

## Next

Use output from this slice as the section-family input candidate for a future
`RAG-S16b` (or equivalent) comparator that checks whether RAPTOR-lite family
summaries improve retrieval quality before any production promotion.
