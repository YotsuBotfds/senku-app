# Slice RAG-S3 - contextual chunk ingest spike

- **Role:** main agent (`gpt-5.5 medium`; use high only if re-ingest parity or metadata integrity gets delicate).
- **Paste to:** after `RAG-S1` shows retrieval/ranking misses are a top bucket.
- **Why this slice now:** many RAG systems fail because chunks lose guide/section context. Senku already has rich metadata, so contextual retrieval can be tested without rewriting the app.

## Outcome

Prototype contextualized retrieval text at ingest time and compare retrieval hit rates against the current corpus on a bounded prompt set.

## Likely Touch Set

- `ingest.py`
- maybe `query.py` if retrieval needs to choose contextual field
- a small comparison script under `scripts/`
- notes/results artifact under `artifacts/bench/`

## Implementation Shape

For each chunk, construct a retrieval text such as:

```text
Guide: <title> (<guide_id>, <slug>)
Purpose: <description>
Section: <section_heading>
Category/tags: <category>, <tags>

<original chunk text>
```

Keep original chunk text for answer display and citations.

## Acceptance

- Spike can run on a small selected guide set first.
- It reports expected-guide hit@k before/after on selected prompt packs.
- It does not overwrite the production collection unless explicitly requested.

## Anti-Recommendations

- Do not re-ingest the full corpus blindly.
- Do not change guide prose.
- Do not make contextual text visible to users as citation text unless intentionally designed.
