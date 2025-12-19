import org.scalatest.funsuite.AnyFunSuite
import frontend.ReceiptPrinter
import engine.LedgerEvent
import ast._

class ReceiptPrinterSpec extends AnyFunSuite {
  test("receiptLines returns empty vector when no events") {
    val lines = ReceiptPrinter.receiptLines(List.empty[LedgerEvent])
    assert(lines.isEmpty)
  }

  test("receiptLines produces header and lines for events") {
    val e = LedgerEvent(
      ts = 1700000000000L,
      action = ast.Buy,
      symbol = "MSFT",
      qty = BigDecimal(10),
      price = BigDecimal(100),
      notional = BigDecimal(1000),
      source = "test",
      note = ""
    )
    val lines = ReceiptPrinter.receiptLines(List(e))
    assert(lines.nonEmpty)
    assert(lines.head.contains("Ricevuta"))
    assert(lines.exists(_.contains("MSFT")))
  }
}
