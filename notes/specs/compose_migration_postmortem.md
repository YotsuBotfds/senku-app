# Compose Migration Postmortem

Date: 2026-04-18
Task: `OPUS-B-12`
Scope: Android UI migration only

This note is a short postmortem for the current hybrid Compose migration in this repo. It reflects the checked-in tree as the source of truth. Where planning docs and code disagree, prefer the code.

## Executive Summary

The migration worked for its main goal: ship Rev 03 visuals without forcing a full Android rewrite. Compose now owns a meaningful share of the visible UI, especially the answer surfaces, result cards, home chrome, and tablet detail screen. That let the team land new visual primitives in isolated Kotlin files while keeping the Java Activities, retrieval logic, and prompt flow stable.

The tradeoff is that this is still a hybrid app, not a Compose-first app. `MainActivity.java` is still 3397 lines and `DetailActivity.java` is still 7888 lines. The UI is cleaner, but the state ownership is not. We traded XML/widget complexity for boundary complexity: host views, hidden legacy mirrors, adapter bindings, Java-to-Compose model mapping, and dual test modes.

## What Landed

Compose is enabled in `android-app/app/build.gradle`, and the migration is no longer just a spike.

The current tree shows these Rev 03 surfaces in Compose:

- Theme foundation: `com/senku/ui/theme/`
- Detail answer body: `PaperAnswerCardHostView` in `DetailActivity`
- Detail follow-up bar: `DockedComposerHostView` in `DetailActivity`
- Detail chrome: `SenkuTopBarHostView` and `SenkuMetaStripHostView`
- Search results: `SearchResultCard` mounted from `SearchResultAdapter`
- Home chrome: `IdentityStripHostView`, `CategoryShelfHostView`, `BottomTabBarHostView`
- Source rows and follow-up chips: `SourceRow`, `SuggestChipRailHostView`
- Tablet detail: full `TabletDetailScreen` root in `layout-large*` and `layout-sw600dp*`
- Supporting tablet/evidence primitives: `EvidenceSnippet`, `XRefRow`, `ThreadRow`, `PivotRow`

This is the important success case for the migration: most new surface work landed in new Kotlin files under `android-app/app/src/main/java/com/senku/ui/`, which reduced merge collisions and made F-lane work genuinely parallel-friendly.

## What Stayed Legacy

The shell stayed legacy on purpose.

- `MainActivity` and `DetailActivity` still own lifecycle, navigation, retrieval wiring, session memory, streaming state, and most UI orchestration.
- Phone screens are still XML-first layouts with Compose mounted into selected regions instead of replacing the whole screen.
- `RecyclerView` still owns the search list; Compose only renders each row body.
- A number of legacy view structures still exist in the layouts even when Compose is the user-visible surface.

Examples of the partial cutover:

- `detail_body_mirror_shell` remains in `activity_detail.xml`
- `detail_followup_legacy_mirror` remains in `activity_detail.xml`
- `result_legacy_mirror` remains in `list_item_result.xml`
- `legacyHomeHeroPanel` is still resolved and then hidden after Compose chrome is inserted

This was a reasonable compromise for landing Rev 03 quickly, but it means the app still pays both the View-system cost and the Compose cost.

## Performance And Cost

The migration looks good on implementation cost and mixed on structural cost.

What got cheaper:

- New visual work moved into bounded Kotlin files instead of expanding already-large Activities.
- Parallel work improved because most primitives lived in new files instead of shared Java/XML hotspots.
- Rev 03 styling is easier to express in Compose than in nested `LinearLayout` plus drawable plus span combinations.

What still costs us:

- We now maintain both XML/View code and Compose code.
- Mount sites are still hotspots: `activity_detail.xml`, `activity_main.xml`, and the Activity bind methods.
- The result list is a hybrid hot path: `RecyclerView` plus per-row `ComposeView`, not a single Compose list.
- The biggest files did not shrink yet, so the migration did not deliver the maintainability win by itself.

Runtime-wise, the current choice is probably the right middle ground for this phase. Most Compose use is scoped to leaf surfaces or one tablet root, so the team avoided the blast radius of a full navigation/state rewrite. The place to watch first if performance regresses later is the result list, because that is the highest-volume repeated Compose mount.

## Deferred-Binding Gotchas

Most of the pain moved to the boundary between Java Views and Compose.

### 1. Runtime tree surgery instead of static ownership

`ensureRev03ComposeMounts()` inserts Compose hosts at runtime and hides old view groups. That kept the patch size small, but it also made mount order and parent assumptions part of the behavior.

### 2. Hidden mirror views as safety rails

The answer body and follow-up bar both keep legacy mirror structures alive at near-zero size and alpha instead of deleting them outright. That reduced breakage risk, but it is also a signal that Compose is not the single source of truth yet.

### 3. Dual state for follow-up input

The follow-up composer still syncs through the legacy `EditText`, guarded by `syncingFollowUpInputFromCompose`. That is exactly the kind of bridge code hybrid migrations accumulate: safe enough, but easy to loop, desync, or break selection behavior.

### 4. Java-owned tablet state

Tablet detail uses a full Compose root, but `DetailActivity` still rebuilds `TabletDetailState` and rebinds it from Java on each interaction. This is simple and explicit, but it keeps the Activity as the real presenter and makes tablet Compose more of a renderer than a true screen owner.

### 5. Two mounting styles

The codebase now uses both `AbstractComposeView` host views and raw `ComposeView.setContent(...)` bindings. That is fine, but it raises the learning and review cost because there is more than one integration pattern to reason about.

## Hybrid Activity-Shell Tradeoffs

The hybrid shell was the right call for this repo phase.

Why it worked:

- It contained blast radius while retrieval, offline pack, and harness work were moving quickly.
- It let the team modernize the highest-value surfaces first.
- It matched the repo's parallel worker model well.
- It preserved existing intent wiring, session behavior, and Android-specific edge handling.

What it did not solve:

- It did not simplify app architecture.
- It did not remove the Activity monolith risk.
- It did not remove XML maintenance.
- It did not make Compose testing the default mental model.

In other words: the migration succeeded as a surface strategy, not as an architecture rewrite.

## Harness And Testing Implications

The testing posture shifted in a healthy direction, but coverage is still uneven.

What changed:

- The repo now prefers instrumentation-backed truth over shell-side polling.
- `android-app/README.md` explicitly calls out "results without shell-polled XML completion logic".
- Wrapper scripts are expected to trust instrumentation `summary.json` when it exists.
- This is a good fit for a hybrid Compose app, because shell-only XML checks get less trustworthy as more UI lives in Compose.

What coverage exists today:

- End-to-end instrumentation smoke: `PromptHarnessSmokeTest.java`
- One direct Compose UI test: `SourceRowTest.kt`
- One small Compose-adjacent unit test: `SearchResultCardHeuristicsTest.kt`

What that means in practice:

- Most Compose assurance is still harness-level and screenshot-level, not primitive-level.
- The four-emulator posture matrix matters more now because tablet and phone surfaces diverge more sharply.
- Regressions in visual primitives will often show up first in instrumentation artifacts, not in tight unit tests.

That is acceptable for the current phase, but if the team keeps moving toward Compose, primitive-level test coverage needs to grow faster than it has so far.

## Path To Fuller Compose Later

If the team chooses to keep going, the safest path is not "rewrite the app in Compose" in one jump. The better path is to remove boundary complexity in stages.

Recommended order:

1. Finish the structural cleanup first.
   - Extract presenter/state-holder logic out of `MainActivity` and `DetailActivity`.
   - Make screen state easier to pass around without direct view mutation.

2. Make Compose the single owner of already-migrated regions.
   - Remove answer/follow-up mirror shells.
   - Stop using legacy view state as the backing store for Compose inputs where possible.

3. Promote phone screens from mounted regions to screen roots.
   - Detail phone screen is the best next candidate.
   - Home/search is second.

4. Convert repeated lists only after state boundaries are stable.
   - Search results are the main candidate for a later `LazyColumn` migration.

5. Expand test shape before deleting the old rails.
   - Add more primitive-level Compose tests.
   - Keep the instrumentation matrix as the final truth lane.

6. Revisit navigation last.
   - Activity navigation is not pretty, but it is stable.
   - Replacing it before the state model is cleaned up would increase risk without much user benefit.

## Bottom Line

`OPUS-B-12` was worth doing. The hybrid strategy delivered visible product value quickly and with manageable blast radius. The cost is that the repo now has a more modern rendering layer sitting on top of old state ownership. If the team stops here, that is still a valid outcome. If the team wants fuller Compose later, the next win is not more primitives; it is removing the bridge code that still makes the Activities the real source of truth.