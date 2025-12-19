package integration

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import scala.util.control.NonFatal
import upickle.default.read

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

    case class ExitTrapped(status: Int) extends SecurityException(s"System.exit($status)")

    def withExitTrapped[T](block: => T): Either[Int, T] = {
      val previous = System.getSecurityManager
      System.setSecurityManager(new SecurityManager {
        override def checkExit(status: Int): Unit = throw ExitTrapped(status)
        override def checkPermission(perm: java.security.Permission): Unit = ()
      })
      try {
        Right(block)
      } catch {
        case ExitTrapped(status) => Left(status)
      } finally {
        System.setSecurityManager(previous)
      }
    }

    def withInput[T](input: String)(block: => T): T = {
      val previousIn = System.in
      try {
        System.setIn(new java.io.ByteArrayInputStream(input.getBytes(UTF_8)))
        block
      } finally {
        System.setIn(previousIn)
      }
    }

    try {
      println(s"Integration runner temporary dir: $tmpDir")

      // --- CLI test: run SophieCli non-interactively ---
      println("\n=== CLI integration test ===")

      val cliFailures = try {
        var failuresLocal = 0
        try {
          val pf = tmpDir.resolve("cli_pf.json")
          val ledger = tmpDir.resolve("cli_ledger.ndjson")

          val symbol = "MSFT"
          val expectedCurrency = "EUR"
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
            val pfJson = read[frontend.PortfolioJson.PortfolioJ](pfText)
            val totalQty = pfJson.positions.values.foldLeft(BigDecimal(0))(_ + _)
            if (totalQty <= 0) {
              println(s"[CLI] Portfolio total quantity not positive: $totalQty")
              failuresLocal += 1
            }
            if (pfJson.cash.isEmpty) {
              println(s"[CLI] Portfolio JSON missing cash field (expected currency=$expectedCurrency): $pfText")
              failuresLocal += 1
            }

            val ledgerEntries = engine.FileLedger(ledger).readAll()
            if (ledgerEntries.isEmpty) {
              println(s"[CLI] Ledger has no entries: $ledger")
              failuresLocal += 1
            }
            if (!ledgerEntries.exists(_.symbol == symbol)) {
              println(s"[CLI] Ledger entries missing expected symbol $symbol: $ledgerEntries")
              failuresLocal += 1
            }

            // Second CLI run (without --reset-portfolio) and verify updates
            val initialQty = pfJson.positions.getOrElse(symbol, BigDecimal(0))
            val initialLedgerCount = ledgerEntries.size
            val args2 = Array(
              "--file", "src/test/resources/programs/buy_ok.sophie",
              "--md", "src/main/resources/md_demo.json",
              "--run",
              "--portfolio", pf.toString,
              "--ledger", ledger.toString
            )
            withInput("n\n") {
              cli.SophieCli.main(args2)
            }

            val pfText2 = Files.readString(pf)
            val pfJson2 = read[frontend.PortfolioJson.PortfolioJ](pfText2)
            val updatedQty = pfJson2.positions.getOrElse(symbol, BigDecimal(0))
            if (updatedQty <= initialQty) {
              println(s"[CLI] Portfolio quantity did not increase for $symbol (initial=$initialQty updated=$updatedQty)")
              failuresLocal += 1
            }
            val ledgerEntries2 = engine.FileLedger(ledger).readAll()
            if (ledgerEntries2.size <= initialLedgerCount) {
              println(s"[CLI] Ledger did not grow after second run (initial=$initialLedgerCount updated=${ledgerEntries2.size})")
              failuresLocal += 1
            }

            // CLI error test: invalid program should fail and be reported
            val badProgram = tmpDir.resolve("invalid_program.sophie")
            Files.writeString(badProgram, "BUY ???", UTF_8)
            val badArgs = Array(
              "--file", badProgram.toString,
              "--md", "src/main/resources/md_demo.json",
              "--run",
              "--portfolio", tmpDir.resolve("bad_pf.json").toString,
              "--ledger", tmpDir.resolve("bad_ledger.ndjson").toString,
              "--reset-portfolio"
            )
            withExitTrapped {
              cli.SophieCli.main(badArgs)
            } match {
              case Left(status) if status != 0 =>
                println(s"[CLI] Controlled failure observed for invalid program (status=$status)")
              case Left(status) =>
                println(s"[CLI] Invalid program exited with status 0 (unexpected)")
                failuresLocal += 1
              case Right(_) =>
                println(s"[CLI] Invalid program did not fail as expected")
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
          if (!plan3.isDefined) {
            println(s"[TUI] sim3 expected lastPlan to be defined for warning case")
            failuresLocal += 1
          }

          // Simulation 4: set price, series, override, show md, then apply
          val inputs4 = Seq(
            ":set price MSFT 320",
            ":set series MSFT volume 10,2000000",
            ":set ovr STDDEV MSFT 20 60000",
            ":show md",
            "BUY 100 EUR OF MSFT IF MSFT.volume > 1000000 && STDDEV(MSFT, 20) > PRICE(MSFT);",
            "",
            ":pf apply"
          )
          val (pf4, plan4) = frontend.SophieTui.simulateSession(inputs4)
          println(s"[TUI] sim4 portfolio: $pf4  lastPlanPresent=${plan4.isDefined}")
          if (!pf4.contains("MSFT")) {
            println(s"[TUI] sim4 missing MSFT in portfolio: $pf4")
            failuresLocal += 1
          }
          if (!plan4.isDefined) {
            println(s"[TUI] sim4 expected lastPlan to be defined")
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
