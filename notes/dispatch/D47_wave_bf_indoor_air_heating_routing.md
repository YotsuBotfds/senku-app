# Slice D47 - wave_bf indoor-air / heating complaint-first routing

- **Role:** main agent (`gpt-5.4 xhigh`). Guide-quality micro-slice; safe to delegate to a `gpt-5.4 high` worker after D46 lands.
- **Paste to:** **new window** only after D46 lands cleanly or the active guide lane otherwise reaches a natural stop point.
- **Parallel with:** avoid any parallel writer touching the same heating / indoor-air guide files or the same `wave_bf` validation output directory.
- **Why this slice now:** If D46 lands, the next bounded guide-validation family in sequence is `wave_bf`. The prompt pack is tight and complaint-first: smoky rooms, stove backdraft, dizziness/headache near heating, and "leave first or ventilate first" triage. This is a cleaner continuation than reopening Android or jumping ahead to a broader chemistry / industrial-precursor lane.

## Outcome

One focused commit that:

1. Improves routing for indoor-heating / indoor-air prompts so:
   - stove smoke-back, chimney, and safe room-heating prompts bias toward `cookstoves-indoor-heating-safety`
   - dizziness, headache, suspected carbon monoxide, and "get out first or ventilate first" prompts bias toward `smoke-inhalation-carbon-monoxide-fire-gas-exposure`
   - whole-room stale-air / make-up-air / airflow-design prompts stay on `ventilation-air-systems`
   - passive heat / insulation / thermal-mass prompts stay on `heat-management` only after active combustion danger is ruled out
2. Re-ingests the touched guides.
3. Runs the bounded `wave_bf` prompt pack with a fresh artifact bundle.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` descends from the D46 landing commit if it exists, or the active guide lane is otherwise at a natural stop point.
2. The bounded prompt pack still exists:
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bf_20260417.txt`
3. Current guide-validation flow is available:
   - `scripts/run_guide_prompt_validation.ps1`
4. The target guides are tracked and clean before edit.
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - residual historical root `notes/` backlog remains

## Read set

Read before editing:

- `GUIDE_PLAN.md`
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_bf_20260417.txt`
- `guides/cookstoves-indoor-heating-safety.md`
- `guides/smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`
- `guides/ventilation-air-systems.md`
- `guides/heat-management.md`

Read adjacent guides only if the rerun proves they are directly part of the miss:

- `guides/winter-survival-systems.md`
- `guides/toxic-gas-identification-detection.md`

## Likely touch set

Primary expected edits:

- `guides/cookstoves-indoor-heating-safety.md`
- `guides/smoke-inhalation-carbon-monoxide-fire-gas-exposure.md`
- `guides/ventilation-air-systems.md`
- `guides/heat-management.md`

Touch only if the rerun proves needed:

- `guides/winter-survival-systems.md`
- `guides/toxic-gas-identification-detection.md`

Validation outputs:

- normal re-ingest artifacts from the repo flow
- one fresh `artifacts/bench/...` bundle for `wave_bf`

## Diagnosis target

Form the smallest routing-hardening hypothesis that explains the pack:

1. active smoke-back / stove troubleshooting prompts should land on `cookstoves-indoor-heating-safety`
2. headache, dizziness, suspected carbon monoxide, or "leave first" prompts must fast-track to `smoke-inhalation-carbon-monoxide-fire-gas-exposure`
3. building-air / make-up-air / stale-room prompts should stay on `ventilation-air-systems` when nobody is actively exposed
4. passive heating or shelter-heat-design prompts should not steal active indoor-combustion safety prompts
5. do not widen unless the rerun directly proves one adjacent guide is the blocker

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** turn this into a broad heating-systems or building-services project.
- Do **not** add new guides.
- Do **not** touch Android/app code in this slice.
- Do **not** widen beyond the named heating / indoor-air boundary unless the rerun directly proves one adjacent guide is the blocker.

## Editing rules

### Step 1 - read-only diagnosis

Use the current prompt pack and guide surfaces to isolate the narrowest fix:

1. complaint-first quick-routing blocks near the top
2. stronger aliases for smoke-back, indoor-air danger, headache/dizziness near heating, and leave-versus-ventilate wording
3. clearer reciprocal links / scope boundaries among stove troubleshooting, medical exposure response, ventilation design, and passive heat design

### Step 2 - make only bounded retrieval-hardening edits

Allowed edit patterns:

1. complaint-first routing bullets near the top
2. stronger natural-language hooks / aliases for the exact `wave_bf` prompt shapes
3. clearer boundary language between `cookstoves-indoor-heating-safety`, `smoke-inhalation-carbon-monoxide-fire-gas-exposure`, `ventilation-air-systems`, and `heat-management`

Not allowed in this slice:

1. broad stove or heating-system rewrite
2. new medical protocol expansion beyond routing / triage surfacing
3. Android/runtime changes
4. sweeping edits across unrelated fire, toxicology, or winter guides without direct rerun evidence

### Step 3 - re-ingest

Re-ingest after the guide edits using the repo's normal ingest flow before trusting retrieval results.

### Step 4 - bounded validation

Run the existing guide-direction validation for:

- `wave_bf`

Write a fresh artifact bundle under `artifacts/bench/`.

If the validation environment is unavailable, report the exact blocker rather than widening into infrastructure repair.

## Acceptance

- One commit only.
- Guide edits stay within the named heating / indoor-air guides unless the rerun proves one adjacent guide is directly needed.
- The change remains routing-hardening / boundary-polish, not a broad heating or toxicology buildout.
- Re-ingest was run after guide edits.
- A fresh `wave_bf` validation artifact bundle exists.
- Final report clearly states whether stove-troubleshooting prompts, CO/smoke-danger prompts, and ventilation/passive-heat prompts route more cleanly after the patch.

## Commit

Single commit only. Suggested subject:

```text
D47: harden wave_bf indoor-air heating routing
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose `wave_bf`, make bounded guide edits, re-ingest, rerun `wave_bf`, commit once.
- If the rerun shows winter-survival or toxic-gas guide bleed outside the named set, stop and report that evidence rather than absorbing a broader heating / toxicology bundle into this slice.

## Anti-recommendations

- Do **not** turn this into a winter-survival mega-slice.
- Do **not** add new guides.
- Do **not** mix in app or retrieval-code changes.

## Report format

- Commit sha + subject.
- Which guide files changed.
- Re-ingest command/result.
- Validation artifact path.
- Short statement on stove-troubleshooting routing, indoor-air danger routing, and whether any adjacent guide actually needed touching.
