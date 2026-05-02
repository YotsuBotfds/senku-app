# Android Controller / Policy Ownership Map

Short map for current Android ownership. Keep this as orientation, not history.

## Main Activity Boundary

- `MainActivity`: Android lifecycle, view binding, rendering, and effect wiring.
- Route state: `MainRouteCoordinator`, `MainRouteDecisionHelper`,
  `MainRouteEffectController`.
- Search orchestration: `MainSearchController`.
- Ask preparation: `AskQueryController`.
- Shared Search/Ask staleness: `LatestJobGate`.
- Saved guide flow: `MainSavedGuidesController`, `SavedGuidesPolicy`,
  `PinnedGuideStore`.
- Home related guide refresh: `MainHomeRelatedGuideController`.
- Review/demo shaping: `ReviewDemoPolicy`; production fixture copy should stay
  behind explicit review mode.

## Answer Generation

- `AnswerPresenter`: detail/request generation orchestration and harness tokens.
- `OfflineAnswerEngine`: prepare/generate answer runs, answer modes, subtitles,
  low-coverage downgrade, prompt building handoff.
- Prompt cleanup/corruption/low-coverage predicates: `PromptAnswerTextPolicy`.
- Host runtime: `HostInferenceConfig`, `HostInferencePolicy`,
  `HostInferenceRequestPolicy`, `HostInferenceResponsePolicy`,
  `HostInferenceClient`.

## Pack / Retrieval

- `PackInstaller`: app-private pack install/reuse validation and schema checks.
- `PackRepository`: repository facade and SQLite/vector resource ownership.
- Route-focused search: `PackRouteSearchSqlPolicy`,
  `PackRouteFocusedSearchExecutor`, route signal/support/refinement policies.
- Search fusion/final ranking: `PackRetrievalFusionPolicy`,
  `PackSearchRerankPolicy`, `PackSearchFinalizationPolicy`.
- Answer context and anchor choice: `PackAnswerContextPolicy`,
  `PackStructuredAnchorPolicy`, `PackRouteAnchorBiasPolicy`,
  `PackWaterDistributionAnchorPolicy`.
- Retrieval map: `notes/PACK_RETRIEVAL_POLICY_MAP.md`.

## Detail Surface

- `DetailActivity`: Android lifecycle, view binding, rendering, navigation.
- Back/home semantics: `DetailBackPolicy`.
- Chrome/action visibility: `DetailChromePresentationPolicy`,
  `DetailOverflowPolicy`, `DetailTabletEmergencyChromePolicy`.
- Source/evidence state: `DetailSourceStackPolicy`,
  `DetailTabletEvidencePolicy`, `DetailPhoneSourceCardPolicy`,
  `DetailRelatedGuidePreviewPolicy`.
- Follow-up composer: `FollowUpComposerController`,
  `FollowUpComposerState`, `DetailFollowUpActionController`.
- Emergency presentation: `DetailEmergencyPresentationPolicy`,
  `DetailEmergencySurfaceBridgePolicy`.

## Harness / Proof

- Validation ladder: `notes/ANDROID_VALIDATION_LADDER.md`.
- Latest proof rollup: `notes/LATEST_ANDROID_PROOFS.md`.
- Live route smoke: `scripts/run_android_route_smoke.ps1`.
- Broad route parity: `PackRepositoryCurrentHeadRouteParityAndroidTest`
  with parsed `SenkuRouteParity` timing breadcrumbs.

## Adding New Helpers

- Do not add a helper/policy/controller unless it removes production logic,
  has production call sites, focused behavior tests, and commit validation.
- Prefer behavior tests over source-string assertions.
