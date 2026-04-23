# UI Direction — Rev 03 (locked)

Date: 2026-04-17
Source prototype: Claude.ai design artifact `e4447a88-7e32-4d36-bf89-afee48623d0f` (`senku-redesign.html`, `senku-components.jsx`, `senku-app.jsx`)
Supersedes: `../reviews/UI_DIRECTION_AUDIT_20260414.md` for all surface-level decisions (olive/parchment + typewriter stamps)
Paired with: `opustasks.md` F-lane, `parallelism_plan.md` Waves 4–9

This doc is the committed design target for the Android app going forward. All B-lane polish and new F-lane migration work targets this doc. The React prototype in the design artifact is canonical — when a primitive spec is ambiguous, diff against `senku-components.jsx` / `senku-app.jsx`.

## Decisions Locked

The Tweaks panel in the prototype presents five live toggles. All five are now locked:

| toggle | locked value | why |
|---|---|---|
| Accent | Brass `#c9b682` | Warm but restrained; reads against the olive palette without clashing. Copper reads too warm on field-manual text; moss flattens against the olive background. |
| Answer card | Paper (cream on dark) | This is the identity move. The answer body stops being "more chrome" and becomes "a thing you're reading". Dark variant retained as fallback token for low-battery / OLED-save mode. |
| Phone nav | Bottom tabs (Home · Search · Ask · Threads · Pins) | Discoverability > novelty. Bottom tabs get field-review users to the second-most-used surface in one tap. |
| Hero density | Compact | Roomy wastes the top half of phone portrait on whitespace; compact keeps the identity strip at 36px height. |
| Evidence chip | Meta strip (mono caps, dot separators) | Single primitive for all telemetry beats pill walls. Pill stack retained only for the `Chip` primitive (tags, category badges). |

**Field-manual voice retained, stamp vocabulary replaced.** The typewriter feel, olive/paper/brass palette, and mono-caps telemetry all carry the field-manual identity. But the specific stamp strings (`FIELD HEADER`, `FOLLOW-UP RAIL`, duplicated `Field entry`) are dropped because they read as form-builder placeholders. Replacement vocabulary:

| old stamp | new label |
|---|---|
| `FIELD HEADER` (external review detail) | `You asked` / `Senku answered` (context-dependent) |
| `FOLLOW-UP RAIL` | `Thread · N turns` |
| `Field question` | `You asked` |
| `Field entry` (body answer) | paper card body — no stamp, serif body is the stamp |
| `Field entry` (thread title) | `Thread` |
| `Field entry` (mode answer) | dropped — evidence tone on MetaStrip carries this |

Principle: notebook vocabulary, not form-builder vocabulary.

## Implementation Target: Compose Primitives in Activity Shells

The Rev 03 primitives are implemented as **Compose primitives** mounted into the existing Activity shells (DetailActivity, MainActivity, etc.) via `AbstractComposeView` or full Compose roots where the Activity is already Compose-ready.

This upgrades `OPUS-B-12` (Compose migration spike) from a scout-only spike to a commitment. Rationale:
- MetaStrip (mono-caps, per-token tone, dot separators) is ~20 lines of Compose vs 80 lines of nested `LinearLayout` + custom spans.
- AnswerCard with serif body + steps list + limits block is much cleaner as a single composable.
- The 3-pane tablet reader is natural in Compose `Row { Rail; Column; EvidencePane }`.
- The Activity shells stay Java; only the rendered content becomes Compose. Blast radius stays in the primitive file.

The Presenter extraction (`OPUS-E-05`) runs AFTER all F-lane primitives land, and extracts the Java side only; the Compose content stays where it is.

## Type System

| role | family | weights |
|---|---|---|
| UI, buttons, titles | **Inter Tight** | 400, 500, 600, 700 |
| Answer body (paper card) | **Source Serif 4** | 400, 500, 600 (opsz 8..60) |
| IDs, telemetry, mono caps | **JetBrains Mono** | 400, 500 |

Font resources live under `android-app/app/src/main/res/font/`. Declared in `values/styles.xml` and surfaced as Compose `Typography` tokens. Do not use `android:fontFamily="sans-serif"` anywhere — those references get the device default, not the brand.

Type scale (locked):

| role | size | line-height | letter-spacing |
|---|---|---|---|
| Canvas title | 36px Inter Tight 600 | 1.1 | -0.02em |
| Section title | 22px Inter Tight 600 | 1.2 | -0.01em |
| Serif answer body | 17px Source Serif 4 500 | 1.45 | -0.005em |
| UI body | 14px Inter Tight 500 | 1.5 | -0.005em |
| Small body | 13px | 1.5 | -0.005em |
| Micro body | 12px | 1.4 | — |
| Mono caps meta | 10–11px JetBrains Mono 400 | 1.4 | 0.08–0.10em uppercase |
| Tag / chip | 11–12px Inter Tight 500 | 1.2 | — |

## Color Tokens

Full palette, source of truth. Any hex that doesn't appear below must be justified in a code review comment.

```
// Deepest / neutral
--bg-0: #1a1d16   // deepest base
--bg-1: #22271d   // card base
--bg-2: #2c3224   // raised
--bg-3: #3a4130   // hover

// Olive accents
--olive-10: #4a5139
--olive-20: #5a6147
--olive-40: #7a8263
--olive-60: #9aa084

// Ink (text on dark)
--ink-0: #f1eee2   // primary, warm paper
--ink-1: #d7d3c2
--ink-2: #a09c8a
--ink-3: #7a7768

// Paper (answer card)
--paper:     #e9e1cf
--paper-ink: #1f2318

// Brand + semantic
--accent:    #c9b682  // warm brass
--danger:    #c4704b  // terracotta
--warn:      #c49a4b
--ok:        #7a9a5a

// Hairlines
--hairline:         rgba(241,238,226,0.08)
--hairline-strong:  rgba(241,238,226,0.14)
```

Alternate accents (kept in palette but not default): copper `#c48a5a`, moss `#9bab6a`. Ship as tokens; do not wire to a runtime toggle yet.

## Primitive Catalogue

Each primitive is one F-lane task. Diff against `senku-components.jsx` for the reference implementation.

### MetaStrip `F-02`

The one telemetry primitive. Replaces every pill wall across the app.

```
answered · 8.0s · host GPU · 2 sources · 1 turn · low coverage
  (ok)                                              (warn)
```

- Mono caps, 10–11px JetBrains Mono.
- Items separated by middle dots · (8px horizontal margin, ink-3 color at 0.6 opacity).
- Per-item tone: `default` (ink-2), `warn` (warn), `ok` (ok), `danger` (danger), `accent` (accent).
- Optional 6px round dot prefix for status-heavy items.
- Wraps to multi-line if overflow; does not truncate tokens.

### AnswerCard `F-03`

Paper-on-dark, serif body.

```
╭─────────────────────────────────╮
│ SENKU ANSWERED    • moderate evidence │
├─────────────────────────────────┤
│ (serif 17, Source Serif 4)      │
│ The provided notes do not …     │
├─────────────────────────────────┤
│ STEPS                           │
│ 1. …  2. …  3. …                │
├─────────────────────────────────┤
│ LIMITS & SAFETY (danger tone)   │
│ (plain serif/sans 13px)         │
├─────────────────────────────────┤
│ 2 sources · host GPU · 8.0s   [Show proof ›] │
╰─────────────────────────────────╯
```

- Background `--paper`, text `--paper-ink`, hairlines `rgba(31,35,24,0.12)`.
- Elevation: `0 1px 0 rgba(0,0,0,0.04), 0 2px 8px rgba(0,0,0,0.18)`.
- Header row: `SENKU ANSWERED` mono caps left, evidence tone+dot right.
- Steps: ordered list, serif 14, `li` margin-bottom 4px.
- Limits: mono caps `LIMITS & SAFETY` header in danger tone, serif 13 body.
- Footer: source count + generation time (mono) on left, `Show proof ›` link on right (accent color on dark variant; paper-ink on paper).
- Two modes: `paper` (default) and `dark` (fallback token, no runtime toggle in prod).

### TopBar `F-04`

Slim icon toolbar.

- Back icon button (left) — 34×34, bg-1, hairline border.
- Home icon button — same, when applicable.
- Title + ID stack (flex-1, ellipsize end, id mono caps subtitle).
- Pin icon button.
- Share icon button.
- Replaces Back|Pin|Share pill row.

### BottomTabs `F-05`

Phone-only. Home · Search · Ask · Threads · Pins.

- 19px icons (Icon primitive, stroke 1.8).
- Labels 10px Inter Tight 500, -0.005em.
- Active tab: accent color. Inactive: ink-3.
- Padding `8px 8px 20px` (bottom indicator spacing).
- Hairline top border.

### Tablet 3-pane `F-06`

Landscape only.

| pane | width | content |
|---|---|---|
| Left rail | 240px | back/home/pin toolbar, Thread · N turns list, Sources list |
| Center | flex (max-width 680px inside) | title bar (GD-id · name + MetaStrip), scrollable Q+A stack, docked composer |
| Right | 280px | Evidence · active card (live anchor snippet), Cross-reference · N linked rows |

- Pane separators: 1px `--hairline`.
- Each pane independently scrolls.
- Collapses to 2-pane on tablet portrait (rail + center; evidence moves to a collapsible footer).

### IdentityStrip `F-07`

Phone and tablet home.

- 36–38px olive gradient tile with "S".
- "Senku" primary, "754 guides · manual ed. 2" mono caps subtitle.
- "Ready" / "Pack ready" pill (ok tone, 4px 8px padding, mono caps).
- Phone: horizontal strip.
- Tablet: left rail, stacked vertically with pack details card.

### SearchResult paper card `F-08`

- Paper bg, paper-ink text.
- Title 15px Inter Tight 600.
- Subtitle: `GD-xxx · CATEGORY` mono caps (ink at 0.55 opacity).
- Lane tag (Hybrid / Vector / Lexical / Cross-ref): mono caps 9px in a pill at right.
- Snippet 12.5px Inter Tight 400 at 0.75 opacity.

### SourceRow `F-09`

- bg-1 with hairline.
- Structure: `[GD-id accent mono]  |  Title (ellipsize)  Category · anchor  ›`.
- Anchor highlight: if source is the anchor guide, id color = accent, background tint at 10% accent.
- Replaces pipe-joined source labels.

### SuggestChip (Try next) `F-10`

- Oval (border-radius 999), bg-1 with hairline-strong border.
- Text 12px Inter Tight 500.
- Trailing arrow icon (11px).
- Candidates come from `B-03` graph-driven generator, not a static list.

### EvidenceSnippet + XRef `F-11`

Tablet evidence pane.

EvidenceSnippet card:
- `GD-xxx · anchor` mono caps (accent) + external-link icon.
- Title 13px Inter Tight 600.
- Section subtitle mono caps ink-3.
- Quoted snippet 12px serif with hairline top separator.

XRef row:
- bg-1, rounded 8, padding 7px 10px.
- `[GD-id ink-3 mono] Title (ellipsize) ›`.

### PivotRow + ThreadRow `F-12`

PivotRow (from home):
- bg-1 (or accent tint for primary), rounded 10, padding 8px 10px.
- `GD-xxx` mono caps + title.
- Primary variant: 10% accent bg, accent-colored id.

ThreadRow (continue list):
- 28×28 bg-2 circle with thread icon (accent).
- Question text 13px Inter Tight 500, ellipsize.
- `GD-xxx · kind · time` mono caps subtitle.
- Chevron right.

### DockedComposer `F-13`

Phone detail, always-visible bottom bar.

- Hairline top border.
- Pill input placeholder "Ask follow-up…" in ink-3.
- 38×38 circle send button with accent fill and arrow icon.

### CategoryShelf `F-14`

Phone: 2-col grid, each card has a 3px colored left border + name + mono caps count.
Tablet rail: 6 rows with 6px color dot + name + count-right.

Category colors (stable):
- Water `#7a9ab4`
- Shelter `#7a9a5a`
- Fire & energy `#c48a5a`
- Medicine `#b67a7a`
- Food `#9aa064`
- Signal `#7a9a9a`

### TweakPanel removal `F-15`

Tweak runtime toggles ship in the prototype. In production:
- Lock the defaults above.
- Remove the `tweaks` panel DOM and wiring.
- Remove the `__tweaks` React state hook.
- Keep the underlying tokens (paper vs dark, strip vs pill) as gated build variants — useful for OLED-save mode, but not user-facing.

## What Gets Superseded

Tasks in the existing opustasks.md that Rev 03 supersedes:

- `OPUS-B-04` telemetry tap-affordance — rolled into MetaStrip behavior (`F-02`). Close with note "folded into F-02".
- `OPUS-B-11` home visual hierarchy pass — entire home is rebuilt under F-07/F-12/F-14. Close with note "folded into F-07+F-12+F-14".
- Staged `B-13` (soften field-manual stamps) — redesign DROPS stamps, doesn't soften. Close superseded.
- Staged `B-15` (evidence-strength indicators concretize) — MetaStrip + tone dot carries this (F-02). Close superseded.
- Staged `B-17` (replace `>>` with chevron) — Rev 03 uses chevron Icon throughout. Close superseded.

What survives (still needed, re-applied to Rev 03 primitives):

- `OPUS-B-03` contextual follow-up chip candidates — feeds F-10 (`SuggestChip` needs graph data).
- `OPUS-B-06` continue-thread chips on results — integrates with F-08 (`SearchResult paper card`).
- `OPUS-B-07` phone-landscape composer focus — applied to F-13 docked composer in landscape.
- `OPUS-B-08` split provenance-open + source-graph a11y regions — now targets the 3-pane tablet (F-06).
- `OPUS-B-09` copy sanitizer — backend string scrub, independent of UI.
- `OPUS-B-10` layout-land overrides — now overrides Rev 03 layouts for landscape phone.
- `OPUS-B-12` Compose migration spike — elevated from spike to implicit commitment via F-lane.
- Staged `B-14` (tag casing ROLE_) → renumbered `B-14` in rev 3 backlog.
- Staged `B-18` (contrast audit) → renumbered `B-15` in rev 3 backlog (now audits Rev 03 palette).

## Validation Strategy

Per-primitive (during F-lane waves):
- Instrumentation screenshot-diff at all four postures (5556/5560/5554/5558).
- Unit test on the Compose primitive's preview parameters.

Per-wave (between waves):
- Fresh gallery pass capturing every redesigned surface.
- Diff against the pre-Rev-03 gallery; the diff IS the receipt.

End-of-migration (after Wave 8):
- Full emulator matrix sweep.
- External review with the new gallery.
- Confirm the Haiku-flagged issues are all closed (truncation, casing, evidence vagueness, `>>` notation, stamp legibility, contrast).

## Non-Goals (Rev 03 scope discipline)

These are tempting but NOT in Rev 03:

- Thread branching tree visualization — kept as "still open" on the prototype; do not implement until we have a branching data model.
- Voice-first Ask flow — separate initiative.
- Large-text / high-contrast accessibility mode — important, but needs its own pass; Rev 03 hits WCAG AA per `B-15` but doesn't ship a dedicated a11y mode.
- Any non-mobile platform target.

## Open Questions to Resolve During Implementation

1. **EvidenceSnippet source.** The prototype quotes a fixed string; production needs to pull the top-matched ~2 sentences from the anchor guide's chunk. Implementation detail for `F-11`.
2. **Tablet portrait layout.** The prototype shows tablet landscape only. Portrait should collapse to rail + center with evidence docked as footer; spec in `F-06`.
3. **Bottom tabs keyboard nav.** Not addressed by the prototype. Default: follow Material 3 tab bar keyboard contract.
4. **OLED-save mode.** Keep dark AnswerCard as a build variant behind a setting; do not expose in Rev 03 UI.

## Change Log

- **rev 03 · 2026-04-17** · initial lock, all five Tweaks decisions committed, primitive catalogue written, supersede list drafted.
