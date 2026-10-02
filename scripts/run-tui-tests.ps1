# Run a full TUI test flow: build, test, simulate TUI, collect artifacts
# Usage: .\scripts\run-tui-tests.ps1 (run from repository root on Windows PowerShell)
#
# Run this script locally or in CI on Windows.
# - Set the console to UTF-8 to avoid encoding problems.
# - Optionally build the project and run the tests.
# - Run the non-interactive TUI simulation.
# - Collect output files from tmp/, data/, and target/global-logging.
# - Create a manifest and a ZIP archive of the collected artifacts.

param(
    [string]$OutDir = ".\tmp\tui_test_artifacts",
    [switch]$SkipBuild
)

# Ensure output dir
if (-not (Test-Path -Path $OutDir)) { New-Item -Path $OutDir -ItemType Directory | Out-Null }

# 1) Set the console code page to UTF-8.
Write-Host "1/7 - Setting console to UTF-8 (chcp 65001)"
chcp 65001 > $null

# 2) Optionally build and test; capture UTF-8 logs for readable diagnostics.
if (-not $SkipBuild) {
    Write-Host "2/7 - Running: sbt clean compile"
    # Capture the complete compilation output before saving it as UTF-8.
    $compileOut = sbt clean compile 2>&1 | Out-String
    $compileOut | Out-File -FilePath (Join-Path $OutDir "sbt_compile_output.txt") -Encoding utf8
    Write-Host $compileOut

    Write-Host "3/7 - Running: sbt test"
    # Capture test output so the bundle includes failures and summaries.
    $testOut = sbt test 2>&1 | Out-String
    $testOut | Out-File -FilePath (Join-Path $OutDir "sbt_test_output.txt") -Encoding utf8
    Write-Host $testOut
} else {
    Write-Host "Skipping build/test as requested"
}

# 3) Run the non-interactive TUI simulation.
Write-Host "4/7 - Running TUI simulation runner (RunTuiSim)"
# Invoke the simulation runner explicitly instead of using a command alias.
$runOut = sbt "runMain RunTuiSim" 2>&1 | Out-String
$runOut | Out-File -FilePath (Join-Path $OutDir "run_tuisim_output.txt") -Encoding utf8
Write-Host $runOut

# 4) Copy the relevant output directories into the artifact bundle.
Write-Host "5/7 - Collecting artifacts (tmp/, data/, target/global-logging/ and script outputs)"
$pathsToCollect = @("tmp", "data", "target/global-logging")
foreach ($p in $pathsToCollect) {
    if (Test-Path -Path $p) {
        $dest = Join-Path $OutDir (Split-Path $p -Leaf)
        Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $dest
        Write-Host "Copying $p -> $dest"
        Copy-Item -Path $p -Destination $dest -Recurse -Force -ErrorAction SilentlyContinue
    } else {
        Write-Host "Path not found, skipping: $p"
    }
}

# 5) Manifest and packaging
Write-Host "6/7 - Writing quick manifest of collected files"
Get-ChildItem -Path $OutDir -Recurse | Select-Object FullName, Length | Out-File -FilePath (Join-Path $OutDir "manifest.txt") -Encoding utf8

Write-Host "7/7 - Packaging artifacts into zip"
$zipPath = Join-Path $OutDir "tui_test_artifacts.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
Add-Type -AssemblyName System.IO.Compression.FileSystem
[IO.Compression.ZipFile]::CreateFromDirectory((Get-Item $OutDir).FullName, $zipPath)

Write-Host "Done. Artifacts and logs collected in $OutDir and zipped to $zipPath"
Write-Host "You can inspect tmp/tui_sim_report.json for the simulation report."
