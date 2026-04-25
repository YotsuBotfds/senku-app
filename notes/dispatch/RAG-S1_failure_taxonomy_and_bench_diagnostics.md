# Slice RAG-S1 - failure taxonomy and bench diagnostics

- **Role:** main agent (`gpt-5.5 medium`; use high only if changing answer-routing behavior, which this slice should not do).
- **Paste to:** new main-agent window after reading `notes/RAG_NEXT_LEVEL_STRATEGY_2026-04-24.md`.
- **Parallel with:** read-only scout is fine. Avoid parallel writers touching `bench.py` or new diagnostic scripts.
- **Why this slice now:** prompt-wave hardening is finding real misses but does not say whether failures are retrieval, ranking, generation, unsupported-claim, or safety-contract problems. This slice creates the diagnostic steering wheel before more deterministic rule growth.

## Outcome

Add a lightweight offline diagnostic tool that reads existing bench JSON artifacts and emits a failure-taxonomy report without changing runtime answer behavior.

## Read Set

- `notes/RAG_NEXT_LEVEL_STRATEGY_2026-04-24.md`
- `bench.py`
- `query.py`
- `scripts/run_guide_prompt_validation.ps1`
- `notes/specs/rag_prompt_expectations_seed_20260424.yaml`
- fresh artifacts:
  - `artifacts/bench/guide_wave_ex_20260424_092327.json`
  - `artifacts/bench/guide_wave_ey_20260424_092407.json`
  - `artifacts/bench/guide_wave_ez_20260424_092437.json`
  - `artifacts/bench/guide_wave_fa_20260424_092935.json`
  - `artifacts/bench/guide_wave_fb_20260424_092957.json`
  - `artifacts/bench/guide_wave_fc_20260424_093011.json`
  - `artifacts/bench/guide_wave_fd_20260424_093037.json`
  - `artifacts/bench/guide_wave_fe_20260424_093104.json`

## Likely Touch Set

Expected:

- new script, for example `scripts/analyze_rag_bench_failures.py`
- optional fixture/expectation support for `notes/specs/rag_prompt_expectations_seed_20260424.yaml`
- focused tests for the analyzer if the repo test style makes that cheap

Avoid changing:

- `query.py`
- `bench.py`
- deterministic registry files
- guide content

## Diagnostic Buckets

Minimum buckets:

- `deterministic_pass`
- `rag_unknown_no_expectation`
- `retrieval_miss`
- `ranking_miss`
- `generation_miss`
- `unsupported_or_truncated_answer`
- `safety_contract_miss`
- `abstain_or_clarify_needed`
- `artifact_error`

Start with heuristic labels; do not pretend they are perfect.

## Report Fields

For each prompt:

- artifact path
- prompt index
- prompt text
- decision path
- decision detail
- generated vs immediate
- source mode
- cited guide IDs
- top retrieved guide IDs and ranks
- expected guide IDs/family if supplied
- expected hit@1 / hit@3 / hit@k
- cap hit / retry/error flags
- suspected failure bucket
- short reason

Summary:

- counts by artifact and bucket
- generation workload
- deterministic vs RAG counts
- expected-guide hit rates where available
- top recurring distractor guide IDs

## Acceptance

- The tool runs on the fresh EX-FE JSON artifacts.
- It writes Markdown plus CSV or JSON output under `artifacts/bench/rag_diagnostics_YYYYMMDD_HHMM/`.
- It does not require LM Studio, LiteRT, network, Android, or re-ingest.
- It uses `notes/specs/rag_prompt_expectations_seed_20260424.yaml` if expectation parsing is cheap; otherwise it leaves expectation fields as unknown and documents that as the next enhancement.
- It gives enough information to decide whether the next fix is retrieval, reranking, answer contract, or deterministic safety gate.

## Validation

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py artifacts\bench\guide_wave_ex_20260424_092327.json artifacts\bench\guide_wave_ey_20260424_092407.json artifacts\bench\guide_wave_ez_20260424_092437.json artifacts\bench\guide_wave_fa_20260424_092935.json artifacts\bench\guide_wave_fb_20260424_092957.json artifacts\bench\guide_wave_fc_20260424_093011.json artifacts\bench\guide_wave_fd_20260424_093037.json artifacts\bench\guide_wave_fe_20260424_093104.json
.\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\analyze_rag_bench_failures.py
```

Add focused unit tests only if the script grows beyond straightforward artifact parsing.

## Anti-Recommendations

- Do not add another deterministic rule in this slice.
- Do not change answer behavior in this slice.
- Do not use evaluator LLMs yet; start with artifact-native guide IDs, citations, decision paths, and prompt metadata.

## Report Format

- Files changed.
- Diagnostic artifact path.
- Bucket counts for EX-FE.
- One recommendation for the next slice based on the bucket counts.
