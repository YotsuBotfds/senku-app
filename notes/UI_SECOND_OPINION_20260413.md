# UI Second Opinion - 2026-04-13

This note captures a product/IA second opinion on the current Android direction.

Read this with:

- [`ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md)
- [`ANDROID_SCOUT_FINDINGS_20260412.md`](./ANDROID_SCOUT_FINDINGS_20260412.md)
- [`../android-app/app/src/main/res/layout/activity_main.xml`](../android-app/app/src/main/res/layout/activity_main.xml)
- [`../android-app/app/src/main/res/layout/activity_detail.xml`](../android-app/app/src/main/res/layout/activity_detail.xml)
- [`../android-app/app/src/main/java/com/senku/mobile/MainActivity.java`](../android-app/app/src/main/java/com/senku/mobile/MainActivity.java)
- [`../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`](../android-app/app/src/main/java/com/senku/mobile/DetailActivity.java)

## Main Read

The current Android direction is substantially better than a plain search app. It is becoming a real knowledge hub with:

- a browse-and-ask split on the home surface
- anchored answer threads instead of one-off result pages
- visible source navigation
- follow-up continuity on the detail screen

That is the right product direction for Senku.

The main caution is that the UI must not hide the app's actual intellectual work:

- route family selection
- anchor-guide choice
- supporting-source mix
- thread continuity

If the app gets prettier while those cues become less visible, trust will go down even if retrieval quality improves.

## Intended Product Shape

The docs and current implementation both point to the same target:

- not a generic chatbot
- not just a document browser
- a retrieval-and-trace-first survival assistant

The strongest underlying shape is:

1. home screen as entry hub into the knowledge space
2. results as guide-family triage, not just a ranked list
3. detail as transcript-first answer thread with provenance
4. guide drill-down as explicit evidence, not hidden implementation detail

## What Is Working

- The home screen is starting to feel like a real knowledge hub rather than a debug launcher.
- Category tiles fit the corpus well and help users enter by domain when they do not yet know exact query wording.
- The answer detail screen is correctly moving toward a threaded advisory surface.
- Source chips and anchor cues are the right trust primitives for this app.
- Follow-up inside the detail screen is much more appropriate than bouncing users back out to a separate search flow.

## Main Risks

### 1. Entry-Point Intent Blur

`activity_main.xml` and `MainActivity.java` still place:

- search
- ask
- browse
- session state
- pack/model/developer controls

in one vertical surface.

That is powerful, but it can make the primary user action feel fuzzy.

The current hint/action wording still risks conflating:

- `find the right guide family`
- `generate an answer from guides`

This matters because Senku is built around routing quality, not generic prompt entry.

### 2. Provenance Can Feel Conditional

`DetailActivity` has the right raw ingredients:

- anchor chip
- route/mode signals
- inline sources
- source buttons
- session thread summary

But several of those cues appear or disappear by mode or layout branch. That makes provenance feel like a secondary diagnostic instead of part of the answer contract.

The user should never have to wonder:

- what family did this route through?
- what guide is leading?
- is this deterministic, generated, or just a source browse?

### 3. Detail Screen Overload

The detail surface currently supports many useful rails:

- hero/status
- question bubble
- answer bubble
- session panel
- inline sources
- inline next steps
- materials
- full sources panel
- follow-up composer

This is directionally correct, but it is easy for the screen to feel "busy-smart" rather than obviously legible.

The risk is not too much information. The risk is weak hierarchy.

### 4. Heuristic Suggestions Need Stronger Separation From Evidence

`DetailActivity.buildRelatedPaths()` and `buildMaterialsChecklist()` are useful product ideas, but they are not the same thing as explicit retrieved evidence.

If they look too similar to provenance-backed items, the app can accidentally blur:

- sourced guide support
- generated convenience suggestions

That distinction matters a lot in a survival context.

## IA Rules Worth Preserving

These should remain true even if the visuals keep changing:

1. Every answer needs a stable answer spine:
   route family, anchor guide, and source count should remain visible without scrolling deep.
2. Thread continuity should be explicit:
   users should always know whether a follow-up continues the current context or starts fresh.
3. Source navigation should be first-class:
   opening the source guide is part of the core workflow, not an advanced feature.
4. Generated helpers should be labeled as helpers:
   next-step chips and materials extraction should not visually impersonate direct citations.
5. The home screen should privilege user intent first:
   browse, find, and ask should feel like deliberate lanes, not one blended command box with extra buttons.

## Recommended Next Moves

### Do Next

- Keep the current knowledge-hub visual direction.
- Keep the detail screen as the core answer surface.
- Preserve explicit source chips, anchor ID, and follow-up continuity.

### Improve Soon

- Make the answer provenance strip stable in answer mode:
  route type + anchor + source count together.
- Clarify entry intent on the home screen:
  `Find guides` versus `Ask from guides`.
- Collapse secondary rails by default on detail:
  keep sources easy to reach, but demote materials/next-steps behind lighter affordances.
- Add one compact `Why this answer` affordance:
  route family, explicit topic hints, and leading source sections.

### Avoid

- Hiding route/anchor/source cues in the name of polish.
- Making the app look like a generic LLM chat shell.
- Treating heuristic helpers as if they are evidence.
- Letting developer controls compete visually with the main user task on the home surface.

## Product Conclusion

The current UI direction is good because it reflects the real architecture of the project:

- route into the right family
- anchor on the right guide
- show the evidence
- keep the thread coherent

That is the product.

The next UI moves should make that architecture easier to read, not flatter or more mysterious.
