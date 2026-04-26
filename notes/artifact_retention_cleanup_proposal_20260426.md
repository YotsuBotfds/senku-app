# Artifact Retention Cleanup Proposal - 2026-04-26

Status: approval-ready proposal only. No files should be deleted, moved, archived, or rewritten by this document.

## Source Inputs

- `AGENTS.md`
- `notes/PLANNER_HANDOFF_2026-04-26_AWAITING_DEEP_RESEARCH.md`
- `artifacts/bench/artifact_retention_20260426.md`
- `artifacts/bench/artifact_retention_20260426.json`

## Executive Summary

The 2026-04-26 retention report measured artifact pressure under `artifacts` in report-only dry-run mode. It found 5,069 artifact families, about 68.3 GB total, about 33.6 GB protected, and about 30.2 GB proposed as archive candidates. It also flagged 41 zero-byte empty generated families as delete candidates.

This proposal requests approval for a conservative first cleanup pass that performs no deletion. The first approved action should be archive-only, limited to unprotected generated artifacts, with proof and validation artifacts preserved in place unless explicitly reviewed later.

## Guardrails

- No deletion is approved by this proposal.
- No cleanup action may touch protected families or proof artifacts.
- No cleanup action may touch code, guides, notes, CI configuration, prompt sources outside `artifacts`, or repository metadata.
- Candidate artifact bodies were not read by the retention report; the report classified paths using references, family metadata, stat metadata, generated markers, age, and size.
- Android and emulator artifact cleanup should remain isolated from active validation work unless a maintainer explicitly reopens that lane.
- A fresh dry run must be generated immediately before any actual cleanup, because artifact state can change quickly during bench and CI work.

## Keep

Keep all families classified as `keep` or `keep_protected` by the dry-run report.

Keep these protected proof artifact classes regardless of age or size:

- High-liability deterministic proof artifacts for rows such as `RE9-SM-001`, `RE9-AN-001`, `RE9-PO-001`, `RE9-MH-001`, `RE7-NB-001`, `RE8-HT-001`, `RE6-IC-004`, `RE6-EV-002`, `RE9-BL-001`, `RE9-GI-001`, and `RE9-BU-001`.
- Analyzer and diagnostics evidence that explains safety-critical pass/fail states, including `strong_supported`, `generation_miss`, `card_contract_gap`, `ranking_miss`, `emergency_first_missing`, `unsafe_or_overconfident`, and `retrieval_missing_expected_owner` evidence.
- Non-Android regression gate outputs, failure logs, uploaded CI artifacts, and run manifests needed to reproduce or triage current validation.
- Any artifact referenced from `notes/dispatch`, `notes/specs`, or `artifacts/runs/run_manifest.jsonl`.
- Current benchmark summaries, CSV/JSON diagnostics, and harness outputs that are part of active deep-research triage or current safety validation.
- Any artifact required to distinguish a true guide knowledge gap from retrieval-language, ranking, answer-card runtime, or analyzer surfacing behavior.

## Archive

Archive-only is the recommended first cleanup action after approval and a fresh dry run. The current report identified 1,064 `archive_candidate` families totaling about 30.2 GB.

Recommended archive scope:

- Large unprotected generated families over the configured 500 MiB threshold.
- Older unprotected generated families beyond the 14-day archive threshold.
- Unprotected smoke, harness, prompt-pack, matrix, validation, review, and temporary generated outputs where the dry run reports no protection sources.
- Duplicate-size Android snapshot and stage-run families only after a maintainer confirms they are not needed for the active Android lane.

Largest current archive candidates by top-level family:

| Family | Approx bytes | Notes |
| --- | ---: | --- |
| `snapshots` | 11.4 GB | Android snapshots and zipped snapshots; archive only after lane owner approval. |
| `cp9_stage1_rcv4_*` through `cp9_stage1_rcv7_*` and `cp9_stage1_reparity_*` | 14.7 GB | Large generated stage-run families; archive if unprotected in fresh dry run. |
| `temp` | 2.0 GB | Generated temporary/build output; archive first, do not delete in this pass. |
| `ui_state_pack` | 679 MB | Large generated UI state pack; archive if not referenced. |
| `emulator5556_base.apk` | 368 MB | Older Android/emulator binary; archive only with Android lane approval. |
| `litert_release_bin` | 165 MB | Older binary family; archive if reproducible or no longer active. |
| `instrumented_ui_smoke` | 373 MB | Many small smoke families; archive non-empty unprotected families. |

Archive implementation requirements:

- Preserve relative paths inside an archive location or archive manifest so artifacts can be restored.
- Record source path, destination path, byte count, file count, mtime range, retention reasons, protection status, and approval ID.
- Produce a post-archive manifest and checksum list.
- Leave the original dry-run reports intact.

## Delete

No deletion is approved.

The current report lists 41 `delete_candidate` families totaling 0 bytes, mostly empty generated smoke directories and `test_tmp`. These may be reasonable future cleanup targets, but they should remain untouched in this pass because the handoff explicitly says the retention plan is report-only and cleanup/deletion requires explicit approval.

Future deletion consideration should require a separate approval that names exact paths or a generated manifest, confirms all candidates are still zero-byte and unprotected, and includes a restore or recreation note where applicable.

## No-Touch

Do not touch:

- Anything outside `artifacts`.
- `artifacts/bench/artifact_retention_20260426.md` and `artifacts/bench/artifact_retention_20260426.json`.
- Current CI failure logs or artifacts until the active non-Android regression run is resolved.
- Any artifact referenced by current planner handoff notes, dispatch notes, specs, run manifests, or benchmark diagnostics.
- Protected local-only planner notes.
- Android/emulator outputs unless the Android lane owner explicitly approves that subset.
- Prompt packs or bench outputs that are still being used for deep-research triage or high-liability proof work.

## Validation And Approval Checklist

Before cleanup:

- [ ] Maintainer approves archive-only scope in writing.
- [ ] Current CI state is checked, especially the latest non-Android regression gate.
- [ ] A fresh report-only retention run is generated.
- [ ] Fresh report still shows every proposed archive family as unprotected.
- [ ] Proposed archive manifest is reviewed for protected proof classes and active triage artifacts.
- [ ] Android/emulator candidates are either excluded or separately approved by the Android lane owner.
- [ ] Available disk space is checked for archive creation.

During cleanup:

- [ ] Archive only; perform no delete operation.
- [ ] Preserve original paths in the archive or manifest.
- [ ] Capture command transcript, archive destination, checksums, and byte/file counts.
- [ ] Stop immediately if a candidate becomes protected, missing, modified during cleanup, or ambiguous.

After cleanup:

- [ ] Verify archived byte/file counts match the approved manifest.
- [ ] Verify protected proof artifacts still exist at their original paths.
- [ ] Run focused non-Android validation or at minimum confirm benchmark/proof artifact discovery still resolves referenced paths.
- [ ] Generate a post-cleanup retention dry run and compare protected/candidate totals.
- [ ] Record the cleanup result in a dated notes handoff.

## Proposed Approval Decision

Approve an archive-only cleanup pass for unprotected generated artifact families confirmed by a fresh dry run. Do not approve deletion. Defer Android/emulator-heavy candidates and any safety proof artifact until a maintainer explicitly confirms they are no longer needed for active validation or deep-research triage.
