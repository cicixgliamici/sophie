package cli

import java.nio.file.{Files, Path, Paths}

import upickle.default._

import frontend.MdJsonCodec._
import frontend.ProgramEvaluator
import frontend.PortfolioJson._
import engine.{InMemoryMarketData, Lowering, Executor, FileLedger, FileJsonPortfolioStore}
import engine._
import frontend.ReceiptPrinter
import util.SLF4JLogger

/**
  * Sophie CLI
  * ----------
  * Lightweight command-line runner that mirrors the main flows available in the
  * TUI but with explicit command-line options. Primary responsibilities:
  *  - load a .sophie source file
  *  - optionally load market-data (JSON) or use the bundled demo
  *  - evaluate -> lower to IR -> optionally print the IR or execute it
  *  - persist ledger entries and portfolio updates when executing
  *
  * Notes for testing:
  *  - The CLI is designed to be non-interactive when invoked with flags
  *    `--run` and `--reset-portfolio` (the latter avoids interactive prompts
  *    when a portfolio file exists). This makes it suitable for automated
  *    integration tests where we call `SophieCli.main(args)` directly.
  *  - For printing instructions (`--print-instructions`) the CLI writes JSON
  *    to stdout so tests can capture Console output.
  */
object SophieCli {

  /**
    * Entry point for the CLI runner.
    *
    * This object wires together parsing, evaluation, optional instruction printing,
    * and execution. It is intentionally verbose because it is meant to be read from
    * top to bottom as a walkthrough of the typical workflow:
    *  1. parse a Sophie program from a file;
    *  2. load market data (either demo or user-provided JSON);
    *  3. evaluate to an execution plan, optionally print lowered instructions;
    *  4. optionally execute, persisting ledger/portfolio and printing receipts.
    * It also contains the small command-line parsing needed to drive those steps.
    */
  def main(args: Array[String]): Unit = {
    val rawConfig = parseArgs(args.toList)
    // Minimal behaviour: if user provided only --file (no flags), assume they want to run using defaults
    val implicitRun = !rawConfig.printInstructions && !rawConfig.execute && rawConfig.sophieFile.isDefined
    val config = if (implicitRun)
      rawConfig.copy(execute = true)
    else rawConfig
    if (config.showHelp) {
      printHelp()
    } else {
      try {
        // 1) Load program
        val programSource = config.sophieFile match {
          case Some(p) => Files.readString(p)
          case None    =>
            SLF4JLogger.error("No input file provided. Use --file <path> or --demo for the sample in resources.")
            sys.exit(2)
            throw new IllegalStateException("unreachable")
        }

        // Confirm if implicit run
        val proceed = if (implicitRun) {
          Console.err.print("Esegui il programma e scrivi ledger/portfolio? [y/N]: ")
          val ans = scala.io.StdIn.readLine().trim.toLowerCase
          ans == "y" || ans == "yes"
        } else true

        if (!proceed) {
          println("Execution cancelled by user.")
        } else {

          // 2) Load market data (either provided JSON or demo bundled)
          val md: InMemoryMarketData = config.mdFile match {
            case Some(mdPath) => loadMdFromFile(mdPath)
            case None => loadDemoMd().getOrElse {
              SLF4JLogger.error("No demo market data found")
              sys.exit(2)
              throw new IllegalStateException("unreachable")
            }
          }

          // 3) Evaluate program
          val evaluation = ProgramEvaluator.evaluate(programSource, md)
          val plan = evaluation.plan

          // Print a readable plan
          println("=== Execution Plan ===")
          evaluation.warnings.foreach(w => println(s"[Warning] $w"))
          plan.trades.foreach { d =>
            val status = if (d.shouldExecute) "EXECUTE" else "SKIP"
            println(s" - [$status] ${d.detail}")
          }
          plan.portfolio.foreach { p =>
            println(" - [PORTFOLIO] target allocations:")
            p.allocations.foreach { a => println(s"   * ${a.value.amount} ${a.value.currency} OF ${a.symbol}") }
          }

          // 4) Optionally print instructions
          if (config.printInstructions) {
            Lowering.from(plan, md, config.source) match {
              case Left(err) => SLF4JLogger.error(s"Error lowering to instructions: $err")
              case Right(inst) =>
                println("\n=== Instructions (JSON) ===")
                println(write(inst, indent = 2))
            }
          }

          // 5) Optionally execute
          if (config.execute) {
            Lowering.from(plan, md, config.source) match {
              case Left(err) =>
                SLF4JLogger.error(s"Error lowering to instructions: $err")
                System.exit(1)
              case Right(inst) =>
                val ledgerPath = config.ledgerPath.getOrElse(Paths.get("ledger.ndjson"))
                val portfolioPath = config.portfolioPath.getOrElse(Paths.get("portfolio.json"))

                // If the portfolio file already exists, ask the user whether to reset it unless --reset-portfolio is provided
                if (Files.exists(portfolioPath) && !config.resetPortfolio) {
                  Console.err.print(s"Portfolio file $portfolioPath esiste. Vuoi eliminarlo e iniziare da zero? [y/N]: ")
                  val ans = scala.io.StdIn.readLine().trim.toLowerCase
                  if (ans == "y" || ans == "yes") {
                    resetPortfolioFile(portfolioPath)
                    println(s"Portfolio $portfolioPath resettato.")
                  } else {
                    println(s"Mantengo il portfolio esistente: $portfolioPath")
                  }
                } else if (Files.exists(portfolioPath) && config.resetPortfolio) {
                  resetPortfolioFile(portfolioPath)
                  println(s"Portfolio $portfolioPath resettato (--reset-portfolio).")
                }

                val ledger = FileLedger(ledgerPath)
                val pfStore = FileJsonPortfolioStore(portfolioPath)
                val events = Executor.run(inst, md, pfStore, ledger, config.source)
                println(s"Executed ${inst.length} instructions; ledger -> $ledgerPath, portfolio -> $portfolioPath")
                val receiptPath = config.receiptFile.map(Paths.get(_))
                ReceiptPrinter.printReceipts(events, receiptPath)
            }
          }

        }

      } catch {
        case ex: Throwable =>
          SLF4JLogger.error(s"Error: ${ex.getMessage}", ex)
          System.exit(1)
      }
    }
  }

  private case class Config(
                             showHelp: Boolean = false,
                             sophieFile: Option[Path] = None,
                             mdFile: Option[Path] = None,
                             printInstructions: Boolean = false,
                             execute: Boolean = false,
                             ledgerPath: Option[Path] = None,
                             portfolioPath: Option[Path] = None,
                             receiptFile: Option[String] = None,
                             source: String = "cli",
                             resetPortfolio: Boolean = false
                           )

  private def parseArgs(args: List[String]): Config = {
    def go(rest: List[String], acc: Config): Config = rest match {
      case Nil => acc
      case "--help" :: _ => acc.copy(showHelp = true)
      case "-h" :: _ => acc.copy(showHelp = true)
      case "--file" :: p :: t => go(t, acc.copy(sophieFile = Some(Paths.get(p))))
      case "--md" :: p :: t => go(t, acc.copy(mdFile = Some(Paths.get(p))))
      case "--print-instructions" :: t => go(t, acc.copy(printInstructions = true))
      case "--run" :: t => go(t, acc.copy(execute = true))
      case "--ledger" :: p :: t => go(t, acc.copy(ledgerPath = Some(Paths.get(p))))
      case "--portfolio" :: p :: t => go(t, acc.copy(portfolioPath = Some(Paths.get(p))))
      case "--receipt-file" :: p :: t => go(t, acc.copy(receiptFile = Some(p)))
      case "--source" :: s :: t => go(t, acc.copy(source = s))
      case "--reset-portfolio" :: t => go(t, acc.copy(resetPortfolio = true))
      case opt :: _ =>
        SLF4JLogger.error(s"Unknown option or missing argument: $opt")
        acc.copy(showHelp = true)
    }
    go(args, Config())
  }

  private def printHelp(): Unit = {
    println("Sophie CLI - usage:")
    println("  --file <path>             : path to .sophie source to evaluate")
    println("  --md <path>               : path to market data JSON (optional, default: demo)")
    println("  --print-instructions      : print lowered instructions as JSON")
    println("  --run                     : execute instructions (write ledger & update portfolio)")
    println("  --ledger <path>           : ledger file path (default: ledger.ndjson)")
    println("  --portfolio <path>        : portfolio file path (default: portfolio.json)")
    println("  --receipt-file <path>     : append textual receipt to this file (optional)")
    println("  --source <id>             : source id used to tag instructions (default: cli)")
    println("  --reset-portfolio         : if portfolio file exists, reset it without asking")
    println("  --help, -h                : show this help")
  }

  private def loadMdFromFile(p: Path): InMemoryMarketData = {
    val json = Files.readString(p)
    val mj = read[MarketDataJ](json)
    fromJ(mj)
  }

  private def loadDemoMd(): Option[InMemoryMarketData] = {
    // Try classpath resource first
    val in = Option(getClass.getResourceAsStream("/md_demo.json"))
    if (in.isDefined) {

      val json = scala.io.Source.fromInputStream(in.get).mkString
      val mj = read[MarketDataJ](json)
      Some(fromJ(mj))
    } else {

      // Fall back to project resources path
      val p = Paths.get("src/main/resources/md_demo.json")
      if (Files.exists(p)) Some(fromJ(read[MarketDataJ](Files.readString(p))))
      else None
    }

  }

  // Reset portfolio file to an empty portfolio JSON format used by the project
  private def resetPortfolioFile(p: Path): Unit = {
    try {
      val emptyPf = PortfolioJ(positions = Map.empty[String, BigDecimal], cash = Some(BigDecimal(0)))
      val empty = upickle.default.write(emptyPf, indent = 2)
      Files.writeString(p, empty)
    } catch { case e: Exception => SLF4JLogger.error(s"Error resetting portfolio file: ${e.getMessage}") }
  }

}
