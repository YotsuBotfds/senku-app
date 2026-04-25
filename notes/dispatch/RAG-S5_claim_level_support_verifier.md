# Slice RAG-S5 - claim-level support verifier

- **Role:** main agent (`gpt-5.5 medium`; use high if verifier starts modifying safety-critical answers).
- **Paste to:** after `RAG-S1` shows unsupported-claim or answer-ignore-context failures are material.
- **Why this slice now:** generated answers can retrieve the right guide but still add unsupported or badly ordered advice. The scalable fix is support checking, not another prompt-specific ban.

## Outcome

Add a post-generation support verifier that identifies unsupported action claims and records them in bench artifacts. A later slice can decide whether to rewrite or block.

## Current S5a Status

- Helper exists: `rag_claim_support.py`.
- It is integrated into `scripts/analyze_rag_bench_failures.py`.
- Analyzer fields: `claim_support_status`, `claim_action_count`, `claim_supported_count`, `claim_unknown_count`, `claim_forbidden_count`, `claim_support_basis`.
- Semantics distinguish supported negative safety instructions from positive forbidden advice.
- Current best proof is `artifacts/bench/rag_diagnostics_20260424_1410_child_choking_gate/report.md`: retrieval/ranking/generation/safety-contract misses are all `0`, expected owner is cited `24/24`, app acceptance is `15` strong supported, `7` moderate supported, `2` uncertain-fit accepted, and claim support is `15` no-generated-answer / `9` pass.
- Next S5-facing work is to use claim/card diagnostics to normalize answer phrasing, required-action coverage, and source-content contradictions; keep the verifier diagnostic-only until the card clauses are better specified.

## Likely Touch Set

- `bench.py` first, as diagnostic-only
- helper module `rag_claim_support.py`
- later runtime path only after diagnostic evidence

## Minimum Verifier

- extract numbered/bulleted action lines;
- detect high-risk action verbs;
- compare cited guide IDs against retrieved guide IDs;
- check forbidden phrases from pilot answer cards when available;
- load pilot answer cards when present and treat `first_actions`, `do_not`, `urgent_red_flags`, and `citation_ids` as the first local support contract;
- flag confident safety answers when expected/card evidence owners are absent from retrieved or cited guide IDs;
- emit per-claim `support_basis`: `card_first_action`, `card_red_flag`, `retrieved_citation`, `forbidden_by_card`, or `unknown`;
- label each action as supported, weak, unsupported, or unknown.

## Acceptance

- Bench JSON includes claim-support diagnostics for generated answers.
- Markdown report summarizes unsupported action count.
- Bench Markdown includes a compact claim-support table for safety-profile generated answers.
- Verifier output distinguishes unsupported actions from missing expected evidence owner.
- Verifier output distinguishes a supported "do not" safety instruction from a positive forbidden action.
- No answer behavior changes in the first implementation.

## Anti-Recommendations

- Do not expose hidden chain-of-thought.
- Do not rely on a cloud evaluator.
- Do not rewrite answers until diagnostics prove the verifier is stable.
