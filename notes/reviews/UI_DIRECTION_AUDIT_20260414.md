# Senku Android UI Direction Audit â€” 2026-04-14

Historical note: This review note was relocated from the repo root into `notes/reviews/` in D13. The substantive audit below is preserved as the original historical snapshot.

## 1. Overall Direction Assessment

**Verdict: Converging strongly. This is clearly becoming a field knowledge hub, not a chatbot.**

The current UI direction is substantively correct and materially ahead of where most retrieval-first apps land. The app now expresses its core architectural identityâ€”route â†’ anchor â†’ evidence â†’ follow-upâ€”through visible UI primitives instead of hiding them behind debug surfaces. The detail screen reads like an answer thread with provenance, not a generic LLM chat bubble. The home screen separates "Find Guides" from "Ask From Guides" and shows the knowledge corpus, not an empty prompt box.

The biggest single proof that the direction is right: the trust spine (`route type â€¢ evidence strength â€¢ source count â€¢ turn count`) appears immediately in the compact header on every answer screen, in every posture. That is the product contract. It is visible. It is honest. That alone puts this ahead of most comparable apps.

> [!IMPORTANT]
> The direction is good. The next moves should deepen trust and readabilityâ€”not add features, not chase polish, and not flatten the hierarchy in the name of simplicity.

---

## Current Build Delta Since This Review

Several review gaps have already been addressed in the live build after this audit snapshot was captured:
- landscape validation now forces rotation and verifies posture before accepting the screenshot
- FTS warnings are gated to a single debug fallback line instead of recurring probe spam
- compact portrait phones no longer waste header space on the redundant Home pill
- tablet portrait now collapses the follow-up composer once the answer is present
- tablet station source selection now reads more decisively with a stronger active state
- the collapsed `Why this answer` affordance now reads more like a proof surface on compact phone and tablet portrait, with proof-specific toggle copy and a stronger accent shell
- that same compact proof surface now also has a clearer accessibility label, so assistive tech announces it as a proof surface rather than a generic expander
- phone landscape now keeps the proof title visible and uses `Proof lead:` language so the trust row is easier to notice in the tighter orientation
- the validation harness now reasserts foreground before final capture, which fixed the misleading launcher screenshot in the physical phone generated lane
- phone landscape hero/status chrome is now a little tighter, so generated answers keep more of the reading surface visible while still showing build progress
- tablet landscape now validates as a true wide capture on `emulator-5556`, so the station posture is trustworthy again for both phone and tablet lanes
- after the fresh guide-state push, home category buckets now tolerate the newer taxonomy (`power-generation`, `biology`, `metalworking`, `textiles-fiber-arts`) so the field-facing tiles do not show false-empty Water/Fire counts
- the home status surface now shows pack generated time, SQLite hash prefix, and mobile top-k, giving testers a quick way to confirm which guide state/backend posture is active

The remaining small UI opportunity from this pass shifts away from the proof affordance and toward persistent pack/trust visibility: sticky route/anchor/evidence/source/pack state while scrolling, plus clearer diagnostics for which guide state and backend are actually active.

---

## 2. Top 3 Strengths

### Strength 1: The Trust Spine Is Real and Visible

Every answer screen shows:
- **Route type** â€” `Instant deterministic` vs `AI answer`
- **Evidence strength** â€” `Strong evidence` / `Moderate evidence` / `No citations`
- **Source count** â€” `3 src` / `2 src`
- **Turn count** â€” `1 turn`
- **Anchor guide chip** â€” `GD-394` / `Anchor GD-394`

This is *not* metadata for developers. This is the product's trust contract. It appears in the compact header rail on phone, in the station rail title on tablet, and in every orientation. The honesty of the `No citations` / `Anchor unavailable` zero-source state is especially strongâ€”most apps would hide this.

**Screenshot evidence:** The phone top screenshot shows `AI answer | Moderate evidence | 2 src | 1 turn` immediately below the guide title, with `GD-569` anchor chip to the right. Tablet detail shows `Instant deterministic | Strong evidence | 3 sources | 1 turn` with `Anchor GD-394`.

### Strength 2: Source Navigation Is First-Class

Sources are not buried. They appear as:
- **Inline source chips** inside the answer bubble on phone (e.g., `GD-569 anchor`, `Simple Forge Designs (â€¦`)
- **Indexed source buttons** in the tablet station rail (`1/3 [GD-394]â€¦`, `2/3 [GD-394]â€¦`, `3/3 [GD-024]â€¦`)
- **Provenance preview** on tablet, showing the selected source's cited excerpt before jumping to the full guide
- **Collapsible proof drawer** on phone, expanding sources in place when tapped

This is exactly right for a survival tool. The user needs to verify before acting. Source navigation is designed into the answer flow, not appended after it.

### Strength 3: Posture-Aware Layouts Are Real, Not Aspirational

Four genuinely different layout behaviors are implemented and validated on real emulators:
- **Phone portrait**: compact drawer model, answer-first, collapsible secondary sections
- **Phone landscape**: terse header rail, compact follow-up, aggressive density
- **Tablet station** (landscape): left reading column + right utility rail with why â†’ provenance â†’ sources â†’ helper section
- **Tablet clipboard** (portrait): centered reading column, inline lower support

This is not just resource qualifier duplicationâ€”the tablet station rail genuinely uses side space for evidence inspection, and phone landscape genuinely compacts secondary chrome. The posture model (station vs. clipboard) is a strong conceptual framework.

---

## 3. Top 3 Problems or Risks

### Problem 1: Phone Portrait Answer Body Starts Too Low

On the phone portrait "top" view, the answer body text doesn't start until roughly the 35â€“40% mark of the viewport. Above it sits:
1. System status bar
2. Custom top bar (`Back` | `GD-569 | Hardening and Tempering Steel` | anchor chip)
3. Hero panel with `Offline answer ready in 6.2 s` + `Answer thread` mode chip
4. `Senku answered` label
5. Inline source chip row

That's 5 layers of chrome before any actual survival instruction appears. Under stress, the first thing the user sees is metadata, not the answer.

> [!WARNING]
> The hero panel (status text + mode chip) is the primary offender. After the initial answer is rendered, the "Offline answer ready in 6.2 s" message has no ongoing value. It should collapse or minimize once the answer is present.

**Recommendation:** Auto-collapse the hero panel after answer render. Keep it visible during staged/pending answers (where it has real value), but minimize it to a single-line status or dismiss entirely once the answer body is stable.

### Problem 2: The "Audit" Screenshots Show Follow-Up Chrome Competing with Answer on Generated Answers

The audit-series screenshots (which seem to show a slightly different build or density) reveal that on AI-generated answers, the bottom of the viewport shows:
- `Keep the thread going`
- `Ask a tighter follow-up without losing context.`
- A full-width text input + `Send` button

That follow-up section title + subtitle consume vertical space that could let more answer body be visible. The compact version (seen in the density_compact screenshots) is betterâ€”just `Ask follow-up...` + `Go`â€”but it's not clear both versions are converging to the same treatment.

**Recommendation:** Ensure the compact follow-up composer (`Ask follow-up... | Go`) is the default on phone portrait for answer mode, matching the compact_density screenshots. Reserve the expanded composer with title/subtitle for tablet station or first-time-user contexts only.

### Problem 3: "Why This Answer" Drawer Is Valuable but Has No Visual Anchor Until Tapped

On phone portrait, `Why this answer | Show` appears as a text-only toggle below the answer body. It's easy to scroll past without noticing. The content inside (route family, evidence basis, retrieval lane) is *exactly* what differentiates this app from a generic chat toolâ€”but it reads like an optional footnote.

**Recommendation:** Give the collapsed `Why this answer` row a subtle left-edge accent bar (matching the evidence-panel treatment) so it's visually distinct from surrounding text. Don't expand it by defaultâ€”the current collapsed-drawer model is correctâ€”but make it visually clear that it's an interactive trust surface, not a footer label.

---

## 4. Smallest Recommended Next Slice

### Auto-Collapse Hero Panel After Answer Render (Phone Portrait)

**Why this is the highest-leverage move:**
- It immediately recovers ~100dp of vertical space on the most constrained and most-used posture
- It moves the actual survival answer closer to the top of the viewport
- It has zero risk to the trust spine (the header rail already carries route/evidence/source/turn)
- The hero panel remains valuable during pending/staged answersâ€”this only collapses it *after* the answer arrives
- It does not require new IDs, new panels, or layout file changesâ€”it's a `DetailActivity.java` visibility toggle

**Implementation sketch:**
1. After `renderAnswerBody()` completes with a real answer, set `detail_hero_panel.setVisibility(View.GONE)` on phone portrait
2. Keep hero visible during staged reveals (the `Using N guides while Senku builds the answerâ€¦` state)
3. Keep hero visible on tablet, where there's enough viewport for it

**Expected result:** Answer body starts at approximately 25% viewport height instead of 40%, which dramatically improves first-glance readability under stress.

---

## 5. Layout-Specific Notes

### Phone Portrait

**Status:** Best it's been. The compact drawer model is correct.

| Aspect | Assessment |
|--------|-----------|
| Trust spine visibility | âœ… Strong â€” immediately visible in header rail |
| Answer body fold position | âš ï¸ Starts too low due to hero panel (see Problem 1) |
| Source access | âœ… Inline chips above body, expandable proof drawer |
| Why panel | âš ï¸ Correct behavior (collapsed drawer), but visually too quiet |
| Follow-up composer | âœ… Compact `Ask follow-up... | Go` is right |
| Drawer toggles | âœ… `Show/Hide` interaction model works well |
| Density | âœ… Good balance â€” not overwhelming, not barren |

**Screenshot reference:** `portrait_phone_density_compact_20260414/top.png` shows the hero panel consuming real estate above the answer.

### Phone Landscape

**Status:** Materially improved. Compact meta rail and section suppression work well.

| Aspect | Assessment |
|--------|-----------|
| Header rail | âœ… Terse and effective: `Instant â€¢ Strong evidence â€¢ 3 src â€¢ 1 turn` |
| Answer density | âœ… Good fold position after hero compaction |
| Result list density | âœ… Two-line compact rows with 5+ visible rows |
| Follow-up composer | âœ… Compact strip |
| Emergency suppression | âœ… Bogus helper chips now suppressed for medical answers |

### Tablet Portrait ("Clipboard")

**Status:** Functional, room to grow.

| Aspect | Assessment |
|--------|-----------|
| Reading column | âœ… Centered, calm, readable |
| Trust spine | âœ… Present in header |
| Lower support panels | ðŸ”¶ Okay below reading flow, but could benefit from the same collapsed-drawer model as phone |
| Source provenance | âœ… Provenance preview present |

### Tablet Landscape ("Station")

**Status:** The strongest posture. The utility rail is genuinely useful.

**Screenshot reference:** `tablet_detail_5556_20260414_v4/detail.png` shows the full station layout.

| Aspect | Assessment |
|--------|-----------|
| Left reading column | âœ… Clean, answer-focused, no competing utility chips |
| Right utility rail order | âœ… Guides â†’ Source preview â†’ (helpers collapsed) |
| Provenance preview | âœ… Selected source shows cited excerpt with `Open full guide` |
| Source indexed buttons | âœ… `1/3`, `2/3`, `3/3` numbering is clear |
| Helper section collapse | âœ… `Thread context | Show` is correct subordination |
| Follow-up | âœ… Full composer with context copy |
| Why panel | ðŸ”¶ Body text visible but title pill hidden â€” acceptable for now but consider restoring it as a section heading in the rail |

---

## 6. Trust Cue Assessment

| Cue | Phone Portrait | Phone Landscape | Tablet Portrait | Tablet Landscape | Verdict |
|-----|---------------|-----------------|-----------------|------------------|---------|
| Route family | âœ… In header rail | âœ… In header rail | âœ… In header rail | âœ… In header rail | **Strong** |
| Anchor guide | âœ… Chip in header | âœ… Chip in header | âœ… Chip in header | âœ… Chip + `Anchor GD-xxx` | **Strong** |
| Evidence strength | âœ… In header rail | âœ… In header rail | âœ… In header rail | âœ… In header rail | **Strong** |
| Source count | âœ… `N src` | âœ… `N src` | âœ… `N sources` | âœ… `N sources` | **Strong** |
| Source preview / provenance | âœ… Expandable drawer | âš ï¸ Present but space-constrained | âœ… Below reading flow | âœ… Inline rail preview | **Adequate** |
| Why this answer | âš ï¸ Present but visually quiet | âš ï¸ Compacted, visible | âš ï¸ Present | âœ… In rail | **Adequate, room to strengthen** |
| No-citation honesty | âœ… Explicit `No citations` + `Anchor unavailable` | âœ… Same | âœ… Same | âœ… Same | **Excellent** |

> [!TIP]
> The trust cue system is one of the strongest aspects of this app. The only improvement needed is making the "Why this answer" surface slightly more visually prominent in its collapsed stateâ€”not more content, just a clearer visual affordance that it's there.

---

## 7. Density Assessment

**Phone portrait:** Just right. The compact drawer system keeps secondary content available without stacking it vertically. The next focus is resilience and source visibility while answers build, not more density trimming.

**Phone landscape:** Well-tuned. Result cards are appropriately terse, header rail is compact, helper noise is suppressed.

**Tablet portrait:** Slightly sparse. Could benefit from the collapsed-drawer treatment that phone portrait now uses, rather than fully-expanded inline panels.

**Tablet landscape:** Near-perfect density. The station rail uses space well. The collapsible helper section is the right call.

---

## Summary

This is a survival knowledge tool that looks and behaves like a survival knowledge tool. The trust surface is honest, the source navigation is first-class, and the posture model is real. The next highest-leverage work is the source-backed slow-answer resilience queue that keeps weak or stalled answers visibly anchored while they are still building.

After that, the next tier of improvements would be:
1. Deepen provenance/source interaction once the stall-recovery posture settles
2. Keep the severity accents subtle but consistent for high-risk routes
3. Revisit any remaining phone landscape chrome density only if it still feels crowded in the next review pass
