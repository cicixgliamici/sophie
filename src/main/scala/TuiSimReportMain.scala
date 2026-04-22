// TuiSimReportMain.scala
// ----------------------
// Runner non-interattivo per la TUI che legge gli input da `docs/tui_commands.txt`,
// esegue `SophieTui.simulateSession` e scrive un report JSON in `tmp/tui_sim_report.json`.
// Scopo: fornire un harness ripetibile per smoke tests / CI senza aprire la console interattiva.

import frontend.SophieTui
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import scala.jdk.CollectionConverters._
import upickle.default.{write => uwrite, ReadWriter, macroRW}

// Report semplice che serializziamo con upickle. I valori BigDecimal sono convertiti a stringa
// per evitare problemi di serializzazione/precisione nei consumer JSON.
case class TuiSimReport(inputs: Seq[String], portfolio: Map[String, String], lastPlanPresent: Boolean)
object TuiSimReport { implicit val rw: ReadWriter[TuiSimReport] = macroRW }

object TuiSimReportMain {
  def main(args: Array[String]): Unit = {
    // percorso del file di comandi (documentato in docs/tui_commands.txt)
    val path = Paths.get("docs/tui_commandsUncommented.txt")
    if (!Files.exists(path)) {
      System.err.println(s"File not found: ${path.toString}")
      System.exit(2)
    }

    // leggiamo le righe così come sono (inclusi i comandi e i blocchi program)
    val rawLines = Files.readAllLines(path).asScala.toSeq

    // Rimuoviamo commenti inline e righe commentate. Supportiamo `#` e `//`.
    // Esempio: "BUY 1 BTC  # comment" -> "BUY 1 BTC"
    val cleanedLines = rawLines.map { raw =>
      // togliamo tutto dopo '#' o '//' (semplice, non gestisce casi in stringhe)
      val noHash = raw.replaceAll("#.*$", "").replaceAll("//.*$", "")
      noHash.trim
    }.filter(_.nonEmpty)

    val removed = rawLines.length - cleanedLines.length
    if (removed > 0) println(s"Ignored $removed comment/empty lines from ${path.toString}")

    // Eseguiamo la simulazione: non apre la console, ritorna l'ultimo portfolio e il lastPlan
    val (portfolio, lastPlanOpt) = SophieTui.simulateSession(cleanedLines)

    // Convertiamo i BigDecimal in stringhe per serializzare coerentemente nel JSON
    val portfolioStr = portfolio.map { case (k, v) => k -> v.toString }

    val report = TuiSimReport(inputs = cleanedLines, portfolio = portfolioStr, lastPlanPresent = lastPlanOpt.isDefined)

    // Assicuriamoci che la cartella tmp esista e scriviamo il report in UTF-8
    val outDir = Paths.get("tmp")
    if (!Files.exists(outDir)) Files.createDirectories(outDir)
    val outPath = outDir.resolve("tui_sim_report.json")

    // Scriviamo il JSON con indentazione per facilità di lettura
    val json = uwrite(report, indent = 2)
    Files.writeString(outPath, json, UTF_8)

    // Stampe di sintesi: utili per log e diagnostica automatica
    println(s"Wrote report to ${outPath.toString}")
    println("=== Summary ===")
    println(s"Inputs: ${cleanedLines.length} lines")
    println(s"Last plan present: ${lastPlanOpt.isDefined}")
    println("Final portfolio positions:")
    if (portfolio.isEmpty) println(" - (empty)")
    else portfolio.foreach { case (sym, bd) => println(s" - $sym -> $bd") }
  }
}
