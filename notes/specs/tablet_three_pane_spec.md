# Spec: Tablet 3-pane Reader

Task: `OPUS-F-06`
Date: 2026-04-17
Priority: P1
Estimate: L (1–2 days)
Owner lane: worker
Reference: `senku-app.jsx:TabletDetail` (design prototype)
Depends on: `F-01` (tokens), `F-02` (MetaStrip), `F-03` (PaperAnswerCard), `F-09` (SourceRow), `F-11` (EvidenceSnippet + XRef — soft dependency)

## Problem

Gallery screenshot 10 (tablet landscape detail) is the single biggest design embarrassment in the app. The layout is a narrow column on the left with massive empty space below it, and the answer body stretched across a wide column that's too wide for comfortable reading. Landscape on a 10" tablet gets worse UX than landscape on a phone.

Rev 03 replaces this with a three-pane dashboard-style reader: left thread+source rail, center answer column with stacked Q+A turns, right evidence pane with live anchor snippet and cross-references.

## Goal

Land a `TabletDetailScreen` Compose layout for tablet landscape (and tablet portrait, with collapse). Replace the current tablet detail XML layout. Wire the evidence pane to pull a live snippet from the anchor guide.

## Design

### Overall layout (landscape)

```
┌─────────┬──────────────────────────────┬────────────┐
│ BACK  🏠│ GD-444  Inclusive Community… │ EVIDENCE · │
│       📌│ answered · 6.9s · host GPU · │ ACTIVE     │
│         │ 2 sources · 2 turns · pack   │            │
│ THREAD  │                              │ [GD-444    │
│ · 3 TURNS│  ╭─── You asked ─────╮      │  anchor] ↗ │
│         │  │ How do I build a   │      │ Inclusive  │
│ T1 ✓    │  │ simple rain shelter│      │ Community… │
│ T2 *    │  │ from tarp and cord?│      │ Section:   │
│ T3 ·    │  ╰───────────────────╯      │ Shelter    │
│         │                              │ ─────────  │
│ SOURCES │  ╭─ SENKU ANSWERED ──╮      │ "A ridge-  │
│ GD-444  │  │ Build a ridgeline │      │ line…"     │
│ GD-201  │  │ first, then drape │      │            │
│ GD-868  │  │ and tension…      │      │ CROSS-REF ·│
│         │  ╰──────────────────╯      │ 6 LINKED   │
│         │                              │ GD-132 ›   │
│         │  ╭── You asked ─────╮       │ GD-220 ›   │
│         │  │ What next after  │       │ GD-099 ›   │
│         │  │ the ridge line…  │       │ GD-868 ›   │
│         │  ╰──────────────────╯       │            │
│         │                              │            │
│         │  [ASK FOLLOW-UP …]  [Ask]   │            │
└─────────┴──────────────────────────────┴────────────┘
   240dp              flex                  280dp
```

### Pane specs

#### Left rail — 240dp

- Padding: 14dp horizontal, 14dp vertical.
- Vertical scroll.
- Sections:
  1. Toolbar: Back · Home icon buttons (left), spacer, Pin icon button (right). Hairline bottom border.
  2. **Thread · N turns** — mono caps header, list of `ThreadTurn` rows.
  3. **Sources** — mono caps header, list of `SourcePill` rows.
- Hairline 1dp right border to separate from center.

`ThreadTurn` row:
- ID label (mono 9sp) + question text (12sp, ellipsize).
- States: `active` (accent, 10% bg wash, 2dp accent left border), `done` (ok color on ID), `pending` (ink-3).
- 6dp 8dp padding, 6dp rounded corners.

`SourcePill` row:
- ID label (mono 9sp) + title (11.5sp, ellipsize).
- Anchor source: ok color on ID, 10% ok bg wash.
- 6dp 8dp padding.

#### Center column — flex

- Max content width: 680dp (content centers when pane is wider).
- Padding: 20dp horizontal, 14dp top.
- Vertical scroll for Q+A stack.
- Sections top to bottom:
  1. **Title bar**: `GD-xxx` (mono accent 11sp) + guide title (20sp Inter Tight 600). MetaStrip below with answered/elapsed/host/sources/turns/pack. Hairline bottom border.
  2. **Q+A stack**: for each turn, render `YouAsked` quote block + `PaperAnswerCard`. 16dp gap between turns.
  3. **Docked composer**: pill input + `Ask` button (accent filled). Hairline top border. Sticks to bottom of center column (NOT page — column).

`YouAsked` quote block:
- Background `rgba(201,182,130,0.06)` (6% accent).
- Border `rgba(201,182,130,0.14)` (14% accent).
- Left border 2dp accent.
- Padding 12dp 16dp.
- Rounded 10dp.
- `YOU ASKED` mono caps header (10sp accent).
- Question body 15sp Inter Tight 500.

#### Right pane — 280dp

- Padding: 14dp.
- Hairline 1dp left border.
- Background `--bg-0`.
- Vertical scroll.
- Sections:
  1. **EVIDENCE · ACTIVE** — `EvidenceSnippet` card (see F-11).
  2. **CROSS-REFERENCE · N LINKED** — list of `XRefRow` (see F-11).

### Tablet portrait collapse

Tablet portrait (5554) has less horizontal room. Layout collapses to:

```
┌──────────────┬──────────────────────┐
│ RAIL (240dp) │ CENTER (flex)        │
│              │                      │
│              │  (evidence footer    │
│              │   collapsed here,    │
│              │   expandable)        │
└──────────────┴──────────────────────┘
```

Evidence pane becomes a collapsible footer in the center column:

```
┌──────────────────────────────────────┐
│ CENTER content …                     │
├──────────────────────────────────────┤
│ ▸ Evidence & cross-refs (6)          │  ← tap to expand
└──────────────────────────────────────┘
```

When expanded, shows the same EvidenceSnippet + XRef list below the fold.

### Phone — no change

Phone stays single-column (F-03 paper card + F-13 docked composer + F-10 try-next chips). This spec does NOT touch phone layouts.

## Implementation — Android

### New files

```
android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt
android-app/app/src/main/java/com/senku/ui/tablet/ThreadRail.kt
android-app/app/src/main/java/com/senku/ui/tablet/EvidencePane.kt
android-app/app/src/main/java/com/senku/ui/tablet/ThreadTurn.kt  (model + composable)
```

### New layout-large files

```
res/layout-large-land/activity_detail.xml   — tablet landscape
res/layout-large/activity_detail.xml        — tablet portrait (rail + collapsing evidence)
```

Both contain a single `ComposeView`:

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/tablet_detail_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

DetailActivity mounts the Compose tree based on resource qualifier:

```java
// DetailActivity.java onCreate, after setContentView
boolean isTabletLand = getResources().getConfiguration().smallestScreenWidthDp >= 600
    && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
ComposeView root = findViewById(R.id.tablet_detail_root);
if (root != null) {
    root.setContent(ComposableLambdaKt.composableLambdaInstance(..., true, (c, ...) -> {
        TabletDetailScreenKt.TabletDetailScreen(buildViewState(), Modifier.fillMaxSize());
        return Unit.INSTANCE;
    }));
}
```

### ViewState model

```kotlin
data class TabletDetailState(
    val guideId: String,
    val guideTitle: String,
    val meta: List<MetaItem>,
    val turns: List<ThreadTurnState>,
    val sources: List<SourceState>,
    val anchor: AnchorState,
    val xrefs: List<XRefState>,
    val composerPlaceholder: String,
)

data class ThreadTurnState(
    val id: String,
    val question: String,
    val answer: AnswerContent,
    val status: Status,
)

data class SourceState(val id: String, val title: String, val isAnchor: Boolean)
data class AnchorState(val id: String, val title: String, val section: String, val snippet: String)
data class XRefState(val id: String, val title: String)
enum class Status { Done, Active, Pending }
```

State assembly happens in `DetailActivity.buildTabletState()`, a new method that reshapes the existing `AnswerRun`/`SessionMemory` data.

### Live anchor snippet (F-11 dependency)

The `AnchorState.snippet` field is the most sensitive part of the spec. Sources:

1. **Preferred:** the top-ranked chunk of the anchor guide (the chunk that scored highest in retrieval). This is the literal evidence for the answer.
2. **Fallback:** the guide's first non-empty paragraph from the guide catalog.
3. **Minimum:** empty string (pane renders a subtle "No snippet" placeholder).

Snippet extraction lives in `F-11`:

```kotlin
// PackRepository.kt
fun getAnchorSnippet(guideId: String, sessionChunkId: String?): String? {
    if (sessionChunkId != null) {
        return chunks[sessionChunkId]?.take(280)  // ~2 sentences
    }
    return guideCatalog.firstParagraph(guideId)?.take(280)
}
```

The `TabletDetailScreen` consumes `AnchorState` as an already-resolved string; it does not know about chunks.

### Cross-reference source (F-11)

`XRefState` list comes from `guide_catalog.getReciprocalLinks(anchorId)` (already introduced for `OPUS-A-01` thread-anchor prior). Reuse that method here.

## Tests

### Unit — TabletDetailScreenTest.kt

1. Empty turns list → center column renders title bar + composer, no Q+A cards.
2. 1 turn → one Q+A pair rendered.
3. 3 turns → three stacked Q+A pairs, correct order (oldest first).
4. Abstain turn → paper card renders in abstain mode (danger border).
5. No anchor → right pane renders "No active evidence" placeholder.
6. Long guide title → title truncates with ellipsize end.
7. Empty xrefs list → "No cross-references" placeholder.

### Instrumentation — layout validation

`TabletDetailLayoutTest.kt`:

1. Tablet landscape (5558): all three panes visible, left rail 240dp, right pane 280dp, center between them.
2. Tablet portrait (5554): two panes, evidence collapsed to footer.
3. Phone landscape (5560): this layout NOT mounted (phone uses F-03 + F-13); confirm phone-landscape still uses the phone layout.
4. Config change rotation (landscape → portrait): Compose state survives; scroll position may reset.

### Screenshot diff — replaces gallery 10

After F-06 lands, re-shoot gallery 10 (tablet landscape detail field links) and confirm:

- Thread rail visible on left with 3 turns.
- Center column has stacked Q+A pairs.
- Right pane has live anchor snippet (not placeholder text from the prototype).
- Cross-reference list pulls from `getReciprocalLinks(anchorId)`.
- No wasted whitespace.

## Dependencies and Bundling

- **F-01 (tokens) + F-02 (MetaStrip) + F-03 (PaperAnswerCard) + F-09 (SourceRow) must all land before F-06.** F-06 is an integrator, not a primitive.
- **F-11 (EvidenceSnippet + XRef) should land with F-06 or immediately before.** If F-11 isn't ready, mount F-06 with placeholder strings and flip the switch when F-11 lands.
- **B-08 (a11y region split) rebases onto F-06.** The current B-08 targets the pre-Rev-03 detail; after F-06, B-08 ensures each of the three panes has a distinct a11y landmark.
- **B-10 (layout-land overrides) is orthogonal — it targets phone-landscape.** F-06 targets tablet. No conflict, can parallel.

## Rollout

1. Wave 4: Land F-01, F-02, F-03, F-04, F-09, F-12 (foundation primitives).
2. Wave 5: Land F-10, F-13 (answer-side wiring).
3. Wave 6: Land F-05, F-07, F-08, F-14 (navigation + home + search).
4. Wave 7: Land F-06 + F-11 (this spec + evidence pane).
5. Wave 7: Run B-07, B-08, B-10 in the same wave (re-apply to Rev 03 primitives).
6. Wave 8: Run B-13, B-15 (overflow + contrast audits) against Rev 03.

## Observability

Log one line on first tablet-landscape bind per session: `tablet_three_pane mounted orientation=<land|port> turns=<n> sources=<n> xrefs=<n>`. Aggregate weekly to catch states where the pane mounts with 0 turns or 0 sources (retrieval broken).

## Non-Goals

- Pane resizing by drag. Panes are fixed at 240 / flex / 280 in Rev 03.
- Swipe-to-collapse rail on landscape. Rail is always visible in landscape.
- Thread branching UI. Linear thread only in Rev 03.
- Foldable-specific layouts (Samsung Z-Fold, etc.). Default landscape treatment applies.

## Open Questions

- **Anchor snippet recomputation.** Does the anchor snippet recompute when the user switches between turns (each turn might have a different anchor)? Default: yes, the right pane reflects the currently-focused turn's anchor. Scroll-linked focus is a later refinement.
- **Evidence pane scroll independence.** Prototype shows independent scroll per pane. Confirm on device that independent scroll doesn't make the layout feel broken; adjacent scrollbars can confuse users.
- **Back button behavior.** On tablet, back navigates turn-by-turn or all the way out? Default: back goes to Home; turn navigation uses the rail's ThreadTurn rows.
