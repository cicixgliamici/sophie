# PowerShell script per eseguire il fat JAR generato con sbt-assembly
$jarCandidates = Get-ChildItem -Path "$PSScriptRoot\target" -Recurse -Filter "sophie-fat*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $jarCandidates) {
    Write-Error "JAR non trovato in target/. Hai eseguito 'sbt clean assembly'?"
    exit 1
}
$jar = $jarCandidates.FullName
Write-Host "Eseguo JAR: $jar"
java -jar "$jar" $args

