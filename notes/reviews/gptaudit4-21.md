# GPT Audit 2026-04-21

Historical note: This historical read-only audit was relocated from the repo root into `notes/reviews/` in D13.

## Scope

This note is a read-only audit pass over the repo with two inputs:

- parallel reading across major `notes/` clusters
- direct inspection of the current desktop and Android code paths

I did not try to cover every single markdown file in `notes/`, but I did cover the main operating surfaces:

- swarm/orchestration notes
- guide/retrieval/routing notes
- Android/mobile/UI notes
- CP9 / RC timeline notes
- all files under `notes/specs/`
- active dispatch docs plus representative completed dispatch/review/handoff docs

I also inspected the code paths that appear to be the center of gravity:

- `query.py`
- `ingest.py`
- `mobile_pack.py`
- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `config.py`
- Android: `PackRepository.java`, `OfflineAnswerEngine.java`, `MainActivity.java`, `DeterministicAnswerRouter.java`, `SessionMemory.java`, `QueryMetadataProfile.java`, `PromptHarnessSmokeTest.java`

## Project Read

The project feels much more like an evidence-driven retrieval/safety program than a normal app repo.

The core product is not just "offline survival assistant." It is really three intertwined systems:

1. a guide corpus and ingest pipeline
2. a retrieval/routing engine that tries to stay conservative under weak support
3. a validation/harness program that is treated almost like production infrastructure

The strongest cultural signal in the repo is that people here do not trust green-looking output until they can prove the exact build, pack, model, posture, and artifact chain that produced it. That is a real strength.

The other strong signal is that a lot of the product behavior is encoded as handwritten heuristics rather than small shared abstractions. That has clearly let the team move fast, but it is now the main maintenance pressure.

## What Looks Strong

- The repo has unusually good operational memory. The notes preserve why work happened, what was believed at the time, and what later turned out to be wrong.
- Safety posture is conservative. The intent is clearly "route dangerous prompts toward honest, bounded behavior first" rather than stretch coverage for appearance.
- There is real test coverage around the behavior that matters most:
  - abstain
  - uncertain-fit
  - anchor prior
  - deterministic rule overlap/parity
  - mobile-pack metadata behavior
- `ingest.py` and `mobile_pack.py` both show serious effort to validate metadata and preserve structure instead of treating the corpus as loose text blobs.
- Android and desktop both have explicit concepts for weak-support handling instead of pretending every query should become a generated answer.

## Main Risks

### 1. Policy Drift Across Implementations

The biggest architectural risk is that the same product idea exists in multiple partially independent forms.

- Desktop has a large Python routing/retrieval policy engine in `query.py`.
- Android has parallel logic in `OfflineAnswerEngine.java`, `PackRepository.java`, `SessionMemory.java`, `QueryMetadataProfile.java`, and `DeterministicAnswerRouter.java`.
- The mobile pack adds a third policy layer through `mobile_pack.py`.
- The specs in `notes/specs/` are a fourth layer.

There are guardrails for some of this drift, especially deterministic parity checks in `mobile_pack.py` and related tests, but the general routing/reranking/mode logic is still duplicated by intent rather than truly shared.

My strongest audit conclusion is: this repo's long-term risk is not lack of ideas or lack of validation. It is semantic drift between the Python truth model, the Android truth model, the pack-export truth model, and the written-spec truth model.

### 2. Monolith Files Are Carrying Too Much Product Meaning

The central files are large:

- `query.py`: about 9,793 lines
- `mobile_pack.py`: about 2,315 lines
- `OfflineAnswerEngine.java`: about 2,685 lines
- `MainActivity.java`: about 2,903 lines
- `PackRepository.java`: about 5,540 lines

Those are not automatically bad, but here they contain real product policy, not just plumbing. In practice that means:

- thresholds live next to rendering behavior
- retrieval heuristics live next to transport/runtime concerns
- safety rules live next to prompt construction
- UI logic and workflow state live together

That makes local fixes easy and global confidence harder.

### 3. `query.py` Is Impressive But Fragile

`query.py` currently owns all of this:

- deterministic predicate matching
- special-case routing
- hybrid vector + lexical fusion
- metadata reranking
- anchor-prior follow-up bias
- scenario frame/session handling
- abstain gating
- uncertain-fit gating
- confidence labeling
- prompt assembly
- citation cleanup/normalization

This is a lot of intelligence in one file, and the densest pressure point is `_metadata_rerank_delta(...)`. It is clearly powerful and probably responsible for a lot of the retrieval wins, but it is also where subtle regressions are most likely to hide because the logic is deeply additive, domain-specific, and marker-heavy.

If I were debugging "why did this route flip?" six weeks from now, `_metadata_rerank_delta(...)` is the first place I would expect to lose time.

### 4. Validation Infrastructure Is Part Of The Product

The notes make this very clear: many important failures were not pure engine bugs.

Recurring failure classes include:

- stale APK / stale pack / wrong model on device
- harness settle bugs
- cross-activity handoff confusion
- missing or misleading logcat
- posture/serial assumptions
- gallery/state-pack assertions that went green for the wrong reason

That means the harness and artifact layer should be treated as product code, not as optional test glue. The notes already act that way. The codebase should too.

### 5. Tracker And Process Drift Are Real

The repo is unusually well documented, but the docs also drift.

Patterns that showed up repeatedly in the notes:

- routing ladders described differently in different files
- active queue / dispatch readme / actual state getting out of sync
- notes that correctly preserve history but are no longer current
- slices going green because the assertion moved, not because the underlying behavior fully improved

This is not a criticism of the documentation culture. It is the cost of having a very active one.

## Code-Level Observations

### Desktop

#### `query.py`

High upside:

- strong behavioral coverage in tests
- explicit answer modes
- anchor prior is implemented in a restrained way
- abstain and uncertain-fit are real first-class branches

Pressure points:

- `_metadata_rerank_delta(...)` is large enough to function as a rule engine without looking like one
- special-case predicates are numerous and live close to retrieval/prompt logic
- confidence, answer mode, and weak-support behavior are easy to reason about locally but hard to reason about globally

#### `ingest.py`

This file looks healthier structurally than `query.py`.

Good signs:

- metadata validation is explicit
- manifest normalization shows real migration thinking
- lexical index creation is deliberate, not bolted on
- chunk IDs are stable and collision-resistant

The main audit risk here is not sloppiness. It is whether ingest-side metadata preservation is strong enough to support all of the routing ambitions documented elsewhere.

#### `mobile_pack.py`

This file is doing more than export. It is also a metadata-classification layer and a parity surface.

That is valuable, but it means pack export is now partially a behavior compiler. That is a powerful idea, and it also raises the cost of drift if Android, desktop, and pack heuristics stop agreeing.

### Android

#### `PackRepository.java`

This looks like the Android retrieval center of gravity.

It is carrying:

- lexical/vector retrieval
- FTS runtime adaptation
- anchor-related weighting
- answer-context selection
- hybrid merge behavior
- route-focused retrieval behavior

Given the notes and the file size, this is the Android file I would watch most closely for retrieval regressions.

#### `OfflineAnswerEngine.java`

This is the Android answer-mode brain.

What stands out:

- Android intentionally mirrors the desktop mode concepts
- the implementation is not a literal parity port; it uses mobile-native heuristics and approximations
- weak-support downgrade behavior is clearly important
- host-vs-on-device fallback behavior is operationally important and observability-sensitive

The biggest risk here is not bad logic. It is partial parity: desktop and Android are conceptually aligned, but not mechanically unified.

#### `MainActivity.java`

This file feels like a workflow shell that has accumulated a lot of product behavior.

That is understandable given the current architecture, but it makes the activity part of the app's behavioral surface, not just navigation. The notes about harness drift and cross-activity truth make that especially relevant.

#### `DeterministicAnswerRouter.java`

The Android deterministic subset being smaller than desktop looks intentional, not accidental.

That is fine. The real risk is not the smaller set. The risk is that Android promotion criteria, desktop registry state, and pack-export metadata could slowly drift unless the parity checks remain actively maintained.

## Spec vs Code

The specs are more mature than I expected. They describe:

- answer-mode ladder
- anchor-prior behavior
- confidence token behavior
- uncertain-fit body contract
- phone/tablet UI direction
- presenter extraction direction

What I would watch:

- uncertain-fit naming and rendering terminology still looks a little unstable across docs
- some UI/navigation details appear to have changed after spec text was written
- several notes effectively say "prefer code if docs disagree," which is practical but also a sign the spec surface is not fully authoritative

So the specs are useful, but I would treat them as strong intent, not final executable truth.

## Highest-Value Insights

### 1. The Main Problem Is No Longer "Need More Guides"

The notes and code both point to the same answer: the hard part now is selection, routing, safety posture, and validation truth. Adding more guides without improving those systems will not move the repo proportionally.

### 2. Retrieval Behavior Is Approaching Rule-Engine Complexity

You already have a rule engine in practice. It is just distributed across:

- metadata markers
- special-case predicates
- answer-mode thresholds
- anchor priors
- pack metadata profiles
- Android route profiles

That is not inherently bad, but it means future maintainability depends on making this more declarative, inspectable, and comparable across platforms.

### 3. Harness Truth Needs To Be Treated As A First-Class Contract

A lot of the repo's most important notes are really about "what actually happened?" rather than "what should the code do?"

That is a clue:

- artifact truth
- busy-signal truth
- APK/pack/model identity truth
- posture truth
- host/local execution truth

are part of the product contract now.

### 4. The Repo Is Strongest When It Uses Small, Explicit Contracts

Examples:

- `abstain` as a distinct answer mode
- deterministic registry
- metadata validation report
- deterministic parity checks
- fixed emulator matrix
- explicit related-link weighting

Whenever the repo makes a behavior explicit, it gets more reliable. Whenever it relies on an implied convention spread across notes and code, it gets harder to reason about.

## What I Would Prioritize Next

If the goal were robustness rather than shipping one more narrow fix, my order would be:

1. Create a more declarative shared routing/spec surface.
   - especially markers, thresholds, and answer-mode conditions
2. Strengthen invariant logging around build/pack/model/runtime identity.
3. Keep shrinking behaviorally important monoliths by extracting policy modules, not just utilities.
4. Continue parity tests between desktop, pack export, and Android wherever possible.
5. Treat harness and artifact schemas as core interfaces.

## Bottom Line

This is a strong repo with real engineering judgment behind it.

The project does not look sloppy. It looks ambitious, fast-moving, and increasingly constrained by the success of its own heuristic sophistication.

If I had to summarize the audit in one sentence:

> The repo's next big risk is not missing capability; it is keeping four different expressions of the same product logic aligned while the validation surface grows more operationally complex.
