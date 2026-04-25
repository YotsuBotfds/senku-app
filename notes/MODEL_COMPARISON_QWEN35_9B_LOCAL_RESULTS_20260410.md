# Qwen 3.5 9B Local Results — 2026-04-10

## Scope

Local-only Senku testing on this machine using:

- generation model: `qwen/qwen3.5-9b`
- embeddings: local LM Studio endpoint
- single generation worker
- thinking left **on**

## Critical Runtime Finding

This model was not failing because of context-window exhaustion.

The actual issue was completion-budget starvation while think-mode was active:

- at lower caps (`768`, `1536`, `2048`), Qwen often spent the entire budget in `reasoning_content` and returned empty `message.content`
- raising `--max-completion-tokens` to `4096` made the main RAG lanes complete successfully

Practical implication:

- keep thinking on if desired, but do **not** benchmark this model on the local Senku RAG path with small completion caps

## Repo Fixes Made During This Pass

### 1. Windows import portability

[`query.py`](../query.py) now treats `termios` as optional so `bench.py` can import cleanly on Windows.

### 2. Watchlist script portability

[`scripts/report_bench_watchlist.py`](../scripts/report_bench_watchlist.py) now reads JSON as UTF-8 explicitly, which fixes the Windows decode failure on fresh artifacts.

## Artifacts Produced

### Smoke / comparison artifacts

- `artifacts/bench/20260410/bench_qwen35_9b_q8_smoke_demo_w1_20260410_r2.*`
  - failed lane at `768` completion cap (`0/3`, empty completions)
- `artifacts/bench/20260410/bench_qwen35_9b_q8_smoke_demo_w1_20260410_r3_t4096.*`
  - successful rerun at `4096` completion cap (`3/3`)
- `artifacts/bench/20260410/bench_qwen35_9b_q8_compare_pack_default_w1_20260410_t4096.*`
  - full 24-prompt comparison pack in `default`
- `artifacts/bench/20260410/bench_qwen35_9b_q8_compare_pack_demo_w1_20260410_t4096.*`
  - full 24-prompt comparison pack in `demo`
- `artifacts/bench/20260410/bench_qwen35_9b_q8_compare_pack_norag_default_w1_20260410_t4096.*`
  - raw-model diagnostic without retrieval
- `artifacts/bench/20260410/bench_qwen35_9b_q8_prompt20_default_w1_20260410_t8192.*`
  - targeted rerun for the capped group-coordination prompt

### Broad validation artifacts

- `artifacts/bench/20260410/bench_qwen35_9b_q8_gate_default_w1_20260410_t4096.*`
- `artifacts/bench/20260410/bench_qwen35_9b_q8_coverage_default_w1_20260410_t4096.*`
- `artifacts/bench/20260410/bench_qwen35_9b_q8_sentinel_default_w1_20260410_t4096.*`

## High-Level Result

Qwen 3.5 9B is mechanically viable on the local Senku RAG path **if** the completion cap is raised substantially.

However, it still shows meaningful quality costs:

- duplicate citations remain high on broad RAG lanes
- several prompts become longer in `demo` mode instead of shorter
- some transport / decision prompts still drift or over-specify
- the raw no-RAG lane is poor enough that this model should be treated as heavily scaffold-dependent

## Bench Summary

### Smoke demo (`4096`)

- prompts: `3`
- successful: `3`
- errors: `0`
- total generation time: `121.0s`
- avg per prompt: `40.3s`
- duplicate citations: `10`

### Compare pack default (`4096`)

- prompts: `24`
- successful: `24`
- errors: `0`
- total generation time: `955.9s`
- avg per prompt: `39.8s`
- duplicate citations: `72`

Main read:

- usable
- often thorough
- still prone to citation duplication and over-answering

### Compare pack demo (`4096`)

- prompts: `24`
- successful: `24`
- errors: `0`
- total generation time: `1093.8s`
- avg per prompt: `45.6s`
- duplicate citations: `64`

Main read:

- duplicate citations improved slightly versus `default`
- but `demo` did **not** become more compact overall
- several prompts were actually longer/slower in `demo`

### Gate default (`4096`)

- prompts: `30`
- successful: `30`
- errors: `0`
- total generation time: `0`
- decision path: `deterministic=30`

Main read:

- Gate is fully clean on the current deterministic/control-path layer
- this model does not introduce a Gate regression under the repo’s present structure

### Coverage default (`4096`)

- prompts: `114`
- successful: `113`
- errors: `1`
- total generation time: `2477.0s`
- avg per prompt: `21.7s`
- duplicate citations: `317`

Main read:

- mechanically strong overall
- one empty-completion failure only
- still too much citation duplication on the broad lane
- a few prompts are pushing or hitting the completion cap

Coverage prompts worth re-reading first:

- `how do i run a fair trial with no lawyers or judges` (`4096` cap hit)
- `one person has chest pain, one has a fever, and one has a deep cut. how do i prioritize them` (`4096` cap hit)
- `how do we set up camp for someone who is blind or has very low vision` (`4096` cap hit)
- `we need a safer place to sleep tonight but cant build much, what matters most` (`4014` tokens)
- `how do i make soap from animal fat` (the single empty-completion error)

### Sentinel default (`4096`)

- prompts: `19`
- successful: `19`
- errors: `0`
- total generation time: `248.3s`
- duplicate citations: `44`

Main read:

- sentinel/control behavior is stable
- only `5` prompts reached live generation
- deterministic/off-topic/underspecified routing stayed intact

### Compare pack no-RAG default (`4096`)

- prompts: `24`
- successful: `11`
- errors: `13`
- total generation time: `1191.9s`
- avg per successful prompt: `108.4s`

Main read:

- this lane is poor
- many prompts either hit the cap or returned empty completions
- unsupported invented citations / unsupported generic placeholders appear
- do not evaluate Qwen’s Senku suitability from no-RAG alone; it clearly depends on the retrieval scaffold

## Prompt Watchlist

The local watchlist from the fresh broad artifacts is dominated by:

### High completion-token prompts

- `how do i run a fair trial with no lawyers or judges`
- `one person has chest pain, one has a fever, and one has a deep cut. how do i prioritize them`
- `how do we set up camp for someone who is blind or has very low vision`
- `where is the best place to hide a supply cache`
- `how do i make snowshoes`
- `how do i build a raft to cross a lake`

### Highest duplicate-citation prompts

- cold-lake crossing comparison prompt
- clay oven starter prompt
- deer preservation triage prompt
- no-electronics camp-to-camp communications prompt
- `how do i build a loom and weave cloth`
- `can i eat acorns`
- `how do i make a perimeter alarm with no electronics`

## Takeaways

### What looks good

- Qwen can complete broad local RAG validation successfully when the completion cap is high enough
- Gate remains fully clean
- Coverage success rate is strong (`113/114`)
- Sentinel/control behavior stays stable

### What still looks weak

- citation duplication is materially worse than ideal
- `demo` mode does not reliably compress answers
- some decision questions still drift into overconfident detail
- no-RAG behavior is weak enough that Qwen should not be treated as a strong standalone fallback on this workflow

## Suggested Next Step

If continuing the Qwen lane:

1. treat `4096` as the current minimum practical local completion cap with thinking on
2. review the prompt watchlist above before touching retrieval or prompt policy
3. compare these Qwen artifacts directly against the local `gemma-4-e4b-it` lane before making any default-model decision
