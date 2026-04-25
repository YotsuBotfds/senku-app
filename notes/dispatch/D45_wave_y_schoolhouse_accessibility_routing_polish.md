# Slice D45 - wave_y schoolhouse/accessibility routing polish

- **Role:** main agent (`gpt-5.4 xhigh`). Guide-quality micro-slice; safe to delegate to a `gpt-5.4 high` worker after D44 lands.
- **Paste to:** **new window** only after D44 lands cleanly or the app-side Android queue has reached a natural pause.
- **Parallel with:** avoid any parallel writer touching the same education/accessibility guide files or the same `wave_y` validation output directory.
- **Why this slice now:** Once the current Android cleanup chain settles, the next named guide-validation family is already clear in the plan: schoolhouse/accessibility routing. `wave_y` is a tight pack for exactly that surface, and it is more bounded than opening a broader facility-infrastructure bundle.

## Outcome

One focused commit that:

1. Improves routing for schoolhouse/classroom layout versus accessibility prompts so:
   - schoolhouse setup / one-room layout prompts bias toward `education-system-design`
   - access-barrier / mobility / entrance / circulation prompts bias toward `accessible-shelter-design`
2. Re-ingests the touched guides.
3. Runs the bounded `wave_y` prompt pack with a fresh artifact bundle.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` descends from the D44 landing commit if it exists, or the active app-side cleanup chain is otherwise at a natural stop point.
2. The bounded prompt pack still exists:
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt`
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
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_y_20260413.txt`
- `guides/education-system-design.md`
- `guides/accessible-shelter-design.md`
- `guides/education-teaching.md`

Read adjacent facility guides only if the rerun proves they are directly part of the miss:

- `guides/clinic-facility-basics.md`
- `guides/marketplace-trade-space-basics.md`
- `guides/settlement-layout-growth-planning.md`

## Likely touch set

Primary expected edits:

- `guides/education-system-design.md`
- `guides/accessible-shelter-design.md`
- `guides/education-teaching.md`

Touch only if the rerun proves needed:

- `guides/clinic-facility-basics.md`
- `guides/marketplace-trade-space-basics.md`
- `guides/settlement-layout-growth-planning.md`

Validation outputs:

- normal re-ingest artifacts from the repo flow
- one fresh `artifacts/bench/...` bundle for `wave_y`

## Diagnosis target

Form the smallest routing-hardening hypothesis that explains the pack:

1. schoolhouse setup / one-room classroom layout prompts should land on `education-system-design`
2. accessibility prompts about entrance, ramps, thresholds, or circulation should land on `accessible-shelter-design`
3. teaching-method or participation-access prompts may need `education-teaching` as the support boundary
4. do not widen unless the actual `wave_y` rerun proves one adjacent facility guide is directly causing the miss

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** turn this into a broad facility-infrastructure project.
- Do **not** add new guides.
- Do **not** touch Android/app code in this slice.
- Do **not** widen beyond schoolhouse/accessibility routing unless the rerun directly proves one adjacent facility guide is the blocker.

## Editing rules

### Step 1 - read-only diagnosis

Use the current prompt pack and guide surfaces to isolate the narrowest fix:

1. complaint-first quick-routing blocks near the top
2. stronger aliases for schoolhouse layout versus accessibility barriers
3. clearer reciprocal links / scope boundaries between school structure, access design, and teaching

### Step 2 - make only bounded retrieval-hardening edits

Allowed edit patterns:

1. complaint-first routing bullets near the top
2. stronger natural-language hooks / aliases for the exact `wave_y` prompt shapes
3. clearer boundary language between `education-system-design`, `accessible-shelter-design`, and `education-teaching`

Not allowed in this slice:

1. broad education-system rewrite
2. new facility guide cluster
3. Android/runtime changes
4. sweeping edits across unrelated facility guides without direct rerun evidence

### Step 3 - re-ingest

Re-ingest after the guide edits using the repo’s normal ingest flow before trusting retrieval results.

### Step 4 - bounded validation

Run the existing guide-direction validation for:

- `wave_y`

Write a fresh artifact bundle under `artifacts/bench/`.

If the validation environment is unavailable, report the exact blocker rather than widening into infrastructure repair.

## Acceptance

- One commit only.
- Guide edits stay within the named education/accessibility guides unless the rerun proves one adjacent facility guide is directly needed.
- The change remains routing-hardening / boundary-polish, not a broad facility buildout.
- Re-ingest was run after guide edits.
- A fresh `wave_y` validation artifact bundle exists.
- Final report clearly states whether schoolhouse layout and accessibility prompts route more cleanly after the patch.

## Commit

Single commit only. Suggested subject:

```text
D45: polish wave_y schoolhouse accessibility routing
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose `wave_y`, make bounded guide edits, re-ingest, rerun `wave_y`, commit once.
- If the rerun shows facility-guide bleed outside the named school/accessibility set, stop and report that evidence rather than absorbing a broad facility bundle into this slice.

## Anti-recommendations

- Do **not** turn this into a clinic/market/settlement mega-slice.
- Do **not** add new guides.
- Do **not** mix in app or retrieval-code changes.

## Report format

- Commit sha + subject.
- Which guide files changed.
- Re-ingest command/result.
- Validation artifact path.
- Short statement on schoolhouse-layout routing, accessibility-routing, and whether any adjacent facility guide actually needed touching.
