package frontend

import engine.{FileJsonPortfolioStore, FileLedger}
import org.scalatest.funsuite.AnyFunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.{Files, Paths}

class SophieTuiIrIntegrationSpec extends AnyFunSuite {

  private val dataDir    = Paths.get("data")
  private val pfPath     = dataDir.resolve("portfolio.json")
  private val ledgerPath = dataDir.resolve("ledger.ndjson")

  private def cleanupData(): Unit = {
    Files.deleteIfExists(pfPath)
    Files.deleteIfExists(ledgerPath)
    if (Files.exists(dataDir)) {
      val stream = Files.list(dataDir)
      try {
        if (!stream.iterator().hasNext) Files.deleteIfExists(dataDir)
      } finally stream.close()
    }
  }

  test("compileIr + execIr roundtrip emits instructions, ledger entries, and portfolio state") {
    cleanupData()
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

      // Execute the IR (simulate execIr)
      // Ensure data directory exists (Executor/FileLedger expect it)
      Files.createDirectories(dataDir)
      val pfStore = FileJsonPortfolioStore(pfPath)
      val ledger = FileLedger(ledgerPath)
      val events = engine.Executor.run(instrs, md, pfStore, ledger, source = "test")

      val pfState = FileJsonPortfolioStore(pfPath).load()
      assert(pfState.positions("MSFT") == BigDecimal(2)) // 100 USD / 50 price -> 2 shares

      val ledgerEntries = FileLedger(ledgerPath).readAll()
      assert(ledgerEntries.exists(_.symbol == "MSFT"))
    } finally {
      cleanupData()
      Files.deleteIfExists(instrPath)
    }
  }
}
