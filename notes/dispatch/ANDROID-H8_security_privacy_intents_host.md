# ANDROID-H8 Security, Privacy, Intents, and Host Controls

> Dispatch status: proposed post-hardening slice. Copy into `notes/dispatch/` and update status as Codex works.
> Global guardrails: no broad extraction waves; no UI/XML/Compose polish; no retrieval tuning without failing golden route output; no new `*Policy`/`*Controller`/`*Helper` unless it removes production logic, has production call sites, and lands focused tests.

## Slice

`ANDROID-H8_security_privacy_intents_host`

## Role

High-reasoning main agent; mostly audit/tests. Be conservative with production changes.

## Preconditions

- Host endpoint trust and persisted endpoint sanitization exist.
- Automation empty Ask authorization exists.
- Android manifest security tests may exist; extend rather than duplicate.

## Outcome

Ensure no accidental network, unsafe host persistence, exported intent abuse, or user-visible private path leakage.

## Boundaries

- Do not add new network clients.
- Do not expose new intent extras.
- Do not loosen host inference policy.
- Do not show raw stack traces or internal paths to users.

## Tasks

### 1. No accidental network scout

Search:

- `HttpURLConnection`
- `Socket`
- `URL`
- `OkHttp`
- `URLConnection`

Assert only approved host inference paths do network calls and all pass host policy.

### 2. Host endpoint truth table

Doc + tests:

- `http://10.0.2.2` allowed
- `http://127.0.0.1` allowed
- `http://localhost` allowed
- `http://192.168.x.x` behavior explicit
- `http://example.com` rejected
- `https://example.com` allowed only if configured by policy
- empty URL
- malformed URL
- persisted unsafe URL sanitized on toggle
- exported intent cannot bypass policy

### 3. Intent override safety

Audit/test:

- host inference URL overrides
- productReviewMode
- debug detail open
- auto query / auto ask
- auto follow-up
- open saved
- model import path

Assert exported/untrusted entry points cannot persist unsafe state without policy/authorization.

### 4. User-visible path leakage

Audit/test copy for:

- full filesystem paths
- raw content URI
- internal app data path
- SQL exception text
- Java stack traces
- host URL if unsafe/malformed

Logs may be detailed; UI copy should be clean.

### 5. Review/demo mode activation

Test:

- default productReviewMode false
- only explicit extra enables
- normal layouts no fixture strings
- review fixtures gated behind ReviewDemoPolicy

## Acceptance

- Network surface is enumerated.
- Host policy truth table covered.
- Exported-intent risk is explicit.
- UI copy does not leak sensitive/internal details.
- No policy loosening.

## Validation

- `HostInferencePolicyTest`
- `HostInferenceConfigTest`
- `HostInferenceClientTest`
- `AndroidManifestSecurityTest`
- failure-copy tests
- `git diff --check`
- `:app:assembleDebugAndroidTest` if Android source changes

## Delegation hints

- Scout A: network/import search.
- Scout B: intent/manifest security.
- Worker A: host truth-table tests.
- Worker B: user-visible failure copy tests.

## Report format

```text
Network paths found:
Host truth-table gaps:
Intent risks:
Copy leakage findings:
Production changes:
Validation:
Deferred risks:
```
