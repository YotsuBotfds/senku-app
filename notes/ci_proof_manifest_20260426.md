# CI Proof Manifest - 2026-04-26

Scope: durable local note for current CI proof, because GitHub Actions
artifacts are retained for a bounded period.

## Current Green Head

- Head: `eb71a3c` (`relax compact smoke retrieval warnings`)
- Non-Android Regression Gate: run `24957922497`
- Result: success
- URL: `https://github.com/YotsuBotfds/senku-app/actions/runs/24957922497`
- Mode: `Fast`
- Label: `github_eb71a3c_compact_warn_ok_20260426`
- Cache state: retrieval index cache miss; compact scoped CI retrieval DB rebuilt.
- Ingest shape: `43` selected guide files -> `43` guide-summary chunks.
- FastEmbed shape: `43` single-chunk embedding batches.
- Retrieval expectation policy: compact smoke index allowed retrieval warnings;
  prompt expectation errors remained hard failures.
- Bench bundle artifact: uploaded by the run; artifact reported by follow-up scan
  as `6647805324`, expiring `2026-05-10T13:39:07Z`.

## Superseded Runs

- `24957798681`: completed infrastructure successfully on `e4a562a`, but failed
  strict compact-index retrieval warnings for `RE9-MH-001`.
- `24957656574`: compact index still batched `16` guide summaries and was
  cancelled while tuning FastEmbed throughput.
- `24957416553`: scoped 43-guide section index produced `3083` chunks and was
  cancelled as still too expensive for the fast lane.
- `24955889219`: stale deep-research referenced timeout on full rebuild,
  `754` files -> `49711` chunks.

## Remaining CI Follow-Ups

- Prove cache-hit behavior on the compact smoke index after the warning-policy
  branch was moved before cache hit/miss handling.
- Keep compact smoke and full-index validation separate. The green run proves
  fast CI wiring, prompt checks, artifact upload, and compact retrieval smoke;
  it is not full-corpus retrieval evidence.
- Add a separate full-index/manual or scheduled lane before treating GitHub as a
  full retrieval quality oracle.
- Prove `Generated` mode no-DB behavior once a small committed or stable
  generated bench fixture exists.
