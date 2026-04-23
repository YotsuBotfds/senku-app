# Spec: MetaStrip Compose primitive

Task: `OPUS-F-02`
Date: 2026-04-17
Priority: P1
Estimate: M (1 day)
Owner lane: worker
Reference: `senku-components.jsx:MetaStrip` (design prototype)

## Problem

Current Android surfaces use pill walls for status telemetry. Pills waste vertical space, truncate awkwardly, and don't communicate tone consistently. Haiku review flagged this as visual noise; gallery screenshots show chip rows that wrap into 2–3 lines on phone portrait.

Rev 03 replaces pill walls with a single primitive: dot-separated mono-caps tokens with per-token tone. One horizontal line where it fits, wraps cleanly when it doesn't.

## Goal

Land a `MetaStrip` Compose primitive as the ONLY telemetry surface for the app. Migrate every current pill-wall site to use it. Delete the old pill-row styles once migration completes.

## Design

### Component API

```kotlin
@Composable
fun MetaStrip(
    items: List<MetaItem>,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
)

data class MetaItem(
    val label: String,
    val tone: Tone = Tone.Default,
    val showDot: Boolean = false,
)

enum class Tone { Default, Warn, Ok, Danger, Accent }
enum class Orientation { Horizontal, Vertical }
```

### Visual spec

- Font: `JetBrains Mono 400` (normal weight), 10–11sp uppercase, letter-spacing 0.08em.
- Color per tone:
  - `Default` → `--ink-2` (`#a09c8a`)
  - `Warn` → `--warn` (`#c49a4b`)
  - `Ok` → `--ok` (`#7a9a5a`)
  - `Danger` → `--danger` (`#c4704b`)
  - `Accent` → `--accent` (`#c9b682`)
- Separator: middle-dot `·` with 8dp horizontal padding, color `--ink-3` at 0.6 opacity.
- Optional 6dp round dot prefix before the label when `showDot = true` — color matches the tone.
- Horizontal orientation: items flow left-to-right, wrap to next line when width exceeded. Each wrapped item retains its tone + optional dot.
- Vertical orientation: items stack top-to-bottom with 3dp gap, no separators. Used in tablet home left rail.

### Wrapping behavior

- Tokens do NOT truncate. If a token doesn't fit, it moves to the next line.
- Separators never appear at the start of a wrapped line (drop the leading `·`).
- Max line count: unbounded in scroll containers; clamp to 2 lines in fixed-height headers with `ellipsize=end` on the final token only.

### Accessibility

- `MetaStrip` is a single landmark region with `contentDescription` composed from the items, joined by commas and tone suffixes (e.g., `"answered (ok), 8.0 seconds, host GPU, 2 sources, 1 turn, low coverage (warn)"`).
- Individual items are NOT focusable — this is a summary line, not an interactive list.
- If tap-affordance is required (future B-04 follow-up), surface via an info icon at the end of the strip, not by making tokens tappable.

## Implementation — Android

### New files

```
android-app/app/src/main/java/com/senku/ui/primitives/MetaStrip.kt
android-app/app/src/main/java/com/senku/ui/primitives/MetaItem.kt
android-app/app/src/main/java/com/senku/ui/theme/SenkuColors.kt  (if not present — extend existing theme)
```

### Compose implementation sketch

```kotlin
@Composable
fun MetaStrip(
    items: List<MetaItem>,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
) {
    val textStyle = TextStyle(
        fontFamily = JetBrainsMono,
        fontSize = 10.sp,
        letterSpacing = 0.08.em,
        color = SenkuColors.ink2,
    )
    if (orientation == Orientation.Horizontal) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
        ) {
            items.forEachIndexed { i, item ->
                if (i > 0) {
                    Text(
                        "·",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = textStyle.copy(color = SenkuColors.ink3.copy(alpha = 0.6f)),
                    )
                }
                MetaToken(item, textStyle)
            }
        }
    } else {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            items.forEach { MetaToken(it, textStyle) }
        }
    }
}

@Composable
private fun MetaToken(item: MetaItem, baseStyle: TextStyle) {
    val color = when (item.tone) {
        Tone.Ok -> SenkuColors.ok
        Tone.Warn -> SenkuColors.warn
        Tone.Danger -> SenkuColors.danger
        Tone.Accent -> SenkuColors.accent
        Tone.Default -> SenkuColors.ink2
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (item.showDot) {
            Box(
                Modifier
                    .size(6.dp)
                    .background(color, shape = CircleShape)
                    .padding(end = 4.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(item.label.uppercase(), style = baseStyle.copy(color = color))
    }
}
```

(Final implementation uses `FlowRow` from `androidx.compose.foundation.layout` for wrap behavior. Verify API availability in the project's Compose version.)

### Mount into existing Activities

Each Activity embeds MetaStrip via `AbstractComposeView`:

```java
// DetailActivity.java — existing Java shell
ComposeView metaView = findViewById(R.id.detail_meta_strip);
metaView.setContent(new Function0<Unit>() { /* Compose lambda */ });
```

Or, if the XML layout gets a new `ComposeView`:

```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/detail_meta_strip"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

See section **Migration Targets** below for per-file edit list.

## Migration Targets

Every current pill-wall site is replaced. Grep targets:

### Layouts to strip pill rows from

| file | old element | new element |
|---|---|---|
| `res/layout/activity_main.xml` | telemetry `LinearLayout` with Chip children | `ComposeView` mounted with horizontal MetaStrip |
| `res/layout/activity_detail.xml` | status chip row in header | `ComposeView` horizontal MetaStrip |
| `res/layout-land/activity_detail.xml` | same as above (once B-10 lands) | same |
| `res/layout/item_search_result.xml` | lane tag pill | inline mono caps tag (NOT MetaStrip — single item, keep as Chip) |

### Code sites to delete

| file | symbol | action |
|---|---|---|
| `DetailActivity.java` | `bindPillRow(...)`, `createChipFor(...)` | delete after migration |
| `MainActivity.java` | `renderTelemetryChips(...)` | delete after migration |
| `res/values/styles.xml` | `Widget.Senku.Chip.Telemetry` style | delete after migration |
| `res/drawable/chip_telemetry_bg.xml` | rounded background | delete after migration |

### String resources

Pill labels migrate to MetaStrip items. Canonical strings live in `values/strings.xml`:

```xml
<!-- Deprecated (delete after migration): -->
<string name="detail_external_review_header_stamp">FIELD HEADER</string>
<string name="detail_external_review_followup_stamp">FOLLOW-UP RAIL</string>
<string name="detail_header_question">Field question</string>

<!-- New labels (mono caps applied at render time, so strings are mixed case): -->
<string name="meta_answered">answered</string>
<string name="meta_host_gpu">host GPU</string>
<string name="meta_host_cpu">host CPU</string>
<string name="meta_host_litert">LiteRT</string>
<string name="meta_sources_one">%d source</string>
<string name="meta_sources_many">%d sources</string>
<string name="meta_turns_one">%d turn</string>
<string name="meta_turns_many">%d turns</string>
<string name="meta_low_coverage">low coverage</string>
<string name="meta_hybrid_retrieval">hybrid retrieval</string>
<string name="meta_rules_count">%d rules</string>
<string name="meta_top_k">k%d</string>
<string name="meta_db_prefix">db %s</string>
<string name="meta_pack_version">pack %s</string>

<!-- New section headers (replace FIELD HEADER / FOLLOW-UP RAIL): -->
<string name="detail_you_asked">You asked</string>
<string name="detail_senku_answered">SENKU ANSWERED</string>
<string name="detail_thread_turns">Thread · %d turns</string>
<string name="detail_sources_count">Sources · %d</string>
```

## Tests

### Unit — Compose preview tests

`android-app/app/src/test/java/com/senku/ui/primitives/MetaStripTest.kt`:

1. Empty list → renders nothing (no separator artifacts).
2. Single item → no separator.
3. 3 items → 2 separators, no leading/trailing separator.
4. Wrapped items (simulate narrow width) → no separator at the start of a wrapped line.
5. All tones render with the expected color per token.
6. `showDot=true` items render with the 6dp dot prefix in matching tone.
7. Vertical orientation → no separators, 3dp vertical gap.

### Instrumentation — screenshot diff

`android-app/app/src/androidTest/java/com/senku/ui/primitives/MetaStripScreenshotTest.kt`:

1. Narrow phone portrait (5556): 6-item strip with mixed tones → clean wrap or single line, no artifacts.
2. Tablet landscape (5558): 6-item strip → fits on single line.
3. Long-label stress test: `pack 04-15 22:50Z · db 5b4d3dc5` → no truncation, wraps to line 2.

### Regression — Haiku-flagged screens

After migration, re-shoot the top three Haiku-flagged screens and confirm:
- No pill walls visible anywhere.
- Tones read correctly (warn is amber, ok is green, danger is terracotta).
- Source count reads `N sources`, not "Strong evidence • 3 sources" (B-15 superseded).

## Rollout

1. Land the primitive file behind a feature flag `FeatureFlags.USE_META_STRIP = false` initially. Flag defaults to false so Activities keep old pill rows.
2. Flip flag to true for DetailActivity first (smallest blast radius).
3. Migrate MainActivity telemetry header.
4. Migrate search result lane tag (this one stays a single `Chip`, not MetaStrip — document in the task).
5. Delete old pill code + styles once every site is migrated.
6. Remove the feature flag.

## Observability

Add one log line on first render per session: `meta_strip rendered n=<count> tones=<comma_tones>`. Aggregate after a week to confirm no site is feeding the strip an empty list or >10 items.

## Open Questions

- **Dot prefix usage.** Prototype uses `showDot=true` only for the `answered` status token. Should it extend to `ready` and other binary-status tokens? Default: yes, any `Ok`/`Warn`/`Danger` status token with a binary meaning gets a dot. Neutral tokens (counts, versions) do not.
- **Accessibility summary.** When the strip has 6+ items, the TalkBack read becomes long. Acceptable? Alternative: split into "status" and "detail" sub-strips, where TalkBack reads only status by default.
- **Interaction.** No tap target in Rev 03. If the future B-04 follow-up lands as an info icon, it mounts at the end of the strip, not by making tokens tappable. Noted in the primitive docstring.
