# Android Visual Review - 2026-04-27 Wave 5 - Home/Search

Reviewer: R1  
Scope: visual audit only for home and search surfaces across phone/tablet portrait/landscape.  
Compared target mocks in `artifacts/mocks` against state pack `artifacts/ui_state_pack/20260427_223445`.  
No production code changes.

## Pack Status

- `artifacts/ui_state_pack/20260427_223445/summary.json`: overall status `partial`, 45 total states, 38 pass, 7 fail, 0 platform ANRs.
- Device matrix is homogeneous for APK/model identity. No rotation mismatches reported.
- Home entry and basic search-results screenshots exist for all four roles.
- Search linked-guide handoff is incomplete on both tablet roles: `searchResultsLinkedGuideHandoffOpensLinkedGuideDetail` failed before trusted summary for `tablet_landscape` and `tablet_portrait`.
- Phone landscape `searchQueryShowsResultsWithoutShellPolling` is marked `pass` in `manifest.json`, but the reviewed screenshot is visually blank except for system/nav chrome and the left rail.

## Per-Target Verdicts

| Target | Verdict | Target mock | Reviewed state screenshot | Notes |
| --- | --- | --- | --- | --- |
| Home - phone portrait | Partial | `artifacts/mocks/home-phone-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Core sections are present, but typography is much larger/heavier, header/title truncates, pack/search/card spacing differs, category counts/content differ, and recent-thread rows use test data. |
| Search - phone portrait | Partial | `artifacts/mocks/search-phone-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Results render, but target compact search header/list is replaced by large card-like result blocks, query is `fire` not `rain shelter`, count is 75 not 4, ranking bars/metadata placement differs, and bottom content is clipped behind nav. |
| Home - phone landscape | Partial | `artifacts/mocks/home-phone-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Two-pane/rail structure is present, but header copy and hierarchy differ, left content is vertically oversized, bottom category row is cut off by viewport, recent-thread panel has different content and larger cards. |
| Search - phone landscape | Fail | `artifacts/mocks/search-phone-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Screenshot is effectively blank aside from status bar, left nav rail, and gesture bar. No search header, query, result list, result count, or score bars are visible despite harness status `pass`. |
| Home - tablet portrait | Partial | `artifacts/mocks/home-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Overall tablet rail/two-column home shape is close, but typography is larger, mock top chrome/search affordance differs, recent threads are test/harness content, category counts differ, and the status/pack lane copy is different. |
| Search - tablet portrait | Partial | `artifacts/mocks/search-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Filter rail plus results list exists, but query/count/content are `fire`/75 rather than `rain shelter`/4, font scale is much larger, checkboxes render as text-like `[]`, score marks differ, and there is no compact mock-style result density. |
| Home - tablet landscape | Partial | `artifacts/mocks/home-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Three-area composition is recognizable, but actual screen uses different breadcrumb/header copy, much larger type and cards, category cards occupy more width/height, and recent-thread cards/content diverge from mock. |
| Search - tablet landscape | Partial | `artifacts/mocks/search-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_223445/screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Three-pane structure is present, but query/count/content are `fire`/75 instead of target `rain shelter`/4, result density and sizing are larger, score marks use text-like hyphen scores, and preview pane exposes markdown/raw heading text rather than the mock's compact preview copy. |

## Additional Home/Search Artifacts Reviewed

- `artifacts/ui_state_pack/20260427_223445/screenshots/phone_portrait/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`
- `artifacts/ui_state_pack/20260427_223445/screenshots/phone_landscape/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail__browse_linked_handoff.png`

Tablet equivalents for the linked-guide handoff were not available because the harness failed before trusted summary.

## Top Remaining UI Deltas

- Search content is not aligned to mocks: state pack uses `fire` with 75 results while mocks use `rain shelter` with 4 results. This makes visual parity noisy even where layout structure is close.
- Phone landscape search is a blocker: the target surface is missing visually while the harness reports pass.
- Typography scale and density remain substantially larger than mocks across all fresh screenshots, especially search result rows/cards and home headers.
- Header/breadcrumb language diverges from mocks across roles: e.g. `HOME SENKU - Field manual -...`, `HOME / SENKU / FIELD MANUAL`, and `SEARCH fire - 75 results` instead of the mock's compact search/home chrome.
- Category guide counts differ from the mocks: screenshots show Shelter 82, Water 65, Fire 70, Food 105, Medicine 138, Tools 149 versus mock counts such as Shelter 84, Water 67, Fire 52, Food 91, Medicine 73, Tools 119.
- Recent-thread content is test/harness data rather than mock content, and the row/card density is larger.
- Tablet search filter controls render as text-like unchecked boxes (`[]`) in the fresh screenshots rather than the mock's compact square controls.
- Tablet landscape preview pane contains raw/markdown-style source text and oversized preview copy; the mock preview is shorter, cleaner, and more scannable.

## Harness Failures Relevant To Home/Search

- Overall state pack status is `partial`.
- `tablet_landscape/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`: `fail`, `smoke wrapper failed before trusted summary`, no screenshot.
- `tablet_portrait/searchResultsLinkedGuideHandoffOpensLinkedGuideDetail`: `fail`, `smoke wrapper failed before trusted summary`, no screenshot.
- `phone_landscape/searchQueryShowsResultsWithoutShellPolling`: manifest status `pass`, but screenshot is visually blank for the search content area.
- No platform ANRs were reported.
