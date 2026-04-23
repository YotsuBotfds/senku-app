# Slice P5 — pre-draft landscape-phone partial-GREEN scope note (hold, do not commit)

- **Role:** main agent (`gpt-5.4 xhigh`). Owns delegation per
  `notes/SUBAGENT_WORKFLOW.md`.
- **Parallel with:** A1b, P3, P4. No emulator touch.
- **Queue row:** `notes/CP9_ACTIVE_QUEUE.md` → On-deck P5.

## Preconditions

None for drafting. This slice produces a drafted file only — the
commit is conditional on A1b's outcome and is held until Opus says
"commit P5."

## Outcome

A drafted
`artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_landscape_phone.md`
that, when committed, lets Stage 0 close partial-GREEN analogously to
the tablet host-inference scope note. Kept as an uncommitted draft
ready to ship iff A1b same-mode fails.

## The draft

Write the file (do NOT commit) with this content shape — adapt pattern
from `apk_deploy_v6/scope_note_tablet_host_fallback.md` where it
exists, but write fresh content for the landscape-phone case:

- **Title and scope.** Landscape-phone posture (`emulator-5560`)
  Stage 0 acceptance.
- **What passes (engine side, on-device).**
  - Logcat evidence: `ask.start`,
    `search ... lexicalHits=121 vectorHits=28`,
    `ask.uncertain_fit query="He has barely slept..." adjacentGuides=3`.
    No `host.request` / `host.response`.
  - Manual `adb` capture evidence:
    `smoke_emulator-5560_v6_retry_manual/ui_dump_after_back.xml`
    (20,058 bytes) contains the escalation sentence
    (`If this is urgent or could be a safety risk...`), the UNSURE
    FIT chip, the `Field entry - Unsure fit` body, and the
    `Possibly relevant guides in the library:` heading.
- **What does not pass (harness side).**
  - The canonical instrumentation `prompt_detail.xml` (108,768 bytes)
    is IME-dominated because `UiDevice.dumpWindowHierarchy()` at
    capture time returns the focused window (Gboard) when landscape
    auto-focuses the follow-up EditText and `adjustResize` shrinks
    the app window.
  - ESC keyevent 111 does not dismiss Gboard on modern AVDs
    (`ui_dump_after_esc.xml` is byte-identical to
    `ui_dump_initial.xml`).
  - The A1b harness slice dispatched a device-level
    `UiDevice.pressBack()` variant — link the resulting artifact dir
    and outcome summary here when the note is actually committed.
- **Post-RC follow-ups.**
  - Tighten `PromptHarnessSmokeTest.assertDetailSettled` (tracked as
    `BACK-T-05` / the test-lane row from slice P4) to fail when the
    dump is IME-dominated.
  - If `pressBack()` wasn't the right shape, consider
    `windowSoftInputMode=adjustPan` for `DetailActivity`, or
    deferring the follow-up EditText auto-focus until user tap.
- **RC gate implication.** Production landscape-phone users see the
  correct rendered UI (proven by the manual capture) — the gap is in
  the test-harness capture path, not the product. CP9 RC v3 is not
  gated by this posture.

File destination:
`artifacts/cp9_stage0_20260419_142539/apk_deploy_v6/scope_note_landscape_phone.md`.
Leave uncommitted. Report back with the path and the full content so
Opus can decide whether and when to commit.

## Acceptance

- File exists at the target path.
- File is NOT committed. `git status` shows it as untracked (or
  modified, if an existing file was updated).
- Report back with a quote of the full contents and the path.
- Explicit "NOT committed — awaiting A1b outcome" flag in the report.

## Boundaries

- No commit. Main agent must stop at the draft.
- No tracker edits — this is a Stage 0 artifact, not a tracker row
  (the `assertDetailSettled` tracker row is P4's territory).
- Do not duplicate content verbatim from
  `scope_note_tablet_host_fallback.md`; reference it for pattern
  only.

## Delegation hints

(Suggestions only.)

- Pure writing. `gpt-5.4 high` worker is a good fit; main agent
  inline also works.

## Report format

Reply with:

- Path to the drafted note.
- Full quote of the final contents.
- Explicit "NOT committed — held pending A1b outcome" flag.
