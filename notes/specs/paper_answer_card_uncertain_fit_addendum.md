# Spec Addendum: PaperAnswerCard Uncertain-Fit Variant

Date: 2026-04-19
Task: `BACK-U-01`

## Card Contract

- uncertain-fit is a non-abstain answer card state
- header label stays `SENKU ANSWERED`
- evidence pill copy is `UNSURE FIT`
- header icon is a compact warning mark (`!`) instead of the normal evidence dot

## Visual Tokens

- paper background wash: `rgba(196, 154, 75, 0.10)`
- dark background wash: `rgba(196, 154, 75, 0.18)`
- left warning border: paper `paperWarn`, dark `warn`
- do not use the abstain danger wash or danger border

## Body Posture

- first sentence must say the guidance is related but not a confident fit
- keep the answer body in clarification / verification posture
- retain `Show proof`, sources, and provenance behavior

## Distinction Rules

- confident: normal paper card, evidence dot, no warning border
- uncertain-fit: warning wash + warning border + `!` icon + `UNSURE FIT`
- abstain: danger wash + danger border + `NO MATCH`
