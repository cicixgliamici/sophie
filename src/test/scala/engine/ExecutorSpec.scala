package engine

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.Files
import java.nio.file.Paths
import scala.math.BigDecimal
import ast._

class ExecutorSpec extends AnyFunSuite {
  test("Executor.run with missing prices returns events for those instructions that can be resolved") {
    // Executor should fail fast when encountering an instruction whose price
    // cannot be resolved from explicit value or MarketData. Here only symbol B
    // lacks a price, so the run is expected to raise with a clear message.
    val md = InMemoryMarketData(prices = Map("A" -> BigDecimal(10)), seriesData = Map.empty, indicatorOverrides = Map.empty)
    val tmpPf = Paths.get("tmp/test_pf_exec.json")
    val tmpLedger = Paths.get("tmp/test_ledger_exec.ndjson")
    Files.createDirectories(tmpPf.getParent)
    Files.deleteIfExists(tmpPf)
    Files.deleteIfExists(tmpLedger)

    val ledger = FileLedger(tmpLedger)
    val pfStore = FileJsonPortfolioStore(tmpPf)

    val instrs = List(
      Instruction("i1", Buy, "A", BigDecimal(1), None, "note1"),
      Instruction("i2", Buy, "B", BigDecimal(2), None, "note2") // B price missing
    )

    // Executor should throw when encountering an instruction with missing price
    val ex = intercept[IllegalStateException] {
      Executor.run(instrs, md, pfStore, ledger, source = "test")
    }
    assert(ex.getMessage.contains("Missing PRICE(B)"))

    // cleanup
    Files.deleteIfExists(tmpPf)
    Files.deleteIfExists(tmpLedger)
  }
