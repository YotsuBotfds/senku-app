# AGENTS.md

Fast landing page for agents in this repo. Keep this file short; put changing
execution detail in focused notes and link it from here.

## Start Here

- Current operating context: [`notes/AGENT_OPERATING_CONTEXT.md`](./notes/AGENT_OPERATING_CONTEXT.md)
- Testing workflow: [`TESTING_METHODOLOGY.md`](./TESTING_METHODOLOGY.md)
- Subagent contract: [`notes/SUBAGENT_WORKFLOW.md`](./notes/SUBAGENT_WORKFLOW.md)
- Guide lane index: [`notes/GUIDE_INDEX.md`](./notes/GUIDE_INDEX.md)
- Android lane index: [`notes/ANDROID_INDEX.md`](./notes/ANDROID_INDEX.md)
- Swarm / model-routing index: [`notes/SWARM_INDEX.md`](./notes/SWARM_INDEX.md)

## Repository Map

- Desktop query/retrieval: [`query.py`](./query.py), [`config.py`](./config.py), [`bench.py`](./bench.py)
- Guide ingestion/catalog: [`ingest.py`](./ingest.py), [`guide_catalog.py`](./guide_catalog.py)
- Deterministic rules: [`deterministic_special_case_registry.py`](./deterministic_special_case_registry.py), [`special_case_builders.py`](./special_case_builders.py)
- Android app: [`android-app`](./android-app)
- Bench outputs: [`artifacts/bench`](./artifacts/bench)
- Prompt packs: [`artifacts/prompts`](./artifacts/prompts)
- Handoffs, dispatches, reviews: [`notes`](./notes)

## Quick Commands

Windows validation Python used by recent work:

```powershell
.\.venvs\senku-validate\Scripts\python.exe
```

Focused deterministic/routing checks:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_special_cases tests.test_query_routing tests.test_registry_overlap tests.test_deterministic_near_miss -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\validate_special_cases.py
```

Guide prompt validation:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run_guide_prompt_validation.ps1 -Wave be -PythonPath .\.venvs\senku-validate\Scripts\python.exe
```

## Operating Rules

- Re-ingest after guide edits before trusting retrieval behavior.
- Keep true knowledge gaps separate from retrieval-language or surfacing work.
- For safety-critical guide edits, grep for shared formulas, thresholds, dose-like statements, and invariants before validation.
- Prefer focused routing blocks and reciprocal links before merging guide families.
- Keep Android/emulator work behind the Android lane notes unless the user explicitly reopens that path.
