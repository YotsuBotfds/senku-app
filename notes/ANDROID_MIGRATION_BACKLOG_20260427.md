# Android Migration Backlog - 2026-04-27

Small backlog note for current-head Android migration follow-up after the
matrix proof. Keep this note limited to known blockers and polish items; do not
use it to approve reviewed-card runtime expansion or product exposure.

## Evidence Baseline

- Current-head pack guard runbook and matrix summary:
  [`ANDROID_CURRENT_HEAD_PACK_GUARDS_20260427.md`](./ANDROID_CURRENT_HEAD_PACK_GUARDS_20260427.md)
- Phone portrait proof note:
  [`ANDROID_CURRENT_HEAD_PHONE_UI_STATE_PROOF_20260427.md`](./ANDROID_CURRENT_HEAD_PHONE_UI_STATE_PROOF_20260427.md)
- Four-role state-pack artifact:
  [`../artifacts/ui_state_pack_current_head_20260427_matrix/20260427_005131/summary.json`](../artifacts/ui_state_pack_current_head_20260427_matrix/20260427_005131/summary.json)
- Phone portrait focused artifact:
  [`../artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/summary.json`](../artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/summary.json)
- Phone landscape install-probe ANR artifact:
  [`../artifacts/android_current_head_guard_install_probe_5560/summary.json`](../artifacts/android_current_head_guard_install_probe_5560/summary.json)

## Current Blockers

- Consolidated host-inclusive state-pack proof: after the harness identity
  fix, the four-role current-head UI state pack passed `45 / 45` under
  `artifacts/ui_state_pack_current_head_20260427_identity_fixed_full_host/20260427_013639/`.
  The rollup reports `matrix_homogeneous=true`,
  `matrix_model_name=gemma-4-e4b-it-litert`, and `identity_missing=false` on
  all four devices. Treat `matrix_model_sha` as a stable host inference
  identity key, not a hash of an installed on-device LiteRT model file.
- `phone_landscape` System UI ANR: resolved for this session after restarting
  `emulator-5560`, reinstalling app/test APKs, and re-pushing the current-head
  pack. The post-restart full `phone_landscape` pack passed `11 / 11` with
  `platform_anr=null` under
  `artifacts/ui_state_pack_current_head_phone_landscape_after_handoff_fix/`.
- Tablet linked-guide handoff context assertion: resolved by carrying
  guide-mode handoff context into the tablet Compose state and harnessing those
  fields. Follow-up proof passed on `emulator-5554` and `emulator-5558` under
  `artifacts/android_tablet_handoff_context_fix_5554_final/summary.json` and
  `artifacts/android_tablet_handoff_context_fix_5558_final/summary.json`.
  Post-fix tablet state packs also passed `10 / 10` for `tablet_portrait` and
  `tablet_landscape` under
  `artifacts/ui_state_pack_current_head_tablet_after_handoff_fix/`.
- Phone portrait clipping polish: resolved for the known home-surface issues.
  Search/composer placeholders now ellipsize, and compact-phone linked path
  chips stack vertically instead of exposing half-visible horizontal-scroll
  chips. Focused proof passed under
  `artifacts/android_phone_portrait_home_polish_final/20260427_014631_897/emulator-5556/summary.json`;
  the broader phone portrait pack also passed `11 / 11` under
  `artifacts/ui_state_pack_phone_portrait_polish_20260427/20260427_014311/`.
- Identity/model metadata missing: resolved for harness/state-pack summaries by
  recording a host inference identity source plus stable host model key. Visible
  UI identity polish remains separate from migration proof.
- Tablet on-device LiteRT readiness: partially reopened and narrowed. E2B
  push/readiness now works on `emulator-5558` tablet landscape after adding a
  helper free-space preflight and local model-readiness smoke. Proof passed
  under
  `artifacts/android_litert_model_readiness_5558_landscape_skipinstall_20260427/20260427_081639_614/emulator-5558/summary.json`
  with `model_identity_source=installed_model`,
  `model_name=gemma-4-E2B-it.litertlm`, `identity_cache_hit=true`, and
  landscape screenshot/dump artifacts. `emulator-5554` still cannot use the
  current staged push path for E2B: the helper failed fast with about `3.38 GiB`
  free versus about `4.87 GiB` required. Treat `5554` as an AVD data-size
  blocker, not an app/runtime failure.

## Stop Lines

- Do not count the earlier ANR-blocked `phone_landscape` lane as proof; use
  the post-restart `11 / 11` artifact as the current clean lane evidence.
- Do not treat tablet linked-guide failures as pack inventory failures without
  fresh retrieval/pack evidence; the known handoff-context regression is fixed
  in the 2026-04-27 tablet proof artifacts above.
- Do not broaden this backlog into tests, scripts, source edits, or runtime
  product policy changes.
- Do not retry on-device LiteRT pushes on `emulator-5554` with the staged helper
  until its AVD data partition is enlarged or a non-staging transfer path is
  implemented.
