# scripts/ — PowerShell helpers

## Purpose
The `scripts/` folder groups Windows PowerShell helper scripts used to run tests, TUI simulations, and collect logs/artifacts. They are intended for local usage or CI on Windows runners.

## Contents
- `run-tui-tests.ps1`: runs a full TUI test flow (optional build + test), executes the non-interactive TUI simulation runner (`RunTuiSim`), and collects artifacts/logs into a zip file.
- `run-and-collect-tests.ps1`: runs `sbt test`, captures the full output, and extracts a compact summary for quick inspection.
- `run-integration-tests.ps1`: runs the integration test runner (`integration.RunIntegrationRunner`) and then executes `sbt test`.

## `run-tui-tests.ps1` details

### Purpose
This script executes a complete TUI end-to-end flow: it compiles the project, runs tests, launches the non-interactive runner `RunTuiSim`, and collects output files and logs into an artifacts directory.

### Locations referenced by the script
- Script: `scripts/run-tui-tests.ps1`
- Scala non-interactive runner: `src/main/scala/RunTuiSim.scala`
- Input file used by the runner: `docs/tui_commands.txt`
- Simulation report produced: `tmp/tui_sim_report.json`
- Artifacts directory (default): `tmp/tui_test_artifacts/`
- Artifacts zip: `tmp/tui_test_artifacts/tui_test_artifacts.zip`

### Prerequisites
- Windows PowerShell (the repository uses `powershell.exe`).
- `sbt` installed and available on `PATH`.
- JDK/JRE available (`java -version`).
- Recommended: set the console code page to UTF-8 to avoid encoding issues.

### Quick usage
Open PowerShell in the repository root and run:

```powershell
# Full run (build, test, simulation)
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-tui-tests.ps1

# Skip build/test (useful if already compiled)
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-tui-tests.ps1 -SkipBuild
```

### What the script does (high-level steps)
1. Sets the console code page to UTF-8 (`chcp 65001`) so output is readable.
2. (Default) Runs `sbt clean compile` and saves output to `tmp/tui_test_artifacts/sbt_compile_output.txt`.
3. (Default) Runs `sbt test` and saves output to `tmp/tui_test_artifacts/sbt_test_output.txt`.
4. Runs the Scala runner `RunTuiSim` using `sbt "runMain RunTuiSim"` and saves output to `tmp/tui_test_artifacts/run_tuisim_output.txt`.
5. Copies useful directories and files into `tmp/tui_test_artifacts/` (`tmp/`, `data/`, `target/global-logging/` when present).
6. Creates a manifest (`manifest.txt`) with the list of collected files.
7. Zips the artifacts directory into `tmp/tui_test_artifacts/tui_test_artifacts.zip`.

### Script parameters
- `-OutDir <path>`: destination for artifacts (default: `.	mp\tui_test_artifacts`).
- `-SkipBuild`: skip `sbt clean compile` and `sbt test`.

### Expected outputs (important files)
- `tmp/tui_sim_report.json`: JSON report from `RunTuiSim` with the input list, final portfolio, and whether a plan exists.
- `tmp/last_prog.sophie`, `tmp/ir.json`, `tmp/pf.json`, etc. (if produced by the simulation).
- `data/portfolio.json` and `data/ledger.ndjson` (if `:exec ir` is used during the simulation).
- `tmp/tui_test_artifacts/run_tuisim_output.txt`: console output from the simulation.
- `tmp/tui_test_artifacts/sbt_test_output.txt`: sbt test output.
- `tmp/tui_test_artifacts/manifest.txt`: complete list of collected files.

### Known issues & quick troubleshooting
1. Garbled characters/accents in the console:
   - Run `chcp 65001` before executing the script and, if needed, start sbt with `-J-Dfile.encoding=UTF-8`.
2. Java warnings (sun.misc.Unsafe, JNA): these are expected on newer JDKs and do not block execution.
3. "Unknown command" in the TUI: usually due to typos or missing spaces (for example `:set ovr RSI MSFT 1425` instead of `14 25`). Check syntax in `docs/tui_commands.txt`.
4. `:exec ir` writes to disk (`data/`) but does not refresh the in-memory portfolio. To update the interactive session after `:exec`, run `:pf load data/portfolio.json`.
5. If sbt logs are not captured correctly, the script writes logs using `Out-File -Encoding utf8`.

### Quick inspection commands (examples)
```powershell
# View the simulation JSON report
Get-Content .\tmp\tui_sim_report.json -Raw | Out-String

# Check the portfolio written to disk
Get-Content .\data\portfolio.json -Raw

# View the first lines of the ledger
Get-Content .\data\ledger.ndjson | Select-Object -First 20

# Check the artifacts manifest
Get-Content .\tmp\tui_test_artifacts\manifest.txt -Raw

# Open the artifacts zip
# Windows Explorer: start .\tmp\tui_test_artifacts\tui_test_artifacts.zip
```
