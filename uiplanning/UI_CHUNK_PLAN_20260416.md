# Senku UI Chunk Plan - 2026-04-16

This plan starts from the current landed state:
- host-to-on-device generative fallback is working
- recent-thread persistence/trust is working
- guide-detail related-guide navigation MVP is working
- instrumentation harness is healthy again on emulator after the Espresso input-path fix

The next UI program should deepen the app as a knowledge hub without breaking the trust spine.

## Review + Status

- This plan was narrowed after a `gpt-5.4` xhigh review.
- The review confirmed that the practical first split is:
  - one detail-only lane
  - one narrowed home-only lane
- It also confirmed that browse/result enrichment and deeper tablet-station work should wait until the first graph surfaces settle.

### First Slice Landed

- Chunk 1 landed:
  - guide-mode related-guide navigation now reads more intentionally as linked-guide movement, not helper prompts
  - related-guide button labels and accessibility copy are stronger
  - related-guide instrumentation smoke remains green
- Chunk 2a + the first small slice of 2b landed:
  - home now selects one anchor from recent-thread context, with pinned-guide fallback
  - home now shows one `Continue exploring` shelf when an anchor and related guides exist
  - the shelf hides cleanly when no anchor/graph result is available

Validation artifacts:
- `artifacts/instrumented_ui_smoke/20260416_180017_231/emulator-5554`
- `artifacts/instrumented_ui_smoke/20260416_180127_797/emulator-5554`
- `artifacts/live_debug/home_related_home_20260416.xml`
- `artifacts/live_debug/home_related_home_20260416.png`

### Next Reviewed Slice Pair

- A second `gpt-5.4` xhigh review confirmed the right next pair is:
  - `Chunk 4a / Trust spine continuity`
  - `Chunk 3a / Minimal browse bridges`
- The review also narrowed `Chunk 3a` from an implied count/family lane to a preview-bridge lane:
  - show a compact related-guide cue
  - do not imply an exact family count with the current repository API
  - do not perform repository work inside `SearchResultAdapter`

### Second Pair Landed

- `Chunk 4a / Trust spine continuity` landed:
  - in-flight answer status now keeps backend/source trust cues steadier through generation, stall, and completion
  - generated-detail smoke coverage now checks that settled answers keep trust meta and visible source backing
- `Chunk 3a / Minimal browse bridges` landed:
  - result cards now show a compact `Linked guides` cue when the top guide-backed rows have adjacent-path availability
  - enrichment is still done in `MainActivity`, not per-row in `SearchResultAdapter`
- Validation / fixes:
  - trust lane compile + unit coverage passed
  - browse lane was proven with direct emulator browse captures in:
    - `artifacts/live_debug/browse_bridge_fire_20260416.xml`
    - `artifacts/live_debug/browse_bridge_fire_20260416.png`
  - the browse proof surfaced and fixed one real regression:
    - landscape/tablet `list_item_result.xml` variants were missing `result_related_cue`
    - `SearchResultAdapter` now also guards against null cue views
  - post-fix basic smoke re-passed in:
    - `artifacts/instrumented_ui_smoke/20260416_190420_094/emulator-5554`

### Third Pair Landed

- A third reviewed cycle narrowed the next pair to:
  - `Chunk 2b completion / Home hub depth`
  - `Chunk 5a / Tablet station linked-guide promotion`
- `Chunk 2b completion` landed:
  - the home graph lane now reads as a stronger surfaced knowledge shelf instead of a loose extra row
  - `Guide connections` now carries source-aware subtitle framing, persistent anchor text, and a compact empty state
  - the shelf stays distinct from `Recent threads` / `Pinned guides` through its own surfaced panel and scroll container
- `Chunk 5a` landed:
  - wide-tablet guide mode now promotes linked-guide movement into a dedicated cross-reference rail
  - answer-mode helper prompts stay in the helper stack
  - phone and tablet portrait behavior remain unchanged
- Validation:
  - rebuild/install fast lane passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - phone regression/basic smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_191516_594/emulator-5554`
  - wide-tablet guide-detail smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_191556_818/emulator-5560`
  - seeded home capture for the stronger graph shelf:
    - `artifacts/live_debug/home_hub_recent_20260416.xml`
    - `artifacts/live_debug/home_hub_recent_20260416.png`

### Fourth Pair Landed

- A fourth reviewed cycle narrowed the next pair to:
  - `Chunk 2c / Home guide-connections guidance`
  - `Chunk 5b / Tablet cross-reference preview hierarchy`
- `Chunk 2c` landed:
  - the home `Guide connections` lane now uses stateful copy instead of the earlier generic recent/pinned subtitle
  - when linked guides are ready, the subtitle now says the latest thread or pinned guide is steering the shelf and includes the linked-guide count
  - the anchor line now reads as a pivot statement instead of raw provenance framing
  - the empty state now tells the user what to do next, rather than only reporting no linked guides
- `Chunk 5b` landed:
  - wide-tablet guide mode keeps preview-first behavior, but the rail preview now reads as cross-reference navigation instead of source/provenance chrome
  - the preview card now includes explicit in-rail guidance and uses `Open full guide` as the exit action
  - related-guide smoke now asserts preview/open-full-guide hierarchy instead of only generic linked-guide wording
- Validation:
  - rebuild passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - phone regression/basic smoke passed on the phone emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_211453_162/emulator-5556`
  - wide-tablet related-guide smoke passed after re-pushing the current mobile pack to the tablet emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_211546_466/emulator-5558`
  - seeded home capture showing the new guidance copy lives in:
    - `artifacts/live_debug/home_guide_connections_guidance_20260416.xml`
    - `artifacts/live_debug/home_guide_connections_guidance_20260416.png`

### Fifth Pair Landed

- A fifth reviewed cycle narrowed the next pair to:
  - `Chunk 2d / Home related-guide button context`
  - `Chunk 5c / Tablet cross-reference identity cleanup`
- `Chunk 2d` landed:
  - home `Guide connections` buttons now stay chip-sized, but carry a second context line when existing guide metadata supports it
  - TalkBack copy for home related-guide buttons now explains why each guide is present and references the active Home pivot
  - pinned guides remain unchanged; the richer treatment is home-related only
- `Chunk 5c` landed:
  - wide-tablet preview-first behavior stays unchanged, but the cross-reference preview panel now uses a surface-panel treatment instead of provenance styling
  - all visible tablet cross-reference preview copy now comes from resources instead of hardcoded literals
  - related-guide smoke now asserts the updated cross-reference title/caption/open-button wording
- Validation:
  - rebuild passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - phone regression/basic smoke passed on the phone emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_213405_442/emulator-5556`
  - wide-tablet related-guide smoke passed on the tablet emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_213405_536/emulator-5558`
  - cold-home proof for the richer related-guide pills lives in:
    - `artifacts/live_debug/home_guide_connections_buttons_20260416.xml`
    - `artifacts/live_debug/home_guide_connections_buttons_20260416.png`

### Sixth Pair Landed

- A sixth reviewed cycle narrowed the next pair to:
  - `Chunk 2e / Home pivot handoff`
  - `Chunk 5d / Tablet cross-reference compare cue`
- `Chunk 2e` landed:
  - the `Guide connections` anchor is now a first-class CTA instead of passive pivot text
  - the same current Home pivot guide can be opened directly from the shelf in both populated and empty states
  - the CTA keeps recent-thread vs pinned-guide context in both visible copy and accessibility text
- `Chunk 5d` landed:
  - wide-tablet guide mode now keeps a persistent `Current guide` context card above the selected linked-guide preview
  - selected linked-guide preview copy now reads as a comparison against the active guide instead of a floating preview
  - preview-first behavior and `Open full guide` navigation remain unchanged
- Validation:
  - rebuild passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - phone basic smoke passed on the phone emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_215111_734/emulator-5556`
  - wide-tablet related-guide smoke passed on the tablet emulator lane in:
    - `artifacts/instrumented_ui_smoke/20260416_215114_554/emulator-5558`
  - seeded home proof for the new pivot CTA lives in:
    - `artifacts/live_debug/home_pivot_cta_20260416.xml`
    - `artifacts/live_debug/home_pivot_cta_20260416.png`
  - the forward scout after this pair pointed at the next smallest slice:
    - `Chunk 4 / Trust spine progression` on detail, especially tightening how in-flight, stalled, and completed answers keep trust/provenance continuity

### Tenth Pair Landed

- A tenth reviewed cycle narrowed the next pair to:
  - `Chunk 2f / Home + browse graph-handoff continuity`
  - `Chunk 5e / Detail-to-detail graph-handoff continuity`
- `Chunk 2f` landed:
  - Home `Guide connections` chips now open destination guide detail with visible guide-mode context that names the source connection instead of dropping the user onto a contextless guide page
  - browse cross-reference handoffs now preserve the originating guide title on the destination detail screen without changing primary result-row taps
  - pinned-guide opens and the Home pivot CTA remain neutral guide opens rather than picking up graph-handoff wording
- `Chunk 5e` landed:
  - phone/tall guide-to-guide opens now preserve current-guide compare context on the destination page
  - wide-tablet rail `Open full guide` now carries cross-reference context into the destination full-guide screen instead of resetting to a generic guide view
  - answer-source `Open full guide` still stays neutral, so provenance opens do not get mixed up with graph-navigation language
- Validation:
  - rebuild passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - targeted phone browse handoff smoke passed with explicit phone-role facts in:
    - `artifacts/instrumented_ui_smoke/20260416_231810_052/emulator-5556`
  - targeted phone home-origin handoff smoke passed in:
    - `artifacts/instrumented_ui_smoke/20260416_231842_246/emulator-5556`
  - targeted tablet landscape guide-to-guide handoff smoke passed with explicit tablet-landscape facts in:
    - `artifacts/instrumented_ui_smoke/20260416_231805_043/emulator-5558`
  - the summary metadata for those runs now explicitly proves the intended posture/device mapping:
    - `resolved_role: phone` on `emulator-5556`
    - `resolved_role: tablet` and `current_orientation: landscape` on `emulator-5558`

### Eleventh Pair Landed

- An eleventh reviewed cycle narrowed the next pair to:
  - `Chunk 2g / Graph source identity plumbing`
  - `Chunk 5f / Non-rail source-return card`
- `Chunk 2g` landed:
  - Home `Guide connections` buttons now capture the rendered anchor/source identity at bind time instead of reading mutable home state later
  - browse cross-reference opens keep their existing graph-aware path, while primary result-row taps, pinned-guide opens, and the Home pivot CTA remain neutral opens
- `Chunk 5f` landed:
  - non-rail guide detail now shows a compact return/source card above `Guide cross-reference` when the page was opened through graph navigation
  - phone/tall guide-to-guide opens and Home-origin graph opens can now return through that local card instead of relying only on top summary text
  - answer-source provenance opens stay neutral and do not show the return card
  - wide-tablet rail behavior remains unchanged except that the destination page continues to preserve source/current-guide context
- Validation:
  - rebuild passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - targeted phone browse handoff proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233236_378/emulator-5556`
  - targeted phone home-origin handoff proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233317_077/emulator-5556`
  - targeted phone non-rail return-card proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233307_397/emulator-5556`
  - targeted tablet-landscape destination continuity proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_233231_091/emulator-5558`
  - the role/orientation facts remain explicit in those summaries:
    - `resolved_role: phone` on `emulator-5556`
    - `resolved_role: tablet` and `current_orientation: landscape` on `emulator-5558`

### Twelfth Pair Landed

- A twelfth reviewed cycle narrowed the next pair to:
  - `Chunk 5g / Non-rail cross-reference preview-first`
  - `Chunk 5h / Non-rail current-guide compare cue`
- `Chunk 5g` landed:
  - non-rail guide detail now uses preview-first cross-reference behavior when the preview panel exists, instead of immediately page-switching on first tap
  - the first tap now reveals a selected-guide preview plus `Open full guide`, while wide-tablet rail auto-preview behavior stays unchanged
- `Chunk 5h` landed:
  - phone, phone-landscape, and tall-tablet non-rail layouts now expose the same active-guide compare card IDs as the rail flow, but with non-rail-neutral compare copy
  - answer-mode provenance copy and provenance opens stay neutral
  - the neutral-provenance smoke initially exposed a test-helper bug, then passed once the helper was corrected to prefer a source chip before clicking `Open full guide`
- Validation:
  - rebuild passed with:
    - `android-app :app:assembleDebug`
    - `android-app :app:assembleDebugAndroidTest`
  - targeted phone off-rail preview-first proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_234342_092/emulator-5556`
  - targeted tablet-landscape regression proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_234348_363/emulator-5558`
  - targeted neutral answer-source provenance proof passed in:
    - `artifacts/instrumented_ui_smoke/20260416_234605_601/emulator-5556`
  - the failed first provenance proof that surfaced the helper bug is preserved in:
    - `artifacts/instrumented_ui_smoke/20260416_234412_161/emulator-5556`

## Chunk 1 - Guide Graph Detail

Goal:
- turn the new guide-detail related-guide MVP into a more intentional knowledge-navigation surface

Scope:
- keep answer-mode follow-up prompts as prompts
- keep guide-mode related-guide rows as navigation
- tighten copy, labels, and accessibility so the two behaviors are clearly distinct
- add any small contextual cues that make the linked-guide panel read as knowledge navigation rather than helper chrome

Likely files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Acceptance:
- guide detail clearly communicates that these are linked guides, not follow-up prompts
- answer-mode and guide-mode semantics remain distinct
- instrumentation smoke continues to prove related-guide navigation

## Chunk 2a - Shared Graph Surface Plumbing

Goal:
- avoid duplicating graph lookup/anchor logic across every surface that wants linked-guide data

Scope:
- keep this minimal
- define one reusable way to pick the current anchor guide for home-facing graph surfaces
- make sure home can consume related-guide data without per-row adapter lookups or ad hoc duplicated selection logic

Likely files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- possibly a tiny new helper class only if reuse becomes cleaner than inlining

Acceptance:
- one clear path exists for home to load related guides from a single anchor
- no per-row repository work is pushed into adapters

## Chunk 2b - Home Hub Lanes

Goal:
- make home feel like a knowledge hub, not just a launcher

Scope:
- add one lightweight graph-driven shelf on home based on one explicit anchor rule
- candidates: `Continue exploring`, `Related to your last guide`, or `Next practical paths`
- use recent-thread / pinned-guide anchors rather than inventing a new recommendation system

Likely files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
- `android-app/app/src/main/res/values/strings.xml`

Acceptance:
- home offers at least one graph-driven path forward from recent/pinned context
- the new shelf feels grounded in the installed pack, not decorative

## Chunk 3 - Browse Bridges

Goal:
- help users pivot across guide families before opening detail

Scope:
- add small cross-reference cues in results/browse
- avoid high-density clutter on phone portrait
- include any data shaping or prefetch required up front; do not do repository lookups per row in the adapter
- likely use compact chips or a single family/related count cue instead of full shelves

Likely files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/res/layout/list_item_result.xml`
- `android-app/app/src/main/res/values/strings.xml`

Acceptance:
- browse/results exposes some adjacent-path signal without turning into a noisy card wall
- users can discover family movement before opening a guide

## Chunk 4 - Trust Spine Progression

Goal:
- make generation/provenance/trust state feel more coherent now that fallback and graph navigation are real

Scope:
- tighten the transition from retrieval -> answer building -> answer ready
- keep source/provenance visibility strong during slow answers
- make sure route / backend / evidence cues remain stable while the content changes

Likely files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Acceptance:
- staged/slow answers still feel trustworthy and anchored
- fallback and provenance states read as one coherent experience

## Chunk 5 - Tablet Station Depth

Goal:
- use the tablet station posture more aggressively for cross-reference and inspection

Scope:
- only revisit this after chunks 1/2/4 settle
- surface related-guide movement more clearly in the landscape tablet utility rail if the earlier graph work proves valuable enough
- preserve the reading column while making the side rail more useful
- avoid duplicating every phone surface in the rail

Likely files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- `android-app/app/src/main/res/values/strings.xml`

Acceptance:
- tablet landscape better expresses “station” behavior than phone-with-rail
- cross-reference and provenance inspection both feel native to the wide layout

## Proposed Implementation Order

1. Chunk 1 - Guide Graph Detail
2. Chunk 2a - Shared Graph Surface Plumbing
3. Chunk 2b - Home Hub Lanes
4. Chunk 4 - Trust Spine Progression
5. Chunk 3 - Browse Bridges
6. Chunk 5 - Tablet Station Depth
7. Chunk 4b - Detail Trust-Language Consolidation
8. Chunk 3b - Browse Linked-Guide Handoff + Non-Rail Cross-Reference Copy
9. Chunk 3c - Home + Browse Cross-Reference Copy Parity

Rationale:
- Chunk 1 finishes the just-landed graph MVP cleanly
- Chunk 2a prevents duplicated graph/anchor logic on home/results
- Chunk 2b makes the graph visible from the app's entry point with a single low-risk shelf
- Chunk 4 keeps trust coherent while the product grows, but it is coupled to the detail state machine and should not run in parallel with chunk 1
- Chunk 3 is valuable, but easier to over-clutter and currently needs data shaping first
- Chunk 5 is lower leverage right now because tablet station already has meaningful dedicated rail surfaces
- Chunk 4b is the follow-through slice that keeps route, backend, and provenance language aligned after the earlier fallback and graph-navigation waves
- Chunk 3b lets browse do one real graph-driven thing and brings phone/tall-detail guide mode up to the cross-reference language the tablet rail already uses
- Chunk 3c finishes language parity across home and browse now that the behavior is already real on both surfaces

## Constraints

- preserve the current route -> anchor -> evidence -> follow-up spine
- do not regress answer-mode follow-up behavior
- prefer small, testable slices over broad relayouts
- emulator-first validation is acceptable, with physical-device checkpoints after meaningful batches

## Landed After Sixth Pair

### Chunk 4b - Detail Trust-Language Consolidation

Goal:
- make in-flight, stalled, and settled answer states speak one consistent trust language

Scope:
- unify route, backend, and provenance wording across header meta, `Why this answer`, source subtitles, and provenance preview
- preserve host-fallback truth after the answer settles
- keep source/provenance actions semantically stable while the answer state changes

Files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Outcome:
- the detail trust spine now uses one shared route/backend summary path instead of assembling slightly different stories in each panel
- host-backed runs that fall back to on-device now keep that fallback truth visible after settle instead of silently snapping back to generic backend copy
- provenance/source copy stays aligned with the same route/backend summary and no longer drifts when answers stall
- the provenance action remains `Open full guide` instead of mutating into a weaker or misleading label during slow states

Validation:
- `:app:assembleDebug` passed
- `:app:assembleDebugAndroidTest` passed
- first host smoke attempt surfaced a real stale test-helper bug in the settled-trust assertions:
  - `artifacts/instrumented_ui_smoke/20260416_221038_487/emulator-5556/summary.json`
- after fixing the helper to inspect the live detail screen via UI Automator, the full host smoke passed:
  - `artifacts/instrumented_ui_smoke/20260416_221314_594/emulator-5556/summary.json`

### Chunk 3b - Browse Linked-Guide Handoff + Non-Rail Cross-Reference Copy

Goal:
- make browse’s existing linked-guide cue do one real navigation job
- make non-rail guide detail speak the same cross-reference language as the tablet rail

Scope:
- turn the existing prefetched browse linked-guide cue into a real secondary handoff without replacing primary row tap
- keep repository work in `MainActivity`, not `SearchResultAdapter`
- clean up phone/tall-tablet guide-detail linked-guide wording so it stops reading like helper prompts

Files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Outcome:
- rows that already have prefetched linked-guide data now expose one real secondary handoff
- primary row tap still opens the original result
- phone and tall-tablet guide mode now frame linked guides as `Guide cross-reference` instead of `Related survival paths`
- wide-tablet rail behavior stays on its existing preview-first cross-reference path

Validation:
- phone basic regression smoke passed:
  - `artifacts/instrumented_ui_smoke/20260416_223440_239/emulator-5556/summary.json`
- targeted browse linked-guide handoff smoke passed:
  - `artifacts/instrumented_ui_smoke/20260416_223641_728/emulator-5556/summary.json`
- targeted non-rail guide-detail cross-reference smoke passed:
  - `artifacts/instrumented_ui_smoke/20260416_223708_490/emulator-5556/summary.json`
- tablet landscape related-guide rail smoke re-passed:
  - `artifacts/instrumented_ui_smoke/20260416_223358_782/emulator-5558/summary.json`

### Chunk 3c - Home + Browse Cross-Reference Copy Parity

Goal:
- align home and browse language with the cross-reference model already established in detail

Scope:
- keep behavior unchanged
- replace remaining `linked guide` / `related guide` home+browse phrasing with `guide connection` / `cross-reference` wording where it clarifies the product model

Files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Outcome:
- home `Guide connections` subtitle, empty state, and accessibility rationale now frame those cards as cross-references from the current home guide connection
- browse cue language now reads as `Cross-ref` / `Guide connection` instead of `Linked` / `Linked guide`
- browse handoff behavior stays unchanged and still proves the secondary action opens the prefetched guide

Validation:
- targeted browse handoff smoke re-passed after the copy parity update:
  - `artifacts/instrumented_ui_smoke/20260416_224534_519/emulator-5556/summary.json`
- seeded home capture shows the new guide-connection / cross-reference wording:
  - `artifacts/live_debug/home_crossref_parity_20260416.xml`
  - `artifacts/live_debug/home_crossref_parity_20260416.png`

### Chunk 5i - Answer-Source Guide Connections + Graph/Provenance Split

Goal:
- bring knowledge-hub movement into answer mode without blurring provenance into graph navigation

Scope:
- keep provenance `Open full guide` neutral
- when the selected answer source has real guide-backed links, surface a source-anchored `Guide cross-reference` lane on the answer screen itself
- make cross-reference taps preserve source-guide context on the destination detail page

Files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Outcome:
- answer mode no longer dead-ends at provenance when the selected source guide has linked guides
- answer-side cross-reference movement is clearly distinct from helper prompts and from provenance `Open full guide`
- selecting a source without a guide-backed ID hides the graph lane instead of silently re-anchoring it to another source

Validation:
- targeted answer-source graph smoke passed:
  - `artifacts/instrumented_ui_smoke/20260417_050910_572/emulator-5556/summary.json`
- targeted neutral provenance smoke passed:
  - `artifacts/instrumented_ui_smoke/20260417_050935_306/emulator-5556/summary.json`
- targeted tablet-landscape guide-mode regression smoke re-passed:
  - `artifacts/instrumented_ui_smoke/20260417_050910_572/emulator-5558/summary.json`

### Chunk 5j - Answer-Source Preview-First + Clear Fallback Truth

Goal:
- make answer-side guide movement behave like the stronger guide-detail pattern without leaving stale graph/accessibility state behind

Scope:
- keep answer-side guide cross-reference preview-first on layouts that support inline preview
- keep the selected-source context card visible while preview is open
- when a non-guide source is selected, clear cross-reference identity and fall back cleanly to neutral helper prompts instead of stale graph wording

Files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

Outcome:
- answer-side cross-reference now proves preview-first behavior explicitly before `Open full guide`
- selected-source context remains anchored to the chosen source while preview is shown
- selecting a non-guide source clears the graph lane truthfully; any remaining helper panel no longer carries stale cross-reference accessibility copy

Validation:
- targeted answer-source preview/clear smoke passed:
  - `artifacts/instrumented_ui_smoke/20260417_052835_448/emulator-5556/summary.json`
- quick phone basic regression re-passed after the fallback/accessibility fix:
  - `artifacts/instrumented_ui_smoke/20260417_052907_814/emulator-5556/summary.json`
