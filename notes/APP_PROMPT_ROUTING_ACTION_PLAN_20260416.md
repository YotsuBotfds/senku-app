# App Prompting, Routing, and Content Action Plan - 2026-04-16

## Purpose

This note turns the current app-side improvement ideas into an execution plan.

Focus:
- better mobile answer shape
- better guide-family routing
- cleaner validation loops
- a clearer rule for when to use GLM sidecars vs faster mini-high agents

## Priority Order

1. Mobile prompt shaping
2. Family-boundary and false-winner cleanup
3. Near-miss validation expansion
4. Deterministic entry points for repeated dangerous queries
5. Observability and route-confidence support

## 1. Mobile Prompt Shaping

### Goal

Make mobile answers open with the first useful action faster, while keeping retrieval context compact enough for LiteRT.

### Changes to target

- tighten the Android generation prompt so answers start with:
  - what to do first
  - what danger to avoid first
  - when to escalate
- reduce repeated route-explainer prose when the winning guide is already focused
- improve follow-up preservation so second-turn questions stay on the same subject without re-explaining the whole topic
- add an ambiguity rule for mixed-intent prompts:
  - if a prompt could be household + medical or practical + emergency, answer the dangerous branch first

### Best worker

- `mini-high agent`

Why:
- this is prompt-template and app-behavior reasoning work
- it benefits from quick iteration and direct code edits
- it does not need the slower async lifecycle unless it becomes a large repo review

### Escalate to sidecar when

- the change needs a broad read across multiple prompt builders, Android files, and retrieval glue
- or we want an async review memo comparing several prompt-template variants

## 2. Family-Boundary and False-Winner Cleanup

### Goal

Keep broad hubs from stealing queries that should land on a focused guide.

### Changes to target

- keep pushing complaint-first routing blocks near the top of sibling guides
- keep adding reciprocal `related:` links where sibling handoff is common
- strip over-broad aliases/tags from hub guides when focused guides should own the query
- add short ownership lines:
  - `if your real question is ...`
  - `use this guide for ...`
  - `go to sibling X for ...`

### Best worker

- `GLM sidecar`

Why:
- this is file-local, read-heavy, and often benefits from async bounded edit waves
- sidecars are proving strong at:
  - finding sibling collisions
  - landing minimal routing edits
  - listing exact changed files

### Escalate to mini-high when

- the issue is already clearly identified and only needs a tight local patch
- or the work is in one or two files and we want faster turnaround than sidecar polling

## 3. Near-Miss Validation Expansion

### Goal

Test prompts where two sibling guides could both plausibly win.

### Changes to target

- expand targeted prompt packs around:
  - broad-hub vs focused-guide conflicts
  - household vs medical mixed-intent prompts
  - complaint-first everyday queries
  - vague prompts like:
    - `what should I check first`
    - `is this safe`
    - `when should I worry`
- keep validation grouped by family instead of random mixed packs

### Best worker

- `GLM sidecar` for draft validation packs
- `mini-high agent` for quick curation or pruning after results come back

Why:
- sidecars are good at bounded read-only pack drafting
- mini-high is good for quick cleanup and prioritization once we already have candidate prompts

## 4. Deterministic Entry Points

### Goal

Protect repeated dangerous phrases from depending entirely on guide competition.

### Changes to target

- add narrow deterministic handlers only for repeated proven misses
- prefer the smallest rule that:
  - recognizes the dangerous phrasing
  - points to the right guide family
  - avoids creating a giant special-case surface

### Good candidates

- repeated poisoning/unknown chemical escalation phrases
- repeated power / live-wire / battery danger phrasing
- repeated severe-cardiac / stroke recognition phrasing

### Best worker

- `mini-high agent`

Why:
- deterministic work is code-local and benefits from faster inline reasoning
- usually needs edits in:
  - `deterministic_special_case_registry.py`
  - `special_case_builders.py`
  - validation helpers

### Escalate to sidecar when

- a family needs read-only evidence gathering first before we codify a rule

## 5. Observability and Route Confidence

### Goal

Make it easier to see when retrieval is thin, ambiguous, or split across siblings.

### Changes to target

- add lightweight internal route-confidence logging
- surface when top results are all sibling guides with weak separation
- log broad-hub wins over focused-guide siblings for later triage
- keep Android/mobile prompts small enough that confidence signals do not bloat context

### Best worker

- `mini-high agent`

Why:
- this is app/runtime instrumentation work, not a big async content pass

## Worker Routing Rule of Thumb

### Use GLM sidecars for

- bounded guide-family edit waves
- read-only sibling-collision review
- validation-pack drafting
- async family planning when the prompt is tightly constrained

### Use mini-high agents for

- prompt-template edits
- deterministic routing code
- observability/logging changes
- fast local cleanup after validation findings
- small direct patches where waiting on sidecars adds more friction than value

### Avoid for now

- using sidecars for broad open-ended planning without very tight constraints
- using mini-high agents for large slow family-review waves that are better handled asynchronously

## Immediate Execution Sequence

1. Keep current guide-family sidecar waves moving.
2. After each landed family:
   - ingest changed files
   - run the matching validation pack
   - decide `park / tighten / deterministic rule`
3. In parallel, open a mini-high lane for mobile prompt shaping:
   - first-step answer shape
   - ambiguity rule
   - follow-up preservation
4. After prompt-shaping changes land, run a small mobile validation pass against:
   - vague prompts
   - mixed-intent prompts
   - false-winner prompt pairs

## Success Criteria

- answers start with a useful first action more often
- focused guides beat broad hubs more consistently
- validation packs produce fewer sibling near-miss failures
- deterministic rules exist only for repeated proven dangerous misses
- mobile prompt assembly stays compact enough for LiteRT constraints
