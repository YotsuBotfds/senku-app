## UI State Pack Recovery Plan

Goal: turn the current partial UI screenshot pack into a trustworthy all-lanes bundle across:
- phone portrait
- phone landscape
- tablet portrait
- tablet landscape

### Current broken states

#### phone_portrait
- generative detail
- follow-up thread
- guide-detail related-guide navigation

#### phone_landscape
- search results
- browse linked-guide handoff
- deterministic detail
- generative detail
- follow-up thread
- guide-detail related-guide navigation
- answer-source anchored cross-reference

#### tablet_portrait
- generative detail
- follow-up thread
- guide-detail related-guide navigation

#### tablet_landscape
- follow-up thread
- guide-detail related-guide navigation

### Root-cause buckets

1. Trust-spine assertions are too rigid
- `assertGeneratedTrustSpineSettled()` assumes the same why/source/provenance structure across all postures.
- The current app intentionally varies between full panels, compact preview, inline chips, and rail-first layouts.

2. Follow-up thread assertions are stale
- The follow-up smoke currently assumes one specific prior-turn rendering pattern.
- The app now supports multiple thread surfaces depending on posture and layout.

3. Related-guide navigation expectations are stale
- Guide detail is now preview-first in more places.
- The current smoke still expects direct-open semantics in some postures.

4. Landscape-phone flow checks need posture-specific truth
- `phone_landscape` has compact inline surfaces and different visibility rules.
- Search/detail assertions should validate that posture honestly instead of assuming portrait-like containers.

5. Artifact trust needs one extra pass
- At least one state reported `pass` without copied screenshot/dump output.
- The rerun needs to verify artifact presence, not only test success.

6. Live follow-up generation is a bad screenshot source
- The app can render a correct follow-up shell while the model returns unstable or irrelevant content.
- For the screenshot pack, the truthful target is the follow-up UI state itself, not semantic answer quality.
- The pack should use a seeded two-turn detail state for `follow-up thread`, while live follow-up remains a separate behavior-validation lane.

### Execution order

#### Slice 1: stabilize instrumentation expectations
- Update `PromptHarnessSmokeTest.java` helpers to accept the real generated-answer trust spine across compact, inline, and rail layouts.
- Make follow-up validation accept the current legitimate thread containers.
- Update related-guide tests to validate preview-first and direct-open modes honestly.

#### Slice 2: fix true phone-landscape mismatches
- Reproduce the current `phone_landscape` failures one by one after helper fixes.
- Patch test posture logic first.
- Patch app/layout code only if the real UI is missing expected state, not just because the old test assumed portrait behavior.

#### Slice 3: rerun only the broken matrix
- Rerun the exact missing states on their affected lanes.
- Require screenshot and dump presence for each repaired state.
- Move `follow-up thread` onto a seeded instrumentation state instead of the live detail-followup runner.

#### Slice 4: rebuild the full pack in parallel
- Use `build_android_ui_state_pack_parallel.ps1`.
- Keep the fixed 4-lane emulator matrix stable; do not rotate or resize to fake posture.

#### Slice 5: honest finish
- If anything still fails, leave it explicitly called out in the manifest and README instead of papering it over.
- If all states pass, use the resulting pack as the new review baseline.

### Primary files to touch
- `android-app/app/src/androidTest/java/com/senku/mobile/PromptHarnessSmokeTest.java`
- `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
- `scripts/build_android_ui_state_pack.ps1`
- `scripts/build_android_ui_state_pack_parallel.ps1`

### Validation target
- Every previously missing state passes on its intended lane.
- Every passing state has:
  - instrumentation success
  - screenshot artifact
  - XML dump artifact
- Final pack status is `pass`, not `partial`.