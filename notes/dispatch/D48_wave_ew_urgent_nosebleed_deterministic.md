# Slice D48 - wave_ew urgent nosebleed deterministic hardening

- **Role:** main agent (`gpt-5.5 medium` is fine; use high only if overlap/priority behavior gets delicate).
- **Paste to:** new main-agent window after reading `notes/PLANNER_HANDOFF_2026-04-24_MORNING.md`.
- **Parallel with:** read-only scout is fine. Avoid parallel writers touching `query.py`, `special_case_builders.py`, `deterministic_special_case_registry.py`, `tests/test_special_cases.py`, or `tests/test_query_routing.py`.
- **Why this slice now:** `EW` was the first unfinished prompt pack after the 2026-04-24 morning deterministic sweep. It has query/rerank support but no deterministic answer path, so all six prompts still invoke LiteRT generation.

## Outcome

One focused desktop deterministic slice that:

1. Adds a bounded urgent-nosebleed deterministic rule/builder for `wave_ew`.
2. Preserves GI-bleed and major-blood-loss routing boundaries.
3. Runs focused tests plus the official `-Wave ew` validation and records the artifact.

## Current Baseline

Latest baseline artifact:

- `artifacts/bench/guide_wave_ew_20260424_085012.md`

Baseline result:

- `6/6` successful
- `0` errors
- `0` cap hits
- all six prompts used `rag`
- `embed_urls=http://127.0.0.1:1234/v1`
- generation used `127.0.0.1:1235/v1`

Prompt pack:

- `nosebleed will not stop after 20 minutes`
- `blood is pouring from the nose and down the throat`
- `nosebleed after starting blood thinners`
- `dizzy and pale from a nosebleed`
- `should I lean forward or get urgent help`
- `repeated heavy nosebleeds in one day`

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

- `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ew_20260417.txt`
- `artifacts/bench/guide_wave_ew_20260424_085012.md`
- `query.py`
- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `tests/test_special_cases.py`
- `tests/test_query_routing.py`

Useful existing anchors:

- `query.py` `_is_urgent_nosebleed_query`
- `query.py` urgent nosebleed supplemental specs/prompt notes
- `tests/test_query_routing.py` existing urgent-nosebleed detector/spec tests around the safety-wave route checks

## Likely Touch Set

Expected:

- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `tests/test_special_cases.py`
- maybe `tests/test_query_routing.py`

Only touch `query.py` if direct classifier checks show the existing `_is_urgent_nosebleed_query` misses one of the six EW prompts or a needed guard.

## Implementation Shape

Prefer a new deterministic rule such as:

- `urgent_nosebleed_emergency`
- predicate can delegate to or mirror `_is_urgent_nosebleed_query`
- builder should cite `GD-947` and `GD-232` if shock/airway monitoring is included

Builder contract:

1. Keep the person upright and leaning forward.
2. Pinch the soft part of the nose with steady pressure for a full 10-15 minutes without checking.
3. Spit blood out; avoid leaning back or swallowing blood.
4. Arrange urgent medical help for heavy/pouring bleeding, blood down throat, not stopping after 20-30 minutes, blood thinners, dizziness/paleness/weakness, or repeated heavy same-day bleeding.
5. Do not drift into dental, GI bleed, visible-wound direct pressure, head-injury, or routine hydration-first care.

## False-Positive Guards

Near misses to preserve/add:

- coffee-ground vomit / black tarry stool / vomiting blood stays `gi_bleed_emergency`
- severe external bleeding stays `major_blood_loss_shock`
- blood or clear fluid from nose/ear after head injury stays head-injury/skull-base path
- routine mild short nosebleed without red flags need not become deterministic urgent
- dental bleeding/tooth extraction does not route to nosebleed
- "lean forward" outside nosebleed context does not route

## Validation

Focused checks:

```powershell
.\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py query_routing_predicates.py deterministic_special_case_registry.py special_case_builders.py tests\test_special_cases.py tests\test_query_routing.py tests\test_regenerate_deterministic_registry.py
.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss tests.test_regenerate_deterministic_registry -v
.\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Prompt validation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave ew -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

Expected final artifact shape:

- `6/6`
- `0` errors
- `0` cap hits
- all six prompts deterministic
- no generation calls after retrieval/build
- report confirms `embed_urls=http://127.0.0.1:1234/v1`

## Acceptance

- Fresh `wave_ew` artifact under `artifacts/bench/`.
- Focused unit suite passes.
- Deterministic registry validator passes.
- Handoff/continuation note records the slice outcome.

## Delegation Hints

- Good scout task: read `guide_wave_ew_20260424_085012.md`, confirm owner/false positives, and report exact prompt gaps.
- Good worker task: implement only the deterministic rule/builder/tests if main wants to keep local context small.

## Anti-Recommendations

- Do not edit guide prose for this slice unless deterministic routing proves impossible.
- Do not mix in `EX` choking, `EY` meningitis, or `EZ` newborn sepsis.
- Do not touch Android parity in this slice; leave parity mirror work for a later explicit Android pass.

## Report Format

- Files changed.
- Rule id / builder added.
- Focused test result.
- `wave_ew` artifact path.
- Any false-positive guard that required special handling.
