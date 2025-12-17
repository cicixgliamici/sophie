# Sophie

Utility per costruire ed eseguire rapidamente un JAR "fat" (con tutte le dipendenze) dell'applicazione TUI.

## Requisiti
- Java 17 o superiore nel `PATH` (JRE o JDK).
- [sbt](https://www.scala-sbt.org/) raggiungibile come comando `sbt` oppure un runner compatibile (es. immagine Docker hseeberger/scala-sbt).

## Costruire il fat JAR
Il progetto include la configurazione `sbt-assembly` (vedi `build.sbt`) che genera `sophie-fat.jar` in `target/scala-3.3.6/`.
Puoi costruirlo con:

```bash
sbt clean assembly
```

In alternativa è disponibile uno script di comodo:

```bash
./build-fat.sh
```

> Suggerimento: imposta `SBT_CMD` se vuoi usare un comando diverso da `sbt` (es. `SBT_CMD="docker run --rm -v \$(pwd):/code -w /code hseeberger/scala-sbt:17.0.2_1.8.0 sbt" ./build-fat.sh`).

## Eseguire il fat JAR
Dopo la build troverai il JAR in `target/scala-3.3.6/`. Sono disponibili tre script d'avvio che lo individuano automaticamente:

- **Linux/macOS**: `./run-fat.sh`
- **Windows (cmd.exe)**: `run-fat.bat`
- **Windows (PowerShell)**: `./run-fat.ps1`

Gli script accettano eventuali argomenti della TUI e falliscono in modo esplicito se il JAR non è presente.
