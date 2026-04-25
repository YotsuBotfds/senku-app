## Orchestrator Routing Discipline

Purpose: keep active OPUS execution aligned with repo guidance while multiple lanes are live.

### Model / agent split

- Scout or read-only investigation:
  - prefer `gpt-5.3-codex-spark`
  - use `xhigh` only for bounded scouting or comparison work
  - do not use edit workers for scouting if a scout lane will do

- Edit work:
  - prefer `gpt-5.4`
  - keep reasoning at `high`
  - reserve workers for concrete code/content changes with a clear write set

### Concurrency rules

- Never edit files locally while a worker owns them.
- If direction changes mid-lane, either:
  - let the worker finish, or
  - explicitly interrupt / stop that worker before rerouting.
- Do not open overlapping write lanes just because capacity is available.

### Dispatch checklist

Before every spawn:

1. Is this scouting or editing?
2. If scouting, can Spark handle it instead of a general worker?
3. If editing, is the write set disjoint from all active lanes?
4. Is the task bounded enough to validate cleanly on return?
5. Am I following the repo preference instead of taking the fastest available shortcut?

### Current correction

- No new generic heavy worker spawns while the current active lanes finish.
- Next scout dispatch should use Spark.
- Next edit dispatch should use `gpt-5.4` at `high`.
