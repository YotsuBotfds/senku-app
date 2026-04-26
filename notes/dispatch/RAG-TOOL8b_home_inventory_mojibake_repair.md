# RAG-TOOL8b Home Inventory Mojibake Repair

## Scope

- Repaired only `guides/home-inventory.md`.
- No semantic guide content changes were intended; repairs were limited to reversible mojibake artifacts.

## Repairs

High-confidence mappings applied:

- mojibake em dash -> em dash
- mojibake en dash/ranges -> en dash
- mojibake degree-F -> degree-F
- mojibake multiplication sign -> multiplication sign
- mojibake oxygen subscript in `O2` -> `O₂`
- mojibake checkmark -> checkmark

## Validation

Pre-repair targeted scan:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\home-inventory.md --report-only
```

- Files scanned: `1`
- Findings: `298`
- Gate findings: `298`

Post-repair targeted scan:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B scripts\scan_mojibake.py --paths guides\home-inventory.md --report-only
```

- Files scanned: `1`
- Findings: `0`
- Gate findings: `0`

Incremental ingest:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B ingest.py --files guides\home-inventory.md --force-files
```

- Files processed: `1`
- Re-ingested chunks: `84`
- Total chunks after ingest: `49,737`

Stats:

```powershell
& .\.venvs\senku-validate\Scripts\python.exe -B ingest.py --stats
```

- Collection: `senku_guides`
- Total chunks: `49,737`
- `resource-management`: `1,314` chunks

Diff check:

```powershell
git diff --check
```

- Exit code: `0`
- Note: emitted a line-ending warning for unrelated `notes/dispatch/RAG-TOOL8_mojibake_cleanup_queue.md`.
