# RAG-RUNTIME1 Gemma/LiteRT Target Naming

## Slice

Disambiguate model naming and default runtime targets before the next runtime
policy slice, so the team has one stable vocabulary for hosted model IDs,
LiteRT aliases, and local file artifacts.

## Role

Main agent / docs and standards lane.

## Preconditions

- `notes/deep_research_report_backlog_20260425.md`
- `config.py` defaults and runtime docs (`notes/AGENT_OPERATING_CONTEXT.md`,
  related CLI usage in `scripts`/`bench.py` notes).

## Outcome

- Publish one canonical naming decision for Gemma/LiteRT runtime targets:
  - default generation target in current desktop/validation lanes is
    `gemma-4-e2b-it-litert`;
  - this is a local LiteRT deployment identifier, not a public API model ID;
  - broad-quality/hosted model families remain separately represented in any
    future host/bench profile changes.
- Map runtime naming to artifacts for implementers:
  - `SENKU_GEN_MODEL` / `--model` value,
  - default generation endpoint contract,
  - and local LiteRT filenames (`*.litertlm` / `*.task`).
- Add a short decision note that can be mirrored in future model-identity or
  launcher docs.

## Boundaries

- Do not change code, tests, scripts, or launch wrappers in this slice.
- Do not alter runtime thresholds, routing logic, or benchmark scripts.

## Acceptance

- The dispatch note contains a single table or short matrix that distinguishes:
  - runtime target ID,
  - public model family identifier (if used in any profile), and
  - local file artifacts.
- No unresolved statement remains about whether `gemma-4-e2b-it-litert` is
  shorthand vs deployment target.
- The naming note is ready for the next engineer to implement without adding
  assumptions.

## Report Format

- New dispatch file path.
- One follow-up sentence in the backlog saying this item is now a dispatched
  docs slice.
