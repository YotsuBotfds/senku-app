# External Review 1400 Execution - 2026-04-17

Source:
- user-provided HTML review: `Senku UI/UX Audit 1400 - Deepening the Field Manual`

Purpose:
- extract the still-usable ideas from the 1400 review wave
- deepen the field-manual identity without turning the UI into theater
- execute with the same plan / review / two-worker / wait / integrate loop used in the prior waves

## Usable Ideas

Adopted:
- `Monospace telemetry data`
- `Primary action weighting`
- `Structural warning indentation`
- `Solid material indexes`
- `Highlighted inline citations`
- `Serial number metadata string`
- `Nav segmented depressed state`
- `Category grid condensation`
- `Top header baseline rule`
- `Deterministic routing icon`

Adapted:
- `Terminal cursor in follow-up`
  - keep it subtle and contextual, not a constant animation stunt
- `Hardware battery and context readout`
  - only if the reading is cheap, trustworthy, and clearly subordinate to offline state
- `Bracketed section anchors`
  - best fit for strong guide section anchors or parsed headings, not every bold line
- `Find input active brackets`
  - likely better as a restrained active-state treatment than literal terminal cosplay

Rejected for this pass:
- `Muted data imprint`
  - too close to trust theater / watermark decoration for too little functional gain

## Loop Rule

Loop rule used:
- main lane writes the plan
- a high-reasoning reviewer checks the loop pairing before implementation
- two disjoint workers implement one pair at a time
- both workers must finish before integration
- validation runs only after integration

## Candidate Working Set

High-confidence candidates:
- `Monospace telemetry data`
- `Primary action weighting`
- `Structural warning indentation`
- `Solid material indexes`
- `Highlighted inline citations`
- `Serial number metadata string`
- `Nav segmented depressed state`
- `Category grid condensation`
- `Top header baseline rule`
- `Deterministic routing icon`

Careful candidates:
- `Terminal cursor in follow-up`
- `Hardware battery and context readout`
- `Bracketed section anchors`
- `Find input active brackets`

## Pending Review

Need reviewer help on:
- the cleanest pair boundaries
- which of the careful candidates are worth landing now versus carrying forward
- the best validation targets per pair

## Reviewed Plan

Careful candidates after review:
- `Terminal cursor in follow-up`
  - `reject`
  - the streamed answer path already carries a cursor treatment; adding one to the composer would tip into terminal cosplay
- `Hardware battery and context readout`
  - `defer`
  - no current battery/device-state plumbing exists, and the trust-bearing metadata is already anchored in pack / route / backend state
- `Bracketed section anchors`
  - `adapt`
  - use only on real anchors such as pivot / source / section surfaces, not every bold line
- `Find input active brackets`
  - `adapt`
  - translate into a restrained focused active-shell treatment, not literal bracket chrome

## Reviewed Loops

### Loop 1

Scope:
- `Primary action weighting`
- `Nav segmented depressed state`
- `Category grid condensation`
- `Top header baseline rule`
- home side of `Serial number metadata string`
- home/search active-state treatment instead of literal active brackets

Lane A:
- resource lane on the `activity_main.xml` variants plus related drawables / strings

Lane B:
- behavior lane on `MainActivity.java`
- home browse / ask emphasis
- category density behavior
- tests as needed

Validation targets:
- `homeEntryShowsPrimaryBrowseAndAskLanes`
- `searchQueryShowsResultsWithoutShellPolling`
- `returningFromAnswerKeepsAskLaneEmphasis`

### Loop 2

Scope:
- browse result-card telemetry cleanup
- linked-path / pivot clarity
- restrained anchor treatment on actual browse handoff surfaces

Lane A:
- result-card lane on `list_item_result.xml` and `SearchResultAdapter.java`

Lane B:
- linked-path / home-handoff lane on `MainActivity.java` and home linked-path surfaces

Validation targets:
- `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`
- `homeGuideIntentShowsGuideConnectionContext`
- `guideDetailUsesCrossReferenceCopyOffRail`

### Loop 3

Scope:
- `Structural warning indentation`
- `Solid material indexes`
- `Highlighted inline citations`
- adapted `Bracketed section anchors`

Lane A:
- parsing / styling lane in `DetailActivity.java`

Lane B:
- detail body / evidence resource lane in the `activity_detail.xml` variants

Validation targets:
- `guideBodyManualWarningFlowStripsFenceAndMarkupResidue`
- `guideDetailInstalledWarningGuideStripsMarkdownResidueOnTabletLandscape`
- `guideDetailShowsRelatedGuideNavigation`
- `answerModeSourceSelectionKeepsSourceAnchoredCrossReferenceLane`

### Loop 4

Scope:
- detail side of `Serial number metadata string`
- `Monospace telemetry data`
- `Deterministic routing icon`
- detail side of `Top header baseline rule`
- reuse the restrained active-state treatment from Loop 1

Lane A:
- metadata / route / follow-up logic in `DetailActivity.java`

Lane B:
- header / proof / follow-up resources in detail layouts and strings

Validation targets:
- `deterministicAskNavigatesToDetailScreen`
- `guideIntentFactoryCarriesRevisionStamp`
- `guideHeroSubtitleStaysHonestWhenHeaderMetaCollapsed`
- `autoFollowUpWithHostInferenceBuildsInlineThreadHistory`
- `scriptedSeededFollowUpThreadShowsInlineHistory`

Recommended order:
- `Loop 1 -> Loop 2 -> Loop 3 -> Loop 4`

## Execution Progress

### Loop 1 - Landed

Status:
- `complete`

Integrated files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/main/res/drawable/bg_search_shell.xml`
- `android-app/app/src/main/res/drawable/bg_search_shell_idle.xml`
- `android-app/app/src/main/res/drawable/bg_search_shell_active.xml`
- `android-app/app/src/main/res/drawable/bg_home_baseline_rule.xml`
- `android-app/app/src/main/res/drawable/bg_home_nav_segment_shell.xml`
- `android-app/app/src/main/res/values/colors.xml`
- `android-app/app/src/main/res/values/dimens.xml`
- `android-app/app/src/main/res/values-sw600dp/dimens.xml`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/main/res/values/strings_external_review_home.xml`

What landed:
- browse/ask now reads as a restrained segmented control instead of two equal parchment slabs
- the home stamp is condensed into a serial-style metadata row
- the search shell now has a focused active treatment instead of generic Android glow
- the top deck is anchored with a baseline divider
- category density is tighter and more useful on compact lanes
- browse remains the primary weighted home state unless the user is actively in the ask lane

Validation:
- build:
  - `.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest`
- instrumentation:
  - `artifacts/instrumented_ui_smoke/20260417_141733_043/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_141733_094/emulator-5560/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_141748_911/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_141801_155/emulator-5556/summary.json`

Visual signoff:
- portrait home and landscape home both hold the field-manual deck direction cleanly enough to proceed to browse/result work

### Loop 2 - Landed

Status:
- `complete`

Integrated files:
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/res/layout/list_item_result.xml`
- `android-app/app/src/main/res/layout-land/list_item_result.xml`
- `android-app/app/src/main/res/layout-sw600dp/list_item_result.xml`
- `android-app/app/src/main/res/values/strings_external_review_browse.xml`
- `android-app/app/src/main/res/values/strings_external_review_home.xml`

What landed:
- browse result cards now carry a cleaner telemetry row and a more deliberate support row
- linked-guide surfaces read as `guide connections` instead of generic extra chips
- home linked-path buttons now surface the pivot more explicitly
- browse-to-detail handoff carries richer source-guide context into the destination
- landscape-phone browse mode now uses an explicit mode flag instead of inferring state from mounted views

Validation:
- build:
  - `android-app/.\\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest`
- portrait phone:
  - `artifacts/instrumented_ui_smoke/20260417_143312_509/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_143329_441/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_143338_219/emulator-5556/summary.json`
- landscape phone:
  - `artifacts/instrumented_ui_smoke/20260417_144106_744/emulator-5560/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_144129_893/emulator-5560/summary.json`

Visual signoff:
- portrait and landscape browse/result states now both hold together cleanly enough to move into the detail-body pass

### Loop 3 - Landed

Status:
- `complete`

Integrated files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_material_tag.xml`
- `android-app/app/src/main/res/drawable/bg_detail_section_anchor.xml`
- `android-app/app/src/main/res/values/strings_external_review_detail_body.xml`
- `android-app/app/src/main/res/values/strings_external_review_detail_resources.xml`

What landed:
- warning blocks now scan like field-manual hazard callouts instead of ordinary inline text
- materials read as solid index tabs instead of dashed placeholder chips
- real structural headings pick up restrained bracket-style anchors
- inline citations are easier to pick out during fast scanning

Validation:
- build:
  - `android-app/.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest`
- portrait phone:
  - `artifacts/instrumented_ui_smoke/20260417_145350_230/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_145401_143/emulator-5556/summary.json`
- tablet landscape:
  - `artifacts/instrumented_ui_smoke/20260417_145415_701/emulator-5558/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_145826_299/emulator-5558/summary.json`

Visual signoff:
- the detail body finally reads like a survival-manual page instead of a generic answer blob

### Loop 4 - Landed

Status:
- `complete`

Integrated files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_detail_followup_active.xml`
- `android-app/app/src/main/res/drawable/bg_detail_header_baseline.xml`
- `android-app/app/src/main/res/values/strings_external_review_detail_header.xml`
- `android-app/app/src/main/res/values/strings_external_review_detail_meta.xml`

What landed:
- detail header metadata now reads like compact serial telemetry instead of stacked app diagnostics
- proof and provenance surfaces use restrained field-rail stamps and baseline rules
- deterministic routing keeps its explicit special-route identity
- follow-up rail copy and focus treatment now match the field-entry system instead of generic input chrome

Validation:
- build:
  - `android-app/.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest`
- portrait phone:
  - `artifacts/instrumented_ui_smoke/20260417_151819_701/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_151853_405/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_155021_941/emulator-5556/summary.json`
- portrait tablet:
  - `artifacts/instrumented_ui_smoke/20260417_151819_701/emulator-5554/summary.json`
- landscape phone:
  - `artifacts/instrumented_ui_smoke/20260417_151853_399/emulator-5560/summary.json`

Implementation note:
- the live host-follow-up proof needed the instrumentation helper to stage follow-up text and send as two distinct UI steps, and the trust assertion needed to recognize the new `EVD ...` serial evidence tokens

Visual signoff:
- the detail top rail, proof rail, and follow-up rail now feel like one coherent field-instrument surface

### Loop 3 - Landed

Status:
- `complete`

Integrated files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/res/drawable/bg_detail_warning_shell.xml`
- `android-app/app/src/main/res/drawable/bg_detail_material_tag.xml`
- `android-app/app/src/main/res/drawable/bg_detail_section_anchor.xml`
- `android-app/app/src/main/res/values/strings_external_review_detail_body.xml`
- `android-app/app/src/main/res/values/strings_external_review_detail_resources.xml`

What landed:
- warning blocks now read as structural field hazards instead of plain inline paragraphs
- material chips now read as indexed manual tabs
- real section anchors get restrained bracket treatment
- inline citations get stronger field-manual emphasis without turning into loud link chrome
- the tablet landscape guide-navigation proof now accepts the current `Field links` label while still enforcing preserved destination context

Validation:
- build:
  - `android-app/.\\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest`
- phone portrait:
  - `artifacts/instrumented_ui_smoke/20260417_145350_230/emulator-5556/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_145401_143/emulator-5556/summary.json`
- tablet landscape:
  - `artifacts/instrumented_ui_smoke/20260417_145415_701/emulator-5558/summary.json`
  - `artifacts/instrumented_ui_smoke/20260417_145826_299/emulator-5558/summary.json`

Visual signoff:
- the warning/body treatment now reads like a proper field-manual lane without collapsing the existing trust/navigation structure
