# Slice D37 - wave_ag dental-family retrieval hardening

- **Role:** main agent (`gpt-5.4 xhigh`). Guide-quality micro-slice; safe to delegate to a `gpt-5.4 high` worker after D36 lands.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** avoid any parallel writer touching the same guide files or the same validation artifact output directory.
- **Why this slice now:** After D36, the repo is ready for a first real guide-facing micro-slice. `wave_ag` already gives a bounded prompt pack around complaint-first dental and poisoning-adjacent routing, which is narrow enough to harden without reopening broad medical/toxicology review.

## Outcome

One focused commit that:

1. Improves complaint-first routing / retrieval cues in the dental family so:
   - mild bleeding-gums / sensitivity / mild toothache complaints bias toward `preventive-dental-hygiene`
   - urgent fever / throbbing / night-waking dental complaints bias toward `emergency-dental`
   - poisoning / cleaner-ingestion prompts are not accidentally pulled into dental guidance
2. Re-ingests the corpus after the guide edits.
3. Runs the bounded `wave_ag` validation pack and writes an artifact bundle.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `8850b63b8a0d45a31280f3d499749d9e3c52fb7f` (`D36: track guides corpus as-is`) or descends from it.
2. The target guide files are now tracked and clean before edit.
3. The bounded prompt pack still exists:
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ag_20260415.txt`
4. Current guide-validation flow is available in the checkout:
   - `scripts/run_guide_prompt_validation.ps1`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - the residual historical root `notes/` backlog remains

## Likely touch set

Primary expected edits:
- `guides/preventive-dental-hygiene.md`
- `guides/emergency-dental.md`

Boundary companions only if genuinely needed:
- `guides/chemical-safety.md`
- `guides/unknown-ingestion-child-poisoning-triage.md`

Validation / ingest surfaces:
- re-ingested desktop corpus outputs as produced by the normal repo flow
- one new `artifacts/bench/...` bundle for the `wave_ag` run

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** add new guides in this slice.
- Do **not** widen into broad toxicology content review.
- Do **not** rewrite pediatric poisoning triage substance-by-substance.
- Do **not** change app code in this slice.
- Do **not** widen beyond the dental family plus the two named boundary companions.
- Do **not** mix unrelated guide families into the same commit.

## Editing rules

### Step 1 - read-only diagnosis

Read:

- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ag_20260415.txt`
- `guides/preventive-dental-hygiene.md`
- `guides/emergency-dental.md`
- `guides/chemical-safety.md`
- `guides/unknown-ingestion-child-poisoning-triage.md`

Before editing, form the smallest routing-hardening hypothesis that explains the `wave_ag` family:

1. Everyday gum-bleeding / mild sensitivity / mild toothache prompts should land on preventive care.
2. Fever + throbbing + night-waking tooth pain should land on emergency dental.
3. Cleaner-ingestion / toddler-pill prompts should route away from dental guides.

### Step 2 - make only bounded retrieval-hardening edits

Allowed edit patterns:

1. Complaint-first quick-routing bullets near the top.
2. Stronger aliases / natural-language hooks for the target complaint shapes.
3. Clearer sibling-boundary language between preventive vs emergency dental.
4. Reciprocal link tightening where the poisoning boundary needs to be explicit.

Not allowed in this slice:

1. New procedural dental treatment content.
2. Substantive toxicology protocol rewrites.
3. Broad section reorganization outside the targeted complaint-first routing surfaces.

### Step 3 - re-ingest

Re-ingest after the guide edits using the repo’s normal ingest flow before trusting retrieval results.

### Step 4 - run bounded validation

Run the existing guide-direction validation for:

- `wave_ag`

Write a fresh artifact bundle under `artifacts/bench/`.

If the expected validation environment is unavailable, report the exact blocker rather than widening into infrastructure repair in this slice.

## Acceptance

- One commit only.
- Guide edits stay within the dental family and, only if needed, the two named boundary companions.
- The change is retrieval-hardening / routing-oriented, not a broad safety rewrite.
- Re-ingest was run after guide edits.
- A fresh `wave_ag` validation artifact bundle exists.
- The final report clearly states whether the target prompts now route more cleanly by complaint family.

## Commit

Single commit only. Suggested subject:

```text
D37: harden wave_ag dental-family routing
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose wave_ag prompt family + bounded guide edits + re-ingest + rerun wave_ag.
- If delegated, the worker should own the guide reads, the bounded edits, the re-ingest, the validation run, the single commit, and a concise note about whether the poisoning-boundary companions needed edits.

## Anti-recommendations

- Do **not** turn this into a toxicology project.
- Do **not** edit unrelated medical guides.
- Do **not** add new guides.
- Do **not** widen into app code or harness refactors.

## Report format

- Commit sha + subject.
- Which guide files changed.
- Whether boundary companions needed edits.
- Re-ingest command/result.
- Validation artifact path.
- Short summary of the routing improvement or remaining blocker.
