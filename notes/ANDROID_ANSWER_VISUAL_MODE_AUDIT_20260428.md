# Android Answer Visual Mode Audit - 2026-04-28

Scope: read-only audit of `artifacts/ui_state_pack/20260428_013056` answer screenshots/dumps and the current render paths in `DetailActivity`, `PaperAnswerCard`, and `TabletDetailScreen`.

## Artifact Baseline

- State pack: `artifacts/ui_state_pack/20260428_013056/summary.json`
- Result: `status=pass`, `47/47`, `platform_anr_count=0`, homogeneous APK SHA `25363347fe8556cb4595daed1a556f2b86720f1c2616abdbfdf8b91327dbd358`
- Pack: `senku-mobile-pack-v2`, `answer_cards=271`, generated `2026-04-27T04:21:12.533181+00:00`

Passing state assertions do not currently prove answer-mode visual separation. Several answer states still read as source/provenance or guide-document surfaces.

## Observed Visual Failure Modes

### 1. Tablet answer mode is still a three-pane evidence browser

Examples:

- `screenshots/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/tablet_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/tablet_landscape/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`

What shows:

- Left rail: `THREAD` and `SOURCES`
- Center pane: question plus answer text, with small document-like mono labels
- Right rail: `CROSS-REFERENCE`
- Footer composer

This is technically marked `ANSWER`, but visually it is still a provenance/source workbench. The answer is not the dominant object; the source graph is.

Relevant code:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java:1189-1221` always binds tablet Compose through `TabletDetailScreen`.
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java:1468-1529` builds one `TabletDetailState` for both guide and answer modes.
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt:348-399` always lays out `ThreadRail + DetailWorkspace`.
- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt:433-471` always adds the evidence pane in `DetailWorkspace`; answer mode only changes labels, not structure.

### 2. Tablet answer typography uses inline document conventions

Examples:

- `screenshots/tablet_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/tablet_landscape/deterministicAskNavigatesToDetailScreen__deterministic_detail.png`

What shows:

- `ANSWER THIS DEVICE 1 TURN`
- `A1 - SOURCES 3` or `A1 - ANCHOR`
- body rendered as a compact text block, not the existing answer-card shape
- proof CTA appears as small inline `Open proof`

Relevant code:

- `TabletDetailScreen.kt:647-683` renders all turns through `ThreadTurnList`.
- `TabletDetailScreen.kt:790-823` uses `ThreadTurnBlock` for answer mode and guide mode alike.
- `TabletDetailScreen.kt:828-893` renders the question with document-ish row labels.
- `TabletDetailScreen.kt:896-1072` renders `AnswerInlineBlock`; it does not reuse `PaperAnswerCard` and has no answer-first card container.

### 3. Phone XML answer path has an answer card, but the full-body mirror still creates source-document pressure

Examples:

- `screenshots/phone_portrait/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`
- `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png`

What shows:

- Phone portrait uses `PaperAnswerCard`, but the card body is the whole uncertainty/provenance explanation because parsing falls back to the full body.
- Phone landscape can land scrolled into a giant serif answer/provenance body with a side source panel, making it look like a document/source view rather than an answer card.

Relevant code:

- `DetailActivity.java:1851-1885` hides `detail_body_mirror_shell` in answer mode by shrinking it to `1dp` and alpha `0.01`, but still keeps it visible.
- `DetailActivity.java:1887-1918` renders `PaperAnswerCardHostView` for phone XML answer mode.
- `AnswerContent.kt:254-295` only splits structured labels `Short answer:`, `Steps:`, and `Limits or safety:`. Unstructured uncertainty bodies remain one large `short` value.
- `DetailActivity.java:2300-2374` still shows the sources/provenance panel for non-emergency answer states with sources.

### 4. Emergency/reviewed-card answer mode already has the desired answer-first direction

Examples:

- `screenshots/phone_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- `screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`

What works:

- The emergency card is visually dominant.
- Immediate actions are separated from route/proof.
- Source material is secondary.

This is the best local model for the next slices: do not copy the exact emergency styling to routine answers, but copy the hierarchy: answer first, proof/source second.

## Root Cause

Answer mode has data labels but not a distinct tablet answer layout. Tablet Compose uses one shared thread/source/evidence structure for both guide and answer modes, and answer mode only flips text labels. Phone XML has a distinct `PaperAnswerCard`, but unstructured generated/uncertain bodies and always-present source/provenance panels still let source-document content dominate.

## Recommended Implementation Slices

### Slice 1 - Add a real tablet answer-first center surface

Files:

- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- tests likely in `android-app/app/src/test/java/com/senku/ui/tablet/TabletDetailScreenTest.kt` or existing tablet UI policy tests if present

Change:

- In `CenterPane`, branch non-guide answer mode to a new composable, for example `AnswerReadingSurface`.
- Keep `GuidePaperSurface` only for `guideMode`.
- `AnswerReadingSurface` should use the active `ThreadTurnState.answer` as the primary card-like object and make prior turns secondary.
- Reuse `PaperAnswerCard` if possible, or extract a shared answer-card composable variant so tablet and phone answer surfaces share labels, footer, evidence tone, and parsing behavior.

Acceptance target:

- Tablet generated/deterministic screenshots should no longer look like a centered document page or inline evidence transcript.
- The first viewport should read as an answer card with proof/source controls, not as a source graph.

### Slice 2 - Gate the tablet evidence pane for answer mode

Files:

- `android-app/app/src/main/java/com/senku/ui/tablet/TabletDetailScreen.kt`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java` only if state needs a new flag

Change:

- In `DetailWorkspace`, show `EvidencePane` by default for guide mode.
- For answer mode, collapse evidence into an explicit proof action unless `state.evidenceExpanded` is true or a source was explicitly selected.
- Rename the answer-mode rail copy away from `CROSS-REFERENCE` when shown, for example `PROOF SOURCES`, while keeping guide mode as `CROSS-REFERENCE`.

Acceptance target:

- `generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail` on tablet should not show the right `CROSS-REFERENCE` rail by default.
- Source-selection states may still show an anchored proof rail, but it should read as proof for the answer, not as navigation into a guide graph.

### Slice 3 - Fix phone landscape answer scroll and mirror behavior

Files:

- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `android-app/app/src/main/res/layout-land/activity_detail.xml`

Change:

- In answer mode, set `detail_body_mirror_shell` to `View.GONE` instead of keeping a `1dp` visible/focusable mirror, unless a harness-only accessibility reason requires it.
- Ensure answer-mode render posts `detailScroll.scrollTo(0, 0)` for phone landscape after `renderAnswerCardSurface`, similar to the emergency portrait reset at `DetailActivity.java:1177-1184`.
- Consider making landscape answer mode use the same `PaperAnswerCard`-first hierarchy as portrait before inline/provenance panels.

Acceptance target:

- `screenshots/phone_landscape/generativeAskWithHostInferenceNavigatesToDetailScreen__generative_detail.png` should open at the answer header/card, not mid-body with a side source panel dominating the viewport.

### Slice 4 - Add answer-body parsing for uncertainty/provenance-style generated bodies

Files:

- `android-app/app/src/main/java/com/senku/ui/answer/AnswerContent.kt`
- `android-app/app/src/test/java/com/senku/ui/answer/PaperAnswerCardLabelTest.kt` or a new `AnswerContentTest.kt`

Change:

- Teach `parseStructured` or a new fallback parser to split common unstructured uncertainty bodies:
  - first paragraph: short answer
  - `Possibly relevant guides in the library:` block: proof/support, not primary answer body
  - `Try:` block: limits or next step
- Keep safety-critical deterministic/reviewed bodies unchanged.

Acceptance target:

- Phone portrait generated uncertainty card should not render a giant full provenance paragraph as the primary `short` answer.
- Tablet answer card from Slice 1 gets a compact short/limits split automatically.

### Slice 5 - Add visual contract assertions for answer-mode separation

Files:

- `android-app/app/src/androidTest/java/.../PromptHarnessSmokeTest.kt` or the state-pack test owner that captures these screenshots
- state-pack validator script if visual dump assertions live outside Android

Change:

- Add text/dump assertions for generated/deterministic answer states:
  - answer-mode default tablet screenshot must not expose `FIELD MANUAL`.
  - answer-mode default tablet screenshot should not expose `CROSS-REFERENCE` unless the state name is explicitly a source-selection/proof state.
  - generated uncertainty answer should expose a compact answer label and not render all `Possibly relevant guides...` lines as the first visible body block.

Acceptance target:

- Future green state packs fail if answer mode regresses into a guide/source document surface.

## Priority Order

1. Slice 1: tablet answer-first surface.
2. Slice 2: collapse/rename tablet answer evidence rail.
3. Slice 3: phone landscape scroll/mirror cleanup.
4. Slice 4: uncertainty body parser cleanup.
5. Slice 5: assertions after UI shape stabilizes.
