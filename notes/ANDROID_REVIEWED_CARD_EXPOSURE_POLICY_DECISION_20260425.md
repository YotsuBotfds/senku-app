# Android Reviewed-Card Exposure Policy Decision - 2026-04-25

## Decision

Choose Option A: keep reviewed-card runtime developer/test-only and default
`off` for now.

This note closes A14d as a policy decision only. It does not approve product
exposure, new cards, new UI, or a default/runtime change.

## Evidence

- A14a layout/pack readiness artifacts:
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/`
  - Fixed four-device/posture coverage was captured.
  - APK SHA:
    `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`
  - Mixed copy was found in this pass and fixed later.
- A14b proof-surface copy artifacts:
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/`
  - Four-posture reviewed poisoning matrix was captured.
  - APK SHA:
    `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`
  - Non-reviewed control packaged proof exists only for phone portrait:
    `artifacts/android_reviewed_card_a14b_non_reviewed_control_20260424/strong_5556_packaged/20260424_234836_248/emulator-5556`
- A14c forbidden-label artifacts:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/`
  - Runtime-on phone reviewed inverse expected `REVIEWED EVIDENCE` and forbade
    `STRONG EVIDENCE`.
  - Runtime-on phone non-reviewed canary expected `STRONG EVIDENCE` and forbade
    `REVIEWED EVIDENCE`.
  - Runtime-off non-reviewed matrix covered all four fixed postures.
  - APK SHA:
    `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`

## Gaps

- Exact support language has not been tested across all four fixed postures:
  `Reviewed guide card`, `Cites guide <ID>`, `Status: pilot reviewed`, and the
  limit line.
- Phone landscape composer/detail remains cramped and can still occlude content.
- A14c strongest runtime-on inverse controls are phone-only, not a full
  local-preview reviewed matrix.

## Stop Lines

- No product-default on.
- No card expansion.
- No top-level product UI.
- No local-preview toggle.
- No runtime default change.

## Follow-Up

If desired, run one narrow slice to test the exact local-preview support
language across the fixed phone/tablet portrait/landscape postures. Keep that
slice limited to support-language proof; do not expand reviewed-card scope or
change runtime exposure.
