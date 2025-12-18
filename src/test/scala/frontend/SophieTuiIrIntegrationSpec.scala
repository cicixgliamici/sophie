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
      SophieTui.setPricePublic("MSFT", "50")
      SophieTui.evalAndPrintPublic("""BUY 100 USD OF MSFT IF PRICE(MSFT) > 0;""")

      SophieTui.compileIrPublic(instrPath.toString)
      val compiled = Files.readString(instrPath, UTF_8)
      assert(compiled.contains("MSFT"))

      SophieTui.execIrPublic(instrPath.toString)

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
