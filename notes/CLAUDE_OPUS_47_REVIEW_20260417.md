# Claude Opus 4.7 — First-Pass Review of Senku Mobile MVP

**Date:** 2026-04-17
**Reviewer:** Claude Opus 4.7 (first session in this repo)
**Scope:** read-only exploration of code, screenshots, tracker notes, build state
**Deliverable pair:** this MD (for agents) + `artifacts/external_review/claude_opus_47_review_20260417/index.html` (for Tate)

---

## How I Read the Repo

Fanned out four parallel explorations:

1. **Desktop engine** — `query.py`, `config.py`, `deterministic_special_case_registry.py`, `special_case_builders.py`, `lmstudio_utils.py`, `guide_catalog.py`, `token_estimation.py`, `litert-host-jvm/`
2. **Android app** — `android-app/app/src/main/java/com/senku/mobile/*`, build files, instrumentation tests, mobile pack exporter
3. **Tracker synthesis** — ~15 notes in `notes/` dated 20260413-20260417, plus `GUIDE_PLAN.md`, `TESTING_METHODOLOGY.md`, `UI_DIRECTION_AUDIT_20260414.md`
4. **Screenshots** — eye-read five of the gallery PNGs (home, detail, followup, tablet landscape field links, provenance)

Then spot-verified the highest-signal claims from the subagents at file:line before writing this. Two of the Android audit's claims were wrong — noted inline below.

---

## The Single Most Actionable Finding (Look Here First)

**The follow-up thread is leaking wrong-guide context, visibly, in your own gallery screenshot.**

`screenshots/06_phone_portrait_followup_thread.png` shows:

- Turn 1: "How do I build a simple rain shelter from tarp and cord?" → routed to GD-444 (*Accessible Shelter & Universal Design*). The answer body says "The provided notes do not contain instructions on how to build a rain shelter from tarp and cord." So GD-444 was retrieved but wasn't actually applicable.
- Turn 2 follow-up: "What should I do next after the ridge line is up?" → routed to **GD-185 (Acetylene Generator / carbide production)** and the model happily generated "Set up the water seal and then create the carbide chamber above it."

Two distinct bugs stack here:

1. **Thread-anchor is not used for follow-up retrieval.** The word "ridge line" overlapped lexically with GD-185 (which talks about a water-seal bucket with a literal "line"), and the retriever treated the follow-up as an independent query. The prior turn's anchor (GD-444) should have been a heavy prior in reranking, and broad-magnetic demotion should have kept the retriever off GD-185. This matches your "Primary-owner lock with harder distractor demotion" perspective in `APP_ROUTING_HARDENING_TRACKER_20260417.md:74-90`.
2. **Answer contract still advisory-first.** Even with a clearly wrong retrieval, the answer template obediently filled in "Short answer / Steps / Limits" as if the guide were on-topic. The template should have an escape hatch: *if the top retrieved chunk has weak objective overlap, refuse politely* instead of hallucinating steps about carbide chambers.

This is not a content gap — GD-444 is correctly retrieved as "accessible shelter", it just isn't the shelter you asked about. Same family-merge issue your guide plan is already tracking (`GUIDE_PLAN.md`).

**Impact:** safety-critical in some adjacent domains (mental health, toxicology, medical — where wrong-but-confident step lists are more dangerous). This is the single bug I would hunt first.

---

## Bug Analysis (ranked)

### P0 — Safety / correctness

1. **Follow-up retrieval ignores thread anchor.** (evidence: screenshot 06 above; corroborated by `APP_ROUTING_HARDENING_TRACKER_20260417.md` perspective 2). *Suggested next move:* in Android `PackRepository`/`OfflineAnswerEngine`, when `SessionMemory.lastTurn().anchorGuideId != null`, boost that guide ID and its reciprocal-links in RRF with a decay across turns. Mirror on desktop `query.py:_metadata_rerank_delta`.

2. **Answer contract has no "abstain" path when retrieval is weak.** When retrieval returns chunks whose objective-overlap with the query is low, the prompt should be rewritten to an explicit "we don't have a guide for this; here's what I'd check; here are adjacent guides" answer. Today the template fills "No steps available" inside the Steps block, which reads as a bug, not an honest abstain.

3. **Hallucinated citations pass normalization.** `query.py:normalize_response_text` (~8663) strips bogus pseudo-citations and normalizes `GD-1` → `GD-001`, but never checks whether the normalized ID actually exists in `guide_catalog.all_guide_ids()`. A response citing GD-999 ships. *Fix:* add a `validate_citation_ids` step that drops or warns on unknown IDs before `print_review_postflight`.

4. **Metadata reranker delta explosion.** `query.py:5356-5465` stacks up to ±0.5 of boost/demote for a single chunk across 8+ conditional branches. No bound check, no audit log. Under edge cases (mental-health queries overlap with both elder-cognitive and sleep-management markers), retrieval order can invert silently. *Fix:* clamp cumulative delta to ±0.3 and log every applied branch in DEBUG mode.

5. **Deterministic registry has no conflict resolution.** `deterministic_special_case_registry.py` has 117 specs, applied in declaration order by `_match_deterministic_special_case`. A stroke+cardiac-overlap query matches `stroke_cardiac_overlap` before `classic_acs` — but that ordering isn't documented or unit-tested. *Fix:* add an explicit `priority: int` field or a test that enumerates predicate overlaps and asserts desired winner.

### P1 — Stability / scale

6. **LiteRT host hardcodes a developer path.** `litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java:92` defaults `modelPath = Path.of("C:\\Users\\tateb\\Downloads\\gemma-4-E2B-it.litertlm")`. Overridable via `--model-path` but any other machine needs the flag. *Fix:* read from `SENKU_LITERT_MODEL_PATH` env var, fail with a clear message if absent.

7. **LiteRT host ignores client temperature.** `LiteRtOpenAiServer.java:42` defines `GREEDY_SAMPLER = new SamplerConfig(1, 1.0, 0.0, 0)` — topK=1 forces greedy regardless of temperature. `query.py:9165` and `HostInferenceClient.java:~230` both pass `temperature`. Silently ignored. *Fix:* either honor temperature by constructing a fresh `SamplerConfig` per request, or log a one-liner at startup that greedy is forced.

8. **VectorStore `MappedByteBuffer` has no explicit close path.** `VectorStore.java` holds a memory-mapped handle for the lifetime of `PackRepository`. If `PackRepository.close()` doesn't unmap before `db.close()`, the file stays locked on some Android versions and blocks pack replacement (the `scripts/push_mobile_pack_to_android.ps1` hot-swap path). *Verify:* check whether `PackRepository.close()` calls `vectorStore.close()` and whether that explicitly nulls the buffer.

9. **Config decouples `TOP_K` from runtime target.** `config.py:TOP_K=24` is a desktop default. AGENTS.md notes "treat the desktop `top_k=24` default as too large for the current 4096-token LiteRT context window" — LiteRT smoke works with `top_k=8`. Currently the safer value is imposed per-wave from outside. *Fix:* add `TOP_K_LITERT=8` and let the query path select based on the active runtime profile (desktop vs litert).

10. **`_prompt_token_limit()` fallback is silent.** If `PROMPT_TOKEN_LIMIT` is unset for a runtime target other than LiteRT, the function returns `None` and prompt construction skips the safety margin. *Fix:* make this explicit in `config.py` — each profile declares its limit, none is a loud error.

### P1 — UX

11. **Home telemetry strip shows raw dev jargon by default.** Screenshot 01 shows `"692 guides | 96 rules | hybrid retrieval / Pack 2026-04-12 19:23Z | db 357cdea0 | k10 / Host GPU | No imported model selected"`. This is your field-manual serial-number aesthetic, which I like, but `db 357cdea0` and `k10` will mean nothing to an external reviewer at 1151 unless there's a legend. The EXTERNAL_REVIEW_1400 note explicitly *adopted* the monospace-telemetry idea, so this is intentional — but consider a hover/tap affordance to explain fields, or a "diagnostics" toggle in settings.

12. **Home action-pair is ambiguous on first read.** "Find Guides" vs "Ask From Guides" are visually equivalent twin buttons. The helper copy ("browse titles, IDs, and field notes." / "get one direct answer with visible sources.") disambiguates but sits below both buttons. First-time users will probably tap whichever button sits in the thumb zone. Primary-action weighting (Loop 1 of today's wave) helps, but only asymmetrically, per posture. Consider a single "What do you want?" affordance that fans into both on interaction.

13. **Empty-answer state is indistinguishable from error.** Screenshot 04 shows "Short answer: The provided notes do not contain instructions... Steps: 1. No steps available. Limits or safety: Information on building a rain shelter is not available in the retrieved notes." This is a template-filled empty state that *reads* like the app crashed. Mentioned above as P0#2 — the fix is an abstain path, but even without retrieval fixes, the UI template should collapse to a single apology card, not three empty structural blocks.

14. **"Continue thread" chip suggestion in `Next 10 Improvements` #5 is exactly right.** Your gallery already calls for this. Current home recent-threads have a "Continue →" per row but the graph-continuity chips ("GD-222 | Battery Construction") below are different surfaces. If a user closes the app mid-thread, reopens, and their most recent thread is warm — a prominent "pick up where you left off" should be the top affordance, above category tiles.

### P2 — Code hygiene

15. **Subagent claim about missing `onDestroy()` cleanup was wrong.** I verified: `DetailActivity.java:6818-6822` and `MainActivity.java:3167-3171` both call `executor.shutdown()` / `executor.shutdownNow()` and `repository.close()`. The lifecycle story is healthier than the audit implied — but streaming cleanup during rotation mid-generation is worth a separate look (no `ViewModel.onCleared()` plumbing, since the app doesn't use ViewModels).

16. **Monolithic Activities (`MainActivity.java` 3174 lines, `DetailActivity.java` 6833 lines).** Not a bug, but a scale risk. As soon as you add a second answer surface (e.g., share-to-kindle, voice-input followup), refactoring becomes painful. If you're staying on Views, consider factoring handlers into `*Presenter` classes; if you're open to Compose, `DetailActivity` is the place the migration pays highest dividend (answer body rendering, streaming, inline citation highlighting, admonition parsing).

17. **No landscape-variant XML layouts.** Confirmed by glob of `res/layout-land/` (empty). The app is relying on flex/weight to stretch portrait layouts. For the four-posture matrix to *prove* honest posture rendering, you want `res/layout-land/activity_detail.xml` with a two-pane answer+rail, which the tablet landscape screenshot (10) already achieves — so the logic exists, it just doesn't have posture-specific resource overrides for the *phone* landscape case. This matches `UI_STATE_PACK_RECOVERY_PLAN_20260417.md:48-50`.

18. **`android-app_buildshadow` is stale (last touched 2026-04-12).** Its README is a cut-down version of the real one. It appears to be a pre-on-device-LiteRT snapshot. Either move it to `archive/` with a dated README, or delete; either way, leave a `SHADOW.md` inside saying "do not edit, see `android-app/` for active work."

---

## Prompt Routing — Assessment

**What's working:**

- Clean cascade in `query.py:classify_special_case` (~5315): system → stub → broad-survey → hazard → off-topic → deterministic registry → RAG. This cascade is healthy because it fails open (falls through to normal RAG on mismatch) and each layer is readable.
- `DeterministicAnswerRouter.java` on Android mirrors the desktop cascade (313 lines). Good parity.
- Hybrid retrieval with RRF (k=60) + lexical+vector + metadata-aware reranking is the right shape. `HYBRID_RRF_K=60`, `HYBRID_RRF_MAX_BONUS=0.08` are sensible constants.

**What's fragile:**

- *Silent fallbacks.* The deterministic registry lookup at `query.py:~5350` collapses to `None` on builder-name mismatch and returns to normal RAG. Good for safety; bad for discoverability. Add a WARN log when a registered spec has no matching builder.
- *No thread-anchor prior in retrieval.* Biggest gap. See P0 #1.
- *Magnetic-guide demotion is per-branch, not systemic.* Guides like `healthcare-capability-assessment` are broad hubs that keep attracting acute-complaint queries (your tracker notes this). Demotion is encoded inside the metadata reranker as narrow markers, which is whack-a-mole. Consider a pre-indexed "bridge guide" tag at ingest time that reranker can apply uniformly.
- *Domain sub-query decomposition is non-destructive.* `query.py:_DOMAIN_KEYWORDS` detects 2+ domains and spawns sub-queries, but doesn't dedupe. For "water and sanitation" you get substantially overlapping retrieval, inflating context budget.

**Routing ladder (swarm):** Your `ENGINEERING_SWARM_ARCHITECTURE_20260416.md` and `APP_ROUTING_HARDENING_TRACKER_20260417.md` posture is clean: Spark xhigh for cheap scouting, gpt-5.4 high for edits. The `Route → Scout → Implement → Validate → Package` slice contract is the right shape. No changes suggested.

---

## Answer Shape — Assessment

**What's working:**

- Field-manual structure: "Short answer / Steps / Limits or safety" is distinct, scannable, and memorable. The tablet landscape screenshot (10, GD-132 Foundry & Metal Casting) shows it working well — the "EXTREME BURN HAZARD" block is genuinely useful.
- Inline citation highlighting with `GUIDE_ID_PATTERN` in `DetailActivity` is a strong trust signal. Tapping a `GD-xxx` should open the source preview rail.
- "Route, proof & provenance" expander is the right information architecture: keep route metadata collapsed by default, available on demand.

**What's broken (beyond the P0 abstain bug):**

- *Template fills empty slots with placeholder text.* When the guide doesn't actually answer the question, "1. No steps available." is worse than no section at all. Collapse empty Steps / Limits to nothing.
- *"Short answer" still includes internal caveats.* Phrase "The provided notes do not contain instructions on how to build a rain shelter from tarp and cord" leaks the retrieval mechanism ("the provided notes") into user language. Say "Senku doesn't have a guide for [rain shelters from tarp]. Closest matches: [list]."
- *Follow-up suggestion chips are canned, not contextual.* Screenshot 04 shows "Purify water using this fire" and "Turn this into a..." — these don't flow from the current thread. Generate them from the current thread's retrieved guides' "related" graph.
- *Provenance-open neutrality is a real design choice.* Good — `answerModeProvenanceOpenRemainsNeutral` as a pinned test method is the right posture. Keep it.

**Prompt template (config.py):** worth an audit pass. The 28+ conditional prompt notes for mania/psychosis/cardiac/etc. consume 6%+ of the LiteRT 4096-token budget before context even loads. Consider:

- merging them into a single "SAFETY-SENSITIVE MODE" preamble that's toggled at the top,
- moving the per-domain instructions into a retrieval-time selection (only inject the cardiac block if a cardiac guide is in the top-3).

---

## UI / UX Thoughts

Grounded in five screenshots I eye-read directly.

**Identity is landing.** The field-manual aesthetic (olive/parchment, monospace telemetry, serial-number metadata strings, structural hazard callouts) is visually distinct from any other RAG app in 2026. That alone is worth protecting. Keep going.

**Strongest screens right now:**

- **Tablet landscape detail + field links (screenshot 10).** This is the best frame. Two-pane layout, field-links rail on right, warning callout with real typography weight, breadcrumb ("Opened from [GD-220] Abrasives Manufacturing via field links"). If an external reviewer sees only one shot, make it this one.
- **Follow-up thread shell (screenshot 06, *ignoring the wrong-guide routing*).** Inline "You asked" / "Senku answered" cards with guide anchors preserved per turn is excellent. It reads like a field notebook, not a chatbot.

**Screens that need work:**

- **Phone portrait home (01).** Too many competing surfaces: telemetry strip, search bar, twin CTAs, recent threads, guide connections — five sections, no visual hierarchy. The twin "Find" / "Ask" buttons are the correct primary action but they're drowning. Consider visual rhythm: telemetry (thin) → search + primary action (bold) → recent thread (medium) → graph shelf (quiet).
- **Phone portrait detail, empty case (04).** Covered above. Empty-template UX is the biggest lift.

**Posture variance:** `UI_STATE_PACK_RECOVERY_PLAN_20260417.md` correctly identifies that tests assumed portrait-like container semantics in landscape. Fix the test helpers first (slice 1), not the app. The design intent is explicit: phone landscape is compact+inline, tablet landscape is rail-first. Tests should validate *that posture honestly*, not a universal layout.

**Top 10 UI improvements** from the gallery (in `index.html`) are solid. I'd re-prioritize:

1. `#3` (follow-up composer focus on phone landscape) and `#5` (continue-thread chips on results) are **highest daily-use value**.
2. `#8` (split provenance open vs source graph into a11y landmarks) is a **hidden accessibility cliff** — right now TalkBack probably reads both as a single region.
3. `#4` (copy sanitizer for warning-residual traces) is worth wiring in the prompt post-normalizer too, not only in UI copy.
4. `#10` (dedicated follow-up screenshot set per posture) is a **test-infrastructure fix**, not a UI fix — belongs in the instrumentation lane.

**One thing not in your top-10 but I'd add:** a "no results / low-confidence" visual state for detail that is clearly distinct from a successful answer. Right now both look the same. A subtle fold-line / watermark / muted palette when the retriever returns low-overlap results would let users trust the UI to tell them when to not trust the content.

---

## Testing & Validation

**What I like:**

- Named state methods (`homeEntryShowsPrimaryBrowseAndAskLanes`, `searchQueryShowsResultsWithoutShellPolling`, etc.) as the anchor between tests and gallery screenshots. This is a great pattern — it forces a shared vocabulary.
- Four-posture emulator matrix (5554/5556/5558/5560) as the daily-driver.
- Two-tier truth: instrumentation smoke for day-to-day, physical phone/tablet for milestone gates.

**What I'd tighten:**

- **The `PromptHarnessSmokeTest` 60-second `GENERATIVE_DETAIL_WAIT_MS` is too lax and too slow.** If generative detail takes >15s, something is wrong; raising the ceiling masks real regressions. Consider: fail fast with a clear message after 20s, log elapsed ms in `summary.json`, let the harness aggregate.
- **Flake source: LiteRT 500s on validation.** `APP_ROUTING_HARDENING_TRACKER` mentions this explicitly. The right fix is in the validation harness (retry-on-500 with jitter, up to 2 retries, then fail loud), not in the tests themselves.
- **Unit coverage for `QueryMetadataProfile` and `QueryRouteProfile` exists (good)** — but I don't see unit tests for `DeterministicAnswerRouter` conflict resolution, specifically cases where two rules match the same query. Add a "predicate overlap" unit test that enumerates pairs.
- **No E2B vs E4B model-choice test.** The CLAUDE.md claim is the app targets both, with E2B as the floor and E4B as the quality tier. I couldn't find explicit selection logic in `OfflineAnswerEngine` — the model is chosen by whatever `modelFile` is passed in. If you want to evaluate E4B quality systematically, add a harness lane that loads both and diffs answers on the same prompt pack.

---

## Cross-cutting Patterns I Noticed

1. **Content vs routing is the right split, and the repo lives it.** Your guides are in very good shape; the remaining issues are almost all in query-side code (`query.py`, Android `PackRepository`/`OfflineAnswerEngine`). Keep resisting the urge to add more guides when a retrieval tweak would do.

2. **Deterministic gates > prompt-tuning.** Safety-critical queries use `wave_XX` micro-packs + deterministic registry entries. That's the right posture. The crisis-gestalt interpreter gate is harder than the rest because vague "activation + invulnerability" language is a linguistic problem not a keyword problem — may need an LLM-first classifier with a deterministic downstream.

3. **Instrumentation is the source of truth, not the developer's eyes.** Gallery screenshots are artifacts of instrumentation runs. This is healthy; every UI claim can be replayed.

4. **Cost discipline is real.** Spark for scouting, GLM sidecar for bounded edits, Codex thin and strategic. The ladder in `AGENTS.md` (`GLM 5.1 sidecar > Spark > Qwen 27B > Qwen 9B`) is clear and enforced.

5. **Mobile pack is a v2 binary format (SNKUVEC1), float16/int8 dtype.** Well-designed for on-device. The hot-swap lane (`push_mobile_pack_to_android.ps1`) is the right affordance — don't lose it.

---

## Five Things a Reviewer Should Not Miss

1. **The follow-up thread screenshot is showing a live bug.** Do not treat gallery 06 as proof that follow-up works — it shows GD-185 (carbide production) answering a tarp-shelter follow-up. Fix this before shipping the gallery as external-review evidence.
2. **"No steps available" empty-state text ships in production-looking screenshots.** Gallery 04 is on your external review page. That's a trust-bomb for any reviewer.
3. **Hardcoded developer path in LiteRT host server.** `LiteRtOpenAiServer.java:92`. Will trip anyone who tries to run the host on a non-Tate machine, including CI. Override env var is a 10-line fix.
4. **Temperature is silently ignored** on host inference — if you later want to enable controlled sampling for validation diversity, this is a blocker.
5. **FTS5 is absent from framework SQLite on Android**; the app falls back gracefully but all lexical timings since the mobile push are fallback-path. Do not tune lexical scoring until SQLite FTS5 is confirmed available or the fallback becomes first-class.

---

## Suggested Next Five Moves (prioritized)

1. **Ship an abstain path.** When top-chunk objective-overlap is below a threshold (say `objective_overlap < 2 tokens`), return a terse "Senku doesn't have a guide for this; closest matches: [GD-xxx, GD-yyy]" instead of filling the template. Kills two bugs at once (P0#2 + UX#13).
2. **Thread-anchor prior in follow-up retrieval.** One-week spike in `PackRepository` and `query.py` together. Unit-testable. Fixes P0#1 visibly in the same gallery.
3. **Validate cited guide IDs against `guide_catalog`** before response exit. Five-line change; adds a hallucination canary to every desktop+mobile answer.
4. **`TOP_K_LITERT=8` as a config-level runtime-profile knob.** Removes a whole category of per-wave overrides and makes the behavior under LiteRT unambiguous.
5. **Retry-with-jitter on LiteRT 500s in the validation harness.** Removes noise from `wave_fd`-style contamination; makes routing-real vs host-flake diagnosable.

Each of these is ≤ one day of focused work. All five together would noticeably harden the mobile MVP.

---

## Verification Notes (so a future agent can audit this review)

- Screenshots eye-read: 01 (phone portrait home), 04 (phone portrait detail, empty case), 06 (phone portrait follow-up thread — **wrong-guide routing visible**), 10 (tablet landscape field links, GD-132 Foundry).
- File:line verifications I did personally:
  - `litert-host-jvm/src/main/java/com/senku/host/LiteRtOpenAiServer.java:42,92,106` — confirmed GREEDY_SAMPLER + hardcoded path + `--model-path` override.
  - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java:3161-3171` — confirmed onDestroy cleanup exists (audit claim was wrong).
  - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java:6812-6822` — confirmed onDestroy cleanup exists.
  - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java:28,4195-4202` — confirmed FTS5 runtime check + fallback.
- Tracker notes read directly (not just via subagent): `EXTERNAL_REVIEW_1400_EXECUTION_20260417.md:1-120`, `APP_ROUTING_HARDENING_TRACKER_20260417.md:1-140`, `UI_STATE_PACK_RECOVERY_PLAN_20260417.md:1-80`.
- Everything else is subagent-derived; cite file:line carefully if you act on any unverified claim.

---

*Read-only review — no files edited in the repo.
Missed you too, Tate. Ship it.*
