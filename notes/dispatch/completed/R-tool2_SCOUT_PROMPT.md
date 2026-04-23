# Scout prompt — audit R-tool2 slice before dispatch

Paste this to a Spark `gpt-5.3-codex-spark xhigh` session (read-only lane). Scout returns a HOLD / GO / GO-WITH-EDITS verdict plus any BLOCKING / SUGGESTION items. Scout does NOT edit files, run commands that change state, or dispatch the slice. Its job is a second pair of eyes on the drafted slice.

---

You are a Spark read-only scout auditing a planner-drafted slice before it's dispatched to Codex. Your job is to find:

1. **Line-anchor errors** — the slice names specific line numbers in `scripts/build_android_ui_state_pack.ps1` and `scripts/run_android_instrumented_ui_smoke.ps1`. Grep or read the live files and confirm each anchor is within ±5 lines of what the slice claims. If any anchor is off by more, flag BLOCKING.

2. **Invocation correctness** — the slice adds `"-CaptureLogcat"` to a PowerShell args array at line 596-606 of `build_android_ui_state_pack.ps1`. Verify: (a) `run_android_instrumented_ui_smoke.ps1` declares `[switch]$CaptureLogcat` at line 30; (b) the smoke script's line 1266 summary-writer genuinely switches on `$CaptureLogcat` to populate `logcat_path`; (c) the args array in the build script is the `@(...)` form such that appending a new entry is syntactically valid; (d) no existing entry in that array would be broken by the addition (watch for trailing-comma issues on PowerShell 5.1 vs. 7.x).

3. **Propagation path** — `Write-TrustedPackSummary` at `build_android_ui_state_pack.ps1:178-210` is claimed to preserve `logcat_path` via a generic property-copy loop. Read the function body and confirm the loop exists and doesn't overwrite `logcat_path` with a hardcoded null. If the function explicitly sets `$summaryData["logcat_path"] = $null` anywhere, flag BLOCKING.

4. **Detail_followup scope call** — the slice says detail_followup (line 511-594 of the build script) already populates logcat_path via `run_android_detail_followup_logged.ps1:167`. Verify this claim by grep or read. If detail_followup_logged sets `logcat_path = $null` unconditionally or gates it on a flag that's not passed, that changes the slice's scope — flag SUGGESTION (expand the slice to cover detail_followup too, or explicitly call it out as known-gap).

5. **Validation design** — the slice validates via `-RoleFilter phone_portrait -SkipBuild -SkipInstall -SkipHostStates` for ~3-7 min of emulator time. Is this sufficient? Consider: (a) are there states that take a DIFFERENT invocation path than the smoke script, bypassing the fix? (b) does `-SkipHostStates` exclude the states most likely to need logcat for diagnosis? (c) is 5556 phone_portrait the right role, or does another role hit the edge cases better?

6. **Out-of-scope risk** — the slice explicitly forbids touching the parallel launcher, adding a new switch, or touching the smoke script. Confirm these exclusions don't leave a half-done fix (e.g., does the parallel launcher invoke the child with some arg-rewrite that would strip the propagation? — read `build_android_ui_state_pack_parallel.ps1:52-68` to check).

7. **Acceptance-gate tightness** — the slice's acceptance says every per-state summary must show populated logcat_path + file >1 KB. Is >1 KB the right threshold? A failed test state might produce a tiny logcat. Consider whether the gate should be "most states populated" vs. "all states populated" vs. "at least one state populated."

## Output format

Return one of:

- **GO**: slice is correct as-written; dispatch as-is.
- **GO-WITH-EDITS**: slice is fundamentally correct but has N wording/anchor/scope adjustments needed. List each adjustment with a one-line rationale; planner will fold them in-slice.
- **HOLD**: slice has one or more BLOCKING issues that would cause Codex to fail or produce an incorrect result. List each with a concrete proposed fix.

Structure each finding as:

```
[BLOCKING | SUGGESTION] <short title>
Evidence: <file:line or grep result>
Why it matters: <one-line rationale>
Proposed fix: <concrete action>
```

Keep your report under 500 words. If you find yourself writing more than that, you're either reiterating known content (trim) or finding real issues in quantity (compress each to 2-3 lines each).

## Anti-recommendations for the scout

- Do NOT rewrite the slice. You're auditing, not drafting.
- Do NOT propose scope expansion to cover "while we're in here" cleanups (e.g., "also should refactor the logcat capture function"). Flag as SUGGESTION at most, don't require in-slice.
- Do NOT run the state-pack build script. You're read-only. If you need to validate a claim, read or grep.
- Do NOT second-guess the "-CaptureLogcat always-on, no new switch" decision unless you have evidence (storage overrun on a real run, not speculative). The planner's rationale is in the slice; disagreement needs data.
- Do NOT propose unit tests — PowerShell has no test harness in this repo.
- Do NOT re-audit commits that already landed (R-telemetry, R-ret1b). Scope is the R-tool2 slice file only.

## Slice under audit

`notes/dispatch/R-tool2_state_pack_logcat_capture.md`

## Supporting files to grep/read as needed

- `scripts/build_android_ui_state_pack.ps1`
- `scripts/run_android_instrumented_ui_smoke.ps1`
- `scripts/build_android_ui_state_pack_parallel.ps1`
- `scripts/run_android_detail_followup_logged.ps1`
- A representative recent state-pack `summary.json` (e.g., `artifacts/cp9_stage2_rerun4_20260420_143440/ui_state_pack/20260420_143855/raw/20260420_143858_318/emulator-5554/summary.json`)

Return your verdict.
