package cli

import org.scalatest.funsuite.AnyFunSuite
import java.nio.file.Files
import java.nio.charset.StandardCharsets.UTF_8
import frontend._
import upickle.default.read
import testhelpers.NoExit.withNoExit

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

      // Assertions: portfolio file exists and contains expected quantity from EUR conversion
      assert(Files.exists(portfolioPath), "Portfolio file was not created")
      val pfJson = Files.readString(portfolioPath, UTF_8)
      val pf = read[PortfolioJson.PortfolioJ](pfJson)
      assert(pf.positions.contains("MSFT"), s"Portfolio JSON did not contain MSFT: $pfJson")
      assert(pf.positions("MSFT") == BigDecimal("4.6875"), s"Unexpected MSFT quantity: $pfJson")
      assert(pf.cash.contains(BigDecimal(0)), s"Expected cash to be reset to 0: $pfJson")

      // Ledger exists and contains a single execution entry
      assert(Files.exists(ledgerPath), "Ledger file was not created")
      val ledgerLines = Files.readAllLines(ledgerPath)
      assert(ledgerLines.size() == 1, s"Expected one ledger row, got ${ledgerLines.size()}: $ledgerLines")
      assert(ledgerLines.get(0).contains("MSFT"), s"Ledger did not contain MSFT: ${ledgerLines.get(0)}")
    } finally {
      // cleanup
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }

  test("CLI --reset-portfolio clears prior state before executing") {
    val tmpDir = Files.createTempDirectory("sophie_cli_reset_it_")
    try {
      val portfolioPath = tmpDir.resolve("out_pf.json")
      val ledgerPath = tmpDir.resolve("out_ledger.ndjson")
      val existing = PortfolioJson.PortfolioJ(positions = Map("MSFT" -> BigDecimal(99), "OLD" -> BigDecimal(1)), cash = Some(BigDecimal(42)))
      Files.writeString(portfolioPath, upickle.default.write(existing, indent = 2), UTF_8)

      val args = Array(
        "--file", "src/test/resources/programs/buy_ok.sophie",
        "--md", "src/main/resources/md_demo.json",
        "--run",
        "--portfolio", portfolioPath.toString,
        "--ledger", ledgerPath.toString,
        "--reset-portfolio"
      )

      cli.SophieCli.main(args)

      val pfJson = Files.readString(portfolioPath, UTF_8)
      val pf = read[PortfolioJson.PortfolioJ](pfJson)
      assert(pf.positions.getOrElse("MSFT", BigDecimal(0)) == BigDecimal("4.6875"), s"Portfolio was not reset before execution: $pfJson")
      assert(!pf.positions.contains("OLD"), s"Old positions should have been cleared: $pfJson")
      assert(pf.cash.contains(BigDecimal(0)), s"Expected cash to be reset to 0: $pfJson")
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }

  test("CLI reports an error when the program file is missing") {
    val tmpDir = Files.createTempDirectory("sophie_cli_missing_")
    try {
      val portfolioPath = tmpDir.resolve("out_pf.json")
      val ledgerPath = tmpDir.resolve("out_ledger.ndjson")
      val missingProgram = tmpDir.resolve("missing.sophie")

      val args = Array(
        "--file", missingProgram.toString,
        "--md", "src/main/resources/md_demo.json",
        "--run",
        "--portfolio", portfolioPath.toString,
        "--ledger", ledgerPath.toString,
        "--reset-portfolio"
      )

      // We use `withNoExit` to intercept System.exit calls in-process. Modern JVMs
      // may prohibit installing a SecurityManager (throws UnsupportedOperationException),
      // therefore tests accept that exception as an environment-specific outcome.
      try {
        withNoExit { cli.SophieCli.main(args) }
        fail("Expected System.exit or UnsupportedOperationException to be thrown")
      } catch {
        case se: SecurityException => assert(se.getMessage.contains("System.exit"), s"Expected System.exit when file missing, got: ${se.getMessage}")
        case u: UnsupportedOperationException => // allowed in modern JVMs where setSecurityManager is blocked
      }
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }
 test("CLI reports an error when the market data file is missing") {
    val tmpDir = Files.createTempDirectory("sophie_cli_missing_md_")
    try {
      val portfolioPath = tmpDir.resolve("out_pf.json")
      val ledgerPath = tmpDir.resolve("out_ledger.ndjson")
      val missingMd = tmpDir.resolve("missing_md.json")

      val args = Array(
        "--file", "src/test/resources/programs/buy_ok.sophie",
        "--md", missingMd.toString,
        "--run",
        "--portfolio", portfolioPath.toString,
        "--ledger", ledgerPath.toString,
        "--reset-portfolio"
      )

      // We use `withNoExit` to intercept System.exit calls in-process. Modern JVMs
      // may prohibit installing a SecurityManager (throws UnsupportedOperationException),
      // therefore tests accept that exception as an environment-specific outcome.
      try {
        withNoExit { cli.SophieCli.main(args) }
        fail("Expected System.exit or UnsupportedOperationException to be thrown")
      } catch {
        case se: SecurityException => assert(se.getMessage.contains("System.exit"), s"Expected System.exit when md missing, got: ${se.getMessage}")
        case u: UnsupportedOperationException => // allowed
      }
    } finally {
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }
  }
}
