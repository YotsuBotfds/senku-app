# Project NOMAD Comparison

Date: 2026-04-08

Sources reviewed:
- `https://www.projectnomad.us/`
- `https://github.com/Crosstalk-Solutions/project-nomad`

## What Project NOMAD Is

Project NOMAD is an offline platform bundle and management layer:
- browser-based "Command Center"
- Docker-orchestrated install/update flow
- Kiwix for offline libraries
- Ollama or compatible local model backends
- Qdrant-backed document upload + semantic search
- Kolibri for education
- ProtoMaps for offline maps
- CyberChef and notes tooling

This is best understood as an offline appliance/platform, not a narrowly tuned survival assistant.

## What Senku Is

Senku is a bounded survival/practical-knowledge system:
- specialized corpus and retrieval policy
- scenario/objective extraction in `query.py`
- metadata-aware hybrid retrieval
- deterministic control-path rules for prompts where answer shape matters more than open-ended generation
- explicit Gate / Coverage / Sentinel evaluation workflow

Senku is deeper on answer control and eval rigor inside its domain. Project NOMAD is broader on packaging, installation UX, and offline bundle breadth.

## Overlap

Real overlap exists at the platform layer:
- offline/local model serving
- offline knowledge access
- prepper / off-grid positioning
- GPU-backed local inference on user-owned hardware

That overlap does not make Senku redundant unless the project goal becomes "ship a general offline knowledge appliance" instead of "ship a high-quality bounded survival assistant."

## Useful Takeaways For Senku

The useful parts to borrow are productization ideas, not core retrieval ideas:

1. Packaging
- Senku could eventually ship as one service inside a broader offline stack instead of only as a Python repo + LM Studio workflow.

2. Operator UX
- A small browser dashboard for model status, index status, guide count, and benchmark/watchlist summaries would make Senku easier to run repeatedly.

3. Content Presentation
- Curated content packs / guide bundles could make the corpus easier to browse than a raw guide tree.

4. Positioning
- Senku should present itself as the specialized expert layer inside a broader offline system, not as a clone of a general-purpose offline box.

## What Not To Copy Blindly

The public NOMAD material does not show evidence of:
- deterministic answer-control rules
- scenario coverage / constraint accounting
- tiered regression evaluation comparable to Gate / Coverage / Sentinel

So the correct response is not "become NOMAD." It is:
- keep Senku's specialist retrieval/eval identity
- treat NOMAD-like packaging/UI as a future deployment shell

## Recommended Project Direction

Short-term:
- continue retrieval/citation cleanup
- continue deterministic-registry maintainability work
- use broad rerun artifacts and watchlists to drive improvements

Medium-term:
- add a lightweight Senku dashboard / operator UI
- expose benchmark/watchlist summaries in that UI
- make installation/runtime easier for non-developer users

Long-term:
- consider Senku as a module inside a larger offline bundle, whether homegrown or interoperable with a NOMAD-like stack