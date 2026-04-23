# Probe — rain_shelter `ask.generate mode=` confirmation

- **Role:** main agent (`gpt-5.4 xhigh`) or Spark xhigh (read-only).
  Either lane fine; Spark preferred for cost.
- **Paste to:** **new window** (fresh session from this file, or
  inline if you prefer — this is a ~5-minute grep probe with no
  commit).
- **Predecessor context:** post-RC retrieval chain closed 2026-04-20
  night. R-anchor1 (`971961b`) flipped rain_shelter's anchor from
  GD-727 → GD-345 and made `context.selected` shelter-dominant.
  R-gal1 (`585320c`) relaxed the trust-spine assertion to tolerate
  `uncertain_fit` / `abstain` settled wording. State-pack matrix
  passed 45/45 (after flake-check). Open question: did the
  rain_shelter query's final answer mode flip to `confident`, or did
  it pass only because the tolerant assertion now accepts
  `uncertain_fit`? Focused probes via
  `PromptHarnessSmokeTest` have been blocked 3× by
  `busy[1]: main.ask.prepare`, so the existing state-pack logcats
  are the best available evidence surface.

## Scope

Read-only grep. No code, no emulator, no commit.

## The work

1. Under `artifacts/external_review/rgal1_state_pack/20260420_215227/raw/`,
   find the per-serial logcat files for the rain_shelter fixture
   (likely named along the lines of `emulator-*/logcat*` or under
   a `generativeAsk*NavigatesToDetailScreen` subfolder — use
   `Glob` with pattern like `**/emulator-*/logcat*` and `**/logcat*`
   to enumerate).
2. Grep each for `ask.generate` lines. Extract the `mode=<...>` value
   for the rain_shelter query on each of the four serials.
3. Cross-reference with `ask.prompt` lines to confirm the query
   identity (rain_shelter preparation).
4. Record the `anchorGuide=` value on `ask.prompt` to confirm
   GD-345 won (should have, per R-anchor1 probe on 5556).

## Verdict template

For each of the four serials (5556 / 5560 / 5554 / 5558), report:

- `anchorGuide`: `GD-???`
- `ask.generate mode`: `confident` | `uncertain_fit` | `low_coverage_route` | `abstain` | not emitted
- Whether `context.selected` shows GD-345 shelter-dominant (as on 5556) or still GD-727-dominant.

Then the chain-closure read:

- **All four emit `mode=confident`** → chain closed semantically. R-anchor1 carried the win; R-gal1's tolerance is extra insurance but was not load-bearing for rain_shelter specifically.
- **All four emit `mode=uncertain_fit`** with shelter-dominant `context.selected` → retrieval routed correctly, but the engine gate still routes conservatively. R-gal1 is load-bearing. Candidate follow-on: investigate whether the engine's confidence floor can be tightened now that retrieval is clean.
- **Mixed** (e.g., on-device phones confident, tablet host-inference uncertain_fit) → host-inference lane still conservative. Candidate tie-in with R-host diagnostic.
- **`ask.generate` not emitted anywhere** → state-pack lane didn't reach generation. Orthogonal finding; the state-pack fixture asserts settled UI, not engine completion. Re-test via focused probe once R-host closes.

## Boundaries

- No code edits.
- No artifact creation beyond the terse report.
- No commits.
- If `busy[1]: main.ask.prepare` appears anywhere in the logcats,
  note it but do not diagnose — that's R-host's job.

## Report format

Reply with:

- Per-serial table (4 rows: anchorGuide, ask.generate mode, context.selected shape).
- Chain-closure verdict (one of the four branches above).
- Any surprising finding (e.g., `mode=low_coverage_route` which would indicate R-eng's post-gen downgrade fired — not expected here but worth noting).
- Path list of the logcat files actually inspected.
