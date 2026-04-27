# Android UI Review Gallery Plan - 2026-04-27

Task-based screenshot checklist for translating the locked UI direction review
into a human review gallery. This is a planning note only; it does not approve
product exposure changes, reviewed-card runtime expansion, or state-pack
acceptance by itself.

## Purpose

Build a gallery that reviewers can read like a product walkthrough, not a
regression ledger. The gallery should show whether the Rev 03 Android direction
feels coherent across the actual jobs users perform:

- first run and pack readiness
- Library browse/search
- guide open and source/cross-reference navigation
- standard answer
- reviewed emergency answer
- follow-up thread continuity
- no-pack, slow-model, abstain, and large-font states

The gallery is the visual/design receipt. State-pack proof remains the
behavioral/screenshot acceptance receipt.

## Posture Matrix

Capture every gallery task across the fixed Android posture matrix unless a row
explicitly says otherwise:

| posture | device | expected emphasis |
|---|---:|---|
| phone portrait | `5556` | primary thumb flow, bottom tabs, docked composer, large-font pressure |
| phone landscape | `5560` | compact vertical space, composer focus, long answer wrapping |
| tablet portrait | `5554` | rail plus center layout, evidence footer/collapse behavior |
| tablet landscape | `5558` | full three-pane reader, evidence pane, cross-reference density |

Physical phone/tablet screenshots may be added as milestone truth checks, but
they should supplement the fixed matrix rather than replace it.

## Gallery Tasks

### G-01 First Run / Pack Ready

Goal: prove the first screen communicates app identity, guide inventory, and
ready/offline posture without relying on developer-panel text.

Shots:

- fresh install first visible home
- pack ready state with guide count and manual identity visible
- empty/recent thread area before any question
- developer/test controls absent from the product path unless the build is
  intentionally a developer capture

Review checks:

- IdentityStrip follows the Rev 03 olive/paper/brass direction.
- first-run copy is calm and useful, not a wall of telemetry.
- no horizontal clipping in phone portrait.
- tablet portrait/landscape use available space without stretching content
  into unreadably wide lines.

### G-02 Library Browse / Search

Goal: show the Library/Search path as a real browse surface, not only as a
query result fixture.

Shots:

- Library/browse landing with category shelf or guide groups
- search field focused with typed query
- search results list with paper-card result treatment
- zero-result or low-confidence search result, if available
- tablet landscape results with rail/detail affordance visible

Review checks:

- SearchResult cards expose title, `GD-xxx`, category, lane tag, and snippet
  without chip walls.
- search and browse use consistent typography and spacing.
- result rows remain tappable and scannable at large font.
- phone landscape does not bury the input or primary result below the fold.

### G-03 Guide Open / Reader

Goal: show a normal guide-open path independent of generated answers.

Shots:

- guide detail opened from Library/Search
- guide opened from a home pivot/recent thread
- sources/related guides region at rest
- tablet landscape three-pane reader with left rail, center reader, and right
  evidence/cross-reference pane

Review checks:

- TopBar replaces legacy pill/button rows with icon toolbar behavior.
- guide title, `GD-xxx`, category, and section context are visible.
- SourceRow replaces pipe-joined labels.
- tablet landscape avoids the old narrow-column plus empty-space failure mode.

### G-04 Standard Answer

Goal: prove ordinary answer presentation after a supported non-emergency ask.

Shots:

- prompt submitted from Ask/Home
- answer loaded with standard evidence tone
- proof/source region closed
- proof/source region opened
- suggested follow-up chips visible after answer

Review checks:

- AnswerCard uses paper-on-dark treatment, serif body, steps when appropriate,
  and a distinct limits/safety area when present.
- MetaStrip carries answer state, timing, source count, model/host posture, and
  confidence tone without wrapping into visual clutter.
- proof affordance is visible and understandable.
- generated answer text does not collide with bottom composer.

### G-05 Reviewed Emergency Answer

Goal: show the reviewed-card emergency answer surface as a special trust state
without implying product-default exposure. Use only developer/test-gated
captures if runtime remains off by default.

Shots:

- reviewed-card runtime off/default product answer for a pilot emergency prompt,
  if useful as a contrast shot
- reviewed-card runtime on developer/test capture for one pilot emergency card
- reviewed emergency answer body
- reviewed proof/source expansion showing reviewed card ID, guide ID, review
  status, and cited reviewed source
- forbidden-label contrast shot, if available: reviewed answer does not show
  non-reviewed strength language

Review checks:

- reviewed state uses reviewed-specific trust copy, not generic strong-evidence
  language.
- emergency action copy stays prominent and unambiguous.
- card metadata is available in proof/detail surfaces without taking over the
  main reading experience.
- screenshots are clearly labeled as developer/test-gated when runtime is not
  product-enabled.

Candidate prompts from current checked-in pilot coverage:

- `my child swallowed an unknown cleaner`
- `newborn is limp, will not feed, and is hard to wake`
- `baby is choking and cannot cry or cough`
- `child has fever, stiff neck, and a purple rash that does not fade when pressed`
- `cut on my hand yesterday and now a red streak is moving up my arm`
- `bike handlebar hit his belly and now he is pale and dizzy`

### G-06 Follow-Up Thread

Goal: prove session continuity and follow-up ergonomics.

Shots:

- first answer with docked composer
- follow-up typed from detail
- second answer in the same thread
- thread list/recent row after follow-up
- tablet landscape thread rail with active turn selected

Review checks:

- thread state naming matches visible UI, not harness-only state labels.
- ThreadRow/PivotRow hierarchy makes the active thread obvious.
- follow-up answer keeps source/proof context from the original lane when
  relevant.
- composer stays reachable in phone portrait and landscape.

### G-07 Cross-Reference / Source Navigation

Goal: show moving from answer to source to related guide without losing context.

Shots:

- answer source list/proof opened
- source row tapped into anchored guide detail
- cross-reference row visible
- cross-reference opened
- back navigation or rail selection returning to original answer/thread

Review checks:

- SourceRow and XRef row styling clearly separates source, anchor, and related
  guide.
- tablet evidence pane gives useful density without becoming a second main
  article.
- phone uses collapsible/progressive disclosure instead of crowding the answer.
- handoff context is visible enough for reviewers to understand where they are.

### G-08 No-Pack / Pack Missing

Goal: prove the app fails helpfully when the pack is absent or unavailable.

Shots:

- first-run/no-pack home
- Library/Search blocked or degraded state
- Ask blocked/degraded state
- recovery action, if present

Review checks:

- message distinguishes missing pack from no results or model failure.
- no emergency or answer surface fabricates confidence without data.
- state is visible in both phone and tablet postures.

### G-09 Slow Model / Waiting

Goal: prove long-running generation feels controlled and recoverable.

Shots:

- prompt submitted with loading/waiting state
- slow-model or host inference status visible
- cancellation/back behavior, if available
- completed answer after the wait

Review checks:

- loading state reserves stable layout space and avoids jumping when the answer
  arrives.
- MetaStrip or status text explains model/host posture without alarming users.
- phone landscape keeps the active prompt and waiting state visible.

### G-10 Abstain / Low Coverage

Goal: prove the app can say it does not have enough support.

Shots:

- unsupported prompt answer
- low-coverage or abstain MetaStrip/state
- suggested next steps or search pivots
- proof/source area showing why support is limited, if available

Review checks:

- abstain copy is visibly different from supported answer copy.
- the UI does not bury the limitation below a confident-looking paper answer.
- suggested pivots are useful but not overconfident.

### G-11 Large Font / Accessibility Pressure

Goal: catch truncation, clipping, and density failures under large text.

Shots:

- first run/home at large font
- Library/Search results at large font
- standard answer at large font
- reviewed emergency answer at large font, developer/test-gated if needed
- follow-up composer at large font
- tablet landscape three-pane at large font

Review checks:

- text wraps instead of clipping in buttons, result rows, MetaStrip, and source
  rows.
- bottom tabs and composer remain usable.
- paper answer body keeps readable line length.
- no row requires hidden horizontal scrolling.

## Gallery Assembly

Recommended gallery grouping:

1. Product tour: G-01 through G-07 in phone portrait.
2. Responsive matrix: one representative shot per task across phone landscape,
   tablet portrait, and tablet landscape.
3. Edge states: G-08 through G-11 across all postures.
4. Reviewed-card appendix: developer/test-gated emergency captures with a clear
   banner in the gallery metadata, not in product UI.

Each screenshot entry should record:

- task ID and short task label
- posture and device serial
- APK SHA
- pack source: bundled, pushed, cached, or missing
- model identity and whether host inference was used
- runtime flags relevant to the capture, especially reviewed-card runtime
- artifact paths for screenshot, UI dump, logcat, and per-state summary when
  available
- known caveats, such as developer/test-only capture or intentionally missing
  pack

## Difference From State-Pack Regression Proof

The review gallery and state-pack proof answer different questions.

Gallery answers:

- Does the UI direction feel right?
- Are the key user tasks understandable?
- Are density, typography, source/proof affordances, and responsive layouts
  ready for human review?
- Do edge states look intentional rather than accidental?

State-pack regression proof answers:

- Did scripted states complete across the fixed four-emulator matrix?
- Were screenshots, UI dumps, logs, pass counts, ANR posture, APK SHA, model
  identity, and installed-pack metadata captured?
- Did the matrix pass or fail the acceptance contract?
- Did a behavior, handoff, source, or state assertion regress?

Rules:

- A beautiful gallery is not UI acceptance unless backed by a passing fixed
  four-emulator state-pack lane.
- A green state pack is not design approval; it may still show awkward density,
  clipping, confusing labels, or poor visual hierarchy.
- `-PlanOnly`, `-DryRun`, `-WhatIf`, metadata-only validators, pack parity
  evidence, and FTS fallback evidence may explain the gallery context, but they
  do not replace screenshots from real device/emulator postures.
- Reviewed-card gallery shots must say when they are developer/test-gated.
  They must not be used to imply product-default runtime exposure.

## Local Proof Commands

Use the gallery wrapper after a coherent UI batch when reviewers need a
task-ordered screenshot packet with the same posture vocabulary as the
state-pack lane.

Preflight only:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_gallery_local_proof.ps1 -PlanOnly
```

Full local gallery proof:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_gallery_local_proof.ps1
```

Focused role while iterating:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_gallery_local_proof.ps1 -RoleFilter phone_portrait -SkipBuild
```

Host-independent visual pass, when host inference is unavailable:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_gallery_local_proof.ps1 -SkipHostStates
```

The wrapper only calls
[`scripts/build_android_ui_state_pack_parallel.ps1`](../scripts/build_android_ui_state_pack_parallel.ps1)
and writes a `GALLERY_LOCAL_PROOF.md` run sheet beside the resulting artifacts.
It does not add new assertions, product behavior, or acceptance semantics.

Default output root:

- `artifacts/android_ui_gallery_local_proof/<run_id>/GALLERY_LOCAL_PROOF.md`
- `artifacts/android_ui_gallery_local_proof/<run_id>/summary.json`
- `artifacts/android_ui_gallery_local_proof/<run_id>/manifest.json`
- `artifacts/android_ui_gallery_local_proof/<run_id>/screenshots/<posture>/`
- `artifacts/android_ui_gallery_local_proof/<run_id>/dumps/<posture>/`
- `artifacts/android_ui_gallery_local_proof/<run_id>/summaries/<posture>/`
- `artifacts/android_ui_gallery_local_proof/<run_id>/parallel_logs/`

Acceptance boundaries for local proof:

- `-PlanOnly` plus
  [`scripts/validate_android_ui_state_pack_plan.py`](../scripts/validate_android_ui_state_pack_plan.py)
  is preflight evidence only.
- A failed or partial wrapped state pack can still be useful for visual triage,
  but it is not UI acceptance proof.
- A passing wrapped state pack is the acceptance receipt only for the scripted
  fixed-posture state-pack contract. The task gallery remains human design
  review context.
- Separately captured no-pack, slow-model, abstain, large-font, or reviewed
  emergency screenshots should be linked from the run sheet or gallery
  metadata, not treated as replacing the fixed matrix.

## Progress Update - 2026-04-27

Commits now reflected in this gallery plan:

- `761162a` gives the gallery a first seam to review Library / Ask / Saved tab
  language and pure evidence models without requiring renderer or runtime
  policy changes.
- `3adbf79` adds local gallery proof commands and tightens field-manual wording
  across answer, proof, source, cross-reference, and tablet evidence surfaces.
- `7294d2b` updates guide connection/source formatter copy, improves emergency
  banner contrast for gallery review, and expands local gates around the current
  UI seams.

Next gallery queue from the scout:

1. Nav intent: prioritize captures that prove Library / Ask / Saved is the
   visible navigation story without implying a broader navigation rewrite.
2. Detail surface contract: add review attention to guide-reader versus
   answer-detail shots so source/proof affordances stay task-specific.
3. Emergency eligibility policy: pair any emergency banner/gallery shot with
   reviewed/support-state evidence and routine negative controls.
4. Stress reading policy: keep phone landscape, large font, and urgent-answer
   density in the gallery queue before treating contrast or copy polish as done.

Boundary: this progress update does not change the gallery acceptance contract.
Plan-only output, local wrapper metadata, copy tightening, and seam tests remain
preflight or implementation evidence until backed by the fixed posture
state-pack screenshots/dumps required for UI acceptance.

## Acceptance For This Planning Note

This note is complete when it lets the next Android/UI agent build or request a
gallery without rereading the full UI direction audit:

- every required user task is named
- phone/tablet portrait/landscape coverage is explicit
- normal, emergency, follow-up, cross-reference, no-pack, slow-model, abstain,
  and large-font states are all represented
- state-pack regression proof remains separate from visual/design review
