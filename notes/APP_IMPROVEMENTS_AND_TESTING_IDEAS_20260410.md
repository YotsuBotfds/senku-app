# App Improvements and Testing Ideas — 2026-04-10

This note synthesizes improvement ideas from the current repo design plus the fresh 2026-04-10 local model-comparison artifacts.

Primary references used:

- `CLAUDE.md`
- `AGENTS.md`
- `config.py`
- `query.py`
- `bench.py`
- `scripts/report_bench_watchlist.py`
- `notes/MODEL_COMPARISON_QWEN35_9B_LOCAL_RESULTS_20260410.md`
- `notes/MODEL_COMPARISON_GEMMA4_E4B_LOCAL_RESULTS_20260410.md`

## Highest-Leverage App Improvements

### 1. Add model profiles instead of manual per-run tuning

Why it matters:

- current local testing showed model-specific behavior differences are large
- Qwen needed a much higher completion budget to avoid empty completions
- Gemma E4B and E2B ran cleanly at much smaller caps
- concurrency behavior also varies by model and quant

What to add:

- a small model-profile registry with defaults for:
  - `max_completion_tokens`
  - `top_k`
  - preferred prompt `mode`
  - recommended same-host worker count
  - optional notes like `thinking_on`, `phone_target`, `quality_profile`
- let `bench.py` and `query.py` resolve defaults from the selected model unless explicitly overridden

Main files:

- `config.py`
- `bench.py`
- `query.py`

Risks / tradeoffs:

- a profile layer adds configuration complexity
- bad defaults can hide problems if overrides are not visible in reports

## 2. Report both throughput and latency explicitly in bench artifacts

Why it matters:

- current bench reports are good on per-prompt generation time, but not on wall-clock throughput
- recent local tests showed concurrency improved overall throughput while reducing per-prompt token speed
- this is a major decision variable for local deployment but is not first-class in the current JSON/MD output

What to add:

- wall-clock run duration
- aggregate completion tokens per wall-clock second
- aggregate completion tokens per summed generation second
- mean per-prompt tokens per second
- queue wait or prep duration vs generation duration split
- optional throughput comparison helpers for `w1` vs `wN`

Main files:

- `bench.py`
- `scripts/report_bench_watchlist.py`
- possibly a new `scripts/compare_bench_artifacts.py`

Risks / tradeoffs:

- more metrics can clutter the markdown report
- derived rates need clear naming so they are not confused with model-side raw generation speed

## 3. Make retrieval/build prep optionally parallelizable

Why it matters:

- the repo explicitly serializes retrieval/build prep before generation fan-out
- on fast local models, serialized prep becomes a more visible fraction of total wall time
- recent testing suggests generation concurrency is useful, but embed/retrieval work still bottlenecks at the front

What to add:

- optional prep worker pool for:
  - embedding requests
  - retrieval/build prompt prep
- start with a conservative flag rather than changing the default profile
- keep a strict single-worker fallback for quality baselines

Main files:

- `bench.py`
- `query.py`
- `lmstudio_utils.py`

Risks / tradeoffs:

- same-host embed + generation concurrency can overload LM Studio
- the repo already records that concurrency assumptions are hardware/profile sensitive
- this needs careful opt-in rather than becoming the default everywhere

## 4. Add a stronger post-generation citation cleanup / compression pass

Why it matters:

- duplicate citations remain a recurring issue across Qwen and some Gemma lanes
- the repo already tracks `duplicate_citation_total`, so the pain is known and measurable
- high duplicate counts reduce readability without improving grounding

What to add:

- tighten response normalization so repeated adjacent or near-adjacent citations are collapsed more aggressively
- consider a final citation dedupe pass that preserves support while compressing noisy repeats
- optionally add a “citation budget” or “avoid repeating identical citation clusters” instruction for smaller local models

Main files:

- `query.py`
- `special_case_builders.py` only if deterministic paths need the same cleanup
- `config.py`

Risks / tradeoffs:

- aggressive dedupe can accidentally strip useful support if implemented too bluntly
- should be validated against the live guide-citation checks, not just surface formatting

## 5. Add mode-specific length control that is stronger for smaller local models

Why it matters:

- smaller local models do not fail the same way
- some models are concise to the point of incompleteness
- others become overly long or hit caps under broad prompts
- `demo` mode does not always become more compact in practice

What to add:

- explicit mode/model length policy in prompt assembly
- optional tighter output-shape instructions for `demo` on smaller local models:
  - summary line
  - 3-5 steps max unless scenario complexity demands more
  - “one starter path, not a catalog”
- optional retrieval-aware cap reductions for low-scope prompts

Main files:

- `config.py`
- `query.py`
- `bench.py`

Risks / tradeoffs:

- too much length pressure can hide safety warnings or omit critical constraints
- should be introduced as profile-specific behavior, not globally

## Best Additional Testing To Add

### 1. Build a model-matrix runner and exportable review bundle

Why it matters:

- this is the most useful next testing upgrade for local model comparisons
- the current workflow already produces multiple model artifacts, but aggregation is manual
- a single export bundle would make external review far easier

What to add:

- a script that runs the same prompt pack across multiple models
- outputs a single machine-readable bundle containing:
  - prompt
  - model id
  - mode
  - worker count
  - response text
  - cited guides
  - retrieval metadata summary
  - latency / token metrics
- optionally include relevant guide excerpts or retrieved chunk metadata for each prompt so an external evaluator can judge grounding

Main files:

- `bench.py`
- new script, likely under `scripts/`

Likely payoff:

- supports exactly the “send outputs + guides to an external evaluator” workflow
- reduces repeated manual artifact stitching

## 2. Add a side-by-side artifact comparison script

Why it matters:

- current comparisons are manual even when artifacts are aligned by prompt pack
- local testing now spans:
  - model variants
  - quant levels
  - worker counts
  - prompt modes
- a formal comparator would speed decisions dramatically

What to add:

- compare two or more JSON artifacts and produce:
  - latency deltas
  - completion token deltas
  - duplicate citation deltas
  - cap-hit prompts
  - error deltas
  - prompts whose source mode or decision path changed

Main files:

- new script under `scripts/`
- maybe extend `scripts/report_bench_watchlist.py`

Likely payoff:

- much faster model/quant/concurrency comparisons
- easier to choose whether a smaller model is “good enough”

## 3. Add dedicated concurrency lanes as first-class tests

Why it matters:

- concurrency is now clearly a separate tuning dimension
- recent local tests showed:
  - higher worker count improves wall-clock throughput
  - per-prompt speed declines
  - runtime stability depends on model + host state

What to add:

- a small standard concurrency pack
- fixed runs at `w1`, `w3`, `w4` for selected local models
- record:
  - wall-clock speedup
  - prompt-level speed loss
  - error rate
  - citation drift under load

Main files:

- `bench.py`
- new prompt pack or script wrapper

Likely payoff:

- cleaner local deployment decisions
- easier to choose “interactive” vs “batch” profiles

## 4. Add targeted replay packs from watchlist prompts automatically

Why it matters:

- the repo already has watchlist tooling and notes
- the missing piece is faster replay generation from fresh artifacts

What to add:

- script that reads bench JSON and emits:
  - cap-hit prompt pack
  - duplicate-citation prompt pack
  - error-only prompt pack
  - selected section-specific replay pack

Main files:

- `scripts/report_bench_watchlist.py`
- new helper under `scripts/`

Likely payoff:

- shortens the loop between broad benches and focused fixes
- reduces manual prompt copying

## 5. Add a local runtime preflight that checks model, cap, and concurrency profile

Why it matters:

- recent runs were sensitive to:
  - model id differences
  - cap settings
  - host reloads
  - stale or interrupted bench clients
- there is already `verify_local_runtime.sh`, but it does not validate the chosen bench profile deeply

What to add:

- preflight script that checks:
  - requested model is loaded
  - embedding model is loaded
  - chosen worker count is allowed for the profile
  - completion cap is above a per-model minimum if needed
  - recent stale bench clients are absent

Main files:

- `scripts/verify_local_runtime.sh`
- maybe a new Python helper for Windows-friendly use

Likely payoff:

- fewer wasted long runs
- fewer “mystery” runtime failures

## Concrete Near-Term Testing Sequence

If prioritizing value over breadth, the next high-yield sequence is:

1. Build a unified multi-model export bundle for the existing local artifacts.
2. Add a side-by-side comparator for JSON artifacts.
3. Promote wall-clock throughput metrics into `bench.py` outputs.
4. Run targeted manual review on:
   - Gemma E4B BF16
   - Gemma E4B Q4_K_S
   - Gemma E2B
   - Qwen 3.5 9B
5. Only then decide whether to:
   - harden app behavior for a chosen local default
   - or continue expanding the model matrix

## Most Practical External-Evaluator Bundle Design

If the goal is external review for accuracy vs time tradeoff, the bundle should include:

- prompt text
- model id
- quant / runtime profile
- worker count
- mode (`default`, `demo`, etc.)
- raw response text
- cited guide ids
- retrieved source titles / sections
- retrieval metadata summary
- generation time
- prompt tokens
- completion tokens
- duplicate citation count
- whether the result was deterministic, RAG, or no-RAG

Optional but useful:

- the exact retrieved chunks or a compact excerpt subset
- a pointer to the guide ids used so the reviewer can inspect grounding

This is the single highest-leverage testing addition for comparing multiple local models without drowning in markdown artifacts.