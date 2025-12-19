package integration

import java.nio.file.Files
import java.nio.file.Path
import scala.util.control.NonFatal

/**
  * Integration Runner
  * ------------------
  * This standalone runner exercises key end-to-end flows in the project:
  *  - a non-interactive CLI execution (parsing -> lowering -> execution)
  *  - several TUI simulations driven by `SophieTui.simulateSession`
  *
  * It is meant to be used as a lightweight verification tool during
  * development or as a step in a packaging process (for example, a fat-jar
  * can include this runner and be executed with `java -jar`).
  *
  * Behavior and contract:
  *  - Prints a human-readable report to stdout describing the checks performed.
  *  - Uses a temporary directory for files and attempts best-effort cleanup.
  *  - Exits with code 0 on success, non-zero on any failure.
  *
  * Keep this runner minimal: it should invoke the real public APIs (CLI main,
  * SophieTui.simulateSession) rather than duplicating logic. Tests should
  * still live under `src/test` and use proper frameworks; this runner is a
  * convenience for manual or packaged integration checks.
  */
object RunIntegrationRunner {
  def main(argv: Array[String]): Unit = {
    val tmpDir = Files.createTempDirectory("sophie_integration_")

    var totalFailures: Int = 0

    try {
      println(s"Integration runner temporary dir: $tmpDir")

      // --- CLI test: run SophieCli non-interactively ---
      println("\n=== CLI integration test ===")

      val cliFailures = try {
        var failuresLocal = 0
        try {
          val pf = tmpDir.resolve("cli_pf.json")
          val ledger = tmpDir.resolve("cli_ledger.ndjson")

          val args = Array(
            "--file", "src/test/resources/programs/buy_ok.sophie",
            "--md", "src/main/resources/md_demo.json",
            "--run",
            "--portfolio", pf.toString,
            "--ledger", ledger.toString,
            "--reset-portfolio"
          )

          // Run CLI main
          cli.SophieCli.main(args)

          // Basic checks: files exist and contain the expected symbol
          if (!Files.exists(pf)) {
            println(s"[CLI] Portfolio file not found: $pf")
            failuresLocal += 1
          } else if (!Files.exists(ledger)) {
            println(s"[CLI] Ledger file not found: $ledger")
            failuresLocal += 1
          } else {
            val pfText = Files.readString(pf)
            val ledgerText = Files.readString(ledger)
            if (!pfText.contains("MSFT")) {
              println(s"[CLI] Portfolio JSON does not contain expected symbol MSFT: $pfText")
              failuresLocal += 1
            }
            if (!ledgerText.toUpperCase.contains("MSFT")) {
              println(s"[CLI] Ledger does not contain expected symbol MSFT: $ledgerText")
              failuresLocal += 1
            }
            if (failuresLocal == 0) println("[CLI] OK")
          }
        } catch { case NonFatal(e) =>
          println(s"[CLI] FAILED with exception: ${e.getMessage}")
          e.printStackTrace()
          1
        }
        failuresLocal
      } catch { case NonFatal(_) => 1 }

      // --- TUI simulations ---
      println("\n=== TUI simulation tests ===")

      val tuiFailures = try {
        var failuresLocal = 0

        try {
          // Simulation 1: set price and buy then apply
          val inputs1 = Seq(":set price MSFT 320", "BUY 1500 EUR OF MSFT;", "", ":pf apply")
          val (pf1, plan1) = frontend.SophieTui.simulateSession(inputs1)
          println(s"[TUI] sim1 portfolio: $pf1  lastPlanPresent=${plan1.isDefined}")
          if (!pf1.contains("MSFT")) {
            println(s"[TUI] sim1 missing MSFT in portfolio: $pf1")
            failuresLocal += 1
          }

          // Simulation 2: paste mode and show last
          val inputs2 = Seq(":set price MSFT 100", "BUY 100 EUR OF MSFT;", ":show last", "", ":pf apply")
          val (pf2, plan2) = frontend.SophieTui.simulateSession(inputs2)
          println(s"[TUI] sim2 portfolio: $pf2  lastPlanPresent=${plan2.isDefined}")
          if (!pf2.contains("MSFT")) {
            println(s"[TUI] sim2 missing MSFT in portfolio: $pf2")
            failuresLocal += 1
          }

          // Simulation 3: missing price -> preview + apply should result in empty portfolio
          val inputs3 = Seq("BUY 100 EUR OF ABC;", "", ":pf preview", ":pf apply")
          val (pf3, plan3) = frontend.SophieTui.simulateSession(inputs3)
          println(s"[TUI] sim3 portfolio: $pf3  lastPlanPresent=${plan3.isDefined}")
          if (pf3.nonEmpty) {
            println(s"[TUI] sim3 expected empty portfolio but got: $pf3")
            failuresLocal += 1
          }

          if (failuresLocal == 0) println("[TUI] All simulations OK")
        } catch { case NonFatal(e) =>
          println(s"[TUI] FAILED with exception: ${e.getMessage}")
          e.printStackTrace()
          1
        }

        failuresLocal
      } catch { case NonFatal(_) => 1 }

      totalFailures = cliFailures + tuiFailures

      // Summary
      println(s"\n=== Integration runner summary: failures=$totalFailures ===")
    } finally {
      // try best-effort cleanup
      try Files.walk(tmpDir).sorted(java.util.Comparator.reverseOrder()).forEach(p => Files.deleteIfExists(p)) catch { case _: Throwable => () }
    }

    if (totalFailures > 0) {
      println("One or more integration checks FAILED")
      sys.exit(1)
    } else {
      println("All integration checks PASSED")
      sys.exit(0)
    }
  }
}
