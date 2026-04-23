# R-TRACK1 Hygiene Report (2026-04-22)

## 1. Summary

| Metric | Count |
| --- | ---: |
| Tracked | 96 |
| Ignored | 13 |
| Deleted | 0 |
| Deferred | 49 |
| TRACK-dispositioned `.py` | 60 |
| TRACK-dispositioned `scripts/*.ps1` | 9 |

| Category | Track | Ignore | Defer |
| --- | ---: | ---: | ---: |
| root .py | 10 | 0 | 0 |
| scripts/*.py | 22 | 0 | 2 |
| scripts support | 3 | 0 | 2 |
| scripts/*.ps1 | 9 | 0 | 28 |
| tests/*.py | 27 | 0 | 0 |
| root non-.py | 5 | 6 | 14 |
| directories | 0 | 7 | 3 |
| subproject | 3 | 0 | 0 |
| litert-host-jvm | 3 | 0 | 0 |
| tools/sidecar-viewer | 5 | 0 | 0 |
| uiplanning | 12 | 0 | 0 |

Rule 1 / hard-stop result: none. Rule 5a carry-over fired because `notes/specs/deterministic_registry_sidecar.yaml` remains untracked.

Must-track anchors staged for this landing:
- `metadata_validation.py` - present
- `config.py` - present
- `guide_catalog.py` - present
- `deterministic_special_case_registry.py` - present
- `special_case_builders.py` - present
- `lmstudio_utils.py` - present
- `token_estimation.py` - present
- `bench_artifact_tools.py` - present
- `confidence_label_contract.py` - present
- `summarize_latency.py` - present
- `scripts/export_mobile_pack.py` - present
- `scripts/validate_special_cases.py` - present
- `scripts/push_mobile_pack_to_android.ps1` - present
- `scripts/run_android_prompt.ps1` - present
- `scripts/run_android_search_log_only.ps1` - present
- `scripts/run_android_ui_validation_pack.ps1` - present
- `scripts/start_senku_emulator_matrix.ps1` - present
- `scripts/start_senku_device_mirrors.ps1` - present
- `scripts/run_guide_prompt_validation.ps1` - present
- `scripts/start_qwen27_scout_job.ps1` - present
- `scripts/get_qwen27_scout_job.ps1` - present
- `scripts/android_harness_common.psm1` - present
- `scripts/verify_local_runtime.sh` - present
- `scripts/rebuild_venv_if_needed.sh` - present
- `tests/test_delta_prompt_scripts.py` - present
- `tests/test_run_guide_prompt_validation_harness.py` - present
- `test_prompts.txt` - present
- `litert-host-jvm/build.gradle` - present
- `litert-host-jvm/settings.gradle` - present
- `litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java` - present
- `tools/sidecar-viewer/app.js` - present
- `tools/sidecar-viewer/server.py` - present
- `uiplanning/CURRENT_STATE.md` - present
- `uiplanning/DECISIONS.md` - present
- `GUIDE_PLAN.md` - present
- `README_OPEN_IN_CODEX.md` - present
- `MIGRATION_LLAMACPP.md` - present
- `requirements.txt` - present

## 2. Pre-commit hash manifest

Git-normalized blob hashes captured before `git add` for all 96 TRACK-dispositioned paths:
```text
67edf1e5d0c40b0ae466ec4a31601c808634758d GUIDE_PLAN.md
4ecc88b06a43372dc8167ef10a445ae63f5d5890 MIGRATION_LLAMACPP.md
370eed91ccb770bbfa0dde26eb38e5f661fea5a5 README_OPEN_IN_CODEX.md
0f136a625dc62980eb45160f2b518c38acabfbed bench_artifact_tools.py
f431704e9decdb4962fa5941cd81fc0daa822f6b confidence_label_contract.py
61a77edd912e994f5b64acbc1613c125888d1741 config.py
28a324e3d31c82331441a539ac621d8a04eff399 deterministic_special_case_registry.py
39b6b0e352fa84059a8531b1796f7aa9f13216ea guide_catalog.py
2ff3960851c3edc905dbb1ff8e79e88161d1c2f4 litert-host-jvm/build.gradle
cf3d351c8752fdfcc8c20a895930972bc49a5c47 litert-host-jvm/settings.gradle
30b1778ed44f62b6004dc33a401bc575f8b44343 litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java
74a4eb439d2370bf4f3f2f3584e10b1237419cd3 lmstudio_utils.py
054938a4b92365f6bfbfecf18fddf07d1a154f89 metadata_validation.py
0b448660687177546b25b0e9e9dcdfd3d0de7edd requirements.txt
ed207d2e545a65015d5e24cde035b30898a1050c scripts/android_harness_common.psm1
62ee05df4e147f7189dfeeade57e9dbb0b84aa00 scripts/audit_mobile_pack_metadata.py
e3dc92f6f1c86b4264b23a8d41e50370b7c0d5c2 scripts/build_carryforward_prompt_pack.py
1a889e813182e0c866745e0533e63122aa4e9c1e scripts/build_delta_prompt_pack.py
65011ae7b0f52019452fa860c7ff5fca5cecca00 scripts/build_guide_graph.py
26c5e5a3058b11e29245ff1c9d1da6d46eab4456 scripts/compare_bench_artifacts.py
a1a77c204d242bc4692210173a8b9a6f996fa591 scripts/export_eval_bundle.py
a90f6aaa73a137b45ab24dca97c3c4a78727cc6d scripts/export_mobile_pack.py
b317a8dcad2dbe6d78c5fdad937747bf87a31636 scripts/extract_guide_invariants.py
706abb85ff3a43a0b3540274b088df245a8812fb scripts/fastembed_openai_server.py
b4f6c925ad499efad38406741278734aef88c55b scripts/find_guide_audit_hotspots.py
078f62b1368c6ae2bf7ee29cbb4d94e04fe29907 scripts/find_invariant_conflict_candidates.py
3083c80ee547a0a0a5f4b47d132421637484c902 scripts/get_qwen27_scout_job.ps1
1fbe66868a2dd823f755a9bdb64eb21c7e7c4cb0 scripts/litert_native_openai_server.py
c5bdb5fd047ad26a9531ad1ccfc1b369ef80cdb6 scripts/merge_structured_prompt_packs.py
5c9b6af047ddbdb0468ef5aef80aad36715f3ae5 scripts/mobile_headless_parity.py
239805cfb85f7472ed2001a7b14a6473f3894145 scripts/package_dual_model_answers_markdown.py
d88fed513af47def4374de4b56a76a2b2b18f965 scripts/push_mobile_pack_to_android.ps1
47658d9c25f80d5c93bf0ee14eb3c473a418ae15 scripts/rebuild_venv_if_needed.sh
1d06f0278cf861355d2da628405143632e620c00 scripts/regenerate_deterministic_registry.py
1af83b9c9bb60f3241a8c940cfbfc9a324e2ce9b scripts/report_bench_watchlist.py
25aa794201a42b09d8c8f0a8cbfe1406bfabb52a scripts/run_android_prompt.ps1
1fb3bb23af29eb538c39aa2d0740d4baf68b8f18 scripts/run_android_search_log_only.ps1
36d319f80e98899ec12e6fc72440caa4d1f8415a scripts/run_android_ui_validation_pack.ps1
0d235abdc24b4ed027c1a1f616bcee86dfb9552f scripts/run_guide_prompt_validation.ps1
bfcf60738e63a6047533f7bad320bb40a964ee9e scripts/run_mobile_headless_answers.py
6f89a6469fa6084fa21c05473a20ae4284af88bd scripts/run_mobile_headless_preflight.py
5c0bb1fb1d9682992361a75bb8fd89a54ab578de scripts/start_qwen27_scout_job.ps1
a13f03b089357168bae406ec09f1830d0a5d0153 scripts/start_senku_device_mirrors.ps1
f89db3baec54f83511788232a7581e436978fcd3 scripts/start_senku_emulator_matrix.ps1
ac70c53ef884ea577e0940a7091dff2bed229b3a scripts/validate_agent_state.py
bc50744c427c0c5fa189dd1cc7d8e3c5c555aa18 scripts/validate_bench_retry_slots.py
34012a92437df24521098e39531ee8c82545758a scripts/validate_special_cases.py
800a0f04ac729dc8bedfcf1ecaed60f5fc6b3701 scripts/verify_local_runtime.sh
c203569fe09827cae50dae8a9a2599ea3b427624 special_case_builders.py
644b87be4b673e1fb49eefa687803ce616f90502 summarize_latency.py
0c674445915a13e43cc8cca5b0f1f89ea48caf9c test_prompts.txt
24f2953d1262cfafa5282aa979f421130dbea7da tests/test_anchor_prior.py
b757e1601a4c42e42bf155da84139231999b2e84 tests/test_bench.py
9628e0457013f0d5d4ff8168b7fd215fac3b8bd3 tests/test_bench_artifact_tools.py
090e39b5d344d9472a68aa416cf4d2e8b24f9b98 tests/test_bench_config.py
f6c19638b4d8f604a908eebdd0efa206c71a5426 tests/test_bench_prompt_loading.py
3e8bae0edc994157703f66a172717c1f34f83079 tests/test_bench_runtime.py
48bb038232b59eadbf4466bff8c3befc66b8dd32 tests/test_build_guide_graph.py
f6ba51e7f0a5d178844da835d2337e50fc21f05d tests/test_citation_validation.py
32c6bfcd6b2c0eff52729ce7d5ea6babd6e53c2c tests/test_confidence_label.py
8065484a61cab1b472aa5ba763a98ae723fb0f38 tests/test_delta_prompt_scripts.py
f28d6cc5ad5d56a761ffd598668245a91dc6cf72 tests/test_deterministic_near_miss.py
abcc197570ee2c4e4ce8ed8d9039be6dd81a26fc tests/test_extract_guide_invariants.py
26aa1956c0677f35471bf7450df678c57242ba02 tests/test_find_guide_audit_hotspots.py
3e5275fdc3f49137143df27e427606dcab2b3d4b tests/test_find_invariant_conflict_candidates.py
a3dba2c536f942ff6a1b0943faa634bb0479f3db tests/test_guide_catalog.py
37c84d6165778bafd75647013d487c53d492e5bb tests/test_litert_native_openai_server.py
b10cbe6b80da72bb195128d945681b0706b86b1c tests/test_lmstudio_utils.py
5d8579363ad1469cb2069ed07658e1f194ec4fb8 tests/test_metadata_rerank_delta.py
b432898b50ba7408e94556c78d80cfdd53885d6b tests/test_mobile_headless_parity.py
1289f17fc5bf0fe0b0389be8e7ec095eaa7d8457 tests/test_query_routing.py
4421615789a3620b46ef50b21b29dad1ed8fec0f tests/test_regenerate_deterministic_registry.py
1f2cf19e928261f14d2f664daa667eeb0d44c203 tests/test_registry_overlap.py
e6ba31e54fb4090f711e1e6981ed87497371ca0d tests/test_run_guide_prompt_validation_harness.py
8f2c543c833c8109ee2587a504976f1d7253f9bf tests/test_run_mobile_headless_preflight.py
ca572de64002e2d27dfc2f1e485ea3203538ac9b tests/test_runtime_profiles.py
56a99257715f9c44d9469268e94ae02d7d967bd2 tests/test_special_cases.py
dcbb9c66ef07e99a0641da6371986585006407b3 tests/test_summarize_latency.py
e81ceafe4362c012bd3675bdc5752a3f67d0e6a2 token_estimation.py
c73150d080e53c92c881d37275d19367b6fee2ae tools/sidecar-viewer/app.js
f8f1d66ed20496b6bb03a27a43244d97e0d24c09 tools/sidecar-viewer/contract.js
7a0f70c910dc59ab7c1e861c693b63080eec76de tools/sidecar-viewer/index.html
6cab573ce2b8064c3929d143ac1e28426135974f tools/sidecar-viewer/server.py
93db85f1636979e1ac636f47fa3cb31d71129a5e tools/sidecar-viewer/styles.css
d043a6b07ca32e191ecca918cdf8e1640d15d03f uiplanning/CURRENT_STATE.md
b13b7e347ff8f53b141198794372fb3b24f7aa83 uiplanning/DECISIONS.md
32a693a35e7b9db13967076aff1a3a4bfb890a57 uiplanning/IMPLEMENTATION_LOG_20260413.md
59d9279d7388a41dd8c7261129c309b6b1af434f uiplanning/SCREEN_SPECS.md
9d8752d1067be216445375e86fee28666657bbe6 uiplanning/SCREEN_SPECS_TABLET_ADDENDUM_20260413.md
fe0b708383aa094d80d741b436a0625e173b75b1 uiplanning/TABLET_LAYOUT_NOTES_20260413.md
08515b4e033fae06ae07b6b1fc18fa51caa4c152 uiplanning/Technical Architecture for Autonomous Multi-Model Engineering Swarms_ Orchestrating GPT-5.4, OpenCode GLM 5.1, Codex Spark 5.3, and Local Qwen 3.5 Scouts.md
555c27f99a5a600554574d669b98d70318f230a2 uiplanning/UI_CHUNK_PLAN_20260416.md
fc05a556e38b7458c66ec4c8866fb3653468b472 uiplanning/UI_TODO_20260414.md
27b47380463e56fe04c09660ff3e8d794980f22e uiplanning/UI_TODO_NEXT_20260414.md
5b3f6cc0ca73e74a1b70a0f1c293af305b26b5b9 uiplanning/UI_TODO_POST_20260415.md
96f50d30ae50415af9da2a5da2e86f492183d689 uiplanning/UI_VALIDATION_CHECKLIST_20260414.md
```

## 3. Post-stage hash verification

All 96 staged blob hashes matched the pre-commit manifest. This proves there was no post-capture content drift before commit.

Staged blob manifest:
```text
67edf1e5d0c40b0ae466ec4a31601c808634758d GUIDE_PLAN.md
4ecc88b06a43372dc8167ef10a445ae63f5d5890 MIGRATION_LLAMACPP.md
370eed91ccb770bbfa0dde26eb38e5f661fea5a5 README_OPEN_IN_CODEX.md
0f136a625dc62980eb45160f2b518c38acabfbed bench_artifact_tools.py
f431704e9decdb4962fa5941cd81fc0daa822f6b confidence_label_contract.py
61a77edd912e994f5b64acbc1613c125888d1741 config.py
28a324e3d31c82331441a539ac621d8a04eff399 deterministic_special_case_registry.py
39b6b0e352fa84059a8531b1796f7aa9f13216ea guide_catalog.py
2ff3960851c3edc905dbb1ff8e79e88161d1c2f4 litert-host-jvm/build.gradle
cf3d351c8752fdfcc8c20a895930972bc49a5c47 litert-host-jvm/settings.gradle
30b1778ed44f62b6004dc33a401bc575f8b44343 litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java
74a4eb439d2370bf4f3f2f3584e10b1237419cd3 lmstudio_utils.py
054938a4b92365f6bfbfecf18fddf07d1a154f89 metadata_validation.py
0b448660687177546b25b0e9e9dcdfd3d0de7edd requirements.txt
ed207d2e545a65015d5e24cde035b30898a1050c scripts/android_harness_common.psm1
62ee05df4e147f7189dfeeade57e9dbb0b84aa00 scripts/audit_mobile_pack_metadata.py
e3dc92f6f1c86b4264b23a8d41e50370b7c0d5c2 scripts/build_carryforward_prompt_pack.py
1a889e813182e0c866745e0533e63122aa4e9c1e scripts/build_delta_prompt_pack.py
65011ae7b0f52019452fa860c7ff5fca5cecca00 scripts/build_guide_graph.py
26c5e5a3058b11e29245ff1c9d1da6d46eab4456 scripts/compare_bench_artifacts.py
a1a77c204d242bc4692210173a8b9a6f996fa591 scripts/export_eval_bundle.py
a90f6aaa73a137b45ab24dca97c3c4a78727cc6d scripts/export_mobile_pack.py
b317a8dcad2dbe6d78c5fdad937747bf87a31636 scripts/extract_guide_invariants.py
706abb85ff3a43a0b3540274b088df245a8812fb scripts/fastembed_openai_server.py
b4f6c925ad499efad38406741278734aef88c55b scripts/find_guide_audit_hotspots.py
078f62b1368c6ae2bf7ee29cbb4d94e04fe29907 scripts/find_invariant_conflict_candidates.py
3083c80ee547a0a0a5f4b47d132421637484c902 scripts/get_qwen27_scout_job.ps1
1fbe66868a2dd823f755a9bdb64eb21c7e7c4cb0 scripts/litert_native_openai_server.py
c5bdb5fd047ad26a9531ad1ccfc1b369ef80cdb6 scripts/merge_structured_prompt_packs.py
5c9b6af047ddbdb0468ef5aef80aad36715f3ae5 scripts/mobile_headless_parity.py
239805cfb85f7472ed2001a7b14a6473f3894145 scripts/package_dual_model_answers_markdown.py
d88fed513af47def4374de4b56a76a2b2b18f965 scripts/push_mobile_pack_to_android.ps1
47658d9c25f80d5c93bf0ee14eb3c473a418ae15 scripts/rebuild_venv_if_needed.sh
1d06f0278cf861355d2da628405143632e620c00 scripts/regenerate_deterministic_registry.py
1af83b9c9bb60f3241a8c940cfbfc9a324e2ce9b scripts/report_bench_watchlist.py
25aa794201a42b09d8c8f0a8cbfe1406bfabb52a scripts/run_android_prompt.ps1
1fb3bb23af29eb538c39aa2d0740d4baf68b8f18 scripts/run_android_search_log_only.ps1
36d319f80e98899ec12e6fc72440caa4d1f8415a scripts/run_android_ui_validation_pack.ps1
0d235abdc24b4ed027c1a1f616bcee86dfb9552f scripts/run_guide_prompt_validation.ps1
bfcf60738e63a6047533f7bad320bb40a964ee9e scripts/run_mobile_headless_answers.py
6f89a6469fa6084fa21c05473a20ae4284af88bd scripts/run_mobile_headless_preflight.py
5c0bb1fb1d9682992361a75bb8fd89a54ab578de scripts/start_qwen27_scout_job.ps1
a13f03b089357168bae406ec09f1830d0a5d0153 scripts/start_senku_device_mirrors.ps1
f89db3baec54f83511788232a7581e436978fcd3 scripts/start_senku_emulator_matrix.ps1
ac70c53ef884ea577e0940a7091dff2bed229b3a scripts/validate_agent_state.py
bc50744c427c0c5fa189dd1cc7d8e3c5c555aa18 scripts/validate_bench_retry_slots.py
34012a92437df24521098e39531ee8c82545758a scripts/validate_special_cases.py
800a0f04ac729dc8bedfcf1ecaed60f5fc6b3701 scripts/verify_local_runtime.sh
c203569fe09827cae50dae8a9a2599ea3b427624 special_case_builders.py
644b87be4b673e1fb49eefa687803ce616f90502 summarize_latency.py
0c674445915a13e43cc8cca5b0f1f89ea48caf9c test_prompts.txt
24f2953d1262cfafa5282aa979f421130dbea7da tests/test_anchor_prior.py
b757e1601a4c42e42bf155da84139231999b2e84 tests/test_bench.py
9628e0457013f0d5d4ff8168b7fd215fac3b8bd3 tests/test_bench_artifact_tools.py
090e39b5d344d9472a68aa416cf4d2e8b24f9b98 tests/test_bench_config.py
f6c19638b4d8f604a908eebdd0efa206c71a5426 tests/test_bench_prompt_loading.py
3e8bae0edc994157703f66a172717c1f34f83079 tests/test_bench_runtime.py
48bb038232b59eadbf4466bff8c3befc66b8dd32 tests/test_build_guide_graph.py
f6ba51e7f0a5d178844da835d2337e50fc21f05d tests/test_citation_validation.py
32c6bfcd6b2c0eff52729ce7d5ea6babd6e53c2c tests/test_confidence_label.py
8065484a61cab1b472aa5ba763a98ae723fb0f38 tests/test_delta_prompt_scripts.py
f28d6cc5ad5d56a761ffd598668245a91dc6cf72 tests/test_deterministic_near_miss.py
abcc197570ee2c4e4ce8ed8d9039be6dd81a26fc tests/test_extract_guide_invariants.py
26aa1956c0677f35471bf7450df678c57242ba02 tests/test_find_guide_audit_hotspots.py
3e5275fdc3f49137143df27e427606dcab2b3d4b tests/test_find_invariant_conflict_candidates.py
a3dba2c536f942ff6a1b0943faa634bb0479f3db tests/test_guide_catalog.py
37c84d6165778bafd75647013d487c53d492e5bb tests/test_litert_native_openai_server.py
b10cbe6b80da72bb195128d945681b0706b86b1c tests/test_lmstudio_utils.py
5d8579363ad1469cb2069ed07658e1f194ec4fb8 tests/test_metadata_rerank_delta.py
b432898b50ba7408e94556c78d80cfdd53885d6b tests/test_mobile_headless_parity.py
1289f17fc5bf0fe0b0389be8e7ec095eaa7d8457 tests/test_query_routing.py
4421615789a3620b46ef50b21b29dad1ed8fec0f tests/test_regenerate_deterministic_registry.py
1f2cf19e928261f14d2f664daa667eeb0d44c203 tests/test_registry_overlap.py
e6ba31e54fb4090f711e1e6981ed87497371ca0d tests/test_run_guide_prompt_validation_harness.py
8f2c543c833c8109ee2587a504976f1d7253f9bf tests/test_run_mobile_headless_preflight.py
ca572de64002e2d27dfc2f1e485ea3203538ac9b tests/test_runtime_profiles.py
56a99257715f9c44d9469268e94ae02d7d967bd2 tests/test_special_cases.py
dcbb9c66ef07e99a0641da6371986585006407b3 tests/test_summarize_latency.py
e81ceafe4362c012bd3675bdc5752a3f67d0e6a2 token_estimation.py
c73150d080e53c92c881d37275d19367b6fee2ae tools/sidecar-viewer/app.js
f8f1d66ed20496b6bb03a27a43244d97e0d24c09 tools/sidecar-viewer/contract.js
7a0f70c910dc59ab7c1e861c693b63080eec76de tools/sidecar-viewer/index.html
6cab573ce2b8064c3929d143ac1e28426135974f tools/sidecar-viewer/server.py
93db85f1636979e1ac636f47fa3cb31d71129a5e tools/sidecar-viewer/styles.css
d043a6b07ca32e191ecca918cdf8e1640d15d03f uiplanning/CURRENT_STATE.md
b13b7e347ff8f53b141198794372fb3b24f7aa83 uiplanning/DECISIONS.md
32a693a35e7b9db13967076aff1a3a4bfb890a57 uiplanning/IMPLEMENTATION_LOG_20260413.md
59d9279d7388a41dd8c7261129c309b6b1af434f uiplanning/SCREEN_SPECS.md
9d8752d1067be216445375e86fee28666657bbe6 uiplanning/SCREEN_SPECS_TABLET_ADDENDUM_20260413.md
fe0b708383aa094d80d741b436a0625e173b75b1 uiplanning/TABLET_LAYOUT_NOTES_20260413.md
08515b4e033fae06ae07b6b1fc18fa51caa4c152 uiplanning/Technical Architecture for Autonomous Multi-Model Engineering Swarms_ Orchestrating GPT-5.4, OpenCode GLM 5.1, Codex Spark 5.3, and Local Qwen 3.5 Scouts.md
555c27f99a5a600554574d669b98d70318f230a2 uiplanning/UI_CHUNK_PLAN_20260416.md
fc05a556e38b7458c66ec4c8866fb3653468b472 uiplanning/UI_TODO_20260414.md
27b47380463e56fe04c09660ff3e8d794980f22e uiplanning/UI_TODO_NEXT_20260414.md
5b3f6cc0ca73e74a1b70a0f1c293af305b26b5b9 uiplanning/UI_TODO_POST_20260415.md
96f50d30ae50415af9da2a5da2e86f492183d689 uiplanning/UI_VALIDATION_CHECKLIST_20260414.md
```

`diff artifacts/R-track1_pre_commit_hashes_20260422.txt artifacts/R-track1_post_stage_hashes_20260422.txt` output:
```text
(empty)
```

## 4. Categories and decision rules applied

- Rule 2 tracked 8 AGENTS.md-named Python sources and operator utilities at root/scripts.
- Rule 2a tracked `test_prompts.txt` as the AGENTS-named test asset.
- Rule 2b tracked the 9 AGENTS-named `scripts/*.ps1` operator tools.
- Rule 2c/2d tracked `scripts/android_harness_common.psm1`, `scripts/rebuild_venv_if_needed.sh`, and `scripts/verify_local_runtime.sh`; `scripts/run_bench_guarded.sh` and `scripts/kill_bench_orphans.sh` stayed deferred because the closure pass found no tracked/TRACK-candidate referrers.
- Rule 3 closure promoted imported support modules and utilities including `metadata_validation.py`, `bench_artifact_tools.py`, `confidence_label_contract.py`, `summarize_latency.py`, `scripts/audit_mobile_pack_metadata.py`, `scripts/mobile_headless_parity.py`, `scripts/run_mobile_headless_answers.py`, and `scripts/run_mobile_headless_preflight.py`.
- Rule 4 tracked 27 tests: 8 via sibling-source Rule 4.a, 17 via import-based Rule 4.b, and 2 via explicit/string-path Rule 4.c anchors.
- Rule 4b tracked 7 script-side helpers with sibling tests (`build_guide_graph`, `extract_guide_invariants`, `find_guide_audit_hotspots`, `find_invariant_conflict_candidates`, `litert_native_openai_server`, `regenerate_deterministic_registry`, `export_mobile_pack`).
- Rule 17a tracked 12 CLI-shape `scripts/*.py` utilities after confirming live `__main__` entrypoints and extracting a one-line rationale from docstrings/descriptions.
- Rule 5a carry-over fired: `notes/specs/deterministic_registry_sidecar.yaml` remains untracked and is explicitly queued in backlog item (a).
- Rules 11 and 16 tracked the forward-looking root docs (`GUIDE_PLAN.md`, `README_OPEN_IN_CODEX.md`, `MIGRATION_LLAMACPP.md`) plus `requirements.txt`.
- Rules 9 and 10 ignored local-only state, scratch outputs, and runtime/cache directories without broad source globs.
- Rules 12-15 deferred dated snapshots, audit markdown, zip archives, and all six screenshots; the screenshot sweep found zero tracked-doc references for promotion.
- Rule 18 deferred only two orphan Python files: `scripts/check_mojibake.py` and `scripts/scan_encoding.py`.
- Out-of-scope partial-tracked trees stayed deferred for follow-up: `guides/`, `notes/handoffs/`, `notes/research/`, and `notes/reviews/`.

## 5. Per-item disposition

### root .py

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `bench_artifact_tools.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: scripts/compare_bench_artifacts.py, scripts/export_eval_bundle.py, scripts/package_dual_model_answers_markdown.py, tests/test_bench_artifact_tools.py |
| `confidence_label_contract.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: query.py, tests/test_confidence_label.py |
| `config.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |
| `deterministic_special_case_registry.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |
| `guide_catalog.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |
| `lmstudio_utils.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |
| `metadata_validation.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: ingest.py, mobile_pack.py, scripts/refresh_mobile_pack_metadata.py, scripts/run_mobile_headless_preflight.py |
| `special_case_builders.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |
| `summarize_latency.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: tests/test_summarize_latency.py |
| `token_estimation.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |

### scripts/*.py

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `scripts/audit_mobile_pack_metadata.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: scripts/run_mobile_headless_preflight.py |
| `scripts/build_carryforward_prompt_pack.py` | Rule 17a | TRACK | Build a structured prompt pack that preserves canonical Senku phrasings. |
| `scripts/build_delta_prompt_pack.py` | Rule 17a | TRACK | Convert the plain-text Senku delta regression pack into a structured prompt pack. |
| `scripts/build_guide_graph.py` | Rule 4b | TRACK | Sibling test on disk: tests/test_build_guide_graph.py |
| `scripts/check_mojibake.py` | Rule 18 | DEFER | No importers, no sibling test trigger, and no CLI shape after Rule 3 closure |
| `scripts/compare_bench_artifacts.py` | Rule 17a | TRACK | Compare two bench artifacts and report prompt-level deltas. |
| `scripts/export_eval_bundle.py` | Rule 17a | TRACK | Export bench artifacts into an evaluator-friendly JSONL or CSV bundle. |
| `scripts/export_mobile_pack.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |
| `scripts/extract_guide_invariants.py` | Rule 4b | TRACK | Sibling test on disk: tests/test_extract_guide_invariants.py |
| `scripts/fastembed_openai_server.py` | Rule 17a | TRACK | Serve FastEmbed text embeddings through a tiny OpenAI-compatible API. |
| `scripts/find_guide_audit_hotspots.py` | Rule 4b | TRACK | Sibling test on disk: tests/test_find_guide_audit_hotspots.py |
| `scripts/find_invariant_conflict_candidates.py` | Rule 4b | TRACK | Sibling test on disk: tests/test_find_invariant_conflict_candidates.py |
| `scripts/litert_native_openai_server.py` | Rule 4b | TRACK | Sibling test on disk: tests/test_litert_native_openai_server.py |
| `scripts/merge_structured_prompt_packs.py` | Rule 17a | TRACK | Merge two structured prompt packs into one combined master pack. |
| `scripts/mobile_headless_parity.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: scripts/run_mobile_headless_answers.py, tests/test_mobile_headless_parity.py |
| `scripts/package_dual_model_answers_markdown.py` | Rule 17a | TRACK | Package two model answer bundles into one markdown review file. |
| `scripts/regenerate_deterministic_registry.py` | Rule 4b | TRACK | Sibling test on disk: tests/test_regenerate_deterministic_registry.py |
| `scripts/report_bench_watchlist.py` | Rule 17a | TRACK | Summarize high-token and citation-heavy prompts from bench JSON artifacts. |
| `scripts/run_mobile_headless_answers.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: tests/test_mobile_headless_parity.py |
| `scripts/run_mobile_headless_preflight.py` | Rule 3 | TRACK | Imported by tracked/about-to-track code: tests/test_run_mobile_headless_preflight.py |
| `scripts/scan_encoding.py` | Rule 18 | DEFER | No importers, no sibling test trigger, and no CLI shape after Rule 3 closure |
| `scripts/validate_agent_state.py` | Rule 17a | TRACK | Validate the structured agent state file used for long-running Android work. |
| `scripts/validate_bench_retry_slots.py` | Rule 17a | TRACK | Validate that bench retry isolation works per worker slot, not per URL. |
| `scripts/validate_special_cases.py` | Rule 2 | TRACK | AGENTS.md-named source entry point/support file |

### scripts support

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `scripts/android_harness_common.psm1` | Rule 2c | TRACK | Referenced by tracked/TRACK-candidate PowerShell scripts |
| `scripts/kill_bench_orphans.sh` | Rule 19 | DEFER | No tracked/TRACK-candidate reference after support-file closure |
| `scripts/rebuild_venv_if_needed.sh` | Rule 2d | TRACK | Referenced from tracked docs or tracked/TRACK-candidate scripts |
| `scripts/run_bench_guarded.sh` | Rule 19 | DEFER | No tracked/TRACK-candidate reference after support-file closure |
| `scripts/verify_local_runtime.sh` | Rule 2d | TRACK | Referenced from tracked docs or tracked/TRACK-candidate scripts |

### scripts/*.ps1

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `scripts/android_fts5_probe.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/cleanup_android_harness_artifacts.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/get_qwen27_scout_job.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/invoke_qwen27_scout.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/invoke_qwen_scout.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/launch_debug_detail_state.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/post_rebuild_sanity_check.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/push_litert_model_to_android.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/push_mobile_pack_to_android.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/run_abstain_regression_panel.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_detail_followup.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_detail_followup_logged.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_detail_followup_matrix.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_followup_matrix.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_followup_suite.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_gap_pack.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_harness_matrix.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_prompt.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/run_android_prompt_batch.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_prompt_logged.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_search_log_only.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/run_android_session_batch.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_android_ui_validation_pack.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/run_e2b_e4b_diff.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/run_guide_prompt_validation.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/run_qwen27_scout_job_worker.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/start_android_detail_followup_lane.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/start_fastembed_server.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/start_litert_host_server.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/start_overnight_continuation.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/start_qwen27_scout_job.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/start_senku_device_mirrors.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/start_senku_emulator_matrix.ps1` | Rule 2b | TRACK | AGENTS.md-named PowerShell operator script |
| `scripts/stop_android_device_processes_safe.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/stop_android_harness_orphans.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/stop_android_harness_runs.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |
| `scripts/validate_mobile_pack_deterministic_parity.ps1` | Carry-over (j) | DEFER | Non-AGENTS PowerShell script deferred to follow-up slice |

### tests/*.py

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `tests/test_anchor_prior.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: query.py |
| `tests/test_bench.py` | Rule 4.a | TRACK | Sibling source exists on disk for bench |
| `tests/test_bench_artifact_tools.py` | Rule 4.a | TRACK | Sibling source exists on disk for bench_artifact_tools |
| `tests/test_bench_config.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: bench.py, config.py |
| `tests/test_bench_prompt_loading.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: bench.py |
| `tests/test_bench_runtime.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: bench.py |
| `tests/test_build_guide_graph.py` | Rule 4.a | TRACK | Sibling source exists on disk for build_guide_graph |
| `tests/test_citation_validation.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: deterministic_special_case_registry.py, guide_catalog.py, query.py |
| `tests/test_confidence_label.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: confidence_label_contract.py |
| `tests/test_delta_prompt_scripts.py` | Rule 4.c | TRACK | Explicit Path-construction override anchor |
| `tests/test_deterministic_near_miss.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: query.py |
| `tests/test_extract_guide_invariants.py` | Rule 4.a | TRACK | Sibling source exists on disk for extract_guide_invariants |
| `tests/test_find_guide_audit_hotspots.py` | Rule 4.a | TRACK | Sibling source exists on disk for find_guide_audit_hotspots |
| `tests/test_find_invariant_conflict_candidates.py` | Rule 4.a | TRACK | Sibling source exists on disk for find_invariant_conflict_candidates |
| `tests/test_guide_catalog.py` | Rule 4.a | TRACK | Sibling source exists on disk for guide_catalog |
| `tests/test_litert_native_openai_server.py` | Rule 4.a | TRACK | Sibling source exists on disk for litert_native_openai_server |
| `tests/test_lmstudio_utils.py` | Rule 4.a | TRACK | Sibling source exists on disk for lmstudio_utils |
| `tests/test_metadata_rerank_delta.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: query.py |
| `tests/test_mobile_headless_parity.py` | Rule 4.a | TRACK | Sibling source exists on disk for mobile_headless_parity |
| `tests/test_query_routing.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: query.py |
| `tests/test_regenerate_deterministic_registry.py` | Rule 4.a | TRACK | Sibling source exists on disk for regenerate_deterministic_registry |
| `tests/test_registry_overlap.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: query.py |
| `tests/test_run_guide_prompt_validation_harness.py` | Rule 4.c | TRACK | Explicit Path-construction override anchor |
| `tests/test_run_mobile_headless_preflight.py` | Rule 4.a | TRACK | Sibling source exists on disk for run_mobile_headless_preflight |
| `tests/test_runtime_profiles.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: bench.py, config.py, query.py |
| `tests/test_special_cases.py` | Rule 4.b | TRACK | Imports first-party tracked/candidate modules: deterministic_special_case_registry.py, guide_catalog.py, query.py |
| `tests/test_summarize_latency.py` | Rule 4.a | TRACK | Sibling source exists on disk for summarize_latency |

### root non-.py

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `.codex_rgal1_flake_dir.txt` | Rule 9 | IGNORE | Local-only state |
| `.codex_stage2_rerun2_root.txt` | Rule 9 | IGNORE | Local-only state |
| `.mcp.json` | Rule 9 | IGNORE | Local-only state |
| `4-13guidearchive.zip` | Rule 14 | DEFER | Zip archive triage deferred |
| `CLAUDE.md` | Rule 9 | IGNORE | Local-only state |
| `CURRENT_LOCAL_TESTING_STATE_20260410.md` | Rule 12 | DEFER | Dated snapshot in filename |
| `GUIDE_PLAN.md` | Rule 11 | TRACK | Forward-looking project documentation |
| `LM_STUDIO_MODELS_20260410.json` | Rule 12 | DEFER | Dated snapshot in filename |
| `MIGRATION_LLAMACPP.md` | Rule 11 | TRACK | Forward-looking project documentation |
| `README_OPEN_IN_CODEX.md` | Rule 11 | TRACK | Forward-looking project documentation |
| `UI_DIRECTION_AUDIT_20260414.md` | Rule 12 | DEFER | Dated snapshot in filename |
| `auditglm.md` | Rule 13 | DEFER | Audit markdown of unclear provenance |
| `gptaudit4-21.md` | Rule 13 | DEFER | Audit markdown of unclear provenance |
| `guides.zip` | Rule 14 | DEFER | Zip archive triage deferred |
| `requirements.txt` | Rule 16 | TRACK | Shared project config |
| `senku_answer_detail_1775908579084.png` | Rule 15 | DEFER | Screenshot deferred pending tracked-doc reference + visual review |
| `senku_first_launch_1775908603459.png` | Rule 15 | DEFER | Screenshot deferred pending tracked-doc reference + visual review |
| `senku_home_screen_1775908565198.png` | Rule 15 | DEFER | Screenshot deferred pending tracked-doc reference + visual review |
| `senku_mobile_mockups.md` | Rule 13 | DEFER | Audit markdown of unclear provenance |
| `senku_model_loaded_1775908948158.png` | Rule 15 | DEFER | Screenshot deferred pending tracked-doc reference + visual review |
| `senku_model_not_loaded_1775908960322.png` | Rule 15 | DEFER | Screenshot deferred pending tracked-doc reference + visual review |
| `senku_search_results_1775908590965.png` | Rule 15 | DEFER | Screenshot deferred pending tracked-doc reference + visual review |
| `test_prompts.txt` | Rule 2a | TRACK | AGENTS-named testing asset |
| `test_startprocess_err.txt` | Rule 10 | IGNORE | Scratch process output |
| `test_startprocess_out.txt` | Rule 10 | IGNORE | Scratch process output |

### directories

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `.serena/` | Rule 9 | IGNORE | Local-only state |
| `android-app/.kotlin/` | Rule 7 | IGNORE | Android build/runtime cache |
| `archive/` | Rule 8 | IGNORE | Archive directory |
| `artifacts/` | Rule 8 | IGNORE | Generated artifacts |
| `chroma_db/` | Rule 7 | IGNORE | Local vector database state |
| `db/` | Rule 7 | IGNORE | Local database runtime state |
| `models/` | Rule 7 | IGNORE | Local model binaries/state |
| `notes/handoffs/` | Scope | DEFER | Out of scope: notes/ partial-tracked content tree follow-up |
| `notes/research/` | Scope | DEFER | Out of scope: notes/ partial-tracked content tree follow-up |
| `notes/reviews/` | Scope | DEFER | Out of scope: notes/ partial-tracked content tree follow-up |

### subproject

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `litert-host-jvm/` | Rule 6 | TRACK | Track source subtree; ignore build/cache dirs separately |
| `tools/` | Rule 6 | TRACK | Track tools/sidecar-viewer source files |
| `uiplanning/` | Rule 6a | TRACK | Track planning docs subdirectory |

### litert-host-jvm

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `litert-host-jvm/build.gradle` | Rule 6 | TRACK | Source-like subproject file |
| `litert-host-jvm/settings.gradle` | Rule 6 | TRACK | Source-like subproject file |
| `litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java` | Rule 6 | TRACK | Source-like subproject file |

### tools/sidecar-viewer

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `tools/sidecar-viewer/app.js` | Rule 6 | TRACK | Source-like sidecar viewer file |
| `tools/sidecar-viewer/contract.js` | Rule 6 | TRACK | Source-like sidecar viewer file |
| `tools/sidecar-viewer/index.html` | Rule 6 | TRACK | Source-like sidecar viewer file |
| `tools/sidecar-viewer/server.py` | Rule 6 | TRACK | Source-like sidecar viewer file |
| `tools/sidecar-viewer/styles.css` | Rule 6 | TRACK | Source-like sidecar viewer file |

### uiplanning

| Path | Rule | Decision | Reason |
| --- | --- | --- | --- |
| `uiplanning/CURRENT_STATE.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/DECISIONS.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/IMPLEMENTATION_LOG_20260413.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/SCREEN_SPECS.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/SCREEN_SPECS_TABLET_ADDENDUM_20260413.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/TABLET_LAYOUT_NOTES_20260413.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/Technical Architecture for Autonomous Multi-Model Engineering Swarms_ Orchestrating GPT-5.4, OpenCode GLM 5.1, Codex Spark 5.3, and Local Qwen 3.5 Scouts.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/UI_CHUNK_PLAN_20260416.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/UI_TODO_20260414.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/UI_TODO_NEXT_20260414.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/UI_TODO_POST_20260415.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |
| `uiplanning/UI_VALIDATION_CHECKLIST_20260414.md` | Rule 6a | TRACK | Planning doc in tracked subdirectory |

## 6. Secret-scan outcome

Applied pattern set: authorization-shaped tokens, PEM/private keys, high-entropy base64 (`.py` only), machine-specific absolute paths, non-LAN FQDN HTTP(S) URLs, emails, and UNC/network-share paths.

No Rule 1 hard-stop findings surfaced in any TRACK, IGNORE, or DEFER candidate reviewed in-scope.
Surface-only findings recorded:
- `.mcp.json`: machine_path (C:\\Users\\tateb)
- `CURRENT_LOCAL_TESTING_STATE_20260410.md`: machine_path (/Users/tbronson)
- `README_OPEN_IN_CODEX.md`: machine_path (/Users/tbronson), machine_path (/Users/tbronson)

Import-smoke notes:
- Root .py import-smoke: PASS (10/10)
- scripts/*.py import-smoke: PASS (22/22)
- scripts/*.ps1 + *.psm1 parse-check: PASS via powershell.exe AST parser (10/10)
- tests/*.py real-import smoke: PASS (27/27)
- Import-smoke skip list: empty

## 7. Deferred items

### Rule 12 - Dated snapshots
- `CURRENT_LOCAL_TESTING_STATE_20260410.md` - keep/archive/delete decision pending.
- `LM_STUDIO_MODELS_20260410.json` - keep/archive/delete decision pending.
- `UI_DIRECTION_AUDIT_20260414.md` - keep/archive/delete decision pending.
### Rule 13 - Audit markdown
- `auditglm.md` - provenance/retention decision pending.
- `gptaudit4-21.md` - provenance/retention decision pending.
- `senku_mobile_mockups.md` - provenance/retention decision pending.
### Rule 14 - Zip archives
- `4-13guidearchive.zip` - delete candidate only after Tate review.
- `guides.zip` - delete candidate only after Tate review.

`4-13guidearchive.zip` preview:
```text
     39195  2026-04-10 09:51  guides/abrasives-manufacturing.md
     20074  2026-04-13 13:48  guides/accessible-shelter-design.md
     83987  2026-04-10 09:51  guides/acetylene-carbide-production.md
     20501  2026-04-10 09:51  guides/acoustics-sound-amplification.md
     25806  2026-04-10 09:51  guides/acute-abdominal-emergencies.md
     26256  2026-04-10 09:51  guides/acute-coronary-cardiac-emergencies.md
     42207  2026-04-10 09:51  guides/acute-radiation-syndrome.md
     19759  2026-04-10 09:51  guides/addiction-withdrawal-management.md
     13880  2026-04-10 09:51  guides/adhesives-binders-formulation.md
     24389  2026-04-13 10:57  guides/adolescent-health-development.md
      7004  2026-04-10 09:51  guides/adult-seizure-status-epilepticus-field-protocol.md
     51435  2026-04-10 09:51  guides/advanced-materials.md
     18889  2026-04-10 09:51  guides/advanced-tool-specialization.md
     25973  2026-04-10 09:51  guides/age-appropriate-skill-progressions.md
     27959  2026-04-13 10:11  guides/age-related-disease-management.md
     22712  2026-04-13 10:52  guides/agricultural-weather-planning.md
    124888  2026-04-10 09:51  guides/agriculture.md
     19149  2026-04-10 09:51  guides/agroforestry-silvopasture.md
     47158  2026-04-10 09:51  guides/alkali-production.md
     18981  2026-04-13 09:58  guides/allergic-reactions-anaphylaxis.md
     48316  2026-04-10 09:51  guides/alloy-decision-tree.md
     18836  2026-04-10 09:51  guides/alloy-embrittlement-failure.md
     57219  2026-04-10 09:51  guides/ammonia-synthesis-simplified.md
     23760  2026-04-10 09:51  guides/anatomy-basics-body-systems.md
```

`guides.zip` preview:
```text
         0  2026-04-10 09:51  guides/
     39195  2026-04-10 09:51  guides/abrasives-manufacturing.md
     19398  2026-04-10 09:51  guides/accessible-shelter-design.md
     83987  2026-04-10 09:51  guides/acetylene-carbide-production.md
     20501  2026-04-10 09:51  guides/acoustics-sound-amplification.md
     25806  2026-04-10 09:51  guides/acute-abdominal-emergencies.md
     26256  2026-04-10 09:51  guides/acute-coronary-cardiac-emergencies.md
     42207  2026-04-10 09:51  guides/acute-radiation-syndrome.md
     19759  2026-04-10 09:51  guides/addiction-withdrawal-management.md
     13880  2026-04-10 09:51  guides/adhesives-binders-formulation.md
     23263  2026-04-10 09:51  guides/adolescent-health-development.md
      7004  2026-04-10 09:51  guides/adult-seizure-status-epilepticus-field-protocol.md
     51435  2026-04-10 09:51  guides/advanced-materials.md
     18889  2026-04-10 09:51  guides/advanced-tool-specialization.md
     25973  2026-04-10 09:51  guides/age-appropriate-skill-progressions.md
     27913  2026-04-10 09:51  guides/age-related-disease-management.md
     22679  2026-04-10 09:51  guides/agricultural-weather-planning.md
    124888  2026-04-10 09:51  guides/agriculture.md
     19149  2026-04-10 09:51  guides/agroforestry-silvopasture.md
     47158  2026-04-10 09:51  guides/alkali-production.md
     18953  2026-04-10 09:51  guides/allergic-reactions-anaphylaxis.md
     48316  2026-04-10 09:51  guides/alloy-decision-tree.md
     18836  2026-04-10 09:51  guides/alloy-embrittlement-failure.md
     57219  2026-04-10 09:51  guides/ammonia-synthesis-simplified.md
```
### Rule 15 - Screenshots
- `senku_answer_detail_1775908579084.png` - zero tracked-doc refs; visual review still required before any promotion.
- `senku_first_launch_1775908603459.png` - zero tracked-doc refs; visual review still required before any promotion.
- `senku_home_screen_1775908565198.png` - zero tracked-doc refs; visual review still required before any promotion.
- `senku_model_loaded_1775908948158.png` - zero tracked-doc refs; visual review still required before any promotion.
- `senku_model_not_loaded_1775908960322.png` - zero tracked-doc refs; visual review still required before any promotion.
- `senku_search_results_1775908590965.png` - zero tracked-doc refs; visual review still required before any promotion.
### Rule 18 - Orphan Python files
- `scripts/check_mojibake.py` - no importers, no sibling test, and no CLI shape after Rule 3 closure. Unblock only if a real importer/test/entrypoint role is established.
- `scripts/scan_encoding.py` - no importers, no sibling test, and no CLI shape after Rule 3 closure. Unblock only if a real importer/test/entrypoint role is established.
### Rule 19 - Support files without in-scope referrers
- `scripts/run_bench_guarded.sh` - no tracked/TRACK-candidate docs/scripts referenced it in this pass.
- `scripts/kill_bench_orphans.sh` - deferred with `run_bench_guarded.sh`; only promote if an in-scope tracked referrer lands.
### Scope-deferred trees
- `guides/` - dedicated content-tracking slice required.
- `notes/handoffs/` - out of scope for R-track1; roll into the next notes-content slice.
- `notes/research/` - out of scope for R-track1; roll into the next notes-content slice.
- `notes/reviews/` - out of scope for R-track1; roll into the next notes-content slice.
### Follow-up PowerShell sweep
- `scripts/android_fts5_probe.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/cleanup_android_harness_artifacts.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/invoke_qwen27_scout.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/invoke_qwen_scout.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/launch_debug_detail_state.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/post_rebuild_sanity_check.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/push_litert_model_to_android.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_abstain_regression_panel.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_detail_followup.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_detail_followup_logged.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_detail_followup_matrix.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_followup_matrix.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_followup_suite.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_gap_pack.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_harness_matrix.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_prompt_batch.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_prompt_logged.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_android_session_batch.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_e2b_e4b_diff.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/run_qwen27_scout_job_worker.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/start_android_detail_followup_lane.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/start_fastembed_server.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/start_litert_host_server.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/start_overnight_continuation.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/stop_android_device_processes_safe.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/stop_android_harness_orphans.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/stop_android_harness_runs.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.
- `scripts/validate_mobile_pack_deterministic_parity.ps1` - rerun Rule-2b secret-scan/HARD-STOP policy first, then evaluate cross-references in the dedicated follow-up slice.

## 8. Carry-over to backlog

- (a) Sidecar YAML tracking for `notes/specs/deterministic_registry_sidecar.yaml`.
- (b) `guides/` content-tracking slice.
- (c) `notes/` content-tracking slice, including nested `handoffs/`, `research/`, and `reviews/` trees.
- (d) Zip archive triage for `4-13guidearchive.zip` and `guides.zip`.
- (e) Screenshot visual-content review for the six repo-root `senku_*.png` files.
- (f) Dated snapshot triage for `CURRENT_LOCAL_TESTING_STATE_20260410.md`, `LM_STUDIO_MODELS_20260410.json`, and `UI_DIRECTION_AUDIT_20260414.md`.
- (g) Audit markdown triage for `auditglm.md`, `gptaudit4-21.md`, and `senku_mobile_mockups.md`.
- (h) Rule 18 orphan `.py` follow-up for `scripts/check_mojibake.py` and `scripts/scan_encoding.py`.
- (j) Non-AGENTS `scripts/*.ps1` follow-up for 28 deferred files.
