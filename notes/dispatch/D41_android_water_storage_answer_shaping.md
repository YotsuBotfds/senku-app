# Slice D41 - Android water storage answer shaping

- **Role:** main agent (`gpt-5.4 xhigh`). App-side answer-quality micro-slice; good fit for a `gpt-5.4 high` worker after D40 lands.
- **Paste to:** **new window** only after D40 lands cleanly and the desktop water/chemical retrieval surfaces are no longer the blocker.
- **Parallel with:** avoid any parallel writer touching `OfflineAnswerEngine`, `PromptBuilder`, or their Android tests.
- **Why this slice now:** The repo’s current Android truth still calls out broad water-storage answer shaping as a live product miss even after the narrower retrieval/rerank fixes. D38 already tightened the storage-vs-distribution routing boundary. The next bounded step is the answer layer: keep broad treated-water storage answers focused on storage/container/rotation guidance instead of drifting into treatment/bleach-heavy framing when retrieval is already right.

## Outcome

One focused commit that:

1. Tightens Android answer shaping for broad water-storage prompts after retrieval has already selected the right storage support.
2. Leaves retrieval/rerank code and guide markdown untouched.
3. Adds or updates narrow Android tests plus one small proof artifact from headless or emulator validation.

## Preconditions (HARD GATE - STOP if violated)

1. D40 has landed cleanly, or equivalent evidence shows the remaining water-storage miss is answer-shaping rather than retrieval/rerank.
2. `HEAD` descends from `ba57aad93e133c176f441069fdd96c3dd4c8e842` (`D38`) and the D40 landing commit if it exists.
3. The target Android files are tracked and clean before edit.
4. The relevant test surfaces still exist:
   - `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
   - `android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java`
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
- `notes/ANDROID_ROADMAP_20260412.md`
- `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java`

## Likely touch set

Primary expected edits:

- `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`

Touch only if a focused proof says the answer wording contract itself needs help:

- `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
- `android-app/app/src/test/java/com/senku/mobile/PromptBuilderTest.java`

Validation outputs:

- one narrow proof artifact from a headless or `5554` broad treated-water storage replay

## Diagnosis target

Confirm the remaining miss is answer shaping, not retrieval:

1. broad treated-water storage prompts already retrieve the correct storage guide/context
2. the Android answer still drifts into treatment / bleach / generalized water-safety framing more than needed
3. explicit water-distribution behavior from D38 stays intact and is not reopened

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** touch retrieval/rerank code (`PackRepository`, `QueryMetadataProfile`, `mobile_pack.py`) in this slice.
- Do **not** edit guide markdown.
- Do **not** widen into session memory, prompt-harness wrappers, or UI classes.
- Do **not** broaden beyond water-storage answer shaping.

## Editing rules

### Step 1 - read-only diagnosis

Use the existing roadmap/tests and any fresh D40 evidence to isolate the narrowest answer-layer fix:

1. same-guide context selection / source prioritization inside `OfflineAnswerEngine`
2. candidate/context limits for broad storage prompts
3. answer-body shaping or prompt-note shaping only if the retrieved context is already correct and still answered too generically

### Step 2 - make the smallest answer-layer change

Good changes:

1. prefer storage-specific context blocks when the prompt is broad treated-water storage
2. reduce treatment/bleach-heavy answer drift when storage context is already present
3. keep distribution follow-up behavior untouched

Bad changes:

1. reopening retrieval/rerank
2. guide edits
3. general prompt-contract overhaul
4. unrelated Android cleanup

### Step 3 - targeted validation

Run the narrowest useful proof:

1. targeted Android JVM tests for touched classes
2. one narrow headless or emulator proof for broad treated-water storage answer quality

If the proof shows retrieval is still wrong rather than answer shaping, stop and report that mismatch instead of widening this slice.

## Acceptance

- One commit only.
- Edits stay inside the named Android answer-shaping files/tests.
- Retrieval/rerank and guide markdown remain untouched.
- Broad water-storage answers become more storage-specific and less treatment/bleach-heavy.
- D38’s explicit distribution behavior stays intact.
- One narrow proof artifact exists and is reported.

## Commit

Single commit only. Suggested subject:

```text
D41: tighten Android water storage answer shaping
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: confirm retrieval is right, make the smallest answer-layer adjustment, run targeted Android tests plus one narrow proof, commit once.
- If `PromptBuilder` is untouched, say so explicitly in the report.

## Anti-recommendations

- Do **not** turn this into another retrieval slice.
- Do **not** widen into water-distribution retuning.
- Do **not** mix in harness work.
- Do **not** touch guide files.

## Report format

- Commit sha + subject.
- Files changed.
- Whether `PromptBuilder` needed edits.
- Tests run + result.
- Proof artifact path.
- Short statement on broad water-storage answer behavior after the patch.
