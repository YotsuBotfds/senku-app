# RAG-DOCS1 Corpus Marker Scanner

## Slice

Add a static corpus scanner for unresolved partial markers, template markers,
HTML comments, bridge metadata, and routing phrases before editing guide
content.

## Outcome

Adds `scripts/scan_corpus_markers.py`, which scans guide Markdown and writes:

- JSON: machine-readable guide records and marker hits
- Markdown: compact summary table with fail/warn/info examples

The scanner reports:

- `{{> ... }}` as `unresolved_partial`, severity `fail`
- other `{{ ... }}` markers as `template_marker`, severity `warn`
- HTML comments as `html_comment`, mostly `info` with TODO/routing comments as
  `warn`
- `bridge: true` frontmatter and routing phrases as diagnostic signals

## Proof

Real corpus scan:

- JSON: `artifacts/bench/corpus_marker_scan_20260425_1544.json`
- Markdown: `artifacts/bench/corpus_marker_scan_20260425_1544.md`

Summary:

- guides scanned: `754`
- guides with any marker signal: `329`
- unresolved partial markers: `55` hits in `55` guides
- bridge-frontmatter guides: `11`
- HTML comments: `117` hits in `59` guides
- routing phrases: `434` hits in `253` guides

The unresolved partial list includes high/critical medical and survival guides,
so this should remain a corpus-packaging/eval lane before direct guide edits.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\scan_corpus_markers.py tests\test_scan_corpus_markers.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_scan_corpus_markers -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_corpus_markers.py --guides-dir guides --output-json artifacts\bench\corpus_marker_scan_20260425_1544.json --output-md artifacts\bench\corpus_marker_scan_20260425_1544.md
```

Focused validation passed `2` tests.

## Next

Use the scanner JSON as input to one retrieval diagnostic: when the top
retrieved guide is bridge/thin/partial-bearing and the expected owner is absent
or lower ranked, surface that as a warning column in
`scripts/analyze_rag_bench_failures.py`.
