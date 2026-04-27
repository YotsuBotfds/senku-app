# RAG-A14f Android Old-Pack Fallback Matrix

Date: 2026-04-26

## Scope

Harden Android reviewed-card runtime compatibility tests for old or partial
mobile-pack shapes. This is test/proof coverage only; production behavior was
unchanged.

## Changed Surfaces

- `android-app/app/src/androidTest/java/com/senku/mobile/AnswerCardDaoTest.java`
- `android-app/app/src/androidTest/java/com/senku/mobile/OfflineAnswerEngineAnswerCardRuntimeTest.java`

## Proof Added

DAO coverage now proves:

- no optional answer-card tables returns an empty list;
- `answer_cards` without clause/source tables returns an empty list;
- `answer_cards` plus clauses but no sources returns an empty list;
- malformed `trigger_terms_json` and `sections_json` degrade to empty lists,
  not crashes.

Runtime coverage now proves:

- an empty old-pack database returns no reviewed-card plan;
- an `answer_cards`-only partial pack returns no reviewed-card plan;
- a pack with no clause rows returns no reviewed-card plan;
- a pack with no source rows returns no reviewed-card plan.

## Validation

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac --console=plain
# BUILD SUCCESSFUL
```

Focused connected Android matrix:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.senku.mobile.AnswerCardDaoTest,com.senku.mobile.OfflineAnswerEngineAnswerCardRuntimeTest" `
  --console=plain
# BUILD SUCCESSFUL
# Starting 16 tests on Senku_Large_3(AVD) - 16
# Starting 16 tests on Senku_Tablet(AVD) - 16
# Starting 16 tests on Senku_Tablet_2(AVD) - 16
# Starting 16 tests on Senku_Large_4(AVD) - 16
```

## Boundary

The reviewed-card runtime remains developer/test scoped and default `off`.
This slice proves fail-closed compatibility for missing or partial answer-card
pack tables; it does not promote the 271-card desktop inventory into Android
runtime selection.
