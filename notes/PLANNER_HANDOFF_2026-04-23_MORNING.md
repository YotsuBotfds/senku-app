# Planner Handoff - app/guide micro-slice chain in good state (morning 2026-04-23)

Written by outgoing Codex planner for the next planner/self.

This handoff covers the chain after the repo-hygiene / tracker cleanup phase, with the goal of getting back to actual app + guide quality work. The key result of this session is that the queue is no longer vague: several narrow product-facing slices landed cleanly, one guide slice failed in a useful way and got converted into a better desktop rerank slice, and the next guide/app slices are already drafted.

Written: 2026-04-23 morning local.

---

## Current head

`HEAD` is now:

- `6ef2310` `D44: tighten Android merge-trust support mix`

Recent landed chain:

- `e4373d9` `D37: harden wave_ag dental-family routing`
- `ba57aad` `D38: harden Android water storage vs distribution routing`
- `2baaf9f` `D40: tighten desktop household chemical rerank`
- `403d64f` `D41: tighten Android water storage answer shaping`
- `a5c1a48` `D42: tighten Android cabin site-selection context mix`
- `a213bce` `D43: tighten Android roof-weatherproofing context mix`
- `6ef2310` `D44: tighten Android merge-trust support mix`

Important non-landing:

- `D39` (`wave_ah` household-chemical guide pass) did **not** land, and that was correct.
  - The guide-only edits improved some routing, but the fresh artifact showed the real remaining miss was desktop retrieval/citation selection.
  - Those unlanded guide edits were reverted cleanly.
  - `D40` is the intentionally correct follow-up that closed the retrieval-side residue.

## What actually improved

### Guides

`D37`:

- complaint-first dental routing is cleaner
- mild gum-bleeding / mild sensitivity / mild toothache now bias harder toward `preventive-dental-hygiene`
- urgent fever + throbbing + night-waking tooth pain now bias harder toward `emergency-dental`
- poisoning / cleaner-ingestion prompts are more clearly routed away from dental guidance

`D40`:

- desktop household-chemical rerank is better
- bleach-eye prompts now prefer the eye-injury guide over lab-safety support
- solvent-fume prompts dropped cookstove bleed
- mixed-cleaner prompts stayed strong
- toddler-cleaner stayed stable
- stove/CO stayed stable

### Android / app

`D38`:

- tightened water-storage vs water-distribution routing boundary
- explicit gravity-fed distribution stayed strong

`D41`:

- answer shaping for broad treated-water storage now prefers storage-specific sections (container safety, sanitation, inspection, rotation)
- did **not** need `PromptBuilder` edits

`D42`:

- broad small-cabin site/foundation prompts now stay more site-selection-led
- explicit foundation prompts still remain foundation-led

`D43`:

- roof-weatherproofing prompts still stay in-family
- `GD-094` calculator / generic structural junk is now penalized as distractor

`D44`:

- merge-and-trust prompts still anchor on `GD-626`
- trust-repair / mediation / vouching style support now rises
- finance-style / monitoring-heavy / quota-heavy support is demoted or filtered for that family
- did **not** need `OfflineAnswerEngine` edits

## Current queue

The immediate next slices are already drafted:

1. `D45`
   - file: `notes/dispatch/D45_wave_y_schoolhouse_accessibility_routing_polish.md`
   - guide-family return
   - focus: schoolhouse layout vs accessibility-routing boundary
   - expected core touch set:
     - `guides/education-system-design.md`
     - `guides/accessible-shelter-design.md`
     - `guides/education-teaching.md`

2. `D46`
   - file: `notes/dispatch/D46_wave_be_symptom_first_medical_routing.md`
   - next guide family after `D45`
   - focus: symptom-first routing boundary between `common-ailments-recognition-care`, `first-aid`, and `medications`

Already drafted beyond that:

- `D45_wave_y_schoolhouse_accessibility_routing_polish.md`
- `D46_wave_be_symptom_first_medical_routing.md`

If you want to keep the exact same cadence, the clean next move is:

1. dispatch `D45`
2. if it lands, dispatch `D46`
3. only after that decide whether to return to Android or continue guide-family validation

## Why the queue is trustworthy now

The useful pattern this session was:

- when a guide slice really was the right layer, it landed (`D37`)
- when a guide slice exposed that the real miss was retrieval/citation selection, it was stopped and converted instead of forced (`D39` -> `D40`)
- Android work stayed deliberately narrow and file-bounded (`D38`, `D41`, `D42`, `D43`, `D44`)

That means the queue is no longer hiding “unknown unknowns” behind vague backlog bullets. Each drafted slice exists because either:

- the repo plan explicitly names the family (`wave_y`, `wave_be`), or
- a fresh artifact / live Android quality miss justified the exact next fix

## Validation / infra note

During `D37` recovery, the split-host guide validation lane initially failed because the FastEmbed host on `127.0.0.1:8801` was not up.

What worked:

- generation host on `127.0.0.1:1235/v1`
- embedding host on `127.0.0.1:8801/v1`
- running the existing guide validation wrapper with the split-host setup after the embed host was available

Practical reminder for the next self:

- before `wave_y` / `wave_be`, verify `http://127.0.0.1:8801/v1/models`
- if dead, bring up the existing FastEmbed host using the repo’s existing tooling; do **not** turn that into a script-edit slice

## Worktree truth

Remaining dirt is still the expected unrelated/deferred shape:

- `AGENTS.md` modified
- `opencode.json` modified
- `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
- `LM_STUDIO_MODELS_20260410.json` untracked
- `4-13guidearchive.zip` untracked
- large residual untracked historical `notes/` backlog
- many untracked dispatch notes under `notes/dispatch/`
- some intentionally deferred/untracked scripts still open

Do **not** spend the next turn “cleaning up” this dirt unless Tate explicitly asks. It is not the bottleneck right now.

## Agent state

All helper subagents used for this chain were closed at wrap-up.

No slice is currently in flight.

## Recommended next move

Dispatch `D45` first.

Why:

- it is already drafted
- it is the clean guide-side return after the Android chain
- it is named directly by the guide plan as the next validation family
- it keeps momentum on actual product-facing work instead of falling back into hygiene or generic exploration

After `D45`, dispatch `D46` if `wave_y` does not uncover a surprise adjacent-facility blocker.

## Compact command memory

Not a script contract, just what mattered operationally this session:

- Android unit targets were run through `.\gradlew.bat testDebugUnitTest --tests ...`
- Python guide/retrieval tests were run through `python -m unittest ...`
- guide reruns used `.\scripts\run_guide_prompt_validation.ps1 -Wave <wave> -PythonPath <python> -GenerationUrl http://127.0.0.1:1235/v1 -EmbedUrl http://127.0.0.1:8801/v1`

## Final note to next self

The important strategic win here is not just the landed commits. It is that the repo is finally back in a mode where the next move is usually obvious and small:

- ship the narrow thing
- let artifacts tell you when the real layer is guides vs desktop retrieval vs Android answer/context
- do not widen unless the fresh evidence forces it

Stay in that rhythm.
