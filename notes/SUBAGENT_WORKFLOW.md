# Subagent Workflow

Authoritative three-tier split for Codex-side model orchestration on this
repo. This supersedes the older "keep Codex thin" phrasing in AGENTS.md
and the two-role split in `REVIEWER_BACKEND_TRACKER_20260418.md` §Worker
Split (which is retained for history but no longer complete).

- Last updated: 2026-04-19
- Proven: Tate's current daily workflow as of CP9 Stage 0.

## The three roles

### Main agent — `gpt-5.4` `xhigh`

The conductor. Owns the critical path, reads the plan, decides what to do
itself vs. what to hand off. This is the agent Opus (planner) briefs with
slice-level prompts. Main agent **delegates the delegation** — it picks
the sub-lane per step rather than the planner pre-assigning one.

Use for:
- Critical-path work where judgment between steps matters
- Multi-file edits that need consistency across the session
- Any work where the next step depends on the previous step's output
- Emulator matrix dispatches, harness runs, live-on-device validation

Do not use for:
- Pure read-only scouts with a crisp question (promote to Spark)
- Bulk mechanical edits where a worker lane is cheaper and safer

### Fast scout — `gpt-5.3-codex-spark` `xhigh` (separate usage budget)

The cheap, fast, read-only scout. Separate budget bucket — using Spark
does **not** burn main agent tokens. Ideal for questions whose answer
reshapes the next step.

Use for:
- "Does this file still match what the tracker claims?"
- "Grep for X; list the call sites."
- "Read these three files and tell me whether Y is wired."
- Near-miss false-positive brainstorming
- Audits of whether a BACK task overlaps a landed OPUS task
- Code-audit-only decisions (e.g. anchor-prior fate)

Do not use for:
- Code edits (read-only by design)
- Safety-sensitive adjudication when the scout is ambiguous — promote
  to main or worker tier

### Heavier scout + worker — `gpt-5.4` `high`

Mid-budget worker. Can do implementation; also handles heavier scouts
that need more reasoning than Spark gives.

Use for:
- All code edits in `query.py`, `OfflineAnswerEngine.java`,
  `PackRepository.java`, `DeterministicAnswerRouter.java`,
  `deterministic_special_case_registry.py`
- Semantic adjudicator implementation (safety-critical paths)
- `LatencyPanel` + `LiteRtModelRunner` instrumentation
- Spec authoring for BACK-D-01, BACK-U-01, BACK-R-01 decisions
- Heavier scouts that hit Spark's reasoning ceiling

Do not use for:
- Work that main agent is actively driving (overlap risk)
- Cheap read-only questions (waste — use Spark)

## Default rule

1. Opus (planner) briefs **main agent** with a slice: outcome, boundaries,
   acceptance criteria. The prompt lives as an editable file in
   `notes/dispatch/` so it can be tweaked before dispatch.
2. Main agent reads the slice, plans sub-steps, and for each sub-step
   decides:
   - "Crisp read-only question?" → punt to Spark scout.
   - "Bounded implementation I don't need to drive?" → punt to
     `gpt-5.4 high` worker.
   - "Judgment step or dependent on my immediate state?" → handle inline.
3. Main agent reports back to Opus with outcome, artifacts, and a short
   delegation log (what went to which lane and why).
4. Do not re-dispatch scout work at worker tier. Do not re-dispatch
   implementation work at scout tier.

## Anti-patterns

- **Inline everything.** If main agent runs every sub-step inline, Spark's
  separate budget is wasted and the critical path bloats. The planner
  should call this out if it shows up in the delegation log.
- **Pre-assigning the lane for every step.** If the planner says "do X in
  Spark, then Y in worker, then Z in main," it is doing main agent's job
  for it. Brief main, let main delegate.
- **Parallel overlap on the same file.** Spark reading and worker editing
  the same file at the same time will produce a stale scout. If a step
  requires both read and write on the same file, keep them in one lane.
- **Safety-critical work in scout.** Any decision that affects the
  safety-critical escalation path (U-02 class) stays in main or worker.

## Handoff format

When main delegates, it uses the existing sidecar tooling (which is the
delivery mechanism, independent of role):

- Fast scout (Spark): `scripts/start_qwen27_scout_job.ps1` style detached
  scout, or direct Spark xhigh invocation if available in the main agent
  session.
- Worker (5.4 high): `scripts/enqueue_opencode_sidecar.ps1` with an
  explicit model route to `gpt-5.4` high, or equivalent sidecar helper.
- Spaced submission for wide waves to avoid
  `artifacts/opencode/sidecar/index.jsonl` contention — 30-second gap
  between batch submissions is usually sufficient.

## Relationship to the local sidecar ladder

AGENTS.md still lists the local routing ladder
(`GLM 5.1 sidecar > Spark > Qwen 27B > Qwen 9B`) as the preferred repo
routing for **local compute**. That ladder is orthogonal to this
subagent split, which is about **Codex-side role allocation**. Both apply:

- Main / scout / worker decides **which Codex role** handles a step.
- The local ladder decides **which local model** a sidecar task is
  shaped toward.

In practice, main agent routes Codex-side first and picks a local sidecar
only when (a) the work is bounded enough to leave Codex entirely, or
(b) cost posture demands it.
