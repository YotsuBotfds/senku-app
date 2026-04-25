# Wave A Closeout Failure - 2026-04-18

## Failing task

- `BACK-P-02-FAIL` - stock install-path validation failed before corrupted-pack cases could be exercised

## Impact on remaining closeout tasks

- `BACK-P-01` was not meaningfully runnable after the first red. The stock app-data reset never reached a healthy installed-pack state, so FTS runtime selection could not be validated on a trustworthy baseline.
- `BACK-R-03` was not executed after the first red, per the closeout instruction to stop and return control on any validation failure.

## Repro

Device:
- `emulator-5556` phone portrait

Steps:
1. Clear app data: `adb -s emulator-5556 shell pm clear com.senku.mobile`
2. Launch app with stock assets: `adb -s emulator-5556 shell am start -n com.senku.mobile/.MainActivity --es auto_query 'how%20do%20i%20build%20a%20house'`
3. Wait for initial install path to settle

Observed:
- Main screen shows `Manual install failed`
- Error text shows:
  `java.io.IOException: Installed senku_mobile.sqlite3 checksum mismatch. Expected 782c5015d5fd31e2d363f06284b4ef9c21c0f40963b1719d1df6b20981977fad but found 12100b07ab1b0c6f998217d0168584021dd8755e04482df29378381224d55881`

## Decisive evidence

- Screenshot: [stock_install_failure_5556.png](/C:/Users/tateb/Documents/senku_local_testing_bundle_20260410/artifacts/bench/wave_a_closeout_20260418/back_p_02/stock_install_failure_5556.png)
- UI dump: [window_dump_stock_install_failure_5556.xml](/C:/Users/tateb/Documents/senku_local_testing_bundle_20260410/artifacts/bench/wave_a_closeout_20260418/back_p_02/window_dump_stock_install_failure_5556.xml)

## Triage hypothesis

- The checked-in stock asset SQLite file no longer matches the manifest checksum.
- Host-side confirmation:
  - asset SHA-256: `12100b07ab1b0c6f998217d0168584021dd8755e04482df29378381224d55881`
  - manifest SHA-256: `782c5015d5fd31e2d363f06284b4ef9c21c0f40963b1719d1df6b20981977fad`
- Because the clean install path fails immediately on the stock asset pack, `BACK-P-02` cannot be closed and `BACK-P-01` cannot be trusted until the asset-pack/manifest mismatch is resolved.
