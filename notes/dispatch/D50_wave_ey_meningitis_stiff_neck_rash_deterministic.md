# Slice D50 - wave_ey meningitis / stiff neck / rash deterministic hardening

- **Role:** main agent (`gpt-5.5 medium`; use high only if predicate overlap with sepsis, overdose, head injury, or anaphylaxis gets delicate).
- **Paste to:** new main-agent window after reading `notes/PLANNER_HANDOFF_2026-04-24_MORNING.md`.
- **Parallel with:** read-only scout is fine. Avoid parallel writers touching `query.py`, `special_case_builders.py`, `deterministic_special_case_registry.py`, `tests/test_special_cases.py`, `tests/test_query_routing.py`, or `notes/specs/deterministic_registry_sidecar.yaml`.
- **Why this slice now:** `EY` is the meningitis / meningococcemia safety pack after `EX`. It has one deterministic owner already, but five prompts still invoke LiteRT generation and some generated answers lead with isolation, hydration, or assessment instead of emergency escalation.

## Outcome

One focused desktop deterministic slice that:

1. Extends the existing meningitis / meningococcemia deterministic path for all six `wave_ey` prompts.
2. Keeps true alternative emergency owners intact for head injury, overdose, anaphylaxis, and ordinary rash/viral-fever near misses.
3. Runs focused tests plus the official `-Wave ey` validation and records the artifact.

## Current Baseline

Latest baseline artifact:

- `artifacts/bench/guide_wave_ey_20260424_092407.md`

Baseline result:

- `6/6` successful
- `0` errors
- `0` cap hits
- `1` deterministic prompt: `meningitis_rash_emergency`
- `5` RAG/generated prompts
- `embed_urls=http://127.0.0.1:1234/v1`
- generation used `127.0.0.1:1235/v1`

Prompt pack:

- `fever with stiff neck and headache`
- `child is hard to wake and has a rash`
- `fever, light hurts the eyes, the neck is rigid, and they are vomiting`
- `vomiting with fever and neck stiffness`
- `petechial rash and very sick-looking`
- `is this meningitis or a viral illness`

Observed direct classifier before the fresh baseline:

- `fever with stiff neck and headache` -> `deterministic / meningitis_rash_emergency`
- all other five prompts -> no deterministic result

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

- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ey_20260417.txt`
- `artifacts/bench/guide_wave_ey_20260424_092407.md`
- `query.py`
- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `tests/test_special_cases.py`
- `tests/test_query_routing.py`

Useful existing anchors:

- `query.py` `_is_meningitis_rash_emergency_query`
- `query.py` `_MENINGITIS_RASH_DANGER_MARKERS`
- `query.py` `_MENINGITIS_RASH_NEURO_MARKERS`
- `special_case_builders.py` `_build_meningitis_rash_emergency_response`
- `tests/test_special_cases.py` `test_meningitis_rash_emergency_eb_prompts_route`
- `tests/test_query_routing.py` meningitis metadata/prompt-note tests

## Likely Touch Set

Expected:

- `query.py`
- `special_case_builders.py`
- `tests/test_special_cases.py`
- maybe `tests/test_query_routing.py`

Only touch registry YAML / generated registry if a new rule is truly needed. Preferred path is to extend `meningitis_rash_emergency`.

## Implementation Shape

Prefer extending the existing rule:

- `meningitis_rash_emergency`

Predicate coverage to consider:

- fever plus stiff/rigid neck and headache, vomiting, or photophobia/light sensitivity
- fever plus neck stiffness/rigidity even when phrased as `neck is rigid`
- hard-to-wake child plus rash, especially with fever or very sick appearance
- petechial/non-blanching/purple/dark/bruise-like rash plus very sick-looking, hard to wake, vomiting, confusion, severe headache, or stiff neck
- explicit uncertainty: `meningitis or viral illness`

Builder contract:

1. Treat meningitis, meningococcemia, or sepsis red flags as emergency-first, not routine viral illness or simple rash care.
2. First action is EMS / urgent evacuation / urgent medical evaluation now when fever plus stiff neck, severe headache, vomiting, photophobia, confusion, hard-to-wake behavior, or petechial/non-blanching rash is present.
3. Keep the person watched, resting, and safely positioned; monitor airway, breathing, alertness, rash, neck stiffness, vomiting, temperature, and shock signs.
4. Do not force fluids, pills, or home remedies if sleepy, confused, vomiting, or unsafe to swallow.
5. For the vague `meningitis or viral illness` prompt, give a red-flag discriminator without implying every viral illness is a meningitis emergency.

## False-Positive Guards

Near misses to preserve/add:

- itchy soap rash / mosquito bites with no fever, stiff neck, petechiae, or altered alertness
- hives or throat tightness after medicine/food stays anaphylaxis/allergy
- head injury with hard-to-wake behavior stays head-injury path
- mixed pills/alcohol with hard-to-wake behavior stays overdose path
- migraine or light sensitivity without fever/stiff neck does not route here
- seatbelt/trauma neck pain does not route here
- routine viral fever without stiff neck, danger rash, confusion, or hard-to-wake behavior does not become deterministic emergency

## Validation

Focused checks:

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_routing_predicates.py deterministic_special_case_registry.py special_case_builders.py tests\test_special_cases.py tests\test_query_routing.py tests\test_regenerate_deterministic_registry.py
.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss tests.test_regenerate_deterministic_registry -v
.\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Prompt validation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave ey -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

Expected final artifact shape:

- `6/6`
- `0` errors
- `0` cap hits
- all six prompts deterministic
- no generation calls after retrieval/build
- report confirms `embed_urls=http://127.0.0.1:1234/v1`

## Acceptance

- Fresh `wave_ey` artifact under `artifacts/bench/`.
- Focused unit suite passes.
- Deterministic registry validator passes.
- Handoff/continuation note records the slice outcome.

## Delegation Hints

- Good scout task: inspect the five generated EY answers and confirm which prompts can safely reuse `meningitis_rash_emergency`.
- Good worker task: implement only the predicate/builder/test extension after main confirms the owner.

## Anti-Recommendations

- Do not edit guide prose unless deterministic routing proves impossible.
- Do not mix in `EZ` newborn sepsis; newborn sepsis needs its own owner and false-positive set.
- Do not broaden the rule so all rash + fatigue, all viral-fever, or all headache prompts become deterministic.
- Do not touch Android parity in this slice.

## Report Format

- Files changed.
- Rule id / builder changed.
- Focused test result.
- `wave_ey` artifact path.
- Any false-positive guard that required special handling.
