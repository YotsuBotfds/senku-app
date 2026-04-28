# Android Search Tablet Shell Plan - 2026-04-27

Worker K read-only handoff. Latest pushed commit from the task brief:
`de0c071`. Target mocks:
`artifacts/mocks/search-tablet-portrait.png` and
`artifacts/mocks/search-tablet-landscape.png`.

## Scope Guard

- No production edits were made.
- Main orchestrator owns `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`.
- This note maps the exact production work needed for the tablet search shell:
  filter rail, preview rail, IDs/layout files, and smoke coverage.

## Current State

- Tablet home layouts exist at:
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - fallback `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
- Search currently reuses the same `results_header` and `results_list` in the
  main activity layouts.
- `MainActivity.runSearch(...)` calls `showBrowseChrome(false)`, clears and
  repopulates `items`, then writes
  `presentationFormatter().buildResultsHeader(...)` into `results_header`.
- `showBrowseChrome(...)` hides home chrome and reveals `results_header` /
  `results_list`; there is only a narrow
  `updateLandscapeTabletResultsPriority(...)` hook for landscape tablet.
- `shouldUseCompactResultsHeader()` is phone/font-scale only. Tablet search
  therefore still receives verbose copy such as:
  `Hybrid results for "rain shelter" (showing top 10: ... route, ... hybrid, ...)`.
- The target mocks use a short header/breadcrumb:
  `SEARCH rain shelter - 4 results`, plus a compact right-side summary:
  `4 RESULTS - 12MS`.
- There are no dedicated tablet search shell IDs today for:
  - search top bar / breadcrumb
  - tablet filter rail
  - tablet result-count/timing line
  - tablet preview rail
  - selected search result preview state

## Target Behavior

Tablet portrait:

- Keep the left navigation rail.
- Replace home/browse body with a search shell after a search.
- Show a top search header with back affordance, `SEARCH`, query text, and
  result count.
- Show a search field row below it.
- Show a left filter rail inside the content area:
  - category section
  - window section
  - checkbox-like affordances
  - counts derived from current result metadata where possible
- Show the results list to the right of the filter rail.
- Do not show a preview rail in portrait; use the width for results.

Tablet landscape:

- Same top header/search row/filter rail.
- Results list becomes the middle column.
- Add a right preview rail for the selected/top result:
  - label: `PREVIEW - <guide id>`
  - title
  - short metadata line
  - snippet/body preview
  - `TAP TO OPEN`
- Initial preview should bind to the first result after search completion.
- Tapping or selecting a result row should update the preview rail first and
  still allow opening detail through the existing result action.

## Production Changes Needed

1. Add stable IDs.

   Candidate IDs in `android-app/app/src/main/res/values/ids.xml` or directly
   in layout XML:

   - `tablet_search_shell`
   - `tablet_search_top_bar`
   - `tablet_search_back_button`
   - `tablet_search_breadcrumb`
   - `tablet_search_query_text`
   - `tablet_search_result_summary`
   - `tablet_search_filter_rail`
   - `tablet_search_filter_category_section`
   - `tablet_search_filter_window_section`
   - `tablet_search_results_column`
   - `tablet_search_preview_rail`
   - `tablet_search_preview_label`
   - `tablet_search_preview_title`
   - `tablet_search_preview_meta`
   - `tablet_search_preview_body`
   - `tablet_search_preview_open_hint`

2. Restructure tablet search layouts.

   Edit only the tablet `activity_main.xml` variants for production:

   - `layout-sw600dp-port/activity_main.xml`
   - `layout-sw600dp-land/activity_main.xml`

   Recommended structure:

   - Keep existing hidden utility view containers so `MainActivity.findViewById`
     continues to resolve all required IDs.
   - Wrap current home content in a home/browse container if needed.
   - Add a `tablet_search_shell` sibling that is initially `gone`.
   - Inside shell:
     - top bar row
     - search query row using the existing `search_input` where practical, or
       a clearly mirrored field if `MainActivity` wiring is adjusted
     - content row:
       - filter rail
       - `results_header` / `results_list` column
       - landscape-only preview rail

   Important: keep a single `@id/results_list` and `@id/results_header` in each
   inflated layout. Duplicate IDs in mutually hidden branches will make
   `findViewById` fragile.

3. Add tablet shell state in `MainActivity.java`.

   Main orchestrator should add fields for the new tablet shell views and a
   selected preview result:

   - `View tabletSearchShell`
   - `View tabletSearchFilterRail`
   - `View tabletSearchPreviewRail`
   - `TextView tabletSearchBreadcrumb`
   - `TextView tabletSearchResultSummary`
   - `TextView tabletSearchPreviewLabel`
   - `TextView tabletSearchPreviewTitle`
   - `TextView tabletSearchPreviewMeta`
   - `TextView tabletSearchPreviewBody`
   - `TextView tabletSearchPreviewOpenHint`
   - `SearchResult selectedTabletSearchPreview`

   Add a helper such as:

   - `private boolean isTabletSearchShellLayout()`
   - `private void showTabletSearchShell(boolean show)`
   - `private void renderTabletSearchShell(String query, List<SearchResult> results, long elapsedMs)`
   - `private void renderTabletSearchFilters(List<SearchResult> results)`
   - `private void renderTabletSearchPreview(SearchResult result)`

4. Header copy.

   Add formatter coverage before changing behavior. Suggested API:

   - `buildTabletSearchHeader(String query, int resultCount)`
   - `buildTabletSearchSummary(int resultCount, long elapsedMs)`

   Expected stable outputs:

   - query `rain shelter`, count `4` -> `SEARCH rain shelter - 4 results`
   - empty query, count `10` -> `SEARCH guides - 10 results`
   - count `1` -> singular `1 result`
   - elapsed `12` -> `4 RESULTS - 12MS`

   Then update `MainActivity.runSearch(...)` to use short tablet copy when
   `isTabletSearchShellLayout()` is true, while preserving current phone
   compact header behavior and no-result copy.

5. Filter rail data.

   Start with display-only filters unless the orchestrator explicitly chooses
   interactive filtering in the same slice.

   Display-only counts can derive from current `results`:

   - category: `SearchResult.category`, humanized through
     `MainPresentationFormatter.humanizeMetadataLabel(...)` or existing
     category labels
   - window: `SearchResult.timeHorizon`

   If interactive filters are included, keep filtering local to current
   `items` and clearly separate it from repository search. Do not silently
   re-query unless the UX contract says filters are retrieval filters.

6. Preview rail data.

   Use existing result fields:

   - guide id: `result.guideId`
   - title: `result.title`
   - meta: category/content role/time horizon/section heading
   - body: prefer `result.snippet`, fall back to `result.body`

   Initial preview:

   - first result after non-empty search result set
   - clear/hide preview on empty results or failed search

   Row selection:

   - If adapter changes are allowed, add an optional selection callback in
     `SearchResultAdapter` and call it from row click before open-detail.
   - If adapter changes are not desired in the first slice, preview can remain
     first-result-only and the row click continues opening detail.

7. Visibility hooks.

   Update these production points carefully:

   - `runSearch(...)`: record search start time, call tablet shell render after
     results arrive.
   - `showBrowseChrome(...)`: hide tablet search shell in browse mode; show it
     in tablet result mode.
   - `updateLandscapeTabletResultsPriority(...)`: include preview rail
     visibility and avoid hiding results merely because the old browse rail is
     toggled.
   - `updatePortraitPhoneResultsPriority(...)` and landscape phone behavior:
     should remain unchanged.
   - deterministic search path: decide explicitly whether deterministic source
     picks use the tablet search shell or stay on the existing results list.

8. Drawables/dimens.

   Prefer existing rev03 colors/drawables first:

   - `@color/senku_rev03_bg_0`
   - `@color/senku_rev03_bg_1`
   - `@color/senku_rev03_ink_0`
   - `@color/senku_rev03_ink_1`
   - `@color/senku_rev03_ink_2`
   - `@color/senku_rev03_accent`
   - existing hairline colors

   Add narrow tablet search drawables only if layout XML cannot express the
   mock with existing assets. Keep them named `bg_tablet_search_*`.

## Test Suggestions

Can add before production edits:

- None recommended in this worker slice. A stable header assertion would need
  a new formatter API or changed current behavior, and production edits are
  out of scope.

Add with the production slice:

- `MainPresentationFormatterTest`
  - `tabletSearchHeaderUsesShortBreadcrumbCopy`
  - `tabletSearchSummaryUsesCompactCountAndTiming`
  - plural/singular result count cases
  - empty query falls back to `guides`
- A narrow JVM test if helpers are package-visible/static:
  - filter bucket counting from sample `SearchResult` rows
  - preview model uses snippet before body and clears on empty results
- Instrumented smoke:
  - tablet portrait search shows `tablet_search_filter_rail`,
    `results_list`, and hides `tablet_search_preview_rail`
  - tablet landscape search shows `tablet_search_filter_rail`,
    `results_list`, and `tablet_search_preview_rail`
  - phone portrait and phone landscape do not show tablet search shell IDs
  - search header does not contain verbose `Hybrid results for`

## Smoke / Acceptance Lane

Use the fixed four-emulator posture matrix after production changes:

- `5556` phone portrait
- `5560` phone landscape
- `5554` tablet portrait
- `5558` tablet landscape

Recommended validation sequence:

1. Focused JVM:

   ```powershell
   cd android-app
   .\gradlew.bat testDebugUnitTest --tests "com.senku.mobile.MainPresentationFormatterTest" --console=plain
   ```

2. Compile guard:

   ```powershell
   cd android-app
   .\gradlew.bat assembleDebug testDebugUnitTest --console=plain
   ```

3. Tablet search UI smoke with screenshots/dumps:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Serial emulator-5554 -SearchQuery "rain shelter"
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_instrumented_ui_smoke.ps1 -Serial emulator-5558 -SearchQuery "rain shelter"
   ```

4. Fixed-four acceptance only after the focused tablet proof is clean:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build_android_ui_state_pack_parallel.ps1 -RealRun
   ```

Compare captured tablet screenshots against:

- `artifacts/mocks/search-tablet-portrait.png`
- `artifacts/mocks/search-tablet-landscape.png`

## Risks / Decisions For Orchestrator

- Decide whether filters are display-only in v1 or interactive. Display-only is
  lower risk and matches this shell slice cleanly.
- Decide whether row tap should update preview only, open detail immediately,
  or do both through separate tap targets. Current adapter row click opens
  detail.
- Avoid duplicate `search_input`, `results_header`, or `results_list` IDs in
  alternate visible branches.
- Keep reviewed-card runtime exposure unchanged; this is search shell/UI work,
  not reviewed-card product enablement.
- Header timing (`12MS`) needs an actual elapsed value if the mock copy is
  implemented literally. If timing is not ready, omit timing or use only the
  count rather than faking latency.

## Worker K Result

- Production files inspected read-only:
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - `android-app/app/src/main/java/com/senku/mobile/MainPresentationFormatter.java`
  - `android-app/app/src/main/res/layout-sw600dp-port/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp-land/activity_main.xml`
  - `android-app/app/src/main/res/layout-sw600dp/activity_main.xml`
  - `android-app/app/src/main/res/values/ids.xml`
  - `android-app/app/src/test/java/com/senku/mobile/MainPresentationFormatterTest.java`
- Test edits made: none. Current formatter has no tablet-specific header API,
  and adding a failing expectation without production changes would not be a
  stable narrow assertion.
