# Senku Mobile Android App -- Comprehensive Audit

Historical note: This historical audit was relocated from the repo root into `notes/reviews/` in D13. Read [`AUDITGLM_VERIFICATION_20260413.md`](./AUDITGLM_VERIFICATION_20260413.md) alongside it for verification and reconciliation context.

**Date:** 2026-04-13
**Auditor:** GLM-5.1
**Scope:** `android-app/` full source, build config, tests, resources
**Source files audited:** 20 Java source, 10 test files, 2 Gradle scripts, 1 manifest, 3 layouts, 4 resource XMLs, 1 ProGuard rules file

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Critical Bugs](#2-critical-bugs)
3. [High-Severity Issues](#3-high-severity-issues)
4. [Medium-Severity Issues](#4-medium-severity-issues)
5. [Low-Severity Issues](#5-low-severity-issues)
6. [Performance Bottlenecks](#6-performance-bottlenecks)
7. [Architecture & Design Issues](#7-architecture--design-issues)
8. [Test Suite Audit](#8-test-suite-audit)
9. [Build Configuration Audit](#9-build-configuration-audit)
10. [Resource & Accessibility Audit](#10-resource--accessibility-audit)
11. [Security Audit](#11-security-audit)
12. [File-by-File Findings](#12-file-by-file-findings)
13. [Prioritized Remediation Roadmap](#13-prioritized-remediation-roadmap)

---

## 1. Executive Summary

The Senku Mobile app is an offline-first survival guide retrieval and answer-generation system built in pure Java for Android 8.0+. The codebase is functionally sound with strong retrieval logic and domain-specific routing, but carries several infrastructure and robustness gaps that warrant attention before production use.

**Key metrics:**

| Metric | Value |
|---|---|
| Source files | 20 Java classes |
| Total source lines | ~10,200 |
| Largest file | `PackRepository.java` (~3,600 lines) |
| Test files | 10 |
| Test lines | ~6,800 |
| Classes with zero test coverage | 10 of 20 |
| Critical bugs | 3 |
| High-severity issues | 5 |
| Medium-severity issues | 14 |
| Low-severity issues | 11 |

---

## 2. Critical Bugs

### 2.1 LiteRtModelRunner: Unsynchronized `generate()` Allows Null Engine Crash

**File:** `LiteRtModelRunner.java:49`
**Severity:** CRITICAL

`ensureEngine()` is synchronized but `generate()` is not. Between `ensureEngine()` returning and actual engine usage, another thread can call `close()` (e.g., from model import), nulling the `engine` field and causing a `NullPointerException` during inference.

```
Thread A: ensureEngine() -> engine is valid
Thread B: close() -> engine = null
Thread A: engine.generate(...) -> NPE crash
```

**Fix:** Synchronize `generate()` or use a `ReadWriteLock` to allow concurrent reads but exclusive writes.

### 2.2 MainActivity: Cross-Thread Data Race on `repository` Field

**File:** `MainActivity.java:57, 192, 212, 259, 400`
**Severity:** CRITICAL

`repository` is assigned on the executor thread (via `runOnUiThread`) and read on the UI thread in `runSearch()`, `runAsk()`, and other callbacks. Without `volatile` or `AtomicReference`, the Java Memory Model does not guarantee the UI thread sees the latest write. On some devices/architectures, the UI thread can see a stale `null` and crash with NPE.

**Fix:** Declare `repository` as `volatile` or wrap in `AtomicReference<PackRepository>`.

### 2.3 DetailActivity: Same Cross-Thread Race on `repository`

**File:** `DetailActivity.java:66, 334-341`
**Severity:** CRITICAL

Identical pattern to 2.2. `ensureRepository()` is called on the executor thread (line 307, 505) and the resulting assignment is not published safely to the UI thread. The `repository` field is read on the executor thread in subsequent calls, but the initial lazy-init race with itself is a hazard if follow-ups overlap.

**Fix:** Same as 2.2 -- use `volatile` or `AtomicReference`.

---

## 3. High-Severity Issues

### 3.1 PackRepository: `subList()` Returns Unstable View

**File:** `PackRepository.java:306`
**Severity:** HIGH

`routeResults.subList(0, Math.min(limit, routeResults.size()))` returns a view of the original list. If the caller modifies the returned list, or if `routeResults` is later modified, a `ConcurrentModificationException` or undefined behavior results.

**Fix:** Wrap with `new ArrayList<>(routeResults.subList(...))`.

### 3.2 ModelFileStore: Hardcoded Filesystem Paths

**File:** `ModelFileStore.java:134, 138`
**Severity:** HIGH

Hardcoded paths like `/storage/emulated/0/Android/data/...` and `/sdcard/Android/data/...` will fail on:
- Devices with scoped storage (Android 10+)
- Devices with non-standard mount points
- Devices where the path doesn't exist

**Fix:** Use `context.getExternalFilesDir()` exclusively and remove hardcoded paths.

### 3.3 HostInferenceConfig: URL Normalization Produces Double `/v1`

**File:** `HostInferenceConfig.java:79`
**Severity:** HIGH

The normalization logic checks `if (!normalized.toLowerCase(Locale.US).endsWith("/v1"))` and appends `/v1`. If a user enters `http://10.0.2.2:1235/v1/chat/completions`, the result becomes `http://10.0.2.2:1235/v1/chat/completions/v1` -- a double `/v1` suffix that causes host inference calls to 404.

**Fix:** Parse the path properly. Strip trailing slashes, strip `/v1` if present, then append `/v1`. Handle paths containing `/v1` in the middle.

### 3.4 PackRepository: Dead Code -- Duplicate `guide-focus` Branch

**File:** `PackRepository.java:601`
**Severity:** HIGH (logic correctness)

Line 599 already handles `"guide-focus"`. Line 601 checks `"lexical" || "guide-focus"` -- the `"guide-focus"` part is unreachable dead code. If the intent was to handle a different case, this is a logic bug.

**Fix:** Remove the duplicate `"guide-focus"` from line 601 or verify the intended logic.

### 3.5 HostInferenceClient: Raw Socket HTTP with No TLS

**File:** `HostInferenceClient.java:80`
**Severity:** HIGH (security)

The client opens a raw TCP socket and writes HTTP/1.1 manually. There is no HTTPS/TLS support. While the AGENTS.md mentions emulator use at `http://10.0.2.2:1235`, the client also works on arbitrary URLs entered by the user. Any non-localhost URL transmits API keys and model prompts in plaintext.

**Fix:** Add an HTTPS path using `HttpsURLConnection` or `OkHttp`. At minimum, warn the user when a non-local URL is configured.

---

## 4. Medium-Severity Issues

### 4.1 MainActivity: Double Search on Enter Key

**File:** `MainActivity.java:127-141`
**Severity:** MEDIUM

Both `setOnEditorActionListener` (IME_ACTION_SEARCH) and `setOnKeyListener` (KEYCODE_ENTER) trigger `runSearch()`. On some devices and input methods, pressing Enter fires both listeners, causing a double search.

**Fix:** Remove `setOnKeyListener` or add a debounce flag.

### 4.2 SearchResultAdapter: `ContextCompat.getColor()` Called Per `getView()`

**File:** `SearchResultAdapter.java:100-129`
**Severity:** MEDIUM

Color lookups via `ContextCompat.getColor()` are performed on every `getView()` invocation for every visible item. Colors should be resolved once and cached.

**Fix:** Cache colors in fields initialized in the constructor.

### 4.3 SearchResultAdapter: No-Op String Replace

**File:** `SearchResultAdapter.java:97`
**Severity:** MEDIUM

`safe(result.subtitle).replace(" | ", " | ")` replaces `" | "` with `" | "` -- this is an identity operation, either a bug or leftover from a formatting change.

**Fix:** Remove or fix to the intended replacement.

### 4.4 SessionMemory: Memory Bloat from Full SearchResult Storage

**File:** `SessionMemory.java:38, 57-60`
**Severity:** MEDIUM

Each `Turn` stores the full `List<SearchResult>` including body text. With `MAX_TURNS = 6` and up to 75 results per turn, this holds 450 `SearchResult` objects with full body text in memory indefinitely.

**Fix:** Store only summarized references (guideId, title, snippet) in older turns. Keep full results only for the most recent turn.

### 4.5 ChatSessionStore: Unbounded Conversation Map

**File:** `ChatSessionStore.java:8`
**Severity:** MEDIUM

`CONVERSATIONS` is a `LinkedHashMap` with access-order eviction but no size limit. Each `createConversation()` adds a new `SessionMemory` that is never removed. Over a long session, memory grows without bound.

**Fix:** Add a maximum capacity (e.g., 10 conversations) and evict the oldest when exceeded.

### 4.6 PackRepository: Brute-Force LIKE Queries Cannot Use Indexes

**File:** `PackRepository.java:1291-1297`
**Severity:** MEDIUM

`searchWithKeywordHits` constructs queries with `%token%` LIKE patterns across 5 columns per token. Leading wildcards prevent SQLite from using indexes, resulting in full table scans.

**Fix:** This is the known FTS5 fallback (AGENTS.md notes `fts.unavailable` / `no such module: fts5`). Ensure FTS5 is available in the SQLite build or use trigram indexes.

### 4.7 QueryRouteProfile: Inline `buildSet()` Creates Throwaway Sets

**File:** `QueryRouteProfile.java:1315-1319, 1331-1335, 1349-1355`
**Severity:** MEDIUM

Detection methods like `hasExplicitWaterDistributionFocus` call `buildSet(...)` on every invocation, creating short-lived sets. These should be `static final` fields.

**Fix:** Move inline set construction to static final fields.

### 4.8 QueryMetadataProfile: `String.matches()` Recompiles Regex Each Call

**File:** `QueryMetadataProfile.java:1641-1643`
**Severity:** MEDIUM

`normalized.matches(...)` compiles a regex on every call. This is called during query classification on every user query.

**Fix:** Pre-compile regex patterns as `static final Pattern` fields.

### 4.9 PackInstaller: No Checksum Validation After Asset Copy

**File:** `PackInstaller.java:53-54, 89`
**Severity:** MEDIUM

After copying assets from APK to internal storage, there is no checksum verification. A partial write or corrupted asset would silently produce a broken installation.

**Fix:** Compare SHA-256 of the installed file against a known hash stored in the manifest.

### 4.10 OfflineAnswerEngine: Boxed `Integer` for Constant

**File:** `OfflineAnswerEngine.java:20`
**Severity:** MEDIUM (code quality)

`private static final Integer MODEL_MAX_TOKENS` uses boxed `Integer` instead of primitive `int`, causing unnecessary auto-boxing.

**Fix:** Change to `private static final int`.

### 4.11 PackManifest: Silent Default on Missing Fields

**File:** `PackManifest.java:57`
**Severity:** MEDIUM

All fields use `opt*` methods, which silently return defaults for missing JSON keys. A corrupted or incomplete manifest will load with zeroed fields instead of throwing a descriptive error.

**Fix:** Use `get*` for required fields and catch `JSONException` with a descriptive message.

### 4.12 DetailActivity: `Exception ignored` in Source Navigation

**File:** `DetailActivity.java:510`
**Severity:** MEDIUM

`catch (Exception ignored) {}` silently swallows all errors during guide loading. Database corruption, I/O errors, and OOM are all hidden.

**Fix:** Log the exception at minimum. Surface a user-facing error if the guide cannot be loaded.

### 4.13 PackInstaller: Unused Import

**File:** `PackInstaller.java:5`
**Severity:** MEDIUM (code hygiene)

`import android.text.TextUtils` is unused.

**Fix:** Remove the unused import.

### 4.14 Duplicate `safe()` Methods Across Files

**Files:** `MainActivity.java`, `DetailActivity.java`, `PromptBuilder.java`, `AnswerContextSelector.java`, `QueryRouteProfile.java`, `QueryMetadataProfile.java`, `DeterministicAnswerRouter.java`, `HostInferenceClient.java`
**Severity:** MEDIUM (code duplication)

Eight classes each define their own `private static String safe(String)` method with identical logic.

**Fix:** Extract to a shared `StringUtils.safe()` or similar utility class.

---

## 5. Low-Severity Issues

### 5.1 SearchResult: `Serializable` Instead of `Parcelable`

**File:** `SearchResult.java:6`
**Severity:** LOW

`Serializable` uses reflection and is slower than `Parcelable` for Android IPC. Since these are passed via `Intent` extras, `Parcelable` would be more efficient.

### 5.2 SearchResult: Missing `equals()`/`hashCode()`/`toString()`

**File:** `SearchResult.java`
**Severity:** LOW

The class is used in `Set.contains()` via `SearchResultAdapter` and in `LinkedHashMap`. While `contains` in `AnswerContextSelector.addRemaining` uses object identity, this is fragile.

### 5.3 Duplicate Stop Token Sets

**Files:** `OfflineAnswerEngine.java:21-29`, `SessionMemory.java`, `AnswerContextSelector.java:16`
**Severity:** LOW

Stop tokens are defined independently in three places. Divergence is likely.

**Fix:** Extract to a shared constants class.

### 5.4 `strings.xml`: Hardcoded Guide Count

**File:** `app/src/main/res/values/strings.xml`
**Severity:** LOW

`search_hint` says "Search 692 guides..." with a hardcoded count that will become stale.

**Fix:** Use a dynamic placeholder or generic "Search guides...".

### 5.5 ProGuard Rules: Placeholder Comment Only

**File:** `app/proguard-rules.pro`
**Severity:** LOW

Contains only `# Debug-first project; keep defaults minimal for now.` -- no actual rules defined.

### 5.6 VectorStore: `MappedByteBuffer` Unmapping

**File:** `VectorStore.java:22-24`
**Severity:** LOW

`MappedByteBuffer` is notoriously difficult to unmap in Java. Closing the channel does not release mapped memory. On Android, GC eventually reclaims it, but large files may cause memory pressure.

### 5.7 PackRepository: `addRemaining` Uses `O(n)` Contains Check

**File:** `AnswerContextSelector.java:180`
**Severity:** LOW

`selected.contains(candidate)` is O(n) per call since `ArrayList.contains` is linear. With up to 75 results, this is O(n^2). Use a `LinkedHashSet` for `selected` or a separate seen-set.

### 5.8 LiteRtModelRunner: Old Cache Directories Never Cleaned

**File:** `LiteRtModelRunner.java:170-181`
**Severity:** LOW

Each model+maxTokens combination creates a new cache directory. Old directories accumulate.

### 5.9 PackInstaller: Unreachable `lastError == null` Branch

**File:** `PackInstaller.java:111`
**Severity:** LOW

In the retry loop, `lastError` is always set in the catch block. The `lastError == null` check at the throw site is unreachable.

### 5.10 DeterministicAnswerRouter: Only 10 Rules for 96 Claimed

**File:** `DeterministicAnswerRouter.java`
**Severity:** LOW

AGENTS.md claims 96 deterministic rules, but the Java class only implements 10. The remaining 86 appear to live in the desktop Python codebase and have not been ported to mobile.

### 5.11 HostInferenceClient: 15-Minute Read Timeout with No Cancellation

**File:** `HostInferenceClient.java:22`
**Severity:** LOW

`READ_TIMEOUT_MS = 15 * 60 * 1000` blocks the executor thread for up to 15 minutes. There is no cancellation mechanism if the user navigates away.

---

## 6. Performance Bottlenecks

### 6.1 Vector Search: Brute-Force O(n*d) Scan

**File:** `VectorStore.java:93-113`
**Impact:** HIGH -- dominant latency for every query

Every vector search performs a full brute-force dot product against all vectors. With 10k rows at 384 dimensions, this is ~3.8M multiply-add operations taking 50-200ms on mobile CPU.

**Recommendation:** Implement approximate nearest-neighbor search (HNSW or IVF). Even a simple random projection LSH would cut latency significantly.

### 6.2 Vector Dot Product: Per-Element Bounds Checking

**File:** `VectorStore.java:118-129`
**Impact:** MEDIUM

For float16 vectors, each element requires `buffer.getShort()` (bounds-checked) + `Half.toFloat()` conversion. Bulk operations via `ShortBuffer` would be faster.

### 6.3 PromptBuilder: Sequential Regex Passes

**File:** `PromptBuilder.java:199-211`
**Impact:** LOW-MEDIUM

`normalizeExcerpt` applies 10+ regex replacements sequentially. Each is O(n) on the text length. Combining into fewer passes would help for long excerpts.

### 6.4 Single-Thread Executor for All Background Work

**Files:** `MainActivity.java:38`, `DetailActivity.java:39`
**Impact:** LOW-MEDIUM

Both activities use a single-thread executor. If a search is running and the user triggers another operation (reinstall, import, follow-up), the new task must wait for the current one to complete.

**Recommendation:** Use a bounded thread pool (2-3 threads) with priority queuing.

### 6.5 SearchResultAdapter: ListView Instead of RecyclerView

**File:** `SearchResultAdapter.java:16`
**Impact:** LOW

`ArrayAdapter` with `ListView` is the legacy pattern. `RecyclerView` provides better performance through enforced ViewHolder pattern and layout prefetching.

---

## 7. Architecture & Design Issues

### 7.1 PackRepository: God Class at 3,600 Lines

`PackRepository.java` handles query parsing, FTS construction, keyword search, vector retrieval, RRF fusion, metadata scoring, answer context building, and route-focused search. This should be decomposed into:

- `SearchEngine` -- orchestrates retrieval
- `RouteSearcher` -- route-specific query handling
- `AnswerContextBuilder` -- context selection
- `Reranker` -- RRF fusion and metadata scoring
- `MetadataScorer` -- bonus calculation

### 7.2 No Dependency Injection

All classes are instantiated directly. No IoC container, no Dagger/Hilt, no manual DI. This makes testing harder and couples components tightly.

### 7.3 No ViewModel or LiveData

Activities manage state directly. Configuration changes (rotation) will lose in-progress operations and state. The `SessionMemory` survives via `ChatSessionStore` (static), but `PackRepository` is re-created on each Activity recreation.

### 7.4 Static Mutable State in LiteRtModelRunner

`engine`, `loadedModelPath`, `loadedMaxTokens`, `loadedBackendLabel` are static mutable fields. This prevents multiple instances and makes testing impossible. Should be instance-based with singleton management at the Application level.

### 7.5 No Offline Caching Strategy for Host Inference Results

When host inference is available, results are not cached locally. If the host goes offline, previously answered queries cannot be replayed from cache.

### 7.6 SearchResult: Telescoping Constructor Pattern

`SearchResult` has multiple constructors with default parameters. A builder pattern would be more maintainable as fields are added.

### 7.7 Raw HTTP Client Instead of OkHttp/HttpURLConnection

`HostInferenceClient` implements HTTP/1.1 from scratch using raw sockets. This misses:
- Connection pooling
- TLS/HTTPS
- Redirect handling
- Chunked transfer encoding
- Compression

---

## 8. Test Suite Audit

### 8.1 Coverage Summary

| Production Class | Has Tests? | Coverage |
|---|---|---|
| `QueryRouteProfile` | Yes (41 methods) | Deep |
| `PackRepository` | Yes (~110 methods) | Deep |
| `SessionMemory` | Yes (22 methods) | Good |
| `QueryMetadataProfile` | Yes (~55 methods) | Deep |
| `OfflineAnswerEngine` | Yes (16 methods) | Good |
| `PromptBuilder` | Yes (8 methods) | Moderate |
| `DeterministicAnswerRouter` | Yes (4 methods) | Light |
| `HostInferenceClient` | Yes (2 methods) | Light |
| `HostInferenceConfig` | Yes (3 methods) | Light |
| `ChatSessionStore` | Yes (2 methods) | Light |
| `MainActivity` | **No** | 0% |
| `DetailActivity` | **No** | 0% |
| `PackInstaller` | **No** | 0% |
| `LiteRtModelRunner` | **No** | 0% |
| `SearchResultAdapter` | **No** | 0% |
| `AnswerContextSelector` | **No** | 0% |
| `ModelFileStore` | **No** | 0% |
| `VectorStore` | **No** | 0% |
| `PackManifest` | **No** | 0% |
| `SearchResult` | **No** | Implicit |

**10 of 20 classes have zero dedicated test coverage.**

### 8.2 Test Bug: Missing `@Test` Annotation

**File:** `QueryMetadataProfileTest.java:348`

`waterStorageContinuationPrefersRotationOverHistoricalContext` is missing the `@Test` annotation and will never be executed by JUnit.

### 8.3 Test File Bloat

- `PackRepositoryTest.java`: 2,860 lines, ~110 test methods -- should be split into domain-focused test classes
- `QueryMetadataProfileTest.java`: 1,249 lines

### 8.4 Missing Test Scenarios

1. Error/exception paths (null inputs, empty strings, malformed data)
2. Concurrency tests for `ChatSessionStore`
3. `DeterministicAnswerRouter` covers only 10 of 96+ rules
4. No tests for `HostInferenceClient` error handling (timeouts, malformed JSON, empty choices)
5. No instrumented tests for Activities
6. No performance benchmarks

### 8.5 Fragile Assertion Patterns

Many tests assert on specific numeric score thresholds (e.g., `assertTrue(bonus >= 30)`). These are regression guards that break when scoring is tuned, not specification tests. Consider testing relative ordering instead of absolute values.

---

## 9. Build Configuration Audit

### 9.1 R8/ProGuard Disabled for Release

**File:** `app/build.gradle:19`

`minifyEnabled false` means release builds include:
- No code shrinking (larger APK)
- No obfuscation (security risk)
- No optimization passes

### 9.2 No Signing Configuration

Release builds have no `signingConfigs` block. Only debug builds can be generated.

### 9.3 Outdated Dependencies

| Dependency | Current | Latest |
|---|---|---|
| `appcompat` | 1.6.1 | 1.7.x |
| `activity` | 1.8.2 | 1.9.x |
| `litertlm-android` | 0.10.0 | Preview/alpha, no stability guarantee |
| JUnit | 4.13.2 | JUnit 5 |
| AGP | 8.2.1 | 8.4+ |

### 9.4 Missing Build Optimizations

- No Gradle configuration cache (`org.gradle.configuration-cache=true`)
- No parallel execution (`org.gradle.parallel=true`)
- No build cache (`org.gradle.caching=true`)
- No version catalog (`libs.versions.toml`)
- No `buildFeatures { viewBinding true }`

### 9.5 Missing `noCompress` for LiteRT Model Files

The `noCompress` list includes `sqlite3`, `f16`, `i8`, `json` but not `.litertlm` or `.task` model files. These may need to be uncompressed for LiteRT to load efficiently.

### 9.6 No Lint Configuration

No `lintOptions` or `lint` block. Default lint runs without baseline files or severity overrides.

### 9.7 Cleartext Traffic Enabled

**File:** `AndroidManifest.xml:11`

`android:usesCleartextTraffic="true"` allows HTTP traffic globally. This is intentional for emulator host inference but should be restricted to specific domains via `network_security_config.xml`.

---

## 10. Resource & Accessibility Audit

### 10.1 Accessibility Failures

| Issue | Impact |
|---|---|
| No `contentDescription` on any interactive/decorative view | Screen readers cannot describe UI |
| No `importantForAccessibility="no"` on decorative elements | Noise in screen reader output |
| No `accessibilityLiveRegion` on status text | Status changes not announced |
| No `labelFor` on labels paired with EditText inputs | Screen readers cannot associate labels |
| Color-only category badges | Color-blind users cannot distinguish categories |
| Missing `autofillHints` on EditText inputs | Reduces accessibility convenience |

### 10.2 Layout Issues

- Deeply nested `LinearLayouts` (4+ levels) -- causes repeated measure/layout passes
- Fixed `132dp` height on session panel may clip on small screens
- `ListView` is legacy; should use `RecyclerView`
- No landscape layouts (`layout-land/`)
- No tablet layouts (`layout-sw600dp/`)
- No night/dark mode resources (`values-night/`)

### 10.3 Theme Inconsistency

Theme parent is `Theme.AppCompat.Light.DarkActionBar` (light system UI), but the app's visual design is entirely dark (olive/charcoal backgrounds). System dialogs, switches, and checkboxes may render in light style against dark content.

---

## 11. Security Audit

### 11.1 No TLS for Host Inference

`HostInferenceClient` uses raw TCP sockets with no HTTPS. Prompts and responses transmit in plaintext.

### 11.2 Cleartext Traffic Globally Enabled

`android:usesCleartextTraffic="true`" affects all network traffic, not just the emulator host.

### 11.3 No Input Sanitization on Search Queries

User input is passed directly into SQL LIKE clauses and FTS queries. While `PackRepository` uses parameterized queries for LIKE, the FTS match syntax is built via string concatenation and could be vulnerable to FTS injection.

### 11.4 Model Files Not Integrity-Checked

Imported model files have no signature or hash verification. A tampered model file could produce misleading or harmful survival advice.

### 11.5 No Certificate Pinning

Even if HTTPS were added, there is no certificate pinning for the host inference endpoint.

---

## 12. File-by-File Findings

### `MainActivity.java` (~675 lines)
- **[CRITICAL]** Data race on `repository` field (line 57, 192, 212, 259)
- **[MEDIUM]** Double search on Enter key (lines 127-141)
- **[LOW]** `NumberFormat.getNumberInstance(Locale.US)` re-created each call (line 496)
- **[LOW]** `SEARCH_RESULT_LIMIT = 75` hardcoded (line 35)
- **[LOW]** Malformed log string with unclosed quote (line 549)

### `DetailActivity.java` (~562 lines)
- **[CRITICAL]** Data race on `repository` field (line 66, 334-341)
- **[MEDIUM]** `catch (Exception ignored) {}` swallows all errors (line 510)
- **[LOW]** `ExecutorService` not `shutdownNow()` in `onDestroy()` (line 554, uses `shutdown()`)

### `PackRepository.java` (~3,600 lines)
- **[HIGH]** `subList()` returns unstable view (line 306)
- **[HIGH]** Dead code: duplicate `guide-focus` branch (line 601)
- **[MEDIUM]** Brute-force LIKE queries (lines 1291-1297)
- **[ARCH]** God class -- needs decomposition
- **[ARCH]** ~100 lines of hardcoded marker sets should be in config

### `PackInstaller.java` (~196 lines)
- **[MEDIUM]** No checksum validation after asset copy (lines 53-54, 89)
- **[LOW]** Unused import `TextUtils` (line 5)
- **[LOW]** Unreachable `lastError == null` branch (line 111)

### `OfflineAnswerEngine.java` (~622 lines)
- **[MEDIUM]** Boxed `Integer MODEL_MAX_TOKENS` (line 20)
- **[LOW]** Duplicate stop token sets (lines 21-29)

### `PromptBuilder.java` (~551 lines)
- **[LOW]** Sequential regex passes in `normalizeExcerpt` (lines 199-211)
- **[LOW]** Duplicate `safe()` method

### `SessionMemory.java` (~990 lines)
- **[MEDIUM]** Memory bloat from full SearchResult storage (line 38)
- **[LOW]** `ArrayList.remove(0)` is O(n) (acceptable with MAX_TURNS=6)

### `QueryRouteProfile.java` (~1,612 lines)
- **[MEDIUM]** Inline `buildSet()` creates throwaway sets (lines 1315-1355)
- **[ARCH]** 500+ lines of static marker sets should be externalized

### `QueryMetadataProfile.java` (~1,651 lines)
- **[MEDIUM]** `String.matches()` recompiles regex each call (lines 1641-1643)
- **[LOW]** Duplicate marker definitions with `QueryRouteProfile`

### `SearchResult.java` (~65 lines)
- **[LOW]** `Serializable` instead of `Parcelable`
- **[LOW]** Missing `equals()`/`hashCode()`/`toString()`

### `SearchResultAdapter.java` (~161 lines)
- **[MEDIUM]** `ContextCompat.getColor()` per `getView()` (lines 100-129)
- **[MEDIUM]** No-op string replace (line 97)
- **[LOW]** Uses `ListView` instead of `RecyclerView`

### `LiteRtModelRunner.java` (~205 lines)
- **[CRITICAL]** Unsynchronized `generate()` (line 49)
- **[LOW]** Static mutable state (lines 24-26)
- **[LOW]** Old cache directories never cleaned (lines 170-181)

### `ModelFileStore.java` (~176 lines)
- **[HIGH]** Hardcoded filesystem paths (lines 134, 138)

### `VectorStore.java` (~177 lines)
- **[PERF]** Brute-force O(n*d) vector search (lines 93-113)
- **[LOW]** `MappedByteBuffer` unmap difficulty (lines 22-24)

### `PackManifest.java` (~84 lines)
- **[MEDIUM]** Silent defaults on missing fields (line 57)

### `HostInferenceConfig.java` (~119 lines)
- **[HIGH]** URL normalization double `/v1` (line 79)

### `HostInferenceClient.java` (~279 lines)
- **[HIGH]** Raw socket HTTP with no TLS (line 80)
- **[LOW]** 15-minute timeout with no cancellation (line 22)

### `DeterministicAnswerRouter.java` (~302 lines)
- **[LOW]** Only 10 of 96+ claimed rules implemented

### `ChatSessionStore.java` (~39 lines)
- **[MEDIUM]** Unbounded conversation map (line 8)

### `AnswerContextSelector.java` (~421 lines)
- **[LOW]** `selected.contains()` is O(n) per call in `addRemaining` (line 180)

### `AndroidManifest.xml` (34 lines)
- **[SEC]** Cleartext traffic globally enabled (line 11)
- **[NOTE]** `INTERNET` permission declared but not needed for pure offline use

### `app/build.gradle`
- **[HIGH]** `minifyEnabled false` for release
- **[HIGH]** No signing configuration
- **[NOTE]** Missing `.litertlm`/`.task` in `noCompress`

### `proguard-rules.pro`
- **[LOW]** Contains only a comment, no actual rules

---

## 13. Prioritized Remediation Roadmap

### Phase 1 -- Critical Fixes (Immediate)

| # | Issue | File | Effort |
|---|---|---|---|
| 1 | Synchronize `LiteRtModelRunner.generate()` | `LiteRtModelRunner.java` | Small |
| 2 | Make `repository` volatile in `MainActivity` | `MainActivity.java` | Small |
| 3 | Make `repository` volatile in `DetailActivity` | `DetailActivity.java` | Small |

### Phase 2 -- High-Impact Fixes (This Week)

| # | Issue | File | Effort |
|---|---|---|---|
| 4 | Fix `subList()` view instability | `PackRepository.java` | Small |
| 5 | Fix URL normalization double `/v1` | `HostInferenceConfig.java` | Small |
| 6 | Remove hardcoded filesystem paths | `ModelFileStore.java` | Small |
| 7 | Remove dead duplicate `guide-focus` branch | `PackRepository.java` | Small |
| 8 | Add missing `@Test` annotation | `QueryMetadataProfileTest.java` | Trivial |
| 9 | Enable `minifyEnabled true` for release | `app/build.gradle` | Medium |
| 10 | Add release signing config | `app/build.gradle` | Medium |

### Phase 3 -- Medium-Impact Fixes (This Sprint)

| # | Issue | File | Effort |
|---|---|---|---|
| 11 | Remove double Enter listener / add debounce | `MainActivity.java` | Small |
| 12 | Cache color lookups in adapter constructor | `SearchResultAdapter.java` | Small |
| 13 | Fix no-op string replace | `SearchResultAdapter.java` | Trivial |
| 14 | Add `volatile` to `installedPack` in `MainActivity` | `MainActivity.java` | Trivial |
| 15 | Log exceptions in `DetailActivity.openSourceGuide` | `DetailActivity.java` | Small |
| 16 | Cap `ChatSessionStore` map size | `ChatSessionStore.java` | Small |
| 17 | Reduce `SessionMemory` memory footprint | `SessionMemory.java` | Medium |
| 18 | Validate manifest required fields | `PackManifest.java` | Small |
| 19 | Move inline `buildSet` calls to static fields | `QueryRouteProfile.java` | Small |
| 20 | Pre-compile regex patterns | `QueryMetadataProfile.java` | Small |
| 21 | Extract shared `safe()` utility | Multiple | Medium |
| 22 | Remove unused `TextUtils` import | `PackInstaller.java` | Trivial |
| 23 | Add `.litertlm`/`.task` to `noCompress` | `app/build.gradle` | Trivial |
| 24 | Restrict cleartext traffic to emulator host via `network_security_config.xml` | `AndroidManifest.xml` | Small |

### Phase 4 -- Performance Improvements (Next Sprint)

| # | Issue | Effort |
|---|---|---|
| 25 | Implement approximate NN search in `VectorStore` | Large |
| 26 | Use `ShortBuffer` bulk ops for float16 dot product | Medium |
| 27 | Combine sequential regex passes in `PromptBuilder` | Small |
| 28 | Use bounded thread pool instead of single-thread executor | Medium |
| 29 | Enable Gradle configuration cache, parallel, build cache | Small |
| 30 | Update dependencies (`appcompat`, `activity`, AGP) | Medium |

### Phase 5 -- Architecture Improvements (Ongoing)

| # | Issue | Effort |
|---|---|---|
| 31 | Decompose `PackRepository` into 4-5 focused classes | Large |
| 32 | Introduce `ViewModel` + `LiveData` for Activity state | Large |
| 33 | Convert `LiteRtModelRunner` to instance-based with Application-level singleton | Medium |
| 34 | Replace raw socket HTTP with `OkHttp`/`HttpURLConnection` | Medium |
| 35 | Migrate `ListView` to `RecyclerView` | Medium |
| 36 | Add dependency injection (Dagger/Hilt or manual) | Large |
| 37 | Extract marker sets and route config to JSON/YAML files | Large |
| 38 | Implement `Parcelable` on `SearchResult` | Small |
| 39 | Add accessibility attributes to all layouts | Medium |

### Phase 6 -- Test Coverage Expansion (Ongoing)

| # | Issue | Effort |
|---|---|---|
| 40 | Add tests for `VectorStore`, `PackManifest`, `PackInstaller` | Medium |
| 41 | Add tests for `ModelFileStore`, `LiteRtModelRunner` | Medium |
| 42 | Add tests for `AnswerContextSelector` | Medium |
| 43 | Add tests for `HostInferenceClient` error paths | Small |
| 44 | Split `PackRepositoryTest` into domain-focused test classes | Medium |
| 45 | Add instrumented/Robolectric tests for Activities | Large |
| 46 | Port remaining 86 deterministic rules to mobile + test | Large |
| 47 | Add `network_security_config.xml` to restrict cleartext traffic | Small |

---

*End of audit.*
