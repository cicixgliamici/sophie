package cli

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8
import scala.io.Source
import frontend._

/**
  * Integration test for the CLI runner.
  *
  * This test invokes `cli.SophieCli.main(...)` in-process with non-interactive
  * flags and verifies that:
  *  - a portfolio JSON file is created and contains the expected symbol (MSFT);
  *  - a ledger NDJSON file is created and contains at least one ledger entry.
  *
  * The test uses a temporary directory and attempts to clean up created files
  * to avoid leaving artifacts on the developer machine.
  */
class SophieCliIntegrationSpec extends AnyFunSuite {
  test("CLI runs non-interactively and writes ledger & portfolio") {
    val tmpDir = Files.createTempDirectory("sophie_cli_it_")
    try {
      val portfolioPath = tmpDir.resolve("out_pf.json")
      val ledgerPath = tmpDir.resolve("out_ledger.ndjson")

      val args = Array(
        "--file", "src/test/resources/programs/buy_ok.sophie",
        "--md", "src/main/resources/md_demo.json",
        "--run",
        "--portfolio", portfolioPath.toString,
        "--ledger", ledgerPath.toString,
        "--reset-portfolio"
      )

      // Run CLI main in-process
      cli.SophieCli.main(args)

      // Assertions: portfolio file exists and contains MSFT
      assert(Files.exists(portfolioPath), "Portfolio file was not created")
      val pfJson = Files.readString(portfolioPath, UTF_8)
      assert(pfJson.contains("MSFT"), s"Portfolio JSON did not contain MSFT: $pfJson")

      // Ledger exists and contains at least one line with MSFT
      assert(Files.exists(ledgerPath), "Ledger file was not created")
      val ledgerText = Files.readAllLines(ledgerPath).toArray.mkString("\n")
      assert(ledgerText.contains("MSFT"), s"Ledger did not contain MSFT: $ledgerText")
    } finally {
      // cleanup
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }
}
