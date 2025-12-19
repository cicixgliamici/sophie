package cli

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import frontend._

class SophieCliReceiptFileSpec extends AnyFunSuite {
  test("CLI --run produces receipt file when requested") {
    val tmpDir = Files.createTempDirectory("sophie_cli_receipt_")
    try {
      val portfolioPath = tmpDir.resolve("out_pf.json")
      val ledgerPath = tmpDir.resolve("out_ledger.ndjson")
      val receiptPath = tmpDir.resolve("out_receipt.txt")

      val args = Array(
        "--file", "src/test/resources/programs/buy_ok.sophie",
        "--md", "src/main/resources/md_demo.json",
        "--run",
        "--portfolio", portfolioPath.toString,
        "--ledger", ledgerPath.toString,
        "--receipt-file", receiptPath.toString,
        "--reset-portfolio"
      )

      // Run CLI main
      cli.SophieCli.main(args)

      assert(Files.exists(receiptPath), "Receipt file was not created")
      val receiptText = Files.readString(receiptPath, UTF_8)
      assert(receiptText.contains("Ricevuta"), s"Receipt did not contain header: $receiptText")
      assert(receiptText.contains("MSFT"), s"Receipt did not reference MSFT: $receiptText")
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }
}

