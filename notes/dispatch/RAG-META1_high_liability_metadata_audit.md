# RAG-META1 High-Liability Metadata Audit

## Slice

Add a non-blocking audit for high-liability guide metadata coverage before
promoting any new metadata rule into ingest validation.

## Outcome

Adds `scripts/audit_metadata_coverage.py`, which parses guide frontmatter and
reports JSON/Markdown coverage for:

- aliases
- `routing_cues`
- body routing markers
- `related`
- tags
- liability level
- reviewed answer-card coverage
- citation/applicability policy, inferred from either guide frontmatter or
  reviewed answer-card metadata

The script intentionally does not fail ingest. It is a planning/reporting tool
for high-liability and high-traffic slices.

## Proof

Real corpus audit:

- JSON: `artifacts/bench/metadata_coverage_audit_20260425_1556.json`
- Markdown: `artifacts/bench/metadata_coverage_audit_20260425_1556.md`

Summary:

- guides scanned: `754`
- high-liability guides: `249`
- high-liability guides with gaps: `248`
- reviewed answer-card guide coverage: `6`
- missing aliases: `214`
- missing explicit `routing_cues`: `240`
- missing any routing support: `144`
- missing citation policy: `247`
- missing applicability policy: `244`
- missing reviewed answer card: `244`

## Interpretation

This should not become a broad hard gate yet. Most high-liability owner guides
are not supposed to be `bridge: true`, and `citations_required` /
`applicability` are not established guide-frontmatter fields. The audit is most
useful for selecting high-liability guide families where answer-card coverage,
routing aliases, and policy metadata would directly improve app answer quality.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\audit_metadata_coverage.py tests\test_audit_metadata_coverage.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1556.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1556.md
```

Focused validation passed `3` tests.

## Next

Pick one high-liability, high-traffic family and add metadata plus answer-card
coverage there, then rerun this audit and a prompt panel. Good candidates are
not simply the largest gap rows; choose families already appearing in prompt
misses or Android reviewed-card expansion work.
