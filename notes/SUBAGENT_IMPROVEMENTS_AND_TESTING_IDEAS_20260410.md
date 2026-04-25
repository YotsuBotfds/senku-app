# Subagent Improvements And Testing Ideas — 2026-04-10

## Purpose

This note consolidates four parallel xhigh investigations into:

- app improvements that would most improve Senku quality or runtime behavior
- testing and evaluation improvements that would make model selection faster and more trustworthy
- concrete next experiment lanes to run

This is a synthesis of subagent findings plus the fresh local model-comparison work completed on 2026-04-10.

## Current Read

The biggest missing signal is no longer raw bench volume.

The repo now needs:

- better quality adjudication between candidate models
- better bench/report tooling for comparison and external review
- better runtime classification and model-aware token budgeting
- less prompt/context waste on non-review lanes

Recent local testing also reinforces a few practical truths:

- Qwen can work, but it is highly completion-budget-sensitive in think mode
- smaller Gemma variants are currently the more practical local and phone-oriented lane
- generation concurrency improves batch throughput, but retrieval/build is still serialized, so total wall-clock gains are bounded
- duplicate citations are now one of the clearest remaining quality problems on successful runs

## Highest-Leverage App Improvements

### 1. Classify LM Studio `400` failures instead of retrying them all the same way

Why it matters:

- true bad requests, context-pressure failures, and transient local-runtime hiccups currently blur together
- this wastes time during concurrency tests and makes root-cause analysis harder

Main files:

- `lmstudio_utils.py`
- `query.py`
- `bench.py`

Practical direction:

- separate retryable `400` cases from real bad-request/config failures
- surface retry cause in bench artifacts instead of hiding it behind generic retries

### 2. Add explicit model/runtime profiles with adaptive completion retry logic

Why it matters:

- static token caps misclassify some model/runtime combinations as “bad” when they are really budget-starved
- the current empty-completion retry path retries with the same cap, which is weak for think-heavy models

Main files:

- `config.py`
- `bench.py`
- `query.py`
- `CURRENT_LOCAL_TESTING_STATE_20260410.md`

Practical direction:

- define small-model, think-heavy, and broad-quality runtime profiles
- allow controlled cap escalation on empty completion or cap-hit prompts instead of blind same-budget retry

### 3. Make prompt compaction truly mode-aware

Why it matters:

- `demo` is not reliably getting shorter in current practice
- prompt scaffolding appears to remain heavy even when the output mode should be compact
- smaller local models pay a real token tax for unnecessary structure

Main files:

- `config.py`
- `query.py`

Practical direction:

- keep the richest scaffolding in `review`
- shrink scenario/coverage/chunk annotation overhead in `default`
- shrink it further in `demo`

### 4. Add a second retrieval pass only for weak or missing objectives

Why it matters:

- the system already computes `objective_coverage`
- weak or missing objectives currently become metadata shown to the model rather than a retrieval trigger
- the watchlists repeatedly show hard prompts with weak/missing coverage, long answers, and drift

Main files:

- `query.py`
- `bench.py`

Practical direction:

- after first-pass rerank, detect weak/missing objectives
- run a narrow supplemental retrieval only for those missing objectives
- merge that evidence back into the final prompt

### 5. Replace fixed `TOP_K=24` with an adaptive context budget

Why it matters:

- single-objective prompts often get too much context
- compound prompts sometimes justify the full budget, but many others do not
- fixed context budgets contribute to citation duplication, answer sprawl, and unnecessary latency

Main files:

- `config.py`
- `query.py`
- `bench.py`

Practical direction:

- budget context based on objective count, domain spread, and prompt mode
- keep stricter diversity caps for non-review modes

### 6. Add citation-aware pruning or a thresholded rewrite pass

Why it matters:

- formatting cleanup exists already, but it does not prevent repeated guide IDs across the answer
- duplicate citations are still one of the strongest remaining broad-lane quality signals

Main files:

- `query.py`
- `bench.py`

Practical direction:

- only trigger this when duplicate citations exceed a threshold
- keep it tightly constrained so it removes repetition without changing substance

## Highest-Leverage Testing And Evaluation Improvements

### 1. Export bench artifacts into an evaluator-ready review bundle

Why it matters:

- this directly supports the external-review workflow the operator wants
- `bench.py` already emits most of the needed fields

Proposed tool:

- `scripts/export_eval_bundle.py`

Desired output:

- JSONL or CSV with prompt, model, mode, response text, citations, timing, source mode, decision path, and artifact provenance

Payoff:

- one command to package aligned outputs for manual review or external scoring

### 2. Add a real artifact comparison script

Why it matters:

- current watchlist reporting is useful, but it is not enough for model-vs-model or run-vs-run deltas
- too much comparison still depends on manual reading

Proposed tool:

- `scripts/compare_bench_artifacts.py`

Desired output:

- latency deltas
- error deltas
- cap-hit deltas
- duplicate-citation deltas
- changed `decision_path` / `source_mode`
- replay prompt packs for errors, cap hits, and duplicate-citation outliers

### 3. Promote wall-clock, prep, throughput, and cap-hit metrics to first-class outputs

Why it matters:

- current summaries emphasize generation time but not enough wall-clock and prep behavior
- concurrency results are easy to misread without wall-clock throughput and prep-share metrics

Main file:

- `bench.py`

Desired additions:

- wall duration
- wall tok/s
- summed-generation tok/s
- prep time vs generation time
- prompt counts near cap
- retry counts by cause

### 4. Standardize a small repeatable bench matrix

Why it matters:

- too many decisions are being made from one-off lanes
- the repo now needs a stable matrix for apples-to-apples comparisons

Recommended matrix:

- `Gate`
- `Coverage`
- `Sentinel`
- compare pack `default`
- compare pack `demo`
- compare pack `--no-rag`
- optional micro-pack `--no-rag --no-system-prompt`
- concurrency pack at `w1/w2/w3/w4`

### 5. Expand automated tests around bench and reporting behavior

Why it matters:

- current unit coverage is narrow relative to the bench/report toolchain
- bench/report/runtime regressions are currently too easy to miss until expensive runs fail

Recommended additions:

- prompt loading and section filtering
- JSON schema stability
- `report_bench_watchlist.py`
- empty-completion retry behavior
- worker failover and retry-slot behavior
- Windows/runtime portability cases

## Operational And Workflow Improvements

### 1. Replace zsh-only bench helpers with a cross-platform runner

Why it matters:

- the core Python path is getting more Windows-safe
- the operator scripts are still heavily Unix-specific

Main files:

- `scripts/verify_local_runtime.sh`
- `scripts/run_bench_guarded.sh`
- `scripts/kill_bench_orphans.sh`

Practical direction:

- add a Python or PowerShell-compatible runner path
- make “guarded bench” and cleanup workflows work the same way on Windows

### 2. Pipeline retrieval/build prep instead of serializing the whole front half of the run

Why it matters:

- generation concurrency is helping, but prep remains serialized
- this limits throughput and blocks any meaningful embedding-concurrency experiments

Main file:

- `bench.py`

Practical direction:

- allow retrieval/build to feed ready prompts into generation workers incrementally
- surface prep queue depth and generation queue depth in progress logs

### 3. Make long runs resumable and preserve richer run-state

Why it matters:

- interrupted runs still cost too much manual cleanup
- progress logs and ad hoc notes carry too much of the run-state

Main files:

- `bench.py`
- `scripts/report_bench_watchlist.py`

Practical direction:

- persist richer retry and run-state metadata in JSON
- support partial rerun or resume by prompt index / question

## Best Next Experiments

### 1. `26B` anchor lane

What it proves:

- how much quality the small phone-target models are giving up relative to the incumbent-quality anchor

Recommended profile:

- `google/gemma-4-26b-a4b`
- compare pack in `default` and `demo`
- then a small 8-12 prompt coverage slice
- `w1`
- local embeddings
- start at `768`, rerun cap-hits at `1024`

### 2. Broad-suite small-model validation

What it proves:

- whether `gemma-4-e4b-it@q4_k_s` and `gemma-4-e2b-it` still hold up outside the compare pack

Recommended profile:

- both models on `Gate`, `Coverage`, and `Sentinel`
- `default`
- `w1`
- local embeddings
- `768` first, `1024` targeted reruns only when needed

### 3. Blind/manual quality scoring pack

What it proves:

- decision discipline
- constraint coverage
- citation cleanliness
- compactness
- safety posture

Recommended profile:

- 10-12 prompts drawn from compare pack plus current watchlist prompts
- outputs from `e2b`, `e4b q4_k_s`, `e4b bf16`, and optionally the `26b` anchor
- same mode, same cap, same prompt wording for every model

This is the best fit for the operator’s idea of handing outputs plus guides to an external reviewer.

### 4. No-RAG dependency check for the phone candidates

What it proves:

- whether `e2b` and `e4b` are robust enough without the full Senku scaffold

Recommended profile:

- compare pack
- `default`
- `w1`
- `--no-rag`
- optional 6-prompt micro-pass with `--no-rag --no-system-prompt`

### 5. Full concurrency curves

What it proves:

- the real operating sweet spot on this 4090

Recommended profile:

- `e4b q4_k_s`: `w1/w2/w3/w4`
- `e2b`: `w1/w2/w3/w4`
- same compare pack in `demo`
- duplicate `localhost` in `--urls`

Record:

- wall time
- total generation time
- wall tok/s
- prompt-level tok/s
- error rate
- prep share

## Model-Lane Read Right Now

If the goal is phone or light-device deployment, the current ladder should be:

- `gemma-4-e2b-it` as the efficiency floor
- `gemma-4-e4b-it@q4_k_s` as the practical phone candidate
- `gemma-4-e4b-it@bf16` as a desktop-only quality check
- `google/gemma-4-26b-a4b` as the quality anchor

Qwen does not currently look like the best use of local-testing time for the phone-oriented lane because it:

- needs much higher completion budgets
- shows heavier scaffold dependence
- still carries more citation duplication and answer sprawl

## Suggested Sequence

### Immediate

- build `export_eval_bundle.py`
- build `compare_bench_artifacts.py`
- add LM Studio `400` classification
- add adaptive completion escalation

### After That

- run broad `Gate/Coverage/Sentinel` for `e2b` and `e4b q4_k_s`
- create an external-review bundle for the top 2-3 models
- run the `26b` anchor lane

### Then

- decide whether `e2b` is good enough for the phone default
- if not, keep `e4b q4_k_s` as the practical device target
- only revisit Qwen if the goal shifts back toward higher-context desktop behavior