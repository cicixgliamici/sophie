package frontend

import engine.{FileJsonPortfolioStore, FileLedger}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

class SophieTuiIrIntegrationSpec extends AnyFunSuite {
  // Remove a temp tree created by the test. Keeping the test sandboxed inside its
  // own temp directory avoids Windows file-lock issues on the shared ./data folder.
  private def deleteRecursively(path: java.nio.file.Path): Unit = {
    if (Files.isDirectory(path)) {
      val children = Files.list(path)
      try children.forEach(deleteRecursively)
      finally children.close()
    }
    Files.deleteIfExists(path)
  }

  test("compileIr + execIr roundtrip emits instructions, ledger entries, and portfolio state") {
    val dataDir = Files.createTempDirectory("sophie_tui_ir")
    val pfPath = dataDir.resolve("portfolio.json")
    val ledgerPath = dataDir.resolve("ledger.ndjson")
    val instrPath = Files.createTempFile("sophie_ir", ".json")

    try {
      // Build market data with PRICE(MSFT) = 50
      val md = engine.InMemoryMarketData(prices = Map("MSFT" -> BigDecimal(50)), seriesData = Map.empty, indicatorOverrides = Map.empty)

      // Evaluate program to an execution plan
      val prog = "BUY 100 USD OF MSFT IF PRICE(MSFT) > 0;"
      val evalRes = ProgramEvaluator.evaluate(prog, md)
      val plan = evalRes.plan

      // Lower to IR instructions
      val instrs = engine.Lowering.from(plan, md, source = "test") match {
        case Left(err) => throw new AssertionError(s"Lowering failed: $err")
        case Right(list) => list
      }
      assert(instrs.exists(_.symbol == "MSFT"))

      // Execute the IR using a private temp directory so this test does not
      // contend with other test runs over a shared `data/` location.
      val pfStore = FileJsonPortfolioStore(pfPath)
      val ledger = FileLedger(ledgerPath)
      val events = engine.Executor.run(instrs, md, pfStore, ledger, source = "test")

      val pfState = FileJsonPortfolioStore(pfPath).load()
      assert(pfState.positions("MSFT") == BigDecimal(2)) // 100 USD / 50 price -> 2 shares

      val ledgerEntries = FileLedger(ledgerPath).readAll()
      assert(ledgerEntries.exists(_.symbol == "MSFT"))
    } finally {
      deleteRecursively(dataDir)
      Files.deleteIfExists(instrPath)
    }
  }
}
