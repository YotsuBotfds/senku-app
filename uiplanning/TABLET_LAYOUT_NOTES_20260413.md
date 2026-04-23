# Senku Tablet Layout Notes - 2026-04-13

This note captures the validated tablet-direction work so future UI passes can continue without replaying the same experiments.

## Core Model

Treat tablet layouts as two different tools, not one stretched phone UI.

- `Station`: wide tablet mode focused on concurrency
- `Clipboard`: tall tablet mode focused on deep reading

## Validated Orientation Reality

On the current `Senku_Tablet` emulator:

- `rotation=0` behaves as the wide station mode
- `rotation=1` behaves as the tall clipboard mode

This means the practical resource split is:

- `res/layout-sw600dp-land/activity_main.xml` = station
- `res/layout-sw600dp-port/activity_main.xml` = clipboard fallback

## Home Screen Direction

### Station

Current validated direction:

- left rail for pack state, session context, and developer tools
- right work surface for search/actions, category browse, and results

What worked:

- splitting browse and results into separate right-side zones
- slimming the left rail to reclaim width for the work surface
- richer result cards on large screens

What failed:

- forcing an over-compressed category layout in the split pane
- a too-narrow browse pane creates unreadable "totem pole" category cards

Guideline:

- if browse-pane width is constrained, favor fewer columns and readable labels over aggressive density

### Clipboard

Current validated direction:

- use the simpler single-column fallback instead of forcing the station split
- preserve the calmer field-manual feel
- keep touch targets large and text flow readable

Future improvement:

- center the reading column more intentionally and use gutters for light context surfaces instead of extra chrome

## Result Card Direction

Tablet results should earn their extra width.

Current rendering lane:

- `res/layout-sw600dp/list_item_result.xml`
- tablet-aware density in `SearchResultAdapter`

Current goal:

- show more useful guide substance before tap
- make the list feel closer to a command-console scan surface than a stretched phone list

## Thread / Detail Direction

### Current Behavior

- `DetailActivity` is still a separate activity
- `Back` exits the thread and returns to the dashboard/results screen
- true in-place master-detail is not implemented yet

### Station Target

The next high-value tablet pass should make wide detail feel like a station:

- use side space for sources, materials, next steps, or session context
- reduce dead whitespace
- keep the answer readable while turning secondary context into a useful rail

Current implementation progress:

- `res/layout-sw600dp-land/activity_detail.xml` now exists as the first live station-detail pass
- the left column is now treated as the reading surface, not a dumping ground for every utility chip
- materials have been promoted into the right rail
- source inspection remains in the right rail
- next-step actions are being promoted ahead of lower-value session context
- session history is currently suppressed in station mode because it competed with more useful survival surfaces

### Clipboard Target

Tall detail should feel like a smart clipboard:

- centered reading column
- quieter margins
- fewer competing surfaces
- optional sticky context in the gutter instead of inline clutter

Current implementation progress:

- `res/layout-sw600dp-port/activity_detail.xml` now exists
- portrait tablet detail now follows a centered reading-column model instead of inheriting the wide station split
- lower support panels remain below the reading flow for now, which keeps the primary instructions visually dominant

## Back Behavior Note

Current behavior is simple and predictable:

- open guide/answer detail
- press `Back`
- return to the prior dashboard/results screen

That is acceptable for now while tablet structure is still being proven.

If a true station controller is added later, revisit whether `Back` should:

- close the right-side detail pane first
- or preserve the current activity-style behavior

## Immediate Next Recommendations

1. Build `layout-sw600dp-land/activity_detail.xml` as the real station-detail pass.
2. Build `layout-sw600dp-port/activity_detail.xml` as the clipboard-detail pass.
3. Keep home/dashboard improvements incremental until detail has the same orientation-aware treatment.
