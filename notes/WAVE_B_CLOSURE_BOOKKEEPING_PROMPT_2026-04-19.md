# Wave B Closure Bookkeeping Prompt - 2026-04-19

**Purpose:** Single tracker-only commit that records the `BACK-U-02`
(`d974ebc`) and `BACK-U-01` (`eb398dc`) landings and closes Wave B.

Runs after U-01 landed and before the overnight low-stakes queue fires.
This gives the overnight worker a clean tracker to inherit.

Follows the same pattern as `notes/TRACKER_REPAIR_PLUS_U03_BOOKKEEPING_PROMPT_2026-04-18.md`.

---

## Dispatch Prompt (paste to Codex)

Tracker hygiene only. No code files in the diff. The only files that
should appear in `git show --stat HEAD` are:

- `reviewer_backend_tasks.md`
- `notes/REVIEWER_BACKEND_TRACKER_20260418.md`
- `opustasks.md` (only if it currently references U-01 or U-02 as pending)

### Step 1 - verify current State Log tail

```
git show HEAD:reviewer_backend_tasks.md | awk '/## State Log/,0' | tail -n 15
```

Confirm whether rows for `BACK-U-02` (`d974ebc`) and `BACK-U-01`
(`eb398dc`) are already in the State Log. The U-02 landing commit
(`d974ebc`) and the U-01 landing commit (`eb398dc`) may or may not have
added their own rows. Only add the rows that are missing.

Also confirm whether the `BACK-U-01` task-body header at
`reviewer_backend_tasks.md` line ~327 still reads:

```
- `BACK-U-01` · **P0 · L · worker** · Low-applicability response mode
```

or already reads:

```
- `BACK-U-01` · **[done 2026-04-19 `eb398dc`]** · **P0 · L · worker** · ...
```

Only flip if not already flipped.

### Step 2 - append the BACK-U-02 State Log row if missing

```
| BACK-U-02 | done | codex | 2026-04-18 | 2026-04-18 | desktop + Android prepend safety-critical escalation line above "Closest matches in the library:" on abstain; `_SAFETY_CRITICAL_ESCALATION_LINE` + `_scenario_frame_is_safety_critical` helpers on desktop, mirrored in `OfflineAnswerEngine.buildAbstainAnswerBody`; phone + tablet instrumentation proof; commit `d974ebc` |
```

### Step 3 - append the BACK-U-01 State Log row

```
| BACK-U-01 | done | codex | 2026-04-19 | 2026-04-19 | desktop + Android introduce three-way `confident`/`uncertain_fit`/`abstain` answer mode with deterministic uncertain-fit body template and `UNSURE FIT` card variant; reuses U-02 escalation helper for safety-critical uncertain-fit; boundary tests + reviewer-worked example pass on both engines; phone + tablet artifacts under `artifacts/bench/wave_b_back-u-01_2026-04-19/`; commit `eb398dc` |
```

### Step 4 - flip the BACK-U-01 task body header (if not already flipped)

Find the `BACK-U-01` task block near line 327 of
`reviewer_backend_tasks.md`. If the header currently reads:

```
- `BACK-U-01` · **P0 · L · worker** · Low-applicability response mode
```

Prepend the done marker so it reads:

```
- `BACK-U-01` · **[done 2026-04-19 `eb398dc`]** · **P0 · L · worker** · Low-applicability response mode
```

Update the final bullet in that block (currently
`**Gate cleared** — OPUS-E-06 landed 2026-04-18 (b41128a); dispatchable`)
to read:

```
  - **Landed 2026-04-19 (`eb398dc`)** — `notes/specs/uncertain_fit_mode_spec.md` and `notes/specs/paper_answer_card_uncertain_fit_addendum.md` pin the three-way mode contract and `UNSURE FIT` card variant; reviewer example (`He has barely slept, keeps pacing...`) routes to uncertain-fit; phone + tablet artifacts at `artifacts/bench/wave_b_back-u-01_2026-04-19/`
```

If the U-01 header was already flipped by the landing commit, skip this
step but verify the final bullet already carries a `Landed ...` summary.
If it only says `Gate cleared`, still update it per above.

### Step 5 - update REVIEWER_BACKEND_TRACKER_20260418.md

In `notes/REVIEWER_BACKEND_TRACKER_20260418.md`, update the Uncertainty
lane / Wave B status prose to reflect:

- `BACK-U-01` landed `eb398dc`
- `BACK-U-02` landed `d974ebc`
- `BACK-U-03` landed `af49d91`
- **Wave B is complete.** All three tasks landed; ready for CHECKPOINT 9
  release-candidate gate planning.

If the note has a "Landed this closeout" list, append U-01 and U-02 to
it. Keep edits focused — do not rewrite the whole file.

### Step 6 - opustasks.md check

```
grep -n 'BACK-U-01\|BACK-U-02\|U-01\|U-02' opustasks.md
```

If U-01 or U-02 are referenced as pending, flip to landed with hashes
`eb398dc` and `d974ebc` respectively. If neither is mentioned (Wave B
was tracked only in `reviewer_backend_tasks.md` and the REVIEWER
tracker note), skip this step.

### Step 7 - validate

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

Also grep for any Wave B row that still says `in_progress` or has a
missing `closed` date — every U-row should now be `done`.

### Step 8 - land

```
git status
git add reviewer_backend_tasks.md notes/REVIEWER_BACKEND_TRACKER_20260418.md
# also add opustasks.md only if step 6 edited it
git commit -m "$(cat <<'EOF'
Tracker: close Wave B (record BACK-U-02 + BACK-U-01 landings)

- Append BACK-U-02 (d974ebc) + BACK-U-01 (eb398dc) State Log rows if missing
- Flip BACK-U-01 task-body header to [done 2026-04-19 eb398dc]
- Update REVIEWER_BACKEND_TRACKER_20260418 Wave B status to complete;
  all three U-tasks landed; ready for CHECKPOINT 9 gate planning

Co-Authored-By: Claude Opus 4.7 <noreply@anthropic.com>
EOF
)"
git show --stat HEAD
```

### Step 9 - report

Reply with:

- Commit hash.
- State Log row count before and after.
- Which steps were skipped because the landing commit had already
  done the bookkeeping (if any).
- Confirmation that no `.py`, `.java`, `.kt`, or `.kts` files appear in
  the diff.

Stop after reporting. Do **not** start the overnight low-stakes queue in
the same session — that is a separate dispatch (see
`notes/OVERNIGHT_LOW_STAKES_QUEUE_U01_INFLIGHT_2026-04-18.md`, minus the
`u01-collision` stop-status entry which can now be removed or softened
since U-01 is landed).
