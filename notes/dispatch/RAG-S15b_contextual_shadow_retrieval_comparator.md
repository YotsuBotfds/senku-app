# RAG-S15b Contextual Shadow Retrieval Comparator

## Slice

Measurement-only comparator for the RAG-S15 contextual shadow export.

## Role

Main agent / analyzer tooling lane. This slice decides whether contextual
retrieval is actually getting us closer before any production retrieval change.

## Outcome

Adds `scripts/compare_contextual_shadow_retrieval.py`, a standalone comparator
that loads an S15 JSONL shadow export, builds temporary shadow retrieval indexes,
runs existing bench prompts against those temporary indexes, and writes paired
baseline-versus-shadow retrieval metrics.

Expected outputs:

- `compare_contextual_shadow_summary.json`
- `compare_contextual_shadow_retrieval_rows.jsonl`
- `compare_contextual_shadow_retrieval_rows.csv`

## Preconditions

- Generate or provide a contextual shadow export from `ingest.py`.
- Use existing bench JSON artifacts as the baseline proof corpus.
- Use the expectation manifest at
  `notes/specs/rag_prompt_expectations_seed_20260424.yaml` when scoring
  expected-guide hit rates.

## Boundaries

- No production Chroma, lexical, query, prompt, guide, or Android behavior
  changes.
- The script creates temporary shadow indexes only.
- The script is not a runtime feature and does not promote contextual retrieval
  into the app or desktop query path.

## Acceptance

- Loads S15 JSONL records with `chunk_id`, raw `document`,
  `contextual_retrieval_text`, and `metadata`.
- Resolves wave-level expected guide IDs from bench artifact filenames and the
  expectation manifest, including slug-to-guide-id lookup.
- Emits row-level baseline and shadow top-guide IDs, expected ranks, hit@1,
  hit@3, hit@k, rank delta, and top-k overlap.
- Emits aggregate baseline/shadow hit counts and comparable-row deltas.
- Treats rows with expected guide IDs but no candidates on one side as
  unscored (`null`) for that side, so deterministic/no-retrieval bench rows do
  not become fake retrieval misses or fake shadow improvements.
- Computes delta counts and mean top-k overlap only from rows where both
  baseline and shadow have scorable retrieval candidates.
- Reports `scored_rows` per side. Baseline and shadow denominators can differ;
  use `deltas.*.comparable_rows` for direct A/B claims.
- Reports `top_k_overlap_jaccard` as a simple set overlap. It is useful for
  drift detection, but rank-blind; it does not say whether shared guides moved
  up or down.
- Keeps live retrieval comparison optional behind the CLI because it requires an
  embedding endpoint and may be expensive on the full corpus.

## Validation

Focused helper validation:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\compare_contextual_shadow_retrieval.py tests\test_compare_contextual_shadow_retrieval.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_compare_contextual_shadow_retrieval -v
```

Measurement hygiene validation added after the first EX smoke: baseline rows
without retrieval candidates are excluded from comparable-row deltas and overlap
averages, while shadow-only scored rows remain visible in the per-side summary.

Optional live smoke:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe ingest.py --contextual-shadow-jsonl artifacts\tmp\contextual_shadow.jsonl --contextual-shadow-only
& .\.venvs\senku-validate\Scripts\python.exe scripts\compare_contextual_shadow_retrieval.py --shadow-jsonl artifacts\tmp\contextual_shadow.jsonl --bench artifacts\bench\guide_wave_ex_20260424_1410_child_choking_gate.json --out-dir artifacts\bench\contextual_shadow_compare_ex_smoke --top-k 8 --max-shadow-records 2000 --max-prompts 2
```

Current EX airway smoke interpretation after that hygiene fix:

- Baseline and shadow are directly comparable on `2` EX rows; both are already
  hit@1/hit@3/hit@k `2/2` there.
- The contextual shadow index scores `6/6` rows, which is useful coverage
  signal, but not proof of a comparable-row improvement over the baseline.
- Use panels with baseline `top_retrieved_guide_ids` on most rows before
  making promotion decisions.

## Next

Use the comparator output to decide whether RAG-S16 should invest in
section-family / RAPTOR-lite summaries, contextual BM25 promotion, or a narrower
answer-card/evidence-unit lane.
