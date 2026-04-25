# Planner Handoff - wave_be residue narrowed, D49 landed (afternoon 2026-04-23)

Written by outgoing Codex planner for the next planner/self.

This handoff picks up after the morning chain. The afternoon result is mixed in the useful way: one guide family landed cleanly, one validation-infra slice landed cleanly, and the failed medical slices were narrowed enough that the next move is now obvious and small instead of vague.

Written: 2026-04-23 afternoon local.

---

## Current head

`HEAD` is now:

- `e122086` `D49: classify litert context overflow in guide validation`

Recent landed chain in this handoff window:

- `96e0d10` `D45: polish wave_y schoolhouse accessibility routing`
- `e122086` `D49: classify litert context overflow in guide validation`

## What landed cleanly

### D45

- `wave_y` landed correctly
- schoolhouse layout/setup prompts now lead with `education-system-design`
- accessibility prompts now lead with `accessible-shelter-design`
- no adjacent facility guide needed touching

Validation artifacts:

- `artifacts/bench/guide_wave_y_20260423_125308.md`
- `artifacts/bench/guide_wave_y_20260423_125308.json`

### D49

- LiteRT request-time token-limit failures are now classified as `context_overflow`, not transient host flake
- guide validation no longer retries the known 4096-cap overflow case
- tests passed:
  - `python -m unittest tests.test_lmstudio_utils tests.test_run_guide_prompt_validation_harness -v`

Touched and landed:

- `lmstudio_utils.py`
- `tests/test_lmstudio_utils.py`
- `tests/test_run_guide_prompt_validation_harness.py`

## What did not land, and why that was correct

### D46 - guide-only `wave_be` pass

The guide-only symptom-routing pass did **not** land, and that was the right call.

What improved:

- chest pain after exertion stayed/emerged correctly emergency-routed
- stroke-style prompt stayed deterministic to `first-aid`
- medications wording became more clearly downstream

What did not clear:

- cough still first-landed on the focused cough guide
- soap-rash still first-landed on the focused rash guide
- one validation run also exposed LiteRT context-window instability

Key artifact:

- `artifacts/bench/guide_wave_be_20260423_130540.md`
- `artifacts/bench/guide_wave_be_20260423_130540.json`

### D48 - query/retrieval follow-up on `wave_be`

The retrieval follow-up also did **not** land, but it narrowed the residue again.

What improved:

- urinary burning first-hop improved to `common-ailments`
- fever/body-aches improved to `common-ailments`
- emergency prompts stayed correct

What still did not clear:

- `This cough will not go away. Which guide should I start with?`
- `I got a rash after using a new soap. Is this just a mild reaction or something urgent?`

Important nuance:

- the remaining blocker is **not only** validation-lane overflow
- the earlier clean rerun already showed real cough/rash first-hop misses
- the later rerun also showed prompt-budget/context-overflow issues on `#3` and `#4`

Comparison artifacts:

- `artifacts/bench/guide_wave_be_20260423_131816.md`
- `artifacts/bench/guide_wave_be_20260423_131816.json`
- `artifacts/bench/guide_wave_be_20260423_132029.md`
- `artifacts/bench/guide_wave_be_20260423_132029.json`

## Most important strategic read

The pattern matched the good failure mode from earlier slices:

- guide-only was not enough
- the next retrieval/code pass helped but still exposed a very narrow residue
- the remaining miss is now small enough that the next move should be a **query.py-only** cleanup, not another broad guide-family pass

That means the right owner for the next critical-path slice is desktop retrieval logic, not more guide churn.

## Recommended next move

Do **one more query.py-only slice** for `wave_be` before touching `D47`.

Target prompts:

1. `This cough will not go away. Which guide should I start with?`
2. `I got a rash after using a new soap. Is this just a mild reaction or something urgent?`
3. `It burns when I pee. Is this home-care first or something more urgent?`
   Keep this in scope because it is still tied to prompt-budget / rerank stability even though the first-hop looked better in one rerun.

Minimal owned file set:

- `query.py`
- `tests/test_query_routing.py`

Goal:

- keep `common-ailments-recognition-care` as the complaint-first first hop for broad mild-vs-urgent symptom-routing questions
- preserve the already-correct emergency routing
- avoid reopening the guide files unless fresh evidence proves query-only is insufficient

## Why D47 should wait

`D47` is already drafted here:

- `notes/dispatch/D47_wave_bf_indoor_air_heating_routing.md`

But it should wait until the `wave_be` medical complaint-first residue is either:

- cleared by the query-only slice, or
- explicitly parked by Tate

Reason:

- the current blocker is isolated and still on the critical path
- moving to `wave_bf` first would leave an unresolved medical-routing residue behind

## Worktree truth after cleanup

I am intentionally **not** handing off the failed `D46` / `D48` edits as live tracked changes.

Those unlanded edits in:

- `guides/common-ailments-recognition-care.md`
- `guides/first-aid.md`
- `guides/medications.md`
- `query.py`
- `tests/test_query_routing.py`

should be treated as exploratory residue, not in-flight truth.

This handoff assumes they have been rolled out of the tracked worktree before wrap-up so the next agent starts from landed head plus artifact evidence, not half-state local modifications.

Unrelated expected dirt still exists and is **not** the bottleneck:

- `AGENTS.md` modified
- `opencode.json` modified
- `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
- `LM_STUDIO_MODELS_20260410.json` untracked
- `4-13guidearchive.zip` untracked
- large historical untracked `notes/` backlog
- many untracked dispatch notes under `notes/dispatch/`

Do not spend the next turn cleaning that unless Tate explicitly asks.

## Agent state

All helper subagents used for this handoff were closed at wrap-up.

No slice is currently in flight after cleanup.

## Compact command memory

- D45 ingest used:
  - `py ingest.py --files guides/education-system-design.md guides/accessible-shelter-design.md guides/education-teaching.md`
- D45 validation used:
  - `.\scripts\run_guide_prompt_validation.ps1 -Wave y -PythonPath <python> -GenerationUrl http://127.0.0.1:1235/v1 -EmbedUrl http://127.0.0.1:8801/v1`
- D48 tests used:
  - `py -3 -m unittest tests.test_query_routing -v`
- D48 ingest used:
  - `py -3 ingest.py --files guides/common-ailments-recognition-care.md guides/first-aid.md guides/medications.md`

## Prompt for next planner/main

Use this as the next-turn brief:

```text
Pick up from HEAD e122086 (D49 landed). D45 is landed and good. D46 and D48 both failed correctly and should not be treated as landed work.

Current critical-path residue is a narrow wave_be medical complaint-first routing miss. Do one more query.py-only slice before D47.

Owned files:
- query.py
- tests/test_query_routing.py

Target prompts:
1. This cough will not go away. Which guide should I start with?
2. I got a rash after using a new soap. Is this just a mild reaction or something urgent?
3. It burns when I pee. Is this home-care first or something more urgent?

Requirements:
- keep emergency routing intact
- keep common-ailments as the first hop for broad mild-vs-urgent symptom-start questions
- do not reopen the medical guide files unless fresh evidence proves query-only is insufficient
- rerun bounded wave_be validation after the query.py change

Important context:
- D49 already fixed LiteRT overflow classification; do not confuse context_overflow with guide-quality failure
- D47 (wave_bf indoor-air/heating) is drafted but should wait until this wave_be residue is cleared or explicitly parked

Key evidence:
- artifacts/bench/guide_wave_be_20260423_131816.md
- artifacts/bench/guide_wave_be_20260423_132029.md
- notes/dispatch/D47_wave_bf_indoor_air_heating_routing.md
```

## Final note to next self

The win here is that the repo is still in the same healthy rhythm as the morning:

- ship the narrow thing
- stop when the artifact says the layer is wrong
- convert the residue into a smaller, truer next move

Right now that next move is small and obvious again. Keep it that way.
