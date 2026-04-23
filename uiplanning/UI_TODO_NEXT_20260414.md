# Senku UI Todo - Next Train (Post Streaming Hardening)

Current live note:
- the checklist below is materially complete as of `2026-04-15`
- fresh next-step backlog now lives in `uiplanning/UI_TODO_POST_20260415.md`

## P0 - Immediate

- [x] Status copy normalization + mojibake cleanup
  - Scope:
    - `android-app/app/src/main/res/values/strings.xml`
    - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
  - Outcome:
    - Streaming ellipsis and retrieval/generation copy are normalized.
    - Completion copy is consistent and field-readable.

- [x] Add explicit retry action on generation failure in detail view
  - Why: failure currently explains what broke but does not offer one-tap recovery.
  - Scope:
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - On failure body/status, show `Retry` action that reruns the last query.
    - Keep source chips visible while failed state is shown.

- [x] Fix validation orientation integrity for portrait/landscape screenshot packs
  - Why: recent review found the "landscape" artifact was still portrait-sized, so the validation bundle can mislead us.
  - Scope:
    - `scripts/run_android_ui_validation_pack.ps1`
    - any future orientation-specific capture helper
  - Acceptance:
    - landscape captures force rotation before screenshot
    - screenshot dimensions are verified
    - the run fails if landscape still captures portrait-sized output
  - Completed:
    - phone landscape now retries display rotation and falls back to a temporary `wm size` landscape override when the device refuses to honor rotation during long generated runs
    - validation pack re-ran successfully on `RFCX607ZM8L` and `R92X51AG48D` with `4 / 4` pass in `artifacts/ui_validation_landscape_smoke_final/20260414_215534`

## P1 - High Value UX

- [x] Promote “home escape hatch” in answer/detail posture
  - Why: field users should always have a clear path back to search/home without relying on back-stack quirks.
  - Scope:
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/res/layout-land/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - Add compact `Home` affordance in top rail.
    - Works in phone/tablet portrait+landscape.

- [x] Stabilize harness completion confidence with “consecutive success” hold on detail surface
  - Why: reduce false-complete captures on preview text under slow generation.
  - Scope:
    - `scripts/run_android_prompt.ps1`
  - Acceptance:
    - Detail completion requires stable signal over 2 polls.
    - Terminal failures still short-circuit immediately.

- [x] Gate or fix the FTS / lexical retrieval warnings
  - Why: recurring `fts5` / `lexical_chunks_fts4` warnings suggest the lexical path is misconfigured or being probed unnecessarily.
  - Scope:
    - retrieval/index startup path
    - any query path that probes unavailable FTS modules
  - Acceptance:
    - warnings stop appearing on every run, or the schema is created consistently
    - fallback behavior is explicit and logged once
  - Completed:
    - `PackRepository.java` now checks SQLite compile options and `sqlite_master` before using FTS tables
    - runtime emits a single debug fallback line instead of warning spam when FTS5/FTS4 are unavailable

- [x] Add overflow affordance to horizontal chip rows
  - Why: inline source/material chip rails are clipped at the edge and do not obviously read as scrollable.
  - Scope:
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - chip rails show a clearer affordance for hidden items
    - no regression to existing horizontal scroll behavior
  - Completed:
    - added horizontal fade edges to chip rails in phone and tablet detail layouts

## P1 - Detail/Streaming Polish

- [x] DetailActivity streaming throttle + first-token fade
  - Why: token-by-token UI updates can feel jumpy; first-token fade-in smooths perceived start of answer rendering.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - UI streaming refresh is throttled with interval constants for body updates and scroll follow.
    - First streamed content appears with a short alpha-in fade.
    - Empty first chunk does not force body churn.

- [x] Compact answer section-label spacing in formatter path
  - Why: section labels such as `Short answer:` and `Steps:` should read with cleaner spacing in answer mode.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - `formatAnswerBody()` applies compact label spacing.
    - Known answer section labels preserve readable separation without adding layout bloat.

- [x] Reduce phone top-bar crowding on narrow widths
  - Why: Back, Home, title, meta, and anchor compete for the same line on compact phones.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/layout/activity_detail.xml`
  - Acceptance:
    - title remains readable longer on narrow phones
    - trust cues stay present but stop fighting each other for the first row
  - Completed:
    - compact portrait phones now hide the redundant Home pill and keep the back long-press escape hatch
    - fresh portrait phone validation shows the title row breathing more cleanly

- [x] Compact tablet portrait composer after the first answer render
  - Why: tablet portrait still spends too much space on the expanded follow-up composer once the thread is already established.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - tablet portrait uses a semi-compact composer after the first answer
    - expanded composer remains for onboarding/empty-thread states
  - Completed:
    - tablet portrait now joins the compact follow-up mode once an answer is rendered and generation is no longer pending
    - fresh tablet portrait validation shows the answer area winning back height while keeping the composer usable

- [x] Trim phone portrait chip padding for answer-first density
  - Why: the compact phone portrait answer stack still gives too much vertical room to source/material/next-step chips once the hero is gone.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - inline chips and helper rails use slightly tighter vertical padding on compact phone portrait only
    - answer body starts a bit earlier without removing trust cues
  - Completed:
    - reduced compact phone portrait chip padding in the inline source, next-step, and material rows
    - portrait validation passed on both physical devices in `artifacts/ui_validation_phone_compact_trim/20260414_224950`

- [x] Trim compact phone portrait question/answer bubble spacing
  - Why: even after the chip trim, the question/answer bubble stack still held a little too much vertical space before the answer body started.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Acceptance:
    - compact phone portrait bubbles get slightly tighter padding and spacing
    - the answer body moves up without breaking the overall field-card feel
  - Completed:
    - compact portrait now uses a tighter bubble padding and reduced answer top margin
    - portrait validation passed in `artifacts/ui_validation_phone_bubble_compact/20260414_225321`

- [x] Spark-ready UI validation pack published and documented
  - Why: Spark-facing UI slices need a consistent deterministic+generative smoke lane with artifact logs.
  - Scope:
    - `scripts/run_android_ui_validation_pack.ps1`
    - `AGENTS.md`
    - `TESTING_METHODOLOGY.md`
  - Acceptance:
    - command executes deterministic + generative cases per device
    - per-case `dump.xml`, `logcat.txt`, `screen.png`
    - summary exports include `summary.json` and `summary.csv`

## P2 - Tablet Depth

- [x] Provenance rail interaction polish (tablet station)
  - Why: source verification should feel first-class, not a dense static block.
  - Scope:
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/drawable/bg_source_link.xml`
    - `android-app/app/src/main/res/drawable/bg_source_link_active.xml`
    - `android-app/app/src/main/res/drawable/bg_sources_stack_shell.xml`
    - `android-app/app/src/main/res/drawable/bg_provenance_panel.xml`
  - Acceptance:
    - Selecting a source chip clearly updates preview focus with stronger visual state.
    - No corner/background spill in landscape rails.

- [x] Tighten tablet landscape provenance shell padding/clipping
  - Why: the review caught green source/provenance backgrounds visually overrunning the rounded shell on the right edge in landscape.
  - Scope:
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
  - Acceptance:
    - source/provenance cards stay visibly inset from the shell edge
    - rounded corners read cleanly in tablet landscape captures
  - Completed:
    - increased tablet landscape source/provenance shell padding
    - added `clipToPadding` in the landscape shell
    - validated with fresh landscape screenshots in `artifacts/ui_validation_landscape_spillcheck/20260414_215941`

- [x] Tablet landscape “station” screenshot pack refresh
  - Why: recent hardening changed completion/failure states; screenshots should match current behavior.
  - Scope:
    - `artifacts/tablet_physical_validation_20260414/*` (new snapshot set)
  - Acceptance:
    - deterministic top/mid/deep
    - streaming top/mid/deep
    - failure-state sample with clear terminal message
  - Completed in `artifacts/ui_validation/20260414_204635`:
    - `summary.json` recorded 4/4 pass across both devices and both case types
    - `summary.csv` emitted with per-case status rows

## P1 - Next Queue (Post 21:22 Checkpoint)

- [x] Low-relevance generative guardrail for off-topic retrieval contexts
  - Why: generative runs can still return cleanly formatted but semantically irrelevant answers when retrieved guides are mismatched (example anchored on `GD-162` for shelter query).
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    - `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/values/strings.xml`
  - Acceptance:
    - when context relevance is weak, route/meta now surfaces `Low coverage` with limited-evidence signaling
    - low-coverage body language is detected and no longer framed as a normal AI-answer route

- [x] Refresh docs from the current build before the next review cycle
  - Why: the audit currently lags the implementation in places like hero collapse and low-coverage signaling.
  - Scope:
    - `../notes/reviews/UI_DIRECTION_AUDIT_20260414.md`
    - `uiplanning/IMPLEMENTATION_LOG_20260413.md`
    - `uiplanning/UI_TODO_NEXT_20260414.md`
  - Acceptance:
    - audit recommendations match current build state
    - no stale "next slice" items remain in the live todo
  - Completed:
    - audit refreshed with a current-build delta note
    - live todo now matches the latest validation posture and keeps the remaining hero/why-panel work clearly separated

- [x] Phone portrait visual compactness pass for answer-first posture
  - Why: fallback text is now cleaner, but above-the-fold density can still bury initial actionable lines in some query paths.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/layout/activity_detail.xml` (only if needed)
  - Acceptance:
    - first actionable sentence appears higher without removing trust spine
    - no regression to source/provenance discoverability
  - Completed:
    - answer scroll target now lands on the answer bubble first instead of the text body
    - compact portrait answer spacing trims the answer stack and lifts the visible content a bit higher
    - validated twice on both physical devices in `artifacts/ui_validation_phone_answer_first_batch/20260414_225824` and `artifacts/ui_validation_phone_answer_first_batch_2/20260414_230125`

- [x] Harness replay pack refresh after sidecar-result parser fix
  - Why: sidecar consumption behavior changed; keep orchestration docs and smoke artifacts aligned.
  - Scope:
    - `scripts/get_opencode_sidecar_result.ps1`
    - `scripts/opencode_sidecar_common.ps1`
    - `notes/AGENT_MANAGEMENT_WORKFLOW_20260414.md`
  - Acceptance:
    - `sidecar_smoke_ready` task returns non-empty `text` through `get_opencode_sidecar_result.ps1`
    - docs mention `value` payload compatibility and preview fallback behavior
  - Completed:
    - `get_opencode_sidecar_result.ps1` now reads completed results through the OpenCode `value` payload path and falls back to the preview text when needed
    - `opencode_sidecar_common.ps1` now matches the same payload contract
    - the workflow note now reflects the completed sidecar replay behavior instead of leaving it as a pending harness cleanup item

- [x] Tablet portrait support-chrome compaction
  - Why: portrait tablets were still spending too much vertical room on the lower support rails even after the answer surface was tightened.
  - Scope:
    - `android-app/app/src/main/res/layout-sw600dp-port/activity_detail.xml`
  - Acceptance:
    - lower support panels and follow-up shell get a little less vertical padding/margin
    - answer column remains the visual priority in tablet portrait
  - Completed:
    - reduced tablet portrait support-rail padding/margins in the main layout
    - validated on both physical tablet and tablet emulator in `artifacts/ui_validation_tablet_portrait_density_batch_fixed3/20260414_233620`

- [x] Strengthen collapsed "Why this answer" affordance on compact phone portrait
  - Why: the collapsed trust drawer was already working, but it still read too quietly for stress use.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/values/strings.xml`
    - `android-app/app/src/main/res/drawable/bg_why_toggle_compact.xml`
  - Acceptance:
    - compact portrait uses proof-specific toggle copy
    - collapsed why row reads more like a trust surface than a generic expander
  - Completed:
    - compact phone and tablet portrait now say `Why this answer | Show proof` / `Hide proof`
    - the compact toggle shell got a slightly stronger accent bar and contrast
    - phone portrait smoke passed in `artifacts/ui_validation_why_toggle_proof/20260414_234130`
    - tablet portrait smoke passed in `artifacts/ui_validation_why_toggle_tablet/20260414_234326`

- [x] Surface the proof title in phone landscape
  - Why: landscape phone already had a tappable proof row, but the title was hidden, so the trust surface read too quietly.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
  - Completed:
    - phone landscape now keeps the `Why this answer` title visible instead of hiding it
    - compact proof summaries now say `Proof lead:` so the trust row reads more explicitly
    - validated on phone landscape in `artifacts/ui_validation_phone_landscape_proof/20260415_062750` with a clear visible proof anchor on the successful deterministic run

- [x] Reassert foreground before final validation capture
  - Why: the physical phone generated landscape run could slip to the launcher right before the final dump/screenshot, which made the artifact look like a UI failure when the answer had already completed.
  - Scope:
    - `scripts/run_android_prompt.ps1`
  - Completed:
    - validation now does a quick foreground reassertion before final dump/screenshot capture
    - the fixed phone landscape pack now passes `4 / 4` in `artifacts/ui_validation_phone_landscape_proof_fixed/20260415_084047`

- [x] Trim phone landscape hero/status chrome
  - Why: the landscape phone generation shell was still spending a bit too much vertical space on the hero/status card while the answer was building.
  - Scope:
    - `android-app/app/src/main/res/layout-land/activity_detail.xml`
  - Completed:
    - slightly reduced the landscape hero padding and top margins
    - the generated phone landscape capture now keeps more room for the answer surface while still showing the build-progress signal
    - validated in `artifacts/ui_validation_phone_landscape_hero_trim/20260415_084402`

## Next UI Queue - Post Completion

### P0 - Field Resilience

- [x] Slow/weak answer resilience UI
  - Why: the current UI can still feel ambiguous when retrieval is ready but the answer is slow, weak, or still building on-device.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/OfflineAnswerEngine.java`
    - `android-app/app/src/main/res/values/strings.xml`
    - optional supporting layouts if a compact status row is needed
  - Acceptance:
    - show `Sources ready` quickly while source cards stay visible
    - keep a compact `Building answer from these guides...` status during generation
    - after a stall threshold, show a recovery-oriented message such as `Still building. You can inspect sources now.`
    - expose compact actions like `Open best guide` and `Retry`
    - low-coverage or fallback answers get a stronger verification posture instead of a quiet meta label
  - Validation:
    - one deterministic fast path
    - one slow/weak retrieval path
    - one low-coverage fallback path
  - Completed:
    - sources-ready copy now appears in the busy state while guide chips stay visible
    - the hero/status path now promotes a stall recovery message and reuses the existing proof/open controls
    - phone smoke passed in `artifacts/ui_validation_p0_p2_phone_smoke/20260415_092006`

### P1 - Provenance Interaction

- [x] Deepen source/proof interaction
  - Why: the trust spine is visible now, but verification should be faster and more obviously interactive.
  - Scope:
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - provenance/source chip drawables and shell backgrounds as needed
  - Acceptance:
    - tapping a source chip clearly highlights the matching preview
    - selected source state is obvious in both phone proof drawer and tablet station rail
    - preview text stays compact and snippet-like rather than dumping raw blocks
    - citation/source navigation remains available during streaming and after completion
  - Validation:
    - tablet landscape source-selection smoke
    - phone portrait proof-drawer smoke
  - Completed:
    - selected source state is now restored after rerenders instead of being lost when the source list redraws
    - provenance preview now keeps the matching source anchor highlighted when it is reopened through the stall path or a chip tap
    - phone smoke passed in `artifacts/ui_validation_p0_p2_phone_smoke/20260415_092006`

### P2 - Severity Mode

- [x] Add subtle high-risk field accents
  - Why: first aid, security, and emergency routes should feel distinct without breaking the olive/parchment visual system.
  - Scope:
    - answer routing / meta labeling
    - high-risk route styling in `DetailActivity.java`
    - optional accent colors in `colors.xml`
  - Acceptance:
    - first aid/security/emergency routes shift to a muted red/orange accent
    - critical actions remain large and easy to hit
    - deterministic emergency answers stay instant-first with no writing animation
    - no haptics unless explicitly approved
  - Validation:
    - one deterministic emergency-style route
    - one normal route to confirm the accent does not leak
  - Completed:
    - high-risk route detection now feeds the accent pass so emergency/first-aid/security answers get a subtle alert-toned treatment without changing the olive baseline
    - the status pill and route chip now reflect the severity accent when the route is high-risk
    - phone smoke passed in `artifacts/ui_validation_p0_p2_phone_smoke/20260415_092006`

### P1 - Follow-Up Transcript Continuity

- [x] Show recent follow-up context as tablet chat bubbles
  - Why: follow-up answers should read like one continuous transcript on tablet instead of forcing the user to open a separate context rail to recover prior turns.
  - Scope:
    - `android-app/app/src/main/res/layout-sw600dp-land/activity_detail.xml`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/values/strings.xml`
  - Completed:
    - tablet landscape now inserts a recent-thread bubble stack above the current question bubble in the left reading column
    - the provenance rail keeps a lightweight thread reminder instead of duplicating the full transcript
    - history bubble offsets are wider in station mode so user and Senku turns read like chat bubbles rather than edge-to-edge cards
    - physical tablet follow-up replay validates the two-turn path with host inference routed through `adb reverse`
    - follow-up completion now keeps that inline transcript in frame by targeting the transcript container during post-answer scroll, validated in `artifacts/live_debug/tablet_anchor_drift_probe_scrollfix.json`
    - phone detail now mirrors that continuity posture with a promoted prior-turn bubble stack above the current Q/A pair, validated in `artifacts/live_debug/followups/phone_thread_promote_20260415c_followup.xml`
    - phone follow-up completion now targets the promoted prior-turn container instead of the current question bubble, so the previous turn stays in frame on completion

## Next UI Queue - Post Guide-State Push

### P0 - Current Pack Truth

- [x] Make home category buckets resilient to new guide taxonomy
  - Why: fresh guide states use categories such as `power-generation`, `biology`, `metalworking`, and `textiles-fiber-arts`, so the home tiles could show `0 guides` for Water or Fire even with relevant guides installed.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
  - Acceptance:
    - Water and Fire tiles no longer show false-empty counts on the physical tablet.
    - Counts are based on stable category/title/topic-tag signals, not broad description text.
    - Category filtering uses the same bucket logic as the visible counts.
  - Completed:
    - physical tablet home now shows non-empty counts for Water, Fire, Food, Tools, and the other current taxonomy buckets after installing the refreshed APK.
    - validation screenshot: `artifacts/live_debug/tablet_category_fix.png`

- [x] Replace hardcoded home guide count with installed-pack count
  - Why: guide states are being pushed frequently, so a fixed `692 guides` subtitle can become stale and undermine trust in the current pack.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `android-app/app/src/main/res/layout/activity_main.xml`
    - `android-app/app/src/main/res/values/strings.xml`
  - Acceptance:
    - Home subtitle reads from the installed guide list or pack manifest instead of a fixed string.
    - Empty/loading state does not show a false count.
    - Counts are formatted for readability and pluralized correctly.
  - Completed:
    - home subtitle now binds to `home_subtitle` and updates through `updateHomeSubtitle(...)` after pack install/status refresh.
    - `home_subtitle_count` plural replaces the stale hardcoded `692 guides` copy.
    - `android-app\\gradlew.bat -p android-app assembleDebug` passed.

- [x] Sticky trust strip with pack freshness
  - Why: field users should never lose route type, anchor guide, source count, evidence strength, or installed-pack freshness while scrolling or following up. This also helps separate stale guide-pack artifacts from true UI failures.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/layout/activity_detail.xml`
    - tablet/land detail layout variants if the strip needs posture-specific placement
    - pack manifest plumbing if freshness is not already available in detail
  - Acceptance:
    - route, evidence, anchor/source count, and pack version/date/hash are visible in answer mode after scroll.
    - phone portrait, phone landscape, tablet portrait, and tablet landscape remain readable.
    - the strip distinguishes current pack state from stale test-artifact retrieval behavior.
  - Completed:
    - the existing sticky detail top rail now satisfies this directly through `detail_screen_meta` plus `detail_anchor_chip`; no extra chrome band was needed.
    - tablet validation: `artifacts/live_debug/tablet_anchor_drift_indicator_followup_answer.xml`
    - phone validation: `artifacts/live_debug/phone_sticky_truth.xml`

- [x] Pack/backend diagnostics as user-visible truth
  - Why: guide states are being pushed frequently, and host inference differs between emulator and physical devices. The UI should expose enough compact status to make stale packs and backend routing obvious during field testing.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/PackManifest.java`
    - host inference config/status code
    - `android-app/app/src/main/res/layout/activity_main.xml`
  - Acceptance:
    - developer/status panel shows manifest guide count, deterministic rule count, pack build/version marker, backend mode, and host URL/reverse state where available.
    - hot-swapping a pack changes the displayed pack marker without requiring a visual guess.
  - Completed:
    - home status now includes pack generated time, DB hash prefix, and mobile top-k next to guide/rule/retrieval/backend state.
    - tablet landscape home variants now expose the shared `home_subtitle` id so the guide-count subtitle updates after pack load.
    - validation screenshot: `artifacts/live_debug/tablet_pack_truth_subtitle.png`

### P1 - Turn-Level Trust

- [x] Follow-up anchor drift indicator
  - Why: tablet transcript bubbles now preserve conversational context, but users also need to know whether the answer stayed anchored to the same guide family or shifted tracks.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`
    - detail layout variants only if a new compact chip is needed
  - Acceptance:
    - each prior/current turn can show a compact anchor/source-family summary.
    - follow-up turns that shift primary anchors get a visible but non-alarmist drift cue.
    - retrieval misses are displayed as evidence/anchor shifts, not hidden as generic chat continuity.
  - Completed:
    - tablet landscape keeps the prior turn inline and now shows the current-turn drift cue directly in the current question bubble.
    - the cue stays compact by reusing the question subtitle slot instead of adding a new badge row.
    - validated in `artifacts/live_debug/tablet_anchor_drift_indicator_followup_answer.xml` where `detail_subtitle` reads `Anchor shift GD-953 -> GD-035`.

- [x] Source rail triage states
  - Why: provenance is first-class now; the next pass should make primary anchor, supporting source, selected preview, retrieval mode, and no-citation states instantly distinguishable.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/res/drawable/bg_source_link.xml`
    - `android-app/app/src/main/res/drawable/bg_source_link_active.xml`
    - `android-app/app/src/main/res/drawable/bg_provenance_panel.xml`
  - Acceptance:
    - primary anchor source is visually distinct from supporting sources.
    - selected source state persists through streaming/follow-up rerenders.
    - no-citation or low-source states are visibly different from normal evidence.
  - Completed:
    - tablet station source rails now label the lead item as `Anchor | ...` and expose supporting sources with distinct accessibility copy.
    - selected preview state continues to persist through rerenders, while the provenance flow now clearly separates preview from `Open full guide`.
    - harness source-probe logic was updated to follow the two-step tablet provenance interaction, and the physical-tablet follow-up replay now ends with `source_link_verified: true` in `artifacts/live_debug/tablet_anchor_followup_probefix2.json`.

### P2 - Field Ergonomics

- [x] High-risk action layout blocks
  - Why: high-risk medical/fire/security answers need more than an accent; they should expose `do first`, `avoid`, and `escalate if` structure when the answer content supports it.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/DeterministicAnswerRouter.java`
    - `android-app/app/src/main/java/com/senku/mobile/PromptBuilder.java`
    - emergency/action drawables as needed
  - Acceptance:
    - high-risk deterministic answers keep instant-first behavior.
    - critical action blocks are scannable without drowning normal low-risk answers in alert styling.
    - no haptics are added without explicit approval.
  - Completed:
    - deterministic high-risk answers now surface `Do first`, `Avoid`, and `Escalate if` blocks above the rest of the answer in `DetailActivity.java`
    - deterministic route detection now keys off `rule_id` too, so the block stack and related trust cues stay active on compact deterministic subtitles
    - extraction now trims `Avoid` and `Escalate if` blocks down to the actual warning/escalation clause when the numbered step also contains setup text
    - physical phone validation passed for puncture and metal-splinter deterministic routes in:
      - `artifacts/live_debug/phone_puncture_action_blocks_harness_fixed.xml`
      - `artifacts/live_debug/phone_metal_splinter_action_blocks_20260415b.xml`
    - no haptics were introduced as part of this slice

- [x] Large-font and small-landscape ergonomics sweep
  - Why: the app is dense now, and field resilience means it still works with dim screens, large font scale, phone landscape, and shaky hands.
  - Scope:
    - `android-app/app/src/main/res/values/dimens.xml` if present or layout-local dimensions
    - `android-app/app/src/main/res/layout*`
    - `android-app/app/src/main/java/com/senku/mobile/SearchResultAdapter.java`
  - Acceptance:
    - no clipped buttons or overlapping text at large font scale.
    - critical controls remain easy to hit.
    - screenshots cover phone portrait, phone landscape, tablet portrait, and tablet landscape.
  - Progress:
    - landed a first partial pass in `DetailActivity.java`, `MainActivity.java`, `SearchResultAdapter.java`, `layout/activity_main.xml`, and `layout-land/activity_main.xml`
    - compact follow-up controls now keep `48dp` touch targets, narrow large-font detail headers collapse secondary meta, and landscape-phone result cards degrade more gracefully
    - physical phone landscape smoke passed in `artifacts/ui_validation_ergonomics_landscape/20260415_184230`
    - explicit large-font validation is now scriptable via `run_android_ui_validation_pack.ps1 -FontScale ...`, with a passing phone portrait smoke at `artifacts/ui_validation_fontscale_smoke/20260415_204730`
    - tablet portrait large-font exposed header crowding, and `DetailActivity.java` now collapses secondary header meta in tablet portrait when `fontScale >= 1.25`
    - large-font smoke now passes across all four posture classes in:
      - `artifacts/ui_validation_fontscale_smoke/20260415_204730`
      - `artifacts/ui_validation_fontscale_phone_landscape/20260415_205243`
      - `artifacts/ui_validation_fontscale_tablet_portrait_fix/20260415_205107`
      - `artifacts/ui_validation_fontscale_tablet_landscape/20260415_205158`

### P3 - Workflow Reach

- [x] Intent-separated home entry lanes
  - Why: the sidecar direction pass called out that home still blends browse, search, ask, session state, and developer controls. A clearer browse/find/ask hierarchy would reduce wrong-lane stress when the user needs either raw guide inspection or a synthesized answer.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `android-app/app/src/main/res/layout/activity_main.xml`
    - tablet/land home layout variants
  - Acceptance:
    - `Find Guides`, category browsing, and `Ask From Guides` read as deliberate lanes.
    - automation extras for search and ask still work.
    - user can recover to home/browse without closing the app.
  - Completed:
    - search copy now reads as guide discovery instead of a blended browse/ask prompt
    - `browse_button` / `ask_button` order is now consistent across phone, landscape, and tablet home layouts
    - phone portrait now shows `Find Guides` alongside `Ask From Guides` with a clearer lane hint in `artifacts/live_debug/phone_home_entry_lanes_20260415.xml`
    - tablet home picked up the same search/lane semantics in `artifacts/live_debug/tablet_home_entry_lanes_20260415.xml`
    - build/install smoke passed on both physical devices after the layout/string refresh
    - results mode now promotes the inline hint into a tappable `back to browse` affordance on phone postures
    - physical phone tap-through validation returned from results to the category browser in `artifacts/live_debug/phone_results_escape_hatch_tap_20260415.xml`
    - physical phone hardware-back validation now also returns from results to the browse/home posture without exiting the app in `artifacts/live_debug/phone_results_back_to_browse_clean_20260415.xml`

- [x] Emergency quick-access pin drawer
  - Why: a field user should be able to pin 3-5 critical guides such as water purification, wound care, or fire starting and reach them in one tap from any posture.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - a small persisted pin store
    - minimal pin drawer or bottom-sheet layout
  - Acceptance:
    - pin/unpin guide from detail.
    - pinned guides survive process death and pack refresh when guide IDs still exist.
    - one-tap access works from home and answer detail.
  - Completed:
    - `DetailActivity` top rails now expose a compact `Pin` / `Saved` action for pinnable guide and answer surfaces
    - `PinnedGuideStore.java` persists guide IDs in SharedPreferences so saved guides survive process death and relaunch
    - home/browse now shows a horizontal `Pinned guides` rail above categories whenever saved guides exist
    - physical phone validation confirmed `senku_pinned_guides.xml`, saved-state relaunch persistence, and rendered quick-access chip recovery in `artifacts/live_debug/phone_home_after_relaunch_with_pin_20260415.xml`

- [x] Session transcript export
  - Why: once a multi-turn field answer is useful, users may need to hand it to someone else. Export should include questions, answers, anchors, and source IDs.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/DetailActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/SessionMemory.java`
    - Android share intent wiring
  - Acceptance:
    - share intent exports a readable multi-turn transcript.
    - source guide IDs and anchor changes are preserved.
    - generated helpers are not presented as citations.
  - Completed:
    - answer-mode top rails now expose a compact `Share` action on phone and tablet detail layouts without adding a new screen
    - `DetailActivity.java` now builds a readable export transcript from recent turns plus the current turn, including anchor / anchor-shift lines and source guide IDs
    - physical phone tap-through opened the Android chooser with the transcript preview in `artifacts/live_debug/phone_detail_share_tap_20260415.xml`
    - answer-state dumps showing the new affordance are captured in `artifacts/live_debug/phone_detail_share_20260415.xml` and `artifacts/live_debug/tablet_detail_share_20260415.xml`

- [x] Search-as-you-type family suggestion chips
  - Why: fast topic-family chips can shorten time to the right guide family without forcing the user into answer generation.
  - Scope:
    - `android-app/app/src/main/java/com/senku/mobile/MainActivity.java`
    - `android-app/app/src/main/java/com/senku/mobile/PackRepository.java`
    - home layout variants
  - Acceptance:
    - partial queries produce compact family suggestions quickly.
    - suggestions degrade gracefully when FTS is unavailable.
    - tapping a suggestion routes to a filtered guide/result lane.
  - Completed:
    - home search now shows a compact horizontal suggestion rail under the search shell after 2+ typed characters
    - MVP uses already-loaded in-memory guides instead of FTS-dependent lookups, so suggestions remain fast even on fallback packs
    - category and guide suggestions route into browse/result lanes without invoking answer generation
    - phone validation confirmed visible chips and tap-through in:
      - `artifacts/live_debug/phone_search_suggestions_fire_trimmed_20260415.xml`
      - `artifacts/live_debug/phone_search_suggestion_tap_fire_category_20260415.xml`
    - tablet landscape validation confirmed the same rail and corrected `Find Guides` browse labeling in:
      - `artifacts/live_debug/tablet_search_suggestions_fire_fixed_20260415.xml`
