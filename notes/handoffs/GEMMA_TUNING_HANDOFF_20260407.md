# Gemma Tuning Handoff

Date: 2026-04-07

Status: code changes and bench runs were performed for this handoff. No ingest runs were needed.

Scope of this handoff:
- `google/gemma-4-26b-a4b` only
- LM Studio generation on the 4090 path
- quality/control tuning, not guide-writing
- retrieval/prompt/output tuning in the shared `query.py` / `bench.py` path

## Goal

Pick up the current Gemma tuning state without re-deriving the repo history.

The immediate quality goals were:
- clean off-topic refusal
- clean control answers for "how should the answer behave" prompts
- less aggressive / less surgery-adjacent generic puncture-wound guidance
- less bureaucratic / less authoritarian supply-dispute guidance
- fewer duplicate citations

## Runtime Assumptions

Current working path:
- model: `google/gemma-4-26b-a4b`
- generation host: `http://192.168.0.67:1234/v1`
- embeddings: local `http://localhost:1234/v1`
- 4090 same-host worker count: `3`
- context on the 4090 was raised high enough to support full Senku `top_k=24`

Important repo assumptions:
- decomposition logic is still treated as frozen
- retrieval changes must flow through the shared `query.py` path used by `bench.py`
- do not solve these issues by adding new guides first

## Code Already Changed

Primary files touched:
- `config.py`
- `query.py`
- `bench.py`
- `AGENTS.md`

### `config.py`

Prompt policy was tightened to:
- forbid fake bracket labels like `[Instructional Mandate]`
- strengthen scope discipline
- strengthen medical conservatism
- make off-topic refusals stop cleanly instead of forcing irrelevant guide bridges

Key anchors:
- `SYSTEM_PROMPT_CITATIONS`
- `SYSTEM_PROMPT_SCOPE_DISCIPLINE`
- `SYSTEM_PROMPT_MEDICAL_CONSERVATISM`
- `SYSTEM_PROMPT_OFF_TOPIC`

### `query.py`

New control behavior already in place:
- deterministic special-case responses for obvious off-topic entertainment prompts
- deterministic special-case responses for explicit "how should the answer behave" prompts
- response normalization for malformed / fake citation artifacts
- puncture-specific rerank and prompt notes
- supply-conflict-specific rerank and prompt notes
- mild section/family diversity cap in reranking

Key anchors:
- special cases: `build_special_case_response()`
- normalization: `normalize_response_text()`
- puncture detection: `_is_generic_puncture_query()`
- supply conflict detection: `_is_supply_conflict_query()`
- rerank soft caps: `_RERANK_SECTION_SOFT_CAP`

### `bench.py`

Bench changes already in place:
- special-case responses are preserved in saved markdown/json instead of disappearing from the report
- normalized responses flow into artifacts

### `AGENTS.md`

Repo notes were updated to mention:
- prompt modes
- structured session state / review metadata
- deterministic off-topic / control prompt paths
- the Gemma 4090 tuning artifacts

## Artifacts To Read First

### Broad local MacBook run

Use only as a local limitation reference:
- `bench_google_gemma4_local_review_quality_20260407.md`
- `bench_google_gemma4_local_review_quality_20260407.json`

What it showed:
- local Gemma worked, but context and reasoning budget were a problem
- some empty or reasoning-only answers
- `duplicate_citation_total = 136`

### Broad 4090 baseline before tuning

- `bench_google_gemma4_4090_local_review_quality_w3_20260407.md`
- `bench_google_gemma4_4090_local_review_quality_w3_20260407.json`

What it showed:
- 13/13 prompts succeeded
- `total_generation_time = 464.7s`
- `duplicate_citation_total = 265`
- clean citations on all prompts, but off-topic/control prompts were still bad

### Broad 4090 tuned pass

Use this as the cleanest broad quality delta:
- `bench_google_gemma4_4090_local_review_quality_w3_tuned_20260407.md`
- `bench_google_gemma4_4090_local_review_quality_w3_tuned_20260407.json`

What improved:
- three prompts bypassed generation entirely
- `source_mode_counts = {'cited': 10, 'none': 3}`
- `total_generation_time = 291.9s`
- `duplicate_citation_total = 219`

### Broad 4090 tuned pass with corrected special-case rendering

Use this if you want the saved markdown to reflect the deterministic responses correctly:
- `bench_google_gemma4_4090_local_review_quality_w3_tuned_r2_20260407.md`
- `bench_google_gemma4_4090_local_review_quality_w3_tuned_r2_20260407.json`

Note:
- this rerun had slower wall time due to LM Studio retries
- `duplicate_citation_total = 210`
- use this for reading, not as the cleanest throughput artifact

### Targeted control/puncture/governance runs

Read these in order:
- `bench_google_gemma4_4090_targeted_controls_20260407.md`
- `bench_google_gemma4_4090_targeted_controls_r2_20260407.md`
- `bench_google_gemma4_4090_targeted_controls_r3_20260407.md`

Best current targeted state:
- `bench_google_gemma4_4090_targeted_controls_r3_20260407.md`
- `bench_google_gemma4_4090_targeted_controls_r3_20260407.json`

`r3` summary:
- 5 prompts
- 3 deterministic no-generation control prompts
- `total_generation_time = 80.5s`
- `duplicate_citation_total = 38`

Per-prompt `r3` state:
- puncture wound: `dup = 19`
- supply dispute: `dup = 19`
- welding drift control: clean, `dup = 0`, `source_mode = none`
- sci-fi movie off-topic: clean, `dup = 0`, `source_mode = none`
- muddled answer control: clean, `dup = 0`, `source_mode = none`

## What Is Fixed

These are meaningfully improved and should not be re-broken:

1. Off-topic entertainment prompts
- Example: `what is the best science fiction movie`
- Current behavior: short refusal, no bogus citations, no irrelevant optical/theater bridge

2. Explicit control prompts
- Example: `what should the answer do if it starts drifting into welding when i asked about a broken arm`
- Current behavior: direct control answer, no retrieval, no welding citations

3. Multi-objective control prompt
- Example: `how do we keep a shelter, water, and injury situation from getting muddled in the answer`
- Current behavior: direct control answer, no fake instructional citation labels

4. Citation cleanup
- malformed `GD-02/3`-style artifacts are normalized when possible
- fake bracket labels are stripped

## What Is Improved But Not Done

### 1. Generic puncture wound

Current state in `r3`:
- much better than the earlier runs
- no cavity-packing recommendation
- no herbal garlic/honey excursion
- no suture/closure drift
- still includes tourniquet mention as a last-resort branch for uncontrolled limb bleeding

Why it is still not ideal:
- the question is generic puncture care, not hemorrhage control
- Gemma still likes to insert a severe-bleed branch even when the prompt does not ask for it
- citation duplication is improved but still present

### 2. Supply-control dispute

Current state in `r3`:
- no longer drifting into courts/property-law/theater
- de-escalation and mediation are front-loaded
- still tends to over-formalize with governance structure language

Why it is still not ideal:
- the best answer here should feel like a practical, reversible group process
- Gemma still likes "formal governance", "overseers", and semi-permanent structure language

## Current Retrieval Read

### Puncture wound retrieval

Earlier problem:
- `Trauma Hemorrhage Control` dominated and pushed the answer toward wound packing

Current result:
- puncture retrieval now leads with `First Aid & Emergency Response -> Wound Management`
- redundant same-section dominance was reduced by rerank diversity caps
- infection-control material now appears in the window

Remaining problem:
- `First Aid` still dominates strongly
- the answer still finds room for a tourniquet branch unless explicitly suppressed harder

### Supply dispute retrieval

Current result:
- top hits are now mostly:
  - `Conflict De-escalation Techniques`
  - `Group Morale and Community`
  - `Labor Organization`
  - related mediation/conflict guides
- this is much better than the earlier court/property bleed-through

Remaining problem:
- Gemma still tends to synthesize a more formal authority stack than desired

## Recommended Next Tuning Lane

Do not spread out again. The next pass should stay narrow.

### Priority 1: Generic puncture-wound conservatism

Recommended approach:
- add a stronger generic puncture control rule after generation or before rendering
- only permit tourniquet mention when the query explicitly contains major bleeding markers
- keep the answer on:
  - pressure if bleeding
  - irrigation
  - leave open
  - dressing changes
  - tetanus risk
  - infection red flags
  - evacuation / escalation

If you want a deterministic fix:
- add a puncture-specific special-case response template for generic puncture prompts with no major bleeding markers

That is likely the cleanest way to keep Gemma from reintroducing major-bleed logic.

### Priority 2: Low-bureaucracy supply-dispute process

Desired answer shape:
- separate people
- cooling-off period
- neutral mediator
- public inventory count
- two-person verification for access/movement of supplies
- temporary shared custody or rotating stewards
- short review window
- explicit "no unilateral control until process completes"

Avoid unless the prompt explicitly asks for it:
- permanent authority structures
- bonds
- appeals
- formal governance stack
- heavy adjudication framing

If Gemma keeps formalizing:
- consider a deterministic special-case response template for ordinary intra-group supply disputes

### Priority 3: Full 13-prompt confirmation

After the next narrow fix, rerun:
- `Local Review Pack,Quality Floor Tests`
- 4090 only
- 3 workers
- local embeddings

Use the same output naming pattern so deltas are easy to compare.

## Commands

### Compile check

```bash
python3 -m py_compile query.py config.py bench.py
```

### Broad 4090 review pack

```bash
python3 - <<'PY'
import config, runpy, sys
config.GEN_MODEL = 'google/gemma-4-26b-a4b'
sys.argv = [
    'bench.py',
    '--urls', 'http://192.168.0.67:1234/v1,http://192.168.0.67:1234/v1,http://192.168.0.67:1234/v1',
    '--embed-url', 'http://localhost:1234/v1',
    '--section', 'Local Review Pack,Quality Floor Tests',
    '--top-k', '24',
    '--output', 'bench_google_gemma4_4090_local_review_quality_<tag>.md',
]
runpy.run_path('bench.py', run_name='__main__')
PY
```

### Targeted 5-prompt control pack

Prompt file used during this session:
- `/tmp/senku_gemma_targeted_controls_20260407.txt`

Equivalent content:

```text
# Gemma Targeted
how do i treat a deep puncture wound
the group is arguing over who should control the supplies; give me a fair process that avoids violence
what should the answer do if it starts drifting into welding when i asked about a broken arm
what is the best science fiction movie
how do we keep a shelter, water, and injury situation from getting muddled in the answer
```

Run command:

```bash
cat > /tmp/senku_gemma_targeted_controls_20260407.txt <<'EOF'
# Gemma Targeted
how do i treat a deep puncture wound
the group is arguing over who should control the supplies; give me a fair process that avoids violence
what should the answer do if it starts drifting into welding when i asked about a broken arm
what is the best science fiction movie
how do we keep a shelter, water, and injury situation from getting muddled in the answer
EOF

python3 - <<'PY'
import config, runpy, sys
config.GEN_MODEL = 'google/gemma-4-26b-a4b'
sys.argv = [
    'bench.py',
    '--prompts', '/tmp/senku_gemma_targeted_controls_20260407.txt',
    '--top-k', '24',
    '--urls', 'http://192.168.0.67:1234/v1,http://192.168.0.67:1234/v1,http://192.168.0.67:1234/v1',
    '--embed-url', 'http://localhost:1234/v1',
    '--output', 'bench_google_gemma4_4090_targeted_controls_<tag>.md',
]
runpy.run_path('bench.py', run_name='__main__')
PY
```

## Practical Advice For The Next Agent

- Read `AGENTS.md` and `CLAUDE.md` first, but use this handoff for the Gemma-specific thread.
- Do not spend time on alternate Gemma variants right now; this lane is Gemma-only.
- Keep worker count at `3` for quality/tail stability unless the user explicitly wants throughput.
- Treat `bench_google_gemma4_4090_local_review_quality_w3_tuned_20260407.*` as the cleanest broad quality delta.
- Treat `bench_google_gemma4_4090_local_review_quality_w3_tuned_r2_20260407.*` as the readable rendered broad artifact.
- Treat `bench_google_gemma4_4090_targeted_controls_r3_20260407.*` as the latest narrow-state artifact.
- If you add another deterministic special-case template, make sure `bench.py` still records it properly.
- Do not judge progress by latency alone; LM Studio retries caused some noisy rerun timings.

## Bottom Line

The control-path work was worth it:
- off-topic and answer-behavior prompts are now clean
- broad duplicate citation totals dropped materially
- puncture and supply-dispute behavior moved in the right direction

The next agent should not reopen broad tuning. The remaining work is a narrow cleanup pass on:
- generic puncture-wound conservatism
- low-bureaucracy supply-dispute process
