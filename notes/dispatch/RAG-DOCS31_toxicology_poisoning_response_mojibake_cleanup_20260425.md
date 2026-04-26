# RAG-DOCS31 Toxicology Poisoning Response Mojibake Cleanup

Date: 2026-04-25
Worker: High Worker AK
Scope: `guides/toxicology-poisoning-response.md`

## Summary

- Cleaned only obvious mojibake punctuation and symbol artifacts in the toxicology and poisoning response guide.
- Replacement classes were numeric range separators, em-dash separators, chemical subscripts, multiplication signs, degree Celsius symbols, greater-than-or-equal and approximately symbols, and the osmolar-gap minus sign.
- Preserved frontmatter, headings, links, URLs, poison-response guidance, antidote and medication wording, dose-like values, exposure times, temperatures, thresholds, red flags, child exposure wording, emergency escalation language, and procedural meaning.

## Validation

- Before cleanup: `scripts\scan_mojibake.py --paths guides\toxicology-poisoning-response.md --report-only --markdown-limit 200` reported 240 findings and 240 gate findings.
- After cleanup: `scripts\scan_mojibake.py --paths guides\toxicology-poisoning-response.md --report-only --markdown-limit 200` reported 0 findings and 0 gate findings.
- Diff hygiene: `git diff --check -- guides\toxicology-poisoning-response.md notes\dispatch\RAG-DOCS31_toxicology_poisoning_response_mojibake_cleanup_20260425.md` passed.

## Semantic Drift Review

- Reviewed changed hunks for antidote and medication guidance, including atropine, pralidoxime, physostigmine, naloxone, benzodiazepines, activated charcoal, chelation agents, fomepizole/ethanol, sodium bicarbonate, folinic acid, epinephrine, and oxygen-related wording.
- Reviewed decontamination timing, dermal and ocular irrigation thresholds, respiratory and neurologic red flags, child and household exposure routing language, mushroom and botulism emergency escalation, carbon monoxide severity thresholds, toxic alcohol timelines, snakebite timing, anaphylaxis observation timing, and emergency evacuation language; changes were punctuation-only.
- Main re-ingest: `ingest.py --files guides\toxicology-poisoning-response.md --force-files` processed 1 file / 92 chunks; collection total 49,714.
