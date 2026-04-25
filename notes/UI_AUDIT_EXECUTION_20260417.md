# UI Audit Execution - 2026-04-17

Source audit:
- `C:\Users\tateb\Downloads\senku_design_audit.md`

## Reconciled Still-Real Slices

After comparing the audit against the current landed Android UI, these are the still-real gaps in smallest-first order:

1. Search-result scan polish
   - clean lingering markdown artifacts in result cards
   - add lightweight query-term emphasis for faster scanning
2. Detail answer chunking / step hierarchy
   - make answer-mode body copy read more like a field manual without rewriting the layout
3. Home-entry lane emphasis
   - make Browse vs Ask feel more intentionally different on the home screen
4. Surface materiality pass
   - deepen the olive/parchment physical-tool feel without blowing up the current palette

## First Wave Landed

### Slice 1 - Search-result scan polish

Files:
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

What changed:
- result titles, sections, and snippets now use lightweight query-term emphasis for active search/ask result states
- displayed result text now strips a wider set of markdown artifacts, including inline heading markers that were leaking into snippets
- search/ask now dismiss the IME and hand focus back to the content surface, which keeps phone-landscape result states usable
- the screenshot smoke now explicitly collapses the main search keyboard before capturing `search_results`, so the artifact lane stops preserving keyboard-obscured states

Validation:
- phone portrait host smoke:
  - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/summary.json`
- phone landscape basic smoke:
  - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5560/summary.json`

Key screenshot proofs:
- `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5560/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`

### Slice 2 - Detail answer chunking / step hierarchy

Files:
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`

What changed:
- answer-mode body rendering now uses styled text rather than a flat block
- `Short answer`, `Steps`, and `Limits or safety` are now visually differentiated
- numbered steps now get stronger hierarchy through bold/accented numbering and hanging indent treatment
- streaming/trust/provenance behavior stayed intact

Validation:
- phone portrait host smoke:
  - `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/summary.json`

Key screenshot proof:
- `artifacts/instrumented_ui_smoke/20260417_095051_386/emulator-5556/screenshots/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`

## Next Pair

1. Home-entry lane emphasis
2. Surface materiality pass

These were the next honest audit slices after the first wave, and they are now landed.

## Second Wave Landed

### Slice 3 - Home-entry lane emphasis

Files:
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
- `android-app/app/src/main/res/values/strings.xml`
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`

What changed:
- Browse is now the clearly-primary cold-home lane
- Ask becomes the clearly-primary lane only when a typed query is active and the app is genuinely in ask flow
- helper copy under the home controls now tells the truth about the current lane instead of using one generic compare blurb
- the ask-return path no longer reopens the keyboard when returning to a result-backed home surface

Validation:
- focused home-entry / ask-return smoke:
  - `artifacts/instrumented_ui_smoke/20260417_100430_644/emulator-5556/summary.json`

Key screenshot proofs:
- `artifacts/instrumented_ui_smoke/20260417_100430_644/emulator-5556/screenshots/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png`
- `artifacts/instrumented_ui_smoke/20260417_100430_644/emulator-5556/screenshots/returningFromAnswerKeepsAskLaneEmphasis__ask_lane_return.png`

### Slice 4 - Surface materiality pass

Files:
- `android-app/app/src/main/res/values/colors.xml`
- `android-app/app/src/main/res/drawable/bg_main_screen.xml`
- `android-app/app/src/main/res/drawable/bg_hero_panel.xml`
- `android-app/app/src/main/res/drawable/bg_surface_panel.xml`
- `android-app/app/src/main/res/drawable/bg_search_shell.xml`
- `android-app/app/src/main/res/drawable/bg_result_card.xml`

What changed:
- shared shells now use deeper olive layering, warmer parchment transitions, and stronger inset/highlight treatment
- the refresh stayed XML-only and low-risk, so it propagates across home, search, and detail without a layout rewrite

Validation:
- phone landscape basic smoke:
  - `artifacts/instrumented_ui_smoke/20260417_100202_388/emulator-5560/summary.json`
- phone portrait host smoke:
  - `artifacts/instrumented_ui_smoke/20260417_100236_402/emulator-5556/summary.json`

Key screenshot proofs:
- `artifacts/instrumented_ui_smoke/20260417_100202_388/emulator-5560/screenshots/searchQueryShowsResultsWithoutShellPolling__search_results.png`
- `artifacts/instrumented_ui_smoke/20260417_100236_402/emulator-5556/screenshots/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`

## Remaining Audit Surface

The original audit also mentioned typography and iconography, but after reconciliation those read as lower-priority follow-on polish rather than still-real blockers. The practical audit-driven work that remained highest leverage is now landed and validated.