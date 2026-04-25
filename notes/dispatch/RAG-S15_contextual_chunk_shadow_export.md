# RAG-S15 Contextual Chunk Shadow Export

## Slice

Non-default shadow export of contextual chunk text.

## Role

Main agent / ingest measurement lane. Keep this non-invasive unless a later
slice explicitly promotes the shadow text into retrieval.

## Outcome

Landed an ingest-only JSONL export that captures contextual chunk sidecar fields
as raw values:

- `document`
- `contextual_retrieval_text`
- `metadata`
- `chunk_id`

This slice is intended as a shadow/export artifact for analysis and routing
experiments, not an active retrieval hot path.

## Preconditions

- The slice is not required to make immediate product-visible behavior changes.
- This is safe only if existing query/runtime contracts and mobile assets are left
  untouched.
- Re-ingest behavior remains the gating surface for any downstream adoption.

## Boundaries

- Production `Chroma` documents and embeddings are unchanged.
- The lexical DB is unchanged.
- Query-time behavior is unchanged.
- Guide source content and guide selection remain unchanged.
- Android app behavior remains unchanged.

## Acceptance

- `ingest.py` exposes `--contextual-shadow-jsonl`.
- `--contextual-shadow-only` writes the JSONL and exits before Chroma setup,
  embedding endpoint checks, embedding calls, lexical DB writes, or manifest
  work.
- Shadow JSONL export shape uses the four raw fields above.
- Shadow chunk IDs stay aligned with successfully embedded production chunks
  even when batch embedding falls back to per-chunk retries and skips an earlier
  chunk.
- A failed shadow-only parse removes stale JSONL output instead of leaving a
  prior export behind.
- No production vector/docstore, query routing, guide, or Android behavior is
  modified by this slice.
- Any downstream usage is blocked behind this non-default export lane.

## Validation

Passed validation:

- `py_compile` over `ingest.py` and `tests/test_ingest.py`.
- `tests.test_ingest` (`10` tests).

The focused tests prove contextual text includes guide/section metadata, the
shadow-only path does not open Chroma or call embeddings, stale shadow-only
output is removed after parse failure, fallback-embedded chunks keep stable
shadow join IDs, and the normal mocked rebuild path keeps SQLite
`lexical_chunk_meta.document` raw while writing the contextual JSONL sidecar.
