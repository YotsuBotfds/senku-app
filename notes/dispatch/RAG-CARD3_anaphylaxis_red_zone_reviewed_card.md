# RAG-CARD3 Anaphylaxis Red-Zone Reviewed Card

## Summary

Implemented a reviewed answer-card for GD-400, `anaphylaxis_red_zone`, scoped to allergen-linked airway, breathing, circulation, color, faintness/collapse, and swelling red-zone prompts.

## Runtime Guard

Added explicit runtime predicate wiring in `query_answer_card_runtime.py` so the card only matches anaphylaxis red-zone prompts. Mild skin-only hives, itch, or rash prompts with normal breathing and normal alertness do not activate the card, even when GD-400 is retrieved.

The runtime also strips common negated red-zone phrases before matching, so prompts such as "no throat swelling", "no trouble breathing", or "no lip swelling" do not activate the card from those words alone.

Post-review follow-up aligned the card predicate with the existing deterministic anaphylaxis red-zone phrases, including throat tightness, rescue inhaler not helping after exposure, dizziness, and weakness. It also keeps isolated face swelling after a sting out of this red-zone card unless hives or airway/circulation/swelling danger signs are present, and reserves a prioritized card slot when `max_cards=2`.

## Contract Shape

The card leads with:

- anaphylaxis-until-proven-otherwise framing for allergen-linked red flags
- epinephrine first if available or prescribed
- emergency services or fastest evacuation
- safe positioning and breathing monitoring
- antihistamines, steroids, and inhalers as adjuncts only

Forbidden advice is concrete and condition-bound to avoid accidental matches on required epinephrine-first text.

## Validation

- Pass: `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_guide_answer_card_contracts tests.test_query_answer_card_runtime -v` (`Ran 50 tests`, `OK`).
- Pass: post-review focused runtime follow-up `.\.venvs\senku-validate\Scripts\python.exe -B -m unittest tests.test_query_answer_card_runtime -v` (`Ran 11 tests`, `OK`).
- Pass: `.\.venvs\senku-validate\Scripts\python.exe scripts\scan_mojibake.py --paths notes/specs/guide_answer_cards/anaphylaxis_red_zone.yaml tests/test_guide_answer_card_contracts.py query_answer_card_runtime.py tests/test_query_answer_card_runtime.py notes/dispatch/RAG-CARD3_anaphylaxis_red_zone_reviewed_card.md --report-only --markdown-limit 200` (`findings_count: 0`, `gate_findings_count: 0`).
- Pass: `git diff --check -- notes/specs/guide_answer_cards/anaphylaxis_red_zone.yaml tests/test_guide_answer_card_contracts.py query_answer_card_runtime.py tests/test_query_answer_card_runtime.py notes/dispatch/RAG-CARD3_anaphylaxis_red_zone_reviewed_card.md`.
- Note: plain `python -B -m unittest ...` failed before test execution for `query.py` import because the system Python lacks `chromadb`; the repo validation venv above was used per `AGENTS.md`.
