# Android Emergency Tablet Polish - 2026-04-28

Worker: Emergency-Tablet-Polish

## Screenshot Finding

Compared:

- Actual: `artifacts/ui_state_pack/20260428_015154/screenshots/tablet_portrait/emergencyPortraitAnswerShowsImmediateActionState__emergency_portrait_answer.png`
- Target: `artifacts/mocks/emergency-tablet-portrait.png`

The current tablet portrait emergency state uses a large `DetailActivity`
overlay that starts over the left rail and spans across the right
cross-reference pane. This reads as a modal blanket over the tablet shell, while
the target mock presents the emergency answer as an inline answer-column band
with actions and proof below it.

## Narrow Candidate Fix

`android-app/app/src/main/java/com/senku/mobile/DetailActivity.java` has a
narrow geometry-only candidate fix inside the existing tablet emergency overlay
layout methods:

- reduced tablet portrait overlay padding from `36/18dp` to `20/16dp`;
- moved the overlay up under the tablet header with `topMargin=56dp`;
- constrained the overlay to the tablet answer column with
  `leftMargin=148dp` and `rightMargin=212dp`.

This would not change emergency eligibility, routing, answer-card selection,
action extraction, or source/proof data. It only changes the tablet portrait
overlay geometry so the cross-reference pane remains visible and the emergency
surface reads closer to the inline mock.

## Coordination Risk

`DetailActivity.java` changed concurrently during this pass. The working diff
contains unrelated edits outside the emergency overlay methods, including answer
body mirror and phone landscape scroll helpers. Those edits were not reverted.

The geometry hunk above was attempted, then overwritten by the concurrent
`DetailActivity.java` work before handoff. Current source ownership is therefore
left to the active `DetailActivity.java` worker; use the candidate values above
when that file is clear.

## Validation

Attempted:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.senku.mobile.DetailSurfaceContractTest
```

Result: blocked before the Java test by an unrelated Kotlin compile failure:

```text
TabletDetailScreen.kt:455:41 Unresolved reference 'onEvidenceToggleClick'
```

No emulator screenshot rerun was performed in this pass.

## Residual Risk

This is still an overlay because true inline tablet emergency rendering belongs
inside the Compose tablet detail surface, which was outside this worker's owned
file. The geometry-only fix should reduce the modal feel, but final acceptance
still needs a fresh tablet portrait state-pack screenshot after the Kotlin
compile issue is resolved.
