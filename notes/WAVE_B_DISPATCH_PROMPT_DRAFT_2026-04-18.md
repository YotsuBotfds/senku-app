# Wave B Dispatch Prompt Draft - 2026-04-18

**Status:** Draft for Tate's review before handoff to Codex.

**Prerequisite check before dispatch:** Confirm the scout-tier Wave B spec addenda
are in hand or accept that Codex drafts them as part of the worker pass:

- MetaStrip confidence-token addendum (feeds `BACK-U-03`)
- PaperAnswerCard uncertain-fit addendum (feeds `BACK-U-01`)
- Escalation-copy draft coordinated with the `guides/mental-health/...` D-lane
  (feeds `BACK-U-02`)

If the addenda are not yet written, the dispatch prompt below includes a
scout-tier preamble step. If they are already drafted, strike that preamble.

---

## Dispatch Prompt (paste to Codex)

You are executing **Wave B** of the reviewer-backend backlog. Wave A closed
2026-04-18 (`OPUS-E-06` = `b41128a`, `BACK-P-04` = `bbc1b1d`, stock pack
re-export = `aa1b399`; Wave A bookkeeping commits = `7b4bf4b` and `a021d19`).
`BACK-R-03` was invalidated (no productized Android anchor-prior path — filed
as `BACK-R-05`) and the session-flow harness bug was filed as `BACK-T-04`;
neither blocks Wave B.

Wave B is three UI-crossing answer-shape tasks. All three are ungated now that
`AnswerPresenter.java` exists as the carve-out boundary between
`DetailActivity` and the answer pipeline. Treat `AnswerPresenter.Host`
(interface on lines 30-43 of `android-app/app/src/main/java/com/senku/mobile/AnswerPresenter.java`)
as the only contract DetailActivity sees; do not reintroduce coupling that E-06
just removed.

### Sequencing (strict)

1. **`BACK-U-03` first** — confidence label is the signal everything else
   branches on.
2. **`BACK-U-01` second** — uncertain-fit mode consumes the label from U-03.
3. **`BACK-U-02` can land in parallel with U-03** (independent).

Land each task as its own commit. Do not bundle.

### Shared ground rules

- Desktop parity first: land the `query.py` change, then mirror in
  `OfflineAnswerEngine.java`. Keep the Python helper names and Java method
  names aligned so the next retrieval-audit reads cleanly across both.
- Confidence label type: `enum ConfidenceLabel { HIGH, MEDIUM, LOW }` on
  Android, `Literal["high", "medium", "low"]` on desktop.
- Where U-03 adds a field to `AnswerRun` / `PreparedAnswer`, also extend
  `AnswerPresenter.AnswerRunResult` so the value reaches
  `DetailAnswerPresenterHost.onSuccess(...)` without DetailActivity reaching
  back into `OfflineAnswerEngine`.
- Do not add new DetailActivity getters that leak engine internals; everything
  U-03 needs to expose should land on the `Host` callback payload.
- Before shipping, re-ingest the desktop pack and run:
  `python3 -m unittest discover -s tests -v`
  `python3 scripts/validate_special_cases.py`
- Android: build the app, push a refreshed mobile pack via
  `scripts/push_mobile_pack_to_android.ps1`, and take one phone-portrait
  (`emulator-5556`) plus one tablet-portrait (`emulator-5554`) screenshot per
  task. Drop artifacts under
  `artifacts/bench/wave_b_<taskid>_<date>/`.

If you hit `Quote-AndroidShellArg not recognized` from
`scripts/run_android_session_flow.ps1`, skip that harness and use
`scripts/run_android_prompt.ps1` + `scripts/run_android_search_log_only.ps1`.
That bug is tracked as `BACK-T-04`; do not fix it inside a Wave B commit.

---

### Task 1 - `BACK-U-03` (P1 / M / worker) - Confidence label on every answer

**Desktop:**

- `query.py:10083` - `_should_abstain(results, query)` stays; add a new helper
  `_confidence_label(reranked, scenario_frame) -> "high" | "medium" | "low"`
  that keys off top-K RRF scores, overlap tokens, vector similarity, metadata
  match deltas, and the abstain flags already produced.
- Call it in the post-rerank pipeline around `query.py:10160` (where
  `_should_abstain` is already invoked). Attach the label to the prompt as a
  system instruction: `"The answer confidence is {label}. If confidence is low,
  note the gap in the first sentence."`
- Propagate through the streaming response metadata so bench + UI surfaces can
  read it.

**Android:**

- `OfflineAnswerEngine.AnswerProgressListener` (interface at line 51): add
  `void onConfidenceLabel(ConfidenceLabel label)` callback (don't stuff it into
  an existing method).
- `OfflineAnswerEngine.AnswerRun`: add `final ConfidenceLabel confidenceLabel`
  field.
- Thread through `OfflineAnswerEngine.generate()` so deterministic, abstain,
  host, on-device, and host-fallback paths all emit a label.
- `AnswerPresenter.AnswerRunResult` (lines 70-84 of `AnswerPresenter.java`):
  add the same field and populate it in the success path.
- `DetailAnswerPresenterHost.onSuccess(...)`: hand the label to DetailActivity
  via a new `setAnswerConfidenceLabel(ConfidenceLabel)` sink; render through
  MetaStrip (F-02, already landed) per the MetaStrip confidence-token addendum.

**Accept:**

- Every answer surface (deterministic, abstain, host, on-device, uncertain-
  fit) carries a confidence tag in the response metadata.
- New unit test: given a curated 50-query panel with ground-truth labels, the
  helper matches >= 80%.
- Emulator screenshot (phone + tablet) shows the MetaStrip token rendering.

**Spec:** If the MetaStrip confidence-token addendum is not yet written,
author `notes/specs/meta_strip_confidence_token_addendum.md` first.

---

### Task 2 - `BACK-U-01` (P0 / L / worker) - Uncertain-fit response mode

Depends on `BACK-U-03`. Do not start until U-03 is merged.

**Desktop:**

- `query.py:10083+` - extend `_should_abstain` region: introduce a third mode
  between confident and abstain. Trigger: average RRF < 0.65 OR top-candidate
  vector similarity in [0.45, 0.62] OR scenario frame flagged safety-critical
  with no primary-owner match.
- New helper `_build_uncertain_fit_body(query, top_chunks, label)` that biases
  output toward clarification + escalation, and surfaces related guides as
  "possibly relevant" rather than "answer".
- Keep abstain behavior unchanged below the abstain floor.

**Android:**

- `OfflineAnswerEngine.java:184-195` - extend `shouldAbstain` sibling logic
  into a three-way decision: confident / uncertain-fit / abstain. Prefer a new
  `enum AnswerMode { CONFIDENT, UNCERTAIN_FIT, ABSTAIN }` returned from a new
  helper and consumed at the `generate()` branching site around line 211.
- New `buildUncertainFitAnswerBody(...)` paired with the existing
  `buildAbstainAnswerBody` (line 942). Card copy per the PaperAnswerCard
  uncertain-fit addendum.
- Do not change DetailActivity render: the mode flows as a field on
  `AnswerRun` / `AnswerRunResult` and the existing renderer branches on it via
  `PaperAnswerCard`.

**Accept:**

- Reviewer-worked example - "He has barely slept, keeps pacing, says normal
  rules do not apply to him. Is this just stress, or should I help him calm
  down?" - routes to uncertain-fit, not bridging and not abstain.
- Emulator screenshot shows the distinct card (phone + tablet).
- New unit test covers: confident -> uncertain-fit boundary, uncertain-fit ->
  abstain boundary, safety-critical + no-owner routing to uncertain-fit.

**Spec:** `notes/specs/uncertain_fit_mode_spec.md` + PaperAnswerCard
uncertain-fit addendum. If either is missing, author it before coding.

---

### Task 3 - `BACK-U-02` (P0 / S / worker) - Safety-critical escalation on abstain

Independent; land in parallel with U-03.

**Desktop:**

- `query.py` abstain formatter (same region as `_should_abstain`): when the
  query is flagged safety-critical (acute medical, poisoning, self-harm,
  emergency keywords) and the system is abstaining, prepend an explicit
  escalation line above the "closest matches" block. Exact wording from the
  escalation-copy draft.

**Android:**

- `OfflineAnswerEngine.java:777-825` (`buildAbstainAnswerBody`): same gated
  prepend. Reuse the scenario-frame signal already in `PreparedAnswer` rather
  than re-classifying.

**Accept:**

- Unit test for both engines: safety-critical abstain -> escalation line
  present. Non-safety-critical abstain -> escalation line absent.
- Emulator screenshot: abstain card for a safety-critical prompt shows the
  escalation line above "closest matches."

**Spec:** Coordinate escalation wording with the `guides/mental-health/...`
D-lane entries. If the draft is missing, author a 5-line
`notes/specs/escalation_copy_draft.md` with the exact string first and call
that out in the commit.

---

### Landing + reporting

For each task, report:

1. Commit hash.
2. `git show <hash> --stat` output.
3. Paths to the emulator screenshots.
4. Unit test name and pass/fail.
5. One-line summary suitable for the State Log in `reviewer_backend_tasks.md`.

Stop and report up after each task. Do not chain landings; Tate will flip
tracker rows between commits.

If any task exceeds 90 minutes of worker time, stop and surface the blocker
rather than compressing scope.