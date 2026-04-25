# RAG-T7 Guide Quality Priority Selector

## Slice

Join observed RAG diagnostics with metadata and corpus-marker audits to produce
an actionable guide-quality shortlist.

## Outcome

Adds `scripts/prioritize_guide_quality_work.py`, a read-only selector that
consumes:

- one or more RAG diagnostic directories or `diagnostics.json` files
- optional metadata audit JSON
- optional corpus marker scan JSON

It emits JSON and Markdown with guide-level ranking fields:

- prompt/bad/expected/cited/top-1/top-k row counts
- bucket and app-acceptance counts
- high-liability metadata gaps
- reviewed answer-card coverage
- corpus marker types
- `candidate_action`
- plain-language `reason`

## Proof

Fresh guide-quality priority artifact:

- JSON: `artifacts/bench/guide_quality_priorities_20260425_1604.json`
- Markdown: `artifacts/bench/guide_quality_priorities_20260425_1604.md`

The selector ranked `18` guides from the fresh FA/FB/FD/FE marker-overlay panel,
including `16` high-liability guides.

Notable rows:

- `GD-585`: `repair_corpus_partial` because the wound guide appears in measured
  prompts and has an unresolved partial marker.
- `GD-859`, `GD-918`, `GD-601`, `GD-858`: `consider_reviewed_answer_card`
  because they are observed high-liability guides without reviewed-card
  coverage.
- `GD-232`, `GD-898`, `GD-589`: `inspect_diagnostic_failure` because they
  participate in the remaining non-passing diagnostic row.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\prioritize_guide_quality_work.py tests\test_prioritize_guide_quality_work.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_prioritize_guide_quality_work -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\prioritize_guide_quality_work.py artifacts\bench\rag_diagnostics_20260425_1546_runtime_plus_marker_overlay --metadata-audit artifacts\bench\metadata_coverage_audit_20260425_1556.json --corpus-marker-scan artifacts\bench\corpus_marker_scan_20260425_1544.json --output-json artifacts\bench\guide_quality_priorities_20260425_1604.json --output-md artifacts\bench\guide_quality_priorities_20260425_1604.md --limit 30
```

Focused validation passed `1` test.

## Next

The scout-recommended mature version is family-grain ranking by
`expected_guide_family`. This first guide-grain selector is useful now, but the
next refinement should collapse related owners/distractors into family rows
before assigning broad metadata or answer-card expansion work.
