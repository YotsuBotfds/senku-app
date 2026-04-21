# Probe — Flake-check3: `searchQueryShowsResultsWithoutShellPolling` on emulator-5554

- **Role:** main agent (`gpt-5.4 xhigh`) or inline. Read-only probe.
- **Paste to:** **same window as the R-host validation dispatch**
  (continuation of that lane; gates whether R-host is truly
  validated end-to-end).
- **Predecessor context:** R-host validation sweep at
  `artifacts/cp9_stage2_post_r_host_20260421_065416/` returned
  44/45. The one failure is
  `searchQueryShowsResultsWithoutShellPolling` on
  `tablet_portrait / emulator-5554` with shape
  `results list never appeared; harness signals=busy[1]: main.search`.
  This is the third incident of the same fixture/shape across
  48 hours (5554 on 2026-04-20 evening, 5556 on 2026-04-21
  early morning, 5554 again today). Prior two incidents both
  cleared 3/3 on re-run. R-host did NOT touch
  `waitForResultsSettled`, `main.search` emission, or the
  search lane directly — its only androidTest change that
  *might* affect 5554 load is the new test
  `hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`
  which runs on host-inference postures and could shift
  timing margins on adjacent fixtures.
- **Epistemic purpose of this probe:** rule out R-host-induced
  regression on 5554. If 3/3 re-runs pass, R-host is fully
  validated at the matrix level; the 5554 failure was another
  instance of the known `main.search` intermittent flake
  (diagnostic slice `R-search_diagnostic.md` files for that
  pattern independently). If ≥1 re-run fails, R-host
  validation is contaminated and the failure needs deeper
  investigation.

## Scope

Read-only probe. No code, no rebuild, no commit.

## Preconditions

- HEAD = `1edde32689c7cd1175248229ab2cc8b9603a143c` (R-host).
- `emulator-5554` online, `sys.boot_completed=1`.
- Debug APK `99e2bfde98acdd425c9318e0d2b7ad919b14c0043898e7fb0a394ead2ac3c6ef`
  and androidTest APK `a0e6283b05cb1dac48e20ffb1b6eb3ecbf563347cbbb3d59851604b02a686fe1`
  installed on 5554 (the R-host substrate).
- Mobile pack homogeneous with the other three serials (live
  `af58bd127de3ec8c391eff1cb0f0a83f49aa8cde855c5d4f3e973bc3767da2c6`
  SQLite / `e5cfa2995623ac250e11c7e7f1a1034e98a94fbf1c02a8ba68ded5c112788981`
  vectors baseline per the R-host validation precondition
  drift note).

## Work

Run
`com.senku.mobile.PromptHarnessSmokeTest#searchQueryShowsResultsWithoutShellPolling`
three times consecutively on `emulator-5554` using the same
invocation pattern as the prior two flake-checks
(`artifacts/external_review/rgal1_flakecheck_20260420_221049/`
and `artifacts/external_review/rgal1_flakecheck2_5556_20260421_062052/`).
Capture per-trial wall-clock, instrumentation time, and
status.

Output folder:
`artifacts/external_review/rhost_flakecheck3_5554_<ts>/`
with trial logs, a summary, and the installed APK sha check.

## Verdict branches

- **3/3 pass → FLAKE confirmed.** R-host validation is clean
  end-to-end at the matrix level. The 5554 failure was
  another instance of the known `main.search` intermittent
  flake. Report FLAKE; no further action on the R-host
  validation lane. `R-search_diagnostic.md` (now
  self-dispatchable in parallel) will address the recurring
  pattern independently. No gallery republish needed (R-host
  is harness-observability only; 2026-04-21 gallery remains
  canonical).
- **≥1/3 fail → SYSTEMIC / possibly R-host-induced.** STOP.
  R-host validation is contaminated; the new
  `hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`
  test may be adding enough load to push `main.search`
  timing over the wait window on tablet_portrait. Capture
  full logcat of the failing trial(s). Planner will decide
  between: (a) rolling back `hostAskProbeWaitsForSettledDetailActivityAfterMainHandoff`
  to isolate, (b) expanding `R-search_diagnostic.md` scope
  to include load-coupling analysis, (c) bumping
  `SEARCH_WAIT_MS`. Do NOT pre-select a remediation from
  this probe.

## Report format

- Per-trial line: wall-clock ms, instrumentation time, OK/FAIL.
- Evidence folder path.
- Installed APK sha on 5554 (confirm `99e2bfde...` debug and
  `a0e6283b...` androidTest).
- Verdict (FLAKE / SYSTEMIC).
- If SYSTEMIC: path to captured logcat of the failing trial(s).
