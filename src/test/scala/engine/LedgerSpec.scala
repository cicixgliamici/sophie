package engine

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.Files
import java.nio.file.Paths
import scala.math.BigDecimal

class LedgerSpec extends AnyFunSuite {
  test("FileLedger append and readAll roundtrip") {
    val tmp = Files.createTempFile("ledger_test", ".ndjson")
    val ledger = FileLedger(tmp)

    val e1 = LedgerEvent(System.currentTimeMillis(), ast.Buy, "MSFT", BigDecimal(1.5), BigDecimal(100), BigDecimal(150), "test", "note1")
    val e2 = LedgerEvent(System.currentTimeMillis()+1, ast.Sell, "BTC", BigDecimal(0.1), BigDecimal(60000), BigDecimal(6000), "test", "note2")

    ledger.append(e1)
    ledger.append(e2)

    val all = ledger.readAll()
    // Expect at least two events and matching content
    assert(all.exists(_.note == "note1"))
    assert(all.exists(_.note == "note2"))

    // cleanup
    Files.deleteIfExists(tmp)
  }
}

