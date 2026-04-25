# Mobile Update Architecture - 2026-04-12

This note records the preferred future-direction architecture for downloadable Android knowledge/model updates.

It is not an active implementation slice yet. Current live work remains retrieval quality, metadata audit, and follow-up/chat stability.

## Core Direction

- Use a manifest-driven content registry with two independently updatable artifact types:
  - `knowledge packs`
  - `model bundles`
- Keep the APK asset pack as the bootstrap fallback.
- Promote downloaded updates into an app-private store with:
  - verify
  - stage
  - activate
  - rollback
- Treat updates as full verified bundles first, not delta-first.

## Knowledge Pack Contract

Future pack updates should include one full logical bundle:

- manifest
- SQLite database
- vector data
- compiled deterministic payload
- guide registry / alias payload

Why:

- the pack already has manifest/hash/count structure in [`PackManifest.java`](../android-app/app/src/main/java/com/senku/mobile/PackManifest.java)
- the exporter already has schema room in [`mobile_pack.py`](../mobile_pack.py)
- this keeps Android/runtime decisions driven by exported data instead of hardcoded Java-only assumptions

## Model Update Contract

- Keep `E2B` as the default floor profile.
- Keep `E4B` as an optional quality tier selected by capability/profile, not by filename assumptions.
- Move toward manifest-backed `model_profile_id` records instead of exact filename/path expectations in Android runtime code.

This is the clean long-term direction away from current E2B-biased assumptions in:

- [`ModelFileStore.java`](../android-app/app/src/main/java/com/senku/mobile/ModelFileStore.java)
- [`HostInferenceConfig.java`](../android-app/app/src/main/java/com/senku/mobile/HostInferenceConfig.java)

## Stable Identity Direction

- Stop treating `guide_id` or title as the long-term runtime anchor.
- Add immutable `guide_key`.
- Eventually add immutable `section_key`.
- Keep:
  - `guide_id` for citation/display
  - title for UI
  - aliases for old ids, slugs, titles, and common section phrases

This should let retrieval/routing and future deterministic payloads survive guide renumbering or title cleanup without breaking behavior.

## Deterministic Direction

- Move deterministic parity toward a compiled shared payload exported from desktop and consumed by Android.
- Avoid growing a permanently separate hardcoded Android router.

Target convergence area:

- [`deterministic_special_case_registry.py`](../deterministic_special_case_registry.py)
- [`special_case_builders.py`](../special_case_builders.py)
- [`mobile_pack.py`](../mobile_pack.py)
- [`DeterministicAnswerRouter.java`](../android-app/app/src/main/java/com/senku/mobile/DeterministicAnswerRouter.java)

## Staged Roadmap

1. Pack updates first.
   - add update catalog
   - add compatibility fields
   - add staged download / verify / activate / rollback
   - keep installs full-file verified first
2. Guide registry hardening second.
   - export `guide_key`
   - export alias tables
   - export section aliases
3. Deterministic convergence third.
   - expand exported deterministic payload beyond sample metadata
4. Model profile lane fourth.
   - add `model_profile_id`
   - add capability gating and recommended pairings
5. Background polish last.
   - background checks
   - resume/retry
   - user-facing update UI
   - delta optimization only after the contract is stable

## Documentation Follow-Ons

When this becomes active work, update:

- [`MOBILE_OFFLINE_PACK_SPEC_20260410.md`](./MOBILE_OFFLINE_PACK_SPEC_20260410.md)
- [`ANDROID_ROADMAP_20260412.md`](./ANDROID_ROADMAP_20260412.md)
- [`TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md)
- [`ACTIVE_WORK_LOG_20260412.md`](./ACTIVE_WORK_LOG_20260412.md)
- [`AGENT_STATE.yaml`](./AGENT_STATE.yaml)

## Practical Read

- This is worth preserving now so future work does not drift back toward hardcoded guide ids, hardcoded model filenames, or one-off update paths.
- It should stay behind current Android retrieval/metadata/chat stabilization until those lanes are healthier.
