# Slice D40 - desktop household chemical rerank follow-up

- **Role:** main agent (`gpt-5.4 xhigh`). Desktop retrieval/rerank micro-slice; good fit for a `gpt-5.4 high` worker.
- **Paste to:** **new window** after confirming the failed D39 guide-only edits are not present in the worktree.
- **Parallel with:** avoid any parallel writer touching `query.py`, `tests/test_query_routing.py`, or the same `wave_ah` / `wave_ai` validation output directory.
- **Why this slice now:** D39 proved the remaining miss is not missing guide language. The fresh `wave_ah` rerun improved toddler-cleaner routing, but `bleach-eye` still cited lab-safety / toxicology support instead of the eye guide, and `solvent-fume` still admitted cookstove support. That is a desktop retrieval/citation-selection problem now, not another safe guide-only pass.

## Outcome

One focused commit that:

1. Tightens desktop retrieval/rerank for household-chemical prompts so:
   - bleach / cleaner eye-splash prompts favor eye-injury support over lab-safety support
   - paint thinner / solvent fume sickness favors toxicology / chemical-exposure support over cookstove or unrelated technical support
   - mixed-cleaner chest-tightness stays on poisoning / chemical exposure support
2. Leaves guide markdown unchanged.
3. Reruns the bounded household-chemical prompt packs with fresh artifact proof.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` descends from `ba57aad93e133c176f441069fdd96c3dd4c8e842` (`D38: harden Android water storage vs distribution routing`) or later.
2. The failed D39 guide-only edits are **not** present in the worktree. This slice must start from clean guides.
3. The D39 evidence artifact exists and is readable:
   - `artifacts/bench/guide_wave_ah_20260423_090724.md`
   - `artifacts/bench/guide_wave_ah_20260423_090724.json`
4. The relevant validation pack files still exist:
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ah_20260415.txt`
   - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ai_20260415.txt`
5. Current guide-validation flow is available:
   - `scripts/run_guide_prompt_validation.ps1`
6. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - residual historical root `notes/` backlog remains

## Read set

Read before editing:

- `artifacts/bench/guide_wave_ah_20260423_090724.md`
- `artifacts/bench/guide_wave_ah_20260423_090724.json`
- `artifacts/bench/guide_wave_ai_20260416_212435.md`
- `query.py`
- `tests/test_query_routing.py`

Focus especially on the current household-chemical retrieval helpers and rerank deltas.

## Likely touch set

Primary expected edits:

- `query.py`
- `tests/test_query_routing.py`

Validation outputs:

- one fresh `artifacts/bench/...` bundle for `wave_ah`
- one fresh `artifacts/bench/...` bundle for `wave_ai`

## Diagnosis target

Use the D39 artifact as the source of truth for the smallest remaining failure:

1. `bleach splashed in my eye while cleaning and it still burns after rinsing`
   - action text is broadly safe, but retrieval/citations still admit lab-safety / generic toxicology support ahead of the eye guide
2. `I inhaled paint thinner fumes in the shed and now feel sick`
   - action text is broadly safe, but cookstove support still bleeds in
3. `I mixed cleaners and now my chest feels tight and I am coughing`
   - should remain anchored on chemical exposure / poison-control support
4. `my toddler licked a cleaning product...`
   - already improved; keep it stable
5. `I get a headache every time we run the stove indoors for heat`
   - already stable; do not regress it

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** edit guide markdown in this slice.
- Do **not** touch Android/app code, `mobile_pack.py`, or `OfflineAnswerEngine`.
- Do **not** edit validation scripts or infra unless they are completely unusable and you need to report a blocker.
- Do **not** widen into a general retrieval overhaul.
- Do **not** reopen unrelated medical, smoke, or toxicology families outside the exact household-chemical prompt shapes.

## Editing rules

### Step 1 - read-only diagnosis

Determine the narrowest query-layer fix:

1. rerank penalties/bonuses for household-chemical prompts
2. support-category filtering for chemical eye exposure versus generic lab safety
3. distractor suppression for solvent-fume prompts that currently admit cookstove / non-exposure support
4. supplemental retrieval spec adjustment only if rerank alone cannot explain the failure

### Step 2 - make the smallest desktop change

Good changes:

1. tighten metadata/rerank deltas for household-chemical hazard prompts
2. promote eye-injury / chemical-exposure support when the prompt is explicitly eye-first
3. penalize cookstove / smoke / non-exposure distractors for solvent-fume queries
4. add minimal prompt-shape detection only if clearly needed by the artifact evidence

Bad changes:

1. broad prompt-builder rewrite
2. guide edits
3. Android changes
4. unrelated retrieval-family tuning

### Step 3 - validation

Run the narrowest useful proof:

1. targeted Python unit tests covering the new rerank behavior
2. rerun `wave_ah`
3. rerun `wave_ai`

If `wave_ai` hits a transient LiteRT 500 again, report whether the relevant completed prompts still support the tuning, but do not broaden this slice into host-reliability repair.

## Acceptance

- One commit only.
- Edits stay inside `query.py` and targeted desktop tests.
- Guide markdown remains untouched.
- `bleach-eye` no longer prefers lab-safety support over the eye guide.
- `solvent-fume` no longer admits cookstove-style support.
- toddler-cleaner and stove/CO behavior stay stable.
- Fresh `wave_ah` and `wave_ai` artifacts exist and are reported.

## Commit

Single commit only. Suggested subject:

```text
D40: tighten desktop household chemical rerank
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose D39 artifact evidence, make the smallest `query.py` rerank/spec change, add targeted tests, rerun `wave_ah` + `wave_ai`, commit once.
- If the reruns still show the same bleed after a small rerank pass, stop and report the remaining failure rather than widening into guides or Android code.

## Anti-recommendations

- Do **not** mix in the reverted D39 guide edits.
- Do **not** turn this into a general toxicology project.
- Do **not** retune unrelated retrieval families.
- Do **not** touch D38-owned water-routing code.

## Report format

- Commit sha + subject.
- Files changed.
- Tests run + result.
- Validation artifact paths.
- Short statement on bleach-eye routing, solvent-fume routing, mixed-cleaner routing, toddler-cleaner stability, and stove/CO stability after the patch.
