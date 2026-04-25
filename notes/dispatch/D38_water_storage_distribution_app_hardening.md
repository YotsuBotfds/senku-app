# Slice D38 - water storage vs distribution app hardening

- **Role:** main agent (`gpt-5.4 xhigh`). App-side retrieval/answer-shaping micro-slice; good fit for a `gpt-5.4 high` worker once D37 is closed.
- **Paste to:** **new window** after D37 lands or is explicitly abandoned.
- **Parallel with:** avoid any parallel writer touching `mobile_pack.py` or the Android mobile retrieval classes/tests.
- **Why this slice now:** D36 unlocked real guide/app work, and the next highest-leverage live miss is still the Android water lane: broad water-storage prompts remain too willing to blur into distribution/treatment material, while explicit gravity-fed distribution must stay strong. This slice is the narrow pack-plus-runtime pass to sharpen that boundary without reopening unrelated Android plumbing.

## Outcome

One focused commit that:

1. Tightens water-storage versus water-distribution markers / bonuses so broad storage prompts stay in the storage lane more consistently.
2. Preserves the explicit gravity-fed distribution win, including storage-tank follow-up behavior inside that lane.
3. Adds or updates targeted tests so the storage/distribution boundary is durable.

## Preconditions (HARD GATE - STOP if violated)

1. `HEAD` is `8850b63b8a0d45a31280f3d499749d9e3c52fb7f` (`D36: track guides corpus as-is`) or descends from it.
2. D37 is no longer actively editing the worktree. Do **not** stack this slice on top of in-flight uncommitted guide edits from another worker.
3. The target Android and pack files are tracked and clean before edit.
4. The relevant targeted test surfaces still exist:
   - `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
   - `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
   - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
5. Pre-existing unrelated dirt is expected and does **not** block:
   - `AGENTS.md` modified
   - `opencode.json` modified
   - `notes/SWARM_COST_REDUCTION_OPERATING_MODEL_20260414.md` deleted
   - `LM_STUDIO_MODELS_20260410.json` remains untracked
   - `4-13guidearchive.zip` remains untracked local-only
   - residual historical root `notes/` backlog remains

## Read set

Read before editing:

- `notes/AGENT_STATE.yaml`
- `mobile_pack.py`
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`

## Likely touch set

Primary expected edits:

- `mobile_pack.py`
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`

Touch only if a test proves the answer-shape contract itself is still wrong after retrieval is right:

- `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`

## Diagnosis target

Confirm the smallest change that addresses the known miss:

1. Broad storage prompts such as long-term treated-water storage should stay in `water_storage` and avoid over-admitting distribution/treatment material.
2. Explicit gravity-fed distribution prompts must continue to prefer `water_distribution`.
3. Distribution follow-ups like `what about storage tanks` inside a gravity-fed distribution thread must keep the distribution lane, not collapse into generic storage.

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** widen into unrelated water families like purification/toxicology unless a direct storage/distribution test proves it is necessary.
- Do **not** edit session memory, prompt-builder, host-inference, harness wrappers, or UI classes in this slice.
- Do **not** rewrite broad answer formatting unless retrieval/selection is already proven correct and one narrow answer-shaping guard is still required.
- Do **not** touch guide markdown in this slice.
- Do **not** widen beyond the water storage/distribution boundary.

## Editing rules

### Step 1 - read-only diagnosis

Use the existing tests and current code to identify the narrowest failure mode:

1. marker seeding in `mobile_pack.py`
2. metadata/section bonus behavior in `QueryMetadataProfile`
3. candidate filtering / reranking in `PackRepository`
4. answer-shape only if retrieval remains correct but the surfaced answer still drifts

### Step 2 - make the smallest pack-plus-runtime change

Good changes:

1. tighten storage markers so generic storage-tank wording does not automatically imply `water_distribution`
2. keep explicit gravity-fed / household taps / community water / distribution-network phrasing strong for the distribution lane
3. adjust section/title bonuses or candidate filters so storage prompts favor storage-specific anchors while explicit distribution prompts keep their specialized anchor requirement

Bad changes:

1. broad route-profile rewrites
2. global ranking refactors
3. new harness tooling
4. unrelated Android cleanup

### Step 3 - targeted validation

Run the narrowest useful validation set that proves both sides of the boundary:

1. targeted Android JVM tests for the touched classes
2. any targeted Python tests if `mobile_pack.py` behavior is covered there

At minimum, cover:

- storage prompt remains `water_storage`
- explicit gravity-fed distribution remains `water_distribution`
- `storage tank` follow-up inside a distribution thread still favors the distribution guide

If a lightweight headless or pack-level replay is already available and cheap, include one narrow proof. Do **not** widen into a long emulator campaign in this slice.

## Acceptance

- One commit only.
- Edits stay inside the listed pack/runtime files and targeted tests.
- Broad storage prompts get a tighter storage bias without regressing explicit gravity-fed distribution.
- Distribution follow-up handling around storage tanks remains intact.
- Targeted tests were run and reported clearly.
- Final report states whether any answer-shape code needed touching, or whether the fix stayed in metadata/pack/reranking only.

## Commit

Single commit only. Suggested subject:

```text
D38: harden Android water storage vs distribution routing
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: diagnose the water storage/distribution boundary, make the smallest pack/runtime change, run targeted tests, commit once.
- Preferred worker discipline: if the retrieval/rerank fix is sufficient, stop there. Only touch `OfflineAnswerEngine` if a focused test shows the answer still drifts after retrieval is corrected.

## Anti-recommendations

- Do **not** turn this into a broad water-system overhaul.
- Do **not** reopen session-follow-up infrastructure.
- Do **not** mix in house/community-governance/parity work.
- Do **not** run a slow emulator sweep unless the narrow proof cannot be obtained another way.

## Report format

- Commit sha + subject.
- Which files changed.
- Whether `OfflineAnswerEngine` needed edits.
- Tests run + result.
- Short statement on storage prompt behavior, explicit distribution behavior, and distribution-follow-up behavior after the patch.
