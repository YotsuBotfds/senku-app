# Android Post-Redesign Functional UX Queue - 2026-04-30

Worker M tracking note only. No Android source or test edits in this lane.

## Context

- Current pushed head for this queue: `d9bc40a`
  (`Clarify ask search and detail back controls`), pushed at local time
  `2026-04-30 02:12:11 -05:00`.
- Overnight integration commits after the original note:
  - `489ba5e` - normalized nav rail labels/icons and topbar accessibility
    semantics.
  - `ac278eb` - made the Saved tab open a Saved Guides destination even before
    repository load, with explicit saved-guides empty copy.
  - `9a1cd89` - tokenized guide/detail surface colors.
  - `d9bc40a` - clarified Ask/Search submit ownership and hid nonfunctional
    detail overflow while routing visible Back through the same detail-back
    path.
- Fresh filtered proof after `9a1cd89`:
  `artifacts/ui_state_pack/post_push_0202_visual_spot/20260430_020308`.
  This is a phone/tablet portrait spot pack with `Pass: 10 / 10`; it is not a
  full 22-target closure pack.
- Latest pushed commit reviewed for this queue: `bd2b8db`
  (`Refine tablet thread flow and state-pack review`).
- Immediate context commits: `98159b5`, `075a1e4`, `ba236b5`,
  `7e61fd6`, `96eee1a`, `12aa170`, `9f3a7f8`.
- Work immediately after this note addressed the first-pass Ask/back items:
  empty `auto_ask=true` handoff opens the Ask lane, Ask/Search input chrome now
  uses distinct hints/descriptions/IME actions, tablet emergency rail labels
  the exit as `Manual`, and `DetailActivity` system Back routes through the
  same fallback as visible back.
- Prior visual checkpoint to preserve as comparison context:
  `artifacts/ui_state_pack/phone_tablet_geometry_wave/20260429_155255`.
  The checkpoint validated the full 22-state pack and mock visual diff at
  local time `2026-04-29 16:02:57 -05:00`.
- Earlier visual evidence remains useful for regression history:
  `artifacts/ui_state_pack/wave69_integrated_final/20260429_081538` and
  `artifacts/mocks`.

## Functional UX Queue

1. Ask handoff
   - Landed: empty auto-Ask handoff is now a unit-covered Ask lane entry
     instead of a search detour.
   - Verify that every visible Ask affordance submits to the Ask/answer flow,
     including tablet rail buttons, restored detail state, IME Enter, and
     hardware Enter.
   - Regression risk: a visually correct button can still fall through to
     Search ownership after rotation or state restoration.

2. Ask/Search separation
   - Landed: Search and Ask submit buttons now expose separate content
     descriptions, shared-input chrome uses the correct submit target, and a
     harness regression proves Search from an Ask lane remains guide-result
     search.
   - Keep Search as retrieval/navigation and Ask as answer generation/composer
     handoff. Do not let shared input plumbing blur labels, submit targets, or
     restored selected-tab ownership.
   - Future proof should include both phone and tablet, portrait and landscape,
     with explicit dump assertions for selected mode and submit target.

3. Saved definition
   - Current product definition: Saved means saved guides only.
   - Landed: Saved has an explicit empty destination and copy clarifying that
     threads/sections are not part of this tab.
   - Remaining: verify persistence, selected nav state, and Back behavior in a
     screenshot/dump proof once a Saved state is included in the state pack.

4. Emergency nav semantics
   - First-pass fix landed locally after this note: tablet emergency rail
     `Library` exit now presents as `Manual` with an explicit Manual-library
     content description.
   - Confirm Emergency is a safety route, not just another content tab. It
     should preserve emergency-first language and avoid implying normal
     browsing, search-result ownership, or reversible low-priority navigation.
   - Tablet rail work around `ba236b5` should be checked for accessibility
     labels, selected state, and back-stack behavior.

5. Back behavior
   - Landed: `DetailActivity` system Back and visible chrome Back share the
     same fallback path; the hidden long-press Back-to-Home shortcut is
     disabled.
   - Specify expected back-stack outcomes from Home -> Search -> Detail,
     Home -> Ask -> Answer, Emergency, Saved, and restored thread states.
   - Validate Android system Back, app up/back controls, and rotation restore
     together; visual parity alone will not catch this.

6. Normalized screenshot review
   - Continue reviewing screenshots against `artifacts/mocks`, but normalize
     the review packet: current screenshot path, target mock path, XML dump
     path, role/device/posture, commit SHA, state-pack summary, and whether the
     issue is visual drift or functional UX behavior.
   - Keep functional UX findings separate from pure MAE/mock-parity drift so
     future workers can route bugs to the right owner.

## Suggested Next Proof

- Run a focused state-pack or equivalent manual proof that captures Ask,
  Search, Emergency, Saved, and Back paths across phone/tablet portrait and
  landscape.
- Attach normalized screenshot/dump references for every finding before
  dispatching source changes.

## Remaining Phased Plan

1. **Structural mock fidelity**
   - Tablet portrait thread/follow-up should regain the target right-side
     `SOURCES` pane while preserving inline source chips for mobile and
     single-column layouts.
   - Home/search tablet geometry still needs a full four-posture proof after
     nav/type token cleanup.

2. **Token and chrome unification**
   - Align Compose vertical rail icon size, label font size, and line height
     with the XML Rev03 rail treatment.
   - Continue converging top chrome typography/action sizing across
     `TopBar.kt`, `TabletDetailScreen.kt`, and the Java emergency/detail
     overlay.

3. **Functional closure**
   - Run or unblock connected prompt-harness tests for Ask/Search once Gradle
     verification metadata for UTP artifacts is intentionally handled.
   - Add Saved and Back-stack variants to the proof matrix rather than relying
     only on visual home/search/detail captures.

4. **Final proof**
   - Capture a full four-posture state pack with all 22 canonical targets.
   - Review each finding with current screenshot path, target mock path, XML
     dump path, role/device/posture, commit SHA, and whether the issue is
     visual drift or functional behavior.
