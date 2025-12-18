package cli

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8

/**
  * Verify that the CLI respects the `--reset-portfolio` flag by overwriting an
  * existing portfolio file rather than preserving it. The test creates a
  * temporary existing portfolio file containing a dummy entry, runs the CLI
  * with reset, and asserts the resulting portfolio contains the expected
  * entries from the executed program.
  */
class SophieCliResetPortfolioSpec extends AnyFunSuite {
  test("CLI --reset-portfolio overwrites existing portfolio file") {
    val tmpDir = Files.createTempDirectory("sophie_cli_reset_")
    try {
      val portfolioPath = tmpDir.resolve("existing_pf.json")
      // create existing portfolio with a dummy holding
      val existing = "{\n  \"positions\": { \"OLD\": " + "\"1\"" + " }\n}"
      Files.writeString(portfolioPath, existing, UTF_8)

      val ledgerPath = tmpDir.resolve("out_ledger.ndjson")

      val args = Array(
        "--file", "src/test/resources/programs/buy_ok.sophie",
        "--md", "src/main/resources/md_demo.json",
        "--run",
        "--portfolio", portfolioPath.toString,
        "--ledger", ledgerPath.toString,
        "--reset-portfolio"
      )

      // Run CLI
      cli.SophieCli.main(args)

      // After run, portfolio file should exist and contain MSFT (not OLD)
      val pfText = Files.readString(portfolioPath, UTF_8)
      assert(pfText.contains("MSFT"), s"Expected MSFT in portfolio after reset-run: $pfText")
      assert(!pfText.contains("OLD"), s"Old portfolio entry should have been removed: $pfText")
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }
}
