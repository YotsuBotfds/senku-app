# GPU Crash Handoff — 2026-04-08

## Current State

- All Senku bench activity is stopped.
- There is no live ESTABLISHED connection to `:1234` from this workspace right now.
- The repo is not the primary suspect anymore.

## What Was Fixed In Senku

- `bench.py` retry handling was fixed so duplicate same-host URLs no longer deadlock the queue.
- Guarded bench runner cleanup works when the wrapper is killed.
- Deterministic control coverage was expanded substantially for the recent Gate weak spots.
- High-risk prompt validation was rerun successfully after those fixes.

## Strongest Repo Evidence

These artifacts were clean after the latest repo-side fixes:

- `bench_google_gemma4_4090_single_smoke_w1_20260408.md`
- `bench_google_gemma4_4090_single_smoke_w1_20260408.json`
- `bench_google_gemma4_4090_gate_highrisk_post_patch_r2_w2_20260408.md`
- `bench_google_gemma4_4090_gate_highrisk_post_patch_r2_w2_20260408.json`

Key result from the high-risk rerun:

- `8/8` successful
- `0` errors
- `decision_path_counts = {'rag': 1, 'deterministic': 7}`
- max completion tokens in that pack dropped to `1729`

That means the remaining instability is no longer explained by the original bench deadlock bug.

## Why This Looks Hardware / Runtime Side

Observed behavior:

- LM Studio generation caused full-PC resets.
- After driver update and resetting MSI Afterburner to defaults, the full-PC resets still happened.
- Folding@home also crashed the machine very early.
- A one-prompt / one-worker LM Studio smoke test succeeded, but broader or longer runs became unstable.

Interpretation:

- This is now much more likely to be:
  - GPU hardware / VRAM
  - PSU / transient power delivery
  - GPU power cable / connector / seating
  - motherboard PCIe path
- This is now much less likely to be:
  - Senku code
  - `bench.py`
  - LM Studio alone

## What To Do On The Other Computer

Do these in order.

### 1. Stop Heavy GPU Testing

Do not keep crashing the system repeatedly.

Pause:

- LM Studio heavy loads
- Folding@home
- BOINC GPU tasks

### 2. Check Reliability Monitor

Open:

```text
Win + R -> perfmon /rel
```

Look at the exact crash time and write down:

- `Windows was not properly shut down`
- `Hardware error`
- `LiveKernelEvent`
- `BlueScreen`

If you see `LiveKernelEvent`, record the code.

### 3. Check Event Viewer

Open:

```text
Win + R -> eventvwr.msc
```

Go to:

- `Windows Logs > System`

Use `Filter Current Log...` and inspect the crash time for:

- `Kernel-Power` Event ID `41`
- `EventLog` Event ID `6008`
- `WHEA-Logger`
- `Display`
- `nvlddmkm`
- `volmgr` Event ID `46`

What matters most:

- `Kernel-Power 41`
  - if `BugcheckCode = 0`, that often points to abrupt reset / power / hardware-class failure
- `WHEA-Logger`
  - PCIe / bus / cache / memory hardware errors
- `Display` or `nvlddmkm`
  - GPU driver or GPU path faults
- `volmgr 46`
  - crash dump could not be written

### 4. Check Crash Dumps

Look for:

- `C:\Windows\Minidump`
- `C:\Windows\MEMORY.DMP`

If those are empty and the machine is still resetting hard, that supports the idea that Windows is not reaching a normal bugcheck path.

### 5. Check Physical GPU Power Path

Before more testing:

- fully power the system down
- reseat the GPU
- reseat all GPU power cables
- if using a `12VHPWR` connector or adapter, inspect it carefully and make sure it is fully seated
- reseat PSU-side modular cables too

### 6. Test At Reduced GPU Power

Only after the above:

- set a conservative power limit like `50%` to `60%`
- run a very short GPU load

Interpretation:

- if reduced power stops the crashes, PSU / power transient suspicion goes up
- if reduced power still crashes early, GPU / PCIe / board suspicion goes up

### 7. Best Swap Tests

If possible, the most useful component isolation order is:

1. another PSU
2. another GPU in this machine
3. this GPU in another machine

## Safe Bring-Up Sequence After Hardware Checks

Do not jump back into broad Sentinel or Coverage.

Use this exact order:

1. Manual LM Studio chat reply
2. Single-prompt, single-worker repo smoke
3. Two-prompt, single-worker batch
4. More tiny batches
5. Only then consider larger benches

### Smallest Repo Smoke Command

```bash
zsh scripts/run_bench_guarded.sh \
  --prompts tmp_prompt_single_smoke_generation_20260408.txt \
  --output bench_google_gemma4_4090_single_smoke_post_hardwarecheck_w1_20260408.md \
  --urls http://192.168.0.67:1234/v1 \
  --embed-url http://localhost:1234/v1
```

## Fill-In Template

Add findings here directly on the other machine if that is easier than pasting them elsewhere.

### Crash Time

- 

### Reliability Monitor

- 

### Kernel-Power 41

- 

### WHEA-Logger

- 

### Display / nvlddmkm

- 

### volmgr / dump status

- 

### Power Limit Test Result

- 

### Physical Inspection Notes

- 

### Other Observations

-