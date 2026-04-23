# R-hygiene1 — android-app untracked files sweep (post-RC)

Goal: resolve 2134 accumulated untracked files under `android-app/` stemming from prior sessions' dirty state. Surfaced by R-ret1's asymmetric SessionMemory/SessionMemoryTest commit shape.

**Dispatch shape:** single worker, main-lane inline. No MCP hint. Four logical commits in sequence, with STOP gates between steps for anomaly detection.

## Planner decisions (authoritative — do NOT relitigate)

Planner ran the inventory and received user sign-off on all four decision points 2026-04-20 evening:

- **D1 — jniLibs (~82 MB, 12 `.so` files):** TRACK. Modern git handles the size; reproducibility from clean clone > history leanness for this personal project.
- **D2 — `mobile_pack/metadata_validation_report.json` (499 bytes):** IGNORE with rest of `mobile_pack/`. Inconsistent to track the validation report while ignoring the pack it validates.
- **D3 — `senku_model_store.xml` (SharedPreferences dump with device path):** IGNORE. Device runtime state, not app source.
- **D4 — `deterministic_predicate_manifest.txt` (desktop↔android predicate parity mapping):** TRACK. Cross-language reference; high utility.

All other items are unambiguous (source → track; build output → ignore; `local.properties` → ignore because it leaks `sdk.dir=C:\Users\tateb\...`).

## Precondition

- HEAD exactly at `2eae0cd` (R-ret1).
- `./gradlew.bat :app:testDebugUnitTest` passes `403/403` before any edits. Codex records baseline count explicitly.
- Working tree outside `android-app/` is NOT in scope — do not stage anything under `notes/`, `artifacts/`, top-level `.md`, `.mcp.json`, `.serena/`, etc., even if those show `??` in `git status`.

## Step 0 — Verify baseline

```bash
git rev-parse HEAD  # expect 2eae0cd... prefix
./gradlew.bat :app:testDebugUnitTest
```

Record the pass count from the gradle output. If baseline is not 403/403, STOP and flag — no hygiene commit should ride over a red baseline.

## Step 1 — Commit 1: `.gitignore` update (FIRST, before any staging)

Append to root `.gitignore` AFTER the existing build-caches section. Do NOT reorder or modify existing entries:

```
# android-app build output
android-app/app/build/
android-app/build/

# android-app machine-specific config (SDK paths, etc.)
android-app/local.properties

# android-app lane bench scratch
android-app/artifacts/

# android-app mobile_pack binary artifacts (regenerated via scripts/export_mobile_pack.py)
android-app/app/src/main/assets/mobile_pack/

# android-app device runtime state (SharedPreferences dump)
android-app/senku_model_store.xml
```

### Safety verification (MUST pass before committing)

After editing `.gitignore`, verify no currently-tracked file under `android-app/` is shadowed by a new pattern:

```bash
git ls-files android-app/ | xargs git check-ignore 2>/dev/null
```

**Expected output: empty.** If this command prints ANY tracked file paths, STOP — a pattern is too broad. Most likely culprit would be a stray `build` directory name elsewhere; fix the pattern and re-verify.

### Commit

```bash
git add .gitignore
git commit -m "repo-hygiene: gitignore android-app build output, local.properties, mobile_pack, bench scratch, and device runtime state"
```

(Use Codex's standard commit-message footer convention.)

After commit: `git status --short android-app/` should show the 2134 items reduced by ~1920 (build/ + local.properties + artifacts/ + mobile_pack/ + senku_model_store.xml no longer appearing as `??`). Remaining `??` count should be ≈210 non-build files.

## Step 2 — Commit 2: gradle config + wrapper + README

Stage only these 10 specific files (avoid tree-level `git add` here so no surprise files land):

```bash
git add \
  android-app/build.gradle \
  android-app/app/build.gradle \
  android-app/app/proguard-rules.pro \
  android-app/settings.gradle \
  android-app/gradle.properties \
  android-app/gradlew \
  android-app/gradlew.bat \
  android-app/gradle/wrapper/gradle-wrapper.jar \
  android-app/gradle/wrapper/gradle-wrapper.properties \
  android-app/README.md
```

### Verification

```bash
git diff --cached --name-only | wc -l   # expect exactly 10
git diff --cached --name-only           # expect exactly the 10 paths above, no others
```

If `wc -l` is not 10, STOP — either a path is missing (file already tracked? typo?) or extras leaked in.

### Commit

```bash
git commit -m "repo-hygiene: track android-app gradle config, wrapper, and README"
```

## Step 3 — Commit 3: source trees (main + test + androidTest + res)

Tree-level stage:

```bash
git add \
  android-app/app/src/main/java/ \
  android-app/app/src/main/res/ \
  android-app/app/src/test/java/ \
  android-app/app/src/androidTest/java/
```

### Verification (counts)

```bash
git diff --cached --name-only | wc -l
```

Expected counts per planner inventory:

| Tree | Expected | Filter |
| --- | --- | --- |
| `src/main/java/**/*.java` | 38 | |
| `src/main/java/**/*.kt` | 23 | |
| `src/test/java/**/*.java` | 14 | |
| `src/test/java/**/*.kt` | 2 | |
| `src/androidTest/java/**/*.java` | 1 | |
| `src/androidTest/java/**/*.kt` | 1 | |
| `src/main/res/**` | 105 | any extension |
| **Total** | **184** | |

Tolerance: ±3. If `wc -l` returns <181 or >187, STOP and emit the full `git diff --cached --name-only` so planner can triage the delta.

Per-category sanity:

```bash
git diff --cached --name-only | grep -c 'src/main/java/.*\.java$'     # ~38
git diff --cached --name-only | grep -c 'src/main/java/.*\.kt$'       # ~23
git diff --cached --name-only | grep -c 'src/test/java/.*\.java$'     # ~14
git diff --cached --name-only | grep -c 'src/test/java/.*\.kt$'       # ~2
git diff --cached --name-only | grep -c 'src/androidTest/.*\.java$'   # ~1
git diff --cached --name-only | grep -c 'src/androidTest/.*\.kt$'     # ~1
git diff --cached --name-only | grep -c 'src/main/res/'               # ~105
```

Any category >5 off expected, STOP.

### Commit

```bash
git commit -m "repo-hygiene: track accumulated android-app source, tests, and resources"
```

## Step 4 — Commit 4: jniLibs + predicate parity manifest (D1 + D4)

```bash
git add android-app/app/src/main/jniLibs/
git add android-app/deterministic_predicate_manifest.txt
```

### Verification

```bash
git diff --cached --name-only | wc -l         # expect exactly 13
git diff --cached --name-only | grep '\.so$' | wc -l   # expect exactly 12
git diff --cached --stat | tail -5             # sanity check: ~82 MB insertions
```

If `.so` count is not 12, STOP — inventory said 6 per arch × 2 archs.

### Commit

```bash
git commit -m "repo-hygiene: track jniLibs LiteRT/Gemma accelerators and deterministic predicate parity manifest"
```

## Step 5 — Final verification

All four checks must pass:

1. **No remaining untracked under android-app/:**
   ```bash
   git ls-files --others --exclude-standard android-app/ | wc -l
   ```
   Expected: `0`. Any non-zero output means a file was missed — print the list and flag.

2. **Test suite intact:**
   ```bash
   ./gradlew.bat cleanTestDebugUnitTest :app:testDebugUnitTest
   ```
   Expected: `403/403` (unchanged from Step 0 baseline). Any regression means a test resource was either missed from tracking or broken — STOP and flag.

3. **Commit chain:**
   ```bash
   git log --oneline -5
   ```
   Top 4 entries are the new hygiene commits; 5th is `2eae0cd` (R-ret1).

4. **Clean status under android-app/:**
   ```bash
   git status --short android-app/
   ```
   Expected: no output.

Report to planner: the four new commit SHAs, the Step 5.1 zero-count, and the Step 5.2 pass count.

## Acceptance

- 4 new commits on top of `2eae0cd`.
- `git ls-files --others --exclude-standard android-app/` returns 0 lines.
- `./gradlew.bat :app:testDebugUnitTest` passes 403/403 (unchanged).
- Commit messages name the content scope (gradle / source / jniLibs) so archaeology is clear.

## Out of scope

- Top-level untracked state: `notes/**` `.md` files, `artifacts/**`, `CLAUDE.md`, `.mcp.json`, `.serena/`, `CURRENT_LOCAL_TESTING_STATE_20260410.md`, `GUIDE_PLAN.md`, etc. Separate concern; this slice is `android-app/`-only.
- Any production-code change.
- Git LFS migration for jniLibs.
- Renaming, restructuring, or deleting any tracked file.
- Modifications to `opencode.json` (appears modified in top-level `git status` but is not in this slice's scope).

## STOP conditions (explicit)

STOP and flag to planner if:

- Step 0 baseline is not 403/403.
- Step 1 safety check (`git check-ignore` output) is non-empty.
- Step 2 cached count is not 10.
- Step 3 cached count is <181 or >187 (or any category is >5 off expected).
- Step 4 `.so` count is not 12 or total cached count is not 13.
- Step 5 any check fails.
- Any `git add` subcommand emits a warning (e.g. LF/CRLF, file mode, etc.) that the operator doesn't recognize as benign.

In any STOP case, do NOT attempt recovery; report state and wait for planner guidance.

---

## Addendum — planner correction 2026-04-20 evening

Codex hit Step 1's safety STOP correctly. The `mobile_pack/` blanket ignore pattern shadowed three tracked production assets (`senku_manifest.json` 2.3 KB, `senku_mobile.sqlite3` 285 MB, `senku_vectors.f16` 76 MB). Planner's D2 decision was based on an incomplete inventory — the directory mixes tracked production assets with 6 untracked items (5 zero-byte placeholders from an older filename convention + 1 stale validation report from 2026-04-19).

Verified: none of the 6 untracked items are referenced by production code (`android-app/app/src/`, `scripts/`, `mobile_pack.py`, `ingest.py`). They are orphan output from an earlier pack-export pipeline. The live pack uses `senku_mobile.sqlite3` (not `.db` variants).

### Revised Step 1

Replace the Step 1 `.gitignore` content block with the version below (removes `mobile_pack/` entry) AND delete the 6 defunct files in the same commit:

```
# android-app build output
android-app/app/build/
android-app/build/

# android-app machine-specific config (SDK paths, etc.)
android-app/local.properties

# android-app lane bench scratch
android-app/artifacts/

# android-app device runtime state (SharedPreferences dump)
android-app/senku_model_store.xml
```

(No `mobile_pack/` pattern. Real pack assets stay tracked.)

### New Step 1 action sequence

1. If the current working tree still has the old Step 1 `.gitignore` edit staged or unstaged, revert it first: `git checkout .gitignore` (safe — .gitignore is tracked, this restores pre-slice state).
2. Re-edit `.gitignore` with the corrected content block above (5 patterns, no `mobile_pack/`).
3. Delete the 6 defunct files:
   ```bash
   rm android-app/app/src/main/assets/mobile_pack/senku.db
   rm android-app/app/src/main/assets/mobile_pack/senku_guides.db
   rm android-app/app/src/main/assets/mobile_pack/senku_mobile.db
   rm android-app/app/src/main/assets/mobile_pack/senku_pack.db
   rm android-app/app/src/main/assets/mobile_pack/senku_pack.sqlite
   rm android-app/app/src/main/assets/mobile_pack/metadata_validation_report.json
   ```
4. Safety verification (re-run from original Step 1):
   ```bash
   git ls-files android-app/ | xargs git check-ignore 2>/dev/null
   ```
   MUST return empty. The three tracked `mobile_pack/` production assets should no longer appear.
5. Stage and commit:
   ```bash
   git add .gitignore
   git commit -m "repo-hygiene: gitignore android-app build output, local.properties, bench scratch, device runtime state; remove defunct mobile_pack placeholders"
   ```

The deletions don't need explicit `git add` — they're untracked files, so `rm` removes them from the working tree and nothing needs staging for the deletion.

### Revised expected post-commit state

After corrected Step 1, the untracked file count should drop by roughly:
- 1915 `app/build/**` (now ignored)
- 1 `local.properties` (now ignored)
- 3 `android-app/artifacts/**` (now ignored)
- 1 `senku_model_store.xml` (now ignored)
- 1 `android-app/build/reports/...` (now ignored)
- 6 `mobile_pack/` defunct files (now deleted from working tree)

Total reduction: 1927. Remaining untracked: ≈207 files (was 2134).

### Revised Step 5 acceptance

Unchanged: `git ls-files --others --exclude-standard android-app/` returns 0 lines. No tracked file shadowed by any ignore pattern (verified at Step 1 gate, implicitly).

### Carry-over backlog flag

Planner to add to `CP9_ACTIVE_QUEUE.md` Carry-over section (not in this slice):
- `scripts/export_mobile_pack.py` / `mobile_pack.py` may have write paths that produce the 6 defunct filenames (`senku.db`, `senku_guides.db`, `senku_mobile.db`, `senku_pack.db`, `senku_pack.sqlite`, `metadata_validation_report.json`). If so, the next pack regen will reintroduce the noise. Audit the export script output conventions and clean up any stale write paths.

### Resume instruction

After corrected Step 1 lands, proceed straight to Steps 2–5 as originally written. No other step is affected.
