# `cleanup_android_harness_artifacts.ps1` investigation (2026-04-23)

## Scope

Read-only audit of `scripts/cleanup_android_harness_artifacts.ps1` in the
current checkout. This note makes the script's present path-safety and
retention behavior explicit without changing the script itself.

## Current-checkout finding

`scripts/cleanup_android_harness_artifacts.ps1` is a path-safety and
retention-policy helper, not a routine lane-bounded Android harness helper.

## Evidence

1. Caller-provided target path handling:
   - The script accepts `-Targets` as a caller-supplied `string[]` parameter at
     `scripts/cleanup_android_harness_artifacts.ps1:2-8`.
   - For each requested target, it resolves rooted paths as-is and only joins
     non-rooted paths against repo root at
     `scripts/cleanup_android_harness_artifacts.ps1:12,16-18`.

2. Default roots are repo artifact directories, but absolute paths are allowed:
   - The default target set is the repo-local artifact trio
     `artifacts/instrumented_ui_smoke`, `artifacts/ui_validation`, and
     `artifacts/live_debug` at
     `scripts/cleanup_android_harness_artifacts.ps1:2-6`.
   - The rooted-path branch means caller-provided absolute targets are accepted
     unchanged at `scripts/cleanup_android_harness_artifacts.ps1:17`.

3. Retention cutoff behavior:
   - The script computes `cutoffUtc` from the current UTC time and
     `AddDays(-1 * [Math]::Abs($KeepDays))` at
     `scripts/cleanup_android_harness_artifacts.ps1:13`.
   - Because it applies `Abs`, negative `KeepDays` values do not invert the
     retention direction; they still become an age-based delete cutoff.

4. Recursive delete behavior:
   - The script enumerates each direct child under every resolved target with
     `Get-ChildItem` and skips only entries whose `LastWriteTimeUtc` is newer
     than the cutoff at `scripts/cleanup_android_harness_artifacts.ps1:22-25`.
   - Older matching children are removed with
     `Remove-Item -LiteralPath $item.FullName -Recurse -Force` at
     `scripts/cleanup_android_harness_artifacts.ps1:33`.

5. `-WhatIf` behavior:
   - Deletion is suppressed only when `-WhatIf` is explicitly supplied at
     `scripts/cleanup_android_harness_artifacts.ps1:28-31`.
   - Without `-WhatIf`, matching older children are actually deleted.

## Conclusion

In the current checkout, this helper is a recursive-delete contract surface.
Its defaults point at repo artifact evidence directories, but its caller-facing
`-Targets` parameter also permits absolute paths, and its retention logic
deletes older child items recursively unless `-WhatIf` is explicitly present.

That makes it part of the recursive-delete/path-safety branch, not the routine
Android helper tranche.

## Recommended next step

Keep this helper on its own future allow/repair/retire follow-up for the
artifact-cleanup contract. Do not merge it into the routine tracker tranche or
the separate Android process-control notes.
