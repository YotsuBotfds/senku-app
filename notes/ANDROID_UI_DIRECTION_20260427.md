# Android UI Direction Roadmap - 2026-04-27

Durable direction note distilled from the downloaded UI review
`C:\Users\tateb\Downloads\Senku Android UI Direction Review.md`, reconciled
with the current Android evidence lane. This is product/UI direction only. It
does not approve reviewed-card runtime exposure, new card coverage, retrieval
changes, or acceptance shortcuts.

## North Star

Senku Android should feel like a field manual first, an assistant second.

- Field manual first: fast scan, source anchored, readable under stress,
  posture-aware, and physically utilitarian rather than consumer-chat polished.
- Assistant second: ask/follow-up exists to route into guide-backed knowledge,
  not to make the app feel like a generic chatbot.
- Emergency distinct: urgent medical/safety surfaces need an unmistakable
  emergency posture that is visually different from routine guide browsing,
  routine planning, and non-urgent assistant answers.
- Trust contract: every answer surface must keep route/mode, anchor guide,
  evidence strength, source count, and follow-up continuity visible or one tap
  away.
- Evidence hierarchy: citations, source previews, reviewed evidence labels,
  heuristic helpers, and generated suggestions must not look interchangeable.

## Product Decision

The full review does not recommend polishing the current shell as-is. It says
the UI is structurally over-combined: home, search, ask, threads, pins,
developer controls, guide reading, answer detail, evidence, provenance,
cross-references, and follow-up are all competing inside surfaces that read
like an internal harness wearing product clothes.

The medium-term product model should be:

- `Library`: browse/search/open/save canonical guides.
- `Ask`: ask a question, receive one primary answer, inspect evidence, and
  continue the thread if needed.
- `Emergency`: distinct reviewed/safety-card presentation when the support and
  product policy justify that lane.

Search is a capability inside Library and Ask, not a destination by itself.
Threads belong inside Ask history. Pins become Saved or Library affordances.
Retrieval modes, runtime toggles, and proof/debug labels must stop acting as
first-class user-facing information architecture.

## Diagnosis

The reviewed direction is broadly right: the app should become a guide-first
survival knowledge tool, not a plain search app or generic chat shell. The
current home Browse/Ask split, source chips, answer thread, tablet evidence
rail, and fixed four-posture validation are useful foundations, but they are
not a coherent product architecture yet.

Still-real risks:

- The visual system can still read as standard Android dark UI unless the
  olive/parchment field-tool materiality is applied consistently and quietly.
- Entry intent can blur when Browse, Ask, search state, session state, and
  developer/model controls compete on the same surface.
- The current five-tab phone model is too implementation-shaped. Library,
  Ask, and Saved should become the visible mental model before any larger
  navigation refactor.
- Answer screens can become busy-smart: many useful panels exist, but hierarchy
  must keep the answer body, source anchor, and trust spine dominant.
- Emergency answers need more than stronger copy. They need a distinct visual
  lane so routine guides, assistant output, and immediate-danger guidance are
  never mistaken for one another.
- Generated helpers and convenience suggestions must stay visually subordinate
  to source-backed proof.

## Phased Roadmap

### Phase 1 - Non-Destructive Product Review Cleanup

Goal: remove the most obvious product-review noise without changing data flows,
runtime policy, navigation state, or acceptance semantics.

- Hide developer/lab-bench controls from product review mode by default while
  preserving an explicit developer opt-out path.
- Replace user-visible retrieval jargon such as Hybrid, Lexical, Vector, and
  Cross-ref with user-value labels such as Best match, Keyword match, Concept
  match, and Related guide.
- Record a task-based gallery plan so UI quality is reviewed by product jobs,
  not only by state-pack pass/fail.
- Keep answer and guide behavior stable while establishing the Library / Ask /
  Saved direction in tests and notes.

### Phase 2 - Navigation and Shell Simplification

Goal: move toward one visible information architecture across phone and tablet.

- Collapse phone navigation toward Library / Ask / Saved without renaming
  internal enum constants in the first pass.
- Keep search as a local control inside Library and Ask.
- Move threads into Ask history and pins into Saved/Library surfaces.
- Start splitting home shell responsibilities out of `MainActivity` only after
  the label/order seams are tested.

### Phase 3 - Trust Spine and Evidence Hierarchy

Goal: make provenance feel permanent, not conditional.

- Preserve a stable compact trust spine across phone portrait, phone landscape,
  tablet portrait, and tablet landscape.
- Keep `Why this answer` available but subordinate: collapsed by default on
  compact screens, visually anchored enough to be discoverable.
- Keep citations and source previews first-class; do not bury them behind
  generic helper panels.
- Label generated inference, deterministic guide/manual content, reviewed
  evidence, and no-citation states distinctly.
- Ensure helper chips, materials lists, and next-step suggestions never share
  the same visual weight as citations or reviewed-card proof.

### Phase 4 - Guide/Answer Detail Separation

Goal: stop guide pages and answer pages from masquerading as the same detail
surface.

- Define separate display models for canonical guide reading and ask answers.
- Make guide detail read like an article with title, guide ID, section anchor,
  table/section affordance, related guides, and "Ask about this guide".
- Make answer detail read like one primary answer, evidence, related guides or
  next steps, and follow-up.
- Keep the tablet pane direction, but repurpose the right pane toward
  evidence/related context rather than proof overload.

### Phase 5 - Emergency-Distinct Surface

Goal: create a recognizable emergency lane before any broader product exposure.

- Define emergency visual grammar: restrained high-contrast alert treatment,
  clear escalation/handoff area, and minimal decorative materiality.
- Keep emergency surfaces anchored to source/review status; no unsupported
  emergency styling based only on generated language.
- Prove negative controls: routine wound care, generic medical education,
  planning guides, and non-emergency comparison prompts must not receive the
  emergency lane.
- Validate on all four fixed postures before treating emergency direction as
  accepted product UI.

### Phase 6 - Tablet Station and Phone Stress Reading

Goal: optimize posture-specific reading without layout churn.

- Tablet landscape: keep true station behavior with reading column plus durable
  evidence/source rail; avoid full-width text lines.
- Tablet portrait: keep clipboard reading calm; consider phone-style collapsed
  support drawers when lower panels crowd the reading flow.
- Phone portrait: answer body should appear early; collapse stale status/hero
  chrome after the answer is ready.
- Phone landscape: prioritize vertical density, compact composer, compact trust
  row, and suppressed nonessential helper copy.

## Implementation Targets

Use these as likely targets for future code slices, not as permission to edit
them from this note:

- `android-app/app/src/main/res/values/colors.xml`: field palette, alert tones,
  text contrast tokens.
- `android-app/app/src/main/res/drawable/bg_main_screen.xml`,
  `bg_hero_panel.xml`, `bg_surface_panel.xml`, `bg_search_shell.xml`,
  `bg_result_card.xml`: quiet materiality and surface hierarchy.
- `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`: home
  Browse/Ask emphasis, search-result scan behavior, developer-control
  subordination.
- `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`:
  markdown cleanup, query emphasis, result metadata density.
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`: answer
  chunking, hero/status collapse, trust spine, proof/source hierarchy,
  emergency-lane visual state.
- Detail/main layout qualifiers under `android-app/app/src/main/res/layout*`:
  only when Java/resource tuning cannot preserve posture behavior.

## Validation and Gallery Checklist

For any UI-direction slice, capture proof separately from RAG/runtime proof.

- Run or cite the fixed four-posture screenshot/dump lane when behavior changes:
  `5556` phone portrait, `5560` phone landscape, `5554` tablet portrait,
  `5558` tablet landscape.
- Include home, search results, deterministic/detail, generated/detail,
  follow-up, source preview/proof, no-citation or weak-citation state when
  touched.
- For emergency styling, include at least one true emergency prompt and at least
  two negative controls that must stay routine/non-emergency.
- Check text fit, clipping, contrast, source visibility, answer-body fold
  position, keyboard-obscured states, and landscape density.
- Record artifact paths and summary counts. Do not call `-PlanOnly`, `-DryRun`,
  `-WhatIf`, metadata-only, validator-only, pack-only, or FTS fallback evidence
  UI acceptance.
- Preserve current evidence taxonomy: true UI acceptance is the fixed
  four-emulator state-pack screenshot/dump proof.

## Immediate Non-Destructive First Wave

Smallest useful UI slices, intended to avoid risky product behavior:

1. Add durable direction and gallery planning notes.
2. Hide developer panel/runtime controls by default in product review mode,
   with explicit opt-out preserved.
3. Replace result-card retrieval jargon with user-value copy.
4. Add a pure evidence source model/mappers package before touching rendering.
5. Add a behavior-preserving phone-tab seam for Library / Ask / Saved.
6. Validate code slices with focused JVM tests first, then promote to fixed
   four-posture state-pack proof after a coherent UI batch lands.

## Progress Update - 2026-04-27

Latest Android UI direction movement:

- `761162a` started the behavior-preserving navigation and evidence seams:
  phone tab labels now have a tested Library / Ask / Saved direction, and pure
  evidence models exist as a non-rendering mapper seam.
- `3adbf79` tightened field-manual copy across answer, source, cross-reference,
  proof, and tablet evidence surfaces, and added the local gallery proof wrapper
  plan without changing acceptance semantics.
- `7294d2b` tightened guide connection and source-formatter copy, improved
  emergency banner contrast, and expanded local gate coverage for the Android
  UI seam work.
- `948b201` is the current clean proof anchor for the Android UI direction
  batch after the result-card, developer-panel, guide/answer contract,
  cross-reference, and phone navigation seam work. The fixed four-posture local
  gallery proof passed at
  `artifacts/android_ui_gallery_local_proof_948b201/20260427_181116/summary.json`:
  `45 / 45`, `fail_count=0`, `platform_anr_count=0`,
  `matrix_homogeneous=true`, model `gemma-4-e2b-it-litert`, APK SHA
  `081fe91540d3e1f856e3c438fe344cbe92ce325a3ddcaea455d8d15df66c1004`.
  Per-role counts were phone portrait `11 / 11`, phone landscape `12 / 12`,
  tablet portrait `11 / 11`, and tablet landscape `11 / 11`.

Next scout queue:

1. Nav intent seam: preserve Library / Ask / Saved as the visible product
   direction while internal navigation constants and runtime behavior stay
   stable.
2. Detail surface contract: separate guide-reading and answer-detail display
   expectations before changing rendering behavior.
3. Emergency eligibility policy: define which reviewed/support states may enter
   the emergency lane, with negative controls for routine medical and planning
   prompts.
4. Stress reading policy: make phone landscape, large font, and urgent-answer
   density rules explicit before accepting visual polish.

Acceptance boundaries remain strict: these commits are direction and seam
progress only. They do not approve reviewed-card product runtime, new card
coverage, retrieval behavior changes, emergency-lane exposure, or UI acceptance
without fixed four-emulator screenshot/dump state-pack proof.

## Stop Lines

- Do not expose reviewed-card runtime beyond the current developer/test policy.
- Do not add cards, alter guide content, or change retrieval to satisfy a UI
  critique.
- Do not make emergency styling depend on unsupported generated inference.
- Do not hide route, anchor, source count, or evidence labels for visual polish.
- Do not let helper suggestions impersonate citations or reviewed evidence.
