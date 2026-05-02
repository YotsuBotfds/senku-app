# Android Audit 3 - Release Security and Build Hardening

## Slice

`ANDROID-AUDIT3` - plan release security/build hardening without destabilizing the current debug/proof harness.

## Role

Read-only release-hardening scout. Implementation only for low-risk tests/docs unless explicitly approved.

## Preconditions

- Current head is clean.
- Read:
  - `AndroidManifest.xml`
  - `network_security_config.xml`
  - `app/build.gradle`
  - `HostInferenceConfig.java`
  - `HostInferencePolicy.java`
  - `AndroidManifestSecurityTest.java`
  - latest host endpoint commits/tests.

## Outcome

Create a release-hardening checklist that distinguishes debug/harness allowances from production behavior.

## Findings being folded in

External audits flagged:
- Cleartext HTTP is allowed for `10.0.2.2`, `127.0.0.1`, and `localhost`.
- Release `minifyEnabled false`.
- Debug/automation intent extras exist in production code.
- AppCompat theme / Material3 migration and Kotlin migration are broader modernization topics.

Current context:
- Host endpoint policy has been hardened, including unsafe persisted endpoint sanitization.
- Network security config cleartext is local-only, but production release should still make this explicit.
- Automation/review intents have been tightened, but intent/export boundaries deserve an RC audit.

## Tasks

1. Build a release security matrix:
   - debug/dev behavior
   - product/release behavior
   - current tests
   - remaining risk
2. Network config plan:
   - keep local cleartext for debug/harness if needed.
   - consider release resource overlay with TLS-only network config.
   - verify host inference policy still blocks unsafe endpoints even if config allows local cleartext.
3. Intent security audit:
   - exported activities.
   - debug detail extras.
   - auto query / auto ask.
   - productReviewMode.
   - host override extras.
   - confirm unauthorized intents cannot change sensitive state.
4. Release build plan:
   - assess R8/minify feasibility.
   - identify required keep rules for LiteRT, Compose, JSON, AndroidX tests.
   - do not enable minify in this slice unless explicitly approved.
5. Modernization deferrals:
   - Material3 theme migration.
   - Kotlin migration.
   - Toast-to-Snackbar migration.
   - Gradle caching/parallel flags.

## Boundaries

Global guardrails for this audit-derived slice:
- Start read-only; patch only verified current-head behavior.
- Do not launch broad extraction waves.
- Do not touch visual polish unless the slice explicitly says accessibility/semantics.
- Do not tune retrieval.
- Prefer focused behavior tests over source-string tests.
- Bundle any tracker update with the code/test slice.
- Stop after one small commit for this slice and report validation.


Do not break debug/local host inference harness. Do not enable minification until a dedicated release build proof exists.

## Acceptance

- A release-hardening checklist or doc exists.
- Any new tests are focused on manifest/intent/host policy.
- No release config behavior changes unless separately approved.
- `AndroidManifestSecurityTest` and host policy/config tests pass if touched.
- `git diff --check` passes.

## Delegation hints

- Use a high-reasoning security scout for intent/export boundaries.
- Use context7/Android docs for network security config and manifest behavior.

## Report format

```text
Current head:
Release hardening findings:
Immediate safe tests/docs added:
Deferred release build changes:
Validation:
Working tree:
```
