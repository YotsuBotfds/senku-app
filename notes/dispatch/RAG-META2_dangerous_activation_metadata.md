# RAG-META2 Dangerous Activation Metadata

## Slice

Add targeted high-liability frontmatter metadata for the dangerous activation /
mania crisis family without changing guide body content or runtime behavior.

## Outcome

Updated frontmatter only:

- `guides/psychological-first-aid-peer-support.md` (`GD-858`)
- `guides/recognizing-mental-health-crises.md` (`GD-859`)
- `guides/anxiety-stress-self-care.md` (`GD-918`)

Each guide now has:

- `aliases`
- `routing_cues`
- `citations_required: true`
- `applicability`

The routing language distinguishes routine anxiety/PFA from acute crisis patterns
such as days of little sleep with grandiosity, recklessness, nonstop talking, or
feeling invincible.

## Proof

Fresh metadata audit:

- JSON: `artifacts/bench/metadata_coverage_audit_20260425_1724_dangerous_activation_metadata.json`
- Markdown: `artifacts/bench/metadata_coverage_audit_20260425_1724_dangerous_activation_metadata.md`

For `GD-858`, `GD-859`, and `GD-918`, the audit now reports:

- aliases present
- routing cues present
- citation policy present
- applicability present
- remaining gap: `missing_reviewed_answer_card`

Fresh family-priority panel:

- JSON:
  `artifacts/bench/high_liability_family_priorities_20260425_1724_dangerous_activation_metadata.json`
- Markdown:
  `artifacts/bench/high_liability_family_priorities_20260425_1724_dangerous_activation_metadata.md`

The dangerous activation family dropped from priority score `14` to `6`; it is
now behind wound, poisoning, and stroke/cardiac metadata work.

## Validation

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B -m py_compile scripts\audit_metadata_coverage.py scripts\prioritize_high_liability_families.py
& .\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\audit_metadata_coverage.py --guides-dir guides --output-json artifacts\bench\metadata_coverage_audit_20260425_1724_dangerous_activation_metadata.json --output-md artifacts\bench\metadata_coverage_audit_20260425_1724_dangerous_activation_metadata.md
```

Focused tests passed: `4`.

## Next

If this family becomes the top measured work again, the remaining gap is a real
reviewed answer-card slice for acute mania / dangerous activation, not more
frontmatter.
