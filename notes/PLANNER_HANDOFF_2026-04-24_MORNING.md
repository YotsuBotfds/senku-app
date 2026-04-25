# Planner Handoff - 2026-04-24 Morning

Generated at approximately 09:20 Central on 2026-04-24 after the no-emulator desktop guide-validation sweep.

## Current State

- Android/emulator validation remains parked for this Codex shell. User-side ADB was unblocked earlier and showed devices, but this shell still cannot reliably operate the Android SDK/emulator paths under the current sandbox behavior.
- Desktop guide validation is working.
- Use Python:
  - `.\.venvs\senku-validate\Scripts\python.exe`
- Guide-validation command shape:
  - `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave <wave> -PythonPath .\.venvs\senku-validate\Scripts\python.exe`
- Embeddings are on `http://127.0.0.1:1234/v1`.
- LiteRT generation is on `http://127.0.0.1:1235/v1`.
- New queue item created:
  - `notes/dispatch/D48_wave_ew_urgent_nosebleed_deterministic.md`

## Workflow Refinement

The successful pattern from `EK` through `EV` was:

1. Run or inspect the fresh wave baseline first.
2. Classify the six prompt-pack prompts directly with `query.classify_special_case`.
3. Prefer extending an existing deterministic predicate/builder when the guide owner and response contract already exist.
4. Add a new deterministic rule only when no existing builder fits. `EW` is the next example: query/rerank support exists, but no deterministic answer builder is present yet.
5. Add every wave prompt to focused tests plus 3-5 near misses. Most bugs found during this pass were substring/negation traps such as `no vomiting or confusion`.
6. Run:
   - `py_compile`
   - focused unittest suite
   - `scripts\validate_special_cases.py`
   - official `run_guide_prompt_validation.ps1 -Wave <wave>`
7. Inspect the final artifact header/footer for:
   - `embed_urls=http://127.0.0.1:1234/v1`
   - `Decision paths: deterministic: 6`
   - `Server workload: 127.0.0.1:1235/v1: 0 prompts`
8. Log the slice in `notes/PLANNER_CONTINUATION_2026-04-24_NO_EMULATOR_DESKTOP.md`.

Keep the rule of thumb: these are safety-contract hardening slices, not guide-rewrite slices.

## Completed Since Early-Morning Reinit

The latest detailed log is:

- `notes/PLANNER_CONTINUATION_2026-04-24_NO_EMULATOR_DESKTOP.md`

Completed deterministic wave slices:

- `EK` asthma severe respiratory distress:
  - `artifacts/bench/guide_wave_ek_20260424_082141.md`
  - all deterministic `asthma_severe_respiratory_distress`
- `EL` electrical injury:
  - `artifacts/bench/guide_wave_el_20260424_082417.md`
  - all deterministic `electrical_injury_red_flag`
- `EM` respiratory infection distress:
  - `artifacts/bench/guide_wave_em_20260424_082705.md`
  - all deterministic `respiratory_infection_distress_emergency`
- `EN` single FAST stroke:
  - `artifacts/bench/guide_wave_en_20260424_082959.md`
  - all deterministic `classic_stroke_fast`
- `EO` diabetes hypoglycemia/DKA:
  - `artifacts/bench/guide_wave_eo_20260424_083216.md`
  - all deterministic `diabetic_glucose_emergency`
- `EP` acute abdomen:
  - `artifacts/bench/guide_wave_ep_20260424_083358.md`
  - all deterministic `surgical_abdomen_emergency`
- `EQ` GI bleed / acute abdomen:
  - `artifacts/bench/guide_wave_eq_20260424_083802.md`
  - five `gi_bleed_emergency`, one `surgical_abdomen_emergency`
- `ER` early-pregnancy / ectopic:
  - `artifacts/bench/guide_wave_er_20260424_084058.md`
  - all deterministic `ectopic_pregnancy_emergency`
- `ES` eye injury / vision loss:
  - `artifacts/bench/guide_wave_es_20260424_084314.md`
  - deterministic across corrosive-eye, retinal, and globe-injury owners
- `ET` severe dehydration / poor intake:
  - `artifacts/bench/guide_wave_et_20260424_084501.md`
  - all deterministic `severe_dehydration_gi_emergency`
- `EU` heat illness / heat stroke:
  - `artifacts/bench/guide_wave_eu_20260424_084724.md`
  - all deterministic `heat_illness_emergency`
- `EV` dental/oral infection airway risk:
  - `artifacts/bench/guide_wave_ev_20260424_084929.md`
  - all deterministic `dental_infection`

Latest focused regression after `EV`:

- compile passed
- `122` focused tests passed:
  - `tests.test_special_cases`
  - `tests.test_query_routing`
  - `tests.test_registry_overlap`
  - `tests.test_deterministic_near_miss`
  - `tests.test_regenerate_deterministic_registry`
- deterministic registry validator passed:
  - `173` rules
  - `5` overlap checks

## Important Code Surfaces Changed

Primary files touched in the deterministic morning pass:

- `query.py`
- `special_case_builders.py`
- `deterministic_special_case_registry.py`
- `tests/test_special_cases.py`
- `tests/test_query_routing.py`
- `notes/specs/deterministic_registry_sidecar.yaml`
- `notes/PLANNER_CONTINUATION_2026-04-24_NO_EMULATOR_DESKTOP.md`

Do not assume the huge dirty worktree is all from this pass. There are many pre-existing guide/doc/script changes and untracked historical notes.

## Next Queue

### D48 / Wave EW - urgent nosebleed deterministic hardening

Dispatch file:

- `notes/dispatch/D48_wave_ew_urgent_nosebleed_deterministic.md`

Baseline artifact:

- `artifacts/bench/guide_wave_ew_20260424_085012.md`

Current EW baseline:

- `6/6` successful
- all six prompts used RAG/generation
- no deterministic builder yet
- embed confirmed on `1234`

Recommended first move:

1. Add `urgent_nosebleed_emergency` deterministic rule/builder.
2. Reuse/mirror the existing `query.py` `_is_urgent_nosebleed_query` behavior.
3. Add tests for all six EW prompts and near misses for GI bleed, major external bleeding, head-injury nose/ear fluid, dental bleeding, and "lean forward" outside nosebleed context.
4. Run the focused regression suite and `-Wave ew`.

### After EW

Do not queue all of these as one slice. The next prompt packs are distinct safety families:

- `EX` choking / food obstruction:
  - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ex_20260417.txt`
- `EY` meningitis / stiff neck / rash:
  - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ey_20260417.txt`
- `EZ` newborn sepsis / sick infant:
  - `artifacts/prompts/adhoc/test_targeted_guide_direction_wave_ez_20260417.txt`

Suggested order: `EW`, then `EX`, then `EY`, then `EZ`.

## Known Caveats

- Full `unittest discover` has previously hit sandbox temp-permission noise. The focused suite above is the reliable acceptance path for these deterministic routing slices.
- `git status` prints permission warnings for `C:\Users\tateb\.config\git\ignore`; treat as environment noise.
- The worktree is heavily dirty. Do not revert unrelated changes.
- `EW` should not be rushed into the same slice as `EX`; nosebleed and choking have different builders and false-positive risks.

## Clean Stop Point

The morning pass intentionally stopped after `EV` and created `D48` for `EW`. The next agent can start at `D48` without rereading the entire continuation log.

## 09:27 Planner Retake Addendum

Planner seat retaken at `2026-04-24 09:21:49 -05:00`, with slicing target still `11:00` Central.

Route correction encoded during retake:

- General-purpose subagent scout/worker delegation should use `gpt-5.5 medium`.
- Use high reasoning only for delicate safety/predicate implementation or ambiguous ownership decisions.
- Updated:
  - `notes/AGENT_OPERATING_CONTEXT.md`
  - `notes/SUBAGENT_WORKFLOW.md`

Fresh baselines run during retake:

- `EX`: `artifacts/bench/guide_wave_ex_20260424_092327.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: deterministic `2`, rag `4`
  - server workload: `127.0.0.1:1235/v1`: `4` prompts
- `EY`: `artifacts/bench/guide_wave_ey_20260424_092407.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: deterministic `1`, rag `5`
  - server workload: `127.0.0.1:1235/v1`: `5` prompts
- `EZ`: `artifacts/bench/guide_wave_ez_20260424_092437.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: rag `6`
  - server workload: `127.0.0.1:1235/v1`: `6` prompts

New dispatch files queued after `D48`:

- `notes/dispatch/D49_wave_ex_choking_food_obstruction_deterministic.md`
- `notes/dispatch/D50_wave_ey_meningitis_stiff_neck_rash_deterministic.md`
- `notes/dispatch/D51_wave_ez_newborn_sepsis_sick_infant_deterministic.md`

Recommended queue remains strictly sequential:

1. `D48` / `EW` urgent nosebleed deterministic hardening.
2. `D49` / `EX` choking and food obstruction deterministic hardening.
3. `D50` / `EY` meningitis / stiff neck / rash deterministic hardening.
4. `D51` / `EZ` newborn sepsis / sick infant deterministic hardening.

Do not merge these into one implementation slice. The false-positive boundaries differ enough that each deserves its own focused validation artifact.

### 09:31 Additional Baseline Scout

Fresh FA-FE baselines were run before pausing slicing for project-level RAG evaluation:

- `FA`: `artifacts/bench/guide_wave_fa_20260424_092935.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: deterministic `2`, rag `4`
  - likely next slice: unknown child ingestion / poison-control deterministic hardening
- `FB`: `artifacts/bench/guide_wave_fb_20260424_092957.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: deterministic `4`, rag `2`
  - likely next slice: infected wound urgent-care boundary completion
- `FC`: `artifacts/bench/guide_wave_fc_20260424_093011.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: deterministic `1`, rag `5`
  - likely next slice: abdominal trauma emergency deterministic owner
- `FD`: `artifacts/bench/guide_wave_fd_20260424_093037.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: rag `6`
  - likely next slice: mania / dangerous activation deterministic owner extension
- `FE`: `artifacts/bench/guide_wave_fe_20260424_093104.md`
  - `6/6`, errors `0`, cap hits `0`
  - decision paths: deterministic `6`
  - server workload `127.0.0.1:1235/v1`: `0` prompts
  - no implementation slice needed unless review finds answer wording problems

If slicing resumes after the RAG-method review, continue with `D52` as `FA`, then `FB`, `FC`, and `FD`. Do not spend a slice on `FE` unless a human review finds a real behavior issue.

## 09:41 RAG Method Review / Backlog Pivot

User direction changed from continuing deterministic prompt slicing to asking
whether the current method is converging or just patching failures forever.
Current answer: deterministic rules are still valuable for emergency safety
gates, but they should not become the default guide-answering strategy.

Durable strategy note:

- `notes/RAG_NEXT_LEVEL_STRATEGY_2026-04-24.md`

New dispatch lane:

- `notes/dispatch/RAG-S1_failure_taxonomy_and_bench_diagnostics.md`
- `notes/dispatch/RAG-S2_guide_answer_cards_schema_spike.md`
- `notes/dispatch/RAG-S3_contextual_chunk_ingest_spike.md`
- `notes/dispatch/RAG-S4_adaptive_retrieval_and_rerank_policy.md`
- `notes/dispatch/RAG-S5_claim_level_support_verifier.md`
- `notes/dispatch/RAG-S6_app_evidence_and_confidence_surface.md`

Queue decision:

- `RAG-S1` is the next active execution slice.
- `D48` through `D51` remain parked high-risk safety gates.
- The implied `D52`+ continuation into `FA` through `FD` is paused unless a
  human explicitly chooses another deterministic safety gate.
- Do not treat every prompt-wave miss as a new `query.py` predicate or
  `special_case_builders.py` response. First classify retrieval, ranking,
  context assembly, answer-generation, unsupported-claim, safety-contract,
  or true guide-content failure.

## 10:05 RAG-S1 / RAG-S1b Result

RAG-S1 landed as an offline diagnostic harness:

- `scripts/analyze_rag_bench_failures.py`
- `tests/test_analyze_rag_bench_failures.py`
- `notes/specs/rag_prompt_expectations_seed_20260424.yaml`
- result: `artifacts/bench/rag_diagnostics_20260424_1000/report.md`
- summary note: `notes/RAG_S1_DIAGNOSTIC_RESULT_20260424.md`

Main bucket counts across EX-FE:

- deterministic pass: `16`
- retrieval miss: `17`
- ranking miss: `4`
- safety-contract miss: `2`
- generation miss: `0`

RAG-S1b observability follow-up also landed:

- future bench JSON now carries `top_retrieved_guide_ids` and
  `source_candidates` per result;
- `bench_artifact_tools.py` flattens `top_retrieved_guide_ids`;
- focused validation passed `22` tests.

Next active behavior slice is `RAG-S4` safety-triage retrieval/rerank
profiles, with `RAG-S2` answer-card schema as the parallel content-contract
lane. Keep `D48` through `D51` parked unless a human asks for another
deterministic safety gate.
