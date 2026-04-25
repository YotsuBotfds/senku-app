# Android Lane Index

Use this as the first stop for Android parity and mobile-pack work.

## Live tracked anchors

- [`../android-app/README.md`](../android-app/README.md): current APK build/install path
- [`../TESTING_METHODOLOGY.md`](../TESTING_METHODOLOGY.md): desktop/mobile validation workflow
- [`CP9_ACTIVE_QUEUE.md`](./CP9_ACTIVE_QUEUE.md): live planner queue and post-RC backlog status
- [`../scripts/start_senku_emulator_matrix.ps1`](../scripts/start_senku_emulator_matrix.ps1): launch the fixed four-emulator matrix with read-only as the default mode
- [`../scripts/export_mobile_pack.py`](../scripts/export_mobile_pack.py): mobile pack export entry point
- [`../scripts/push_mobile_pack_to_android.ps1`](../scripts/push_mobile_pack_to_android.ps1): fast pack hot-swap path
- [`../scripts/run_android_instrumented_ui_smoke.ps1`](../scripts/run_android_instrumented_ui_smoke.ps1): instrumentation-backed smoke lane with screenshots, dumps, logcat, and `summary.json`
- [`../scripts/run_android_prompt.ps1`](../scripts/run_android_prompt.ps1): prompt automation harness
- [`../scripts/run_android_search_log_only.ps1`](../scripts/run_android_search_log_only.ps1): preferred long retrieval harness
- [`../scripts/run_android_ui_validation_pack.ps1`](../scripts/run_android_ui_validation_pack.ps1): deterministic + generative UI smoke pack
- [`../scripts/build_android_ui_state_pack_parallel.ps1`](../scripts/build_android_ui_state_pack_parallel.ps1): parallel four-lane screenshot/dump sweep across the fixed emulator matrix
- [`ANDROID_RAG_CONTRACT_TRANSLATION_20260424.md`](./ANDROID_RAG_CONTRACT_TRANSLATION_20260424.md): desktop reviewed-card/provenance contract translated into Android work
- [`ANDROID_REVIEWED_CARD_RUNTIME_BACKLOG_20260424.md`](./ANDROID_REVIEWED_CARD_RUNTIME_BACKLOG_20260424.md): current Android reviewed-card runtime backlog after `RAG-A1` through `RAG-A11e`, `RAG-A14a` through `RAG-A14d`, `RAG-CH1` through `RAG-CH3`

## Current artifact baseline

- Current broad visual baseline: `artifacts/external_review/ui_review_20260421_retrieval_chain_closed/`
- Wave B actual contract reference: `artifacts/cp9_stage2_rerun4_20260420_143440/summary.md`
- State-pack reference paired with the republished gallery: `artifacts/cp9_stage2_rerun4_5_retry_v2_20260420_171857/summary.md`
- Reviewed-card real-pack probe:
  `artifacts/mobile_pack/senku_20260424_answer_cards_probe_20260424_190810/`
  (`6` cards, `116` clauses, `19` sources)
- Reviewed-card checked-in asset-pack parity proof:
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5554/20260424_210714_546/emulator-5554`,
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5556/20260424_210714_546/emulator-5556`,
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5558/20260424_210714_555/emulator-5558`,
  and
  `artifacts/android_reviewed_card_asset_pack_harness_20260424/clean_matrix_5560/20260424_210714_546/emulator-5560`
  (clean-install no-push proof with APK SHA
  `be32ff34f66e7d478082ae2bd292f0e8315f231ec9f55c4852debfdd0e3cc553`)
- Reviewed-card detail metadata bridge proof:
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5554/20260424_212147_831/emulator-5554`,
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5556/20260424_212147_832/emulator-5556`,
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5558/20260424_212147_831/emulator-5558`,
  and
  `artifacts/android_reviewed_card_bridge_matrix_20260424/clean_matrix_5560/20260424_212147_828/emulator-5560`
  (clean-install no-push proof with APK SHA
  `7dae8277227c8f5ddc2997c412203da1309addff6e76f7f278e1e0734b80464c`)
- Reviewed-card A14 first guard proof:
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/20260424_213205_634/emulator-5556`
  and
  `artifacts/android_reviewed_card_a14_guard_harness_20260424/clean_5554/20260424_213252_605/emulator-5554`
  (reinstall/no-pack-push prompt proofs with APK SHA
  `ffe6f23038d20f488c73f5166649b4319b3413e37d7dfd759037f8ef7541eaca`;
  uninstall returned `DELETE_FAILED_INTERNAL_ERROR`, so do not call these
  clean-install proofs)
- Reviewed-card A11a newborn runtime proof:
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5554/20260424_214842_111/emulator-5554`,
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5556/20260424_214842_109/emulator-5556`,
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5558_landscape/20260424_214914_616/emulator-5558`,
  and
  `artifacts/android_reviewed_card_a11a_newborn_matrix_20260424/clean_matrix_5560_landscape/20260424_214914_616/emulator-5560`
  (reinstall/no-pack-push prompt matrix with APK SHA
  `60bb6751d18f04f13dff4a6441c65f2cd59f98195ca243f26e5e591830582b5c`;
  query `newborn is limp, will not feed, and is hard to wake`; expected card
  `newborn_danger_sepsis` / `GD-284`; cited source IDs
  `GD-284|GD-298|GD-492|GD-617`)
- Reviewed-card A11b choking runtime proof:
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5554/20260424_220443_026/emulator-5554`,
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5556/20260424_220443_026/emulator-5556`,
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5558_landscape/20260424_220443_026/emulator-5558`,
  and
  `artifacts/android_reviewed_card_a11b_choking_matrix_20260424/matrix_5560_landscape/20260424_220443_026/emulator-5560`
  (reinstall/no-pack-push prompt matrix with APK SHA
  `63126e427e21385f1e55e0451b81a32d768eb9ab63cc734b77efca723dd740af`;
  query `baby is choking and cannot cry or cough`; expected card
  `choking_airway_obstruction` / `GD-232`; cited source IDs
  `GD-232|GD-284|GD-298|GD-617`)
- Reviewed-card A11c meningitis/sepsis child runtime proof:
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5554/20260424_222904_794/emulator-5554`,
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5556/20260424_222904_793/emulator-5556`,
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5558_landscape/20260424_222904_794/emulator-5558`,
  and
  `artifacts/android_reviewed_card_a11c_meningitis_matrix_20260424/matrix_5560_landscape/20260424_222904_794/emulator-5560`
  (reinstall/no-pack-push prompt matrix with APK SHA
  `8bee066f6ce3a988388cd60d767e70bb93c46bf8cf43a4329ddd7ff21171ddd4`;
  query `child has fever, stiff neck, and a purple rash that does not fade when pressed`;
  expected card `meningitis_sepsis_child` / `GD-589`; Android DAO cited source
  IDs `GD-589|GD-235|GD-268|GD-284|GD-298`)
- Reviewed-card A11d infected wound / spreading infection runtime proof:
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5554/20260424_225122_947/emulator-5554`,
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5556/20260424_225122_947/emulator-5556`,
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5558_landscape/20260424_225122_947/emulator-5558`,
  and
  `artifacts/android_reviewed_card_a11d_infected_wound_matrix_20260424/matrix_5560_landscape/20260424_225122_947/emulator-5560`
  (no-pack-push prompt matrix with APK SHA
  `f1ae9a28e619069cbd8bc057070440e49948d30395e56d9fa727a2855c1de6d6`;
  query `cut on my hand yesterday and now a red streak is moving up my arm`;
  expected card `infected_wound_spreading_infection` / `GD-585`; cited source
  ID `GD-585`)
- Reviewed-card A11e abdominal / internal bleeding runtime proof:
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5554/20260424_230341_476/emulator-5554`,
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5556/20260424_230341_476/emulator-5556`,
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5558_landscape/20260424_230341_476/emulator-5558`,
  and
  `artifacts/android_reviewed_card_a11e_abdominal_internal_bleeding_matrix_20260424/matrix_5560_landscape/20260424_230341_503/emulator-5560`
  (no-pack-push prompt matrix with APK SHA
  `3bd27a2aad4504328428aa78ec2e5e91414eafa05b5dfd1c918505264e249f2c`;
  query `bike handlebar hit his belly and now he is pale and dizzy`;
  expected card `abdominal_internal_bleeding` / `GD-380`; cited source IDs
  `GD-380|GD-232|GD-584`)
- Reviewed-card CH2 runtime planner registry proof:
  `artifacts/android_reviewed_card_ch2_runtime_registry_harness_20260424/20260424_231010_747/emulator-5556`
  (post-refactor phone prompt canary with APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`;
  connected `OfflineAnswerEngineAnswerCardRuntimeTest` also passed `8` tests
  per device across the fixed matrix)
- Reviewed-card A14a layout / pack readiness proof:
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/developer_panel/`
  (collapsed, expanded runtime-off, and runtime-on screenshots for all four
  documented emulators) plus prompt proof matrix
  `artifacts/android_reviewed_card_a14a_layout_readiness_20260424/prompt_matrix_5554/20260424_232015_186/emulator-5554`,
  `.../prompt_matrix_5556/20260424_232015_189/emulator-5556`,
  `.../prompt_matrix_5558_landscape/20260424_232015_187/emulator-5558`, and
  `.../prompt_matrix_5560_landscape/20260424_232015_186/emulator-5560`
  on APK SHA
  `69edef64c37d41b437df684b453f1ba0e5d8787bd73b1c34435771ff5f778c9f`.
- Reviewed-card A14b proof-surface copy proof:
  `artifacts/android_reviewed_card_a14b_proof_surface_20260424/matrix_5554/20260424_233418_753/emulator-5554`,
  `.../matrix_5556/20260424_233418_753/emulator-5556`,
  `.../matrix_5558_landscape/20260424_233418_753/emulator-5558`, and
  `.../matrix_5560_landscape/20260424_233418_753/emulator-5560`
  on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`;
  reviewed-card meta strip/body/card/proof surfaces use reviewed-card-specific
  trust copy.
- Reviewed-card A14c forbidden-label proof:
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/canary_5556_runtime_on/20260425_084723_076/emulator-5556`
  proves runtime-on non-reviewed `generic_puncture` stays `STRONG EVIDENCE`
  without `REVIEWED EVIDENCE`;
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/reviewed_5556_runtime_on/20260425_084832_600/emulator-5556`
  proves reviewed poisoning stays `REVIEWED EVIDENCE` without
  `STRONG EVIDENCE`; runtime-off non-reviewed matrix passed under
  `artifacts/android_reviewed_card_a14c_forbidden_label_20260425/matrix_5554/20260425_084910_317/emulator-5554`,
  `.../matrix_5556/20260425_084910_339/emulator-5556`,
  `.../matrix_5558_landscape/20260425_084910_317/emulator-5558`, and
  `.../matrix_5560_landscape/20260425_084910_317/emulator-5560`
  on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
- Reviewed-card A14d exposure-policy decision:
  [`ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`](./ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md).
  Decision is Option A: keep reviewed-card runtime developer/test-only and
  default `off`; no product-default behavior, card expansion, top-level product
  UI, local-preview toggle, or runtime default change is approved.
- Reviewed-card CH3 scripted harness contract proof:
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/reviewed_5556/20260425_091908_871/emulator-5556`
  and
  `artifacts/android_reviewed_card_ch3_harness_contract_20260425/non_reviewed_5556_runtime_on/20260425_092020_829/emulator-5556`
  prove the extracted androidTest helper still parses reviewed and
  non-reviewed expected/forbidden label contracts on APK SHA
  `c01e8e3ccc890a1fc9b2234b6c11955f8589eb8e1bcfeff2ef2bab5dffac8347`.
- Reviewed-card developer-panel toggle proof:
  `artifacts/android_reviewed_card_toggle_20260424_193049/`
  (phone/tablet portrait and landscape screenshots)
- Reviewed-card prompt-harness proof against the real pushed pack:
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_475/`
  and
  `artifacts/android_reviewed_card_prompt_harness_20260424/20260424_195737_566/`
  (phone/tablet portrait and landscape scripted asks)
- Reviewed-card metadata/proof harness against the real pushed pack:
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_371/`
  and
  `artifacts/android_reviewed_card_metadata_harness_20260424/20260424_202123_366/`
  (explicit card ID, guide ID, review status, cited reviewed source, and proof
  summary assertions)
- Treat those artifact paths as the current evidence set; keep [`CP9_ACTIVE_QUEUE.md`](./CP9_ACTIVE_QUEUE.md) as the durable tracked summary.

## Historical / local context

- Local dated Android notes such as `AGENT_STATE.yaml`, `ACTIVE_WORK_LOG_20260412.md`, `ANDROID_ROADMAP_20260412.md`, `NEXT_AGENT_HANDOFF_20260411.md`, `UI_SECOND_OPINION_20260413.md`, and `UI_STATE_PACK_RECOVERY_PLAN_20260417.md` remain useful as local context only.
- The 2026-04-17 review gallery and its execution notes are historical review records, not the current baseline.

## Current practical reminder

- Prefer the pack hot-swap lane when code has not changed.
- Fresh APK installs now include the six pilot-reviewed answer cards in the
  checked-in asset pack; no manual probe-pack push is needed for that pilot
  proof. Runtime still requires the developer/test flag.
- Runtime selection currently covers all six checked-in developer/test-gated
  pilot cards:
  `poisoning_unknown_ingestion`, `newborn_danger_sepsis`, and
  `choking_airway_obstruction`, `meningitis_sepsis_child`, and
  `infected_wound_spreading_infection`, and `abdominal_internal_bleeding`.
  `RAG-CH2` has already extracted the repeated runtime planner
  descriptor/helper shape while leaving clinical predicates explicit.
  `RAG-A14a` then added proof-only layout / pack readiness evidence, and
  `RAG-A14b` harmonized reviewed-card proof-surface copy so the meta strip,
  body/card label, and proof summary use reviewed-card-specific trust wording.
  `RAG-A14c` added forbidden-label negative controls so reviewed and
  non-reviewed flows cannot leak each other's answer-surface labels in prompt
  proof. `RAG-A14d` then chose Option A for exposure policy: runtime remains
  developer/test-only and default `off`. A future non-developer preview would
  need its exact support language proven across the fixed four postures first.
  `RAG-CH3` then extracted pure scripted reviewed-card harness parsing and
  fail-closed validation into `ScriptedPromptHarnessContract`, reducing future
  prompt-proof edits in `PromptHarnessSmokeTest` without product behavior
  changes.
- `RAG-A14` is a product enablement gate, not card expansion. Its first
  enforceable guard has landed for reviewed-evidence labeling, proof-harness
  fail-closed checks, and old-pack fallback, but runtime remains off by default
  until the remaining policy, layout, pack, and validation gates close.
- Reviewed-card exposure policy draft:
  [`ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DRAFT_20260424.md`](./ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DRAFT_20260424.md).
  A14d decision:
  [`ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md`](./ANDROID_REVIEWED_CARD_EXPOSURE_POLICY_DECISION_20260425.md).
  Current recommendation is developer/test-only; do not expand cards or expose
  reviewed-card runtime outside developer/test controls from the current
  evidence set.
- `RAG-CH1` moved reviewed-card detail metadata transport/state and rule-id
  guarded lookup into `DetailReviewedCardMetadataBridge`; behavior is unchanged
  after compile/unit checks and the clean-install no-push matrix under
  `artifacts/android_reviewed_card_bridge_matrix_20260424/`.
- Prefer the fixed emulator matrix for day-to-day posture proof; use physical devices as checkpoint truth checks instead of rotating live emulator lanes.
- Prefer logged/search-only harnesses over repeated UI-dump polling for slow retrieval checks.
- Do not assume current lexical timings reflect the intended FTS-backed path unless a trusted artifact confirms it.
- Fixed emulator matrix for posture proof:
  - `5556` phone portrait
  - `5560` phone landscape
  - `5554` tablet portrait
  - `5558` tablet landscape
