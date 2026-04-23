# Abstain Tuning Analysis - 2026-04-18

## Scope

BACK-U-04 asked for two things:

1. document why each `_ABSTAIN_*` constant sits where it does
2. add a reusable regression/sensitivity panel with a checked-in baseline

The reusable runner now lives at [`../scripts/run_abstain_regression_panel.ps1`](../scripts/run_abstain_regression_panel.ps1), and the checked-in baseline artifacts live under [`../artifacts/bench/abstain_baseline_20260418/`](../artifacts/bench/abstain_baseline_20260418/).

This baseline is intentionally retrieval-only. That is the right scope for this gate because `_should_abstain()` runs before generation, so the meaningful regression surface is retrieval strength, not LLM phrasing.

## What I Ran

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run_abstain_regression_panel.ps1
```

Run details from the checked-in baseline:

- timestamp: `2026-04-18T19:52:43Z`
- collection size: `49651`
- top-k: `8`
- LM Studio URL: `http://localhost:1234/v1`
- panel size: `12` queries
  - `6` should abstain
  - `6` should not abstain

Primary outputs:

- [`../artifacts/bench/abstain_baseline_20260418/summary.md`](../artifacts/bench/abstain_baseline_20260418/summary.md)
- [`../artifacts/bench/abstain_baseline_20260418/summary.json`](../artifacts/bench/abstain_baseline_20260418/summary.json)
- [`../artifacts/bench/abstain_baseline_20260418/sweep_summary.csv`](../artifacts/bench/abstain_baseline_20260418/sweep_summary.csv)
- [`../artifacts/bench/abstain_baseline_20260418/query_details.csv`](../artifacts/bench/abstain_baseline_20260418/query_details.csv)

## Constant-by-Constant Rationale

### `_ABSTAIN_ROW_LIMIT = 3`

Keep the gate focused on the same top evidence the user actually feels.

- The top 3 rows were enough to capture every strong in-domain support case in the curated panel.
- Going deeper mostly adds duplicate-family or weak-adjacent rows that expand token overlap without improving support quality.
- The checked-in row-depth comparison found one concrete regression if we look deeper: `video_laptop` abstains at row limits `1`, `2`, and `3`, but flips to **answer** at row limit `5` because the tail adds another generic token and breaks the unique-hit guard.

So `3` is a practical noise cap: deep enough to catch adjacent corroboration, shallow enough to avoid tail-row contamination.

### `_ABSTAIN_MAX_OVERLAP_TOKENS = 1`

This is the main lexical "weak support" guard.

- In the baseline panel, every `should_not_abstain` query had top-3 `max_overlap >= 3`.
- The current abstain true positives sit at `max_overlap` `0` or `1`.
- Allowing abstain at `max_overlap <= 1` means a single token like `video`, `list`, `design`, or `tune` is treated as adjacency, not proof.
- Raising the limit to `2` would start treating thin-but-real paraphrases as abstain candidates. Exploratory checks outside the checked-in panel already showed in-domain prompts like "dirty nail" and "clean brown water" often land at exactly `2` overlap tokens before the rest of the support accumulates.

So `1` is the right side of the line for now: one token can be accidental; two starts to look like actual topical anchoring.

### `_ABSTAIN_MIN_VECTOR_SIMILARITY = 0.62`

`0.62` is the current conservative floor for "vector support is too weak to trust."

Observed baseline behavior:

- `debug_csv` (`0.560`) and `video_laptop` (`0.601`) abstain cleanly at the current floor.
- `sort_names` (`0.666`) and `logo` (`0.668`) do **not** abstain at the current floor; they only flip once similarity rises to `0.67`.
- The sweep shows that moving similarity from `0.62` to `0.67` improves abstain recall on this panel without introducing a false positive on this particular set.

Why not raise it immediately anyway?

- This panel is good for catching obvious false negatives, but it does **not** yet include fragile "should answer" cases that live near the boundary.
- Every checked-in `should_not_abstain` case is comfortably supported, so the measured `0.000` false-positive rate is real for this panel but still optimistic for production.
- Because of that, `0.62` remains the safer production default and `0.67` is better treated as a watchpoint, not an automatic retune.

In short: `0.62` is where the current gate still catches very weak vector matches without ratcheting upward based on a panel that lacks near-boundary supported prompts.

### `_ABSTAIN_MIN_UNIQUE_LEXICAL_HITS = 2`

This is the "one token is not enough" companion to the overlap cap.

- At the current similarity floor, moving hits from `1` to `2` is what converts the baseline from `1/6` abstain true positives to `2/6`.
- `video_laptop` has only one unique lexical hit (`video`) and should abstain.
- `resume` reaches exactly two weak lexical hits (`application`, `job`); raising the hit threshold to `3` would catch it, but again that is a stricter posture than this panel alone can justify.
- Keeping the threshold at `2` means the gate still allows generation once retrieval produces more than one distinct content token across the top rows.

So `2` stays as the smallest non-trivial lexical support requirement: `1` is too permissive, `3` looks promising on this panel but should wait for a broader weak-support "should answer" set.

## Sensitivity Sweep

Per spec, I swept:

- similarity: `0.57`, `0.62`, `0.67`
- unique hits: `1`, `2`, `3`

Checked-in results:

| similarity | hits | abstain rate | false-positive rate | TP | FP | FN |
| --- | --- | --- | --- | --- | --- | --- |
| 0.57 | 1 | 0.083 | 0.000 | 1 | 0 | 5 |
| 0.57 | 2 | 0.083 | 0.000 | 1 | 0 | 5 |
| 0.57 | 3 | 0.083 | 0.000 | 1 | 0 | 5 |
| 0.62 | 1 | 0.083 | 0.000 | 1 | 0 | 5 |
| 0.62 | 2 | 0.167 | 0.000 | 2 | 0 | 4 |
| 0.62 | 3 | 0.167 | 0.000 | 2 | 0 | 4 |
| 0.67 | 1 | 0.083 | 0.000 | 1 | 0 | 5 |
| 0.67 | 2 | 0.333 | 0.000 | 4 | 0 | 2 |
| 0.67 | 3 | 0.417 | 0.000 | 5 | 0 | 1 |

Readout:

- Current production point (`0.62 / 2`) is conservative:
  - abstain rate: `0.167`
  - false-positive rate: `0.000`
  - true positives: `2 / 6`
- The strictest tested point (`0.67 / 3`) improves abstain recall materially:
  - abstain rate: `0.417`
  - false-positive rate: `0.000`
  - true positives: `5 / 6`

That does **not** mean we should silently move to `0.67 / 3` yet. It means the quarterly panel is doing its job: it shows exactly where the current defaults are leaving recall on the table, while also showing that the next obvious move still needs validation against a larger weak-support "should answer" set before production retuning.

## Known Limits

This is the smallest credible quarterly baseline, not a full tuning corpus.

- The panel is intentionally compact, so the false-positive estimate is stable only for clear in-domain support cases.
- The sweep only varies similarity and unique-hit thresholds, per task spec. Row limit and overlap cap are documented and observed, but not swept.
- There are still known false-negative shapes outside this panel where generic action words create too much lexical overlap for the current heuristic to abstain. Those are better treated as follow-on overlap-token cleanup than as part of this bounded U-04 patch.

## Quarterly Rerun Expectation

Quarterly rerun command:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run_abstain_regression_panel.ps1
```

If the panel changes, compare:

- abstain rate at the current production point (`0.62 / 2`)
- false-positive rate at the current production point
- which specific queries flip between `answer` and `abstain`
- whether row-limit drift starts showing up on more than the current one query (`video_laptop`)