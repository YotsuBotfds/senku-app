# Android Current-Head Phone UI State Proof - 2026-04-27

Focused proof note for the current-head screenshot/dump state pack at:

`artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/`

## Result

- Status: `pass`
- Pass count: `10 / 10`
- Fail count: `0`
- Run window: `2026-04-27T05:43:29.7085637Z` to
  `2026-04-27T05:45:22.5660033Z`
- Lane: `phone_portrait` only
- Device: `emulator-5556`
- AVD / size: `Senku_Large_4` / `1080x2400`
- APK SHA:
  `87648dceeddbf5238d0ce93985cedad88242352ed172c75dabb9dbead06c6be3`

## Artifact Pointers

- Rollup: `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/summary.json`
- Manifest: `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/manifest.json`
- README: `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/README.md`
- Screenshots:
  `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/screenshots/phone_portrait/`
- XML dumps:
  `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/dumps/phone_portrait/`
- Per-state summaries:
  `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/summaries/phone_portrait/`
- Role manifest:
  `artifacts/ui_state_pack_current_head_screenshot_proof/current_head_phone/role_manifests/phone_portrait.json`

## Visible Current-Head Pack Metadata

Per-state summaries show an installed pack available at
`files/mobile_pack/senku_manifest.json`:

- `pack_format`: `senku-mobile-pack-v2`
- `pack_version`: `2`
- `generated_at`: `2026-04-27T04:21:12.533181+00:00`
- `guides`: `754`
- `chunks`: `49841`
- `deterministic_rules`: `9`
- `guide_related_links`: `5750`
- `retrieval_metadata_guides`: `237`
- `answer_cards`: `271`
- SQLite bytes / SHA-256:
  `290738176` /
  `bca1dc3d6de3e8ecd4d2ac585b97e4914974cb6d6889443a313646f295d686c5`
- Vector bytes / SHA-256:
  `76555808` /
  `5c4decacbf506b31acf8ae1d2568771be24004c46c96944456c8d33b7948eeb1`

The pack rollup itself has `matrix_homogeneous: false` and null matrix-level
APK/model identity because it contains only one repaired lane. The per-device
entry and per-state summaries carry the useful APK SHA and installed-pack
metadata.

## Covered UI States

- Home entry
- Search results
- Browse linked-guide handoff
- Home guide-connection detail handoff
- Deterministic detail
- Follow-up thread
- Guide-detail related-guide state
- Answer-source anchored cross-reference state
- Neutral provenance state
- Off-rail cross-reference state

## Read-Only Visual Findings

The phone portrait state pack is migration-proof green, but visual review found
follow-up polish/metadata items:

- Placeholder text clips in the narrow phone portrait surface.
- Source/path chips clip at the right edge.
- Metadata identity is present in the artifact summaries, but missing from the
  visible UI identity surface.
- Follow-up state naming does not match the visible follow-up thread state.

Classify these as follow-up polish/metadata work. They do not block the
current-head pack migration proof.

## Caveats

- This is a single phone portrait proof on `emulator-5556`; it is not a fixed
  four-posture matrix proof.
- `host_states_included` is `false`; host/generative state coverage is not part
  of this pack despite the broader README state list.
- A later four-role state pack at
  `artifacts/ui_state_pack_current_head_20260427_matrix/20260427_005131/`
  reached `31 / 45`: phone portrait stayed green, tablet lanes exposed one
  linked-guide handoff context failure each, and phone landscape remained
  blocked by System UI ANR.
- That `31 / 45` matrix is superseded for current UI acceptance by the full
  `45 / 45` proof at
  `artifacts/ui_state_pack_current_head_surface_guard_full_20260427/20260427_101835/summary.json`.
- Treat skipped, absent, or ANR-blocked current-head results as not-proof for
  that lane even when direct pack guards pass separately.
