# RAG-A14g Android Forward-Pack Tolerance Tests

Date: 2026-04-26

## Scope

Lock Android forward-pack tolerance around manifest parsing and installed-pack
schema validation. This is test coverage only; production code was unchanged.

## Changed Surfaces

- `android-app/app/src/test/java/com/senku/mobile/PackManifestTest.java`
- `android-app/app/src/test/java/com/senku/mobile/PackInstallerTest.java`

## Proof Added

Manifest parsing now explicitly proves that future optional fields are ignored
under:

- `counts`;
- `embedding`;
- `runtime_defaults`;
- `files.sqlite`;
- `files.vectors`;
- future sidecar file entries;
- `schema.answer_cards`;
- top-level future objects.

Installer schema validation now explicitly proves:

- answer-card tables remain optional at the required pack-table gate;
- future optional tables such as `answer_card_tags` and
  `retrieval_metadata_v2` do not make an otherwise valid pack invalid.

## Validation

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:compileDebugAndroidTestJavaWithJavac :app:testDebugUnitTest `
  --tests com.senku.mobile.PackManifestTest `
  --tests com.senku.mobile.PackInstallerTest `
  --console=plain
# BUILD SUCCESSFUL
```

## Boundary

This does not relax required manifest fields, required SQLite core tables, file
size checks, checksum checks, or reviewed-card runtime gating. It only locks
the intended tolerance for optional future pack metadata.
