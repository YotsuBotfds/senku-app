# Android lightweight-model spike notes (2026-04-16)

Context:
- User wants to explore whether a very small Qwen-family model (for example Qwen3.5-0.8B, or nearby light variants) could run on Android faster than the current gemma-4-e2b-it-litert path while maintaining acceptable answer quality.
- Current repo Android path is LiteRT-LM / .litertlm oriented, so GGUF / llama.cpp would be a parallel spike rather than a drop-in swap.

What to revisit:
- Compare Qwen3.5-0.8B vs Qwen3.5-2B as Android latency/quality candidates.
- Prefer documented Android-friendly local path first: llama.cpp + GGUF, likely via Termux or llama.android, before deeper app integration.
- Evaluate against current Senku validation prompts, with gemma-4-e2b-it-litert as the quality baseline.
- Do not assume 0.8B is good enough for final quality; treat it as a speed spike first.
- If 0.8B is too weak, jump to 2B rather than over-investing in 0.8B tuning.

Suggested future spike outline:
1. Confirm latest official Android/local-runtime docs for Qwen + llama.cpp.
2. Pick a GGUF quant and run a minimal Android/Termux smoke.
3. Measure latency, memory, and prompt completion reliability.
4. Run a small Senku prompt pack side-by-side against E2B.
5. Decide whether to pursue deeper Android integration.