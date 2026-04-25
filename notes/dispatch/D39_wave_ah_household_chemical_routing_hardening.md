# Slice D39 - wave_ah household chemical routing hardening

- **Role:** main agent (`gpt-5.4 xhigh`). Guide-quality micro-slice; safe to delegate to a `gpt-5.4 high` worker after D38 finishes.
- **Paste to:** **new window** after D38 lands or is explicitly abandoned.
- **Parallel with:** avoid any parallel writer touching the same chemical / poisoning / eye-injury guide files or the same `wave_ah` output directory.
- **Why this slice now:** After D37, the next highest-leverage guide family is the household-chemical / poisoning boundary. `GUIDE_PLAN.md` already names household chemicals and cleaning as the next high-value retrieval-hardening family, and the prior `wave_ah` run still showed weak objective coverage on toddler-cleaner and bleach-eye prompts plus solvent-fume bleed into irrelevant support.

## Outcome

One focused commit that:

1. Strengthens complaint-first routing for household chemical exposure prompts:
   - cleaner ingestion / toddler cleaner-licking
   - bleach or cleaner eye splash
   - paint thinner / solvent fume sickness
2. Keeps those prompts anchored on poisoning / toxicology / eye-injury guidance before generic chemical handling, lab safety, or unrelated household support.
3. Re-ingests the touched guides and reruns the bounded `wave_ah` prompt pack with a fresh artifact bundle.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` descends from `e4373d9` (`D37: harden wave_ag dental-family routing`) or later.
2. D38 is no longer actively editing the worktree. Do **not** overlap this slice with in-flight app-side edits from another worker.
3. The bounded prompt pack still exists:
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ah_20260415.txt`
4. Current guide-validation flow is available:
   - `scripts/run_guide_prompt_validation.ps1`
5. Existing validation infra may be used operationally but not edited:
   - generation host at `http://127.0.0.1:1235/v1`
   - embedding host at `http://127.0.0.1:8801/v1` if reachable or startable via existing repo tooling
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - residual historical root `notes/` backlog remains

## Read set

Read before editing:

- `GUIDE_PLAN.md`
- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ah_20260415.txt`
- `artifacts/bench/guide_wave_ah_20260416_212125.md`
- `guides/chemical-safety.md`
- `guides/toxicology-poisoning-response.md`
- `guides/unknown-ingestion-child-poisoning-triage.md`
- `guides/eye-injuries-emergency-care.md`

## Likely touch set

Primary expected edits:

- `guides/chemical-safety.md`
- `guides/toxicology-poisoning-response.md`
- `guides/unknown-ingestion-child-poisoning-triage.md`
- `guides/eye-injuries-emergency-care.md`

Validation outputs:

- normal re-ingest artifacts from the repo flow
- one fresh `artifacts/bench/...` bundle for `wave_ah`

## Diagnosis target

Form the smallest routing-hardening hypothesis that explains the family:

1. `my toddler licked bathroom cleaner off the counter` should land on caregiver-facing poisoning triage immediately.
2. `bleach splashed in my eye while cleaning and it still burns after rinsing` should land on eye chemical-burn care, not lab-safety framing.
3. `I inhaled paint thinner fumes in the shed and now feel sick` should land on solvent / toxic exposure guidance, not photography or unrelated technical support.
4. `my dog ate rat poison and I do not know what kind it was` is only a poisoning-boundary signal in this slice; do not widen into veterinary content.
5. `I get a headache every time we run the stove indoors for heat` already looks acceptable; do not reopen the stove / CO family unless a rerun proves a new regression.

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** touch Android/app code, `mobile_pack.py`, `query.py`, or `OfflineAnswerEngine` in this slice.
- Do **not** edit `scripts/run_guide_prompt_validation.ps1` or other harness code unless validation becomes impossible and you need to report the blocker.
- Do **not** widen into a general toxicology rewrite.
- Do **not** turn the dog / rat-poison prompt into a veterinary guide project.
- Do **not** reopen cookstove / smoke / CO guides unless the rerun shows an actual new miss.
- Do **not** add new guides.

## Editing rules

### Step 1 - read-only diagnosis

Use the prior `wave_ah` artifact plus the current guide surfaces to identify the narrowest fix:

1. complaint-first quick-routing blocks near the top
2. stronger aliases for toddler cleaner-licking, bleach-eye splash, and solvent fume sickness
3. clearer reciprocal handoffs among chemical safety, toxicology poisoning response, unknown ingestion, and eye injuries
4. stronger "not this / go there" boundary language to reduce unrelated support bleed

### Step 2 - make only bounded retrieval-hardening edits

Allowed edit patterns:

1. complaint-first routing bullets near the top
2. stronger natural-language hooks / aliases for the exact prompt shapes
3. clearer boundary language between generic chemical handling versus active exposure care
4. reciprocal link tightening across the four named guides

Not allowed in this slice:

1. broad procedural toxicology expansion
2. substance-by-substance overhaul
3. app/runtime/harness edits
4. unrelated household guide edits

### Step 3 - re-ingest

Re-ingest after the guide edits using the repo's normal ingest flow before trusting retrieval results.

### Step 4 - bounded validation

Run the existing guide-direction validation for:

- `wave_ah`

Write a fresh artifact bundle under `artifacts/bench/`.

Operational note:

- You may use the existing split-host validation setup and start the existing FastEmbed host if needed, but do **not** modify validation scripts in this slice.
- If the environment is unavailable even after using the existing tooling, report the exact blocker rather than widening into infrastructure repair.

## Acceptance

- One commit only.
- Guide edits stay within the four named guides.
- The change remains retrieval-hardening / complaint-first routing, not a broad toxicology rewrite.
- Re-ingest was run after guide edits.
- A fresh `wave_ah` validation artifact bundle exists.
- Final report clearly states whether toddler-cleaner, bleach-eye, and solvent-fume prompts route more cleanly and whether the stove/CO prompt stayed stable.

## Commit

Single commit only. Suggested subject:

```text
D39: harden wave_ah household chemical routing
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose `wave_ah`, make bounded guide edits, re-ingest, rerun `wave_ah`, commit once, and report whether the irrelevant support bleed is gone.
- If the rerun still admits photography / documentation or other non-exposure support after the guide edits, stop and report that as likely desktop rerank follow-up material rather than widening this slice.

## Anti-recommendations

- Do **not** turn this into a veterinary project.
- Do **not** modify desktop retrieval code in the same commit.
- Do **not** touch D38-owned app-side files.
- Do **not** widen into general chemical manufacturing safety beyond the exact complaint-first exposure surfaces.

## Report format

- Commit sha + subject.
- Which guide files changed.
- Re-ingest command/result.
- Validation artifact path.
- Short statement on toddler-cleaner routing, bleach-eye routing, solvent-fume routing, and whether irrelevant support bleed remains.
