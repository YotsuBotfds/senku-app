# RAG-A4 Mobile-Pack Answer-Card Helper Extraction

## Slice

Extract the answer-card export helpers added in `RAG-A2` out of
`mobile_pack.py` so the mobile-pack exporter does not become harder to reason
about.

## Role

Python worker on `gpt-5.5 medium`.

## Preconditions

- `RAG-A2` optional answer-card table export is already green.
- `RAG-A3` Android reader/model layer is already green across the Senku matrix.

## Target Files

- `mobile_pack.py`
- `mobile_pack_answer_cards.py`

## Outcome

- Added `mobile_pack_answer_cards.py` for answer-card preparation, validation,
  clause/source row shaping, and SQLite insertion.
- `mobile_pack.py` now imports:
  - `prepare_answer_cards_for_mobile_pack` as
    `_prepare_answer_cards_for_mobile_pack`;
  - `insert_answer_cards` as `_insert_answer_cards`.
- The extracted module does not import `mobile_pack.py` or `chromadb`.
- Existing private helper names remain available in `mobile_pack.py` for
  compatibility with current tests and patch points.

## Validation

- `python -B -m py_compile mobile_pack.py mobile_pack_answer_cards.py tests\test_mobile_pack.py`
- `python -B -m unittest tests.test_mobile_pack -v` passed `51` tests.
- `python -B -m unittest tests.test_mobile_pack_manifest_parity tests.test_guide_answer_card_contracts -v` passed `29` tests.
- `python -B scripts\validate_guide_answer_cards.py` validated `6` cards.
- Import smoke confirmed `mobile_pack_answer_cards` does not load `chromadb`.

## Boundaries

- No schema changes beyond `RAG-A2`.
- No Android behavior changes.
- No runtime answer-composition changes.
