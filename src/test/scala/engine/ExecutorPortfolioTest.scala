package engine

import org.scalatest.funsuite.AnyFunSuite
import ast._
import java.nio.file.Files
import java.nio.file.Path
import frontend.MdJsonCodec._
import frontend.PortfolioJson._

class ExecutorPortfolioTest extends AnyFunSuite {

  private def withTempFile[T](action: Path => T): T = {
    val tmp = Files.createTempFile("sophie-test-pf", ".json")
    // ensure file does not exist so FileJsonPortfolioStore.load treats it as absent
    Files.deleteIfExists(tmp)
    try action(tmp)
    finally Files.deleteIfExists(tmp)
  }

  test("Executor BUY updates portfolio file and ledger") {
    // Happy path: executing a BUY should persist the new position and append
    // a ledger event so downstream tooling has a full audit trail.
    withTempFile { pfPath =>
      val pfStore = FileJsonPortfolioStore(pfPath)
      val ledgerPath = Files.createTempFile("sophie-ledger", ".ndjson")
      try {
        val ledger = FileLedger(ledgerPath)
        val md = InMemoryMarketData(prices = Map("MSFT" -> BigDecimal(100)))

        val instr = Instruction(id = "i1", action = ast.Buy, symbol = "MSFT", qty = BigDecimal(5), price = None, note = "test")
        val events = Executor.run(List(instr), md, pfStore, ledger, source = "test")

        // portfolio file should contain MSFT: 5
        val loaded = pfStore.load()
        assert(loaded.positions("MSFT") == BigDecimal(5))

        // ledger should have one event
        val lines = ledger.readAll()
        assert(lines.nonEmpty)
        val e = lines.last
        assert(e.symbol == "MSFT")
        assert(e.action == ast.Buy)
      } finally {
        Files.deleteIfExists(ledgerPath)
      }
    }
  }

  test("Executor SELL reduces but not below zero") {
    // Selling more than current holdings should clamp to zero instead of going negative.
    withTempFile { pfPath =>
      val pfStore = FileJsonPortfolioStore(pfPath)
      // initialize portfolio with 2 BTC
      pfStore.save(PortfolioState(Map("BTC" -> BigDecimal(2)), cash = BigDecimal(0)))

      val ledgerPath = Files.createTempFile("sophie-ledger", ".ndjson")
      try {
        val ledger = FileLedger(ledgerPath)
        val md = InMemoryMarketData(prices = Map("BTC" -> BigDecimal(20000)))
        val instr = Instruction(id = "i2", action = ast.Sell, symbol = "BTC", qty = BigDecimal(3), price = None, note = "sellall")
        val events = Executor.run(List(instr), md, pfStore, ledger, source = "test")

        val loaded = pfStore.load()
        assert(loaded.positions("BTC") == BigDecimal(0))
      } finally {
        Files.deleteIfExists(ledgerPath)
      }
    }
  }

  test("PortfolioJson roundtrip writes and reads expected JSON") {
    // Verify the portfolio persistence format remains stable: positive positions
    // survive a save/load cycle and the JSON contains expected keys for inspection.
    withTempFile { pfPath =>
      val pfStore = FileJsonPortfolioStore(pfPath)
      pfStore.save(PortfolioState(Map("AAPL" -> BigDecimal(10), "MSFT" -> BigDecimal(0)), cash = BigDecimal(1234)))
      val loaded = pfStore.load()
      assert(loaded.positions("AAPL") == BigDecimal(10))
      assert(loaded.positions("MSFT") == BigDecimal(0))
      assert(loaded.cash == BigDecimal(1234))
      // file content non-empty
      val content = Files.readString(pfPath)
      assert(content.contains("AAPL"))
      assert(content.contains("cash"))
    }
  }
}
