package cli

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

import upickle.default._

import frontend.MdJsonCodec._
import frontend.SophieParserFacade
import frontend.PortfolioJson._
import engine.{InMemoryMarketData, Lowering, Executor, FileLedger, FileJsonPortfolioStore}
import engine._
import frontend.ReceiptPrinter

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
