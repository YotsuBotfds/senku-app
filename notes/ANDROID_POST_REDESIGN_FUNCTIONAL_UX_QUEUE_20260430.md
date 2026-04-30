# Android Post-Redesign Functional UX Queue - 2026-04-30

Worker M tracking note only. No Android source or test edits in this lane.

## Context

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
   - First-pass fix landed locally after this note: empty auto-Ask handoff is
     now a unit-covered Ask lane entry instead of a search detour.
   - Verify that every visible Ask affordance submits to the Ask/answer flow,
     including tablet rail buttons, restored detail state, IME Enter, and
     hardware Enter.
   - Regression risk: a visually correct button can still fall through to
     Search ownership after rotation or state restoration.

2. Ask/Search separation
   - Keep Search as retrieval/navigation and Ask as answer generation/composer
     handoff. Do not let shared input plumbing blur labels, submit targets, or
     restored selected-tab ownership.
   - Future proof should include both phone and tablet, portrait and landscape,
     with explicit dump assertions for selected mode and submit target.

3. Saved definition
   - Define what "Saved" means before polishing visuals: saved guide,
     saved answer, saved source, saved thread, or a generic bookmark.
   - Then align icon state, empty state, persistence, and navigation return
     semantics to that definition.

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
   - First-pass fix landed locally after this note: `DetailActivity` system Back
     now calls the same fallback path as visible chrome back.
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
