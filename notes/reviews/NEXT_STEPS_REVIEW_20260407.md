# Senku Next Steps Review

Status: brainstorming only. No tests or benches were run for this document.

Scope: near-term directions for improving accuracy, bias control, calibration, and demo-readiness without overfitting the bot into a character impression or weakening grounding.

## Current Read

The system is already strongest when the user asks a practical, guide-covered question with explicit constraints. The current weak spots are mostly not "raw model intelligence" problems. They are coordination problems between decomposition, retrieval balance, prompt scoping, citation discipline, and output calibration.

The main pattern to avoid is adding more "persona" while leaving the retrieval and control surfaces fuzzy. A sharper voice is fine; a less reliable answer is not.

## Direction 1: Accuracy and Grounding

### 1. Structured scenario parsing before retrieval

Today, the system mostly treats scenario prompts as text to decompose. A stronger approach is to extract a light structure first:

- objectives: stop bleeding, get water, make shelter
- assets: tarp, buckets, lighter, gas can
- constraints: rain, time before dark
- hazards: bleeding, contamination, hypothermia, fire risk

This does not need full semantic parsing. Even a small rule-based layer would help retrieval and answer completeness.

Why it matters:

- improves multi-goal coverage
- reduces inventory fragments crowding retrieval
- makes asset accounting more consistent

### 2. Guide diversity in reranking

The current retrieval can over-cluster on one category or one guide family. A simple rerank bonus for cross-guide and cross-category diversity would reduce answers that look over-medicalized or over-water-focused.

Good target behavior:

- one or two best chunks per guide before taking near-duplicates
- maintain at least one high-quality chunk for each explicit user objective when available

### 3. Objective coverage checks before generation

Before sending the prompt to generation, compute a small checklist from the user query:

- did retrieval cover bleeding
- did retrieval cover water
- did retrieval cover shelter
- did retrieval cover the time constraint

If a stated objective has no obvious retrieval support, mark it in the prompt as a gap to address conservatively instead of silently dropping it.

### 4. Conservative suppression for invasive medical content

Medical retrieval should keep aggressive field procedures available for true last-resort cases, but they should not dominate ordinary injury scenarios. This is partly prompt control, but it is also a retrieval/rerank problem.

Directions:

- add soft penalties for surgery-heavy guides unless the query explicitly includes tool/training markers
- add bonuses for first-aid, stabilization, evacuation, and monitoring guides in generic injury prompts

### 5. Citation quality over citation quantity

Inline `GD-xxx` citations are useful, but the next step is better citation behavior, not more citations.

Directions:

- discourage stacked duplicate IDs
- prefer one strong citation per substantive claim
- consider lightweight post-generation checks for uncited recommendations and obviously mismatched citations

### 6. Separate "direct support" from "inference"

The system already allows logical conclusions from the guides. That is good, but it is opaque.

Useful next step:

- label claims as either directly supported by retrieved material or inferred from retrieved principles

This would improve calibration without making answers timid.

## Direction 2: Bias Control and Policy Quality

### 1. Treat bias as a system-control problem, not just a fairness problem

The most relevant biases here are:

- domain bias: medical or water retrieval crowding out other objectives
- escalation bias: jumping to invasive or advanced procedures
- persona bias: sounding "more Senku" by becoming colder, more absolute, or more copyright-adjacent
- moral bias: defaulting to crude utilitarian answers under scarcity

These are all controllable.

### 2. Keep persona original and constrained

The product should keep a scientist-engineer voice without drifting into franchise mimicry.

Directions:

- keep "mechanism-first" and "systems-thinking" as the persona anchor
- avoid explicit canon phrases, quotes, or named-scene imitation
- do not reward style that weakens sourcing or caution

### 3. Prevent "cold rationalist" drift

A common failure mode for science-flavored personas is confusing analytical tone with morally shallow reasoning.

The better posture is:

- explicit tradeoffs
- stated decision frameworks
- acknowledgement of uncertainty
- refusal to pretend that one moral framework is the only scientific answer

This is especially important for triage, governance, punishment, and rationing prompts.

### 4. Control overreach in off-topic and metaphysical prompts

The bot should reject unsupported metaphysical claims, but the redirect should stay short and purposeful.

Future refinement:

- distinguish clean off-topic refusal from "social resilience" fallback
- only pivot into practical domains when the user intent actually suggests it

### 5. Calibration against authoritative tone

The current voice can sound highly certain even when the system is synthesizing from partial coverage.

Directions:

- expose confidence based on source breadth and directness
- use stronger calibration language when retrieval is thin, indirect, or cross-domain
- reserve hard certainty for strongly supported claims

## Direction 3: Evaluation and Observability

### 1. Expand the rubric beyond five binary fields

The current rubric is useful, but it misses some of the failure modes that matter most in live use.

Additions worth considering:

- completeness: did it address all stated objectives
- constraint coverage: did it account for time, weather, assets, and hazards
- calibration: did confidence match support
- citation precision: did citations actually back the nearby claim
- source diversity: did the answer rely on one narrow lane when the question was multi-domain

### 2. Add targeted stress-test sections

Recommended new prompt groups:

- asset-accounting scenarios
- conflicting asset use prompts
- metaphysical or mystical premise rejection
- panic/high-emotion de-escalation
- moral allocation under scarcity
- "why" questions that require system design rather than instructions
- prompts where aggressive guides are retrieved but should not dominate

### 3. Log more retrieval-side metadata in bench artifacts

Current bench output is already solid, but the next layer should make diagnosis faster.

Add fields like:

- retrieved category distribution
- top guide diversity count
- whether every extracted objective had at least one retrieved chunk
- duplicate citation count
- inferred-vs-direct claim estimate
- which prompt constraints were explicitly mentioned in the response

### 4. Add a lightweight local review pack

Since local iteration matters, a small hand-curated prompt pack is more useful than always reaching for a full bench.

Good shape:

- 10 to 15 prompts
- one prompt per key failure mode
- small enough to review manually on local hardware
- stable wording so deltas are visible

This should be a complement to Gate/Coverage/Sentinel, not a replacement.

### 5. Improve source-display UX

The current source display is useful for debugging but could do more for trust.

Possible upgrades:

- explicitly separate cited sources from retrieved-only context
- show source category mix
- mark when a response used inference across multiple guides
- add a compact "why these sources" debug mode for demo or development use

## Direction 4: Demo and Product Readiness

### 1. Build a small "show, stress, explain" demo script

Instead of one long bench-style session, keep a short live-demo sequence:

- one practical emergency prompt
- one systems/governance prompt
- one off-topic or metaphysical redirect
- one mixed-domain stress prompt

This demonstrates breadth without depending on perfect retrieval for every class of prompt.

### 2. Add a public-safe mode concept

If the project may go public later, it would help to define a stable public behavior target now:

- original scientist-engineer voice
- no copyright-adjacent phrasing
- careful ethical posture
- explicit citation discipline

This reduces future prompt churn.

### 3. Keep "demo polish" separate from "release quality"

A quick demo only needs:

- coherent structure
- visible citations
- obvious prioritization
- no embarrassing policy or medical escalation misses

Release quality needs much more:

- broader evaluation
- guide gap tracking
- failure analytics
- regression discipline

Treating those as separate tracks prevents over-optimization for one live showing.

## Phased Roadmap

### Phase 1: Low-risk control improvements

- tighten prompt and output calibration rules
- expand rubric to include completeness and constraint coverage
- improve source display labeling
- define a lightweight local review pack

Expected payoff:

- better demos
- easier human review
- fewer "looked smart but missed the ask" failures

### Phase 2: Retrieval and rerank hardening

- structured objective and asset extraction
- diversity-aware reranking
- stronger medical conservatism in generic injury retrieval
- objective coverage checks before generation

Expected payoff:

- fewer multi-constraint misses
- less domain crowding
- cleaner ordered plans

### Phase 3: Calibration and attribution quality

- direct-support vs inference labeling
- better citation precision checks
- confidence signals based on retrieval support
- richer bench metadata for diagnosis

Expected payoff:

- higher trust
- easier debugging
- less overconfident synthesis

### Phase 4: Deeper policy and product work

- public-safe mode definition
- clearer ethical stance for scarcity and governance prompts
- demo script and documentation
- optional UX improvements around source explanation and confidence

Expected payoff:

- easier demos
- smoother public-facing positioning
- better long-term maintainability

## Recommended Near-Term Sequence

If the goal is "best payoff without turning this into a months-long project," the order should be:

1. rubric expansion for completeness and calibration
2. lightweight local review pack
3. retrieval diversity and objective coverage work
4. medical overreach suppression in retrieval/reranking
5. citation precision and confidence signaling

## Things Not To Optimize For Yet

- stronger fictional character mimicry
- heavier prose styling
- more aggressive system-prompt complexity without better retrieval signals
- large benchmark expansion before observability improves
- using Sentinel scores as release criteria

## Bottom Line

The next meaningful improvements are not about making the bot "sound smarter." They are about making it more explicit, more calibrated, and more balanced across user objectives. The best next step is to improve objective coverage, retrieval diversity, and evaluation visibility together so each prompt is easier to trust and easier to debug.
