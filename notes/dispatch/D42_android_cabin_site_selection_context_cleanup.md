# Slice D42 - Android cabin site-selection context cleanup

- **Role:** main agent (`gpt-5.4 xhigh`). Android routing/context micro-slice; good fit for a `gpt-5.4 high` worker after D41 lands.
- **Paste to:** **new window** only after D41 lands cleanly and the broad water-storage answer-shaping lane is no longer the sharper miss.
- **Parallel with:** avoid any parallel writer touching `QueryRouteProfile`, `QueryMetadataProfile`, `PackRepository`, or their Android tests.
- **Why this slice now:** The remaining app-facing house miss is narrower now: small-cabin safe-site/foundation prompts still lean too hard on foundation detail and support bleed instead of surfacing more site-selection material early. The goal is not broad house cleanup. It is a tight context-mix correction so `GD-446` style site-selection support rises without regressing explicit foundation/drainage behavior.

## Outcome

One focused commit that:

1. Improves the Android cabin safe-site / foundation support mix so broad site-selection context rises earlier for prompts like `safe site and foundation for a small cabin`.
2. Keeps explicit foundation/drainage prompts working.
3. Adds or updates narrow Android tests plus one small proof artifact.

## Preconditions (HARD GATE - STOP if violated)

1. D41 has landed cleanly, or equivalent evidence shows the water-storage answer-shaping lane is no longer the sharper miss.
2. `HEAD` descends from the D41 landing commit if it exists.
3. The target Android routing files are tracked and clean before edit.
4. The relevant test surfaces still exist:
   - `android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`
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
- `notes/ACTIVE_WORK_LOG_20260412.md`
- `android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`

## Likely touch set

Primary expected edits:

- `android-app/app/src/main/java/com/senku/mobile/QueryRouteProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/QueryMetadataProfile.java`
- `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryRouteProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/QueryMetadataProfileTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackRepositoryTest.java`

Validation outputs:

- one narrow Android or headless proof artifact for a broad small-cabin site/foundation prompt

## Diagnosis target

Confirm the smallest remaining failure:

1. broad site/foundation prompts for a small cabin still overweight foundation/drainage support
2. site-selection material should rise earlier without losing legitimate foundation/drainage support when the prompt is truly explicit
3. explicit roof / wall / foundation subsystem behavior from earlier house cleanup must stay intact

## Boundaries (HARD GATE - STOP if you would violate)

- Do **not** widen into broad house cleanup.
- Do **not** edit guide markdown.
- Do **not** touch session memory, prompt-builder, or harness scripts in this slice.
- Do **not** start a pack-side metadata expansion project.
- Do **not** mix in unrelated Android lanes like governance or water.

## Editing rules

### Step 1 - read-only diagnosis

Use the roadmap/work-log evidence plus current tests to isolate the narrowest fix:

1. route-spec balance for broad site/foundation phrasing
2. metadata topic interpretation for site-selection versus explicit foundation intent
3. pack-side candidate filtering only if route/meta changes alone cannot explain the failure

### Step 2 - make the smallest routing/context change

Good changes:

1. nudge broad cabin safe-site/foundation prompts toward site-selection support
2. preserve explicit foundation/drainage behavior
3. tighten support mix without reopening roof/wall subsystems

Bad changes:

1. broad house-route refactor
2. guide edits
3. UI/harness work
4. unrelated pack metadata expansion

### Step 3 - targeted validation

Run the narrowest useful proof:

1. targeted Android JVM tests for the touched classes
2. one narrow Android or headless proof for a broad cabin safe-site/foundation prompt

If the proof shows the miss is actually answer-shaping or guide content instead of routing/context mix, stop and report that mismatch rather than widening this slice.

## Acceptance

- One commit only.
- Edits stay inside the named Android routing/context files/tests.
- Broad small-cabin site/foundation prompts surface more site-selection context earlier.
- Explicit foundation/drainage prompts stay intact.
- One narrow proof artifact exists and is reported.

## Commit

Single commit only. Suggested subject:

```text
D42: tighten Android cabin site-selection context mix
```

## Delegation hints

- Good `gpt-5.4 high` worker slice: isolate the small-cabin site/foundation mix issue, make the smallest route/meta/pack adjustment, run targeted JVM tests plus one narrow proof, commit once.

## Anti-recommendations

- Do **not** turn this into a general house-lane project.
- Do **not** touch water, governance, or chemical-routing code.
- Do **not** widen into guide edits.
- Do **not** mix in harness/tooling cleanup.

## Report format

- Commit sha + subject.
- Files changed.
- Tests run + result.
- Proof artifact path.
- Short statement on broad small-cabin site/foundation behavior after the patch.
