# Spec: Paper AnswerCard Compose primitive

Task: `OPUS-F-03`
Date: 2026-04-17
Priority: P1
Estimate: L (1–2 days)
Owner lane: worker
Reference: `senku-components.jsx:AnswerCard` (design prototype)
Depends on: `F-01` (color tokens, fonts), `F-02` (MetaStrip) — soft dependency; card header uses MetaStrip tone

## Problem

The current answer surface renders the answer body inside the same olive/dark card chrome as the rest of the detail screen. Visually, the answer and the telemetry around it are indistinguishable — both are "dark card, olive border, sans text". Haiku flagged this as "flat hierarchy on detail pages".

Rev 03 resolves this by treating the answer body as an *authored artifact* — a cream paper card with a serif body — inside the dark UI chrome. The paper-on-dark contrast makes the answer visually distinct from the surrounding surfaces.

## Goal

Land a `PaperAnswerCard` Compose primitive as the ONLY answer-body surface. Replace the current inline answer rendering in DetailActivity with this primitive. Support paper (default) and dark (fallback) modes as compile-time variants, no runtime toggle in prod.

## Design

### Component API

```kotlin
@Composable
fun PaperAnswerCard(
    content: AnswerContent,
    modifier: Modifier = Modifier,
    mode: Mode = Mode.Paper,
    onShowProof: () -> Unit = {},
)

data class AnswerContent(
    val short: String,
    val steps: List<String>? = null,
    val limits: String? = null,
    val evidence: Evidence = Evidence.Moderate,
    val sourceCount: Int,
    val host: String,
    val elapsedSeconds: Double,
    val abstain: Boolean = false,
)

enum class Mode { Paper, Dark }
enum class Evidence { Strong, Moderate, None }
```

### Visual spec — Paper mode (default)

```
┌──────────────────────────────────────────────┐
│ SENKU ANSWERED               • MODERATE EVIDENCE │   ← header row
├──────────────────────────────────────────────┤
│ The provided notes do not contain specific   │   ← serif 17 body
│ instructions for rain-shelter construction   │
│ from tarp and cord.                          │
│                                              │
│ STEPS                                        │   ← optional
│ 1. Pull the tarp evenly across the ridgeline │
│ 2. Stake the two windward corners first      │
│ 3. Tension the remaining corners             │
│                                              │
│ LIMITS & SAFETY                              │   ← optional
│ The retrieved notes assume flat ground …     │
├──────────────────────────────────────────────┤
│ 2 SOURCES · HOST GPU · 8.0S    Show proof ›  │   ← footer
└──────────────────────────────────────────────┘
```

Tokens:

- Background: `--paper` (`#e9e1cf`)
- Body text: `--paper-ink` (`#1f2318`)
- Muted text (headers, footer): `rgba(31,35,24,0.6)` (paper-ink at 60%)
- Hairlines: `rgba(31,35,24,0.12)`
- Border: `1px solid rgba(31,35,24,0.08)`
- Corner radius: 14dp
- Padding: 18dp
- Elevation: `0 1dp 0 rgba(0,0,0,0.04), 0 2dp 8dp rgba(0,0,0,0.18)`

Type:

- Header label (`SENKU ANSWERED`): 10sp JetBrains Mono 400, letter-spacing 0.1em, uppercase, color muted.
- Evidence label (`MODERATE EVIDENCE`): 10sp JetBrains Mono, tone-colored (warn/ok/danger), with 6dp dot prefix.
- Body (`short`): 17sp Source Serif 4 500, line-height 1.45, letter-spacing -0.005em, color paper-ink.
- Steps header (`STEPS`): 10sp JetBrains Mono, muted.
- Step list: 14sp Source Serif 4 400, line-height 1.55, `margin-bottom: 4dp` per item, numbered list style.
- Limits header (`LIMITS & SAFETY`): 10sp JetBrains Mono, danger tone.
- Limits body: 13sp Source Serif 4 400, line-height 1.5, paper-ink.
- Footer meta (`2 SOURCES · HOST GPU · 8.0S`): 10sp JetBrains Mono, muted, uppercase.
- Show proof link: 12sp Inter Tight 600, color paper-ink (paper mode) or accent (dark mode), with trailing 12dp arrow icon.

Sections separated by 10–14dp vertical padding plus a hairline.

### Visual spec — Dark mode (fallback)

Same layout, different tokens:

- Background: `--bg-2` (`#2c3224`)
- Body text: `--ink-0` (`#f1eee2`)
- Muted: `--ink-2` (`#a09c8a`)
- Hairlines: `--hairline`
- Border: `1px solid var(--hairline)`
- Elevation: none
- Show proof link color: `--accent`

Dark mode ships as a compile-time `Mode.Dark` variant for OLED-save builds. Not user-toggleable in Rev 03.

### Abstain mode

When `content.abstain = true` (from `OPUS-A-02` abstain path):

- Background: `rgba(196,112,75,0.06)` — very faint terracotta wash on paper, or `rgba(196,112,75,0.14)` on dark.
- Left border: 2dp solid `--danger`.
- Header label reads `NO MATCH` instead of `SENKU ANSWERED`, danger tone.
- Evidence label: `NONE` in danger tone.
- Steps block NOT rendered (abstain has no steps).
- Limits block renders the abstain explanation text (e.g., "Rain shelter assembly is not covered in the retrieved notes. Open anchor to read source, or broaden search.").
- Footer: `0 sources · …` retained.

This is the visual half of `OPUS-B-05` (low-confidence / no-answer visual state), already landed. `F-03` replaces that implementation with the paper-card version.

### Evidence tone mapping

```
Strong   → Ok    (#7a9a5a green)     label: "STRONG EVIDENCE"    dot: true
Moderate → Warn  (#c49a4b amber)     label: "MODERATE EVIDENCE"  dot: true
None     → Danger (#c4704b terracotta) label: "NO MATCH"         dot: true
```

### Show proof action

- Button invokes `onShowProof` callback.
- Default callback opens the provenance sheet (current behavior in DetailActivity).
- Icon: `arrow` (size 12dp) trailing the label.
- Spacing: 4dp between label and icon.

## Implementation — Android

### New files

```
android-app/app/src/main/java/com/senku/ui/answer/PaperAnswerCard.kt
android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt
android-app/app/src/main/java/com/senku/ui/answer/Evidence.kt
```

### DetailActivity.java migration

The existing answer render block (currently a series of `TextView` + pill rows) is replaced by a single `ComposeView`:

```xml
<!-- res/layout/activity_detail.xml — answer region -->
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/detail_answer_card"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="14dp"
    android:layout_marginTop="10dp" />
```

```java
// DetailActivity.java — on answer bind
ComposeView card = findViewById(R.id.detail_answer_card);
card.setContent(ComposableLambdaKt.composableLambdaInstance(..., true, (composer, ...) -> {
    PaperAnswerCardKt.PaperAnswerCard(
        buildAnswerContent(answerRun),
        Modifier.fillMaxWidth(),
        Mode.Paper,
        () -> openProvenanceSheet()
    );
    return Unit.INSTANCE;
}));
```

### AnswerContent builder

`AnswerRun` (existing backend model) maps to `AnswerContent`:

| AnswerRun field | AnswerContent field | transform |
|---|---|---|
| `normalizedText` (first paragraph) | `short` | paragraph split, take first |
| `numberedSteps` | `steps` | list; null if empty |
| `limitsAndSafety` | `limits` | string; null if empty |
| `metadata.evidence` (`strong`/`moderate`/`weak`) | `evidence` | enum map; weak → None |
| `sources.size` | `sourceCount` | pass through |
| `metadata.host` | `host` | pass through |
| `metadata.elapsedMs / 1000` | `elapsedSeconds` | divide, round to 1 decimal |
| `metadata.abstain` | `abstain` | pass through |

Builder lives in `AnswerContent.kt` as a `fun fromAnswerRun(run: AnswerRun): AnswerContent` factory.

### Fonts

Ensure `Source Serif 4` is loaded:

```
res/font/source_serif_4_regular.ttf
res/font/source_serif_4_medium.ttf
res/font/source_serif_4_semibold.ttf
```

Font family XML:
```xml
<!-- res/font/source_serif_4.xml -->
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:fontStyle="normal" android:fontWeight="400"
          android:font="@font/source_serif_4_regular" />
    <font android:fontStyle="normal" android:fontWeight="500"
          android:font="@font/source_serif_4_medium" />
    <font android:fontStyle="normal" android:fontWeight="600"
          android:font="@font/source_serif_4_semibold" />
</font-family>
```

Compose Typography declares:
```kotlin
val SourceSerif4 = FontFamily(
    Font(R.font.source_serif_4_regular, FontWeight.Normal),
    Font(R.font.source_serif_4_medium, FontWeight.Medium),
    Font(R.font.source_serif_4_semibold, FontWeight.SemiBold),
)
```

## Tests

### Unit — PaperAnswerCardTest.kt

1. Short answer only (no steps, no limits) → renders body + footer, no steps/limits blocks.
2. Answer with steps → numbered list renders; last step has no trailing separator.
3. Answer with limits → limits block renders below steps with danger-toned header.
4. Abstain mode → header reads "NO MATCH" in danger tone; no steps block; limits explains abstain.
5. Mode.Dark → background is `--bg-2`, text is `--ink-0`, show-proof link is accent.
6. Evidence tones → Strong/Moderate/None render in Ok/Warn/Danger colors respectively.
7. Show proof callback → clicking the link invokes `onShowProof`.

### Instrumentation — screenshot diff

`PaperAnswerCardScreenshotTest.kt`:

1. Paper mode, moderate evidence, 3 steps, limits present — canonical full-feature state.
2. Paper mode, strong evidence, no steps, no limits — short-answer state.
3. Paper mode, abstain — empty-match state (replaces gallery 04).
4. Dark mode variant — OLED-save visual target.
5. Phone portrait (5556) + tablet landscape (5558) — density check.

### Regression — replaces gallery 04 and 06

After F-03 lands, re-shoot:
- Gallery 04 (empty-results): should show abstain paper card with danger tone, no steps block, limits explanation. No "1. No steps available." or template leaks.
- Gallery 06 (follow-up thread): should show stacked "You asked" + PaperAnswerCard pairs with thread continuity.

## Dependencies and Bundling

- **F-01 (tokens + fonts) must ship first or in same wave.** The card depends on `SenkuColors.paper`, `SenkuColors.paperInk`, and the Source Serif 4 font family.
- **F-02 (MetaStrip) soft dependency.** The header's evidence tone-dot-label pattern matches MetaStrip. If F-02 lands first, reuse its `Tone` enum; otherwise duplicate the enum temporarily and deduplicate in Wave 4 cleanup.
- **F-13 (DockedComposer) must bundle with F-03 on `activity_detail.xml`.** Both replace sibling elements in the detail layout; running them as separate workers races the layout file. **Recommended: one worker owns F-03 + F-13 as a bundle.**
- **B-05 (already landed) is re-surfaced by F-03.** The current abstain visual lives in the old answer rendering; F-03 adopts the abstain mode natively. Confirm B-05's instrumentation tests still pass after migration (or update them to screenshot the new paper-card variant).

## Rollout

1. Ship F-01 tokens + fonts (can parallel with F-03 primitive file creation; primitive uses the tokens).
2. Build PaperAnswerCard Compose primitive + unit tests (no Activity wiring yet).
3. Replace DetailActivity answer render with `ComposeView` mounting the primitive.
4. Run instrumentation + re-shoot gallery 04 / 06 to confirm visuals.
5. Delete the old answer-render XML nodes + Java bind methods.
6. Update B-05 tests if needed.

## Observability

Log one line per render: `paper_card rendered mode=<paper|dark> evidence=<tone> abstain=<bool> sources=<n> steps=<n>`. Aggregate after a week to catch any surface still falling back to the dark variant in prod.

## Non-Goals

- Streaming text animation (cursor effect, typewriter reveal). Rev 03 renders the final text; animation is separate.
- In-card citation hover previews (show quote on hover). Evidence pane handles this in tablet 3-pane (F-11).
- Multi-paragraph formatting with inline code / tables. If the LLM returns such content, fall back to plain paragraph rendering; rich formatting is a later concern.

## Open Questions

- **Body truncation.** No truncation in Rev 03; card grows to fit. Confirm this doesn't blow out the detail scroll view on extreme answers (>1000 words). Default: no clamp; if it becomes a problem, introduce a "Show full answer" expand at 400 words.
- **Paper card on tablet landscape.** The 3-pane tablet reader (F-06) stacks multiple paper cards (one per thread turn). Confirm the paper-on-dark rhythm reads well when repeated — may need a slightly thinner elevation shadow to avoid visual noise. Validate during F-06 implementation.
- **Elevation on paper mode.** The drop shadow on a cream card against dark background reads well in the prototype. Verify on actual device — shadow anti-aliasing differs between the web prototype and Android Compose.