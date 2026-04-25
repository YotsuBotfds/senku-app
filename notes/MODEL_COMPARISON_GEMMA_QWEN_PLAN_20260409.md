# Model Comparison Plan: Gemma 4 26B A4B vs Qwen 3.5 9B Q8 (2026-04-09)

## Goal

Choose the better default Senku generation model between:

- `google/gemma-4-26b-a4b`
- the local LM Studio Qwen 3.5 9B Q8 quant the operator wants to evaluate

This comparison is about Senku quality, not raw model vibe. The winner must be at least as safe, grounded, and scope-disciplined as the current Gemma baseline in the repo's actual validation workflow.

## Current Repo Reality

- Gemma is the current incumbent in [`config.py`](../config.py).
- The active validation profile is still the guarded single-worker generation path with local embeddings.
- Current Gate is fully closed and effectively deterministic, so Gate is now a sanity check, not the main winner signal.
- `query.py` already supports `--model`.
- `bench.py` does not currently expose a `--model` override, so the bench series must either:
  - temporarily switch `GEN_MODEL` in [`config.py`](../config.py) before each run series, or
  - add a minimal `--model` override to `bench.py` first and then use that for all runs

For this plan, assume current repo state and use `config.GEN_MODEL` swaps for bench runs unless the agent adds the override first.

## Decision Standard

Qwen should only replace Gemma if all of the following are true:

1. It introduces no Gate regression.
2. It is at least as good on broad Coverage in `default` mode.
3. It is cleaner on the custom comparison pack, especially on decision discipline, constraint handling, starter-plan restraint, and citation behavior.
4. Any remaining Qwen weaknesses look easier to correct than Gemma's remaining weaknesses.

Latency is a tie-breaker only after quality.

## Required Outputs

Create or collect these artifacts:

- `artifacts/bench/20260409/bench_<modeltag>_gate_default_w1_20260409.*`
- `artifacts/bench/20260409/bench_<modeltag>_compare_pack_default_w1_20260409.*`
- `artifacts/bench/20260409/bench_<modeltag>_compare_pack_demo_w1_20260409.*`
- `artifacts/bench/20260409/bench_<modeltag>_coverage_default_w1_20260409.*`
- `artifacts/bench/20260409/bench_<modeltag>_compare_pack_norag_default_w1_20260409.*`
- `notes/MODEL_COMPARISON_GEMMA_QWEN_RESULTS_20260409.md`

Use `gemma4` and `qwen35_9b_q8` as `modeltag` unless the operator wants different short names.

## Comparison Pack

Use the prompt pack at [`artifacts/prompts/adhoc/model_compare_gemma_qwen_pack_20260409.txt`](../artifacts/prompts/adhoc/model_compare_gemma_qwen_pack_20260409.txt).

The pack is designed to:

- stay mostly in normal RAG territory instead of collapsing into deterministic starter/control paths
- stress go/no-go judgment, not just recipe recall
- expose model habits around invented helpers, invented gear, and unsupported certainty
- test whether the model stays compact and practical in `demo` mode
- test whether the model remains complete and grounded in `default` mode

Section names in the pack:

- `Transport Decision`
- `Compound Scenarios`
- `Water and Sanitation`
- `Technical Starter Plans`
- `Group Coordination`
- `Risk Triage and Communications`

## Execution Plan

### 1. Preflight

Run:

```bash
source venv/bin/activate
mkdir -p artifacts/bench/20260409
```

Confirm in LM Studio that:

- the embedding model is available locally
- Gemma is loaded and reachable
- the Qwen quant is loaded and reachable

If a prior bench was interrupted:

```bash
zsh scripts/kill_bench_orphans.sh
```

### 2. Smoke Test Both Models Interactively

Use `query.py --model` first. This is the fastest way to catch obvious failures before a longer bench.

Run for Gemma:

```bash
python3 query.py --model "google/gemma-4-26b-a4b" --mode demo
```

Run for Qwen:

```bash
python3 query.py --model "<qwen_lm_studio_model_id>" --mode demo
```

Use at least these smoke prompts from the pack:

- prompt 1
- prompt 5
- prompt 13

Stop early if either model shows any of these:

- fabricated people, tools, or helper roles
- fabricated or malformed citations
- immediate commitment to a risky course of action when the prompt explicitly asks for a decision process
- medical overreach unsupported by the guides
- severe scope drift

### 3. Gate Sanity Check

Before each bench series, set `GEN_MODEL` in [`config.py`](../config.py) to the target model id.

Then run:

```bash
zsh scripts/run_bench_guarded.sh --section "Core Regression,Quality Floor Tests" --mode default --urls http://192.168.0.67:1234/v1 --embed-url http://localhost:1234/v1 --output artifacts/bench/20260409/bench_<modeltag>_gate_default_w1_20260409.md
```

Interpretation:

- Any Gate regression is a serious strike.
- If both models pass, do not over-read the result. Current Gate is mostly validating deterministic routing and repo health, not deep model quality.

### 4. Custom Comparison Pack in `default` Mode

This is the first meaningful head-to-head lane.

Run:

```bash
zsh scripts/run_bench_guarded.sh --prompts artifacts/prompts/adhoc/model_compare_gemma_qwen_pack_20260409.txt --mode default --urls http://192.168.0.67:1234/v1 --embed-url http://localhost:1234/v1 --output artifacts/bench/20260409/bench_<modeltag>_compare_pack_default_w1_20260409.md
```

This lane should answer:

- Which model is better at structured practical planning?
- Which model covers all stated constraints more reliably?
- Which model is less likely to invent missing support?
- Which model handles long scenario prompts without turning them into vague essays?

### 5. Custom Comparison Pack in `demo` Mode

Run:

```bash
zsh scripts/run_bench_guarded.sh --prompts artifacts/prompts/adhoc/model_compare_gemma_qwen_pack_20260409.txt --mode demo --urls http://192.168.0.67:1234/v1 --embed-url http://localhost:1234/v1 --output artifacts/bench/20260409/bench_<modeltag>_compare_pack_demo_w1_20260409.md
```

This lane should answer:

- Which model front-loads the highest-value actions better?
- Which model stays compact without dropping critical warnings?
- Which model feels better for live demo use without becoming sloppy?

### 6. Broad Coverage in `default` Mode

Run the normal breadth lane for each model:

```bash
zsh scripts/run_bench_guarded.sh --section "Technical / Engineering,Medical Edge Cases,Social / Governance,Resource Scarcity,Seasonal / Environmental,Adversarial / Myth-Busting,Emotional / Psychological,Compound Scenarios (decomposition tests),Dark Horse / Surprising Retrieval,Security / Defense,Sanitation / Disease Prevention,Water Systems,Animal Husbandry / Food Production,Transportation / Mobility,Abstract Retrieval Robustness,Conflicting Constraints,Multi-Person Triage,Insufficient Information / Safe Refusal,Quantity / Planning / Rationing,Accessibility" --mode default --urls http://192.168.0.67:1234/v1 --embed-url http://localhost:1234/v1 --output artifacts/bench/20260409/bench_<modeltag>_coverage_default_w1_20260409.md
```

This is the primary winner signal because it reflects normal Senku work across the live suite.

### 7. Custom Pack Without RAG

Run one raw-model diagnostic lane on the same comparison pack:

```bash
zsh scripts/run_bench_guarded.sh --prompts artifacts/prompts/adhoc/model_compare_gemma_qwen_pack_20260409.txt --mode default --no-rag --output artifacts/bench/20260409/bench_<modeltag>_compare_pack_norag_default_w1_20260409.md
```

Use this only diagnostically. Questions to answer:

- Does the model already have strong native planning instincts without Senku retrieval?
- Does the Senku scaffold improve the model or fight it?
- Is one model especially prone to confident unsupported world knowledge when retrieval is removed?

### 8. Manual Side-by-Side Read

Do not rely on summary counts alone. Read these prompts side-by-side for both models in both `default` and `demo` modes:

- prompt 1
- prompt 5
- prompt 7
- prompt 9
- prompt 13
- prompt 17
- prompt 21
- prompt 24

Record short notes in the results file for each:

- winner
- why it won
- whether the loser failed because of model behavior or because of a fixable prompt-shaping issue

## Review Heuristics

### Hard Failures

Mark these as serious failures:

- unsafe recommendation that ignores the repo's conservative safety posture
- invented helper, second person, tool, or material not supplied by the prompt
- fabricated certainty around timing, survival windows, structural loads, or medical outcomes without guide support
- failure to answer the actual decision question
- medical escalation that skips stabilize -> clean/protect -> monitor -> evacuate logic
- citation breakage or invented citation ids

### Strong Positive Signals

Give extra credit for:

- explicit go or no-go decision gates
- clean prioritization under time pressure
- visible use of stated materials and constraints
- compact starter-plan answers instead of encyclopedic sprawl
- narrow, defensible warnings instead of generic scolding
- good objective coverage without padded output

## Section-Specific Failure Modes

### Transport Decision

Watch for:

- committing to a crossing before analyzing whether crossing is justified
- importing river-hand-line doctrine into a lake prompt without acknowledging setup constraints
- unsupported hard claims about cold-water timelines
- treating improvised flotation as automatically better than staying put and signaling

### Compound Scenarios

Watch for:

- ignoring deadline, weather, injury, or fuel constraints
- failing to prioritize immediate threats first
- vague advice that never resolves tradeoffs

### Water and Sanitation

Watch for:

- overconfidence about questionable water
- missing contamination-control discipline between sick and well people
- generic cleanliness advice with no rationing logic

### Technical Starter Plans

Watch for:

- scope blow-up into textbooks
- missing process hazards
- multiple branching methods when the prompt clearly asks for one workable starter path

### Group Coordination

Watch for:

- ideology drift
- overbuilding institutions
- skipping review, rotation, or verification mechanisms

### Risk Triage and Communications

Watch for:

- amateur experimentation with unknown chemicals
- underplaying contamination/isolation steps
- weak message-discipline or chain-of-custody thinking for low-tech communication

## Final Decision Rule

Choose Gemma if:

- Qwen is only faster or shorter, but less safe, less complete, or less grounded

Choose Qwen if:

- it matches Gemma on Gate
- it is at least as good on Coverage
- it clearly wins the custom pack on decision quality and compactness
- its losses look fixable without destabilizing Senku's current behavior

If the result is mixed, keep Gemma as default and note Qwen as a promising alternate for future tuning rather than replacing the incumbent immediately.