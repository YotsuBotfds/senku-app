# External Review 1151 Execution - 2026-04-17

Source note:
- `C:\Users\tateb\Documents\senku_local_testing_bundle_20260410\1151-4-17-26.html`

Purpose:
- pick the still-usable ideas from the second review wave
- land them with the same design / assign / wait / integrate loop
- keep only ideas that deepen trust and field-manual clarity without drifting into gimmicks

## Usable Ideas

Adopted:
- `Strict label:value provenance`
- `Tablet landscape rail rename`
- `Tablet portrait chip dedup`
- `Corpus-limit language hardening`
- `Guide-body/manual-warning cleanup`

Adapted:
- `Hardware nav segmented control`
  - still usable
  - should be implemented as a tougher grouped control, not a flashy new widget
- `Mute extraneous field-links instructions`
  - still usable
  - the rail can assume more competence now
- `Search bar target icon`
  - still usable
  - should stay subtle and not compete with the `Find` action
- `T-minus timestamps`
  - still usable
  - should be done consistently on recent-thread surfaces, not piecemeal
- `Action chevrons on follow-ups`
  - still usable
  - low-risk way to make follow-up actions feel more intentional
- `Linked paths card fill`
  - still usable
  - should read like dossier tabs, not empty outlines
- `Corpus-limit contrast shift`
  - still usable
  - amber can improve glare readability without breaking the palette
- `Category badges in wide lists`
  - still usable
  - best fit for the tablet `Field links` list, not every surface

Rejected for this pass:
- `Hazard stripe borders`
  - too theatrical relative to the current manual language
- `Heartbeat status dot`
  - risks feeling gadgety instead of austere
- `Massive category watermarks`
  - too decorative for the current surface density
- `Hard-cut scroll fades / custom scrollbars`
  - too invasive for too little trust gain in this pass

## Loop Structure

Loop rule used:
- main lane writes the plan
- two disjoint workers implement one pair at a time
- both workers must finish before integration
- validation runs only after integration

## Remaining Usable Ideas

- `Hardware nav segmented control`
- `Mute extraneous field-links instructions`
- `Search bar target icon`
- `T-minus timestamps`
- `Action chevrons on follow-ups`
- `Linked paths card fill`
- `Corpus-limit contrast shift`
- `Category badges in wide lists`

## Reviewed Next Loops

### Loop 3

Planned pair:
- `Hardware nav segmented control`
- `T-minus timestamps`

Worker split:
- Worker A:
  - detail top-bar layout/drawable grouping
- Worker B:
  - home recent-thread timestamp formatting

### Loop 4

Planned pair:
- `Mute extraneous field-links instructions`
- `Category badges in wide lists`

Worker split:
- Worker A:
  - field-links copy tightening
- Worker B:
  - tablet field-links row treatment

### Loop 5

Carry-forward usable ideas:
- `Search bar target icon`
- `Linked paths card fill`
- `Action chevrons on follow-ups`
- `Corpus-limit contrast shift`

Notes:
- these are still considered usable
- they were pushed behind Loops 3 and 4 because the reviewer found cleaner worker boundaries there first

## Loop 1

Status:
- `done`

Focus:
- tablet portrait dedup
- tablet landscape rail rename
- compact corpus-gap wording
- first pass at guide-body/manual-warning cleanup

Landed:
- wide-tablet rail now uses `Field links`
- tablet portrait guide mode hides redundant top-state chips when the title rail already carries the state
- compact phone low-coverage answer now uses an explicit `Corpus limit` / pack-gap wording path
- guide-body cleanup strips fence markers, duplicated warning labels, backticks, markdown-link residue, and heading markup in the validated cases

Validation:
- tablet portrait chip dedup:
  - `artifacts/instrumented_ui_smoke/20260417_113847_600/emulator-5554/summary.json`
- tablet landscape field-links proof:
  - `artifacts/instrumented_ui_smoke/20260417_113847_652/emulator-5558/summary.json`
- compact phone corpus-gap proof:
  - `artifacts/instrumented_ui_smoke/20260417_114155_187/emulator-5556/summary.json`
- landscape-phone regression spot check:
  - `artifacts/instrumented_ui_smoke/20260417_114211_720/emulator-5560/summary.json`

## Loop 2

Status:
- `done`

Focus:
- final real-guide residue cleanup
- stricter proof coverage for synthetic and installed warning flows

Landed:
- synthetic warning cleanup now removes duplicated warning labels rather than only removing fence syntax
- real installed guide pages no longer leak the markdown-link / heading residue that was still visible after Loop 1

Validation:
- phone synthetic warning cleanup:
  - `artifacts/instrumented_ui_smoke/20260417_114726_052/emulator-5556/summary.json`
- tablet landscape installed-guide warning cleanup:
  - `artifacts/instrumented_ui_smoke/20260417_114726_072/emulator-5558/summary.json`

## Loop 3

Status:
- `done`

Focus:
- hardware-nav segmented control
- `T-minus` recent-thread timestamps

Landed:
- detail top actions now sit inside one grouped segmented shell instead of reading like separate floating pills
- recent-thread home metadata now uses `T-00:30` / `T-2d 03h` style timestamps across the surfaced recent-thread cards

Validation:
- phone portrait home entry:
  - `artifacts/instrumented_ui_smoke/20260417_121247_982/emulator-5556/summary.json`
- phone landscape home entry:
  - `artifacts/instrumented_ui_smoke/20260417_121322_859/emulator-5560/summary.json`
- tablet portrait home entry:
  - `artifacts/instrumented_ui_smoke/20260417_121248_018/emulator-5554/summary.json`
- tablet landscape home entry:
  - `artifacts/instrumented_ui_smoke/20260417_121322_859/emulator-5558/summary.json`
- phone portrait generative detail:
  - `artifacts/instrumented_ui_smoke/20260417_121349_676/emulator-5556/summary.json`
- phone landscape generative detail:
  - `artifacts/instrumented_ui_smoke/20260417_121349_702/emulator-5560/summary.json`
- tablet portrait generative detail:
  - `artifacts/instrumented_ui_smoke/20260417_121349_702/emulator-5554/summary.json`
- tablet landscape detail context proof:
  - `artifacts/instrumented_ui_smoke/20260417_121515_217/emulator-5558/summary.json`

Direct proof notes:
- the new `T-...` format is visible in the captured home dumps, including:
  - `artifacts/instrumented_ui_smoke/20260417_121247_982/emulator-5556/dumps/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
  - `artifacts/instrumented_ui_smoke/20260417_121322_859/emulator-5560/dumps/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
  - `artifacts/instrumented_ui_smoke/20260417_121248_018/emulator-5554/dumps/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
  - `artifacts/instrumented_ui_smoke/20260417_121322_859/emulator-5558/dumps/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.xml`
- one older tablet-landscape generative-detail assertion failed on a separate provenance-wording expectation, so the final landscape-tablet proof for this loop uses the stable destination-context detail state instead of claiming that unrelated assertion as part of Loop 3

## Loop 4

Status:
- `done`

Focus:
- mute extraneous `Field links` instructions
- add restrained category badges in wide tablet field-links rows

Landed:
- tablet landscape `Field links` subtitle now stays terse instead of coaching the user through the rail
- field-links preview caption and panel description now use the tighter preview-first copy
- wide-tablet field-links rows now carry a tiny category marker for faster peripheral sorting without turning the rail into a decorative dashboard

Validation:
- landscape-tablet field-links destination/context proof:
  - `artifacts/instrumented_ui_smoke/20260417_122339_405/emulator-5558/summary.json`
- landscape-tablet warning regression after pack resync:
  - `artifacts/instrumented_ui_smoke/20260417_122417_533/emulator-5558/summary.json`

Direct proof notes:
- the first validation attempt exposed one real miss: the visible sidebar subtitle still used the older tutorial sentence
- after tightening that subtitle and reinstalling a fresh build on `emulator-5558`, the landscape-tablet proof passed cleanly
- the warning regression briefly failed only because the reinstall left the tablet with a stale pack checksum; repushing the current mobile pack resolved that and the warning lane passed again

## Loop 5

Status:
- `done`

Focus:
- search-bar target icon
- linked-paths card fill
- action chevrons on follow-ups
- corpus-limit contrast shift

Landed:
- all five home layouts now carry a small muted target cue inside the search field
- home `Linked paths` / `Guide connections` buttons now read like filled dossier tabs instead of empty outline pills
- follow-up send and answer-side suggested-next-step actions now carry a restrained directional chevron affordance
- low-coverage / corpus-limit treatment now uses a clearer amber accent instead of the muddier warning brown

Validation:
- phone portrait home entry:
  - `artifacts/instrumented_ui_smoke/20260417_123105_942/emulator-5556/summary.json`
- phone landscape home entry:
  - `artifacts/instrumented_ui_smoke/20260417_123105_925/emulator-5560/summary.json`
- phone portrait deterministic detail:
  - `artifacts/instrumented_ui_smoke/20260417_123130_360/emulator-5556/summary.json`
- phone portrait compact corpus-gap proof:
  - `artifacts/instrumented_ui_smoke/20260417_123140_126/emulator-5556/summary.json`

Direct proof notes:
- the portrait-home screenshot shows the search target cue and the filled linked-path tabs together:
  - `artifacts/instrumented_ui_smoke/20260417_123105_942/emulator-5556/screenshots/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- the compact corpus-gap screenshot shows the amber corpus-limit treatment and the new directional action affordances together:
  - `artifacts/instrumented_ui_smoke/20260417_123140_126/emulator-5556/screenshots/compactPhoneAnswerShowsCorpusGapWording__compact_corpus_gap.png`

## Notes

- The second review wave had several aesthetically interesting ideas, but the high-value gains were still trust surfaces and display cleanup, not new decorative chrome.
- The strongest durable changes from this pass are:
  - `Field links`
  - chip dedup on tablet portrait
  - explicit corpus-limit language
  - real-guide warning/body sanitization
