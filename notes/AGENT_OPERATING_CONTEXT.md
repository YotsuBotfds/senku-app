# Agent Operating Context

Living context for agents. Keep `AGENTS.md` as the short landing page and move
changing operational detail here.

## Runtime Baseline

- Current planner queue source: [`ACTIVE_WORK_TRACKER_20260426.md`](./ACTIVE_WORK_TRACKER_20260426.md).
  Use it before interpreting `notes/dispatch/` filenames as active work.
- Desktop retrieval uses hybrid retrieval plus metadata-aware reranking and structured session state in [`query.py`](../query.py).
- Android app is the active parity project: retrieval quality, prompt shaping, and chat/session continuity.
- Mobile targets both `E2B` and `E4B`: `E2B` is the practical floor, `E4B` is the quality tier to keep evaluating.
- Session-aware follow-up behavior is part of normal validation.
- Local compute routing ladder is `GLM 5.1 > Spark > Qwen 27B > Qwen 9B`.
- For local Qwen 27B, prefer pinned identifier `qwen27_58k`; avoid running 9B and 27B concurrently on constrained VRAM.

## Validation Lanes

- GitHub Actions billing posture: workflows are intentionally manual-only.
  Do not reintroduce automatic `push`, `pull_request`, or scheduled triggers
  without an explicit user request. Use the local gate as the default
  validation lane for new heads; treat `gh run list` only as a sanity check
  that no automatic run was started.

- Standard desktop deterministic/routing check:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile query.py bench.py query_abstain_policy.py query_answer_card_runtime.py query_citation_policy.py query_completion_hardening.py query_response_normalization.py query_prompt_runtime.py query_scenario_frame.py query_routing_text.py rag_bench_answer_diagnostics.py query_routing_predicates.py deterministic_special_case_registry.py special_case_builders.py scripts\analyze_rag_bench_failures.py tests\test_special_cases.py tests\test_query_routing.py tests\test_query_abstain_policy.py tests\test_query_answer_card_runtime.py tests\test_query_citation_policy.py tests\test_query_completion_hardening.py tests\test_query_response_normalization.py tests\test_query_prompt_runtime.py tests\test_query_scenario_frame.py tests\test_query_routing_text.py tests\test_rag_bench_answer_diagnostics.py
$text = Get-Content -LiteralPath .\scripts\run_guide_prompt_validation.ps1 -Raw; [scriptblock]::Create($text) | Out-Null
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_response_normalization tests.test_citation_validation tests.test_query_completion_hardening tests.test_query_prompt_runtime tests.test_query_citation_policy tests.test_rag_bench_answer_diagnostics tests.test_query_scenario_frame tests.test_query_routing_text tests.test_query_abstain_policy tests.test_query_answer_card_runtime tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

- Guide prompt validation uses LiteRT generation on `1235` and an embedding-capable LM Studio endpoint on `1234` by default:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave be -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

- Reviewed-card runtime proof runs should use the explicit opt-in switch and
  then check the artifact config/summary fields
  `reviewed_card_runtime_answers_enabled` and
  `reviewed_card_runtime_answers_env`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave fa -PythonPath .\.venvs\senku-validate\Scripts\python.exe -EnableCardBackedRuntimeAnswers
```

- Manual desktop `query.py` uses the same split runtime by default:
  - generation: `SENKU_GEN_URL` / `--gen-url`, default `http://127.0.0.1:1235/v1`;
  - generation model: `SENKU_GEN_MODEL` / `--model`, default `gemma-4-e2b-it-litert`;
  - embeddings: `SENKU_EMBED_URL` / `--embed-url`, default `http://127.0.0.1:1234/v1`;
  - legacy `LM_STUDIO_URL` remains an embedding alias only.
- Runtime naming decision:
  [`notes/RUNTIME_TARGET_NAMING_20260425.md`](./RUNTIME_TARGET_NAMING_20260425.md).
  If `query.py` starts on `google/gemma-4-26b-a4b`, check `SENKU_*`
  environment variables or CLI overrides before changing defaults.

- If the default embedding lane is unavailable, pass the endpoint explicitly:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave be -PythonPath .\.venvs\senku-validate\Scripts\python.exe -EmbedUrl http://127.0.0.1:1234/v1
```

- Shadow retrieval comparison summaries:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_shadow_comparisons.py artifacts\bench\contextual_shadow_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_20260425 artifacts\bench\section_family_compare_ez_newborn_stride1_20260425 --label contextual --label section-family --label section-family-stride1
```

- RAG diagnostic trend summaries:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\summarize_rag_diagnostics.py artifacts\bench\rag_diagnostics_20260424_1654_rags12_meningitis_compare_final artifacts\bench\rag_diagnostics_20260424_1750_rags13_code_health_final_smoke --label rags12-gap --label rags13-final
```

- RAG answer/card/evidence trend table:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\rag_trend.py artifacts\bench\rag_diagnostics_20260424_1654_rags12_meningitis_compare_final artifacts\bench\rag_diagnostics_20260424_1750_rags13_code_health_final_smoke --label rags12-gap --label rags13-final
```

`rag_trend.py` now includes owner-family concentration columns. Use
`expected_owner_best_rank`, top-3/top-k hit counts, and top-3/top-k shares to
verify expected owners are moving closer instead of merely remaining somewhere
in top-k. It also includes top-1 corpus marker overlay columns when diagnostics
were run with `--corpus-marker-scan`.

- Corpus marker scan for unresolved partials / router-page diagnostics:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_corpus_markers.py --guides-dir guides --output-json artifacts\bench\corpus_marker_scan.json --output-md artifacts\bench\corpus_marker_scan.md
```

Pass the scanner JSON into RAG diagnostics when investigating retrieval/ranking
misses:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\analyze_rag_bench_failures.py <bench-json> --corpus-marker-scan artifacts\bench\corpus_marker_scan.json --output-dir artifacts\bench\<diagnostic-dir>
```

`RAG-EVAL2` adds a held-out eval pack for bridge-thin and unresolved-partial
primary-answer regressions. Keep this in context:

- Pack: `artifacts/prompts/adhoc/rag_eval_partial_router_holdouts_20260425.jsonl`
- Spec: `notes/specs/rag_eval_partial_router_holdouts_20260425.md`
- Dispatch: `notes/dispatch/RAG-EVAL2_partial_router_holdout_pack.md`
- Latest diagnostics:
  `artifacts/bench/rag_eval_partial_router_holdouts_20260425_diag/report.md`
  with 21 expected-guide rows, 8 retrieval misses, 4 ranking misses, 4 prompt
  artifact errors (2 prompt-budget overflows, 2 local generation server 500s),
  2 expected supported, 2 deterministic passes, 1 accepted `uncertain_fit`, 2
  top-1 unresolved-partial rows, and 1 top-1 bridge row.

`RAG-DOCS2` repaired the observed GD-585 `wound-assessment-tool` unresolved
partial with static, source-local wound assessment content. Fresh proof:
`artifacts/bench/corpus_marker_scan_20260425_1622_gd585_repair.md`;
unresolved partials are now `54`.

- High-liability metadata coverage audit:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit.json --output-md artifacts\bench\metadata_coverage_audit.md
```

- Guide-quality priority selector from observed diagnostics plus audits:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_guide_quality_work.py artifacts\bench\<diagnostic-dir> --metadata-audit artifacts\bench\metadata_coverage_audit.json --corpus-marker-scan artifacts\bench\corpus_marker_scan.json --output-json artifacts\bench\guide_quality_priorities.json --output-md artifacts\bench\guide_quality_priorities.md
```

- Family-grain priority selector for assigning actual guide/card work:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_high_liability_families.py artifacts\bench\<diagnostic-dir> --metadata-audit artifacts\bench\metadata_coverage_audit.json --corpus-marker-scan artifacts\bench\corpus_marker_scan.json --output-json artifacts\bench\high_liability_family_priorities.json --output-md artifacts\bench\high_liability_family_priorities.md
```

`RAG-T9` changed the family-priority card columns from pass/gap/no-card to
pass/gap/missing/skipped. Deterministic rows without generated answers now count
as skipped/not evaluable rather than answer-card gaps. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1645_card_semantics.md`.

`RAG-T10` added FB support-owner expectations for `GD-235` and `GD-622` and
tracks uncertain-fit non-owner citations separately from true ranking drift.
Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1658_support_uncertainfit_semantics.md`.

`RAG-META2` added aliases, routing cues, citation policy, and applicability to
the dangerous activation family guides `GD-858`, `GD-859`, and `GD-918`.
Current proof:
`artifacts/bench/metadata_coverage_audit_20260425_1724_dangerous_activation_metadata.md`.

`RAG-META3` added targeted wound-family metadata to `GD-232`, `GD-235`,
`GD-589`, and `GD-622`. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1732_wound_family_metadata.md`.

`RAG-META4` added targeted poisoning-family metadata to `GD-301` and `GD-898`.
Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1738_poisoning_metadata.md`.

`RAG-META5` added targeted stroke/cardiac metadata to `GD-601`. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1743_stroke_cardiac_metadata.md`.

`RAG-T11` separates frontmatter metadata gaps from
`missing_reviewed_answer_card` in the family-priority selector. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1648_reviewed_card_semantics.md`.
If a family shows `0/N` frontmatter/card gaps, do not continue metadata churn;
evaluate reviewed-card coverage or the deep-research `RAG-RUNTIME1` /
`RAG-TRUST1` backlog instead.

`RAG-RUNTIME1` resolved the current model-target vocabulary. Current decision:
`notes/RUNTIME_TARGET_NAMING_20260425.md`.

`RAG-TRUST1` drafted the high-risk answer policy. Current policy:
`notes/SENKU_TRUST_MODEL_HIGH_RISK_POLICY_20260425.md`.

`RAG-TRUST2` added the high-liability red-team/release handbook. Use it when
changing deterministic safety rules, reviewed-card runtime, answer-card
contracts, evidence diagnostics, or Android exposure for high-liability
families. Current handbook:
`notes/SENKU_HIGH_LIABILITY_RED_TEAM_HANDBOOK_20260425.md`.

`RAG-OPS1` drafted the RAG schema, vector-store, mobile-pack snapshot, release
gate, migration, and rollback ADR. Current contract:
`notes/RAG_SCHEMA_VECTOR_RELEASE_OPS_ADR_20260425.md`.

`RAG-PRIV1` added the privacy/export/delete/incident policy. Do not add notes,
sync, account-linked storage, or long-lived personal incident records as an
incidental RAG/runtime change; those features now require explicit delete,
export, artifact hygiene, and incident gates. Current policy:
`notes/SENKU_PRIVACY_EXPORT_DELETE_INCIDENT_POLICY_20260425.md`.

`RAG-REL1` added the release/update/migration runbook. Use it to record change
class, `reindex_required`, guide `version`/`last_updated` expectations, manifest
capture, migration rules, and rollback proof. Current runbook:
`notes/SENKU_RELEASE_UPDATE_MIGRATION_RUNBOOK_20260425.md`.

`RAG-PLAY1` added compound-scenario evaluation playbooks for multi-lane
stressors such as sick child plus outage, contaminated well plus food shortage,
evacuation with livestock, storm-damaged shelter recovery, and injury/cold/water
lane separation. Current playbooks:
`notes/SENKU_COMPOUND_SCENARIO_PLAYBOOKS_20260425.md`.

`RAG-CARD1` added a `GD-859` reviewed answer-card contract for dangerous
activation / acute mania crisis. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1650_dangerous_activation_card.md`.
The dangerous activation family now shows `0/2` frontmatter/card gaps; do not
add routine anxiety or PFA cards just to force this family to zero.

`RAG-CARD2` added a `GD-601` reviewed answer-card contract for acute coronary /
stroke overlap. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1656_acute_coronary_card.md`.
The stroke/cardiac overlap family now shows `0/0` frontmatter/card gaps and
`regression_monitor`; keep it out of additional card-count closure unless a
fresh behavior panel exposes a real miss.

`RAG-CARD3` added a `GD-301` reviewed answer-card contract for poisoning /
unknown toxic exposure support. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1700_poisoning_gd301_card.md`.
The unknown-child-ingestion / poisoning family now shows `0/0`
frontmatter/card gaps and `regression_monitor`; keep it out of further
card-count closure unless a fresh behavior panel exposes a real miss.

`RAG-CARD4` added reviewed support-card contracts for wound-family support
owners `GD-235` and `GD-622`. Current proof:
`artifacts/bench/high_liability_family_priorities_20260425_1720_wound_support_cards.md`.
The infected-wound / spreading-redness family now shows `0/0`
frontmatter/card gaps and `regression_monitor`; keep it out of further
card-count closure unless a fresh behavior panel exposes a real miss.
Fresh post-wound FA/FB/FD/FE runtime proof:
`artifacts/bench/rag_diagnostics_20260425_1714_post_wound_cards_runtime_panel/`
has `18` deterministic passes, `5` expected-supported reviewed-card runtime
rows, `1` accepted uncertain-fit row, `0` retrieval/ranking/generation/safety
misses, and `5` answer-card passes. Dangerous activation still reports `0/2`
frontmatter/card gaps only because `GD-858`/`GD-918` remain support surfaces;
the behavior panel does not justify adding those cards mechanically.
The dangerous activation card now lists `GD-858` and `GD-918` as
`boundary-support` source sections with invariants proving handoff to the
`GD-859` crisis lane; they remain non-owner/non-runtime-citation support guides.

`RAG-T12` fixed analyzer diagnostics for actual reviewed-card runtime rows so
they evaluate against artifact `reviewed_card_ids`, not every reviewed card in
the same expected guide family. Current proof:
`artifacts/bench/rag_diagnostics_20260425_1708_runtime_card_id_filter/`.
The post-card FA/FB/FD/FE panel now has `0` ranking misses, `0` generated
prompts, `5` reviewed-card runtime answers, `5` answer-card passes, and
`strong_supported:23|uncertain_fit_accepted:1`.

- Append a durable run manifest record for ignored/local artifacts:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\write_run_manifest.py --task RAG-T --lane tooling --role main --model gpt-5.5 --label example --command "..." --output artifacts\bench\example --metric rows=24 --validation "tests passed"
```

## Android Baseline

- Focused Android JVM tests are currently runnable from this shell. Emulator /
  connected instrumentation remains device-lane work and should be called out
  separately from JVM parser/repository tests.
- Android validation defaults to the fixed four-emulator posture matrix:
  - `5556` phone portrait
  - `5560` phone landscape
  - `5554` tablet portrait
  - `5558` tablet landscape
- Daily Android work is emulator-first; physical phone/tablet checks are milestone truth checks and mirror/review lanes.
- Latest broad visual baseline:
  - [`artifacts/external_review/ui_review_20260420_gallery_v6/index.html`](../artifacts/external_review/ui_review_20260420_gallery_v6/index.html)
  - CP9 RC v5 cut, `41/45` state-pack; four `generativeAskWithHostInferenceNavigatesToDetailScreen` limitations tracked as `R-gal1`.
- CP9 / RC v5 status from 2026-04-20:
  - Wave B actual contract: `20/20` under Option C scoring, confirmed in `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md`.
  - RC-blocking safety prompts `mania` and `poisoning` render escalation line plus Poison Control clause on all four serials.
  - `5560` landscape-phone body-render gap resolved via `R-ui2 v3`.
  - `rain_shelter` settles to `uncertain_fit` instead of `confident`; tracked as `R-ret1` post-RC.

## Subagent Routing

- Current budget posture as of 2026-04-25: use `gpt-5.5 low` for
  general-purpose scout and worker delegation after Spark budget is exhausted;
  reserve higher reasoning for delicate safety/predicate implementation or
  ambiguous ownership decisions.
- Codex-side main conductor: current session default unless the user requests otherwise.
- Fast read-only scout: `gpt-5.5 low` unless a cheaper separate-budget lane is explicitly available and requested.
- Heavier scout / worker: `gpt-5.5 high` only when the work is genuinely delicate.
- Main agent owns delegation; planner briefs slice-level prompts in [`notes/dispatch`](./dispatch), and main picks the lane per step.
- Async Qwen scout helpers:
  - [`scripts/start_qwen27_scout_job.ps1`](../scripts/start_qwen27_scout_job.ps1)
  - [`scripts/get_qwen27_scout_job.ps1`](../scripts/get_qwen27_scout_job.ps1)

## Practical Cautions

- Re-ingest after guide edits before trusting desktop retrieval results.
- Distinguish true knowledge gaps from retrieval-language issues before adding guides.
- For safety-critical guide edits, grep the repo for shared formulas, thresholds, dose-like statements, and other invariants before validation.
- For guide-family cleanup, prefer stronger routing blocks and reciprocal links before merging files.
- LiteRT guide-validation smoke works with `top_k=8`; desktop `top_k=24` is too large for the current 4096-token LiteRT context window.
- Prompt-pack validation and manual desktop queries should use the split LiteRT generation host plus a separate embedding-capable endpoint; do not silently fall back to the broad desktop generation lane.
- Desktop `RAG-S17` lets strict reviewed-card runtime plans answer before
  uncertain-fit copy when the opt-in runtime can produce a ready cited card
  answer. It improved EX/EY/EZ/FC answer-card pass from `8` to `10` and
  claim-support pass from `9` to `11`; the two promoted rows still carry
  `answer_mode=uncertain_fit` / `app_acceptance_status=uncertain_fit_accepted`
  because product-surface relabeling is intentionally deferred.
- Desktop `RAG-S18` aligns unknown-ingestion / poisoning retrieval with the
  reviewed `poisoning_unknown_ingestion` card. The strict predicate covers
  possible medicine ingestion, mouth burns after tasting a liquid, and
  unknown-food/acting-off wording while guarding medicine-allergy prompts.
  Matching prompts now use safety-triage retrieval, a targeted toxicology
  supplemental lane, owner-positive metadata rerank, and reviewed source-family
  citation for GD-301/GD-898. Fresh FA proof:
  `artifacts/bench/rag_diagnostics_20260425_1431_fa_poison_card_alignment/`;
  FA #4/#5/#6 are now strong reviewed-card runtime passes, and FA #3 has
  card/claim/evidence pass but remains a ranking-miss row because an adjacent
  non-owner still ranks above the expected owner.
- Desktop `RAG-S19` lets the reviewed `infected_wound_spreading_infection`
  card answer wound-boundary prompts when `GD-585` is retrieved but outside
  the top-two card-selection window. FB #6 now uses reviewed-card runtime,
  cites `GD-585`, and passes card/claim/evidence checks; owner concentration
  still shows rank-4 drift, so do not treat this as a retrieval-ranking fix.
- Desktop `RAG-S20` updates analyzer taxonomy for strict reviewed-card runtime
  rank drift. If a reviewed-card answer cites an expected owner and passes
  answer-card, claim-support, and evidence-nugget diagnostics, it is
  `expected_supported` even when the expected owner is not rank 1. Generated
  rank drift remains `ranking_miss`, and owner concentration metrics remain the
  retrieval-quality signal. Fresh FA/FB/FD/FE proof:
  `artifacts/bench/rag_diagnostics_20260425_1448_fa_fb_fd_fe_current_taxonomy/`;
  quality score is `8.83`, bad diagnostic buckets are `0`, owner best-rank is
  `1.36`, top-3 `21/24`, top-k `22/24`.
- Desktop `RAG-S21` extends the existing `mania_no_sleep_immediate_safety`
  deterministic rule to the FD dangerous-activation phrasings. FD now has no
  generation calls, the combined FA/FB/FD/FE panel reaches quality score
  `9.37`, and deterministic rows are `18/24`. Proof:
  `artifacts/bench/rag_diagnostics_20260425_1503_fa_fb_fd_fe_fd_deterministic/`.
  This is safety routing, not a retrieval-ranking or Android change.
- Desktop `RAG-S22` moves child/toddler unknown-pill ingestion away from the
  generic found-medication rule and into `unknown_child_ingestion`. With
  `SENKU_ENABLE_CARD_BACKED_RUNTIME_ANSWERS=1`, the combined FA/FB/FD/FE panel
  reaches quality score `9.56`, generated rows remain `0/24`, and the prior
  FA #1 `needs_evidence_owner` row is gone. Proof:
  `artifacts/bench/rag_diagnostics_20260425_1513_fa_child_pill_runtime_fd_deterministic/`.
- External deep-research backlog input from
  `C:\Users\tateb\Documents\prep\Books\deep-research-report.md` is captured at
  `notes/deep_research_report_backlog_20260425.md`. Main themes: canonical
  rendered corpus/partial expansion, high-liability metadata audit, runtime
  naming clarity, trust model, eval diagnostics, vector/release operations, and
  privacy/update docs.
- Desktop reviewed-card/provenance work does not automatically change Android
  behavior. Android needs explicit Kotlin/mobile-pack plumbing: `RAG-A1`
  landed display-only answer surface labels, `RAG-A2` landed optional
  answer-card pack tables, and `RAG-A3` landed the backward-compatible
  reader/model layer. Connected DAO instrumentation passed on the documented
  four-emulator Senku matrix. `RAG-A5` landed a disabled-by-default
  single-card `poisoning_unknown_ingestion` runtime pilot behind the hidden
  `reviewed_card_runtime_enabled` preference, with connected full
  `prepare(...)` runtime instrumentation passing on the same matrix. Android
  now has `RAG-A6` display-model inference for deterministic `answer_card:`
  rule IDs as reviewed-card evidence. `RAG-A7` extracted the Android
  planner/composer into `AnswerCardRuntime` and added
  `ReviewedCardRuntimeConfig` for the hidden preference. No exported-activity
  intent override is active for this runtime. `RAG-A8` added a visible
  developer-panel toggle for the hidden preference and captured matrix
  screenshots at `artifacts/android_reviewed_card_toggle_20260424_193049/`;
  normal product enablement remains deferred. A fresh real-pack probe at
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
  confirms exported answer-card tables contain `6` pilot cards, `116` clauses,
  and `19` sources. `RAG-A9` now proves the reviewed-card runtime through the
  normal scripted prompt path against that real pushed pack across the fixed
  four-emulator matrix. Trusted artifacts are under
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_475/`
  and
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_566/`.
  `RAG-A10` now carries explicit reviewed-card metadata through
  `ReviewedCardMetadata`, `AnswerCardRuntime`, `OfflineAnswerEngine`,
  `DetailActivity`, `AnswerContent`, and `DetailProofPresentationFormatter`.
  The trusted metadata/proof harness asserts card ID, card guide ID, review
  status, cited reviewed source guide ID, and proof-summary visibility under
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/`
  and
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_366/`.
  Same-block persistence follow-up records ready instant reviewed-card answers
  into recent threads and round-trips `ReviewedCardMetadata` through
  `SessionMemory` JSON; focused JVM coverage is in `SessionMemoryTest`.
  `RAG-A13` replaced the checked-in Android asset pack with the proven six-card
  probe pack. Bundled manifest generated at
  `2026-04-25T00:08:46.459832+00:00` reports `answer_cards=6`; SQLite counts
  are `6` answer cards, `116` clauses, and `19` sources, all
  `pilot_reviewed` with no missing clause/source links. Clean-install no-push
  matrix proof passed with APK SHA
  `be32ff34f66e7d478082ae2bd292f0e8315f231ec9f55c4852debfdd0e3cc553` under
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/`, using query
  `my child swallowed an unknown cleaner` and expected card
  `poisoning_unknown_ingestion` / guide `GD-898`. Fresh APK installs no longer
  need a manual probe-pack push for the six pilot-reviewed cards, but the
  runtime remains off by default and requires the developer/test flag.
  `RAG-CH1` extracted reviewed-card detail metadata intent transport, current
  detail state, and rule-id guarded lookup into
  `DetailReviewedCardMetadataBridge`; behavior is unchanged after compile/unit
  checks and the clean-install no-push matrix under
  `artifacts/android_reviewed_card_bridge_matrix_20260424/` with APK SHA
  `7dae8277227c8f5ddc2997c412203da1309addff6e76f7f278e1e0734b80464c`.
  `RAG-A14` dispatch now defines product enablement gates before
  reviewed-card runtime can move beyond developer/test exposure. Its first
  enforceable guard has landed: `ReviewedCardEvidence` requires a
  deterministic `answer_card:` rule ID, nonzero source count, present
  `ReviewedCardMetadata`, `reviewed_card_runtime` provenance, and non-empty
  cited reviewed source guide IDs; the prompt harness fails closed unless the
  expected reviewed-card identifiers and at least one source row are present;
  old packs with missing answer-card tables return null card planning instead
  of crashing or fabricating a card. Focused JVM/build, connected
  instrumentation, and prompt reinstall/no-pack-push proofs passed on APK SHA
  `ffe6f23038d20f488c73f5166649b4319b3413e37d7dfd759037f8ef7541eaca` under
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/20260424_213205_634/emulator-5556`
  and
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/clean_5554/20260424_213252_605/emulator-5554`.
  Do not call those two prompt proofs clean-install proofs: uninstall returned
  `DELETE_FAILED_INTERNAL_ERROR`, then the APK was reinstalled with
  `install -r`, with no mobile pack push. Runtime remains off by default, full
  product enablement remains open, and A14 does not expand cards.
  `RAG-A11a` then expanded runtime coverage to the second developer/test-gated
  card, `newborn_danger_sepsis` / `GD-284`, without product-enabling the
  runtime. The prompt matrix used the checked-in six-card asset pack with no
  pack push and passed across tablet portrait, phone portrait, tablet
  landscape, and phone landscape on APK SHA
  `60bb6751d18f04f13dff4a6441c65f2cd59f98195ca243f26e5e591830582b5c` under
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/`. It asserted
  `REVIEWED EVIDENCE`, `answer_card:newborn_danger_sepsis`, card guide
  `GD-284`, review status `pilot_reviewed`, cited source IDs
  `GD-284|GD-298|GD-492|GD-617`, recent-thread metadata, and newborn danger
  body fragments. Treat this as reinstall/no-pack-push proof, not strict clean
  uninstall proof.
  `RAG-A11b` expanded runtime coverage to the third developer/test-gated card,
  `choking_airway_obstruction` / `GD-232`, still without product-enabling the
  runtime. The prompt matrix used the checked-in six-card asset pack with no
  pack push and passed across tablet portrait, phone portrait, tablet
  landscape, and phone landscape on APK SHA
  `63126e427e21385f1e55e0451b81a32d768eb9ab63cc734b77efca723dd740af` under
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/`. It asserted
  `REVIEWED EVIDENCE`, `answer_card:choking_airway_obstruction`, card guide
  `GD-232`, review status `pilot_reviewed`, cited source IDs
  `GD-232|GD-284|GD-298|GD-617`, recent-thread metadata, and infant choking
  body fragments. Treat this as reinstall/no-pack-push proof, not strict clean
  uninstall proof.
  `RAG-A11c` expanded runtime coverage to the fourth developer/test-gated card,
  `meningitis_sepsis_child` / `GD-589`, still without product-enabling the
  runtime. The prompt matrix used the checked-in six-card asset pack with no
  pack push and passed across tablet portrait, phone portrait, tablet
  landscape, and phone landscape on APK SHA
  `8bee066f6ce3a988388cd60d767e70bb93c46bf8cf43a4329ddd7ff21171ddd4` under
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/`. It
  asserted `REVIEWED EVIDENCE`, `answer_card:meningitis_sepsis_child`, card
  guide `GD-589`, review status `pilot_reviewed`, cited source IDs
  `GD-589|GD-235|GD-268|GD-284|GD-298`, recent-thread metadata, and red-flag
  meningitis/sepsis body fragments. Treat this as reinstall/no-pack-push proof,
  not strict clean uninstall proof. Reviewed-card runtime now runs before the
  host/local model availability gate, so prompt proof works with
  `model_name: null`. Bare meningitis-vs-viral comparison prompts and
  public-health reporting/contact-tracing prompts must stay generated or
  compare/clarify, not strict emergency-card fallback.
  `RAG-A11d` expanded runtime coverage to the fifth developer/test-gated card,
  `infected_wound_spreading_infection` / `GD-585`, still without
  product-enabling the runtime. The prompt matrix used the checked-in six-card
  asset pack with no pack push and passed across tablet portrait, phone
  portrait, tablet landscape, and phone landscape on APK SHA
  `f1ae9a28e619069cbd8bc057070440e49948d30395e56d9fa727a2855c1de6d6` under
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/`. It
  asserted `REVIEWED EVIDENCE`,
  `answer_card:infected_wound_spreading_infection`, card guide `GD-585`,
  review status `pilot_reviewed`, cited source ID `GD-585`, recent-thread
  metadata, and spreading wound infection body fragments. Treat this as
  no-pack-push prompt proof, not strict clean uninstall proof. Routine wound
  care, generic cellulitis education, newborn cord infection, and
  meningitis/sepsis rash prompts must stay out of strict emergency-card
  fallback.
  `RAG-A11e` expanded runtime coverage to the sixth developer/test-gated card,
  `abdominal_internal_bleeding` / `GD-380`, completing the checked-in six-card
  pilot runtime pack without product-enabling the runtime. The prompt matrix
  used the checked-in six-card asset pack with no pack push and passed across
  tablet portrait, phone portrait, tablet landscape, and phone landscape on APK
  SHA `3bd27a2aad4504328428aa78ec2e5e91414eafa05b5dfd1c918505264e249f2c`
  under
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/`.
  It asserted `REVIEWED EVIDENCE`,
  `answer_card:abdominal_internal_bleeding`, card guide `GD-380`, review
  status `pilot_reviewed`, cited source IDs `GD-380|GD-232|GD-584`,
  recent-thread metadata, and abdominal trauma/internal bleeding body
  fragments. Treat this as no-pack-push prompt proof, not strict clean
  uninstall proof. Pregnancy/ectopic prompts, routine GI prompts, black stool
  without danger signs, and other emergency-card families must stay out of
  strict emergency-card fallback.
  `RAG-CH2` then extracted the repeated runtime planner descriptor/helper
  shape in `AnswerCardRuntime` with an in-file `CardSpec` registry and shared
  planner helper. Clinical predicates, marker sets, source construction,
  metadata, product gating, UI, and pack schema remain unchanged. Focused
  JVM/build checks passed, connected `OfflineAnswerEngineAnswerCardRuntimeTest`
  passed `8` tests per device across the fixed matrix, and a phone prompt
  canary passed on APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f` under
  `artifacts/android_reviewed_card_ch2_runtime_registry_harness_20260424/`.
  `RAG-A14a` added proof-only layout / pack readiness without changing product
  exposure. `AnswerCardAssetPackParityTest` now asserts shipped asset-pack
  answer-card tables, counts, expected card IDs, `pilot_reviewed` status, and
  clause/source coverage; it passed across the fixed matrix. Developer-panel
  collapsed/off/on screenshots and a four-posture poisoning reviewed-card
  prompt matrix live under
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/`, also on
  APK SHA `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`.
  `RAG-A14b` then harmonized reviewed-card proof-surface copy: the reviewed-card
  meta strip, body/card label, and proof summary now use reviewed-card-specific
  trust wording. Focused build/JVM checks and a four-posture poisoning prompt
  matrix passed under
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/` on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
  `RAG-A14c` then added prompt-harness forbidden-label assertions. Reviewed
  prompts can forbid `STRONG EVIDENCE`, non-reviewed prompts can forbid
  `REVIEWED EVIDENCE`, and the harness checks UIAutomator text,
  accessibility descriptions, and settled detail signals. PowerShell parse,
  Android test compilation, one runtime-on non-reviewed phone canary, one
  runtime-on reviewed inverse canary, and a runtime-off non-reviewed
  four-posture matrix passed under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/` on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
  `RAG-A14d` then closed the exposure-policy decision with Option A: keep
  reviewed-card runtime developer/test-only and default `off` for now. No
  product-default behavior, card expansion, top-level product UI,
  local-preview toggle, or runtime default change is approved from the current
  evidence set. Decision note:
  `notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`; draft
  background:
  `notes/ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DRAFT_20260424.md`.
  `RAG-CH3` then extracted androidTest-only scripted reviewed-card contract
  parsing and pure fail-closed validation into
  `ScriptedPromptHarnessContract`. `PromptHarnessSmokeTest` still owns
  UIAutomator waits, activity assertions, settled detail signals, reflection
  helpers, and artifact capture. Android test compile / test APK assembly plus
  phone reviewed and non-reviewed forbidden-label canaries passed under
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/`.
  `RAG-CH5` then added direct `ScriptedPromptHarnessContractTest`
  instrumentation coverage for decoded/pipe-delimited scripted arguments,
  legacy forbidden-label alias support, strict boolean parsing, and the
  fail-closed reviewed-evidence guard. This is test-only; runtime defaults,
  product exposure, prompt UI flow, pack assets, and card predicates are
  unchanged.
- `RAG-A4` extracted mobile-pack answer-card export helpers into
  `mobile_pack_answer_cards.py`; use that module for future pack-card export
  changes instead of growing `mobile_pack.py`.
- Android layout/UI is not finalized. During harness or screenshot work, record
  UI improvement suggestions separately from data-layer/RAG acceptance.
- Reviewed-card Android continuation backlog:
  [`ANDROID_REVIEWED_CARD_RUNTIME_BACKLOG_20260424.md`](./ANDROID_REVIEWED_CARD_RUNTIME_BACKLOG_20260424.md).
