# Slice D46 - wave_be symptom-first medical routing

- **Role:** main agent (`gpt-5.4 xhigh`). Guide-quality micro-slice; safe to delegate to a `gpt-5.4 high` worker after D45 lands.
- **Paste to:** **new window** only after D45 lands cleanly or the active Android chain reaches a natural pause.
- **Parallel with:** avoid any parallel writer touching the same medical guide files or the same `wave_be` validation output directory.
- **Why this slice now:** After the schoolhouse/accessibility pass, the guide plan already names symptom-first medical routing as the next high-leverage guide family. `wave_be` is a tight pack for the exact boundary between everyday self-care, urgent red flags, and medication handoff.

## Outcome

One focused commit that:

1. Improves symptom-first routing so:
   - chest pain / stroke-like complaints route to `first-aid`
   - urinary burning / cough / mild rash / fever-body-ache complaints start in `common-ailments-recognition-care` unless urgent features are present
   - medication-choice / dosing / side-effect questions remain handed off to `medications`
2. Re-ingests the touched guides.
3. Runs the bounded `wave_be` prompt pack with a fresh artifact bundle.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` descends from the D45 landing commit if it exists, or the active app-side chain is otherwise at a natural stop point.
2. The bounded prompt pack still exists:
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_be_20260417.txt`
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
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_be_20260417.txt`
- `guides/common-ailments-recognition-care.md`
- `guides/first-aid.md`
- `guides/medications.md`

Read focused symptom guides only if the rerun proves one is directly causing the miss.

## Likely touch set

Primary expected edits:

- `guides/common-ailments-recognition-care.md`
- `guides/first-aid.md`
- `guides/medications.md`

Touch only if the rerun proves needed:

- one or two focused symptom guides directly implicated by the `wave_be` artifact

Validation outputs:

- normal re-ingest artifacts from the repo flow
- one fresh `artifacts/bench/...` bundle for `wave_be`

## Diagnosis target

Form the smallest routing-hardening hypothesis that explains the pack:

1. chest-pain and stroke-sign prompts must fast-track to `first-aid`
2. burning-urination, lingering cough, mild soap-rash, and fever/body-aches prompts should start in `common-ailments-recognition-care` unless a real red flag is present
3. `medications` should stay a downstream handoff for dosing / drug-choice / side-effect detail, not steal the first landing for symptom recognition prompts

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** turn this into a broad medical content expansion.
- Do **not** add new guides.
- Do **not** touch Android/app code in this slice.
- Do **not** widen beyond the symptom-routing boundary unless the rerun directly proves one focused symptom guide is the blocker.

## Editing rules

### Step 1 - read-only diagnosis

Use the current pack and guide surfaces to isolate the narrowest fix:

1. complaint-first quick-routing blocks near the top
2. stronger aliases for exact `wave_be` prompt shapes
3. clearer boundary language between `common-ailments-recognition-care`, `first-aid`, and `medications`

### Step 2 - make only bounded retrieval-hardening edits

Allowed edit patterns:

1. complaint-first routing bullets near the top
2. stronger natural-language hooks / aliases for the exact symptom shapes
3. clearer reciprocal links / handoffs among the three named guides

Not allowed in this slice:

1. broad medical-protocol rewrite
2. substance-by-substance medication expansion
3. Android/runtime edits
4. wide edits across many symptom guides without direct rerun evidence

### Step 3 - re-ingest

Re-ingest after the guide edits using the repo’s normal ingest flow before trusting retrieval results.

### Step 4 - bounded validation

Run the existing guide-direction validation for:

- `wave_be`

Write a fresh artifact bundle under `artifacts/bench/`.

If the validation environment is unavailable, report the exact blocker rather than widening into infrastructure repair.

## Acceptance

- One commit only.
- Guide edits stay within the three named guides unless the rerun proves one focused symptom guide is directly needed.
- The change remains routing-hardening / boundary-polish, not a broad medical expansion.
- Re-ingest was run after guide edits.
- A fresh `wave_be` validation artifact bundle exists.
- Final report clearly states whether chest-pain/stroke prompts route more cleanly to `first-aid`, whether everyday symptom prompts stay in `common-ailments`, and whether `medications` stayed a downstream handoff.

## Commit

Single commit only. Suggested subject:

```text
D46: harden wave_be symptom-first medical routing
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose `wave_be`, make bounded guide edits, re-ingest, rerun `wave_be`, commit once.
- If the rerun shows one focused symptom guide is directly interfering, report that evidence clearly before widening beyond the three named guides.

## Anti-recommendations

- Do **not** turn this into a broad medical overhaul.
- Do **not** add new guides.
- Do **not** mix in app or retrieval-code changes.

## Report format

- Commit sha + subject.
- Which guide files changed.
- Re-ingest command/result.
- Validation artifact path.
- Short statement on emergency symptom routing, everyday symptom routing, and medication handoff after the patch.
