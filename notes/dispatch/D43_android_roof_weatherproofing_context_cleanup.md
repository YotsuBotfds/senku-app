# Slice D43 - Android roof-weatherproofing context cleanup

- **Role:** main agent (`gpt-5.4 xhigh`). Android routing/context micro-slice; good fit for a `gpt-5.4 high` worker after D42 lands.
- **Paste to:** **new window** only after D42 lands cleanly and the small-cabin site/foundation context mix is no longer the sharper miss.
- **Parallel with:** avoid any parallel writer touching `QueryMetadataProfile`, `PackRepository`, or their Android tests.
- **Why this slice now:** The remaining roof lane is narrow and well-defined: roof-weatherproofing prompts are already in the right family, but they still admit `GD-094` calculator-style context junk. This is not broad house cleanup. It is a focused section-level/context-level penalty pass for roofing/weatherproofing asks.

## Outcome

One focused commit that:

1. Reduces `GD-094` calculator-style or generic structural context bleed on roofing/weatherproofing prompts.
2. Keeps legitimate roof/weatherproof support intact.
3. Adds or updates narrow Android tests plus one small proof artifact.

## Preconditions (HARD GATE - STOP if violated)

1. D42 has landed cleanly, or equivalent evidence shows cabin site/foundation context mix is no longer the sharper miss.
2. `HEAD` descends from the D42 landing commit if it exists.
3. The target Android files are tracked and clean before edit.
4. The relevant test surfaces still exist:
   - `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
   - `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`
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

## Likely touch set

Primary expected edits:

- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`

Validation outputs:

- one narrow headless or `5556` proof artifact for a roof-weatherproofing prompt such as `waterproof a roof with no tar or shingles`

## Diagnosis target

Confirm the smallest remaining failure:

1. roofing/weatherproofing prompts are already in-family
2. the bad residue is section/context bleed from `GD-094` calculator or generic structural support
3. legitimate roof/weatherproof support must remain
4. earlier subsystem topic-prune work for wall/weatherproof and foundation must not regress

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** widen into broad house-route cleanup.
- Do **not** touch guides, water lanes, governance, or harness scripts.
- Do **not** reopen the general roof/topic-detection cleanup unless the current evidence proves it regressed.
- Do **not** turn this into a pack-metadata expansion slice.

## Editing rules

### Step 1 - read-only diagnosis

Use current roadmap/tests and code to isolate the narrowest fix:

1. topic interpretation for roof/weatherproofing prompts
2. section-level penalties for calculator-style support
3. context filtering in `PackRepository` only if metadata interpretation alone cannot explain the bleed

### Step 2 - make the smallest routing/context change

Good changes:

1. penalize calculator-style or generic structural context on roof-weatherproofing asks
2. preserve legitimate roof/weatherproof sections
3. keep wall-weatherproof and foundation topic footprints intact

Bad changes:

1. broad house-lane refactor
2. guide edits
3. unrelated app answer-shaping work
4. general metadata expansion

### Step 3 - targeted validation

Run the narrowest useful proof:

1. targeted Android JVM tests for the touched classes
2. one narrow headless or Android proof for a roof-weatherproofing prompt

If the proof shows the miss is actually in answer-shaping or guide content rather than context selection, stop and report that mismatch rather than widening the slice.

## Acceptance

- One commit only.
- Edits stay inside the named Android routing/context files/tests.
- Roof-weatherproofing prompts keep the right family but lose the `GD-094` calculator junk.
- Legitimate roof/weatherproof support remains.
- One narrow proof artifact exists and is reported.

## Commit

Single commit only. Suggested subject:

```text
D43: tighten Android roof-weatherproofing context mix
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: isolate the roof-weatherproofing context bleed, make the smallest metadata/context adjustment, run targeted JVM tests plus one narrow proof, commit once.

## Anti-recommendations

- Do **not** turn this into a broader house-quality project.
- Do **not** touch water-storage or site-selection code in the same commit.
- Do **not** mix in guide edits or harness work.

## Report format

- Commit sha + subject.
- Files changed.
- Tests run + result.
- Proof artifact path.
- Short statement on roof-weatherproofing behavior after the patch.
