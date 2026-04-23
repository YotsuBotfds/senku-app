# Slice A1-retry — 5560 landscape targeted rerun

- **Role:** main agent (`gpt-5.4 xhigh`). Owns delegation per
  `notes/SUBAGENT_WORKFLOW.md`.
- **Parallel with:** P1 (planner cleanup) and P2 (Stage 1 preflight) —
  they do not touch the emulator matrix. Serial with everything else on
  5560.
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → "A1 — Stage 0 Step 6j v6"
  (5560 retry)

## Context

v6 Stage 0 is RED on `emulator-5560` only. Pattern-2 classification is
misleading: the engine DID emit. Evidence from the v6 artifacts:

- `smoke_emulator-5560_v6/logcat.txt` contains
  `ask.uncertain_fit query="He has barely slept..." adjacentGuides=3` —
  identical event to 5556 which passed.
- Retrieval ran: `lexicalHits=121`, `vectorHits=28`, six top guides
  including `GD-047`.
- `host.request` / `host.response` absent — local on-device inference,
  not host fallback.
- `smoke_emulator-5556_v6/ui_dump.xml` shows the full settled card:
  `text="SENKU ANSWERED"`, `text="UNSURE FIT"`,
  `text="Field entry - Unsure fit"`, follow-up rail with GD-178 and
  GD-180, plus the a11y content-desc
  `"answered (warn), THIS DEVICE (accent), likely match, 3 sources, 1 turn, Unsure fit (warn)"`.
- `smoke_emulator-5560_v6/ui_dump.xml` contains NONE of those — only
  the top bar (back / pin / share / query title / GD-047 chip), the
  soft-keyboard IME pane (Sticker/GIF/Clipboard/Settings/Shift/etc.),
  and the follow-up input label `"Next field question"`.

The 5560 dump shows the IME occupying the content area. The detail
card's body, escalation line, and follow-up rail are not in the node
hierarchy — either recycled by landscape + IME-driven layout resize, or
scrolled out of the visible portion of the ScrollView/RecyclerView.

**This is a harness capture artifact, not an engine failure.** The fix
lives in the smoke harness, not in Wave B code.

## Outcome

Re-capture 5560 landscape with the IME dismissed and the body scrolled
into view, OR capture via the instrumentation-level
`prompt_detail.xml`/`prompt_detail.png` (which is what the UI smoke
already copies from inside the app) and validate against that. If the
escalation sentence is present in one of those two artifacts, 5560 is
PASS and Stage 0 flips GREEN.

## The rerun

1. Confirm pre-state on 5560: `files/models/` still has the pushed
   LiteRT model; `senku_model_store.xml` still points at it; no
   `senku_host_inference.xml`. Use `adb -s emulator-5560 shell run-as
   com.senku.mobile ls files/models/` and cat the prefs.

2. Run `scripts/run_android_instrumented_ui_smoke.ps1` against
   `emulator-5560` with:
   - `-Orientation landscape`
   - The same scripted query ("He has barely slept, keeps pacing...")
   - A pre-dump step that either:
     - dismisses the IME via `adb shell input keyevent 111` (ESC) or
       `input keyevent 4` (BACK) before `uiautomator dump`, OR
     - explicitly scrolls the detail body to the top
       (`adb shell input swipe 300 200 300 1000 300`) before dump.
   - Prefer the IME dismissal path — it's less invasive.

3. Capture both `ui_dump.xml` (harness-level) and
   `prompt_detail.xml` (instrumentation artifact from inside the app,
   already copied into the per-serial artifact dir). Compare.

4. Pass criterion: the escalation sentence
   `"If this is urgent or could be a safety risk, stop and call local emergency services now ..."`
   appears in at least one of the two captures, AND the UNSURE FIT
   chip + body text are present in at least one of the two.

5. Land the fix: if the IME-dismiss path works, extend
   `scripts/run_android_instrumented_ui_smoke.ps1` to do the dismiss
   before dump on every landscape run (not just 5560). Ship this as a
   small script edit in the same dispatch. This prevents the same
   occlusion bite on future landscape smokes.

## Boundaries

- Do NOT change Wave B code. There is no Wave B bug here.
- Do NOT re-push the APK or the model to 5560 — they are correct.
- Do NOT touch the other three serials (5556 already passed; 5554 /
  5558 are host-inference pass with a documented scope cut).

## Acceptance

- `artifacts/cp9_stage0_20260419_142539/smoke_emulator-5560_v6_retry/`
  (or a new timestamped run dir) with a `ui_dump.xml` or
  `prompt_detail.xml` that contains the escalation sentence.
- `scripts/run_android_instrumented_ui_smoke.ps1` extended with the
  pre-dump IME dismiss on landscape runs (if that was the fix path).
- Stage 0 `summary.md` appended with "Resume v6 retry — 5560 GREEN via
  IME-dismiss capture" and verdict flipped to GREEN (or partial GREEN
  with the tablet host-inference scope cut documented).

## Delegation hints

(Suggestions only.)

- Step 1 is a three-line adb probe. Main agent inline.
- Step 2 requires editing a PS1 script if the IME dismiss is the fix.
  `gpt-5.4 high` worker is natural here — bounded edit, live-device
  rerun.
- Spark is not well-suited for any step in this slice (not read-only).

## Report format

Reply with:
- Pre-state confirmation for 5560 (model present, prefs correct).
- Which capture path worked (harness ui_dump after IME dismiss, vs
  instrumentation prompt_detail.xml, vs post-scroll dump).
- Paste of the escalation sentence and surrounding context as found.
- Whether the harness script was extended, and the commit sha if so.
- Updated Stage 0 verdict.
