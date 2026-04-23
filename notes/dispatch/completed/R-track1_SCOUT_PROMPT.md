# Scout prompt v3 — R-track1 atomic disposition audit (round 3)

Paste to a Spark `gpt-5.3-codex-spark xhigh` session (read-only). Output: GO / GO-WITH-EDITS / HOLD with BLOCKING/SUGGESTION findings. Scout does NOT run generator scripts, edit files, dispatch, run `git add`, run `sha256sum`, or execute any of the slice's steps — audit only.

## Context — v2 HOLD → v3 response

Round 2 found 5 BLOCKINGs + 1 SUGGESTION on slice v2. Slice has been rewritten to v3. Each prior finding has a specific fix; your job is to verify the fixes are correct and to attack the v3-specific surface that didn't exist in v2.

### Round-2 BLOCKINGs → v3 fixes (DO NOT re-litigate unless broken)

1. **Rule 5 unreachable under first-match order** → v3 added **Rule 5a** as an additive override: the sidecar-gap carry-over note fires regardless of which rule caused TRACK. See slice §Decision rules Rule 5 and Rule 5a.
2. **Script-test pairing gap** → v3 added **Rule 4b**: `scripts/*.py` with matching `tests/test_<x>.py` → TRACK. See §Decision rules Rule 4b.
3. **Missing candidates (`litert-host-jvm/`, `tools/`, `uiplanning/`, `test_prompts.txt`, scratch files)** → v3 §Pre-state now enumerates these explicitly; v3 added **Rule 2a** (AGENTS-named non-`.py` assets), **Rule 6** (source-like subproject), **Rule 6a** (planning-docs subdir), **Rule 10** (scratch process-output).
4. **`.mcp.json` misclassification** → v3 removed `.mcp.json` from Rule 16 (shared project config); routed to Rule 9 (local-only with machine-specific path). Slice §Pre-state explicitly notes `.mcp.json:11` hardcodes a user-specific path and AGENTS.md:80 names `opencode.json` (separate, tracked).
5. **Acceptance rubber-stamp + content-edit proof gap** → v3 added:
   - Hard-anchored must-track list by name (not derived from report counts)
   - Hard lower bounds (`git ls-files -- "*.py" >=60`, subproject subset counts)
   - Pre-commit sha256 hash manifest + post-commit hash verification (§Work Step 8 and Step 11; §Acceptance § Hash invariance)
   - Blocking import-smoke (§Work Step 10; §Acceptance § Import-smoke)

### Round-2 SUGGESTION → v3 fixes

- Secret-scan pattern extended to include PEM/private-key, opaque-blob (`.py` only), machine-path, non-localhost URLs, email, UNC/network-share. See slice §Outcome § Secret-scan pattern set.
- Screenshot Rule 15 now scopes reference-grep to **tracked docs only** (`git ls-files -- "*.md" | xargs grep -l ...`), not all markdown. Untracked-doc references do NOT promote to TRACK.

### Round-2 precondition note → v3 fix

- Precondition 4 loosened: `git diff -- .gitignore notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md` must be empty, but pre-existing dirt on `AGENTS.md` (M), `opencode.json` (M), and `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` (D) is expected and explicitly does NOT block. §Boundaries forbids staging them.

### Confirmed clean across rounds 1-2 (DO NOT re-examine)

- Secret scan clean on the original 5 root `.py` (Angle 3 round 2 confirmed): `config.py`, `guide_catalog.py`, `bench_artifact_tools.py`, `confidence_label_contract.py`, `deterministic_special_case_registry.py`.
- `git ls-files <dir>/` returned empty for `archive/`, `artifacts/`, `chroma_db/`, `db/`, `models/`, `.serena/`, `android-app/.kotlin/` — safe to ignore wholesale.
- No path lets Codex delete zips or snapshots without Tate review (Rule 14 / Rule 15 verified DEFER-only).
- `10` untracked root `.py` enumeration is correct (confirmed multiple rounds).
- Generator output path for `deterministic_special_case_registry.py` matches slice claim.

**Your job this round: attack v3-specific surface — hash-mechanism correctness, rule interactions, import-smoke execution details, and handoff completeness. 5 fresh angles.**

---

## Angle 1 — Hash manifest mechanism correctness (Windows + CRLF risk)

The v3 hash-manifest mechanism (§Work Step 8 pre-commit + Step 11 post-commit) is the load-bearing proof that no content edits occurred during the tracking action. Verify:

- **Line-ending risk.** On Windows, `git config core.autocrlf=true` (likely default) rewrites line endings on `git add` / `git checkout`. A file whose working-tree state is `LF` will have `CRLF` after a roundtrip, or vice versa. If sha256 is computed BEFORE `git add` on the working-tree file, and AFTER commit on the working-tree file again, do both hashes match? (Answer may be yes — `git add` stores the normalized form, but the working-tree file may be left as-is or converted depending on `core.autocrlf` semantics.) Check `git config --get core.autocrlf` state if visible; flag if line-ending conversion could create false-positive "content edit" diffs in the post-commit check.
- **Scout-actionable verification.** Run `git config --get core.autocrlf` via bash or powershell equivalent. If the output is `true` or `input`, the risk exists and v3 should document an adjusted verification mechanism (e.g., compute post-commit hash from `git cat-file blob :<file>` output rather than working-tree file).
- **Empty-file edge case.** `test_startprocess_err.txt` is empty. sha256 on an empty file is a known fixed value (`e3b0c44...`). If the file is IGNORE-dispositioned (Rule 10 default), it's not in the TRACK set and not in the manifest. Verify no contradiction.
- **Subproject enumeration correctness.** Hash-manifest §Work Step 8 shell snippet uses `<TRACK-list>` as a placeholder. Will Codex enumerate `litert-host-jvm/src/**` files individually or use a directory-level shortcut that could silently drop a nested file? Verify the slice gives clear instruction on recursive enumeration.

Flag BLOCKING if line-ending conversion WILL create false post-commit diffs on common file types (Python source, markdown). Flag SUGGESTION if the risk is theoretical but worth documenting.

## Angle 2 — Decision-rule interaction and duplicate-trigger risk

v3 added several new rules (2a, 4b, 5a, 6, 6a, 10) and the first-match-wins discipline plus the Rule 5a additive override. Verify interactions:

- **Rule 4b vs Rule 17.** Both mention `scripts/*.py`. Rule 4b: has matching test → TRACK. Rule 17: "imported or test-paired → TRACK via Rules 3 / 4 / 4b." The slice flags Rule 17 as "explicit pointer" — redundant with Rule 4b? If so, the rule set has a duplicate fire-triggers that may confuse Codex about which rule fired "first." Flag SUGGESTION to collapse or clarify.
- **Rule 5a vs other additive overrides.** The slice phrases Rule 5a as "runs even though Rule 2/3 already caused TRACK." Are there other cases where an additive carry-over should fire regardless of primary rule? (Spot-check: the sidecar gap is unique in shape; no other rule needs additive override. Verify.)
- **Rule 6 subproject boundary.** Rule 6 covers `litert-host-jvm/` (TRACK source + IGNORE subdirs) and `tools/sidecar-viewer/` (TRACK all). Rule 6a covers `uiplanning/` (TRACK all). Will `tools/` at the top level without a subproject-qualified subdir (i.e., anything directly under `tools/` outside `tools/sidecar-viewer/`) be correctly handled? If `tools/` has other content (subdirectories, loose files), the slice doesn't say. Check `ls tools/` output — if only `sidecar-viewer/` exists, no issue. If other things exist, flag.
- **`.gitignore` addition for `litert-host-jvm/` subdirs.** Rule 6 adds `.gitignore` entries for `litert-host-jvm/.gradle/`, `.../build/`, `.../bin/`. Does the slice's existing `.gitignore` already have a top-level `.gradle/` rule that would cover `litert-host-jvm/.gradle/`? Line 2-3 of `.gitignore` show `.gradle/` and `.gradle-user-home/` — these are top-level. Git's gitignore matches based on the pattern's relative position. Does `.gradle/` at repo root match `litert-host-jvm/.gradle/`? **It likely does** (`.gradle/` without a leading `/` matches any `.gradle/` directory in the tree). If so, the v3 addition is redundant. Flag SUGGESTION.
- **Rule 18 "dead code" threshold.** Rule 18 says DEFER if "zero imports AND no test pair AND not AGENTS-named." What's the false-positive rate? A utility script could be run from command line without being imported (e.g., `scripts/audit_mobile_pack_metadata.py`). Would the slice defer it incorrectly? Inspect the slice's instruction — does it consider CLI-invocation patterns (module-level `if __name__ == "__main__":`) as evidence of legitimate source? Flag SUGGESTION if the discipline is weak.

Flag BLOCKING for rule-interaction that produces a wrong disposition. Flag SUGGESTION for redundancies or ambiguous phrasing.

## Angle 3 — Import-smoke execution correctness

v3 §Work Step 10 is BLOCKING import-smoke. Verify:

- **Root-level `.py` import.** The line `python -c "import config, guide_catalog, ..."` requires the Python cwd to be the repo root (since those modules are at root, not under a package). Does the slice's Step 10 instruction make this explicit? Or will Codex run the command from a different cwd and hit spurious ImportError?
- **`scripts/*.py` import.** The slice says "`python -c 'import scripts.<name1>, scripts.<name2>, ...'`". **This requires `scripts/` to be a Python package (have `__init__.py`).** Verify: `ls scripts/__init__.py`. If no `__init__.py` exists, `import scripts.<name>` fails with ModuleNotFoundError. The correct form would be `cd scripts && python -c "import <name1>, <name2>"` or `PYTHONPATH=scripts python -c "import <name>"`. Flag BLOCKING if no `__init__.py` exists and the slice instruction would fail.
- **Import-side-effects.** Some of these modules may execute code at import time (e.g., `config.py` builds paths; `lmstudio_utils.py` may attempt to connect). Is the import-smoke robust to an LM Studio server being offline, a missing env var, etc.? `lmstudio_utils` name suggests HTTP client code that may or may not execute at import. If the import call actually attempts a network connection, Step 10 will hang or fail unpredictably. Verify by reading the import-time code of `lmstudio_utils.py` (first 30 lines). Flag SUGGESTION if any import triggers network activity or heavy work.
- **Test-file import is NOT in the smoke.** The slice's import-smoke covers TRACK-dispositioned `.py` at root and under `scripts/`. It does NOT cover `tests/*.py`. Should it? Tests import test frameworks + target modules; a test file's import-smoke would fail if the target is broken. Since the root + scripts smoke already covers target correctness, test smoke is arguably redundant. But if a test file has a syntactic error, the slice won't catch it. Flag SUGGESTION if tests should be in the smoke.

Flag BLOCKING for import-smoke command correctness (e.g., missing `__init__.py` issue). Flag SUGGESTION for robustness or coverage.

## Angle 4 — Must-track anchor list completeness

v3 §Acceptance § Hard-anchored must-track list specifies 19 files. Verify each is:

- **Actually on disk.** `for f in <list>; do test -f "$f" && echo PRESENT || echo MISSING; done`. Any MISSING = slice can't succeed. Flag BLOCKING.
- **Actually tracking-eligible (no secret, not machine-specific).** Spot-check each with a fast secret-scan: `grep -niE "password|token|api[_-]?key|secret|bearer|credential|-----BEGIN |C:\\\\Users\\\\tateb" <file>`. Any hit = file can't be tracked as-is. Flag BLOCKING.
- **Complete set.** Is any critical dependency missing from the list?
  - Is `config.py` alone enough for `metadata_validation.py`'s transitive imports to all land? (i.e., does `metadata_validation.py` import from `config.py` or other untracked modules not on the list?)
  - Are there `scripts/*.py` must-tracks (beyond the anchor list's implicit coverage via "scripts/refresh_mobile_pack_metadata.py is tracked from R-hygiene2")? The anchor list has NO `scripts/*.py` entries, but the slice TRACKs most of them via Rule 4b. Should any scripts/ file be in the anchor list because it's critical?
  - Are `tests/*.py` on the anchor list? None are. Most would track via Rule 4 but there's no hard anchor. Is that OK?

- **`requirements.txt`.** Is the file present? If absent or trivially wrong, tracking an incomplete dependency list could be worse than not tracking. Quick look: `head -20 requirements.txt`.

Flag BLOCKING for any list-entry that can't actually be tracked. Flag SUGGESTION for gaps in the list's coverage.

## Angle 5 — Handoff and carry-over completeness

The slice defers substantial scope to follow-up slices (guides/ content, notes/ content, zip-DELETE triage, screenshot visual review, dated snapshots, audit markdown, orphan `.py` DEFERs). Verify:

- **Carry-over entries will be actionable.** §Work Step 13 lists 8 carry-over entries. For each, does the entry carry enough info for a follow-up slice to proceed without re-doing R-track1's enumeration? Example: the "`guides/` content-tracking slice" carry-over — does it say how many files, what subset is critical, or just "there are untracked files in `guides/`"? Read §Work Step 13's listed carry-overs; flag SUGGESTION for any that need sharpening.
- **Rule-5a carry-over specific content.** The sidecar-gap carry-over should include: the sidecar path (`notes/specs/deterministic_registry_sidecar.yaml`), its current tracking status (untracked per scout round 1), and the drift risk. Slice says "include explicit carry-over entry" but doesn't template the wording. Verify.
- **Orphan `.py` DEFER list handling.** If Rule 18 fires on any `.py`, the file sits untracked post-commit. The carry-over must list each orphan by path. Slice mentions "orphan `.py` DEFERs from Rule 18" as a carry-over bucket. Verify the report's §Deferred items + the carry-over-list together give a complete picture; don't let orphans slip silently.
- **Report-to-tracker link integrity.** The tracker adds `Report at notes/R-TRACK1_HYGIENE_REPORT_20260422.md`. Verify the report path is consistent across the slice (§Outcome, §Work Step 12, §Acceptance, §Report format). Any drift in filename = broken link.

Flag SUGGESTION for any carry-over entry that needs enrichment before the slice is dispatched.

---

## Output format

Return one of:

- **GO**: no BLOCKING; at most 3 SUGGESTIONs.
- **GO-WITH-EDITS**: 4-6 small adjustments the planner should fold.
- **HOLD**: one or more BLOCKING issues.

Structure each finding:

```
[BLOCKING | SUGGESTION] <short title>
Evidence: <file:line or grep output — quote the text>
Why it matters: <one-line rationale>
Proposed fix: <concrete action>
```

Per-angle confirmation required — for each of Angles 1-5, include at minimum a one-line "clean" or "[SUGGESTION]/[BLOCKING] ...". Silence looks like omission.

Report ≤ 1000 words.

**Round-3 calibration note:** This is the third scout round on R-track1. If v3 is broadly sound, GO or GO-WITH-EDITS is the appropriate verdict — the slice has cycled through v1 (5-file scope), v2 (atomic scope, 5 BLOCKINGs), and v3 (v2 fixes + expanded scope + hash mechanism + hard acceptance). HOLD is appropriate only for new structural issues (not rule-phrasing polish). Bar for BLOCKING this round should be "slice will produce a wrong commit" or "acceptance can't catch a real failure mode" — not "this could be worded better."

## Anti-recommendations for the scout

- DO NOT re-litigate round-1 or round-2 findings that v3 already addressed (see §Round-2 BLOCKINGs → v3 fixes). If you believe a v3 fix is incomplete, the evidence must be "v3 text at line N shows the fix is incomplete" — not "round-2 finding still applies."
- DO NOT execute any generator script, `git add`, `git rm`, `.gitignore` edit, sha256sum, or commit. Audit read-only.
- DO NOT propose reverting v3 to a multi-commit shape or to a narrower scope. Tate authorized atomic with full-scope; the scout's job is to verify atomic is safely executable.
- DO NOT propose adding Wave-B / Wave-C awareness, R-telemetry coverage, or any cross-slice concern. R-track1 is tracking-hygiene only.
- DO NOT propose scope changes to `guides/` or `notes/` content — explicitly out of scope in v3, handled in future slices.
- DO NOT speculate without evidence. "X might leak" requires citing `file:line` or grep output.
- DO NOT re-flag `.mcp.json` as anything other than IGNORE (Rule 9) — v3 locked this.
- DO NOT re-flag Rule 5's "unreachable" issue — v3's Rule 5a override addresses it; scout's angle this round is on whether Rule 5a is correctly phrased and fires on the one item it targets.
- DO NOT re-flag acceptance-criteria self-reference — v3 added hard anchors, lower bounds, and hash invariance. Attack those NEW mechanisms, not the prior weak version.
- Read-only. No edits.

## Files

- `notes/dispatch/R-track1_core_entry_point_tracking_audit.md` — slice under audit (v3).
- `.gitignore` — primary target of Angle 2.
- `AGENTS.md` — reference for Rules 2, 2a, and the must-track anchor list.
- `tools/`, `uiplanning/`, `litert-host-jvm/` — subproject surface for Angle 2.
- All files on the v3 must-track anchor list — Angle 4.
- `scripts/__init__.py` existence check — Angle 3.
- `.git/config` / `git config` output — Angle 1 (core.autocrlf).
- `lmstudio_utils.py` first 30 lines — Angle 3 (import-side-effect check).

Return verdict.
