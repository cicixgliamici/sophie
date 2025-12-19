# Run sbt test, capture full output and produce a compact summary for quick inspection
# Usage: .\scripts\run-and-collect-tests.ps1 (run from repo root on Windows PowerShell)

# Ensure tmp directory exists
if (-not (Test-Path -Path .\tmp)) { New-Item -Path .\tmp -ItemType Directory | Out-Null }

$fullLog = Join-Path -Path .\tmp -ChildPath "sbt_test_output.txt"
$summary = Join-Path -Path .\tmp -ChildPath "sbt_test_summary.txt"

Write-Host "Running: sbt test (output -> $fullLog)"
# Run sbt test and capture full output
sbt test 2>&1 | Tee-Object -FilePath $fullLog

# Try to extract common ScalaTest summary lines
$lines = Get-Content -Path $fullLog -ErrorAction SilentlyContinue
if ($null -eq $lines -or $lines.Count -eq 0) {
    Write-Host "No sbt output captured in $fullLog"
    "No output captured." | Out-File -FilePath $summary -Encoding utf8
    exit 1
}

# Patterns commonly containing totals and failures
$patterns = @(
    'Total number of tests run',
    'Tests run:',
    'Failures:',
    'Errors:',
    'Skipped:',
    'Failed: ',
    'There were \d+ failed',
    'sbt.TestFramework',
    '>>> FAILURE',
    'java.lang.AssertionError',
    'java.lang.Exception'
)

$matches = @()
foreach ($p in $patterns) {
    $matches += Select-String -Path $fullLog -Pattern $p -SimpleMatch -ErrorAction SilentlyContinue | ForEach-Object { $_.Line }
}

if ($matches.Count -gt 0) {
    $matches | Out-File -FilePath $summary -Encoding utf8
    Write-Host "Wrote summary with $($matches.Count) matching lines to $summary"
} else {
    # No summary lines detected: write the last 200 lines of the log
    $tail = if ($lines.Count -gt 200) { $lines[-200..-1] } else { $lines }
    $tail | Out-File -FilePath $summary -Encoding utf8
    Write-Host "No recognizable summary lines found; wrote last $($tail.Count) lines to $summary"
}

Write-Host "Done. Full log: $fullLog, Summary: $summary"
