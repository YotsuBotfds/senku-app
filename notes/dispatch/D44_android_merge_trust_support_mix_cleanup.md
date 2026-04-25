# Slice D44 - Android merge/trust support-mix cleanup

- **Role:** main agent (`gpt-5.4 xhigh`). Android routing/context micro-slice; good fit for a `gpt-5.4 high` worker after D43 lands.
- **Paste to:** **new window** only after D43 lands cleanly and the roof-weatherproofing context bleed is no longer the sharper miss.
- **Parallel with:** avoid any parallel writer touching the governance-related Android routing/context files or their tests.
- **Why this slice now:** The governance lane is already in the right family and on the right anchor (`GD-626`), but merge-and-trust prompts still lean on the wrong support mix. The bounded next move is to prefer trust-repair / mediation support over monitoring/quota-heavy or finance-adjacent sections without reopening the whole governance/security cluster.

## Outcome

One focused commit that:

1. Keeps merge-and-trust prompts anchored on `GD-626`.
2. Demotes `GD-657` / finance-style or monitoring/quota-heavy support when the user is really asking about trust repair and group merge.
3. Adds or updates narrow Android tests plus one small proof artifact.

## Preconditions (HARD GATE - STOP if violated)

1. D43 has landed cleanly, or equivalent evidence shows roof-weatherproofing is no longer the sharper miss.
2. `HEAD` descends from the D43 landing commit if it exists.
3. The target Android files are tracked and clean before edit.
4. The relevant test surfaces still exist:
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
- `notes/ANDROID_ROADMAP_20260412.md`
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`

## Likely touch set

Primary expected edits:

- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`

Touch only if retrieval/context is proven right but the answer text still drifts:

- `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
- `android-app/app/src/test/java/com/senku/mobile/OfflineAnswerEngineTest.java`

Validation outputs:

- one narrow Android or headless proof artifact for `merge with another group if we don't trust each other yet`

## Diagnosis target

Confirm the smallest remaining failure:

1. merge-and-trust prompts are already in the right family and on the right anchor
2. the bad residue is support mix: too much monitoring/quota-heavy or finance-adjacent material
3. trust-repair / mediation sections from `GD-626` / `GD-385` should rise instead

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** widen into a broad governance or security overhaul.
- Do **not** edit guides.
- Do **not** touch unrelated Android lanes like water, roofing, or site selection.
- Do **not** mix in harness work or a pack-metadata project.

## Editing rules

### Step 1 - read-only diagnosis

Use the current roadmap/tests and code to isolate the narrowest fix:

1. metadata interpretation for governance merge/trust intent
2. section-level penalties/bonuses in `PackRepository`
3. answer-layer adjustment only if retrieval/context is already right and still phrased poorly

### Step 2 - make the smallest support-mix change

Good changes:

1. keep `GD-626` leading
2. prefer trust-repair / mediation sections for merge/trust prompts
3. demote finance-adjacent or monitoring/quota-heavy support when it is not the real ask

Bad changes:

1. broad governance-family retuning
2. security-lane changes
3. guide edits
4. unrelated app/harness work

### Step 3 - targeted validation

Run the narrowest useful proof:

1. targeted Android JVM tests for the touched classes
2. one narrow Android or headless proof for a merge-and-trust prompt

If the proof shows the miss is actually answer-shaping rather than support mix, stop and report that mismatch rather than widening this slice.

## Acceptance

- One commit only.
- Edits stay inside the named Android routing/context files/tests, plus `OfflineAnswerEngine` only if truly needed.
- Merge-and-trust prompts keep `GD-626` as anchor.
- Trust-repair / mediation support rises over finance/quota-heavy support.
- One narrow proof artifact exists and is reported.

## Commit

Single commit only. Suggested subject:

```text
D44: tighten Android merge-trust support mix
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: isolate the merge/trust support-mix issue, make the smallest metadata/context adjustment, run targeted JVM tests plus one narrow proof, commit once.
- If `OfflineAnswerEngine` is untouched, say so explicitly in the report.

## Anti-recommendations

- Do **not** turn this into a general governance project.
- Do **not** mix in security, economics, or guide work.
- Do **not** touch water or house lanes in the same commit.

## Report format

- Commit sha + subject.
- Files changed.
- Whether `OfflineAnswerEngine` needed edits.
- Tests run + result.
- Proof artifact path.
- Short statement on merge-and-trust behavior after the patch.
