# Slice R-track1 v5.9 - atomic tracking-hygiene disposition for untracked tree (full-scope, post final brutal review)
> **v5.9 - triple-xhigh final audit fixes (all folded):**
> - **Finding 1 (PowerShell parse gate false-passed syntax errors):** Step 10 no longer treats `[Parser]::ParseFile(... ) | Out-Null` as self-validating. The parser command now captures the error array and hard-fails if any parse errors were reported.
> - **Finding 2 (hard gates were descriptive, not executable):** Step 1 now proves `HEAD` descends from `5fb7719` via `git merge-base --is-ancestor`, and the R-hygiene2 dependency gate uses `git ls-files --error-unmatch` instead of a print-only command that can false-pass empty.
> - **Finding 3 (README/CP9 bookkeeping acceptance was under-scoped):** Step 13 now explicitly updates the stale `R-anchor-refactor1` active-slices sentence, reconciles the full landed-but-unrotated set (including `R-pack-drift1`), and acceptance scopes checks to the target sections instead of loose literals anywhere in the file.
> - **Finding 4 (environment/verification cleanup):** Step 10 now resolves `PYTHON_BIN` for Git Bash or WSL, `.gitignore` acceptance proves repo `.gitignore` supplied the ignore, and the hash-verification language now states the exact guarantee it actually proves: no post-capture content drift before commit.
> **v5.8 - final brutal review fixes (all folded):**
> - **Finding 1 (Step 13 impossible pre-commit hash reference):** Step 13 / Acceptance no longer require `notes/CP9_ACTIVE_QUEUE.md` to name the landing commit hash before Step 14 exists. Tracker prose now uses landed-form status (`R-track1` landed this commit); any exact SHA backfill is explicitly post-landing work.
> - **Finding 2 (Rule 3 promised fixed point, procedure was single-pass):** Rule 3 now uses an authoritative raw TRACK ledger plus a Step 6.5 closure pass that reruns Rule 3 / support-file resolution until no new TRACK paths are added.
> - **Finding 3 (Step 4c provisional ledger was both underspecified and speculative):** support-file handling no longer depends on an empty/speculative Step-4c seed. Step 4c now captures + secret-scans `.psm1` / `.sh` candidates; Step 6.5 resolves them only against the validated TRACK ledger built from already-cleared referrers.
> - **Finding 4 (manual Step 8 TRACK list let real TRACK items disappear):** Step 8 no longer hand-maintains a literal `printf` list. The final NUL list is generated from the raw TRACK ledger accumulated during Steps 3-7 / 6.5, and acceptance now checks the ledger-derived count.
> - **Finding 5 (acceptance/runtime cleanup):** fixed the README landed-form grep quoting bug, aligned the PowerShell acceptance text with `pwsh` OR `powershell.exe`, tightened carry-over (j) to exact dynamic count, and refreshed stale count/line citations (`root non-.py = 25`, `README_OPEN_IN_CODEX.md:45/51`, current AGENTS line anchors).
> **v5.6 — Codex round-8 fixes (all three folded, plus consistency sweep):** Codex round 8 caught three self-inflicted issues from v5.5, all of the form "rule text updated, operational step not updated." This is the third round in a row where my edits introduced spec/execution mismatches — a durable pattern I should call out rather than keep repeating.
> • **Finding 1 (Rule 2c/2d text vs Step 4c execution mismatch):** v5.5 rewrote the Rule 2c and 2d bodies to use basename-literal grep + two-pass cross-candidate scan, but Step 4c (the operational recipe) still carried the old v5.4 regex and single-pass `$(git ls-files)` command. v5.6 rewrites Step 4c to match the rule bodies literally.
> • **Finding 2 (completion/rotation contradiction):** slice said "update Active slices" without specifying WHERE; dispatch/README.md:39 and CP9_ACTIVE_QUEUE.md:13 name R-track1 as pending post-RC; slice forbids rotation but dispatch README says "rotate when lands." v5.6 spells out exact edit targets (README.md:38-45, CP9_ACTIVE_QUEUE:13) and clarifies that FILE rotation (move to `completed/`) is explicitly out-of-scope for this slice per in-slice tracker cadence memory — only the status-line text is edited.
> • **Finding 3 (~34 vs ~28 stale counts):** three locations (Expected Volumes table rows + Step 4b prose) still said "~34" after v5.2's pre-state correction to ~28. All four occurrences now normalized.
> • **Consistency sweep (v5.6 NEW):** final grep-pass for downstream inconsistencies. Verified: no remaining `~34` residuals; no orphaned py_compile / sha256sum executable blocks; no lingering `Import-Module.*<basename>` regex; no re-introduction of auto-unstage. Only references to these now exist in changelog prose (historical audit trail).
>
> **v5.5 — Codex round-7 fixes (preserved):** Codex round 7 reviewed v5.4 and found 1 High + 3 Medium + 1 Low. All folded.
> • **Finding 1 (Step 13(j) vs acceptance mismatch):** Step 13(j) was a single prose paragraph; acceptance awk-counted >=5 sub-bullets — faithful executor could pass Step 13 and still fail acceptance. v5.5 reformats (j) as per-sub-bullet (like (h)), with enumeration command + the 17+ known candidates spelled out individually.
> • **Finding 2 (machine-path regex username-specific):** v5.4 regex hardcoded `tateb`/`tate`, missing `/Users/tbronson/...` in tracked `README_OPEN_IN_CODEX.md:5,42`. Generalized to `[A-Za-z0-9._-]+` to catch any username. Markdown-doc carve-out from v5.2 handles README-type files as surface-only.
> • **Finding 3 (Rule 2c import-detection brittle):** real PowerShell callers assign the module path to `$commonHarnessModule` then `Import-Module $commonHarnessModule`, which the `Import-Module.*<basename>` regex misses. v5.5 uses `grep -lF "<basename>.psm1"` — catches both direct and variable-indirection patterns.
> • **Finding 4 (Rule 2d cross-candidate gap):** `grep ... $(git ls-files)` only scanned pre-existing tracked files, missing same-pass TRACK candidates. If `run_bench_guarded.sh` TRACKs this pass, its reference to `kill_bench_orphans.sh` is invisible to the pre-pass grep. v5.5 adds a Pass 2 that scans the same-slice TRACK list itself.
> • **Finding 5 (stale py_compile block orphaned):** v5.3 added the real `python -c "import tests.<name>"` import-smoke but didn't delete the v5.2 py_compile block it replaced — both were present. Removed the stale block; the current block correctly documents ImportError coverage.
>
> **v5.4 — Codex round-6 fixes (preserved):** Codex round 6 reviewed v5.3 and found 3 High + 2 Medium issues. All folded; atomic shape preserved.
> • **Finding 1 (printf + inline-comment = 0-byte track list):** Step 8's backslash-continued `printf '%s\0' \` with a mid-list `# comment` line merged the comment INTO the command, commenting out the `> artifacts/...` redirect and producing a 0-byte file. Fixed by removing the inline comment; added a post-write non-empty guard (`test -s`) that aborts if the list is empty.
> • **Finding 2 (missing categories for load-bearing support files):** Rule 2c NEW covers `scripts/*.psm1` (PowerShell modules imported by tracked ps1) — anchor `scripts/android_harness_common.psm1` imported by 4+ ps1 scripts. Rule 2d NEW covers `scripts/*.sh` referenced from tracked docs/scripts — anchors `scripts/verify_local_runtime.sh` and `scripts/rebuild_venv_if_needed.sh` (referenced from `README_OPEN_IN_CODEX.md:45` and `README_OPEN_IN_CODEX.md:51`). New Step 4c enumerates both categories.
> • **Finding 3 (Step 11 silently re-introduced auto-unstage):** Prior v5.3 hash-mismatch path said "STOP, `git restore --staged` everything." That is the same unauthorized index mutation Round 5 Finding 3 removed. v5.4 replaces with "STOP and escalate to planner. Do NOT auto-unstage." Consistent with the Precondition 4 / Step 1 policy.
> • **Finding 4 (pwsh-only ps1 check degraded on Windows):** Windows Git Bash typically has `powershell.exe` on PATH but not `pwsh`. v5.3 would record "SKIPPED (pwsh unavailable)" even though the real parser was right there. v5.4 tries `pwsh` then `powershell.exe` — both expose the same `[Parser]::ParseFile` API since PS 3.0.
> • **Finding 5 ((h) carry-over acceptance was header-only):** acceptance now awk-counts sub-bullets under (h) and (j); empty placeholder fails. (h) passes only if there are per-path entries OR an explicit "No Rule 18 DEFERs" line; (j) requires >=5 per-path entries.
>
> **v5.3 — Codex round-5 fixes (preserved):** Codex round 5 reviewed v5.2 and found 2 High + 1 Medium-High + 2 Medium issues. All folded; atomic shape preserved; Tate directive "worth it for when we continue development" confirmed the ambitious scope.
> • **Finding 1 (Rule 17a too broad for "core entry point" framing):** Rule 17a kept permissive (auto-TRACK on `__main__`) per Tate's "everything" intent. Scope-impact acknowledgment added: Rule 17a sweeps ~12 additional `scripts/*.py` beyond AGENTS-anchored / test-paired. Per-item rationale now required in report (docstring / description / `__main__` body summary). If rationale is genuinely empty, record DEFER under Rule 18 instead — preserves the throwaway-edge-case safety without tightening the common case.
> • **Finding 2 (ps1 validation weak):** Step 10 now runs `pwsh -NoProfile -Command "[Parser]::ParseFile(...)"` when pwsh is on PATH. Falls back to operational-record check if pwsh unavailable. Acceptance language distinguishes parse-verified from fallback-only.
> • **Finding 3 (Precondition 4 auto-unstage = unauthorized index mutation):** Both Precondition 4 and Step 1 now ABORT + escalate if a slice-target file is already staged. No `git restore --staged` auto-fix. Executor has no authority to mutate a dirty index in a shared tree.
> • **Finding 4 (py_compile overclaim):** Step 10 replaces `python -m py_compile` with real `python -c "import tests.<name>"` (PEP 420 namespace package; no `tests/__init__.py` required). Now catches ImportError, SyntaxError, and module-level exceptions — not just parse errors.
> • **Finding 5 (Rule 3 rule-list drift):** Rule 3 now references Rules 2, 4 (a/b/c), 4b, 17a — matches the actual Python TRACK-candidate set. Prior erroneous "Rule 15" reference (screenshots) removed. Recursive-transitive behavior made explicit (apply to fixed point).
>
> **Operational note (shell).** Codex round 5 confirmed default shell in Tate's workspace is PowerShell; `bash` not on PATH at check time. Precondition 0 already hard-gates on `bash --version`; expected failure mode is executor opens Git Bash / WSL and re-launches. No slice change required — behavior is correct.
>
> **v5.2 — Codex round-4 fixes (preserved):** Codex round 4 reviewed v5.1 and found 3 High + 1 Medium + 1 Low issue. All folded; atomic shape preserved.
> • **Finding 1 (TRACK-all subtrees tripped unified machine-path / non-LAN-URL HARD STOP):** URL regex narrowed to FQDN-shape only (excludes template fragments like `http://{args.host}` in `tools/sidecar-viewer/server.py:169` and `"http://" + config.host` in `litert-host-jvm/src/.../LiteRtOpenAiServer.java:72`). Machine-path policy refined: HARD STOP for CODE files (`.py`, `.ps1`, `.java`, `.js`, `.ts`, `.go`, `.c`, `.cpp`, `.sh`, `.kt`, `.rs`); surface-only for MARKDOWN / plaintext (`.md`, `.txt`) where dev-note path references are benign operational context (e.g., `uiplanning/IMPLEMENTATION_LOG_20260413.md:1480` cites `C:\Users\tateb\Downloads\gemma-4-E2B-it.litertlm` — operational, not a leak).
> • **Finding 2 (Rule 4.c fix absent from Step 5):** Step 5 step 4 now uses two-pass grep (quoted-path + Path-construction) matching the v5.1 Rule 4.c definition. Explicit TRACK overrides for `tests/test_delta_prompt_scripts.py` and `tests/test_run_guide_prompt_validation_harness.py` restated inside Step 5 so it's standalone.
> • **Finding 3 (hash / staging not whitespace-safe):** Step 8 / Step 11 hash-manifest loops now read from a NUL-delimited TRACK list (`artifacts/R-track1_track_list_20260422.nul`) via `while IFS= read -r -d ''`. Step 9 staging uses `xargs -0 -a <list> git add --` instead of word-splitting `<explicit-paths>`. Handles paths with spaces like `uiplanning/Technical Architecture for Autonomous Multi-Model Engineering Swarms_ ... Scouts.md`.
> • **Finding 4 (acceptance overclaims Step 10 scope):** Step 10 now includes a py_compile pass over every TRACK-dispositioned `tests/*.py` (BLOCKING). Acceptance §Import-smoke split into two bullets: root + scripts import-smoke and tests py_compile.
> • **Finding 5 (stale ps1 counts):** Pre-state re-verified 2026-04-22: `scripts/*.ps1` = 5 tracked (not 2), 37 untracked (not ~43). Hard lower bound updated to `>=14`.
>
> **v5.1 — Codex round-3 fixes (preserved):** Codex round 3 reviewed v5 and found 4 High + 3 Medium issues. All folded; atomic shape preserved; no strategic re-scoping required.
> • **Finding 1 (hash acceptance said `post_commit` but Step 11 says `post_stage`):** acceptance §Hash invariance now reads `post_stage_hashes` (unified with Step 11). Optional post-commit spot-check added as a single-file sanity, not BLOCKING.
> • **Finding 2 (Precondition 4 executable regressed):** Step 1 now runs `git diff HEAD -- …` (catches staged + unstaged) plus a `git diff --cached --name-only` sanity check that hard-aborts if any slice-target file is already staged.
> • **Finding 3 (Rule 4.c misses Path-construction tests):** Rule 4.c grep pattern broadened to catch `REPO_ROOT / "scripts" / "<name>.py"` idiom (the real style in `tests/test_delta_prompt_scripts.py:11-12` and `tests/test_run_guide_prompt_validation_harness.py:9`). Belt-and-suspenders: both known tests added as explicit TRACK overrides so pattern-miss cannot drop them.
> • **Finding 4 (machine-path policy contradiction):** unified — machine-specific paths HARD STOP for TRACK-dispositioned files; surface-but-continue (logged only) for IGNORE/DEFER items that never enter the tree. Rule 2b's PowerShell rule restated as a specific instance of the unified rule.
> • **Finding 5 (carry-over entries not acceptance-anchored):** new acceptance block "Carry-over integrity" greps for each lettered entry (a–h, j) in `CP9_ACTIVE_QUEUE.md` post-commit.
> • **Finding 6 (file-level ignore patterns not acceptance-anchored):** new acceptance block "File-level ignore anchors" runs `git check-ignore -v` on `.mcp.json`, `CLAUDE.md`, `.codex_*`, `test_startprocess_*.txt`.
> • **Finding 7 (scope wording self-contradictory on `notes/`):** Scope §NO-sweeps clause now explicitly enumerates the permitted `notes/` touches (new disposition report + tracker edits); closes the apparent contradiction with Scope item 6.
>
> **v5 — full scope per Tate directive ("i want everything"):** Both Codex round-2 strategic findings resolved as GO. Scope expands from Python-only to Python + AGENTS-named `.ps1`. Rule 4.c added for subprocess/string-path tests. Atomic shape preserved.
>
> **v5 additions over v4:**
> • **Finding 2 (ps1 scope) — ACCEPTED.** 9 AGENTS-named untracked `.ps1` in `scripts/` now in scope: `push_mobile_pack_to_android.ps1` (AGENTS:53), `run_android_instrumented_ui_smoke.ps1` is already tracked, `run_android_prompt.ps1` (AGENTS:55), `run_android_search_log_only.ps1` (AGENTS:56), `run_android_ui_validation_pack.ps1` (AGENTS:57), `run_guide_prompt_validation.ps1` (AGENTS:61), `start_senku_emulator_matrix.ps1` (AGENTS:58), `start_senku_device_mirrors.ps1` (AGENTS:60), `start_qwen27_scout_job.ps1` (AGENTS:66), `get_qwen27_scout_job.ps1` (AGENTS:67). Rule 2 expanded to cover AGENTS-named non-`.py` scripts. New Step 4b processes `scripts/*.ps1` category.
> • **Finding 4b (subprocess/string-path tests) — ACCEPTED.** New Rule 4.c: `grep -E "['\"]scripts/[a-z_0-9]+\\.(py|ps1)['\"]" <test>` — tests that reference a tracked/TRACK-candidate script via string-path (including subprocess invocations) TRACK. Known examples: `tests/test_delta_prompt_scripts.py` → `scripts/build_delta_prompt_pack.py`, `scripts/merge_structured_prompt_packs.py`; `tests/test_run_guide_prompt_validation_harness.py` → `scripts/run_guide_prompt_validation.ps1`. Hard-anchor list extended to include these tests. Carry-over entry (i) retired.
> • **Hard anchors +9 ps1 + 2 subprocess tests.** Acceptance verifies AGENTS-named ps1 + subprocess-path tests all land in the commit.
> • **Commit scope: ~75 → ~86 files** (9 ps1 + 2 subprocess-path tests, maybe +1-2 string-path hits from Rule 4.c sweep).
>
> **v4 — post Codex round-2 uncontroversial fixes (preserved):** Codex round 2 found 5 issues post-v4 fold. Four folded in v4.1 (now rolled into v5 baseline):
> • **Finding 1 (ordering impossibility):** post-commit verification cannot be inside a committed report. Fixed by switching Step 11 to POST-STAGE verification (`git rev-parse :<file>` on staged blob == committed blob by construction). Atomic shape preserved.
> • **Finding 3 (secret-scan false-STOPs):** `token` pattern tightened to authorization-shape (`api_token`, `access_token`, `bearer <N-chars>`, etc.); LAN URLs (10.x, 192.168.x, 172.16-31.x, 127.x, localhost) moved from hard-STOP to surface-and-continue.
> • **Finding 4a (Rule 17a leaks into test fall-through):** Step 5 now explicitly forbids Rule 17a for tests; `unittest.main()` footers are not CLI-entrypoint signals.
> • **Finding 5 (Precondition 4 unstaged-only):** now uses `git diff HEAD --` to catch staged + unstaged.
>
> **v3.5 → v4 (prior fold, Codex round 1):** Substantive revision after post-scout Codex sanity check. Codex (executor) reviewed v3.5 and surfaced 5 findings that v3's scout pass did not. All correctness/completeness issues within the atomic shape. Changes from v3.5:
> 1. **NEW Precondition 0 — shell requirement.** Slice is bash-only (Git Bash on Windows, bash on POSIX). PowerShell is NOT supported for this slice. `awk`, `sed`, heredoc `$(cat <<'EOF'...)`, and `/tmp`-path semantics are used throughout. Open Git Bash and launch Codex from there, or use WSL.
> 2. **Rule 2 source list expanded** to include AGENTS-named `scripts/export_mobile_pack.py` (AGENTS.md:51, mobile pack export entry point) and `scripts/validate_special_cases.py` (AGENTS.md:92, deterministic citation/routing guard). Previously v3.5 relied on Rule 17a fallback for these, which Finding 3 showed is fragile.
> 3. **Rule 17a CLI-shape detection loosened** from `__main__` + (argparse|click|sys.argv) to `__main__` guard alone. `validate_special_cases.py` and `validate_bench_retry_slots.py` have `__main__` but call a no-arg `main()`; the argparse requirement false-DEFERred them. Any top-level `if __name__ == "__main__":` guard is a CLI-entrypoint signal.
> 4. **Rule 4 broadened** from strict filename-pair (`tests/test_<X>.py` ↔ `<X>.py`) to import-based: a test that imports any tracked-or-TRACK-candidate first-party module TRACKs. Finding 4 surfaced 12+ tests (e.g., `test_query_routing.py` imports `query`, `test_confidence_label.py` imports `confidence_label_contract`, `test_bench_prompt_loading.py` imports `bench`, `test_special_cases.py` imports `query`/registry/catalog) that the strict rule would false-DEFER. **Scope impact: expected TRACK-set for `tests/` expands from roughly 8 exact-sibling matches to 25-30 of the 27 untracked tests.**
> 5. **Hash-story consistency cleanup** — Disposition report §2/§3 spec, Step 2 enumeration output, and commit template all updated to blob-hash language (`git hash-object` / `git rev-parse HEAD:<file>`) instead of `sha256`. Intermediate enumeration temp-file moved from `/tmp/` to `artifacts/` for Windows-safety.
> 6. Hard-anchor list extended with `scripts/export_mobile_pack.py` and `scripts/validate_special_cases.py` (Rule 2 AGENTS-named additions).
>
> **v3 → v3.5 (prior, retained for audit trail):** scout round 3 GO-WITH-EDITS. Hash mechanism (`git hash-object`), Rule 17 retired as ordered rule, Rule 17a added as CLI escape hatch, Step 10 cwd-explicit, +3 hard anchors (`bench_artifact_tools.py`, `confidence_label_contract.py`, `summarize_latency.py`), Step 13 path-explicit carry-over.


- **Role:** main agent (`gpt-5.4 xhigh`). Heavy context work: enumerate every untracked item at repo root + `scripts/` + `tests/` + three source-like subprojects (`litert-host-jvm/`, `tools/`, `uiplanning/`), categorize, secret-scan, decide disposition (TRACK / IGNORE / DELETE / DEFER), execute atomically. Single commit. Optional worker fanout (`gpt-5.4 high`) for per-file secret scanning if main wants parallelism.
- **Execution surface:** Git Bash or WSL only. AGENTS.md includes a normal PowerShell quick start for day-to-day repo work, but this slice is intentionally bash-only and is not runnable from plain PowerShell / `cmd` without a full rewrite.
- **Paste to:** **new window** (fresh session from this file).
- **Parallel with:** Nothing else that edits `notes/CP9_ACTIVE_QUEUE.md`, `notes/dispatch/README.md`, `.gitignore`, or adds files at repo root / `scripts/` / `tests/`. This slice is large-surface; treat it as exclusive lane while in flight.
- **Predecessor context:** R-hygiene2 landed at `5fb7719`, inadvertently tracking `scripts/refresh_mobile_pack_metadata.py` (previously untracked). Planner surveyed the broader tracking state 2026-04-22 afternoon and found **61 untracked `.py` files vs 14 tracked** across the repo, plus 25 root-level non-`.py` items, 7 in-scope untracked directories, and 3 source-like subprojects (`litert-host-jvm/`, `tools/`, `uiplanning/`). Scout round 2 (see `R-track1_SCOUT_PROMPT.md`) surfaced 5 BLOCKINGs on v2 that this v3 revision addresses: (1) Rule 5 unreachable under first-match ordering, (2) script-test pairing gap, (3) missing candidates (`litert-host-jvm/`, `tools/`, `uiplanning/`, `test_prompts.txt`, `test_startprocess_*.txt`), (4) `.mcp.json` Rule 14 misclassification (AGENTS names `opencode.json` not `.mcp.json`; `.mcp.json:11` has machine-specific path), (5) acceptance criteria self-referential and can't prove no content edits. The bundle name `senku_local_testing_bundle_20260410` suggests a partial testing bundle, but R-hygiene2's tracked additions now import from `metadata_validation.py` (untracked) — fresh-clone ImportError exposure is real. Tate authorized atomic resolution.

## Pre-state (verified by planner 2026-04-22, expanded per scout round-2 findings)

### Python file tracking coverage

| Location | Tracked | Untracked | Candidate list |
| --- | --- | --- | --- |
| Repo root | 5 (`bench.py`, `ingest.py`, `metadata_helpers.py`, `mobile_pack.py`, `query.py`) | **10** | Below, §Root `.py` candidates |
| `scripts/*.py` | 1 (`refresh_mobile_pack_metadata.py` — R-hygiene2) | **24** | Below, §`scripts/` candidates |
| `tests/*.py` | 8 | **27** | Below, §`tests/` candidates |
| **Python total** | **14** | **61** | |

### PowerShell + shell script tracking coverage (v5.4 — per Codex round-6 Finding 2)

| Location | Tracked | Untracked | In-scope subset |
| --- | --- | --- | --- |
| `scripts/*.ps1` | 5 (`build_android_ui_state_pack.ps1`, `build_android_ui_state_pack_parallel.ps1`, `run_android_instrumented_ui_smoke.ps1`, `run_android_session_flow.ps1`, `run_overnight_queue_wrapped.ps1`) | **37** | **9 AGENTS-named (Rule 2b — v5);** ~28 non-AGENTS-named → carry-over (j) follow-up slice |
| `scripts/*.psm1` | 0 | 1 (`android_harness_common.psm1`) | **1 TRACK** (Rule 2c — v5.4) — imported by tracked + TRACK-candidate ps1 |
| `scripts/*.sh` | 0 | 4 (`kill_bench_orphans.sh`, `rebuild_venv_if_needed.sh`, `run_bench_guarded.sh`, `verify_local_runtime.sh`) | **2 TRACK** confirmed via Rule 2d (`verify_local_runtime.sh` at `README_OPEN_IN_CODEX.md:45`, `rebuild_venv_if_needed.sh` at `README_OPEN_IN_CODEX.md:51`); other 2 evaluated per cross-reference during Step 6.5 |

### Root-level non-`.py` items (confirmed enumeration)

**Files:**
- Local state markers: `.codex_rgal1_flake_dir.txt`, `.codex_stage2_rerun2_root.txt`
- MCP config (MACHINE-SPECIFIC): `.mcp.json` — line 11 hardcodes `C:\\Users\\tateb\\Documents\\senku_local_testing_bundle_20260410`. **NOT TRACK-able as-is.** AGENTS.md line 80 names `opencode.json` (separate, tracked) as the MCP config of record. `.mcp.json` is local-only.
- Zip backups: `4-13guidearchive.zip`, `guides.zip`
- Mirror of AGENTS.md: `CLAUDE.md`
- State/snapshot docs: `CURRENT_LOCAL_TESTING_STATE_20260410.md`, `LM_STUDIO_MODELS_20260410.json`, `UI_DIRECTION_AUDIT_20260414.md`
- Forward-looking project docs: `GUIDE_PLAN.md` (AGENTS.md-named: "living guide-direction backlog"), `MIGRATION_LLAMACPP.md`, `README_OPEN_IN_CODEX.md`
- Audit markdown: `auditglm.md`, `gptaudit4-21.md`, `senku_mobile_mockups.md`
- AGENTS-named non-code asset: **`test_prompts.txt`** (AGENTS.md line 93 names it as the current prompt suite) — **TRACK** per the new AGENTS-asset rule.
- Scratch process-output: `test_startprocess_err.txt` (empty), `test_startprocess_out.txt` (one-line "hello") — scratch debugging artifacts → IGNORE or DELETE.
- Screenshots (6): `senku_answer_detail_*.png`, `senku_first_launch_*.png`, `senku_home_screen_*.png`, `senku_model_loaded_*.png`, `senku_model_not_loaded_*.png`, `senku_search_results_*.png`
- Dependencies: `requirements.txt`

### Untracked directories (confirmed sizing)

| Directory | Files | Size | Disposition signal |
| --- | --- | --- | --- |
| `.serena/` | 5 | 854K | IGNORE — MCP state |
| `archive/` | 4041 | **1.9 GB** | IGNORE — historical |
| `artifacts/` | **87,715** | **63 GB** | IGNORE — bench artifacts |
| `chroma_db/` | 1 | 184K | IGNORE — runtime DB |
| `db/` | 9 | **617 MB** | IGNORE — runtime DB |
| `models/` | 3 | **6.3 GB** | IGNORE — model binaries |
| `android-app/.kotlin/` | 2 | 16K | IGNORE — Kotlin build cache |

### Source-like subprojects (NEW — scout round 2 surfaced)

| Subproject | Content | Disposition signal |
| --- | --- | --- |
| `litert-host-jvm/` | Real source: `build.gradle`, `settings.gradle`, `src/main/java/com/senku/host/LiteRtOpenAiServer.java`. Also `.gradle/` subtree (build cache). | TRACK source, IGNORE `litert-host-jvm/.gradle/` + `litert-host-jvm/build/` + `litert-host-jvm/bin/` |
| `tools/` | Subdir `tools/sidecar-viewer/` with `app.js`, `contract.js`, `index.html`, `server.py`, `styles.css` — real source utility | TRACK the whole `tools/sidecar-viewer/` subtree |
| `uiplanning/` | 12 planning docs (`CURRENT_STATE.md`, `DECISIONS.md`, `IMPLEMENTATION_LOG_*.md`, `SCREEN_SPECS*.md`, `UI_TODO*.md`, etc.), 408K | TRACK all (project planning docs, Rule 6a) |

### `guides/` and `notes/` content (partial-tracked trees) — OUT OF SCOPE

`guides/` and `notes/` have existing partial tracking with many new untracked entries. Explicitly excluded from this slice; handled in separate follow-up slices. The slice must NOT add or ignore anything inside these trees beyond the specific new disposition report it writes.

### AGENTS.md-named Python source (authoritative list for Rule 2 — v4 expanded)

Verified by planner 2026-04-22 by reading AGENTS.md in full. **v4 correction:** v3.5 omitted two AGENTS-named `scripts/*.py` entries; both are added below with citations.

- **Core Entry Points** (§Core Entry Points): `config.py` (AGENTS.md:35), `query.py` (:36), `bench.py` (:37), `ingest.py` (:38), `deterministic_special_case_registry.py` (:39), `special_case_builders.py` (:40)
- **Shared support** (§Shared support): `lmstudio_utils.py` (:45), `guide_catalog.py` (:46), `token_estimation.py` (:47)
- **Mobile** (§Core Entry Points / Mobile): **`scripts/export_mobile_pack.py`** (AGENTS.md:51, "mobile pack export entry point") — **v4 addition**
- **Testing** (§Testing): **`scripts/validate_special_cases.py`** (AGENTS.md:92, "Deterministic citation/routing guard") — **v4 addition**; `test_prompts.txt` (:93, non-`.py` asset)

Currently untracked of these (all TRACK per Rule 2): `config.py`, `deterministic_special_case_registry.py`, `special_case_builders.py`, `lmstudio_utils.py`, `guide_catalog.py`, `token_estimation.py`, `scripts/export_mobile_pack.py`, `scripts/validate_special_cases.py`, `test_prompts.txt`. (`bench.py`, `ingest.py`, `query.py`, `mobile_pack.py` are already tracked; `refresh_mobile_pack_metadata.py` tracked via R-hygiene2.)

### Current `.gitignore` state

HEAD `.gitignore` covers build caches, IDE metadata, `.claude/`, temp dirs, `venv/`, `__pycache__/`, `*.pyc`, `.pytest_cache/`, `.mypy_cache/`, `.ruff_cache/`, android-specific build output, and top-level `.gradle/` (which already covers `litert-host-jvm/.gradle/`). No existing rules for `archive/`, `artifacts/`, `chroma_db/`, `db/`, `models/`, `.serena/`, `litert-host-jvm/build/`, `litert-host-jvm/bin/`, `.mcp.json`, or the scratch `test_startprocess_*.txt`.

### Known urgent constraint

`metadata_validation.py` (untracked) is imported by tracked `ingest.py:29-35`, tracked `mobile_pack.py:23-27`, and tracked `scripts/refresh_mobile_pack_metadata.py:21-25`. Fresh-clone -> ImportError. **metadata_validation.py MUST be in the final commit's TRACK set.**

## Scope

- **One commit** covering:
  1. `git add` for every TRACK-dispositioned file / subtree (Python at repo root, `scripts/*.py`, `scripts/*.ps1` AGENTS-named subset (v5), `tests/*.py`, `litert-host-jvm/` source, `tools/sidecar-viewer/`, `uiplanning/`, `test_prompts.txt`, specific root docs/config).
  2. `.gitignore` additions for every directory/pattern determined IGNORE-dispositioned, with one-line comments per entry.
  3. File deletions only for scratch process-output if DELETE is chosen over IGNORE (expected: `test_startprocess_*.txt` candidates).
  4. New disposition report at `notes/R-TRACK1_HYGIENE_REPORT_20260422.md`.
  5. Generated hash-manifest artifacts under `artifacts/` for the pre-add / post-stage verification flow (`artifacts/R-track1_pre_commit_hashes_20260422.txt`, `artifacts/R-track1_post_stage_hashes_20260422.txt`, and the NUL track list). These `artifacts/` files are NOT committed; their contents are embedded in `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` per §Work Steps 8 and 11.
  6. Tracker updates to `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md`.
- **NO** content changes to any TRACK-dispositioned file. Pure tracking action. The executable proof below is the Step 8 -> Step 11 blob-hash comparison, which guarantees no post-capture content drift before commit; it is not a retroactive proof about anything that happened earlier in the slice. (Optional post-commit spot-check remains belt-and-suspenders only; it is not the primary invariant.)
- **NO** new-file additions or ignore-rule sweeps into `guides/` or `notes/` content trees (v5.1 clarification per Codex round-3 Finding 7). The only permitted `notes/` touches are: (a) the new disposition report `notes/R-TRACK1_HYGIENE_REPORT_20260422.md`, and (b) modifications to already-tracked tracker files `notes/CP9_ACTIVE_QUEUE.md` and `notes/dispatch/README.md` (Scope item 6 above). No `notes/*.md` content-tracking, no `guides/*.md` additions, no `.gitignore` additions targeting `guides/` or `notes/` subtrees.
- **NO** touching `android-app/` tracked tree.

## Preconditions (HARD GATE — STOP if violated)

0. **Shell is bash.** Verify `bash --version` succeeds and prints a GNU bash banner. If the shell is PowerShell, `cmd`, or Windows Command Prompt, **STOP**. Re-launch Codex from a Git Bash session (on Windows: "Git Bash" shortcut from Git for Windows) or from WSL. This slice uses `awk`, `sed`, `git` pathspecs, process substitution, and heredoc commit messages — none portable to PowerShell without rewrite.
1. HEAD descends from `5fb7719` (R-hygiene2). Verify with `git merge-base --is-ancestor 5fb7719 HEAD`.
2. `git status --short | awk '$1=="??"' | grep -cE "\.py$"` returns >=61. If drastically lower, STOP.
3. `git ls-files -- "*.py" | wc -l` returns >=14. If drastically lower, STOP.
4. **No uncommitted changes (staged OR unstaged) to files this slice will modify.** Specifically: `git diff HEAD -- .gitignore notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md` must return empty (v4 — uses `HEAD` to cover both the staging area and working tree). **Pre-existing modifications to `AGENTS.md`, `opencode.json`, or `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` (deleted) are EXPECTED and do NOT block** — they predate this slice, out of scope, and will remain dirty post-commit. The slice does NOT stage them.

**Staged slice-target check (v5.3 — ABORT, do NOT auto-unstage per Codex round-5 Finding 3).** Run `git diff --cached --name-only | grep -E "^(\.gitignore|notes/CP9_ACTIVE_QUEUE\.md|notes/dispatch/README\.md)$"`. If any match: **STOP immediately, escalate to planner. Do NOT run `git restore --staged`.** Auto-unstaging mutates the user's index state in a dirty shared tree and may clobber in-progress work. The executor has no authority to unstage; a staged slice-target file means the slice's preconditions are actually in conflict with concurrent work, which is a planner-resolvable issue.
5. `notes/R-PACK-DRIFT_INVESTIGATION_20260422.md` exists (forensic record; preserve).
6. `scripts/refresh_mobile_pack_metadata.py` is tracked (from R-hygiene2). Verify: `git ls-files --error-unmatch -- scripts/refresh_mobile_pack_metadata.py >/dev/null`.

## Outcome

### Disposition report structure

New file `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` with sections:

1. **Summary** — counts table (tracked / ignored / deleted / deferred) and must-track anchor list confirming each required file landed.
2. **Pre-commit hash manifest** — `git hash-object` (git blob sha-1, CRLF-normalized) of every TRACK-dispositioned file captured before `git add`. Embedded as a fenced block in the report (rendered, not external). v4: sha256 language retired; blob-hash mechanism is canonical.
3. **Post-stage hash verification** — `git rev-parse :<file>` read for every TRACK-dispositioned path after `git add` but BEFORE `git commit`; must match the Step 8 pre-add manifest line-by-line. Staged blob hash == committed blob hash by construction, so post-stage verification can be written into the report BEFORE the commit that contains the report. This proves the Step 8-captured TRACK contents did not drift before commit; it does NOT retroactively prove nothing was edited earlier in the slice. Report states "all match" or lists any differences (differences = post-capture content drift -> acceptance fail).
4. **Categories and decision rules applied** — the 18-rule set with any judgment calls documented.
5. **Per-item disposition** — table (path, category, rule number, decision, one-line reason). Group by category.
6. **Secret-scan outcome** — pattern list applied + clean confirmation per file (or specific flags).
7. **Deferred items** — dedicated section per DEFER with reason + unblock condition.
8. **Carry-over to backlog** — items for follow-up slices (e.g., sidecar YAML gap, `guides/` content, `notes/` content, screenshots deferred for visual-content review).

### Decision rules (v3 — revised per scout round 2)

Apply in order. First match wins, EXCEPT Rule 5a is an additive carry-over rule (see below). Record rule-number applied per item.

**Rule 1 — Hard secrets = STOP.** Unambiguous secrets (real API keys, passwords, tokens, private keys including PEM-encoded, customer identifiers) → STOP and flag for Tate.

**Rule 2 — AGENTS.md-named source `.py` = TRACK.** Listed files per §Pre-state § AGENTS.md-named Python source. TRACK unless Rule 1 fires.

**Rule 2a — AGENTS.md-named non-`.py` asset = TRACK.** `test_prompts.txt` (AGENTS line 93). TRACK unless Rule 1 fires. Apply the same secret/machine-path scan as for code.

**Rule 2b — AGENTS.md-named `.ps1` operator script = TRACK (v5 — NEW per Tate directive).** Any `scripts/*.ps1` named in AGENTS.md is a canonical operator tool. TRACK unless Rule 1 fires. Authoritative list (9 untracked candidates at slice-drafting time): `scripts/push_mobile_pack_to_android.ps1` (AGENTS.md:53), `scripts/run_android_prompt.ps1` (:55), `scripts/run_android_search_log_only.ps1` (:56), `scripts/run_android_ui_validation_pack.ps1` (:57), `scripts/start_senku_emulator_matrix.ps1` (:58), `scripts/start_senku_device_mirrors.ps1` (:60), `scripts/run_guide_prompt_validation.ps1` (:61), `scripts/start_qwen27_scout_job.ps1` (:66), `scripts/get_qwen27_scout_job.ps1` (:67). Secret-scan each with the v4 pattern set (machine-specific paths are a realistic risk in PowerShell scripts — flag hard-STOP on `C:\\Users\\tateb` hits).

**Rule 2c - PowerShell module `scripts/*.psm1` imported by tracked-or-TRACK-candidate ps1 = TRACK (v5.8 - resolve against the validated TRACK ledger in Step 6.5).** Enumerate untracked `.psm1` via `git status --short | awk '$1=="??"' | grep -E "^\\?\\? scripts/.+\\.psm1$"`. Secret-scan in Step 4c immediately, but do not finalize TRACK / DEFER there. Final Rule 2c resolution in Step 6.5 scans BOTH the already-tracked corpus AND `artifacts/R-track1_track_paths_validated_20260422.txt`, which contains only files that have already passed Rule 1 + category disposition. This catches the common PowerShell pattern of assigning the module path to a variable (e.g., `$commonHarnessModule = Join-Path $PSScriptRoot "android_harness_common.psm1"` then `Import-Module $commonHarnessModule`), which `Import-Module.*<basename>` regex misses.

Operational detection (v5.5):

```bash
# Basename-match grep — catches both direct Import-Module and variable-indirection patterns.
BASENAME_FULL="${f##*/}"                 # e.g., android_harness_common.psm1
git grep -lF -e "${BASENAME_FULL}" 2>/dev/null
BASENAME_NO_EXT="${f##*/}"; BASENAME_NO_EXT="${BASENAME_NO_EXT%.psm1}"
# v5.8 note: Pass 2 reads the validated TRACK ledger built in Step 6.5.
# Only files that have already passed Rule 1 / category disposition may act as
# referrers here; do not seed speculative future TRACK paths.
while IFS= read -r candidate; do
  grep -lF "${BASENAME_NO_EXT}.psm1" "$candidate" 2>/dev/null
done < artifacts/R-track1_track_paths_validated_20260422.txt
```

Known v5.5 anchor: **`scripts/android_harness_common.psm1`** — string `"android_harness_common.psm1"` appears in already-tracked `scripts/run_android_instrumented_ui_smoke.ps1:47` and `scripts/build_android_ui_state_pack.ps1:24`, plus TRACK-candidate `scripts/run_android_prompt.ps1:38` and `scripts/run_android_ui_validation_pack.ps1:43`. The basename-grep hits these cleanly. Without this rule, AGENTS-named ps1 scripts would import a module that fails to resolve on fresh clone.

**Rule 2d - Shell helper `scripts/*.sh` referenced from tracked docs or tracked/TRACK-candidate scripts = TRACK (v5.8 - cross-candidate scan uses the validated TRACK ledger from Step 6.5, not a speculative Step-4c seed or the later Step-8 NUL list).** Enumerate untracked `.sh` via `git status --short | awk '$1=="??"' | grep -E "^\\?\\? scripts/.+\\.sh$"`. Two-pass scan because same-slice TRACK candidates are not all in `git ls-files`:

```bash
# Pass 1: grep tracked corpus for the sh basename.
git grep -lF -e "scripts/${BASENAME}" 2>/dev/null

    # Pass 2 - grep the validated TRACK ledger built in Step 6.5.
    # Only files that have already cleared Rule 1 / category disposition may
    # act as referrers for support-file promotion.
while IFS= read -r candidate; do
  grep -lF "scripts/${BASENAME}" "$candidate" 2>/dev/null
done < artifacts/R-track1_track_paths_validated_20260422.txt
```

If EITHER pass hits, TRACK. In v5.8 the authoritative anchors are resolved in Step 6.5 against the validated ledger, not a speculative Step-4c seed: **`scripts/verify_local_runtime.sh`** (`README_OPEN_IN_CODEX.md:45`), **`scripts/rebuild_venv_if_needed.sh`** (`README_OPEN_IN_CODEX.md:51`). Known cross-candidate dependency (Pass 2): **`scripts/kill_bench_orphans.sh`** is referenced from `scripts/run_bench_guarded.sh:24` ("Run: zsh scripts/kill_bench_orphans.sh"). If `run_bench_guarded.sh` itself TRACKs via Pass 1 or any earlier Step 6.5 decision, append it to the validated ledger immediately so `kill_bench_orphans.sh` can resolve in the same closure loop. If `run_bench_guarded.sh` does NOT track, neither does `kill_bench_orphans.sh` - record both as DEFER candidates with cross-reference annotation.

Apply the v4 secret-scan (shell scripts can carry machine paths + LAN URLs).

**Rule 3 — Imported by tracked or about-to-track code = TRACK (v5.3 — rule-list corrected per Codex round-5 Finding 5).** Grep for `import <module>` / `from <module>` across BOTH currently-tracked `.py` AND currently-untracked `.py` that are themselves TRACK candidates under ANY of Rules 2, 4 (a/b/c), 4b, or 17a. (v5.3 corrections: prior versions listed Rule 15, which is about screenshots — unrelated to Python TRACK; also omitted Rule 17a, which is a primary Python-TRACK rule. Both fixed here.) Rule 3 is transitively recursive: if module A tracks via Rule 3 by being imported by B (which tracks via Rule 17a), and A itself imports C, then C also tracks via Rule 3.

**v5.8 operational override:** Maintain a newline-delimited raw TRACK ledger at `artifacts/R-track1_track_paths_raw_20260422.txt` throughout Steps 3–7, plus pending recheck ledgers for first-pass non-TRACK repo-root / `scripts/*.py` candidates. After Step 6, run a dedicated closure pass (Step 6.5) that re-checks those pending `.py` paths plus Step-4c support-file candidates against the validated ledger derived from the raw file. Rule 3 is not complete until the Step 6.5 loop reaches fixed point; the single-pass category scans are necessary but not sufficient.

**Rule 4 — Test file in `tests/` that exercises tracked-or-TRACK-candidate first-party code = TRACK (v5 — three branches).** Fires if ANY of:
- (4.a, original) For `tests/test_<X>.py`, a matching `<X>.py` exists on disk at repo root OR under `scripts/` (on-disk state, not git); OR
- (4.b, v4) The test file's `import`/`from` lines include any first-party module that is tracked OR TRACK-candidate in this slice. First-party modules are any `.py` at repo root (tracked: `bench`, `ingest`, `metadata_helpers`, `mobile_pack`, `query`; TRACK-candidates: `config`, `guide_catalog`, `deterministic_special_case_registry`, `special_case_builders`, `lmstudio_utils`, `token_estimation`, `metadata_validation`, `bench_artifact_tools`, `confidence_label_contract`, `summarize_latency`) OR `scripts.<name>` / `from scripts.<name>` referring to a TRACK-candidate script.
- (4.c, v5.1 — broadened for Path-construction idiom per Codex round-3 Finding 3) The test file contains a string-path reference to any tracked-or-TRACK-candidate first-party script. Match fires on ANY of these string forms:
  - Quoted full path: `"scripts/<name>.(py|ps1)"` or `'scripts/<name>.(py|ps1)'`.
  - `pathlib` construction: `/ "scripts" /` or `"scripts" /` followed on the same or next line by `"<name>.(py|ps1)"`.
  - Subprocess arg list containing `"scripts"` and a `.py`/`.ps1` literal in the same argv.
- Grep pattern (v5.1 broadened): `grep -nE "['\"]scripts/[a-z_0-9]+\\.(py|ps1)['\"]|/ *['\"]scripts['\"] */" <test_file>`. Any match triggers a follow-up scan: `grep -nE "['\"][a-z_0-9]+\\.(py|ps1)['\"]" <test_file>` to find the companion filename literal. Cross-reference the concatenated path against the TRACK-candidate set.
- **Explicit overrides (belt-and-suspenders, v5.1).** Regardless of pattern-match outcome, the following two tests TRACK because of known string-path references that use `Path`-construction idioms the regex may fail to capture:
  - `tests/test_delta_prompt_scripts.py` — references `scripts/build_delta_prompt_pack.py` (line 11) and `scripts/merge_structured_prompt_packs.py` (line 12) via `REPO_ROOT / "scripts" / "<name>.py"`.
  - `tests/test_run_guide_prompt_validation_harness.py` — references `scripts/run_guide_prompt_validation.ps1` (line 9) via `REPO_ROOT / "scripts" / "run_guide_prompt_validation.ps1"`.

Rationale: many real tests use descriptive names that don't sibling-match (e.g., `test_query_routing.py` → imports `query`; `test_confidence_label.py` → imports `confidence_label_contract`; `test_special_cases.py` → imports `query`/registry/catalog). Strict sibling-matching would false-DEFER ~12 legitimate first-party tests.

Operational check: `grep -oE "^(import|from) [a-z_][a-z_0-9.]*" <test_file>` collects full dotted module references. Strip the leading `import ` / `from ` token, then match each module name against the combined tracked + TRACK-candidate set. If any first-party match (including `scripts.<name>`), TRACK.

**Rule 4b — `scripts/*.py` with matching `tests/test_<x>.py` = TRACK (script side).** If `scripts/<X>.py` has a sibling `tests/test_<X>.py` on disk (regardless of current tracking state), TRACK the script. Addresses scout finding on `regenerate_deterministic_registry`, `build_guide_graph`, `extract_guide_invariants`, `find_guide_audit_hotspots`, and any similar pairs.

**Rule 5 — Generator output: TRACK under Rules 2/3 if they fire, BUT always add Rule-5a carry-over.** `deterministic_special_case_registry.py` is AGENTS-named (Rule 2) AND imported (Rule 3), so it TRACKs regardless. The sidecar-gap notation must happen independently.

**Rule 5a — Override: generator-output → sidecar check regardless of TRACK rule.** For `deterministic_special_case_registry.py`: verify `notes/specs/deterministic_registry_sidecar.yaml` tracking status. If untracked, add explicit carry-over entry: "sidecar YAML untracked; registry drift possible unless sidecar is tracked or discipline locked elsewhere." This runs even though Rule 2/3 already caused TRACK. Independent notation, not a TRACK override.

**Rule 6 — Source-like subproject directory = TRACK source subtree, IGNORE its build/cache subdirs.** Targets: `litert-host-jvm/` (TRACK `build.gradle`, `settings.gradle`, `src/**`; IGNORE `litert-host-jvm/.gradle/`, `litert-host-jvm/build/`, `litert-host-jvm/bin/`), `tools/sidecar-viewer/` (TRACK all source files). Reuse the existing top-level `.gradle/` ignore for `litert-host-jvm/.gradle/`; add explicit per-subproject `.gitignore` entries only for uncovered build/cache dirs.

**Rule 6a — Project planning docs subdirectory = TRACK.** `uiplanning/` contains 12 planning markdown files. TRACK all.

**Rule 7 — Build/cache/runtime directory = IGNORE at directory level.** Targets: `__pycache__/` (existing), `.pytest_cache/` (existing), `android-app/.kotlin/`, `chroma_db/`, `db/`, `models/`. Add `.gitignore` entries with one-line comments.

**Rule 8 — Artifact/archive directory = IGNORE at directory level.** Targets: `archive/`, `artifacts/`.

**Rule 9 — Local-only state = IGNORE with comment.** Targets: `.serena/`, `.codex_rgal1_flake_dir.txt`, `.codex_stage2_rerun2_root.txt`, `CLAUDE.md` (local AGENTS mirror), **`.mcp.json`** (machine-specific path at line 11; not shared config despite similar name to `opencode.json`).

**Rule 10 — Scratch process-output = IGNORE (default) or DELETE if trivially empty.** Targets: `test_startprocess_err.txt` (empty), `test_startprocess_out.txt` (one line "hello"). Default IGNORE via `test_startprocess_*.txt` pattern; DELETE is acceptable but requires explicit Tate-facing mention in the report. Don't auto-DELETE.

**Rule 11 — Forward-looking project doc = TRACK.** Targets: `GUIDE_PLAN.md` (AGENTS-named), `README_OPEN_IN_CODEX.md`, `MIGRATION_LLAMACPP.md`. TRACK as-is.

**Rule 12 — Dated state snapshot in filename = DEFER.** Targets: `CURRENT_LOCAL_TESTING_STATE_20260410.md`, `LM_STUDIO_MODELS_20260410.json`, `UI_DIRECTION_AUDIT_20260414.md`. DEFER with carry-over entry for Tate triage.

**Rule 13 — Audit markdown of unclear provenance = DEFER.** Targets: `auditglm.md`, `gptaudit4-21.md`, `senku_mobile_mockups.md`. DEFER.

**Rule 14 — Zip archives at root = DEFER (never DELETE in-slice).** Targets: `4-13guidearchive.zip`, `guides.zip`. Run `unzip -l` for informational listing in the report, but default is DEFER. Any DELETE requires explicit Tate confirmation post-commit — the slice flags "DELETE candidate: <file>, rationale: <zip listing shows X% overlap>" but does NOT delete.

**Rule 15 — Screenshots at root = DEFER (default).** Targets: 6 `senku_*.png`. **Scope screenshot-reference grep to TRACKED docs only**: `git grep -lF "<screenshot name>" -- "*.md" 2>/dev/null`. If zero tracked-doc hits -> DEFER. If tracked-doc hits -> TRACK candidate BUT additionally require a visual-content sanity check (no secrets visible in the screenshot). If the visual check is not confidently completable in-session, DEFER anyway. Untracked-doc references (e.g., `senku_mobile_mockups.md:9`) do NOT promote screenshots to TRACK.

**Rule 16 — Shared project config at root = TRACK.** Targets: `requirements.txt` (Python dependencies). **`.mcp.json` is NOT included here** — reroute to Rule 9. `opencode.json` is already tracked (and has a pre-existing modification outside this slice's scope per Precondition 4).

**(Rule 17 — REMOVED as ordered rule; was redundant with Rules 3/4b.** Build/dev helpers imported or test-paired are already covered by first-match on those rules. Retained here as inline reminder only: if a `scripts/*.py` isn't AGENTS-named but is imported or has a sibling test, Rules 3 or 4b fire first. Do NOT re-evaluate Rule 17 separately during Step 4.)

**Rule 17a — CLI-utility escape hatch (v5.3 — kept permissive; scope clarified per Codex round-5 Finding 1).** If a `.py` file has an `if __name__ == "__main__":` guard, TRACK as a live operator tool. `__main__` guards are strong entrypoint signals; library modules don't add them gratuitously.

**Acknowledged scope impact (Codex round-5 Finding 1):** Rule 17a is broad — it auto-TRACKs any `scripts/*.py` with `__main__`, including files without AGENTS anchor or sibling test. Estimated ~12 additional scripts land on this heuristic alone (e.g., `audit_mobile_pack_metadata.py`, `compare_bench_artifacts.py`, `run_mobile_headless_answers.py`, `validate_bench_retry_slots.py`, and similar). **This is intentional under the v5 "i want everything" directive** — the slice is a near-complete tracking sweep of operator tooling, not an AGENTS-only audit. The filename `R-track1_core_entry_point_tracking_audit.md` is historical; the actual scope is "atomic tracking-hygiene disposition for untracked tree" (slice title).

Operational check: `grep -l "^if __name__ == " <file>`. If any hit, TRACK.

Per-item rationale is REQUIRED in the §Per-item disposition report: for each Rule 17a TRACK, one line explaining what the script does (parsed from the top docstring, the argparse `description=`, or the `__main__` block itself). If the rationale is genuinely empty ("no docstring, no description, `__main__` body is `print('hi')`"), record DEFER under Rule 18 instead — this covers the actual throwaway edge case Codex flagged without tightening the rule for the common case.

**Rule 18 — Unknown-purpose source with no importers, no test pair, AND no CLI shape = DEFER.** If a `.py` file has zero `import` references from tracked/about-to-track code AND no matching test file AND is not AGENTS.md-named AND Rule 17a does NOT fire, DEFER. Don't track dead code.

**Rule 19 — Everything else = DEFER with explicit reason.** Fallback; expected to fire on <3 items total across the enumeration.

### Secret-scan pattern set (v4 — tightened per Codex round-2 Finding 3)

For every TRACK candidate (including non-`.py` root files like `test_prompts.txt`):

```bash
# Authorization-shaped tokens only (not bare "token" substring — avoids false-hits
# like `metastrip_token`, `refresh_token_id`, column names, etc.)
grep -niE "(password|api[_-]?key|api[_-]?token|access[_-]?token|auth[_-]?token|bearer[ \"']+[A-Za-z0-9\\-_.]{16,}|client[_-]?secret)\\b" <file>

# PEM / private keys
grep -nE "-----BEGIN (RSA|OPENSSH|EC|DSA|PGP) (PRIVATE KEY|CERTIFICATE)" <file>

# Opaque high-entropy blobs (>48 consecutive base64 chars) — .py only
grep -nE "[A-Za-z0-9+/]{48,}={0,2}" <file>

# Machine-specific absolute paths (v5.5 — generalized per Codex round-7 Finding 2).
# Prior versions hardcoded `tateb` / `tate`, missing `/Users/tbronson` in tracked
# `README_OPEN_IN_CODEX.md:5,42` and any future contributor's username. v5.5 matches
# any user-home path shape regardless of username.
grep -nE "C:\\\\Users\\\\[A-Za-z0-9._-]+|/home/[A-Za-z0-9._-]+|/Users/[A-Za-z0-9._-]+" <file>

# Non-localhost, non-LAN HTTP(S) URLs (v5.2 — FQDN-shaped only, templates excluded).
# Safelist: localhost, 127.0.0.0/8, 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16, 0.0.0.0.
# LAN URLs are dev-local by convention and do NOT indicate exfil; surface but don't STOP.
# FQDN-requirement: regex requires a real `<label>.<tld>` shape. This excludes template
# fragments like `http://{args.host}`, `http://" + config.host`, `http://$env:HOST` —
# such fragments are print-template literals, not URL leaks. They surface-only if needed.
grep -nE "https?://(?!(localhost|127\\.|10\\.|192\\.168\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|0\\.0\\.0\\.0))[a-zA-Z0-9][a-zA-Z0-9.-]*\\.[a-zA-Z]{2,}" <file>

# Email addresses (keep; personal-email leak signal)
grep -nE "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}" <file>

# UNC / network share paths
grep -nE "^//[^/]|\\\\\\\\[^\\\\]" <file>
```

**STOP rule (v5.1 — severity-tiered, unified per Codex round-3 Finding 4):**

- **Hard STOP (Rule 1):** Applies regardless of disposition.
  - Authorization-shaped tokens, PEM/private keys, high-entropy base64 blobs.
  - Non-LAN URLs with a real FQDN (FQDN-shape regex above) that aren't obvious dev dependencies (PyPI, GitHub, etc.).
  - **Machine-specific absolute paths (`C:\Users\tateb`, `/home/tateb`, `/Users/tate`) IN CODE FILES that are TRACK-dispositioned.** Code files = `.py`, `.ps1`, `.java`, `.js`, `.ts`, `.go`, `.c`, `.cpp`, `.sh`, `.kt`, `.rs`. Machine paths in code are real identity leaks.
- **Surface-but-continue (logged only, does NOT STOP):**
  - LAN URLs (e.g., `http://192.168.0.67:1234/v1` in `scripts/validate_bench_retry_slots.py:17`).
  - Template-fragment URLs that aren't FQDN-shaped (excluded by the FQDN regex by design).
  - **Machine-specific paths IN MARKDOWN / PLAINTEXT DOCS (`.md`, `.txt`) that are TRACK-dispositioned** (v5.2 — refined per Codex round-4 Finding 1). Dev notes routinely reference local paths like `C:\Users\tateb\Downloads\gemma-4-E2B-it.litertlm` (e.g., `uiplanning/IMPLEMENTATION_LOG_20260413.md:1480`); these are benign operational context, not exfiltration signals. Log and continue.
  - Machine-specific paths IN FILES THAT ARE IGNORE-DISPOSITIONED (e.g., `.mcp.json:11` has `C:\\Users\\tateb\\...` but Rule 9 IGNOREs the file; the path never enters the tree). Log the finding in the report §Secret-scan outcome for audit, but do not STOP.
  - Emails in author strings / license headers.
- **Planner safelist carry-forward:** `http://localhost:1234/v1` in `config.py`, `requirements.txt` package URLs — marked safe historically. Also pre-authorized: non-FQDN URL fragments in `tools/sidecar-viewer/server.py` (template: `http://{args.host}`), `litert-host-jvm/src/.../LiteRtOpenAiServer.java` (concat: `"http://" + config.host`). Machine-path mentions in `uiplanning/*.md` pre-authorized per the markdown-doc carve-out above.

Log every hit in the report regardless of disposition. Any HARD STOP hit escalates to planner; surface-but-continue hits disposition in-report with a one-line rationale.

### Expected disposition volumes (v3 first-cut — not binding)

| Category | Est. count | Disposition |
| --- | --- | --- |
| Root `.py` (10) | 10 | All TRACK (Rules 2/3 + Rule 5a override carry-over for registry) |
| `scripts/*.py` (24) | 24 | ~20 TRACK (Rules 2/3/4b/17a), ~4 DEFER (Rule 18) |
| **`scripts/*.ps1` AGENTS-named subset (9)** | **9** | **All TRACK (Rule 2b — v5). Remaining ~28 non-AGENTS-named ps1 OUT OF SCOPE → carry-over (j)** |
| `tests/*.py` (27) | 27 | Very high TRACK rate via Rule 4 (a/b/c three branches); DEFER only for genuinely-orphan tests with no sibling, no first-party import, no string-path reference |
| Root non-`.py` (25) | 25 | 5 TRACK (Rules 2a/11/16: `test_prompts.txt`, `GUIDE_PLAN.md`, `README_OPEN_IN_CODEX.md`, `MIGRATION_LLAMACPP.md`, `requirements.txt`), 6 IGNORE (Rules 9/10), 14 DEFER (Rules 12-15) |
| Untracked directories (7) | 7 | All IGNORE (Rules 7/8/9) |
| `litert-host-jvm/` | 1 subproject | TRACK source + IGNORE `.gradle/`/`build/`/`bin/` (Rule 6) |
| `tools/sidecar-viewer/` | 5 files | All TRACK (Rule 6) |
| `uiplanning/` | 12 files | All TRACK (Rule 6a) |
| `guides/` new entries | Many | OUT OF SCOPE |
| `notes/` new entries | Many | OUT OF SCOPE |
| `scripts/*.ps1` non-AGENTS (~28) | ~28 | OUT OF SCOPE (v5) — tracked in follow-up slice per carry-over (j) |

### Commit

Single commit, message:
```
R-track1: atomic tracking-hygiene disposition for repo-root + scripts/ + tests/ + source subprojects
```

Body: 15-40 line summary with per-category count table + must-track anchor confirmation + pointer to the disposition report note + pointer to the hash-manifest section of the report.

## Boundaries (HARD GATE)

- Touch only: TRACK-dispositioned files (as additions); `.gitignore` (additions); `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` (new); `notes/CP9_ACTIVE_QUEUE.md`; `notes/dispatch/README.md`.
- Scope-in subprojects: `litert-host-jvm/` (source subtree), `tools/sidecar-viewer/` (all), `uiplanning/` (all).
- Scope-in root assets beyond `.py`: `test_prompts.txt` (TRACK), `GUIDE_PLAN.md`, `README_OPEN_IN_CODEX.md`, `MIGRATION_LLAMACPP.md`, `requirements.txt` (TRACK), various IGNORE and DEFER targets per rules.
- Do NOT:
  - Use `git add -A` / `git add .` / `git add *.py` / `git add <dir>/*` without explicit per-file enumeration. Path-by-path only; for directories, enumerate the subset to track (e.g., `litert-host-jvm/src/` recursively = explicit), not blanket.
  - Modify the CONTENT of any TRACK-dispositioned file. The hash-manifest step (§Work Step 8) is the verification.
  - Execute ANY generator or script (`scripts/regenerate_deterministic_registry.py`, `scripts/export_mobile_pack.py`, etc.). Track the existing state.
  - DELETE any file without explicit report-documented rationale. Default is DEFER or IGNORE for ambiguous. Scratch process-output files (Rule 10) may DELETE as last resort, but IGNORE is the default.
  - Force-add files matching existing `.gitignore` rules (no `git add -f`).
  - Stage pre-existing modified files outside slice scope: `AGENTS.md` (M), `opencode.json` (M), `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` (D). These predate the slice; leave in place.
  - Extend `.gitignore` with blanket globs across unrelated domains (e.g., `*.py`, `*.md`). Each addition is a specific directory or specific file pattern with a one-line comment.
  - Touch `guides/` content, `notes/` content beyond the new disposition report + tracker updates, `android-app/` tracked tree, any tracked source file.
  - Touch R-pack-drift1 investigation note or any R-hygiene* slice file.
  - Rotate any slice file. (v5.6 clarification per Codex round-8 Finding 2: slice-file rotation — moving completed slice files out of `notes/dispatch/` into `notes/dispatch/completed/` — is deferred to a separate D-series rotation slice per in-slice tracker cadence memory. The slice ONLY edits `notes/dispatch/README.md` to update the "Active / Landed" status of R-track1 itself; it does NOT move other slice files.)

## The work

### Step 1 — Verify preconditions

```bash
git merge-base --is-ancestor 5fb7719 HEAD || { echo "ABORT: HEAD is not descended from R-hygiene2 (5fb7719)."; exit 1; }
git log -1 --format=%H
git log --oneline -5
git status --short | awk '$1=="??"' | wc -l
git status --short | awk '$1=="??"' | grep -cE "\.py$"
git ls-files -- "*.py" | wc -l

# Precondition 4: slice-target files clean (staged AND unstaged), pre-existing dirt OK
# v5.1 fix — use `git diff HEAD --` to catch already-staged changes that `git diff --` alone misses.
git diff HEAD -- .gitignore notes/CP9_ACTIVE_QUEUE.md notes/dispatch/README.md

# Additional cached-name check (v5.3 — ABORT ONLY, no auto-unstage per Codex round-5 Finding 3).
# If any slice-target file is already staged, STOP and escalate to planner.
# Do NOT auto-unstage — that would mutate the user's index state without authorization.
if git diff --cached --name-only | grep -qE "^(\.gitignore|notes/CP9_ACTIVE_QUEUE\.md|notes/dispatch/README\.md)$"; then
  echo "ABORT: slice target already staged. Concurrent work detected. Escalate to planner — do NOT unstage."
  git diff --cached --name-only | grep -E "^(\.gitignore|notes/CP9_ACTIVE_QUEUE\.md|notes/dispatch/README\.md)$"
  exit 1
fi

echo "pre-existing dirt (expected): AGENTS.md M, opencode.json M, SWARM note D"
git status --short | grep -E "^[MD] " | head

ls notes/R-PACK-DRIFT_INVESTIGATION_20260422.md
git ls-files --error-unmatch -- scripts/refresh_mobile_pack_metadata.py >/dev/null
```

If `git diff` on the three slice-target files is non-empty, STOP. Pre-existing M/D on AGENTS.md / opencode.json / SWARM note do NOT block.

### Step 2 — Full enumeration

```bash
mkdir -p artifacts
git status --short | awk '$1=="??"' | sed -E 's|^\?\? ||' > artifacts/R-track1_untracked_full.txt
wc -l artifacts/R-track1_untracked_full.txt

# Per-category enumerations
git status --short | awk '$1=="??"' | grep -E "^\?\? [^/]+\.py$"
git status --short | awk '$1=="??"' | grep -E "^\?\? scripts/[^/]+\.py$"
git status --short | awk '$1=="??"' | grep -E "^\?\? tests/[^/]+\.py$"
git status --short | awk '$1=="??"' | grep -vE "^\?\? .*/" | grep -vE "\.py$"
git status --short | awk '$1=="??" && /\/$/'

# Verify the expected subprojects exist
test -f litert-host-jvm/build.gradle && echo "litert-host-jvm/ present"
test -d tools/sidecar-viewer && echo "tools/sidecar-viewer/ present"
test -d uiplanning && echo "uiplanning/ present"

# Verify AGENTS-named non-code asset
test -f test_prompts.txt && echo "test_prompts.txt present"

# Verify AGENTS-named Python source on disk
for f in config.py guide_catalog.py deterministic_special_case_registry.py special_case_builders.py lmstudio_utils.py token_estimation.py; do
  test -f "$f" && echo "$f present"
done
```

**v5.8 operational note:** Immediately after the enumeration block above, initialize the slice-wide ledgers below and keep them authoritative for the rest of the slice. Every time Steps 3–7 or Step 6.5 disposition a path as TRACK, append the literal path to `artifacts/R-track1_track_paths_raw_20260422.txt` immediately. Root / `scripts/*.py` files that do NOT TRACK on the first pass but still need Rule 3 fixed-point rechecks go into the pending ledgers; do not finalize Rule 18 on them until Step 6.5 closes.

```bash
: > artifacts/R-track1_track_paths_raw_20260422.txt
: > artifacts/R-track1_support_file_candidates_20260422.txt
: > artifacts/R-track1_rule3_root_pending_20260422.txt
: > artifacts/R-track1_rule3_scripts_pending_20260422.txt
: > artifacts/R-track1_import_smoke_skip_scripts_20260422.txt
```

### Step 3 — Category: root `.py` (10 items)

Per file:
1. Run the v3 secret-scan pattern set (§Outcome § Secret-scan).
2. Rule 1 trigger → STOP.
3. Importer check.
4. **Sibling-test check:** `test -f tests/test_<X>.py` where `<X>` is the file stem.
5. **CLI-shape check:** `grep -l "^if __name__ == " <file>`. Record whether a `__main__` guard is present.
6. Apply rules 2 → 3 → 5/5a → 17a. If none fire on the first pass, do **not** finalize Rule 18 yet; append the literal path to `artifacts/R-track1_rule3_root_pending_20260422.txt` for Step 6.5 closure.
7. Record disposition + secret-scan outcome per file. TRACK paths append immediately to `artifacts/R-track1_track_paths_raw_20260422.txt`; non-TRACK first-pass root `.py` files stay `PENDING-RULE3-CLOSURE` until Step 6.5 makes the final Rule 18 / Rule 19 call.

### Step 4 — Category: `scripts/*.py` (24 items)

Pre-check: `git ls-files notes/specs/deterministic_registry_sidecar.yaml`. Record result for Rule 5a.

Per file:
1. Secret-scan.
2. Importer check.
3. **Test-pair check:** `test -f tests/test_<X>.py` (on disk, not git).
4. **CLI-shape check (v4 — simplified):** `grep -l "^if __name__ == " <file>`. Record whether `__main__` guard is present.
5. **AGENTS-named check:** is this script named in AGENTS.md? (Currently: `scripts/export_mobile_pack.py`, `scripts/validate_special_cases.py`. If adding scripts to AGENTS.md later, update this list.) If yes, TRACK under Rule 2.
6. Apply rules 2 → 3 → 4b → 17a. If none fire on the first pass, do **not** finalize Rule 18 yet; append the literal path to `artifacts/R-track1_rule3_scripts_pending_20260422.txt` for Step 6.5 closure. (Rule 17 is retired as ordered rule; Rule 17a fires only if neither Rule 2 nor Rule 3 nor Rule 4b fires. Rule 17a itself fires on `__main__` presence alone — see v4 Rule 17a body.)
7. Record. TRACK paths append immediately to `artifacts/R-track1_track_paths_raw_20260422.txt`; non-TRACK first-pass `scripts/*.py` files stay `PENDING-RULE3-CLOSURE` until Step 6.5 makes the final Rule 18 / Rule 19 call.

### Step 4c — Category: `scripts/*.psm1` and `scripts/*.sh` support files (v5.8 override)

Enumerate:

```bash
git status --short | awk '$1=="??"' | grep -E "^\\?\\? scripts/.+\\.(psm1|sh)$" | sed 's/^?? //' > artifacts/R-track1_support_file_candidates_20260422.txt
cat artifacts/R-track1_support_file_candidates_20260422.txt
```

**v5.8 override (use this, not the speculative v5.7 seed below):**
- Step 4c now does only two things: enumerate the support-file candidates and run the Rule 1 secret-scan on each candidate immediately.
- Record every enumerated candidate path in `artifacts/R-track1_support_file_candidates_20260422.txt`.
- Do **not** treat future root docs or future `.ps1` landings as referrers before they clear Rule 1 / category disposition.
- Final Rule 2c / 2d TRACK-vs-DEFER resolution now happens in Step 6.5 against `artifacts/R-track1_track_paths_validated_20260422.txt`.
- Historical note: the old v5.7 provisional-ledger setup is intentionally retired in v5.8. Do not create or use `artifacts/R-track1_track_candidates_step4c.txt`; Step 6.5 is the only authoritative support-file resolution pass.
- Historical Step-4c commands that referenced the provisional ledger have been removed from the live execution path in this final pass to eliminate copy-paste risk. For `.psm1` / `.sh` support files, use only Rule 2c, Rule 2d, and the executable Step 6.5 closure below.

### Step 4b — Category: `scripts/*.ps1` AGENTS-named subset (9 items — v5)

For each AGENTS-named `.ps1` script in §Rule 2b's authoritative list (verified against `git status --short | awk '$1=="??"' | grep -E "^\\?\\? scripts/.+\\.ps1$"` — only items that appear in BOTH the AGENTS-named list AND the untracked-ps1 list):

1. Secret-scan with the v4 pattern set. **Machine-path hits are real risks** in PowerShell scripts that automate local paths — `C:\\Users\\tateb` and similar trigger HARD STOP (Rule 1); LAN URLs (10.x, 127.x, 172.16-31.x, 192.168.x, localhost) surface-and-continue.
2. TRACK under Rule 2b unless Rule 1 fires.
3. Record. If TRACK, append the literal path to `artifacts/R-track1_track_paths_raw_20260422.txt` immediately.

Non-AGENTS-named `.ps1` in `scripts/` (~28 items) are OUT OF SCOPE for v5 — they fall into the "scripts/ content-tracking follow-up" carry-over entry (new §Step 13 entry (j)).

### Step 5 — Category: `tests/*.py` (27 items) — v5 three-branch Rule 4, Rule 17a excluded

Per file:
1. Secret-scan.
2. **Sibling-source check (Rule 4.a):** `test -f <X>.py || test -f scripts/<X>.py` (on disk) where `<X>` is the test's stem (`test_<X>.py` → `<X>`).
3. **Import-based first-party check (Rule 4.b):** `grep -oE "^(import|from) [a-z_][a-z_0-9.]*" <test_file>` then strip the leading keyword and filter against the first-party module set. First-party set = repo-root `*.py` (tracked + TRACK-candidate) + TRACK-candidate `scripts.<name>`. If ANY first-party match, including dotted `scripts.<name>` imports, Rule 4 fires.
4. **String-path check (Rule 4.c — v5.2 broadened to match Rule 4.c definition):** Two-pass grep to cover both quoted-path and `Path`-construction idioms:
   - Pass 1 (quoted full path): `grep -nE "['\"]scripts/[a-z_0-9]+\\.(py|ps1)['\"]" <test_file>`
   - Pass 2 (Path-construction): `grep -nE "['\"]scripts['\"] */|/ *['\"]scripts['\"]" <test_file>`; if Pass 2 hits, follow-up: `grep -nE "['\"][a-z_0-9]+\\.(py|ps1)['\"]" <test_file>` to find the companion filename literal, then cross-reference against the TRACK-candidate script set.
   If either pass resolves to a TRACK-candidate script (Rule 2 / 2b / 3 / 4b / 17a), Rule 4 fires.
   
   **Explicit TRACK overrides (v5.2 — restate from Rule 4.c body so Step 5 is standalone):** regardless of grep outcome, `tests/test_delta_prompt_scripts.py` and `tests/test_run_guide_prompt_validation_harness.py` TRACK. These anchor Rule 4.c's Path-construction coverage.
5. Apply Rule 4 if ANY of 4.a / 4.b / 4.c fires. Fall through to Rule 18 ONLY if none fire.

**Rule 17a is NOT a valid fall-through for tests (v4).** Test files commonly end with `if __name__ == "__main__": unittest.main()` as boilerplate — this is not evidence of "live operator tool" status, it's the standard `python -m unittest <test>` vs direct-run idiom. If a test falls through Rule 4 (all three branches) and is not AGENTS-named, it DEFERs under Rule 18. Do NOT promote it via Rule 17a.

6. Record which branch (4.a, 4.b, 4.c) caused the TRACK, for audit. If TRACK, append the literal path to `artifacts/R-track1_track_paths_raw_20260422.txt` immediately. If none, record "DEFER (Rule 18 — no sibling + no first-party import + no string-path reference)" with the test's import list + any grep matches enumerated.

### Step 6 — Category: root non-`.py` files (25 items) + source-like subprojects

**Root non-`.py`:**
Per file, apply the v3 rules in order (1 → 2a → 11 → 16 → 9 → 10 → 12 → 13 → 14 → 15 → 19). If the final decision is TRACK, append the literal path to `artifacts/R-track1_track_paths_raw_20260422.txt` immediately.

For Rule 14 (zips): `unzip -l 4-13guidearchive.zip | head -30` and `unzip -l guides.zip | head -30` — log to report. DEFER regardless.

For Rule 15 (screenshots): `git grep -lF "<screenshot base name>" -- "*.md" 2>/dev/null`. Only tracked-doc hits promote to TRACK. If TRACK, additionally note "visual-content sanity check needed" and DEFER unless visual check is doable confidently in-session.

**Subprojects:**
- `litert-host-jvm/`: enumerate `find litert-host-jvm -type f -not -path "*/\.gradle/*" -not -path "*/build/*" -not -path "*/bin/*"`. TRACK the source files explicitly. Reuse the existing top-level `.gradle/` ignore for `litert-host-jvm/.gradle/`; add explicit `.gitignore` entries only for `litert-host-jvm/build/` and `litert-host-jvm/bin/` if they are still uncovered.
- `tools/sidecar-viewer/`: enumerate `find tools/sidecar-viewer -type f`. TRACK all. Secret-scan each.
- `uiplanning/`: enumerate `find uiplanning -type f`. TRACK all markdown. Secret-scan each (machine-path, email, URLs).

Any TRACK-dispositioned subproject file also appends its literal path to `artifacts/R-track1_track_paths_raw_20260422.txt` immediately.

### Step 6.5 — Rule 3 closure + support-file resolution (v5.8 — validated-ledger pass)

```bash
# Build the first validated TRACK ledger from the raw TRACK paths accumulated so far.
sort -u artifacts/R-track1_track_paths_raw_20260422.txt | awk 'NF' > artifacts/R-track1_track_paths_validated_20260422.txt

# Helper: does the current validated corpus import this repo-root or scripts module?
rule3_ref_hit() {
  local mod="$1"
  git grep -IlE "^(from|import)[[:space:]]+(${mod}|scripts\\.${mod})([[:space:].]|$)" -- '*.py' >/dev/null 2>&1 && return 0
  while IFS= read -r candidate; do
    case "$candidate" in
      *.py)
        grep -IE "^(from|import)[[:space:]]+(${mod}|scripts\\.${mod})([[:space:].]|$)" "$candidate" >/dev/null 2>&1 && return 0
        ;;
    esac
  done < artifacts/R-track1_track_paths_validated_20260422.txt
  return 1
}

rule2c_ref_hit() {
  local f="$1"
  local basename_full="${f##*/}"
  local basename_no_ext="${basename_full%.psm1}"
  git grep -lF -e "${basename_full}" >/dev/null 2>&1 && return 0
  while IFS= read -r candidate; do
    grep -lF "${basename_no_ext}.psm1" "$candidate" >/dev/null 2>&1 && return 0
  done < artifacts/R-track1_track_paths_validated_20260422.txt
  return 1
}

rule2d_ref_hit() {
  local f="$1"
  local basename="${f##*/}"
  git grep -lF -e "scripts/${basename}" >/dev/null 2>&1 && return 0
  while IFS= read -r candidate; do
    grep -lF "scripts/${basename}" "$candidate" >/dev/null 2>&1 && return 0
  done < artifacts/R-track1_track_paths_validated_20260422.txt
  return 1
}

# Rule 3 / support-file closure pass: keep looping until the validated ledger stops growing.
while :; do
  PREV=$(wc -l < artifacts/R-track1_track_paths_validated_20260422.txt)

  : > artifacts/R-track1_rule3_root_pending_next_20260422.txt
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    mod="${f##*/}"
    mod="${mod%.py}"
    if rule3_ref_hit "$mod"; then
      printf '%s\n' "$f" >> artifacts/R-track1_track_paths_raw_20260422.txt
    else
      printf '%s\n' "$f" >> artifacts/R-track1_rule3_root_pending_next_20260422.txt
    fi
  done < artifacts/R-track1_rule3_root_pending_20260422.txt

  : > artifacts/R-track1_rule3_scripts_pending_next_20260422.txt
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    mod="${f##*/}"
    mod="${mod%.py}"
    if rule3_ref_hit "$mod"; then
      printf '%s\n' "$f" >> artifacts/R-track1_track_paths_raw_20260422.txt
    else
      printf '%s\n' "$f" >> artifacts/R-track1_rule3_scripts_pending_next_20260422.txt
    fi
  done < artifacts/R-track1_rule3_scripts_pending_20260422.txt

  : > artifacts/R-track1_support_file_candidates_next_20260422.txt
  while IFS= read -r f; do
    [ -n "$f" ] || continue
    if [[ "$f" == *.psm1 ]]; then
      if rule2c_ref_hit "$f"; then
        printf '%s\n' "$f" >> artifacts/R-track1_track_paths_raw_20260422.txt
      else
        printf '%s\n' "$f" >> artifacts/R-track1_support_file_candidates_next_20260422.txt
      fi
    elif [[ "$f" == *.sh ]]; then
      if rule2d_ref_hit "$f"; then
        printf '%s\n' "$f" >> artifacts/R-track1_track_paths_raw_20260422.txt
      else
        printf '%s\n' "$f" >> artifacts/R-track1_support_file_candidates_next_20260422.txt
      fi
    fi
  done < artifacts/R-track1_support_file_candidates_20260422.txt

  mv artifacts/R-track1_rule3_root_pending_next_20260422.txt artifacts/R-track1_rule3_root_pending_20260422.txt
  mv artifacts/R-track1_rule3_scripts_pending_next_20260422.txt artifacts/R-track1_rule3_scripts_pending_20260422.txt
  mv artifacts/R-track1_support_file_candidates_next_20260422.txt artifacts/R-track1_support_file_candidates_20260422.txt

  sort -u artifacts/R-track1_track_paths_raw_20260422.txt | awk 'NF' > artifacts/R-track1_track_paths_validated_20260422.txt
  CUR=$(wc -l < artifacts/R-track1_track_paths_validated_20260422.txt)
  [ "$CUR" -eq "$PREV" ] && break
done

# Finalize the remaining first-pass non-TRACK `.py` files only AFTER the closure loop.
# Remaining paths with no importer, no sibling test, and no `__main__` become final Rule 18 DEFERs.
# Any remaining path that still has a sibling test or `__main__` is a Rule 19 ambiguity -> STOP and escalate.
cat artifacts/R-track1_rule3_root_pending_20260422.txt artifacts/R-track1_rule3_scripts_pending_20260422.txt 2>/dev/null | awk 'NF' > artifacts/R-track1_rule3_pending_final_20260422.txt
while IFS= read -r f; do
  [ -n "$f" ] || continue
  stem="${f##*/}"
  stem="${stem%.py}"
  if grep -q "^if __name__ == " "$f" || test -f "tests/test_${stem}.py"; then
    echo "FATAL: $f still unresolved after Rule 3 closure despite CLI-shape or sibling-test signal -> escalate under Rule 19"
    exit 1
  fi
done < artifacts/R-track1_rule3_pending_final_20260422.txt
```

Use the same basename-literal grep patterns defined in Rules 2c / 2d for the support-file pass. Only files already present in `artifacts/R-track1_track_paths_validated_20260422.txt` may act as referrers here; do not seed future docs or future `.ps1` landings before they have completed their own Rule 1 / category pass. Rule 3 is not complete until this loop reaches fixed point, unresolved pending `.py` paths have been finalized, and the remaining `.psm1` / `.sh` candidates have been recorded as final DEFERs with cross-reference notes.

### Step 7 — Category: untracked directories (7 items)

Per directory, verify `git ls-files <dir>/ | head` returns empty. If any tracked file exists inside, flag OUT OF SCOPE anomaly + carry-over; do NOT add a blanket ignore. If truly empty-of-tracked, add `.gitignore` entry with one-line comment.

### Step 8 — Pre-commit hash manifest (v5.2 — whitespace-safe, CRLF-invariant)

Before any `git add`:

**v5.8 override:** Do not hand-maintain the literal `printf` TRACK list from older revisions. Build the authoritative NUL list from the raw TRACK ledger accumulated during Steps 3–7 / 6.5:

```bash
sort -u artifacts/R-track1_track_paths_raw_20260422.txt | awk 'NF' > artifacts/R-track1_track_paths_validated_20260422.txt
test -s artifacts/R-track1_track_paths_validated_20260422.txt || { echo "FATAL: validated TRACK ledger is empty"; exit 1; }
tr '\n' '\0' < artifacts/R-track1_track_paths_validated_20260422.txt > artifacts/R-track1_track_list_20260422.nul
TRACK_COUNT=$(wc -l < artifacts/R-track1_track_paths_validated_20260422.txt)
NUL_COUNT=$(tr -cd '\0' < artifacts/R-track1_track_list_20260422.nul | wc -c)
[ "$TRACK_COUNT" -eq "$NUL_COUNT" ] || { echo "FATAL: validated TRACK ledger count ($TRACK_COUNT) != NUL count ($NUL_COUNT)"; exit 1; }
echo "TRACK ledger contains $TRACK_COUNT paths"
```

Treat the older explicit-`printf` block below as historical context only; the ledger-derived method above is authoritative in v5.8.

```bash
# Capture git-normalized blob hash for every TRACK-dispositioned file.
# `git hash-object` applies the same CRLF normalization git WILL apply on `git add`,
# so it is CRLF-safe on Windows with core.autocrlf=true. No sha256sum dependency.
# v5.2 whitespace-safe: TRACK paths are read line-by-line from a NUL-delimited list
# so filenames with spaces (e.g., `uiplanning/Technical Architecture for Autonomous
# Multi-Model Engineering Swarms_ ... Scouts.md`) are NOT split.
mkdir -p artifacts

# Write the authoritative TRACK list with one NUL-delimited path per record.
# v5.4 bug fix per Codex round-6 Finding 1: DO NOT embed inline comments inside a
# backslash-continued printf. Bash line-continuation merges the `#` onto the command,
# which then comments out the redirect — leaving a 0-byte file. Put any annotation
# BEFORE the command, or use a heredoc.
# Populate the TRACK list from the disposition pass; each path must be a literal
# string, never `$(...)` or word-splitting array expansion.
#
# Approach A — explicit printf with ALL literal paths continuous (no inline comments):
printf '%s\0' \
  "metadata_validation.py" \
  "config.py" \
  "bench_artifact_tools.py" \
  "confidence_label_contract.py" \
  "summarize_latency.py" \
  > artifacts/R-track1_track_list_20260422.nul
#   (…extend the printf above with every other TRACK literal — root py, scripts/py, scripts/ps1,
#   scripts/psm1, scripts/sh, tests/py, subproject files, docs, test_prompts.txt. Keep them all
#   inside ONE printf invocation so no stale file gets left behind.)
#
# Approach B — heredoc (preferred if the list is long; handles spaces; no inline-comment trap):
# cat <<'EOF' | tr '\n' '\0' > artifacts/R-track1_track_list_20260422.nul
# metadata_validation.py
# config.py
# (... one TRACK path per line, no comments, no blank lines ...)
# EOF

# Sanity check: the output file MUST be non-empty and contain the expected count.
test -s artifacts/R-track1_track_list_20260422.nul || { echo "FATAL: track list is empty — printf redirect failed"; exit 1; }
TRACK_COUNT=$(tr -cd '\0' < artifacts/R-track1_track_list_20260422.nul | wc -c)
echo "TRACK list contains $TRACK_COUNT paths"

# Generate the hash manifest. `while IFS= read -r -d ''` preserves spaces/newlines.
: > artifacts/R-track1_pre_commit_hashes_20260422.txt
while IFS= read -r -d '' f; do
  printf '%s %s\n' "$(git hash-object -- "$f")" "$f" >> artifacts/R-track1_pre_commit_hashes_20260422.txt
done < artifacts/R-track1_track_list_20260422.nul

wc -l artifacts/R-track1_pre_commit_hashes_20260422.txt    # should equal TRACK count
```

Note: the `artifacts/` directory is IGNORE-dispositioned under Rule 8, so this file is not committed. Its contents are preserved by embedding the full manifest verbatim into `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` §Pre-commit hash manifest (the report IS committed).

### Step 9 — Build dispositions, execute atomically (v5.2 whitespace-safe staging)

1. Compose `.gitignore` additions (collected from Rules 6/7/8/9/10/IGNORE paths). Dedupe. Each addition: one-line comment + the pattern.
2. **Whitespace-safe `git add`.** Feed the NUL-delimited TRACK list (from Step 8) into `xargs -0` so filenames with spaces stay intact:
   ```bash
   xargs -0 -a artifacts/R-track1_track_list_20260422.nul git add --
   ```
   The `--` guards against paths that start with `-`. Do NOT use `for f in $(...)` or unquoted variable expansion — both split on whitespace. For directory-level TRACK (e.g., `litert-host-jvm/src/**`), enumerate the specific subtree inclusions as literal paths in the NUL list — no raw `litert-host-jvm/*` wildcards.
3. For any DELETE (Rule 10 only, and only if Tate-facing report rationale is present): `rm -- "<file>"` BEFORE any `git rm` (since these are untracked). Quote the path literal; these files won't have spaces (scratch `test_startprocess_*.txt`) but quote defensively.
4. Apply `.gitignore` edits (Write/Edit tool; not shell redirects).

### Step 10 — Import-smoke (BLOCKING in v3 — cwd-explicit in v3.5)

**Run from repo root.** All imports below are bare module names (`import config`, `import scripts.<name>`), which requires the repo root on `sys.path` (Python's default for `-c` invocations when cwd == repo root). `scripts.<name>` works without `scripts/__init__.py` via Python 3's namespace-package behavior (PEP 420); do NOT add `__init__.py` files solely to make the smoke pass.

```bash
# Verify cwd FIRST
pwd                                  # must be repo root (…/senku_local_testing_bundle_20260410)
git rev-parse --show-toplevel        # must equal cwd

# Resolve Python once so both Git Bash and WSL work.
PYTHON_BIN="$(command -v python3 || command -v python || true)"
[ -n "$PYTHON_BIN" ] || { echo "ABORT: neither python3 nor python found on PATH"; exit 1; }
"$PYTHON_BIN" -V
```

For each TRACK-dispositioned root-level `.py`:

```bash
while IFS= read -r -d '' f; do
  mod="${f%.py}"
  "$PYTHON_BIN" -c "import ${mod}" || { echo "IMPORT-FAIL: $f"; exit 1; }
done < <(grep -zE "^[^/]+\\.py$" artifacts/R-track1_track_list_20260422.nul)
```

Expected: no ImportError. This is BLOCKING — any failure means a tracked file has a runtime bug and the commit does not proceed as-is.

**Scripts with known network side-effects on import (skip only with fallback):** if any `scripts/*.py` module performs a top-level network call at import time, append the literal path to `artifacts/R-track1_import_smoke_skip_scripts_20260422.txt`, then skip the runtime import for that file only after running `"$PYTHON_BIN" -m py_compile "$f"` as the mandatory fallback. The smoke asserts module-load cleanliness where safe and at least parse/import-stub validation where import-time side effects would be operationally dangerous.

For `scripts/*.py` TRACK-dispositioned (still from repo root):

```bash
while IFS= read -r -d '' f; do
  mod="${f%.py}"
  mod="${mod//\//.}"
  if grep -Fx "$f" artifacts/R-track1_import_smoke_skip_scripts_20260422.txt >/dev/null 2>&1; then
    "$PYTHON_BIN" -m py_compile "$f" || { echo "PY-COMPILE-FAIL: $f"; exit 1; }
  else
    "$PYTHON_BIN" -c "import ${mod}" || { echo "IMPORT-FAIL: $f"; exit 1; }
  fi
done < <(grep -zE "^scripts/.+\\.py$" artifacts/R-track1_track_list_20260422.nul)
```

Same BLOCKING discipline; any ImportError fails the slice. If a script is explicitly skipped for network side effects, `"$PYTHON_BIN" -m py_compile` becomes the required fallback and must be called out in the report. If `ModuleNotFoundError: No module named 'scripts'` appears, the cwd is wrong — re-run from repo root.

**PowerShell scripts (`scripts/*.ps1`) — parse-check if pwsh available, operational-record fallback (v5.3 — strengthened per Codex round-5 Finding 2).**

```bash
# v5.4 fix per Codex round-6 Finding 4: try pwsh (PowerShell 7+) FIRST, then
# powershell.exe (Windows PowerShell 5.1 — present on every Windows host). Both
# expose the same AST parser API. Prior v5.3 only checked pwsh and degraded to
# fallback on Windows/Git Bash even though powershell.exe was available.
if command -v pwsh >/dev/null 2>&1; then
  PS_CMD="pwsh"
elif command -v powershell.exe >/dev/null 2>&1; then
  PS_CMD="powershell.exe"
else
  PS_CMD=""
  echo "WARN: neither pwsh nor powershell.exe on PATH — ps1 validation limited to operational-record check."
fi

# Filter NUL list for ps1 (and psm1 — same parser) entries.
while IFS= read -r -d '' f; do
  if [ -n "$PS_CMD" ]; then
    # Parse-check via the PowerShell AST parser. Catches syntax errors without executing.
    # Same API on PS 5.1 (powershell.exe) and PS 7+ (pwsh), but the parser
    # reports syntax problems through the errors array rather than exit status.
    "$PS_CMD" -NoProfile -Command "\$parseErrors = \$null; [System.Management.Automation.Language.Parser]::ParseFile('$f', [ref]\$null, [ref]\$parseErrors) | Out-Null; if (\$parseErrors -and \$parseErrors.Count -gt 0) { \$parseErrors | ForEach-Object { Write-Error \$_.Message }; exit 1 }" \
      || { echo "PARSE-FAIL: $f"; exit 1; }
  else
    # Last-resort fallback: file-read + non-empty-non-all-comment check. Weak evidence.
    cat -- "$f" >/dev/null || { echo "READ-FAIL: $f"; exit 1; }
    [ "$(grep -cE '^[^#]' -- "$f")" -gt 0 ] || { echo "EMPTY-OR-ALL-COMMENT: $f"; exit 1; }
  fi
done < <(grep -zE "^scripts/.+\\.(ps1|psm1)$" artifacts/R-track1_track_list_20260422.nul)
```

Acceptance language (v5.9): if either `pwsh` OR `powershell.exe` was available and all ps1/psm1 files parsed clean with **zero parser errors**, report records "ps1/psm1 parse-check: PASSED via <engine> AST parser" (naming which). Only if BOTH engines were unavailable does the report record "SKIPPED; operational-record fallback only." The fallback does NOT claim syntactic validation.

**Tests (`tests/*.py`) — real import-smoke (v5.3 — strengthened from py_compile per Codex round-5 Finding 4).** Prior v5.2 used `python -m py_compile` which only parses; it does NOT catch `ImportError`. v5.3 runs actual imports via Python 3's namespace-package behavior (PEP 420 — no `tests/__init__.py` required):

```bash
# Import every newly-TRACK-dispositioned test from repo root.
# Catches both SyntaxError (at parse) and ImportError (at module-load).
# Use a separate subprocess per test to isolate side-effect crashes.
while IFS= read -r -d '' f; do
  mod="${f%.py}"           # tests/test_abstain.py -> tests/test_abstain
  mod="${mod//\//.}"       # tests/test_abstain     -> tests.test_abstain
  "$PYTHON_BIN" -c "import ${mod}" || { echo "IMPORT-FAIL: $f"; exit 1; }
done < <(grep -zE "^tests/.+\\.py$" artifacts/R-track1_track_list_20260422.nul)
```

Expected: exit 0 for every test. Any `ImportError`, `SyntaxError`, or module-level exception fails the slice. BLOCKING. This is stronger than py_compile: a test whose import statements are stale (e.g., references a module renamed elsewhere) will fail here but would have passed py_compile.

### Step 11 — Post-STAGE hash verification (v4 — ordering fix per Codex round-2 Finding 1)

**Why post-stage, not post-commit:** the disposition report must be PART of the atomic commit (§Scope), and the report must include the verification result (§Disposition report structure §3). v3.5 had these in Step 11 → Step 12 → Step 14 order, which was logically impossible: post-commit hashes can't be read until commit exists, but the report-inclusive-of-verification must be written BEFORE commit. v4 breaks the circularity by running verification POST-STAGE, not post-commit. A staged blob's hash equals the committed blob's hash by construction (commit is a no-op on staged blobs), so post-stage verification is equivalent evidence.

After Step 9's `git add` stages all TRACK files (but BEFORE Step 14's `git commit`):

```bash
# Read the STAGED blob id for every TRACK-dispositioned file.
# `git rev-parse :<file>` (bare colon prefix) returns the staged-index blob sha-1,
# which is what the subsequent `git commit` will snapshot verbatim.
# This hash equals `git hash-object` of the pre-add working-tree file IFF no content
# edit occurred between Step 8 capture and Step 9 staging.
# v5.2 whitespace-safe: iterate the same NUL-delimited TRACK list from Step 8.
: > artifacts/R-track1_post_stage_hashes_20260422.txt
while IFS= read -r -d '' f; do
  printf '%s %s\n' "$(git rev-parse ":$f")" "$f" >> artifacts/R-track1_post_stage_hashes_20260422.txt
done < artifacts/R-track1_track_list_20260422.nul

diff artifacts/R-track1_pre_commit_hashes_20260422.txt artifacts/R-track1_post_stage_hashes_20260422.txt \
  || { echo "FATAL: post-stage hash drift detected"; exit 1; }
```

Expected: `diff` returns nothing (empty output, exit 0). Any difference = post-capture content drift occurred between Step 8's pre-add hash capture and staging. Capture the diff output in the report §Post-stage hash verification. If non-empty, the slice has failed its post-capture invariance check — **STOP and escalate to planner. Do NOT auto-unstage (v5.4 fix per Codex round-6 Finding 3).** The executor has no authority to mutate the index by running `git restore --staged`; hash-mismatch on staged blobs means something outside the slice's mental model has interfered, and unilaterally unstaging could clobber concurrent work. Leave the staged state intact and hand off to planner with the manifest diff attached.

**Why this works with `core.autocrlf=true`:** both `git hash-object` (pre-add) and `git rev-parse :<file>` (post-stage) return git's internal blob hash, computed AFTER CRLF normalization. So even if working-tree bytes have CRLF and the stored blob has LF, both sides compute the same hash. Using `sha256sum` on working-tree files would break this invariant on Windows — do NOT substitute.

**Sanity follow-through (post-commit, belt-and-suspenders — optional, not BLOCKING):** after Step 14 lands, a one-liner `git rev-parse "HEAD:<file>"` for any file should equal the Step 11 staged hash. If Step 11 was clean, this is guaranteed; log it as a single-file spot check in the report.

### Step 12 — Write disposition report

Write `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` with all 8 sections specified in §Outcome § Disposition report structure. The §Pre-commit hash manifest and §Post-stage hash verification sections are load-bearing — include them verbatim from the manifests (Step 8 + Step 11 outputs). §Post-stage verification shows the full diff output (expected: empty) plus the total file count. The report itself will then be staged in Step 14 as part of the same atomic commit.

### Step 13 — Tracker updates

**`notes/CP9_ACTIVE_QUEUE.md`:**
- Update top-of-file "Last updated" line to reference R-track1 landing.
- Add to Completed rolling log using the actual final counts from the report §Summary (do not leave placeholders): `- 2026-04-22 — R-track1 atomic: <tracked-count> tracked / <ignored-count> ignored / <deleted-count> deleted / <deferred-count> deferred. Report at notes/R-TRACK1_HYGIENE_REPORT_20260422.md.`
- Add Carry-over Backlog entries (path-explicit — v3.5):
  - **(a) Sidecar YAML tracking.** `notes/specs/deterministic_registry_sidecar.yaml` UNTRACKED — registry drift possible unless sidecar is tracked or discipline locked elsewhere (Rule 5a). Separate narrow slice: TRACK the sidecar + validate `scripts/regenerate_deterministic_registry.py --check` against `deterministic_special_case_registry.py` (and its existing test coverage in `tests/test_regenerate_deterministic_registry.py`). Assign post-R-track1.
  - **(b) `guides/` content-tracking slice.** Dozens of untracked guide markdowns (`guides/abrasives-manufacturing.md`, `guides/accessible-shelter-design.md`, ... per `git status`). Out-of-scope here per §Scope.
  - **(c) `notes/` content-tracking slice.** Includes load-bearing `notes/ABSTAIN_TUNING_ANALYSIS_20260418.md` (Wave C dependency per `notes/WAVE_C_FORWARD_RESEARCH_20260422.md` §11). Also dispatches D8 slice-file rotation. Priority-ordered first after R-track1 lands.
  - **(d) Zip archives DELETE-candidate triage.** `4-13guidearchive.zip`, `guides.zip`. Requires Tate OK before removal. Listings captured in §Deferred of this slice's report for reference.
  - **(e) Screenshots visual-content review.** 6 `senku_*.png` at repo root. DEFER pending visual sanity check (no secrets/PII visible). Promote to TRACK only if tracked-doc reference exists AND visual check passes.
  - **(f) Dated snapshot triage.** `CURRENT_LOCAL_TESTING_STATE_20260410.md`, `LM_STUDIO_MODELS_20260410.json`, `UI_DIRECTION_AUDIT_20260414.md`. Tate keep/archive/delete decision.
  - **(g) Audit markdown triage.** `auditglm.md`, `gptaudit4-21.md`, `senku_mobile_mockups.md`. Tate keep/archive/delete decision.
  - **(h) Orphan `.py` DEFERs (Rule 18).** Populate this entry with ONE sub-bullet per `.py` file dispositioned DEFER under Rule 18, using the exact path (e.g., `- scripts/scan_encoding.py — DEFER, Rule 18 (no importers, no test pair, no CLI shape)`). Do NOT bucket as "orphan `.py` DEFERs from Rule 18"; the next slice needs per-path entries to work from. If there are zero Rule 18 `.py` DEFERs after Step 6.5 closes, add a single explicit sub-line under (h): `- No Rule 18 DEFERs in this pass - all repo-root/scripts/tests .py candidates resolved via Rules 2/3/4/4b/5/17a or higher.` Do NOT leave (h) as a header-only placeholder.
  - **(i) RETIRED in v5** — subprocess/string-path test broadening is now in-scope via Rule 4.c. Known TRACKs under 4.c: `tests/test_delta_prompt_scripts.py`, `tests/test_run_guide_prompt_validation_harness.py`. Any additional 4.c hits surfaced during Step 5 enumeration will be documented in §Per-item disposition, not carried over.
  - **(j) `scripts/*.ps1` non-AGENTS-named tracking (v5.8 — exact dynamic count, not >=5).** Populate with ONE sub-bullet per untracked non-AGENTS ps1 candidate (same structure as (h)). Acceptance requires EXACTLY one sub-bullet per line of output from the enumeration command below. Enumerate via:
    ```bash
    comm -23 \
      <(git status --short | awk '$1=="??"' | grep -E "^\\?\\? scripts/.+\\.ps1$" | sed 's/^?? //' | sort) \
      <(printf '%s\n' scripts/push_mobile_pack_to_android.ps1 scripts/run_android_prompt.ps1 \
                     scripts/run_android_search_log_only.ps1 scripts/run_android_ui_validation_pack.ps1 \
                     scripts/start_senku_emulator_matrix.ps1 scripts/start_senku_device_mirrors.ps1 \
                     scripts/run_guide_prompt_validation.ps1 scripts/start_qwen27_scout_job.ps1 \
                     scripts/get_qwen27_scout_job.ps1 | sort)
    ```
    Write one sub-bullet per line of output, format: `- scripts/<name>.ps1 - DEFER to follow-up; no AGENTS anchor, rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in follow-up slice.`. If present in the enumeration output, expected examples include `scripts/run_android_harness_matrix.ps1`, `scripts/run_android_gap_pack.ps1`, `scripts/run_android_session_batch.ps1`, the expanded `scripts/run_android_followup_*.ps1` family, the expanded `scripts/run_android_detail_*.ps1` family, `scripts/invoke_qwen*_scout.ps1`, `scripts/run_qwen27_scout_job_worker.ps1`, `scripts/start_fastembed_server.ps1`, `scripts/start_litert_host_server.ps1`, `scripts/push_litert_model_to_android.ps1`, `scripts/cleanup_android_harness_artifacts.ps1`, `scripts/post_rebuild_sanity_check.ps1`, `scripts/run_abstain_regression_panel.ps1`, `scripts/run_e2b_e4b_diff.ps1`, `scripts/launch_debug_detail_state.ps1`, `scripts/android_fts5_probe.ps1`, `scripts/start_overnight_continuation.ps1`, and `scripts/start_android_detail_followup_lane.ps1`. The command output is authoritative if it ever disagrees with the example list. Follow-up slice scope: run the same secret-scan + Rule 1 HARD STOP policy used for Rule 2b on each candidate BEFORE any promote/defer judgment, then evaluate each via the Rule 2d-style cross-reference pass (TRACK if referenced from tracked docs/scripts/tests, DEFER otherwise). Size estimate: 20-24 TRACK, 4-8 DEFER.

**`notes/dispatch/README.md` (v5.7 - explicit):**
- Update the stale `## Active slices` sentence near `notes/dispatch/README.md:33-36` so it no longer anchors current state to `R-anchor-refactor1`. After R-track1 lands, that sentence should say no slices are currently in flight and `R-track1` landed this commit.
- Edit the "Key remaining post-RC tracked items" paragraph near `notes/dispatch/README.md:38-45` to REMOVE `R-track1` from the remaining-post-RC list. After R-track1 lands, the paragraph should read (approx): "Key remaining post-RC tracked items live in `notes/CP9_ACTIVE_QUEUE.md`: the post-`R-hygiene2` lane was `R-track1` (landed this commit); next is Wave C planning and optional ask-telemetry enrichment; follow-ups per R-track1 carry-over (a)-(j). `R-tool2`, `R-anchor-refactor1`, `R-ret1b`, `R-host`, `R-search`, `R-telemetry` are closed."
- Under `## Landed (not yet rotated)` near `notes/dispatch/README.md:47-57`, reconcile the full pending-rotation list instead of merely appending `R-track1`. After this slice lands, that section should explicitly reflect every currently-landed-but-intentionally-unrotated slice file still in `notes/dispatch/`, including `R-pack-drift1_forensic_investigation.md`, `R-hygiene2_metadata_report_mobile_write_removal.md`, `R-anchor-refactor1_pack_support_breakdown.md`, `R-tool2_state_pack_logcat_capture.md`, and `R-track1_core_entry_point_tracking_audit.md`.
- Update the lead sentence to exact landed-form prose: `Pending rotation as of `R-track1` landing (intentionally not rotated in-slice):`
- Update or remove the stale repo-level rotation rule near `notes/dispatch/README.md:72-74` so it no longer contradicts the pending-rotation section. Minimum acceptable rewrite: `Rotate landed slices during the next D-series cleanup slice unless the slice explicitly keeps the file live.`
- Do NOT move slice files to `notes/dispatch/completed/` in this slice — that's deferred to a later D-series per in-slice tracker cadence memory. Rotation is called out in the carry-over for the next D-slice.

**`notes/CP9_ACTIVE_QUEUE.md` (v5.8 — explicit):**
- Also update the active-lane pointer at `notes/CP9_ACTIVE_QUEUE.md:13` (or equivalent "current lane" line) to reflect R-track1 landed. Rewrite the line in landed-form prose (for example: `R-track1` landed this commit; next is Wave C planning + optional ask-telemetry enrichment). Do NOT try to embed the landing commit hash inside the same commit — that SHA does not exist until Step 14 lands. If a later D-slice wants the exact hash in tracker prose, backfill it post-landing.

### Step 14 — Commit

```bash
# v5.2 whitespace-safe: TRACK paths were already staged in Step 9 via xargs -0.
# Here we stage the small set of non-TRACK files (report + trackers + .gitignore) by name.
# These paths are fixed and space-free; quote defensively.
git add -- ".gitignore" "notes/R-TRACK1_HYGIENE_REPORT_20260422.md" "notes/CP9_ACTIVE_QUEUE.md" "notes/dispatch/README.md"
git status                      # confirm pre-existing dirt on AGENTS.md / opencode.json / SWARM is NOT staged
git diff --cached --stat | head -100

git commit -m "$(cat <<'EOF'
R-track1: atomic tracking-hygiene disposition for repo-root + scripts/ + tests/ + source subprojects

Resolves fresh-clone ImportError exposure: metadata_validation.py (imported by tracked ingest.py, mobile_pack.py, scripts/refresh_mobile_pack_metadata.py) now tracked.

Must-track anchors (verified present in commit):
- metadata_validation.py (urgent dependency)
- config.py, guide_catalog.py, deterministic_special_case_registry.py, special_case_builders.py, lmstudio_utils.py, token_estimation.py (AGENTS Core Entry Points / Shared support)
- bench_artifact_tools.py, confidence_label_contract.py, summarize_latency.py (required by import-smoke)
- scripts/export_mobile_pack.py, scripts/validate_special_cases.py (AGENTS-named live Python entry points)
- scripts/push_mobile_pack_to_android.ps1, scripts/run_android_prompt.ps1, scripts/run_android_search_log_only.ps1, scripts/run_android_ui_validation_pack.ps1, scripts/start_senku_emulator_matrix.ps1, scripts/start_senku_device_mirrors.ps1, scripts/run_guide_prompt_validation.ps1, scripts/start_qwen27_scout_job.ps1, scripts/get_qwen27_scout_job.ps1 (AGENTS-named PowerShell operator tools — v5)
- tests/test_delta_prompt_scripts.py, tests/test_run_guide_prompt_validation_harness.py (Rule 4.c anchors — v5)
- test_prompts.txt (AGENTS testing asset)
- litert-host-jvm/src/, tools/sidecar-viewer/, uiplanning/ (source subprojects)

Counts:
- Per-category totals (tracked / ignored / deleted / deferred): see `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` §Summary.
- PowerShell scripts tracked include the 9 AGENTS-named additions plus any Rule 2c support-file landing documented in the report.
- No post-capture content drift on TRACK files (verified via Step 8 pre-add / Step 11 post-stage git blob hash diff; see report §Post-stage hash verification)

Full per-item disposition + hash manifest at notes/R-TRACK1_HYGIENE_REPORT_20260422.md.
EOF
)"

git log -1 --stat | head -60
```

## Acceptance (v3 — hardened per scout BLOCKING 5)

**Hard-anchored must-track list (explicit — not derived from report counts):**

Each of these MUST appear in `git ls-files` post-commit, or acceptance fails:

- `metadata_validation.py` (CRITICAL — dependency for already-tracked code)
- `config.py`
- `guide_catalog.py`
- `deterministic_special_case_registry.py`
- `special_case_builders.py`
- `lmstudio_utils.py`
- `token_estimation.py`
- `bench_artifact_tools.py` (v3.5 — required by Step 10 import-smoke)
- `confidence_label_contract.py` (v3.5 — required by Step 10 import-smoke)
- `summarize_latency.py` (v3.5 — required by Step 10 import-smoke)
- `scripts/export_mobile_pack.py` (v4 — AGENTS.md:51 mobile pack export entry point)
- `scripts/validate_special_cases.py` (v4 — AGENTS.md:92 deterministic citation/routing guard)
- `scripts/push_mobile_pack_to_android.ps1` (v5 — AGENTS.md:53)
- `scripts/run_android_prompt.ps1` (v5 — AGENTS.md:55)
- `scripts/run_android_search_log_only.ps1` (v5 — AGENTS.md:56)
- `scripts/run_android_ui_validation_pack.ps1` (v5 — AGENTS.md:57)
- `scripts/start_senku_emulator_matrix.ps1` (v5 — AGENTS.md:58)
- `scripts/start_senku_device_mirrors.ps1` (v5 — AGENTS.md:60)
- `scripts/run_guide_prompt_validation.ps1` (v5 — AGENTS.md:61)
- `scripts/start_qwen27_scout_job.ps1` (v5 — AGENTS.md:66)
- `scripts/get_qwen27_scout_job.ps1` (v5 — AGENTS.md:67)
- `scripts/android_harness_common.psm1` (v5.4 — Rule 2c anchor, imported by 4+ tracked/TRACK-candidate ps1 scripts)
- `scripts/verify_local_runtime.sh` (v5.4 — Rule 2d anchor, `README_OPEN_IN_CODEX.md:45` reference)
- `scripts/rebuild_venv_if_needed.sh` (v5.4 — Rule 2d anchor, `README_OPEN_IN_CODEX.md:51` reference)
- `tests/test_delta_prompt_scripts.py` (v5 — Rule 4.c anchor, string-path to `scripts/build_delta_prompt_pack.py` + `scripts/merge_structured_prompt_packs.py`)
- `tests/test_run_guide_prompt_validation_harness.py` (v5 — Rule 4.c anchor, string-path to `scripts/run_guide_prompt_validation.ps1`)
- `test_prompts.txt`
- `litert-host-jvm/build.gradle`
- `litert-host-jvm/settings.gradle`
- `litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java`
- `tools/sidecar-viewer/app.js`
- `tools/sidecar-viewer/server.py`
- `uiplanning/CURRENT_STATE.md`
- `uiplanning/DECISIONS.md`
- `GUIDE_PLAN.md`
- `README_OPEN_IN_CODEX.md`
- `MIGRATION_LLAMACPP.md`
- `requirements.txt`

Verify:

```bash
while IFS= read -r f; do
  git ls-files --error-unmatch "$f" >/dev/null || echo "MISSING: $f"
done <<'EOF'
metadata_validation.py
config.py
guide_catalog.py
deterministic_special_case_registry.py
special_case_builders.py
lmstudio_utils.py
token_estimation.py
bench_artifact_tools.py
confidence_label_contract.py
summarize_latency.py
scripts/export_mobile_pack.py
scripts/validate_special_cases.py
scripts/push_mobile_pack_to_android.ps1
scripts/run_android_prompt.ps1
scripts/run_android_search_log_only.ps1
scripts/run_android_ui_validation_pack.ps1
scripts/start_senku_emulator_matrix.ps1
scripts/start_senku_device_mirrors.ps1
scripts/run_guide_prompt_validation.ps1
scripts/start_qwen27_scout_job.ps1
scripts/get_qwen27_scout_job.ps1
scripts/android_harness_common.psm1
scripts/verify_local_runtime.sh
scripts/rebuild_venv_if_needed.sh
tests/test_delta_prompt_scripts.py
tests/test_run_guide_prompt_validation_harness.py
test_prompts.txt
litert-host-jvm/build.gradle
litert-host-jvm/settings.gradle
litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java
tools/sidecar-viewer/app.js
tools/sidecar-viewer/server.py
uiplanning/CURRENT_STATE.md
uiplanning/DECISIONS.md
GUIDE_PLAN.md
README_OPEN_IN_CODEX.md
MIGRATION_LLAMACPP.md
requirements.txt
EOF
```

Any MISSING line = acceptance fail.

**Hard lower bounds:**

- `git ls-files -- "*.py" | wc -l` returns `>=60`. (14 tracked pre-slice + >=46 added; leaves room for up to ~15 DEFER-dispositioned `.py` across the 61 candidates.)
- `git ls-files -- "scripts/*.ps1" | wc -l` returns `>=14` (v5.2 — 5 already tracked + >=9 AGENTS-named additions).
- `git ls-files -- "litert-host-jvm/**" | wc -l` returns `>=3`.
- `git ls-files -- "tools/sidecar-viewer/**" | wc -l` returns `>=5`.
- `git ls-files -- "uiplanning/**" | wc -l` returns `>=12`.
- `wc -l artifacts/R-track1_track_paths_validated_20260422.txt` equals the NUL-record count in `artifacts/R-track1_track_list_20260422.nul`. Any mismatch = acceptance fail.

**Hash invariance (post-capture drift proof) — v5.1 post-stage blob-hash form:**

- `diff artifacts/R-track1_pre_commit_hashes_20260422.txt artifacts/R-track1_post_stage_hashes_20260422.txt` returns empty output. Any non-empty diff = acceptance fail. This proves the Step 8-captured TRACK contents did not drift before commit; it does NOT prove nothing was edited earlier in the slice. Both manifests preserved verbatim in the report §Post-stage hash verification (the `artifacts/` files are not committed — Rule 8 — but their contents live in the committed report).
- **Optional post-commit spot-check (not BLOCKING):** `git rev-parse "HEAD:metadata_validation.py"` equals the Step 8 / Step 11 hash for that file. One-file sanity confirms commit snapshot == staged blob; full per-file post-commit diff is unnecessary since staged blob == committed blob by construction.

**Import-smoke (v5.3 — tests use real import, ps1 uses pwsh parse if available):**

- Step 10's root-level `.py` import-smoke passes. ImportError = acceptance fail.
- Step 10's `scripts/*.py` import-smoke passes; any script explicitly skipped for import-time network side effects must instead pass the mandatory `"$PYTHON_BIN" -m py_compile` fallback and be named in the report §Import-smoke notes. ImportError or fallback failure = acceptance fail.
- Step 10's `tests/*.py` real-import smoke (v5.3) — `"$PYTHON_BIN" -c "import tests.<name>"` — passes for every TRACK-dispositioned test. ImportError / SyntaxError / module-level exception = acceptance fail.
- Step 10's `scripts/*.ps1` parse-check (v5.9) — if `pwsh` OR `powershell.exe` was on PATH, every TRACK-dispositioned ps1 / psm1 produced zero parser errors from `[System.Management.Automation.Language.Parser]::ParseFile`. Only if BOTH engines were unavailable does the report record "SKIPPED; operational-record fallback only" — this does NOT fail acceptance but is documented explicitly.

**Carry-over integrity (v5.1 — NEW per Codex round-3 Finding 5):**

Each of the following carry-over entries from Step 13 MUST be present in `notes/CP9_ACTIVE_QUEUE.md` post-commit. Verify each header AND (for (h) and (j)) verify the per-path body is non-empty (v5.4 tighten per Codex round-6 Finding 5):

```bash
CP9_CARRY_BLOCK="$(awk '/^## Carry-over Backlog/{flag=1; next} /^## /{flag=0} flag' notes/CP9_ACTIVE_QUEUE.md)"
[ -n "$CP9_CARRY_BLOCK" ] || { echo "FAIL: CP9 Carry-over Backlog section missing"; exit 1; }

for k in "(a)" "(b)" "(c)" "(d)" "(e)" "(f)" "(g)" "(h)" "(j)"; do
  printf '%s\n' "$CP9_CARRY_BLOCK" | grep -F "$k " >/dev/null || { echo "MISSING carry-over header $k"; exit 1; }
done

# (h) must list per-path orphan .py DEFERs under Rule 18. An empty placeholder
# passes header check but fails the slice's intent. Count the sub-bullets under (h).
H_BLOCK="$(printf '%s\n' "$CP9_CARRY_BLOCK" | awk '/^ *- \*\*\(h\) Orphan/{flag=1; print; next} flag && /^ *- \*\*\([a-z]\)/{flag=0} flag')"
H_BULLETS=$(printf '%s\n' "$H_BLOCK" | awk 'NR>1 && /^ *-/{c++} END{print c+0}')
if [ "$H_BULLETS" -lt 1 ]; then
  # If the slice found zero Rule-18 DEFERs anywhere, (h) should explicitly say
  # "No Rule 18 DEFERs in this pass - all repo-root/scripts/tests .py candidates
  # resolved via Rules 2/3/4/4b/5/17a or higher."
  printf '%s\n' "$H_BLOCK" | grep -F "No Rule 18 DEFERs" >/dev/null \
    || { echo "FAIL: (h) has zero sub-bullets and no explicit 'No Rule 18 DEFERs' line"; exit 1; }
fi

# (j) must list per-path non-AGENTS-named ps1 candidates for the follow-up slice.
# Require exact dynamic parity with the enumeration command from Step 13(j); do NOT
# allow a token placeholder count like ">=5".
J_BULLETS=$(printf '%s\n' "$CP9_CARRY_BLOCK" | awk '/^ *- \*\*\(j\)/{flag=1; next} flag && /^ *- \*\*\([a-z]\)/{flag=0} flag && /^ *-/{c++} END{print c+0}')
EXPECTED_J=$(comm -23 \
  <(git status --short | awk '$1=="??"' | grep -E "^\\?\\? scripts/.+\\.ps1$" | sed 's/^?? //' | sort) \
  <(printf '%s\n' scripts/push_mobile_pack_to_android.ps1 scripts/run_android_prompt.ps1 \
                 scripts/run_android_search_log_only.ps1 scripts/run_android_ui_validation_pack.ps1 \
                 scripts/start_senku_emulator_matrix.ps1 scripts/start_senku_device_mirrors.ps1 \
                 scripts/run_guide_prompt_validation.ps1 scripts/start_qwen27_scout_job.ps1 \
                 scripts/get_qwen27_scout_job.ps1 | sort) | wc -l)
if [ "$J_BULLETS" -ne "$EXPECTED_J" ]; then
  echo "FAIL: (j) has $J_BULLETS sub-bullets; expected $EXPECTED_J per-path entries"
  exit 1
fi
```

Any FAIL line = acceptance fail.

- (a) Sidecar YAML tracking
- (b) `guides/` content-tracking slice
- (c) `notes/` content-tracking slice
- (d) Zip archives DELETE-candidate triage
- (e) Screenshots visual-content review
- (f) Dated snapshot triage
- (g) Audit markdown triage
- (h) Orphan `.py` DEFERs (repo-wide per-path body required; or explicit "No Rule 18 DEFERs" line if none)
- (j) `scripts/*.ps1` non-AGENTS-named tracking follow-up (v5.8; per-path body required, exact dynamic count)

Note: (i) is retired in v5 — no check.

**Dispatch README bookkeeping (v5.9 - updated):**

- `notes/dispatch/README.md` no longer anchors the active-state sentence to `R-anchor-refactor1`; it says no slices are currently in flight and `R-track1` landed this commit.
- `notes/dispatch/README.md` keeps `R-track1` in the top "Key remaining post-RC tracked items" paragraph only in landed form (`R-track1` landed this commit), not as a still-pending lane.
- `notes/dispatch/README.md` reconciles `## Landed (not yet rotated)` to the current landed-but-live slice set, including `R-pack-drift1`, updates the lead sentence to the exact `R-track1`-landed wording, and removes the contradictory "rotate immediately when a slice lands" repo-level rule.

Verify:

```bash
README_ACTIVE_BLOCK="$(awk '/^## Active slices/{flag=1; next} /^## /{flag=0} flag' notes/dispatch/README.md)"
README_LANDED_BLOCK="$(awk '/^## Landed \(not yet rotated\)/{flag=1; next} /^## /{flag=0} flag' notes/dispatch/README.md)"
[ -n "$README_ACTIVE_BLOCK" ] || { echo "FAIL: README Active slices section missing"; exit 1; }
[ -n "$README_LANDED_BLOCK" ] || { echo "FAIL: README Landed (not yet rotated) section missing"; exit 1; }

printf '%s\n' "$README_ACTIVE_BLOCK" | grep -E 'R-track1.*landed this commit|landed this commit.*R-track1' >/dev/null \
  || { echo "FAIL: README active section still does not present R-track1 as landed"; exit 1; }

printf '%s\n' "$README_ACTIVE_BLOCK" | grep -F 'No slices are currently in flight after `R-anchor-refactor1`.' >/dev/null \
  && { echo "FAIL: README active section still anchors current state to R-anchor-refactor1"; exit 1; }

printf '%s\n' "$README_LANDED_BLOCK" | grep -F 'Pending rotation as of `R-track1` landing (intentionally not rotated in-slice):' >/dev/null \
  || { echo "FAIL: README pending-rotation lead sentence still stale"; exit 1; }

printf '%s\n' "$README_LANDED_BLOCK" | grep -F "R-pack-drift1_forensic_investigation.md" >/dev/null \
  || { echo "FAIL: README missing landed-but-unrotated R-pack-drift1"; exit 1; }

printf '%s\n' "$README_LANDED_BLOCK" | grep -F "R-hygiene2_metadata_report_mobile_write_removal.md" >/dev/null \
  || { echo "FAIL: README missing landed-but-unrotated R-hygiene2"; exit 1; }

printf '%s\n' "$README_LANDED_BLOCK" | grep -F "R-anchor-refactor1_pack_support_breakdown.md" >/dev/null \
  || { echo "FAIL: README missing landed-but-unrotated R-anchor-refactor1"; exit 1; }

printf '%s\n' "$README_LANDED_BLOCK" | grep -F "R-tool2_state_pack_logcat_capture.md" >/dev/null \
  || { echo "FAIL: README missing landed-but-unrotated R-tool2"; exit 1; }

printf '%s\n' "$README_LANDED_BLOCK" | grep -F "R-track1_core_entry_point_tracking_audit.md" >/dev/null \
  || { echo "FAIL: README missing R-track1 under Landed (not yet rotated)"; exit 1; }

printf '%s\n' "$README_ACTIVE_BLOCK" | grep -F 'the post-`R-pack-drift1` hygiene/audit lane is `R-hygiene2` then `R-track1`' >/dev/null \
  && { echo "FAIL: README still contains the old pending R-hygiene2 -> R-track1 prose"; exit 1; }

grep -F 'Rotate (move to `notes/dispatch/completed/` or delete) when a slice lands.' notes/dispatch/README.md >/dev/null \
  && { echo "FAIL: README still contains the stale immediate-rotation rule"; exit 1; }
```

**CP9 tracker bookkeeping (v5.8 - NEW):**

- `notes/CP9_ACTIVE_QUEUE.md` no longer presents `R-track1` as pending in the top status paragraph; it uses landed-form prose instead.
- `notes/CP9_ACTIVE_QUEUE.md` updates the top-of-file "Last updated" line to mention R-track1 landing and adds the R-track1 closeout line to the Completed rolling log.

Verify:

```bash
CP9_HEAD_BLOCK="$(sed -n '1,20p' notes/CP9_ACTIVE_QUEUE.md)"

printf '%s\n' "$CP9_HEAD_BLOCK" | grep -E 'R-track1.*landed this commit|landed this commit.*R-track1' >/dev/null \
  || { echo "FAIL: CP9 tracker still treats R-track1 as pending"; exit 1; }

printf '%s\n' "$CP9_HEAD_BLOCK" | grep -F '(`R-hygiene2`, `R-track1`)' >/dev/null \
  && { echo "FAIL: CP9 tracker still contains the old pending hygiene tuple"; exit 1; }

printf '%s\n' "$CP9_HEAD_BLOCK" | grep -E '^- Last updated: .*R-track1' >/dev/null \
  || { echo "FAIL: CP9 tracker last-updated line does not mention R-track1"; exit 1; }

grep -F 'R-track1 atomic:' notes/CP9_ACTIVE_QUEUE.md >/dev/null \
  || { echo "FAIL: CP9 tracker missing the R-track1 completed-log entry"; exit 1; }
```

**File-level ignore anchors (v5.1 — NEW per Codex round-3 Finding 6):**

`.gitignore` MUST contain each of these file-level patterns post-commit (existing rules may already cover some; the acceptance is whether `git check-ignore` confirms ignored state, not whether the pattern was added in THIS slice):

- `.mcp.json` — machine-specific MCP config (Rule 9)
- `CLAUDE.md` — local AGENTS mirror (Rule 9)
- `.codex_rgal1_flake_dir.txt` + `.codex_stage2_rerun2_root.txt` OR a `.codex_*` glob — local state (Rule 9)
- `test_startprocess_err.txt` + `test_startprocess_out.txt` OR a `test_startprocess_*.txt` glob — scratch (Rule 10)

Verify:

```bash
for f in .mcp.json CLAUDE.md .codex_rgal1_flake_dir.txt .codex_stage2_rerun2_root.txt test_startprocess_err.txt test_startprocess_out.txt; do
  out=$(git check-ignore -v "$f") || { echo "NOT IGNORED: $f"; exit 1; }
  printf '%s\n' "$out" | grep -E '^\.gitignore:' >/dev/null \
    || { echo "NOT REPO-GITIGNORE: $f"; exit 1; }
done

grep -qE '^\.mcp\.json$' .gitignore \
  || { echo "FAIL: .gitignore missing exact .mcp.json anchor"; exit 1; }

grep -qE '^CLAUDE\.md$' .gitignore \
  || { echo "FAIL: .gitignore missing exact CLAUDE.md anchor"; exit 1; }

if ! grep -qE '^\.codex_\*$' .gitignore; then
  grep -qE '^\.codex_rgal1_flake_dir\.txt$' .gitignore \
    && grep -qE '^\.codex_stage2_rerun2_root\.txt$' .gitignore \
    || { echo "FAIL: .gitignore missing .codex_* anchor (glob or both explicit entries)"; exit 1; }
fi

if ! grep -qE '^test_startprocess_\*\.txt$' .gitignore; then
  grep -qE '^test_startprocess_err\.txt$' .gitignore \
    && grep -qE '^test_startprocess_out\.txt$' .gitignore \
    || { echo "FAIL: .gitignore missing test_startprocess anchor (glob or both explicit entries)"; exit 1; }
fi
```

**`.gitignore` discipline:**

- `git check-ignore -v` on each ignored directory resolves to a repo `.gitignore` rule (new or pre-existing). `litert-host-jvm/.gradle/` is expected to resolve via the existing top-level `.gradle/` rule; the others should resolve via the slice's new directory/file-pattern entries.
- No blanket globs (`*.py`, `*.md`) appear in the diff.

**Pre-existing dirt unchanged:**

- `AGENTS.md`, `opencode.json`, `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` retain their pre-slice dirty state in `git status`. The slice did not stage them.

**Carry-over completeness:**

- `CP9_ACTIVE_QUEUE.md` Carry-over Backlog has entries for: sidecar YAML gap, `guides/` content slice, `notes/` content slice, zip-DELETE triage, screenshot visual review, dated snapshots triage, audit markdown triage, orphan `.py` DEFER list, and the `scripts/*.ps1` non-AGENTS-named tracking follow-up.

**Report completeness:**

- `notes/R-TRACK1_HYGIENE_REPORT_20260422.md` has all 8 sections per §Outcome § Disposition report structure.

## Delegation hints

Secret-scanning across roughly **120 files** is the biggest time sink (10 root `.py` + 24 `scripts/*.py` + 9 AGENTS-named `scripts/*.ps1` + 1 `.psm1` + 4 `.sh` + 27 tests + 25 root non-`.py` + 20 subproject files). Fanout option:

- `gpt-5.4 high` worker per category (root `.py`, scripts/, tests/, root non-`.py` + subprojects). Each worker:
  - Runs the full v3 secret-scan pattern set per file.
  - Runs the importer / test-pair / sibling-source checks per file.
  - Returns a per-file table with (path, scan-findings, matched rule, recommended disposition).
- Main agent: trust-but-verify — sample 3 files per worker batch; re-run the scan and confirm. Flag any worker false-negative.
- Main agent: apply rule framework, compose the TRACK / IGNORE / DEFER lists, execute Steps 8-14.

For the hash manifest steps (8, 11), no fanout — main agent runs and diffs.

For ambiguity (Rule 19 fires), main agent decides inline. Do not fan out judgment.

## Anti-recommendations (v3 — extended per scout SUGGESTION)

- Do NOT pre-read every `.py` file's full content. The v3 secret-scan pattern + importer checks handle the vast majority. Full reads only for Rule-19 ambiguous cases.
- Do NOT apply a blanket `*.py` inclusion in `.gitignore` anywhere. The asymmetry (source files TRACK; cache files under existing `__pycache__/` rule) makes blanket patterns dangerous.
- Do NOT treat absence of `.gitignore` rule as green light to track. Content check (Rules 1 + secret-scan) is authority.
- Do NOT use `git add -f` unless specifically authorized in-slice. If a file matches existing ignore rule, flag collision in report.
- Do NOT delete zips, screenshots, or dated snapshots without explicit Tate confirmation. Default is DEFER.
- Do NOT manufacture dispositions. When no rule matches cleanly, DEFER with explicit reason.
- Do NOT expand scope to `guides/`, `notes/` (beyond the new report + tracker), or `android-app/` tracked tree.
- Do NOT run the full pytest suite. Import-smoke (Step 10) is the verification.
- Do NOT fold R-verify1 / CABIN_HOUSE into this slice — R-hygiene2 already dispositioned it.
- Do NOT modify AGENTS.md content. If you find an AGENTS entry pointing at a missing file, add carry-over; do not edit AGENTS.
- Do NOT promote a screenshot to TRACK based on untracked-doc references. Scope the grep to tracked docs only (see Rule 15).
- Do NOT TRACK `.mcp.json`. It has a machine-specific path and AGENTS names `opencode.json` as the shared MCP config, not `.mcp.json`.
- Do NOT stage pre-existing modified files (`AGENTS.md`, `opencode.json`, deleted `SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md`). Leave dirty — they predate this slice.

## Report format

After landing, respond with:

1. Commit SHA and subject line.
2. Per-category count table (tracked/ignored/deleted/deferred/must-track-anchors-present).
3. Total files changed count and net line delta.
4. Import-smoke result per category (PASS/FAIL).
5. Hash-invariance result (pre/post diff empty? YES/NO).
6. First 20 lines of the disposition report's §Summary table.
7. Full must-track-anchor check output.
8. Carry-over entries added (bulleted list).
9. Any Rule-1 STOP event or acceptance fail (expected: none; if present, detail).
10. One-line risk flags for each DEFER-dispositioned item (Tate triage).




