# Reviewer-facing backend unknowns

This note captures the remaining backend-side unknowns that matter most before calling the current offline Android stack "done enough."

It is intentionally conservative. The goal is to separate:
- what is already solid
- what is directionally good but not yet fully measured
- what still represents meaningful safety or UX risk

## What is already strong

- The Android stack is not a naive source-count RAG app.
  - Retrieval uses layered query-time routing, metadata-aware reranking, route-focused and guide-focused candidate construction, and confidence heuristics that look at more than source count.
- Session handling is better than strict single-turn retrieval.
  - The current system is session-aware, though not fully conversation-native.
- The offline path is real.
  - The physical phone can answer locally with host fallback disabled.
- The abstain path exists.
  - The app does not have to hallucinate an answer when no guide match is credible.
- Deterministic routing exists for a small number of narrow, high-value cases.
  - This is the right shape for safety-critical shortcuts, as long as activation remains conservative.

## Remaining backend unknowns

## 1. Deterministic false-positive risk

This is the highest-priority unresolved backend risk.

Current state:
- The mobile pack currently exports `116` deterministic rule records.
- The active Android deterministic router currently exposes only `9` live rules.
- Those live rules are intentionally narrow and rely on handcrafted inclusion and exclusion clauses.

Why this matters:
- A deterministic false positive is more dangerous than an ordinary retrieval miss.
- If a deterministic route fires incorrectly, the system can present a wrong answer with a stronger evidence signal than a normal generative answer would receive.
- The current safety of the deterministic layer comes mainly from small scope and conservative lexical gating, not from a second-stage semantic verifier.

What is good about the current design:
- Keeping the live Android rule set much smaller than the pack is the right instinct.
- Narrow handcrafted exclusions reduce accidental triggering on obvious near-misses.

What is still missing:
- There is no explicit "graduation gate" documented for moving a deterministic rule from pack metadata into the active Android router.
- There is no second-stage semantic adjudicator or confidence sanity-check after a lexical match.

Recommended next step:
- Establish an explicit rule-promotion policy before expanding the live deterministic surface.

Good policy candidates:
- require repeated validated misses on the same query shape
- require strong lexical specificity
- require low ambiguity with neighboring guide families
- require a false-positive review against near-miss prompts
- require a user-harm analysis before activating any new emergency rule

## 2. Abstain UX and low-applicability handling

Current state:
- The app has an explicit abstain mode.
- When it abstains, the answer surface says that Senku does not have a guide for the query, shows closest matches, and suggests rephrasing or browsing nearby categories.
- The evidence label is forced to limited.

Why this is good:
- It is much safer than bluffing with a weak but confident answer.
- It gives the user something actionable instead of a blank screen.

What is still missing:
- The current abstain behavior is designed mainly for "no credible guide match."
- It does not yet cleanly express a second kind of uncertainty:
  - "a guide family matched, but confidence that it truly applies to this exact situation is still low"

Why that matters:
- In safety-critical contexts, "closest match" and "reliable match" are not the same thing.
- The harder failure mode is not total ignorance.
- The harder failure mode is partial confidence in the wrong frame.

Example of the missing flavor:
- a query that partially resembles a known guide family
- enough to retrieve something plausible
- but not enough to justify decisive advice without more clarification or escalation

Illustrative example:
- `He has barely slept, keeps pacing, and says normal rules do not apply to him. Is this just stress, or should I help him calm down-`
- That query is close enough to retrieve sleep- or stress-adjacent material, but the safer frame may be acute crisis rather than routine self-management.

Recommended next step:
- Add a distinct low-applicability / uncertain-fit response mode later, separate from pure no-match abstain.

That mode should probably:
- say that the library has related material but may not fit the exact situation
- avoid over-specific instructions
- bias toward clarification and escalation when the topic is safety-critical

## 3. On-device latency and instrumentation quality

Current state:
- Offline answering on the physical phone is working.
- The current phone validation proves local inference can succeed with host fallback disabled.
- We have end-to-end smoke timing, but not yet a clean shareable latency breakdown by query class.

Why this matters:
- Retrieval quality is no longer the only UX constraint.
- Inference latency may now be the dominant product-feel problem.
- Without trustworthy time-to-first-token and full-answer timing by scenario type, UI decisions are partly guesswork.

What is known:
- End-to-end smoke timing exists for at least one offline physical-phone run.
- That number is not yet the same thing as:
  - model-only latency
  - retrieval-only latency
  - time to first token
  - time to usable answer for emergency prompts

What is still unknown:
- how much of current runtime is model inference versus harness/UI overhead
- whether emergency/medical prompts are materially slower than practical how-to prompts
- whether streaming materially improves the user experience on the actual target hardware

Recommended next step:
- instrument and record a small latency panel for representative prompt classes

Suggested classes:
- practical how-to
- medical query requiring cross-guide retrieval and disambiguation
- deterministic route
- abstain
- acute emergency generative

Useful metrics:
- retrieval time
- prompt-build time
- generation start time
- time to first visible output
- total answer completion time

## What we can safely say now

- The current Android retrieval and routing stack is already more sophisticated than a basic offline RAG application.
- The system is session-aware rather than purely single-turn.
- The offline local-inference path is real and demonstrable.
- The app has explicit abstain behavior rather than forcing weak answers in all cases.
- Deterministic routing is currently small and conservative rather than broad and reckless.

## What we should not overclaim yet

- That deterministic routing is broadly mature or low-risk at larger scale.
- That the abstain story fully covers low-confidence applicability cases.
- That current phone latency is fully characterized.
- That the present UI behavior has already been optimized around measured on-device emergency-query timing.

## Bottom line

The backend is in a strong place structurally.

The remaining issues are not "basic retrieval is broken" problems.
They are more mature, harder problems:
- deterministic false-positive control
- uncertainty communication when a guide is nearby but not clearly right
- honest measurement of on-device latency for real use cases

That is a good place to be, but it is also exactly where disciplined product/safety choices matter most.
