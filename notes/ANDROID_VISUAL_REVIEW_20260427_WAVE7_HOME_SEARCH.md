# Android Visual Review - 2026-04-27 Wave 7 - Home/Search

Reviewer: W7-Review  
Scope: visual audit only for home and search targets across phone/tablet portrait/landscape.  
Compared target mocks in `artifacts/mocks` against fresh state pack `artifacts/ui_state_pack/20260427_225149`.  
No production code changes.

## Pack Status

- `artifacts/ui_state_pack/20260427_225149/summary.json`: fresh pack reviewed for home/search surfaces.
- Home entry screenshots exist for all four roles.
- Search results screenshots exist for all four roles.
- Phone-landscape search is no longer blank: `artifacts/ui_state_pack/20260427_225149/screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` shows a visible search surface, `Results for "fire" (4)`, and result cards.
- Search query/count alignment is materially better than Wave 5: all reviewed search dumps expose query `fire` and count `4`, rather than the prior 75-result mismatch. The query still differs from the mocks, which show `rain shelter`.

## Per-Target Verdicts

| Target | Verdict | Target mock | Fresh screenshot | Notes |
| --- | --- | --- | --- | --- |
| Home - phone portrait | Partial | `artifacts/mocks/home-phone-portrait.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/phone_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Core sections are present: ready/offline status, search input, category grid, and recent threads. Remaining deltas are visual density and content: larger type/cards, different category counts, and harness recent-thread copy. |
| Search - phone portrait | Partial | `artifacts/mocks/search-phone-portrait.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Results are visible and count is now `4`, but query/content are `fire` rather than mock `rain shelter`; rows remain large card blocks with guide-connection chips and bottom nav occluding the last card. |
| Home - phone landscape | Partial | `artifacts/mocks/home-phone-landscape.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/phone_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Two-pane home composition is present and readable. Density remains heavier than mock: oversized breadcrumb, status/search modules, category cards, and recent-thread cards; lower category row is clipped by the landscape viewport. |
| Search - phone landscape | Partial | `artifacts/mocks/search-phone-landscape.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Former blank state is fixed. The screen now shows `Results for "fire" (4)` and visible results. Still not a pass because the mock expects compact `rain shelter` search chrome, three compact rows, inline score marks, and much denser spacing. |
| Home - tablet portrait | Partial | `artifacts/mocks/home-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/tablet_portrait/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Tablet rail/two-column home structure is recognizable. Remaining deltas: larger type, changed status/header copy, category counts differ, and recent threads use harness content. |
| Search - tablet portrait | Partial | `artifacts/mocks/search-tablet-portrait.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Query/count now align internally as `fire - 4 results` and `4 RESULTS`. Structure has filter rail plus results list, but density is still larger than mock, checkboxes render as text-like `[]`, and no preview rail is visible in portrait. |
| Home - tablet landscape | Partial | `artifacts/mocks/home-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/tablet_landscape/homeEntryShowsPrimaryBrowseAndAskLanes__home_entry.png` | Three-area home layout is present with left nav, central library content, and recent threads. Visual scale remains larger than mock, category cards are bulkier, and recent-thread text/content differs. |
| Search - tablet landscape | Partial | `artifacts/mocks/search-tablet-landscape.png` | `artifacts/ui_state_pack/20260427_225149/screenshots/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.png` | Best search-tablet match in this pass: filter rail, result list, and preview pane are all visible with `fire - 4 results`. Remaining deltas are density, `fire` vs `rain shelter`, text-like filters, hyphen score markers, and raw markdown-style preview body. |

## Query And Count Check

- Phone portrait dump: `artifacts/ui_state_pack/20260427_225149/dumps/phone_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.xml` shows `Results for "fire" (4)`.
- Phone landscape dump: `artifacts/ui_state_pack/20260427_225149/dumps/phone_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.xml` shows `Results for "fire" (4)`.
- Tablet portrait dump: `artifacts/ui_state_pack/20260427_225149/dumps/tablet_portrait/searchQueryShowsResultsWithoutShellPolling__search_results.xml` shows `SEARCH  fire - 4 results`, `fire`, and `4 RESULTS`.
- Tablet landscape dump: `artifacts/ui_state_pack/20260427_225149/dumps/tablet_landscape/searchQueryShowsResultsWithoutShellPolling__search_results.xml` shows `SEARCH  fire - 4 results`, `fire`, and `4 RESULTS`.

## Remaining Density Deltas

- Typography is still substantially larger/heavier than the mocks across home and search, especially phone-landscape and phone-portrait search result cards.
- Search results remain card/block oriented on phone, while mocks are compact list rows with tighter metadata, snippets, and score treatment.
- Tablet search has the intended multi-pane structure, but filter controls are text-like `[]`, result rows have larger vertical rhythm, and preview text is raw/markdown-like rather than compact mock copy.
- Home category cards still use larger dimensions and different counts than mocks: fresh screenshots show Shelter 82, Water 65, Fire 70, Food 105, Medicine 138, Tools 149.
- Recent-thread areas are structurally present but use harness content and larger row/card treatment than target mocks.

## Overall

Verdict: Partial for all eight targets. The Wave 7 pack fixes the main phone-landscape search blocker and improves search query/count coherence to `fire`/`4`. Remaining work is visual-density parity and mock-content alignment rather than blank or missing home/search surfaces.
