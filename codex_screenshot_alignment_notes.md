# Codex notes: align `screenshots.zip` to the Senku mock ZIP

I compared the current generated `screenshots.zip` against the mock/reference ZIP. Treat the mock ZIP as canonical.

## Main goal
Update the screenshot tests, fixture data, and responsive UI states so the generated ZIP visually matches the reference mock ZIP, not just the app's generic interaction states.

## 1. Output inventory and packaging

Current output has 56 PNGs under nested folders such as `screenshots/phone_portrait/...`; the mock ZIP has 22 canonical PNGs in a flat `mocks/` folder.

Generate exactly these canonical families:

- `home-{phone,tablet}-{portrait,landscape}.png`
- `search-{phone,tablet}-{portrait,landscape}.png`
- `thread-{phone,tablet}-{portrait,landscape}.png`
- `guide-{phone,tablet}-{portrait,landscape}.png`
- `answer-{phone,tablet}-{portrait,landscape}.png`
- `emergency-phone-portrait.png`
- `emergency-tablet-portrait.png`

Do not include intermediate/state-debug screenshots such as `answer_source_graph_*`, `answer_provenance_*`, `deterministic_detail`, `guide_cross_reference_*`, or `browse_linked_handoff` in the goal ZIP.

## 2. Global visual frame

The mocks use a custom, deterministic app/device frame. Current screenshots show live OS chrome and live times.

Implement a `goalScreenshotMode` or equivalent screenshot-only wrapper:

- Fixed status time: `4:21`.
- Right status text: `OFFLINE` plus battery outline.
- Rounded outer device corners on phone/tablet mock outputs.
- Hide live status icons such as the shield icon, Wi-Fi/cell icons, live battery color, and changing time.
- Use the mock's dark green-black background and subtle 1px border lines.
- Match the mock typography hierarchy:
  - Small metadata/section labels: uppercase, monospaced, wide letter spacing.
  - Main titles: bold sans-serif.
  - Long answer/guide body copy: serif.

## 3. Shared header/navigation component

Replace the current large centered headers with the compact breadcrumb-style mock header.

Target pattern:

- Left: back chevron, thin vertical divider, uppercase screen label, guide/thread ID, bullet, title.
- Right: only the screen-specific icons from the mock.
- Examples:
  - Home: `HOME SENKU • Field manual • ed.2`, plus search and overflow icons.
  - Search: `SEARCH 'rain shelter'`, mostly overflow only.
  - Thread: `THREAD GD-220 • Rain shelter • 2 turns`, with home/share icons.
  - Guide: `GUIDE GD-132 • Foundry & Metal Ca...`, with home/pin/overflow as mocked.
  - Answer: `ANSWER GD-345 • Rain shelter`, with home/share icons.
  - Emergency: `ANSWER GD-132 • Burn haz...`, with a `• DANGER` pill.

Current screenshots often show huge centered titles, a second ID line, or left icon stacks. Those should be replaced in the goal screenshot states.

## 4. Responsive layout breakpoints

Landscape and tablet mocks are not just wider versions of phone portrait. They use persistent rails and multi-column layouts.

Implement these goal layouts:

- Phone portrait: full-width content, bottom tabs for Library/Ask/Saved where applicable, sticky composer on answer/thread/emergency screens.
- Phone landscape: left vertical nav rail is visible; answer/search/guide screens split into content plus source/preview/section rails.
- Tablet portrait: left nav rail plus centered content column; where mocked, show adjacent source/recent/guide rail.
- Tablet landscape: left nav rail, secondary left context rail, central content column, and right source/preview rail.

Current phone-landscape guide/answer screenshots are especially far off because they lack the mock's left rail and use the wrong split layout.

## 5. Screen-specific fixes

### Home

Target content:

- Hero title on tablet: `Field manual`.
- Subtitle: `754 guides • 12 categories • ready offline • ed. 2`.
- Pack row: `PACK READY` with green dot and right metadata `754 GUIDES • ED.2 • 184 MB`.
- Search placeholder: `Search guide titles, IDs, field notes...`.
- Section label: `— CATEGORIES • 12`.
- Category cards:
  - Shelter — 84 guides
  - Water — 67 guides
  - Fire — 52 guides
  - Food — 91 guides
  - Medicine — 73 guides
  - Tools — 119 guides
- Recent threads:
  - `How do I build a simple rain shelter...` — `GD-345 • 04:21 • UNSURE`
  - `Best tinder when materials are wet` — `GD-027 • 04:08 • CONFIDENT`
  - `Boil water without a fire-safe pot` — `GD-094 • YESTERDAY • CONFIDENT`

Current home is close structurally, but the text case, row metadata, section labels, card spacing, and header style do not match.

### Search

Target content:

- Query row should be flat/minimal, not a rounded home-style search pill.
- Query text should read `rain shelter`, not `Search rain shelter`.
- Right metadata: `4 RESULTS • 12MS`.
- Results, scores, and ordering:
  - `GD-023` — `Survival Basics & First 72 Hours` — score `92`
  - `GD-027` — `Primitive Technology & Stone Age` — score `78`
  - `GD-345` — `Tarp & Cord Shelters` — score `74`
  - `GD-294` — `Cave Shelter Systems & Cold-Weather` — score `61`

Tablet/landscape search must include the filter column and the preview pane for `GD-023`, as shown in the mocks.

### Thread

Use exact deterministic thread fixture data.

Header: `THREAD GD-220 • Rain shelter • 2 turns`.

Q1:

- Meta: `Q1 • 04:21 • FIELD QUESTION`
- Question: `How do I build a simple rain shelter from tarp and cord?`
- Answer meta: `A1 • 04:21 • ANCHOR GD-220` with `• UNSURE`
- Answer text: `Build a ridgeline first, then drape and tension the tarp around it. Pitch ridge along prevailing wind.`
- Chips: `GD-220`, `GD-132`

Q2:

- Meta: `Q2 • 04:23 • FIELD QUESTION`
- Question: `What should I do next after the ridge line is up?`
- Answer meta: `A2 • 04:23 • ANCHOR GD-345` with `• CONFIDENT`
- Answer text: `Drape the tarp evenly across the ridge with both edges hanging the same length. Tension the four corners with taut-line hitches; aim for the windward edge to sit closest to the ground.`
- Chip: `GD-345`

Current screenshots incorrectly repeat the A1 source/status on A2 and use a shorter A2 answer. Fix the fixture before tuning layout.

Footer/composer:

- Context label: `THREAD CONTEXT • 2 TURNS • GD-220 ANCHOR`
- Placeholder: `Ask a follow-up...`

### Guide

Target guide is `GD-132 • Foundry & Metal Casting`.

Header/body:

- Metadata: `FIELD MANUAL • REV 04-27 • PK 2`
- Title: `Foundry & Metal Casting`
- Submeta: `GD-132 • 17 SECTIONS • OPENED FROM GD-220` where shown.
- Danger block title: `DANGER • EXTREME BURN HAZARD`
- Danger block text: `A single drop of water contacting molten metal causes a violent steam explosion, spraying molten metal 3+ meters in all directions. Every tool, mold, crucible, and surface that contacts molten metal must be completely dry.`
- Section: `— § 1 • AREA READINESS`
- Heading: `Reviewed Answer-Card Boundary`
- Body copy should match the mock.
- Required reading rows:
  - `GD-220 Abrasives Manufacturing`
  - `GD-499 Bellows & Forge Blower Construction`
  - `GD-225 Bloomery Furnace Construction`

Do not show the large cross-reference/dashed drawer on the phone portrait guide mock. The target guide portrait is just the guide page plus bottom tabs.

Landscape guide must include the left section rail:

- `§1 Area readiness` selected
- `§2 Required reading`
- `§3 Hazard screen`
- `§4 Material labeling`
- `§5 No-go triggers`
- `§6 Access control`
- `§7 Owner handoff`

### Answer

Target answer is `GD-345 • Rain shelter`.

Top content:

- Meta row: `ANSWER • THIS DEVICE • 1 TURN` with `• UNSURE` at the right.
- Question: `How do I build a simple rain shelter from tarp and cord?`
- Submeta: `GD-345 • 3 SOURCES • REV 04-27 04:21`.

Answer body should be the mock's two-paragraph prose, not the current shorter answer with numbered steps:

- Paragraph 1 starts: `Build a ridgeline first, then drape and tension the tarp around it...`
- Paragraph 2 starts: `Pitch the tarp ridge along the prevailing wind...`

Callout:

- Label: `UNSURE FIT`
- Text: `Senku found 3 guides that may apply but no single guide is a confident anchor. Treat this as guidance, not procedure. See sources below.`

Sources must be rendered as the actual three source cards, not generic collapsed controls:

- `GD-220 • ANCHOR` — `74%` — `Abrasives Manufacturing`
- `GD-132 • RELATED` — `68%` — `Foundry & Metal Casting`
- `GD-345 • TOPIC` — `61%` — `Tarp & Cord Shelters`

Related guides list:

- `GD-294 Cave Shelter Systems & Cold-Weather`
- `GD-695 Hurricane & Severe Storm Sheltering`
- `GD-484 Insulation Materials & Cold-Soak`
- `GD-027 Primitive Technology & Stone Age`

Footer/composer:

- Phone portrait: `GD-345 • THIS DEVICE • CONTEXT KEPT`
- Landscape/tablet: `GD-345 • CONTEXT KEPT • 3 SOURCES VISIBLE`
- Placeholder: `Ask a follow-up about this answer...`

### Emergency

Target emergency answer is `GD-132 • Burn haz...`.

Header and banner:

- Header includes a small `• DANGER` pill.
- Orange top separator line under the app bar.
- Banner label: `• DANGER • EXTREME BURN HAZARD`.
- Main instruction: `Stop work immediately. Move to minimum 5 m from active work zone. Confirm two paths of egress.`
- Underline/highlight `minimum 5 m from active work zone` in orange.

Immediate actions:

1. `Stop all hot work` — `No new charges, no new pours.`
2. `Clear the floor to 5 m radius` — `Move personnel upwind.`
3. `Confirm two paths of egress` — `Door and roll-up open and unobstructed.`
4. `Notify the area owner` — `GD-132 lists current owner.`

Why card:

- Section label: `— WHY THIS ANSWER`
- Card: `GD-132 • ANCHOR` — `93%`
- Title: `Foundry & Metal Casting · §1 Area readiness`
- Quote starts: `A single drop of water contacting molten metal causes a violent steam explosion...`

Footer/composer:

- Context label: `EMERGENCY CONTEXT • GD-132 ANCHOR`
- Placeholder: `Ask about safe re-entry...`

Current emergency is one of the closer screens, but it still uses the wrong header, a collapsed/rounded why card, wrong score, slightly different action subtitles, and the wrong footer context.

## 6. Acceptance checklist

Before re-zipping, verify:

- ZIP contains exactly 22 PNGs matching the mock filenames.
- No live timestamps; all goal screenshots use deterministic `4:21` / `04:21` fixture timing.
- No live OS status icons or shield icon.
- Header styling matches the compact mock header across all screens.
- Search row is not reused from the home search component.
- Answer sources are expanded as cards, not collapsed `Show/Hide` controls.
- Guide phone portrait does not include the cross-reference drawer.
- Thread A2 uses `GD-345`, `CONFIDENT`, and the long drape/tension answer.
- Emergency why card uses `93%`, not `61%`.
- Phone landscape and tablet screenshots use the mock's rail/split-pane layouts.
