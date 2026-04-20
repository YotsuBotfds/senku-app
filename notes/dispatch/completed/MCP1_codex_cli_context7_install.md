# Slice MCP1 — Install context7 MCP in Codex CLI config

- **Role:** main agent (`gpt-5.4 xhigh`). Main inline, single-step.
- **Parallel with:** nothing — fires solo after S2-rerun3 lands.
- **No code changes to Senku source.** This slice touches Codex CLI
  user config only.

## Context

Tate uses Codex CLI daily (not OpenCode). Senku already has
context7 configured on the OpenCode side in `opencode.json` but
that config is inert for day-to-day work because Codex CLI
reads its own MCP config. Porting context7 to Codex CLI gets
you library/API docs grounding during code generation — the
executor's most natural ask.

Research note at `notes/MCP_RESEARCH_20260420.md` evaluated four
candidate MCPs for the Codex CLI side (context7, git,
sequential-thinking, playwright). Planner verdict: **only
context7 is worth installing.** Rationale is in that doc; the
short form is:
- `git` is redundant with the `git` CLI Codex uses natively and
  the Anthropic MCP Git server had three CVEs in Jan 2026.
- `sequential-thinking` adds a reasoning layer that GPT-5.4
  doesn't need (it reasons structurally natively).
- `playwright` has no concrete Senku use case (no web UI, no
  HTML report automation, galleries are eyeballed in a
  browser).

If Codex finds evidence that contradicts this verdict mid-slice
(e.g., an obvious Senku workflow that would benefit from one
of the three), stop and flag to planner rather than adding
anything unscoped.

## Preconditions

1. Codex CLI is the tool running this slice (obviously — but
   verify the CLI binary you're running is `codex`, not
   `opencode` or something else, so the config path you modify
   actually takes effect for Tate's daily usage).
2. `CONTEXT7_API_KEY` environment variable is set in Tate's
   shell environment. `opencode.json` already references it via
   `{env:CONTEXT7_API_KEY}`, so it should be present. If it is
   NOT set, stop and report — do not fabricate a key.

## Outcome

- Codex CLI config has a `context7` MCP entry wired the same way
  `opencode.json` has it: remote transport, URL
  `https://mcp.context7.com/mcp`, bearer token from the
  `CONTEXT7_API_KEY` environment variable.
- A minimal connectivity check confirms Codex CLI can talk to
  the server (e.g., list tools, or successfully resolve one
  library doc lookup).
- No secrets written to disk in plaintext — the API key stays
  an env-var reference, not a hardcoded string.

## The work

### Step 1 — Locate the Codex CLI MCP config

Find where your CLI reads MCP server definitions from. Typical
paths are `~/.codex/config.toml` or similar, but confirm the
actual path your current binary uses by running
`codex --help` / `codex config --help` or whatever the
inspect-config subcommand is. Do not guess the path from
memory.

Record the path you're modifying so the report can cite it.

### Step 2 — Add the context7 entry

Add a `context7` MCP server in the format Codex CLI expects,
using the same intent as `opencode.json`'s entry:

- Transport: remote (HTTP) — Codex CLI's equivalent.
- URL: `https://mcp.context7.com/mcp`
- Auth: `Authorization: Bearer <value from CONTEXT7_API_KEY env>`
- Enabled by default.

Mirror whatever TOML/JSON/YAML shape Codex CLI documents. Do
NOT copy opencode.json verbatim — OpenCode uses a different
schema.

### Step 3 — Verify connectivity

Restart or reload Codex CLI so the config takes effect. Then
run one concrete check:

- List MCP servers / tools via whatever Codex CLI exposes for
  that (commonly `codex mcp list`, `codex mcp status`, or
  similar).
- OR issue one context7 tool call for a known library (e.g.,
  `resolve-library-id` for "chromadb") and confirm a valid
  response comes back.

If the check fails, diagnose — likely causes are: wrong schema
shape (TOML vs JSON mismatch), env-var interpolation syntax
different from OpenCode's `{env:NAME}`, missing restart after
config write, or auth header format.

### Step 4 — Report

No commit. This is a user-config change, not a repo change.

## Boundaries

- Touch only Codex CLI's user config file.
- Do NOT add git, sequential-thinking, or playwright. See
  Context section for the planner rationale.
- Do NOT modify `opencode.json` — that's on a separate config
  lane.
- Do NOT write the API key in plaintext anywhere (config file,
  notes, logs). Use env-var interpolation per Codex CLI's
  convention.
- Do NOT change any Senku source, notes, or dispatch markdown.
- Single atomic edit to the Codex CLI config. If the config file
  doesn't exist yet, create it minimally with just the context7
  entry and required scaffolding.

## Acceptance

- Codex CLI's config file now contains a functional context7
  MCP entry.
- Connectivity check succeeds (one tool call or equivalent).
- `CONTEXT7_API_KEY` referenced via env-var indirection, not
  hardcoded.
- `git status` in the Senku repo shows no changes (config is
  in Tate's user home, not the repo).

## Report format

Reply with:
- Codex CLI config file path modified.
- The shape of the context7 entry added (redact the env-var
  value — just confirm it's an env-var reference).
- Connectivity check command and its result.
- Any schema differences you encountered vs OpenCode's
  `{env:CONTEXT7_API_KEY}` pattern (so this can be codified for
  future Codex-side MCPs).
- Any caveat worth flagging (e.g., rate-limit headers observed,
  restart required, version mismatch notice).
- Delegation log (expected: "none; main inline").
