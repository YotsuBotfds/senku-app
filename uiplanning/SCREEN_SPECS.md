# Senku Mobile — Screen-by-Screen Redesign Spec

> This document maps every proposed screen to the verified current state in
> `CURRENT_STATE.md`. Implementation order follows the phase plan in
> `DECISIONS.md`.

---

## Screen 1: Home / Search

### Current → Proposed

| Aspect | Current (verified) | Proposed |
|--------|-------------------|----------|
| Layout | `activity_main.xml` — vertical `LinearLayout`, 223 lines | New layout, probably `ConstraintLayout` or `CoordinatorLayout` |
| Search entry | `EditText` + `Button` side-by-side in a horizontal `LinearLayout` | Hero search bar, full-width, IME action triggers search |
| Mode switch | Separate `search_button` and `ask_button` | Unified bar — app auto-detects mode (model loaded → ask, else → search) |
| Button wall | 6 buttons: Search, Ask, Import Model, Browse, Reinstall, Host GPU | Only search visible; dev-facing buttons move to Settings |
| Pack status | `info_text` verbose panel + `status_text` label | Compact status pill ("96 guides · Offline ready"), expand on tap |
| Progress | `ProgressBar` indeterminate | Determinate progress with "Loading guide 47/96…" |
| Results | `ListView` with `ArrayAdapter` | `RecyclerView` with `LinearLayoutManager`, diff adapter |
| Session panel | `session_panel` (gone by default), shows session text dump | Recent threads strip — last 3–5 sessions as tappable chips |
| Browse | `browse_button` opens flat list in same activity | Category grid (2×2 or 2×3), always visible below search |
| Welcome state | Empty screen with buttons | Category cards (Water, Fire, Medicine, Shelter) as browse tiles |

### New UI Elements Needed

1. **Category grid** — 2×2 `GridLayout` or `RecyclerView` grid. Each cell: category icon, name, guide count, category accent color as left strip or background tint.
2. **Recent threads strip** — horizontal `RecyclerView` or `HorizontalScrollView` with chip-style items. Data source: `ChatSessionStore`.
3. **Status pill** — compact `TextView` with expand animation. Replaces `info_text`.
4. **Search mode indicator** — subtle label or icon change (magnifying glass vs. brain) when model is loaded.
5. **Panic FAB** — optional floating action button for direct deterministic-only lookup. Low priority.

### Data Sources (already exist)

- Category guide counts: query `PackRepository` SQLite `SELECT category, COUNT(*) FROM guides`
- Recent threads: `ChatSessionStore` (in-memory, LRU 10)
- Pack status: `PackManifest` + `PackRepository.isOpen()`
- Model status: `ModelFileStore.hasModel()` + `LiteRtModelRunner`

### Harness ID Preservation

The harnesses target `search_input` and `search_button`. In the unified bar:
- Keep `search_input` as the `EditText` ID
- Either keep `search_button` as a hidden action or map it to the IME action
- Alternative: update harness scripts to use `search_input` + press Enter

### Tablet Orientation Direction

- **Station mode**: wide tablet layout for concurrent browse + results scanning.
- **Clipboard mode**: tall tablet fallback with simpler single-column flow.
- Current resource split:
  - `res/layout-sw600dp-land/activity_main.xml`
  - `res/layout-sw600dp-port/activity_main.xml`
- The first compressed 3-column experiment proved that readability beats density when the browse pane gets too narrow.

---

## Screen 2: Results / Browse

### Current → Proposed

| Aspect | Current | Proposed |
|--------|---------|----------|
| Container | `ListView` in `activity_main.xml` | `RecyclerView` with `StaggeredGridLayoutManager` or `LinearLayoutManager` |
| Card layout | `list_item_result.xml` — horizontal `LinearLayout` | Same basic structure, enhanced |
| Accent strip | `result_accent_strip` — always olive (`senku_accent_olive`) | Color by retrieval mode: hybrid=olive, vector=teal, lexical=amber |
| Badges | `result_retrieval_badge`, `result_category_badge` — text-only | Add category icon (water drop, flame, cross, house) |
| Snippet | `result_snippet` — plain text, maxLines 3 | Expand on long-press or chevron; bold matching terms |
| Section | `result_section` — plain text | Show matched section heading, styled as subtitle |
| Swipe actions | None | Swipe right → open detail; swipe left → bookmark (future) |

### New UI Elements Needed

1. **Retrieval-mode color coding** — change `result_accent_strip` background color per retrieval mode in adapter `getView()`. Colors already exist in `colors.xml`.
2. **Category icons** — need vector drawable icons (water_drop, flame, medical_cross, house). Could use Material Icons or custom SVGs.
3. **Expand-in-place** — add a `View` toggle in `list_item_result.xml` for expanded snippet. Initially `gone`, toggle on click/chevron.
4. **Matched term highlighting** — `SpannableString` with `StyleSpan(BOLD)` for matched terms in snippet.

### Implementation Notes

- `SearchResultAdapter` is 181 lines, all in `getView()`. Straightforward to rewrite as `RecyclerView.Adapter`.
- The `SearchResult` data model (118 lines, Serializable) already has all needed fields: `retrievalMode`, `category`, `sectionHeading`, `snippet`.
- `result_accent_strip` color is already set dynamically via `GradientDrawable.setColor()` in the adapter — just needs to use retrieval mode instead of category.

---

## Screen 3: Answer Thread (DetailActivity replacement)

### Current → Proposed

| Aspect | Current | Proposed |
|--------|---------|----------|
| Layout | `activity_detail.xml` — `ScrollView` with fixed Q/A bubbles | Scrollable chat thread with per-turn bubbles |
| Question display | Single `bg_chat_question` bubble | User bubbles right-aligned, one per turn |
| Answer display | Single `bg_chat_answer` bubble | Senku bubbles left-aligned, one per turn |
| Sources | `detail_sources_container` — shared panel at bottom | Inline source chips per answer turn |
| Thread history | `detail_session_panel` + `detail_thread_container` — programmatic bubbles | Natural scroll history, all turns visible |
| Follow-up | `detail_followup_panel` — bottom docked | Same position, stays pinned |
| Rich formatting | `detail_body` — plain `TextView` with `textIsSelectable` | Markdown-aware rendering (numbered lists, bold, callouts) |
| Quality indicator | None | Tag per turn: "deterministic" / "model-generated" / "host GPU" |
| Context scope | None visible | Pill near input: "Using 5 chunks + 2 prior turns" |
| Suggested follow-ups | None | 2–3 tappable suggestion chips after each answer |

### New UI Elements Needed

1. **Chat turn item layout** — new `list_item_chat_turn_user.xml` and `list_item_chat_turn_answer.xml` for the `RecyclerView`.
2. **Inline source chips** — horizontal `RecyclerView` or `ChipGroup` per answer turn. Data: `SearchResult.sourceGuides`.
3. **Answer quality tag** — small `TextView` badge per answer turn.
4. **Context scope pill** — `TextView` above follow-up input, updated each turn.
5. **Suggestion chips** — horizontal `ChipGroup` below each answer. Content: generated from answer keywords or hardcoded patterns.
6. **Markdown rendering** — `TextView` with `Spannable` or a library like Markwon (needs new dependency).

### Data Sources

- Turn history: `SessionMemory.getTurns()` — already stores question, answer, sources, retrieval mode
- Source guides: `SearchResult` already has `sourceGuideIds` (or similar)
- Context scope: `SessionMemory.getRetrievalPlan()` / `getPrioritizedSources()`
- Answer quality: comes from the pipeline — `DeterministicAnswerRouter` returns deterministic; `OfflineAnswerEngine` returns model-generated; `HostInferenceClient` returns host GPU

### Harness ID Preservation

Critical IDs that harnesses use:
- `detail_title` → map to first user turn text
- `detail_body` → map to first answer turn text
- `detail_followup_input`, `detail_followup_send` → keep as-is
- `detail_sources_container` → map to first answer's source chip group

---

## Screen 4: Guide Detail / Source View (NEW)

### Current State

No dedicated guide detail screen exists. Tapping a result opens `DetailActivity`
in "guide" mode, which shows the guide title and body as a single answer.

### Proposed

| Element | Description |
|---------|-------------|
| Hero header | Guide ID badge, category badge, difficulty badge, title, summary |
| Section tabs | Horizontal scrollable tab strip for guide sections |
| Guide body | Full guide text with rich formatting, callout boxes |
| "Ask about this" FAB | Starts a thread scoped to this guide |
| Category gradient | Hero header tints to category accent color |

### Data Sources

- Guide metadata: `PackRepository` SQLite `guides` table — has `guide_id`, `title`, `category`, `difficulty`, `body_markdown`
- Section structure: parse `body_markdown` headings or use `chunks` table `section_heading` column
- Related guides: `guide_related` table in the pack database

### Implementation

New activity or fragment: `GuideDetailActivity.java` (or `GuideDetailFragment.java`).
Needs a Markdown renderer. Could use `WebView` with CSS for simplest path, or
Markwon for native rendering.

---

## Screen 5: Category Browser (NEW)

### Current State

`browse_button` in `MainActivity` calls `browseGuides()` which loads all guides
into the same `ListView` used for search results. No category grouping.

### Proposed

| Element | Description |
|---------|-------------|
| Category grid | 2×2 or 2×3 card grid (Water, Fire, Medicine, Shelter, Food, Tools, Comms, Community) |
| Category detail | Filtered list within a category, section-grouped headers |
| Search-within | Search bar scoped to active category |
| Guide families | "Related guides" section at bottom of filtered results |

### Data Sources

- Category list: `SELECT DISTINCT category FROM guides` in `PackRepository`
- Filtered guides: `SELECT * FROM guides WHERE category = ?`
- Section grouping: `SELECT DISTINCT section_heading FROM chunks WHERE guide_id IN (…)`
- Related guides: `guide_related` table

### Implementation

This could be:
- A separate `BrowseActivity` / `BrowseFragment`
- Or the home screen itself with the category grid always visible
- Decision tracked in `DECISIONS.md`

---

## Screen 6: Settings (NEW)

### Current State

Dev-facing buttons scattered across `activity_main.xml`:
- `import_model_button`
- `host_inference_button`
- `reinstall_button`
Plus `HostInferenceConfig` stored in `SharedPreferences`.

### Proposed

Absorbs into a dedicated Settings screen:

| Setting | Current | Proposed |
|---------|---------|----------|
| Import model | `import_model_button` on home | Settings → Import Model |
| Host GPU | `host_inference_button` on home | Settings → Host GPU (with config) |
| Reinstall pack | `reinstall_button` on home | Settings → Reinstall Pack |
| Pack info | `info_text` dump on home | Settings → Pack Info (full diagnostic) |
| Model info | nowhere explicit | Settings → Model Info (path, size, loaded status) |
| Clear sessions | `clear_chat_button` in session panel | Settings → Clear All Sessions |

### Implementation

Simple `AppCompatActivity` with a `RecyclerView` or `PreferenceFragmentCompat`.
If using `PreferenceFragmentCompat`, needs new dependency on `androidx.preference`.

---

## Screen 7: Onboarding (NEW)

### Current State

Pack installs silently via `PackInstaller`. "Pack ready" appears in `status_text`.
No first-launch detection. No model import guidance.

### Proposed

| Screen | Content |
|--------|---------|
| Welcome | Logo, tagline ("692 survival guides. Zero internet required.") |
| Pack install | Determinate progress bar, "Loading guide 47/96…" |
| Model import | Prompt to import a model; "Skip" option |
| Done | Summary card with capability stats |

### Data Sources

- Pack install progress: `PackInstaller` currently has no progress callback — needs instrumentation
- First-launch detection: `SharedPreferences` boolean flag
- Capability stats: `PackManifest` (guide count, rule count, vector count)

---

## Navigation Architecture

### Current

```
MainActivity ──startActivity()──→ DetailActivity ──finish()──→ back
```

### Proposed Options

**Option A: Bottom Navigation (3 tabs)**
```
MainActivity
├── HomeFragment     (search + category grid)
├── BrowseFragment   (category browser)
└── SettingsFragment (dev settings)
DetailActivity       (answer thread)
GuideDetailActivity  (source view)
```

**Option B: Two-screen (minimal)**
```
MainActivity         (search + browse + category grid)
DetailActivity       (answer thread / guide detail combined)
```

**Option C: Bottom Navigation (4 tabs)**
```
MainActivity
├── HomeFragment     (search + recent threads)
├── BrowseFragment   (category grid → filtered list)
├── ThreadsFragment  (session history list)
└── SettingsFragment (dev settings)
DetailActivity       (answer thread)
GuideDetailActivity  (source view)
```

Decision tracked in `DECISIONS.md` question 3.

---

## Dependency Additions Required

| Library | Purpose | Priority |
|---------|---------|----------|
| `androidx.recyclerview:recyclerview` | Replace ListView | Phase 1 |
| `androidx.constraintlayout:constraintlayout` | Flexible layouts | Phase 1 |
| `com.google.android.material:material` | Bottom nav, chips, FAB, tabs | Phase 1 |
| `androidx.viewpager2:viewpager2` | Onboarding carousel (optional) | Phase 3 |
| `androidx.preference:preference` | Settings screen (optional) | Phase 2 |
| `io.noties.markwon:core` | Markdown rendering in answers | Phase 2 |
| `androidx.room:room-runtime` | Session persistence (optional, future) | Phase 3+ |

---

## Design System Summary

### Colors to Add

| Name | Hex | Role |
|------|-----|------|
| `senku_badge_food` | `#8B8A3E` | Food & Agriculture |
| `senku_badge_tools` | `#BE8C39` | Tools & Craft (reuse lexical amber) |
| `senku_badge_comms` | `#4C8E8A` | Communications (reuse vector teal) |
| `senku_badge_community` | `#7A6AAA` | Community |

### Typography

Current: system default `sans-serif` everywhere.

Proposed:
- Body: Inter 400 14sp
- UI labels: Inter 500–600 12–13sp
- Headers: Inter 700 16–22sp
- Monospace: JetBrains Mono 400–600 10–12sp

This requires bundling font files in `res/font/` or using downloadable fonts.

### Component Patterns

| Component | Current | Proposed |
|-----------|---------|----------|
| Cards | `bg_result_card` shape drawable | Same base, add ripple, elevation |
| Chips | None (badges are `bg_badge` + `TextView`) | Material `Chip` or custom pill |
| Buttons | `bg_button_primary` / `bg_button_secondary` | Same + ripple + Material styling |
| Chat bubbles | `bg_chat_question` / `bg_chat_answer` | Asymmetric radius, per-turn layout |
| Callouts | None | Left 3dp olive border, tinted bg |
| Bottom nav | None | Material `BottomNavigationView` |
