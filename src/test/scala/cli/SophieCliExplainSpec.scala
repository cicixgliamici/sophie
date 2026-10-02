package cli

import engine._
import java.io.{ByteArrayOutputStream, PrintStream}
import java.nio.file.{Files, Path}
import org.scalatest.funsuite.AnyFunSuite
import upickle.default.read

class SophieCliExplainSpec extends AnyFunSuite {
  private def withDirectory(test: Path => Unit): Unit = {
    val directory = Files.createTempDirectory("sophie-explain-")
    try test(directory)
    finally {
      val files = Files.walk(directory)
      try files.sorted(java.util.Comparator.reverseOrder()).forEach(path => Files.deleteIfExists(path))
      finally files.close()
    }
  }

  private val input = Array("--file", "examples/explain_decisions.sophie",
    "--md", "examples/explain-market-data.json")

  test("CLI explains and exports all decisions without implicitly executing") {
    withDirectory { directory =>
      val reportPath = directory.resolve("reports/decisions.json")
      val portfolio = directory.resolve("portfolio.json")
      val ledger = directory.resolve("ledger.ndjson")
      val output = new ByteArrayOutputStream()
      val stream = new PrintStream(output, true, "UTF-8")
      try Console.withOut(stream) {
        SophieCli.main(input ++ Array("--explain", "--explain-json", reportPath.toString,
          "--portfolio", portfolio.toString, "--ledger", ledger.toString))
      } finally stream.close()
      val report = read[DecisionReport](Files.readString(reportPath))
      assert(report.schemaVersion == 1 && report.trades.size == 4)
      assert(report.trades.map(_.conditionMet) == Vector(true, false, false, true))
      assert(report.trades(2).explanation.get.children(1).result.isEmpty)
      assert(output.toString("UTF-8").contains("RSI(MSFT, 14) = 28 [indicator override]"))
      assert(output.toString("UTF-8").contains("NOT EVALUATED"))
      assert(!Files.exists(portfolio) && !Files.exists(ledger))
    }
  }

  test("executed decisions keep their captured explanations in the ledger") {
    withDirectory { directory =>
      val reportPath = directory.resolve("decisions.json")
      val portfolio = directory.resolve("portfolio.json")
      val ledger = directory.resolve("ledger.ndjson")
      SophieCli.main(input ++ Array("--run", "--explain-json", reportPath.toString,
        "--initial-cash", "500", "--portfolio", portfolio.toString, "--ledger", ledger.toString))
      val report = read[DecisionReport](Files.readString(reportPath))
      val events = FileLedger(ledger).readAll()
      assert(events.size == 2)
      assert(events.map(_.explanation) == report.trades.filter(_.conditionMet).map(_.explanation))
      val state = FileJsonPortfolioStore(portfolio).load()
      assert(state.positions("MSFT") == 2 && state.cash == 300)
    }
  }
}
