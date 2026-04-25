# Wave B Remainder Dispatch Prompt - 2026-04-18

Post-`BACK-U-03`. Covers `BACK-U-02` and `BACK-U-01` only. Supersedes
`notes/WAVE_B_DISPATCH_PROMPT_DRAFT_2026-04-18.md` for the remainder of
Wave B.

**Prerequisite:** Tracker repair + U-03 bookkeeping commit lands first
(see `notes/TRACKER_REPAIR_PLUS_U03_BOOKKEEPING_PROMPT_2026-04-18.md`).
After that lands, U-01's worker inherits a clean tracker.

---

## Dispatch Prompt (paste to Codex)

You are resuming **Wave B** of the reviewer-backend backlog. `BACK-U-03`
landed `af49d91` and introduced the confidence-label plumbing end-to-end
(desktop `_confidence_label` helper, Android `ConfidenceLabel` enum,
`AnswerRun.confidenceLabel` field, `AnswerRunResult.confidenceLabel`
field, `AnswerProgressListener.onConfidenceLabel(...)` callback, MetaStrip
token render). The contract is captured in
`notes/specs/meta_strip_confidence_token_addendum.md`.

Two tasks remain. Recommended order is `BACK-U-02` first (smaller, purely
additive, no refactor) then `BACK-U-01` (larger, introduces a new answer
mode). They can also go in parallel if you prefer - they touch different
files and neither depends on the other's output.

### Shared ground rules (carried over from the original Wave B prompt)

- Desktop parity first: land the `query.py` change, then mirror in
  `OfflineAnswerEngine.java`. Keep Python helper names and Java method
  names aligned across files.
- Do not re-declare any symbol U-03 already landed. Reuse
  `ConfidenceLabel.HIGH/MEDIUM/LOW`, `_confidence_label(...)`,
  `AnswerRun.confidenceLabel`, `AnswerRunResult.confidenceLabel`, and
  `AnswerProgressListener.onConfidenceLabel(...)` as-is.
- Treat `AnswerPresenter.Host` (interface on lines 30-43 of
  `android-app/app/src/main/java/com/senku/mobile/AnswerPresenter.java`)
  as the only contract DetailActivity sees. Do not reintroduce coupling
  that E-06 removed. Any new field on the success path flows through
  `AnswerPresenter.AnswerRunResult` and out via
  `DetailAnswerPresenterHost.onSuccess(...)`.
- Line numbers referenced below are from the pre-U-03 tracker. Grep for
  the actual symbol name if the line has drifted:
  `grep -n _should_abstain query.py`,
  `grep -n 'shouldAbstain\|buildAbstainAnswerBody' android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`.
- Before shipping, re-ingest the desktop pack and run:
  ```
  python3 -m unittest discover -s tests -v
  python3 scripts/validate_special_cases.py
  ```
- Android: build the app, push a refreshed mobile pack via
  `scripts/push_mobile_pack_to_android.ps1`, take one phone-portrait
  (`emulator-5556`) and one tablet-portrait (`emulator-5554`) screenshot
  per task. Drop artifacts under
  `artifacts/bench/wave_b_<taskid>_<date>/`.
- If `scripts/run_android_session_flow.ps1` fails with
  `Quote-AndroidShellArg not recognized`, skip that harness and use
  `scripts/run_android_prompt.ps1` + `scripts/run_android_search_log_only.ps1`.
  That harness bug is tracked as `BACK-T-04`; do not fix it inside a
  Wave B commit.
- One task per commit. Stop and report up after each commit - do not
  chain U-02 into U-01 without a human gate.
- Hard stop: if any single task exceeds 90 minutes of worker time,
  stop and surface the blocker rather than compressing scope.

---

### Task 1 - `BACK-U-02` (P0 / S / worker) - Safety-critical escalation on abstain

**Why first:** small, additive, no refactor, independent of U-01. Lands
as a tight commit so the Wave B tail gets simpler before U-01 opens.

**Desktop:**

- `query.py` abstain formatter (same region as `_should_abstain` near
  line 10083): when the query is flagged safety-critical (acute medical,
  poisoning, self-harm, emergency keywords) and the system is abstaining,
  prepend an explicit escalation line above the "closest matches" block.
- Exact wording from
  `notes/specs/escalation_copy_draft.md`. If that file does not yet
  exist, author it first as a 5-10 line note that pins the exact string.
  Coordinate wording with the `guides/mental-health/...` D-lane so the
  copy is consistent with the crisis-gestalt interpreter output.
- Source the safety-critical flag from the scenario-frame signal already
  produced upstream. Do not re-classify.

**Android:**

- `OfflineAnswerEngine.java` around line 777 (`buildAbstainAnswerBody`):
  same gated prepend. Reuse the scenario-frame signal already carried on
  `PreparedAnswer`.

**Accept:**

- New unit test on both engines. Safety-critical abstain -> escalation
  line present above "closest matches." Non-safety-critical abstain ->
  escalation line absent. Table-driven.
- Emulator screenshot (phone + tablet): abstain card for a safety-
  critical prompt shows the escalation line. A reference non-safety-
  critical abstain shows no escalation line.

**Out of scope for this task:**

- Do not touch `_confidence_label` or any U-03 code. The escalation line
  sits above "closest matches" regardless of confidence label.
- Do not add an uncertain-fit branch. That is U-01's job.

**Report on landing:**

1. Commit hash.
2. `git show <hash> --stat`.
3. Screenshot paths.
4. Unit test names + pass/fail.
5. One-line State Log summary suitable for
   `reviewer_backend_tasks.md`.

Stop before starting U-01.

---

### Task 2 - `BACK-U-01` (P0 / L / worker) - Uncertain-fit response mode

**Depends on U-03's landed confidence label.** Consume `ConfidenceLabel`
and `_confidence_label(...)` directly; do not introduce a parallel scoring
path.

**Desktop:**

- `query.py` around `_should_abstain` (line ~10083): introduce a three-
  way mode between "confident answer" and "no-match abstain." Prefer
  adding a helper `_resolve_answer_mode(reranked, scenario_frame,
  confidence_label)` that returns a `Literal["confident", "uncertain_fit",
  "abstain"]`. Trigger uncertain-fit when:
  - average RRF < 0.65 AND not already abstaining, **or**
  - top-candidate vector similarity in `[0.45, 0.62]`, **or**
  - scenario frame flagged safety-critical with no primary-owner match
    AND `confidence_label in {"medium", "low"}`.
- Call it in the post-rerank pipeline where `_should_abstain` is already
  invoked (~line 10160). Branch once, carry the mode through the prompt-
  build path.
- New helper `_build_uncertain_fit_body(query, top_chunks,
  confidence_label)` that biases output toward clarification and
  escalation, surfaces related guides as "possibly relevant" rather than
  "answer." Copy per the PaperAnswerCard uncertain-fit addendum.
- Keep abstain behavior unchanged below the abstain floor. Uncertain-fit
  is a new branch, not a widening of abstain.

**Android:**

- `OfflineAnswerEngine.java` around line 184-195 (`shouldAbstain` and
  siblings): mirror the three-way decision. Prefer a new
  `enum AnswerMode { CONFIDENT, UNCERTAIN_FIT, ABSTAIN }` returned from
  a new helper and consumed at the `generate()` branching site around
  line 211. Reuse `ConfidenceLabel` from U-03 as a helper input.
- New `buildUncertainFitAnswerBody(query, topChunks, confidenceLabel)`
  paired with the existing `buildAbstainAnswerBody` (line ~942).
- Extend `AnswerRun` and `AnswerPresenter.AnswerRunResult` with an
  `AnswerMode mode` field. Forward via
  `DetailAnswerPresenterHost.onSuccess(...)`; DetailActivity reads it and
  branches `PaperAnswerCard` rendering. Do not add new DetailActivity
  getters that leak engine internals.
- Safety-critical uncertain-fit: if the U-02 escalation line has already
  landed, the uncertain-fit body should also prepend the escalation line
  on safety-critical queries. Reuse the U-02 helper rather than
  duplicating the string.

**Accept:**

- Reviewer-worked example: "He has barely slept, keeps pacing, says
  normal rules do not apply to him. Is this just stress, or should I
  help him calm down?" routes to uncertain-fit, not bridging and not
  abstain. Covered by a unit test on both engines.
- Boundary tests on both engines:
  - confident -> uncertain-fit boundary (average RRF crossing 0.65)
  - uncertain-fit -> abstain boundary (below the abstain floor)
  - safety-critical + no-owner + medium/low label -> uncertain-fit
  - safety-critical + high label -> confident (not uncertain-fit)
- Emulator screenshot (phone + tablet): uncertain-fit card visibly
  distinct from confident and from abstain. Three-state screenshot
  strip (confident / uncertain-fit / abstain) in the artifacts folder
  is ideal.

**Specs required:**

- `notes/specs/uncertain_fit_mode_spec.md` pinning thresholds, trigger
  conditions, and the exact `_build_uncertain_fit_body` output template.
- PaperAnswerCard uncertain-fit addendum pinning the card copy, color
  token, and icon. If either spec is missing, author it before coding
  and call that out in the commit message body.

**Out of scope:**

- Do not retune `_confidence_label` thresholds. U-03 froze those; adjust
  only if a boundary test fails unambiguously and note the retune in the
  commit message.
- Do not touch MetaStrip render. U-03's token is label-keyed, not mode-
  keyed.

**Report on landing:** same five-item format as U-02.

---

### After U-01 lands

Wave B is formally complete. Report up and wait for Tate's next dispatch
before moving to `CHECKPOINT 9` or any other lane. Do not fold Wave B
bookkeeping into the U-01 commit - that will go in a follow-up tracker
commit like the U-03 one.