# Slice D51 - wave_ez newborn sepsis / sick infant deterministic hardening

- **Role:** main agent (`gpt-5.5 medium`; use high only if newborn sepsis overlaps with broader pediatric distress, postpartum, or poisoning rules).
- **Paste to:** new main-agent window after reading `notes/PLANNER_HANDOFF_2026-04-24_MORNING.md`.
- **Parallel with:** read-only scout is fine. Avoid parallel writers touching `query.py`, `special_case_builders.py`, `deterministic_special_case_registry.py`, `notes/specs/deterministic_registry_sidecar.yaml`, `tests/test_special_cases.py`, or `tests/test_query_routing.py`.
- **Why this slice now:** `EZ` is the next safety family after `EY`. The fresh baseline still sends all six prompts through RAG/generation, and the answers cite the right neonatal danger-sign family but are not stable enough for a safety-critical contract.

## Outcome

One focused desktop deterministic slice that:

1. Adds a bounded newborn sepsis / sick-infant deterministic rule and builder for `wave_ez`.
2. Keeps older-child fever, normal newborn behavior, teething/colic, postpartum-mother, veterinary, and generic respiratory/dehydration lanes out of the newborn sepsis owner.
3. Runs focused tests plus the official `-Wave ez` validation and records the artifact.

## Current Baseline

Latest baseline artifact:

- `artifacts/bench/guide_wave_ez_20260424_092437.md`

Baseline result:

- `6/6` successful
- `0` errors
- `0` cap hits
- all six prompts used `rag`
- `embed_urls=http://127.0.0.1:1234/v1`
- generation used `127.0.0.1:1235/v1`

Prompt pack:

- `newborn is limp, will not feed, is hard to wake, and seems very sick`
- `baby has fever or low temperature and is acting weak`
- `poor feeding with breathing trouble in a newborn`
- `baby is sleepy and hard to wake`
- `newborn looks very sick and weak`
- `is this normal newborn behavior or sepsis`

Observed direct classifier before the fresh baseline:

- all six prompts -> no deterministic result

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

- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ez_20260417.txt`
- `artifacts/bench/guide_wave_ez_20260424_092437.md`
- `query.py`
- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `tests/test_special_cases.py`
- `tests/test_query_routing.py`

Useful existing anchors:

- existing pediatric/sepsis prompt notes in `query.py`
- `special_case_builders.py` infection and pediatric emergency builders for wording style
- registry samples near other medical emergency rules
- `tests/test_special_cases.py` medical emergency deterministic tests

## Likely Touch Set

Expected:

- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `tests/test_special_cases.py`
- maybe `query.py` / `tests/test_query_routing.py` if supplemental retrieval or prompt notes are needed

## Implementation Shape

Add a new deterministic rule such as:

- `newborn_sepsis_emergency`

Predicate coverage to consider:

- newborn / neonate / infant / baby context plus limp, floppy, very weak, very sick, hard to wake, difficult to wake, unusually sleepy, poor feeding, will not feed, weak suck, fever, low temperature, cold temperature, breathing trouble, grunting, retractions, blue color, or sepsis uncertainty
- explicit `normal newborn behavior or sepsis` uncertainty
- `baby is sleepy and hard to wake` should route when the prompt is about a baby/newborn and hard-to-wake behavior, but should not swallow ordinary older-child tiredness prompts

Builder contract:

1. Newborns can become dangerously ill from sepsis, dehydration, breathing trouble, or temperature instability with nonspecific signs; limp/floppy behavior, hard-to-wake behavior, poor feeding, fever, low temperature, weak appearance, or breathing trouble is urgent.
2. First action is emergency medical help / urgent neonatal evaluation / urgent evacuation now. Do not wait to observe at home when those red flags are present.
3. Check breathing, color, temperature, alertness, feeding, wet diapers/urine, rash, vomiting, and umbilical infection signs while help is arranged.
4. Keep the baby warm, positioned safely, and with a watcher. If not breathing normally, begin infant CPR/rescue breathing if trained and call for emergency help.
5. Do not force feeds, fluids, medicines, fever routines, or home remedies into a baby who is hard to wake, breathing poorly, vomiting, or unsafe to swallow.
6. Avoid improvised dosing/antibiotic specifics; focus on escalation and what to report.

Likely citations from the baseline answer family:

- `GD-492` newborn danger signs / neonatal emergencies
- `GD-298` pediatric emergency support
- `GD-232` airway/positioning emergency basics
- optionally `GD-916` for baby danger signs if the builder needs feeding/hydration language

## False-Positive Guards

Near misses to preserve/add:

- normal newborn sleepiness when the baby wakes, feeds, breathes normally, has normal color, and has no fever/low temperature
- colic, teething, drooling, diaper rash, or fussiness with normal feeding/alertness
- older child or adult fever/sepsis prompts should use existing non-newborn owners
- postpartum mother infection/bleeding should not route to newborn sepsis
- animal/veterinary newborn prompts should not route to human neonatal sepsis unless explicitly intended
- poison ingestion, choking, anaphylaxis, or trauma should keep their specific owners when those clues dominate

## Validation

Focused checks:

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_routing_predicates.py deterministic_special_case_registry.py special_case_builders.py tests\test_special_cases.py tests\test_query_routing.py tests\test_regenerate_deterministic_registry.py
.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss tests.test_regenerate_deterministic_registry -v
.\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Prompt validation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave ez -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

Expected final artifact shape:

- `6/6`
- `0` errors
- `0` cap hits
- all six prompts deterministic
- no generation calls after retrieval/build
- report confirms `embed_urls=http://127.0.0.1:1234/v1`

## Acceptance

- Fresh `wave_ez` artifact under `artifacts/bench/`.
- Focused unit suite passes.
- Deterministic registry validator passes.
- Handoff/continuation note records the slice outcome.

## Delegation Hints

- Good scout task: inspect existing pediatric/newborn guide IDs from the baseline and confirm the safest citation set.
- Good worker task: implement the new rule/builder/tests only after main confirms newborn-specific ownership.

## Anti-Recommendations

- Do not fold this into generic `infection_delirium` or generic pediatric respiratory distress; the age-specific newborn threshold is the point of the slice.
- Do not add antibiotic, dosing, temperature-treatment, or feeding instructions beyond safe escalation/support.
- Do not edit guide prose unless deterministic routing proves impossible.
- Do not touch Android parity in this slice.

## Report Format

- Files changed.
- Rule id / builder added.
- Focused test result.
- `wave_ez` artifact path.
- Any false-positive guard that required special handling.
