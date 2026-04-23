# Senku Mobile — Open Decisions & Phased Rollout

> All decisions here are unresolved. Update status when decided.

---

## Decision Matrix

### D1: Navigation Architecture

| Option | Screens | Complexity | Fragility | Harness Impact |
|--------|---------|------------|-----------|----------------|
| **A: 3-tab bottom nav** | Home / Browse / Settings + Detail + GuideDetail | Medium | Medium | Need to route harness intents to HomeFragment |
| **B: Two-screen minimal** | Main (search+browse) + Detail (thread+guide) | Low | Low | Minimal — same ID surface |
| **C: 4-tab bottom nav** | Home / Browse / Threads / Settings + Detail + GuideDetail | High | Higher | More screens to manage |

**Recommendation:** Start with **B** for Phase 1, evolve to **A** in Phase 2.
Rationale: Two-screen keeps the harness contract simple and avoids Fragment
navigation complexity during the foundational RecyclerView migration. Bottom
nav adds value once browse and threads are real surfaces, not placeholders.

**Status:** OPEN

---

### D2: Home Screen Default Mode

| Option | UX | Complexity |
|--------|-----|------------|
| Search-first (search bar on top, categories below) | Clean, focused | Low |
| Browse-first (full category grid, search as collapsible header) | Discovery-oriented | Medium |
| Adaptive (search-first, but show categories on empty state) | Best of both | Low |

**Recommendation:** Adaptive. Default to search bar with category grid visible
below. When a query is active, categories scroll up or collapse.

**Status:** OPEN

---

### D3: Panic FAB Scope

| Option | Behavior | Use Case |
|--------|----------|----------|
| Disabled/hidden | Not in Phase 1 | — |
| Category shortcut | Tap → jump to that category's guide list | Quick browse |
| Direct query lane | Tap → search field with deterministic-only flag, no AI | Urgent first-aid |
| Full field-manual mode | Tap → disable AI, show only raw guide text | Offline-low-power |

**Recommendation:** Start with "disabled/hidden". Add "category shortcut" in
Phase 2 if the category grid isn't prominent enough. Reserve "direct query"
for Phase 3 as a differentiator.

**Status:** OPEN

---

### D4: Material3 vs Custom Theming

| Option | Pros | Cons |
|--------|------|------|
| Material3 components + custom color overlay | Standard components, accessibility built-in, dark mode support | May dilute field-manual aesthetic; larger APK |
| Stay custom AppCompat | Full control, minimal dependency, lighter APK | More work, miss accessibility defaults |
| Hybrid: Material3 components, fully custom theme attrs | Best of both — standard widgets, custom look | Moderate complexity |

**Recommendation:** Hybrid. Add `com.google.android.material` as a dependency
for `BottomNavigationView`, `Chip`, `FloatingActionButton`, `TabLayout`, and
`MaterialCardView`. Override their colors/shape with custom theme attributes
to preserve the olive/parchment palette.

**Status:** OPEN

---

### D5: Session Persistence

| Option | Scope | Complexity |
|--------|-------|------------|
| In-memory only (current) | Lost on process death | Zero |
| SharedPreferences JSON | Survives restart, small data | Low |
| Room SQLite | Full query, scalable | High |
| File-based JSON per session | Simple, exportable | Low |

**Recommendation:** Defer to Phase 3. Current in-memory is fine for the UI
redesign. When persistent threads are needed, Room is the right answer since
we already depend on raw SQLite for pack data. But Room is a heavy dependency
add for Phase 1.

**Status:** DEFERRED to Phase 3

---

### D6: Markdown Rendering

| Option | Pros | Cons |
|--------|------|------|
| `android.text.Html` (built-in) | Zero dependency, handles basic formatting | Limited Markdown support |
| Markwon library | Full CommonMark, extensible, inline images | New dependency (~200KB) |
| WebView + CSS | Zero dependency for Markdown, full HTML/CSS | Heavy, async loading, accessibility gap |

**Recommendation:** Markwon for native rendering. The guide bodies are already
Markdown. The 200KB dependency is worth it for proper list rendering, bold,
headers, and code blocks.

**Status:** OPEN

---

### D7: Language — Stay Java or Move to Kotlin?

| Option | Pros | Cons |
|--------|------|------|
| Stay Java 17 | No migration risk, consistent with existing code | Verbose for new UI code |
| Kotlin for new code only | Modern syntax for new screens/fragments | Mixed codebase, two languages |
| Full migration to Kotlin | Modern, concise | Massive rewrite risk |

**Recommendation:** Stay Java for all new UI code. The codebase is 20 Java
files, ~6000 lines. Mixing languages for UI code adds cognitive overhead with
no functional benefit. If Kotlin is desired, do a full migration later.

**Status:** OPEN (but lean strongly toward Java)

---

## Phased Rollout Plan

### Phase 1: Foundation (no new screens, modernize existing)

**Goal:** Replace ListView with RecyclerView, add dependencies, clean up layouts.

| Task | Files Changed | Notes |
|------|--------------|-------|
| Add Material, RecyclerView, ConstraintLayout deps | `build.gradle` | 3 new dependencies |
| Convert `SearchResultAdapter` to `RecyclerView.Adapter` | `SearchResultAdapter.java`, `activity_main.xml` | Keep same visual, just change list widget |
| Add `ConstraintLayout` to `activity_main.xml` | `activity_main.xml` | Remove nested LinearLayouts |
| Unify search bar (remove `ask_button`, auto-detect mode) | `MainActivity.java`, `activity_main.xml` | Model loaded → ask; else → search-only |
| Move dev buttons to a collapsible "Developer" section | `activity_main.xml`, `MainActivity.java` | Not a separate screen yet |
| Add retrieval-mode color coding to accent strip | `SearchResultAdapter.java` | Colors already in `colors.xml` |
| Add category icons to result cards | `list_item_result.xml`, drawables | Need 4+ vector drawable icons |
| Replace indeterminate progress with determinate | `PackInstaller.java`, `activity_main.xml` | Needs progress callback in PackInstaller |
| Preserve harness IDs | all | Verify automation still works |

**Estimated effort:** Medium. Core list migration + layout cleanup.
**Risk:** Low. Same screens, same navigation, same harness surface.

---

### Phase 2: New Screens & Navigation

**Goal:** Add bottom nav, category browser, settings screen, improved detail.

| Task | Files Changed | Notes |
|------|--------------|-------|
| Add `BottomNavigationView` to `activity_main.xml` | `activity_main.xml`, new menu XML | 3 tabs: Home, Browse, Settings |
| Create `HomeFragment.java` | New file | Search + category grid + recent threads |
| Create `BrowseFragment.java` | New file | Category grid → filtered guide list |
| Create `SettingsFragment.java` (or Activity) | New file | Absorbs dev buttons |
| Add Fragment navigation via `FragmentManager` | `MainActivity.java` | Or Jetpack Nav if adopted |
| Rework `DetailActivity` to chat-thread layout | `activity_detail.xml`, `DetailActivity.java` | Per-turn RecyclerView, inline sources |
| Add answer quality tags per turn | `DetailActivity.java` | Tag: deterministic / model / host |
| Add context scope pill | `activity_detail.xml` | "Using 5 chunks + 2 prior turns" |
| Add Markwon dependency | `build.gradle` | Markdown rendering |
| Add new category colors (food, comms, community) | `colors.xml` | 3 new entries |

**Estimated effort:** High. New fragments, new navigation, detail rework.
**Risk:** Medium. Harness scripts need updates for Fragment routing.

---

### Phase 3: Polish & New Surfaces

**Goal:** Onboarding, guide detail view, persistent threads, optional features.

| Task | Files Changed | Notes |
|------|--------------|-------|
| Onboarding flow (2–3 screens) | New `OnboardingActivity.java`, layouts | First-launch only, SharedPreferences flag |
| Guide detail activity | New `GuideDetailActivity.java`, layout | Section tabs, hero header, "Ask about this" FAB |
| Session persistence (Room or file-based) | New files | Depends on D5 decision |
| Suggested follow-ups | `DetailActivity.java` | Auto-generated from answer keywords |
| Expand-in-place result snippets | `list_item_result.xml` | Toggle expanded state |
| Skeleton loading states | New drawables | Replace status text with shimmer/skeleton |
| Edge-to-edge + system bar theming | `themes.xml`, `styles.xml` | Match `senku_bg_primary` |
| Panic FAB (if decided) | `activity_main.xml` | Depends on D3 decision |

**Estimated effort:** High.
**Risk:** Medium. Many new files, but isolated from core retrieval pipeline.

---

## Constraints & Guardrails

1. **Harness stability:** All automation harnesses (`run_android_prompt.ps1`,
   `run_android_search_log_only.ps1`, `run_android_detail_followup_logged.ps1`,
   etc.) rely on specific resource IDs. Any ID rename must update all harness
   scripts simultaneously. Prefer keeping IDs stable and wrapping with new
   containers.

2. **No Kotlin migration:** All 20 source files are Java. New UI code should
   stay Java unless D7 is decided otherwise.

3. **Pack format stability:** The mobile pack SQLite schema (`guides`, `chunks`,
   `deterministic_rules`, `guide_related`, `lexical_chunks_fts`, `pack_meta`)
   is produced by Python and consumed by the app. UI changes must not require
   pack format changes.

4. **Offline-first:** The app must work fully offline. Any new dependency must
   not require network. Material components, Markwon, Room — all work offline.

5. **Retrieval pipeline untouched:** `PackRepository`, `OfflineAnswerEngine`,
   `DeterministicAnswerRouter`, `QueryMetadataProfile`, `QueryRouteProfile`,
   `AnswerContextSelector`, `PromptBuilder` should not change during UI work.
   The redesign is purely presentation-layer.

6. **API level floor:** Min SDK 26 (Android 8.0). All new components must
   support API 26+. Material3 requires API 21+, so we're fine.

7. **APK size:** Current APK is lean. Adding Material + Markwon adds ~1–2MB.
   Acceptable. Don't add Play Services or large transitive dependencies.

---

## Verification Checklist (per phase)

After each phase, verify:

- [ ] `./gradlew assembleDebug` succeeds
- [ ] APK installs on emulator (API 26+)
- [ ] Pack installs and search returns results
- [ ] Ask mode works (if model imported)
- [ ] Host inference works (if configured)
- [ ] Deterministic rules still fire
- [ ] Follow-up still works in DetailActivity
- [ ] All harness scripts pass without modification (Phase 1) or with documented updates (Phase 2+)
- [ ] `run_mobile_headless_preflight.py` passes
- [ ] No new network permissions required
- [ ] APK size delta < 2MB per phase

---

## Tablet Decisions Addendum (2026-04-13)

### D8: Tablet Posture Model

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: One tablet layout for all postures** | Stretch one `sw600dp` layout across both tall and wide tablet orientations | Lowest file count | Risks awkward compromise layouts |
| **B: Split tablet by posture** | Wide tablet = station, tall tablet = clipboard/manual | Matches actual field use, clearer UX goals | More layout files to maintain |
| **C: Disable tablet specialization for tall mode** | Only wide tablets get special treatment; tall mode falls back to phone-like UI | Lowest risk for tall mode | Tall tablet wastes potential |

**Recommendation:** Choose **B**.

Rationale: live emulator validation showed that wide and tall tablet postures behave like different tools. Wide mode supports concurrent browse/results work. Tall mode is better treated as a focused reading/manual surface. Forcing one compromise layout across both creates avoidable UI failures.

**Status:** WORKING DECISION

### D9: Tablet Navigation Semantics

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Keep current two-activity behavior** | Detail remains its own activity; back returns to dashboard/results | Predictable, low risk, harness-safe | Not true master-detail |
| **B: Embedded master-detail only on tablet** | Landscape keeps results left and detail right in one activity | Strong station feel | Higher complexity, more state handling |
| **C: Full master-detail across tablets and large phones** | Unified in-place browsing model | Powerful once stable | Highest fragility and harness impact |

**Recommendation:** Stay with **A** for now, revisit **B** after tablet detail layouts land.

Rationale: the current back behavior is simple and validated. The project still needs posture-specific detail layouts before an embedded master-detail controller is worth the risk.

**Status:** WORKING DECISION

### D10: Retrieval Confidence Presentation

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Numeric confidence meter** | Show a percent or calibrated-looking confidence value | Visually strong, easy to scan | Misleading with current heuristic signals |
| **B: Evidence-strength label** | Show `Strong / Moderate / Limited evidence` from visible provenance signals | Honest, low risk, fits survival trust model | Less flashy than a meter |
| **C: No confidence signal** | Keep route/anchor/sources only | Simplest | Misses a useful trust cue |

**Recommendation:** Choose **B**.

Rationale: Android currently has route family, deterministic status, source count, retrieval-mode mix, and anchor/source provenance, but not a calibrated confidence contract. Exposing a fake percentage would overstate certainty in a survival product. An evidence-strength label is the safer bridge.

**Status:** WORKING DECISION

### D11: Tablet Source Chip Interaction

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Keep full screen guide jump everywhere** | Tapping a source chip always opens the source guide in `DetailActivity` | Lowest complexity, consistent | Breaks answer-reading flow on wide tablet |
| **B: Add a provenance rail on wide tablet only** | Wide tablet opens an inline side panel with cited snippet + highlight; phone/tall tablet still jump to guide | Strengthens trust loop without breaking core flow, fits station posture | Needs a new tablet-only panel/state path |
| **C: Replace guide jump entirely with provenance drawer** | All source taps open snippet panels instead of guide view | Very strong inline verification | Hides full source navigation and risks overfitting to one posture |

**Recommendation:** Choose **B**.

Rationale: wide tablet has the space to support `trust but verify` inline without ejecting the user from the answer thread. Phone and tall-tablet still benefit from the simpler full-screen guide jump. This keeps the core thread model intact while giving the station posture a meaningful verification upgrade.

**Status:** CANDIDATE FOR POST-CORE PASS

---

## UI Direction Audit Addendum (2026-04-14)

### D12: Phone Portrait Hero Panel Lifecycle

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Always visible hero** | Keep status/mode hero visible for full thread lifetime | Maximum state visibility | Pushes answer body too low on small phones |
| **B: Auto-collapse after answer render** | Keep hero during pending/staged states, hide after stable body render on phone portrait | Recovers above-the-fold space without losing trust spine | Needs careful state toggle logic in `DetailActivity` |
| **C: Manual dismiss only** | User taps to hide hero | Explicit control | Extra tap in stressful contexts |

**Recommendation:** Choose **B**.

Rationale: the trust contract is already preserved in the compact top rail (`route`, `evidence`, `src`, `turn`, anchor chip). Keeping the larger hero after answer render creates metadata-first clutter on the most constrained posture.

**Status:** WORKING DECISION

### D13: Collapsed "Why This Answer" Affordance

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Text-only row** | Keep current minimal `Why this answer | Show` row | Low implementation effort | Easy to miss while scrolling |
| **B: Subtle accent affordance** | Add left-edge accent/bar to collapsed row only | Increases discoverability without expanding content | Small visual tuning work |
| **C: Expanded by default** | Always show `Why this answer` body | Maximum transparency | Adds vertical clutter and pushes answer lower |

**Recommendation:** Choose **B**.

Rationale: this keeps the compact drawer model while making the trust explanation visibly discoverable in high-speed reading.

**Status:** WORKING DECISION

### D14: Phone Portrait Follow-up Composer Density

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Expanded composer always** | Title + subtitle + full input row on phone portrait | More explanatory | Consumes too much vertical space |
| **B: Compact default composer** | `Ask follow-up... | Go` on phone portrait answer mode | Better reading density and faster interaction | Slightly less explicit onboarding copy |
| **C: Adaptive first-turn expanded** | Expanded once, compact thereafter | Balanced onboarding/density | More UI-state complexity |

**Recommendation:** Keep **B** as default.

Rationale: compact treatment is already proving better in live phone validation and aligns with field-readability goals.

**Status:** WORKING DECISION

### D15: Tablet Portrait Support Panel Behavior

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A: Fully expanded support panels** | Keep all lower support panels expanded in clipboard posture | Immediate visibility | Creates long scroll stacks |
| **B: Drawer parity with phone portrait** | Keep compact collapsed drawers for support sections | Cleaner reading flow and consistency | Requires careful divider/spacing pass |
| **C: Hybrid (sources open, others collapsed)** | Keep provenance open, collapse helper/context panels | Preserves evidence visibility with cleaner flow | Slightly more custom logic |

**Recommendation:** Start with **C**, then evaluate **B** if density still feels heavy.

Rationale: clipboard posture should stay reading-first, but explicit provenance should remain easy to inspect without extra taps.

**Status:** CANDIDATE FOR NEXT UI SLICE
