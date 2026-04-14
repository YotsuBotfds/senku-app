# Swarm Cost Reduction Operating Model - 2026-04-14

- Timestamp: `2026-04-14`
- Purpose: define the practical multi-model operating policy for this repo so we reduce Codex usage without losing momentum

## Core Rule

Codex should be the thin strategic orchestrator, not the default worker.

Default posture:
- use Codex for:
  - task decomposition
  - choosing which lane should own work
  - integrating results
  - deciding whether a quality gate is cleared
  - only the smallest critical-path edits when delegation overhead would dominate
- push implementation, scouting, and read-heavy analysis downward first

## Delegation Ladder

Use the cheapest lane that can do the job safely:

1. `Qwen 3.5 9B` local scout
- best for:
  - quick triage
  - short synthesis
  - cheap first-pass judgment
  - option generation
  - “what is the smallest next move?”
- avoid for:
  - ungrounded repo-specific file targeting
  - autonomous coding decisions without file context

2. `Spark 5.3` worker / explorer
- best for:
  - tiny file-local code patches
  - exact hook-finding
  - bounded read-only exploration
  - parallel low-overlap tasks
- avoid for:
  - huge cross-cutting changes
  - long-horizon autonomous implementation

3. `OpenCode GLM 5.1` sidecar
- best for:
  - long-running coding tasks
  - sustained review passes
  - bounded async implementation
  - “go work on this while the main lane keeps moving”
- use staggered submission for multi-task waves

4. `Codex main lane`
- reserve for:
  - orchestration
  - integration
  - critical-path judgment
  - repo-wide reasoning where miscoordination would be costly

## Practical Routing Rules

### Use Qwen first when:
- the task is short
- the answer can be approximate but useful
- the main need is prioritization, clustering, or quick summarization
- OpenAI/Codex budget is the main constraint

### Use Spark first when:
- there is a concrete file or two to patch
- the write scope is disjoint
- you want multiple bounded workers in parallel
- the task is too repo-specific for Qwen but too small for a GLM sidecar

### Use GLM sidecars first when:
- the task may run for a while
- the work can proceed asynchronously
- the task is implementation-heavy or review-heavy
- Codex should keep orchestrating instead of waiting

### Keep work on Codex only when:
- the very next decision depends on the result
- the patch is tiny and delegation overhead is higher than just doing it
- the task needs tight coordination across multiple active lanes

## Repo-Specific Patterns That Reduce Codex Burn

### 1. Qwen before Codex review

Before Codex spends time reading lots of artifacts:
- ask Qwen for:
  - the likely hotspot
  - the smallest file set to inspect
  - whether the problem is routing, retrieval, or content

### 2. Spark for minimal file slices

If the task is “edit this one guide heading” or “change this one XML layout”:
- use Spark
- keep ownership disjoint
- let Codex integrate only if needed

### 3. GLM for background coding waves

For real coding work:
- prefer OpenCode sidecars
- prefer `enqueue_opencode_sidecar_batch.ps1` for waves
- use spacing like `60s`
- avoid true simultaneous bursts

### 4. Codex only at the decision points

Codex should mostly step in to answer:
- did this fix the blocker?
- which family do we polish next?
- which lane should own the next task?

## Known Good Lane Assignments In This Repo

### Guide lane
- Qwen:
  - cluster ranking
  - “smallest next move” triage
- MCP helpers:
  - `sequential-thinking` for structured review and decomposition
  - `git` for history/diff/risk context around guide edits
  - `context7` only when a guide depends on external docs or APIs
- Spark:
  - file-local guide wording patches
  - exact retrieval-language hook finding
- GLM sidecar:
  - prompt-pack reviews
  - family-level judgment
  - deeper read-heavy comparisons
- Codex:
  - gate decisions (`wave_w`, `wave_x`, `wave_y`)
  - integration of retrieval vs guide fixes

### Android/UI lane
- Qwen:
  - UX idea generation
  - short prioritization
- MCP helpers:
  - `puppeteer` for browser verification when the surface is web-testable
  - `sequential-thinking` for stepwise UI planning if the flow gets tangled
- Spark:
  - tight file-local XML/Java/Kotlin edits
  - locator/hook finding
- GLM sidecar:
  - background coding wave
  - review of larger UI surfaces
- Codex:
  - coordinate validation
  - decide when to stop iterating

## Cost-Control Rules

- Do not use Codex for broad scouting if Qwen can narrow the file set first.
- Do not use Codex for bounded patch work if Spark can own the file cleanly.
- Do not block Codex on slow analysis if a GLM sidecar can run asynchronously.
- Prefer one Codex orchestration turn plus several delegated worker turns over many Codex read/think loops.

## Failure / Escalation Rules

- If Qwen hallucinates file paths, stop using it for file targeting and switch to Spark or grounded sidecar prompts.
- If Spark tasks overlap too much, reduce parallelism and give stricter file ownership.
- If GLM sidecars become flaky under burst load, stagger submissions and treat registry contention before blaming the model.
- If a task remains stubborn after multiple tiny guide-only tweaks, stop burning Codex on micro-iteration and either:
  - move to retrieval-layer changes
  - or pivot to a higher-leverage family

## Carry-Forward Rule

Future agents should assume:
- Codex usage is a scarce orchestration budget
- Qwen is the cheap local scout lane
- Spark is the bounded patch / explorer lane
- GLM sidecars are the async long-work lane
- the default question is not “can Codex do this?”
- the default question is “what is the cheapest safe lane that can do this first?”
