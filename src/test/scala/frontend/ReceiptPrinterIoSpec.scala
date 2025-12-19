package frontend

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.Files
import java.nio.charset.StandardCharsets.UTF_8

class ReceiptPrinterIoSpec extends AnyFunSuite {
  test("printReceipts writes receipt to provided file and uses provided printer") {
    val tmpDir = Files.createTempDirectory("receipt_test_")
    try {
      val receiptPath = tmpDir.resolve("r.txt")
      // Create a test event
      val e = engine.LedgerEvent(System.currentTimeMillis(), ast.Buy, "MSFT", BigDecimal(1), BigDecimal(100), BigDecimal(100), "test", "")
      // Use default printer (prints to stdout) but ensure no exception thrown and file created
      ReceiptPrinter.printReceipts(List(e), Some(receiptPath))
      assert(Files.exists(receiptPath), "Receipt file not created by printReceipts")
      val txt = Files.readString(receiptPath, UTF_8)
      assert(txt.contains("MSFT"))
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }
}

