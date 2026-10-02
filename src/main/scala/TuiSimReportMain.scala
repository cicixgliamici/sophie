// TuiSimReportMain.scala
// ----------------------
// Run the TUI non-interactively with inputs from docs/tui_commandsUncommented.txt,
// simulate the session, and write a JSON report to tmp/tui_sim_report.json.
// A reproducible harness supports smoke checks and CI without an interactive console.

import frontend.SophieTui
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import scala.jdk.CollectionConverters._
import upickle.default.{write => uwrite, ReadWriter, macroRW}

// Encode BigDecimal quantities as strings when serializing the report with uPickle
// so JSON consumers do not lose precision through binary floating-point conversion.
case class TuiSimReport(inputs: Seq[String], portfolio: Map[String, String], lastPlanPresent: Boolean)
object TuiSimReport { implicit val rw: ReadWriter[TuiSimReport] = macroRW }

object TuiSimReportMain {
  def main(args: Array[String]): Unit = {
    // Use the command fixture documented in docs/tui_commands.txt.
    val path = Paths.get("docs/tui_commandsUncommented.txt")
    if (!Files.exists(path)) {
      System.err.println(s"File not found: ${path.toString}")
      System.exit(2)
    }

    // Read the original lines, including commands and program blocks.
    val rawLines = Files.readAllLines(path).asScala.toSeq

    // Remove whole-line and inline comments using the fixture conventions # and //.
    // Example: "BUY 1 BTC  # comment" becomes "BUY 1 BTC".
    val cleanedLines = rawLines.map { raw =>
      // This fixture cleaner strips text after # or // without interpreting quoted strings.
      val noHash = raw.replaceAll("#.*$", "").replaceAll("//.*$", "")
      noHash.trim
    }.filter(_.nonEmpty)

    val removed = rawLines.length - cleanedLines.length
    if (removed > 0) println(s"Ignored $removed comment/empty lines from ${path.toString}")

    // Simulate without a console and retain the resulting portfolio and last plan.
    val (portfolio, lastPlanOpt) = SophieTui.simulateSession(cleanedLines)

    // Preserve exact decimal quantities in the JSON report.
    val portfolioStr = portfolio.map { case (k, v) => k -> v.toString }

    val report = TuiSimReport(inputs = cleanedLines, portfolio = portfolioStr, lastPlanPresent = lastPlanOpt.isDefined)

    // Create the output directory before writing the UTF-8 report.
    val outDir = Paths.get("tmp")
    if (!Files.exists(outDir)) Files.createDirectories(outDir)
    val outPath = outDir.resolve("tui_sim_report.json")

    // Indent the JSON so reviewers can inspect the generated report.
    val json = uwrite(report, indent = 2)
    Files.writeString(outPath, json, UTF_8)

    // Print a compact summary for logs and automated diagnostics.
    println(s"Wrote report to ${outPath.toString}")
    println("=== Summary ===")
    println(s"Inputs: ${cleanedLines.length} lines")
    println(s"Last plan present: ${lastPlanOpt.isDefined}")
    println("Final portfolio positions:")
    if (portfolio.isEmpty) println(" - (empty)")
    else portfolio.foreach { case (sym, bd) => println(s" - $sym -> $bd") }
  }
}
