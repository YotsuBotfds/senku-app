# Spec: .gitignore Cleanup

Date: 2026-04-19
Task: `DAY-G-01`

## Purpose

The repo currently has no `.gitignore` committed. `.gitignore` itself
shows as untracked in `git status`. This means every preflight gate
has to tolerate a large untracked baseline — which is what tripped the
first 2026-04-19 overnight run.

Fix: commit a `.gitignore` that covers the confirmed build-artifact and
editor-metadata directories from the current untracked baseline. Do
NOT ignore files that might be intentionally untracked-for-now (user
notes, dated snapshots). Err on the side of ignoring less — leaving a
file untracked is reversible, committing a pattern that hides a
legitimate file is harder to debug.

## File Content

Write exactly this content to `.gitignore` at the repo root:

```gitignore
# --- Build caches and generated output ---
.gradle/
.gradle-user-home/
android-app/.gradle/
android-app/.gradle-home/

# --- IDE and editor metadata ---
.idea/
android-app/.idea/
.android/
android-app/.android/

# --- Claude local configuration ---
.claude/

# --- Temporary directories ---
.tmp/
_tmp_gallery/

# --- Python ---
venv/
.venv/
__pycache__/
*.pyc
.pytest_cache/
.mypy_cache/
.ruff_cache/

# --- OS cruft ---
.DS_Store
Thumbs.db

# --- Timestamped generated HTML reports at repo root ---
# Pattern matches files like "1151-4-17-26.html" or "1400-4-17-26.html"
# at the repo root only. Does not match HTML files under artifacts/ or
# notes/ (those are intentional deliverables).
/[0-9]*-[0-9]*-[0-9]*.html
```

## Explicitly NOT Ignored (leave untracked for now)

The following currently-untracked files are NOT included in the
`.gitignore`. They may be committed separately in a follow-up pass
after the owner decides their fate. Codex must not add them to the
.gitignore in this task:

- `CLAUDE.md`, `GUIDE_PLAN.md`, `TESTING_METHODOLOGY.md`,
  `MIGRATION_LLAMACPP.md`, `README_OPEN_IN_CODEX.md`,
  `CURRENT_LOCAL_TESTING_STATE_20260410.md`,
  `UI_DIRECTION_AUDIT_20260414.md` — documentation notes, may or may
  not be intended for version control
- `LM_STUDIO_MODELS_20260410.json` — config snapshot, owner decision
  pending
- `4-13guidearchive.zip` — archive, owner decision pending

If the owner wants any of these tracked, that is a separate commit.
If the owner wants any ignored, that is a separate .gitignore edit.

## Verification

After writing .gitignore, run:

```
git status --short
```

Expected outcome:

- All paths listed under "Build caches", "IDE metadata", "Temporary
  directories", and "Python" patterns are no longer shown as untracked
- `.gitignore` itself now shows as `??` (still untracked — it will be
  committed in Step 2 below)
- The "Explicitly NOT Ignored" files above ARE still shown as `??`
  (correct — they should remain untracked until owner decides)

## Commit

One commit:

```
git add .gitignore

git commit -m "$(cat <<'EOF'
Add .gitignore for build caches, IDE metadata, Python, and timestamped HTML

Establishes the project's first committed .gitignore. Covers the
confirmed build-artifact and editor-metadata patterns from the
current untracked baseline. Leaves documentation notes and dated
snapshots untracked-but-not-ignored pending per-file owner decision.

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
```

## Acceptance Criteria

- `.gitignore` exists at repo root with the content above
- `git status --short` output shrinks meaningfully — the four
  pattern categories above should all disappear from the untracked
  list
- The "Explicitly NOT Ignored" files remain visible as untracked
- No tracked file is affected
