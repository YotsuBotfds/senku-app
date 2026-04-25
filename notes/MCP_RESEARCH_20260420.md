# MCP Research — 2026-04-20

Research brief for what MCP servers are currently valuable for Senku.
Ran for both the Claude Code (Opus planner) side and the OpenCode
(Codex executor) side. Filtered for: maturity, Windows 11
compatibility, Senku-specific workflow fit (not generic utility).

Scope intentionally narrow. Does not re-recommend what Senku already
has in `opencode.json` (context7, git, sequential-thinking,
puppeteer). Cites sources inline for verification.

## Net recommendations (do these)

1. **Replace puppeteer → Playwright in `opencode.json`**.
   `@modelcontextprotocol/server-puppeteer` is **archived and
   deprecated**. `@playwright/mcp` (Microsoft-official) is the
   direct successor with accessibility-tree output that's more
   stable for the `artifacts/external_review/` gallery viewers
   than pixel scraping.
   Install for OpenCode: add `"playwright": { "command":
   ["npx", "@playwright/mcp@latest"] }` to `opencode.json`.
   Windows: first-class support.
2. **Add Serena to Claude Code side** for cross-boundary
   Python/Java semantic navigation with LSP.
   Windows workarounds required: `MCP_TIMEOUT=60000`,
   `--context claude-code`, disable TypeScript LSP to avoid
   issue #368. Stable for Python/Java.
3. **Add Basic Memory indexed over `notes/`** for semantic recall
   over handoff history. `notes/` has 50+ markdown files
   (handoffs, dispatches, trackers, root-cause docs). Basic
   Memory keeps files as plain markdown, builds a SQLite +
   FastEmbed semantic index on top. No vendor lock-in.
   Windows: `just test-windows` target exists; supported.

Optional 4th: **Chroma MCP (official, chroma-core/chroma-mcp)**.
Only add if debug-level retrieval inspection over the 49k-chunk
guide index becomes frequent. Today's throwaway Python scripts
work fine.

## Security / hygiene to action now

- **Upgrade or remove the `git` MCP in `opencode.json`.** Three
  CVEs landed January 2026 on the Anthropic MCP Git server
  enabling prompt-injection-driven file access. Either upgrade
  to the latest patched release or fall back to `gh` + `git`
  CLI via Bash (Senku already uses both heavily, so removal is
  low-cost). Source: [Hacker News coverage](https://thehackernews.com/2026/01/three-flaws-in-anthropic-mcp-git-server.html).
- **context7 quota change.** Upstash dropped the free tier from
  6k → 1k requests/month in January 2026. If the cap starts
  biting, **Docfork** (MIT, 9k+ libraries, open source) is the
  strongest free alternative.

## Anti-recommendations

Skip these even though they sound relevant to Senku:

- **Every Android-ADB MCP.** `srmorete/adb-mcp` is archived
  (April 2026). `mobile-next/mobile-mcp` (the successor) still
  lacks logcat streaming and multi-serial selection, so the
  fixed 4-emulator matrix workflow is not well-served. The
  existing PowerShell scripts under `scripts/` are more
  capable. Watch for 2026-Q3 if they add logcat.
- **Every SQLite MCP.** Anthropic's official server was archived
  May 2025. Community options (`jparkerweb/mcp-sqlite`,
  `hannesrudolph/sqlite-explorer-fastmcp`) are single-maintainer
  early-stage. For 285MB `senku_pack.db` the `sqlite3` CLI via
  Bash is more reliable.
- **GitHub MCP Server.** Benchmarks show `gh` CLI is 10-32× cheaper
  and 100% reliable vs the MCP's ~72% reliability. Senku's
  cadence (mostly local commits, occasional PR) does not
  justify the switch. Source: [Mario Zechner benchmark](https://mariozechner.at/posts/2025-08-15-mcp-vs-cli/).
- **Knowledge Graph Memory** (`@modelcontextprotocol/server-memory`).
  Designed for progressive conversation-time accumulation of
  entities/relations, not bulk ingestion of existing markdown.
  Wrong shape for Senku's `notes/`.
- **Filesystem MCP.** EscapeRoute CVEs (CVE-2025-53109/53110)
  pre-2025.7.1. Built-in Read/Grep/Glob tools already cover
  everything, with no CVE exposure.

## Watch list (not ready yet)

- **mobile-next/mobile-mcp** — actively developed, 4.6k stars,
  but tool surface (`mobile_list_apps`, `mobile_take_screenshot`,
  `mobile_list_elements_on_screen`) is shallow for Senku's
  logcat-heavy + multi-serial workflow. Check again 2026-Q3.
- **Code Pathfinder** (codepathfinder.dev/mcp) — AST + call-graph
  + dataflow. Promising but young. Wait for production
  validation.

## Workflows with no mature MCP answer

Worth stating explicitly so we don't hunt for one:

- Logcat structured extraction with timeline correlation for
  `ask.prompt` / `ask.generate` / `ask.abstain` events. Current
  log-analyzer MCPs work on JSON logs, not semi-structured
  logcat. Keep PowerShell + grep.
- Batch gallery comparison (45×4 screenshot matrix).
  `mcp-image-compare-server` is single-pair pixel diff, 3 stars,
  immature. Existing PowerShell is better.
- Android emulator-matrix management with logcat streaming.
  Nothing ready.

## Detailed per-MCP notes

### High-value for Claude Code (Opus planner)

**Serena** — `oraios/serena`, https://github.com/oraios/serena

LSP-backed symbol-level semantic toolkit for Python and Java.
Why Senku: cross-boundary planning queries like "who calls
`DetailAnswerPresentationFormatter` across Android + Python"
beat Grep at the semantic level. Symbol-level go-to-definition,
find-references, rename.

- Install: `uv tool install -p 3.13 serena-agent@latest --prerelease=allow`
- Add stdio entry in Claude Code MCP config with `--project-from-cwd`
- Windows caveats: TypeScript LSP install fails on Windows
  ([issue #368](https://github.com/oraios/serena/issues/368)),
  tool exposure sometimes fails on Claude Code v2.x
  ([issue #780](https://github.com/oraios/serena/issues/780)).
  Set `MCP_TIMEOUT=60000`, force `--context claude-code`,
  disable TypeScript LSP.
  ([Paul George's fix guide](https://blog.paulgeorge.co.uk/2026/01/21/fix-serena-plugin-issues-in-claude-code/))
- Maturity: v1.1.2 released 2026-04-14, 23.2k stars, 2,688
  commits. Stable for Python/Java.

**Basic Memory** — `basicmachines-co/basic-memory`, https://github.com/basicmachines-co/basic-memory

Local-first markdown knowledge graph; bidirectional sync
between `.md` files and a SQLite + FastEmbed semantic index.
Why Senku: 50+ handoff/dispatch/tracker files under `notes/`.
Semantic recall over historical handoffs without touching the
files themselves.

- Install: `uv tool install basic-memory`, point at `notes/`.
- Windows: `just test-windows` target exists, confirmed.
- Maturity: v0.20.3 (2026-03-27), 2.9k stars, 77 releases,
  1,279 commits.

**Chroma MCP (official)** — `chroma-core/chroma-mcp`, https://github.com/chroma-core/chroma-mcp

Official Chroma-Core-maintained server exposing
`chroma_list_collections`, `chroma_get_collection_count`,
`chroma_peek_collection`, `chroma_query_documents` over a
persistent local instance.
Why Senku: direct introspection of the 49k-chunk guide index.
Debugging retrieval regressions (the S2-rerun placeholder issue,
R-pack poisoning coverage) would benefit from structured peek
+ metadata filter + count without writing throwaway Python.

- Install: `"command": "uvx", "args": ["chroma-mcp",
  "--client-type", "persistent", "--data-dir",
  "C:\\Users\\tateb\\...\\chroma_db"]`
- Maturity: v0.2.6 family, official, last PyPI version Aug
  2025 — slower cadence; confirm version compatibility with
  your Chroma client.

### High-value for OpenCode (Codex executor)

**Playwright MCP (Microsoft)** — `microsoft/playwright-mcp`, https://github.com/microsoft/playwright-mcp

Direct replacement for the deprecated puppeteer server.
Browser-only — does NOT automate Android emulators, so this is
not a replacement for ADB tooling. It's for the HTML gallery
viewers under `artifacts/external_review/`.

- Install for OpenCode: `"playwright": { "command":
  ["npx", "@playwright/mcp@latest"] }` in `opencode.json`.
- Install for Claude Code: `claude mcp add playwright npx
  @playwright/mcp@latest`.
- Windows: first-class support, explicit Windows paths
  documented.
- Maturity: v0.0.70 (2026-04-01), 31.1k stars, Microsoft-
  official.

## Delta on existing four MCPs

- **context7**: Free tier cut to 1k req/month Jan 2026. Watch
  quota; Docfork is the drop-in free alternative.
  ([DEV alternatives post](https://dev.to/moshe_io/top-7-mcp-alternatives-for-context7-in-2026-2555))
- **git**: Jan 2026 CVEs on Anthropic MCP Git server. Upgrade
  or remove in favor of `git` CLI.
- **sequential-thinking**: Still maintained. Diminishing returns
  since Claude 4.x reasons structurally. Not urgent to remove.
- **puppeteer**: Archived. Replace with Playwright.

## Sources

- [modelcontextprotocol/servers (official)](https://github.com/modelcontextprotocol/servers)
- [modelcontextprotocol/servers-archived](https://github.com/modelcontextprotocol/servers-archived)
- [Official MCP Registry](https://registry.modelcontextprotocol.io/)
- [Serena repo](https://github.com/oraios/serena)
- [Serena Windows issue #368](https://github.com/oraios/serena/issues/368)
- [Serena Claude Code v2.x issue #780](https://github.com/oraios/serena/issues/780)
- [Paul George: Fix Serena Plugin Issues in Claude Code](https://blog.paulgeorge.co.uk/2026/01/21/fix-serena-plugin-issues-in-claude-code/)
- [Chroma MCP (official)](https://github.com/chroma-core/chroma-mcp)
- [Basic Memory repo](https://github.com/basicmachines-co/basic-memory)
- [Microsoft Playwright MCP](https://github.com/microsoft/playwright-mcp)
- [Playwright vs Puppeteer 2026](https://www.morphllm.com/comparisons/playwright-vs-puppeteer)
- [Puppeteer deprecation thread](https://community.latenode.com/t/looking-for-working-mcp-server-replacements-after-puppeteer-deprecation/26681)
- [GitHub MCP Server](https://github.com/github/github-mcp-server)
- [MCP vs CLI benchmark](https://mariozechner.at/posts/2025-08-15-mcp-vs-cli/)
- [srmorete/adb-mcp archived](https://github.com/srmorete/adb-mcp)
- [mobile-next/mobile-mcp](https://github.com/mobile-next/mobile-mcp)
- [Knowledge Graph Memory Server](https://github.com/modelcontextprotocol/servers/tree/main/src/memory)
- [jparkerweb/mcp-sqlite](https://github.com/jparkerweb/mcp-sqlite)
- [hannesrudolph/sqlite-explorer-fastmcp](https://github.com/hannesrudolph/sqlite-explorer-fastmcp-mcp-server)
- [MCP Git Server CVEs Jan 2026](https://thehackernews.com/2026/01/three-flaws-in-anthropic-mcp-git-server.html)
- [EscapeRoute filesystem MCP CVEs](https://cymulate.com/blog/cve-2025-53109-53110-escaperoute-anthropic/)
- [Context7 alternatives 2026](https://dev.to/moshe_io/top-7-mcp-alternatives-for-context7-in-2026-2555)
- [mcp-image-compare-server](https://github.com/leky90/mcp-image-compare-server)
- [wong2/awesome-mcp-servers](https://github.com/wong2/awesome-mcp-servers)