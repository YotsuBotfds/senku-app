# Mobile Offline Pack Spec - 2026-04-10

This note captures the first concrete Senku mobile-pack export format for a fully offline Android/iPhone app.

## Purpose

Package:

- all guides
- chunk metadata
- lexical retrieval data
- precomputed chunk embeddings
- deterministic rule metadata
- optional reviewed answer-card metadata

into a self-contained bundle that a native mobile app can ship and query locally.

## Export Command

```bash
python scripts/export_mobile_pack.py artifacts/mobile_pack/senku_20260410
```

Useful flags:

- `--vector-dtype float16|int8`
- `--mobile-top-k 10`
- `--embedding-model-id <model-id>`
- `--chroma-dir <path>`
- `--guides-dir <path>`

## Files Produced

The exporter writes:

- `senku_mobile.sqlite3`
- `senku_vectors.f16` or `senku_vectors.i8`
- `senku_manifest.json`

## SQLite Layout

Tables:

- `guides`
- `guide_related`
- `deterministic_rules`
- `answer_cards`
- `answer_card_clauses`
- `answer_card_sources`
- `chunks`
- `lexical_chunks_fts`
- `lexical_chunks_fts4`
- `pack_meta`

Design notes:

- `chunks.vector_row_id` maps each chunk to the row-major vector file
- `lexical_chunks_fts` mirrors the current hybrid lexical search fields from `ingest.py`
- `guide_related` resolves related slugs to guide ids when possible
- `deterministic_rules` stores registry metadata only; the actual predicates/builders still live in app code
  - current columns: `rule_id`, `predicate_name`, `builder_name`, `sample_prompt`
- `answer_cards`, `answer_card_clauses`, and `answer_card_sources` are optional metadata-only tables for reviewed answer-card contracts
  - `answer_cards` records card identity, owning guide, review status, risk tier, citation policy, and routine/uncertain-fit notes
  - `answer_card_clauses` records required actions, conditional required actions, first actions, red flags, forbidden advice, `do_not`, and uncertain-fit clauses
  - `answer_card_sources` records reviewed source-guide family rows and source section labels
  - Android readers must tolerate old packs where these tables are absent

## Vector File Format

Format id:

- `senku-vectors-v1`

Header:

- little-endian
- struct layout: `<8s6I`
- magic: `SNKUVEC1`
- version: `1`
- header bytes: `32`
- row count
- embedding dimension
- dtype code: `1=float16`, `2=int8`
- flags: bit `1` normalized, bit `2` little-endian

Payload:

- row-major dense matrix
- `float16`: IEEE half floats
- `int8`: signed bytes representing normalized values scaled by `127`

Mobile runtime expectation:

- normalize the query embedding before scoring
- score with cosine-equivalent dot product against normalized chunk vectors

## Manifest Highlights

`senku_manifest.json` records:

- counts
- file hashes and sizes
- embedding model id
- vector dtype and dimension
- recommended mobile `top_k`
- optional `answer_cards` count
- source paths / collection name
- SQLite tables and vector-file schema metadata

## Important Caveat

The exported chunk vectors must match the embedder used on-device for query embeddings.

Today the repo default is:

- `nomic-ai/text-embedding-nomic-embed-text-v1.5`

If the mobile app uses a different embedder, rebuild the Chroma collection with that embedder first, then export a fresh pack. Do not mix a mobile query embedder with chunk vectors generated from a different embedding model and expect stable retrieval quality.

## Recommended First Mobile Profile

- generation model: `gemma-4-e2b` or `gemma-4-e4b`
- retrieval `top_k`: `8-12`
- vector dtype: start with `float16`, switch to `int8` only if storage pressure matters
- keep deterministic rules in native app code and use the pack for guide/rag assets
