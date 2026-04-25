# External Review 1109 Execution - 2026-04-17

Source note:
- `C:\Users\tateb\Downloads\externalreview1109-4-17-26`

Purpose:
- turn the external review into a real execution tracker
- keep only the recommendations that still fit the current app
- record progress, validation, and follow-on decisions as we land each loop

## Current Status

Overall state:
- `in_progress`

Working rule for this pass:
- design in the main lane
- split implementation into 2 disjoint worker lanes
- wait for both workers to finish
- integrate only after both are back
- validate immediately after each integrated loop

## Adopt / Adapt / Reject

### Adopt

- `Home redundancy trim`
  - the current home screen still doubles status meaning:
    - `Manual ready`
    - `Ready offline with 692 guides.`
    - header count
- `Corpus-gap styling`
  - `No steps available.` still reads too much like failure rather than honest corpus limits
- `Danger / warning hardening`
  - guide-mode raw warning blocks still need more visual/manual weight
- `Proof surface schematicization`
  - the current compact proof block is still too sentence-heavy on phone
- `Tablet portrait chip dedup`
  - `Manual entry` + `Single guide open` can still overstate what the title rail already says

### Adapt

- `Guide connections` naming
  - the underlying critique is right
  - we should move to clearer field-manual language without drifting into vague desktop wording
- `Cross-reference rail` naming
  - the critique is right
  - the replacement should stay clear and trustworthy, not theatrical
- `Search button hardening`
  - the current button still feels generic
  - we should harden the label/treatment without sacrificing obviousness on compact screens

### Reject

- `fragment_home.xml`
  - stale file reference; this app uses `activity_main.xml` variants
- `ic_field_search.xml`
  - not present in this repo
- `Nexus 5 verification note`
  - stale relative to the current fixed emulator matrix

## Design Targets

### Loop 1

Home lane:
- collapse home readiness into one terse status story
- harden the search action from generic `Search` to a more manual-feeling action
- rename the home graph shelf to clearer field language

Detail lane:
- turn the proof surface into labels + values instead of prose-first text
- make corpus-gap states read as honest missing corpus coverage
- harden guide-mode danger / warning surfaces

### Loop 2

Home / naming cleanup:
- apply the chosen home naming consistently across layouts and accessibility text

Detail / tablet cleanup:
- suppress redundant tablet-portrait scope chips when the title rail already carries the state
- rename the landscape rail to match the new field-manual vocabulary
- validate both tablet postures after the rename/dedup pass

## Naming Decisions

Locked for this pass:
- home shelf:
  - from `Guide connections`
  - to `Linked paths`
- wide-tablet rail:
  - from `Cross-reference rail`
  - to `Field links`
- search action:
  - from `Search`
  - to `Find`

Why:
- shorter
- clearer under stress
- still consistent with the field-manual voice
- avoids overly technical or overly theatrical wording

## Ownership Split

### Worker Lane A

Scope:
- home screen austerity and naming

Target files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/res/layout/activity_main.xml`
- `android-app/app/src/main/res/layout-land/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`

### Worker Lane B

Scope:
- detail trust surface, corpus-gap styling, warning hardening, tablet chip dedup

Target files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout/activity_detail.xml`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
- `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
- detail-related drawables if needed

### Main Lane

Scope:
- shared strings
- shared colors / drawables only if both workers need them
- integration
- validation
- progress logging
- reviewer pack refresh at the end

## Progress Log

### 2026-04-17 - Setup

Status:
- `done`

Notes:
- source note reviewed and reconciled against the live app
- accepted recommendations split into 2 implementation loops
- worker ownership is now defined to avoid file overlap

### 2026-04-17 - Loop 1

Status:
- `done`

Landed:
- home readiness is now a single terse story instead of a status pill plus a second summary card
- search action is now `Find`
- home graph shelf is now `Linked paths`
- proof / provenance copy is more label-value oriented
- guide/detail corpus-limit messaging is more explicit about pack coverage
- guide-mode admonition fences like `:::danger` and `:::warning` are stripped and surfaced with stronger manual-warning headings

Validation:
- build:
  - `android-app :app:assembleDebug`
  - `android-app :app:assembleDebugAndroidTest`
- phone portrait home:
  - `artifacts/instrumented_ui_smoke/20260417_112710_880/emulator-5556/summary.json`
- phone landscape home:
  - `artifacts/instrumented_ui_smoke/20260417_112746_882/emulator-5560/summary.json`
- phone portrait generative detail:
  - `artifacts/instrumented_ui_smoke/20260417_112814_105/emulator-5556/summary.json`
- phone landscape generative detail:
  - `artifacts/instrumented_ui_smoke/20260417_112840_491/emulator-5560/summary.json`
- tablet landscape guide-detail proof:
  - `artifacts/instrumented_ui_smoke/20260417_112746_916/emulator-5558/summary.json`

Planned validation:
- phone portrait home
- phone landscape home
- phone portrait generative detail
- phone landscape generative detail

### 2026-04-17 - Loop 2

Status:
- `done`

Planned scope:
- rename the tablet landscape rail from `Cross-reference rail` to `Field links`
- suppress redundant `Manual entry` / `Single guide open` chips on tablet portrait when the title rail already carries the state
- finish the manual-warning cleanup by stripping the still-visible inline markdown/html residue that survives after fence cleanup
- make the corpus-gap state visually clearer than a plain numbered `No steps available.`

Landed:
- wide-tablet guide rail now reads `Field links`
- tablet portrait guide detail now suppresses redundant top-state chips when the title rail is already visible
- compact phone corpus-gap state now reads as an explicit corpus-limit / pack-gap notice
- guide/manual warning cleanup now strips fence markers, duplicated warning labels, backticks, markdown-link residue, and heading markup in the validated phone/tablet cases

Validation:
- tablet portrait chip dedup:
  - `artifacts/instrumented_ui_smoke/20260417_113847_600/emulator-5554/summary.json`
- tablet landscape field-links guide handoff:
  - `artifacts/instrumented_ui_smoke/20260417_113847_652/emulator-5558/summary.json`
- compact phone corpus-gap proof:
  - `artifacts/instrumented_ui_smoke/20260417_114155_187/emulator-5556/summary.json`
- synthetic warning-cleanup proof:
  - `artifacts/instrumented_ui_smoke/20260417_114726_052/emulator-5556/summary.json`
- real installed-guide warning cleanup on tablet landscape:
  - `artifacts/instrumented_ui_smoke/20260417_114726_072/emulator-5558/summary.json`
- landscape-phone regression spot check:
  - `artifacts/instrumented_ui_smoke/20260417_114211_720/emulator-5560/summary.json`

### 2026-04-17 - Final Cleanup

Status:
- `done`

Landed:
- real installed guide pages no longer leak the specific markdown-link / heading residue that was still visible after Loop 2
- synthetic warning proof now rejects duplicated warning labels, not just fence syntax

Validation:
- phone synthetic warning cleanup:
  - `artifacts/instrumented_ui_smoke/20260417_114726_052/emulator-5556/summary.json`
- tablet landscape installed-guide cleanup:
  - `artifacts/instrumented_ui_smoke/20260417_114726_072/emulator-5558/summary.json`

## Final Packaging

Deliverable at completion:
- fresh unzipped reviewer folder under `artifacts/external_review/`
- current screenshots
- matching XML dumps
- validation summaries
- current key Android UI code
- current docs tied to this execution pass
