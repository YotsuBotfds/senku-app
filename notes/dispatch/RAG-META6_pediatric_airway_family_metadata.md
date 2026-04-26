# RAG-META6 Pediatric/Airway Family Metadata

- Date: 2026-04-25
- Scope: frontmatter metadata only for measured pediatric/airway high-liability family gaps.
- Owned guide edits:
  - GD-579 `emergency-airway-management`: added `citations_required` and `applicability`.
  - GD-298 `pediatric-emergency-medicine`: added `aliases`, `routing_cues`, `citations_required`, and `applicability`.
  - GD-617 `pediatric-emergencies-field`: added `aliases`, `routing_cues`, `citations_required`, and `applicability`.
  - GD-492 `postpartum-care-mother-infant`: added `aliases`, `routing_cues`, `citations_required`, and `applicability`.

No body content, guide IDs, slugs, titles, tags, procedure text, or answer cards were changed.

## Validation

- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides\emergency-airway-management.md guides\pediatric-emergency-medicine.md guides\pediatric-emergencies-field.md guides\postpartum-care-mother-infant.md --force-files` (`Files processed: 4`, `Total chunks: 270`, collection total `49709`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_audit_metadata_coverage tests.test_prioritize_high_liability_families -v` (`Ran 5 tests`, `OK`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\emergency-airway-management.md guides\pediatric-emergency-medicine.md guides\pediatric-emergencies-field.md guides\postpartum-care-mother-infant.md notes\dispatch\RAG-META6_pediatric_airway_family_metadata.md --report-only --markdown-limit 200` (`findings_count: 0`, `gate_findings_count: 0`).
- Pass: `git diff --check -- guides/emergency-airway-management.md guides/pediatric-emergency-medicine.md guides/pediatric-emergencies-field.md guides/postpartum-care-mother-infant.md notes/dispatch/RAG-META6_pediatric_airway_family_metadata.md`.
