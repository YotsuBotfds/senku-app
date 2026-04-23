# App Routing Hardening Tracker

> Historical lane record: this is the 2026-04-17 mental-health routing lane tracker. It is not the live current queue. Use [`CP9_ACTIVE_QUEUE.md`](./CP9_ACTIVE_QUEUE.md) for live execution order.

Date: 2026-04-17

Purpose:
- Capture the merged post-guide-loop playbook as a historical lane snapshot.
- Keep the prompt-understanding, routing / owner-selection, answer-shape, and validation notes together for reference.
- Keep live execution order and speculative backlog in the current planner queue, not here.

## Current State

The broad guide-content hunt is largely exhausted.

The remaining highest-value work is concentrated in:
- prompt understanding of messy high-risk language
- routing / owner-selection drift toward broad magnetic guides
- answer-shape drift toward calming, routine, or self-management before containment / supervision / escalation
- validation noise when LiteRT returns intermittent `500`s

Current read on the active mental-health lane:
- `wave_de` is improved but still not parkable
- `wave_fd` is useful but partially contaminated by LiteRT `500` failures in the latest run
- the remaining problem is still mostly `query.py`, not missing guide content

## Worker Split

### `gpt-5.3-codex-spark` `xhigh`

Use for:
- cheap read-only scouting
- queue fill and overlap scouting while validation is running
- "what is the smallest next slice?" branch planning
- exact-file candidate narrowing
- validation-pack ideation
- triage of whether an issue is mostly prompt understanding, routing, answer shape, or validation noise

Do not use for:
- final safety-sensitive adjudication when scout output is ambiguous
- code edits

### `gpt-5.4` `high`

Use for:
- exact-file scouting when the slice is subtle or safety-critical
- adjudicating Spark scout output when precision matters
- bounded implementation work
- prompt-runner / tracker updates
- code changes in `query.py`, `config.py`, deterministic files, or selected guides

Default rule:
1. start with `Spark xhigh` for light read-only work
2. promote only the best or ambiguous slice to `gpt-5.4 high`
3. use `gpt-5.4 high` workers for actual edits

## Strategic Perspectives

### 1. Interpreter-first crisis gestalt gate

Problem attacked:
- vague observer-language high-risk prompts still get interpreted too softly

Best lane:
- `query.py`

Leverage:
- high

Risk:
- medium

Current implication:
- keep strengthening recognition of "activation + invulnerability + food/sleep collapse + unsafe behavior" as one acute crisis gestalt

### 2. Primary-owner lock with harder distractor demotion

Problem attacked:
- broad magnetic guides still survive retrieval and steer the answer

Best lane:
- `query.py`

Leverage:
- high

Risk:
- medium

Current implication:
- when a prompt crosses the crisis threshold, keep the primary answer lane centered on the crisis owner and demote sleep/anxiety/PFA/elder/self-care support unless explicit evidence justifies them

### 3. Hard answer contract instead of advisory notes only

Problem attacked:
- even when retrieval is close enough, the answer still leaks routine/calming/self-management content into the first action block

Best lane:
- `query.py`
- `config.py` only if `query.py` prompt scaffolding still proves too soft

Leverage:
- high

Risk:
- low to medium

Current implication:
- the opening block for crisis prompts should stay on:
  - close supervision
  - means restriction
  - escalate now

### 4. Bridge-guide quarantine for acute complaints

Problem attacked:
- broad planning/system guides can dilute acute complaint retrieval

Best lane:
- `query.py`
- later, selected guide packaging

Leverage:
- medium-high

Risk:
- low to medium

Current implication:
- guides like `healthcare-capability-assessment` should not behave like primary acute complaint owners unless the query is explicitly about planning/capability/readiness

### 5. Validation stability lane

Problem attacked:
- noisy LiteRT failures make it harder to tell real routing misses from host flake

Best lane:
- `scripts/run_guide_prompt_validation.ps1`
- prompt-pack workflow
- validation operating notes

Leverage:
- high

Risk:
- low

Current implication:
- do not silently treat flaky LiteRT runs as product truth
- keep explicit note of when a pack is "product miss" vs "host contaminated"

### 6. Deterministic escape hatches for narrow highest-risk patterns

Problem attacked:
- some repeated high-risk prompts may be too important to leave fully probabilistic

Best lane:
- `deterministic_special_case_registry.py`
- `special_case_builders.py`
- `query.py`

Leverage:
- medium-high

Risk:
- high

Current implication:
- only consider after query/routing/answer-shape improvements stop giving strong returns

### 7. Guide-family anti-magnet surgery

Problem attacked:
- overlap terms inside broad nearby guides still attract retrieval

Best lane:
- selected guide families

Leverage:
- medium-high

Risk:
- high

Current implication:
- this is later work, not the current first lever

## Ranked Execution Order

### 1. Finish the mental-health crisis lane in `query.py`

Why first:
- it still has the highest concentration of real misses
- the remaining work is still mostly routing / answer hierarchy, not guide gaps

Current target:
- harder crisis-gestalt recognition
- stronger primary-owner lock
- harder ban on routine/self-management in the opening block

Status:
- active

### 2. Keep the vague behavior-change validation lane hot

Why second:
- it is the best pressure test for messy observer-language prompts

Current wave:
- `wave_fd`

Status:
- active but partially contaminated by intermittent LiteRT `500`s in the latest run

### 3. Split product misses from host flake

Why third:
- we need to keep trusting the optimization loop

What this means:
- when a wave is contaminated by host errors, do not overfit the product to noisy artifacts
- keep separate notes on:
  - real routing / answer-shape misses
  - runtime failure contamination

Status:
- active supporting lane

### 4. Open stroke / TIA packaging once the mental-health lane is parkable enough

Why fourth:
- this is the next best acute-owner domain after the current lane

Expected first slice:
- `query.py`
- `guides/acute-coronary-cardiac-emergencies.md`
- `guides/first-aid.md`

Status:
- queued behind mental-health parkability

### 5. Revisit deterministic acute crisis escapes only if returns diminish

Why fifth:
- higher risk, but still valuable if the remaining misses are repeated and narrow

Status:
- gated

## Active Findings From Latest Runs

### Mental-health lane

Confirmed:
- the system is much better at recognizing crisis-like prompts than it was at the start of this lane
- owner drift is reduced but not eliminated
- the first action block still sometimes leaks:
  - routine
  - basic function
  - calming / non-threatening presence
  - meals / sleep resets

Still live:
- some vague observer-language prompts are still too soft
- some prompts still invite sleep/anxiety/self-care residue
- some packs are partially contaminated by LiteRT instability

### Validation lane

Confirmed:
- some recent packs completed cleanly
- some recent packs hit intermittent `500` failures at `1235`

Rule:
- do not treat a host-contaminated wave as a pure product signal

## Immediate Next Move

1. Continue the mental-health `query.py` hardening lane while the remaining misses are still clearly higher-value than the next domain.
2. Keep `Spark xhigh` overlap scouting active so the next slice is already prepared before each rerun finishes.
3. When the mental-health lane is parkable enough, open the stroke/TIA routing slice immediately.
