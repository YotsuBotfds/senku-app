# Slice D49 - wave_ex choking / food obstruction deterministic hardening

- **Role:** main agent (`gpt-5.5 medium` is fine; use high only if rule priority or pediatric overlap gets delicate).
- **Paste to:** new main-agent window after reading `notes/PLANNER_HANDOFF_2026-04-24_MORNING.md`.
- **Parallel with:** read-only scout is fine. Avoid parallel writers touching `query.py`, `special_case_builders.py`, `deterministic_special_case_registry.py`, `notes/specs/deterministic_registry_sidecar.yaml`, `tests/test_special_cases.py`, or `tests/test_query_routing.py`.
- **Why this slice now:** `EX` is already partly covered by `generic_choking_help`, but four of six prompts still invoke LiteRT generation and one airway-color prompt currently falls into the respiratory-infection emergency owner. This slice should make direct choking/food-obstruction help deterministic without disturbing retained-foreign-body or respiratory-infection boundaries.

## Outcome

One focused desktop deterministic slice that:

1. Extends the existing `generic_choking_help` direct-choking path to cover `wave_ex` food-obstruction prompts.
2. Keeps post-choking aspiration / retained foreign body on its existing pediatric rule.
3. Prevents silent-cough / airway-obstruction color-change prompts from drifting into `respiratory_infection_distress_emergency`.
4. Runs focused tests plus the official `-Wave ex` validation and records the artifact.

## Current Baseline

Latest baseline artifact:

- `artifacts/bench/guide_wave_ex_20260424_092327.md`

Baseline result:

- `6/6` successful
- `0` errors
- `0` cap hits
- immediate deterministic results: `2`
- generated results: `4`
- `embed_urls=http://127.0.0.1:1234/v1`
- generation used `127.0.0.1:1235/v1`

Prompt pack:

- `food stuck and they cannot speak`
- `child is choking on a grape`
- `silent coughing and blue lips`
- `should I do back blows or Heimlich first`
- `drooling and cannot swallow after a bite of food`
- `is this choking or just panic`

Observed classifier state before the fresh baseline:

- `food stuck and they cannot speak` -> deterministic `generic_choking_help`
- `child is choking on a grape` -> `None`
- `silent coughing and blue lips` -> deterministic `respiratory_infection_distress_emergency` (likely wrong owner for this wave)
- `should I do back blows or Heimlich first` -> `None`
- `drooling and cannot swallow after a bite of food` -> `None`
- `is this choking or just panic` -> `None`

## Preconditions

1. Working Python:
   - `.\.venvs\senku-validate\Scripts\python.exe`
2. Guide harness is available:
   - `scripts/run_guide_prompt_validation.ps1`
3. LM Studio embedding endpoint remains:
   - `http://127.0.0.1:1234/v1`
4. LiteRT generation endpoint remains:
   - `http://127.0.0.1:1235/v1`
5. Android/emulator access is not required for this slice.

## Read Set

Read before editing:

- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ex_20260417.txt`
- `artifacts/bench/guide_wave_ex_20260424_092327.md`
- `query.py`
- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `tests/test_special_cases.py`
- `tests/test_query_routing.py`

Useful existing anchors:

- `query.py` `_is_generic_choking_help_special_case`
- `special_case_builders.py` `_build_generic_choking_help_response`
- registry rule `generic_choking_help`
- `tests/test_special_cases.py` `test_choking_food_obstruction_dp_prompts_route`
- `tests/test_special_cases.py` `test_child_aspirated_foreign_body_ej_prompts_route`
- existing `child_aspirated_foreign_body_emergency` rule/builder for post-choking retained-object patterns
- existing `respiratory_infection_distress_emergency` rule for respiratory-infection red flags

## Likely Touch Set

Expected:

- `query.py`
- `tests/test_special_cases.py`
- maybe `tests/test_query_routing.py`

Possible only if direct classifier review proves the current builder/registry metadata needs adjustment:

- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`

Prefer extending the existing `generic_choking_help` predicate rather than adding a new deterministic rule. Add a separate rule only if direct classifier review shows that broadening `generic_choking_help` would create unacceptable priority or false-positive risk.

## Implementation Shape

Target rule:

- `generic_choking_help`

Likely predicate expansion should cover:

1. Direct food/object obstruction with inability to speak, breathe, cough effectively, cry, or swallow normally.
2. Child choking on food/object such as grape, meat, bread, peanut, toy, bead, or bite of food.
3. Action-choice prompts such as back blows versus Heimlich/abdominal thrusts when choking context is explicit.
4. Blue lips, silent cough, ineffective cough, or cannot make sound when a choking/food-obstruction frame is present.
5. Triage prompts asking whether this is choking versus panic, but only when choking signs or obstruction language are present.

Builder contract:

1. Keep the current airway-emergency framing.
2. Split effective cough/speech from unable to breathe/speak/effective cough.
3. Include back blows plus abdominal thrusts for non-infant conscious severe choking; chest thrusts for pregnant/too-large patients.
4. Preserve no blind finger sweeps and visible-object-only removal.
5. Preserve infant-under-1 wording as a special sequence, not adult Heimlich-style thrusts.
6. Preserve urgent escalation after apparent choking if drooling, noisy breathing, inability to swallow, one-sided wheeze, ongoing cough, blue/gray color, collapse, or abnormal breathing remains.

Do not let `silent coughing and blue lips` route first to respiratory infection; silent or ineffective cough plus blue/gray color is an airway-obstruction pattern for this slice. If the prompt has blue lips without choking, food/object, silent-cough, ineffective-cough, or cannot-speak/cannot-breathe context, keep the existing respiratory distress owner.

## False-Positive Guards

Near misses to preserve/add:

- panic without obstruction, choking signs, food/object event, or inability to speak/breathe/cough effectively
- allergic swelling/anaphylaxis after food, especially hives, face/lip/tongue swelling, wheeze, epinephrine, or known allergen exposure
- asthma, pneumonia, cold/flu, or respiratory infection without food/choking/object onset
- post-choking aspiration or retained foreign body stays `child_aspirated_foreign_body_emergency` when the object may have been inhaled and cough/wheeze/trouble breathing persists after the choking episode
- routine sore throat, painful swallowing, reflux, dry mouth, or chronic dysphagia without sudden food obstruction
- infant under 1 prompts should not receive adult Heimlich-only wording
- CPR-only prompts without choking/obstruction context should stay on their existing CPR/general emergency path
- choking on fuel/chemical ingestion with vomiting/coughing should not steal from hydrocarbon ingestion handling

## Validation

Focused checks:

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_routing_predicates.py deterministic_special_case_registry.py special_case_builders.py tests\test_special_cases.py tests\test_query_routing.py tests\test_regenerate_deterministic_registry.py
.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss tests.test_regenerate_deterministic_registry -v
.\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Prompt validation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave ex -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

Expected final artifact shape:

- `6/6`
- `0` errors
- `0` cap hits
- all six prompts deterministic
- no generation calls after retrieval/build
- report confirms `embed_urls=http://127.0.0.1:1234/v1`

## Acceptance

- Fresh `wave_ex` artifact under `artifacts/bench/`.
- Focused unit suite passes.
- Deterministic registry validator passes.
- `child is choking on a grape`, `should I do back blows or Heimlich first`, and `drooling and cannot swallow after a bite of food` no longer require generation.
- `silent coughing and blue lips` no longer routes to respiratory infection when choking/food-obstruction context is present.
- Handoff/continuation note records the slice outcome.

## Delegation Hints

- Good scout task: compare `guide_wave_ex_20260424_092327.md` against existing `generic_choking_help` and `child_aspirated_foreign_body_emergency` tests, then report exact owner boundaries.
- Good worker task: implement only the predicate/test expansion for `generic_choking_help` if main wants to keep local context small.

## Anti-Recommendations

- Do not edit guide prose for this slice unless deterministic routing proves impossible.
- Do not create a new choking builder unless the existing `generic_choking_help` builder cannot safely satisfy the prompt pack.
- Do not weaken `child_aspirated_foreign_body_emergency`; it owns retained-object / post-choking aspiration patterns.
- Do not mix in `EW` urgent nosebleed, `EY` meningitis, or `EZ` newborn sepsis.
- Do not touch Android parity in this slice; leave parity mirror work for a later explicit Android pass.

## Report Format

- Files changed.
- Rule id / builder changed or added.
- Focused test result.
- `wave_ex` artifact path.
- Any false-positive guard that required special handling.
