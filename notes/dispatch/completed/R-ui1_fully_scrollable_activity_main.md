# Slice R-ui1 — Make `activity_main.xml` fully scrollable on phone-narrow viewports

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline. No subagent
  grant needed — single-file XML restructure.
- **Not a predecessor of any CP9 RC validation work.** Layout fix
  is orthogonal to Wave B correctness. See Sequencing below.

## Context

Tate reports that on a physical phone (narrower viewport than
the 4-emulator matrix presents), the Home tab's fixed chrome
(hero panel with PACK READY, "Source guides ready..." status
banner, search input, GD chips `HorizontalScrollView`, and the
Browse/Ask tab toggle) consumes more than 3/4 of the screen
real estate, leaving the Categories grid inside a tiny
separately-scrollable viewport. He reports the same pattern
applies to every tab (Home / Search / Ask / Threads / Pins),
which is consistent with the shared-layout architecture — the
activity swaps per-tab content via `setVisibility(...)` inside
one `activity_main.xml`, not via separate layouts/fragments.

### Why it happens

`android-app/app/src/main/res/layout/activity_main.xml` has two
sibling regions inside the root `LinearLayout`:

- Lines 9-359: the fixed chrome blocks (hero, status banner,
  search, chip scroller, tab toggle, etc.). These are NOT inside
  any scrollable container.
- Line 361-805: a `ScrollView` (`@+id/browse_scroll_view`,
  `android:layout_height="0dp"` + `android:layout_weight="1"`)
  that holds ONLY the Categories grid and the dev-tools reveal.
- Line 820: a `RecyclerView` for the results list (not relevant
  here — separate surface).

On tall emulator viewports the fixed chrome doesn't dominate,
so the `browse_scroll_view` gets enough remaining vertical space
to feel usable. On phone aspect ratios the chrome eats most of
the screen and the scroll viewport becomes tiny.

### The fix

Move the fixed chrome blocks INSIDE
`@+id/browse_scroll_view`'s child `LinearLayout` so the whole
page scrolls as one unit. Keep the bottom nav (which is OUTSIDE
the scroll today, correctly — standard mobile pattern)
unchanged.

Tate's explicit preference is "fully scrollable" — that is,
nothing pinned at the top. The planner considered a "pin the
hero panel, scroll everything else" variant and rejected it in
favor of the simpler design. Bottom nav stays pinned as the
single fixed element.

## Boundaries (HARD GATE — STOP if you would violate)

- Touch only
  `android-app/app/src/main/res/layout/activity_main.xml`.
- Preserve all `android:id="@+id/..."` attributes exactly as
  they are today — Java code in `MainActivity.java` and
  sibling controllers binds by ID.
- No changes to
  `android-app/app/src/main/java/...` (no controller rewrites;
  the restructure must not break `MainActivity.findViewById(...)`
  calls).
- No changes to `strings.xml`, `dimens.xml`, or drawables
  unless absolutely required to make the restructure compile.
  If a change IS required, keep it minimal and call it out.
- Do NOT move the bottom nav (`BottomTabBarView` or equivalent)
  inside the scroll. Bottom nav stays pinned.
- Do NOT introduce a `CoordinatorLayout` / collapsing toolbar
  pattern — keep the existing `LinearLayout` + `ScrollView`
  shape. Planner considered and rejected the collapsing-toolbar
  variant as more complex than Tate's ask warrants.
- Do NOT touch `activity_detail.xml`, `list_item_result.xml`,
  or any other layout file.
- Do NOT touch anything under `androidTest/` — R-val2 owns that.
- No commits outside this scope. Single commit for the XML
  restructure.

## Outcome

On a phone viewport (roughly 390dp-430dp wide × 700dp-900dp
tall portrait), the Home tab shows the hero panel, status
banner, search input, chips, Browse/Ask tabs, Categories, and
the dev-tools reveal all reachable via a single scroll gesture
starting from anywhere in the content area. No nested scroll
conflict inside the normal vertical swipe. The GD-chips
`HorizontalScrollView` still works as a horizontal carousel
inside the outer vertical scroll. Bottom nav stays visible.
On tall emulator postures (phone landscape / tablet portrait /
tablet landscape) the change does not regress existing layout —
the page just scrolls less because there is more room.

## The work

### Step 1 — Read and map the current structure

Read the whole `activity_main.xml`. Build a mental model of
these blocks:

- Root `LinearLayout` (vertical, `match_parent` / `match_parent`).
- Lines 9-~165: hero panel + header meta row (Senku / guides /
  PACK READY).
- Around line 166-181: first `HorizontalScrollView` (one of
  the chip rows).
- Status banner: "Source guides ready on host GPU..." or
  equivalent.
- Search / test input with Find button.
- Second and third `HorizontalScrollView`s around 272-285 and
  343-358 (additional chip rows).
- `home_related_scroll` at 344 — this is a Home-only horizontal
  carousel. Keep its own horizontal scrolling behavior
  unchanged.
- Browse Manual / Ask Manual toggle pair.
- Line 361-805: `ScrollView @+id/browse_scroll_view` currently
  containing the Categories section and dev-tools reveal.
- Line 820: `RecyclerView` for results list.
- Below that: bottom nav.

Identify every per-tab visibility group by cross-referencing
`MainActivity.java` for `setVisibility(...)` call sites that
target any of these block IDs. If a per-tab group already
lives inside `browse_scroll_view`, it's already in the scroll
and nothing needs to move for it. If a per-tab group is
currently outside `browse_scroll_view`, it needs to move in.

### Step 2 — Restructure

Move every pre-`ScrollView` chrome block into
`@+id/browse_scroll_view`'s child `LinearLayout` as the first
children, preserving the existing visual order:

1. Hero panel (hero/header meta row).
2. Status banner.
3. Search / test input with Find button.
4. GD chip rows (preserve their `HorizontalScrollView`
   behavior; they nest inside the outer vertical scroll fine
   — this is the standard Android pattern).
5. Browse / Ask tab toggle.
6. Existing Categories section (already inside the scroll).
7. Dev-tools reveal (already inside the scroll).

Keep the `RecyclerView` at line 820 wherever it lives today
(likely outside the `ScrollView`, used by Search tab's results
list with `setVisibility(VISIBLE/GONE)` swaps). It should
continue to handle its own scrolling for results view. Do not
nest the `RecyclerView` inside `browse_scroll_view` — that
would re-create a nested-scroll bug on the Search tab. If
mutual visibility between the `ScrollView` and the
`RecyclerView` is currently managed by Java, that behavior must
continue to work unchanged after the restructure.

Adjust the `ScrollView` itself:
- Keep `android:id="@+id/browse_scroll_view"`.
- Keep `layout_width="match_parent"`,
  `layout_height="0dp"`, `layout_weight="1"`.
- Keep `android:fillViewport="false"` (or flip to `true` if
  you need content to fill the viewport for layouts with less
  scrollable content — judgment call; default is `false`).
- Its child `LinearLayout` keeps `orientation="vertical"` and
  `layout_height="wrap_content"`.

### Step 3 — Verify IDs and references

Before committing, verify that every `android:id` reference in
`MainActivity.java` and related controllers still resolves —
`findViewById(R.id.<id>)` must return a non-null view for every
ID present in the new structure.

Run a focused grep: for each ID in the new layout, confirm at
least one `findViewById` or `R.id.<id>` reference exists in
`android-app/app/src/main/java/com/senku/mobile/` (and
`android-app/app/src/main/java/com/senku/ui/` if that path has
controllers).

If an ID was previously present and you need to remove it,
STOP — removing an ID that Java binds will crash at runtime.
Flag as out-of-scope and leave the ID in place.

### Step 4 — Build

From the `android-app/` directory:

- `./gradlew.bat :app:assembleDebug`

The build must succeed. If it fails, read the error carefully
— typical failure modes are a dropped `xmlns` declaration on
a nested node or a Style reference lost in a copy-paste. Fix
in place and re-run.

### Step 5 — Smoke on the emulator matrix

Use the existing smoke infrastructure. The goal is NOT a Wave
B validation — that's S2-rerun2's job. The goal is to confirm
the layout change doesn't crash and scrolls as expected on at
least one phone posture and one tablet posture.

- Install the new debug APK on serial 5556 (phone portrait).
  Use
  `adb -s emulator-5556 install -r android-app/app/build/outputs/apk/debug/app-debug.apk`.
- Launch the app, confirm Home renders and scrolls as one unit
  (hero + status + search + chips + tabs + Categories + dev-
  tools reveal all reachable via one vertical scroll).
- Install on serial 5554 (tablet portrait). Confirm no
  regression — tablet should still look clean, just with less
  content needing to scroll.
- Capture one screenshot per serial for the report.

No instrumentation test run required in this slice —
R-val2's harness still exercises the settle/capture lane as a
side effect of S2-rerun2, which will run separately.

### Step 6 — Commit

Commit only the XML change + any dim/drawable/string files
that genuinely needed touching (hopefully zero).

Commit suggestion:
`R-ui1: make activity_main.xml fully scrollable on phone-narrow viewports`

## Acceptance

- `activity_main.xml` restructured so the fixed chrome lives
  inside `@+id/browse_scroll_view`'s child `LinearLayout`.
- All previously-present `android:id` attributes still present.
- `./gradlew.bat :app:assembleDebug` succeeds.
- Home tab on 5556 scrolls as one unit; Categories section is
  usable without a tiny nested viewport.
- Tablet posture (5554) does not visibly regress.
- Single commit, XML-only (unless dim/drawable/string files
  genuinely had to change — in which case minimal and
  called out).

## Sequencing note for planner

R-ui1 is orthogonal to Wave B validation. It does NOT need to
block S2-rerun2, and S2-rerun2 does NOT need to wait for it.
Current planner intent:

1. Dispatch S2-rerun2 first (RC gate work, already queued).
2. After S2-rerun2 GREEN, dispatch R-ui1 as a simple foreground
   lane.
3. Then an RP-shape slice (RP3) to rebuild + re-provision the
   APK with R-ui1 included.
4. S3 closure + RC cut runs on the RP3 substrate so the RC v5
   cut has both Wave B correctness AND the layout fix.

If Tate prefers to land R-ui1 before S2-rerun2 to avoid any
ambiguity about what APK S2-rerun2 tested against, that works
too — just adds one more RP cycle before S2-rerun2. Planner
default is "S2-rerun2 first."

## Report format

Reply with:
- Commit sha.
- One-line summary of the restructure (what moved, what
  stayed).
- Count of IDs confirmed still present.
- Gradle assemble output line that shows success (one line).
- Paths to the two smoke screenshots (5556 + 5554).
- Any out-of-scope finding (e.g., a Java controller that
  assumed a view was outside the scroll; a style that no
  longer matches; a dim resource that needed touching).
- Delegation log (expected: "none; main inline").