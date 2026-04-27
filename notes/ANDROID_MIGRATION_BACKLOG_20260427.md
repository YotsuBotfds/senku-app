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

- `phone_landscape` System UI ANR: `emulator-5560` reached `0 / 12` in the
  four-role state pack because Android showed `System UI isn't responding`.
  Direct current-head guard classes passed after the pack was pushed, so treat
  this as a lane/platform UI-proof blocker, not a pack migration failure.
- Tablet linked-guide handoff context assertion: resolved by carrying
  guide-mode handoff context into the tablet Compose state and harnessing those
  fields. Follow-up proof passed on `emulator-5554` and `emulator-5558` under
  `artifacts/android_tablet_handoff_context_fix_5554_final/summary.json` and
  `artifacts/android_tablet_handoff_context_fix_5558_final/summary.json`.
- Phone portrait clipping polish: the focused phone portrait proof is green,
  but visual review found placeholder text clipping and source/path chip
  clipping on the narrow surface. This is polish debt, not a migration proof
  blocker for the current phone portrait lane.
- Identity/model metadata missing: current artifacts carry APK SHA and pack
  metadata, but `model_name` and `model_sha` are null and the state-pack
  summaries report `identity_missing=true`. The visible UI identity surface also
  lacks the metadata needed for quick proof review.

## Stop Lines

- Do not count the ANR-blocked `phone_landscape` lane as proof.
- Do not treat tablet linked-guide failures as pack inventory failures without
  fresh retrieval/pack evidence; the known handoff-context regression is fixed
  in the 2026-04-27 tablet proof artifacts above.
- Do not broaden this backlog into tests, scripts, source edits, or runtime
  product policy changes.
