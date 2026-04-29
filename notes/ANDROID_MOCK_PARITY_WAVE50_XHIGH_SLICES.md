# Android Mock Parity Wave50 XHigh Slices

Planner/reviewer lane only. Do not edit Android source from this note. Treat
`artifacts/mocks` as canonical and compare every generated candidate against
those 22 flat PNGs.

## Baseline

- Canonical target pack: `artifacts/mocks`, exactly 22 PNGs:
  `home-*`, `search-*`, `thread-*`, `guide-*`, `answer-*` across phone/tablet
  portrait/landscape, plus `emergency-phone-portrait.png` and
  `emergency-tablet-portrait.png`.
- Last green canonical export in the phase notes: `wave48_integrated_b`
  (`20260428_183602`, 22/22 pass).
- Freshest attempted pack: `artifacts/ui_state_pack/wave49_integrated_b/20260428_185525`
  failed 21/22. `guide-phone-landscape.png` was not exported because
  `PromptHarnessSmokeTest#guideDetailShowsRelatedGuideNavigation` failed with
  `first related guide row should be a button`. Its raw screenshot/dump exist,
  but the canonical mock pack and zip are missing that target.
- Current worktree is dirty in detail formatter/layout/test files. Every worker
  must start with `git status --short`, `git diff --stat`, and a quick ownership
  check before touching any Android file.

## Prioritized Slices

| Pri | Slice | Target screenshots | Owned file areas | Acceptance checks |
| --- | --- | --- | --- | --- |
| P0 | Restore 22/22 export and guide phone-landscape contract | `guide-phone-landscape.png` first, then full 22-pack | `PromptHarnessSmokeTest.java`, `DetailActivity.java`, `activity_detail.xml` variants, `DetailRelatedGuidePresentationFormatter.java`, guide-related tests | `wave50` focused phone-landscape guide run passes; generated `mocks/` contains all 22 canonical names; first related guide row remains clickable/button-like without reintroducing the portrait cross-reference drawer. |
| P1 | Guide reader layout closure | `guide-phone-landscape.png`, `guide-tablet-landscape.png`, `guide-tablet-portrait.png`, `guide-phone-portrait.png` | `DetailActivity.java`, `DetailGuidePresentationFormatter.java`, `DetailGuideContextPresentationFormatter.java`, `GuideBodySanitizer.java`, guide paper/link drawables, `activity_detail` landscape/tablet XML, guide tests | Phone landscape has a true left section rail with rows 1-7 and a paper body, not a right-heavy related-guide surface. Tablet landscape keeps section rail, centered paper, and `CROSS-REFERENCE - 6` style rail without top chrome overlap. Phone portrait shows guide page plus bottom tabs, no first-viewport cross-reference drawer. |
| P2 | Answer article/source hierarchy | `answer-phone-portrait.png`, `answer-phone-landscape.png`, `answer-tablet-portrait.png`, `answer-tablet-landscape.png` | `DetailAnswerBodyFormatter.java`, `DetailAnswerPresentationFormatter.java`, `DetailSourcePresentationFormatter.java`, `DetailRelatedGuidePresentationFormatter.java`, source/card drawables, answer/source tests | First viewport is the GD-345 rain-shelter answer article plus `UNSURE FIT`; source cards order exactly `GD-220 ANCHOR 74%`, `GD-132 RELATED 68%`, `GD-345 TOPIC 61%`; related-guide rows land in tablet/landscape visible area; no proof/provenance/workflow vocabulary leaks. |
| P3 | Thread transcript rhythm and rail cleanup | `thread-phone-portrait.png`, `thread-phone-landscape.png`, `thread-tablet-portrait.png`, `thread-tablet-landscape.png` | `DetailThreadHistoryRenderer.java`, `DetailTranscriptFormatter.java`, `ThreadRail.kt`, thread rail drawables, thread/tablet tests | Q1/A1/Q2/A2 copy, times, anchors, and confidence match the mock. Wide rails show only `GD-220` and `GD-345`; remove `QUESTION USER`, `Rule match`, `Open proof`, and answer-detail residue from the first viewport. Footer reads quiet thread-context copy. |
| P4 | Emergency danger surface polish | `emergency-phone-portrait.png`, `emergency-tablet-portrait.png` | `DetailActionBlockPresentationFormatter.java`, `DetailWarningCopySanitizer.java`, `EmergencySurfacePolicy.java`, emergency banner/action/why drawables, emergency tests | Header keeps burn-hazard identity and danger pill; orange separator/banner is visually dominant; four numbered actions are separate rows; why card is flat and uses `GD-132 ANCHOR 93%`; footer context and placeholder match the target. |
| P5 | Home/Search residual density | `home-*`, `search-*` all four postures | `MainActivity.java`, `MainPresentationFormatter.java`, `HomeGuidePresentationFormatter.java`, `SearchResultAdapter.java`, `SearchResultCard.kt`, `CategoryShelf.kt`, `activity_main`/`list_item_result` variants, home/search drawables/tests | Search row stays flat with query `rain shelter`, metadata `4 RESULTS - 12MS`, scores `92/78/74/61`, and GD-023 preview. Home keeps six category cards and three recent threads visible with calmer stroke/type weight. No wholesale shell rewrite unless detail screens are already closed. |

## Delegation Order

1. Worker 1 - P0/P1 shell unblock: own the failed guide phone-landscape export
   and any shared detail-shell/layout prerequisite. This worker must land before
   others change guide/detail layout files.
2. Worker 2 - P1 guide formatter/reader: after Worker 1 stabilizes shell
   ownership, finish guide paper, section rail, required-reading, and
   cross-reference fidelity inside formatter/drawable boundaries.
3. Worker 3 - P2 answer: run after Worker 1 if touching `DetailActivity` or
   tablet rail sizing; otherwise stay in answer/source formatter files and
   tests.
4. Worker 4 - P3 thread: can run parallel with Worker 3 only if it stays in
   thread renderer/rail files and does not alter shared detail shell.
5. Worker 5 - P4 emergency, then P5 home/search if time: emergency has detail
   formatter overlap, so avoid touching files already owned by Workers 1-4.
   Home/search polish is lower-risk and should wait until the 22-pack is green.
6. Main integrator: serialize any shared-file collisions, run focused checks for
   each slice, then run the full local quality gate and four-role pack. Do not
   accept a closure pack unless `summary.json` is pass, `goal_mock_pack.status`
   is pass, `mocks/` has exactly the 22 names from `artifacts/mocks`, and the
   zip path is non-null.

## Global Acceptance

- Use `artifacts/mocks` as the visual target, not a generated previous pack.
- Every worker cites target PNG, current generated PNG, matching XML dump, APK
  SHA, and whether each remaining delta is content/data, layout/density, or
  behavior.
- Final closure requires no missing `guide-phone-landscape.png`, no mojibake or
  `GD-?` fallback strings in dumps, no `PROOF RAIL`/`Answer source selected`
  residue, deterministic `4:21`/`04:21` goal frame, and no live OS chrome in the
  exported canonical mocks.
