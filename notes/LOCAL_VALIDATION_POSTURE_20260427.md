# Local Validation Posture - 2026-04-27

GitHub Actions are manual-only for this repo posture. Before each commit or
push, and again after a push when a low local worker is available, prefer this
focused local validation loop instead of waiting on cloud CI.

## Android Local Quality Gate

Default focused Android JVM gate:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1
```

The helper runs:

- `git diff --check`
- `android-app\gradlew.bat --no-daemon :app:testDebugUnitTest` with focused UI
  JVM filters for answer content, detail action/warning presentation, detail
  surface contracts, emergency surface policy, phone navigation, search result
  adapters/cards, pure evidence models, source rows, stress-reading policy,
  tablet evidence visibility, and contrast audit coverage

Add extra Gradle `--tests` filters with `-Tests`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_android_local_quality_gate.ps1 -Tests "com.senku.mobile.SessionMemoryTest"
```

Use `-SkipDiffCheck` only when another local gate has already run
`git diff --check` for the same workspace state. The script exits nonzero on the
first failing gate.
