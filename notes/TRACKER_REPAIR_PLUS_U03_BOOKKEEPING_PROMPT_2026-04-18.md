# Tracker Repair + BACK-U-03 Bookkeeping - Combined Prompt

**Purpose:** Single commit that repairs the `reviewer_backend_tasks.md`
State Log truncation, backfills missing Wave A rows, and records the
`BACK-U-03` landing (`af49d91`). Runs between Wave B task commits. Lands
before `BACK-U-01` dispatches so U-01's worker inherits a clean tracker.

Supersedes `notes/TRACKER_STATE_LOG_REPAIR_PROMPT_2026-04-18.md` for this
session.

---

## Dispatch Prompt (paste to Codex)

Tracker hygiene only. No code files in the diff. The only files that
should appear in `git show --stat HEAD` are:

- `reviewer_backend_tasks.md`
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
- `opustasks.md` (only if it currently references U-03 as pending)

### Step 1 - verify current state

```
git show HEAD:reviewer_backend_tasks.md | tail -n 25
```

Confirm the State Log's last row is the truncated `BACK-L-04` row ending
with `"prints p50/p95/max p"` and no closing `|`. If `HEAD` already has
all rows closed, the on-disk truncation is a mount artifact - sync the
on-disk copy to `HEAD`'s tail and skip step 2's reformat, but still do
steps 3-6.

### Step 2 - complete the truncated BACK-L-04 row

Replace the truncated final line with:

```
| BACK-L-04 | done | codex | 2026-04-18 | 2026-04-18 | `summarize_latency.py` now prints p50/p95/max per-stage latencies with a structured summary line suitable for bench ingestion |
```

If `git log --all --oneline --grep=L-04` yields a more accurate one-line
summary from the commit body, prefer that wording.

### Step 3 - append the BACK-U-03 landing row

```
| BACK-U-03 | done | codex | 2026-04-18 | 2026-04-18 | desktop + Android compute and surface high/medium/low answer confidence labels; MetaStrip renders likely-match and low-confidence tokens with phone + tablet instrumentation proof; commit `af49d91` |
```

### Step 4 - backfill missing Wave A State Log rows

The Wave A closeout claim is 25 done + 2 invalidated. The current log has
roughly 8 done + 1 invalidated. Reconstruct the missing rows from:

1. `git log --all --oneline --grep='BACK-'` - every commit whose subject
   mentions a `BACK-*` ID.
2. Task-body headers in `reviewer_backend_tasks.md` that carry a
   `[done <date> <hash>]` marker.
3. The "Landed this closeout" list inside
   `notes/REVIEWER_BACKEND_TRACKER_20260418.md`.

For each landed task ID not yet in the State Log, append a row using this
format:

```
| BACK-XX-nn | done | codex | <opened YYYY-MM-DD> | <closed YYYY-MM-DD> | one-line summary |
```

The following rows should be present if missing (do not duplicate if
already there):

```
| BACK-R-04 | done | codex | 2026-04-18 | 2026-04-18 | anchor prior now passes as a typed `AnchorPriorState` through retrieval and rerank |
| OPUS-E-06 | done | codex | 2026-04-18 | 2026-04-18 | DetailActivity answer generation extracted behind `AnswerPresenter` + `DetailAnswerPresenterHost`; commit `b41128a` |
| BACK-R-03 | invalidated | codex | 2026-04-18 | 2026-04-18 | not observable on Android: `ENABLE_ANCHOR_PRIOR = false` in `SessionMemory.java` and no productized caller; follow-up filed as `BACK-R-05` |
| BACK-R-05 | open | - | 2026-04-18 | - | scout: decide whether to productize the Android anchor-prior path or delete the flag; post-release |
| BACK-T-04 | open | - | 2026-04-18 | - | worker test-infra: repair `Quote-AndroidShellArg` in `scripts/run_android_session_flow.ps1`; post-release |
```

For any task ID you find landed in git history but cannot confidently
backfill (unclear closed-date or ambiguous summary), leave it for Tate
rather than guessing. List those IDs in the commit message body.

Do **not** flip any `BACK-U-01` or `BACK-U-02` row - those are still in
flight and their workers will append their own rows when they land.

### Step 5 - flip the BACK-U-03 task body header

Find the `BACK-U-03` task block near line 358 of
`reviewer_backend_tasks.md`. The current header reads:

```
- `BACK-U-03` · **P1 · M · worker** · Confidence label on every answer
```

Prepend the done marker so the header reads:

```
- `BACK-U-03` · **[done 2026-04-18 `af49d91`]** · **P1 · M · worker** · Confidence label on every answer
```

Update the final bullet in that block (currently
`**Gate cleared** ... dispatchable`) to read:

```
  - **Landed 2026-04-18 (`af49d91`)** - desktop 50-query confidence panel + Android `ConfidenceTokenRenderTest` prove phone + tablet rendering; `notes/specs/meta_strip_confidence_token_addendum.md` captures the MetaStrip contract
```

### Step 6 - update the REVIEWER_BACKEND_TRACKER_20260418 note

In `notes/REVIEWER_BACKEND_TRACKER_20260418.md`, update the Uncertainty
lane / Wave B status prose to reflect:

- `BACK-U-03` landed `af49d91`
- `BACK-U-01` and `BACK-U-02` are the remaining Wave B items
- U-01 is unblocked (the confidence-label dependency is satisfied)
- U-02 was never dependency-gated

If the note has a "Landed this closeout" list, append U-03 to it. Keep
edits focused - do not rewrite the whole file.

### Step 7 - opustasks.md check

```
grep -n 'BACK-U-03\|U-03' opustasks.md
```

If U-03 is referenced as pending, flip to landed with hash `af49d91`. If
U-03 is not mentioned (Wave B was tracked only in reviewer_backend_tasks
and the REVIEWER tracker note), skip this step.

### Step 8 - validate

Run the State Log row sanity check:

```
python3 -c "
text = open('reviewer_backend_tasks.md', encoding='utf-8').read()
log = text.split('## State Log', 1)[1]
rows = [line for line in log.splitlines() if line.startswith('|') and ('BACK-' in line or 'OPUS-' in line)]
for r in rows:
    cells = r.split('|')
    assert len(cells) == 8, f'bad row: {r!r} ({len(cells)} cells)'
print(f'{len(rows)} rows validated')
"
```

All rows must pass. Fix any that don't before committing.

### Step 9 - land

```
git status
git add reviewer_backend_tasks.md notes/REVIEWER_BACKEND_TRACKER_20260418.md
# also add opustasks.md only if step 7 edited it
git commit -m "$(cat <<'EOF'
Tracker: repair State Log + record BACK-U-03 landing

- Complete truncated BACK-L-04 row
- Backfill Wave A State Log rows missing from prior bookkeeping
- Append BACK-U-03 row (af49d91) and flip its task-body header
- Update REVIEWER_BACKEND_TRACKER_20260418 Wave B status

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
git show --stat HEAD
```

### Step 10 - report

Reply with:

- Commit hash.
- State Log row count before and after.
- Any task IDs that were left for Tate to resolve (unconfident backfill).
- Confirmation that no `.py`, `.java`, `.kt`, or `.kts` files appear in
  the diff.

If Wave B is still in flight, resume the next Wave B task immediately
after reporting. This tracker repair does not block U-01 or U-02.
