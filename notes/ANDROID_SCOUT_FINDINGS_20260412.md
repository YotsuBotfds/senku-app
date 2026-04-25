# Android Scout Findings - 2026-04-12

This note condenses the most useful read-only scout output from the `2026-04-12` brainstorming pass.

## Main Read

The main Android risk is now coverage mismatch more than isolated prompt bugs.

- The repo has a large prompt surface, including `test_prompts.txt`, [`../artifacts/prompts/adhoc/senku_591_prompt_pack_v2_20260410.csv`](../artifacts/prompts/adhoc/senku_591_prompt_pack_v2_20260410.csv), and [`../artifacts/prompts/adhoc/senku_delta_regression_pack_96_20260410.csv`](../artifacts/prompts/adhoc/senku_delta_regression_pack_96_20260410.csv).
- The mobile pack manifest still reports relatively sparse retrieval metadata coverage.
- Android has improved a lot on the currently watched lanes, but it is still under-validated across many prompt families the repo already knows matter.

## Highest-Risk Coverage Gaps

### Communications / Governance / Defense / Rumor-Authentication

- Likely miss cluster because the guides exist but mobile metadata is thin.
- The scout read these categories as under-tagged in the current mobile pack.
- Relevant guide families include courier/authentication, oral communications, news distribution, governance systems, and defense planning.

### Water Infrastructure / Sanitation Beyond Container Safety

- Bottle/jug deterministics are strong now.
- Broader water prompts are still slower and narrower than they should be.
- The likely weak slice is everything around distribution, rainwater, wells, latrines, hauling, rationing, and contamination control.

### Broad Construction / Small-Watercraft

- House/site selection is much better than before, but still expensive and occasionally wobbly on section choice.
- Canoe/small-watercraft remains lightly replayed on Android compared with desktop.

### Multi-Objective Separation / Triage

- Android follow-up validation is proving simple continuity well.
- It is still under-tested on the harder cases where the answer must keep multiple objectives or injury/shelter/water concerns separate.

### Craft / Refinement Watchlist

- Desktop already has a known refinement/watchlist history for several craft prompts.
- Android has replayed only a small portion of those.
- Candle/tallow is the proof case: the knowledge existed, but mobile had to be steered into it.

## Highest-Value Structural Risks

### Shared-Contract Drift

- Deterministic routing still lacks a true shared source of truth across desktop, pack export, and Android runtime.
- Manifest/runtime config still drifts because Android shows pack defaults while still running several hardcoded limits/templates.

### Session-Model Drift

- Desktop and Android still use different session models.
- Desktop remains the quality reference overall, but deterministic desktop turns still need explicit session-memory coverage before calling the two systems fully aligned.

### Metadata Foundation Is Helping But Still Partial

- Metadata-based Android steering is working.
- It is still too sparse and heuristic-heavy to trust without audits and broader replay.

## Best Next Validation Pack

Build a compact Android gap pack from existing repo prompts:

- 4 communications / governance / security prompts
- 4 water infrastructure / sanitation prompts
- 4 construction / small-watercraft prompts
- 4 multi-objective separation prompts
- 4 craft / refinement prompts

Add 8 follow-up pairs on top:

- `what next`
- `what about storage tanks`
- `what about sealing`
- `how do we verify that`
- `what gets priority first`

Grade only:

- correct anchor guide family
- no obvious support drift
- usable section choice
- latency band

Use desktop as oracle before adding content.

## Highest-ROI Fix Order

1. Expand mobile metadata before adding new guides.
2. Add section-level aliases for buried everyday terms that Android still misses.
3. Split water metadata more finely.
4. Split construction metadata more finely.
5. Only then add compact starter subsections where the guide exists but mobile retrieval still cannot surface it cleanly.

## Immediate Practical Takeaway

The likeliest Android failures are increasingly not missing-knowledge failures.

They are:

- the guide exists
- desktop can find it or shape it
- mobile metadata and section surfacing still cannot steer into it reliably enough