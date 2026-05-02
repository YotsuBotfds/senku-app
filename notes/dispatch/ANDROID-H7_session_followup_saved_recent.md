# ANDROID-H7 Session, Follow-Up, Saved, and Recent State

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H7_session_followup_saved_recent`

## Role

Main agent plus one session-memory worker and one saved/recent worker. Keep app flow stable.

## Preconditions

- Shared Search/Ask stale gate is in production.
- Follow-up generation harness token leak is fixed.
- Saved/Home async failure containment exists.

## Outcome

Ensure conversation memory, follow-up drafts, saved guides, and recent-thread previews do not leak stale state, lose user drafts, or publish corrupt pack data.

## Boundaries

- Do not change retrieval scoring.
- Do not touch UI visual polish.
- Avoid MainActivity edits unless fixing concrete stale/async/failure behavior.

## Tasks

### 1. Follow-up anchor matrix

Test:

- what about rain?
- can I do this without rope?
- next step
- what if I have no tarp?
- switch to water storage
- forget that / new topic

Assert:

- anchor retained for true follow-up
- anchor cleared for new topic
- retrieval plan uses source context appropriately
- stale guide/source does not bleed after topic switch

### 2. Follow-up draft safety matrix

Test:

- success clears only submitted draft
- failure preserves visible draft
- newer draft typed while old generation completes is preserved
- retry uses submitted query, not current field text
- busy duplicate retry blocked
- IME submit and visible send share same path
- empty follow-up blocked

### 3. Session command isolation

Test:

- clear chat
- reset
- new topic
- show history

Assert:

- commands do not become retrieval queries
- stale async invalidated
- route state restored
- session panel updates

### 4. Thread/source provenance across turns

Scenario:

- turn 1 uses guide A
- turn 2 follow-up uses guide A
- turn 3 switches to guide B

Assert guide A no longer appears as selected/active source after turn 3.

### 5. Saved guide matrix

Review/add tests for:

- save guide
- unsave guide
- resave moves to top
- cap at 12
- duplicate normalized ID
- blank ID dropped
- missing guide dropped
- saved search routes to Search, not Ask
- saved open/back restores route state
- saved list does not save answer threads or per-turn thread state

### 6. Recent-thread preview lifecycle

Test:

- new conversation created
- answer added updates last activity
- follow-up updates preview
- clear chat resets preview
- deleted/missing conversation skipped
- same timestamp deterministic order
- review placeholder gated

## Acceptance

- Follow-up/session/saved/recent matrices cover stale and failure cases.
- No user draft loss in tested scenarios.
- Missing saved/recent data fails closed.
- No route/retrieval tuning.

## Validation

- `SessionMemoryTest`
- `ChatSessionStoreTest`
- `FollowUpComposerControllerTest`
- `DetailFollowup*` tests
- `SavedGuidesPolicyTest`
- `PinnedGuideStoreTest`
- relevant Main controller tests
- `:app:assembleDebugAndroidTest` if Android source touched
- `git diff --check`

## Delegation hints

- Worker A: follow-up/session tests.
- Worker B: saved/recent tests.
- Main agent: integrate only real failure fixes.

## Report format

```text
Follow-up cases:
Session cases:
Saved cases:
Recent cases:
Production behavior changed:
Validation:
Deferred cases:
```
