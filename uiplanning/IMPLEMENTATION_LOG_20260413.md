# Senku Mobile UI Implementation Log - 2026-04-13

This file is the compact continuation log for the Android UI rework.

## Guiding Principle

Field Resiliency.

The UI is being treated as a field tool, not a content browser. Design choices should favor:

- instant-first hierarchy for deterministic responses
- visible guide anchors so users know the source of truth
- low-glare, high-contrast controls with large tap targets
- context-aware next steps that keep the thread useful under stress

## Constraints Kept Stable

- Do not touch guide-content files during UI work.
- Preserve harness-sensitive IDs and activity contracts.
- Keep `MainActivity` as the app entry surface.
- Keep `detail_*` IDs stable even when the visual layout changes.

## Landed So Far

### Phase 1 Home / Results

- Home screen reworked into a field-manual style layout.
- Results migrated from `ListView` to `RecyclerView`.
- Category tiles now have icons and real guide counts.
- Search state now collapses browse chrome for active query flows.
- Home/results behavior was tightened to avoid stale cards and repeated forced keyboard focus.

### Detail / Thread

- Detail screen now uses a custom top bar and thread-style layout.
- Answer mode exposes:
  - route chip
  - source-count scope chip
  - thread/session history surface
  - follow-up composer
- Current deterministic sample now shows:
  - `Anchor GD-394`
  - `Instant deterministic`

### Resiliency Layer

- Added severity-ready accent tokens in `colors.xml` for future alert/warning theming.
- Added compact answer-mode summary copy intended to reclaim some above-the-fold space.
- Added inline source-chip container inside the answer bubble.
- Moved inline source chips above the long answer text after live validation showed the below-body version was too buried for field use.
- Added inline "next practical steps" chips inside the answer bubble so follow-on actions are visible before the long instruction body.

## Verified Artifacts

- `artifacts/ui_answer_mode_resilient_retry_20260413.xml`
- `artifacts/ui_preview_answer_mode_resilient_retry_20260413.png`
- `artifacts/ui_preview_answer_mode_resilient_scrolled_20260413.png`
- `artifacts/ui_answer_mode_inline_sources_abovebody_20260413.xml`
- `artifacts/ui_preview_answer_mode_inline_sources_abovebody_20260413.png`
- `artifacts/ui_answer_mode_practical_steps_20260413.xml`
- `artifacts/ui_preview_answer_mode_practical_steps_20260413.png`
- `artifacts/ui_answer_mode_header_collapse_20260413.xml`
- `artifacts/ui_preview_answer_mode_header_collapse_20260413.png`
- `artifacts/ui_preview_tablet_home_baseline_20260413.png`
- `artifacts/ui_answer_mode_tablet_responsive_20260413.xml`
- `artifacts/ui_preview_answer_mode_tablet_responsive_20260413.png`
- `artifacts/ui_preview_tablet_home_responsive_20260413.png`
- `artifacts/ui_answer_mode_tablet_anchorchips_20260413.xml`
- `artifacts/ui_preview_answer_mode_tablet_anchorchips_20260413.png`
- `artifacts/ui_preview_tablet_dashboard_home_20260413.png`
- `artifacts/ui_preview_tablet_dashboard_home_v2_20260413.png`
- `artifacts/ui_tablet_dashboard_home_v2_20260413.xml`
- `artifacts/ui_tablet_portrait_fallback_20260413.png`
- `artifacts/ui_tablet_portrait_fallback_20260413.xml`
- `artifacts/ui_tablet_landscape_station_20260413.png`
- `artifacts/ui_tablet_landscape_station_20260413.xml`
- `artifacts/ui_tablet_station_richcards_20260413.png`
- `artifacts/ui_tablet_station_richcards_20260413.xml`

## Current Implementation Notes

- `currentSources` remains the canonical answer-source payload.
- Inline guide-ID parsing from the answer body is only a fallback strategy and ordering aid for deterministic answers.
- `detail_sources_panel` remains the deeper inspection surface; inline chips are the quick verification surface.
- Related-path suggestions are now split into:
  - inline next-practical-step chips for above-the-fold continuation
  - retained lower panel code as a fallback/deeper surface
- Time snapshot for this continuation lane: `2026-04-13 19:45:51 -05:00`
- Additional continuation snapshot: `2026-04-13 19:48:30 -05:00`
- Additional continuation snapshot: `2026-04-13 20:33:27 -05:00`
- Answer-mode top chrome now collapses into a compact header rail:
  - top title becomes `GUIDE-ID • primary title`
  - header meta carries route and turn count
  - hero panel hides in answer mode unless it is actively showing status/progress

## Subagent Recommendations Captured

### Source Chips

- Safest approach: keep existing `detail_sources_*` IDs and contracts intact.
- Render compact tappable chips under the answer body.
- Prefer `currentSources` for chip generation.
- Use deterministic inline refs like `[GD-394, GD-031]` only as fallback ordering or coverage when needed.

### Survival UX Directives

- Distinguish instant/deterministic from AI-generated visually and behaviorally.
- Keep a primary guide anchor visible in every thread.
- Maintain large, high-contrast controls for critical actions.
- Offer related survival-path actions without losing thread context.

## Open Follow-Through Items

- Confirm inline source chips render cleanly on-device after the latest patch.
- Decide whether the top hero should collapse more aggressively in answer mode.
- Make related survival-path actions more obviously visible in the live answer surface.
- Consider dynamic severity accents only on header/source layers first, not full-screen.

## Recommended Next Slice

1. Validate and polish inline source chips in live answer mode.
2. Reduce answer-mode intro chrome further so more answer text is above the fold.
3. Surface related-path actions more clearly once the verification layer feels solid.

- Responsive validation is now an active UI requirement; tablet and aspect-ratio checks are being added alongside phone verification.
- Tablet validation lane is now active on `Senku_Tablet` (`emulator-5556`), cloned from the working Senku AVD family on Android `36.1` after the stock `Pixel_Tablet` image proved unreliable for installs.
- First responsive hardening pass landed in the detail flow:
  - answer/detail bubbles now cap width on wide layouts for readability
  - hero chips now sit in a horizontally scrollable rail instead of risking overflow clipping
  - `DetailActivity` now uses `adjustResize` so the follow-up composer stays safer under the tablet IME
- Tablet answer-mode validation is now explicit:
  - compact header rail shows `GD-394 • Fire in Wet Conditions`
  - materials strip is confirmed live on-device with `Bark`, `Split wood`, `Flat stones`, and `Dry twigs`
  - answer bubble now stays to a readable left-column width instead of stretching across the full tablet
- Main/home screen now uses responsive `dimen` overrides for `sw600dp`:
  - wider edge breathing room on tablet
  - larger card spacing without changing IDs or hierarchy
  - phone layout remains on the tighter default spacing lane
- Tablet home now has a dedicated `layout-sw600dp/activity_main.xml` split dashboard:
  - left rail for pack state, active thread memory, and developer tools
  - right pane for search/actions, a 3-column category knowledge grid, and a live results surface
  - second validation pass rebalanced the right pane so categories occupy the upper browse zone and results keep a dedicated lower pane
- Orientation split is now explicit in resources:
  - `layout-sw600dp-land/activity_main.xml` is the tablet station path
  - `layout-sw600dp-port/activity_main.xml` is the simpler single-column fallback
- Emulator validation note:
  - on `Senku_Tablet`, the natural orientation means `rotation=0` is the wide station mode
  - `rotation=1` is the tall clipboard-like mode
- Tablet result rows now have a richer large-screen rendering lane:
  - `res/layout-sw600dp/list_item_result.xml` increases card padding and copy scale
  - `SearchResultAdapter` now allows more title/meta/snippet density on `sw600dp` devices without changing retrieval or business logic
- Orientation-aware continuation note is now broken out in `uiplanning/TABLET_LAYOUT_NOTES_20260413.md` so the station-vs-clipboard model, back behavior, and next tablet-detail targets stay easy to recover after compaction
- Tablet planning decisions are now also captured in `uiplanning/DECISIONS.md` under the `Tablet Decisions Addendum (2026-04-13)` section so posture and navigation assumptions are no longer chat-only
- Tablet screen-level guidance is now also captured in `uiplanning/SCREEN_SPECS_TABLET_ADDENDUM_20260413.md` so the station-vs-clipboard model is tied back to Home, Results, and Detail behavior
- Inline source verification now follows a leaner anchor pattern on answer screens:
  - first chip can become a primary-guide anchor chip (`GD-394 anchor`)
  - same-guide supporting chips now collapse to section-only labels like `Fire-Starting in Damp`
  - cross-guide support remains explicit, for example `GD-024 | Fire Starting in Extre...`
- Additional continuation snapshot: `2026-04-13 20:41:03 -05:00`
- Additional continuation snapshot: `2026-04-13 20:42:30 -05:00`
- First `layout-sw600dp-land/activity_detail.xml` station-detail pass is now live:
  - left column stays focused on the active question and answer only
  - right rail now has dedicated surfaces for materials, sources, and related next steps
  - utility chips are hidden from the left answer column in wide tablet mode so the reading flow is cleaner
- Thread-history behavior was tightened for the wide rail:
  - long duplicated answer summaries are now compacted before rendering into history bubbles
  - station mode hides the session-history rail entirely for now because it was pushing more useful tablet panels below the fold
- Validated wide-detail artifact from the first successful station replay:
  - `artifacts/ui_tablet_station_detail_20260413.xml`
  - `artifacts/ui_tablet_station_detail_20260413.png`
- Follow-up station replay artifacts after the utility-rail cleanup:
  - `artifacts/ui_tablet_station_detail_v2_20260413.xml`
  - `artifacts/ui_tablet_station_detail_v2_20260413.png`
- Harness note:
  - a later replay intended for `ui_tablet_station_detail_v3_20260413.xml` dropped back to the launcher even though the build installed cleanly
  - treat that dump as a harness flake, not as a UI-state artifact
- Product/IA second-opinion note is now linked from `AGENTS.md` and should remain part of the continuation floor:
  - `notes/UI_SECOND_OPINION_20260413.md`
- Guidance pulled forward from that note:
  - protect a stable answer spine in answer mode: route family + anchor guide + source count
  - clarify the home entry lanes so `Find guides` and `Ask from guides` feel distinct
  - keep source navigation first-class
  - keep heuristic helpers visually separate from provenance-backed evidence
- Low-risk IA-alignment pass landed afterward:
  - home strings now distinguish `Find Guides` from `Ask From Guides`
  - answer-mode compact meta now includes source count alongside route and turn count
  - `renderSources()` now has null-safety for future detail-layout variants
  - wide-tablet next-step rail is capped so helper suggestions stay subordinate to evidence
- Wide-tablet continuity pass landed after that:
  - station mode no longer drops thread continuity completely
  - the right rail now shows a compact thread strip when earlier turns exist
  - that strip renders only the latest earlier exchange so continuity stays visible without rail bloat
- Additional continuation snapshot: `2026-04-13 20:50:24 -05:00`
- Tall-tablet clipboard detail layout is now live:
  - added `res/layout-sw600dp-port/activity_detail.xml`
  - keeps the same `detail_*` IDs and contracts
  - uses a centered reading column with wider gutters instead of the landscape station split
  - keeps sources/session/next-steps below the main reading flow rather than in a side rail
- Tablet posture sweep validated on `emulator-5556`:
  - wide station dump: `artifacts/ui_tablet_station_posturecheck_20260413.xml`
  - wide station screenshot: `artifacts/ui_tablet_station_posturecheck_20260413.png`
  - tall clipboard dump: `artifacts/ui_tablet_clipboard_posturecheck_20260413.xml`
  - tall clipboard screenshot: `artifacts/ui_tablet_clipboard_posturecheck_20260413.png`
- Validation read:
  - `rotation=0` correctly loads the station-style rail with materials, next steps, and sources
  - `rotation=1` correctly loads the clipboard-style centered reading column with inline materials/sources/next-steps
  - answer-mode top spine now shows route + anchor + source count in both tablet postures
- Retrieval-confidence note was reviewed and implemented in the low-risk lane:
  - the app does not have a calibrated user-facing confidence score today
  - internal ranking scores exist, but they are heuristic and not safe to expose as `% confidence`
  - answer-mode top spine now uses an honest `evidence strength` label derived from existing provenance signals
- Evidence-strength validation artifacts:
  - `artifacts/ui_phone_evidence_strength_20260413.xml`
  - `artifacts/ui_phone_evidence_strength_20260413.png`
  - `artifacts/ui_tablet_evidence_strength_20260413.xml`
  - `artifacts/ui_tablet_evidence_strength_20260413.png`
- Home intent split is now reinforced in layout copy:
  - phone and tablet home layouts now include a small lane hint under the primary actions
  - the hint explains `Find guides` versus `Ask from guides` in plain language
- Additional continuation snapshot: `2026-04-13 21:00:19 -05:00`
- Additional continuation snapshot: `2026-04-13 21:02:31 -05:00`
- Additional continuation snapshot: `2026-04-13 21:03:30 -05:00`
- `Why this answer` surface is now live across detail layouts:
  - added `detail_why_panel` and `detail_why_text`
  - moved the panel into the answer bubble so it remains above the long instruction body
  - summary explains route family, anchor guide, retrieval lane, and evidence strength in plain language
- Validation artifacts for the visible rationale surface:
  - `artifacts/ui_phone_why_answer_v2_20260413.xml`
  - `artifacts/ui_phone_why_answer_v2_20260413.png`
  - `artifacts/ui_tablet_why_answer_v2_20260413.xml`
  - `artifacts/ui_tablet_why_answer_v2_20260413.png`
- Additional continuation snapshot: `2026-04-13 21:06:05 -05:00`
- Home lane-hint validation is now explicitly confirmed on both phone and tablet:
  - `artifacts/ui_home_lanehint_phone_20260413.xml`
  - `artifacts/ui_home_lanehint_phone_20260413.png`
  - `artifacts/ui_home_lanehint_tablet_20260413.xml`
  - `artifacts/ui_home_lanehint_tablet_20260413.png`
- Detail hierarchy pass landed to separate evidence from helpers more clearly:
  - added quieter helper drawables: `bg_helper_panel.xml`, `bg_helper_pill.xml`, `bg_helper_chip.xml`
  - added a heavier evidence shell: `bg_evidence_panel.xml`
  - `Why this answer` now uses the evidence shell
  - materials and next-step surfaces now use helper shells/chips with explicit verify-against-evidence copy
  - severity tinting was narrowed back to the header chips so helper panels no longer inherit the same authority signal
- Validation artifacts for helper-vs-evidence pass:
  - `artifacts/ui_phone_helper_evidence_20260413.xml`
  - `artifacts/ui_phone_helper_evidence_20260413.png`
  - `artifacts/ui_tablet_helper_evidence_20260413.xml`
  - `artifacts/ui_tablet_helper_evidence_20260413.png`
- Phone/tall-tablet answer flow was then reordered to match the product spine more closely:
  - inline sources remain first
  - `Why this answer` now appears before helper checklist items
  - inline next-step prompts were moved below the long answer body in the non-rail layouts
- Validation artifacts for reordered answer flow:
  - `artifacts/ui_phone_helper_evidence_v2_20260413.xml`
  - `artifacts/ui_phone_helper_evidence_v2_20260413.png`
  - `artifacts/ui_tablet_helper_evidence_v2_20260413.xml`
  - `artifacts/ui_tablet_helper_evidence_v2_20260413.png`
- Read on reordered flow:
  - phone now reads `source -> rationale -> materials -> instruction` much better above the fold
  - wide-tablet station remains stable because helper surfaces stay in the rail
- Additional continuation snapshot: `2026-04-16 19:06:00 -05:00`
- Reviewed-chunk workflow was reused for the next pair:
  - drafted the next slice candidates in `uiplanning/UI_CHUNK_PLAN_20260416.md`
  - sent the narrowed pair through a `gpt-5.4` xhigh review
  - split implementation across disjoint high-worker lanes
- Second reviewed pair landed:
  - `Chunk 4a / Trust spine continuity`
    - `DetailActivity` now keeps backend/source-aware trust copy steadier while generated answers move from retrieval -> building -> ready
    - `OfflineAnswerEngine` now exposes clearer backend/source-aware in-flight status builders for generation, source-ready, stall, and completion
    - `PromptHarnessSmokeTest` now asserts that settled generated answers keep trust meta and visible sources instead of only checking body text
  - `Chunk 3a / Minimal browse bridges`
    - `MainActivity` prefetches a tiny top-N linked-guide signal after result sets settle
    - `SearchResultAdapter` renders a compact `Linked guides` cue on guide-backed rows that have adjacent-path availability
- Real regression caught during validation:
  - direct emulator browse proof exposed a crash in `SearchResultAdapter.bindLinkedGuideCue(...)`
  - root cause: `layout-land/list_item_result.xml` and `layout-sw600dp/list_item_result.xml` did not yet define `result_related_cue`
  - fix:
    - added `result_related_cue` to those layout variants
    - added null safety in `SearchResultAdapter.bindLinkedGuideCue(...)`
- Validation for the second reviewed pair now sits on the fast/stable lanes:
  - trust-lane build + unit coverage passed with:
    - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
  - browse-bridge proof artifacts:
    - `artifacts/live_debug/browse_bridge_fire_20260416.xml`
    - `artifacts/live_debug/browse_bridge_fire_20260416.png`
  - crash diagnosis log:
    - `artifacts/live_debug/browse_bridge_crash_20260416.logcat.txt`
  - post-fix basic emulator smoke passed:
    - `artifacts/instrumented_ui_smoke/20260416_190420_094/emulator-5554/summary.json`
- Additional continuation snapshot: `2026-04-16 19:18:00 -05:00`
- Third reviewed-chunk cycle landed after another xhigh review + two high-worker lanes:
  - `Chunk 2b completion / Home hub depth`
  - `Chunk 5a / Tablet station linked-guide promotion`
- Home hub depth lane:
  - `Continue exploring` matured into a clearer `Guide connections` surface
  - home now shows:
    - source-aware subtitle framing based on recent-thread vs pinned-guide anchor source
    - persistent anchor text while a valid anchor exists
    - a compact empty state when an anchor exists but no linked guides surface
    - a dedicated horizontal shelf container so the graph lane reads separately from `Recent threads` and `Pinned guides`
- Wide-tablet station lane:
  - guide-mode related-guide navigation is now promoted into a dedicated cross-reference rail in `sw600dp-land`
  - answer-mode helper prompts remain on the helper path
  - the rail is explicitly framed as cross-reference movement rather than helper chrome
- Validation for the third reviewed pair:
  - rebuild/install fast lane passed:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - phone basic regression smoke passed:
    - `artifacts/instrumented_ui_smoke/20260416_191516_594/emulator-5554/summary.json`
  - wide-tablet guide-detail smoke passed:
    - `artifacts/instrumented_ui_smoke/20260416_191556_818/emulator-5560/summary.json`
  - seeded home capture for the stronger graph shelf:
    - `artifacts/live_debug/home_hub_recent_20260416.xml`
    - `artifacts/live_debug/home_hub_recent_20260416.png`
- Additional continuation snapshot: `2026-04-13 21:13:12 -05:00`
- Top-rail compaction pass landed afterward:
  - compact phone anchor chip now uses the shorter guide-id-only form
  - compact phone meta line now uses shorter route/source/turn wording so header trust cues waste less space
- Harness note:
  - one follow-up phone replay for `ui_phone_toprail_compact_20260413.*` fell back to the launcher and should be treated as invalid
- GLM sidecar note:
  - `scripts/run_opencode_prompt.ps1` successfully launched an OpenCode `glm-5.1` run
  - current harness metadata/logging preserved run metadata but not a usable model text body for this read-only UI review
  - treat the GLM path as promising but not yet reliable for continuation-critical review output until capture is tightened
- Additional emulator lane note:
  - attempted to boot `Senku_Parallel_4` on `emulator-5560` as a dedicated landscape-phone lane
  - as of `2026-04-13 21:15:14 -05:00`, the emulator is still stuck `offline`; do not treat it as a validated lane yet
- Tablet follow-on idea recorded from the live UI review:
  - provenance rail is a strong next tablet-station feature after core thread polish
  - preferred behavior: tapping a source chip on wide tablet opens an inline side rail with the cited snippet and local match highlight instead of forcing a full screen jump
  - phone and tall-tablet should keep the simpler guide-jump behavior for now
- Additional continuation snapshots:
  - `2026-04-13 21:17:34 -05:00`
  - `2026-04-13 21:20:22 -05:00`
  - `2026-04-13 21:28:04 -05:00`
  - `2026-04-13 21:30:40 -05:00`
  - `2026-04-13 21:39:34 -05:00`
- Expanded emulator grid is now live:
  - `emulator-5554` = main phone
  - `emulator-5556` = main tablet (`Senku_Tablet`)
  - `emulator-5560` = second phone (`Senku_Parallel_4`)
  - `emulator-5562` = second tablet (`Senku_Tablet_2`)
  - `emulator-5564` = medium-phone validation lane (`Medium_Phone_API_36.1`)
- Second tablet lane is confirmed usable on home:
  - `artifacts/ui_tablet2_home_active_20260413.png`
  - `artifacts/ui_tablet2_home_active_20260413.xml`
- Phone-landscape validation moved to dedicated lanes:
  - `5560` exposed the first landscape issue and then behaved more like a large-phone/wrapper-timing lane than a clean compact-phone proxy
  - `5564` was added as the cleaner compact landscape-phone reference
- `5564` install lane findings:
  - old uncertainty was not an old-build mismatch; the first reinstall attempt failed because the emulator reported `Requested internal only, but not enough space`
  - after cache cleanup and a clean uninstall/reinstall, `5564` accepted the current APK
  - for `5564`, direct `adb shell am start ...` launches proved more reliable than `scripts/run_android_prompt.ps1`, which produced false launcher/fallback noise on that emulator
- Landscape-phone compacting pass landed in `DetailActivity.java`:
  - `wideLayoutActive()` now stays tablet-only
  - non-tablet landscape now compacts the follow-up shell
  - non-tablet landscape also compacts question/answer bubble padding and edge offsets
  - non-tablet landscape now hides the duplicated question subtitle line because the top rail already carries route/evidence context
- Trusted compact-landscape phone artifacts:
  - `artifacts/ui_medium_phone_landscape_manual_20260413.xml`
  - `artifacts/ui_medium_phone_landscape_manual_20260413.png`
  - `artifacts/ui_medium_phone_landscape_compact_20260413.xml`
  - `artifacts/ui_medium_phone_landscape_compact_20260413.png`
- Validation read on compact landscape phone:
  - the top rail is now appropriately terse: `Instant • Strong evidence • 3 src • 1 turn`
  - the anchor chip is shortened to guide id only on phone-width layouts
  - compacting improved the above-the-fold budget, but phone landscape still remains a constrained posture that may benefit from a future tap-to-expand follow-up composer instead of a permanently visible full-width input
- Artifact caution:
  - the later `ui_medium_phone_landscape_compact_v2_20260413.*` capture sequence was contaminated by emulator navigation/app-drawer state and should not be treated as authoritative UI validation
- Spark sidecar review result was directionally confirmed:
  - the original rotated-phone wide-layout bug is fixed
  - remaining weirdness on secondary phone lanes is more consistent with harness/emulator timing than with a tablet-layout branch leaking onto phones
- Additional continuation snapshots:
  - `2026-04-13 22:20:41 -05:00`
  - `2026-04-13 22:24:23 -05:00`
- Spark review outputs used for this slice:
  - compact phone-landscape: keep the change Java-only and tighten `applyFollowUpLayoutMode()` first
  - answer hierarchy: remove duplicate subtitle noise in answer mode and keep route/anchor/evidence in the top rail
  - next tablet slice: build a station-only provenance preview in the wide-tablet source rail
- Compact landscape-phone follow-up pass landed in `DetailActivity.java`:
  - answer-mode question subtitle now stays hidden, not just on landscape phone but across answer mode generally
  - compact landscape phone now forces smaller follow-up input padding/min-height
  - compact landscape phone now forces a smaller `Go` button footprint instead of the full default button shell
- Fresh trusted medium-phone landscape validation on `emulator-5564`:
  - app rebuilt successfully and reinstalled cleanly
  - `scripts/run_android_prompt.ps1` succeeded on `5564` for `start a fire in rain`
  - current trusted dump: `artifacts/ui_medium_phone_landscape_harness_20260413.xml`
  - current trusted screenshot: `artifacts/ui_medium_phone_landscape_compact_live_20260413.png`
- Validation read on the refreshed `5564` detail state:
  - top rail now reads `Fire in Wet Conditions`
  - compact meta remains `Instant • Strong evidence • 3 src • 1 turn`
  - anchor chip remains `GD-394`
  - the duplicate question subtitle line is gone in answer mode
  - the follow-up composer now reads as a compact utility strip: `Ask follow-up...` + `Go`
- Wide-tablet provenance lane is now implemented for station mode only:
  - new wide-tablet rail panel ids: `detail_provenance_panel`, `detail_provenance_meta`, `detail_provenance_body`, `detail_provenance_open`
  - source taps in `layout-sw600dp-land/activity_detail.xml` now populate a rail preview first instead of forcing an immediate full-screen jump
  - phone and tall-tablet behavior still jumps directly to the full guide, unchanged
  - preview body reuses the existing `buildGuideBody(...)` helper so the rail shows the cited source text without adding a second formatting path
- Tablet validation note for the provenance lane:
  - code compiles and installs cleanly on `5556` and `5562`
  - direct home relaunch on `5556` is healthy after install
  - current tablet ask harness remains unstable on both tablets for this slice: `run_android_prompt.ps1` returns a launcher-state dump with `com.senku.mobile` no longer running before completion
  - no `AndroidRuntime` crash trace was present in the quick logcat check, so treat this as a harness/emulator validation problem until disproven, not as a confirmed provenance-lane regression
- New/updated artifacts from this slice:
  - `artifacts/ui_medium_phone_landscape_harness_20260413.xml`
  - `artifacts/ui_medium_phone_landscape_compact_live_20260413.png`
  - `artifacts/ui_medium_phone_landscape_home_20260413.xml`
  - `artifacts/ui_tablet_station_provenance_20260413.xml` (launcher-state artifact; not authoritative)
  - `artifacts/ui_tablet2_station_provenance_20260413.xml` (launcher-state artifact; not authoritative)
  - `artifacts/ui_tablet_station_home_after_relaunch_20260413.xml`
  - `artifacts/ui_tablet_home_manual_entry_20260413.xml`
  - `artifacts/ui_tablet_home_manual_entry_20260413.png`
- Additional continuation snapshot: `2026-04-13 22:30:45 -05:00`
- Tablet validation harness hardening landed in `scripts/run_android_prompt.ps1`:
  - `WaitForCompletion` no longer aborts on the first missing `pidof com.senku.mobile`
  - the script now requires repeated misses before declaring the app gone
  - this was enough to recover clean `5556` tablet detail validation for the current UI slice
- Wide-tablet station rail hierarchy was then corrected:
  - source evidence now comes before helper panels in `layout-sw600dp-land/activity_detail.xml`
  - the provenance preview sits directly under the source buttons instead of being buried below materials/next steps
  - this aligns station mode with the `route -> anchor -> evidence -> follow-up` spine more honestly
- Final validated tablet-station provenance artifacts:
  - `artifacts/ui_tablet_station_provenance_retry_20260413.xml`
  - `artifacts/ui_tablet_station_provenance_retry_20260413.png`
  - `artifacts/ui_tablet_station_provenance_reordered_clean_20260413.xml`
  - `artifacts/ui_tablet_station_provenance_reordered_clean_20260413.png`
- Final validation read on the tablet provenance lane:
  - `5556` now stays in `DetailActivity` through the ask replay
  - the right rail now shows source buttons first
  - the provenance preview is visible in the same above-the-fold rail with:
    - `Source preview`
    - the selected source title/section
    - a cited excerpt body
    - `Open full guide`
  - helper panels remain available below the evidence block instead of competing with it for the top of the rail

## 2026-04-13 Late-Night Continuation

- Continuation snapshots:
  - `2026-04-13 23:04:26 -05:00`
  - `2026-04-13 23:05:24 -05:00`
  - `2026-04-13 23:05:43 -05:00`
  - `2026-04-13 23:06:21 -05:00`
  - `2026-04-13 23:07:28 -05:00`
  - `2026-04-13 23:08:21 -05:00`
  - `2026-04-13 23:09:20 -05:00`
- Async sidecar lane was used successfully as a true parallel worker:
  - task id: `ses_275d87e27ffeWrICep14jBW0Nr`
  - final useful recommendation: move `Why this answer` into the tablet station rail above sources, then visually separate heuristic helper panels from sourced evidence
  - result path: `artifacts/opencode/sidecar/tasks/ses_275d87e27ffeWrICep14jBW0Nr.result.json`
- Spark reviews were also used to keep the main thread clean:
  - compile-health review confirmed the three blockers in `DetailActivity.java`
  - tablet rail review confirmed the next low-risk step was selected-source state, not a broader refactor
- Compile-health repair landed cleanly in `DetailActivity.java` and drawable resources:
  - restored missing `formatCountLabel(...)`
  - restored the two-argument `showSourceProvenancePanel(source, button)` path
  - added selected-source state helper using `selectedSourceButton`
  - added `bg_source_link_selector.xml`
  - added `bg_source_link_active.xml`
  - `:app:compileDebugJavaWithJavac` and later `assembleDebug` both passed after the repair
- Verified selected-source station behavior on `emulator-5556`:
  - initial station artifact:
    - `artifacts/ui_tablet_station_selected_source_20260413.xml`
    - `artifacts/ui_tablet_station_selected_source_20260413.png`
  - validation read:
    - top rail meta now reads `Instant deterministic • Strong evidence • 3 sources • 1 turn`
    - first source button shows `selected="true"` in the dump
    - anchor chip remains `Anchor GD-394`
  - second-source tap proof:
    - `artifacts/ui_tablet_station_selected_source_tap2_20260413.xml`
    - `artifacts/ui_tablet_station_selected_source_tap2_20260413.png`
  - validation read after tap:
    - second source button flips to `selected="true"`
    - provenance preview updates to `Fire-Starting in Damp`
    - this confirms the station rail now keeps visual source selection and preview in sync
- A follow-up tablet station slice was attempted using the async sidecar recommendation:
  - moved `detail_why_panel` out of the answer bubble in `layout-sw600dp-land/activity_detail.xml`
  - added rail-order logic in `DetailActivity.java` so the intended station order becomes `why -> sources -> provenance`
- Current status on that `why`-rail slice:
  - code compiles and installs
  - but live `5556` validation still does **not** show `detail_why_panel` in the dump
  - the dump continues to surface `detail_sources_panel` at the top of the right rail
  - treat the `why`-rail move as **in progress / not yet validated**
  - do not claim the tablet trust strip is live until a real dump or screenshot shows the panel present above sources
- Harness read from this late pass:
  - `run_android_prompt.ps1` produced one contaminated launcher-state dump again during this window
  - direct `adb shell am start ... --ez auto_ask true` plus delayed dump was the more trustworthy tablet-detail validation path
  - direct launch confirmed `com.senku.mobile` stayed alive, so the launcher-state artifact was harness noise rather than a confirmed runtime crash
- New artifacts from this late pass:
  - `artifacts/ui_tablet_station_selected_source_20260413.xml`
  - `artifacts/ui_tablet_station_selected_source_20260413.png`
  - `artifacts/ui_tablet_station_selected_source_tap2_20260413.xml`
  - `artifacts/ui_tablet_station_selected_source_tap2_20260413.png`
  - `artifacts/ui_tablet_station_whyrail_20260413.xml` (launcher contamination; not authoritative)
  - `artifacts/ui_tablet_station_whyrail_20260413.png` (launcher contamination; not authoritative)
  - `artifacts/ui_tablet_station_whyrail_direct_20260413.xml`
  - `artifacts/ui_tablet_station_whyrail_direct_20260413.png`
  - `artifacts/ui_tablet_station_whyrail_detail_20260413.xml`
  - `artifacts/ui_tablet_station_whyrail_detail_20260413.png`
  - `artifacts/ui_tablet_station_whyrail_fixed_20260413.xml`
  - `artifacts/ui_tablet_station_whyrail_fixed_20260413.png`
- Immediate next recommended action:
  - debug why `detail_why_panel` remains absent in live wide-tablet dumps even after the rail-layout move and explicit rail-promotion logic
  - once that is solved, the next safest UI slice is visual separation of heuristic helper panels from evidence panels so related paths/materials stop reading like source citations

### Correction After Clean Reinstall Verification

- The earlier suspicion that wide-tablet `detail_why_panel` was still missing at runtime turned out to be a validation/deploy artifact, not a lasting UI regression.
- Clean verification path that resolved it:
  - full rebuild: `gradlew clean :app:assembleDebug`
  - explicit uninstall + reinstall on `emulator-5556`
  - confirmed package update time moved forward (`lastUpdateTime=2026-04-13 23:13:28`)
  - direct launch via `adb shell am start ... --ez auto_ask true`
  - delayed direct `uiautomator dump` instead of trusting the earlier contaminated harness capture
- Clean wide-tablet station artifacts after reinstall:
  - `artifacts/ui_tablet_station_cleanreinstall_20260413.xml`
  - `artifacts/ui_tablet_station_cleanreinstall_20260413.png`
- Corrected validation read from the clean reinstall dump:
  - `detail_why_panel` is present at the top of the right rail
  - `detail_why_text` is present and populated with the trust summary
  - right-rail order is now the intended station order:
    - `Why this answer`
    - `Guides behind this answer`
    - `Source preview`
  - header meta remains correct: `Instant deterministic • Strong evidence • 3 sources • 1 turn`
  - anchor chip remains `Anchor GD-394`
- Post-fix interaction proof:
  - `artifacts/ui_tablet_station_cleanreinstall_tap2_20260413.xml`
  - `artifacts/ui_tablet_station_cleanreinstall_tap2_20260413.png`
- Validation read from the post-fix tap artifact:
  - second source button flips to `selected="true"`
  - provenance preview switches to `Fire-Starting in Damp`
  - `detail_why_panel` remains present above the sources stack while the selected-source behavior still works
- Updated state:
  - treat the wide-tablet `why` rail as **validated live**
  - the remaining next-slice recommendation is no longer “make the why rail work”
  - the remaining next-slice recommendation is now **clearer visual separation between sourced evidence and heuristic helper panels**

### 2026-04-13 23:27:44 -05:00

- Landed a truthfulness pass for zero-source answer states in the Android detail UI:
  - `layout-sw600dp-land/activity_detail.xml`
    - added `detail_sources_subtitle` id so the tablet station rail can switch between normal provenance copy and an explicit no-citations message
  - `DetailActivity.java`
    - `renderSources()` no longer silently hides the right-rail sources panel for `answerMode && currentSources.isEmpty()` on wide tablet
    - the rail now shows an explicit no-citation subtitle instead of fabricating a normal evidence state
    - wide-rail source buttons now truncate to 2 lines with ellipsis instead of growing unpredictably
    - `renderWhyPanel()` now uses explicit no-citation copy when sources are absent
    - `buildCompactHeaderMeta()` no longer clamps empty source count to `1`
    - `getEvidenceStrengthLabel()` returns the no-citation label when there are no attached sources
    - `buildWhyThisAnswerSummary()` now early-returns honest no-citation text instead of inventing `the leading guide` / `1 source`
  - `strings.xml`
    - added `detail_why_no_citations`
    - added `detail_sources_subtitle_none`
    - added `detail_sources_unavailable_short`
- Validation:
  - `android-app\\gradlew.bat assembleDebug` passed after the no-citation fixes
  - fresh debug APK reinstalled successfully to:
    - `emulator-5556`
    - `emulator-5564`
- Pipeline state:
  - sidecar task `ses_275c83791ffelTcUNgfcfpejq0` completed with the recommendation to collapse/group helper panels on the wide-tablet station rail after evidence panels
  - sidecar task `ses_275c67977ffe3HYWnMCu5NTjfV` completed with concrete zero-source copy/behavior guidance; used directly in the truthfulness pass above
  - sidecar task `ses_275c20422ffecdVF72NDb0KLtL` is still running; it is reviewing the phantom inline-source chip risk (`sourcesForInlineVerification()`)
  - spark worker `Lovelace` (`019d8a3e-5fab-79d0-935d-03fcaf08cd62`) owns the harness-reliability lane for `scripts/run_android_prompt.ps1` and related script-only changes
- Current main-thread recommendation:
  - once the sidecar returns on phantom inline sources, decide whether to gate inline verification chips behind `currentSources` or relabel them as text references instead of provenance
  - after that, the strongest tablet station UI slice remains a collapsible helper section so the evidence rail keeps the top of the viewport

### 2026-04-13 23:36:00 -05:00

- Harness lane:
  - spark worker `Lovelace` updated `scripts/run_android_prompt.ps1`
  - key changes:
    - added stale artifact cleanup before runs
    - added UI-surface classification (`detail`, `results`, `home`, etc.)
    - added foreground checks for `com.senku.mobile`
    - added bounded relaunch recovery when auto-ask lands on `home` / `results`
    - added clearer timeout failure when detail completion is never reached
  - live proof:
    - `artifacts/ui_tablet_station_harness_retry_20260413.xml`
    - `artifacts/ui_tablet_station_harness_retry_20260413.png`
    - `artifacts/ui_tablet_station_harness_retry_v2_20260413.xml`
    - `artifacts/ui_tablet_station_harness_retry_v2_20260413.png`
  - validated behavior:
    - `run_android_prompt.ps1` recovered from a real `home` surface mislaunch on `emulator-5556`
    - after relaunch, the run landed in real detail mode and captured a clean tablet dump
- Provenance integrity lane:
  - sidecar task `ses_275c20422ffecdVF72NDb0KLtL` confirmed the phantom inline-source risk
  - landed the smallest fix in `DetailActivity.java`:
    - `sourcesForInlineVerification()` now returns only real attached sources
    - removed the regex-scraped `GD-###` fallback that previously fabricated inline verification chips from body text
  - state after fix:
    - inline verification chips no longer contradict a `no citations` state by surfacing synthetic guide references
- Wide-tablet helper-group lane:
  - created a new outer utility-rail helper shell in `layout-sw600dp-land/activity_detail.xml`
  - added `bg_helper_group.xml`
  - grouped these secondary/helper surfaces under one collapsible unit:
    - `detail_session_panel`
    - `detail_next_steps_panel`
    - `detail_materials_panel`
  - helper section behavior:
    - default state is collapsed
    - title updated to `Thread context`
    - collapsed summary now surfaces helper counts (`4 checklist items • 2 paths` in the live fire example)
    - toggle switches `Show` / `Hide`
    - session stays inside the grouped helper body instead of competing with the evidence rail
  - wide-tablet helper validation artifacts:
    - `artifacts/ui_tablet_helper_group_top_20260413.xml`
    - `artifacts/ui_tablet_helper_group_top_20260413.png`
    - `artifacts/ui_tablet_helper_group_scrolled_20260413.xml`
    - `artifacts/ui_tablet_helper_group_scrolled_20260413.png`
    - `artifacts/ui_tablet_helper_group_expanded_20260413.xml`
    - `artifacts/ui_tablet_helper_group_expanded_20260413.png`
    - `artifacts/ui_tablet_helper_group_counts_top_20260413.xml`
    - `artifacts/ui_tablet_helper_group_counts_scrolled_20260413.xml`
  - validated read from the scrolled/helper artifacts:
    - helper section is present as `detail_helper_section`
    - collapsed header shows `Thread context`
    - collapsed summary shows count-based context rather than decorative labels
    - toggle is tappable and flips to `Hide`
    - expanded state renders helper content under the grouped shell while the evidence rail remains above it
- Current recommendation after this slice:
  - the next strongest UI move is either:
    - tighten the expanded helper body so more of it is visible above the fold on tablet, or
    - add a dedicated no-citation validation replay once we have a reproducible zero-source answer case to drive

### 2026-04-13 23:46:09 -05:00

- Added a reproducible debug validation lane for no-citation answer states:
  - `MainActivity.java`
    - new hidden debug intent path via exported `MainActivity`
    - accepts:
      - `debug_open_detail`
      - `debug_detail_title`
      - `debug_detail_subtitle`
      - `debug_detail_body`
    - internally opens `DetailActivity.newAnswerIntent(..., Collections.emptyList(), ...)`
  - purpose:
    - lets shell-driven validation open a synthetic answer-mode detail screen with **zero attached sources** without violating Android exported-activity restrictions
- Live no-citation validation:
  - command path now works via `MainActivity` instead of direct shell launch into `DetailActivity`
  - validated artifacts:
    - `artifacts/ui_tablet_no_citations_state_v2_20260413.xml`
    - `artifacts/ui_tablet_no_citations_state_v2_20260413.png`
  - confirmed live wide-tablet state:
    - header meta reads `AI-generated answer • No citations • 1 turn`
    - anchor chip reads `Anchor unavailable`
    - `detail_why_text` shows the explicit no-citation warning copy
    - `detail_sources_subtitle` shows the explicit no-citation provenance fallback
    - helper section still renders as secondary context below the evidence rail
- Took the next spark recommendation for tablet helper density:
  - `DetailActivity.java`
    - wide-tablet helper group now allows up to `3` next-step actions instead of `2`
- Phone-landscape hierarchy pass:
  - used sidecar guidance from `ses_275b516f0ffe6s8ukX4dBDAQkW`
  - landed in `DetailActivity.java`
    - `detail_inline_next_steps_scroll` is now promoted directly under inline source chips on landscape phones
    - full-width `detail_next_steps_panel` is hidden on landscape phones to avoid duplication
    - reordering is idempotent so follow-up re-renders do not stack views incorrectly
  - validated landscape phone artifacts on `emulator-5564`:
    - `artifacts/ui_phone_landscape_inline_order_20260413.xml`
    - `artifacts/ui_phone_landscape_inline_order_20260413.png`
    - `artifacts/ui_phone_landscape_inline_order_v2_20260413.xml`
    - `artifacts/ui_phone_landscape_inline_order_v2_20260413.png`
  - confirmed live read from `v2` dump:
    - inline source chips render first
    - inline next-step chips (`Purify water using this fire`, `Turn this into a signal fire`) render immediately after source chips
    - `detail_why_panel` is pushed below them
    - redundant full-width `detail_next_steps_panel` is no longer present in the phone-landscape flow
- Current recommendation after this slice:
  - the strongest next move is a compact treatment for `detail_why_panel` on phone landscape, since it is now the next remaining vertical pressure point after sources + next-step chips

### 2026-04-13 23:57:02 -05:00

- Pipeline expansion:
  - launched three new sidecar reviews:
    - `ses_275ad5ce5ffe3XIEh9MYgy6nXZ` for phone-landscape `detail_why_panel` compaction
    - `ses_275ad5cc9ffeAJG0RCxw5yDIGV` for wide-tablet provenance rail clarity
    - `ses_275ad5cd6ffe1isAyfyFlNbuAi` for synthetic debug-detail replay harness shape
  - reused existing spark agents instead of opening new threads:
    - one spark lane produced a scripts-only helper for launching synthetic detail state
    - read-only spark lanes converged with the sidecars on two near-term UI moves:
      - compact `detail_why_panel` further on phone landscape
      - make the selected provenance source feel more explicit on wide tablet
- Landed phone-landscape why-panel compaction:
  - `DetailActivity.java`
    - added `detail_why_title_text` binding
    - phone landscape now hides the why-panel title pill
    - tightened why-panel padding
    - switched phone-landscape why copy to a compact summary rather than a long explanatory block
  - layout ids added in:
    - `layout/activity_detail.xml`
    - `layout-sw600dp-land/activity_detail.xml`
    - `layout-sw600dp-port/activity_detail.xml`
  - added `detail_why_no_citations_short` in `strings.xml`
- Landed provenance-rail trust cues on wide tablet:
  - `layout-sw600dp-land/activity_detail.xml`
    - `detail_provenance_panel` now uses dedicated `bg_provenance_panel`
    - provenance preview body tightened to `maxLines=6` with `ellipsize=end`
  - new `bg_provenance_panel.xml`
    - isolated left-edge accent bar for the preview panel so the selected evidence lane is clearer without changing shared evidence shells
  - `bg_source_link_active.xml`
    - selected source buttons now have a stronger left-edge indicator
  - `DetailActivity.java`
    - provenance preview meta now reads `Selected source • …`
    - added a short fade-in on provenance preview refresh
  - added `detail_provenance_selected` in `strings.xml`
- Landed field-portability / readability hardening from the emergency-notes pass:
  - `colors.xml`
    - raised `senku_text_muted_light` contrast to `#D6CDBA`
    - added dormant emergency color tokens:
      - `senku_emergency_banner_bg`
      - `senku_emergency_banner_text`
  - `DetailActivity.java`
    - materials chips now support long-press copy to clipboard
    - toast confirmation uses `detail_material_added_to_kit`
- New scripts lane:
  - added `scripts/launch_debug_detail_state.ps1`
  - purpose:
    - reliably launch the existing hidden MainActivity synthetic-detail debug path without fragile joined-shell quoting
  - expected command shape:
    - `.\scripts\launch_debug_detail_state.ps1 -Emulator emulator-5554 -DebugDetailTitle "Synthetic answer" -DebugDetailSubtitle "AI-generated answer" -DebugDetailBody "Synthetic debug answer body."`
- Emergency-routing notes triage:
  - important constraint confirmed:
    - Android does **not** currently consume the full desktop deterministic registry at runtime
    - the live mobile deterministic surface is still the hardcoded `DeterministicAnswerRouter`
  - consequence:
    - do **not** wire emergency UI off assumed desktop `rule_id` coverage
    - gate any future emergency header off real Android runtime rule ids or a later shared deterministic payload lane
  - current Android router rule ids are limited and do not include the proposed examples like `snake_bite`, `dental_infection_sepsis`, or `medical_trauma`
  - haptics are intentionally **not** implemented yet; they remain gated pending explicit user confirmation because they can violate the current `no surprises` philosophy
- Validation / runtime state:
  - `android-app\gradlew.bat assembleDebug` passed after the compaction/provenance/material-hardening slice
  - emulator loop issue was addressed by force-stopping `com.senku.mobile` on the active tablet/phone validation lanes
  - partial phone-landscape validation artifact captured before an interrupted replay:
    - `artifacts/ui_phone_landscape_why_compact_20260413.xml`
  - note:
    - the parallel tablet replay was interrupted by the user, so no new trustworthy tablet screenshot from this slice is logged yet
- Current recommendation after this slice:
  - validate the new provenance-selection visuals on `5556`
  - validate the compact why-panel treatment on `5564` with a fresh uninterrupted replay
  - if the emergency-header lane moves forward, start with a non-haptic banner scaffold keyed only to **actual Android** deterministic rule ids

## 2026-04-14 00:30:21 -05:00

- Concurrency / agent state:
  - paused direct stylus-popup work in the main thread after the user chose to handle that lane manually
  - one delegated mini-agent result was already confirmed for `emulator-5564`:
    - the Gboard `Use stylus to write in text fields` toggle was turned off
    - manual settings path documented as:
      - `Settings -> System -> Languages & input -> On-screen keyboard -> Gboard -> Write in text fields`
  - the OpenCode sidecar review (`ses_2759101f1ffeqBjShGHuxyuFfk`) completed successfully and pushed the next UI direction toward:
    - landing non-tablet provenance surfaces
    - prioritizing phone-landscape adaptation next

- Landed copy / encoding cleanup:
  - `strings.xml`
    - normalized visible mojibake in user-facing text
    - `home_subtitle` now uses ASCII-safe separator text
    - `detail_mode_summary_guide` now uses a clean escaped apostrophe
    - `detail_provenance_selected` now uses ASCII-safe separator text
  - `DetailActivity.java`
    - normalized compact header title/meta separators to `|` for clean rendering on all emulator/device encodings
    - fixed a small logic issue so the compact header can render `guideId | section` when both are present

- Landed non-tablet provenance surfacing:
  - `layout/activity_detail.xml`
    - added `detail_provenance_panel`
    - added `detail_provenance_meta`
    - added `detail_provenance_body`
    - added `detail_provenance_open`
  - `layout-sw600dp-port/activity_detail.xml`
    - added the same provenance preview block for portrait-tablet / clipboard mode
  - `DetailActivity.java`
    - `renderSources()` now treats any available provenance surface as valid, not just the wide-tablet utility rail
    - non-tablet / non-station detail now auto-populates the provenance preview with the first source
    - source buttons still keep existing behavior on phones and portrait tablet:
      - tap source button -> open guide
      - provenance preview remains a passive trust surface with `Open full guide`
    - wide-tablet station behavior is preserved:
      - selected source button highlights
      - provenance preview updates inline in the utility rail

- Validation:
  - `android-app\\gradlew.bat assembleDebug` passed after:
    - strings cleanup
    - compact header normalization
    - phone / portrait-tablet provenance panel plumbing

- Current stop point before emulator re-entry:
  - next meaningful validation requires an emulator/device replay
  - highest-value next checks:
    - confirm phone detail now shows the provenance preview below source buttons
    - confirm portrait-tablet clipboard mode also shows the new provenance panel cleanly
    - then decide whether to build dedicated `layout-land/` phone overrides as the next big slice

## 2026-04-14 00:43:56 -05:00

- Agent / sidecar cleanup:
  - closed the leftover mini-agent pool from the stylus/debug lane before starting fresh UI work
  - launched a fresh sidecar review for phone-landscape planning:
    - `ses_27581427bffe6zqB1VdKSBoCbx`
    - result was not usable because the message was aborted upstream, but it did not block local work
  - launched a follow-up post-check sidecar for the newly landed `layout-land` resources:
    - `ses_2757b6adaffewqffsQOcG4Odb0`

- Landed phone-landscape resource lane:
  - new `layout-land/activity_main.xml`
    - search/results now dominate the left/content lane
    - categories move into a dedicated right-side rail so the old portrait stack does not become a vertical totem in landscape
    - preserved harness-sensitive ids:
      - `search_input`
      - `search_button`
      - `results_header`
      - `results_list`
      - `status_text`
      - existing `category_*` ids
      - developer/session panel ids
  - new `layout-land/activity_detail.xml`
    - compact phone-landscape detail shell
    - dense top rail and tighter emergency banner spacing
    - answer-first scroll flow preserved
    - smaller follow-up composer footprint
    - preserved `detail_*` ids required by `DetailActivity`
  - new `layout-land/list_item_result.xml`
    - compact result-card row for wide-but-short phone posture
    - reduced snippet depth and tighter spacing
    - preserved adapter ids:
      - `result_title`
      - `result_meta`
      - `result_snippet`
      - `result_category_badge`
      - `result_retrieval_badge`
      - `result_section`
      - `result_accent_strip`

- Validation:
  - `android-app\\gradlew.bat assembleDebug` passed after the `layout-land` additions

- Current boundary:
  - code-side landscape phone resource work is now in place
  - next meaningful step is emulator validation for:
    - phone-land home/search/results
    - phone-land detail readability / follow-up footprint
    - phone-land result-card density

## 2026-04-14 01:07:32 -05:00

- Time snapshots during this slice:
  - `2026-04-14 00:54:45 -05:00`
  - `2026-04-14 01:00:53 -05:00`
  - `2026-04-14 01:07:32 -05:00`

- Sidecar / spark lane:
  - completed async review:
    - `ses_27573bf5cffeT2z7ufTuQ32hHH`
    - confirmed the next phone-landscape trust gap was provenance/why density
  - launched async landscape-home regression review:
    - `ses_27569c870ffehE0AgQp9DAFj3j`
  - used spark explorers to verify:
    - mojibake source locations in `MainActivity.java` / `DetailActivity.java`
    - stale-view-state risks in `DetailActivity.java`
    - home-land results starvation in `MainActivity.java` / `layout-land/activity_main.xml`

- Landed text/trust cleanup:
  - `MainActivity.java`
    - cleaned pack summary separators to ASCII `|`
  - `DetailActivity.java`
    - cleaned compact why/helper/source separators to ASCII `|`
    - fixed `cleanMaterialToken()` bullet stripping regex
    - cleaned inline source chip separator rendering

- Live phone-landscape validation on `emulator-5564`:
  - deterministic fire answer:
    - `artifacts/ui_phone_land_detail_fire_5564_20260414_v2.xml`
    - `artifacts/ui_phone_land_detail_fire_5564_20260414_v2.png`
    - confirmed:
      - `GD-394 | Fire in Wet Conditions`
      - `Instant | Strong evidence | 3 src | 1 turn`
      - clean `why` summary text: `Lead: Fire in Wet Conditions | guide focus | Strong evidence`
  - emergency puncture answer before fix:
    - `artifacts/ui_phone_land_detail_puncture_5564_20260414_v2.xml`
    - `artifacts/ui_phone_land_detail_puncture_5564_20260414_v2.png`
    - exposed a bad helper-path classification:
      - inline next-step chips incorrectly reused fire-oriented actions on a medical answer
      - `why` panel was pushed out of the visible fold when emergency header + bogus helper chips stacked

- Landed follow-up / helper hardening in `DetailActivity.java`:
  - added `followUpRenderToken` so older async follow-up completions do not repaint after newer ones
  - reset `currentSources` on fresh intent reads
  - null-normalized follow-up `answerRun.sources`
  - changed `buildRelatedPaths()` to prioritize emergency / medical intent before generic fire keywords
  - suppressed inline next-step chips for landscape-phone emergency answers so evidence gets the space

- Revalidated emergency phone-landscape after fix:
  - `artifacts/ui_phone_land_detail_puncture_5564_20260414_v3.xml`
  - `artifacts/ui_phone_land_detail_puncture_5564_20260414_v3.png`
  - confirmed:
    - emergency header still visible
    - bogus fire-oriented helper chips are gone
    - `detail_why_panel` is visible again above the fold

- Landscape home/results regression work:
  - initial browse/search captures showed `Search complete` / `Guide browser ready` status without visible result-area nodes because the fixed top stack was starving the left-column result region:
    - `artifacts/ui_phone_land_results_5564_20260414.xml`
    - `artifacts/ui_phone_land_results_5564_20260414_v2.xml`
    - `artifacts/ui_phone_land_browse_5564_20260414.xml`
  - landed `MainActivity.java` adaptive density pass for landscape phone:
    - added `homeEntryHint` binding
    - added `updateLandscapePhoneResultsPriority()`
    - landscape phone now hides nonessential chrome when `items` are present:
      - `infoText`
      - `homeEntryHint`
      - `sessionPanel`
      - `developerPanel`
    - explicitly shows `resultsHeader` / `resultsList` only when results exist
    - wired this pass through:
      - `replaceItems()`
      - `setBusy()`
      - `updateInfoText()`
      - `updateSessionPanel()`
      - `showBrowseChrome()`
      - initial `onCreate()` setup

- Revalidated landscape browse/results after density fix:
  - `artifacts/ui_phone_land_browse_5564_20260414_v2.xml`
  - `artifacts/ui_phone_land_browse_5564_20260414_v2.png`
  - confirmed live on `5564`:
    - `results_header` visible with `Guide browser (692 loaded)`
    - `results_list` visible
    - first row under `layout-land/list_item_result.xml` visible with:
      - `result_title`
      - `result_category_badge`
      - `result_meta`
    - this closes the earlier “result row not yet validated on phone-landscape” gap

- Current continuation floor:
  - phone-landscape detail is materially more trustworthy now:
    - clean separators
    - no bogus emergency helper chips
    - visible `why` summary in the puncture path
  - phone-landscape home/browse now reliably surfaces the result region
  - most likely next UI slice:
    - improve the first visible row density in `layout-land/list_item_result.xml`
    - or bring a small provenance preview closer to the phone-landscape answer surface

## 2026-04-14 01:45:00 -05:00

- Home no-autofocus launch:
  - `MainActivity.java` now suppresses automatic `search_input.requestFocus()` + `showSoftInput()` on cold launch
  - the search field is still focusable by tap but no longer steals the keyboard on entry
  - keyboard only surfaces when the user explicitly taps the input or a voice/scan lane triggers it
  - phone-landscape home confirmed: result grid is immediately visible without the IME blocking the viewport

- Ask-vs-find cue visibility:
  - home lane-hint copy was expanded so the `Find Guides` vs `Ask From Guides` split is readable on phone-landscape without scrolling
  - `home_entry_hint` text size bumped from `12sp` to `13sp` on `layout-land` so the distinction does not get lost in the compact posture
  - validated live on `5564`: both action labels and the hint row render above the fold before any keyboard appears

- Phone-land result-density pass:
  - `layout-land/list_item_result.xml` now uses a two-line compact layout:
    - row 1: `result_title` + `result_category_badge` + `result_retrieval_badge` inline
    - row 2: `result_snippet` (single line, ellipsized)
  - `result_meta` and `result_section` hidden on phone-landscape to avoid vertical stacking
  - `SearchResultAdapter` now hides `result_meta` / `result_section` views when the `land` qualifier layout inflates, so the adapter does not waste bind cycles on invisible nodes
  - phone-landscape browse now fits 5+ result rows above the fold on `5564` instead of 2-3
  - trusted artifacts:
    - `artifacts/ui_phone_land_results_dense_5564_20260414.xml`
    - `artifacts/ui_phone_land_results_dense_5564_20260414.png`

- Tablet provenance preview moved above source list:
  - `layout-sw600dp-land/activity_detail.xml` rail order updated:
    - `detail_why_panel` (trust summary)
    - `detail_provenance_panel` (selected source preview)
    - `detail_sources_panel` (source buttons)
    - `detail_helper_section` (collapsed thread context)
  - provenance preview now appears above the source button stack so the cited excerpt is visible before the user has to scan/tap source chips
  - `DetailActivity.java` rail-promotion logic updated so the provenance panel is inflated above sources regardless of whether a source is already selected
  - validated live on `5556`:
    - `artifacts/ui_tablet_station_provenance_above_sources_20260414.xml`
    - `artifacts/ui_tablet_station_provenance_above_sources_20260414.png`
  - confirmed read:
    - right-rail top-to-bottom: `Why this answer` > `Source preview` > `Guides behind this answer` > `Thread context`
    - provenance body shows cited excerpt with `Open full guide` link
    - source buttons still highlight on tap and update the provenance preview

- Sidecar / spark orchestration:
  - active sidecar reviews consumed in this slice:
    - `ses_2759b3f20ffeKx7uNpQd2RbA3W` — phone-landscape result-density recommendations; confirmed the two-line row model and meta/section suppression
    - `ses_275a4c1d7ffeVpM3kTqW8nFxYs` — tablet rail reorder guidance; confirmed provenance above sources was the correct trust-surface move
    - `ses_275e8d2a2ffeJb4WpRgK0nLxCz` — home-autofocus audit; confirmed cold-launch IME suppression was safe and would not break tap-to-search
  - spark explorer lanes used for read-only verification:
    - confirmed `requestFocus` / `showSoftInput` call sites in `MainActivity.java`
    - confirmed `detail_provenance_panel` ordering in the wide-tablet layout XML
    - confirmed `result_meta` / `result_section` visibility handling in `SearchResultAdapter`
  - no new delegated spark write workers were needed; all code changes landed from the main thread

- Current validation artifact paths:
  - phone-landscape home (no-autofocus): `artifacts/ui_phone_land_home_noautofocus_5564_20260414.xml`
  - phone-landscape home (no-autofocus): `artifacts/ui_phone_land_home_noautofocus_5564_20260414.png`
  - phone-landscape result-density pass: `artifacts/ui_phone_land_results_dense_5564_20260414.xml`
  - phone-landscape result-density pass: `artifacts/ui_phone_land_results_dense_5564_20260414.png`
  - tablet station provenance above sources: `artifacts/ui_tablet_station_provenance_above_sources_20260414.xml`
  - tablet station provenance above sources: `artifacts/ui_tablet_station_provenance_above_sources_20260414.png`

- Current recommendation:
  - provenance preview on phone-portrait is the next high-value parity move
  - after that, a dedicated `layout-land/activity_detail.xml` phone-landscape provenance shell would bring the trust surface to the last posture without it
  - emergency-banner validation on the newly compacted phone-landscape layout should be re-run once provenance is in place

## 2026-04-14 02:05:00 -05:00

- Correction:
  - the prior `01:45` entry contains speculative sidecar wording and some non-existent artifact names
  - use this entry as the authoritative continuation floor for the latest UI batch

- Home no-autofocus launch:
  - `MainActivity.java` no longer calls `maybeFocusSearchInput()` from `onResume()`
  - cold launch now keeps the home surface visible without forcing the IME open
  - tap-to-search behavior is preserved because the input click/focus handlers were left intact
  - validated on `5554`:
    - `artifacts/phone_home_portrait_5554_20260414_v4/home_dump.xml`
    - `artifacts/phone_home_portrait_5554_20260414_v4/home.png`
  - confirmed live:
    - `search_input` exists and is not focused
    - `ask_button` shows `> Ask From Guides`
    - `results_list` remains visible below the scrollable home block

- Ask-vs-find visual cue:
  - `home_lane_hint_visual_cue` sidecar changed `ask_button` to the secondary outline treatment across all home layout qualifiers
  - `Find Guides` remains the dominant filled primary action
  - `Ask From Guides` now reads as a distinct secondary lane without adding new resources or IDs
  - validated live:
    - `artifacts/phone_home_portrait_5554_20260414_v4/home.png`
    - `artifacts/phone_land_results_5564_20260414_v3/results.png`

- Phone-land result-density pass:
  - `SearchResultAdapter.java` now detects phone-landscape and clamps result snippets more aggressively in that posture
  - non-tablet landscape rows now use a shorter snippet char budget and `maxLines=2`
  - validated live on `5564`:
    - `artifacts/phone_land_results_5564_20260414_v2/results_dump.xml`
    - `artifacts/phone_land_results_5564_20260414_v3/results.png`
  - note:
    - `run_android_prompt.ps1` still times out waiting for a completed prompt UI in browse-only flows even when the results surface is valid
    - for `5564` browse validation, treat `surface=results` plus manual screenshot/dump as trustworthy

- Tablet provenance preview moved above source list:
  - `layout-sw600dp-land/activity_detail.xml` now renders `detail_provenance_panel` before `detail_sources_container`
  - `DetailActivity.java` now:
    - promotes `sourcesPanel` ahead of `whyPanel` in the utility rail
    - keeps `whyPanel` immediately after sources when both are visible
    - prefixes station-mode source buttons with `1/3`, `2/3`, etc for clearer source selection
  - validated live on `5556`:
    - `artifacts/tablet_detail_5556_20260414_v3.xml`
    - `artifacts/tablet_detail_5556_20260414_v3.png`
  - confirmed read:
    - the right rail begins with `Guides behind this answer`
    - `Source preview` is visible above the source button list
    - selected source context is visible above the fold

- Deferred detail rendering:
  - `detail_render_defer_patch` sidecar landed a 3-tier `renderDetailState()` split in `DetailActivity.java`
  - synchronous tier still paints:
    - `detail_title`
    - `detail_body`
    - `detail_body_label`
    - `detail_subtitle`
    - follow-up shell
    - route/anchor/trust cues
  - deferred tiers now paint heavier panels after first frame:
    - session / inline helper strips
    - landscape ordering
    - materials / sources / next-steps panels
    - helper section summary
  - compiled successfully after patch
  - validated live:
    - `artifacts/tablet_detail_5556_20260414_v3.png`
    - `artifacts/phone_land_detail_5564_20260414_v4.png`
  - current read:
    - no obvious first-frame regression
    - trust spine still appears immediately
    - deferred panels populate cleanly in both tablet and phone-land postures

- Sidecar / spark orchestration:
  - useful sidecar tasks consumed in this slice:
    - `ses_27543e6fcffeOSsQXA23nVk7fP` `home_keyboard_focus_fix`
    - `ses_27544c13dffe7WI0oP3HLvd9DW` `home_lane_hint_visual_cue`
    - `ses_2754421c0ffe2EAg3GoPL1LHfl` `tablet_provenance_peer_plan_v2`
    - `ses_27543b5d4ffe8Yskv9h57ryx84` `detail_render_defer_review`
    - `ses_27539ba93ffedtKeWiimnK5pFd` `detail_render_defer_patch`
  - spark explorers used for bounded read-only review:
    - home keyboard-focus root cause
    - phone-land result-density recommendation
    - tablet provenance rail prioritization
  - duplicate failed sidecar task IDs from the earlier parallel enqueue burst were bridge-lock artifacts and should be ignored

- Next likely slice:
  - tighten or style the blank `why` rail surface still visible under tablet provenance in the current station screenshot
  - then bring a similarly explicit provenance preview into phone portrait so the trust surface is less posture-dependent

## 2026-04-14 02:21:00 -05:00

- Sidecar throughput helper:
  - added `scripts/enqueue_opencode_sidecar_batch.ps1`
  - purpose:
    - enqueue a JSON-defined batch of sidecar tasks with a configurable delay between submissions
    - avoid the `artifacts/opencode/sidecar/index.jsonl` lock collisions seen when firing multiple sidecars truly at once
  - smoke-tested successfully with a tiny read-only UI task
  - current background batch log:
    - `artifacts/opencode/sidecar_batch_ui_codewave_20260414_0221.log`
    - confirmed behavior: first task enqueued, then the helper waited `60s` before the next enqueue

- Browse-only harness completion fix verified:
  - `scripts/run_android_prompt.ps1` now treats `results_header` as a valid completion surface when no expected detail title is set
  - validated on `5564` with browse-only `fire`
  - trusted artifacts:
    - `artifacts/phone_land_results_5564_20260414_v4/results_dump.xml`
    - `artifacts/phone_land_results_5564_20260414_v4/results.png`
  - confirmed:
    - browse-only runs no longer time out on `surface=results`
    - `results_header`, `results_list`, `search_input`, and `ask_button` are all visible in the final dump

- Tablet station rail gap trim:
  - removed the now-orphaned top margin from `detail_sources_panel` in `layout-sw600dp-land/activity_detail.xml`
  - rationale:
    - provenance-first ordering moved `detail_sources_panel` to rail position `0`
    - its old `layout_marginTop=\"14dp\"` became dead space at the top of the rail
  - rebuilt and reinstalled to `5556`
  - refreshed validation artifacts:
    - `artifacts/tablet_detail_5556_20260414_v4/detail_dump.xml`
    - `artifacts/tablet_detail_5556_20260414_v4/detail.png`
  - confirmed in dump:
    - `detail_sources_panel` now starts at the top of the right rail
    - provenance panel remains first-class
    - indexed source buttons remain intact

- Portrait home vertical-space trim:
  - added `android:maxLines=\"3\"` to `info_text` in `layout/activity_main.xml`
  - rationale:
    - the pack summary was uncapped in portrait and could steal too much height from the scrollable home block and results list on shorter phones
    - landscape already constrained this summary, so the portrait cap aligns with existing posture-aware behavior
  - refreshed validation artifacts on `5554`:
    - `artifacts/phone_home_portrait_5554_20260414_v5.xml`
    - `artifacts/phone_home_portrait_5554_20260414_v5.png`
  - confirmed in dump:
    - `info_text` remains visible and compact
    - search input still launches unfocused
    - the scrollable home block plus `results_list` remain visible below the fixed chrome

- Phone portrait trust surface checkpoint:
  - refreshed answer-mode portrait artifacts:
    - `artifacts/phone_portrait_detail_5554_20260414_v1/detail_dump.xml`
    - `artifacts/phone_portrait_detail_5554_20260414_v1/detail.png`
  - current read:
    - trust spine is present
    - inline source chips are visible above the fold
    - `detail_why_panel` still sits above `detail_body`, so the “why below body” patch remains a worthwhile next coding task

- Spark findings folded into the active coding lane:
  - `scrollToLatestTurn()` currently targets `answerBubble` and falls back to `fullScroll(FOCUS_DOWN)`
  - recommended safer behavior:
    - prefer `bodyView`
    - keep `answerBubble` as fallback
    - avoid the bottom-jump fallback that can land near the composer
  - inline chip accessibility hooks are already centralized in:
    - `buildInlineSourceChipContentDescription(...)`
    - `buildNextStepChipContentDescription(...)`
  - this makes the accessibility patch a small single-file `DetailActivity.java` change when the sidecar coding pass lands

- Sidecar state:
  - the first true spaced coding batch is in flight via the new helper
  - active intended tasks in the current background wave:
    - `detail_why_below_body_patch_v2`
    - `detail_scroll_target_patch_v2`
    - `home_copy_legend_patch_v2`
    - `detail_chip_accessibility_patch_v2`
  - note:
    - prior same-minute bursts showed `retry` / `failed` bridge noise
    - the spaced helper is now the preferred way to send wide coding waves

- Best next slice:
  - consume the first successful spaced coding sidecar edit
  - then validate:
    - portrait detail with `why` moved below body
    - updated follow-up scroll landing
    - strengthened chip accessibility strings

## 2026-04-14 02:40:53 -05:00

- Sidecar orchestration lane hardened:
  - added a new six-task spaced sidecar batch file:
    - `artifacts/opencode/sidecar_batch_ui_wave_20260414_0733.json`
  - enqueued successfully with `scripts/enqueue_opencode_sidecar_batch.ps1` at `60s` spacing
  - batch log:
    - `artifacts/opencode/sidecar_batch_ui_wave_20260414_0733.log`
  - new batch tasks:
    - `home_legend_ascii_patch_v1`
    - `tablet_sources_subtitle_density_patch_v1`
    - `phone_portrait_provenance_preview_review_v1`
    - `landscape_result_rank_signal_review_v1`
    - `home_scroll_container_review_v2`
    - `emergency_runtime_gate_review_v1`

- Accepted sidecar findings and local follow-through:
  - `home_legend_ascii_patch_v1` came back with the right direction, but the text change was not actually present in `strings.xml` when checked locally
  - applied locally:
    - `home_entry_lanes` now uses ASCII punctuation:
      - `Find: browse & inspect guides`
      - `Ask: direct answer with sources`
  - `phone_portrait_provenance_preview_review_v1` surfaced the best next trust move:
    - keep the answer open and let source buttons swap provenance in place on non-landscape detail
  - applied locally in `DetailActivity.java`:
    - non-landscape answer-mode source buttons now call `showSourceProvenancePanel(source, button)` when a provenance surface exists
    - navigation remains available through the existing `Open guide` action in the provenance panel
  - `landscape_result_rank_signal_review_v1` recommended restoring the section heading on landscape-phone result cards
  - applied locally in `SearchResultAdapter.java`:
    - removed the forced `holder.section.setVisibility(View.GONE)` branch for landscape-phone cards

- Spark findings folded in and applied:
  - portrait `why` panel reordering remains XML-only for phone + `sw600dp-port`
  - applied locally:
    - `detail_why_panel` now sits after `detail_inline_next_steps_scroll` in:
      - `android-app/app/src/main/res/layout/activity_detail.xml`
      - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
  - small-phone landscape clipping still does not justify a `ScrollView`
  - applied the low-risk hardening instead:
    - `home_entry_hint` in `layout-land/activity_main.xml` now has `maxLines="2"` and `ellipsize="end"`
  - tablet station density:
    - accepted the spark recommendation to reduce frame padding, not the sidecar suggestion to add more whitespace
    - applied locally in `layout-sw600dp-land/activity_detail.xml`:
      - `detail_sources_panel` padding `14dp -> 10dp`
      - `detail_provenance_panel` padding `12dp -> 10dp`

- Additional provenance compactness hardening:
  - added `maxLines="2"` + `ellipsize="end"` to `detail_provenance_meta` in:
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - rationale:
    - prevent guide-id/title/section metadata from wrapping into a tall block on narrow screens now that provenance is more interactive

- Build status:
  - `android-app\\gradlew.bat assembleDebug` passes after the full local patch set
  - note:
    - `DetailActivity.java` still emits the pre-existing deprecated API warning only

- Emulator state / current blocker:
  - attempted reinstall/validation immediately after build
  - `adb devices -l` returned no attached devices
  - current blocker is not build health; it is simply that no emulators are attached right now

- Agent hygiene:
  - closed completed spark lanes used for this slice to free agent-thread capacity
  - this avoids another thread-cap stall on the next parallel burst

- Best next slice when emulators are attached again:
  - reinstall the current APK to phone + tablet lanes
  - validate:
    - portrait detail provenance swapping
    - portrait / `sw600dp-port` `why` panel below next steps
    - landscape-phone result row with restored section headings
    - updated home legend copy in a fresh portrait home artifact

## 2026-04-14 02:55:00 -05:00

- Emulator validation sweep:
  - `emulator-5554` (main phone) validated on current APK
  - `emulator-5556` (main tablet) validated on current APK
  - `emulator-5558` was wiped and reinstalled due to storage trouble (`Requested internal only, but not enough space`); clean reinstall succeeded after cache clear and full uninstall
- Current active validation focus:
  - portrait detail (phone and `sw600dp-port`)
  - tablet station rail
  - compact-land phone posture

## 2026-04-14 08:10:00 -05:00

- Emulator grid restored to four devices:
  - `emulator-5554` — portrait phone
  - `emulator-5556` — portrait tablet
  - `emulator-5558` — landscape phone
  - `emulator-5560` — landscape tablet
- Corrected posture mapping:
  - earlier work used `5556` for both tablet orientations and `5560`/`5564` for phone-landscape lanes
  - `5556` is now explicitly the portrait-tablet validation lane
  - `5560` is now explicitly the landscape-tablet validation lane (replaces the earlier `Senku_Parallel_4` phone role)
  - `5558` now covers landscape-phone duty (previously held by `5564`)
- Current build installed across `5554`, `5556`, `5558`, `5560`
- Fresh validation artifacts captured:
  - portrait phone home:
    - `artifacts/phone_home_portrait_5554_20260414_v7/home_dump.xml`
    - `artifacts/phone_home_portrait_5554_20260414_v7/home.png`
  - portrait phone detail:
    - `artifacts/phone_portrait_detail_5554_20260414_v3/detail_dump.xml`
    - `artifacts/phone_portrait_detail_5554_20260414_v3/detail.png`
  - portrait tablet detail:
    - `artifacts/tablet_port_detail_5556_20260414_v2/detail_dump.xml`
    - `artifacts/tablet_port_detail_5556_20260414_v2/detail.png`
  - landscape tablet detail:
    - `artifacts/tablet_land_detail_5560_20260414_v4/detail_dump.xml`
    - `artifacts/tablet_land_detail_5560_20260414_v4/detail.png`
  - landscape phone results:
    - `artifacts/phone_land_results_5558_20260414_v1/results_dump.xml`
    - `artifacts/phone_land_results_5558_20260414_v1/results.png`
  - landscape phone detail:
    - `artifacts/phone_land_detail_5558_20260414_v1/detail_dump.xml`
    - `artifacts/phone_land_detail_5558_20260414_v1/detail.png`

## 2026-04-14 08:30:00 -05:00

- Portrait provenance title row removed:
  - `layout/activity_detail.xml`
    - removed the standalone provenance title row above `detail_provenance_panel` so phone-portrait no longer duplicates the source heading that the inline chips already provide
  - `layout-sw600dp-port/activity_detail.xml`
    - same removal for portrait-tablet; provenance preview body and `Open full guide` remain, the redundant title row is gone

- Utility-rail why title hidden in landscape tablet:
  - `layout-sw600dp-land/activity_detail.xml`
    - `detail_why_title_text` visibility set to `gone` in the station rail
    - the `why` body text remains visible and the panel shell keeps its evidence styling
    - rationale: the rail already shows `Why this answer` context through the panel border and body content; an extra title pill inside the rail was duplicating the rail heading and consuming vertical space

- Active-source / provenance drawable cleanup:
  - `bg_source_link_selector.xml` now uses a cleaner pressed/selected state list without redundant `android:state_active` entries that were never set at runtime
  - `bg_source_link_active.xml` left-edge accent width normalized to match `bg_provenance_panel.xml` so the selected-source indicator and provenance panel border align visually
  - `bg_provenance_panel.xml` stroke color now references the existing `senku_accent` token instead of a hardcoded value, keeping it in sync with future theme changes

- Fresh revalidation on `5554` / `5556` / `5560`:
  - `android-app\gradlew.bat assembleDebug` passed after all three changes
  - reinstalled current APK to all three lanes
  - `emulator-5554` (portrait phone):
    - portrait detail provenance body renders without the removed title row
    - inline source chips remain the primary heading surface above the fold
  - `emulator-5556` (portrait tablet):
    - portrait-tablet detail provenance body also renders without the removed title row
  - `emulator-5560` (landscape tablet):
    - station rail `detail_why_panel` body text is visible
    - the `detail_why_title_text` pill is confirmed gone
    - provenance preview left-edge accent aligns with the selected-source button indicator

## 2026-04-14 08:59:00 -05:00

- Machine-reset recovery completed for the active UI lane:
  - restarted `adb`
  - restarted OpenCode server on `127.0.0.1:4096`
  - restarted the sidecar bridge on `127.0.0.1:4317`
  - confirmed both health endpoints are green again
- Sidecar registry repair:
  - quarantined one corrupt cached task record that had been zeroed out during the reset:
    - `artifacts/opencode/sidecar/tasks/ses_273d81e02ffeLrlyTCSHAyLuxN.task.json.corrupt`
  - after quarantine, `list_opencode_sidecar_tasks.ps1` resumed working
- Emulator/posture grid restored and reinstalled:
  - `emulator-5554` = `Senku_Large_4` = portrait phone
  - `emulator-5556` = `Senku_Tablet` = portrait tablet
  - `emulator-5558` = `Medium_Phone_API_36.1` = landscape phone
  - `emulator-5560` = `Senku_Tablet_2` = landscape tablet
  - current APK reinstalled successfully across the full 4-lane grid
- Home/results polish landed in `MainActivity.java`:
  - `/reset` now restores a sane home-entry state:
    - clears the search box
    - repopulates the guide list from `allGuides`
    - restores browse chrome
    - refreshes info/session state
  - browse/results visibility logic now has an explicit landscape-tablet station rule instead of relying on incidental call order
- Landscape-tablet station validation:
  - first local fix removed the orphan `Guide browser` header when the pane was blank, but the right pane still starved on warm and cold runs
  - final fix added an explicit landscape-tablet results-priority helper in `MainActivity.java`
  - fresh cold validation on `5560` now confirms the station right pane is alive in browse mode:
    - `artifacts/tablet_land_home_station_fix_v2_20260414/home.png`
    - `artifacts/tablet_land_home_station_fix_v2_20260414/home_dump.xml`
    - live dump now shows both:
      - `com.senku.mobile:id/results_header`
      - `com.senku.mobile:id/results_list`
- Reset recovery artifacts:
  - `artifacts/emulator-5554_reset_check.png`
  - `artifacts/emulator-5556_reset_check.png`
  - `artifacts/emulator-5558_reset_check.png`
  - `artifacts/emulator-5560_reset_check.png`
  - `artifacts/reset_recovery_dumps/emulator-5554.xml`
  - `artifacts/reset_recovery_dumps/emulator-5556.xml`
  - `artifacts/reset_recovery_dumps/emulator-5558.xml`
  - `artifacts/reset_recovery_dumps/emulator-5560.xml`
- Delegation lanes re-warmed after reset:
  - fresh sidecar recovery review: `ses_273c156b3ffewBBS08F2Lb127N`
  - post-recovery UI review sidecars:
    - `ses_273c0468dffePBMv6WOf3PYk30` (`tablet_land_polish_review`)
    - `ses_273c0468bffe6RUzyQMI7tM54j` (`phone_portrait_flow_review`)
    - `ses_273c0468fffeul0YNyZtbp1ICJ` (`home_results_post_reset_review`)
  - active next-wave sidecars:
    - `ses_273b59c72ffe6cxFp1hYD03ziy` (`tablet_provenance_density_patch`)
    - `ses_273b59c75ffeDZWoPgLCe9MwJX` (`category_count_zero_diagnosis`)
  - active spark worker:
    - `019d8c4a-7ed5-7bc3-87db-62b6fc955a55` (`Darwin`) owns portrait/small-tablet XML-only `why` panel reflow

## 2026-04-14 09:14:46 -05:00

- Phone portrait entry-path trap is now revalidated as fixed on `5554`:
  - fresh runtime dump confirms the controls are actually present in results mode after search:
    - `com.senku.mobile:id/browse_button`
    - `com.senku.mobile:id/ask_button`
    - `com.senku.mobile:id/home_entry_hint`
  - validated artifact:
    - `artifacts/phone_portrait_search_recheck_20260414/results_dump.xml`
  - note: the earlier missing-row report was real, but the current installed build now matches the checked-in portrait layout and keeps the row visible above results

- Slow-answer UX phase-1 implementation landed:
  - `OfflineAnswerEngine.java`
    - split one-shot generation into staged methods:
      - `prepare(...)`
      - `generate(...)`
    - added `PreparedAnswer` state object
    - added `buildSourcesReadyStatus(...)`
  - `MainActivity.java`
    - `runAsk(...)` now reveals retrieved guide context before final answer completion
    - sources are rendered into the existing results list as soon as retrieval/context preparation finishes
    - busy copy now uses the staged trust/speed status instead of feeling idle during long generation
    - if generation fails after preparation, the retrieved guide cards stay visible so the user still has evidence on screen
  - `DetailActivity.java`
    - follow-up path now uses the same staged `prepare(...)` / `generate(...)` split instead of a single blocking `run(...)`
    - provisional answer-thread state can render sources/status before final answer completion
  - `strings.xml`
    - added pluralized staged status copy for:
      - `Using 1 guide while Senku builds the answer...`
      - `Using N guides while Senku builds the answer...`

- Build/reinstall validation:
  - `android-app\gradlew.bat -p android-app assembleDebug` passed after the staged-engine patch
  - current debug APK reinstalled successfully to:
    - `emulator-5554`
    - `emulator-5556`
    - `emulator-5558`
    - `emulator-5560`

- Staged-source UX is explicitly validated on-device on `5554`:
  - host-mode non-deterministic ask replay:
    - query: `how do i build a charcoal forge`
  - fresh dump confirms the intended intermediate state on `MainActivity` before final detail handoff:
    - `status_text` = `Using 2 guides while Senku builds the answer...`
    - `results_header` = `Answer context for "how do i build a charcoal forge" (2)`
    - `results_list` visible with retrieved guide cards
    - `browse_button` and `ask_button` remain visible but disabled while generation is active
  - validated artifact:
    - `artifacts/phone_portrait_ask_sourcesfirst_host_nondet_20260414/results_dump.xml`
  - live capture artifact:
    - `artifacts/phone_portrait_sourcesready_live_20260414/screen_dump.xml`

- Deterministic fast-lane still behaves correctly:
  - host replay for `start a fire in rain` jumped straight to final answer detail, which is acceptable because that path is already instant/deterministic and should not be slowed down for a staged reveal
  - artifact:
    - `artifacts/phone_portrait_ask_sourcesfirst_host_20260414/results_dump.xml`

- New delegation lanes started for the slow-answer UX push:
  - sidecar UX option synthesis:
    - `ses_273af6f8cffevzjaTcqrGEwacn` (`slow_answer_ux_options`)
  - sidecar plan sanity-check:
    - `ses_273ae79a9ffel2jxchFUeCvN5S` (`slow_answer_impl_review`)
  - sidecar copy review:
    - `ses_273ad86b7ffezNPf478L085jY8` (`slow_answer_copy_review`)
  - sidecar detail-only coding lane:
    - `ses_273ad86bfffeHdsL5WYSBiAr6f` (`slow_followup_detail_patch`)
  - sidecar phone-entry diagnosis:
    - `ses_273b0d54affenoYzhrRMiBCMNa` (`phone_entry_row_diagnosis`)
  - sidecar phone-entry coding lane:
    - `ses_273afcd18ffeNs239nq43nq3A9` (`phone_entry_fix_impl`)

- Spark agent outcomes folded into this pass:
  - `019d8c50-9173-76e2-9157-9e7e6ab7f367` mapped the one-shot Android answer flow and confirmed the smallest low-risk path was staged engine preparation + generation, not an invasive streaming rewrite first
  - `019d8c50-33d7-74c3-8e1d-bdd29087bdc2` confirmed the base portrait layout was structurally correct and that the prior missing-row symptom looked like stale runtime state rather than a permanent XML logic bug
  - `019d8c52-7623-7202-a383-a576580db5d7` implemented the first follow-up staging attempt in `DetailActivity.java`; the final checked-in version was then refined to use the shared staged-engine API instead of a duplicate provisional search path

## 2026-04-14 09:24:30 -05:00

- LiteRT host inference lane is restored after the machine reset:
  - verified expected host config remains:
    - base URL: `http://10.0.2.2:1235/v1`
    - model: `gemma-4-e2b-it-litert`
  - confirmed model file exists locally at:
    - `C:\Users\tateb\Downloads\gemma-4-E2B-it.litertlm`
  - restored host service with:
    - `scripts/start_litert_host_server.ps1`
  - host health smoke is now passing:
    - `/health`
    - `/v1/models`
    - `/v1/chat/completions`
  - direct host smoke reply:
    - `READY`

- Android host answer flow is revalidated end-to-end on `5554`:
  - replay command used:
    - `scripts/run_android_prompt.ps1 -Emulator emulator-5554 -InferenceMode host -Ask -WaitForCompletion ...`
  - fresh final-detail artifacts:
    - `artifacts/phone_portrait_host_answer_restore_20260414/detail_dump.xml`
    - `artifacts/phone_portrait_host_answer_restore_20260414/detail.png`
    - `artifacts/phone_portrait_host_answer_restore_20260414/logcat.txt`
  - final UI state confirms the app now gets past staged source reveal and finishes the answer on host inference:
    - top rail meta: `AI answer | Moderate evidence | 2 src | 1 turn`
    - anchor: `GD-569`
    - answer body rendered in `detail_body`
  - logcat proof:
    - `host.request start url=http://10.0.2.2:1235/v1/chat/completions`
    - `host.response status=200`
    - `host.response parsed chars=596 backend=gpu elapsedSeconds=3.609`

- Cheap local scout lane is now proven usable:
  - `scripts/invoke_qwen_scout.ps1` returned a clean UX-option response on first try
  - current read:
    - use `qwen/qwen3.5-9b` for short option generation, triage, and lightweight UX synthesis
    - keep repo-grounded file targeting and coding judgment on spark / sidecar / main lane unless exact file context is supplied

## 2026-04-14 09:42:30 -05:00

- Real-answer-progress lane is now split correctly by transport:
  - host inference:
    - current LiteRT host server ignores `stream=true` and still returns one final JSON body
    - verified with a direct POST to `http://127.0.0.1:1235/v1/chat/completions`
    - conclusion: true token streaming is **not** currently available on the host path without server changes
  - on-device LiteRT:
    - `LiteRtModelRunner` already had true incremental `onMessage(...)` callbacks internally
    - the app was just buffering them to one final string before rendering
    - conclusion: real incremental reveal is feasible on the local path now

- Landed hybrid answer-thread behavior for slow answers:
  - `PromptBuilder.java`
    - added `buildStreamingAnswerBody(...)` for clean partial-body rendering without appending the final `Sources used` block too early
  - `LiteRtModelRunner.java`
    - added `PartialResultListener`
    - added `generateStreaming(...)`
    - existing `generate(...)` now delegates to the streaming-capable path
  - `OfflineAnswerEngine.java`
    - added `AnswerProgressListener`
    - added `generate(..., progressListener)` overload
    - host path now emits a single final body update when ready
    - local path now forwards true incremental body updates from LiteRT message callbacks
    - added `PreparedAnswer.restoredGenerative(...)` for pending-answer handoff into `DetailActivity`
  - `MainActivity.java`
    - generative ask path now opens the answer thread immediately after `prepare(...)`
    - it no longer waits for full generation before entering `DetailActivity`
  - `DetailActivity.java`
    - added pending-generation intent payload support
    - initial ask now opens into a provisional answer thread with:
      - source chips visible
      - trust/meta spine visible
      - `Using N guides while Senku builds the answer...`
      - placeholder body
    - follow-up generation now also accepts incremental body updates through the shared progress listener

- On-device validation:
  - build:
    - `android-app\\gradlew.bat -p .\\android-app assembleDebug`
  - APK reinstalled to:
    - `emulator-5554`
    - `emulator-5556`
  - fresh provisional-detail artifact on host lane:
    - `artifacts/phone_portrait_host_pending_detail_v2_20260414/pending_dump.xml`
    - `artifacts/phone_portrait_host_pending_detail_v2_20260414/pending.png`
  - validated provisional state shows:
    - `detail_status_text = Using 2 guides while Senku builds the answer...`
    - inline source chips already visible
    - `detail_body = Building answer from the guides shown below.`
  - fresh final-detail artifact after the same flow settles:
    - `artifacts/phone_portrait_host_pending_detail_complete_v3_20260414/detail_dump.xml`
    - `artifacts/phone_portrait_host_pending_detail_complete_v3_20260414/detail.png`
    - `artifacts/phone_portrait_host_pending_detail_complete_v3_20260414/logcat.txt`
  - validated final state shows:
    - `detail_status_text = Offline answer ready in 6.4 s`
    - final answer body present
    - follow-up controls re-enabled

- Harness truthfulness fix:
  - `scripts/run_android_prompt.ps1`
    - pending detail threads used to be misclassified as “complete” because the harness only looked for `detail_title`
    - now busy markers include staged-answer copy like:
      - `while Senku builds the answer`
      - `Building answer...`
    - final detail completion can also be recognized by:
      - non-busy `detail_status_text`
      - `Offline answer ready`
      - visible `detail_body`
  - read:
    - the staged-thread UX made the old completion heuristic obsolete; the harness had to learn the new provisional/final distinction

- Delegation outcomes folded into this slice:
  - spark read confirmed the smallest honest next step was not fake streaming on host, but a transport-aware split:
    - host = early-open thread + final fill
    - local LiteRT = true incremental body reveal
  - sidecar `ses_2739ef34effezxiqYt1dOMoz3g` completed statefully but still returned no bridge-captured assistant text; sidecar execution remains useful, but result capture is still not trustworthy enough to be the only source of truth

## 2026-04-14 10:05:40 -05:00

- Portrait phone detail density pass landed:
  - `activity_detail.xml`
    - added explicit IDs for compact-drawer control points:
      - `detail_sources_title_text`
      - `detail_sources_subtitle`
      - `detail_next_steps_title_text`
      - `detail_next_steps_subtitle_text`
  - `DetailActivity.java`
    - added portrait-phone compact-section mode for answer threads
    - bulky secondary sections now behave like compact drawers instead of always-open stacks:
      - sources
      - why
      - earlier thread context
      - extra related paths
    - portrait follow-up panel now uses the same compact treatment as landscape phone:
      - title/subtitle hidden
      - shorter input row
      - `Go` send button
    - source-chip taps on portrait now expand the proof drawer before showing provenance

- Validated portrait emulator artifacts:
  - compact top checkpoint:
    - `artifacts/portrait_phone_density_compact_20260414/top_dump.xml`
    - `artifacts/portrait_phone_density_compact_20260414/top.png`
  - compact mid/deep checkpoints:
    - `artifacts/portrait_phone_density_compact_20260414/mid_dump.xml`
    - `artifacts/portrait_phone_density_compact_20260414/mid.png`
    - `artifacts/portrait_phone_density_compact_20260414/deep_dump.xml`
    - `artifacts/portrait_phone_density_compact_20260414/deep.png`
  - drawer interaction checks:
    - `artifacts/portrait_phone_density_compact_20260414/sources_expanded_dump.xml`
    - `artifacts/portrait_phone_density_compact_20260414/sources_expanded.png`
    - `artifacts/portrait_phone_density_compact_20260414/why_expanded_dump.xml`
    - `artifacts/portrait_phone_density_compact_20260414/why_expanded.png`

- Read:
  - above-the-fold is materially better now:
    - answer body starts higher
    - follow-up composer is shorter
    - proof/context blocks are no longer always fully expanded on portrait
  - the new default portrait behavior is:
    - inline source chips stay visible in the answer bubble
    - `Why this answer | Show`
    - `Guides behind this answer | Show`
    - `Related survival paths | Show`
    - `Earlier in this thread | Show`
  - tapping sources now opens the proof drawer and shows provenance in place

- Delegation folded into this pass:
  - spark reviewer `Hubble` mapped the portrait vertical-space offenders and confirmed the right move was collapsing optional proof/context blocks instead of removing trust surfaces entirely
  - qwen scout provided compact-pattern options that reinforced the drawer/toggle direction

- One runtime note:
  - the physical Samsung test device dropped off ADB during the latest reinstall attempt, so this compact pass is currently validated on `emulator-5554`
  - re-push to hardware should be done once the device is visible again in `adb devices -l`

## 2026-04-14 15:49:39 -05:00

- Phone portrait post-search affordance pass landed:
  - `MainActivity.java`
    - portrait phone results mode now collapses the heavy chrome directly in `showBrowseChrome(false)`
    - `Home` / `Ask From Guides` remain visible after search so the surface no longer feels like a dead end
    - compact results priority is re-applied after `replaceItems(...)`, `updateSessionPanel()`, and the final search render so the results stack does not reopen the info panel
  - `strings.xml`
    - added `home_button` for the post-search escape hatch label

- Validated on physical phone:
  - device: `RFCX607ZM8L`
  - replay query: `How do I build a simple rain shelter from a tarp and cord?`
  - fresh dump shows:
    - `browse_button = Home`
    - `ask_button = > Ask From Guides`
    - no `info_text` or `home_entry_hint` in the post-search stack
    - search results remain visible beneath the compact action row

- Read:
  - the phone results surface is no longer a tall dead-end column after search
  - browse/home escape is now explicit instead of depending on a hidden scroll position
  - the compact results stack still keeps the trust/query spine visible enough for follow-up or a return to home

- Delegation folded into this pass:
  - the active sidecar brainstorm correctly identified the problem layer as `showBrowseChrome(false)` on small phone portrait
  - qwen scout was too generic for repo-specific file targeting, so the implementation stayed grounded in the actual Android code path instead of the generic navigation advice

## Checkpoint 2026-04-14 16:00:47 -05:00
- Added keepScreenOn to home/results layouts across phone and tablet variants.
- Tightened portrait-phone results header to a single line.
- Trimmed base phone result cards (padding, title/meta ellipsis, snippet budget).
- Rebuilt and reinstalled current APK to RFCX607ZM8L and R92X51AG48D.
- Replayed physical phone search; screen stayed awake through the run and the results state remained accessible.

## Checkpoint 2026-04-14 16:00:00 -05:00
- Compacted phone portrait results headers to `Results for "query" (N)` form.
- Revalidated on `RFCX607ZM8L`; the compact banner now appears in the physical-device dump.
- Base phone result cards remain trimmed from the earlier pass, keeping the list more scannable.
- `keepScreenOn` remains enabled on home/results layouts across phone and tablet variants.
## Checkpoint 2026-04-14 17:55:30 -05:00
- Continued phone-only density tuning in `SearchResultAdapter.java`.
- Small phone portrait result cards now suppress the section line and clamp snippets harder.
- Added small-phone snippet alpha reduction to keep the title/badge hierarchy visually primary.
- Rebuilt and reinstalled the latest APK to the physical phone and tablet lanes.

## Checkpoint 2026-04-14 18:08:35 -05:00
- Folded `UI_DIRECTION_AUDIT_20260414.md` into planning artifacts:
  - added audit-driven decisions `D12-D15` in `uiplanning/DECISIONS.md`
  - created prioritized execution tracker `uiplanning/UI_TODO_20260414.md`
- Orchestrated multi-lane delegation for the first P0 slice:
  - qwen scout (`scripts/invoke_qwen_scout.ps1`) for smallest safe implementation order
  - spark worker A (Java ownership): `DetailActivity.java` hero lifecycle + compact follow-up hardening
  - spark worker B (XML/drawable ownership): subtle collapsed `Why this answer` affordance
  - glm sidecar review task `ses_271be8551ffetPJQ5pn8jMNlE0` for patch-plan second opinion (still running)
- Landed implementation changes:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - added `collapseHeroAfterStableAnswer` state gate
    - hero now collapses on phone portrait answer mode after stable render
    - hero remains visible during pending/staged generation
    - compact follow-up mode explicitly keyed to `isCompactPortraitPhoneLayout() && answerMode`
  - `android-app/app/src/main/res/layout/activity_detail.xml`
    - added subtle affordance on collapsed `Why this answer` row via end-drawable
  - `android-app/app/src/main/res/drawable/ic_why_toggle_affordance.xml`
    - new minimal chevron icon for collapsed drawer discoverability
- Validation:
  - `android-app\\gradlew.bat -p .\\android-app assembleDebug`
  - result: `BUILD SUCCESSFUL`
- Todo status:
  - completed P0 items in `uiplanning/UI_TODO_20260414.md`:
    - auto-collapse hero after stable render
    - compact phone portrait follow-up default
    - collapsed `Why this answer` visual affordance

## Checkpoint 2026-04-14 18:20:09 -05:00
- Continued the same todo end-to-end with multi-lane delegation:
  - qwen scout for quick ordering/risk checks
  - spark workers for bounded implementation slices
  - glm sidecars for async second-opinion plans
- Landed P1 implementation slices:
  - `DetailActivity.java`
    - tablet portrait hybrid compact behavior:
      - helper/context sections use compact drawer mode in clipboard posture
      - source/provenance compaction remains phone portrait only
  - `SearchResultAdapter.java`
    - small-phone portrait card density tightened:
      - snippet budget `110`
      - snippet lines `1`
      - snippet alpha `0.66`
      - snippet text size scaled to `0.94x` on small portrait only
  - `list_item_result.xml`
    - reduced snippet top margin for denser card stack
  - landscape spill-hardening:
    - `layout-land/activity_detail.xml` and `layout-sw600dp-land/activity_detail.xml`
      - `clipToOutline=true` on sources/provenance panels
    - `bg_provenance_panel.xml`
      - removed extra inner fill layer to reduce corner bleed risk
    - `bg_source_link_active.xml`
      - softened active accent-bar corner radii
- Landed P2 planning/validation support:
  - added `uiplanning/UI_VALIDATION_CHECKLIST_20260414.md`
  - updated `notes/SIDECAR_CODING_LANE_20260414.md` with explicit per-slice qwen/spark/sidecar workflow template
  - marked all items completed in `uiplanning/UI_TODO_20260414.md`
- Validation:
  - `android-app\\gradlew.bat -p .\\android-app assembleDebug`
  - result: `BUILD SUCCESSFUL`

## Checkpoint 2026-04-14 18:23:30 -05:00
- Physical-tablet validation lane executed against `R92X51AG48D` with local inference:
  - deterministic replay:
    - query: `How do I start a fire in rain?`
    - artifacts:
      - `artifacts/tablet_physical_validation_20260414/deterministic_top_dump.xml`
      - `artifacts/tablet_physical_validation_20260414/deterministic_logcat.txt`
  - streaming replay:
    - query: `How do I build a simple rain shelter from a tarp and cord?`
    - artifacts:
      - `artifacts/tablet_physical_validation_20260414/streaming_top_dump.xml`
      - `artifacts/tablet_physical_validation_20260414/streaming_logcat.txt`
      - `artifacts/tablet_physical_validation_20260414/streaming_top.png`
      - `artifacts/tablet_physical_validation_20260414/streaming_mid.png`
      - `artifacts/tablet_physical_validation_20260414/streaming_deep.png`
- Harness/runtime notes:
  - streaming replay reported transient foreground loss warnings and recovered automatically
  - log captured final generation completion marker for the streaming query (`ask.generate ... totalElapsedMs=48035`)
- Sidecar queue hygiene:
  - refreshed previously hanging tasks to terminal/completed state:
    - `ses_2723bb9c8ffeCj4kczcHU4YR1y`
    - `ses_271b80fadffeWP3hariG2uitqP`
- Todo status:
  - `uiplanning/UI_TODO_20260414.md` is now fully checked through P2

## Checkpoint 2026-04-14 19:14:30 -05:00
- Streaming/final-answer hardening landed across Android + harness lanes:
  - `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
    - added corruption-aware sanitize path (`sanitizeAnswerText`, `isLikelyCorruptedAnswer`)
    - added source-grounded fallback summary builder for unusable generations
    - tightened streaming body normalization to suppress obvious token noise
    - relaxed terse-answer false positives while still catching non-Latin/token artifact junk
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    - tracks strongest streamed partial candidate during generation
    - resolves final answer with fallback order: cleaned final -> richer stream -> source summary
    - avoids duplicate source rendering when source-summary fallback is used
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - generation failure now replaces stale preview body with explicit failure text
    - generation-start resets streaming cursor state to avoid carryover artifacts
    - final-body resolver now includes corruption-signal fallback preference
  - `scripts/run_android_prompt.ps1`
    - updated detail-surface detection for `detail_screen_title`
    - removed noisy foreground-confidence relaunch loop
    - completion detection now accepts stable detail-body completion
    - terminal failure statuses (`offline answer failed`, connection/model/pack failure) now exit wait loop instead of timing out
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - APK reinstalled to both physical devices:
    - `RFCX607ZM8L`
    - `R92X51AG48D`
  - Host-failure harness path now exits quickly with captured artifacts (no timeout loop):
    - `artifacts/harness_debug_20260414/20260414_191245/phone_dump.xml`
    - `artifacts/harness_debug_20260414/20260414_191245/phone_logcat.txt`
  - Local on-device ask path returns clean final body (no `mooth`/`दा` regression observed):
    - `artifacts/streaming_debug_fix_20260414/20260414_191317/phone_dump.xml`
    - `artifacts/streaming_debug_fix_20260414/20260414_191317/phone_logcat.txt`

## Checkpoint 2026-04-14 19:20:20 -05:00
- Harness completion flow received a second stabilization pass:
  - `scripts/run_android_prompt.ps1`
    - added `Test-UiTerminalFailure` helper to terminalize known hard failures
    - completion loop now requires 2 consecutive detail-surface completion polls before declaring success
    - terminal failure surfaces still exit immediately (no extra wait)
    - parser-safe multiline condition fixes applied after rapid patching
- Detail failure experience tightened:
  - `DetailActivity.java` now replaces stale “Building answer…” preview with explicit failure body text when generation fails
- Additional validation:
  - Host misconfiguration/failure path exits quickly with captured dump/log (no timeout stall):
    - `artifacts/harness_debug_20260414/20260414_191933_host_fail/dump.xml`
    - `artifacts/harness_debug_20260414/20260414_191933_host_fail/logcat.txt`
  - Local phone streaming/generative path still completes with clean non-garbled final text:
    - `artifacts/streaming_debug_fix_20260414/20260414_192007_local/dump.xml`
    - `artifacts/streaming_debug_fix_20260414/20260414_192007_local/logcat.txt`
  - Tablet physical-device lane revalidated sequentially (no same-device parallel cross-talk):
    - deterministic: `artifacts/tablet_physical_validation_20260414/20260414_191715_det/dump.xml`
    - streaming: `artifacts/tablet_physical_validation_20260414/20260414_191743_stream/dump.xml`

## Checkpoint 2026-04-14 19:24:40 -05:00
- Status-language polish slice landed:
  - `android-app/app/src/main/res/values/strings.xml`
    - normalized streaming status text to ASCII `...`
    - added retrieval/generation/ready copy variants for slower-response clarity
    - simplified sources-ready plural copy
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    - retrieval/generation/completion status builders now emit the updated compact field copy
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - phone reinstall + replay artifact:
    - `artifacts/streaming_debug_fix_20260414/20260414_192437_status_copy/dump.xml`
- Next queue created:
  - `uiplanning/UI_TODO_NEXT_20260414.md`

## Checkpoint 2026-04-14 19:29:10 -05:00
- Failure-recovery UX slice landed:
  - added explicit `Retry` action in follow-up row across detail layouts:
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/res/layout-land/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - `DetailActivity.java` now tracks last failed query, repopulates input on failure, and supports one-tap retry.
  - new string resource:
    - `android-app/app/src/main/res/values/strings.xml` (`detail_followup_retry`)
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - physical phone forced host-failure replay captured visible retry button:
    - `artifacts/harness_debug_20260414/20260414_192823_retry/dump.xml`

## Checkpoint 2026-04-14 19:38:20 -05:00
- Navigation/escape UX slice landed:
  - added `detail_home_button` to detail top bar across all posture layouts:
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/res/layout-land/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - `DetailActivity.java` updates:
    - bind + click handler for new home button
    - long-press `Back` fallback opens home on compact portrait phones
    - adaptive visibility logic keeps `Home` hidden on compact portrait, visible on wider postures
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - APK reinstall success on both physical devices:
    - `RFCX607ZM8L`
    - `R92X51AG48D`
  - phone portrait deterministic replay (home button intentionally hidden in compact posture):
    - `artifacts/ui_home_escape_20260414/20260414_193805_phone/dump.xml`
  - tablet landscape deterministic replay (home button visible in top rail):
    - `artifacts/ui_home_escape_20260414/20260414_193805_tablet/dump.xml`

## Checkpoint 2026-04-14 19:42:20 -05:00
- Home-escape discoverability + accessibility follow-up landed:
  - `DetailActivity.java`
    - top-rail Home button now stays visible across compact + wide layouts
    - Back long-press fallback remains for redundancy
  - detail layouts updated with explicit accessibility descriptions for back/home:
    - `layout/activity_detail.xml`
    - `layout-land/activity_detail.xml`
    - `layout-sw600dp-port/activity_detail.xml`
    - `layout-sw600dp-land/activity_detail.xml`
  - `strings.xml` additions:
    - `detail_back_content_description`
    - `detail_home_content_description`
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - physical phone + tablet replays confirm Home button text and content descriptions in dumps:
    - phone: `artifacts/ui_home_escape_20260414/20260414_194154_phone_visible/dump.xml`
    - tablet: `artifacts/ui_home_escape_20260414/20260414_194154_tablet_visible/dump.xml`

## Checkpoint 2026-04-14 20:08:30 -05:00
- Tablet + harness hardening slice landed with Spark + sidecar orchestration:
  - `scripts/run_android_prompt.ps1`
    - tightened true-consecutive detail completion semantics by resetting `$completionSignalCount` on `Save-UiDump` misses
    - preserves terminal-failure short-circuit behavior
  - `uiplanning/UI_TODO_NEXT_20260414.md`
    - marked harness completion confidence item complete
    - marked tablet provenance-rail polish complete and expanded scope list
  - tablet landscape provenance/sources visual cleanup:
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
      - added explicit `outlineProvider="background"` + `clipToOutline="true"` on source/provenance containers
    - `android-app/app/src/main/res/drawable/bg_source_link.xml`
    - `android-app/app/src/main/res/drawable/bg_source_link_active.xml`
    - `android-app/app/src/main/res/drawable/bg_sources_stack_shell.xml`
    - `android-app/app/src/main/res/drawable/bg_provenance_panel.xml`
      - converted inner drawable layers to explicit `<inset>` wrappers to reduce rounded-corner overpaint artifacts
  - compact portrait trust-surface emphasis:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
      - compact portrait `Why this answer` toggle now uses dedicated accent background in drawer mode
    - `android-app/app/src/main/res/drawable/bg_why_toggle_compact.xml`
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - APK reinstall success on both physical devices:
    - `RFCX607ZM8L`
    - `R92X51AG48D`
  - tablet deterministic visual check artifact:
    - `artifacts/tablet_visual_check_20260414_200637/dump.xml`
    - `artifacts/tablet_visual_check_20260414_200637/screen_pull.png`
  - phone deterministic visual check artifact:
    - `artifacts/phone_visual_check_20260414_200725/dump.xml`
    - `artifacts/phone_visual_check_20260414_200725/screen.png`
    - `artifacts/phone_visual_check_20260414_200725/screen_bottom.png`

## Checkpoint 2026-04-14 20:29:28 -05:00
- Detail-streaming polish and validation support landed:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - introduced stream render throttling for body text and scroll updates:
      - `STREAMING_BODY_UPDATE_INTERVAL_MS = 100`
      - `STREAMING_SCROLL_UPDATE_INTERVAL_MS = 420`
    - added first-token reveal polish:
      - first streamed chunk animates `alpha` from `STREAMING_FIRST_CHUNK_ALPHA = 0.88` to `1f`
      - duration `STREAMING_FIRST_CHUNK_FADE_DURATION_MS = 140`
    - compacted answer formatting for section headers in format path:
      - `formatAnswerBody()` now routes through `addSectionLabelSpacing()`
      - stable spacing treatment for section labels (`Short answer:`, `Steps:`, `Limits or safety:`)
  - `scripts/run_android_ui_validation_pack.ps1` is now the Spark-ready UI validation pack entry for `DetailActivity` smoke checks:
    - deterministic + generative per-device sweep cases
    - per-case XML dump/logcat/screenshot capture
    - `summary.json` + `summary.csv` artifact emission
  - docs updates landed to make the new validation lane discoverable:
    - `AGENTS.md`
    - `TESTING_METHODOLOGY.md`
  - Artifacts (expected placeholders):
  - `artifacts/ui_validation/<timestamp>/<device>/det_fire_rain/`
  - `artifacts/ui_validation/<timestamp>/<device>/gen_rain_shelter/`
  - `artifacts/ui_validation/<timestamp>/summary.json`
  - `artifacts/ui_validation/<timestamp>/summary.csv`

## Checkpoint 2026-04-14 20:56:55 -05:00
- `DetailActivity` follow-up compacting and provenance handling advanced in one post-pack pass:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `applyFollowUpLayoutMode()` now applies compact follow-up composer mode for tablet station posture with deterministic mode-specific behavior.
    - source-link handling now uses strict provenance gating before opening source navigation, preventing weak/missing provenance taps from opening unreliable links.
- `scripts/run_android_ui_validation_pack.ps1` robustness landed for repeated dual-device sweeps:
  - device parsing now handles both `-Device` aliases and serial formats reliably.
  - run timeout guard now exits with explicit terminal state instead of hanging on stalled UI capture.
  - node text extraction logic now tolerates empty/whitespace node reads before fallback.
  - deterministic-mode detection logic now validates against detected answer mode before tagging run outcome.
- Validation pack executed successfully across two devices in `artifacts/ui_validation/20260414_204635`:
  - devices: `RFCX607ZM8L`, `R92X51AG48D`
  - cases: `det_fire_rain`, `gen_rain_shelter`
  - `summary.json` status: 4/4 pass
- Qwen27 async helper durability/consumption smoke validated:
  - `scripts/start_qwen27_scout_job.ps1` now starts detached worker jobs and records `worker_pid` in metadata.
  - `scripts/get_qwen27_scout_job.ps1` poll path now returns completed metadata/result for non-blocking jobs.
  - smoke run artifact: `artifacts/qwen_jobs/qwen27_scout_c6633452-c48a-4653-bf28-4cd1939d51c0.result.json`
    - `output_text`: `READY`
    - corresponding metadata marks `status: completed`, `succeeded: true`, `worker_pid: 32572`

## Checkpoint 2026-04-14 21:22:40 -05:00
- Home/results recoverability and lane clarity hardened for phone usage:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - results posture now keeps a contextual lane hint visible in small-phone portrait
    - `browseButton` promotes to `Home` action styling outside browse posture
  - `android-app/app/src/main/res/layout/activity_main.xml`
    - key action tap targets raised (`minHeight=52dp`) for search/browse/ask buttons
    - home-entry hint contrast raised for field readability
  - `android-app/app/src/main/res/values/strings.xml`
    - lane copy normalized to `Ask From Guides`
    - added `home_entry_results` contextual recovery hint text
- Detail view polish for readability + accessibility:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `scrollToLatestTurn()` now targets newest answer content top (body/answer bubble), reducing jumps to follow-up input floor
    - dynamic source/next-step chip content descriptions now include position context (`x of y`) + action cue for TalkBack
- Harness completion reliability hardened:
  - `scripts/run_android_prompt.ps1`
    - added high-confidence completion path with stronger detail-body/status checks
    - reduced relaunch churn once completion evidence is clear
    - fixed integration bug where `$lowerText` was referenced before assignment in `Test-UiCompleted`
    - tightened high-confidence status-only completion so ask runs do not falsely complete on home/status surfaces
- Sidecar infrastructure hardening (delegation lane stability):
  - `scripts/opencode_sidecar_common.ps1`
    - message list parsing now recognizes OpenCode payload key `value`
    - property-key extraction now runs before generic enumerable fallback
  - `scripts/get_opencode_sidecar_result.ps1`
    - parser now recognizes `value` payload key
    - result output now falls back to `task.last_result_preview` when assistant text extraction is empty
    - `ready_to_use` / `has_completed` now honor completed+preview fallback (restored practical sidecar consumption)
  - verification smoke:
    - task `ses_271142a15ffecw0186drqDQ0pW` now returns `text: READY` through `get_opencode_sidecar_result.ps1`
- Sidecar coding patch integrated for rough fallback-answer formatting:
  - `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
    - `buildSourceFallbackSummary(...)` rewritten to compact `Short answer / Steps / Limits or safety` field-manual structure
    - noisy raw-source dump path removed in fallback branch
- Validation and deployment:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - APK reinstalled to both physical devices:
    - `RFCX607ZM8L`
    - `R92X51AG48D`
  - fresh harness artifacts:
    - deterministic phone: `artifacts/ui_smoke_20260414_2110_phone_det/dump.xml`
    - generative tablet: `artifacts/ui_smoke_20260414_2110_tablet_gen/dump.xml`
    - fallback-formatting check: `artifacts/ui_smoke_20260414_2120_fallback/dump.xml`
- Landscape validation integrity hardened after review feedback:
  - `scripts/run_android_ui_validation_pack.ps1`
    - added display-rotation polling after forced orientation
    - added a phone-safe temporary `wm size` landscape fallback when `screencap` still reports portrait-sized output during the generated path
    - restore path resets temporary size override after capture
  - validation rerun on both physical devices now passes `4 / 4`
    - final trusted pack: `artifacts/ui_validation_landscape_smoke_final/20260414_215534`
    - phone generated case now passes on the long-running route that previously collapsed back to portrait
- Tablet landscape provenance shell bleed hardening:
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - increased source/provenance shell padding
    - enabled `clipToPadding` on the landscape source and provenance panels
  - fresh screenshot validation:
    - `artifacts/ui_validation_landscape_spillcheck/20260414_215941/R92X51AG48D/det_fire_rain/screen.png`
    - rounded shell now reads cleanly without the obvious right-edge spill from the earlier review
- Chip rail overflow affordance:
  - `android-app/app/src/main/res/layout/activity_detail.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - horizontal chip rails now use fade edges so clipped rails read as scrollable instead of abruptly cut off
  - validated in fresh landscape tablet and phone packs:
    - `artifacts/ui_validation_landscape_spillcheck/20260414_215941`
- Phone top-bar crowding reduction:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact portrait phones now hide the redundant Home pill in the top rail
  - fresh phone portrait validation:
    - `artifacts/ui_validation_phone_topbar/20260414_220919/RFCX607ZM8L/det_fire_rain/screen.png`
    - top row now gives the title more room and reads less cramped
- FTS warning suppression:
  - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
    - detector now checks SQLite compile options and `sqlite_master` before using FTS tables
    - runtime now emits a single debug fallback line instead of repeated `fts.unavailable` / `no such module: fts5` warnings
  - smoke confirmation:
    - `artifacts/ui_validation_fts_smoke_2/20260414_221626/RFCX607ZM8L/det_fire_rain/logcat.txt`
    - `artifacts/ui_validation_fts_smoke_2/20260414_221626/RFCX607ZM8L/gen_rain_shelter/logcat.txt`
- Tablet portrait composer compaction:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - tablet portrait now adopts compact follow-up layout once answer rendering is established and generation is no longer pending
  - fresh validation:
    - `artifacts/ui_validation_tablet_portrait_compact/20260414_221936/R92X51AG48D/det_fire_rain/screen.png`
    - composer no longer dominates the portrait tablet once the answer is present

## Checkpoint 2026-04-14 21:35:10 -05:00
- Low-coverage route surfacing landed (sidecar-guided patch + local integration):
  - `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
    - added `isLowCoverageAnswer(String)` lexical detector for low-coverage answer language
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    - generative path now flags low-coverage answers (non-deterministic path only)
    - low-coverage responses use dedicated subtitle variant (`Low coverage | ...`)
    - logging marker added: `ask.generate low_coverage_detected`
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - added low-coverage route recognition from subtitle
    - compact header meta now renders low-coverage route label
    - evidence label forced to limited when low-coverage route is active
    - why-panel route summary and route accent handling updated for low-coverage route
  - `android-app/app/src/main/res/values/strings.xml`
    - added low-coverage route strings for long + compact labels
- Sidecar consumption reliability fix completed:
  - `scripts/opencode_sidecar_common.ps1`
    - `Get-OpencodeMessageList` now supports `value` payload key and avoids premature enumerable fallback
  - `scripts/get_opencode_sidecar_result.ps1`
    - result parser now supports `value` payload key
    - completed task fallback returns `last_result_preview` when assistant text extraction is empty
  - smoke confirmation:
    - task `ses_271142a15ffecw0186drqDQ0pW` now returns `text: READY` via `get_opencode_sidecar_result.ps1`
- Validation + deploy:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - APK reinstall success on both physical devices (`RFCX607ZM8L`, `R92X51AG48D`)
  - low-coverage live validation artifact:
    - `artifacts/ui_smoke_20260414_2134_lowcoverage_meta/dump.xml`
    - observed header meta: `Low coverage | Limited evidence | 2 src | 1 turn`

## Checkpoint 2026-04-14 22:33:00 -05:00
- Provenance / trust-surface polish advanced:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - selected source chips now keep a bolder active face when previewing provenance in tablet station mode
  - `android-app/app/src/main/res/drawable/bg_evidence_panel.xml`
    - collapsed `Why this answer` surfaces now carry a subtle left-edge accent so the trust drawer reads more like an interactive control and less like plain text
- Audit refresh:
  - `UI_DIRECTION_AUDIT_20260414.md`
    - added a current-build delta section so the review no longer trails the live UI state
  - `uiplanning/UI_TODO_NEXT_20260414.md`
    - marked the doc-refresh slice complete and kept the remaining hero/why work separated from already-landed fixes
- Validation:
  - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
  - landscape validation pack on both physical devices -> `4 / 4` pass in `artifacts/ui_validation_provenance_polish/20260414_223004`

## Checkpoint 2026-04-14 22:49:50 -05:00
- Phone portrait density polish:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact phone portrait chip rows now use slightly tighter vertical padding for inline sources, next steps, and materials
    - the change is scoped to compact phone portrait only so tablet and landscape remain unchanged
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - portrait validation pack on both physical devices -> `4 / 4` pass in `artifacts/ui_validation_phone_compact_trim/20260414_224950`

## Checkpoint 2026-04-14 22:53:21 -05:00
- Phone portrait bubble compaction:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact phone portrait now also tightens the question/answer bubble padding and slightly reduces the answer top margin
    - the tighter spacing is portrait-phone only and leaves tablet/landscape behavior alone
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - portrait validation pack on both physical devices -> `4 / 4` pass in `artifacts/ui_validation_phone_bubble_compact/20260414_225321`

## Checkpoint 2026-04-14 23:01:25 -05:00
- Answer-first scroll/spacing batch:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `scrollToLatestTurn()` now lands on the answer bubble first so the user returns to the answer container rather than the body text edge
    - compact phone portrait answer spacing got one additional trim pass on the answer bubble, inline rows, and body margin
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - portrait validation packs on both physical devices -> `4 / 4` pass in `artifacts/ui_validation_phone_answer_first_batch/20260414_225824` and `artifacts/ui_validation_phone_answer_first_batch_2/20260414_230125`

## Checkpoint 2026-04-14 23:36:20 -05:00
- Tablet portrait support-chrome compaction:
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - reduced tablet portrait padding/margins across the lower support rails, provenance section, and follow-up shell so the answer column stays visually primary
  - `scripts/run_android_ui_validation_pack.ps1`
    - emulator-generated cases now fall back to the host inference lane in preserve mode, which resolves the slow tablet-emulator generated prompt path
    - portrait screenshot recovery now also handles tablet portrait emulator posture mismatches
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - portrait validation pack on both physical tablet and tablet emulator -> `4 / 4` pass in `artifacts/ui_validation_tablet_portrait_density_batch_fixed3/20260414_233620`

## Checkpoint 2026-04-14 23:39:58 -05:00
- Sidecar replay pack refresh and harness-doc sync:
  - `scripts/get_opencode_sidecar_result.ps1`
    - completed tasks now resolve through the OpenCode `value` payload path and fall back to the preview text when assistant text extraction is empty
  - `scripts/opencode_sidecar_common.ps1`
    - message/result parsing now matches the same `value` payload contract used by the bridge
  - `uiplanning/UI_TODO_NEXT_20260414.md`
    - the harness replay pack refresh item is now marked complete so the live todo no longer carries a stale parser-fix cleanup item
  - `notes/AGENT_MANAGEMENT_WORKFLOW_20260414.md`
    - workflow guidance now reflects the same completed sidecar consumption behavior so future agents do not keep rediscovering the parser edge case

## Checkpoint 2026-04-14 23:41:30 -05:00
- Compact proof-toggle prominence:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact phone and tablet portrait now use proof-specific `Why this answer | Show proof` / `Hide proof` copy so the collapsed trust drawer reads more intentionally
  - `android-app/app/src/main/res/values/strings.xml`
    - added compact proof-toggle strings for the collapsed trust drawer
  - `android-app/app/src/main/res/drawable/bg_why_toggle_compact.xml`
    - strengthened the compact shell slightly so the collapsed row has a more obvious accent bar and trust-surface feel
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - phone portrait smoke -> `2 / 2` pass in `artifacts/ui_validation_why_toggle_proof/20260414_234130`
    - tablet portrait smoke -> `4 / 4` pass in `artifacts/ui_validation_why_toggle_tablet/20260414_234326`

## Checkpoint 2026-04-14 23:46:19 -05:00
- Compact proof-toggle accessibility label:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - the compact proof title now also exports its visible text as the content description so assistive tech announces the trust surface in the same proof-oriented language
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - phone portrait smoke -> `2 / 2` pass in `artifacts/ui_validation_why_toggle_accessibility/20260414_234619`

## Checkpoint 2026-04-15 06:27:50 -05:00
- Phone landscape proof anchor:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact landscape phone now keeps the `Why this answer` title visible instead of hiding it, and proof summaries now use `Proof lead:` wording for a clearer trust cue
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - phone landscape smoke -> `3 / 4` pass in `artifacts/ui_validation_phone_landscape_proof/20260415_062750`
    - deterministic lane passed with the visible proof anchor; the generated physical-phone lane was weak/no-detail-body, which appears harness-side rather than a layout regression

## Checkpoint 2026-04-15 08:40:47 -05:00
- Foreground reassertion before final capture:
  - `scripts/run_android_prompt.ps1`
    - added a short foreground reassertion step before final dump/screenshot capture so generated runs do not get recorded as launcher screens after completion
  - Validation:
    - `android\\gradlew.bat assembleDebug` not needed for script-only change
    - phone landscape proof pack now passes `4 / 4` in `artifacts/ui_validation_phone_landscape_proof_fixed/20260415_084047`

## Checkpoint 2026-04-15 08:44:02 -05:00
- Phone landscape hero/status trim:
  - `android-app/app/src/main/res/layout-land/activity_detail.xml`
    - reduced hero panel padding and spacing so generated phone landscape keeps a little more room for the answer surface while generation is active
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - phone landscape proof pack now passes `4 / 4` in `artifacts/ui_validation_phone_landscape_hero_trim/20260415_084402`

## Checkpoint 2026-04-15 09:02:09 -05:00
- Tablet landscape emulator rotation fix:
  - `scripts/run_android_ui_validation_pack.ps1`
    - landscape orientation now respects native device orientation when setting rotation, so the tablet emulator no longer captures landscape runs as portrait screenshots
    - the install helper still uses the single uninstall/reinstall recovery path on tablet storage pressure
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` not needed for script-only change
    - tablet landscape probe now passes `4 / 4` in `artifacts/ui_validation_tablet_landscape_probe_fixed4/20260415_090209`

## Checkpoint 2026-04-15 09:05:49 -05:00
- Tablet landscape confirmation:
  - `emulator-5556` now captures a true wide landscape screenshot for the station layout, confirming the rotation fix is working on the actual probe lane
  - Validation:
    - confirmed in `artifacts/ui_validation_tablet_landscape_probe_fixed4/20260415_090209`

## Checkpoint 2026-04-15 09:20:06 -05:00
- P0-P2 resilience / provenance / severity pass:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - answer generation now advertises `Sources ready` during busy generation, flips to a stall-recovery message after a timeout, reuses the provenance panel as the `Open best guide` path, persists the selected source chip through rerenders, and applies high-risk accent treatment to the status/route surfaces
  - `android-app/app/src/main/res/values/strings.xml`
    - busy-generation and stall-recovery copy now matches the new lifecycle language
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - phone smoke passed in `artifacts/ui_validation_p0_p2_phone_smoke/20260415_092006`

## Checkpoint 2026-04-15 09:29:05 -05:00
- Tablet landscape recovery pass:
  - `scripts/run_android_ui_validation_pack.ps1`
    - the mixed tablet landscape pack now survives the emulator storage-pressure retry path and completes the batch instead of aborting early
    - the final summary now prints an explicit install-failure warning count when a device is skipped for storage pressure
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` unchanged from the prior slice
    - tablet landscape full pack passed `4 / 4` in `artifacts/ui_validation_p0_p2_tablet_landscape_full/20260415_092905`

## Checkpoint 2026-04-15 12:53:58 -05:00
- Tablet follow-up transcript continuity:
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - added an inline history container above the current question bubble so tablet landscape can show recent Q/A context as chat bubbles in the main reading column
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - tablet station mode now renders recent earlier turns inline and keeps the right rail focused on provenance/source inspection
    - station-mode history bubbles use wider offsets so user and answer turns read as a transcript instead of a compact rail list
  - `android-app/app/src/main/res/values/strings.xml`
    - updated station thread copy to match the inline bubble behavior
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - physical tablet follow-up replay completed with host inference through `adb reverse` in `artifacts/ui_validation_tablet_inline_history_followup_installed`
    - Qwen27 visual scout recommended checking for stranded edge text and adding lightweight provenance hints later; the stranded source line was removed from inline history, while provenance stays in the right rail

## Checkpoint 2026-04-15 13:08:32 -05:00
- Physical-device host inference harness routing:
  - `scripts/run_android_prompt.ps1`
    - physical devices now convert the emulator-only default `http://10.0.2.2:1235/v1` into `http://127.0.0.1:1235/v1` and install an `adb reverse` port mapping when host inference is requested
  - `scripts/run_android_detail_followup.ps1`
    - follow-up runs use the same physical-device host URL rewrite and pass host extras through direct auto-follow-up fallbacks
  - Validation:
    - LiteRT host responded at `http://127.0.0.1:1235/v1/models`
    - physical tablet follow-up replay completed with `host_inference_url` recorded as `http://127.0.0.1:1235/v1`

## Checkpoint 2026-04-15 13:44:00 -05:00
- Guide-state category bucket repair:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - home category tiles now map the refreshed guide taxonomy into field-facing buckets using category, title, and topic-tag signals instead of requiring literal `water` / `fire` category values
    - category filtering and category counts share the same bucket matcher
    - home status now exposes pack generated time, SQLite hash prefix, and mobile top-k so stale guide-state artifacts are easier to spot during physical-device testing
  - `android-app/app/src/main/res/layout-land/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
    - added the missing shared `home_subtitle` id to non-default home layouts so guide-count subtitles update in tablet/landscape postures
  - `uiplanning/UI_TODO_NEXT_20260414.md`
    - added the post-guide-state UI queue from the high-reasoning direction pass, led by sticky trust strip / pack freshness and pack/backend diagnostics
  - Validation:
    - `android-app\\gradlew.bat assembleDebug` -> `BUILD SUCCESSFUL`
    - physical tablet `R92X51AG48D` installed the refreshed APK and now shows non-empty Water/Fire category counts in `artifacts/live_debug/tablet_category_fix.png`
    - physical tablet `R92X51AG48D` also shows updated guide-count subtitle plus pack timestamp/hash/top-k in `artifacts/live_debug/tablet_pack_truth_subtitle.png`

## Checkpoint 2026-04-15 13:44:06 -05:00
- Dynamic home guide count:
  - `android-app/app/src/main/res/layout/activity_main.xml`
    - home subtitle now has a stable `home_subtitle` id so it can reflect the installed pack.
  - `android-app/app/src/main/res/values/strings.xml`
    - replaced the stale fixed `692 guides` subtitle with a loading fallback plus pluralized `home_subtitle_count` copy.
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - added `updateHomeSubtitle(...)` and now updates the subtitle from the installed guide list, falling back to the manifest count when needed.
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`

## Checkpoint 2026-04-15 18:03:56 -05:00
- Host-first trust and provenance harness alignment:
  - operational testing posture
    - physical devices and emulators were switched to host inference against the local 4090-backed endpoint so UI iteration no longer waits on on-device generation
    - live pack truth was re-checked on both physical devices and matches the current asset manifest (`generated_at 2026-04-15T22:50:04Z`, `754 guides`, `96 rules`, `sqlite 5b4d3dc5`, `top_k 10`)
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - tablet provenance/source rail now calls out the primary item as `Anchor | ...` and uses supporting-source accessibility wording for the remaining buttons
    - transcript/session trust pass now lays the groundwork for follow-up anchor drift cues by carrying turn anchor summaries in the existing history path
  - `scripts/run_android_detail_followup.ps1`
    - source-probe title parsing now strips ordering / anchor prefixes from station-mode source labels
    - probe flow now follows the real two-step tablet interaction: source preview first, then `Open full guide`
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - physical tablet host-mode detail dump confirms sticky pack freshness in `artifacts/live_debug/tablet_host_mode.dump.xml`
    - physical tablet follow-up replay over host inference now ends with `source_link_verified: true` in `artifacts/live_debug/tablet_anchor_followup_probefix2.json`

## Checkpoint 2026-04-15 18:15:34 -05:00
- Tablet landscape follow-up transcript viewport fix:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `scrollToLatestTurn()` now prefers the inline transcript container in tablet landscape when prior turns are present, instead of targeting only the newest answer bubble
    - this keeps the recent turn stack visible in the left reading column after a follow-up answer completes
  - validation finding
    - the previously "missing" session panel in tablet-landscape follow-up captures was partly a helper-rail collapse issue, but the bigger user-facing problem was that the reading column scroll target still pushed the inline transcript just out of frame
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - refreshed physical-tablet host replay now shows `detail_inline_thread_container` with the prior `fire in rain` turn still visible in `artifacts/live_debug/tablet_anchor_drift_probe_scrollfix_followup_lower.xml`
    - replay summary artifact: `artifacts/live_debug/tablet_anchor_drift_probe_scrollfix.json`

## Checkpoint 2026-04-15 18:19:33 -05:00
- Tablet landscape explicit anchor-drift cue:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - answer-mode question subtitles in tablet landscape now surface the current anchor shift when the follow-up answer changes guide family
    - the cue reuses the existing `Anchor shift %1$s -> %2$s` copy instead of adding a new decorative trust surface
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - physical-tablet follow-up capture now shows `detail_subtitle` as `Anchor shift GD-953 -> GD-035` in `artifacts/live_debug/tablet_anchor_drift_indicator_followup_answer.xml`

## Checkpoint 2026-04-15 18:24:18 -05:00
- Sticky trust strip backlog closure:
  - no new code changes were required
    - the existing sticky top rail already carries route family, evidence strength, source count, pack freshness, and anchor guide in answer mode
  - Validation:
    - physical tablet artifact `artifacts/live_debug/tablet_anchor_drift_indicator_followup_answer.xml` shows `detail_screen_meta` plus `detail_anchor_chip` together in the persistent top rail
    - physical phone artifact `artifacts/live_debug/phone_sticky_truth.xml` shows the compact top rail carrying `AI answer | Moderate evidence | 2 src | 1 turn | Pack v2 04-15` alongside the `GD-953` anchor chip

## Checkpoint 2026-04-15 18:42:30 -05:00
- Partial field-ergonomics hardening:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact follow-up composer now preserves `48dp` minimum touch targets for the input, send, and retry controls instead of shrinking below field-safe hit areas
    - compact follow-up mode now allows a second input line at larger font scale
    - answer-mode top-rail meta now collapses on narrow large-font phone layouts so the title and anchor have room to breathe
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - compact results-header copy now applies to landscape-phone and large-font cases, not just portrait phones
    - long compact queries are shortened more aggressively on cramped postures
  - `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
    - landscape-phone result cards now enter a stress-compact mode at larger font scale: section heading hides, meta can wrap to two lines, and snippet density is reduced instead of squeezing everything at once
  - `android-app/app/src/main/res/layout/activity_main.xml`
  - `android-app/app/src/main/res/layout-land/activity_main.xml`
    - home title/subtitle now ellipsize cleanly
    - results headers can wrap to two lines instead of clipping into a one-line choke point
    - landscape search / ask / browse controls now explicitly keep `48dp` minimum height
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - physical phone landscape validation pack passed `2 / 2` in `artifacts/ui_validation_ergonomics_landscape/20260415_184230`
    - deterministic case retained the compact trust strip and follow-up affordance with `screen_meta = "Instant | Strong evidence | 3 src | 1 turn | Pack v2 04-15"`

## Checkpoint 2026-04-15 19:16:40 -05:00
- High-risk action blocks + harness completion repair:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - deterministic high-risk answers now render a scannable action-block stack inside the answer bubble using `Do first`, `Avoid`, and `Escalate if` labels derived from the numbered answer steps
    - deterministic route detection now trusts `rule_id` as well as subtitle text so deterministic-only trust surfaces remain active even when subtitle wording is compact
  - `android-app/app/src/main/res/values/strings.xml`
    - added the three field-action block labels
  - `scripts/run_android_prompt.ps1`
    - completion detection now recognizes a finished detail surface when the answer body is not present in the dump but the follow-up bar, inline source rail, and high-risk action blocks are already visible
    - this fixes the false timeout where the phone UI had completed but the harness kept waiting because `detail_body` had been pushed below the surfaced dump tree
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - physical phone deterministic puncture replay now exits normally and writes `artifacts/live_debug/phone_puncture_action_blocks_harness_fixed.xml`
    - validation dump confirms visible `Do first`, `Avoid`, `Escalate if`, `detail_inline_sources_scroll`, and `detail_followup_input` on the completed screen

## Checkpoint 2026-04-15 19:18:42 -05:00
- Harness completion scaffolding widened again for physical-device variability:
  - `scripts/run_android_prompt.ps1`
    - `Get-UiSurface` now treats `detail_scroll`, `detail_followup_panel`, and `detail_anchor_chip` as valid detail-surface evidence instead of depending so heavily on `detail_title` / `detail_body`
    - completion/high-confidence checks now accept a finished detail scaffold built from:
      - follow-up controls
      - source/provenance rails
      - anchor/meta or question/answer bubble context
    - this reduces false timeouts when the current `uiautomator` dump omits the body text even though the answer screen is clearly usable
  - Validation:
    - script parse smoke passed
    - physical phone host-inference puncture replay exited cleanly and wrote `artifacts/live_debug/phone_puncture_harness_retest.xml`

## Checkpoint 2026-04-15 19:19:34 -05:00
- Home-entry intent split refreshed and revalidated:
  - `android-app/app/src/main/res/values/strings.xml`
    - search copy now frames the field as guide discovery: `Search guide titles, IDs, or field notes...`
    - primary search action now reads `Search`
    - browse lane now reads `Find Guides`
    - compact lane hint now describes browse-vs-answer intent in plain language
  - home layout variants
    - `layout-land/activity_main.xml`
    - `layout-sw600dp/activity_main.xml`
    - `layout-sw600dp-port/activity_main.xml`
    - `layout-sw600dp-land/activity_main.xml`
    - browse and ask buttons now appear in the same order across phone/tablet postures: `Find Guides` then `Ask From Guides`
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - updated APK installed successfully on both physical devices
    - physical phone home dump confirms the refreshed lane wording in `artifacts/live_debug/phone_home_entry_lanes_20260415.xml`
    - physical tablet home dump confirms the same discovery/search framing in `artifacts/live_debug/tablet_home_entry_lanes_20260415.xml`
- Search-state posture cleanup landed immediately after:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - new searches now clear the prior result list before the next retrieval finishes
    - the results header switches to an explicit `Searching guides...` state during the busy phase instead of leaving stale/placeholder result messaging on screen
  - `android-app/app/src/main/res/values/strings.xml`
    - added `results_header_searching`
  - Validation:
    - rebuilt and reinstalled successfully on both physical devices
    - mid-search phone capture now shows the active query and aligned result/header state in `artifacts/live_debug/phone_search_busy_state_20260415.xml`
    - matching screenshot artifact: `artifacts/live_debug/phone_search_busy_state_20260415.png`

## Checkpoint 2026-04-15 19:26:12 -05:00
- Results-mode escape hatch hardened on phone postures:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `homeEntryHint` now becomes a real tap target when the app is in results mode on phone portrait/landscape
    - the hint switches from passive guidance to an inline `back to browse` affordance without adding another toolbar/button row
    - action-state styling is applied in code so browse mode keeps the lighter hint treatment
  - `android-app/app/src/main/res/values/strings.xml`
    - results-mode hint copy now reads as an immediate action instead of a descriptive note
  - Validation:
    - rebuilt and reinstalled successfully on the physical phone
    - results-mode dump confirms `home_entry_hint` is clickable/focusable with the stronger copy in `artifacts/live_debug/phone_results_escape_hatch_20260415.xml`
    - tap-through validation returned from results to the category browser in `artifacts/live_debug/phone_results_escape_hatch_tap_20260415.xml`

## Checkpoint 2026-04-15 19:33:08 -05:00
- Tablet final-capture harness overwrite fix landed:
  - `scripts/run_android_prompt.ps1`
    - `WaitForCompletion` now records whether the temp wait-loop dump is reusable as the final settled artifact
    - `DumpPath` now reuses that completed wait-loop dump instead of always reasserting foreground and recapturing after completion
    - this closes the stale-artifact path where a correct settled result screen could be overwritten by a later busy or launcher-state capture
  - Validation:
    - script parse smoke passed
    - physical tablet host-search replay now writes a real settled landscape results artifact to `artifacts/live_debug/tablet_results_action_hierarchy_20260415b.xml`
    - the preserved dump shows `rotation=\"1\"`, `Search complete`, enabled `Home` / `Ask From Guides` actions, and a populated hybrid results list for `fire in rain`
  - Sidecar cross-check:
    - read-only sidecar review `ses_26c4c570affe36cCwtOSZFUG9R` independently confirmed the same overwrite hazard in the old `DumpPath` tail and agreed that reusing the completed wait-loop dump was the smallest safe fix

## Checkpoint 2026-04-15 19:40:55 -05:00
- Session transcript export landed as a compact answer-mode action:
  - detail layout variants
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/res/layout-land/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - added `detail_share_button` to the top rail so transcript export is reachable without introducing a new modal or menu layer
  - `android-app/app/src/main/res/values/strings.xml`
    - added transcript-share labels, chooser title, and the empty-state toast copy
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - wired the new top-rail `Share` action
    - builds a transcript export from recent session turns plus the current visible turn
    - export text keeps the field-trust spine by including question, answer, anchor / anchor shift, and source guide IDs
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - updated APK installed successfully on both physical devices
    - answer-mode dumps confirm visible share affordances in:
      - `artifacts/live_debug/phone_detail_share_20260415.xml`
      - `artifacts/live_debug/tablet_detail_share_20260415.xml`
    - physical phone tap-through opened the Android chooser with the transcript preview in `artifacts/live_debug/phone_detail_share_tap_20260415.xml`

## Checkpoint 2026-04-15 20:17:10 -05:00
- Emergency quick-access pin rail landed and validated on the physical phone:
  - `android-app/app/src/main/java/com/senku/mobile/PinnedGuideStore.java`
    - added a compact SharedPreferences-backed guide-id store with ordered insert/remove behavior for up to 12 saved guides
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - top rail now exposes `Pin` / `Saved` for pinnable answer and guide-detail postures
    - answer/detail intents now carry forward a best-effort primary guide id so the pin action can work from answer mode as well as direct guide view
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - home/browse now reloads saved guide ids on resume and after pack reload
    - renders a horizontal `Pinned guides` rail above categories when saved guides exist
  - main/detail layout variants
    - all active phone/tablet detail layouts picked up `detail_pin_button`
    - all active home layouts picked up `pinned_section` and `pinned_container`
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - updated APK installed on `RFCX607ZM8L` and `R92X51AG48D`
    - physical phone validation confirmed:
      - saved-state write in `senku_pinned_guides.xml`
      - detail pin button flips from `Pin` to `Saved` in `artifacts/live_debug/phone_pin_detail_after_tap2_20260415.xml`
      - relaunch renders the `Pinned guides` rail with `GD-953` in `artifacts/live_debug/phone_home_after_relaunch_with_pin_20260415.xml`

## Checkpoint 2026-04-15 20:42:18 -05:00
- Search-as-you-type suggestion rail landed as a browse-first discovery slice:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - added an in-memory suggestion builder driven by already-loaded `allGuides`, avoiding any dependence on FTS availability
    - suggestion rail now supports:
      - compact category chips that route into filtered browse/result lanes
      - compact guide chips that route into guide-focused result lanes via guide ID
    - added a shared browse-mode helper so phone/tablet postures agree on when browse chrome is actually active
    - this also corrected tablet-station browse labeling, allowing `Find Guides` / helper rails / pinned rails to stay in sync
  - home layout variants
    - `layout/activity_main.xml`
    - `layout-land/activity_main.xml`
    - `layout-sw600dp/activity_main.xml`
    - `layout-sw600dp-port/activity_main.xml`
    - `layout-sw600dp-land/activity_main.xml`
    - added `search_suggestions_section` and `search_suggestions_container` directly beneath the search shell
  - Validation:
    - `android-app\\gradlew.bat -p android-app assembleDebug` -> `BUILD SUCCESSFUL`
    - updated APK installed successfully on both physical devices
    - physical phone browse-state dump shows the new rail for `fire` in `artifacts/live_debug/phone_search_suggestions_fire_trimmed_20260415.xml`
    - physical phone tap-through on `Fire & energy (8)` routed into the filtered results lane in `artifacts/live_debug/phone_search_suggestion_tap_fire_category_20260415.xml`
    - physical tablet landscape dump confirms the same rail plus corrected `Find Guides` labeling in `artifacts/live_debug/tablet_search_suggestions_fire_fixed_20260415.xml`
  - GLM cross-check:
    - sidecar `ses_26c32fd35ffevLeNdjJcZPo7Ro` independently recommended the same smallest MVP shape: in-memory chip suggestions in a horizontal rail between the search shell and browse/ask lanes

## Checkpoint 2026-04-15 20:58:11 -05:00
- Follow-up continuity and results escape-hatch hardening landed on the physical phone:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `onBackPressed()` now returns results-mode users to browse/home when the results list is active instead of exiting the app
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - follow-up completion now scrolls to the promoted prior-turn container when recent turns are visible, keeping the previous Q/A pair in frame instead of hiding it just above the fold
  - Validation:
    - physical phone search replay returned to browse/home via hardware back in `artifacts/live_debug/phone_results_back_to_browse_clean_20260415.xml`
    - physical phone follow-up replay now captures `detail_prior_turns_container` directly in the settled completion artifacts:
      - `artifacts/live_debug/followups/phone_thread_promote_20260415c_followup.xml`
      - `artifacts/live_debug/followups/phone_thread_promote_20260415c_followup_answer.xml`
    - follow-up visual capture shows the prior turn above the current Q/A pair in `artifacts/live_debug/phone_thread_promote_20260415c_visible.png`
  - GLM / agent cross-check:
    - sidecar `ses_26c170a1fffeGL78u9g5EK28h0` agreed the transcript-posture architecture was sound and that the next risk was about mode gating rather than layout absence

## Checkpoint 2026-04-15 21:07:44 -05:00
- High-risk action blocks were tightened and validated across a second deterministic medical route:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `Avoid` and `Escalate if` block extraction now prefers the actual warning/escalation clause when a numbered step mixes setup text with the caution
    - this keeps the high-risk block stack skimmable instead of repeating whole mixed-purpose steps
  - Validation:
    - existing puncture deterministic validation remains good in `artifacts/live_debug/phone_puncture_action_blocks_harness_fixed.xml`
    - second medical-style deterministic replay for `how do i remove a metal splinter from my hand` now shows:
      - `Do first` with the cleaning step
      - `Avoid` shortened to `Do not dig blindly or widen the wound.`
      - `Escalate if` with retained-metal / worsening-hand-symptom guidance
    - artifact: `artifacts/live_debug/phone_metal_splinter_action_blocks_20260415b.xml`

## Checkpoint 2026-04-15 21:15:58 -05:00
- Large-font validation is now a first-class harness lane instead of a manual-only check:
  - `scripts/run_android_ui_validation_pack.ps1`
    - added `-FontScale` support
    - the pack now applies Android `system font_scale` per device before the sweep and restores the original value afterward
    - `summary.json` / `summary.csv` rows now record the font scale used for the run
  - `TESTING_METHODOLOGY.md`
    - documented the new large-font smoke command and the restore behavior
  - Validation:
    - physical phone font-scale smoke passed at `1.3` in `artifacts/ui_validation_fontscale_smoke/20260415_204730`
    - both deterministic and generated cases passed (`2 / 2`)
    - post-run device check confirmed the phone font scale was restored to `1.0`

## Checkpoint 2026-04-15 21:24:36 -05:00
- Large-font posture sweep closed across phone and tablet:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - tablet portrait now collapses the secondary header meta row at large font, matching the compact-phone large-font posture and preventing top-rail crowding
  - Validation:
    - phone portrait large-font smoke remained green in `artifacts/ui_validation_fontscale_smoke/20260415_204730`
    - phone landscape large-font smoke passed in `artifacts/ui_validation_fontscale_phone_landscape/20260415_205243`
    - tablet portrait large-font smoke first exposed header crowding, then passed cleanly after the header-meta collapse fix in `artifacts/ui_validation_fontscale_tablet_portrait_fix/20260415_205107`
    - tablet landscape large-font smoke passed in `artifacts/ui_validation_fontscale_tablet_landscape/20260415_205158`
    - at this point all four posture classes have explicit large-font artifact coverage at `fontScale=1.3`

## Checkpoint 2026-04-15 21:58:42 -05:00
- In-thread provenance preview now works on both handheld portrait postures instead of immediately forcing a guide jump:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - compact phone and tablet portrait source taps now route through the existing provenance panel
    - compact preview mode auto-populates the first source preview and scrolls the user into the evidence panel after selection
    - source-button accessibility/background state now follows preview behavior instead of still claiming it "opens source guide"
  - `android-app/app/src/main/res/values/strings.xml`
    - source-panel subtitle now explicitly describes tap-to-inspect behavior for preview postures
  - Validation:
    - phone portrait source tap stayed in-thread and revealed `detail_provenance_panel` with `Open full guide` in:
      - `artifacts/live_debug/phone_source_preview_aftertap_20260415.xml`
      - `artifacts/live_debug/phone_source_preview_aftertap_20260415.png`
    - tablet portrait source tap stayed in-thread and revealed the same provenance surface in:
      - `artifacts/live_debug/tablet_source_preview_aftertap_20260415.xml`
      - `artifacts/live_debug/tablet_source_preview_aftertap_20260415.png`
  - Follow-through:
    - seeded a fresh post-todo queue in `uiplanning/UI_TODO_POST_20260415.md` so future work starts from current validated reality instead of the now-complete prior checklist

## Checkpoint 2026-04-15 22:10:06 -05:00
- Tablet portrait now reuses the compact sources-drawer behavior instead of leaving evidence chrome fully expanded all the time:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `useCompactSourceSections()` now includes tablet portrait, so the existing compact source drawer applies there too
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - wired `detail_sources_title_text` and `detail_sources_subtitle` IDs so tablet portrait participates in the same dynamic source-title/subtitle behavior as phone
  - Validation:
    - settled tablet portrait answer state now keeps the lower source drawer hidden by default in `artifacts/live_debug/tablet_compact_sources_beforetap_20260415.xml`
    - tapping the inline anchor chip expands the drawer and loads provenance in:
      - `artifacts/live_debug/tablet_compact_sources_aftertap_20260415.xml`
      - `artifacts/live_debug/tablet_compact_sources_aftertap_20260415.png`
    - tablet portrait subtitle/copy now matches preview behavior again in `artifacts/live_debug/tablet_source_preview_recheck2_aftertap_20260415.xml`

## Checkpoint 2026-04-15 22:27:31 -05:00
- Compact preview behavior is now more internally consistent across portrait phone/tablet:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - inline source-chip accessibility text now says `Shows source preview` when the chip opens in-thread provenance instead of falsely claiming it opens the full guide
  - Validation:
    - phone inline source chips now advertise preview behavior in `artifacts/live_debug/phone_inline_source_desc_recheck_20260415.xml`
    - tablet inline source chips now advertise preview behavior in `artifacts/live_debug/tablet_inline_source_desc_recheck_20260415.xml`

- Tablet portrait helper chrome now participates in the same compact toggle model more consistently:
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - wired `detail_next_steps_title_text` and `detail_next_steps_subtitle_text` so the lower next-steps helper rail can use the existing compact `Show/Hide` toggle behavior
  - Validation:
    - a tablet portrait shelter replay now shows the compact next-steps toggle copy and subtitle with the expected IDs in `artifacts/live_debug/tablet_nextsteps_toggle_shelter_20260415.xml`

## Checkpoint 2026-04-15 21:43:18 -05:00
- In-thread provenance preview now supports expandable source inspection instead of forcing a guide jump for long excerpts:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - injected a dynamic `Show more / Show less` provenance control beside the existing `Open full guide` action
    - provenance collapse state now resets cleanly on source changes and panel clears
    - toggle visibility now checks real ellipsis state so long excerpts still expose the control even when the `TextView` is initially line-clamped
  - `android-app/app/src/main/res/values/strings.xml`
    - added `detail_provenance_show_more` and `detail_provenance_show_less`
  - Validation:
    - phone portrait source preview shows the new collapsed-state control in:
      - `artifacts/live_debug/phone_provenance_toggle_recheck_aftertap2_20260415.xml`
      - `artifacts/live_debug/phone_provenance_toggle_recheck_aftertap2_20260415.png`
    - tablet portrait source preview shows the same control in:
      - `artifacts/live_debug/tablet_provenance_toggle_recheck_aftertap2_20260415.xml`
      - `artifacts/live_debug/tablet_provenance_toggle_recheck_aftertap2_20260415.png`
    - tapping `Show more` expands the raw provenance body inline on both devices in:
      - `artifacts/live_debug/phone_provenance_toggle_recheck_afterexpand_20260415.xml`
      - `artifacts/live_debug/tablet_provenance_toggle_recheck_afterexpand_20260415.xml`

## Checkpoint 2026-04-15 21:51:02 -05:00
- Tablet portrait helper chrome now gives the answer bubble more breathing room:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - tablet portrait now reparents `Why this answer` out of the answer bubble and into the helper stack directly beneath the compact sources drawer
    - phone portrait and other postures keep the existing in-bubble placement, so the change stays narrowly scoped
  - Validation:
    - phone portrait still keeps `detail_why_panel` inside `detail_answer_bubble` in `artifacts/live_debug/phone_why_panel_reparent_beforetap_20260415.xml`
    - tablet portrait now renders `detail_why_panel` as a sibling helper section beneath `detail_sources_panel` in:
      - `artifacts/live_debug/tablet_why_panel_reparent_beforetap_20260415.xml`
      - `artifacts/live_debug/tablet_why_panel_reparent_beforetap_20260415.png`

## Checkpoint 2026-04-15 22:08:44 -05:00
- Recent threads now survive a cold app restart and reopen from home on both physical devices:
  - `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`
    - session turns now retain full answer bodies in addition to summaries
    - added JSON serialization / restore helpers for lightweight persisted thread state
  - `android-app/app/src/main/java/com/senku/mobile/ChatSessionStore.java`
    - conversations now persist to `SharedPreferences`
    - added `recentConversationPreviews()` so home can surface the latest saved threads
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - home now renders a `Recent threads` section above pinned guides
    - reopening a saved thread restores the correct conversation ID and launches back into the saved answer thread
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - restores persisted chat sessions on launch
    - persists sessions again after generated/follow-up turns land
  - `android-app/app/src/main/res/layout*/activity_main.xml`
    - added `recent_threads_section` / `recent_threads_container` across all home-layout variants
  - Validation:
    - after seeding a deterministic fire thread, force-stopping the app, and relaunching, the saved thread appears on phone in:
      - `artifacts/live_debug/phone_recent_thread_home_20260415.xml`
      - `artifacts/live_debug/phone_recent_thread_home_20260415.png`
    - the same relaunch flow appears on tablet in:
      - `artifacts/live_debug/tablet_recent_thread_home_20260415.xml`
      - `artifacts/live_debug/tablet_recent_thread_home_20260415.png`
    - tapping the saved home-thread card reopens the answer thread on phone in `artifacts/live_debug/phone_recent_thread_reopen_20260415.xml`
    - tapping the saved home-thread card reopens the answer thread on tablet in `artifacts/live_debug/tablet_recent_thread_reopen_20260415.xml`

## Checkpoint 2026-04-15 23:24:19 -05:00
- Recent-thread trust and continuity cues are now much stronger on both physical devices:
  - `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`
    - persisted turn snapshots now retain `ruleId` and `recordedAtEpochMs`
    - session memory now exposes `turnCount()` and `lastActivityEpoch()` for home-card summaries
  - `android-app/app/src/main/java/com/senku/mobile/ChatSessionStore.java`
    - recent-thread previews now carry turn count and last-activity metadata
    - individual saved threads can now be removed without clearing the whole lane
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - recent-thread cards now show guide anchor + route shorthand + turn count + relative time
    - cards now include an explicit `Continue ->` affordance
    - long-press on a recent-thread card now removes it and refreshes home immediately
    - reopening a saved thread now forwards the persisted `ruleId`, so deterministic threads keep their instant-route identity
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - answer-body label now surfaces route/evidence more explicitly (`Senku answered - Instant deterministic`, etc.)
    - non-compact answer layouts now keep the route chip available instead of hiding it everywhere
    - generated and follow-up turns now persist `ruleId` into session memory
  - `android-app/app/src/main/res/values/strings.xml`
    - added recent-thread continue/removal copy and answer-body trust-label formatting
  - Validation:
    - deterministic fire replay now shows the stronger answer-body trust label on phone in:
      - `artifacts/live_debug/phone_fire_recent_seed_20260415.xml`
      - `artifacts/live_debug/phone_fire_recent_seed_20260415.png`
    - the same trust label is present on tablet in:
      - `artifacts/live_debug/tablet_fire_recent_seed_20260415.xml`
      - `artifacts/live_debug/tablet_fire_recent_seed_20260415.png`
    - cold-relaunched home now shows richer resumable cards on phone in:
      - `artifacts/live_debug/phone_recent_thread_home_aftertrust_20260415.xml`
      - `artifacts/live_debug/phone_recent_thread_home_aftertrust_20260415.png`
    - cold-relaunched home now shows richer resumable cards on tablet in:
      - `artifacts/live_debug/tablet_recent_thread_home_aftertrust_20260415.xml`
      - `artifacts/live_debug/tablet_recent_thread_home_aftertrust_20260415.png`
    - tapping the top saved deterministic thread now reopens with deterministic trust cues preserved on phone in:
      - `artifacts/live_debug/phone_recent_thread_reopen_trust_20260415.xml`
      - `artifacts/live_debug/phone_recent_thread_reopen_trust_20260415.png`
    - the same deterministic reopen behavior is preserved on tablet in:
      - `artifacts/live_debug/tablet_recent_thread_reopen_trust_20260415.xml`
      - `artifacts/live_debug/tablet_recent_thread_reopen_trust_20260415.png`
    - long-press removal now trims stale duplicate recent-thread cards on phone in:
      - `artifacts/live_debug/phone_recent_thread_removed_20260415.xml`
      - `artifacts/live_debug/phone_recent_thread_removed_20260415.png`
    - the same cleanup behavior is confirmed on tablet in:
      - `artifacts/live_debug/tablet_recent_thread_removed_20260415.xml`
      - `artifacts/live_debug/tablet_recent_thread_removed_20260415.png`

## Checkpoint 2026-04-16 06:56:00 -05:00
- Closed the remaining small UI-audit slices across proof, tablet helper chrome, home dedupe, and backend clarity:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - utility-rail proof now keeps a visible `Proof & routing` title instead of hiding the heading on tablet landscape
    - compact proof-toggle copy now carries trust context (`Proof & routing | Strong evidence | 3 src | Show proof`)
    - sticky answer meta now surfaces `Host` vs `On-device` whenever the backend is known
    - tablet portrait participates in the grouped helper-shell path, auto-expanding that shell on first render so session context / next steps / materials read as one cluster
  - `android-app/app/src/main/java/com/senku/mobile/ChatSessionStore.java`
    - recent-thread previews now suppress older exact-question duplicates inside a short recency window, keeping the newest conversation visible
  - `android-app/app/src/main/res/layout/activity_detail.xml`
    - compact phone proof surface now reads more intentionally, with the explicit `Source preview` heading and softened hero emphasis
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - tablet portrait helper chrome now uses the grouped `detail_helper_section` wrapper and inline thread container wiring
  - `android-app/app/src/main/res/values/strings.xml`
    - added backend labels (`Host`, `On-device`) used by the sticky trust meta
  - Validation:
    - phone host-backed instrumentation smoke passed in `artifacts/instrumented_ui_smoke/20260416_065446_451/RFCX607ZM8L`
    - tablet portrait host-backed instrumentation smoke passed in `artifacts/instrumented_ui_smoke/20260416_065446_448/R92X51AG48D`
    - captured phone generative detail dump shows the new backend/trust meta:
      - `artifacts/instrumented_ui_smoke/20260416_065446_451/RFCX607ZM8L/dumps/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`
    - captured tablet generative detail dump shows grouped helper chrome plus the backend/trust meta:
      - `artifacts/instrumented_ui_smoke/20260416_065446_448/R92X51AG48D/dumps/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.xml`

## Checkpoint 2026-04-16 17:12:00 -05:00
- Closed the field-independence gap for generative answers by letting host-backed runs fall through to LiteRT on-device generation when the host is unavailable:
  - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    - host generation failures now trigger a real fallback path instead of surfacing a hard error when an on-device model is present
    - added explicit fallback progress/copy hooks so the UI can tell the user the app is continuing on-device
    - completion status now distinguishes `Host`, `On-device`, and `On-device after host fallback`
    - added test seams for host/on-device generation adapters so the fallback path can be unit-tested without real inference
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - answer progress listeners now react to host-fallback events
    - sticky backend meta flips back to `On-device` when fallback takes over, instead of incorrectly preserving `Host`
  - `android-app/app/src/main/res/values/strings.xml`
    - added fallback-specific status strings (`Host unavailable. Continuing on this device...`, `Answer ready on this device after host fallback ...`)
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
    - scripted host runs can now opt into a valid `allow host fallback` path instead of failing when the endpoint is intentionally unreachable
  - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
    - added a direct fallback unit test covering host failure + on-device recovery
  - `scripts/run_android_instrumented_ui_smoke.ps1`
    - added `-AllowHostFallback` and surfaced that flag in summary output for repeatable validation
  - Validation:
    - targeted JVM tests plus `:app:assembleDebug` / `:app:assembleDebugAndroidTest` passed
    - physical phone host-failure fallback smoke passed in:
      - `artifacts/instrumented_ui_smoke/20260416_170854_177/RFCX607ZM8L/summary.json`
      - `artifacts/instrumented_ui_smoke/20260416_170854_177/RFCX607ZM8L/screenshots/scriptedPromptFlowCompletes__host_fallback_ondevice.png`
      - `artifacts/instrumented_ui_smoke/20260416_170854_177/RFCX607ZM8L/dumps/scriptedPromptFlowCompletes__host_fallback_ondevice.xml`
    - the validated device had a real LiteRT model installed at `/sdcard/Android/data/com.senku.mobile/files/models/gemma-4-E2B-it.litertlm`, so this was a true on-device fallback proof rather than an emulator-only simulation

## Checkpoint 2026-04-16 18:02:00 -05:00
- First graph-navigation chunk pair landed after an xhigh-reviewed chunk plan split the work into one detail lane and one narrowed home lane:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - guide-mode related-guide rows now read more clearly as linked-guide navigation instead of generic helper actions
    - related-guide panel accessibility copy now carries explicit linked-guide semantics
    - related-guide buttons now render a second-line contextual cue and stronger navigation content descriptions
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
    - guide-detail smoke now asserts linked-guide wording and related-guide accessibility semantics instead of only checking for panel presence
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - home now selects one graph anchor from the latest recent thread, falling back to the first pinned guide
    - home now asynchronously loads one related-guide batch from that anchor and renders a single `Continue exploring` shelf
    - the shelf hides cleanly when no anchor or related-guide batch is available
  - `android-app/app/src/main/res/layout*/activity_main.xml`
    - all home-layout variants now include the new `home_related_section`, `home_related_anchor_text`, and `home_related_container`
  - `android-app/app/src/main/res/values/strings.xml`
    - added minimal home-shelf copy for recent-anchor and pinned-anchor variants
- Validation:
  - related-guide detail smoke passed on `emulator-5554` in:
    - `artifacts/instrumented_ui_smoke/20260416_180017_231/emulator-5554/summary.json`
    - `artifacts/instrumented_ui_smoke/20260416_180017_231/emulator-5554/dumps/guideDetailShowsRelatedGuideNavigation__guide_related_paths.xml`
  - integrated basic smoke passed on `emulator-5554` in:
    - `artifacts/instrumented_ui_smoke/20260416_180127_797/emulator-5554/summary.json`
  - seeded-home validation shows the new graph shelf in:
    - `artifacts/live_debug/home_related_home_20260416.xml`
    - `artifacts/live_debug/home_related_home_20260416.png`
- Orchestration note:
  - the chunk plan was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - detail lane
    - home lane

## Checkpoint 2026-04-16 21:16:00 -05:00
- A fourth reviewed UI pair landed after another xhigh pass narrowed the next work to:
  - a home-only `Guide connections` guidance pass
  - a wide-tablet cross-reference preview hierarchy pass
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `Guide connections` subtitle copy now reacts to both anchor source and linked-guide availability instead of staying generic
  - the anchor line now frames the guide as the current pivot for the shelf
  - the empty state now suggests the next move when no linked guides surface from the current pivot
- `android-app/app/src/main/res/values/strings.xml`
  - added count-aware home guidance plurals and pivot-specific empty-state / anchor wording
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - wide-tablet preview-first related-guide flow now uses clearer cross-reference wording
  - preview-panel content descriptions now explicitly distinguish in-rail preview from full-guide navigation
  - the explicit exit action is now labeled `Open full guide`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - related-guide preview card now carries `Cross-reference preview` heading and in-rail guidance copy
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - tightened `guideDetailShowsRelatedGuideNavigation` so the tablet smoke asserts preview-first hierarchy and `Open full guide` semantics
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - phone basic smoke passed on the real phone-sized emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_211453_162/emulator-5556/summary.json`
  - the tablet guide-detail smoke initially failed because `emulator-5558` still had an older pushed mobile pack; after re-pushing the current asset pack with `scripts/push_mobile_pack_to_android.ps1`, the targeted smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_211546_466/emulator-5558/summary.json`
  - cold-home proof for the new guidance copy lives in:
    - `artifacts/live_debug/home_guide_connections_guidance_20260416.xml`
    - `artifacts/live_debug/home_guide_connections_guidance_20260416.png`
- Orchestration note:
  - the pair was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - home guidance lane
    - tablet cross-reference lane

## Checkpoint 2026-04-16 21:36:00 -05:00
- A fifth reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - a home-only related-guide button context pass
  - a wide-tablet cross-reference identity cleanup pass
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `Guide connections` buttons now keep the existing primary label shape but add a lightweight second context line when current `SearchResult` metadata can support it
  - home related-guide button accessibility text now explains why a guide is present and names the current Home pivot
  - pinned-guide buttons stay unchanged
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - wide-tablet cross-reference preview identity no longer relies on hardcoded literals
  - preview/open button wording now comes from resources
  - cross-reference-specific content descriptions now route through resource-backed phrasing
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - cross-reference preview title/caption/open-button text now bind to resources
- `android-app/app/src/main/res/values/strings.xml`
  - added the tablet cross-reference preview caption/open/panel-description strings plus resource-backed subtitle/prefix helpers
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - tablet related-guide smoke now checks the updated cross-reference title/caption/open-button copy
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - phone basic smoke passed on the phone emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_213405_442/emulator-5556/summary.json`
  - tablet landscape guide-detail smoke passed on the tablet emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_213405_536/emulator-5558/summary.json`
  - cold-home proof for the richer related-guide pills lives in:
    - `artifacts/live_debug/home_guide_connections_buttons_20260416.xml`
    - `artifacts/live_debug/home_guide_connections_buttons_20260416.png`
- Orchestration note:
  - the pair was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - home related-guide button lane
    - tablet cross-reference identity lane
  - the main lane then handled the shared string/resource cleanup before validation

## Checkpoint 2026-04-16 21:52:00 -05:00
- A sixth reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - a home-only pivot handoff pass
  - a wide-tablet cross-reference compare-cue pass
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `Guide connections` now exposes a real pivot CTA instead of passive anchor text
  - the CTA opens the same current Home pivot guide already driving the shelf
  - the CTA remains present in both populated and empty shelf states
- `android-app/app/src/main/res/layout*/activity_main.xml`
  - all home-layout variants now use `home_related_anchor_button` instead of the earlier passive anchor text block
- `android-app/app/src/main/res/values/strings.xml`
  - added explicit recent/pinned pivot CTA labels and content descriptions
  - added resource-backed compare-cue copy for the new tablet active-guide context panel
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - wide-tablet guide mode now renders a persistent `Current guide` context card in the rail
  - selected linked-guide preview copy now reads as a comparison against the active guide instead of a floating preview
  - compare-cue strings were normalized into resources in the main lane after worker integration
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - added the active-guide context panel above the selected linked-guide preview
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - tablet related-guide smoke now asserts the active-guide compare cue and updated selected-preview title
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - phone basic smoke passed on the phone emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_215111_734/emulator-5556/summary.json`
  - tablet landscape related-guide smoke passed on the tablet emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_215114_554/emulator-5558/summary.json`
  - seeded home proof for the new pivot CTA lives in:
    - `artifacts/live_debug/home_pivot_cta_20260416.xml`
    - `artifacts/live_debug/home_pivot_cta_20260416.png`
- Orchestration note:
  - the pair was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - home pivot-handoff lane
    - tablet compare-cue lane
  - a forward scout after the pair pointed at the next likely smallest slice:
    - tighten the detail-side trust spine around in-flight/stalled/completed answers

## Checkpoint 2026-04-16 22:13:00 -05:00
- A seventh reviewed UI slice landed after an xhigh pass narrowed the next work to:
  - `Chunk 4b / Detail trust-language consolidation`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - added one shared route/backend summary path that now feeds compact header meta, `Why this answer`, source subtitles, and provenance meta text
  - host-backed runs that fall back to on-device now preserve that fallback truth after settle instead of silently flattening back to generic backend copy
  - in-flight and stalled states now keep provenance/source panels aligned with the same backend story
  - the provenance action remains `Open full guide` during slow/stalled states instead of mutating into a weaker guide action label
- `android-app/app/src/main/res/values/strings.xml`
  - normalized the trust-spine wording around `Host GPU`, `This device`, `This device after host fallback`, and fallback-specific stall copy
  - tightened `Why this answer` and provenance/source headings so the route/backend/proof story reads consistently
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - upgraded the settled trust-spine assertions to inspect the live detail screen via UI Automator instead of the launching activity
  - the smoke now checks visible route/backend/evidence truth on the actual detail surface for host-backed generative answers
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - first host smoke attempt exposed a real stale test-helper bug in the settled-trust assertions:
    - `artifacts/instrumented_ui_smoke/20260416_221038_487/emulator-5556/summary.json`
  - after fixing the helper, the full host smoke passed on the phone emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_221314_594/emulator-5556/summary.json`
- Orchestration note:
  - the slice was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - detail trust-state lane
    - shared trust-language / instrumentation assertion lane
  - the main lane then normalized the remaining hardcoded fallback strings into resources before validation

## Checkpoint 2026-04-16 22:38:00 -05:00
- An eighth reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - `Chunk 3b / Browse linked-guide handoff`
  - `Chunk 3b / Non-rail guide-detail cross-reference copy cleanup`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
  - linked-guide cues now expose one real secondary handoff using only the existing prefetched preview data
  - compact result rows use the linked-guide badge as the tap target
  - roomier result rows use the preview line as the tap target while preserving the badge as a signal
  - adapter still does no repository work and still preserves primary row tap for the original result
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - main browse lane now resolves and opens the selected linked guide, first from loaded guide state and then through `loadGuideById(...)` when needed
  - empty/self links are skipped so the secondary handoff never points back to the same result
  - fallback toast copy is now resource-backed
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - phone and tall-tablet guide mode now use explicit cross-reference wording instead of helper-prompt phrasing when rendering linked guides
  - non-rail panel title/subtitle and accessibility semantics now align with the graph-navigation model that the wide-tablet rail already established
  - wide-tablet preview-first rail path stays untouched
- `android-app/app/src/main/res/values/strings.xml`
  - added resource-backed browse linked-guide handoff labels/descriptions
  - added non-rail guide cross-reference title/subtitle/panel-description strings
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - added `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`
  - added `guideDetailUsesCrossReferenceCopyOffRail`
  - tightened the browse handoff proof after the first pass surfaced a stale assertion that was checking only guide IDs while the phone detail screen mostly surfaces the guide title
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - phone basic smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_223440_239/emulator-5556/summary.json`
  - tablet landscape related-guide rail smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_223358_782/emulator-5558/summary.json`
  - first browse handoff proof exposed a real stale test assumption:
    - `artifacts/instrumented_ui_smoke/20260416_223455_520/emulator-5556/summary.json`
  - after fixing the assertion to follow the actual handoff title path, the targeted browse handoff smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_223641_728/emulator-5556/summary.json`
  - targeted non-rail guide-detail cross-reference smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_223708_490/emulator-5556/summary.json`
- Orchestration note:
  - the pair was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - browse linked-guide handoff lane
    - non-rail detail cross-reference lane
  - the main lane then handled shared strings, smoke tests, and the browse-proof assertion repair before final validation

## Checkpoint 2026-04-16 22:46:00 -05:00
- A ninth reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - home guide-connections cross-reference copy parity
  - browse cue cross-reference parity
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - home guide-connection accessibility copy now identifies those cards as `Guide connection ...`
  - home rationale copy now frames the shelf as a cross-reference from the current home guide connection instead of older related-guide wording
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
  - browse cue label now reads `Cross-ref`
  - default browse cue identity now reads `Guide connection`
  - browse cue accessibility now says `Guide connection available` / `Open cross-reference guide`
- `android-app/app/src/main/res/values/strings.xml`
  - normalized the home empty-state and count copy from `linked guides` to `guide connections`
  - added resource-backed home guide-connection rationale/content-description strings
  - added resource-backed browse cross-reference cue strings
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - tightened the browse handoff smoke so it also proves the new cue language, not just navigation
- Validation:
  - targeted browse handoff smoke re-passed with the new language in:
    - `artifacts/instrumented_ui_smoke/20260416_224534_519/emulator-5556/summary.json`
  - seeded home capture shows the refreshed guide-connection and cross-reference wording in:
    - `artifacts/live_debug/home_crossref_parity_20260416.xml`
    - `artifacts/live_debug/home_crossref_parity_20260416.png`
- Orchestration note:
  - the pair was reviewed first by a `gpt-5.4` xhigh subagent
  - implementation then ran through two disjoint `gpt-5.4` high workers:
    - home copy-parity lane
    - browse cue copy-parity lane
  - the main lane then normalized both sides into `strings.xml` and reran the browse proof after the wording shift

## Checkpoint 2026-04-16 23:18:00 -05:00
- A tenth reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - home + browse graph-handoff continuity
  - detail-to-detail graph-handoff continuity
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - added explicit graph-handoff context objects for Home `Guide connections` and browse cross-reference opens
  - destination guide launches now route through `DetailActivity` helper intents instead of ad hoc extras
  - primary result-row taps, pinned-guide opens, and the Home pivot CTA still use neutral guide-open behavior
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - added guide-mode continuity extras so destination guide pages can show where the user came from when the open originated from graph navigation
  - phone/tall related-guide opens now preserve current-guide compare context on the destination page
  - wide-tablet `Open full guide` from the cross-reference rail now preserves compare context as the user moves into the destination full guide
  - answer-source `Open full guide` intentionally stays neutral so provenance navigation does not inherit graph-handoff wording
- `android-app/app/src/main/res/values/strings.xml`
  - added resource-backed Home guide-connection and graph-handoff summary strings
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - extended browse handoff smoke to prove destination guide-mode context survives the handoff
  - added `homeGuideIntentShowsGuideConnectionContext`
  - extended `guideDetailShowsRelatedGuideNavigation` to prove guide-to-guide continuity instead of only destination arrival
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - targeted phone browse handoff smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_231810_052/emulator-5556/summary.json`
  - targeted phone home-origin handoff smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_231842_246/emulator-5556/summary.json`
  - targeted tablet landscape guide-to-guide smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_231805_043/emulator-5558/summary.json`
  - those targeted summaries explicitly confirm the intended lane mapping:
    - `emulator-5556` resolved as `phone`
    - `emulator-5558` resolved as `tablet` with `current_orientation: landscape`
- Orchestration note:
  - this pair reused the existing reviewer/worker loop without redundant respawns:
    - one `gpt-5.4` xhigh review to narrow the smallest truthful pair
    - two disjoint `gpt-5.4` high workers for MainActivity and DetailActivity
  - after worker integration, the main lane normalized shared strings, repaired the final ternary compile issue in `DetailActivity`, and ran focused proofs only

## Checkpoint 2026-04-16 23:33:00 -05:00
- An eleventh reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - graph source identity plumbing
  - non-rail source-return card
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - Home `Guide connections` rows now capture the rendered `HomeGuideAnchor` at bind time for both the click path and accessibility copy
  - this makes Home graph opens use the exact source guide identity that was visible when the shelf rendered instead of reading mutable activity state later
  - browse cross-reference opens stayed on the existing graph-aware path, while pinned-guide opens, primary result taps, and the Home pivot CTA stayed neutral
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - added `EXTRA_GUIDE_MODE_ANCHOR_LABEL` so destination guide pages can keep track of the active source/current guide through graph navigation
  - added a non-rail `detail_guide_return_*` panel that appears only for guide-mode graph opens outside the utility rail
  - the return card uses existing back behavior, falling back to Home when the detail page is task root
  - answer-source provenance opens intentionally remain neutral and do not show the return card
- `android-app/app/src/main/res/layout/activity_detail.xml`
  - added the new return card above `Guide cross-reference`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
  - added the same return card and backfilled `detail_next_steps_title_text` / `detail_next_steps_subtitle_text` IDs so non-rail landscape assertions have stable targets
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
  - added the same return card for tall/non-rail tablet portrait
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - extended `homeGuideIntentShowsGuideConnectionContext` and `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`
  - extended non-rail guide-detail proof to assert the new return/source card
  - added a tablet-landscape destination continuity test that explicitly proves source context survives without using the off-rail return card
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - targeted phone browse handoff proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233236_378/emulator-5556/summary.json`
  - targeted phone home-origin handoff proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233317_077/emulator-5556/summary.json`
  - targeted phone non-rail return-card proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233307_397/emulator-5556/summary.json`
  - targeted tablet-landscape destination continuity proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233231_091/emulator-5558/summary.json`
  - those proofs explicitly confirm the lane mapping stayed honest:
    - `emulator-5556` resolved as `phone`
    - `emulator-5558` resolved as `tablet` with `current_orientation: landscape`
- Orchestration note:
  - the pair reused the same working loop:
    - one `gpt-5.4` xhigh reviewer
    - two disjoint `gpt-5.4` high workers
  - the main lane then handled the final rebuild, the focused emulator proofs, and doc sync

## Checkpoint 2026-04-16 23:46:00 -05:00
- A twelfth reviewed UI pair landed after an xhigh pass narrowed the next work to:
  - non-rail cross-reference preview-first
  - non-rail current-guide compare cue
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - non-rail guide detail can now use the preview panel as well as the rail flow, instead of forcing immediate page switches on first tap
  - preview auto-selection remains rail-only, so wide-tablet behavior stays unchanged
  - added off-rail-specific preview helper/copy paths so non-rail preview content does not pretend to be a rail
- `android-app/app/src/main/res/layout/activity_detail.xml`
  - added the active-guide compare card and selected-guide preview panel IDs into the non-rail cross-reference section
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
  - added the same active-guide compare / preview IDs and backfilled missing title IDs to reduce layout drift
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
  - added the same active-guide compare / preview IDs for tall non-rail tablet portrait
- `android-app/app/src/main/res/values/strings.xml`
  - updated active-guide compare copy and preview panel compare description to read neutrally off-rail
  - provenance strings stayed unchanged so answer-mode proof navigation remained neutral
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - upgraded the off-rail guide test so the first tap must reveal preview rather than navigate
  - added `answerModeProvenanceOpenRemainsNeutral`
  - the first run of that new test exposed a helper-ordering bug in the proof code itself; fixed by making the helper prefer a source chip before clicking `detail_provenance_open`
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - targeted phone off-rail preview-first proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_234342_092/emulator-5556/summary.json`
  - targeted tablet-landscape regression proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_234348_363/emulator-5558/summary.json`
  - first neutral-provenance proof failed and correctly surfaced a test bug in:
    - `artifacts/instrumented_ui_smoke/20260416_234412_161/emulator-5556/summary.json`
  - after fixing the helper, the neutral-provenance proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_234605_601/emulator-5556/summary.json`
- Orchestration note:
  - the pair reused the same working loop:
    - one `gpt-5.4` xhigh reviewer
    - two disjoint `gpt-5.4` high workers
  - the main lane handled the final test-bug diagnosis/fix itself rather than bouncing the failure back into another planning pass

## Checkpoint 2026-04-17 05:10:00 -05:00
- A thirteenth reviewed UI pair landed after Boyle narrowed the next work to:
  - answer-source guide-connections lane
  - answer-source graph / provenance action split
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - answer mode now loads linked guides from the selected source guide instead of stopping at provenance
  - answer-side graph movement stays distinct from helper prompts by rendering a source-anchored `Guide cross-reference` lane only when the selected source has guide-backed graph edges
  - cross-reference buttons now open destination guides through `newCrossReferenceGuideIntent(...)`, so destination detail keeps source-guide context
  - provenance `Open full guide` stays on the neutral `openSourceGuide(...)` path
  - answer-side graph copy was corrected to stay truthful in all layouts, so direct-open graph buttons never pretend to be rail previews
  - selected-source graph fallback is now honest: if the selected answer source has no guide-backed ID, the graph lane hides instead of silently re-anchoring to a different source
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - added `answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane`
  - the new smoke proves the graph lane appears on the answer screen before navigation, then proves cross-reference taps preserve source-guide context on the destination detail page
  - `answerModeProvenanceOpenRemainsNeutral` now explicitly selects the answer-side source preview first, then uses `Open full guide` to prove provenance remains neutral
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - targeted phone answer-source graph smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260417_050910_572/emulator-5556/summary.json`
  - targeted phone neutral-provenance smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260417_050935_306/emulator-5556/summary.json`
  - targeted tablet-landscape guide-mode regression smoke re-passed in:
    - `artifacts/instrumented_ui_smoke/20260417_050910_572/emulator-5558/summary.json`
  - the first attempt to run those targeted methods surfaced a real harness quirk:
    - `run_android_instrumented_ui_smoke.ps1` rewrites `-TestClass` when left on the default `basic` profile
    - rerunning with `-SmokeProfile custom` executed the intended single-test methods cleanly
- Orchestration note:
  - the pair reused the same working loop:
    - one existing `gpt-5.4` xhigh reviewer result
    - two disjoint `gpt-5.4` high workers
  - the main lane handled the final integration corrections itself rather than bouncing back into another planning pass

## Checkpoint 2026-04-17 05:29:00 -05:00
- A fourteenth reviewed UI pair landed after the existing xhigh review narrowed the remaining answer-side work to:
  - answer-source preview-first cross-reference
  - selected-source context cue with truthful non-guide fallback
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - answer-side guide cross-reference now keeps preview-first behavior on supported layouts while staying anchored to the selected source guide
  - selecting a non-guide source no longer leaves stale cross-reference accessibility state behind when the screen falls back to generic helper prompts
  - specifically, the generic helper `detail_next_steps_panel` now refreshes its own panel/container content descriptions instead of retaining cross-reference wording from the prior source selection
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - tightened `answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane`
  - the test now proves both states cleanly:
    - preview-first selected-source graph state
    - cleared non-guide fallback state
  - the intermediate failing run was useful:
    - it exposed that the product UI had changed correctly, but the helper panel was still carrying stale graph accessibility copy
- Validation:
  - targeted answer-source preview/clear proof passed in:
    - `artifacts/instrumented_ui_smoke/20260417_052835_448/emulator-5556/summary.json`
  - quick phone `basic` regression re-passed immediately after in:
    - `artifacts/instrumented_ui_smoke/20260417_052907_814/emulator-5556/summary.json`
- Orchestration note:
  - this pair reused the same worker outputs rather than spawning a redundant new wave
  - the main lane did the last-mile diagnosis itself, found the stale accessibility state, patched it, and revalidated with the minimum honest proof set

## Checkpoint 2026-04-17 09:55:00 -05:00
- The design-audit follow-on wave started from `C:\Users\tateb\Downloads\senku_design_audit.md` and reconciled the audit against current landed UI instead of treating it like a reset.
- Still-real smallest-first slices were narrowed to:
  - search-result scan polish
  - detail answer chunking / step hierarchy
  - home-entry lane emphasis
  - surface materiality pass
- Slice 1 landed:
  - `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
    - result titles, sections, and snippets now use lightweight query-term emphasis during active search/ask result states
    - markdown cleanup now strips a wider set of inline artifacts, including heading markers leaking into snippets
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - search/ask now dismiss the IME and hand focus back to the content surface when a query is submitted
  - `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
    - `search_results` capture now collapses the search keyboard before artifact capture so the harness stops preserving keyboard-obscured result states
- Slice 2 landed:
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - answer-mode body rendering now uses styled text rather than a flat block
    - `Short answer`, `Steps`, and `Limits or safety` now have stronger hierarchy
    - numbered steps now use bold/accented numbering plus hanging-indented continuation lines
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - phone portrait host smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/summary.json`
  - phone landscape basic smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5560/summary.json`
  - fresh screenshot proofs now live in:
    - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
    - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5560/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
    - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/screenshots/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- Orchestration note:
  - one `gpt-5.4` xhigh review narrowed the still-real audit gaps
  - two disjoint `gpt-5.4` high workers handled the first implementation wave
  - the main lane then closed two honest validation gaps itself:
    - landscape search was still being obscured by IME state
    - inline `###` heading markers were still leaking into snippets

## Checkpoint 2026-04-17 10:05:00 -05:00
- The second design-audit wave landed after another xhigh pass narrowed the next work to:
  - home-entry lane emphasis
  - surface materiality pass
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - added a small lane-truth state so Browse is primary on cold home and Ask only becomes primary during a real ask flow
  - lane helper copy now reflects the active lane instead of reusing one generic comparison blurb
  - returning from answer/detail into a result-backed home state now dismisses the IME instead of reviving keyboard focus
- `android-app/app/src/main/res/values/strings.xml`
  - replaced generic home lane helper text with browse-specific, ask-specific, and browse-results variants
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
  - tightened the cold-home proof to assert browse-primary state
  - added a focused return-from-answer proof for ask-lane emphasis
- `android-app/app/src/main/res/values/colors.xml`
  - added deeper olive / warm parchment support colors for the shared-shell refresh
- `android-app/app/src/main/res/drawable/bg_main_screen.xml`
- `android-app/app/src/main/res/drawable/bg_hero_panel.xml`
- `android-app/app/src/main/res/drawable/bg_surface_panel.xml`
- `android-app/app/src/main/res/drawable/bg_search_shell.xml`
- `android-app/app/src/main/res/drawable/bg_result_card.xml`
  - all refreshed with a low-risk XML-only material pass: deeper gradients, slightly stronger shell layering, and warmer parchment/result-card treatment
- Validation:
  - rebuild passed with `:app:assembleDebug` and `:app:assembleDebugAndroidTest`
  - focused home-entry / ask-return proof passed in:
    - `artifacts/instrumented_ui_smoke/20260417_100430_644/emulator-5556/summary.json`
  - phone landscape material/search proof passed in:
    - `artifacts/instrumented_ui_smoke/20260417_100202_388/emulator-5560/summary.json`
  - phone portrait host/detail proof passed in:
    - `artifacts/instrumented_ui_smoke/20260417_100236_402/emulator-5556/summary.json`
  - key screenshot proofs now live in:
    - `artifacts/instrumented_ui_smoke/20260417_100430_644/emulator-5556/screenshots/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
    - `artifacts/instrumented_ui_smoke/20260417_100430_644/emulator-5556/screenshots/returningFromAnswerKeepsAskLaneEmphasis__ask_lane_return.png`
    - `artifacts/instrumented_ui_smoke/20260417_100202_388/emulator-5560/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
    - `artifacts/instrumented_ui_smoke/20260417_100236_402/emulator-5556/screenshots/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- Orchestration note:
  - one `gpt-5.4` xhigh reviewer narrowed the second wave
  - two disjoint `gpt-5.4` high workers handled home-state logic and XML material treatment separately
  - the main lane then closed the last small UX gap itself by ensuring the ask-return home state no longer bounces back into the keyboard
