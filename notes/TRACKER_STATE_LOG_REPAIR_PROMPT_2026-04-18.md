# Tracker State Log Repair - Mid-Wave-B Dispatch

**Purpose:** Fix `reviewer_backend_tasks.md` State Log truncation + backfill
missing Wave A rows. Lands as its own commit so Wave B history stays clean.

**Timing:** Run this **between** Wave B task commits, not inside one. If you
are mid-edit on a Wave B task, finish that task's commit first, then run
this, then resume the next Wave B task.

---

## Dispatch Prompt (paste to Codex)

This is a tracker-hygiene task, not a code change. Do not touch any `.py`,
`.java`, or `.kt` files. Only `reviewer_backend_tasks.md` should appear in
the diff.

### What's wrong

The State Log table at the bottom of `reviewer_backend_tasks.md` is
truncated. In the current `HEAD` the last row is cut mid-sentence:

```
| BACK-L-04 | done | codex | 2026-04-18 | 2026-04-18 | summarize_latency.py now prints p50/p95/max p
```

The row has no closing `|` and no following newline structure. Any future
append to this table will either concatenate onto the dangling row or
produce an unparseable markdown table. Separately, the log is also
incomplete vs. the Wave A closeout claim ("25 done + 2 invalidated out of
26 original"). Several rows that were supposed to land with commit
`a021d19` are missing.

### Step 1 - verify the current tail state

Before editing, confirm the truncation exists in your working tree (not a
mount artifact):

```
git show HEAD:reviewer_backend_tasks.md | tail -n 25
```

If the tail in `HEAD` already has all rows closed properly and you only see
a truncation in the on-disk copy, the fix is just to sync the on-disk copy
to `HEAD`'s tail. In that case, report what you see and stop before
editing.

If `HEAD` itself has the truncation, continue.

### Step 2 - complete the truncated BACK-L-04 row

Replace the final line with:

```
| BACK-L-04 | done | codex | 2026-04-18 | 2026-04-18 | `summarize_latency.py` now prints p50/p95/max per-stage latencies with a structured summary line suitable for bench ingestion |
```

If you have more accurate wording from the actual L-04 commit message
(`git log --all --oneline --grep=L-04`), use that. The above is a safe
default if the commit summary is unavailable.

### Step 3 - audit State Log completeness vs. Wave A landings

The Wave A closeout claim is 25 done + 2 invalidated. The current table
visible rows are:

```
BACK-P-04 done, BACK-P-02 done, BACK-P-01 done, BACK-H-05 invalidated,
BACK-H-04 done, BACK-H-02 done, BACK-T-02 done, BACK-T-01 done,
BACK-L-04 done (truncated).
```

That's 8 done + 1 invalidated, not 25 + 2. The remaining rows need to be
reconstructed from:

1. `git log --all --oneline --grep='BACK-'` - every commit whose subject
   mentions a `BACK-*` ID.
2. Task-body headers in `reviewer_backend_tasks.md` that carry a
   `[done <date> <hash>]` marker.
3. The "Landed this closeout" list inside
   `notes/REVIEWER_BACKEND_TRACKER_20260418.md`.

For each task ID that appears landed in any of those three sources but
does **not** yet have a State Log row, append a row using this format:

```
| BACK-XX-nn | done | codex | <opened YYYY-MM-DD> | <closed YYYY-MM-DD> | one-line summary pulled from the commit subject or task body |
```

For the three rows that were explicitly planned in commit `a021d19` but
appear to be missing, use these exact entries if they're not already
present:

```
| BACK-R-04 | done | codex | 2026-04-18 | 2026-04-18 | anchor prior now passes as a typed `AnchorPriorState` through retrieval and rerank |
| OPUS-E-06 | done | codex | 2026-04-18 | 2026-04-18 | DetailActivity answer generation extracted behind `AnswerPresenter` + `DetailAnswerPresenterHost`; commit `b41128a` |
| BACK-R-03 | invalidated | codex | 2026-04-18 | 2026-04-18 | not observable on Android: `ENABLE_ANCHOR_PRIOR = false` in `SessionMemory.java` and no productized caller; follow-up filed as `BACK-R-05` |
| BACK-R-05 | open | - | 2026-04-18 | - | scout: decide whether to productize the Android anchor-prior path or delete the flag; post-release |
| BACK-T-04 | open | - | 2026-04-18 | - | worker test-infra: repair `Quote-AndroidShellArg` in `scripts/run_android_session_flow.ps1`; post-release |
```

Do **not** flip rows for any `BACK-U-*` task that is currently in flight
from the Wave B dispatch. Leave Wave B State Log additions to the worker
landing those tasks.

### Step 4 - ordering and formatting rules

- Keep the existing header row and separator row unchanged.
- Keep existing rows in their current order; append new rows at the
  bottom. Chronological ordering within the append is fine.
- Every row must have exactly 6 `|`-separated columns plus leading and
  trailing `|`.
- Every row must end with a newline; the file must end with a single
  trailing newline.
- Do not reflow or rewrap any row.

### Step 5 - validate

Run:

```
python3 -c "
import re
text = open('reviewer_backend_tasks.md', encoding='utf-8').read()
log = text.split('## State Log', 1)[1]
rows = [line for line in log.splitlines() if line.startswith('|') and 'BACK-' in line or 'OPUS-' in line]
for r in rows:
    cells = r.split('|')
    assert len(cells) == 8, f'bad row: {r!r} ({len(cells)} cells)'
print(f'{len(rows)} rows validated')
"
```

All rows must pass. If any row fails, fix it before committing.

### Step 6 - land

```
git add reviewer_backend_tasks.md
git commit -m "Repair State Log: complete truncated BACK-L-04 row + backfill Wave A landings"
git show --stat HEAD
```

Report:

- Commit hash.
- Number of rows in the State Log before and after.
- Any task IDs you found landed in git history but could not confidently
  backfill (leave those for Tate to resolve rather than guessing).
- Confirmation that no non-`reviewer_backend_tasks.md` files are in the
  diff.

If Wave B is still in flight after this lands, resume the next Wave B
task immediately. The State Log repair is fire-and-forget; it does not
block Wave B.