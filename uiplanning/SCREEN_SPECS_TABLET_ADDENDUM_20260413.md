# Senku Mobile Screen Specs - Tablet Addendum - 2026-04-13

This addendum captures posture-specific tablet guidance discovered during live emulator validation.

It supplements `SCREEN_SPECS.md` without rewriting the older file in-place.

## Screen 1: Home / Search

### Wide Tablet ("Station")

Intent:

- concurrent browse + search + result scanning
- pack/session context remains visible while working

Current validated direction:

- left rail for pack state, session memory, and developer tools
- right work surface for search/actions, category browse, and results

Guardrails:

- do not over-compress category cards inside a split pane
- if pane width is constrained, favor readability over a forced extra column

### Tall Tablet ("Clipboard")

Intent:

- calm reading and direct interaction
- less chrome competition

Current validated direction:

- simpler single-column fallback
- closer to a large field manual than a dashboard

## Screen 2: Results / Browse

### Wide Tablet

Intent:

- help the user decide before tapping
- make the list feel like a working console, not a stretched phone list

Current validated direction:

- richer large-screen result card layout in `res/layout-sw600dp/list_item_result.xml`
- adapter allows more title/meta/snippet density on `sw600dp`

Future opportunities:

- material icons or extracted item hints
- stronger section previewing
- better differentiation between guide families before tap

### Tall Tablet

Intent:

- keep the list readable and touch-friendly
- preserve the "manual" feeling instead of overloading the screen

## Screen 3: Answer Thread / Detail

### Wide Tablet

Intent:

- evolve toward a station-style detail view
- use side space for support context instead of dead margin

Near-term target:

- posture-specific `activity_detail.xml` in `layout-sw600dp-land`
- likely side rail candidates:
  - sources
  - materials
  - next steps
  - session/thread context

Current landed direction:

- `res/layout-sw600dp-land/activity_detail.xml` is now live
- active question + answer remain in the left reading column
- utility rail is now the place for materials and source inspection
- session context is currently deprioritized in wide mode so the rail can stay focused on actionable support panels
- inline utility chips are intentionally hidden from the left answer surface in wide mode
- validated on-device through `rotation=0` posture sweep

### Tall Tablet

Intent:

- deep-focus reading
- cleaner, centered instructional flow

Near-term target:

- posture-specific `activity_detail.xml` in `layout-sw600dp-port`
- centered reading column
- calmer margins
- optional sticky context in the gutter rather than more inline clutter

Current landed direction:

- `res/layout-sw600dp-port/activity_detail.xml` is now live
- active answer content is centered into a clipboard-style reading column
- materials, source chips, and next-step chips remain inline above the answer body
- lower support surfaces stay below the reading flow instead of moving into a side rail
- validated on-device through `rotation=1` posture sweep

## Navigation Semantics

Current behavior:

- `DetailActivity` is still a separate activity
- `Back` exits the thread and returns to dashboard/results

Interpretation:

- acceptable for now because the app does not yet have a true embedded master-detail controller
- keep this behavior stable while posture-specific detail layouts are proven

## Resource Mapping

Current tablet resource split:

- `res/layout-sw600dp-land/activity_main.xml`
- `res/layout-sw600dp-port/activity_main.xml`
- `res/layout-sw600dp/list_item_result.xml`

Recommended next additions:

- `res/layout-sw600dp-land/activity_detail.xml`
- `res/layout-sw600dp-port/activity_detail.xml`

## Validation Notes

Validated on `Senku_Tablet` (`emulator-5556`):

- `rotation=0` = wide station mode on this image
- `rotation=1` = tall clipboard mode on this image

This orientation reality should be treated as part of the practical test plan, not assumed from naming alone.
