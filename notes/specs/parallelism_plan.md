# Parallelism Fan-Out Plan

Date: 2026-04-18 (rev 21: release-candidate v2 pack + gallery ready)
Author: Claude Opus 4.7 review
Purpose: so the orchestrator can go brrr through `opustasks.md` without workers stepping on each other.
Paired with: [`../../opustasks.md`](../../opustasks.md) Ãƒâ€šÃ‚Â· [`./ui_direction_rev03.md`](./ui_direction_rev03.md)

Read this file once per session before dispatching a wave. It tells you:
- which tasks share files and therefore need spaced submission or bundling
- which tasks are truly disjoint and safe to fan out
- the per-wave worker plan with suggested owner-tier assignments
- where to expect contention and how to avoid it

### Routing Ladder (rev 2 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â unchanged in rev 3)

| tier | model | role |
|------|-------|------|
| **Orchestrator** | **GPT-5.4 ÃƒÆ’Ã¢â‚¬â€ high ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â fast mode** | single slot; dispatches waves, merges, adjudicates checkpoints, keeps the sidecar index coherent |
| **Workers** | **GPT-5.4 high mode** | file-edit lane; N parallel; one worker owns one task (or one bundle) at a time |
| **Scouts** | **GPT-5.3 Codex Spark high** (fallback: **GPT-5.4 mini medium**) | read-only audits, triage, prompt-pack drafting, risk checks, compose doc spikes; fallback kicks in on Spark outage |

All labels below collapse to this ladder: "worker" = GPT-5.4 high, "scout" = Spark 5.3 high (ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini medium fallback), "orchestrator" = GPT-5.4 fast.

---

## Status Snapshot (top of file ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â read this first)

- **Waves 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“4 landed** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â do not redispatch. `F-01`, `F-02`, `F-04`, `F-09`, `F-12`, `A-09`, `A-10`, and `E-03` are closed.
- **Wave 5 landed** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â `F-03 + F-13 + B-03 + F-10 + B-09` are closed.
- **Wave 6 landed** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â `F-08 + B-06`, `F-05 + F-07`, and `F-14` are closed.
- **Wave 7 repo truth reconciled** - `F-06`, `B-08`, `B-10`, and `B-07` are closed from repo evidence; the landscape composer choreography gate is green on `emulator-5560`.
- **F-15 backlog audit closed** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the Rev 03 runtime tweak panel is already absent on Android; only generic developer tooling remains.
- **Wave 8 repo truth reconciled** - `B-13`, `B-14`, `B-15`, `C-02`, `C-07`, and `C-08` are closed from repo evidence.
- **Wave 8 closed** - `C-01`, `C-06`, `B-13`, `B-14`, `B-15`, `C-02`, `C-07`, and `C-08` are all reconciled from repo evidence and fresh validation.
- **B-12 doc drift resolved** - `notes/specs/compose_migration_postmortem.md` is landed and should be treated as done, not a surviving Phase 9 open item.
- **Phase D guide closeout** - `D-01`, `D-02`, `D-03`, and `D-06` now have passing targeted reruns (`wave_de`, `wave_cr`, `wave_cv`, `wave_y`) after re-ingest; remaining misses are citation/ranking follow-through, not owned copy blockers.
- **C-06 repo truth** - the repo now has real `E4B` and `E2B` LiteRTLM files under `models/`, the LiteRT hosts are live on `127.0.0.1:1235` and `127.0.0.1:1236`, and the full LiteRT-backed baseline diff landed under `artifacts/bench/e2b_e4b_diff_20260418_112240_207`; it produced 8 shared rows with 0 cross-model diffs, while both landscape legs stayed partial because `emulator-5560 / det_fire_rain` shares the same launcher-after-pass artifact capture.
- **State-pack finalizer fix landed** - `build_android_ui_state_pack.ps1` slice runs no longer overwrite the shared top-level manifest, and `build_android_ui_state_pack_parallel.ps1` always executes the final aggregation pass before deciding failure.
- **Launcher false-pass gap closed** - `PromptHarnessSmokeTest` now captures app-rendered activity content before falling back to device screenshots, so the pack no longer treats launcher frames as trustworthy evidence.
- **Landscape focus-guard rerun landed** - `artifacts/ui_state_pack_landscape_focusguard/20260418_140701` is pass on `emulator-5560` and supersedes the earlier failed probe at `artifacts/ui_state_pack_landscape_focusguard/20260418_135308`.
- **B-16 landed** - phone-landscape now mounts a left nav rail instead of a stretched bottom tab bar, with the targeted `emulator-5560` rerun green at `artifacts/ui_state_pack_landscape_b16/20260418_131528`.
- **Current Android gate** - the release-candidate v2 packet is green at `artifacts/ui_state_pack_release_candidate_v2/20260418_200902` with `45 / 45` passing across the four-emulator matrix and app-rendered screenshots in every role.
- **External-review gallery regenerated** - the fresh review packet is ready at `artifacts/external_review/ui_review_20260418_201903_gallery/index.html`, built from the copied v2 pack artifacts instead of legacy placeholder screenshot names.
- **OPUS-C-09 follow-up logged** - stale per-device install-state cache can survive read-only emulator cold boots and cause a false `skip_install=true` path with zero artifacts; track it post-release from `artifacts/tablet_portrait_retest_20260418_1956` and `artifacts/tablet_portrait_diag_20260418_2001`, but do not let it block the current release candidate.
- **Phase 9 is release-candidate ready** - `E-05` is paused at a stable partial-completion milestone. The validated passes cover `MainPresentationFormatter`, `HomeGuidePresentationFormatter`, `DetailProofPresentationFormatter`, `DetailAnswerBodyFormatter`, `DetailAnswerPresentationFormatter`, `DetailGuidePresentationFormatter`, `DetailCitationPresentationFormatter`, `DetailActionBlockPresentationFormatter`, `DetailWarningCopySanitizer`, `DetailTranscriptFormatter`, `DetailSourcePresentationFormatter`, `DetailMetaPresentationFormatter`, `DetailGuideContextPresentationFormatter`, `DetailRelatedGuidePresentationFormatter`, `DetailRecommendationFormatter`, `DetailProvenancePresentationFormatter`, `DetailExpandableTextHelper`, `DetailSessionPresentationFormatter`, and `DetailThreadHistoryRenderer`; Python source-of-truth line counts put `MainActivity.java` at 3055 and `DetailActivity.java` at 6265, phone basic smokes are green through `artifacts/e05_smoke_pass18`, guide formatter validation remains green at `artifacts/e05_guide_smoke_pass4`, and presenter-pattern decomposition is deferred for a future session instead of gating release.
- **Lane F (Rev 03) is where the collision surface moved.** Pre-Rev-03 contention was on `query.py` and `DetailActivity.java`. Post-Rev-03 contention is on `activity_detail.xml` / `activity_main.xml` (shared by 3ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“4 F-tasks each) and the new Compose primitive files under `android-app/app/src/main/java/com/senku/ui/`.
- **F-lane is largely parallel-friendly** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â each new primitive lives in its own new `*.kt` file; collisions occur at the XML layouts that mount them and at `values/strings.xml`.

---

## Collision Hotspots (file ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ touching tasks)

Hot files in descending order of contention. If two workers edit a hot file at the same time, expect merge pain and index.jsonl churn.

### `res/layout/activity_detail.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â #1 hotspot (post-Rev-03)

| task | region | safe with |
|------|--------|-----------|
| `F-03` | answer card region (replaces chat-bubble body) | **bundle with F-13** |
| `F-13` | docked composer footer region | **bundle with F-03** |
| `B-08` | landmark / contentDescription markup | **rebase onto F-06** for tablet; after F-03+F-13 land on base |

**Rule:** `F-03 + F-13` is a single bundle (same layout, sibling regions, small). `B-08` waits until Phase 7 and rebases onto whatever the final Rev 03 layout looks like. NO other worker touches `activity_detail.xml` until Phase 7.

### `MainActivity.java` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â #2 hotspot

| task | region | safe with |
|------|--------|-----------|
| `F-05` | bottom-tab navigation plumbing + per-tab back stack | **bundle with F-07** |
| `F-07` | identity strip bind | **bundle with F-05** |
| `F-02` (migration site) | telemetry strip mount via ComposeView | sequential ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â land as part of F-02's migration step, not concurrent with F-05/F-07 |
| `F-12` (migration site) | recent threads mount | sequential after F-07 |
| `F-14` (migration site) | category shelf mount | sequential after F-07 |

**Rule:** Treat `MainActivity.java` as serialized across Phases 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“6. Bundle `F-05 + F-07` in Phase 6. F-02's MainActivity mount is a small follow-on patch that rebases onto F-05+F-07 when Phase 6 lands. F-12 and F-14 mount sites rebase last.

### `res/values/strings.xml` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â #3 hotspot

| task | additions |
|------|-----------|
| `F-01` | color semantic names tied to tokens (if surfacing any as strings) |
| `F-02` | `meta_*` keys per `metastrip_primitive_spec.md:187-215`; deprecate stamp keys |
| `F-07` | identity strip labels |
| `F-12` | pivot + thread row labels |
| `F-14` | category shelf labels |
| `B-09` | post-normalizer copy scrub |
| `B-14` | drop `ROLE_` prefix (may edit strings.xml if any `ROLE_` lives there) |

**Rule:** `strings.xml` is append-only for most of these; the real risk is two workers claiming adjacent lines at the same time. Bundle F-01 resource patch + F-02 strings vocabulary; subsequent F-tasks each append their own block with a clear section comment. B-09 is the post-normalizer wiring ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â no strings.xml edit expected but grep before starting.

### `res/values/colors.xml` and `res/font/` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â #4 hotspot

Only `F-01` touches these files. Counted as hotspot only because downstream F-tasks *read* the tokens ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â if tokens are renamed mid-stream, every primitive rebases. Freeze token names in `F-01` before dispatching F-02..F-15.

### `item_search_result.xml` + `SearchResultAdapter.java` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â #5 hotspot

| task | region |
|------|--------|
| `F-08` | paper-card container swap; Compose mount |
| `B-06` | continue-thread chip state |

**Rule:** **bundle F-08 + B-06** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the adapter edit is trivial and the container swap invalidates any solo B-06 diff.

### `res/layout-land/activity_detail.xml` (NEW file) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â hotspot

| task | region |
|------|--------|
| `B-07` | composer focus; creates the file |
| `B-10` | broader Rev-03 landscape overrides |

**Rule:** `B-07 + B-10` bundle, unchanged from rev 2.

### `DetailActivity.java` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â used to be #2; now #6 post-Rev-03

Most of the detail-surface logic moves into Compose primitives. What remains in the Activity:

| task | region | safe with |
|------|--------|-----------|
| `F-03` (mount site) | ComposeView bind for PaperAnswerCard | covered by F-03+F-13 bundle |
| `F-13` (mount site) | ComposeView bind for DockedComposer | covered by F-03+F-13 bundle |
| `B-03` | `buildContextualFollowupCandidates(anchorId)` feeding F-10 | independent method; safe-parallel with F-03+F-13 (different methods) |
| `B-08` | landmark wiring (after F-06 lands) | Phase 7 only |
| `E-05` | Presenter extraction | **Phase 9 strictly serial ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â after all F-lane and B-lane surface edits land** |

### `android-app/app/src/main/java/com/senku/ui/` (new Compose primitives)

Each primitive lives in its own new file. No intra-directory collisions.

| file | owning task |
|------|-------------|
| `theme/SenkuColors.kt`, `theme/SenkuTypography.kt` | F-01 |
| `primitives/MetaStrip.kt` + `MetaItem.kt` | F-02 |
| `primitives/TopBar.kt` | F-04 |
| `primitives/BottomTabBar.kt` | F-05 |
| `primitives/PivotRow.kt`, `primitives/ThreadRow.kt` | F-12 |
| `answer/PaperAnswerCard.kt`, `answer/AnswerContent.kt` | F-03 |
| `tablet/TabletDetailScreen.kt` + tablet helpers | F-06 |
| `home/IdentityStrip.kt`, `home/CategoryShelf.kt` | F-07, F-14 |
| `search/SearchResultCard.kt` | F-08 |
| `sources/SourceRow.kt` | F-09 |
| `suggest/SuggestChip.kt` | F-10 |
| `evidence/EvidenceSnippet.kt`, `evidence/XRefRow.kt` | F-11 |
| `composer/DockedComposer.kt` | F-13 |

**Rule:** these files are all new. Zero merge pain from fan-out. Collisions only appear at the mount sites (XML layouts, Activity Java) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â see the XML/Java hotspots above.

### `query.py` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â pre-Rev-03 #1; now quiet

Almost all Lane A tasks landed. Remaining open Lane A tasks that touch query.py:
| task | region |
|------|--------|
| `A-09` | `decompose_query` L6528 (codex may have shifted this ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â grep before editing) |
| `A-10` | reranker uniform demotion inside `_metadata_rerank_delta` (rebases onto landed A-04) |

**Rule:** serial submission in Wave 4. A-09 first (smaller blast radius), then A-10.

### Inherited hotspots that no longer apply

- `config.py` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â all three old concurrent tasks landed.
- `DetailActivity.java` (chip generator area) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â B-03 is the only remaining Lane B DetailActivity task, and it's method-scoped. Safe-parallel with F-03+F-13 bundle.
- `LiteRtOpenAiServer.java`, `PackRepository.java` (Lane A sites), `PromptHarnessSmokeTest.java` (C-03) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â all landed.

---

## Safe Parallelism Matrix (who can run with whom)

Within a given wave, the following pairs are confirmed safe-to-parallel (file-disjoint or region-disjoint-and-bounded):

- `F-02` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-04` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-09` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-12` (all new files under `com/senku/ui/primitives/` or sibling packages)
- `F-01` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â any F-task as long as token names are frozen before the other starts
- `F-03` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-10` (answer body vs suggest chip ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â disjoint new files, even though both touch DetailActivity mount, F-10 mount is a trivial follow-on)
- `F-06` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â anything outside `res/layout-large*/` and `TabletDetailScreen.kt`
- `F-08 + B-06` (bundle) ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â any non-search task
- `A-09` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â F-lane (query.py vs Android ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â totally disjoint)
- `A-10` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â F-lane (query.py vs Android ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â totally disjoint)
- `E-03` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â F-lane (backend vs UI ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â totally disjoint)
- `B-03` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-03 + F-13` (different method regions inside DetailActivity; safe with announcement contract)
- `B-09` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â any F-task (copy-scrub helper is a shared module edit, but lives in its own helper file; grep before starting)
- Any Lane D task ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â any other lane (guides are pure content)
- `C-01`..`C-08` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â each other (test-infra files, mostly disjoint; C-01 + C-02 soft-depend, see below)

Pairs that **look** parallel but are actually NOT:

- `F-03` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-13` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â both land in `res/layout/activity_detail.xml` sibling regions. **Bundle.**
- `F-05` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-07` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â both touch `MainActivity.java` nav plumbing area. **Bundle.**
- `F-08` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `B-06` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â both in `item_search_result.xml` + `SearchResultAdapter.java`. **Bundle.**
- `B-07` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `B-10` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â both target `res/layout-land/activity_detail.xml`. **Bundle.**
- `F-01` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `F-02`..`F-15` mid-stream ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â only parallel-safe if tokens are frozen before other primitives start. Safest: land `F-01` first, then fan out F-02..F-15.
- `C-01` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `C-02` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â C-02 consumes helpers added by C-01. **Staircase: C-01 first, C-02 starts 2 hours in.**
- Any Lane B surface task ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `E-05` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â **forbidden concurrency.** E-05 is strictly Phase 9, serialized.
- `A-10` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬Â `A-04` (landed) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â A-10 already rebases onto the landed A-04 region; no concurrency issue unless two A-10 attempts dispatch simultaneously.

---

## Must-Bundle Pairs (authoritative list)

Never fan out across these pairs. One worker owns the bundle.

1. `F-03 + F-13` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `res/layout/activity_detail.xml` answer card + docked composer
2. `F-05 + F-07` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `MainActivity.java` bottom-tab plumbing + identity strip bind
3. `F-08 + B-06` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `item_search_result.xml` + `SearchResultAdapter.java`
4. `B-07 + B-10` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `res/layout-land/activity_detail.xml` + `res/layout-land/activity_main.xml`
5. `F-01` tokens + fonts + `SenkuColors.kt` + `SenkuTypography.kt` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ one patch, token names frozen after merge

## Strict-Serial Pairs

Do one fully, then the other. Not concurrent.

1. `A-04` (landed) ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `A-10` (rebases onto it) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â serial inside Wave 4
2. `F-01` ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ any other F-task that reads tokens (F-02..F-15) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â land F-01 first
3. `F-03 + F-13` (Phase 5) ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `B-08` (Phase 7 rebases onto this)
4. `F-06` (Phase 7) ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `B-08` (Phase 7 rebases onto F-06)
5. All `F-*` + all surviving `B-*` surface edits ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ `E-05` (Phase 9)

---

## Wave Fan-Out Plan

Each wave lists suggested slots. Under the rev 2 ladder: one worker slot = one GPT-5.4 high mode task; one scout slot = one Spark 5.3 high (or mini-medium fallback) task; the orchestrator is a single GPT-5.4 fast-mode lane that dispatches, merges, and adjudicates. Do not oversubscribe the worker queue ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â stagger with 30-second spacing during wide waves.

### Waves 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â **LANDED** (codex completed 2026-04-17)

Reference only. Do not redispatch.

- **Wave 1 (Phase 1 Hygiene)** Ãƒâ€šÃ‚Â· E-01+E-02, E-04, A-03+A-08, C-03 Ãƒâ€šÃ‚Â· CP1 green
- **Wave 2 (Phase 2 Safety-routing)** Ãƒâ€šÃ‚Â· A-01, A-02, A-04, A-05, A-06+A-07, C-05 Ãƒâ€šÃ‚Â· CP2 green (gallery 06 re-shoot clean)
- **Wave 3 (Phase 3 Answer contract + empty-state UX)** Ãƒâ€šÃ‚Â· B-01, B-02, B-05, C-04 Ãƒâ€šÃ‚Â· CP3 green (gallery 04 replacement clean)

### Wave 4 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Phase 4 Rev 03 foundation + backend cleanup Ãƒâ€šÃ‚Â· 5 workers + scout + D-lane fill Ãƒâ€šÃ‚Â· 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days

Goal: land the Compose primitive foundation and close the remaining backend cleanup.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: F-01          design tokens + fonts + SenkuColors/Typography     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: res/values/colors.xml (+dimens, +styles), res/font/* (new), ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          SenkuColors.kt, SenkuTypography.kt (new)                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   RULE: W1 LANDS FIRST. Token names freeze at merge.                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: F-02          MetaStrip Compose primitive                        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   spec: metastrip_primitive_spec.md                                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: primitives/MetaStrip.kt, MetaItem.kt (new) + tests          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          values/strings.xml (meta_* keys ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â append block)             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   starts 1 hour after W1 merge (reads frozen tokens)                 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W3: F-04          slim TopBar primitive                              ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: primitives/TopBar.kt (new) + test                           ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   starts 1 hour after W1 merge                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W4: F-09          SourceRow compact primitive                        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: sources/SourceRow.kt (new) + test                           ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   starts 1 hour after W1 merge                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W5: F-12          Pivot + Thread row primitives                      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: primitives/PivotRow.kt, primitives/ThreadRow.kt (new) + testsÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   starts 1 hour after W1 merge                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W6: A-09          dedupe domain sub-queries                          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (backend-lane)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: query.py (decompose_query)                                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   independent of F-lane ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â dispatch immediately                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W7: A-10          bridge-guide ingest tag + uniform demotion         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (backend-lane)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: ingest.py, guide front-matter, query.py reranker            ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   serial AFTER A-09 (same file)                                      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W8: E-03          VectorStore unmap verification                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (backend-lane)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: PackRepository.java, VectorStore.java                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   totally disjoint ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â run any time                                    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scout: read-audit A-10 ingest-time bridge detection; draft D-04/D-05 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout (Spark ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini fallback)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡        micro-pack candidates                                          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ D-lane fill (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 parallel): D-02, D-03, D-06 (P1 short tasks)        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (fill-lane)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Orchestrator: land W1 first; then dispatch W2..W5 staircased (1h     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                apart); A-09 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ A-10 serial; E-03 + D-lane anywhere;    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                batch one re-ingest at the end for A-10 + D-lane.      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
CHECKPOINT 4 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Foundation primitives green
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ MetaStrip unit + screenshot tests pass on all four postures
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ TopBar / SourceRow / PivotRow / ThreadRow render in isolation
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ design tokens compile clean; no hardcoded hex in Rev 03 files
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ A-09 / A-10 land without breaking any routing prompt pack
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ E-03 VectorStore unmap confirmed via hot-swap test
```

**Wave 4 worker-count reality check:** 8 worker slots looks rich, but only 5 are concurrent at peak (W1 alone for first hour; then W2+W3+W4+W5 after token freeze; W6+W7+W8 run backend-lane in parallel, each disjoint from F-lane). D-lane fill adds 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 more slots during idle capacity. Orchestrator is the merge bottleneck: stagger W2..W5 by 1 hour so their patches arrive sequentially, not simultaneously.

**Worker announcement contract applies to every dispatch.** See Ãƒâ€šÃ‚Â§Worker Submission Discipline.

### Wave 5 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Phase 5 Rev 03 answer surface Ãƒâ€šÃ‚Â· 4 workers + scout Ãƒâ€šÃ‚Â· 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days

Goal: replace the answer body with the paper AnswerCard and its wiring.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: [F-03 + F-13]  BUNDLE ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â paper AnswerCard + docked composer      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   spec: answercard_paper_spec.md                                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: answer/PaperAnswerCard.kt, answer/AnswerContent.kt (new)    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          composer/DockedComposer.kt (new)                            ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          res/layout/activity_detail.xml (sibling region edits)       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          DetailActivity.java (ComposeView bind sites)                ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          values/strings.xml (detail_* section headers)               ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: F-10          Try next SuggestChip primitive                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: suggest/SuggestChip.kt (new)                                ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   starts with static placeholder; rebinds after B-03                 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W3: B-03          contextual follow-up chip candidate generator      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: DetailActivity.java (new method buildContextualFollowup...) ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   feeds W2 (F-10)                                                    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   method-scoped; safe-parallel with W1's DetailActivity mount edits  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W4: B-09          copy sanitizer helper                              ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: shared copy-scrub helper (new module), mount at result +    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          detail + helper surfaces + query.py post-normalizer         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   grep before editing ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â confirm no strings.xml overlap with W1       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scout: draft the wave_F05 gallery re-shoot prompt pack (gallery 04 + ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout (Spark ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini fallback)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡        06); audit PaperAnswerCard a11y regions                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Orchestrator: merge W1 first (largest blast radius); W2 then rebinds ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                its SuggestChip source to B-03 once W3 lands.          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                Two DetailActivity workers (W1 + W3): announcement     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                contract REQUIRED; different methods.                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
CHECKPOINT 5 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Answer surface green
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ F-03 + F-13 (bundled) replace the detail answer region
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ gallery 04 re-shoot shows abstain in paper-card form
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ gallery 06 re-shoot shows stacked Q+A paper cards with thread continuity
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ F-10 Try next chips render real graph-derived candidates via B-03
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ B-09 copy sanitizer catches all known warning residues
```

**Wave 5 contention notes:** only DetailActivity.java has two workers (W1 mount, W3 method add). Both are method-scoped or region-scoped, so announce method names in the dispatch. W4 (B-09) risks strings.xml collisions with W1's `detail_*` additions ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â grep before editing; if a collision surfaces, merge W1 first and rebase W4.

### Wave 6 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Phase 6 Rev 03 navigation + home + search Ãƒâ€šÃ‚Â· 5 workers + scout Ãƒâ€šÃ‚Â· 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days

Goal: replace home and search surfaces with Rev 03 primitives.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: [F-05 + F-07]  BUNDLE ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â bottom tabs + identity strip             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: primitives/BottomTabBar.kt (new), home/IdentityStrip.kt     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          (new), MainActivity.java (nav plumbing + identity bind)     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          values/strings.xml (identity labels ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â append block)         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: [F-08 + B-06]  BUNDLE ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â search result paper cards + continue-    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   thread chip                                                         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: search/SearchResultCard.kt (new)                            ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          res/layout/item_search_result.xml (container swap)          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          SearchResultAdapter.java (Compose mount + chip state)       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W3: F-14          category shelf                                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: home/CategoryShelf.kt (new), MainActivity.java mount (small ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          follow-on, rebases onto W1)                                 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W4: D-04          symptom-first medical expansion                    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (fill-lane)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   long-form D-lane work ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â file-disjoint, lives in guides/            ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W5: D-05          household chemical early-triage                    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (fill-lane)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scout: draft wave_F06 home + search screenshot prompt pack; audit    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout (Spark ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini fallback)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡        IdentityStrip against Haiku-review notes                      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Orchestrator: W1 first (MainActivity hot); W2 and W3 parallel;       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                W3's MainActivity mount rebases onto W1 (trivial).     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                Batch one re-ingest after W4+W5 land.                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
CHECKPOINT 6 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Home + search green
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ bottom tabs render with correct active states
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ home identity strip, pivot rows, category shelf render at expected densities
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ search results render as paper cards with lane tags
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ B-06 continue-thread chip surfaces on result rows when warm
```

### Wave 7 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Phase 7 Rev 03 tablet 3-pane + landscape Ãƒâ€šÃ‚Â· 4 workers + scout Ãƒâ€šÃ‚Â· 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days

Goal: eliminate the wasted-landscape problem; wire live evidence pane.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: F-06          TabletDetailScreen 3-pane reader                   ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   spec: tablet_three_pane_spec.md                                    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: tablet/TabletDetailScreen.kt (new)                          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          res/layout-large-land/activity_detail.xml (new)             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          res/layout-large/activity_detail.xml (new ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â tablet portrait ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡            collapse)                                                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          DetailActivity.java (tablet posture detection ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Compose     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡            tree mount)                                                ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: F-11          live evidence snippet + XRef primitives            ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: evidence/EvidenceSnippet.kt, evidence/XRefRow.kt (new)      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          PackRepository.java (new method getAnchorSnippet)           ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   used by: W1 (F-06 right pane) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â W1 mounts the primitives after W2 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡            lands                                                      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W3: [B-07 + B-10]  BUNDLE ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â phone-landscape variants                 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: res/layout-land/activity_detail.xml (new)                   ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          res/layout-land/activity_main.xml (new)                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   totally disjoint from tablet layouts                               ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W4: B-08          provenance + source-graph a11y regions             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: res/layout/activity_detail.xml (phone regions ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â rebased     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡            onto F-03+F-13 landed in Phase 5)                         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          tablet/TabletDetailScreen.kt (rebases onto W1 mid-wave)     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          DetailActivity.java (landmark binding)                      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   serial AFTER W1 on TabletDetailScreen.kt; parallel with W2/W3     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   for its phone-base edits                                            ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scout: draft wave_F07 tablet landscape + phone landscape prompt pack;ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout (Spark ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini fallback)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡        audit TabletDetailScreen 3-pane TalkBack walk                 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Orchestrator: W2 merges mid-wave (W1 right pane binds to the         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                EvidenceSnippet primitive + getAnchorSnippet method   ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                that W2 lands). W1 then W4 against tablet Compose.    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
CHECKPOINT 7 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Tablet parity green
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ gallery 10 (tablet landscape) re-shoot shows 3-pane reader ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â no wasted space
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ evidence pane pulls a live snippet from the anchor chunk
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ cross-reference list uses getReciprocalLinks(anchorId)
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ phone landscape (5560) composer never obscured
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ TalkBack walks the three panes as distinct regions
```

**Wave 7 coordination note:** W1 and W4 touch `TabletDetailScreen.kt`. W4's edits are landmark attributes on regions W1 defines ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â cannot dispatch concurrently. Sequence: W2 and W3 fan out immediately; W1 starts same time (different file); W4 starts after W1 lands (2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 hours into the wave).

### Wave 8 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Phase 8 Test-infra + Rev 03 audits Ãƒâ€šÃ‚Â· 5 workers + scout Ãƒâ€šÃ‚Â· 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 days

Goal: rebuild the trusted test harness, run the Rev 03 audit tasks.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: C-01          PromptHarnessSmokeTest posture truth               ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (test-infra)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: C-02          seeded two-turn detail state                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (test-infra)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   soft-depends on W1; staircase 2 hours in                           ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W3: C-07          summary.json screenshot-presence assertion         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (test-infra)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W4: C-06          E2B vs E4B diff lane (new script)                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (test-infra)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W5: C-08          SQLite FTS5 probe (new script)                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (test-infra)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W6: B-13          text-overflow handling pass                        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   Compose primitive edits + res/layout children ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â grep touched filesÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   for collisions with any deferred F-lane follow-on                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W7: B-14          drop ROLE_ prefix                                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   trivial; runs alongside W6                                         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W8: B-15          Rev 03 palette contrast audit                      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   files: values/colors.xml (may tune tokens), severity card + strip ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡          render tests                                                 ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   **bundle with B-08 IF B-08 slipped into Phase 8 wave**             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W9: F-15          remove Tweak runtime toggle from prod              ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   XS task ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â collapse into any worker with idle capacity; NOT a       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   dedicated slot                                                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scout: rerun build_android_ui_state_pack_parallel.ps1 after each     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout + emulator matrix
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡        audit lands; final state pack aim = 45/45                     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Orchestrator: W1 first; stagger W2. W3/W4/W5 parallel (each new file).ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                W6/W7/W8 parallel once F-lane is fully quiet. W9 inline.ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
CHECKPOINT 8 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ State pack 45/45, Rev 03 audits green
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ build_android_ui_state_pack_parallel.ps1 produces 45/45 with artifacts
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ every Rev 03 primitive clears B-13 overflow, B-14 tag casing, B-15 contrast audit
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ F-15 removes Tweak runtime toggle from prod builds
```

**Emulator matrix is pre-parallel.** `start_senku_emulator_matrix.ps1` + `build_android_ui_state_pack_parallel.ps1` already fan out across 4 postures. Do NOT re-parallelize at the task level ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â it'll contend for ADB and produce stale screenshots.

**Wave 8 is worker-rich (6 active at peak) but collision-light** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â test-infra files are largely new, and B-13/B-14/B-15 are audits with small edits to already-landed Compose primitives. Orchestrator can push to 6 concurrent comfortably because most work is file-disjoint.

### Wave 9 ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Phase 9 Presenter extraction + release prep Ãƒâ€šÃ‚Â· 2 workers serial + orchestrator + scout Ãƒâ€šÃ‚Â· release-candidate ready

Goal: package the field-manual Rev 03 release candidate; deeper presenter-pattern decomposition can resume later if needed.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: E-05          Presenter extraction (MainActivity + DetailActivity)ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker (test-infra)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   **STRICTLY SERIAL** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â E-05 is the only worker editing these files ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   during Phase 9. No concurrent Lane B tasks.                         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   Incremental: extract one handler group at a time; rebuild +        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   validate between extractions.                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: B-12          Compose migration post-mortem (scout doc)          ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout (Spark ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini fallback)
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   deliverable: notes/specs/compose_migration_postmortem.md           ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡   doc-only; can run in parallel with W1 since no code file overlap   ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scout: full gallery rebuild + external-review HTML regenerate        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Orchestrator: E-05 is the only code-edit worker; merges each         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                extraction pass and validates before the next.         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
CHECKPOINT 9 ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ Release candidate ready
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ state pack is clean at `artifacts/ui_state_pack_release_candidate_v2/20260418_200902` (`45 / 45`)
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ external-review gallery is ready at `artifacts/external_review/ui_review_20260418_201903_gallery/index.html`
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ E-05 is paused at `3055` / `6265`; presenter-pattern decomposition is deferred
              ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¢ reviewer sign-off + release APK build remain
```

**Phase 9 is deliberately narrow.** The only live follow-through after this point is reviewer sign-off, release APK build, and the non-blocking `OPUS-C-09` harness-cache follow-up.

### Phase D ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â Content / guides Ãƒâ€šÃ‚Â· fill-lane, continuous

Wave D is **not a peer of Waves 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â it is a continuously-running fill lane that uses *idle worker capacity* during the other waves. The total worker count across (current wave)+D at any instant must still respect the orchestrator merge throughput (ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¤5 conflict-heavy, ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¤7 disjoint).

Because guide edits are file-disjoint (each task edits its own guide), they count toward the "disjoint" budget, not the "conflict-heavy" budget.

```
ÃƒÂ¢Ã¢â‚¬ÂÃ…â€™ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‚Â
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ D-01 through D-06 are embarrassingly parallel ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â different guides     ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Fan out as wide as the worker lane can sustain alongside the         ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ active Wave 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9 cohort.                                               ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W1: D-01 (crisis gestalt, wave_de)    [in-flight, carry-over]        ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W2: D-02 (wave_cr overdose)                                           ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W3: D-03 (wave_cv surgical abdomen)                                   ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W4: D-04 (symptom-first expansion)                                    ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W5: D-05 (household chemical triage)                                  ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ W6: D-06 (GD-190 dominance recheck)                                   ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ worker
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡                                                                       ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ Scouts draft the matching wave_XX micro-packs in parallel             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ scout lane
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ ONE re-ingest (orchestrator-owned) after each wave's guide batch      ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ orchestrator
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡ lands, then validate once.                                             ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬Å¡
ÃƒÂ¢Ã¢â‚¬ÂÃ¢â‚¬ÂÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ¢â€šÂ¬ÃƒÂ¢Ã¢â‚¬ÂÃ‹Å“
```

**Hard rule: one re-ingest per batch.** Per `AGENTS.md`: *"Re-ingest after guide edits before trusting desktop retrieval results."* Doing N re-ingests for N parallel guide edits is wasteful. Batch them ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the orchestrator owns the single re-ingest trigger per wave.

**Collision with A-10:** `OPUS-A-10` (bridge-guide ingest-time tag) changes `ingest.py`. Land A-10 in Wave 4, then the first D-lane batch re-ingest after Wave 4 picks up the new tags atomically. Subsequent D-lane re-ingests are simple reindexes.

---

## Lanes That Are Always Free

These lanes can run at any point in any wave because they do not touch shared files:

- **Scout lane** (Spark 5.3 high, fallback GPT-5.4 mini medium; read-only audits) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â always running. Use it to triage next wave's candidates, draft `wave_XX` prompt packs, and run risk checks.
- **Validation lane** (`scripts/run_guide_prompt_validation.ps1`) ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â single-consumer bottleneck on LM Studio, but independent of code edits.
- **Emulator matrix** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â already internally parallel; treat as one resource.
- **Documentation / specs** ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â new `notes/specs/*.md` files are free; existing ones are not.

---

## Worker Submission Discipline

From `AGENTS.md`: *"For wide sidecar coding waves, prefer spaced submission to avoid `artifacts/opencode/sidecar/index.jsonl` contention."* ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â the same discipline applies to the GPT-5.4 worker lane. Too many concurrent enqueues still race the queue.

### Spacing rules

| concurrent workers | spacing between submissions |
|---|---|
| 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“2 | none required |
| 3ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“4 | 30 seconds |
| 5+ | 60 seconds, strict |
| 6+ on same file | NOT ALLOWED ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â bundle or serialize |

### F-lane-specific spacing

For the foundational Wave 4 dispatch:
- W1 (F-01) lands **before** anything in W2..W5 starts. Hold W2..W5 for 1 hour post-merge, not 1 hour post-dispatch.
- Reason: F-01 freezes token names that F-02..F-15 consume. Starting a dependent primitive against unfrozen tokens leads to rename-storm rebases.

### Announcement contract

Every worker task prompt dispatched by the orchestrator should include these three lines at the top:

```
FILE: <path(s)>
LINES: <range or "new file">
CONFLICTS WITH: <task IDs that also touch these files in the current wave>
```

For F-lane tasks with new files, state the file path explicitly; for XML mount sites, state the region (e.g., "sibling region to existing answer body").

### When a conflict shows up

If two workers land with overlapping line ranges:
1. Orchestrator stops dispatching new work on that file.
2. Orchestrator pulls both patches and does the merge (fast mode is built for this).
3. Re-dispatch follow-on tasks only after merge validation.

---

## Model Routing During Brrrr (rev 3)

The routing ladder has exactly three tiers ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â unchanged from rev 2:

| tier | model | concurrency | role |
|------|-------|-------------|------|
| orchestrator | **GPT-5.4 ÃƒÆ’Ã¢â‚¬â€ high ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â fast mode** | 1 | dispatch, merge, checkpoint adjudication, integration; must stay snappy to keep the queue flowing |
| worker | **GPT-5.4 high mode** | up to **4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“5** per wave (6ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“7 for disjoint waves) | all file-edit tasks (Python, Java, XML, Kotlin, guides); one task or one bundle per worker |
| scout | **GPT-5.3 Codex Spark high** (fallback **GPT-5.4 mini medium** on Spark outage) | 2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 concurrent | read-only audits, prompt-pack drafting, risk checks, wave triage, compose/doc spikes |

### Tier rules (unchanged)

- **Orchestrator is the only merge authority.** Workers do not merge each other's patches. If two workers collide, the orchestrator pulls both and reconciles.
- **Workers announce files + line ranges up front** (see Announcement Contract). Workers do not read each other's WIP; the orchestrator sequences landings.
- **Scouts never write production files.** Scout outputs are drafts in `notes/specs/*.md`, `artifacts/prompts/*.jsonl`, or the scratch area ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â never `query.py`, `*.java`, `*.kt`, `*.xml`, or anything under `guides/`.
- **Spark ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini fallback** is automatic per request. If Spark returns an error or the Spark lane is unresponsive, re-dispatch the same scout prompt at `gpt-5.4 mini medium`. Do not re-dispatch scout work at `GPT-5.4 high` ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â that burns the worker lane on audit work.

### F-lane guidance

Most F-lane work lives in **new Compose files** under `android-app/app/src/main/java/com/senku/ui/`. This is a parallelism windfall:

- New files = zero rebase cost, so per-wave worker count can push toward the 6ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“7 upper end.
- Collisions are concentrated in **4 files**: `activity_detail.xml`, `MainActivity.java`, `item_search_result.xml`, `values/strings.xml`. Bundle or serialize around those four.
- Any Compose primitive task that also needs a mount-site edit (e.g., F-02 mounts via ComposeView in DetailActivity) dispatches the **primitive file alone first**, and the mount edit as a tiny follow-on patch. Halves the merge cost.

### Suggested maximum wave width (rev 3)

Given the tiers above, the practical ceiling per wave is:

- **1 orchestrator** (always on)
- **4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“5 workers typical** for file-conflict-heavy waves; orchestrator merge lane is the bottleneck
- **Up to 6ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“7 workers for embarrassingly-parallel waves** (Wave 4 foundation, Wave 8 audits) where most files are disjoint or new
- **2ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 scouts** (parallel read-audit and prompt drafting)

Total: typically **7ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9 concurrent slots active**; stretch to **10ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“11** for Waves 4 and 8. The orchestrator is the rate-limiter for merges ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â if you exceed 5 workers on a conflict-heavy wave, the orchestrator's merge queue starts trailing the worker queue. Exceed the stretch ceiling and you start fighting the sidecar `index.jsonl`, not the task.

**Rule of thumb:** *worker count should be ÃƒÂ¢Ã¢â‚¬Â°Ã‚Â¤ (orchestrator merge throughput ÃƒÆ’Ã‚Â· average worker turnaround)*. If worker patches arrive faster than the orchestrator can merge them, dispatch fewer workers.

### When to reach for which tier

- **File edit, spec-driven, 1+ hours** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ worker
- **Bundle of 2 small edits in the same file** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ one worker (not two)
- **New Compose primitive file + test** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ worker
- **Mount-site edit (tiny follow-on to a landed primitive)** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ worker, tiny patch
- **Read-only code audit, "does X happen anywhere in repo"** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ scout
- **Drafting a wave_XX prompt pack** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ scout
- **Drafting `notes/specs/*.md` content** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ scout (orchestrator reviews before commit)
- **Drafting `compose_migration_postmortem.md` (B-12)** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ scout, Phase 9
- **Rebase/merge of parallel worker patches** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ orchestrator
- **Checkpoint gate adjudication (CP4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“CP9)** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ orchestrator
- **Dispatching the next wave** ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ orchestrator (after CP green)

---

## Brrr Checklist (per wave ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â orchestrator runs this)

1. Read this file's wave section for the current phase.
2. Identify the collision hotspots in the wave.
3. Decide bundles (refer to Ãƒâ€šÃ‚Â§Must-Bundle Pairs).
4. Dispatch workers spaced, with the three-line announcement contract.
5. Queue blocked tasks (e.g., F-10 rebinds after B-03 lands; A-10 after A-09) as follow-ups, not initial wave members.
6. Launch scouts in parallel ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â they audit next wave while current wave executes.
7. If a scout request fails on Spark, re-route the same prompt to GPT-5.4 mini medium (fallback).
8. Merge worker patches as they arrive (orchestrator is the only merge lane), and mark each task's state in `opustasks.md` as it transitions `open -> in-flight -> done`.
9. Re-run local validation for the integrated wave before dispatching anything from the next wave. Worker-reported green is not sufficient by itself.
10. After the integrated wave is locally green, hit the checkpoint. If the checkpoint is red, do NOT start the next wave ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â diagnose first.

### Wave-4-specific checklist addenda

- **Land F-01 first.** Hold F-02..F-05 workers until F-01 merges. Token names freeze at merge.
- **Batch one re-ingest at the end of the wave** covering A-10 and any D-lane work that landed during Wave 4.
- **Validate MetaStrip on all four postures** before closing CP4. `start_senku_emulator_matrix.ps1` + `build_android_ui_state_pack_parallel.ps1`.

---

## Known Contention Risks (rev 3, post-codex-wave-3)

- **`activity_detail.xml` is now the #1 cliff.** Phase 5 (F-03+F-13 bundle) and Phase 7 (B-08 rebase) both edit it. Keep the bundle tight and Phase 7 strictly after Phase 5.
- **`MainActivity.java` is the #2 cliff.** Bundle F-05+F-07 in Phase 6; F-02, F-12, F-14 add small mount-site follow-ons that rebase trivially.
- **`values/strings.xml` will see many append blocks across Waves 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“6.** Grep before every F-task dispatch to confirm no lane collision. Use clear section comments.
- **F-01 token freeze is a wave-4 invariant.** Renaming a color token after F-02..F-15 start triggers a wide rebase. Freeze at merge.
- **Worker queue depth.** Hard cap at 5+ concurrent enqueues without 60s spacing ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â same discipline as the legacy sidecar lane.
- **Orchestrator merge lane.** If the orchestrator is idle while 5 workers are running, you're fine. If the orchestrator is processing another merge while new patches arrive, queue depth spikes ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â consider pausing new dispatch.
- **Re-ingest is serial.** No matter how many guide edits land in parallel, there is exactly one re-ingest before the next validation cycle.
- **Scout fallback latency.** GPT-5.4 mini medium is slower than Spark high ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â if Spark is down for a full wave, expect scout throughput to drop ~40 %. Plan accordingly.
- **LM Studio is a single consumer.** Generative validation bottlenecks on it; all other waves must not assume the validation bench is free.
- **Phase 9 E-05 is concurrent-hostile.** No other worker edits `MainActivity.java` or `DetailActivity.java` while E-05 runs. This is not a soft rule.

---

## Quick Lookup: "is task X parallel-safe with task Y?"

Check the collision matrix in Ãƒâ€šÃ‚Â§Collision Hotspots first, then Ãƒâ€šÃ‚Â§Must-Bundle Pairs. If neither rules it out, it's safe to parallel ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â but still announce files + line ranges in the worker dispatch prompt.

## Change Log

- **rev 3 Ãƒâ€šÃ‚Â· 2026-04-17** Ãƒâ€šÃ‚Â· Rev 03 UI migration absorbed. Waves 1ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“3 marked landed (codex completed). New Waves 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9 map to Phases 4ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“9 of `opustasks.md` rev 3. Hotspots rewritten: `activity_detail.xml` + `MainActivity.java` + `values/strings.xml` replace `query.py` + `DetailActivity.java` as the top cliffs. New Ãƒâ€šÃ‚Â§Must-Bundle Pairs and Ãƒâ€šÃ‚Â§Strict-Serial Pairs sections. Lane F guidance added to Ãƒâ€šÃ‚Â§Model Routing. Wave-4-specific checklist addenda added.
- **rev 2.1 Ãƒâ€šÃ‚Â· 2026-04-17** Ãƒâ€šÃ‚Â· double-check pass: resolved A-03/B-02/B-09 triad bundle; split activity_detail.xml hotspot base vs landscape; reconciled worker ceiling; added B-04ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬ÂB-11 serial pair; fixed stale query.py line numbers.
- **rev 2 Ãƒâ€šÃ‚Â· 2026-04-17** Ãƒâ€šÃ‚Â· routing ladder swapped to GPT-5.4 fast orchestrator + GPT-5.4 high worker + Spark 5.3 high (ÃƒÂ¢Ã¢â‚¬Â Ã¢â‚¬â„¢ mini medium fallback) scout.
- **rev 1 Ãƒâ€šÃ‚Â· 2026-04-17** Ãƒâ€šÃ‚Â· initial plan ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Â contention map, matrix, wave fan-out, discipline rules.