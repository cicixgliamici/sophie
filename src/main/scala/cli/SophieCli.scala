package cli

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

import upickle.default._

import frontend.MdJsonCodec._
import frontend.SophieParserFacade
import engine.{InMemoryMarketData, Lowering, Executor, FileLedger, FileJsonPortfolioStore}
import engine._
import frontend.ReceiptPrinter

object SophieCli {

  def main(args: Array[String]): Unit = {
    val rawConfig = parseArgs(args.toList)
    // Minimal behaviour: if user provided only --file (no flags), assume they want to run using defaults
    val implicitRun = !rawConfig.printInstructions && !rawConfig.execute && rawConfig.sophieFile.isDefined
    val config = if (implicitRun)
      rawConfig.copy(execute = true)
    else rawConfig
    if (config.showHelp) { printHelp(); return }

    try {
      // 1) Load program
      val program = config.sophieFile match {
        case Some(p) => SophieParserFacade.parseFile(p)
        case None    =>
          System.err.println("No input file provided. Use --file <path> or --demo for the sample in resources.")
          sys.exit(2)
      }

      // Confirm if implicit run
      if (implicitRun) {
        Console.err.print("Esegui il programma e scrivi ledger/portfolio? [y/N]: ")
        val ans = scala.io.StdIn.readLine().trim.toLowerCase
        if (ans != "y" && ans != "yes") {
          println("Execution cancelled by user.")
          return
        }
      }

      // 2) Load market data (either provided JSON or demo bundled)
      val md: InMemoryMarketData = config.mdFile match {
        case Some(mdPath) => loadMdFromFile(mdPath)
        case None => loadDemoMd().getOrElse {
          System.err.println("No demo market data found")
          sys.exit(2)
          throw new IllegalStateException("unreachable")
        }
      }

      // 3) Evaluate program
      val plan = engine.Evaluator.evaluate(program, md)

      // Print a readable plan
      println("=== Execution Plan ===")
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
        val inst = Lowering.from(plan, md, config.source)
        println("\n=== Instructions (JSON) ===")
        println(write(inst, indent = 2))
      }

      // 5) Optionally execute
      if (config.execute) {
        val inst = Lowering.from(plan, md, config.source)
        val ledgerPath = config.ledgerPath.getOrElse(Paths.get("ledger.ndjson"))
        val portfolioPath = config.portfolioPath.getOrElse(Paths.get("portfolio.json"))
        val ledger = FileLedger(ledgerPath)
        val pfStore = FileJsonPortfolioStore(portfolioPath)
        val events = Executor.run(inst, md, pfStore, ledger, config.source)
        println(s"Executed ${inst.length} instructions; ledger -> $ledgerPath, portfolio -> $portfolioPath")
        val receiptPath = config.receiptFile.map(Paths.get(_))
        ReceiptPrinter.printReceipts(events, receiptPath)
      }

    } catch {
      case ex: Throwable =>
        System.err.println(s"Error: ${ex.getMessage}")
        ex.printStackTrace()
        System.exit(1)
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
                             source: String = "cli"
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
      case opt :: _ =>
        System.err.println(s"Unknown option or missing argument: $opt")
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

}

