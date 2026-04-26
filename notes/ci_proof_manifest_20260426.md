# CI Proof Manifest - 2026-04-26

Scope: durable local note for current CI proof, because GitHub Actions
artifacts are retained for a bounded period.

## Current Green Head

- Head: `88564b1` (`cover deleted guide ingest freshness`)
- Non-Android Regression Gate: run `24958570887`
- Result: success
- URL: `https://github.com/YotsuBotfds/senku-app/actions/runs/24958570887`
- Mode: `Generated`
- Label: `github_88564b1_generated_fixture_current_head_20260426`
- Speed proof: `Cache FastEmbed model files` and `Cache retrieval index` were
  both skipped; generated analyzer/regression gate completed green.
- Security companion: no `Actions Security Lint` run was expected for
  `c7c9270`, `0624765`, or `88564b1` because those pushes did not touch
  `.github/workflows/**` or `.github/CODEOWNERS`; the latest applicable
  security lint remains `379181a` / run `24958384160`, which passed.

## Previous Green Head

- Head: `c7c9270` (`add freshness and overlap backlog proofs`)
- Non-Android Regression Gate: run `24958459769`
- Result: success
- URL: `https://github.com/YotsuBotfds/senku-app/actions/runs/24958459769`
- Mode: `Generated`
- Label: `github_c7c9270_generated_fixture_current_head_20260426`
- Speed proof: `Cache FastEmbed model files` and `Cache retrieval index` were
  both skipped; generated analyzer/regression gate completed green.

## Previous Green Generated Head

- Head: `379181a` (`skip fastembed for generated ci`)
- Non-Android Regression Gate: run `24958386310`
- Result: success
- URL: `https://github.com/YotsuBotfds/senku-app/actions/runs/24958386310`
- Mode: `Generated`
- Label: `github_379181a_generated_fixture_skip_fastembed_20260426`
- Generated fixture: `tests\fixtures\non_android_generated\candidate.json`
- Baseline diagnostics: `tests\fixtures\non_android_generated\baseline_diag`
- Speed proof: `Cache FastEmbed model files` and `Cache retrieval index` were
  both skipped; the gate ran the generated analyzer/regression path directly.
- Security companion: `Actions Security Lint` run `24958384160` passed on the
  same head.

## Previous Green Fast Head

- Head: `4926d93` (`record ci proof backlog notes`)
- Non-Android Regression Gate: run `24958108279`
- Result: success
- URL: `https://github.com/YotsuBotfds/senku-app/actions/runs/24958108279`
- Mode: `Fast`
- Label: `github_4926d93_cachehit_smoke_20260426`
- Cache state: retrieval index cache hit; restored key
  `retrieval-index-Windows-Fast-false-3654458045aabc5aa590f8788f7ffdd8e2b843e2e50f04cc63b1db60f7097e48`.
- Validation shape: selected `43` guide files, validated existing compact DB
  with `ingest.py --stats`, and did not rebuild.
- Retrieval expectation policy: compact smoke index allowed retrieval warnings
  before the cache hit/miss branch, so cache-hit and cache-miss compact runs
  now share the same policy.
- Warnings observed: `RE9-MH-001` retrieval owner warnings in Eval9 compact
  smoke retrieval; accepted for compact smoke only.

## Previous Green Cache-Miss Proof

- Head: `1d3832f` (`add generated mode ci fixtures`)
- Non-Android Regression Gate: run `24958353451`
- Result: success
- URL: `https://github.com/YotsuBotfds/senku-app/actions/runs/24958353451`
- Mode: `Generated`
- Label: `github_1d3832f_generated_fixture_20260426`
- Validation shape: tracked generated bench fixture analyzed successfully
  against tracked baseline diagnostics. This proved Generated mode no-DB
  behavior before the later FastEmbed/cache skip optimization.

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
- Bench bundle artifact: uploaded by the run; artifact reported by follow-up
  scan as `6647805324`, expiring `2026-05-10T13:39:07Z`.

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

- Keep compact smoke and full-index validation separate. The green run proves
  fast CI wiring, prompt checks, artifact upload, and compact retrieval smoke;
  it is not full-corpus retrieval evidence.
- Add a separate full-index/manual or scheduled lane before treating GitHub as a
  full retrieval quality oracle.
- Generated mode no-DB behavior is now proven with a committed fixture and a
  follow-up skip-FastEmbed workflow run. Remaining work is policy only: decide
  whether this tiny fixture should stay as the permanent CI smoke or be
  supplemented by a larger scheduled generated bench.
