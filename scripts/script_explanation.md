# script_explanation — spiegazione dello script `run-tui-tests.ps1`

Scopo
-----
Questo documento spiega come usare lo script PowerShell `scripts/run-tui-tests.ps1` che esegue una batteria di test TUI end-to-end: compila il progetto, esegue i test, lancia il runner non-interattivo `RunTuiSim`, e raccoglie i file di output e i log in una cartella di artefatti.

Dove si trova
------------
- Script: `scripts/run-tui-tests.ps1`
- Runner Scala non-interattivo: `src/main/scala/RunTuiSim.scala`
- File di input usato dal runner: `docs/tui_commands.txt`
- Report di simulazione prodotto: `tmp/tui_sim_report.json`
- Cartella artefatti raccolti (default): `tmp/tui_test_artifacts/`
- Zip degli artefatti: `tmp/tui_test_artifacts/tui_test_artifacts.zip`

Prerequisiti
------------
- Windows PowerShell (si usa `powershell.exe` nel repository). 
- sbt installato e accessibile dalla PATH.
- JDK/JRE (controlla con `java -version`).
- Consigliato: impostare la code page della console su UTF-8 per evitare problemi di encoding (accenti) prima di eseguire lo script.

Uso rapido
----------
Apri PowerShell nella root del repository e lancia:

```powershell
# esecuzione completa (build, test, simulazione)
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-tui-tests.ps1

# oppure saltando la build/test (utile se hai già compilato)
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\run-tui-tests.ps1 -SkipBuild
```

Cosa fa lo script (passi principali)
-----------------------------------
1. Imposta la code page su UTF-8 (chcp 65001) per rendere l'output leggibile.
2. (default) Esegue `sbt clean compile` e salva l'output in `tmp/tui_test_artifacts/sbt_compile_output.txt`.
3. (default) Esegue `sbt test` e salva l'output in `tmp/tui_test_artifacts/sbt_test_output.txt`.
4. Esegue il runner Scala `RunTuiSim` con `sbt "runMain RunTuiSim"` e salva l'output in `tmp/tui_test_artifacts/run_tuisim_output.txt`.
5. Copia in `tmp/tui_test_artifacts/` le directory e i file utili (cartelle `tmp/`, `data/`, `target/global-logging/` quando presenti).
6. Crea un manifesto (`manifest.txt`) con il dettaglio dei file raccolti.
7. Comprime il contenuto di `tmp/tui_test_artifacts/` in `tmp/tui_test_artifacts/tui_test_artifacts.zip`.

Parametri dello script
----------------------
- `-OutDir <path>`: percorso di destinazione per gli artefatti (default: `.\tmp\tui_test_artifacts`).
- `-SkipBuild`: flag; se presente salta i passi `sbt clean compile` e `sbt test` (utile se si esegue frequentemente la sola simulazione).

Cosa aspettarsi (file importanti)
---------------------------------
- `tmp/tui_sim_report.json`: report JSON prodotto da `RunTuiSim` con la lista di input usati, il portafoglio finale e se è presente un piano.
- `tmp/last_prog.sophie`, `tmp/ir.json`, `tmp/pf.json` ecc. (se prodotti dalla simulazione).
- `data/portfolio.json` e `data/ledger.ndjson` (se `:exec ir` è stato invocato durante la simulazione).
- `tmp/tui_test_artifacts/run_tuisim_output.txt`: output console della simulazione.
- `tmp/tui_test_artifacts/sbt_test_output.txt`: output dei test sbt.
- `tmp/tui_test_artifacts/manifest.txt`: elenco completo dei file raccolti.

Problemi noti e troubleshooting rapido
-------------------------------------
1. Caratteri corrotti/accents (es. "Quantit├á") nella console:
   - Prima di eseguire lo script: `chcp 65001` e avviare sbt con `-J-Dfile.encoding=UTF-8` se necessario.
2. Warning Java (sun.misc.Unsafe, JNA): è normale su JDK recenti; non bloccano l'esecuzione.
3. Comandi "Unknown command" nella TUI: spesso sono dovuti a typo o argomenti non separati da spazi (es. `:set ovr RSI MSFT 1425` era `14 25` unito). Controllare sintassi in `docs/tui_commands.txt`.
4. `:exec ir` salva su disco (`data/`), ma non aggiorna automaticamente il gestore di portafoglio in memoria nella TUI; per vedere subito il risultato nella sessione interattiva dopo `:exec` eseguire `:pf load data/portfolio.json`.
5. Se i log sbt non vengono catturati correttamente (caratteri non leggibili), lo script ora salva i log usando `Out-File -Encoding utf8`.

Come ispezionare rapidamente i risultati (comandi esempio)
---------------------------------------------------------
```powershell
# Visualizza il report JSON della simulazione
Get-Content .\tmp\tui_sim_report.json -Raw | Out-String

# Controlla il portfolio scritto su disco
Get-Content .\data\portfolio.json -Raw

# Visualizza le prime righe del ledger
Get-Content .\data\ledger.ndjson | Select-Object -First 20

# Controlla il manifest degli artefatti
Get-Content .\tmp\tui_test_artifacts\manifest.txt -Raw

# Apri lo zip degli artefatti (es. con explorer o estrailo)
# Windows Explorer: start .\tmp\tui_test_artifacts\tui_test_artifacts.zip
```
