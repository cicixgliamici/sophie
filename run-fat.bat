@echo off
REM Batch script per eseguire il fat JAR generato con sbt-assembly
setlocal
set JAR=
for /f "delims=" %%I in ('dir /b /s "%~dp0target\sophie-fat*.jar" 2^>nul') do (
  set JAR=%%I
  goto :found
)
:found
if "%JAR%"=="" (
  echo JAR non trovato in target\. Hai eseguito 'sbt clean assembly'?
  exit /b 1
)
echo Eseguo JAR: %JAR%
java -jar "%JAR%" %*
endlocal
