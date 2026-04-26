# RAG-DOCS30 Sepsis Recognition Antibiotic Protocols Mojibake Cleanup

Date: 2026-04-25
Worker: High Worker AJ

## Scope

- Cleaned only obvious mojibake punctuation and symbol artifacts in `guides/sepsis-recognition-antibiotic-protocols.md`.
- Replacement classes were dash punctuation, right-arrow separators, comparison operators, degree temperature symbols, respiratory gas subscripts, micro-liter unit notation, plus/minus notation, and multiplication signs.
- Preserved sepsis recognition thresholds, mortality statements, diagnostic and triage guidance, antibiotic protocol wording, dosing-like and schedule-like values, temperatures, durations, headings, frontmatter, links, URLs, and procedural meaning.

## Safety Review

- Reviewed changed hunks for semantic drift in antibiotic guidance, red flags, timing, thresholds, shock and organ dysfunction criteria, escalation language, source control timing, fluid and vasopressor targets, and meningitis emergency language.
- Main re-ingest: `ingest.py --files guides\sepsis-recognition-antibiotic-protocols.md --force-files` processed 1 file / 40 chunks; collection total 49,723.

## Validation

- Before scan: `scripts\scan_mojibake.py --paths guides\sepsis-recognition-antibiotic-protocols.md --report-only --markdown-limit 200` reported 65 findings and 65 gate findings.
- After scan: `scripts\scan_mojibake.py --paths guides\sepsis-recognition-antibiotic-protocols.md --report-only --markdown-limit 200` reported 0 findings and 0 gate findings.
- Diff check: `git diff --check -- guides\sepsis-recognition-antibiotic-protocols.md notes\dispatch\RAG-DOCS30_sepsis_recognition_antibiotic_protocols_mojibake_cleanup_20260425.md` passed.
